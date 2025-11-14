package metaheuristics.neighbourhoodMove;

import utils.Pair;

import java.util.List;
import java.util.Objects;

public class RemoveFlow extends NeighborhoodMove{
    protected List<Pair<Integer, Integer>> path;

    public RemoveFlow(int request, List<Pair<Integer, Integer>> path) {
        this.request = request;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RemoveFlow that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }
}
