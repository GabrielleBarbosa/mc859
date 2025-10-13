"""
Main execution script for GRASP MAX-SC-QBF
"""

import sys
import os
import time

# Adicionar o diretório atual ao path para importar os módulos
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

# Importar os módulos locais
from algorithms.grasp_qbf_sc import GRASP_QBF_SC
from utils.instance_generator import create_sample_instance


def run_experiments():
    """Run computational experiments as specified in the activity"""
    
    parent_dir = "../../instances/15_qbf_sc_instances"
    filenames = sorted(os.listdir(parent_dir))
    filenames = [f"{parent_dir}/{f}" for f in filenames]

    results = []
    
    # Configuration parameters
    alphas = [0.1, 0.3]  # α₁ and α₂
    iterations = 1000  # Reduced for testing

    
    print("Running computational experiments...")
    print("=" * 60)
    
    # Base configuration
    alpha1 = alphas[0]
    
    configs = [
        ("PADRÃO", alpha1, "first_improving", "standard"),
        ("PADRÃO+ALPHA", alphas[1], "first_improving", "standard"),
        ("PADRÃO+BEST", alpha1, "best_improving", "standard"),
        ("PADRÃO+HC1", alpha1, "first_improving", "random_plus_greedy"),
        ("PADRÃO+HC2", alpha1, "first_improving", "sampled_greedy"),
        ("PADRÃO+HC3", alpha1, "first_improving", "pop_in_construction"),
    ]
    
    for filename in filenames:
        print("\n" + "-" * 60)
        print(f"File: {filename}")
        print("-" * 60)
        for config_name, alpha, local_search, construction in configs:
            print(f"\nRunning {config_name}...")
            start_time = time.time()
            
            grasp = GRASP_QBF_SC(
                alpha=alpha, 
                iterations=iterations, 
                filename=filename,
                construction_method=construction,
                local_search_method=local_search
            )
            
            best_sol = grasp.solve()
            end_time = time.time()
            
            results.append({
                'config': config_name,
                'cost': best_sol.cost,
                'size': len(best_sol),
                'time': end_time - start_time,
                'iterations': grasp.iterations,
                'feasible': grasp.obj_function.is_feasible(best_sol),
            })
            
            print(f"Cost: {best_sol.cost}, Size: {len(best_sol)}, Iterations: {grasp.iterations}, Time: {end_time - start_time:.3f}s")
            

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


def main():
    """Main function"""
    
    if len(sys.argv) > 1:
        if sys.argv[1] == "--experiment":
            run_experiments()
            return
        
        filename = sys.argv[1]
        alpha = float(sys.argv[2]) if len(sys.argv) > 2 else 0.1
        iterations = int(sys.argv[3]) if len(sys.argv) > 3 else 100
        construction = sys.argv[4] if len(sys.argv) > 4 else "standard"
        local_search = sys.argv[5] if len(sys.argv) > 5 else "first_improving"
    else:
        # Default test
        filename = create_sample_instance()
        alpha = 0.1
        iterations = 50
        construction = "standard"
        local_search = "first_improving"
    
    print(f"Running GRASP for MAX-SC-QBF")
    print(f"File: {filename}")
    print(f"Alpha: {alpha}, Iterations: {iterations}")
    print(f"Construction: {construction}, Local Search: {local_search}")
    print("-" * 60)
    
    start_time = time.time()
    grasp = GRASP_QBF_SC(
        alpha=alpha, 
        iterations=iterations, 
        filename=filename,
        construction_method=construction,
        local_search_method=local_search
    )
    
    best_sol = grasp.solve()
    end_time = time.time()
    
    print(f"\nFinal Result:")
    print(f"Best Solution: {best_sol}")
    print(f"Feasible: {grasp.obj_function.is_feasible(best_sol)}")
    print(f"Execution Time: {end_time - start_time:.3f} seconds")


if __name__ == "__main__":
    main()