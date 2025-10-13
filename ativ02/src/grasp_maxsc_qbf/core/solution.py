"""
Solution class for optimization problems
"""

import copy
from typing import Generic, TypeVar, List

# Generic type for solution elements
E = TypeVar('E')


class Solution(Generic[E]):
    """Solution class that stores elements and cost"""
    
    def __init__(self, elements_or_solution=None, cost: float = None):
        if isinstance(elements_or_solution, Solution):
            # Copy constructor
            self.elements = copy.deepcopy(elements_or_solution.elements)
            self.cost = elements_or_solution.cost
        elif elements_or_solution is not None:
            # Constructor with elements and cost
            self.elements = elements_or_solution
            self.cost = cost if cost is not None else float('-inf')
        else:
            # Default constructor
            self.elements = []
            self.cost = float('-inf')
    
    def add(self, element: E):
        """Add element to solution"""
        if element not in self.elements:
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
        return f"Solution: cost=[{self.cost}], size=[{len(self.elements)}], elements={sorted(self.elements)}"