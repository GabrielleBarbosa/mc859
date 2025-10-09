import pandas as pd

df = pd.read_csv("data/comparation_results.csv")  


df['GA_vs_Tabu'] = (df['MS_{GA}'] - df['MS_{tabu}']) / df['MS_{tabu}']
df['GA_vs_GRASP'] = (df['MS_{GA}'] - df['MS_{GRASP}']) / df['MS_{GRASP}']
df['GA_vs_PLI'] = (df['MS_{GA}'] - df['MS_{PLI}']) / df['MS_{PLI}']

df['GA_vs_Tabu'] = df['GA_vs_Tabu'].round(4)
df['GA_vs_GRASP'] = df['GA_vs_GRASP'].round(4)
df['GA_vs_PLI'] = df['GA_vs_PLI'].round(4)

df_out = df[['Instância', 'GA_vs_Tabu', 'GA_vs_GRASP', 'GA_vs_PLI']]

df_out.to_csv("data/improvement_ga.csv", index=False)

