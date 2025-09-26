import math
import time
from collections import deque, defaultdict
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


        self.iterations_without_new_best_sol = 0
        self.elements_frequency = self.make_elem_frequency()
        self.iterations_for_diversification = 50
        

    def make_elem_frequency(self):
        freq = {}
        for i in range(self.obj_function.get_domain_size()):
            freq[i] = 0
        
        return freq
    
    def update_elem_frequency(self):
        for i in self.sol:
            self.elements_frequency[i] += 1
    
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
        
        if self.strategy == "diversification_by_restart":
            if self.iterations_without_new_best_sol >= self.iterations_for_diversification:
                self.iterations_without_new_best_sol = 0
                # double the number of iterations to try again diversification, so it can possibly recover to a new minimum
                self.iterations_for_diversification *= 2 
                # self.sol = Solution(self.best_sol)
                # self.update_cl()
                previous_cost = self.sol.cost

                freq_groups = defaultdict(list)
                for elem, freq in self.elements_frequency.items():
                    freq_groups[freq].append(elem)

                # sort frequencies ascending
                sorted_freqs = sorted(freq_groups.keys())

                # flatten: shuffle within equal-frequency groups
                sorted_freq = []
                for f in sorted_freqs:
                    group = freq_groups[f]
                    self.rng.shuffle(group)
                    sorted_freq.extend(group)

                # add to best solution the 5% least frequently used items that are not already in the solution
                elems_len = math.ceil(len(sorted_freq) / 20)
                i = 0
                for k in sorted_freq:
                    if i >= elems_len:
                        break

                    if k not in self.sol:
                        self.sol.append(k)
                        self.cl.remove(k)
                        self.tl.append(k)
                        i += 1

                # # remove from best solution the 2.5% most frequently used items 
                # elems_len = math.ceil(len(sorted_freq) / 40)
                # i = 0
                # for k in reversed(sorted_freq):
                #     if i >= elems_len:
                #         break

                #     if k in self.sol:
                #         self.sol.remove(k)
                #         self.cl.append(k)
                #         self.tl.append(k)
                #         i += 1

                self.obj_function.evaluate(self.sol)
                if self.verbose:
                    print(f"(Iter. {self.current_iter}) performed diversification by restart, previous cost = {previous_cost}, sol = {self.sol}, feasible = {self.obj_function.is_feasible(self.sol)}")
                    

        # Evaluate insertions
        for cand_in in self.cl:
            movements.append((cand_in, None))

        # Evaluate removals
        for cand_out in self.sol:
            movements.append((None, cand_out))

        # Evaluate exchanges
        for cand_in in self.cl:
            for cand_out in self.sol:
                movements.append((cand_in, cand_out))


        self.rng.shuffle(movements)
        if self.strategy == "probabilistic":
            movements = self.rng.sample(movements, len(movements) // 2)
        
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
            self.elements_frequency[best_cand_in] += 1
        else:
            self.tl.append(self.fake)

        self.obj_function.evaluate(self.sol)

    def solve(self):
        self.best_sol = self.create_empty_sol()
        self.constructive_heuristic()
        self.update_elem_frequency()
        self.tl = self.make_tl()

        time_limit = time.time() + 30 * 60  # 30 minutes timeout

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
            else:
                self.iterations_without_new_best_sol += 1

        return self.best_sol
