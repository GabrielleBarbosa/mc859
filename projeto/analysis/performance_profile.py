import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

df_tabu = pd.read_csv("results/tabu.csv")
df_tabu = df_tabu[df_tabu["Configuration"] == "PP1"]

tabu_best = (
    df_tabu
    .sort_values(["Instance", "BestSol"], ascending=[True, False])
    .groupby("Instance", as_index=False)
    .first()[["Instance", "BestSol"]]
)

df_gurobi = pd.read_csv("results/gurobi.csv")
df_gurobi["Instance"] = df_gurobi["instance"].str.replace(".json", "", regex=False)
gurobi_best = df_gurobi[["Instance", "result"]].rename(columns={"result": "GurobiSol"})

merged = tabu_best.merge(gurobi_best, on="Instance", how="inner")

merged["BestOverall"] = merged[["BestSol", "GurobiSol"]].max(axis=1)

merged["tau_pp1"] = merged["BestOverall"] / merged["BestSol"]
merged["tau_gurobi"] = merged["BestOverall"] / merged["GurobiSol"]

plt.figure(figsize=(9, 6))

for col, label in [("tau_pp1", "PP1"), ("tau_gurobi", "Gurobi")]:
    taus = np.sort(merged[col].values)
    y = np.arange(1, len(taus) + 1) / len(taus)
    plt.step(taus, y, where="post", label=label)

plt.xlabel(r"Performance Ratio $\tau$")
plt.ylabel("Fraction of Instances")
plt.title("Performance Profile — PP1 vs Gurobi")
plt.grid(True)
plt.legend()
plt.tight_layout()
plt.savefig("out/performance_profile.png", dpi=150)
plt.close()
