import abc
import random
from collections import deque
from src.solutions.solution import Solution
from src.problems.evaluator import Evaluator

class AbstractTS(abc.ABC):
    def __init__(self, obj_function: Evaluator, tenure: int, iterations: int):
        self.obj_function = obj_function
        self.tenure = tenure
        self.iterations = iterations
        self.best_sol = self.create_empty_sol()
        self.sol = self.create_empty_sol()
        self.cl = self.make_cl()
        self.rcl = self.make_rcl()
        self.tl = self.make_tl()
        self.rng = random.Random(42)
        self.verbose = True

    @abc.abstractmethod
    def make_cl(self):
        pass

    @abc.abstractmethod
    def make_rcl(self):
        pass

    @abc.abstractmethod
    def make_tl(self):
        pass

    @abc.abstractmethod
    def update_cl(self):
        pass

    @abc.abstractmethod
    def create_empty_sol(self):
        pass

    @abc.abstractmethod
    def neighborhood_move(self):
        pass

    def constructive_heuristic(self):
        self.cl = self.make_cl()
        self.rcl = self.make_rcl()
        self.sol = self.create_empty_sol()
        cost = float('inf')

        while not self.constructive_stop_criteria(cost):
            max_cost = -float('inf')
            min_cost = float('inf')
            cost = self.sol.cost
            self.update_cl()

            if not self.cl:
                break

            for c in self.cl:
                delta_cost = self.obj_function.evaluate_insertion_cost(c, self.sol)
                if delta_cost < min_cost:
                    min_cost = delta_cost
                if delta_cost > max_cost:
                    max_cost = delta_cost

            for c in self.cl:
                delta_cost = self.obj_function.evaluate_insertion_cost(c, self.sol)
                if delta_cost <= min_cost:
                    self.rcl.append(c)

            rnd_index = self.rng.randint(0, len(self.rcl) - 1)
            in_cand = self.rcl[rnd_index]
            self.cl.remove(in_cand)
            self.sol.append(in_cand)
            self.obj_function.evaluate(self.sol)
            self.rcl.clear()

        return self.sol

    def solve(self):
        self.best_sol = self.create_empty_sol()
        self.constructive_heuristic()
        self.tl = self.make_tl()
        for i in range(self.iterations):
            self.neighborhood_move()
            if self.best_sol.cost > self.sol.cost:
                self.best_sol = Solution(self.sol)
                if self.verbose:
                    print(f"(Iter. {i}) BestSol = {self.best_sol}")

        return self.best_sol

    def constructive_stop_criteria(self, cost):
        return cost <= self.sol.cost
