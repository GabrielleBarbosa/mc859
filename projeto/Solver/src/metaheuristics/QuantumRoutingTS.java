package metaheuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.jgalgo.alg.common.Path;
import com.jgalgo.alg.shortestpath.ShortestPathSingleSource;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightsFloat;
import instance.QuantumRoutingInstance;
import metaheuristics.neighbourhoodMove.AddFlow;
import metaheuristics.neighbourhoodMove.FlowExchange;
import metaheuristics.neighbourhoodMove.NeighborhoodMove;
import metaheuristics.neighbourhoodMove.RemoveFlow;
import solution.QuantumRoutingSolution;
import solution.SolutionMetadata;
import utils.Pair;

public class QuantumRoutingTS {

    protected static final String WEIGHT_KEY = "cost";

    public static boolean verbose = true;

    protected QuantumRoutingInstance instance;

    protected Graph<Integer, String> restrictionGraph;

    protected QuantumRoutingSolution bestSol;

    protected QuantumRoutingSolution currentSol;

    protected Random rng;

    protected OptionsTS opts;


    protected List<SolutionMetadata> bestSolutions;

    //List[request, path]
    protected List<Pair<Integer, List<Pair<Integer, Integer>>>> bestPaths;

    protected HashMap<Pair<Integer, Integer>, Integer> edgeUsage;

    protected Integer tenure;

    protected QuantumRoutingTabuTable TL;

    public QuantumRoutingTS(QuantumRoutingInstance instance, Integer tenure, OptionsTS opts) {
        this.bestSolutions = new ArrayList<>();
        this.edgeUsage = new HashMap<>();
        this.instance = instance;
        this.tenure = tenure;
        this.rng = new Random(opts.rngSeed);
        this.opts = opts;
    }

    /**
     * Creates the Tabu List, which is an ArrayDeque of the Tabu
     * candidate elements. The number of iterations a candidate
     * is considered tabu is given by the Tabu Tenure {@link #tenure}
     *
     * @return The Tabu List.
     */
    public void makeTL() {
        TL = new QuantumRoutingTabuTable(tenure * instance.getRequests().size());
    }

    //Metodos de criação de instâncias

    //Cria uma solução vazia e o grafo de restrições
    protected void createEmptySol() {
        Graph<Integer, String> graph = Graph.newDirected();

        for (int i = 0; i < instance.getSize(); i++) {
            graph.addVertex(i);
        }

        WeightsFloat<String> weights = graph.addEdgesWeights(WEIGHT_KEY, float.class);

        for (int i = 0; i < instance.getArcs().size(); i++) {
            for (int e = 0; e < instance.getArcs().size(); e++) {
                Pair<Integer, Float> arc = instance.getArcs().get(i).get(e);
                if (arc != null) {
                    graph.addEdge(i, e, i + "_" + e);
                    weights.set(i + "_" + e, 1f / arc.getSecond());
                }
            }
        }

        restrictionGraph = graph;
        currentSol = new QuantumRoutingSolution(instance);
    }

    //Adciona os caminhos mais usados da ultima melhor solução à nova
    protected void intensify() {
        for (Pair<Integer, List<Pair<Integer, Integer>>> requestPathPair : bestPaths) {
            Integer request = requestPathPair.getFirst();
            List<Pair<Integer, Integer>> path = requestPathPair.getSecond();

            incrementMemory(path.get(0).getFirst());

            for (Pair<Integer, Integer> edge : path) {
                Integer u = edge.getFirst();
                Integer v = edge.getSecond();

                incrementMemory(v);
                incrementEdgeUsage(request, u, v);
            }

            currentSol.getPathsPerRequest().get(request).add(path);
            currentSol.getThroughputPerRequest().set(request, currentSol.getThroughputPerRequest().get(request) + 1);
        }
    }

    private void diversify() {
        //TODO
    }

    //Preenche a solução atual de maneira gulosa
    protected void randomGreedyHeuristic() {
        boolean added = addFlowMove();
        while (added) {
            added = addFlowMove();
        }
    }

    //TODO
    //Métodos para troca de caminhos
    protected List<Integer> shuffleReqIndexes(int n) {
        List<Integer> list = new ArrayList<>(n + 1);

        for (int i = 0; i < n; i++) {
            list.add(i);
        }

        Collections.shuffle(list, rng);
        return list;
    }

    private boolean subpathsEqual(List<Pair<Integer,Integer>> p1, int i1, int j1,
                                  List<Pair<Integer,Integer>> p2, int i2, int j2) {

        int len1 = j1 - i1 + 1;
        int len2 = j2 - i2 + 1;
        if (len1 != len2) {
            return false;
        }

        for (int k = 0; k < len1; k++) {
            if (!p1.get(i1 + k).equals(p2.get(i2 + k))){
                return false;
            }
        }
        return true;
    }

    private boolean findDifferingCommonSD(int r1_index, int r2_index, int p1_index, int p2_index) {
        List<Pair<Integer, Integer>> p1 = currentSol.getPathsPerRequest().get(r1_index).get(p1_index);
        List<Pair<Integer, Integer>> p2 = currentSol.getPathsPerRequest().get(r2_index).get(p2_index);
        int n1 = p1.size();
        int n2 = p2.size();

        for (int i1 = 0; i1 < n1; i1++) {
            int s = p1.get(i1).getFirst();

            for (int i2 = 0; i2 < n2; i2++) {
                if (p2.get(i2).getFirst() != s) {
                    continue;
                }

                int k1 = i1;
                int k2 = i2;

                while (k1 < n1 && k2 < n2) {
                    int d1 = p1.get(k1).getSecond();
                    int d2 = p2.get(k2).getSecond();

                    if (d1 == d2) {
                        if (!subpathsEqual(p1, i1, k1, p2, i2, k2)) {
                            NeighborhoodMove move = new FlowExchange(r1_index, r2_index, p1_index, p2_index);
                            if (!TL.contains(move)) {
                                List<Pair<Integer, Integer>> sub1 = new ArrayList<>(p1.subList(i1, k1 + 1));
                                List<Pair<Integer, Integer>> sub2 = new ArrayList<>(p2.subList(i2, k2 + 1));

                                List<Pair<Integer, Integer>> newP1 = new ArrayList<>();
                                if (i1 > 0) newP1.addAll(p1.subList(0, i1));
                                newP1.addAll(sub2);
                                if (k1 + 1 < p1.size()) newP1.addAll(p1.subList(k1 + 1, p1.size()));

                                Float p1Cost = evaluatePath(p1);
                                Float newP1Cost = evaluatePath(newP1);
                                if (newP1Cost < p1Cost) {
                                    continue;
                                }

                                List<Pair<Integer, Integer>> newP2 = new ArrayList<>();

                                if (i2 > 0) newP2.addAll(p2.subList(0, i2));
                                newP2.addAll(sub1);
                                if (k2 + 1 < p2.size()) newP2.addAll(p2.subList(k2 + 1, p2.size()));

                                Float p2Cost = evaluatePath(p2);
                                Float newP2Cost = evaluatePath(newP2);
                                if (newP2Cost < p2Cost) {
                                    continue;
                                }

                                currentSol.getPathsPerRequest().get(r1_index).set(p1_index, newP1);
                                currentSol.getPathsPerRequest().get(r2_index).set(p2_index, newP2);
                            }
                            return true;
                        }
                        break;
                    }

                    k1++;
                    k2++;
                }
            }
        }

        return false;
    }

    protected boolean exchangeFlowMove() {
        List<List<List<Pair<Integer, Integer>>>> paths = currentSol.getPathsPerRequest();
        List<Integer> requests = shuffleReqIndexes(paths.size());

        if (requests.size() < 2) {
            return false;
        }

        int r1_index = requests.get(0);
        int r2_index = requests.get(1);

        List<Integer> p1s = shuffleReqIndexes(paths.get(r1_index).size());
        List<Integer> p2s = shuffleReqIndexes(paths.get(r2_index).size());
        for (int p1_index : p1s) {
            for (int p2_index : p2s) {
                if (findDifferingCommonSD(r1_index, r2_index, p1_index, p2_index)) {
                    return true;
                }
            }
        }

        return false;
    }


    //Métodos para remover um fluxo
    protected boolean removeFlow() {
        float worseCost = 2; //path cost is never higher than 1
        int worseRequest = -1;
        int worseIndex = -1;

        List<Pair<Integer, Integer>> worsePath = null;
        for (int i = 0; i < instance.getRequests().size(); i++) {
            List<List<Pair<Integer, Integer>>> currentPaths = currentSol.getPathsPerRequest().get(i);
            int index = 0;
            for (List<Pair<Integer, Integer>> path : currentPaths) {
                if (TL.contains(new AddFlow(i, index))) {
                    continue;
                }
                float currentCost = evaluatePath(path);
                if (currentCost < worseCost) {
                    worseCost = currentCost;
                    worsePath = path;
                    worseRequest = i;
                    worseIndex = index;
                }
                index++;
            }
        }
        if (worsePath == null) {
            return false;
        }

        currentSol.getPathsPerRequest().get(worseRequest).remove(worsePath);

        decrementMemory(worsePath.get(0).getFirst());

        for (Pair<Integer, Integer> edge : worsePath) {
            decrementMemory(edge.getSecond());
            decrementEdgeUsage(worseRequest, edge.getFirst(), edge.getSecond());
        }
        TL.add(new RemoveFlow(worseRequest, worsePath));
        TL.removeExchange(worseRequest, worseIndex);
        currentSol.getThroughputPerRequest().set(worseRequest, currentSol.getThroughputPerRequest().get(worseRequest) - 1);

        return true;
    }

    protected void decrementMemory(int node) {
        int currentUsage = currentSol.getMemoryUsage().get(node);
        currentSol.getMemoryUsage().set(node, currentUsage - 1);
        if (currentUsage == instance.getNodes().get(node)) {
            restrictionGraph.addVertex(node);
            //Adciona de volta as arestas que entram
            for (int i = 0; i < instance.getSize(); i ++) {
                Pair<Integer, Float> edge = instance.getArcs().get(i).get(node);
                try {
                    if (edge != null &&
                            currentSol.getEdgeUsage().get(i).get(node) < edge.getFirst() &&
                            !restrictionGraph.containsEdge(i, node)) {
                        restrictionGraph.addEdge(i, node, i + "_" + node);
                        restrictionGraph.edgesWeights(WEIGHT_KEY).setAsObj(
                                i + "_" + node,
                                1 / instance.getArcs().get(i).get(node).getSecond()
                        );
                    }
                } catch (NoSuchVertexException ignored) {
                }
            }
            //Adciona de volta as arestas que saem
            for (int i = 0; i < instance.getSize(); i ++) {
                Pair<Integer, Float> edge = instance.getArcs().get(node).get(i);
                try {
                    if (edge != null &&
                            currentSol.getEdgeUsage().get(node).get(i) < edge.getFirst() &&
                            !restrictionGraph.containsEdge(node, i)) {
                        restrictionGraph.addEdge(node, i, node + "_" + i);
                        restrictionGraph.edgesWeights(WEIGHT_KEY).setAsObj(
                                node + "_" + i,
                                1 / instance.getArcs().get(node).get(i).getSecond()
                        );
                    }
                } catch (NoSuchVertexException ignored) {
                }
            }
        }
    }

    protected void decrementEdgeUsage(int request, int source, int dest) {
        //Atualiza o fluxo atual da aresta por request
        Integer currentFlow = currentSol.getEdgeUsagePerRequest().get(request).get(source).get(dest);
        currentSol.getEdgeUsagePerRequest().get(request).get(source).set(dest, currentFlow - 1);

        //Atualiza o fluxo atual da aresta
        currentFlow = currentSol.getEdgeUsage().get(source).get(dest);
        currentSol.getEdgeUsage().get(source).set(dest, currentFlow - 1);

        //Atualiza o grafo caso a aresta ainda exista (pode ter sido removida com a restrição de memória de nó)
        if (currentFlow.equals(instance.getArcs().get(source).get(dest).getFirst())) {
            restrictionGraph.addEdge(source, dest, source + "_" + dest);
            restrictionGraph.edgesWeights(WEIGHT_KEY).setAsObj(
                    source + "_" + dest,
                    1 / instance.getArcs().get(source).get(dest).getSecond());
        }
    }

    //Métodos para adcionar um fluxo

    protected boolean addFlowMove() {
        List<Integer> randomRequestList = IntStream.rangeClosed(0, instance.getRequests().size() - 1)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(randomRequestList, rng);

        for (Integer request: randomRequestList) {
            Pair<Integer, Integer> requestPair = instance.getRequests().get(request);
            //Checa se os vértices de entrada e saída têm capacidade (estão no grafo de restrição)
            if (!restrictionGraph.vertices().contains(requestPair.getFirst()) || !restrictionGraph.vertices().contains(requestPair.getSecond())) {
                continue;
            }
            //Calcula o melhor caminho entre source e dest
            ShortestPathSingleSource.Result<Integer, String> res = ShortestPathSingleSource.newInstance().computeShortestPaths(restrictionGraph, restrictionGraph.edgesWeights(WEIGHT_KEY), requestPair.getFirst());

            //Atualiza a solução e o grafo de restrições
            List<Pair<Integer, Integer>> newPath = new ArrayList<>();
            Path<Integer, String> path = res.getPath(requestPair.getSecond());
            if (path == null) {
                continue;
            }
            for (String e : path.edges()) {
                newPath.add(new Pair<>(restrictionGraph.edgeSource(e), restrictionGraph.edgeTarget(e)));
            }

            if (TL.contains(new RemoveFlow(request, newPath))) {
                continue;
            }

            incrementMemory(path.vertices().get(0));
            for (Pair<Integer, Integer> edge : newPath) {
                Integer u = edge.getFirst();
                Integer v = edge.getSecond();

                incrementMemory(v);
                incrementEdgeUsage(request, u, v);
            }

            TL.add(new AddFlow(request, currentSol.getPathsPerRequest().get(request).size()));
            currentSol.getPathsPerRequest().get(request).add(newPath);
            currentSol.getThroughputPerRequest().set(request, currentSol.getThroughputPerRequest().get(request) + 1);

            return true;
        }
        return false;
    }

    protected void incrementMemory(int node) {
        int currentUsage = currentSol.getMemoryUsage().get(node);
        currentSol.getMemoryUsage().set(node, currentUsage + 1);

        if (Objects.equals(currentUsage + 1, instance.getNodes().get(node))) {
            restrictionGraph.removeVertex(node);
        }
    }

    protected void incrementEdgeUsage(int request, int source, int dest) {
        //Atualiza o fluxo atual da aresta por request
        Integer currentFlow = currentSol.getEdgeUsagePerRequest().get(request).get(source).get(dest);
        currentSol.getEdgeUsagePerRequest().get(request).get(source).set(dest, currentFlow + 1);

        //Atualiza o fluxo atual da aresta
        currentFlow = currentSol.getEdgeUsage().get(source).get(dest);
        currentSol.getEdgeUsage().get(source).set(dest, currentFlow + 1);

        //Atualiza o grafo caso a aresta ainda exista (pode ter sido removida com a restrição de memória de nó)
        if (currentFlow + 1 == instance.getArcs().get(source).get(dest).getFirst()) {
            boolean contains;
            try {
               contains = restrictionGraph.containsEdge(source, dest);
            } catch (Exception e) {
                contains = false;
            }
            if (contains) {
                restrictionGraph.removeEdge(source + "_" + dest);
            }
        }
    }

    //Métodos para coleta de métricas para intensificação e diversificação

    protected void computeEdgeUsage(QuantumRoutingSolution sol) {
        for (List<List<Integer>> request : sol.getEdgeUsagePerRequest()) {
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

    protected void getBestPaths() {
        bestPaths = new ArrayList<>();
        List<Pair<Float, Pair<Integer, Integer>>> indexAndValues = new ArrayList<>();
        int request = 0, pathInd;
        for (List<List<Pair<Integer, Integer>>> paths: bestSol.getPathsPerRequest()) {
            pathInd = 0;
            for (List<Pair<Integer, Integer>> path : paths) {
                indexAndValues.add(new Pair<>(evaluatePath(path), new Pair<>(request, pathInd)));
                pathInd++;
            }
            request++;
        }

        indexAndValues.sort(Comparator
                .comparing(Pair<Float, Pair<Integer, Integer>>::getFirst)
                .reversed());

        for (int i = 0; i < Math.round(opts.intensifyRate * bestSol.getCost()); i++) {
            request = indexAndValues.get(i).getSecond().getFirst();
            pathInd = indexAndValues.get(i).getSecond().getSecond();
            List<Pair<Integer, Integer>> path = bestSol.getPathsPerRequest().get(request).get(pathInd);
            bestPaths.add(new Pair<>( request, path));
        }
    }

    //Métodos encapsuladores para a execução da heurística
    protected boolean neighbourhoodMove() {
        if (addFlowMove()) {
            return true;
        } else if (exchangeFlowMove()) {
            return true;
        }

        return removeFlow();
    }

    protected void evaluateNewSolution(final long elapsed, final int iteration) {
        if (currentSol.getCost() > bestSol.getCost()) {
            bestSol = new QuantumRoutingSolution(currentSol);
            bestSolutions.add(new SolutionMetadata(bestSol, elapsed, iteration));
            if (opts.diversifyEnabled) {
                //Collect edge usage
            }
        }
    }

    protected boolean evaluateReturn(final long elapsed, final long timeoutMillis, int i) {
        if (opts.target != null && bestSol.getCost() >= opts.target) {
            if (verbose)
                System.out.println("Target reached after " + i + " iterations (" +
                        (elapsed / 1000.0) + "s). Stopping early.");
            return true;
        }
        if (elapsed >= timeoutMillis) {
            if (verbose)
                System.out.println("Timeout reached after " + i + " iterations (" +
                        (elapsed / 1000.0) + "s). Stopping early.");
            return true;
        }
        return false;
    }

    private Float evaluatePath(List<Pair<Integer, Integer>> path) {
        float cost = 1;
        for (Pair<Integer, Integer> edge : path) {
            cost *= instance.getArcs().get(edge.getFirst()).get(edge.getSecond()).getSecond();
        }
        return cost;
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
        long elapsed;
        bestSol = new QuantumRoutingSolution(instance);
        int i = 0;

        for (int r = 0; r <= opts.restarts; r++) {

            createEmptySol();
            if (opts.intensifyEnabled) {
                intensify();
            }
            if (opts.diversifyEnabled) {
                //TODO diversify
            }
            randomGreedyHeuristic();
            elapsed = System.currentTimeMillis() - startTime;
            evaluateNewSolution(elapsed, r * opts.iterations + i);

            //Run local procedures
            for (i = 0; i < opts.iterations; i++) {
                if (evaluateReturn(elapsed, timeoutMillis, r * opts.iterations + i)) {
                    return bestSolutions;
                }
                if (neighbourhoodMove()) {
                    elapsed = System.currentTimeMillis() - startTime;
                    evaluateNewSolution(elapsed, r * opts.iterations + i);
                }
                //TODO check if should enable diversify edges
            }

            //Fill the current solution and validate it
            randomGreedyHeuristic();
            elapsed = System.currentTimeMillis() - startTime;
            evaluateNewSolution(elapsed, r * opts.iterations + i);
            evaluateReturn(elapsed, timeoutMillis, r * opts.iterations + i);

            if (opts.intensifyEnabled) {
                getBestPaths();
            }
        }

        return bestSolutions;
    }

    public Float practicalEvaluation() {
        float acc = 0;
        for (List<List<Pair<Integer, Integer>>> request : currentSol.getPathsPerRequest()) {
            for (List<Pair<Integer, Integer>> path : request) {
                float cost = 1;
                for (Pair<Integer, Integer> edge : path) {
                    cost *= instance.getArcs().get(edge.getFirst()).get(edge.getSecond()).getSecond();
                }
                acc += cost;
            }
        }
        return acc;
    }

    public void test() {
        createEmptySol();
        randomGreedyHeuristic();
        bestSol = new QuantumRoutingSolution(currentSol);
        getBestPaths();
        createEmptySol();
        intensify();
        practicalEvaluation();
    }
}
