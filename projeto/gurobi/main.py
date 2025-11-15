import json
import os
import time
import csv
from gurobipy import Model, GRB, quicksum

def solve_instance(filename: str):
    with open(filename, 'r') as f:
        data = json.load(f)

    nodes_data = data["nodes"]
    links_data = data["links"]
    requests_data = data["graph"]["sd_pairs"]

    V = [node["id"] for node in nodes_data]
    R = [(r[0], r[1]) for r in requests_data]

    directed = data.get("directed", False)
    A = []
    P = {}
    C = {}
    for link in links_data:
        u, v = link["source"], link["target"]
        prob = link["external_link_prob"]
        cap = link["num_channels"]

        A.append((u, v))
        P[(u, v)] = prob
        C[(u, v)] = cap

        if not directed:
            A.append((v, u))
            P[(v, u)] = prob
            C[(v, u)] = cap

    M = {node["id"]: node["memory_size"] for node in nodes_data}

    model = Model("QuantumRouting")

    x = model.addVars(
        [(r, a) for r in range(len(R)) for a in A],
        vtype=GRB.INTEGER, lb=0, name="x"
    )
    z = model.addVars(A, vtype=GRB.INTEGER, lb=0, name="z")
    t = model.addVars(range(len(R)), vtype=GRB.INTEGER, lb=0, name="t")

    model.setObjective(quicksum(t[r] for r in range(len(R))), GRB.MAXIMIZE)

    for r, (r_s, r_d) in enumerate(R):
        for v in V:
            inflow = quicksum(x[r, (u, v)] for u in V if (u, v) in A)
            outflow = quicksum(x[r, (v, u)] for u in V if (v, u) in A)

            if v == r_s:
                model.addConstr(inflow - outflow == -t[r], name=f"flow_src[{r},{v}]")
            elif v == r_d:
                model.addConstr(inflow - outflow == t[r], name=f"flow_dst[{r},{v}]")
            else:
                model.addConstr(inflow - outflow == 0, name=f"flow_mid[{r},{v}]")

    for (u, v) in A:
        model.addConstr(
            quicksum(
                (x[r, (u, v)] if (u, v) in A else 0)
                for r in range(len(R))
            )
            <= z[(u, v)],
            name=f"link_reserve[{u},{v}]"
        )

    for a in A:
        model.addConstr(z[a] <= C[a], name=f"capacity[{a}]")

    for v in V:
        model.addConstr(
            quicksum(z[a] for a in A if a[1] == v) <= M[v],
            name=f"memory[{v}]"
        )

    for v in V:
        model.addConstr(
            quicksum(z[a] for a in A if a[0] == v) <= M[v],
            name=f"memory[{v}]"
        )

    model.setParam('TimeLimit', 300)
    model.optimize()

    is_optimal = model.status == GRB.OPTIMAL
    if is_optimal:
        print(f"\nValor ótimo: {model.objVal}\n")
    else:
        print(f"\nNão atingiu valor ótimo, melhor: {model.objVal}\n")

    return model.objVal, is_optimal


if __name__ == "__main__":
    instances_path = "instances/data"
    instances = os.listdir(instances_path)

    with open('gurobi_results.csv', 'w', newline='') as csvfile:
        csv_writer = csv.writer(csvfile)
        csv_writer.writerow(['instance', 'result', 'time', 'is_optimal'])

        for i in sorted(instances):
            instance_path = os.path.join(instances_path, i)
            start = time.time()
            print(f"Running instance {i}")
            result, is_optimal = solve_instance(instance_path)
            end = time.time()
            total_time = end - start
            print(f"End instance {i} in {total_time} seconds")

            csv_writer.writerow([i, result, total_time, is_optimal])
            csvfile.flush()
