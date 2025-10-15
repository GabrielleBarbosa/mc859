import sys
import os

# Add the project root to the Python path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

from collections import deque
from src.metaheuristics.tabusearch.abstract_ts import AbstractTS
from src.problems.qbf.qbf_inverse import QBF_Inverse
from src.solutions.solution import Solution

class TS_QBF(AbstractTS):
    def __init__(self, tenure: int, iterations: int, filename: str):
        self.fake = -1
        super().__init__(QBF_Inverse(filename), tenure, iterations)

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

    def neighborhood_move(self):
        min_delta_cost = float('inf')
        best_cand_in = None
        best_cand_out = None

        # Evaluate insertions
        for cand_in in self.cl:
            delta_cost = self.obj_function.evaluate_insertion_cost(cand_in, self.sol)
            if (cand_in not in self.tl) or (self.sol.cost + delta_cost < self.best_sol.cost):
                if delta_cost < min_delta_cost:
                    min_delta_cost = delta_cost
                    best_cand_in = cand_in
                    best_cand_out = None

        # Evaluate removals
        for cand_out in self.sol:
            delta_cost = self.obj_function.evaluate_removal_cost(cand_out, self.sol)
            if (cand_out not in self.tl) or (self.sol.cost + delta_cost < self.best_sol.cost):
                if delta_cost < min_delta_cost:
                    min_delta_cost = delta_cost
                    best_cand_in = None
                    best_cand_out = cand_out

        # Evaluate exchanges
        for cand_in in self.cl:
            for cand_out in self.sol:
                delta_cost = self.obj_function.evaluate_exchange_cost(cand_in, cand_out, self.sol)
                if ((cand_in not in self.tl) and (cand_out not in self.tl)) or \
                   (self.sol.cost + delta_cost < self.best_sol.cost):
                    if delta_cost < min_delta_cost:
                        min_delta_cost = delta_cost
                        best_cand_in = cand_in
                        best_cand_out = cand_out

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

if __name__ == '__main__':
    import time
    
    # Adjust the path to the instances folder
    instances_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..', '..', 'instances', 'qbf', 'qbf020'))

    start_time = time.time()
    tabusearch = TS_QBF(20, 1000, instances_path)
    best_sol = tabusearch.solve()
    print("maxVal =", best_sol)
    end_time = time.time()
    total_time = end_time - start_time
    print(f"Time = {total_time:.3f} seg")