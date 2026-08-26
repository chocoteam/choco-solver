/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.knapsack.structure.*;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Propagator for the 0/1-Knapsack constraint using cost-based filtering.
 * <p>
 * This propagator implements the algorithm described in:
 * <ul>
 *   <li>Fahle, T., & Sellmann, M. (2002). Cost Based Filtering for the Constrained Knapsack Problem.
 *       Annals of Operations Research, 115, 73-93.</li>
 *   <li>Katriel, I., Sellmann, M., Upfal, E., & Van Hentenryck, P. (2007).
 *       Propagating Knapsack Constraints in Sublinear Time. AAAI 2007, 231-236.</li>
 * </ul>
 * <p>
 * The algorithm performs cost-based filtering by:
 * <ol>
 *   <li>Sorting items by decreasing efficiency (profit/weight ratio), breaking ties in favor of larger weights.</li>
 *   <li>Computing the Dantzig relaxation to find the critical item (first item that would exceed capacity).</li>
 *   <li>Using two specialized search trees:
 *       <ul>
 *         <li>{@code ItemFindingSearchTree}: To efficiently find the next item to check, based on weight.</li>
 *         <li>{@code ComputingLossWeightTree}: To compute the maximum weight that can be replaced
 *             without violating the profit bound.</li>
 *       </ul>
 *   </li>
 *   <li>Identifying mandatory items (items that must be included in any solution) and
 *       forbidden items (items that cannot be included in any solution).</li>
 * </ol>
 * <p>
 * The propagator maintains the following invariant:
 * For a given lower bound B on the objective (profit), an item x_i is:
 * <ul>
 *   <li><b>Mandatory</b> if the fractional optimum of (X \ {x_i}, C) &lt; B.</li>
 *   <li><b>Forbidden</b> if the fractional optimum of (X \ {x_i}, C - w_i) + p_i &lt; B.</li>
 * </ul>
 * <p>
 * This implementation uses an incremental approach with amortized linear time complexity
 * per call after an O(n log n) preprocessing step.
 *
 * @author Nicolas PIERRE
 * @author Charles Prud'homme
 */
public class PropKnapsackKatriel01 extends Propagator<IntVar> {
    
    /**
     * State constant indicating that an item has been added to the solution.
     * An item in this state is included in every valid solution and its value 0
     * has been removed from the variable domain.
     */
    static final int ADDED = 1;
    
    /**
     * State constant indicating that an item has been removed from the problem.
     * An item in this state cannot be included in any valid solution and its value 1
     * has been removed from the variable domain.
     */
    static final int REMOVED = -1;
    
    /**
     * State constant indicating that an item's status is not yet determined.
     * Items in this state may or may not be included in the optimal solution.
     */
    static final int NOT_DEFINED = 0;
    // ***********************************************************************************
    // VARIABLES
    // ***********************************************************************************

    /**
     * Array mapping original item indices to their position in the efficiency-sorted order.
     * Items are sorted by decreasing efficiency (profit/weight), with ties broken in favor of larger weights.
     */
    private final int[] order;
    
    /**
     * Array mapping efficiency-sorted indices back to original item indices.
     * This is the inverse of {@link #order}.
     */
    private final int[] reverseOrder;
    
    /**
     * Number of items in the knapsack problem.
     */
    private final int n;
    
    /**
     * The capacity variable of the knapsack (maximum allowed weight).
     * The upper bound of this variable represents the available capacity.
     */
    private final IntVar capacity;
    
    /**
     * The total profit variable of the knapsack (profit to maximize).
     * The lower bound of this variable represents the minimum required profit.
     */
    private final IntVar totalProfit;
    
    /**
     * State of each item: {@link #NOT_DEFINED}, {@link #ADDED}, or {@link #REMOVED}.
     * This array tracks which items have been forced into or out of the solution.
     */
    private final int[] itemState;
    
    /**
     * Search tree for efficiently finding the next item to check based on weight.
     * Used to implement the monotonicity property: when processing items in weight order,
     * the critical item position increases monotonically.
     */
    private final ItemFindingSearchTree findingTree;
    
    /**
     * Search tree for computing the maximum weight that can be replaced without violating
     * the profit bound. Contains all items sorted by efficiency and stores cumulative weight
     * and profit sums in internal nodes.
     */
    private final ComputingLossWeightTree computingTree;
    
    /**
     * Information about the critical item from the Dantzig relaxation:
     * index (position in the tree), profit (relaxed solution profit),
     * weight (total weight used), and weightWithoutCriticalItem (weight without the critical item).
     */
    private Info criticalItemInfos;
    
    // ***********************************************************************************
    // STATE VARIABLES
    // ***********************************************************************************

    /**
     * Total weight of items currently included in the solution.
     * This is updated when items are added to the solution via {@link #addItemToSolution}.
     */
    private int totalWeight;
    
    /**
     * Total profit of items currently included in the solution.
     * This is updated when items are added to the solution via {@link #addItemToSolution}.
     */
    private int accumulatedProfit;
    
    /**
     * Flag indicating whether the critical item information needs to be recomputed.
     * This is set to true when the world changes (backtrack/restart) or when items are added/removed.
     */
    private boolean mustRecomputeCriticalInfos;
    
    /**
     * Last known world index, used to detect backtracks.
     */
    private int lastWorld = -1;
    
    /**
     * Last known backtrack count, used to detect backtracks.
     */
    private long lastNbOfBacktracks = -1;
    
    /**
     * Last known restart count, used to detect restarts.
     */
    private long lastNbOfRestarts = -1;

    // ***********************************************************************************
    // CONSTRUCTORS
    // ***********************************************************************************

    /**
     * Constructs a knapsack propagator using the Katriel et al. algorithm.
     *
     * @param itemOccurence array of boolean variables indicating whether each item is included (1) or not (0)
     * @param capacity      the integer variable representing the knapsack capacity (maximum weight)
     * @param totalProfit   the integer variable representing the knapsack total profit
     * @param weight        array of item weights
     * @param profit        array of item profits
     */
    public PropKnapsackKatriel01(BoolVar[] itemOccurence, IntVar capacity, IntVar totalProfit,
                                 int[] weight, int[] profit) {
        super(ArrayUtils.append(itemOccurence, new IntVar[]{capacity, totalProfit}), PropagatorPriority.QUADRATIC, true);
        this.n = itemOccurence.length;
        this.itemState = new int[n];
        this.reverseOrder = new int[n];
        this.capacity = capacity;
        this.totalProfit = totalProfit;
        this.totalWeight = 0;
        this.accumulatedProfit = 0;
        Arrays.fill(this.itemState, 0);
        // we find the decreasing order of efficiency
        this.order = ArrayUtils.array(0, n - 1);
        ArraySort<Integer> sorter = new ArraySort<>(n, false, true);
        sorter.sort(order, n, (i1, i2) -> {
            // Compares efficiencies decreasingly
            long comparaison = (long) profit[i2] * weight[i1] - (long) profit[i1] * weight[i2];
            if (comparaison == 0) {
                if (weight[i1] * weight[i2] == 0) {
                    comparaison = (long) profit[i2] - profit[i1];
                } else {
                    // breaking ties in favor of larger weights
                    comparaison = (long) weight[i2] - weight[i1];
                }
            }
            return Long.signum(comparaison);
        });
        ArrayList<KPItem> orderedItems = new ArrayList<>();
        orderedItems.ensureCapacity(n);
        for (int i = 0; i < n; ++i) {
            orderedItems.add(new KPItem(profit[order[i]], weight[order[i]]));
            reverseOrder[order[i]] = i;
        }
        this.findingTree = new ItemFindingSearchTree(orderedItems);
        this.computingTree = new ComputingLossWeightTree(orderedItems);
        this.mustRecomputeCriticalInfos = true;
    }

    // ***********************************************************************************
    // METHODS
    // ***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx < n) {
            // updates on items
            return IntEventType.boundAndInst();
        } else if (vIdx == n) {
            // updates on the max weight
            return IntEventType.upperBoundAndInst();
        } else /* vIdx == n + 1 */ {
            // updates on the total profit variable
            return IntEventType.lowerBoundAndInst();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // Recompute critical item information if needed (after backtrack, restart, or capacity change)
        if (mustRecomputeCriticalInfos()) {
            // compute the Dantzig solution and set members variables
            this.criticalItemInfos = this.computingTree.findCriticalItem(this.capacity.getUB() - totalWeight);
            mustRecomputeCriticalInfos = false;
        }
        // if total profit LB is not reachable so we can't filter
        // (every item would be mandatory and forbidden)
        if (criticalItemInfos.profit() + accumulatedProfit >= totalProfit.getLB()) {
            TIntList mandatoryList = findMandatoryItems();
            TIntList forbiddenList = findForbiddenItems();
            // Process mandatory items: add them to the solution
            for (int i = 0; i < mandatoryList.size(); i++) {
                int unorderedLeafIdx = mandatoryList.get(i);
                addItemToSolution(unorderedLeafIdx, true);
                // case 3.  from mustRecomputeCriticalInfos()
                mustRecomputeCriticalInfos = true;
            }
            // Process forbidden items: remove them from the problem
            for (int i = 0; i < forbiddenList.size(); i++) {
                int unorderedLeafIdx = forbiddenList.get(i);
                removeItemFromProblem(unorderedLeafIdx, true);
                // case 3.  from mustRecomputeCriticalInfos()
                mustRecomputeCriticalInfos = true;
            }
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (varIdx < n) {
            // item changed
            // we update the trees
            if (this.vars[varIdx].isInstantiatedTo(0)) {
                this.removeItemFromProblem(varIdx, false);
            } else if (this.vars[varIdx].isInstantiatedTo(1)) {
                this.addItemToSolution(varIdx, false); // TODO
            }
        }
        // case 2. from mustRecomputeCriticalInfos()
        mustRecomputeCriticalInfos |= varIdx < n + 1;
        forcePropagate(PropagatorEventType.FULL_PROPAGATION);

    }

    /**
     * Checks if the critical item information needs to be recomputed.
     * This happens when:
     * <ul>
     *   <li>A backtrack has occurred</li>
     *   <li>A restart has occurred</li>
     *   <li>The capacity upper bound has changed</li>
     *   <li>An item has been added to or removed from the solution</li>
     * </ul>
     *
     * @return true if critical item information must be recomputed, false otherwise
     */
    private boolean mustRecomputeCriticalInfos() {
        checkWorld();
        return mustRecomputeCriticalInfos;
    }

    /**
     * Checks if the solver's world has changed (due to backtrack or restart).
     * If a change is detected, sets the flag to recompute critical item information.
     */
    private void checkWorld() {
        int currentworld = model.getEnvironment().getWorldIndex();
        long currentbt = model.getSolver().getBackTrackCount();
        long currentrestart = model.getSolver().getRestartCount();
        if (currentworld < lastWorld || currentbt != lastNbOfBacktracks || currentrestart > lastNbOfRestarts) {
            // case 1.  from mustRecomputeCriticalInfos()
            mustRecomputeCriticalInfos = true;
        }
        lastWorld = currentworld;
        lastNbOfBacktracks = currentbt;
        lastNbOfRestarts = currentrestart;
    }

    @Override
    public ESat isEntailed() {
        // sum propagators of the KP contraint define the entailment
        return ESat.TRUE;
    }

    /**
     * changes the constraint such that the item is in the solution
     * Informs backtrack environment what to do
     *
     * @param i              index of an item in the list given in the constructor
     * @param removeVarValue true to remove the value in the variable domain
     */
    private void addItemToSolution(int i, boolean removeVarValue) throws ContradictionException {
        if (this.itemState[i] == NOT_DEFINED) {
            this.itemState[i] = ADDED;
            int sortedGlobalIndex = computingTree.leafToGlobalIndex(this.reverseOrder[i]);
            computingTree.removeLeaf(sortedGlobalIndex);
            findingTree.removeLeaf(sortedGlobalIndex);
            // we update intern values
            this.totalWeight += computingTree.getLeaf(sortedGlobalIndex).getActivatedWeight();
            this.accumulatedProfit += computingTree.getLeaf(sortedGlobalIndex).getActivatedProfit();
            getEnvironment().save(() -> activateItemToProblem(i, ADDED));
            if (removeVarValue) {
                vars[i].removeValue(0, this);
            }
        }
    }

    /**
     * changes the constraint such that the item is NOT in the solution
     * Informs backtrack environment what to do
     *
     * @param i              index of an item in the list given in the constructor
     * @param removeVarValue true to remove the value in the variable domain
     */
    private void removeItemFromProblem(int i, boolean removeVarValue) throws ContradictionException {
        if (this.itemState[i] == NOT_DEFINED) {
            this.itemState[i] = REMOVED;
            int sortedGlobalIndex = computingTree.leafToGlobalIndex(this.reverseOrder[i]);
            computingTree.removeLeaf(sortedGlobalIndex);
            findingTree.removeLeaf(sortedGlobalIndex);
            getEnvironment().save(() -> activateItemToProblem(i, REMOVED));
            if (removeVarValue) {
                vars[i].removeValue(1, this);
            }
        }
    }

    /**
     * changes the propagator state such that the item is not determined anymore
     *
     * @param i             index of an item in the list given in the constructor
     * @param expectedState state of the item when reverting
     */
    private void activateItemToProblem(int i, int expectedState) {
        if (this.itemState[i] == expectedState) {
            int sortedGlobalIndex = computingTree.leafToGlobalIndex(this.reverseOrder[i]);
            computingTree.activateLeaf(sortedGlobalIndex);
            findingTree.activateLeaf(sortedGlobalIndex);
            if (this.itemState[i] == ADDED) {
                // we update intern values as the item was added to every solutions
                this.totalWeight -= computingTree.getLeaf(sortedGlobalIndex).getActivatedWeight();
                this.accumulatedProfit -= computingTree.getLeaf(sortedGlobalIndex).getActivatedProfit();
            }
            this.itemState[i] = NOT_DEFINED;
        } else if (this.itemState[i] != NOT_DEFINED) {
            throw new RuntimeException("the item reverted does not have the expected state");
        }
    }

    /**
     * Finds all mandatory items by scanning items to the left of the critical item.
     * <p>
     * An item x_i (i &lt; s, where s is the critical item) is mandatory if removing it
     * would make it impossible to reach the lower bound on profit.
     * This is determined by checking if the fractional optimum of (X \ {x_i}, C) &lt; B,
     * where B is the current lower bound on profit.
     * <p>
     * The method uses the monotonicity property: when processing items in weight order,
     * the critical item position increases monotonically, allowing a linear scan.
     *
     * @return list of indices (from the constructor's item list) of mandatory items
     */
    private TIntList findMandatoryItems() {

        TIntList mandatoryList = new TIntArrayList();
        double allowedProfitLoss = criticalItemInfos.profit() + accumulatedProfit - totalProfit.getLB();
        // finding first active item
        int index = computingTree.leafToGlobalIndex(0);
        if (index != -1) {
            if (!computingTree.getLeaf(index).isActive()) {
                index = findingTree.findNextRightItem(index, criticalItemInfos.index(), 0);
            }
            // not a trivial KP
            int maxWeight = 0;
            double criticalItemRemainingWeight = 0;
            if (computingTree.isLeaf(criticalItemInfos.index())) {
                criticalItemRemainingWeight = computingTree.getNodeWeight(criticalItemInfos.index())
                        - (criticalItemInfos.weight() - criticalItemInfos.weightWithoutCriticalItem());
            }
            SearchInfos infos = new SearchInfos(false, criticalItemInfos.index(),
                    0, 0, criticalItemRemainingWeight);

            while (index != -1) {
                infos = computingTree.computeLimitWeightMandatory(criticalItemInfos, index, infos.lastItemIndex(),
                        infos.accumulatedProfit(), infos.accumulatedWeight(), allowedProfitLoss,
                        infos.remainingWeight());
                if (infos.decision()) {
                    mandatoryList.add(order[computingTree.globalToLeaf(index)]);
                } else {
                    maxWeight = Math.max(maxWeight, computingTree.getNodeWeight(index));
                    maxWeight = Math.max(maxWeight, (int) infos.accumulatedWeight());
                }
                index = findingTree.findNextRightItem(index, criticalItemInfos.index(), maxWeight);
            }
        }
        return mandatoryList;
    }

    /**
     * Finds all forbidden items by scanning items to the right of the critical item.
     * <p>
     * An item x_i (i &gt; s, where s is the critical item) is forbidden if including it
     * would make it impossible to reach the lower bound on profit.
     * This is determined by checking if the fractional optimum of (X \ {x_i}, C - w_i) + p_i &lt; B,
     * where B is the current lower bound on profit.
     * <p>
     * The method uses the monotonicity property: when processing items in weight order,
     * the critical item position decreases monotonically, allowing a linear scan.
     *
     * @return list of indices (from the constructor's item list) of forbidden items
     */
    private TIntList findForbiddenItems() {

        TIntList forbiddenList = new TIntArrayList();
        double allowedProfitLoss = criticalItemInfos.profit() + accumulatedProfit - totalProfit.getLB();
        // finding first active item
        int index = criticalItemInfos.index();
        if (index != -1 && criticalItemInfos.index() != computingTree.getNumberNodes()) {
            int maxWeight = 0;
            double criticalItemIncludedWeight = criticalItemInfos.weight() - criticalItemInfos.weightWithoutCriticalItem();
            if (!computingTree.getLeaf(index).isActive()) {
                index = findingTree.findNextRightItem(index, computingTree.getNumberNodes() - 1, maxWeight);
            }
            SearchInfos infos = new SearchInfos(false, criticalItemInfos.index(),
                    0, 0, criticalItemIncludedWeight);
            while (index != -1) {
                infos = computingTree.computeLimitWeightForbidden(criticalItemInfos, index, infos.lastItemIndex(),
                        infos.accumulatedProfit(), infos.accumulatedWeight(), allowedProfitLoss,
                        infos.remainingWeight());
                if (infos.decision()) {
                    forbiddenList.add(order[computingTree.globalToLeaf(index)]);
                } else {
                    maxWeight = Math.max(maxWeight, (int) infos.accumulatedWeight());
                    maxWeight = Math.max(maxWeight, computingTree.getNodeWeight(index));
                }
                index = findingTree.findNextRightItem(index, computingTree.getNumberNodes() - 1, maxWeight);
            }
        }
        return forbiddenList;
    }

    /**
     * Returns the environment associated with this propagator's model.
     * The environment is used for saving restoration callbacks during backtracking.
     *
     * @return the environment of the model
     */
    private IEnvironment getEnvironment() {
        return this.getModel().getEnvironment();
    }
}
