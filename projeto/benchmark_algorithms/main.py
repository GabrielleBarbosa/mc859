# Quantum Network Simulation: REPS, Q-CAST, and Greedy Algorithms
# This script implements and compares entanglement routing algorithms based on the paper
# "Redundant Entanglement Provisioning and Selection for Throughput Maximization in Quantum Networks"
# by Zhao and Qiao (IEEE INFOCOM 2021).
#
# It loads a network topology, parameters, and SD pairs from a JSON file.
#
# --- DEPENDENCIES ---
# You must install the following libraries for this script to run:
# pip install numpy networkx pulp
#
# --- HOW TO RUN ---
# python quantum_network_simulation.py --file path/to/your/instance.json

import random
import math
import numpy as np
import networkx as nx
from pulp import LpProblem, LpMaximize, LpVariable, lpSum, value, PULP_CBC_CMD
import json
import argparse

# --- SIMULATION PARAMETERS ---
EXTERNAL_LINK_ALPHA = 0.0002  # System parameter for external link success probability

# --- 1. NETWORK TOPOLOGY HANDLING ---

def load_network_from_json(file_path):
    """
    Loads a network topology, parameters, and SD pairs from a JSON file.
    """
    with open(file_path, 'r') as f:
        data = json.load(f)

    G = nx.Graph()
    m_u = {}
    q_u = {}

    # Add nodes and their attributes
    for node_data in data['nodes']:
        node_id = node_data['id']
        G.add_node(node_id, pos=tuple(node_data['pos']))
        m_u[node_id] = node_data['memory_size']
        q_u[node_id] = node_data['internal_link_prob']

    # Add edges and their attributes
    c_uv_dict = {}
    for link_data in data['links']:
        u, v = link_data['source'], link_data['target']
        G.add_edge(u, v)
        # Store capacity for both directions of the undirected edge
        u_s,v_s = min(u,v), max(u,v)
        c_uv_dict[(u_s,v_s)] = link_data['num_channels']

    # Get SD pairs
    sd_pairs = [tuple(p) for p in data['graph']['sd_pairs']]
    
    # Calculate link success probabilities based on the loaded positions
    p_uv = get_link_success_prob(G, EXTERNAL_LINK_ALPHA)
    
    c_uv = c_uv_dict
    
    print(f"Loaded network from {file_path} with {G.number_of_nodes()} nodes and {G.number_of_edges()} edges.")
    
    return G, sd_pairs, c_uv, m_u, q_u, p_uv

def get_link_success_prob(G, alpha):
    """Calculates external link success probabilities based on distance."""
    probs = {}
    for u, v in G.edges():
        dist = np.linalg.norm(np.array(G.nodes[u]['pos']) - np.array(G.nodes[v]['pos']))
        prob = math.exp(-alpha * dist)
        probs[(u, v)] = prob
        probs[(v, u)] = prob
    return probs

# --- 2. ALGORITHMS ---

# 2.1. GREEDY ALGORITHM (Shortest Hop Path)
def greedy_algorithm(G, sd_pairs, e_uv, q_u):
    """
    Establishes connections using the fewest-hop paths.
    Does not consider link success probabilities.
    """
    # Use a dictionary for used links, handling undirected edges
    used_links = { (min(u, v), max(u, v)): 0 for u, v in G.edges() }
    
    established_paths = {pair: [] for pair in sd_pairs}
    
    # Create a copy of the graph based on available successful links (e_uv)
    graph_copy = nx.Graph()
    for (u,v), count in e_uv.items():
        if count > 0:
            graph_copy.add_edge(u,v)

    for s, d in sd_pairs:
        while True:
            try:
                # Find shortest path based on hops (unweighted)
                path = nx.shortest_path(graph_copy, source=s, target=d)
                
                # Check if swapping succeeds along the path
                swapping_ok = True
                for node in path[1:-1]: # intermediate nodes
                    if random.random() > q_u.get(node, 1.0):
                        swapping_ok = False
                        break # Swapping failed at this node
                
                if not swapping_ok:
                    # Can't use this path for this attempt, but links are not consumed
                    break

                # If successful, reserve resources and update graph
                established_paths[(s, d)].append(path)
                path_edges = list(zip(path[:-1], path[1:]))
                
                for u, v in path_edges:
                    u_s, v_s = min(u, v), max(u, v)
                    used_links[(u_s, v_s)] += 1
                    if used_links[(u_s, v_s)] >= e_uv.get((u_s,v_s), 0):
                        if graph_copy.has_edge(u_s, v_s):
                            graph_copy.remove_edge(u_s, v_s)
                
            except (nx.NetworkXNoPath, nx.NodeNotFound):
                break # No more paths for this S-D pair
                
    return established_paths

# 2.2. Q-CAST ALGORITHM (Most Reliable Path First)
def q_cast_algorithm(G, sd_pairs, e_uv, p_uv, q_u):
    """
    A simplified implementation of Q-CAST.
    Prioritizes paths with the highest end-to-end success probability.
    """
    used_links = {(min(u, v), max(u, v)): 0 for u, v in G.edges()}
    established_paths = {pair: [] for pair in sd_pairs}
    
    # Create a weighted graph where edge weights are -log(prob)
    reliable_graph = nx.Graph()
    for u, v in G.edges():
        u_s,v_s = min(u,v), max(u,v)
        if e_uv.get((u_s,v_s), 0) > 0:
            prob = p_uv.get((u,v), 0) * math.sqrt(q_u.get(u,1) * q_u.get(v,1))
            if prob > 1e-9: # Avoid log(0)
                reliable_graph.add_edge(u, v, weight=-math.log(prob))

    for s, d in sd_pairs:
        while True:
            try:
                # Find the most reliable path (shortest path in the log-prob graph)
                path = nx.shortest_path(reliable_graph, source=s, target=d, weight='weight')
                
                established_paths[(s, d)].append(path)
                path_edges = list(zip(path[:-1], path[1:]))
                
                for u, v in path_edges:
                    u_s, v_s = min(u, v), max(u, v)
                    used_links[(u_s, v_s)] += 1
                    if used_links.get((u_s, v_s), 0) >= e_uv.get((u_s, v_s), 0):
                        if reliable_graph.has_edge(u, v):
                            reliable_graph.remove_edge(u, v)
            
            except (nx.NetworkXNoPath, nx.NodeNotFound):
                break # No more paths for this S-D pair
                
    return established_paths

# 2.3. REPS ALGORITHM SUITE

def pft_algorithm(G, sd_pairs, c_uv, m_u, p_uv):
    """Provisioning for Fault Tolerance (PFT) - Phase 1 of REPS."""
    prob = LpProblem("PFT_Problem", LpMaximize)
    
    # Variables
    t = {i: LpVariable(f"t_{i}", 0) for i, _ in enumerate(sd_pairs)}
    f = {(i, u, v): LpVariable(f"f_{i}_{u}_{v}", 0) for i, _ in enumerate(sd_pairs) for u, v in G.edges}
    f.update({(i, v, u): LpVariable(f"f_{i}_{v}_{u}", 0) for i, _ in enumerate(sd_pairs) for u, v in G.edges})
    x = {(min(u, v), max(u, v)): LpVariable(f"x_{min(u, v)}_{max(u, v)}", 0) for u, v in G.edges}

    # Objective
    prob += lpSum(t)
    
    # Constraints
    for i, (s, d) in enumerate(sd_pairs):
        for u in G.nodes():
            in_flow = lpSum(f.get((i, v, u), 0) for v in G.neighbors(u))
            out_flow = lpSum(f.get((i, u, v), 0) for v in G.neighbors(u))
            if u == s:
                prob += out_flow - in_flow == t[i]
            elif u == d:
                prob += out_flow - in_flow == -t[i]
            else:
                prob += out_flow - in_flow == 0

    for u, v in G.edges():
        u_s, v_s = min(u, v), max(u, v)
        prob += lpSum(f.get((i, u, v), 0) + f.get((i, v, u), 0) for i, _ in enumerate(sd_pairs)) <= p_uv.get((u, v), 0) * x[u_s, v_s]
        prob += x[u_s, v_s] <= c_uv.get((u_s, v_s), 0)

    for u in G.nodes():
        prob += lpSum(x.get((min(u, v), max(u, v)), 0) for v in G.neighbors(u)) <= m_u[u]

    # Solve the relaxed LP
    prob.solve(PULP_CBC_CMD(msg=0))
    
    # Simple rounding
    x_uv_solution = {}
    for u, v in G.edges():
        u_s, v_s = min(u, v), max(u, v)
        flow_on_edge = sum(value(f.get((i, u, v), 0)) + value(f.get((i, v, u), 0)) for i, _ in enumerate(sd_pairs))
        if p_uv.get((u, v), 0) > 1e-9:
            required_attempts = math.ceil(flow_on_edge / p_uv.get((u, v), 0))
            x_uv_solution[(u_s, v_s)] = min(required_attempts, c_uv.get((u_s, v_s), 0))
        else:
            x_uv_solution[(u_s, v_s)] = 0
            
    return x_uv_solution

def eps_algorithm(G, sd_pairs, e_uv, t_hat):
    """Entanglement Path Selection (EPS) - Phase 3, Part 1 of REPS."""
    prob = LpProblem("EPS_Problem", LpMaximize)
    
    # Variables
    t_ki = {(i, k): LpVariable(f"t_{i}_{k}", 0, 1) for i, _ in enumerate(sd_pairs) for k in range(t_hat[i])}
    f_ki = {(i, k, u, v): LpVariable(f"f_{i}_{k}_{u}_{v}", 0, 1) for i,k in t_ki for u,v in G.edges()}
    f_ki.update({(i, k, v, u): LpVariable(f"f_{i}_{k}_{v}_{u}", 0, 1) for i,k in t_ki for u,v in G.edges()})
    
    # Objective
    prob += lpSum(t_ki)
    
    # Constraints
    for i, (s, d) in enumerate(sd_pairs):
        for k in range(t_hat[i]):
            for u in G.nodes():
                in_flow = lpSum(f_ki.get((i, k, v, u), 0) for v in G.neighbors(u))
                out_flow = lpSum(f_ki.get((i, k, u, v), 0) for v in G.neighbors(u))
                if u == s:
                    prob += out_flow - in_flow == t_ki[i,k]
                elif u == d:
                    prob += out_flow - in_flow == -t_ki[i,k]
                else:
                    prob += out_flow - in_flow == 0

    for u, v in G.edges():
         u_s, v_s = min(u,v), max(u,v)
         prob += lpSum(f_ki.get((i, k, u, v), 0) + f_ki.get((i, k, v, u), 0) for i, k in t_ki) <= e_uv.get((u_s, v_s), 0)

    prob.solve(PULP_CBC_CMD(msg=0))
    
    # Randomized Rounding
    ideal_paths = {pair: [] for pair in sd_pairs}
    for i, pair in enumerate(sd_pairs):
        for k in range(t_hat[i]):
            if random.random() < value(t_ki.get((i, k), 0)):
                path_graph = nx.DiGraph()
                for u, v in G.edges():
                    if value(f_ki.get((i, k, u, v), 0)) > 0.1: # Threshold to build graph
                        path_graph.add_edge(u, v)
                try:
                    path = nx.shortest_path(path_graph, source=pair[0], target=pair[1])
                    ideal_paths[pair].append(path)
                except (nx.NetworkXNoPath, nx.NodeNotFound):
                    continue
                    
    return ideal_paths

def els_algorithm(G, ideal_paths, e_uv, q_u):
    """Entanglement Link Selection (ELS) - Phase 3, Part 2 of REPS."""
    y_uv = {(min(u, v), max(u, v)): 0 for u, v in G.edges()}
    final_paths = {pair: [] for pair in ideal_paths.keys()}
    
    # Add node weights for Dijkstra
    for u in G.nodes():
        G.nodes[u]['weight'] = -math.log(q_u[u]) if q_u.get(u,0) > 1e-9 else float('inf')

    def path_cost(path):
        return sum(G.nodes[n].get('weight', 0) for n in path[1:-1])

    def is_feasible(path, y_uv, e_uv):
        for u, v in zip(path[:-1], path[1:]):
            u_s, v_s = min(u, v), max(u, v)
            if y_uv.get((u_s, v_s), 0) >= e_uv.get((u_s, v_s), 0):
                return False
        return True

    # Step 1: Use ideal paths from EPS
    candidates = {pair: sorted(paths, key=path_cost) for pair, paths in ideal_paths.items()}
    
    while any(candidates.values()):
        eligible_pairs = [p for p, paths in candidates.items() if paths]
        if not eligible_pairs:
            break
        
        min_paths_pair = min(eligible_pairs, key=lambda p: len(final_paths[p]))
        
        path_added_in_iteration = False
        if candidates.get(min_paths_pair):
            path_to_add = None
            for path in candidates[min_paths_pair]:
                if is_feasible(path, y_uv, e_uv):
                    path_to_add = path
                    break
            
            if path_to_add:
                final_paths[min_paths_pair].append(path_to_add)
                candidates[min_paths_pair].remove(path_to_add)
                for u, v in zip(path_to_add[:-1], path_to_add[1:]):
                    u_s, v_s = min(u, v), max(u, v)
                    y_uv[(u_s, v_s)] += 1
                path_added_in_iteration = True
        
        if not path_added_in_iteration:
            candidates.pop(min_paths_pair, None)
    
    return final_paths

def reps_pipeline(G, sd_pairs, c_uv, m_u, p_uv, q_u):
    """Executes the full REPS pipeline."""
    # Phase 1: Provisioning
    x_uv = pft_algorithm(G, sd_pairs, c_uv, m_u, p_uv)

    # Phase 2: Simulate Link Creation
    e_uv = {}
    for (u, v), attempts in x_uv.items():
        attempts_int = int(round(attempts)) # Ensure integer attempts
        successes = sum(1 for _ in range(attempts_int) if random.random() < p_uv.get((u, v), 0))
        u_s, v_s = min(u,v), max(u,v)
        e_uv[(u_s, v_s)] = successes

    # Estimate t_hat (max possible connections) for EPS
    t_hat = {i: max(10, len(sd_pairs) * 2) for i, _ in enumerate(sd_pairs)}

    # Phase 3: Selection
    ideal_paths = eps_algorithm(G, sd_pairs, e_uv, t_hat)
    final_paths = els_algorithm(G, ideal_paths, e_uv, q_u)

    return final_paths, e_uv

# --- 3. MAIN SIMULATION ---

def main():
    parser = argparse.ArgumentParser(description="Run Quantum Network Simulation on a JSON instance file.")
    parser.add_argument('--file', type=str, required=True, help='Path to the JSON network instance file.')
    args = parser.parse_args()

    print("--- Quantum Network Simulation ---")
    
    # 1. Setup Network from the file
    G, sd_pairs, c_uv, m_u, q_u, p_uv = load_network_from_json(args.file)

    # --- Run REPS ---
    print("\nRunning REPS Algorithm...")
    reps_paths, e_uv_for_others = reps_pipeline(G, sd_pairs, c_uv, m_u, p_uv, q_u)
    
    reps_throughput = sum(len(p) for p in reps_paths.values())
    reps_throughputs_per_pair = [len(p) for p in reps_paths.values()]
    reps_fairness = max(reps_throughputs_per_pair) - min(reps_throughputs_per_pair) if reps_throughputs_per_pair else 0
    
    # --- Run Q-CAST & Greedy on the *same* set of successfully created links for fair comparison ---
    print("Running Q-CAST Algorithm...")
    qcast_paths = q_cast_algorithm(G, sd_pairs, e_uv_for_others, p_uv, q_u)
    qcast_throughput = sum(len(p) for p in qcast_paths.values())
    qcast_throughputs_per_pair = [len(p) for p in qcast_paths.values()]
    qcast_fairness = max(qcast_throughputs_per_pair) - min(qcast_throughputs_per_pair) if qcast_throughputs_per_pair else 0

    print("Running Greedy Algorithm...")
    greedy_paths = greedy_algorithm(G, sd_pairs, e_uv_for_others, q_u)
    greedy_throughput = sum(len(p) for p in greedy_paths.values())
    greedy_throughputs_per_pair = [len(p) for p in greedy_paths.values()]
    greedy_fairness = max(greedy_throughputs_per_pair) - min(greedy_throughputs_per_pair) if greedy_throughputs_per_pair else 0

    # --- Print Final Results ---
    print("\n--- Simulation Results ---")
    print(f"Algorithm: REPS")
    print(f"  - Throughput: {reps_throughput} qbps (qubits per slot)")
    print(f"  - Fairness Gap: {reps_fairness}")

    print(f"\nAlgorithm: Q-CAST")
    print(f"  - Throughput: {qcast_throughput} qbps (qubits per slot)")
    print(f"  - Fairness Gap: {qcast_fairness}")

    print(f"\nAlgorithm: GREEDY")
    print(f"  - Throughput: {greedy_throughput} qbps (qubits per slot)")
    print(f"  - Fairness Gap: {greedy_fairness}")

if __name__ == "__main__":
    main()

