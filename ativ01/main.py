import gurobipy as gp
from gurobipy import GRB

def printSolution(m: gp.Model, n: int, x):
    if m.status == GRB.OPTIMAL:
        print(f"\nResult: {m.ObjVal:g}\n")
        x_print = [x[i].X for i in range(n)]
        print("X:", x_print)
    else:
        print("No solution")
        
def main():
    n = int(input())
    _ = list(map(int, input().split()))
    s = []
    for _ in range(n):
        s.append(list(map(int, input().split())))

    A = []
    for i in range(n):
        _a = list(map(int, input().split()))
        a = [0 for _ in range(i)] + _a
        A.append(a)

    model = gp.Model("max-sc-qbf")   
    x = model.addVars(n, vtype=GRB.BINARY, name="x")
    y = model.addVars(n, n, vtype=GRB.BINARY, name="y")


    model.setObjective(
        sum(A[i][j] * y[i,j] for i in range(n) for j in range(n)),
        GRB.MAXIMIZE
    )

    for i in range(n):
        for j in range(n):
            model.addConstr(y[i,j] <= x[i])
            model.addConstr(y[i,j] <= x[j])
            model.addConstr(y[i,j] >= x[i] + x[j] - 1)

    for k in range(1, n+1):
        indexes = []
        for i in range(n):
            if k in s[i]:
                indexes.append(i)
        model.addConstr(gp.quicksum(x[i] for i in indexes) >= 1)

    print("Vars:", model.NumVars)
    print("Constraints:", model.NumConstrs)

    model.optimize()
    printSolution(model, n, x)

if __name__=='__main__':
    main()