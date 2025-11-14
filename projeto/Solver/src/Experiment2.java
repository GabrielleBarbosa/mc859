import instance.QuantumRoutingInstance;
import metaheuristics.OptionsTS;
import metaheuristics.QuantumRoutingTS;

public class Experiment2 {
    public static void main(String[] args) {
        QuantumRoutingInstance instance = new QuantumRoutingInstance("../instances/data/" + "instance_n100_sd08_3" + ".json");
        OptionsTS optionsTS = new OptionsTS("TTT1", "instance_n300_sd10_0", 10000, 1800, 120, false, 0.1f, 0.1f, 2);
        QuantumRoutingTS ts = new QuantumRoutingTS(instance, 2, optionsTS);
        ts.solve();
        ts.practicalEvaluation();
    }
}
