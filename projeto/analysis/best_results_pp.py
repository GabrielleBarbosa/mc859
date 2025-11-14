import pandas as pd

df = pd.read_csv("results/tabu.csv")

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
best_per_conf["InstanceSize"] = best_per_conf["Instance"].str.split("_").str[1]

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

result = (
    pd.concat([wins], axis=1)
    .fillna(0)
    .astype(int)
)

# restore groupby index → normal columns
result = result.reset_index()

# merge the mean time
result = result.merge(mean_time, on=["Configuration", "InstanceSize"]).merge(mean_iterations, on=["Configuration", "InstanceSize"])
result = result.sort_values(["InstanceSize", "Configuration"], ascending=[True, True])

result.to_csv("out/tabu_instance_stats_pp.csv", header=True, index=False)