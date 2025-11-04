import instance.QuantumRoutingInstance;
import metaheuristics.OptionsTS;
import metaheuristics.QuantumRoutingTS;
import metaheuristics.SolutionMetadata;
import solution.QuantumRoutingSolution;

import java.util.List;

public class Experiment {
    public static void main(String[] args) {
        List<String> instances = List.of(
                "instance_n100_sd02_0",
                "instance_n100_sd02_1",
                "instance_n100_sd02_2"
        );

        List<OptionsTS> configurations = List.of(
                new OptionsTS(1000, 1800, false, 0, 0, 1)
        );

        for (String inst : instances) {
            for (OptionsTS config : configurations) {
                QuantumRoutingInstance instance = new QuantumRoutingInstance("../instances/data/" + inst + ".json");
                QuantumRoutingTS solver = new QuantumRoutingTS(instance, 1, config);
                List<SolutionMetadata> result = solver.solve();

                System.out.println("Instance " + inst + " best cost: " + result.get(result.size() - 1).getSol().getCost());
            }
        }
    }
}
