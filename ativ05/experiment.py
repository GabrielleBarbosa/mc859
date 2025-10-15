import time
import os
import sys

# Add the project root to the Python path
sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))

from src.problems.sc_qbf.solvers.ts_sc_qbf import TS_SC_QBF


def main():    
    parent_dir = "instances/sc_qbf"
    filenames = sorted(os.listdir(parent_dir))
    filenames = [f"{parent_dir}/{f}" for f in filenames]

    results = []
    
    iterations = 10000  
    
    print("Running computational experiments...")
    print("=" * 60)
    
    configs = [
        ("FIRST_PROBABILISTIC", 0.2, "first_improving", "probabilistic"),
        ("BEST_PROBABILISTIC", 0.2, "best_improving", "probabilistic"),
    ]
    
    for filename in filenames:
        print("\n" + "-" * 60)
        print(f"File: {filename}")
        print("-" * 60)
        for config_name, tenure, local_search, strategy in configs:
            print(f"\nRunning {config_name}...")
            start_time = time.time()
            
            ts = TS_SC_QBF(
                tenure=tenure, 
                iterations=iterations, 
                filename=filename,
                strategy=strategy,
                search_method=local_search,
                timeout=30*60, 
                # target_value=-735.0,
                random_seed=1
            )
            
            best_sol = ts.solve()
            end_time = time.time()
            
            results.append({
                'file': filename.split("/")[-1],
                'config': config_name,
                'cost': best_sol.cost,
                'size': len(best_sol),
                'time': end_time - start_time,
                'iterations': ts.current_iter + 1,
                'feasible': ts.obj_function.is_feasible(best_sol),
            })
            
            print(f"Cost: {best_sol.cost}, Size: {len(best_sol)}, Iterations: {ts.current_iter + 1}, Time: {end_time - start_time:.3f}s")
            

    # Print results table
    print("\n" + "=" * 80)
    print("RESULTS SUMMARY")
    print("=" * 80)
    print(f"{'File':<20} {'Configuration':<15} {'Cost':<10} {'Size':<6} {'Time(s)':<8} {'Iterations':<10} {'Feasible':<10}")
    print("-" * 80)
    
    for result in results:
        print(f"{result['file']:<20} {result['config']:<15} {result['cost']:<10.2f} {result['size']:<6} "
              f"{result['time']:<8.3f} {result['iterations']:<10} {result['feasible']:<10}")
    
    return results

if __name__ == "__main__":
    main()