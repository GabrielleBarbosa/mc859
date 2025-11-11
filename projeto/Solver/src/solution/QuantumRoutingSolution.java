package solution;

import instance.QuantumRoutingInstance;
import utils.Pair;

import java.util.*;

public class QuantumRoutingSolution {

    private List<List<List<Integer>>> xra;

    private List<List<Integer>> za;

    private List<Float> tr;

    public QuantumRoutingSolution(final QuantumRoutingSolution solution) {
        this.xra = new ArrayList<>(solution.getXra().size());
        for (int in = 0; in < solution.getXra().size(); in++) {
            List<List<Integer>> flows = new ArrayList<>();
            xra.add(in, flows);
            for (List<Integer> flow : solution.getXra().get(in)) {
                flows.add(new ArrayList<>(flow));
            }
        }

        this.za = new ArrayList<>(solution.getZa().size());
        for (int in = 0; in < solution.getZa().size(); in++) {
            za.add(new ArrayList<>(solution.getZa().get(in)));
        }

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
        for (int r = 0; r < instance.getRequests().size(); r++) {
            xra.add(r, new ArrayList<>());
            for (int i = 0; i < instance.getSize(); i++) {
                xra.get(r).add(new ArrayList<>(instance.getSize()));
                for (int e = 0; e < instance.getSize(); e++) {
                    xra.get(r).get(i).add(0);
                }
            }
        }

        this.za = new ArrayList<>();
        for (int i = 0; i < instance.getSize(); i++) {
            za.add(new ArrayList<>(instance.getSize()));
            for (int e = 0; e < instance.getSize(); e++) {
                za.get(i).add(0);
            }
        }

        this.tr = new ArrayList<>(instance.getRequests().size());
        for (int r = 0; r < instance.getRequests().size(); r++) {
            tr.add(r, 0f);
        }
    }

    public List<List<List<Integer>>> getXra() {
        return xra;
    }

    public List<List<Integer>> getZa() {
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

    public void removeFlow(int r, int i) {
        //double pathCost = this.xra.get(r).get(i).stream().mapToDouble(Pair::getSecond).sum(); // FIXME: multiplicar pela probabilidade
        //this.xra.get(r).get(i).clear();

        //float flowCost = this.tr.get(r);
        //this.tr.set(i, flowCost - (float)pathCost);
    }
}
