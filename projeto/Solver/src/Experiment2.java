import instance.QuantumRoutingInstance;
import metaheuristics.OptionsTS;
import metaheuristics.QuantumRoutingTS;
import solution.SolutionMetadata;

public class Experiment2 {
    public static void main(String[] args) {
        QuantumRoutingInstance instance = new QuantumRoutingInstance("../instances/data/" + "instance_n500_sd06_0" + ".json");
        OptionsTS optionsTS = new OptionsTS("PP1", 0, 999999, 10000 ,300, null, true, 0.2f, true, 0.05f, 0.2f);
        QuantumRoutingTS ts = new QuantumRoutingTS(instance, 5, optionsTS);
        var metadatas = ts.solve();
        System.out.println("Cost|time|iterations");
        for (SolutionMetadata metadata : metadatas) {
            System.out.println(metadata.log());
        }
    }
}
