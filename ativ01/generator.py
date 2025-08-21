import os
import random
import math
# import numpy as np


def write_file(filename, n, S, A):
    os.makedirs('data', exist_ok=True)
    filename = f"data/{filename}.txt"
    with open(filename, 'w') as file:
        file.write(str(n))
        file.write('\n')
        file.write(' '.join([str(len(s)) for s in S]))
        file.write('\n')
        for s in S:
            file.write(' '.join(map(str, s)))
            file.write('\n')
        for i in range(n):
            file.write(' '.join(map(str, A[i][i:])))
            file.write('\n')


# -----------------------------
# Subset generation strategies
# -----------------------------
def generate_S_structured(n, k):
    """Each subset has exactly k elements, with structured repetition."""
    numbers = [val for _ in range(k) for val in range(1, n + 1)]
    return [numbers[i:i + k] for i in range(0, n*k, k)]


def generate_S_uniform(n, k):
    """Subsets with uniform random sizes between 1 and k."""
    return [random.sample(range(1, n + 1), random.randint(1, k)) for _ in range(n)]


def generate_S_random(n, k):
    """Randomly decide size and elements of each subset, but keeping all subsets at least 25% smaller than N"""
    elements = list(range(1, n + 1))
    subsets = []

    while len(subsets) < n:
        size = random.randint(1, n - (n // 4))
        random.shuffle(elements)
        subsets.append(elements[:size])

    return subsets

def generate_S_ocurrence(n, k=None):
    """Generate subsets with controlled occurrences for each number, but making sure each number appears at least once and at most in 75% of the subsets."""
    # Step 1: Decide occurrences for each variable, at most 75% of n
    occurrences = [random.randint(1, n - n // 4) for _ in range(1, n + 1)]
    subsets = [[] for _ in range(n)]
    for i in range(n):
        selected = random.sample(range(0, n), occurrences[i])
        for subset in selected:
            subsets[subset].append(i + 1)  # +1 to match 1-based indexing
    
    return subsets

# -----------------------------
# Matrix generation strategies
# -----------------------------
def generate_A_dense(n, p):
    """Dense upper triangular, always at least one negative."""
    A = [[0] * n for _ in range(n)]
    has_negative = False
    for i in range(n):
        for j in range(i, n):
            val = random.randint(-p, p)
            if val < 0:
                has_negative = True
            A[i][j] = val
    if not has_negative:  # force at least one negative
        i = random.randint(0, n - 1)
        A[i][i] = -random.randint(1, p)
    return A


def generate_A_sparse(n, p, density=0.2):
    """Sparse upper triangular, with guaranteed negative."""
    A = [[0] * n for _ in range(n)]
    has_negative = False
    for i in range(n):
        for j in range(i, n):
            if random.random() < density:
                val = random.randint(-p, p)
                if val < 0:
                    has_negative = True
                A[i][j] = val
    if not has_negative:
        i = random.randint(0, n - 1)
        A[i][i] = -random.randint(1, p)
    return A


def generate_A_negative(n, p):
    """Dense matrix but force all elements negative."""
    A = [[0] * n for _ in range(n)]
    for i in range(n):
        for j in range(i, n):
            A[i][j] = random.randint(-p, 0)
    
    return A


# -----------------------------
# Strategy selector
# -----------------------------
S_strategies = {
    "structured": generate_S_structured,
    "uniform": generate_S_uniform,
    "random": generate_S_random,
    "occurence": generate_S_ocurrence,
}

A_strategies = {
    "dense": generate_A_dense,
    "sparse": generate_A_sparse,
    "negative": generate_A_negative,
}


def generate_instance(n, k, p, strategy_s, strategy_a):
    S = S_strategies[strategy_s](n, k)
    A = A_strategies[strategy_a](n, p)

    # ensure union of S covers 1..n
    covered = set().union(*S)
    missing = set(range(1, n + 1)) - covered
    for m in missing:
        S[random.randint(0, n - 1)].append(m)
    return S, A


def main():
    sizes = [25, 50, 100, 200, 400]  
    k = 10
    p = 100
    strategy_s = "occurence"   # "uniform", "structured", "random"
    strategy_a = "negative"  # "dense", "sparse", "negative"

    for n in sizes:
        S, A = generate_instance(n, k, p, strategy_s, strategy_a)

        filename = f"gen_{n}_k{k}_p{p}_{strategy_s}_{strategy_a}"
        write_file(filename, n, S, A)
    

if __name__ == '__main__':
    main()
