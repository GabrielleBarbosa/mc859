import instance.QuantumRoutingInstance;
import metaheuristics.OptionsTS;
import metaheuristics.QuantumRoutingTS;

public class Experiment2 {
    public static void main(String[] args) {
        QuantumRoutingInstance instance = new QuantumRoutingInstance("../instances/data/" + "instance_n100_sd08_3" + ".json");
        OptionsTS optionsTS = new OptionsTS("PP1", 0, 999999, 10000 ,120, null, true, 0.2f, true, 0.2f, 0.2f);
        QuantumRoutingTS ts = new QuantumRoutingTS(instance, 1, optionsTS);
        ts.solve();
        ts.practicalEvaluation();
    }
}
