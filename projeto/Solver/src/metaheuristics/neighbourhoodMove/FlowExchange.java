package metaheuristics.neighbourhoodMove;

import java.util.Objects;

public class FlowExchange extends NeighborhoodMove {
    protected int requestExchanged;
    protected int indexRequest;
    protected int indexRequestExchanged;

    public FlowExchange(int request, int exchange, int indexRequest, int indexRequestExchanged) {
        this.request = request;
        this.requestExchanged = exchange;
        this.indexRequest = indexRequest;
        this.indexRequestExchanged = indexRequestExchanged;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FlowExchange that)) return false;
        if (!super.equals(o)) return false;
        return requestExchanged == that.requestExchanged && indexRequest == that.indexRequest && indexRequestExchanged == that.indexRequestExchanged;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), requestExchanged, indexRequest, indexRequestExchanged);
    }

    public int getRequestExchanged() {
        return requestExchanged;
    }

    public int getIndexRequest() {
        return indexRequest;
    }

    public int getIndexRequestExchanged() {
        return indexRequestExchanged;
    }
}
