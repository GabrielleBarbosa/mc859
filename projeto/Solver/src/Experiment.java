import instance.QuantumRoutingInstance;
import metaheuristics.OptionsTS;
import metaheuristics.QuantumRoutingTS;
import solution.QuantumRoutingSolution;

import java.util.List;

public class Experiment {
    public static void main(String[] args) {
        List<String> instances = List.of(
                "instance_n100_sd02_0",
                "instance_n100_sd02_1",
                "instance_n100_sd02_2"
        );
        for (String inst : instances) {
            QuantumRoutingInstance instance = new QuantumRoutingInstance("../instances/data/" + inst + ".json");
            QuantumRoutingTS solver = new QuantumRoutingTS(instance, 1, 1000, new OptionsTS());
            QuantumRoutingSolution result = solver.solve();

            System.out.println("Instance " + inst + " result: " + result.getCost());
        }


    }
}
