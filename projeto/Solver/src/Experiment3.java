import instance.QuantumRoutingInstance;
import metaheuristics.OptionsTS;
import metaheuristics.QuantumRoutingTS;
import solution.SolutionMetadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Experiment3 {
    private static List<OptionsTS> setupConfigs() {
        Random rng = new Random(1);
        List<String> instances = new ArrayList<>();
        File dataDir = new File("../instances/data/");
        File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".json"));

        assert files != null;
        for (File file : Arrays.stream(files).sorted().toList()) {
            String fileName = file.getName();
            instances.add(fileName.substring(0, fileName.lastIndexOf(".")));
        }

        List<OptionsTS> allConfigurations = new ArrayList<>();

        List<OptionsTS> performanceProfileConfigurations = List.of(
                new OptionsTS("PP1", 0, 999999, 10000 ,300, null, true, 0.2f, true, 0.2f, 0.2f),
                new OptionsTS("PP2", 0, 999999, 10000 ,300, null, false, 0.2f, false, 0.2f, 0.2f),
                new OptionsTS("PP3", 0, 999999, 10000 ,300, null, true, 0.2f, false, 0.2f, 0.2f),
                new OptionsTS("PP4", 0, 999999, 10000 ,300, null, false, 0.2f, false, 0.2f, 0.2f)
                );

        List<OptionsTS> tttConfigurations = List.of(
                new OptionsTS("TTT1", "instance_n300_sd10_0", 0, 99999, 10000, 300, 64, true, 0.2f, true, 0.2f, 0.2f),
                new OptionsTS("TTT2", "instance_n300_sd10_0", 0, 99999,10000, 300, 64, false, 0.2f, true, 0.2f, 0.2f),
                new OptionsTS("TTT3", "instance_n300_sd10_0", 0, 99999, 10000, 300, 64, true, 0.2f, false, 0.2f, 0.2f),
                new OptionsTS("TTT4", "instance_n300_sd10_0", 0, 99999, 10000, 300, 64, false, 0.2f, false, 0.2f, 0.2f)
        );

        for (String instance : instances) {
            for (OptionsTS config : performanceProfileConfigurations) {
                allConfigurations.add(new OptionsTS(config).setInstanceName(instance));
            }
        }

        for (OptionsTS config : tttConfigurations) {
            for (int i = 0; i < 50; i++) {
                allConfigurations.add(new OptionsTS(config).setRngSeed(rng.nextInt(1000)));
            }
        }

        return allConfigurations;
    }

    public static void main(String[] args) throws Exception {
        List<OptionsTS> configurations = setupConfigs();

        int numThreads = Runtime.getRuntime().availableProcessors(); // Define the number of threads

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        FileWriter writer = null;
        try {
            writer = new FileWriter("results.csv");
            writer.write("Instance,Configuration,Seed,Target,BestSol,TimeMS,Iteration\n");

            final FileWriter finalWriter = writer;
            for (OptionsTS config : configurations) {
                Runnable task = () -> {
                    try {
                        System.out.println("Running instance: " + config.getInstanceName() + " with config " + config.getName());
                        QuantumRoutingInstance instance = new QuantumRoutingInstance("../instances/data/" + config.getInstanceName() + ".json");
                        QuantumRoutingTS solver = new QuantumRoutingTS(instance, 1, config);
                        List<SolutionMetadata> results = solver.solve();

                        StringBuilder csvLines = new StringBuilder();
                        for (SolutionMetadata result : results) {
                            csvLines.append(config.getInstanceName()).append(",");
                            csvLines.append(config.getName()).append(",");
                            csvLines.append(config.getRngSeed()).append(",");
                            csvLines.append(config.getTarget()).append(",");
                            csvLines.append(result.getSol().getCost()).append(",");
                            csvLines.append(result.getTime()).append(",");
                            csvLines.append(result.getIterations()).append("\n");
                        }
                        synchronized (finalWriter) {
                            finalWriter.write(csvLines.toString());
                            finalWriter.flush();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                executor.submit(task);
            }

        } catch (IOException e) {
            throw new Exception(e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.HOURS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
