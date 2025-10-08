import gurobipy as gp
from gurobipy import GRB

def printSolution(m: gp.Model, n: int, x):
    """Prints the solution of the model if it is optimal."""
    if m.status == GRB.OPTIMAL:
        print(f"\nResult: {m.ObjVal:g}\n")
        x_print = [x[i].X for i in range(n)]
        print("X:", x_print)
    else:
        print("No solution")
        
def main():
    # Number of variables
    n = int(input())

    # Number of elements in each set
    _ = list(map(int, input().split()))

    # List of sets
    s = []

    # Read the sets
    for _ in range(n):
        s.append(list(map(int, input().split())))

    # Read the coefficient matrix, which is upper triangular
    A = []
    for i in range(n):
        _a = list(map(float, input().split()))
        a = [0 for _ in range(i)] + _a
        A.append(a)

    # Create the model
    model = gp.Model("max-sc-qbf")   
    x = model.addVars(n, vtype=GRB.BINARY, name="x")
    pairs = [(i, j) for i in range(n) for j in range(i, n)]
    y = model.addVars(pairs, vtype=GRB.BINARY, name="y")


    model.setObjective(
        sum(A[i][j] * y[i,j] for i in range(n) for j in range(i, n)),
        GRB.MAXIMIZE
    )

    # Constraints
    for i in range(n):
        for j in range(i, n):
            model.addConstr(y[i,j] <= x[i])
            model.addConstr(y[i,j] <= x[j])
            model.addConstr(y[i,j] >= x[i] + x[j] - 1)

    # Add the Set Cover constraints
    for k in range(1, n+1):
        indexes = []
        for i in range(n):
            if k in s[i]:
                indexes.append(i)
        model.addConstr(gp.quicksum(x[i] for i in indexes) >= 1)

    model.setParam('TimeLimit', 600)
    model.optimize()
    printSolution(model, n, x)

if __name__=='__main__':
    main()