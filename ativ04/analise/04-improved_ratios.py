import pandas as pd

df = pd.read_csv("out/merged_results_with_ga.csv")

results = []

for _, row in df.iterrows():
    instance = row['Instance']
    MS_GA = row['MS_GA']
    
    ga_tabu  = (MS_GA - row['MS_Tabu']) / row['MS_Tabu']
    ga_grasp = (MS_GA - row['MS_GRASP']) / row['MS_GRASP']
    ga_pli   = (MS_GA - row['MS_PLI']) / row['MS_PLI']
    
    results.append({
        "Instance": instance,
        "GA_Tabu": ga_tabu,
        "GA_GRASP": ga_grasp,
        "GA_PLI": ga_pli
    })

df_ratios = pd.DataFrame(results)

df_ratios = df_ratios.round(4)

df_ratios.to_csv("out/ga_improvement_ratios.csv", index=False)