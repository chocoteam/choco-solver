/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sum;

import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * A propagator for SUM(x_i) o b
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 * <p>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public class PropSumWithLong extends Propagator<IntVar> {

    /**
     * The position of the last positive coefficient
     */
    protected final int pos;

    /**
     * Number of variables
     */
    protected final int l;

    /**
     * Bound to respect
     */
    protected final long b;

    /**
     * Variability of each variable (ie domain amplitude)
     */
    protected final long[] I;

    /**
     * Stores the maximal variability
     */
    protected long maxI;

    /**
     * SUm of lower bounds
     */
    protected long sumLB;

    /**
     * Sum of upper bounds
     */
    protected long sumUB;

    /**
     * The operator among EQ, LE, GE and NE
     */
    protected final Operator o;


    /**
     * Creates a sum propagator: SUM(x_i) o b
     * Coefficients are induced by <code>pos</code>:
     * those before <code>pos</code> (included) are equal to 1,
     * the other ones are equal to -1.
     *
     * @param variables list of integer variables
     * @param pos position of the last positive coefficient
     * @param o operator amng EQ, LE, GE and NE
     * @param b bound to respect
     */
    public PropSumWithLong(IntVar[] variables, int pos, Operator o, long b) {
        this(variables, pos, o, b, computePriority(variables.length), false);
    }


    PropSumWithLong(IntVar[] variables, int pos, Operator o, long b, PropagatorPriority priority, boolean reactOnFineEvent){
        super(variables, priority, reactOnFineEvent);
        this.pos = pos;
        this.o = o;
        this.b = b;
        l = variables.length;
        I = new long[l];
        maxI = 0;
    }

    /**
     * Compute the priority of the propagator wrt the number of involved variables
     * @param nbvars number of variables
     * @return the priority
     */
    protected static PropagatorPriority computePriority(int nbvars) {
        if (nbvars == 1) {
            return PropagatorPriority.UNARY;
        } else if (nbvars == 2) {
            return PropagatorPriority.BINARY;
        } else if (nbvars == 3) {
            return PropagatorPriority.TERNARY;
        } else {
            return PropagatorPriority.LINEAR;
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        switch (o) {
            case NQ:
                return IntEventType.instantiation();
            case LE:
                return IntEventType.combine(IntEventType.INSTANTIATE, vIdx < pos ? IntEventType.INCLOW : IntEventType.DECUPP);
            case GE:
                return IntEventType.combine(IntEventType.INSTANTIATE, vIdx < pos ? IntEventType.DECUPP : IntEventType.INCLOW);
            default:
                return IntEventType.boundAndInst();
        }
    }


    /**
     * Prepare the propagation: compute sumLB, sumUB and I
     */
    protected void prepare() {
        sumLB = sumUB = 0;
        int i = 0;
        int lb, ub;
        maxI = 0;
        for (; i < pos; i++) { // first the positive coefficients
            lb = vars[i].getLB();
            ub = vars[i].getUB();
            sumLB += lb;
            sumUB += ub;
            I[i] = (ub - lb);
            if(maxI < I[i])maxI = I[i];
        }
        for (; i < l; i++) { // then the negative ones
            lb = -vars[i].getUB();
            ub = -vars[i].getLB();
            sumLB += lb;
            sumUB += ub;
            I[i] = (ub - lb);
            if(maxI < I[i])maxI = I[i];
        }
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }

    /**
     * Execute filtering wrt the operator
     * @throws ContradictionException if contradiction is detected
     */
    protected void filter() throws ContradictionException {
        prepare();
        switch (o) {
            case LE:
                filterOnLeq();
                break;
            case GE:
                filterOnGeq();
                break;
            case NQ:
                filterOnNeq();
                break;
            default:
                filterOnEq();
                break;
        }
    }

    /**
     * Apply filtering when operator is EQ
     * @throws ContradictionException if contradiction is detected
     */
    protected void filterOnEq() throws ContradictionException {
        boolean anychange;
        long F = b - sumLB;
        long E = sumUB - b;
        do {
            anychange = false;
            // When explanations are on, no global failure allowed
            if (model.getSolver().isLearnOff() && (F < 0 || E < 0)) {
                fails();
            }
            if (maxI > F || maxI > E) {
                long lb, ub;
                int i = 0;
                maxI = 0;
                // positive coefficients first
                while (i < pos) {
                    if (I[i] - F > 0) {
                        lb = vars[i].getLB();
                        ub = lb + I[i];
                        if (vars[i].updateUpperBound(F + lb, this)) {
                            int nub = vars[i].getUB();
                            E += nub - ub;
                            I[i] = nub - lb;
                            anychange = true;
                        }
                    }
                    if (I[i] - E > 0) {
                        ub = vars[i].getUB();
                        lb = ub - I[i];
                        if (vars[i].updateLowerBound(ub - E, this)) {
                            int nlb = vars[i].getLB();
                            F -= nlb - lb;
                            I[i] = ub - nlb;
                            anychange = true;
                        }
                    }
                    if(maxI < I[i])maxI = I[i];
                    i++;
                }
                // then negative ones
                while (i < l) {
                    if (I[i] - F > 0) {
                        lb = -vars[i].getUB();
                        ub = lb + I[i];
                        if (vars[i].updateLowerBound(-F - lb, this)) {
                            int nub = -vars[i].getLB();
                            E += nub - ub;
                            I[i] = nub - lb;
                            anychange = true;
                        }
                    }
                    if (I[i] - E > 0) {
                        ub = -vars[i].getLB();
                        lb = ub - I[i];
                        if (vars[i].updateUpperBound(-ub + E, this)) {
                            int nlb = -vars[i].getUB();
                            F -= nlb - lb;
                            I[i] = ub - nlb;
                            anychange = true;
                        }
                    }
                    if(maxI < I[i])maxI = I[i];
                    i++;
                }
            }
            // useless since true when all variables are instantiated
            if (F <= 0 && E <= 0) {
                this.setPassive();
                return;
            }
        }while (anychange) ;
    }

    /**
     * Apply filtering when operator is LE
     * @throws ContradictionException if contradiction is detected
     */
    protected void filterOnLeq() throws ContradictionException {
        long F = b - sumLB;
        long E = sumUB - b;
        // When explanations are on, no global failure allowed
        if (model.getSolver().isLearnOff() && F < 0) {
            fails();
        }
        if (maxI > F) {
            maxI = 0;
            long lb, ub;
            int i = 0;
            // positive coefficients first
            while (i < pos) {
                if (I[i] - F > 0) {
                    lb = vars[i].getLB();
                    ub = lb + I[i];
                    if (vars[i].updateUpperBound(F + lb, this)) {
                        int nub = vars[i].getUB();
                        E += nub - ub;
                        I[i] = nub - lb;
                    }
                }
                if(maxI < I[i])maxI = I[i];
                i++;
            }
            // then negative ones
            while (i < l) {
                if (I[i] - F > 0) {
                    lb = -vars[i].getUB();
                    ub = lb + I[i];
                    if (vars[i].updateLowerBound(-F - lb, this)) {
                        int nub = -vars[i].getLB();
                        E += nub - ub;
                        I[i] = nub - lb;
                    }
                }
                if(maxI < I[i])maxI = I[i];
                i++;
            }
        }
        if (E <= 0) {
            this.setPassive();
        }
    }

    /**
     * Apply filtering when operator is GE
     * @throws ContradictionException if contradiction is detected
     */
    protected void filterOnGeq() throws ContradictionException {
        long F = b - sumLB;
        long E = sumUB - b;
        // When explanations are on, no global failure allowed
        if (model.getSolver().isLearnOff() && E < 0) {
            fails();
        }
        if(maxI > E) {
            maxI = 0;
            long lb, ub;
            int i = 0;
            // positive coefficients first
            while (i < pos) {
                if (I[i] - E > 0) {
                    ub = vars[i].getUB();
                    lb = ub - I[i];
                    if (vars[i].updateLowerBound(ub - E, this)) {
                        int nlb = vars[i].getLB();
                        F -= nlb - lb;
                        I[i] = ub - nlb;
                    }
                }
                if(maxI < I[i])maxI = I[i];
                i++;
            }
            // then negative ones
            while (i < l) {
                if (I[i] - E > 0) {
                    ub = -vars[i].getLB();
                    lb = ub - I[i];
                    if (vars[i].updateUpperBound(-ub + E, this)) {
                        int nlb = -vars[i].getUB();
                        F -= nlb - lb;
                        I[i] = ub - nlb;
                    }
                }
                if(maxI < I[i])maxI = I[i];
                i++;
            }
        }
        if (F <= 0) {
            this.setPassive();
        }
    }

    /**
     * Apply filtering when operator is NE
     * @throws ContradictionException if contradiction is detected
     */
    protected void filterOnNeq() throws ContradictionException {
        long F = b - sumLB;
        long E = sumUB - b;
        if (F < 0 || E < 0) {
            setPassive();
            return;
        }
        int w = -1;
        int sum = 0;
        for (int i = 0; i < l; i++) {
            if (vars[i].isInstantiated()) {
                sum += i < pos ? vars[i].getValue() : -vars[i].getValue();
            } else if (w == -1) {
                w = i;
            } else return;
        }
        if (w == -1) {
            if (sum == b) {
                this.fails();
            }
        } else {
            vars[w].removeValue(w < pos ? b - sum : sum - b, this);
        }
    }

    @Override
    public ESat isEntailed() {
        long sumUB = 0, sumLB = 0;
        int i = 0;
        for (; i < pos; i++) { // first the positive coefficients
            sumLB += vars[i].getLB();
            sumUB += vars[i].getUB();
        }
        for (; i < l; i++) { // then the negative ones
            sumLB -= vars[i].getUB();
            sumUB -= vars[i].getLB();
        }
        return check(sumLB, sumUB);
    }

    /**
     * Whether the current state of the scalar product is entailed
     * @param sumLB sum of lower bounds
     * @param sumUB sum of upper bounds
     * @return the entailment check
     */
    public ESat check(long sumLB, long sumUB){
        switch (o) {
            case NQ:
                if (sumUB < b || sumLB > b) {
                    return ESat.TRUE;
                }
                if (sumUB == b && sumLB == b) {
                    return ESat.FALSE;
                }
                return ESat.UNDEFINED;
            case LE:
                if (sumUB <= b) {
                    return ESat.TRUE;
                }
                if (sumLB > b) {
                    return ESat.FALSE;
                }
                return ESat.UNDEFINED;
            case GE:
                if (sumLB >= b) {
                    return ESat.TRUE;
                }
                if (sumUB < b) {
                    return ESat.FALSE;
                }
                return ESat.UNDEFINED;
            default:
                if (sumLB == b && sumUB == b) {
                    return ESat.TRUE;
                }
                if (sumUB < b || sumLB > b) {
                    return ESat.FALSE;
                }
                return ESat.UNDEFINED;
        }
    }

    @Override
    public String toString() {
        StringBuilder linComb = new StringBuilder(20);
        linComb.append(pos == 0 ? "-" : "").append(vars[0].getName());
        int i = 1;
        for (; i < pos; i++) {
            linComb.append(" + ").append(vars[i].getName());
        }
        for (; i < l; i++) {
            linComb.append(" - ").append(vars[i].getName());
        }
        linComb.append(" ").append(o).append(" ");
        linComb.append(b);
        return linComb.toString();
    }

    public static long nb(Operator co){
        switch (co){
            case LE:
                return 1;
            case GE:
                return -1;
            default:
                return 0;
        }
    }

    public static Operator nop(Operator co){
        switch (co){
            case LE:
                return Operator.GE;
            case GE:
                return Operator.LE;
            default:
                return Operator.getOpposite(co);
        }
    }

    protected PropSumWithLong opposite(){
        return new PropSumWithLong(vars, pos, nop(o), b + nb(o));
    }
}
