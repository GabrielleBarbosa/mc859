package solution;

public class SolutionMetadata {
    protected QuantumRoutingSolution sol;
    protected long time;
    protected int iterations;

    public SolutionMetadata(QuantumRoutingSolution sol, long time, int iterations) {
        this.sol = sol;
        this.time = time;
        this.iterations = iterations;
    }

    public QuantumRoutingSolution getSol() {
        return sol;
    }

    public void setSol(QuantumRoutingSolution sol) {
        this.sol = sol;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public String log() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sol.getCost());
        stringBuilder.append("|");
        stringBuilder.append(time);
        stringBuilder.append("|");
        stringBuilder.append(iterations);
        return stringBuilder.toString();
    }
}
