package metaheuristics;

public class OptionsTS {
    protected boolean adaptiveTL = false;
    protected boolean removeEnabled = true;
    protected boolean exchangeEnabled = true;
    protected float intensifyRate = 0f;
    protected float diversifyRate = 0f;
    protected int rngSeed = 0;
    protected int timeoutSeconds = 1800;
    protected Integer iterations = 1000;
    protected Integer target = null;

    public OptionsTS(int iterations, int timeoutSeconds, Integer target, boolean removeEnabled, boolean exchangeEnabled,
                     boolean adaptiveTL, float intensifyRate, float diversifyRate, int rngSeed) {
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

    public boolean isAdaptiveTL() {
        return adaptiveTL;
    }

    public void setAdaptiveTL(boolean adaptiveTL) {
        this.adaptiveTL = adaptiveTL;
    }

    public boolean isRemoveEnabled() {
        return removeEnabled;
    }

    public void setRemoveEnabled(boolean removeEnabled) {
        this.removeEnabled = removeEnabled;
    }

    public boolean isExchangeEnabled() {
        return exchangeEnabled;
    }

    public void setExchangeEnabled(boolean exchangeEnabled) {
        this.exchangeEnabled = exchangeEnabled;
    }

    public float getIntensifyRate() {
        return intensifyRate;
    }

    public void setIntensifyRate(float intensifyRate) {
        this.intensifyRate = intensifyRate;
    }

    public float getDiversifyRate() {
        return diversifyRate;
    }

    public void setDiversifyRate(float diversifyRate) {
        this.diversifyRate = diversifyRate;
    }

    public int getRngSeed() {
        return rngSeed;
    }

    public void setRngSeed(int rngSeed) {
        this.rngSeed = rngSeed;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Integer getIterations() {
        return iterations;
    }

    public void setIterations(Integer iterations) {
        this.iterations = iterations;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }
}
