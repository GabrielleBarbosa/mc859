import instance.QuantumRoutingInstance;
import solution.QuantumRoutingSolution;
import utils.MaxFlowSolver;

public class Experiment2 {
    public static void main(String[] args) {
        QuantumRoutingInstance instance = new QuantumRoutingInstance("../instances/data/" + "instance_n8_sd02_0" + ".json");
        MaxFlowSolver.solve(new QuantumRoutingSolution(instance), instance, 0);

    }
}
