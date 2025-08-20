import subprocess
import time
import os
import glob

def main():
    input_dir = "data"
    log_dir = "logs"
    os.makedirs(log_dir, exist_ok=True)

    files = sorted(glob.glob(os.path.join(input_dir, "*.txt")))

    #For each input file, run the main.py script and log the output.
    for file in files:
        base = os.path.basename(file).replace(".txt", "")
        log_file = os.path.join(log_dir, f"{base}.log")
        filename = os.path.splitext(os.path.basename(file))[0]

        print(f"Running main.py with input {filename}...")

        start = time.time()
        try:
            with open(file, "r") as fin, open(log_file, "w") as fout:
                subprocess.run(
                    ["python", "main.py"],
                    stdin=fin,
                    stdout=fout,
                    stderr=subprocess.STDOUT,
                    timeout=600  # 10 minutes
                )
            end = time.time()
            # Append the time taken to the log file
            with open(log_file, "a") as fout:
                fout.write(f"\n--- Finished in {end - start:.2f} seconds ---\n")
            print(f"Finished {filename} in {end - start:.2f} seconds\n")
        except subprocess.TimeoutExpired:
            with open(log_file, "a") as fout:
                fout.write("\n--- Execution timed out after 600 seconds ---\n")
            print(f"⏱️ Timeout: {file}")
        except Exception as e:
            with open(log_file, "a") as fout:
                fout.write(f"\n--- Error: {e} ---\n")
            print(f"❌ Error running {file}: {e}")


if __name__ == "__main__":
    main()
    print("All files processed. Check the logs directory for outputs.")
