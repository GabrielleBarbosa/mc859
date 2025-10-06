package problems.scqbf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import problems.qbf.QBF;
import solutions.Solution;

/**
 * Set Cover Quadratic Binary Function (SC-QBF) implementation.
 * Extends QBF to include set cover constraints.
 */
public class SC_QBF extends QBF {
    
    /**
     * Collection of subsets, where each subset contains indices of elements it covers
     */
    private ArrayList<Set<Integer>> subsets;
    
    /**
     * Number of subsets (same as number of variables)
     */
    private Integer numSubsets;
    
    /**
     * Constructor for SC_QBF class
     * @param filename Name of the file containing the SC-QBF instance
     * @throws IOException Necessary for I/O operations
     */
    public SC_QBF(String filename) throws IOException {
        super(filename);
    }
    
    /**
     * Checks if a solution covers all elements from 1 to n
     * @param sol The solution to be verified
     * @return true if all elements are covered, false otherwise
     */
    public boolean isCoverValid(Solution<Integer> sol) {
        Set<Integer> covered = new HashSet<>();
        
        // Add all elements covered by selected subsets
        for (Integer subsetIdx : sol) {
            if (subsetIdx >= 0 && subsetIdx < numSubsets) {
                covered.addAll(subsets.get(subsetIdx));
            }
        }
        
        // Check if all elements from 1 to n are covered
        for (int i = 1; i <= numSubsets; i++) {
            if (!covered.contains(i)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Evaluates the solution considering set cover constraints
     * @param sol The solution to be evaluated
     * @return The QBF value if cover is valid, negative infinity otherwise
     */
    @Override
    public Double evaluate(Solution<Integer> sol) {
        if (!isCoverValid(sol)) {
            return sol.cost = Double.NEGATIVE_INFINITY;
        }
        return super.evaluate(sol);
    }
    
    /**
     * Evaluates insertion considering set cover constraints
     */
    @Override
    public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {
        Solution<Integer> tempSol = new Solution<>(sol);
        tempSol.add(elem);
        
        if (!isCoverValid(tempSol)) {
            return -1000000.0;
        }
        
        return super.evaluateInsertionCost(elem, sol);
    }
    
    /**
     * Evaluates removal considering set cover constraints
     */
    @Override
    public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {
        Solution<Integer> tempSol = new Solution<>(sol);
        tempSol.remove(elem);
        
        if (!isCoverValid(tempSol)) {
            return Double.POSITIVE_INFINITY;
        }
        
        return super.evaluateRemovalCost(elem, sol);
    }
    
    /**
     * Evaluates exchange considering set cover constraints
     */
    @Override
    public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {
        Solution<Integer> tempSol = new Solution<>(sol);
        tempSol.remove(elemOut);
        tempSol.add(elemIn);
        
        if (!isCoverValid(tempSol)) {
            return Double.POSITIVE_INFINITY;
        }
        
        return super.evaluateExchangeCost(elemIn, elemOut, sol);
    }
    
    /**
     * Reads the SC-QBF instance from file
     */
    @Override
    protected Integer readInput(String filename) throws IOException {
        Reader fileInst = new BufferedReader(new FileReader(filename));
        StreamTokenizer stok = new StreamTokenizer(fileInst);
        
        // Read n (number of variables/subsets)
        stok.nextToken();
        numSubsets = (int) stok.nval;
        Integer _size = numSubsets;
        
        // Initialize subsets collection
        subsets = new ArrayList<>(numSubsets);
        for (int i = 0; i < numSubsets; i++) {
            subsets.add(new HashSet<>());
        }
        
        // Read number of elements covered by each subset
        int[] coverSizes = new int[numSubsets];
        for (int i = 0; i < numSubsets; i++) {
            stok.nextToken();
            coverSizes[i] = (int) stok.nval;
        }
        
        // Read elements covered by each subset
        for (int i = 0; i < numSubsets; i++) {
            for (int j = 0; j < coverSizes[i]; j++) {
                stok.nextToken();
                int element = (int) stok.nval;
                subsets.get(i).add(element);
            }
        }
        
        // Read the QBF matrix A
        A = new Double[_size][_size];
        for (int i = 0; i < _size; i++) {
            for (int j = i; j < _size; j++) {
                stok.nextToken();
                A[i][j] = stok.nval;
                if (j > i) {
                    A[j][i] = 0.0;
                }
            }
        }
        
        fileInst.close();
        return _size;
    }
    
    /**
     * Gets the subsets for the SC-QBF instance
     * @return ArrayList of sets representing the subsets
     */
    public ArrayList<Set<Integer>> getSubsets() {
        return subsets;
    }
    
    /**
     * Prints the subsets structure
     */
    public void printSubsets() {
        System.out.println("Subsets structure:");
        for (int i = 0; i < numSubsets; i++) {
            System.out.println("S" + (i+1) + ": " + subsets.get(i));
        }
    }
    
    /**
     * Main method for testing
     * Not Working :/
     
    public static void main(String[] args) throws IOException {
        SC_QBF scqbf = new SC_QBF("instances/scqbf/n25p1.txt");
        
        System.out.println("SC-QBF instance loaded:");
        System.out.println("Number of subsets: " + scqbf.numSubsets);
        scqbf.printSubsets();
        System.out.println("\nMatrix A:");
        scqbf.printMatrix();
        
        // Test with a valid cover solution
        Solution<Integer> testSol = new Solution<>();
        testSol.add(0); // S1
        testSol.add(1); // S2
        testSol.add(3); // S4
        
        System.out.println("\nTest solution: " + testSol);
        System.out.println("Is valid cover: " + scqbf.isCoverValid(testSol));
        System.out.println("Evaluation: " + scqbf.evaluate(testSol));
    }
    */
}