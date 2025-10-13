"""
Abstract evaluator interface for optimization problems
"""

from abc import ABC, abstractmethod
from typing import Generic, TypeVar
from .solution import Solution

# Generic type for solution elements
E = TypeVar('E')


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
    
    @abstractmethod
    def is_feasible(self, solution: Solution[E]) -> bool:
        """Check if solution is feasible"""
        pass