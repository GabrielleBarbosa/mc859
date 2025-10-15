import numpy as np
from src.problems.evaluator import Evaluator
from src.solutions.solution import Solution

class QBF(Evaluator):
    def __init__(self, filename: str):
        self.size, self.A = self._read_input(filename)
        self.variables = self._allocate_variables()

    def _read_input(self, filename: str):
        with open(filename, 'r') as f:
            content = f.read().split()
            size = int(content[0])
            A = np.zeros((size, size))
            content_index = 1
            for i in range(size):
                for j in range(i, size):
                    A[i, j] = float(content[content_index])
                    content_index += 1
                    if i != j:
                        A[j, i] = 0.0
        return size, A

    def _allocate_variables(self):
        return np.zeros(self.size)

    def set_variables(self, sol: Solution):
        self.reset_variables()
        if len(sol) > 0:
            for elem in sol:
                self.variables[elem] = 1.0

    def get_domain_size(self):
        return self.size

    def evaluate(self, sol: Solution):
        self.set_variables(sol)
        sol.cost = self.evaluate_qbf()
        return sol.cost

    def evaluate_qbf(self):
        # total = 0.0
        # for i in range(self.size):
        #     for j in range(self.size):
        #         total += self.variables[i] * self.variables[j] * self.A[i][j]

        return self.variables @ self.A @ self.variables


    def evaluate_insertion_cost(self, elem, sol: Solution):
        self.set_variables(sol)
        return self.evaluate_insertion_qbf(elem)

    def evaluate_insertion_qbf(self, i: int):
        if self.variables[i] == 1:
            return 0.0
        return self._evaluate_contribution_qbf(i)

    def evaluate_removal_cost(self, elem, sol: Solution):
        self.set_variables(sol)
        return self.evaluate_removal_qbf(elem)

    def evaluate_removal_qbf(self, i: int):
        if self.variables[i] == 0:
            return 0.0
        return -self._evaluate_contribution_qbf(i)

    def evaluate_exchange_cost(self, elem_in, elem_out, sol: Solution):
        self.set_variables(sol)
        return self.evaluate_exchange_qbf(elem_in, elem_out)

    def evaluate_exchange_qbf(self, in_elem: int, out_elem: int):
        if in_elem == out_elem:
            return 0.0
        if self.variables[in_elem] == 1:
            return self.evaluate_removal_qbf(out_elem)
        if self.variables[out_elem] == 0:
            return self.evaluate_insertion_qbf(in_elem)

        sum_val = self._evaluate_contribution_qbf(in_elem)
        sum_val -= self._evaluate_contribution_qbf(out_elem)
        sum_val -= (self.A[in_elem, out_elem] + self.A[out_elem, in_elem])
        return sum_val

    def _evaluate_contribution_qbf(self, i: int):
        sum_val = 0.0
        # for j in range(self.size):
        #     if i != j:
        #         sum_val += self.variables[j] * (self.A[i, j] + self.A[j, i])

        # using numpy on this step significantly increased performance
        sum_val = np.dot(self.variables, self.A[i, :] + self.A[:, i])
        sum_val -= self.variables[i] * (self.A[i, i] + self.A[i, i])

        sum_val += self.A[i, i]
        return sum_val

    def reset_variables(self):
        self.variables.fill(0.0)

    def print_matrix(self):
        print(self.A)

if __name__ == '__main__':
    import random
    qbf = QBF("instances/qbf/qbf040")
    qbf.print_matrix()
    max_val = -float('inf')

    for i in range(100000):
        for j in range(qbf.size):
            if random.random() < 0.5:
                qbf.variables[j] = 0.0
            else:
                qbf.variables[j] = 1.0
        eval_val = qbf.evaluate_qbf()
        if max_val < eval_val:
            max_val = eval_val
    print("maxVal =", max_val)

    qbf.variables.fill(0.0)
    print("x =", qbf.variables)
    print("f(x) =", qbf.evaluate_qbf())

    qbf.variables.fill(1.0)
    print("x =", qbf.variables)
    print("f(x) =", qbf.evaluate_qbf())
