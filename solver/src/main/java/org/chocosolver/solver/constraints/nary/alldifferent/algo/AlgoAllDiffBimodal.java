/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.algo;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.alldifferent.AllDifferent;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.BipartiteMatching;
import org.chocosolver.util.objects.TrackingList;

import static org.chocosolver.solver.variables.IntVar.*;


/**
 * Algorithm of Alldifferent ensuring GAC.
 * <p/>
 * Uses a variant of Regin algorithm based on the partially complemented (PC) approach
 * <p/>
 * Keeps track of previous matching and the sets of relevant variables and values for further calls
 * <p/>
 * 
 * @author Sulian Le Bozec-Chiffoleau
 */
public class AlgoAllDiffBimodal implements IAlldifferentAlgorithm {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    public static final int BFS = 0;
    public static final int DFS = 1;
    public static final int PRUNE = 2;

    Propagator<IntVar> aCause;
    Model model;
    final MiniSat sat;
    protected IntVar[] vars;
    private final int R;    // Total number of variables
    private final TrackingList variablesDynamic;  // The dynamic list of uninstantiated variables
    private final int minValue;
    private final int maxValue;
    private final int D;    // Total number of values
    private final TrackingList valuesDynamic;  // The dynamic list of values present in the domain of at least one variable and not matched to an instantiated variable
    private final int fail; // Symbol signifying we couldn't find an augmenting path
    private final BipartiteMatching matching; // The matching used dynamically
    private final int[] parentBFS; // Array storing the parent of each value-node in the BFS tree
    private final int[] queueBFS; // Queue of the variables to explore during the BFS
    private int headBFS;
    private int tailBFS;
    private int minValSearchedNodes;  // For failure explanation
    private int maxValSearchedNodes;  // For failure explanation
    private final int t_node; // Symbol representing the artificial sink node of the Residual Graph
    private final TrackingList complementSCC; // List of the values that are not in the discovered SCC
    private final int[] tarjanStack;  // The stack used in Tarjan's algorithm to find the SCCs
    private int topTarjan;
    private final boolean[] inStack; // Boolean array informing the presence of a value in the stack
    private final int[] pre; // Pre visit order of the values
    private final int[] low; // Low point of the values
    private int numVisit; // Current visit number of the DFS in Tarjan's algorithm
    private boolean firstSCC; // Indicates if the discovered SCC is the first discovered one in the current propagation
    private final AllDifferent.Consistency mode; // Indicating the mode in which we are using the procedure (Classic, Complement, Hybrid or Tuned)
    private boolean pruned; // True if some variable-value pairs were pruned

    private final int[] sccPartition; // Partition of the values relative to the SCCs
    private final int[] sccIndices; // End point of each SCC in the partition
    private int numberOfSCCs;
    private final int[] sccBelonging; // index of the SCC each value belongs to
    private final int[] upToDateSCC; // indicating for each value if its SCC has been updated in this call
    private int updateKey; // This key is incremented at each call, to reset the upToDate information

    private final int[] sccFactors; // Factor of each SCC, if already created, for generating explanations
    private final int[] upToDateFactor; // indicating for each SCC if its factor has been created in this call

    private final boolean allEnum;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public AlgoAllDiffBimodal(IntVar[] variables, Propagator<IntVar> cause, AllDifferent.Consistency acMode) {
        // Variables and data structures for the whole procedure
        this.aCause = cause;
        this.model = variables[0].getModel();
        this.vars = variables;
        this.R = variables.length;
        this.variablesDynamic = new TrackingList(0, R-1);
        int tempMinValue = vars[0].getLB();
        int tempMaxValue = vars[0].getUB();
        for (IntVar x : vars) {
            tempMinValue = Math.min(tempMinValue, x.getLB());
            tempMaxValue = Math.max(tempMaxValue, x.getUB());
        }
        this.minValue = tempMinValue;
        this.maxValue = tempMaxValue;
        this.D = maxValue - minValue + 1;
        this.valuesDynamic = new TrackingList(minValue, maxValue);
        refineUniverse(valuesDynamic);
        this.fail = minValue - 1;
        this.matching = new BipartiteMatching(0, R-1, minValue, maxValue);

        this.mode = acMode;

        // Specific data structures for finding the maximum matching
        this.parentBFS = new int[D];
        this.queueBFS = new int[R];
        this.headBFS = 0;
        this.tailBFS = 0;

        // Specific data structures for computing the strongly connected components
        this.t_node = minValue - 1;
        this.complementSCC = new TrackingList(minValue, maxValue);
        refineUniverse(complementSCC);
        this.tarjanStack = new int[D];
        this.topTarjan = 0;
        this.inStack = new boolean[D];
        this.pre = new int[D];
        this.low = new int[D];

        // Specific data structures for generating the explanations
        //TODO: only if LCG
        this.sat = model.getSolver().getSat();
        this.sccPartition = new int[D];
        this.sccIndices = new int[D]; //TODO: bounded by R + 1 ?
        this.numberOfSCCs = 0;
        this.sccBelonging = new int[D];
        this.upToDateSCC = new int[D];
        this.updateKey = 0;

        // Specific data structures for factorising the explanations
        this.sccFactors = new int[D];
        this.upToDateFactor = new int[D];

        // Specific data structures for dealing with variables defined by their bounds
        boolean temp = true;
        for (IntVar x : vars) {
            if (!x.hasEnumeratedDomain()) {
                temp = false;
                break;
            }
        }
        this.allEnum = temp;
    }

    private void refineUniverse(TrackingList valueUniverse) { // The tracking list initially contains an interval, so we refine it by removing the values that are present in no variables' domain (which may contain holes)
        boolean valuePresent = false;
        for (int value = minValue; value <= maxValue; value++) {
            for (IntVar variable : vars) {
                if (variable.contains(value)) {
                    valuePresent = true;
                    break;
                }
            }
            if (!valuePresent) {valueUniverse.removeFromUniverse(value);}
        }
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    public boolean propagate() throws ContradictionException {
        this.pruned = false;
        updateDynamicStructuresOpening();
        findMaximumMatching();
        filter();
        updateDynamicStructuresEnding();
        return this.pruned;
    }

    //***********************************************************************************
    // MAXIMUM MATCHING
    //***********************************************************************************


    private void findMaximumMatching() throws ContradictionException {
        complementSCC.refill(); // This will be used to find the cut for a failure explanation
        int var = variablesDynamic.getSource();
        while (variablesDynamic.hasNext(var)) { // We increase the size of the current matching until no unmatched variable remains
            var = variablesDynamic.getNext(var);
            if (!matching.inMatchingU(var)) {
                valuesDynamic.refill();   // We refill the list with the recently removed elements, instead of recreating it from scratch
                int val = augmentingPath(var);
                if (val != fail) {
                    augmentMatching(val);
                }
                else {
                    // It is not possible to get a maximum matching --> the constraint can not be satisfied
                    generateFailureExplanation();
                }
            }
        }
        valuesDynamic.refill(); // valuesDynamic is a global variable used in the whole filtering procedure, so we must refill it
    }


    private void augmentMatching(int root) { // By knowing the parent of each value in the BFS tree and the current match of the variables, we can retrieve the augmenting path from the last value in the path
        int v = root;
        while (matching.inMatchingU(getParent(v))) {
            int v_next = matching.getMatchU(getParent(v));
            // We switch the edges of the matching on the augmenting path
            matching.unMatch(getParent(v), v_next);
            matching.setMatch(getParent(v), v);
            v = v_next;
        }
        // The last variable we encounter is the one we performed the BFS from, and is then unmatched
        matching.setMatch(getParent(v), v);
    }

    private int augmentingPath(int root) {
        // Additional global variables for explanations
        minValSearchedNodes = maxValue + 1;
        maxValSearchedNodes = minValue - 1;
        // Starting the bimodalBFS
        headBFS = 0;
        tailBFS = 1;
        queueBFS[0] = root;
        while (headBFS != tailBFS) {
            int var = queueBFS[headBFS];
            headBFS++;
            int val;
            if (choice(BFS, var)) {    // If var has a small domain, we iterate over its domain and explore the unvisited values
                int ub = vars[var].getUB();
                for (val = vars[var].getLB(); val <= ub; val = vars[var].nextValue(val)) {
                    if (valuesDynamic.isPresent(val) && stop(var, val)) {return val;}
                }
            } else {    // If var has a large domain, we iterate over the unvisited values and explore the ones that are in the domain of var
                val = valuesDynamic.getSource();
                while (valuesDynamic.hasNext(val)) {
                    val = valuesDynamic.getNext(val);
                    if (vars[var].contains(val) && stop(var, val)) {return val;}
                }
            }
        }
        return fail;
    }

    private boolean stop(int var, int val) {
        setParent(var, val);
        if (matching.inMatchingV(val)) { // If the value is already matched, we continue the exploration from its matched variable
            valuesDynamic.remove(val);
            queueBFS[tailBFS] = matching.getMatchV(val);
            tailBFS++;
            // For explanations
            minValSearchedNodes = Math.min(minValSearchedNodes, val);
            maxValSearchedNodes = Math.max(maxValSearchedNodes, val);
            return false;
        } else {return true;} // If the value is not matched, we can stop the exploration because we found an augmenting path
    }

    public void generateFailureExplanation() throws ContradictionException {
        Reason reason = Reason.undef();
        if (aCause.lcg()) {
            int nVars = tailBFS;
            int nVals = tailBFS - 1;
            int[] explanation = new int[1 + nVars * (2 + (maxValSearchedNodes - minValSearchedNodes + 1) - nVals)];
            // Store the values between minValSearchedNodes and maxValSearchedNodes that does not belong to the searched nodes
            int[] nonValues = new int[(maxValSearchedNodes - minValSearchedNodes + 1) - nVals];
            int m = 0;
            for (int value = minValSearchedNodes + 1; value < maxValSearchedNodes; value++) {
                if (!isInSearchedNodes(value)) { // The value does not belong to the searched nodes
                    nonValues[m++] = value;
                }
            }
            assert m == nonValues.length;
            // Start generating the explanation
            m = 1;
            for (int i = 0; i < tailBFS; i++) {
                int var = queueBFS[i];
                explanation[m++] = MiniSat.neg(vars[var].getLit(minValSearchedNodes, LR_GE));
                for (int value : nonValues) {
                    explanation[m++] = MiniSat.neg(vars[var].getLit(value, LR_NE));
                }
                explanation[m++] = MiniSat.neg(vars[var].getLit(maxValSearchedNodes, LR_LE));
            }
            assert m == explanation.length;
            reason = Reason.r(explanation);
        }
        valuesDynamic.refill(); // valuesDynamic is a backtrackable TrackingList, we must refill it to avoid breaking its structure when backtracking
        aCause.fails(reason);
    }

    private boolean isInSearchedNodes(int val) {
        // At this point of the filtering procedure, valuesDynamic is the list of unvisited values in the reduced variable-value graph and complementSCC is the list of values in the reduced variable-value graph
        // So to check whether a value has been visited we must verify that it does not belong to valuesDynamic AND it does belong to complementSCC
        return !valuesDynamic.isPresent(val) && complementSCC.isPresent(val);
    }

    //***********************************************************************************
    // SCC + PRUNING
    //***********************************************************************************

    private void filter() throws ContradictionException {
        this.numVisit = 1;
        this.firstSCC = true;
        resetSCCs();
        int var = variablesDynamic.getSource();

        while(variablesDynamic.hasNext(var)) {
            var = variablesDynamic.getNext(var);
            if (valuesDynamic.isPresent(matching.getMatchU(var))) {
                bimodalDFS(var);
            }
        }
        if (topTarjan != 0) {prune(t_node);} // If the artificial node t_node is alone, no pruning is possible and the structures tarjanStack and inStack are already cleared.
    }

    private void bimodalDFS(int var) throws ContradictionException {
        setPre(matching.getMatchU(var), numVisit);
        setLow(matching.getMatchU(var), numVisit);
        numVisit++;
        valuesDynamic.remove(matching.getMatchU(var));
        tarjanStack[topTarjan] = matching.getMatchU(var);
        topTarjan++;
        declareInStack(matching.getMatchU(var), true);
        int val;

        if(choice(DFS, var)) {   // If var has a small domain then iterate over the domain
            int ub = vars[var].getUB();
            for (val = vars[var].getLB(); val <= ub; val = vars[var].nextValue(val)) {
                // ======================= Case 1 : explore a non-visited value =======================
                if (val != matching.getMatchU(var) && valuesDynamic.isPresent(val)) {process(var, val);}

                // ======================= Case 2 :  update M(var).low via an already visited and unassigned value =======================
                else if (val != matching.getMatchU(var) && isInStack(val)) {setLow(matching.getMatchU(var), Math.min(getLow(matching.getMatchU(var)), getPre(val)));} // M(var).low = min(M(var).low, val.pre)
            }

        } else { // If var has a large domain then iterate over the unvisited values and over the values in Tarjan's stack

            // ======================= Step 1: explore the non-visited values =======================
//            int pointerVar = valuesDynamic.getSource();
            int pointerVar = valuesDynamic.getPrevious(vars[var].getLB()); //Optimisation: start iterating from the lower bound instead of the beginning of the list (the list is sorted in ascending order)
            int var_ub = vars[var].getUB(); //Optimisation
            while (valuesDynamic.hasNext(pointerVar) && pointerVar < var_ub) { // Explore all the branches going out of var in the DFS tree
                pointerVar = valuesDynamic.trackPrev(pointerVar); // Go back in the list of unvisited values

                while(valuesDynamic.hasNext(pointerVar) && pointerVar < var_ub && !vars[var].contains(valuesDynamic.getNext(pointerVar))) { // Go to the last consecutive non-domain value
                    pointerVar = valuesDynamic.getNext(pointerVar);
                }
                if (valuesDynamic.hasNext(pointerVar) && pointerVar < var_ub) {// If we did not reach the end of the list of unvisited values, the next value is a domain value
                    process(var, valuesDynamic.getNext(pointerVar));
                    var_ub = vars[var].getUB(); //Optimisation: iterate until the upper bound instead of the end of the list (the list is sorted in ascending order)
                }
            }

            // ======================= Step 2 : update M(var).low thanks to the most ancient visited and unassigned value =======================
            for (int index = 0; index < topTarjan; index++) { // Iterate over tarjan's stack from the bottom until you find a value in the domain of var, or until it is not possible to decrease M(var).low
                val = tarjanStack[index];
                if (vars[var].contains(val) || getPre(val) >= getLow(matching.getMatchU(var))) {
                    setLow(matching.getMatchU(var), Math.min(getLow(matching.getMatchU(var)), getPre(val))); // M(var).low = min(M(var).low, val.pre)
                    break;
                }
            }
        }
        if (getPre(matching.getMatchU(var)) == getLow(matching.getMatchU(var))) {   // If M(var) is the root of its SCC, then we run the pruning procedure
            prune(matching.getMatchU(var));
        }

    }


    private void process(int var, int val) throws ContradictionException {
        if (matching.inMatchingV(val)) {    // If the value is already matched, we continue the exploration from its matched variable
            bimodalDFS(matching.getMatchV(val));
            setLow(matching.getMatchU(var), Math.min(getLow(matching.getMatchU(var)), getLow(val))); // M(var).low = min(M(var).low, val.low)
        } else {    // If the value is not matched it leads to the artificial node t_node, so we artificially explore it
            setPre(val, numVisit);
            setLow(val, 0);
            numVisit++;
            setLow(matching.getMatchU(var), 0); // M(var).low = 0
            valuesDynamic.remove(val);
            tarjanStack[topTarjan] = val;
            topTarjan++;
            declareInStack(val, true);
        }
    }


    /**
     * In this function we prune all the arcs coming from the variables of the discovered SCC and pointing toward values outside the discovered SCC
     */
    private void prune(int root) throws ContradictionException {
        complementSCC.refill();
        int var;
        int val;


        //======================= Step 1 : Get all the values of the discovered SCC and construct the complement =======================

        // We will use the max and min values in the SCC to update the lower and upper bounds of the variables in this SCC
        int minValueSCC = maxValue;
        int maxValueSCC = minValue;

        int rootIndex = topTarjan;
        // Declare the creation of a new SCC
        newSCC();

        do {
            // Empty Tarjan's stack
            rootIndex--;
            val = tarjanStack[rootIndex];
            declareInStack(val, false);
            // Empty the complement
            complementSCC.remove(val);
            // Update the bounds of the current SCC
            minValueSCC = Math.min(val, minValueSCC);
            maxValueSCC = Math.max(val, maxValueSCC);
            // Store the SCC
            addToSCC(val, getLastSCC());
        } while (val != root && rootIndex != 0);


        //======================= Step 2 : For each variable of the SCC, we prune their domain values that are not in the same SCC =======================


        // Particular case where we can force the instantiation of the matched variable to the unique value of the SCC, and remove them from the universes of variables and values
        if (topTarjan - rootIndex == 1) {
            // The unique value of the SCC is necessarily matched, otherwise it would have been in the same SCC as t_node
            var = matching.getMatchV(val);
            //NOTE: We can force the instantiation of var only if we do not use LCG, otherwise we remove the other values one by one and generate the corresponding explanations
            if (!vars[var].getModel().getSolver().isLCG()) {
                val = tarjanStack[rootIndex];
                if (vars[var].getDomainSize() > 1) {pruned = true;}
                vars[var].instantiateTo(val, aCause);
            }

        }

        if (!firstSCC) { // Run the pruning procedure only if it is not the first SCC discovered

            for (int index = rootIndex; index < topTarjan; index++) {
                val = tarjanStack[index];
                if (matching.inMatchingV(val)) {
                    var = matching.getMatchV(val);

                    //NOTE: we don't keep the bound update when using LCG
                    if (!vars[var].getModel().getSolver().isLCG() && vars[var].updateBounds(minValueSCC, maxValueSCC, aCause)) { // All values outside [minValueSCC, maxValueSCC] can not be present in the discovered SCC, and thus are pruned from the domain of every variable of the SCC
                        pruned = true;
                    }

                    if (vars[var].getDomainSize() > 1) { // If the domain is a singleton there is nothing to prune

                        if (vars[var].hasEnumeratedDomain()) { // If the domain is enumerated, proceed with the bimodal pruning
                            if (choice(PRUNE, var)) {  // If var has a small domain then iterate over the domain and prune the values that are in the complement
                                int ub = vars[var].getUB();
                                for (int domainValue = vars[var].getLB(); domainValue <= ub; domainValue = vars[var].nextValue(domainValue)) {
                                    if (complementSCC.isPresent(domainValue)) {
                                        pruned = true;
                                        // Prune the pair (var, domainValue) + Explanation
                                        generatePruningExplanation(var, domainValue);
                                    }
                                }

                            } else {    // If var has a large domain then iterate over the values in the complement and prune the ones that are in the domain of var
                                int complementValue = complementSCC.getSource();
                                while (complementSCC.hasNext(complementValue)) {
                                    complementValue = complementSCC.getNext(complementValue);
                                    if (vars[var].contains(complementValue)) {
                                        pruned = true;
                                        // Prune the pair (var, complementValue) + Explanation
                                        generatePruningExplanation(var, complementValue);
                                    }
                                }
                            }
                        } else { // If the domain is not enumerated but is an interval, then we prune the bounds in the right order
                            assert !vars[var].getModel().getSolver().isLCG() : "not compatible with LCG yet";
                            // Iterate over the interval from left to right until we reach a value that we can not prune
                            int ub = vars[var].getUB();
                            boolean boundPruned = true;
                            for (int boundValue = vars[var].getLB(); boundValue <= ub && boundPruned; boundValue++) {
                                if (complementSCC.isPresent(boundValue)) {
                                    generatePruningExplanation(var, boundValue);
                                } else {
                                    boundPruned = false;
                                }
                            }
                            // Iterate over the interval from right to left until we reach a value that we can not prune
                            int lb = vars[var].getLB();
                            boundPruned = true;
                            for (int boundValue = vars[var].getUB(); boundValue >= lb && boundPruned; boundValue--) {
                                if (complementSCC.isPresent(boundValue)) {
                                    generatePruningExplanation(var, boundValue);
                                } else {
                                    boundPruned = false;
                                }
                            }

                        }

                    }
                }
            }
        }
        firstSCC = false;
        topTarjan = rootIndex; // Remove the discovered SCC from Tarjan's stack
    }

    public void generatePruningExplanation(int sourceVar, int destinationVal) throws ContradictionException {
        Reason reason = Reason.undef();
        if (vars[sourceVar].getModel().getSolver().isLCG()) {
            int minValSCC = maxValue;
            int maxValSCC = minValue;
            int scc = getSCC(destinationVal);
            if (getSizeScc(scc) == 1) { // Got a specific way to generate the explanation in case of forced instantiation
                int matchedVar = matching.getMatchV(destinationVal);
                assert vars[matchedVar].isInstantiatedTo(destinationVal);
                reason = Reason.r(vars[matchedVar].getValLit());
            } else if (factorExists(scc)) { // Check whether the explanation related to the destination SCC has already been computed
                reason = Reason.r(MiniSat.neg(sccFactors[scc]));
            } else {
                // Get the minimum and maximum values of the destination SCC
                for (int i = getStartPositionSCC(scc); i < getEndPositionSCC(scc); i++) {
                    minValSCC = Math.min(minValSCC, sccPartition[i]);
                    maxValSCC = Math.max(maxValSCC, sccPartition[i]);
                }

                // Create the array of the explanation with the right size
                int[] explanation = new int[1 + getSizeScc(scc) * (2 + (maxValSCC - minValSCC + 1) - getSizeScc(scc))];

                // Store the values between minValSCC and maxValSCC that does not belong to the SCC
                int[] nonValues = new int[(maxValSCC - minValSCC + 1) - getSizeScc(scc)];
                int m = 0;
                for (int value = minValSCC + 1; value < maxValSCC; value++) {
                    if (!isInSCC(value, scc)) {
                        nonValues[m++] = value;
                    }
                }
                assert m == nonValues.length;
                // Start computing the explanation
                m = 1;
                for (int i = getStartPositionSCC(scc); i < getEndPositionSCC(scc); i++) {
                    int var = matching.getMatchV(sccPartition[i]);
                    explanation[m++] = MiniSat.neg(vars[var].getLit(minValSCC, LR_GE));
                    for (int value : nonValues) {
                        explanation[m++] = MiniSat.neg(vars[var].getLit(value, LR_NE));
                    }
                    explanation[m++] = MiniSat.neg(vars[var].getLit(maxValSCC, LR_LE));
                }
                assert m == explanation.length;

                // Create the corresponding factor
                int factor = MiniSat.makeLiteral(sat.newVariable(new MiniSat.ChannelInfo(null, -1, -1, -1, false)), true);
                sat.cEnqueue(factor, Reason.r(explanation));
                sccFactors[scc] = factor;
                upToDateFactor[scc] = updateKey;

                reason = Reason.r(MiniSat.neg(factor));

            }
        }
        vars[sourceVar].removeValue(destinationVal, aCause, reason);
    }

    //***********************************************************************************
    // Dynamic Structures and Backtrack Management
    //      In this section we manage the decrementality and backtrack of the universe of variables variablesDynamic
    //      and the universes of values valuesDynamic and complementSCC
    //      The backtrack operations are managed within the removeFromUniverse method of the TrackingList 
    //***********************************************************************************


    private void updateDynamicStructuresOpening(){
        // Repair the matching
        int var  = variablesDynamic.getSource();
        while (variablesDynamic.hasNext(var)) {
            var = variablesDynamic.getNext(var);
            if (vars[var].isInstantiated()) {
                if (matching.inMatchingU(var)) { // Unmatch the instantiated variable from its current matched value
                    matching.unMatch(var, matching.getMatchU(var));
                }
                if (matching.inMatchingV(vars[var].getValue())) { // Unmatch the value of the instantiated variable from its current matched variable
                    matching.unMatch(matching.getMatchV(vars[var].getValue()), vars[var].getValue());
                }
                matching.setMatch(var, vars[var].getValue()); //Match the instantiated variable and its value together
            } else if (matching.inMatchingU(var) && !vars[var].contains(matching.getMatchU(var))) { // Unmatch a variable from its matched value if it does not belong to the domain anymore
                matching.unMatch(var, matching.getMatchU(var));
            }
        }
    }

    private void updateDynamicStructuresEnding() {
        IEnvironment env = model.getEnvironment();

        // The remaining unvisited values are present in the domain of no variables, thus we can remove them from the universe of values for the next call to the propagator
        int val = valuesDynamic.getSource();
        while (valuesDynamic.hasNext(val)) {
            val = valuesDynamic.getNext(val);
            // Here we reuse tarjan's stack instead of creating a new data structure
            tarjanStack[topTarjan] = val;
            topTarjan++;
        }
        // We must refill the tracking lists before removing some elements from their universe, otherwise their structure is broken
        valuesDynamic.refill();
        complementSCC.refill();
        while (topTarjan != 0) {
            valuesDynamic.removeFromUniverse(tarjanStack[topTarjan - 1], env);
            complementSCC.removeFromUniverse(tarjanStack[topTarjan - 1], env);
            topTarjan--;
        }

        // Now that the pruning is done, we can remove from the universes of variables and values the pairs that were instantiated either before or during the call to this propagator
        int var = variablesDynamic.getSource();
        if (allEnum) { // If all the domains are enumerated, then we can safely proceed with the removals as all the required pruning has been done
            while (variablesDynamic.hasNext(var)) {
                var = variablesDynamic.getNext(var);
                if (vars[var].isInstantiated()) {
                    // The instantiated variables are removed from the universe
                    variablesDynamic.removeFromUniverse(var, env);

                    // The values of the instantiated variables are removed from the universe
                    valuesDynamic.removeFromUniverse(vars[var].getValue(), env);
                    complementSCC.removeFromUniverse(vars[var].getValue(), env);
                }
            }
        } else { // If all domains are not enumerated, then some instantiated values may still belong to the domain of a bounded variable. Thus, it must remain in the graph to be pruned in a future call to the propagator.
            while (variablesDynamic.hasNext(var)) {
                var = variablesDynamic.getNext(var);
                if (vars[var].isInstantiated() && !isInAnotherBoundedDomain(var, vars[var].getValue())) {
                    // The instantiated variables are removed from the universe
                    variablesDynamic.removeFromUniverse(var, env);

                    // The values of the instantiated variables are removed from the universe
                    valuesDynamic.removeFromUniverse(vars[var].getValue(), env);
                    complementSCC.removeFromUniverse(vars[var].getValue(), env);
                }
            }
        }
    }

    private boolean isInAnotherBoundedDomain(int var, int val) {
        int x = variablesDynamic.getSource();
        while (variablesDynamic.hasNext(x)) {
            x = variablesDynamic.getNext(x);
            if (x != var && !vars[x].hasEnumeratedDomain() && vars[x].contains(val)) {
                return true;
            }
        }
        return false;
    }

    //***********************************************************************************
    // Choice Functions for the Search Algorithms
    //      These functions decide whether the domain is considered as small or large
    //***********************************************************************************

    private boolean choice(int algo, int var) {
        switch (mode) {
            case AC_CLASSIC:
                return true;
            case AC_COMPLEMENT:
                return false;
            case AC_PARTIAL:
                if (algo == BFS || algo == DFS) {
//                    return vars[var].getDomainSize() < valuesDynamic.getSize();
                    return Math.max((vars[var].getUB() - vars[var].getLB() + 63) >> 6, vars[var].getDomainSize()) < valuesDynamic.getSize(); // More suited to BitSet domain representation
                } else {
//                    return vars[var].getDomainSize() < complementSCC.getSize();
                    return Math.max((vars[var].getUB() - vars[var].getLB() + 63) >> 6, vars[var].getDomainSize()) < complementSCC.getSize(); // More suited to BitSet domain representation
                }
            case AC_TUNED:
                if (algo == BFS) {
//                    return vars[var].getDomainSize() < valuesDynamic.getSize();
                    return Math.max((vars[var].getUB() - vars[var].getLB() + 63) >> 6, vars[var].getDomainSize()) < valuesDynamic.getSize(); // More suited to BitSet domain representation
                } else if (algo == DFS) {
//                    return vars[var].getDomainSize() < Math.sqrt(valuesDynamic.getSize());
                    return Math.max((vars[var].getUB() - vars[var].getLB() + 63) >> 6, vars[var].getDomainSize()) < Math.sqrt(valuesDynamic.getSize()); // More suited to BitSet domain representation
                } else {
//                    return vars[var].getDomainSize() < complementSCC.getSize();
                    return Math.max((vars[var].getUB() - vars[var].getLB() + 63) >> 6, vars[var].getDomainSize()) < complementSCC.getSize(); // More suited to BitSet domain representation
                }
        }
        return true;
    }


    //***********************************************************************************
    // Getter and Setter for Internal Data Structures
    //***********************************************************************************

    private int getParent(int val) {
        return parentBFS[val - minValue];
    }

    private void setParent(int var, int val) {
        parentBFS[val - minValue] = var;
    }

    private int getPre(int val) {
        return pre[val - minValue];
    }

    private void setPre(int val, int order) {
        pre[val - minValue] = order;
    }

    private int getLow(int val) {
        return low[val - minValue];
    }

    private void setLow(int val, int point) {
        low[val - minValue] = point;
    }

    private boolean isInStack(int val) {
        return inStack[val - minValue];
    }

    private void declareInStack(int val, boolean present) {
        inStack[val - minValue] = present;
    }

    private int getSCC(int val) {
        return sccBelonging[val - minValue];
    }

    private void setSCC(int val, int scc) {
        sccBelonging[val - minValue] = scc;
    }

    private boolean isUpToDateSCC(int val) {return upToDateSCC[val - minValue] == updateKey; }

    private void setUpToDateSCC(int val) {
        upToDateSCC[val - minValue] = updateKey;
    }

    private void resetSCCs() {
        numberOfSCCs = 0;
        updateKey++;
    }

    private int getLastSCC() {return numberOfSCCs - 1;}

    private void newSCC() {
        numberOfSCCs++;
        int lastSCC = getLastSCC();
        if (lastSCC == 0) {
            sccIndices[lastSCC] = 0;}
        else {
            sccIndices[lastSCC] = sccIndices[lastSCC - 1];}
    }

    private void addToSCC(int val, int scc) {
        sccPartition[sccIndices[scc]] = val;
        sccIndices[scc]++;
        setSCC(val, scc);
        setUpToDateSCC(val);
    }

    private boolean isInSCC(int val, int scc) {
        return getSCC(val) == scc && isUpToDateSCC(val);
        // The structure SCCbelonging is not reset between each call to the propagator, so some values may be outdated
        // So we ensure that the current SCCbelonging is up to date
    }

    private int getStartPositionSCC(int scc) {
        if(scc == 0) {
            return 0;
        } else {
            return sccIndices[scc - 1];
        }
    }

    private int getEndPositionSCC(int scc) {
        return sccIndices[scc];
    }

    private int getSizeScc(int scc) {return getEndPositionSCC(scc) - getStartPositionSCC(scc);}

    private boolean factorExists(int scc) {return upToDateFactor[scc] == updateKey; }
}
