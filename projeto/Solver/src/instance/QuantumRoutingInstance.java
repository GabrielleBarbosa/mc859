package instance;

import org.json.JSONArray;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;


public class QuantumRoutingInstance {
    
    private int size;

    private List<Integer> nodes;

    private List<List<Pair<Integer, Float>>> arcs;

    private List<Pair<Integer, Integer>> requests;

    public QuantumRoutingInstance(int size, List<Integer> nodes,
                                  List<List<Pair<Integer, Float>>> arcs,
                                  List<Pair<Integer, Integer>> requests) {
        this.size = size;
        this.nodes = nodes;
        this.arcs = arcs;
        this.requests = requests;
    }

    public QuantumRoutingInstance(String filename) {
        try {
            String content = Files.readString(Paths.get(filename));
            JSONObject json = new JSONObject(content);

            this.requests = new ArrayList<>();
            JSONArray requests = json.getJSONObject("graph").getJSONArray("sd_pairs");
            for (int i = 0; i < requests.length(); i++) {
                JSONArray pair = requests.getJSONArray(i);
                int source = pair.getInt(0);
                int destiny = pair.getInt(1);
                this.requests.add(new Pair<>(source, destiny));
            }

            this.nodes = new ArrayList<>();
            JSONArray nodes = json.getJSONArray("nodes");
            for (int i = 0; i < nodes.length(); i++) {
                JSONObject node = nodes.getJSONObject(i);
                int memorySize = node.getInt("memory_size");
                this.nodes.add(memorySize);
            }

            this.arcs = new ArrayList<>();
            for (int i = 0; i < nodes.length(); i++) {
                this.arcs.add(new ArrayList<>());
                for (int j = 0; j < nodes.length(); j++) {
                    this.arcs.get(i).add(null);
                }
            }

            JSONArray arcs = json.getJSONArray("links");
            for (int i = 0; i < arcs.length(); i++) {
                JSONObject arc = arcs.getJSONObject(i);
                int source = arc.getInt("source");
                int target = arc.getInt("target");
                int channels = arc.getInt("num_channels");
                float prob = arc.getInt("external_link_prob");

                this.arcs.get(source).set(target, new Pair<>(channels, prob));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getSize() {
        return size;
    }

    public List<Integer> getNodes() {
        return nodes;
    }

    public List<List<Pair<Integer, Float>>> getArcs() {
        return arcs;
    }

    public List<Pair<Integer, Integer>> getRequests() {
        return requests;
    }

    @Override
    public QuantumRoutingInstance clone() {
        int newSize = this.size;

        List<Integer> newNodes = new ArrayList<>(this.nodes);

        List<List<Pair<Integer, Float>>> newArcs = new ArrayList<>();
        for (List<Pair<Integer, Float>> row : this.arcs) {
            List<Pair<Integer, Float>> newRow = new ArrayList<>();
            for (Pair<Integer, Float> pair : row) {
                if (pair == null) {
                    newRow.add(null);
                } else {
                    newRow.add(new Pair<>(pair.getFirst(), pair.getSecond()));
                }
            }
            newArcs.add(newRow);
        }

        List<Pair<Integer, Integer>> newRequests = new ArrayList<>();
        for (Pair<Integer, Integer> req : this.requests) {
            newRequests.add(new Pair<>(req.getFirst(), req.getSecond()));
        }

        return new QuantumRoutingInstance(newSize, newNodes, newArcs, newRequests);
    }

    public static void main(String[] args) {
        QuantumRoutingInstance instance = new QuantumRoutingInstance("../instances/data/instance_n100_sd02_0.json");
        System.out.println("Instance loaded successfully");
    }
}
