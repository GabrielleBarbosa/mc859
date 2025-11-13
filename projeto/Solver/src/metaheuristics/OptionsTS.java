package metaheuristics;

public class OptionsTS {
    protected String name;
    protected String instanceName;
    protected boolean adaptiveTL = false;
    protected boolean removeEnabled = true;
    protected boolean exchangeEnabled = true;
    protected float intensifyRate = 0f;
    protected float diversifyRate = 0f;
    protected int rngSeed = 0;
    protected int timeoutSeconds = 1800;
    protected Integer iterations = 1000;
    protected Integer target = null;

    public OptionsTS(OptionsTS opts) {
        this.name = opts.name;
        this.iterations = opts.iterations;
        this.timeoutSeconds = opts.timeoutSeconds;
        this.target = opts.target;
        this.removeEnabled = opts.removeEnabled;
        this.exchangeEnabled = opts.exchangeEnabled;
        this.adaptiveTL = opts.adaptiveTL;
        this.intensifyRate = opts.intensifyRate;
        this.diversifyRate = opts.diversifyRate;
        this.rngSeed = opts.rngSeed;
        this.instanceName = opts.instanceName;
    }

    public OptionsTS(String name, int iterations, int timeoutSeconds, Integer target, boolean removeEnabled, boolean exchangeEnabled,
                     boolean adaptiveTL, float intensifyRate, float diversifyRate, int rngSeed) {
        this.name = name;
        this.iterations = iterations;
        this.timeoutSeconds = timeoutSeconds;
        this.target = target;
        this.removeEnabled = removeEnabled;
        this.exchangeEnabled = exchangeEnabled;
        this.adaptiveTL = adaptiveTL;
        this.intensifyRate = intensifyRate;
        this.diversifyRate = diversifyRate;
        this.rngSeed = rngSeed;
    }

    public OptionsTS(String name, String instanceName, int iterations, int timeoutSeconds, Integer target, boolean removeEnabled, boolean exchangeEnabled,
                     boolean adaptiveTL, float intensifyRate, float diversifyRate, int rngSeed) {
        this.name = name;
        this.iterations = iterations;
        this.timeoutSeconds = timeoutSeconds;
        this.target = target;
        this.removeEnabled = removeEnabled;
        this.exchangeEnabled = exchangeEnabled;
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

    public boolean isRemoveEnabled() {
        return removeEnabled;
    }

    public OptionsTS setRemoveEnabled(boolean removeEnabled) {
        this.removeEnabled = removeEnabled;
        return this;
    }

    public boolean isExchangeEnabled() {
        return exchangeEnabled;
    }

    public OptionsTS setExchangeEnabled(boolean exchangeEnabled) {
        this.exchangeEnabled = exchangeEnabled;
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
