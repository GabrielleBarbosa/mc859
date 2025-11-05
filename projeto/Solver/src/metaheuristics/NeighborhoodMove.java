package metaheuristics;

import utils.Pair;

import java.util.List;

public class NeighborhoodMove {

    protected String type;
    protected int r1;
    protected int p1;
    protected int r2;
    protected int p2;
    protected Pair<Integer, Integer> path;

    public static NeighborhoodMove FlowExchange(int r1, int p1, int r2, int p2, Pair<Integer, Integer> path) {
        NeighborhoodMove ret = new NeighborhoodMove();
        ret.type = "flow_exchange";
        ret.p1 = p1;
        ret.p2 = p2;
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


}
