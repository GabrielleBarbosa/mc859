package metaheuristics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import instance.QuantumRoutingInstance;
import solution.QuantumRoutingSolution;
import utils.Pair;

public abstract class AbstractTS<E> {
    public static boolean verbose = true;

    static Random rng = new Random(0);

    protected QuantumRoutingInstance instance;

    protected Double bestCost;

    protected Double cost;

    protected QuantumRoutingSolution bestSol;

    protected QuantumRoutingSolution sol;

    protected Integer iterations;

    /**
     * the tabu tenure.
     */
    protected Integer tenure;

    /**
     * the Candidate List of elements to enter the solution.
     */
    protected ArrayList<E> CL;

    /**
     * the Restricted Candidate List of elements to enter the solution.
     */
    protected ArrayList<E> RCL;

    /**
     * the Tabu List of elements to enter the solution.
     */
    protected ArrayDeque<E> TL;

    public AbstractTS(QuantumRoutingInstance instance, Integer tenure, Integer iterations) {
        this.instance = instance;
        this.tenure = tenure;
        this.iterations = iterations;
    }

    public abstract ArrayList<E> makeCL();

    public abstract ArrayList<E> makeRCL();

    /**
     * Creates the Tabu List, which is an ArrayDeque of the Tabu
     * candidate elements. The number of iterations a candidate
     * is considered tabu is given by the Tabu Tenure {@link #tenure}
     *
     * @return The Tabu List.
     */
    public abstract ArrayDeque<E> makeTL();

    /**
     * Updates the Candidate List according to the incumbent solution
     * {@link #sol}. In other words, this method is responsible for
     * updating the costs of the candidate solution elements.
     */
    public abstract void updateCL();

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
    public abstract QuantumRoutingSolution neighborhoodMove();

    /**
     * The TS constructive heuristic, which is responsible for building a
     * feasible solution by selecting in a greedy fashion, candidate
     * elements to enter the solution.
     *
     * @return A feasible solution to the problem being minimized.
     */
    public QuantumRoutingSolution constructiveHeuristic() {

        sol = createEmptySol();

        cost = Double.POSITIVE_INFINITY;

        /* Main loop, which repeats until the stopping criteria is reached. */
        while (!constructiveStopCriteria()) {

            Double maxCost = Double.NEGATIVE_INFINITY, minCost = Double.POSITIVE_INFINITY;
            cost = sol.cost;
            updateCL();

            /*
             * Explore all candidate elements to enter the solution, saving the
             * highest and lowest cost variation achieved by the candidates.
             */
            for (E c : CL) {
                Double deltaCost = ObjFunction.evaluateInsertionCost(c, sol);
                if (deltaCost < minCost)
                    minCost = deltaCost;
                if (deltaCost > maxCost)
                    maxCost = deltaCost;
            }

            /*
             * Among all candidates, insert into the RCL those with the highest
             * performance.
             */
            for (E c : CL) {
                Double deltaCost = ObjFunction.evaluateInsertionCost(c, sol);
                if (deltaCost <= minCost) {
                    RCL.add(c);
                }
            }

            /* Choose a candidate randomly from the RCL */
            int rndIndex = rng.nextInt(RCL.size());
            E inCand = RCL.get(rndIndex);
            CL.remove(inCand);
            sol.add(inCand);
            ObjFunction.evaluate(sol);
            RCL.clear();

        }

        return sol;
    }

    private QuantumRoutingSolution findMaxFlux(final int requestInd, final QuantumRoutingInstance instance, final QuantumRoutingSolution currentSol) {
        if (requestInd <= instance.getSize()) {
            return null;
        }

        Pair<Integer, Integer> sourceDestPair = instance.getRequests().get(requestInd);

        if (sourceDestPair == null || Objects.equals(sourceDestPair.getFirst(), sourceDestPair.getSecond())) {
            return null;
        }

        QuantumRoutingSolution partialSol = new QuantumRoutingSolution(sol);

        int[] bfsPath = new int[instance.getSize()];

        float maxFlow = 0;


    }

    /**
     * The TS mainframe. It consists of a constructive heuristic followed by
     * a loop, in which each iteration a neighborhood move is performed on
     * the current solution. The best solution is returned as result.
     *
     * @return The best feasible solution obtained throughout all iterations.
     */
    public QuantumRoutingSolution solve() {

        bestSol = createEmptySol();
        constructiveHeuristic();
        TL = makeTL();
        for (int i = 0; i < iterations; i++) {
            neighborhoodMove();
            if (bestSol.cost > sol.cost) {
                bestSol = new Solution<E>(sol);
                if (verbose)
                    System.out.println("(Iter. " + i + ") BestSol = " + bestSol);
            }
        }

        return bestSol;
    }

    /**
     * A standard stopping criteria for the constructive heuristic is to repeat
     * until the incumbent solution improves by inserting a new candidate
     * element.
     *
     * @return true if the criteria is met.
     */
    public Boolean constructiveStopCriteria() {
        return (cost > sol.cost) ? false : true;
    }

}
