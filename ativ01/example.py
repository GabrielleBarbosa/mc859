import gurobipy as gp
from gurobipy import GRB

sizes, demand =  gp.multidict(
    {
        1.5: 150,
        2.1: 200,
        2.7: 300,
    }
)

cuts, cutWaste = gp.multidict(
    {
        "a": 1.2,
        "b": 0.9,
        "c": 0.3,
    }
)

cutSizeQt = {
    ("a", 1.5): 0,
    ("a", 2.1): 1,
    ("a", 2.7): 1,
    ("b", 1.5): 2,
    ("b", 2.1): 1,
    ("b", 2.7): 0,
    ("c", 1.5): 2,
    ("c", 2.1): 0,
    ("c", 2.7): 1,
}

m = gp.Model("cutting-stock")
use = m.addVars(cuts, name="use")

m.setObjective(use.prod(cutWaste), GRB.MINIMIZE)

m.addConstrs(
    (
        gp.quicksum(cutSizeQt[c, s] * use[c] for c in cuts)
        >= demand[s]
        for s in sizes
    ),
    "size_demand",
)

def printSolution():
    if m.status == GRB.OPTIMAL:
        print(f"\nWaste: {m.ObjVal:g}")
        print("\nUse:")
        for c in cuts:
            print(f"{c} {use[c].X:g}")
    else:
        print("No solution")


m.optimize()
printSolution()