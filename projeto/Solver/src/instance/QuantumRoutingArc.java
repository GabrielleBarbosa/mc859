package instance;

import utils.Pair;

public class QuantumRoutingArc extends Pair<Integer, Float> {
    public QuantumRoutingArc(Integer first, Float second) {
        super(first, second);
    }

    public Integer getCapacity() {
        return getFirst();
    }

    public Float getProbability() {
        return getSecond();
    }
}
