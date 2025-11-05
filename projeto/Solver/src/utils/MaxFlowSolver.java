package utils;

import com.jgalgo.alg.flow.Flow;
import com.jgalgo.alg.flow.MaximumFlow;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import instance.QuantumRoutingInstance;
import solution.QuantumRoutingSolution;
import com.jgalgo.graph.Graph;


public class MaxFlowSolver {

    public static QuantumRoutingSolution solve(final QuantumRoutingSolution current, final QuantumRoutingInstance instance, int request) {
        Graph<Integer, String> graph = Graph.newUndirected();
        WeightsFloat<String> weights = graph.addEdgesWeights("capacity", Integer.class);
        for (int i = 0; i < instance.getSize(); i++) {
            graph.addVertex(i);
        }

        for (int i = 0; i < instance.getArcs().size(); i++) {
            for (int e = 0; e < instance.getArcs().size(); e++) {
                Pair<Integer, Float> arc = instance.getArcs().get(i).get(e);
                graph.addEdge(i, e, i + "" + e);
                weights.set(i + "" + e, arc.getFirst() - current.getZa().get(i).get(e));
            }
        }

        Pair<Integer, Integer> requestPair = instance.getRequests().get(request);
        MaximumFlow maxFlowAlg = MaximumFlow.newInstance();
        Flow<Integer, String> flow = maxFlowAlg.computeMaximumFlow(graph, weights, requestPair.getFirst(), requestPair.getSecond());
        return null;
    }

}
