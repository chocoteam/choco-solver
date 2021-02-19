/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.continuous.relational;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.expression.continuous.arithmetic.CArExpression;
import org.chocosolver.solver.expression.continuous.arithmetic.RealIntervalConstant;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.RealInterval;
import org.chocosolver.util.tools.RealUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A basic constraint using HC4 algorithm for filtering values with respect to a mathematical equation.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/01/2020
 */
public class PropEquation extends Propagator<RealVar> {

    protected RealIntervalConstant cste;
    protected CArExpression exp;
    protected CArExpression[] subExps;

    protected int nbBoxedVars = 0;
    protected RealVar[] boxedVars;
    protected CArExpression[][] subExpsWX;
    protected CArExpression[][] subExpsWOX;
    protected int boxConsistencyDepth = 6;
    RealInterval[] unexplored = new RealInterval[this.boxConsistencyDepth * 2];

    public PropEquation(RealVar[] vars, CArExpression e1, CReExpression.Operator op) {
        super(vars, PropagatorPriority.LINEAR, false);
        exp = e1;
        switch (op) {
            case LT:
                cste = new RealIntervalConstant(Double.NEGATIVE_INFINITY, -RealUtils.nextFloat(0.));
                break;
            case LE:
                cste = new RealIntervalConstant(Double.NEGATIVE_INFINITY, 0.);
                break;
            case GE:
                cste = new RealIntervalConstant(0., Double.POSITIVE_INFINITY);
                break;
            case GT:
                cste = new RealIntervalConstant(RealUtils.nextFloat(0.), Double.POSITIVE_INFINITY);
                break;
            case EQ:
                cste = new RealIntervalConstant(0., 0.);
                break;
        }
        boxedVars = new RealVar[vars.length];
        subExpsWX = new CArExpression[vars.length][];
        subExpsWOX = new CArExpression[vars.length][];

        for (int i = 0; i < vars.length; i++) {
            RealVar var = vars[i];
            this.addBoxedVar(var);
        }

        exp.init();
        // Collect the subexpressions
        List<CArExpression> collectedSubExp = new ArrayList<>();
        exp.subExps(collectedSubExp);
        //noinspection ConstantForZeroLengthArrayAllocation
        subExps = collectedSubExp.toArray(new CArExpression[0]);
    }

    public void addBoxedVar(RealVar var) {
        List<CArExpression> wx = new ArrayList<>();
        List<CArExpression> wox = new ArrayList<>();
        this.exp.isolate(var, wx, wox);
        boxedVars[nbBoxedVars] = var;
        //noinspection ConstantForZeroLengthArrayAllocation
        subExpsWX[nbBoxedVars] = wx.toArray(new CArExpression[0]);
        //noinspection ConstantForZeroLengthArrayAllocation
        subExpsWOX[nbBoxedVars] = wox.toArray(new CArExpression[0]);
        nbBoxedVars++;
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // Hull consitency: HC4
        this.tighten(subExps);
        this.proj();
        // Box consistency
        for (int i = 0; i < nbBoxedVars; i++) {
            bc(boxedVars[i], subExpsWX[i], subExpsWOX[i]);
        }
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            return ESat.eval(not_inconsistent(subExps));
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return exp.toString()+" = "+cste.toString();
    }

    public void tighten(CArExpression[] exps) throws ContradictionException {
        for (CArExpression exp : exps) {
            exp.tighten();
            if (exp.getLB() > exp.getUB())
                this.fails();
        }
    }

    private boolean not_inconsistent(CArExpression[] wx) {
        boolean contradiction = false;
        try {
            tighten(wx);
        } catch (ContradictionException e) {
            contradiction = true;
        }
        if (contradiction)
            return false;
        else
            return (this.exp.getLB() <= this.cste.getUB() && this.exp.getUB() >= this.cste.getLB());
    }

    protected void bc(RealVar var, CArExpression[] wx, CArExpression[] wox) throws ContradictionException {
        int[] depths = new int[this.boxConsistencyDepth * 2];
        int depth = 0;
        int idx = 0;
        boolean fin = false;

        double leftB = 0, rightB = 0;
        double[] oldValue = {var.getLB(), var.getUB()};

        tighten(wox);

        // Left bound !
        while (!fin) {
            if (not_inconsistent(wx)) {
                if (this.boxConsistencyDepth <= depth) {
                    leftB = var.getLB();
                    rightB = var.getUB(); // Valeur provisoire
                    fin = true;
                } else {
                    RealInterval left = RealUtils.firstHalf(var);
                    RealInterval right = RealUtils.secondHalf(var);

                    var.silentlyAssign(left);
                    depth++;
                    unexplored[idx] = right;
                    depths[idx] = depth;
                    idx++;
                }
            } else if (idx != 0) {
                var.silentlyAssign(unexplored[--idx]);
                depth = depths[idx];
            } else {
                this.fails();
            }
        }

        // Reversing not explored intervals (in order to avoid to check already checked parts of the search space.

        RealInterval[] tmp1 = new RealInterval[this.boxConsistencyDepth * 2];
        int[] tmp2 = new int[this.boxConsistencyDepth * 2];

        for (int i = 0; i < idx; i++) {
            int j = idx - i - 1;
            tmp1[i] = unexplored[j];
            tmp2[i] = depths[j];
        }

        unexplored = tmp1;
        depths = tmp2;

        // Right bound if needed
        if (idx != 0) {
            var.silentlyAssign(unexplored[--idx]);
            depth = depths[idx];
            fin = false;

            while (!fin) {
                if (not_inconsistent(wx)) {
                    if (this.boxConsistencyDepth <= depth) {
                        rightB = var.getUB();
                        fin = true;
                    } else {
                        RealInterval left = RealUtils.firstHalf(var);
                        RealInterval right = RealUtils.secondHalf(var);

                        var.silentlyAssign(right);
                        depth++;
                        unexplored[idx] = left;
                        depths[idx] = depth;
                        idx++;
                    }
                } else if (idx != 0) {
                    var.silentlyAssign(unexplored[--idx]);
                    depth = depths[idx];
                } else {
                    fin = true;
                }
            }
        }

        // Propagation
        var.silentlyAssign(oldValue[0], oldValue[1]);
        var.intersect(leftB, rightB, this);

    }

    public void proj() throws ContradictionException {
        subExps[subExps.length - 1].intersect(cste, this);
        int i = subExps.length - 1;
        while (i > 0) {
            subExps[i].project(this);
            i--;
        }
    }
}
