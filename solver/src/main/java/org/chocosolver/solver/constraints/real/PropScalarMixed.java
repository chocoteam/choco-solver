/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.RealEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.VariableUtils;

import java.util.Arrays;
import java.util.OptionalDouble;

/**
 * A propagator for SUM(x_i*c_i) = b <br/> Based on "Bounds Consistency Techniques for Long Linear
 * Constraint" </br> W. Harvey and J. Schimpf <p>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public class PropScalarMixed extends Propagator<Variable> {

    /**
     * Number of variables
     */
    protected final int l;

    /**
     * Bound to respect
     */
    protected final double b;

    /**
     * Variability of each variable (ie domain amplitude)
     */
    protected final double[] I;

    /**
     * Stores the maximal variability
     */
    protected double maxI;

    /**
     * SUm of lower bounds
     */
    protected double sumLB;

    /**
     * Sum of upper bounds
     */
    protected double sumUB;

    /**
     * The operator among EQ, LE, GE and NE
     */
    protected final Operator o;

    /**
     * The coefficients
     */
    private final double[] c;

    /**
     * Smallest precision
     */
    private final double sprc;

    /**
     * Create a scalar product: SCALAR(x_i*c_i) o b
     *
     * @param variables list of variables
     * @param coeffs    list of coefficients
     * @param o         operator
     * @param b         bound to respect.
     */
    public PropScalarMixed(Variable[] variables, double[] coeffs, Operator o, double b) {
        super(variables, PropagatorPriority.LINEAR, false);
        this.c = coeffs;
        l = variables.length;
        OptionalDouble d = Arrays.stream(vars)
                .filter(VariableUtils::isReal)
                .mapToDouble(r -> r.asRealVar().getPrecision())
                .min();
        if (d.isPresent()) {
            sprc = d.getAsDouble();
        } else {
            sprc = variables[0].getModel().getPrecision();
        }
        this.o = o;
        this.b = b;
        I = new double[l];
        maxI = 0;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        switch (o) {
            case LE:
                if (VariableUtils.isReal(vars[vIdx])) {
                    return c[vIdx] > 0 ? RealEventType.INCLOW.getMask() : RealEventType.DECUPP.getMask();
                } else {
                    return IntEventType.combine(IntEventType.INSTANTIATE, c[vIdx] > 0 ? IntEventType.INCLOW : IntEventType.DECUPP);
                }
            case GE:
                if (VariableUtils.isReal(vars[vIdx])) {
                    return c[vIdx] > 0 ? RealEventType.DECUPP.getMask() : RealEventType.INCLOW.getMask();
                } else {
                    return IntEventType.combine(IntEventType.INSTANTIATE, c[vIdx] > 0 ? IntEventType.DECUPP : IntEventType.INCLOW);
                }
            default:
                if (VariableUtils.isReal(vars[vIdx])) {
                    return RealEventType.BOUND.getMask();
                } else {
                    return IntEventType.boundAndInst();
                }
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }

    /**
     * Execute filtering wrt the operator
     *
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
            default:
                filterOnEq();
                break;
        }
    }

    protected void prepare() {
        sumLB = sumUB = 0;
        double lb, ub;
        maxI = 0;
        for (int i = 0; i < l; i++) { // first the positive coefficients
            if (VariableUtils.isReal(vars[i])) {
                if (c[i] > 0) {
                    lb = vars[i].asRealVar().getLB() * c[i];
                    ub = vars[i].asRealVar().getUB() * c[i];
                } else {
                    lb = vars[i].asRealVar().getUB() * c[i];
                    ub = vars[i].asRealVar().getLB() * c[i];
                }
            } else {
                if (c[i] > 0) {
                    lb = vars[i].asIntVar().getLB() * c[i];
                    ub = vars[i].asIntVar().getUB() * c[i];
                } else {
                    lb = vars[i].asIntVar().getUB() * c[i];
                    ub = vars[i].asIntVar().getLB() * c[i];
                }
            }
            sumLB += lb;
            sumUB += ub;
            I[i] = (ub - lb);
            if (maxI < I[i]) maxI = I[i];
        }
    }


    protected void filterOnEq() throws ContradictionException {
        boolean anychange;
        double F = b - sumLB;
        double E = sumUB - b;
        do {
            anychange = false;
            if (F < 0 || E < 0) {
                fails();
            }
            if (maxI - F > sprc || maxI - E > sprc) {
                maxI = 0;
                double lb, ub;
                for (int i = 0; i < l; i++) {
                    if (I[i] - F > 0) {
                        if (c[i] > 0) {
                            if (VariableUtils.isReal(vars[i])) {
                                lb = vars[i].asRealVar().getLB() * c[i];
                                ub = lb + I[i];
                                if (vars[i].asRealVar().updateUpperBound((F + lb) / c[i], this)) {
                                    double nub = vars[i].asRealVar().getUB() * c[i];
                                    E += nub - ub;
                                    I[i] = nub - lb;
                                    anychange = true;
                                }
                            } else {
                                lb = vars[i].asIntVar().getLB() * c[i];
                                ub = lb + I[i];
                                if (vars[i].asIntVar().updateUpperBound(divFloor(F + lb, c[i]), this)) {
                                    double nub = vars[i].asIntVar().getUB() * c[i];
                                    E += nub - ub;
                                    I[i] = nub - lb;
                                    anychange = true;
                                }
                            }
                        } else {
                            if (VariableUtils.isReal(vars[i])) {
                                lb = vars[i].asRealVar().getUB() * c[i];
                                ub = lb + I[i];
                                if (vars[i].asRealVar().updateLowerBound((-F - lb) / -c[i], this)) {
                                    double nub = vars[i].asRealVar().getLB() * c[i];
                                    E += nub - ub;
                                    I[i] = nub - lb;
                                    anychange = true;
                                }
                            } else {
                                lb = vars[i].asIntVar().getUB() * c[i];
                                ub = lb + I[i];
                                if (vars[i].asIntVar().updateLowerBound(divCeil(-F - lb, -c[i]), this)) {
                                    double nub = vars[i].asIntVar().getLB() * c[i];
                                    E += nub - ub;
                                    I[i] = nub - lb;
                                    anychange = true;
                                }
                            }
                        }
                    }
                    if (I[i] - E > 0) {
                        if (c[i] > 0) {
                            if (VariableUtils.isReal(vars[i])) {
                                ub = vars[i].asRealVar().getUB() * c[i];
                                lb = ub - I[i];
                                if (vars[i].asRealVar().updateLowerBound((ub - E) / c[i], this)) {
                                    double nlb = vars[i].asRealVar().getLB() * c[i];
                                    F -= nlb - lb;
                                    I[i] = ub - nlb;
                                    anychange = true;
                                }
                            } else {
                                ub = vars[i].asIntVar().getUB() * c[i];
                                lb = ub - I[i];
                                if (vars[i].asIntVar().updateLowerBound(divCeil(ub - E, c[i]), this)) {
                                    double nlb = vars[i].asIntVar().getLB() * c[i];
                                    F -= nlb - lb;
                                    I[i] = ub - nlb;
                                    anychange = true;
                                }
                            }
                        } else {
                            if (VariableUtils.isReal(vars[i])) {
                                ub = vars[i].asRealVar().getLB() * c[i];
                                lb = ub - I[i];
                                if (vars[i].asRealVar().updateUpperBound((-ub + E) / -c[i], this)) {
                                    double nlb = vars[i].asRealVar().getUB() * c[i];
                                    F -= nlb - lb;
                                    I[i] = ub - nlb;
                                    anychange = true;
                                }
                            } else {
                                ub = vars[i].asIntVar().getLB() * c[i];
                                lb = ub - I[i];
                                if (vars[i].asIntVar().updateUpperBound(divFloor(-ub + E, -c[i]), this)) {
                                    double nlb = vars[i].asIntVar().getUB() * c[i];
                                    F -= nlb - lb;
                                    I[i] = ub - nlb;
                                    anychange = true;
                                }
                            }

                        }
                    }
                    if (maxI < I[i]) maxI = I[i];
                }
            }
            if (F < 0 && E < 0) {
                this.setPassive();
                return;
            }
        } while (anychange);
    }

    protected void filterOnLeq() throws ContradictionException {
        double F = b - sumLB;
        double E = sumUB - b;
        if (F < 0) {
            fails();
        }
        if (maxI - F > sprc) {
            maxI = 0;
            double lb, ub;
            for (int i = 0; i < l; i++) {
                if (I[i] - F > 0) {
                    if (c[i] > 0) {
                        if (VariableUtils.isReal(vars[i])) {
                            lb = vars[i].asRealVar().getLB() * c[i];
                            ub = lb + I[i];
                            if (vars[i].asRealVar().updateUpperBound((F + lb) / c[i], this)) {
                                double nub = vars[i].asRealVar().getUB() * c[i];
                                E += nub - ub;
                                I[i] = nub - lb;
                            }
                        } else {
                            lb = vars[i].asIntVar().getLB() * c[i];
                            ub = lb + I[i];
                            if (vars[i].asIntVar().updateUpperBound(divFloor(F + lb, c[i]), this)) {
                                double nub = vars[i].asIntVar().getUB() * c[i];
                                E += nub - ub;
                                I[i] = nub - lb;
                            }
                        }
                    } else {
                        if (VariableUtils.isReal(vars[i])) {
                            lb = vars[i].asRealVar().getUB() * c[i];
                            ub = lb + I[i];
                            if (vars[i].asRealVar().updateLowerBound((-F - lb) / -c[i], this)) {
                                double nub = vars[i].asRealVar().getLB() * c[i];
                                E += nub - ub;
                                I[i] = nub - lb;
                            }
                        } else {
                            lb = vars[i].asIntVar().getUB() * c[i];
                            ub = lb + I[i];
                            if (vars[i].asIntVar().updateLowerBound(divCeil(-F - lb, -c[i]), this)) {
                                double nub = vars[i].asIntVar().getLB() * c[i];
                                E += nub - ub;
                                I[i] = nub - lb;
                            }
                        }
                    }
                }
                if (maxI < I[i]) maxI = I[i];
            }
        }
        if (E < 0) {
            this.setPassive();
        }
    }

    protected void filterOnGeq() throws ContradictionException {
        double F = b - sumLB;
        double E = sumUB - b;
        if (E < 0) {
            fails();
        }
        if (maxI - E > sprc) {
            maxI = 0;
            double lb, ub;
            for (int i = 0; i < l; i++) {
                if (I[i] - E > 0) {
                    if (c[i] > 0) {
                        if (VariableUtils.isReal(vars[i])) {
                            ub = vars[i].asRealVar().getUB() * c[i];
                            lb = ub - I[i];
                            if (vars[i].asRealVar().updateLowerBound((ub - E) / c[i], this)) {
                                double nlb = vars[i].asRealVar().getLB() * c[i];
                                F -= nlb - lb;
                                I[i] = ub - nlb;
                            }
                        } else {
                            ub = vars[i].asIntVar().getUB() * c[i];
                            lb = ub - I[i];
                            if (vars[i].asIntVar().updateLowerBound(divCeil(ub - E, c[i]), this)) {
                                double nlb = vars[i].asIntVar().getLB() * c[i];
                                F -= nlb - lb;
                                I[i] = ub - nlb;
                            }
                        }
                    } else {
                        if (VariableUtils.isReal(vars[i])) {
                            ub = vars[i].asRealVar().getLB() * c[i];
                            lb = ub - I[i];
                            if (vars[i].asRealVar().updateUpperBound((-ub + E) / -c[i], this)) {
                                double nlb = vars[i].asRealVar().getUB() * c[i];
                                F -= nlb - lb;
                                I[i] = ub - nlb;
                            }
                        } else {
                            ub = vars[i].asIntVar().getLB() * c[i];
                            lb = ub - I[i];
                            if (vars[i].asIntVar().updateUpperBound(divFloor(-ub + E, -c[i]), this)) {
                                double nlb = vars[i].asIntVar().getUB() * c[i];
                                F -= nlb - lb;
                                I[i] = ub - nlb;
                            }
                        }

                    }
                }
                if (maxI < I[i]) maxI = I[i];
            }
        }
        if (F < 0) {
            this.setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        double sumUB = 0, sumLB = 0;
        for (int i = 0; i < l; i++) { // first the positive coefficients
            if (VariableUtils.isReal(vars[i])) {
                if (c[i] > 0) {
                    sumLB += vars[i].asRealVar().getLB() * c[i];
                    sumUB += vars[i].asRealVar().getUB() * c[i];
                } else {
                    sumLB += vars[i].asRealVar().getUB() * c[i];
                    sumUB += vars[i].asRealVar().getLB() * c[i];
                }
            } else {
                if (c[i] > 0) {
                    sumLB += vars[i].asIntVar().getLB() * c[i];
                    sumUB += vars[i].asIntVar().getUB() * c[i];
                } else {
                    sumLB += vars[i].asIntVar().getUB() * c[i];
                    sumUB += vars[i].asIntVar().getLB() * c[i];
                }
            }
        }
        return check(sumLB, sumUB);
    }


    /**
     * Whether the current state of the scalar product is entailed
     *
     * @param sumLB sum of lower bounds
     * @param sumUB sum of upper bounds
     * @return the entailment check
     */
    @SuppressWarnings("Duplicates")
    protected ESat check(double sumLB, double sumUB) {
        switch (o) {
            case LE:
                if (sumLB <= b) {
                    return ESat.TRUE;
                }
                if (sumLB > b) {
                    return ESat.FALSE;
                }
                return ESat.UNDEFINED;
            case GE:
                if (sumUB >= b) {
                    return ESat.TRUE;
                }
                if (sumUB < b) {
                    return ESat.FALSE;
                }
                return ESat.UNDEFINED;
            default:
                if (sumLB <= b && b <= sumUB) {
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
        linComb.append(c[0]).append('.').append(vars[0].getName());
        int i = 1;
        for (; i < l; i++) {
            if (c[i] > 0) {
                linComb.append(" + ").append(c[i]);
            } else {
                linComb.append(" - ").append(-c[i]);
            }
            linComb.append('.').append(vars[i].getName());
        }
        linComb.append(" ").append(o).append(" ");
        linComb.append(b);
        return linComb.toString();
    }


    private int divFloor(double a, double b) {
        // <!> we assume b > 0
        if (a >= 0) {
            return (int) (a / b);
        } else {
            return (int) ((a - b + 1) / b);
        }
    }

    private int divCeil(double a, double b) {
        // <!> we assume b > 0
        if (a >= 0) {
            return (int) ((a + b - 1) / b);
        } else {
            return (int) (a / b);
        }
    }


}
