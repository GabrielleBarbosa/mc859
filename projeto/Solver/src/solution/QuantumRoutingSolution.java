package solution;

import instance.QuantumRoutingInstance;
import utils.Pair;

import java.util.*;

public class QuantumRoutingSolution {

    private List<List<List<Pair<Integer, Integer>>>> xra;

    private HashMap<Integer, Integer> za;

    private List<Float> tr;

    public QuantumRoutingSolution(final QuantumRoutingSolution solution) {
        this.xra = new ArrayList<>(solution.getXra().size());
        for (int in = 0; in < solution.getXra().size(); in++) {
            List<List<Pair<Integer, Integer>>> paths = new ArrayList<>();
            xra.add(in, paths);
            for (List<Pair<Integer, Integer>> path : solution.getXra().get(in)) {
                List<Pair<Integer, Integer>> p = new ArrayList<>();
                for (Pair<Integer, Integer> node : path) {
                    p.add(new Pair<>(node.getFirst(), node.getSecond()));
                }
                paths.add(p);
            }

        }

        this.za = new HashMap<>(solution.getZa());

        this.tr = new ArrayList<>(solution.getTr());
    }

    public Float getCost() {
        float cost = 0;
        for (Float t : tr) {
            cost += t;
        }
        return cost;
    }

    public QuantumRoutingSolution(final QuantumRoutingInstance instance) {
        this.xra = new ArrayList<>(instance.getRequests().size());
        for (int in = 0; in < instance.getRequests().size(); in++) {
            xra.add(in, new ArrayList<>());
        }

        this.za = new HashMap<>();

        this.tr = new ArrayList<>(instance.getRequests().size());
        for (int in = 0; in < instance.getRequests().size(); in++) {
            tr.add(in, 0f);
        }
    }

    public List<List<List<Pair<Integer, Integer>>>> getXra() {
        return xra;
    }

    public HashMap<Integer, Integer> getZa() {
        return za;
    }

    public List<Float> getTr() {
        return tr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuantumRoutingSolution that = (QuantumRoutingSolution) o;
        return Objects.equals(getXra(), that.getXra()) && Objects.equals(getZa(), that.getZa()) && Objects.equals(getTr(), that.getTr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getXra(), getZa(), getTr());
    }

    @Override
    public String toString() {
        return "QuantumRoutingSolution{" +
                "xra=" + xra +
                ", za=" + za +
                ", tr=" + tr +
                '}';
    }

    public void removeFlow(int i) {
        this.xra.get(i).clear();
        this.tr.set(i, 0f);
    }
}
