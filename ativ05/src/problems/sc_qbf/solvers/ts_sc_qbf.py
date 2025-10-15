import random
import time
from collections import deque
from src.metaheuristics.tabusearch.abstract_ts import AbstractTS
from src.problems.sc_qbf.sc_qbf_inverse import SC_QBF_Inverse
from src.solutions.solution import Solution

class TS_SC_QBF(AbstractTS):
    def __init__(self, tenure: int, iterations: int, timeout: int, filename: str, search_method: str, strategy: str, random_seed = 42, target_value = None):
        self.fake = -1
        self.strategy = strategy
        self.search_method = search_method        
        obj_function = SC_QBF_Inverse(filename)
        if tenure < 1:
            tenure = int(tenure * obj_function.get_domain_size())
        self.timeout = timeout
        self.target_value = target_value
        super().__init__(obj_function, tenure, iterations)

        self.rng = random.Random(random_seed)
    
    def make_cl(self):
        return list(range(self.obj_function.get_domain_size()))

    def make_rcl(self):
        return []

    def make_tl(self):
        return deque([self.fake] * (2 * self.tenure), maxlen=2 * self.tenure)

    def update_cl(self):
        self.cl = self.make_cl()
        for i in self.sol:
            self.cl.remove(i)

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

        # append insertions
        for cand_in in self.cl:
            movements.append((cand_in, None))

        # append removals
        for cand_out in self.sol:
            movements.append((None, cand_out))

        # append exchanges
        for cand_in in self.cl:
            for cand_out in self.sol:
                movements.append((cand_in, cand_out))

        # shuffle movements to avoid bias
        self.rng.shuffle(movements)
        if self.strategy == "probabilistic":
            movements = self.rng.sample(movements, int(0.8 * len(movements)))
        
        # search for best
        for movement in movements:
            cand_in, cand_out = movement
            if cand_in != None and cand_out != None:
                delta_cost = self.obj_function.evaluate_exchange_cost(cand_in, cand_out, self.sol)
                if ((cand_in not in self.tl) and (cand_out not in self.tl)) or (self.sol.cost + delta_cost < self.best_sol.cost):
                    if delta_cost < min_delta_cost:
                        temp_sol = Solution(self.sol)
                        temp_sol.append(cand_in)
                        temp_sol.remove(cand_out)
                        if self.obj_function.is_feasible(temp_sol):
                            min_delta_cost = delta_cost
                            best_cand_in = cand_in
                            best_cand_out = cand_out
                            if self.search_method == "first_improving" and self.sol.cost + delta_cost < self.sol.cost:
                                break
            elif cand_out != None:
                delta_cost = self.obj_function.evaluate_removal_cost(cand_out, self.sol)
                if (cand_out not in self.tl) or (self.sol.cost + delta_cost < self.best_sol.cost):
                    if delta_cost < min_delta_cost:
                        temp_sol = Solution(self.sol)
                        temp_sol.remove(cand_out)
                        if self.obj_function.is_feasible(temp_sol):
                            min_delta_cost = delta_cost
                            best_cand_in = None
                            best_cand_out = cand_out
                            if self.search_method == "first_improving" and self.sol.cost + delta_cost < self.sol.cost:
                                break
            else:
                delta_cost = self.obj_function.evaluate_insertion_cost(cand_in, self.sol)
                if (cand_in not in self.tl) or (self.sol.cost + delta_cost < self.best_sol.cost):
                    if delta_cost < min_delta_cost:
                        min_delta_cost = delta_cost
                        best_cand_in = cand_in
                        best_cand_out = None
                        if self.search_method == "first_improving" and self.sol.cost + delta_cost < self.sol.cost:
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

        time_limit = time.time() + self.timeout 

        for i in range(self.iterations):
            self.current_iter = i
            if time.time() >= time_limit:
                if self.verbose:
                    print(f"(Iter. {i}) Time limit reached. Stopping early.")
                break

            self.neighborhood_move()
            if self.best_sol.cost > self.sol.cost:
                if self.obj_function.is_feasible(self.sol):
                    self.best_sol = Solution(self.sol)
                    if self.verbose:
                        print(f"(Iter. {i}) BestSol = {self.best_sol}")
                    if self.target_value != None and self.best_sol.cost <= self.target_value:
                        print(f"(Iter. {i}) Target value reached, stopping method")
                        break

        return self.best_sol
