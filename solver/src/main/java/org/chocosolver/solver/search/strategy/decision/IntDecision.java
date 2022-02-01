/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.PoolManager;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils;

import java.util.function.Consumer;

/**
 * A decision based on a {@link IntVar}
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class IntDecision extends Decision<IntVar> {

    private static final long serialVersionUID = 4319290465131546449L;
 
    /**
     * The decision value
     */
    private int value;
    /**
     * The assignment operator
     */
    private DecisionOperator<IntVar> assignment;
    /**
     * Decision pool manager, to recycle decisions
     */
    transient private final PoolManager<IntDecision> poolManager;

    /**
     * Create an decision based on an {@link IntVar}
     * @param poolManager decision pool manager, to recycle decisions
     */
    public IntDecision(PoolManager<IntDecision> poolManager) {
        super(2);
        this.poolManager = poolManager;
    }

    @Override
    public Integer getDecisionValue() {
        return value;
    }

    @Override
    public void apply() throws ContradictionException {
        if (branch == 1) {
            var.getModel().getSolver().getEventObserver().pushDecisionLevel();
            assignment.apply(var, value, this);
        } else if (branch == 2) {
            assignment.unapply(var, value, this);
        }
        // TODO #538 assert modif: "(un-)applying decision "+ this + " does not modify the variable's domain.";
    }

    /**
     * Instantiate this decision with the parameters
     * @param v a variable
     * @param value a value
     * @param assignment a decision operator
     */
    public void set(IntVar v, int value, DecisionOperator<IntVar> assignment) {
        super.set(v);
        this.value = value;
        this.assignment = assignment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void reverse() {
        this.assignment = assignment.opposite();
    }

    @Override
    public void free() {
        poolManager.returnE(this);
    }

    @Override
    public IntDecision duplicate() {
        IntDecision d = poolManager.getE();
        if (d == null) {
            d = new IntDecision(poolManager);
        }
        d.set(var, value, assignment);
        return d;
    }

    @Override
    public boolean isEquivalentTo(Decision dec) {
        if (dec instanceof IntDecision) {
            IntDecision id = (IntDecision) dec;
            return (id.var == this.var
                    && id.assignment == this.assignment
                    && id.value == this.value
                    && id.max_branching == this.max_branching
                    && id.branch == this.branch);
        } else {
            return false;
        }
    }

    /**
     * @return the current decision operator
     */
    public DecisionOperator<IntVar> getDecOp() {
        return assignment;
    }

    /**
     * @return a copy of this decision wherein the he decision operator is reversed
     */
    @SuppressWarnings("unchecked")
    public IntDecision flip(){
        IntDecision d = poolManager.getE();
        if (d == null) {
            d = new IntDecision(poolManager);
        }
        int val = value;
        if(assignment == DecisionOperatorFactory.makeIntSplit()){
            val++;
        }
        else if(assignment == DecisionOperatorFactory.makeIntReverseSplit()){
            val--;
        }
        d.set(var, val, assignment.opposite());
        return d;
    }

    @Override
    public String toString() {
        boolean nonrefuted = ((branch < max_branching) || (max_branching == 1 && branch == max_branching));
        if (assignment.getClass().equals(DecisionOperatorFactory.makeIntEq().getClass())) {
            return String.format("d_%d: %s%s%d",
                    getPosition(),
                    var.getName(),
                    nonrefuted ? "=": '\\',
                    value);
        } else if (assignment.getClass().equals(DecisionOperatorFactory.makeIntNeq().getClass())) {
            return String.format("d_%d: %s%s%d",
                    getPosition(),
                    var.getName(),
                    nonrefuted ? "\u2260" : '=',
                    value);
        } else if (assignment.getClass().equals(DecisionOperatorFactory.makeIntSplit().getClass())) {
            return String.format("d_%d: %s%s%s%d,%d]",
                    getPosition(),
                    var.getName(),
                    "\u2208",
                    nonrefuted ? '[' : ']',
                    nonrefuted ? var.getLB() : value,
                    nonrefuted ? value : var.getUB());
        } else if (assignment.getClass().equals(DecisionOperatorFactory.makeIntReverseSplit().getClass())) {
            return String.format("d_%d: %s%s[%d,%d%s",
                    getPosition(),
                    var.getName(),
                    "\u2208",
                    nonrefuted ? value : var.getLB(),
                    nonrefuted ? var.getUB() : value,
                    nonrefuted ? ']' : '[');
        } else {
            return String.format("d_%d: %s%s{%s}",
                    getPosition(),
                    var.getName(),
                    nonrefuted ? assignment.toString() : assignment.opposite().toString(),
                    value);
        }
    }

    /**
     * @implSpec
     * <p>Since a decision only relies on a unique variable, designed by 'p' and this decision,
     * this can be treated as unary constraint, depending on this {@link #assignment}
     * and {@link #branch}.
     * </p>
     * <p>
     *  Let's consider an assignment decisions, (X = a) and that Dx is the domain of x just
     *  before applying this decision.
     *  Then, we can formalize the application of this like:
     *
     *     <pre>
     *         (v1 &isin; D1) &rarr; v1 &isin; a
     *     </pre>
     *      Converting to DNF:
     *     <pre>
     *         (v1 &isin; (U \ D1) &cup; a)
     *     </pre>
     *
     * </p>
     */
    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        IntIterableRangeSet dom = explanation.complement(var);
        IntIterableSetUtils.unionOf(dom, explanation.readDom(p));
        var.intersectLit(dom, explanation);
    }

    @Override
    public void forEachIntVar(Consumer<IntVar> action) {
        // nothing to do
    }

}
