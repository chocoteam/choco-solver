/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import org.chocosolver.memory.IStateDouble;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.loop.monitors.IMonitorDownBranch;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.IntList;

import java.util.Random;

/**
 * Implementation of the search described in:
 * "Impact-Based Search Strategies for Constraint Programming",
 * Philippe Refalo, CP2004.
 * <br/>
 * <p/>
 * Impacts of variables are first computed on initialisation, (only 3 values are tested for bounded variables);
 * Ties are broken randomly.
 *
 * @author Charles Prud'homme
 * @since 21/09/12
 */
public class ImpactBased extends AbstractStrategy<IntVar> implements IMonitorDownBranch, IMonitorContradiction, ICause {

    private final int aging; // aging parameter
    private double[][] Ilabel; // impact per labeling
    private int[] offsets; // initial lower bound of each variable
    private int split; // domains are divided into at most 2^s subdomains
    private IStateDouble searchSpaceSize;

    private int currentVar = -1, currentVal = -1;

    private IntList bests = new IntList();

    private java.util.Random random; //  a random object to break ties

    private int nodeImpact;

    private Model model;

    private boolean asgntFailed; // does the assignment leads to a failure

    private boolean learnsAndFails; // does the learning pahse leads to a failure

    private long timeLimit = Integer.MAX_VALUE; // a time limit for init()

    /**
     * Create an Impact-based search strategy with Node Impact strategy.
     * <p/>
     * <b>"Impact-Based Search Strategies for Constraint Programming",
     * Philippe Refalo, CP2004.</b>
     *
     * @param ivariables variables of the problem (should be integers)
     * @param alpha      aging parameter
     * @param split      split parameter for subdomains computation
     * @param nodeImpact force update of impacts every <code>nodeImpact</code> nodes. Set value to 0 to avoid using it.
     * @param seed       a seed for random
     * @param initOnly   only apply the initialisation phase, do not update impact thereafter
     */
    public ImpactBased(IntVar[] ivariables, int alpha, int split, int nodeImpact, long seed, boolean initOnly) { //TODO: node impacts
        super(ivariables);
        this.model = ivariables[0].getModel();
        this.aging = alpha;
        this.split = (int) Math.pow(2, split);
        this.searchSpaceSize = model.getEnvironment().makeFloat();
        random = new Random(seed);
        this.nodeImpact = nodeImpact;
        if (!initOnly) model.getSolver().plugMonitor(this);
    }

    public ImpactBased(IntVar[] vars, boolean initOnly){
        this(vars,2,3,10,0,initOnly);
    }

    @Override
    public Decision<IntVar> computeDecision(IntVar variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        if (currentVar == -1 || vars[currentVar] != variable) {
            // retrieve indice of the variable in vars
			for(int i=0;i<vars.length;i++){
				if(vars[i]==variable){
					currentVar = i;
				}
			}
            assert vars[currentVar] == variable;
        }
        bests.clear();
        double bestImpact = 1.0;
        if (variable.hasEnumeratedDomain()) {
            DisposableValueIterator it = variable.getValueIterator(true);
            int o = offsets[currentVar];
            while (it.hasNext()) {
                int val = it.next();
                double impact = Ilabel[currentVar][val - o];
                if (impact < bestImpact) {
                    bests.clear();
                    bests.add(val);
                    bestImpact = impact;
                } else if (impact == bestImpact) {
                    bests.add(val);
                }
            }
            it.dispose();

            currentVal = bests.get(random.nextInt(bests.size()));
        } else {
            int lb = variable.getLB();
            int ub = variable.getUB();
            currentVal = random.nextBoolean() ? lb : ub;
        }
        return model.getSolver().getDecisionPath().makeIntDecision(variable, DecisionOperatorFactory.makeIntEq(), currentVal);
    }

    @Override
    public Decision<IntVar> getDecision() {
        IntVar best = null;
        // 1. first select the variable with the largest impact
        bests.clear();
        double bestImpact = -Double.MAX_VALUE;
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].isInstantiated()) {
                double imp = computeImpact(i);
                if (imp > bestImpact) {
                    bests.clear();
                    bests.add(i);
                    bestImpact = imp;
                } else if (imp == bestImpact) {
                    bests.add(i);
                }
            }
        }
        if (bests.size() > 0) {
            // 2. select the variable
            currentVar = bests.get(random.nextInt(bests.size()));
            best = vars[currentVar];
        }
        return computeDecision(best);
    }

    public void setTimeLimit(long timeLimit) {
        if (timeLimit > -1) {
            this.timeLimit = timeLimit;
        }
    }

    @Override
    public boolean init(){
        long tl = System.currentTimeMillis() + this.timeLimit;
        // 0. Data structure construction
        Ilabel = new double[vars.length][];
        offsets = new int[vars.length];
        // 1. Estimation of assignment and variable impacts
        double before = searchSpaceSize();
        searchSpaceSize.set(before);
        learnsAndFails = false;
        loop:
        for (int i = 0; i < vars.length; i++) {
            IntVar v = vars[i];
            int offset = v.getLB();
            int UB = v.getUB();
            int dsz = UB - offset + 1;//v.getDomainSize();
            if (!v.isInstantiated()) { // if the variable is not instantiated
                Ilabel[i] = new double[v.hasEnumeratedDomain() ? dsz : 1];
                offsets[i] = offset;

                if (v.hasEnumeratedDomain()) {
                    if (v.getDomainSize() < split) { // try each value
                        DisposableValueIterator it = v.getValueIterator(true);
                        while (it.hasNext()) {
                            if (System.currentTimeMillis() > tl) {
                                break loop;
                            }
                            int a = it.next();
                            double im = computeImpact(v, a, before);
                            Ilabel[i][a - offset] = im;
                        }
                        it.dispose();
                    } else { // estimate per subdomains
                        int step = 0;
                        int size = dsz / split;
                        DisposableValueIterator it = v.getValueIterator(true);
                        while (it.hasNext()) {
                            if (System.currentTimeMillis() > tl) {
                                break loop;
                            }
                            int a = it.next();
                            double im;
                            if (step % size == 0) {
                                im = computeImpact(v, a, before);
                            } else {
                                im = Ilabel[i][a - 1 - offset];
                            }
                            Ilabel[i][a - offset] = im;
                            step++;
                        }
                        it.dispose();
                    }
                } else {
                    if (System.currentTimeMillis() > tl) {
                        break;
                    }
                    // A. choose 3 values in the domain to have an estimation of the impact
                    double i1 = computeImpact(v, v.getLB(), before);
                    double i2 = computeImpact(v, v.getUB(), before);
                    double i3 = computeImpact(v, (v.getLB() + v.getUB()) / 2, before);
                    Ilabel[i][0] = (i1 + i2 + i3) / 3d;
                }
            }
        }
        if (learnsAndFails) {
            // If the initialisation detects a failure, then the problem has no solution!
            learnsAndFails = false;
//            solver.getEngine().fails(this, lAfVar, "Impact::init:: detect failures");
            return false;
        } else if (System.currentTimeMillis() > tl) {
            if(model.getSettings().warnUser()) model.getSolver().getErr().printf("impact Search stops its init phase -- reach time limit!");
            for (int i = 0; i < vars.length; i++) {  // create arrays to avoid null pointer errors
                IntVar v = vars[i];
                int offset = v.getLB();
                int UB = v.getUB();
                int dsz = UB - offset + 1;//v.getDomainSize();
                if (!v.isInstantiated() && Ilabel[i] == null) {
                    Ilabel[i] = new double[v.hasEnumeratedDomain() ? dsz : 1];
                    offsets[i] = offset;
                }
            }
        }
        return true;
    }


    @Override
    public void onContradiction(ContradictionException cex) {
        asgntFailed = true;
    }



    @Override
    public void beforeDownBranch(boolean left) {
    }

    @Override
    public void afterDownBranch(boolean left) {
        if(left){
            if (currentVar > -1) { // if the decision was computed by another strategy
                if (asgntFailed) {
                    updateImpact(1.0d, currentVar, currentVal);
                } else {
                    double sssz = searchSpaceSize();
                    updateImpact(sssz / searchSpaceSize.get(), currentVar, currentVal);
                    searchSpaceSize.set(sssz);
                }
                currentVar = -1;
            }
            asgntFailed = false; // to handle cases where a contradiction was thrown, but the decision was computed outside
        }
        reevaluateImpact();
    }

    /**
     * Compute the impact of a <b>variable</b>
     *
     * @param idx index of the variable
     * @return the impact of the variable idx
     */
    private double computeImpact(int idx) {
        IntVar var = vars[idx];
        if (var.hasEnumeratedDomain()) {
            int of = offsets[idx];
            DisposableValueIterator it = var.getValueIterator(true);
            double impact = 0.0;
            while (it.hasNext()) {
                int val = it.next();
                impact += Ilabel[idx][val - of];
            }
            it.dispose();
            return impact - var.getDomainSize();
        } else {
            return Ilabel[idx][0] - var.getDomainSize();
        }
    }

    /**
     * Compute the impact of an <b>assignment</b>
     *
     * @param v      the variable
     * @param a      the value
     * @param before search space size before the assignment
     * @return the impact I(v = a)
     */
    private double computeImpact(IntVar v, int a, double before) {
        model.getEnvironment().worldPush();
        double after;
        try {
            v.instantiateTo(a, this);
            model.getSolver().getEngine().propagate();
            after = searchSpaceSize();
            return 1.0d - (after / before);
        } catch (ContradictionException e) {
            model.getSolver().getEngine().flush();
            model.getEnvironment().worldPop();
            model.getEnvironment().worldPush();
            // if the value leads to fail, then the value can be removed from the domain
            try {
                v.removeValue(a, this);
                model.getSolver().getEngine().propagate();
            } catch (ContradictionException ex) {
                learnsAndFails = true;
                model.getSolver().getEngine().flush();
            }
            return 1.0d;
        }finally {
            model.getEnvironment().worldPop();
        }
    }

    /**
     * Update the impact of an assignment I(v=a)
     *
     * @param nImpact new impact
     * @param varIdx  index of the variable
     * @param valIdx  index of the value
     */
    private void updateImpact(double nImpact, int varIdx, int valIdx) {
        valIdx = Ilabel[varIdx].length > 1 ? valIdx - offsets[varIdx] : 0;
        double impact = Ilabel[varIdx][valIdx] * (aging - 1);
        impact += nImpact;
        impact /= aging;
        Ilabel[varIdx][valIdx] = impact;
    }

    /**
     * Compute the search space size
     *
     * @return search space size
     */
    private double searchSpaceSize() {
        double size = 1;
        for (int i = 0; i < vars.length; i++) {
            size *= vars[i].getDomainSize();
            assert size > 0 : "Search space is not correct!";
        }
        if (size == Double.POSITIVE_INFINITY) {
            size = Double.MAX_VALUE;
        }
        return size;
    }

    private void reevaluateImpact() {
        if (nodeImpact > 0 && model.getSolver().getNodeCount() % nodeImpact == 0) {
            double before = searchSpaceSize.get();
            learnsAndFails = false;
            for (int i = 0; i < vars.length; i++) {
                IntVar v = vars[i];
                int dsz = v.getDomainSize();
                if (!v.isInstantiated()) { // if the variable is not instantiated
                    int offset = v.getLB();
                    if (v.hasEnumeratedDomain()) {
                        if (v.getDomainSize() < split) { // try each value
                            DisposableValueIterator it = v.getValueIterator(true);
                            while (it.hasNext()) {
                                int a = it.next();
                                double im = computeImpact(v, a, before);
                                updateImpact(im, i, a);
                            }
                            it.dispose();
                        } else { // estimate per subdomains
                            int step = 0;
                            int size = dsz / split;
                            DisposableValueIterator it = v.getValueIterator(true);
                            while (it.hasNext()) {
                                int a = it.next();
                                double im;
                                if (step % size == 0) {
                                    im = computeImpact(v, a, before);
                                } else {
                                    im = Ilabel[i][a - 1 - offset];
                                }
                                updateImpact(im, i, a);
                                step++;
                            }
                            it.dispose();
                        }
                    } else {
                        // A. choose 3 values in the domain to have an estimation of the impact
                        double i1 = computeImpact(v, v.getLB(), before);
                        double i2 = computeImpact(v, v.getUB(), before);
                        double i3 = computeImpact(v, (v.getLB() + v.getUB()) / 2, before);
                        updateImpact((i1 + i2 + i3) / 3d, i, 0);
                    }
                }
            }
            if (learnsAndFails) {
                learnsAndFails = false;
            }
        }
    }
}
