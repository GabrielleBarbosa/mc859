import sys
import os

# Add the project root to the Python path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

from collections import deque
from src.metaheuristics.tabusearch.abstract_ts import AbstractTS
from src.problems.sc_qbf.sc_qbf_inverse import SC_QBF_Inverse
from src.solutions.solution import Solution

class TS_SC_QBF(AbstractTS):
    def __init__(self, tenure: int, iterations: int, filename: str, search_method: str, strategy: str):
        self.fake = -1
        self.strategy = strategy
        self.search_method = search_method
        obj_function = SC_QBF_Inverse(filename)
        if tenure < 1:
            tenure = int(tenure * obj_function.get_domain_size())
        super().__init__(obj_function, tenure, iterations)
        

    def make_cl(self):
        return list(range(self.obj_function.get_domain_size()))

    def make_rcl(self):
        return []

    def make_tl(self):
        return deque([self.fake] * (2 * self.tenure), maxlen=2 * self.tenure)

    def update_cl(self):
        pass

    def create_empty_sol(self):
        sol = Solution()
        sol.cost = 0.0
        return sol

    def constructive_stop_criteria(self, cost):
        return self.obj_function.is_feasible(self.sol) and cost <= self.sol.cost

    def neighborhood_move(self):
        min_delta_cost = float('inf')
        best_cand_in = None
        best_cand_out = None

        movements = []

        # Evaluate insertions
        for cand_in in self.cl:
            delta_cost = self.obj_function.evaluate_insertion_cost(cand_in, self.sol)
            movements.append((cand_in, None, delta_cost))

        # Evaluate removals
        for cand_out in self.sol:
            temp_sol = Solution(self.sol)
            temp_sol.remove(cand_out)
            if self.obj_function.is_feasible(temp_sol):
                delta_cost = self.obj_function.evaluate_removal_cost(cand_out, self.sol)
                movements.append((None, cand_out, delta_cost))

        # Evaluate exchanges
        for cand_in in self.cl:
            for cand_out in self.sol:
                temp_sol = Solution(self.sol)
                temp_sol.append(cand_in)
                temp_sol.remove(cand_out)
                if self.obj_function.is_feasible(temp_sol):
                    delta_cost = self.obj_function.evaluate_exchange_cost(cand_in, cand_out, self.sol)
                    movements.append((cand_in, cand_out, delta_cost))


        self.rng.shuffle(movements)
        if self.strategy == "probabilistic":
            movements = self.rng.sample(movements, len(movements) // 2)
        
        for movement in movements:
            cand_in, cand_out, delta_cost = movement
            if cand_in != None and cand_out != None:
                if ((cand_in not in self.tl) and (cand_out not in self.tl)) or \
                    (self.sol.cost + delta_cost < self.best_sol.cost):
                        if delta_cost < min_delta_cost:
                            min_delta_cost = delta_cost
                            best_cand_in = cand_in
                            best_cand_out = cand_out
                            if self.search_method == "first_improving":
                                break
            elif cand_out != None:
                if (cand_out not in self.tl) or (self.sol.cost + delta_cost < self.best_sol.cost):
                    if delta_cost < min_delta_cost:
                        min_delta_cost = delta_cost
                        best_cand_in = None
                        best_cand_out = cand_out
                        if self.search_method == "first_improving":
                            break
            else:
                if (cand_in not in self.tl) or (self.sol.cost + delta_cost < self.best_sol.cost):
                    if delta_cost < min_delta_cost:
                        min_delta_cost = delta_cost
                        best_cand_in = cand_in
                        best_cand_out = None
                        if self.search_method == "first_improving":
                            break

        # Implement the best non-tabu move
        if len(self.tl) >= 2 * self.tenure:
            self.tl.popleft()
        if best_cand_out is not None:
            self.sol.remove(best_cand_out)
            self.cl.append(best_cand_out)
            self.tl.append(best_cand_out)
        else:
            self.tl.append(self.fake)
        
        if len(self.tl) >= 2 * self.tenure:
            self.tl.popleft()
        if best_cand_in is not None:
            self.sol.append(best_cand_in)
            self.cl.remove(best_cand_in)
            self.tl.append(best_cand_in)
        else:
            self.tl.append(self.fake)

        self.obj_function.evaluate(self.sol)

    def solve(self):
        self.best_sol = self.create_empty_sol()
        self.constructive_heuristic()
        self.tl = self.make_tl()
        for i in range(self.iterations):
            self.neighborhood_move()
            if self.obj_function.is_feasible(self.sol) and self.best_sol.cost > self.sol.cost:
                self.best_sol = Solution(self.sol)
                if self.verbose:
                    print(f"(Iter. {i}) BestSol = {self.best_sol}")

        return self.best_sol
