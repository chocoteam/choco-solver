/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.PoolManager;

/**
 * A decision based on a {@link RealVar} that splits the domain in two ranges.
 * The lower range is evaluated first, then the upper range.

 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class RealDecision extends Decision<RealVar> {

    private static final long serialVersionUID = -4723411613242027280L;
    /**
     * The decision value
     */
    private double value;
    /**
     * The step value
     */
    private double epsilon;
    /**
     * Select left range first
     */
    private boolean leftFirst;
    /**
     * Decision pool manager, to recycle decisions
     */
    transient private final PoolManager<RealDecision> poolManager;

    /**
     * Create an decision based on an {@link RealVar}
     * @param poolManager decision pool manager, to recycle decisions
     */
    public RealDecision(PoolManager<RealDecision> poolManager) {
        super(2);
        this.poolManager = poolManager;
    }

    @Override
    public Double getDecisionValue() {
        return value;
    }

    @Override
    public void apply() throws ContradictionException {
        if (branch == 1) {
            boolean modif;
            if(leftFirst){
                modif = var.updateUpperBound(value, this);
            }else{
                modif = var.updateLowerBound(value, this);
            }
            assert modif : "applying decision " + this + " does not modify the variable's domain.";
        } else if (branch == 2) {
            if(leftFirst) {
                var.updateLowerBound(value + epsilon, this);
            }else{
                var.updateUpperBound(value - epsilon, this);
            }
        }
    }

    /**
     * Instantiate this decision with the parameters
     * @param v a variable
     * @param value a value
     * @param epsilon gap between value as upper bound and as lower bound (to simulate strictness)
     * @param leftFirst select left range first
     */
    public void set(RealVar v, double value, double epsilon, boolean leftFirst) {
        assert epsilon >= Double.MIN_VALUE : "epsilon should be greater or equal to Double.MIN_VALUE";
        super.set(v);
        this.value = value;
        this.epsilon = epsilon;
        this.leftFirst = leftFirst;
    }

    @Override
    public void free() {
        poolManager.returnE(this);
    }

    @Override
    public String toString() {
        return String.format("%s%s %s %s", (branch < 2 ? "" : "!"), var.getName(), "<=", value);
    }
}
