# Genetic Algorithm Framework for SC-QBF

This project implements a Genetic Algorithm (GA) to solve the Set Covering Quantum Boolean Formula (SC-QBF) problem. The framework is written in Java and is designed to find optimal or near-optimal solutions for given SC-QBF instances.

## The SC-QBF Problem

The `SC_QBF.java` file implements the evaluator for the Set Covering Quantum Boolean Formula problem. The goal is to select a subset of variables that maximizes a given objective function, subject to a set of constraints defined in the instance files.

## How to Run the Solver

The main entry point for the solver is the `main` method within the `problems.scqbf.solvers.GA_SC_QBF` class.

### Compilation

You can compile the Java source files using your IDE (like Eclipse or Intellij) or a build tool. 

### Execution

To run the solver, execute the `GA_SC_QBF` class. The `main` method is configured to run a series of experiments, iterating through all available instances in the `instances/scqbf` directory with various GA configurations.

The program will:
1.  Instantiate the GA with different configurations (population size, mutation rate, crossover strategy).
2.  Run the solver for each instance file located in `GA-Framework/instances/scqbf/`.
3.  Print the progress and best solution found for each run to the console.
4.  Save the aggregated results of all runs into a CSV file named `ga_results.csv` in the project's root directory.

## Instances

The `GA-Framework/instances/scqbf/` directory contains the problem instances. These are text files (`*.txt`) that define the constraints and parameters of the SC-QBF problem for the solver to work on.

## Output

The solver produces two main outputs:
1.  **Console Output:** During execution, the GA will print the best solution found for each generation (if `verbose` is enabled) and a summary for each instance run.
2.  **CSV Results:** A file named `ga_results.csv` is generated, containing detailed results for each configuration and instance pair. The columns include:
    - `Configuration`: The name of the GA parameter configuration used.
    - `Instance`: The name of the instance file.
    - `BestSolution`: The final cost of the best solution found.
    - `Time_ms`: The execution time in milliseconds.
    - `Generations`: The number of generations the GA ran for.
    - `StopReason`: The criterion that caused the execution to stop (e.g., time limit, max generations, no improvement).

## Result Analisys

The directory "analise" contains python scripts to process and compile different views of the ga_result.csv obtained running the experiment. The "data" directory contains the GA results and the results obtained in other activities, using an exact method and other frameworks (GRASP and Tabu) and the "out" directory is where the python scripts generate their results. 