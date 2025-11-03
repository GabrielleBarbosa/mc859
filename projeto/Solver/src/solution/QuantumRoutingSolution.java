package solution;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class QuantumRoutingSolution {

    private List<HashMap<Integer, Integer>> xra;

    private HashMap<Integer, Integer> za;

    private List<Integer> tr;

    public QuantumRoutingSolution(List<HashMap<Integer, Integer>> xra, HashMap<Integer, Integer> za, List<Integer> tr) {
        this.xra = xra;
        this.za = za;
        this.tr = tr;
    }

    public List<HashMap<Integer, Integer>> getXra() {
        return xra;
    }

    public HashMap<Integer, Integer> getZa() {
        return za;
    }

    public List<Integer> getTr() {
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
}
