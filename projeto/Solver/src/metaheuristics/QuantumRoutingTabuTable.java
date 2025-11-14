package metaheuristics;

import metaheuristics.neighbourhoodMove.AddFlow;
import metaheuristics.neighbourhoodMove.FlowExchange;
import metaheuristics.neighbourhoodMove.NeighborhoodMove;
import metaheuristics.neighbourhoodMove.RemoveFlow;

import java.util.ArrayDeque;
import java.util.Deque;

public class QuantumRoutingTabuTable {
    protected Deque<NeighborhoodMove> table;
    protected int maxSize;

    public QuantumRoutingTabuTable(int size) {
        this.table = new ArrayDeque<>(size);
        this.maxSize = size;
    }

    public void add(NeighborhoodMove move) {
        if (table.size() == maxSize) {
            table.removeFirst();
        }
        table.add(move);
    }

    public void add(RemoveFlow removeFlow, int index) {
        for (NeighborhoodMove  move: table) {
            if (move instanceof AddFlow) {
                AddFlow addFlow = (AddFlow) move;
                if (move.getRequest() == removeFlow.getRequest() && index <= addFlow.getIndex()) {
                    addFlow.setIndex(addFlow.getIndex() - 1);
                }
            }
        }

        add(removeFlow);
    }

    public boolean contains(NeighborhoodMove move) {
        return table.contains(move);
    }

    public boolean removeExchange(int request, int index) {
        for (NeighborhoodMove move : table) {
            if (move instanceof FlowExchange &&
                    ((move.getRequest() == request && ((FlowExchange) move).getIndexRequest() == index) ||
                        (((FlowExchange) move).getRequestExchanged() == request && ((FlowExchange) move).getIndexRequestExchanged() == index))) {
                    table.remove(move);
                    return true;
                }

        }
        return false;
    }

    public void pop() {
        table.removeFirst();
    }
}
