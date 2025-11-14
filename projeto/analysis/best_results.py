import pandas as pd

df = pd.read_csv("results/tabu.csv")

# Extract instance size (everything before the last _)
df["InstanceSize"] = df["Instance"].str.rsplit("_", n=1).str[0]

configs = ["PP1", "PP2", "PP3", "PP4"]
df_filt = df[df["Configuration"].isin(configs)]

# Best row per (Instance, Configuration)
best_per_conf = (
    df_filt
    .sort_values(["Instance", "Configuration", "BestSol"], ascending=[True, True, False])
    .groupby(["Instance", "Configuration"], as_index=False)
    .first()   # NO reset_index()
)

# Correct extraction of instance size again
best_per_conf["InstanceSize"] = best_per_conf["Instance"].str.rsplit("_", n=2).str[0]

# Mean time per config + instance size
mean_time = (
    best_per_conf
    .groupby(["Configuration", "InstanceSize"])["TimeMS"]
    .mean()
    .reset_index(name="MeanTimeMS")
)

# Mean iterations per config + instance size
mean_iterations = (
    best_per_conf
    .groupby(["Configuration", "InstanceSize"])["Iteration"]
    .mean()
    .reset_index(name="MeanIteratios")
)

# Best global sol per instance
best_global = (
    best_per_conf
    .groupby("Instance")["BestSol"]
    .max()
    .reset_index()
    .rename(columns={"BestSol": "BestSolGlobal"})
)

merged = best_per_conf.merge(best_global, on="Instance")
merged["IsWinner"] = merged["BestSol"] == merged["BestSolGlobal"]

# Wins and totals
wins = (
    merged[merged["IsWinner"]]
    .groupby(["Configuration", "InstanceSize"])
    .size()
    .rename("Wins")
)

totals = (
    merged
    .groupby(["Configuration", "InstanceSize"])
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
result = result.merge(mean_time, on=["Configuration", "InstanceSize"]).merge(mean_iterations, on=["Configuration", "InstanceSize"])
result = result.sort_values(["InstanceSize", "Configuration"], ascending=[True, True])

result.to_csv("out/tabu_instance_stats.csv", header=True, index=False)