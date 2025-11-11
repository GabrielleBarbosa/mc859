package utils;

import com.jgalgo.alg.flow.Flow;
import com.jgalgo.alg.flow.MaximumFlow;
import com.jgalgo.alg.flow.MaximumFlowDinic;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import instance.QuantumRoutingInstance;
import solution.QuantumRoutingSolution;
import com.jgalgo.graph.Graph;


public class MaxFlowSolver {

    public static QuantumRoutingSolution solve(final QuantumRoutingSolution current, final QuantumRoutingInstance instance, int request) {
        Flow<Integer, String> flow = calculateFlowApproximation(current, instance, 0);
        return null;
    }

    private static Flow<Integer, String> calculateFlowApproximation(final QuantumRoutingSolution current, final QuantumRoutingInstance instance, int request) {
        Graph<Integer, String> graph = Graph.newUndirected();
        for (int i = 0; i < instance.getSize(); i++) {
            graph.addVertex(i);
        }
        WeightsFloat<String> weights = graph.addEdgesWeights("capacity", float.class);

        for (int i = 0; i < instance.getArcs().size(); i++) {
            for (int e = 0; e < instance.getArcs().size(); e++) {
                Pair<Integer, Float> arc = instance.getArcs().get(i).get(e);
                if (arc != null) {
                    graph.addEdge(i, e, i + "_" + e);
                    weights.set(i + "_" + e, (arc.getFirst() - current.getZa().get(i).get(e)) * arc.second);
                }
            }
        }

        Pair<Integer, Integer> requestPair = instance.getRequests().get(request);
        return MaximumFlow.newInstance().computeMaximumFlow(graph, weights, requestPair.getFirst(), requestPair.getSecond());
    }

    private static QuantumRoutingSolution convertToSolution(final QuantumRoutingSolution current, final QuantumRoutingInstance instance, int request) {
        return null;
    }

    private static QuantumRoutingSolution fixSolution() {
        return null;
    }
}
