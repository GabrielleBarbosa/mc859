package solution;

import instance.QuantumRoutingInstance;
import utils.Pair;

import java.util.*;

public class QuantumRoutingSolution {

    protected List<List<List<Integer>>> edgeUsagePerRequest;

    protected List<List<Integer>> edgeUsage;

    protected List<Integer> throughputPerRequest;

    protected List<List<List<Pair<Integer, Integer>>>> pathsPerRequest;

    protected List<Integer> memoryUsage;

    public QuantumRoutingSolution(final QuantumRoutingSolution solution) {
        this.edgeUsagePerRequest = new ArrayList<>(solution.getEdgeUsagePerRequest().size());
        for (int in = 0; in < solution.getEdgeUsagePerRequest().size(); in++) {
            List<List<Integer>> flows = new ArrayList<>();
            edgeUsagePerRequest.add(in, flows);
            for (List<Integer> flow : solution.getEdgeUsagePerRequest().get(in)) {
                flows.add(new ArrayList<>(flow));
            }
        }

        this.edgeUsage = new ArrayList<>(solution.getEdgeUsage().size());
        for (int in = 0; in < solution.getEdgeUsage().size(); in++) {
            edgeUsage.add(new ArrayList<>(solution.getEdgeUsage().get(in)));
        }

        this.throughputPerRequest = new ArrayList<>(solution.getThroughputPerRequest());

        this.memoryUsage = new ArrayList<>(solution.getMemoryUsage());

        this.pathsPerRequest = new ArrayList<>();
        for (int in = 0; in < solution.getPathsPerRequest().size(); in++) {
            List<List<Pair<Integer, Integer>>> pathList = new ArrayList<>();
            for (List<Pair<Integer, Integer>> path : solution.pathsPerRequest.get(in)) {
                pathList.add(new ArrayList<>(path));
            }
            pathsPerRequest.add(pathList);
        }
    }

    public Integer getCost() {
        int cost = 0;
        for (Integer t : throughputPerRequest) {
            cost += t;
        }
        return cost;
    }

    public QuantumRoutingSolution(final QuantumRoutingInstance instance) {
        this.edgeUsagePerRequest = new ArrayList<>(instance.getRequests().size());
        for (int r = 0; r < instance.getRequests().size(); r++) {
            edgeUsagePerRequest.add(r, new ArrayList<>());
            for (int i = 0; i < instance.getSize(); i++) {
                edgeUsagePerRequest.get(r).add(new ArrayList<>(instance.getSize()));
                for (int e = 0; e < instance.getSize(); e++) {
                    edgeUsagePerRequest.get(r).get(i).add(0);
                }
            }
        }

        this.edgeUsage = new ArrayList<>();
        for (int i = 0; i < instance.getSize(); i++) {
            edgeUsage.add(new ArrayList<>(instance.getSize()));
            for (int e = 0; e < instance.getSize(); e++) {
                edgeUsage.get(i).add(0);
            }
        }

        this.throughputPerRequest = new ArrayList<>(instance.getRequests().size());
        for (int r = 0; r < instance.getRequests().size(); r++) {
            throughputPerRequest.add(0);
        }

        this.memoryUsage = new ArrayList<>();
        for (int i = 0; i < instance.getSize(); i++) {
            memoryUsage.add(0);
        }

        this.pathsPerRequest = new ArrayList<>();
        for (int i = 0; i < instance.getRequests().size(); i++) {
            pathsPerRequest.add(new ArrayList<>());
        }
    }

    public List<List<List<Integer>>> getEdgeUsagePerRequest() {
        return edgeUsagePerRequest;
    }

    public List<List<Integer>> getEdgeUsage() {
        return edgeUsage;
    }

    public List<Integer> getThroughputPerRequest() {
        return throughputPerRequest;
    }

    public List<Integer> getMemoryUsage() {
        return memoryUsage;
    }

    public List<List<List<Pair<Integer, Integer>>>> getPathsPerRequest() {
        return pathsPerRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        QuantumRoutingSolution that = (QuantumRoutingSolution) o;
        return Objects.equals(getEdgeUsagePerRequest(), that.getEdgeUsagePerRequest()) && Objects.equals(getEdgeUsage(), that.getEdgeUsage()) && Objects.equals(getThroughputPerRequest(), that.getThroughputPerRequest()) && Objects.equals(getPathsPerRequest(), that.getPathsPerRequest()) && Objects.equals(getMemoryUsage(), that.getMemoryUsage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEdgeUsagePerRequest(), getEdgeUsage(), getThroughputPerRequest(), getPathsPerRequest(), getMemoryUsage());
    }

    @Override
    public String toString() {
        return "QuantumRoutingSolution{" +
                "edgeUsagePerRequest=" + edgeUsagePerRequest +
                ", edgeUsage=" + edgeUsage +
                ", ThroughputPerRequest=" + throughputPerRequest +
                ", pathsPerRequest=" + pathsPerRequest +
                ", memoryUsage=" + memoryUsage +
                '}';
    }
}
