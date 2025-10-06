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
        if (!isCoverValid(sol)) {
            return sol.cost = Double.POSITIVE_INFINITY;
        }
        setVariables(sol);
        return sol.cost = -evaluateQBF();
    }
    
    /**
     * Evaluates insertion for inverse SC-QBF
     */
    @Override
    public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {
        Solution<Integer> tempSol = new Solution<>(sol);
        tempSol.add(elem);
        
        if (!isCoverValid(tempSol)) {
            return -1000000.0;
        }
        
        setVariables(sol);
        return -evaluateInsertionQBF(elem);
    }
    
    /**
     * Evaluates removal for inverse SC-QBF
     */
    @Override
    public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {
        Solution<Integer> tempSol = new Solution<>(sol);
        tempSol.remove(elem);
        
        if (!isCoverValid(tempSol)) {
            return Double.POSITIVE_INFINITY;
        }
        
        setVariables(sol);
        return -evaluateRemovalQBF(elem);
    }
    
    /**
     * Evaluates exchange for inverse SC-QBF
     */
    @Override
    public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {
        Solution<Integer> tempSol = new Solution<>(sol);
        tempSol.remove(elemOut);
        tempSol.add(elemIn);
        
        if (!isCoverValid(tempSol)) {
            return Double.POSITIVE_INFINITY;
        }
        
        setVariables(sol);
        return -evaluateExchangeQBF(elemIn, elemOut);
    }
}