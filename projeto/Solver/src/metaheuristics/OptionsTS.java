package metaheuristics;

public class OptionsTS {
    protected boolean adaptiveTL = false;
    protected float intensifyRate = 0f;
    protected float diversifyRate = 0f;
    protected int rngSeed = 0;
    protected int timeoutSeconds = 1800;
    protected Integer iterations = 1000;

    public OptionsTS() {}

    public OptionsTS(int iterations, int timeoutSeconds, boolean adaptiveTL, float intensifyRate, float diversifyRate, int rngSeed) {
        this.iterations = iterations;
        this.timeoutSeconds = timeoutSeconds;
        this.adaptiveTL = adaptiveTL;
        this.intensifyRate = intensifyRate;
        this.diversifyRate = diversifyRate;
        this.rngSeed = rngSeed;
    }
}
