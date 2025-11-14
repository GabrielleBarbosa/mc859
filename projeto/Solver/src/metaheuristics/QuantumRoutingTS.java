package metaheuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.jgalgo.alg.common.Path;
import com.jgalgo.alg.shortestpath.ShortestPathSingleSource;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightsFloat;
import instance.QuantumRoutingInstance;
import metaheuristics.neighbourhoodMove.AddFlow;
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

    protected List<Pair<Integer, List<Pair<Integer, Integer>>>> bestPaths;

    protected HashMap<Pair<Integer, Integer>, Integer> edgeUsage;

    protected Set<Pair<Integer, Integer>> lockedEdges;

    protected Integer tenure;

    protected QuantumRoutingTabuTable TL;

    public QuantumRoutingTS(QuantumRoutingInstance instance, Integer tenure, OptionsTS opts) {
        this.bestSolutions = new ArrayList<>();
        this.instance = instance;
        this.tenure = tenure;
        this.rng = new Random(opts.rngSeed);
        this.opts = opts;
        if (opts.diversifyEnabled) {
            this.edgeUsage = new HashMap<>();
            this.lockedEdges = new HashSet<>();
        }
        if (opts.intensifyEnabled) {
            bestPaths = new ArrayList<>();
        }

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

    //Preenche a solução atual de maneira gulosa
    protected void randomGreedyHeuristic() {
        boolean added = addFlowMove();
        while (added) {
            added = addFlowMove();
        }
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

    //Remove arestas mais utilizadas do grafo que calcula caminhos
    private void diversify() {
        List<Map.Entry<Pair<Integer, Integer>, Integer>> edges = new ArrayList<>(edgeUsage.entrySet());
        edges.sort(Map.Entry.comparingByValue());

        for (int i = Math.round(opts.diversifyRate * edges.size() - 1); i >= 0; i--) {
            lockedEdges.add(edges.get(i).getKey());
        }

        for (Pair<Integer, Integer> edge: lockedEdges) {
            if (restrictionGraph.containsEdge(edge.getFirst(), edge.getSecond())) {
                restrictionGraph.removeEdge(edge.getFirst() + "_" + edge.getSecond());
            }
        }
    }

    //Adciona de volta as arestas travadas pela diversificação
    private void unlockGraph() {
        for (Pair<Integer, Integer> edge: lockedEdges) {
            int source = edge.getFirst();
            int dest = edge.getSecond();
            if (!restrictionGraph.containsEdge(source, dest)) {
                if (currentSol.getEdgeUsage().get(source).get(dest) < instance.getArcs().get(source).get(dest).getFirst()) {
                    if (restrictionGraph.vertices().contains(source) && restrictionGraph.vertices().contains(dest)) {
                        restrictionGraph.addEdge(source, dest, source + "_" + dest);
                        restrictionGraph.edgesWeights(WEIGHT_KEY)
                                .setAsObj(source + "_" + dest, 1 / instance.getArcs().get(source).get(dest).getSecond());
                    }
                }
            }
        }
    }

    //TODO
    //Métodos para troca de caminhos


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
                            !restrictionGraph.containsEdge(i, node) &&
                            !lockedEdges.contains(new Pair<>(i, node))) {
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
                            !restrictionGraph.containsEdge(node, i) &&
                            !lockedEdges.contains(new Pair<>(node, i))) {
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

    protected void computeEdgeUsage() {
        for (int i = 0; i < instance.getSize(); i++) {
            for (int j = 0; j < instance.getSize(); j++) {
                if (bestSol.getEdgeUsage().get(i).get(j) > 0) {
                    Pair<Integer, Integer> edge = new Pair<>(i, j);
                    this.edgeUsage.put(edge, this.edgeUsage.getOrDefault(edge, 0) + 1);
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
        } else return removeFlow();
    }

    protected void evaluateNewSolution(final long elapsed, final int iteration) {
        if (currentSol.getCost() > bestSol.getCost()) {
            bestSol = new QuantumRoutingSolution(currentSol);
            bestSolutions.add(new SolutionMetadata(bestSol, elapsed, iteration));
            if (opts.diversifyEnabled) {
                computeEdgeUsage();
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
            makeTL();
            createEmptySol();
            if (opts.intensifyEnabled) {
                intensify();
            }
            if (opts.diversifyEnabled) {
                diversify();
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
                if (opts.diversifyEnabled && !lockedEdges.isEmpty() && i >= opts.diversifyDuration * opts.iterations) {
                    lockedEdges.clear();
                    unlockGraph();
                }
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
}
