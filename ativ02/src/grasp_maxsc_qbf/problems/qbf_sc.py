"""
Quadratic Binary Function with Set Cover constraints evaluator
"""

from typing import List, Dict, Set
from core.evaluator import Evaluator
from core.solution import Solution


class QBF_SC(Evaluator[int]):
    """Quadratic Binary Function with Set Cover constraints"""
    
    def __init__(self, filename: str):
        self.size, self.A, self.subsets = self.read_input(filename)
        self.variables = [0.0] * self.size
    
    def read_input(self, filename: str) -> tuple[int, List[List[float]], Dict[int, Set[int]]]:
        """Read QBF-SC instance from file in the new format:
        <n> (número de variáveis binárias)
        <s1> <s2> ... <sn> (número de elementos cobertos por cada subconjunto)
        <lista de elementos cobertos por S1>
        <lista de elementos cobertos por S2>
        ...
        <lista de elementos cobertos por Sn>
        <a11> <a12> ... <a1n>
        <a22> ... <a2n>
        ...
        <ann>
        """
        with open(filename, 'r') as f:
            lines = [line.strip() for line in f.readlines() if line.strip()]
        
        line_idx = 0
        
        # Parse number of binary variables (n)
        n = int(lines[line_idx])
        line_idx += 1
        
        # Parse subset sizes (s1, s2, ..., sn)
        subset_sizes = list(map(int, lines[line_idx].split()))
        line_idx += 1
        
        if len(subset_sizes) != n:
            raise ValueError(f"Expected {n} subset sizes, got {len(subset_sizes)}")
        
        # Parse subset elements
        subsets = {}
        for i in range(n):
            if subset_sizes[i] > 0:
                if line_idx >= len(lines):
                    raise ValueError(f"Missing elements for subset {i}")
                subset_elements = list(map(int, lines[line_idx].split()))
                if len(subset_elements) != subset_sizes[i]:
                    raise ValueError(f"Subset {i} should have {subset_sizes[i]} elements, got {len(subset_elements)}")
                # Convert to 0-based indexing (assuming input is 1-based)
                subsets[i] = set(elem - 1 for elem in subset_elements if elem > 0)
                line_idx += 1
            else:
                subsets[i] = set()  # Empty subset
        
        # Parse QBF matrix A (upper triangular format)
        A = [[0.0] * n for _ in range(n)]
        
        for i in range(n):
            if line_idx >= len(lines):
                raise ValueError(f"Missing matrix row {i}")
            
            # Read elements from position i to n-1 (upper triangular)
            row_elements = list(map(float, lines[line_idx].split()))
            expected_elements = n - i
            
            if len(row_elements) != expected_elements:
                raise ValueError(f"Row {i} should have {expected_elements} elements, got {len(row_elements)}")
            
            # Fill upper triangular part
            for j, val in enumerate(row_elements):
                col_idx = i + j
                if col_idx < n:
                    A[i][col_idx] = val
            
            line_idx += 1
        
        return n, A, subsets
    
    def get_domain_size(self) -> int:
        return self.size
    
    def set_variables(self, solution: Solution[int]):
        """Set binary variables based on solution"""
        self.reset_variables()
        if not solution.is_empty():
            for elem in solution:
                if 0 <= elem < self.size:
                    self.variables[elem] = 1.0
    
    def reset_variables(self):
        """Reset all variables to 0"""
        self.variables = [0.0] * self.size
    
    def evaluate(self, solution: Solution[int]) -> float:
        """Evaluate QBF: f(x) = x'.A.x"""
        self.set_variables(solution)
        
        if not self.is_feasible(solution):
            solution.cost = float('-inf')  # Infeasible solution
            return solution.cost
        
        solution.cost = self.evaluate_QBF()
        return solution.cost
    
    def evaluate_QBF(self) -> float:
        """Matrix multiplication for QBF evaluation"""
        total = 0.0
        
        for i in range(self.size):
            for j in range(self.size):
                total += self.variables[i] * self.variables[j] * self.A[i][j]
        
        return total
    
    def is_feasible(self, solution: Solution[int]) -> bool:
        """Check if solution satisfies set cover constraints"""
        covered_variables = set()
        
        for subset_idx in solution:
            if subset_idx in self.subsets:
                covered_variables.update(self.subsets[subset_idx])
        
        # All variables must be covered (0 to size-1)
        required_coverage = set(range(self.size))
        return covered_variables == required_coverage
    
    def get_uncovered_variables(self, solution: Solution[int]) -> Set[int]:
        """Get variables not covered by current solution"""
        covered_variables = set()
        
        for subset_idx in solution:
            if subset_idx in self.subsets:
                covered_variables.update(self.subsets[subset_idx])
        
        all_variables = set(range(self.size))
        return all_variables - covered_variables
    
    def evaluate_insertion_cost(self, elem: int, solution: Solution[int]) -> float:
        if elem in solution:
            return 0.0
        
        self.set_variables(solution)
        return self.evaluate_insertion_QBF(elem)
    
    def evaluate_insertion_QBF(self, i: int) -> float:
        """Evaluate insertion cost efficiently"""
        if self.variables[i] == 1:
            return 0.0
        return self.evaluate_contribution_QBF(i)
    
    def evaluate_removal_cost(self, elem: int, solution: Solution[int]) -> float:
        if elem not in solution:
            return 0.0
        
        # Check if removal maintains feasibility
        temp_sol = Solution(list(solution.elements))
        temp_sol.remove(elem)
        if not self.is_feasible(temp_sol):
            return float('-inf')  # Cannot remove - would make infeasible
        
        self.set_variables(solution)
        return self.evaluate_removal_QBF(elem)
    
    def evaluate_removal_QBF(self, i: int) -> float:
        """Evaluate removal cost efficiently"""
        if self.variables[i] == 0:
            return 0.0
        return -self.evaluate_contribution_QBF(i)
    
    def evaluate_exchange_cost(self, elem_in: int, elem_out: int, solution: Solution[int]) -> float:
        # Check if exchange maintains feasibility
        temp_sol = Solution(list(solution.elements))
        temp_sol.remove(elem_out)
        temp_sol.add(elem_in)
        if not self.is_feasible(temp_sol):
            return float('-inf')  # Cannot exchange - would make infeasible
        
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