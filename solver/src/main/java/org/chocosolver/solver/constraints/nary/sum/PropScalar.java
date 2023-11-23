/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sum;

import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * A propagator for SUM(x_i*c_i) = b
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 * <p>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
@Explained
public class PropScalar extends PropSum {

    /**
     * The coefficients
     */
    private final int[] c;

    /**
     * Create a scalar product: SUM(x_i*c_i) o b
     * Variables and coefficients are excepted to be ordered wrt to coefficients: first positive ones then negative ones.
     * @param variables list of integer variables
     * @param coeffs list of coefficients
     * @param pos position of the last positive coefficient
     * @param o operator
     * @param b bound to respect.
     */
    public PropScalar(IntVar[] variables, int[] coeffs, int pos, Operator o, int b) {
        super(variables, pos, o, b);
        this.c = coeffs;
    }


    @Override
    protected void prepare() {
        sumLB = sumUB = 0;
        int i = 0, lb, ub;
        maxI = 0;
        for (; i < pos; i++) { // first the positive coefficients
            lb = vars[i].getLB() * c[i];
            ub = vars[i].getUB() * c[i];
            sumLB += lb;
            sumUB += ub;
            I[i] = (ub - lb);
            if(maxI < I[i])maxI = I[i];
        }
        for (; i < l; i++) { // then the negative ones
            lb = vars[i].getUB() * c[i];
            ub = vars[i].getLB() * c[i];
            sumLB += lb;
            sumUB += ub;
            I[i] = (ub - lb);
            if(maxI < I[i])maxI = I[i];
        }
    }


    @Override
    protected void filterOnEq() throws ContradictionException {
        boolean anychange;
        int F = b - sumLB;
        int E = sumUB - b;
        do {
            anychange = false;
            // When explanations are on, no global failure allowed
            if (!model.getSolver().isLCG() && (F < 0 || E < 0)) {
                fails();
            }
            if (maxI > F || maxI > E) {
                maxI = 0;
                int lb, ub, i = 0;
                // positive coefficients first
                while (i < pos) {
                    if (I[i] - F > 0) {
                        lb = vars[i].getLB() * c[i];
                        ub = lb + I[i];
                        if (vars[i].updateUpperBound(divFloor(F + lb, c[i]), this, explainMax(i))) {
                            int nub = vars[i].getUB() * c[i];
                            E += nub - ub;
                            I[i] = nub - lb;
                            anychange = true;
                        }
                    }
                    if (I[i] - E > 0) {
                        ub = vars[i].getUB() * c[i];
                        lb = ub - I[i];
                        if (vars[i].updateLowerBound(divCeil(ub - E, c[i]), this, explainMin(i))) {
                            int nlb = vars[i].getLB() * c[i];
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
                        lb = vars[i].getUB() * c[i];
                        ub = lb + I[i];
                        if (vars[i].updateLowerBound(divCeil(-F - lb, -c[i]), this, explainMax(i))) {
                            int nub = vars[i].getLB() * c[i];
                            E += nub - ub;
                            I[i] = nub - lb;
                            anychange = true;
                        }
                    }
                    if (I[i] - E > 0) {
                        ub = vars[i].getLB() * c[i];
                        lb = ub - I[i];
                        if (vars[i].updateUpperBound(divFloor(-ub + E, -c[i]), this, explainMin(i))) {
                            int nlb = vars[i].getUB() * c[i];
                            F -= nlb - lb;
                            I[i] = ub - nlb;
                            anychange = true;
                        }
                    }
                    if(maxI < I[i])maxI = I[i];
                    i++;
                }
            }
            if (F <= 0 && E <= 0) {
                this.setPassive();
                return;
            }
        } while (anychange);
    }

    @Override
    protected void filterOnLeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        // When explanations are on, no global failure allowed
        if (!model.getSolver().isLCG() &&F < 0) {
            fails();
        }
        if (maxI > F) {
            int lb, ub, i = 0;
            maxI = 0;
            // positive coefficients first
            while (i < pos) {
                maxI = 0;
                if (I[i] - F > 0) {
                    lb = vars[i].getLB() * c[i];
                    ub = lb + I[i];
                    if (vars[i].updateUpperBound(divFloor(F + lb, c[i]), this, explainMax(i))) {
                        int nub = vars[i].getUB() * c[i];
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
                    lb = vars[i].getUB() * c[i];
                    ub = lb + I[i];
                    if (vars[i].updateLowerBound(divCeil(-F - lb, -c[i]), this, explainMax(i))) {
                        int nub = vars[i].getLB() * c[i];
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

    @Override
    protected void filterOnGeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        // When explanations are on, no global failure allowed
        if (!model.getSolver().isLCG() && E < 0) {
            fails();
        }
        if (maxI > E) {
            maxI = 0;
            int lb, ub, i = 0;
            // positive coefficients first
            while (i < pos) {
                if (I[i] - E > 0) {
                    ub = vars[i].getUB() * c[i];
                    lb = ub - I[i];
                    if (vars[i].updateLowerBound(divCeil(ub - E, c[i]), this, explainMin(i))) {
                        int nlb = vars[i].getLB() * c[i];
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
                    ub = vars[i].getLB() * c[i];
                    lb = ub - I[i];
                    if (vars[i].updateUpperBound(divFloor(-ub + E, -c[i]), this, explainMin(i))) {
                        int nlb = vars[i].getUB() * c[i];
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

    @Override
    protected void filterOnNeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        if (F < 0 || E < 0) {
            setPassive();
            return;
        }
        int w = -1;
        int sum = 0;
        for (int i = 0; i < l; i++) {
            if (vars[i].isInstantiated()) {
                sum += vars[i].getValue() * c[i];
            } else if (w == -1) {
                w = i;
            } else return;
        }
        if (w == -1) {
            if (sum == b) {
                // default reason is ok
                this.fails();
            }
        } else if(c[w]!=0 && (b - sum)%c[w]==0){
            vars[w].removeValue((b - sum)/c[w], this, this.defaultReason(vars[w]));
        }
    }

    @Override
    public ESat isEntailed() {
        int sumUB = 0, sumLB = 0, i = 0;
        for (; i < pos; i++) { // first the positive coefficients
            sumLB += vars[i].getLB() * c[i];
            sumUB += vars[i].getUB() * c[i];
        }
        for (; i < l; i++) { // then the negative ones
            sumLB += vars[i].getUB() * c[i];
            sumUB += vars[i].getLB() * c[i];
        }
        return check(sumLB, sumUB);
    }

    @Override
    public String toString() {
        StringBuilder linComb = new StringBuilder(20);
        linComb.append(c[0]).append('.').append(vars[0].getName());
        int i = 1;
        for (; i < pos; i++) {
            linComb.append(" + ").append(c[i]).append('.').append(vars[i].getName());
        }
        for (; i < l; i++) {
            linComb.append(" - ").append(-c[i]).append('.').append(vars[i].getName());
        }
        linComb.append(" ").append(o).append(" ");
        linComb.append(b);
        return linComb.toString();
    }


    private int divFloor(int a, int b) {
        // <!> we assume b > 0
        if (a >= 0) {
            return (a / b);
        } else {
            return (a - b + 1) / b;
        }
    }

    private int divCeil(int a, int b) {
        // <!> we assume b > 0
        if (a >= 0) {
            return ((a + b - 1) / b);
        } else {
            return a / b;
        }
    }

    @Override
    protected PropSum opposite(){
        return new PropScalar(vars, c, pos, nop(o), b + nb(o));
    }

}
