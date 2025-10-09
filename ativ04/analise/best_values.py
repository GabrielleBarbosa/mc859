import pandas as pd

df = pd.read_csv("../GA-Framework/ga_results.csv")  

instances = df['Instance'].unique()
methods = df['Configuration'].unique()

results = []

for inst in instances:
    df_inst = df[df['Instance'] == inst] 
    max_val = df_inst['BestSolution'].max()  
    min_time = df_inst[df_inst['BestSolution'] == max_val]['Time_ms'].min()
    
    line = {'Instance': str(inst).removesuffix(".txt"), "BestSol": max_val, 'Time_ms': min_time}
    for method in methods:
        line[method] = 1 if df_inst[df_inst['Configuration'] == method]['BestSolution'].max() == max_val else 0
    
    results.append(line)

df_max = pd.DataFrame(results)

df_max.to_csv("data/best_results_by_instance.csv", index=False)

