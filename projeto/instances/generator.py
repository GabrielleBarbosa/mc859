import networkx as nx
import numpy as np
import json
import os
import matplotlib.pyplot as plt
from scipy.spatial.distance import pdist, squareform

class NumpyEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, np.integer):
            return int(obj)
        elif isinstance(obj, np.floating):
            return float(obj)
        elif isinstance(obj, np.ndarray):
            return obj.tolist()
        return json.JSONEncoder.default(self, obj)

def generate_quantum_topology(
    num_nodes: int = 100,
    area_side_km: float = 10000.0,
    gamma: float = 0.9,
    beta: float = 0.1,  
    alpha: float = 0.0002,
    min_memory: int = 10,
    max_memory: int = 14,
    internal_link_prob: float = 0.9,
    num_channels_per_edge: int = 3,
    num_sd_pairs: int = 6
) -> nx.Graph:
    """
    Generates a quantum network topology based on the Waxman model and other specified parameters.
    """
    G = nx.Graph()

    positions = np.random.rand(num_nodes, 2) * area_side_km
    for i in range(num_nodes):
        G.add_node(
            i,
            pos=tuple(positions[i]),
            memory_size=np.random.randint(min_memory, max_memory + 1),
            internal_link_prob=internal_link_prob
        )

    dist_matrix = squareform(pdist(positions))
    L = np.max(dist_matrix)

    for u in range(num_nodes):
        for v in range(u + 1, num_nodes):
            l_uv = dist_matrix[u, v]
            p_waxman = gamma * np.exp(-l_uv / (beta * L))

            if np.random.rand() < p_waxman:
                p_external_link = np.exp(-alpha * l_uv)
                G.add_edge(
                    u,
                    v,
                    length_km=round(l_uv, 2),
                    external_link_prob=round(p_external_link, 4),
                    num_channels=num_channels_per_edge
                )

    if G.number_of_nodes() > 0:
        node_list = list(G.nodes())
        num_nodes_for_pairs = min(len(node_list), num_sd_pairs * 2)
        if num_nodes_for_pairs % 2 != 0:
            num_nodes_for_pairs -= 1

        if num_nodes_for_pairs > 0:
            selected_nodes = np.random.choice(
                node_list,
                size=num_nodes_for_pairs,
                replace=False
            )
            sd_pairs = list(zip(selected_nodes[::2], selected_nodes[1::2]))
            G.graph['sd_pairs'] = sd_pairs
        else:
            G.graph['sd_pairs'] = []
    else:
        G.graph['sd_pairs'] = []

    return G

if __name__ == "__main__":
    os.makedirs("data", exist_ok=True)

    for i in range(20):
        quantum_network = generate_quantum_topology()
    
        graph_data = nx.node_link_data(quantum_network)
        
        file_path = os.path.join("data", f"instance_{i}.json")
        with open(file_path, 'w') as f:
            json.dump(graph_data, f, indent=4, cls=NumpyEncoder)

        print(f"✅ Instance {i} generated successfully!")
        print(f"  Nodes: {quantum_network.number_of_nodes()}")
        print(f"  Edges: {quantum_network.number_of_edges()}")
        print(f"  Saved to: {file_path}")

    print("\n--- All instances generated ---")


    quantum_network_to_plot = generate_quantum_topology()

    plt.figure(figsize=(10, 10))
    node_positions = nx.get_node_attributes(quantum_network_to_plot, 'pos')
    nx.draw(
        quantum_network_to_plot,
        pos=node_positions,
        with_labels=True,
        node_size=150,
        font_size=8,
        node_color='skyblue'
    )
    plt.title("Generated Quantum Network Topology (Waxman Model)")
    plt.xlabel("Distance (km)")
    plt.ylabel("Distance (km)")
    plt.grid(True)
    plt.show()