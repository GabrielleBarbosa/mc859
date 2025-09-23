import abc
from src.solutions.solution import Solution

class Evaluator(abc.ABC):
    @abc.abstractmethod
    def get_domain_size(self):
        pass

    @abc.abstractmethod
    def evaluate(self, sol: Solution):
        pass

    @abc.abstractmethod
    def evaluate_insertion_cost(self, elem, sol: Solution):
        pass

    @abc.abstractmethod
    def evaluate_removal_cost(self, elem, sol: Solution):
        pass

    @abc.abstractmethod
    def evaluate_exchange_cost(self, elem_in, elem_out, sol: Solution):
        pass
