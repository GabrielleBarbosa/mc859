import subprocess
import time
import os
import glob
import signal

def main():
    input_dir = "data"
    log_dir = "logs"
    os.makedirs(log_dir, exist_ok=True)

    files = sorted(glob.glob(os.path.join(input_dir, "gen_050*.txt")))

    #For each input file, run the main.py script and log the output.
    for file in files:
        base = os.path.basename(file).replace(".txt", "")
        log_file = os.path.join(log_dir, f"{base}.log")

        print(f"Running main.py with input {file}...")

        start = time.time()
        try:
            with open(file, "r") as fin, open(log_file, "w") as fout:
                proc = subprocess.Popen(
                    ["python", "main.py"],
                    stdin=fin,
                    stdout=fout,
                    stderr=subprocess.STDOUT,
                )
                try:
                    proc.wait(timeout=600)  # 10 minutes
                    end = time.time()
                    with open(log_file, "a") as fout:
                        fout.write(f"\n--- Finished in {end - start:.2f} seconds ---\n")
                    print(f"Finished {file} in {end - start:.2f} seconds\n")
                except subprocess.TimeoutExpired:
                    fout.write("\n--- Execution timed out after 600 seconds ---\n")
                    # send SIGINT to stop the program
                    proc.send_signal(signal.SIGINT)
                    try:
                        proc.wait(timeout=5)  # give it some time to clean up
                    except subprocess.TimeoutExpired:
                        fout.write("\n--- Force killing process ---\n")
                        proc.kill()
                    print(f"⏱️ Timeout: {file}")
        except Exception as e:
            with open(log_file, "a") as fout:
                fout.write(f"\n--- Error: {e} ---\n")
            print(f"❌ Error running {file}: {e}")

        


if __name__ == "__main__":
    main()
    print("All files processed. Check the logs directory for outputs.")

