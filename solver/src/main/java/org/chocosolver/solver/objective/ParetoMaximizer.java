/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.objective;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Class to store the pareto front (multi-objective optimization).
 * <p>
 * Based on "Multi-Objective Large Neighborhood Search", P. Schaus , R. Hartert (CP'2013)
 * </p>
 *
 * @author Charles Vernerey
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 */
public class ParetoMaximizer extends Propagator<IntVar> implements IMonitorSolution {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // Set of incomparable and Pareto-best solutions
    private final LinkedList<Solution> paretoSolutions;
    private final LinkedList<int[]> paretoFront;

    private final Model model;

    // Allow to recycle (dominated) Solution objects
    private final LinkedList<Solution> poolSols = new LinkedList<>();

    // objective function
    private final IntVar[] objectives;
    private final int n;

    //private final int[] vals;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Create an object to compute the Pareto front of a multi-objective problem.
     * Objectives are expected to be maximized (use {@link org.chocosolver.solver.variables.IViewFactory#intMinusView(IntVar)} in case of minimisation).
     * <p>
     * Maintain the set of dominating solutions and
     * posts constraints dynamically to prevent search from computing dominated ones.
     * <p>
     * The Solutions store decision variables (those declared in the search strategy)
     * BEWARE: requires the objectives to be declared in the search strategy
     *
     * @param objectives objective variables (must all be optimized in the same direction)
     */
    public ParetoMaximizer(final IntVar[] objectives) {
        super(objectives, PropagatorPriority.QUADRATIC, false);
        this.paretoSolutions = new LinkedList<>();
        this.paretoFront = new LinkedList<>();
        this.objectives = objectives.clone();
        n = objectives.length;
        model = objectives[0].getModel();
        //vals = new int[n];
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    /**
     * @return the set of Pareto-best (possibly optimal) solutions found so far
     */
    public List<Solution> getParetoFront() {
        return paretoSolutions;
    }

    @Override
    public void onSolution() {
        // get objective values
        int[] vals = Stream.of(objectives).mapToInt(IntVar::getValue).toArray();
        // remove dominated solutions
        for (int i = paretoFront.size() - 1; i >= 0; i--) {
            if (isDominated(paretoSolutions.get(i), vals)) {
                poolSols.add(paretoSolutions.remove(i));
                paretoFront.remove(i);
            }
        }
        // store current solution
        Solution solution;
        if (poolSols.isEmpty()) {
            solution = new Solution(model);
        } else {
            solution = poolSols.remove();
        }
        solution.record();
        paretoSolutions.add(solution);
        paretoFront.add(vals);
    }

    private boolean isDominated(Solution solution, int[] vals) {
        for (int i = 0; i < n; i++) {
            int delta = solution.getIntVal(objectives[i]) - vals[i];
            if (delta > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < objectives.length; i++) {
            computeTightestPoint(i);
        }
    }

    /**
     * Compute tightest point for objective i
     * i.e. the point that dominates DP_i and has the biggest obj_i
     *
     * @param i index of the variable
     */
    private void computeTightestPoint(int i) throws ContradictionException {
        int tightestPoint = Integer.MIN_VALUE;
        int[] dominatedPoint = computeDominatedPoint(i);
        for (int[] sol : paretoFront) {
            int dominates = dominates(sol, dominatedPoint, i);
            if (dominates > 0) {
                int currentPoint = dominates == 1 ? sol[i] : sol[i] + 1;
                if (tightestPoint < currentPoint) {
                    tightestPoint = currentPoint;
                }
            }
        }
        if (tightestPoint > Integer.MIN_VALUE) {
            objectives[i].updateLowerBound(tightestPoint, this);
        }
    }

    /**
     * Compute dominated point for objective i,
     * i.e. DP_i = (obj_1_max,...,obj_i_min,...,obj_m_max)
     *
     * @param i index of the variable
     * @return dominated point
     */
    private int[] computeDominatedPoint(int i) {
        int[] dp = Stream.of(objectives).mapToInt(IntVar::getUB).toArray();
        dp[i] = objectives[i].getLB();
        return dp;
    }

    /**
     * Return an int :
     * 0 if a doesn't dominate b
     * 1 if a dominates b and a = b if we don't take into account index i
     * 2 if a dominates b and a dominates b if we don't take into account index i
     *
     * @param a vector
     * @param b vector
     * @param i index
     * @return an int representing the fact that a dominates b
     */
    private int dominates(int[] a, int[] b, int i) {
        int dominates = 0;
        for (int j = 0; j < objectives.length; j++) {
            if (a[j] < b[j]) return 0;
            if (a[j] > b[j]) {
                if (dominates == 0) dominates = 1;
                if (j != i) dominates = 2;
            }
        }
        return dominates;
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }
}
