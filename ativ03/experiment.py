import time
import os
import sys

# Add the project root to the Python path
sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))

from src.problems.sc_qbf.solvers.ts_sc_qbf import TS_SC_QBF


def main():
    """Run computational experiments as specified in the activity"""
    
    parent_dir = "instances/sc_qbf"
    filenames = sorted(os.listdir(parent_dir))
    filenames = [f"{parent_dir}/{f}" for f in filenames]

    results = []
    
    tenures = [0.3, 20]  
    iterations = 1000  
    
    print("Running computational experiments...")
    print("=" * 60)
    
    configs = [
        ("PADRÃO", tenures[0], "first_improving", "standard"),
        ("PADRÃO+BEST", tenures[0], "best_improving", "standard"),
        ("PADRÃO+TENURE", tenures[1], "first_improving", "standard"),
        ("PADRÃO+METHOD1", tenures[0], "first_improving", "probabilistic"),
        ("PADRÃO+METHOD2", tenures[0], "first_improving", "diversification_by_restart"),
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
                search_method=local_search
            )
            
            best_sol = ts.solve()
            end_time = time.time()
            
            results.append({
                'config': config_name,
                'cost': best_sol.cost,
                'size': len(best_sol),
                'time': end_time - start_time,
                'iterations': ts.iterations,
                'feasible': ts.obj_function.is_feasible(best_sol),
            })
            
            print(f"Cost: {best_sol.cost}, Size: {len(best_sol)}, Iterations: {ts.iterations}, Time: {end_time - start_time:.3f}s")
            

    # Print results table
    print("\n" + "=" * 80)
    print("RESULTS SUMMARY")
    print("=" * 80)
    print(f"{'Configuration':<15} {'Cost':<10} {'Size':<6} {'Time(s)':<8} {'Iterations':<10} {'Feasible':<10}")
    print("-" * 80)
    
    for result in results:
        print(f"{result['config']:<15} {result['cost']:<10.2f} {result['size']:<6} "
              f"{result['time']:<8.3f} {result['iterations']:<10} {result['feasible']:<10}")
    
    return results

if __name__ == "__main__":
    main()