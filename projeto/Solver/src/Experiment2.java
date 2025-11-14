import instance.QuantumRoutingInstance;
import metaheuristics.OptionsTS;
import metaheuristics.QuantumRoutingTS;

public class Experiment2 {
    public static void main(String[] args) {
        QuantumRoutingInstance instance = new QuantumRoutingInstance("../instances/data/" + "instance_n400_sd10_0" + ".json");
        OptionsTS optionsTS = new OptionsTS("TTT1", "instance_n300_sd10_0", 1000, 1800, 20, true, true, false, 0.1f, 0, 2);
        QuantumRoutingTS ts = new QuantumRoutingTS(instance, 1, optionsTS);
        ts.test();
    }
}
