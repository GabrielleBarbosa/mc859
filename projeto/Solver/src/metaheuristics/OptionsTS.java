package metaheuristics;

public class OptionsTS {
    //Basic configurations
    protected String name;
    protected String instanceName;
    protected int rngSeed = 0;
    protected int timeoutSeconds = 1800;
    protected Integer iterations = 1000;
    protected Integer target = null;
    //Advanced procedures
    protected boolean adaptiveTL = false;

    //Restart, intensify and difersify configs
    protected Integer restarts = 4; //Enable the restart of the heuristic, creating a solution from the ground
    protected boolean intensifyEnabled = true;  //Enables intensify data gathering (best paths) and application (creates the instance with the paths)
    protected float intensifyRate = 0.05f; //Percentage of the best solution to keep
    protected boolean diversifyEnabled = true; //Enables diversify data gathering and application, removing edges from the graph until a certain point
    protected float diversifyRate = 0.05f; //How many of the most used edges will be locked from the start
    protected float diversifyDuration = 0.05f; //How long will it last



    public OptionsTS(OptionsTS opts) {
        this.name = opts.name;
        this.iterations = opts.iterations;
        this.timeoutSeconds = opts.timeoutSeconds;
        this.target = opts.target;
        this.adaptiveTL = opts.adaptiveTL;
        this.intensifyRate = opts.intensifyRate;
        this.diversifyRate = opts.diversifyRate;
        this.rngSeed = opts.rngSeed;
        this.instanceName = opts.instanceName;
    }

    public OptionsTS(String name, int iterations, int timeoutSeconds, Integer target,
                     boolean adaptiveTL, float intensifyRate, float diversifyRate, int rngSeed) {
        this.name = name;
        this.iterations = iterations;
        this.timeoutSeconds = timeoutSeconds;
        this.target = target;
        this.adaptiveTL = adaptiveTL;
        this.intensifyRate = intensifyRate;
        this.diversifyRate = diversifyRate;
        this.rngSeed = rngSeed;
    }

    public OptionsTS(String name, String instanceName, int iterations, int timeoutSeconds, Integer target,
                     boolean adaptiveTL, float intensifyRate, float diversifyRate, int rngSeed) {
        this.name = name;
        this.iterations = iterations;
        this.timeoutSeconds = timeoutSeconds;
        this.target = target;
        this.adaptiveTL = adaptiveTL;
        this.intensifyRate = intensifyRate;
        this.diversifyRate = diversifyRate;
        this.rngSeed = rngSeed;
        this.instanceName = instanceName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public OptionsTS setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }

    public String getName() {
        return name;
    }

    public OptionsTS setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isAdaptiveTL() {
        return adaptiveTL;
    }

    public OptionsTS setAdaptiveTL(boolean adaptiveTL) {
        this.adaptiveTL = adaptiveTL;
        return this;
    }

    public float getIntensifyRate() {
        return intensifyRate;
    }

    public OptionsTS setIntensifyRate(float intensifyRate) {
        this.intensifyRate = intensifyRate;
        return this;
    }

    public float getDiversifyRate() {
        return diversifyRate;
    }

    public OptionsTS setDiversifyRate(float diversifyRate) {
        this.diversifyRate = diversifyRate;
        return this;
    }

    public int getRngSeed() {
        return rngSeed;
    }

    public OptionsTS setRngSeed(int rngSeed) {
        this.rngSeed = rngSeed;
        return this;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public OptionsTS setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public Integer getIterations() {
        return iterations;
    }

    public OptionsTS setIterations(Integer iterations) {
        this.iterations = iterations;
        return this;
    }

    public Integer getTarget() {
        return target;
    }

    public OptionsTS setTarget(Integer target) {
        this.target = target;
        return this;
    }
}
