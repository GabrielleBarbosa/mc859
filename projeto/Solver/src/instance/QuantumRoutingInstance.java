package instance;

import utils.Pair;
import java.util.List;


public class QuantumRoutingInstance {
    
    private int size;

    private List<Integer> nodes;

    private List<List<Pair<Integer, Float>>> arcs;

    private List<Pair<Integer, Integer>> requests;

    public QuantumRoutingInstance(int size, List<Integer> nodes,
                                  List<List<Pair<Integer, Float>>> arcs,
                                  List<Pair<Integer, Integer>> requests) {
        this.size = size;
        this.nodes = nodes;
        this.arcs = arcs;
        this.requests = requests;
    }

    public QuantumRoutingInstance(String filename) {

    }

    public int getSize() {
        return size;
    }

    public List<Integer> getNodes() {
        return nodes;
    }

    public List<List<Pair<Integer, Float>>> getArcs() {
        return arcs;
    }

    public List<Pair<Integer, Integer>> getRequests() {
        return requests;
    }
}