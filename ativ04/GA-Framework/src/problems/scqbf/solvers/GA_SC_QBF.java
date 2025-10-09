package problems.scqbf.solvers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import metaheuristics.ga.AbstractGA;
import problems.Evaluator;
import problems.scqbf.SC_QBF;
import solutions.Solution;

/**
 * Genetic Algorithm implementation for the MAX-SC-QBF problem
 */
public class GA_SC_QBF extends AbstractGA<Integer, Integer> {

    private static final double uniformCrossoverP = 0.5;

    // Configuration parameters
    private boolean usePopulationFunction; // For population variation
    private boolean useMutationFunction; // For Mutation variation
    private String crossoverStrategy; // For Crossover variation
    private String mutationStrategy; // For Mutation variation
    
    // Stopping criteria constants
    private static final long MAX_TIME_MS = 30 * 60 * 1000; // 30 minutes
    private static final int MAX_GENERATIONS = 2000;
    private static final int MAX_GENERATIONS_NO_IMPROVEMENT = 200;
    
    // Statistics tracking
    private long startTime;
    private int generationsNoImprovement;
    private int actualGenerations;
    private String stopReason;

    public GA_SC_QBF(Evaluator<Integer> objFunction, Integer generations, Integer popSize, 
                     Double mutationRate, boolean usePopFunction, boolean useMutFunction, String crossoverStrategy, String mutationStrategy) {
        super(objFunction, generations, popSize, mutationRate);
        this.usePopulationFunction = usePopFunction;
        this.useMutationFunction = useMutFunction;
        this.crossoverStrategy = crossoverStrategy;
        this.mutationStrategy = mutationStrategy;
        if (usePopulationFunction) {
            this.popSize = calculatePopulationSize(chromosomeSize);
        }
        if (useMutationFunction) {
            this.mutationRate = calculateMutationRate(chromosomeSize);
        }
    }
    
    private int calculatePopulationSize(int n) {
        return Math.max(50, 2 * n);
    }
    
    private double calculateMutationRate(int n) {
        return 1.0 / n;
    }
    
    @Override
    public Solution<Integer> createEmptySol() {
        return new Solution<Integer>();
    }
    
    @Override
    protected Solution<Integer> decode(Chromosome chromosome) {
        Solution<Integer> solution = createEmptySol();
        for (int i = 0; i < chromosome.size(); i++) {
            if (chromosome.get(i) == 1) {
                solution.add(i);
            }
        }
        solution.cost = ObjFunction.evaluate(solution);
        return solution;
    }
    
    @Override
    protected Chromosome generateRandomChromosome() {
        Chromosome chromosome = new Chromosome();
        
        // Generate random chromosome with higher probability of 1s
        for (int i = 0; i < chromosomeSize; i++) {
            chromosome.add(rng.nextDouble() < 0.5 ? 1 : 0);
        }
        
        // Ensure at least one feasible solution
        Solution<Integer> testSol = decode(chromosome);
        if (testSol.cost.equals(Double.POSITIVE_INFINITY)) {
            // Add all subsets to guarantee feasible solution
            for (int i = 0; i < chromosomeSize; i++) {
                chromosome.set(i, 1);
            }
        }
        
        return chromosome;
    }
    
    @Override
    protected Double fitness(Chromosome chromosome) {
        Solution<Integer> solution = decode(chromosome);
        
        return solution.cost;
    }
    
    @Override
    protected void mutateGene(Chromosome chromosome, Integer locus) {
        chromosome.set(locus, 1 - chromosome.get(locus));
    }

    @Override
    protected Population crossover(Population parents) {
        if (this.crossoverStrategy.equals("standard")) {
            return super.crossover(parents);
        }

        if (this.crossoverStrategy.equals("uniform_crossover")) {
            Population offsprings = new Population();
            for (int i = 0; i < popSize; i += 2) {
                Chromosome parent1 = parents.get(i);
                Chromosome parent2 = parents.get(i + 1);

                Chromosome offspring1 = new Chromosome();
                Chromosome offspring2 = new Chromosome();

                for (int j = 0; j < chromosomeSize; j++) {
                    if (rng.nextDouble() < uniformCrossoverP) {
                        // no exchange
                        offspring1.add(parent1.get(j));
                        offspring2.add(parent2.get(j));
                    } else {
                        // exchange
                        offspring1.add(parent2.get(j));
                        offspring2.add(parent1.get(j));
                    }
                }

                offsprings.add(offspring1);
                offsprings.add(offspring2);
            }

            return offsprings;
        }

        return null;
    }
    
    @Override
    protected Population mutate(Population offsprings) {
        if ("adaptive_mutation".equals(this.mutationStrategy)) {
            return adaptiveMutate(offsprings);
        } else {
            return super.mutate(offsprings);
        }
    }

    protected Population adaptiveMutate(Population offsprings) {
        double meanFitness = 0.0;
        for (Chromosome c : offsprings) {
            meanFitness += fitness(c);
        }
        meanFitness /= offsprings.size();

        double stdDev = 0.0;
        for (Chromosome c : offsprings) {
            stdDev += Math.pow(fitness(c) - meanFitness, 2);
        }
        stdDev = Math.sqrt(stdDev / offsprings.size());

        double coefficientOfVariation = stdDev / meanFitness;
        double adjustedMutationRate = this.mutationRate * (1 - coefficientOfVariation);

        for (Chromosome c : offsprings) {
            for (int locus = 0; locus < chromosomeSize; locus++) {
                if (rng.nextDouble() < adjustedMutationRate) {
                    mutateGene(c, locus);
                }
            }
        }

        return offsprings;
    }

    @Override
    protected Population initializePopulation() {
        Population population = new Population();
        
        // Ensure first chromosome is all 1s (always feasible for first population)
        Chromosome allOnes = new Chromosome();
        for (int i = 0; i < chromosomeSize; i++) {
            allOnes.add(1);
        }
        population.add(allOnes);
        
        // Generate rest of population
        while (population.size() < popSize) {
            population.add(generateRandomChromosome());
        }
        
        return population;
    }
    
    @Override
    public Solution<Integer> solve() {
        startTime = System.currentTimeMillis();
        generationsNoImprovement = 0;
        actualGenerations = 0;
        stopReason = "";
        
        Population population = initializePopulation();
        
        bestChromosome = getBestChromosome(population);
        bestSol = decode(bestChromosome);
        System.out.println("(Gen. 0) BestSol = " + bestSol);
        
        for (int g = 1; g <= generations; g++) {
            actualGenerations = g;
            
            if (checkStoppingCriteria(g)) {
                break;
            }
            
            Population parents = selectParents(population);
            Population offsprings = crossover(parents);
            Population mutants = mutate(offsprings);
            Population newPopulation = selectPopulation(mutants);
            
            population = newPopulation;
            
            Chromosome currentBest = getBestChromosome(population);
            double currentFitness = fitness(currentBest);
            
            if (currentFitness > fitness(bestChromosome)) {
                bestChromosome = currentBest;
                bestSol = decode(bestChromosome);
                generationsNoImprovement = 0;
                if (verbose) {
                    System.out.println("(Gen. " + g + ") BestSol = " + bestSol);
                }
            } else {
                generationsNoImprovement++;
            }
        }
        
        if (stopReason.isEmpty()) {
            stopReason = "MAX_GENERATIONS";
        }
        
        return bestSol;
    }
    
    private boolean checkStoppingCriteria(int generation) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime >= MAX_TIME_MS) {
            stopReason = "TIME_LIMIT";
            return true;
        }
        
        if (generation >= MAX_GENERATIONS) {
            stopReason = "MAX_GENERATIONS";
            return true;
        }
        
        if (generationsNoImprovement >= MAX_GENERATIONS_NO_IMPROVEMENT) {
            stopReason = "NO_IMPROVEMENT";
            return true;
        }
        
        return false;
    }
    
    public String getStopReason() {
        return stopReason;
    }
    
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    public int getActualGenerations() {
        return actualGenerations;
    }
    
    public static void main(String[] args) throws IOException {
        String[] instances = {
    		"instances/scqbf/n25p1.txt",
            "instances/scqbf/n25p2.txt",
            "instances/scqbf/n25p3.txt",
            "instances/scqbf/n50p1.txt",
            "instances/scqbf/n50p2.txt",
            "instances/scqbf/n50p3.txt",
            // "instances/scqbf/n100p1.txt",
            // "instances/scqbf/n100p2.txt",
            // "instances/scqbf/n100p3.txt",
            // "instances/scqbf/n200p1.txt",
            // "instances/scqbf/n200p2.txt",
            // "instances/scqbf/n200p3.txt",
            // "instances/scqbf/n400p1.txt",
            // "instances/scqbf/n400p2.txt",
            // "instances/scqbf/n400p3.txt"
        };
        
        Object[][] configs = {
            {"STANDARD", 100, 0.01, false, false, "standard", "standard"},
            {"STANDARD_P2", 200, 0.01, false, false, "standard", "standard"},
            {"STANDARD_M2", 100, 0.05, false, false, "standard", "standard"},
            {"FUNC_POP", 100, 0.01, true, false, "standard", "standard"},
            {"FUNC_MUT", 100, 0.01, false, true, "standard", "standard"},
            {"STANDARD_EVOL1", 100, 0.01, false, false, "uniform_crossover", "standard"},
            {"STANDARD_MUT_EVOL1", 100, 0.01, false, true, "uniform_crossover", "standard"},
            {"STANDARD_EVOL_2", 100, 0.01, false, false, "standard", "adaptive_mutation"},
            {"POP_EVOL_2", 100, 0.01, true, false, "standard", "adaptive_mutation"},
        };
        
        PrintWriter csvWriter = new PrintWriter(new FileWriter("ga_results.csv"));
        csvWriter.println("Configuration,Instance,BestSolution,Time_ms,Generations,StopReason");

        for (String instanceFile : instances) {
            for (Object[] config : configs) {
                String configName = (String) config[0];
                int popSize = (Integer) config[1];
                double mutRate = (Double) config[2];
                boolean usePopFunc = (Boolean) config[3];
                boolean useMutFunc = (Boolean) config[4];
                String crossoverStrategy = (String) config[5];
                String mutationStrategy = (String) config[6];

                System.out.println("\n" + "=".repeat(50));
                System.out.println("Config: " + configName + " | Instance: " + instanceFile);
                System.out.println("=".repeat(50));

                SC_QBF scqbf = new SC_QBF(instanceFile);
                GA_SC_QBF ga;

                ga = new GA_SC_QBF(scqbf, MAX_GENERATIONS, popSize, mutRate,
                        usePopFunc, useMutFunc, crossoverStrategy, mutationStrategy);

                Solution<Integer> solution = ga.solve();

                csvWriter.printf("%s,%s,%.2f,%d,%d,%s%n",
                    configName,
                    instanceFile.split("/")[2],
                    solution.cost,
                    ga.getElapsedTime(),
                    ga.getActualGenerations(),
                    ga.getStopReason()
                );
                csvWriter.flush();

                System.out.println("Final: " + solution);
                System.out.println("Time: " + ga.getElapsedTime() + " ms | Stop: " + ga.getStopReason());
            }
        }
        
        csvWriter.close();
        System.out.println("\nResults saved to ga_results.csv");
    }
}