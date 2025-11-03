package instance;

import utils.Pair;

public class QuantumRoutingNode extends Pair<Integer, Float> {
    public QuantumRoutingNode(Integer first, Float second) {
        super(first, second);
    }

    public Integer getCapacity() {
        return getFirst();
    }

    public Float getProbability() {
        return getSecond();
    }
}
