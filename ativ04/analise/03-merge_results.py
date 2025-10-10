import pandas as pd

old_results = pd.read_csv("data/other_methods_results.csv")
ga_results = pd.read_csv("out/best_results_by_instance.csv")

ga_results['T_GA'] = ga_results['Time_ms'] / 1000.0
ga_results['MS_GA'] = ga_results['BestSol']

ga_subset = ga_results[['Instance', 'MS_GA', 'T_GA']]

# merged_df = pd.merge(ga_subset, old_results[['Instance', 'MS_PLI', 'T_PLI']], on='Instance', how='left')
merged_df = pd.merge(ga_subset, old_results, on='Instance', how='left')

merged_df.to_csv("out/merged_results_with_ga.csv", index=False)

