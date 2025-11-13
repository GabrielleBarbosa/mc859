package metaheuristics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import instance.QuantumRoutingInstance;
import solution.QuantumRoutingSolution;
import solution.SolutionMetadata;
import utils.MaxFlowSolver;
import utils.Pair;

public class QuantumRoutingTS {
    public static boolean verbose = true;

    protected QuantumRoutingInstance instance;

    protected QuantumRoutingSolution bestSol;

    protected QuantumRoutingSolution sol;

    protected List<SolutionMetadata> bestSolutions;

    protected HashMap<Pair<Integer, Integer>, Integer> edgeUsage;

    protected Random rng;

    protected OptionsTS opts;

    protected Integer tenure;

    protected ArrayDeque<NeighborhoodMove> TL;

    public QuantumRoutingTS(QuantumRoutingInstance instance, Integer tenure, OptionsTS opts) {
        this.bestSolutions = new ArrayList<>();
        this.edgeUsage = new HashMap<>();
        this.instance = instance;
        this.tenure = tenure;
        this.rng = new Random(opts.rngSeed);
        this.opts = opts;
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
    public ArrayDeque<NeighborhoodMove> makeTL() {
        return new ArrayDeque<>();
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

    private void computeEdgeUsage(QuantumRoutingSolution sol) {
        for (List<List<Integer>> request : sol.getXra()) {
            for (int i = 0; i < request.size(); i++) {
                for (int j = 0; j < request.get(i).size(); j++) {
                    if (request.get(i).get(j) > 0) {
                        Pair<Integer, Integer> edge = new Pair<>(i, j);
                        this.edgeUsage.put(edge, this.edgeUsage.getOrDefault(edge, 0) + 1);
                    }
                }
            }
        }
    }

    private void findPathsDFS(
            int current,
            int target,
            List<List<Integer>> flow,
            List<Pair<Integer, Integer>> currentPath,
            List<List<Pair<Integer, Integer>>> allPaths,
            boolean[] visited) {

        if (current == target) {
            allPaths.add(currentPath);
            return;
        }

        visited[current] = true;

        for (int next = 0; next < flow.get(current).size(); next++) {
            int value = flow.get(current).get(next);
            if (value > 0 && !visited[next]) {
                currentPath.add(new Pair<>(current, next));
                findPathsDFS(next, target, flow, new ArrayList<>(currentPath), allPaths, visited);
                currentPath.remove(currentPath.size() - 1);
            }
        }

        visited[current] = false;
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

        List<List<List<Pair<Integer, Integer>>>> requestPaths = calculateReqPaths(currentSol);
        Map<Pair<Integer, Integer>, List<int[]>> subpaths = calculateSubpaths(requestPaths);
        List<Pair<int[], int[]>> possibleExchanges = calculatePossibleExchanges(subpaths);

        if (!possibleExchanges.isEmpty() && opts.exchangeEnabled) {
            int exchangeIdx = this.rng.nextInt(possibleExchanges.size());
            Pair<int[], int[]> exchange = possibleExchanges.get(exchangeIdx);
            int[] candidate1 = exchange.getFirst();
            int[] candidate2 = exchange.getSecond();

            makeExchangeOfPaths(candidate1, candidate2, requestPaths, currentSol);
        }

        if (!subpaths.isEmpty() && opts.removeEnabled) {
            List<Pair<Integer, Integer>> subPathKeys = new ArrayList<>(subpaths.keySet());
            Pair<Integer, Integer> subpathToRemove = subPathKeys.get(this.rng.nextInt(subPathKeys.size()));
            int[] requestToRemove = subpaths.get(subpathToRemove).get(this.rng.nextInt(subpaths.get(subpathToRemove).size()));

            int r = requestToRemove[0];
            int p = requestToRemove[1];
            int start = requestToRemove[2];
            int end = requestToRemove[3];
            List<Pair<Integer, Integer>> path = requestPaths.get(r).get(p);

            NeighborhoodMove move = NeighborhoodMove.RemoveFlow(r, subpathToRemove);
            if (!TL.contains(move)) {
                List<Pair<Integer, Integer>> subpath = new ArrayList<>(path.subList(start, end + 1));
                for (Pair<Integer, Integer> edge : subpath) {
                    currentSol.getXra().get(r).get(edge.getFirst()).set(edge.getSecond(), 0);
                }
                TL.add(move);
            }

        }

        this.computeEdgeUsage(currentSol);
        return currentSol;
    }

    private List<List<List<Pair<Integer, Integer>>>> calculateReqPaths(QuantumRoutingSolution currentSol) {
        List<List<List<Pair<Integer, Integer>>>> requestPaths = new ArrayList<>();
        List<Pair<Integer, Integer>> requests = this.instance.getRequests();
        for (int r = 0; r < requests.size(); r++) {
            Integer source = requests.get(r).getFirst();
            Integer destiny = requests.get(r).getSecond();
            List<List<Integer>> requestFlow = currentSol.getXra().get(r);

            for (int i = 0; i < requestFlow.size(); i++) {
                for (int j = 0; j < requestFlow.get(i).size(); j++) {
                    if (requestFlow.get(i).get(j) < 0) {
                        requestFlow.get(i).set(j, - requestFlow.get(i).get(j));
                    }
                }
            }

            List<List<Pair<Integer, Integer>>> paths = new ArrayList<>();
            boolean[] visited = new boolean[requestFlow.size()];

            findPathsDFS(source, destiny, requestFlow, new ArrayList<>(), paths, visited);
            requestPaths.add(paths);
        }

        return requestPaths;
    }

    private void makeExchangeOfPaths(int[] candidate1, int[] candidate2, List<List<List<Pair<Integer, Integer>>>> requestPaths, QuantumRoutingSolution currentSol) {
        int req1 = candidate1[0];
        int req2 = candidate2[0];

        int start1 = candidate1[2];
        int end1 = candidate1[3];
        int path_idx1 = candidate1[1];
        List<Pair<Integer, Integer>> path1 = requestPaths.get(req1).get(path_idx1);
        NeighborhoodMove move = NeighborhoodMove.FlowExchange(req1, req2, new Pair<>(path1.get(start1).getFirst(), path1.get(end1).getSecond()));
        if (!TL.contains(move)) {
            List<Pair<Integer, Integer>> subpath1 = new ArrayList<>(path1.subList(start1, end1 + 1));
            for (Pair<Integer, Integer> edge : subpath1) {
                Integer v1 = currentSol.getXra().get(req1).get(edge.getFirst()).get(edge.getSecond());
                Integer v2 = currentSol.getXra().get(req2).get(edge.getFirst()).get(edge.getSecond());
                currentSol.getXra().get(req1).get(edge.getFirst()).set(edge.getSecond(), v2);
                currentSol.getXra().get(req2).get(edge.getFirst()).set(edge.getSecond(), v1);
            }

            int start2 = candidate2[2];
            int end2 = candidate2[3];
            int path_idx2 = candidate2[1];
            List<Pair<Integer, Integer>> path2 = requestPaths.get(req2).get(path_idx2);
            List<Pair<Integer, Integer>> subpath2 = new ArrayList<>(path2.subList(start2, end2 + 1));
            for (Pair<Integer, Integer> edge : subpath2) {
                if (!subpath1.contains(edge)) {
                    Integer v1 = currentSol.getXra().get(req1).get(edge.getFirst()).get(edge.getSecond());
                    Integer v2 = currentSol.getXra().get(req2).get(edge.getFirst()).get(edge.getSecond());
                    currentSol.getXra().get(req1).get(edge.getFirst()).set(edge.getSecond(), v2);
                    currentSol.getXra().get(req2).get(edge.getFirst()).set(edge.getSecond(), v1);
                }
            }

            TL.add(move);
        }
    }

    private List<Pair<int[], int[]>> calculatePossibleExchanges(Map<Pair<Integer, Integer>, List<int[]>> subpaths) {
        List<Pair<int[], int[]>> possibleExchanges = new ArrayList<>();
        for (Pair<Integer, Integer> originDest : subpaths.keySet()) {
            List<int[]> candidates = subpaths.get(originDest);
            if (candidates.size() > 1) {
                for (int i = 0; i < candidates.size(); i++) {
                    for (int j = i + 1; j < candidates.size(); j++) {
                        int[] candidate1 = candidates.get(i);
                        int[] candidate2 = candidates.get(j);

                        if (candidate1[0] != candidate2[0]) { // different requests
                            possibleExchanges.add(new Pair<>(candidate1, candidate2));
                        }
                    }
                }
            }
        }
        return possibleExchanges;
    }

    private Map<Pair<Integer, Integer>, List<int[]>> calculateSubpaths(List<List<List<Pair<Integer, Integer>>>> requestPaths) {
        Map<Pair<Integer, Integer>, List<int[]>> subpaths = new HashMap<>();
        for (int r = 0; r < requestPaths.size(); r++) {
            for (int p = 0; p < requestPaths.get(r).size(); p++) {
                List<Pair<Integer, Integer>> path = requestPaths.get(r).get(p);
                for (int i = 0; i < path.size(); i++) {
                    for (int j = i; j < path.size(); j++) {
                        Pair<Integer, Integer> originDest = new Pair<>(path.get(i).getFirst(), path.get(j).getSecond());
                        subpaths.computeIfAbsent(originDest, k -> new ArrayList<>()).add(new int[]{r, p, i, j});
                    }
                }
            }
        }
        return subpaths;
    }

    /**
     * The TS constructive heuristic, which is responsible for building a
     * feasible solution by selecting in a greedy fashion, candidate
     * elements to enter the solution.
     *
     * @return A feasible solution to the problem being maximized.
     */
    public QuantumRoutingSolution randomGreedyHeuristic(final QuantumRoutingSolution startingSol) {

        QuantumRoutingSolution currentSol = new QuantumRoutingSolution(startingSol);

        List<Integer> randomRequestList = IntStream.rangeClosed(0, instance.getRequests().size() - 1)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(randomRequestList, this.rng);

        while (!randomRequestList.isEmpty()) {

            Integer request = randomRequestList.remove(0);

            currentSol = MaxFlowSolver.solve(currentSol, instance, request);
        }

        return currentSol;
    }

    public QuantumRoutingSolution greedyGreedyHeuristic(final QuantumRoutingSolution startingSol) {

        QuantumRoutingSolution currentSol = new QuantumRoutingSolution(startingSol);

        List<Integer> requestList = IntStream.rangeClosed(0, instance.getRequests().size())
                .boxed()
                .collect(Collectors.toList());

        while (!requestList.isEmpty()) {
            int bestRequest = 0;
            for (int i = 0; i < requestList.size(); i++) {
                Integer request = requestList.get(i);
                QuantumRoutingSolution newSol = MaxFlowSolver.solve(currentSol, instance, request);
                if (newSol != null && newSol.getCost() >= currentSol.getCost()) {
                    currentSol = newSol;
                    bestRequest = i;
                }
            }
            requestList.remove(bestRequest);
        }

        return currentSol;
    }

    /**
     * The TS mainframe. It consists of a constructive heuristic followed by
     * a loop, in which each iteration a neighborhood move is performed on
     * the current solution. The best solution is returned as result.
     *
     * @return The best feasible solution obtained throughout all iterations.
     */
    public List<SolutionMetadata> solve() {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = this.opts.timeoutSeconds * 1000L;

        bestSol = randomGreedyHeuristic(createEmptySol());
        sol = bestSol;
        TL = makeTL();

        int i;
        for (i = 0; i < this.opts.iterations; i++) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (opts.target != null && bestSol.getCost() >= opts.target) {
                if (verbose)
                    System.out.println("Target reached after " + i + " iterations (" +
                            (elapsed / 1000.0) + "s). Stopping early.");
                break;
            }
            if (elapsed >= timeoutMillis) {
                if (verbose)
                    System.out.println("Timeout reached after " + i + " iterations (" +
                            (elapsed / 1000.0) + "s). Stopping early.");
                break;
            }
            this.sol = neighborhoodMove();
            if (bestSol.getCost() > sol.getCost()) {
                bestSol = new QuantumRoutingSolution(sol);
                if (verbose)
                    System.out.println("(Iter. " + i + ") BestSol = " + bestSol.getCost());
                bestSolutions.add(new SolutionMetadata(bestSol, elapsed, i+1));
            }
        }

        // Complete the solution with any remaining flow possible
        bestSol = randomGreedyHeuristic(bestSol);
        long elapsed = System.currentTimeMillis() - startTime;
        bestSolutions.add(new SolutionMetadata(bestSol, elapsed, i));
        return bestSolutions;
    }

}
