import instance.QuantumRoutingInstance;
import metaheuristics.OptionsTS;
import metaheuristics.QuantumRoutingTS;
import solution.SolutionMetadata;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Experiment {
    public static void main(String[] args) {
        List<String> instances = List.of(
                "instance_n010_sd02_0"
        );

        List<OptionsTS> configurations = List.of(
                new OptionsTS("CONFIG1", 1000, 1800, null, false, 0, 0, 1)
        );

        try (FileWriter writer = new FileWriter("results.csv")) {
            writer.write("Instance,Configuration,Cost,Time,Iteration\n");

            for (String inst : instances) {
                int configIndex = 1;
                for (OptionsTS config : configurations) {
                    QuantumRoutingInstance instance = new QuantumRoutingInstance("../instances/data/" + inst + ".json");
                    QuantumRoutingTS solver = new QuantumRoutingTS(instance, 1, config);
                    List<SolutionMetadata> results = solver.solve();

                    String configName = "CONFIG" + configIndex;
                    StringBuilder csvLines = new StringBuilder();
                    for (SolutionMetadata result : results) {
                        csvLines.append(inst).append(",");
                        csvLines.append(configName).append(",");
                        csvLines.append(result.getSol().getCost()).append(",");
                        csvLines.append(result.getTime()).append(",");
                        csvLines.append(result.getIterations()).append("\n");
                    }
                    writer.write(csvLines.toString());
                    writer.flush();
                    configIndex++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
