import pandas as pd
import os

input_file = "data/ga_results.csv"

replace_config_dict = {
    'STANDARD': 'Padrão',
    'STANDARD_P2': 'Padrão+P2',
    'STANDARD_M2': 'Padrão+M2',
    'FUNC_MUT': 'FuncMut',
    'FUNC_POP': 'FuncPop',
    'STANDARD_EVOL1': 'Padrão+Alt1',
    'POP_MUT_EVOL1': 'Pop+Mut+Alt1',
    'STANDARD_EVOL_2': 'Padrão+Alt2',
    'POP_EVOL_2': 'Pop+Alt2',
    'STANDARD_EVOL_3': 'Padrão+Alt3',
    'POP_EVOL_3': 'Pop+Alt3',
    'MUT_EVOL_3': 'Mut+Alt3',
}

replace_stop_dict = {
    'TIME_LIMIT': 'Limite de Tempo',
    'MAX_GENERATIONS': 'Máximo de gerações',
    'NO_IMPROVEMENT': 'Sem Melhoria',
}

df = pd.read_csv(input_file)
df['Configuration'] = df['Configuration'].replace(replace_config_dict)
df['StopReason'] = df['StopReason'].replace(replace_stop_dict)
df['Instance'] = df['Instance'].str.replace('.txt', '', regex=False)

df.to_csv("out/ga_results.csv", index=False)

