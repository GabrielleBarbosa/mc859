package metaheuristics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import instance.QuantumRoutingInstance;
import solution.QuantumRoutingSolution;
import utils.Pair;

public class QuantumRoutingTS {
    public static boolean verbose = true;

    static Random rng = new Random(0);

    protected QuantumRoutingInstance instance;

    protected QuantumRoutingSolution bestSol;

    protected QuantumRoutingSolution sol;

    protected Integer iterations;

    /**
     * the tabu tenure.
     */
    protected Integer tenure;

    /**
     * the Tabu List of elements to enter the solution.
     */
    protected ArrayDeque<Integer> TL;

    public QuantumRoutingTS(QuantumRoutingInstance instance, Integer tenure, Integer iterations, OptionsTS opts) {
        this.instance = instance;
        this.tenure = tenure;
        this.iterations = iterations;
    }

    public ArrayList<Integer> makeCL(){
        return null;
    }

    public ArrayList<Integer> makeRCL() {
        return null;
    }

    /**
     * Creates the Tabu List, which is an ArrayDeque of the Tabu
     * candidate elements. The number of iterations a candidate
     * is considered tabu is given by the Tabu Tenure {@link #tenure}
     *
     * @return The Tabu List.
     */
    public ArrayDeque<Integer> makeTL() {
        return null;
    }

    /**
     * Updates the Candidate List according to the incumbent solution
     * {@link #sol}. In other words, this method is responsible for
     * updating the costs of the candidate solution elements.
     */
    public void updateCL() {

    }

    private QuantumRoutingSolution createEmptySol() {
        return new QuantumRoutingSolution(instance);
    }

    /**
     * The TS local search phase is responsible for repeatedly applying a
     * neighborhood operation while the solution is getting improved, i.e.,
     * until a local optimum is attained. When a local optimum is attained
     * the search continues by exploring moves which can make the current
     * solution worse. Cycling is prevented by not allowing forbidden
     * (tabu) moves that would otherwise backtrack to a previous solution.
     *
     * @return An local optimum solution.
     */
    public QuantumRoutingSolution neighborhoodMove() {
        QuantumRoutingSolution currentSol = new QuantumRoutingSolution(sol);
        int removedFlow = rng.nextInt(currentSol.getTr().size());
        currentSol.removeFlow(removedFlow);
        return currentSol;
    }

    /**
     * The TS constructive heuristic, which is responsible for building a
     * feasible solution by selecting in a greedy fashion, candidate
     * elements to enter the solution.
     *
     * @return A feasible solution to the problem being maximized.
     */
    public QuantumRoutingSolution randomGreedyHeuristic() {

        QuantumRoutingSolution currentSol = createEmptySol();

        List<Pair<Integer, Integer>> randomRequestList = new ArrayList<>(instance.getRequests());
        Collections.shuffle(randomRequestList, rng);

        while (!randomRequestList.isEmpty()) {

            Pair<Integer, Integer> sourceDestPair = randomRequestList.remove(0);

            currentSol = findMaxFlux(sourceDestPair.getFirst(), sourceDestPair.getSecond(), instance, currentSol);
        }

        return currentSol;
    }

    public QuantumRoutingSolution greedyGreedyHeuristic() {

        QuantumRoutingSolution currentSol = createEmptySol();

        List<Pair<Integer, Integer>> requestList = new ArrayList<>(instance.getRequests());

        while (!requestList.isEmpty()) {
            int bestRequest = 0;
            for (int i = 0; i < requestList.size(); i++) {
                Pair<Integer, Integer> request = requestList.get(i);
                QuantumRoutingSolution newSol = findMaxFlux(request.getFirst(), request.getSecond(), instance, currentSol);
                if (newSol != null && newSol.getCost() >= currentSol.getCost()) {
                    currentSol = newSol;
                    bestRequest = i;
                }
            }
            requestList.remove(bestRequest);
        }

        return currentSol;
    }

    private QuantumRoutingSolution findMaxFlux(final int source, final int dest, final QuantumRoutingInstance instance, final QuantumRoutingSolution currentSol) {
        if (dest == source) {
            return null;
        }

        QuantumRoutingSolution partialSol = new QuantumRoutingSolution(sol);

        int[] bfsPath = new int[instance.getSize()];

        float maxFlow = 0;

        return null;
    }

    /**
     * The TS mainframe. It consists of a constructive heuristic followed by
     * a loop, in which each iteration a neighborhood move is performed on
     * the current solution. The best solution is returned as result.
     *
     * @return The best feasible solution obtained throughout all iterations.
     */
    public QuantumRoutingSolution solve() {

        bestSol = randomGreedyHeuristic();
        TL = makeTL();
        for (int i = 0; i < iterations; i++) {
            neighborhoodMove();
            if (bestSol.getCost() > sol.getCost()) {
                bestSol = new QuantumRoutingSolution(sol);
                if (verbose)
                    System.out.println("(Iter. " + i + ") BestSol = " + bestSol);
            }
        }

        return bestSol;
    }
}
