"""
GRASP (Greedy Randomized Adaptive Search Procedure) Implementation in Python
Converted from Java implementation for solving combinatorial optimization problems
"""

import random
import copy
from abc import ABC, abstractmethod
from typing import List, Generic, TypeVar, Optional
import time

# Generic type for solution elements
E = TypeVar('E')


class Solution(Generic[E]):
    """Solution class that extends a list to store elements and cost"""
    
    def __init__(self, elements_or_solution = None, cost: float = None):
        if isinstance(elements_or_solution, Solution):
            # Copy constructor
            self.elements = copy.deepcopy(elements_or_solution.elements)
            self.cost = elements_or_solution.cost
        elif elements_or_solution is not None:
            # Constructor with elements and cost
            self.elements = elements_or_solution if elements_or_solution is not None else []
            self.cost = cost if cost is not None else float('inf')
        else:
            # Default constructor
            self.elements = []
            self.cost = float('inf')
    
    def add(self, element: E):
        """Add element to solution"""
        self.elements.append(element)
    
    def remove(self, element: E):
        """Remove element from solution"""
        if element in self.elements:
            self.elements.remove(element)
    
    def __contains__(self, element: E):
        """Check if element is in solution"""
        return element in self.elements
    
    def __iter__(self):
        """Make solution iterable"""
        return iter(self.elements)
    
    def __len__(self):
        """Get solution size"""
        return len(self.elements)
    
    def is_empty(self):
        """Check if solution is empty"""
        return len(self.elements) == 0
    
    def size(self):
        """Get solution size"""
        return len(self.elements)
    
    def __str__(self):
        return f"Solution: cost=[{self.cost}], size=[{len(self.elements)}], elements={self.elements}"


class Evaluator(ABC, Generic[E]):
    """Abstract evaluator interface for optimization problems"""
    
    @abstractmethod
    def get_domain_size(self) -> int:
        """Get the size of the problem domain"""
        pass
    
    @abstractmethod
    def evaluate(self, solution: Solution[E]) -> float:
        """Evaluate a complete solution"""
        pass
    
    @abstractmethod
    def evaluate_insertion_cost(self, element: E, solution: Solution[E]) -> float:
        """Evaluate cost of inserting an element into solution"""
        pass
    
    @abstractmethod
    def evaluate_removal_cost(self, element: E, solution: Solution[E]) -> float:
        """Evaluate cost of removing an element from solution"""
        pass
    
    @abstractmethod
    def evaluate_exchange_cost(self, elem_in: E, elem_out: E, solution: Solution[E]) -> float:
        """Evaluate cost of exchanging two elements"""
        pass


class AbstractGRASP(ABC, Generic[E]):
    """Abstract GRASP metaheuristic class"""
    
    verbose = True
    
    def __init__(self, obj_function: Evaluator[E], alpha: float, iterations: int):
        self.obj_function = obj_function
        self.alpha = alpha
        self.iterations = iterations
        self.best_cost = float('inf')
        self.cost = float('inf')
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
    
    def constructive_heuristic(self) -> Solution[E]:
        """GRASP constructive heuristic phase"""
        
        self.CL = self.make_CL()
        self.RCL = self.make_RCL()
        self.sol = self.create_empty_sol()
        self.cost = float('inf')
        
        # Main constructive loop
        while not self.constructive_stop_criteria():
            max_cost = float('-inf')
            min_cost = float('inf')
            self.cost = self.obj_function.evaluate(self.sol)
            self.update_CL()
            
            # Explore all candidates to find min and max costs
            for c in self.CL:
                delta_cost = self.obj_function.evaluate_insertion_cost(c, self.sol)
                if delta_cost < min_cost:
                    min_cost = delta_cost
                if delta_cost > max_cost:
                    max_cost = delta_cost
            
            # Build RCL with candidates within alpha threshold
            self.RCL.clear()
            for c in self.CL:
                delta_cost = self.obj_function.evaluate_insertion_cost(c, self.sol)
                if delta_cost <= min_cost + self.alpha * (max_cost - min_cost):
                    self.RCL.append(c)
            
            # Choose random candidate from RCL
            if self.RCL:
                rnd_index = random.randint(0, len(self.RCL) - 1)
                in_cand = self.RCL[rnd_index]
                self.CL.remove(in_cand)
                self.sol.add(in_cand)
                self.obj_function.evaluate(self.sol)
                self.RCL.clear()
        
        return self.sol
    
    def solve(self) -> Solution[E]:
        """Main GRASP procedure"""
        
        self.best_sol = self.create_empty_sol()
        
        for i in range(self.iterations):
            self.constructive_heuristic()
            self.local_search()
            
            if self.best_sol.cost > self.sol.cost:
                self.best_sol = Solution(self.sol)
                if self.verbose:
                    print(f"(Iter. {i}) BestSol = {self.best_sol}")
        
        return self.best_sol
    
    def constructive_stop_criteria(self) -> bool:
        """Standard stopping criteria for constructive heuristic"""
        return self.cost <= self.sol.cost


class QBF(Evaluator[int]):
    """Quadratic Binary Function (QBF) implementation"""
    
    def __init__(self, filename: str):
        self.size, self.A = self.read_input(filename)
        self.variables = [0.0] * self.size
    
    def read_input(self, filename: str) -> tuple[int, List[List[float]]]:
        """Read QBF matrix from file"""
        with open(filename, 'r') as f:
            lines = f.readlines()
        
        # Parse size
        size = int(lines[0].strip())
        
        # Initialize matrix
        A = [[0.0] * size for _ in range(size)]
        
        # Parse matrix (upper triangular)
        line_idx = 1
        for i in range(size):
            values = list(map(float, lines[line_idx].strip().split()))
            for j, val in enumerate(values):
                A[i][i + j] = val
                if i + j > i:  # Fill lower triangular with zeros
                    A[i + j][i] = 0.0
            line_idx += 1
        
        return size, A
    
    def set_variables(self, solution: Solution[int]):
        """Set binary variables based on solution"""
        self.reset_variables()
        if not solution.is_empty():
            for elem in solution:
                self.variables[elem] = 1.0
    
    def reset_variables(self):
        """Reset all variables to 0"""
        self.variables = [0.0] * self.size
    
    def get_domain_size(self) -> int:
        return self.size
    
    def evaluate(self, solution: Solution[int]) -> float:
        """Evaluate QBF: f(x) = x'.A.x"""
        self.set_variables(solution)
        solution.cost = self.evaluate_QBF()
        return solution.cost
    
    def evaluate_QBF(self) -> float:
        """Matrix multiplication for QBF evaluation"""
        total = 0.0
        vec_aux = [0.0] * self.size
        
        for i in range(self.size):
            aux = 0.0
            for j in range(self.size):
                aux += self.variables[j] * self.A[i][j]
            vec_aux[i] = aux
            total += aux * self.variables[i]
        
        return total
    
    def evaluate_insertion_cost(self, elem: int, solution: Solution[int]) -> float:
        self.set_variables(solution)
        return self.evaluate_insertion_QBF(elem)
    
    def evaluate_insertion_QBF(self, i: int) -> float:
        """Evaluate insertion cost efficiently"""
        if self.variables[i] == 1:
            return 0.0
        return self.evaluate_contribution_QBF(i)
    
    def evaluate_removal_cost(self, elem: int, solution: Solution[int]) -> float:
        self.set_variables(solution)
        return self.evaluate_removal_QBF(elem)
    
    def evaluate_removal_QBF(self, i: int) -> float:
        """Evaluate removal cost efficiently"""
        if self.variables[i] == 0:
            return 0.0
        return -self.evaluate_contribution_QBF(i)
    
    def evaluate_exchange_cost(self, elem_in: int, elem_out: int, solution: Solution[int]) -> float:
        self.set_variables(solution)
        return self.evaluate_exchange_QBF(elem_in, elem_out)
    
    def evaluate_exchange_QBF(self, elem_in: int, elem_out: int) -> float:
        """Evaluate exchange cost efficiently"""
        if elem_in == elem_out:
            return 0.0
        if self.variables[elem_in] == 1:
            return self.evaluate_removal_QBF(elem_out)
        if self.variables[elem_out] == 0:
            return self.evaluate_insertion_QBF(elem_in)
        
        cost = 0.0
        cost += self.evaluate_contribution_QBF(elem_in)
        cost -= self.evaluate_contribution_QBF(elem_out)
        cost -= (self.A[elem_in][elem_out] + self.A[elem_out][elem_in])
        
        return cost
    
    def evaluate_contribution_QBF(self, i: int) -> float:
        """Evaluate contribution of element i to QBF"""
        total = 0.0
        
        for j in range(self.size):
            if i != j:
                total += self.variables[j] * (self.A[i][j] + self.A[j][i])
        total += self.A[i][i]
        
        return total
    
    def print_matrix(self):
        """Print the QBF matrix"""
        for i in range(self.size):
            row = []
            for j in range(i, self.size):
                row.append(str(self.A[i][j]))
            print(" ".join(row))


class QBF_Inverse(QBF):
    """Inverse QBF for maximization problems (GRASP minimizes by default)"""
    
    def evaluate_QBF(self) -> float:
        return -super().evaluate_QBF()
    
    def evaluate_insertion_QBF(self, i: int) -> float:
        return -super().evaluate_insertion_QBF(i)
    
    def evaluate_removal_QBF(self, i: int) -> float:
        return -super().evaluate_removal_QBF(i)
    
    def evaluate_exchange_QBF(self, elem_in: int, elem_out: int) -> float:
        return -super().evaluate_exchange_QBF(elem_in, elem_out)


class GRASP_QBF(AbstractGRASP[int]):
    """GRASP implementation for QBF problems"""
    
    def __init__(self, alpha: float, iterations: int, filename: str):
        super().__init__(QBF_Inverse(filename), alpha, iterations)
    
    def make_CL(self) -> List[int]:
        """Create candidate list with all domain elements"""
        return list(range(self.obj_function.get_domain_size()))
    
    def make_RCL(self) -> List[int]:
        """Create empty RCL (populated during constructive phase)"""
        return []
    
    def update_CL(self):
        """Update CL - for QBF, all elements not in solution are candidates"""
        # All elements outside solution are viable candidates
        pass
    
    def create_empty_sol(self) -> Solution[int]:
        """Create empty solution with zero cost"""
        sol = Solution[int]()
        sol.cost = 0.0
        return sol
    
    def local_search(self) -> Solution[int]:
        """Local search with insertion, removal, and exchange moves"""
        
        while True:
            min_delta_cost = float('inf')
            best_cand_in = None
            best_cand_out = None
            
            self.update_CL()
            
            # Create current CL (elements not in solution)
            current_CL = []
            for i in range(self.obj_function.get_domain_size()):
                if i not in self.sol:
                    current_CL.append(i)
            
            # Evaluate insertions
            for cand_in in current_CL:
                delta_cost = self.obj_function.evaluate_insertion_cost(cand_in, self.sol)
                if delta_cost < min_delta_cost:
                    min_delta_cost = delta_cost
                    best_cand_in = cand_in
                    best_cand_out = None
            
            # Evaluate removals
            for cand_out in self.sol:
                delta_cost = self.obj_function.evaluate_removal_cost(cand_out, self.sol)
                if delta_cost < min_delta_cost:
                    min_delta_cost = delta_cost
                    best_cand_in = None
                    best_cand_out = cand_out
            
            # Evaluate exchanges
            for cand_in in current_CL:
                for cand_out in self.sol:
                    delta_cost = self.obj_function.evaluate_exchange_cost(cand_in, cand_out, self.sol)
                    if delta_cost < min_delta_cost:
                        min_delta_cost = delta_cost
                        best_cand_in = cand_in
                        best_cand_out = cand_out
            
            # Implement best move if it improves solution
            if min_delta_cost < -1e-10:  # Using small epsilon instead of Double.MIN_VALUE
                if best_cand_out is not None:
                    self.sol.remove(best_cand_out)
                if best_cand_in is not None:
                    self.sol.add(best_cand_in)
                self.obj_function.evaluate(self.sol)
            else:
                break
        
        return self.sol


def main():
    """Main function for testing GRASP_QBF"""
    import sys
    
    # Check if filename is provided as command line argument
    if len(sys.argv) > 1:
        filename = sys.argv[1]
        alpha = float(sys.argv[2]) if len(sys.argv) > 2 else 0.05
        iterations = int(sys.argv[3]) if len(sys.argv) > 3 else 1000
    else:
        # Create QBF instance file content for testing (default)
        filename = 'qbf040'
        alpha = 0.05
        iterations = 1000
        
        qbf_content = """40
3 2 7 -4 1 1 -9 -8 -3 10 -5 4 -5 5 -8 1 9 4 -9 0 -1 7 9 -2 -1 6 5 10 -7 -8 -5 7 4 -8 -8 -5 3 -6 -3 -10
9 4 7 -7 6 -5 6 4 4 4 -8 -9 -3 1 -3 7 -2 8 7 -2 3 1 3 10 6 7 -10 5 -6 -4 1 -7 -10 -2 -3 6 4 -8 -1
2 9 -3 -9 -5 -1 3 -2 1 5 -6 -1 4 -2 3 -6 8 10 -9 4 -6 1 -7 7 4 -3 -4 9 10 -3 -4 2 -5 3 3 0 -8 -5
-10 -3 -7 -4 -3 10 -7 -8 4 -1 3 -1 7 -7 5 -1 -1 -5 5 8 9 3 6 -9 5 -2 -5 3 -5 5 4 -5 -9 7 2 -2 7
3 10 2 7 -9 3 -3 10 -6 3 8 10 3 4 3 -9 1 8 -3 4 -3 7 6 6 4 10 10 0 3 6 -8 -4 5 5 -7 7
-9 -10 -7 -9 3 -6 0 -2 -8 7 -4 -6 7 2 -2 -5 -10 -5 -8 3 -2 -9 -7 7 -3 -2 -2 -5 -8 -2 -7 -7 -2 -3 -5
-9 -1 5 -3 3 3 10 -9 -6 10 -9 2 10 -10 7 -7 6 -4 9 5 -5 -3 -5 8 4 5 0 -8 -4 4 -2 -2 -7 -7
-7 10 9 8 7 9 2 -8 6 -9 -9 -2 10 -9 -6 3 8 6 6 -9 -3 5 -4 -5 7 4 3 7 -10 -3 -1 -7 -4
10 -8 -2 -8 -3 -7 5 -4 -5 3 5 1 10 -10 5 -10 -1 2 -2 1 -10 -5 5 -3 3 -3 -9 5 0 -1 -9 -1
-1 9 8 4 1 2 8 -3 -4 -2 -10 -4 8 0 10 -9 -7 8 -4 -7 6 -10 8 6 5 8 -2 10 -10 -6 -9
1 -10 8 4 -2 5 -10 -6 -3 -3 6 8 0 -8 -6 -4 10 -4 -9 -3 -8 -8 -5 -7 0 4 3 10 -5 4
-9 9 -2 5 0 -6 4 5 5 7 9 -10 1 -7 -7 -9 3 5 -8 10 3 8 7 -1 -6 10 3 2 -2
-1 -2 -3 3 -8 1 1 9 -9 5 0 -8 -7 1 -1 6 -1 -1 -1 7 2 3 -7 1 7 5 -6 -6
2 -4 -7 -8 -7 -4 -10 -4 -7 4 -7 -5 10 -3 -8 10 -2 4 -1 2 -6 8 4 -3 -9 9 -6
9 -6 -9 8 3 5 4 5 0 3 -6 6 -1 -5 0 5 -6 5 -2 -4 -9 -5 10 -6 1 -9
-7 5 9 -2 -8 -8 9 -2 -10 9 -4 -10 6 9 -1 -2 -1 6 8 5 7 3 -10 8 9
0 0 8 1 -6 0 8 -5 -4 -9 -2 -6 6 0 7 -9 -1 -7 1 -1 -7 5 -2 2
-9 -1 9 3 -2 7 9 -10 2 -3 -2 5 -2 5 -10 -1 3 8 -1 -4 -7 -3 1
-4 -3 -1 1 -3 9 6 -2 5 5 -3 -1 4 -1 -2 -8 0 -7 9 -7 -10 2
-2 -6 9 -10 -9 7 3 3 -6 7 -7 -10 5 3 6 7 10 -2 9 7 1
5 9 -3 -5 -4 -6 -4 10 2 8 -6 -2 4 4 -9 -9 -2 -4 5 -2
-1 -7 -9 3 0 3 -3 8 9 -9 1 2 -7 5 -3 -9 2 2 -2
7 0 9 -10 -6 -4 1 -8 -2 7 6 4 3 6 9 -8 6 4
0 -1 -7 9 -10 3 -4 -3 1 -3 -8 10 0 8 -5 6 5
10 4 2 6 -1 9 -9 9 -4 1 9 -4 7 0 -4 -7
6 7 -5 3 -10 -9 -1 4 -3 -1 2 -10 4 2 7
-7 -5 -9 -9 3 9 -1 9 2 -4 -5 -6 10 -2
-9 -8 0 3 -6 -3 2 5 2 9 8 -8 -5
-8 7 -9 -8 -3 -2 4 1 -1 8 0 -3
2 0 -4 -2 4 2 -7 6 4 8 10
-8 8 6 -5 4 4 1 -10 -8 -10
-5 -8 10 1 10 9 -3 -7 8
6 10 9 -9 0 -10 5 -5
-5 0 10 3 -5 -2 5
7 4 3 -3 -7 -7
1 8 -5 -10 8
-8 -5 8 -2
10 8 -1
-4 -7
-10"""
        
        # Write test file
        with open('qbf040', 'w') as f:
            f.write(qbf_content)
    
    # Test GRASP_QBF - Change the filename here to your instance file
    start_time = time.time()
    grasp = GRASP_QBF(alpha=alpha, iterations=iterations, filename=filename)
    best_sol = grasp.solve()
    end_time = time.time()
    
    print(f"maxVal = {best_sol}")
    print(f"Time = {end_time - start_time:.3f} sec")


if __name__ == "__main__":
    main()
