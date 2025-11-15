import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# Load data
df = pd.read_csv("results/tabu2.csv")

# Extract instance size (everything before the last "_")
df["InstanceSize"] = df["Instance"].str.rsplit("_", n=1).str[0]

configs = ["TTT1", "TTT2", "TTT3", "TTT4"]

# Filter only TTT configs
df_filt = df[df["Configuration"].isin(configs)]

# Best (largest BestSol) row per (Instance, Configuration, Seed)
best_per_conf = (
    df_filt
    .sort_values(["Instance", "Configuration", "BestSol"], ascending=[True, True, False])
    .groupby(["Instance", "Configuration", "Seed"], as_index=False)
    .first()
)

# Mark whether that run achieved the target
best_per_conf["IsWinner"] = best_per_conf["BestSol"] >= best_per_conf["Target"]

# === One figure containing all ECDFs ===

plt.figure(figsize=(9, 6))

for cfg in configs:
    all_runs = best_per_conf[best_per_conf["Configuration"] == cfg]
    sub = all_runs[all_runs["IsWinner"]]

    if sub.empty:
        print(f"No successful runs for {cfg}, skipping...")
        continue

    # Sort times of successful runs
    times = np.sort(sub["TimeMS"].to_numpy())

    # ECDF Y-values: achieved / total runs
    y = np.arange(1, len(times) + 1) / len(all_runs)

    # Add the ECDF curve to the same plot
    plt.step(times, y, where="post", label=cfg)

plt.title("Time-To-Target (All Configurations)")
plt.xlabel("Time (ms)")
plt.ylabel("Cumulative Probability")
plt.grid(True)
plt.legend()
plt.tight_layout()

plt.savefig("out/ttt_plot2.png", dpi=150)
plt.close()
