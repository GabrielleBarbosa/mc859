import pandas as pd

df = pd.read_csv("results/tabu2.csv")

# Extract instance size (everything before the last _)
df["InstanceSize"] = df["Instance"].str.rsplit("_", n=1).str[0]

configs = ["TTT1", "TTT2", "TTT3", "TTT4"]
df_filt = df[df["Configuration"].isin(configs)]

# Best row per (Instance, Configuration)
best_per_conf = (
    df_filt
    .sort_values(["Instance", "Configuration", "BestSol"], ascending=[True, True, False])
    .groupby(["Instance", "Configuration", "Seed"], as_index=False)
    .first()   
)

best_per_conf["IsWinner"] = best_per_conf["BestSol"] >= best_per_conf["Target"]

# Wins and totals
wins = (
    best_per_conf[best_per_conf["IsWinner"]]
    .groupby(["Configuration", "Instance"])
    .size()
    .rename("Wins")
)

totals = (
    best_per_conf
    .groupby(["Configuration", "Instance"])
    .size()
    .rename("Total")
)

result = (
    pd.concat([wins, totals], axis=1)
    .fillna(0)
    .astype(int)
)

result["Losses"] = result["Total"] - result["Wins"]

# restore groupby index → normal columns
result = result.reset_index()

# merge the mean time
result = result.sort_values(["Instance", "Configuration"], ascending=[True, True])

result.to_csv("out/tabu_instance_stats_ttt.csv", header=True, index=False)