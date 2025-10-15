import time
import os
import sys

from src.problems.sc_qbf.solvers.ts_sc_qbf import TS_SC_QBF

def main():
    # Get the absolute path to the instance file
    instance_file = os.path.join('./instances', 'sc_qbf', 'scqbf_100_1')
    
    start_time = time.time()
    tabusearch = TS_SC_QBF(0.3, 1000, instance_file, "first_improving", "diversification_by_restart")
    best_sol = tabusearch.solve()
    print("maxVal =", best_sol)
    end_time = time.time()
    total_time = end_time - start_time
    print(f"Time = {total_time:.3f} seg")

if __name__ == "__main__":
    main()