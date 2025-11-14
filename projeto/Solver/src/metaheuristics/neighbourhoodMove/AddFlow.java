package metaheuristics.neighbourhoodMove;

import java.util.Objects;

public class AddFlow extends NeighborhoodMove{
    protected int index;

    public AddFlow(int request, int index) {
        this.index = index;
        this.request = request;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AddFlow addFlow)) return false;
        if (!super.equals(o)) return false;
        return index == addFlow.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), index);
    }
}
