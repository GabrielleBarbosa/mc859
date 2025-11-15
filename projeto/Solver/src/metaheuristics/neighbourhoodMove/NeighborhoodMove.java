package metaheuristics.neighbourhoodMove;

import java.util.Objects;

public abstract class NeighborhoodMove {

    protected int request;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NeighborhoodMove that)) return false;
        return request == that.request;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(request);
    }

    public int getRequest() {
        return request;
    }
}

