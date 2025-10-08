package problems.scqbf;

import java.io.IOException;
import solutions.Solution;

/**
 * Inverse of the Set Cover Quadratic Binary Function (SC-QBF)
 * Used for maximization with GRASP framework
 */
public class SC_QBF_Inverse extends SC_QBF {
    
    /**
     * Constructor for SC_QBF_Inverse class
     * @param filename Name of the file containing the SC-QBF instance
     */
    public SC_QBF_Inverse(String filename) throws IOException {
        super(filename);
    }
    
    /**
     * Evaluates the inverse of SC-QBF (for maximization)
     */
    @Override
    public Double evaluate(Solution<Integer> sol) {
        return -super.evaluate(sol);
    }
    
    /**
     * Evaluates insertion for inverse SC-QBF
     */
    @Override
    public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {
        return -super.evaluateInsertionCost(elem, sol);
    }
    
    /**
     * Evaluates removal for inverse SC-QBF
     */
    @Override
    public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {
        return -super.evaluateRemovalCost(elem, sol);
    }
    
    /**
     * Evaluates exchange for inverse SC-QBF
     */
    @Override
    public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {
        return -super.evaluateExchangeCost(elemIn, elemOut, sol);
    }
}