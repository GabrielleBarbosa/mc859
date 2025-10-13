"""
Abstract GRASP metaheuristic class
"""

import random
import time
from abc import ABC, abstractmethod
from typing import List, Generic, TypeVar, Optional

from .solution import Solution
from .evaluator import Evaluator

# Generic type for solution elements
E = TypeVar('E')


class AbstractGRASP(ABC, Generic[E]):
    """Abstract GRASP metaheuristic class"""
    
    verbose = True
    
    def __init__(self, obj_function: Evaluator[E], alpha: float, iterations: int, 
                 construction_method: str = "standard", local_search_method: str = "first_improving"):
        self.obj_function = obj_function
        self.alpha = alpha
        self.iterations = iterations
        self.construction_method = construction_method
        self.local_search_method = local_search_method
        self.best_cost = float('-inf')  # Maximization problem
        self.cost = float('-inf')
        self.best_sol: Optional[Solution[E]] = None
        self.sol: Optional[Solution[E]] = None
        self.CL: List[E] = []
        self.RCL: List[E] = []
        random.seed(0)  # For reproducibility
    
    @abstractmethod
    def make_CL(self) -> List[E]:
        """Create the Candidate List"""
        pass
    
    @abstractmethod
    def make_RCL(self) -> List[E]:
        """Create the Restricted Candidate List"""
        pass
    
    @abstractmethod
    def update_CL(self):
        """Update the Candidate List according to current solution"""
        pass
    
    @abstractmethod
    def create_empty_sol(self) -> Solution[E]:
        """Create an empty solution"""
        pass
    
    @abstractmethod
    def local_search(self) -> Solution[E]:
        """Perform local search optimization"""
        pass
    
    @abstractmethod
    def make_feasible(self):
        """Make solution feasible if needed"""
        pass
    
    def constructive_heuristic(self) -> Solution[E]:
        """GRASP constructive heuristic phase with different construction methods"""
        
        if self.construction_method == "random_plus_greedy":
            return self.random_plus_greedy_construction()
        elif self.construction_method == "sampled_greedy":
            return self.sampled_greedy_construction()
        elif self.construction_method == "pop_in_construction":
            return self.standard_constructive_heuristic(pop_in_construction=True)
        else:  # standard
            return self.standard_constructive_heuristic()
    
    def standard_constructive_heuristic(self, pop_in_construction=False) -> Solution[E]:
        """Standard GRASP constructive heuristic"""
        # percentages for local search, if pop_in_construction is True
        percentages_for_local_search = [0.4, 0.6, 0.8]
        
        self.CL = self.make_CL()
        self.RCL = self.make_RCL()
        self.sol = self.create_empty_sol()
        self.cost = float('-inf')
        
        # Main constructive loop
        while not self.constructive_stop_criteria():
            max_cost = float('-inf')
            min_cost = float('inf')
            self.cost = self.obj_function.evaluate(self.sol)
            self.update_CL()
            
            if not self.CL:  # No more candidates
                break

            # if pop in construction is selected, when reach certain percentages, make local search 
            if pop_in_construction:
                percentage = len(self.sol) / self.obj_function.get_domain_size()

                if percentage >= percentages_for_local_search[0]:
                    percentages_for_local_search.pop(0)
                    self.sol = self.local_search()
                    continue
            
            insertion_costs = []
            # Explore all candidates to find min and max costs
            for c in self.CL:
                delta_cost = self.obj_function.evaluate_insertion_cost(c, self.sol)
                insertion_costs.append(delta_cost)
                if delta_cost < min_cost:
                    min_cost = delta_cost
                if delta_cost > max_cost:
                    max_cost = delta_cost
            
            # Build RCL with candidates within alpha threshold
            self.RCL.clear()
            threshold = max_cost - self.alpha * (max_cost - min_cost)
            for i, c in enumerate(insertion_costs):
                if c >= threshold:  # For maximization
                    self.RCL.append(self.CL[i])
            
            # Choose random candidate from RCL
            if self.RCL:
                rnd_index = random.randint(0, len(self.RCL) - 1)
                in_cand = self.RCL[rnd_index]
                self.CL.remove(in_cand)
                self.sol.add(in_cand)
                self.obj_function.evaluate(self.sol)

        
        return self.sol
    
    def random_plus_greedy_construction(self) -> Solution[E]:
        """Random plus greedy construction method"""
        
        self.CL = self.make_CL()
        self.sol = self.create_empty_sol()
        
        # Phase 1: Random selection (5% of elements)
        num_random = max(1, int(0.05 * len(self.CL)))
        for _ in range(min(num_random, len(self.CL))):
            if not self.CL:
                break
            rnd_idx = random.randint(0, len(self.CL) - 1)
            selected = self.CL.pop(rnd_idx)
            self.sol.add(selected)
        
        # Phase 2: Greedy completion
        while not self.constructive_stop_criteria() and self.CL:
            self.update_CL()
            if not self.CL:
                break
                
            best_candidate = None
            best_cost = float('-inf')
            
            for c in self.CL:
                delta_cost = self.obj_function.evaluate_insertion_cost(c, self.sol)
                if delta_cost > best_cost:
                    best_cost = delta_cost
                    best_candidate = c
            
            if best_candidate is not None:
                self.CL.remove(best_candidate)
                self.sol.add(best_candidate)
                self.obj_function.evaluate(self.sol)
        
        return self.sol
    
    def sampled_greedy_construction(self) -> Solution[E]:
        """Sampled greedy construction method"""
        
        self.CL = self.make_CL()
        self.sol = self.create_empty_sol()
        sample_size = max(2, int(0.5 * len(self.CL)))  # Sample 50% of candidates
        
        while not self.constructive_stop_criteria() and self.CL:
            self.update_CL()
            if not self.CL:
                break
            
            # Sample candidates
            sample_size_current = min(sample_size, len(self.CL))
            sampled_candidates = random.sample(self.CL, sample_size_current)
            
            # Find best among sampled
            best_candidate = None
            best_cost = float('-inf')
            
            for c in sampled_candidates:
                delta_cost = self.obj_function.evaluate_insertion_cost(c, self.sol)
                if delta_cost > best_cost:
                    best_cost = delta_cost
                    best_candidate = c
            
            if best_candidate is not None:
                self.CL.remove(best_candidate)
                self.sol.add(best_candidate)
                self.obj_function.evaluate(self.sol)
        
        return self.sol
    
    def solve(self) -> Solution[E]:
        """Main GRASP procedure"""
        
        total_time_limit = 1800  # Total time limit in seconds
        start_total_time_counter = time.time()

        self.best_sol = self.create_empty_sol()
        
        for i in range(self.iterations):
            self.constructive_heuristic()
            
            # Ensure feasibility before local search
            if not self.obj_function.is_feasible(self.sol):
                self.make_feasible()
            
            self.local_search()
            
            if self.best_sol.cost < self.sol.cost:  # Maximization
                self.best_sol = Solution(self.sol)
                if self.verbose:
                    print(f"(Iter. {i}) BestSol = {self.best_sol}")
        
            if time.time() - start_total_time_counter > total_time_limit:
                print("Total time limit reached, stopping this instance.")
                self.iterations = i # set how many iterations actually run
                break

        return self.best_sol
    
    def constructive_stop_criteria(self) -> bool:
        """Stopping criteria for constructive heuristic"""
        return self.obj_function.is_feasible(self.sol)