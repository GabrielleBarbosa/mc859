import pandas as pd

# Read CSV
df = pd.read_csv("out/ga_results.csv")
df.columns = df.columns.str.strip()
df['Instance'] = df['Instance'].astype(str).str.strip()
df['Configuration'] = df['Configuration'].astype(str).str.strip()

results = []

for inst in df['Instance'].unique():
    df_inst = df[df['Instance'] == inst]

    if df_inst.empty:
        continue

    # Find best solution
    max_val = df_inst['BestSolution'].max()

    # Find minimum time among best solutions
    min_time = df_inst[df_inst['BestSolution'] == max_val]['Time_ms'].min()

    # Find all configurations that reached the best solution (ignore time)
    best_configs = df_inst[df_inst['BestSolution'] == max_val]['Configuration'].unique().tolist()

    results.append({
        'Instance': inst.removesuffix(".txt"),
        'BestSol': max_val,
        'Time_ms': min_time,
        'BestConfigs': ', '.join(best_configs)
    })

df_max = pd.DataFrame(results)
df_max.to_csv("out/best_results_by_instance.csv", index=False)
