import pandas as pd
import os

input_file = "out/ga_results.csv"

instances_to_split = [
    "n50p2", "n200p1", "n400p3"
]

output_dir = "out/instance_results/"
os.makedirs(output_dir, exist_ok=True)

df = pd.read_csv(input_file)

for instance in instances_to_split:
    df_instance = df[df["Instance"] == instance]
    df_instance = df_instance.drop(columns=["Instance"])

    df_instance["Time_ms"] = df_instance["Time_ms"] / 1000

    output_path = os.path.join(output_dir, f"{instance}.csv")
    df_instance.to_csv(output_path, index=False)


