import time
import os
import sys

# Add the project root to the Python path
sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))

from src.problems.sc_qbf.solvers.ts_sc_qbf import TS_SC_QBF

def main():
    # Get the absolute path to the instance file
    instance_file = os.path.join(os.path.dirname(__file__), 'instances', 'sc_qbf', 'scqbf_025_1')
    
    start_time = time.time()
    tabusearch = TS_SC_QBF(20, 1000, instance_file, "best_improving", "probabilistic")
    best_sol = tabusearch.solve()
    print("maxVal =", best_sol)
    end_time = time.time()
    total_time = end_time - start_time
    print(f"Time = {total_time:.3f} seg")

if __name__ == "__main__":
    main()