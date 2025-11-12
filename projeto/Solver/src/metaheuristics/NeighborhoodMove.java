package metaheuristics;

import utils.Pair;

import java.util.List;
import java.util.Objects;

public class NeighborhoodMove {

    protected String type;
    protected int r1;
    protected int r2;
    protected Pair<Integer, Integer> path;

    public static NeighborhoodMove FlowExchange(int r1, int r2, Pair<Integer, Integer> path) {
        NeighborhoodMove ret = new NeighborhoodMove();
        ret.type = "flow_exchange";
        ret.r1 = r1;
        ret.r2 = r2;
        ret.path = path;
        return ret;
    }

    public static NeighborhoodMove RemoveFlow(int r1) {
        NeighborhoodMove ret = new NeighborhoodMove();
        ret.type = "remove_flow";
        ret.r1 = r1;
        return ret;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NeighborhoodMove that = (NeighborhoodMove) o;
        if (that.type.equals("flow_exchange")) {
            return Objects.equals(path, that.path) && ((r1 == that.r1 && r2 == that.r2) || (r1 == that.r2 && r2 == that.r1 ));
        }


        return r1 == that.r1 && r2 == that.r2 && Objects.equals(type, that.type) && Objects.equals(path, that.path);
    }
}
