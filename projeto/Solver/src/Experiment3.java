import instance.QuantumRoutingInstance;
import metaheuristics.OptionsTS;
import metaheuristics.QuantumRoutingTS;
import solution.SolutionMetadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Experiment3 {
    public static void main(String[] args) throws Exception {
        List<String> instances = new ArrayList<>();
        File dataDir = new File("../instances/data/");
        File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".json"));

        assert files != null;
        for (File file : files) {
            String fileName = file.getName();
            instances.add(fileName.substring(0, fileName.lastIndexOf(".")));
        }


        List<OptionsTS> configurations = List.of(
                new OptionsTS(1000, 1800, null, true, true, false, 0, 0, 1)
        );

        try (FileWriter writer = new FileWriter("results.csv")) {
            writer.write("Instance,Configuration,BestSol,Target,Time,Iteration\n");

            for (String inst : instances) {
                int configIndex = 1;
                for (OptionsTS config : configurations) {
                    String configName = "CONFIG" + configIndex;
                    System.out.println("Running instance: " + inst + " with config " + configName);
                    QuantumRoutingInstance instance = new QuantumRoutingInstance("../instances/data/" + inst + ".json");
                    QuantumRoutingTS solver = new QuantumRoutingTS(instance, 1, config);
                    List<SolutionMetadata> results = solver.solve();

                    StringBuilder csvLines = new StringBuilder();
                    for (SolutionMetadata result : results) {
                        csvLines.append(inst).append(",");
                        csvLines.append(configName).append(",");
                        csvLines.append(result.getSol().getCost()).append(",");
                        csvLines.append(config.getTarget()).append(",");
                        csvLines.append(result.getTime()).append(",");
                        csvLines.append(result.getIterations()).append("\n");
                    }
                    writer.write(csvLines.toString());
                    writer.flush();
                    configIndex++;
                }
            }
        } catch (IOException e) {
            throw new Exception(e);
        }
    }
}
