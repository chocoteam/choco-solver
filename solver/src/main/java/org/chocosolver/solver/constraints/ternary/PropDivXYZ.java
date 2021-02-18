/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.MathUtils;

/**
 * X/Y = Z
 * A propagator for the constraint Z = X / Y where X, Y and Z are integer, possibly negative, variables
 * The filtering algorithm both supports bounded and enumerated integer variables
 */
public class PropDivXYZ extends Propagator<IntVar> {

    // Z = X/Y
    private final IntVar X, Y, Z;

    // Abs views of respectively X, Y and Z
    private final IntVar absX, absY, absZ;


    public PropDivXYZ(IntVar x, IntVar y, IntVar z) {
        this(x, y, z, x.getModel().intAbsView(x), x.getModel().intAbsView(y), x.getModel().intAbsView(z));
    }

    private PropDivXYZ(IntVar x, IntVar y, IntVar z, IntVar ax, IntVar ay, IntVar az) {
        super(new IntVar[]{x, y, z, ax, ay, az}, PropagatorPriority.TERNARY, false);
        this.X = x;
        this.Y = y;
        this.Z = z;
        this.absX = ax;
        this.absY = ay;
        this.absZ = az;
    }

    /**
     * The main propagation method that filters according to the constraint defintion
     *
     * @param evtmask   is it the initial propagation or not?
     * @throws ContradictionException if failure occurs
     */
    @Override
    public void propagate(int evtmask) throws ContradictionException {
        boolean hasChanged;
        int mask;
        do {
            mask = 0;
            mask += X.isInstantiated() ? 1 : 0;
            mask += Y.isInstantiated() ? 2 : 0;
            mask += Z.isInstantiated() ? 4 : 0;

            hasChanged = Y.removeValue(0, this);
            if (outInterval(Y, 0, 0)) return;

            int vx, vy, vz;
            switch (mask) {
                case 0: // nothing instanciated
                    hasChanged |= updateAbsX();
                    hasChanged |= updateAbsY();
                    hasChanged |= updateAbsZ();
                    break;
                case 1: // X is instanciated
                    hasChanged |= updateAbsY();
                    hasChanged |= updateAbsZ();
                    if (X.isInstantiatedTo(0)) {
                        // sY!=0 && sX=0 => sZ=0
                        hasChanged |= Z.instantiateTo(0, this);
                    }
                    break;
                case 2: // Y is instanciated
                    hasChanged |= updateAbsX();
                    hasChanged |= updateAbsZ();
                    break;
                case 3: // X and Y are instanciated
                    vx = X.getValue();
                    vy = Y.getValue();
                    hasChanged |= updateAbsX();
                    hasChanged |= updateAbsY();
                    vz = vx / vy;//(int) Math.floor((double) (vx + ((vx * vy < 0 ? 1 : 0) * (vy - 1))) / (double) vy);
                    if (inInterval(Z, vz, vz)) return; // entail
                    break;
                case 4: // Z is instanciated
                    hasChanged |= updateAbsX();
                    hasChanged |= updateAbsY();
                    // sZ = 0 && sX!=0 => |x| < |y|
                    if (Z.isInstantiatedTo(0) && !X.contains(0)) {
                        hasChanged |= absX.updateUpperBound(absY.getUB() - 1, this);
                    }
                    break;
                case 5: // X and Z are instanciated
                    vx = X.getValue();
                    vz = Z.getValue();
                    hasChanged |= updateAbsX();
                    hasChanged |= updateAbsZ();
                    if (vz != 0 && vx == 0) {
                        fails(); // TODO: could be more precise, for explanation purpose
                    }
                    hasChanged |= updateAbsY();
                    break;
                case 6: // Y and Z are instanciated
                    vy = Y.getValue();
                    vz = Z.getValue();
                    hasChanged |= updateAbsY();
                    hasChanged |= updateAbsZ();
                    if (vz == 0) {
                        if (inInterval(X, -Math.abs(vy) + 1, Math.abs(vy) - 1)) return;
                    } else { // Y*Z > 0  ou < 0
                        hasChanged |= updateAbsX();
                    }
                    break;
                case 7: // X, Y and Z are instanciated
                    vx = X.getValue();
                    vy = Y.getValue();
                    vz = Z.getValue();
                    int val = vx / vy;
                    if ((vz != val)) {
                        fails(); // TODO: could be more precise, for explanation purpose
                    } else {
                        return;
                    }
                    break;
                default: throw new SolverException("Unexpected mask "+mask);
            }
            //------ update sign ---------
            // at this step, Y != 0 => sY != 0
            if (absX.getUB() < absY.getLB()) {
                // sX!=0 && |X|<|Y| => sZ=0
                hasChanged |= Z.instantiateTo(0, this);
            } else if (X.getLB() > 0 && absX.getLB() >= absY.getUB()) {
                // sX=1 && |X|>=|Y| => sZ=sY
                hasChanged |= sameSign(Z, Y);
                hasChanged |= sameSign(Y, Z);
            } else if (X.getUB() < 0 && absX.getLB() >= absY.getUB()) {
                // sX=-1 && |X|>=|Y| => sZ=-sY
                hasChanged |= oppSign(Z, Y);
                hasChanged |= oppSign(Y, Z);
            } //*/
        } while (hasChanged);
    }


    @Override
    public ESat isEntailed() {
        // forbid Y=0
        if (Y.isInstantiatedTo(0)) {
            return ESat.FALSE;
        }
        // X=0 => Z=0
        if (X.isInstantiatedTo(0) && !Z.contains(0)) {
            return ESat.FALSE;
        }
        // check sign
        boolean pos = (X.getLB() >= 0 && Y.getLB() >= 0) || (X.getUB() < 0 && Y.getUB() < 0);
        if (pos && Z.getUB() < 0) {
            return ESat.FALSE;
        }
        boolean neg = (X.getLB() >= 0 && Y.getUB() < 0) || (X.getUB() < 0 && Y.getLB() >= 0);
        if (neg && Z.getLB() > 0) {
            return ESat.FALSE;
        }
        // compute absolute bounds
        int minAbsX;
        if (X.getLB() > 0) {
            minAbsX = X.getLB();
        } else if (X.getUB() < 0) {
            minAbsX = -X.getUB();
        } else {
            minAbsX = 0;
        }
        int maxAbsX = Math.max(X.getUB(), -X.getLB());
        int minAbsY;
        if (Y.getLB() > 0) {
            minAbsY = Y.getLB();
        } else if (Y.getUB() < 0) {
            minAbsY = -Y.getUB();
        } else {
            minAbsY = 1;
        }
        int maxAbsY = Math.max(Y.getUB(), -Y.getLB());
        int minAbsZ;
        if (Z.getLB() > 0) {
            minAbsZ = Z.getLB();
        } else if (Z.getUB() < 0) {
            minAbsZ = -Z.getUB();
        } else {
            minAbsZ = 0;
        }
        int maxAbsZ = Math.max(Z.getUB(), -Z.getLB());
        // check absolute bounds
        if ((minAbsZ > maxAbsX / minAbsY) || (maxAbsZ < minAbsX / maxAbsY)) {
            return ESat.FALSE;
        }
        // check case Z=0
        if ((Z.isInstantiatedTo(0) && minAbsX > maxAbsY) || (maxAbsX < minAbsY && !Z.contains(0))) {
            return ESat.FALSE;
        }
        // check tuple
        if (isCompletelyInstantiated()) {
            return ESat.eval(X.getValue() / Y.getValue() == Z.getValue());
        }
        return ESat.UNDEFINED;
    }


    /**
     * ensure v is already included in [lb;ub]
     *
     * @param v  variable to check
     * @param lb new lower bound
     * @param ub new upper bound
     * @throws org.chocosolver.solver.exception.ContradictionException
     */
    private boolean inInterval(IntVar v, int lb, int ub) throws ContradictionException {
        if (v.getLB() >= lb && v.getUB() <= ub) {
            setPassive(); // v is already included
            return true;
        } else {
            if (v.getLB() > ub || v.getUB() < lb) {
                fails(); // TODO: could be more precise, for explanation purpose
            } else {
                v.updateBounds(lb, ub, this);
                setPassive();
                return true;
            }
        }
        return false;
    }

    /**
     * ensure variable v does not take values in [lb;ub]
     *
     * @param v  variable to check
     * @param lb new lower bound
     * @param ub new upper bound
     * @return true iff a value has been removed from v
     * @throws org.chocosolver.solver.exception.ContradictionException
     */
    private boolean outInterval(IntVar v, int lb, int ub) throws ContradictionException {
        if (lb > ub) {
            setPassive();
            return true;
        } else if (lb < ub) {
            if (v.getLB() > ub || v.getUB() < lb) {
                setPassive(); // v is already disjoint from [lb;ub]
                return true;
            } else {
                if (v.getLB() >= lb && v.getUB() <= ub) {
                    fails(); // TODO: could be more precise, for explanation purpose
                } else {
                    if (v.getLB() >= lb) {
                        v.updateLowerBound(ub + 1, this);
                    } else {
                        if (v.getUB() <= ub) {
                            v.updateUpperBound(lb - 1, this);
                        }
                    }
                    setPassive();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Update upper and lower bounds of variable X
     *
     * @return true iff a modification occurred
     * @throws ContradictionException
     */
    private boolean updateAbsX() throws ContradictionException {
        return absX.updateLowerBound(MathUtils.safeMultiply(absZ.getLB(), absY.getLB()), this)
                | absX.updateUpperBound(MathUtils.safeAdd(MathUtils.safeMultiply(absZ.getUB(), absY.getUB()), absY.getUB() - 1), this);
    }

    /**
     * Update upper and lower bounds of variable Y
     *
     * @return true iff a modification occurred
     * @throws ContradictionException
     */
    private boolean updateAbsY() throws ContradictionException {
        boolean res = absZ.getLB() != 0 && absY.updateUpperBound((int) Math.floor(absX.getUB() / absZ.getLB()), this);
        int zlb = absZ.getLB();
        int zub = absZ.getUB();
        int xlb = absX.getLB();
        int yub = absY.getUB();
        int num = xlb - (yub - 1);
        if (num >= 0 && zub != 0) {
            res |= absY.updateLowerBound((int) Math.ceil(num / zub), this);
        } else {
            res |= zlb != 0 && absY.updateLowerBound(-(int) Math.floor((-xlb + (yub - 1)) / zlb), this);
        }
        return res;
    }

    /**
     * Update upper and lower bounds of variable Y
     *
     * @return true iff a modification occurred
     * @throws ContradictionException
     */
    private boolean updateAbsZ() throws ContradictionException {
        boolean res = absY.getLB() != 0 && absZ.updateUpperBound((int) Math.floor(absX.getUB() / absY.getLB()), this);
        int xlb = absX.getLB();
        int ylb = absY.getLB();
        int yub = absY.getUB();
        int num = xlb - (yub - 1);
        if (num >= 0 && yub != 0) {
            res |= absZ.updateLowerBound((int) Math.ceil(num / yub), this);
        } else {
            res |= ylb != 0 && absZ.updateLowerBound(-(int) Math.floor((-xlb + (yub - 1)) / ylb), this);
        }
        return res;
    }

    /**
     * A take the signs of B
     *
     * @param a first var
     * @param b second var
     */
    protected boolean sameSign(IntVar a, IntVar b) throws ContradictionException {
        boolean res = false;
        if (b.getLB() >= 0) {
            res = a.updateLowerBound(0, this);
        }
        if (b.getUB() <= 0) {
            res |= a.updateUpperBound(0, this);
        }
        if (!b.contains(0)) {
            res |= a.removeValue(0, this);
        }
        return res;
    }

    /**
     * A take the opposite signs of B
     *
     * @param a first var
     * @param b second var
     */
    protected boolean oppSign(IntVar a, IntVar b) throws ContradictionException {
        boolean res = false;
        if (b.getLB() >= 0) {
            res = a.updateUpperBound(0, this);
        }
        if (b.getUB() <= 0) {
            res |= a.updateLowerBound(0, this);
        }
        if (b.contains(0)) {
            res |= a.removeValue(0, this);
        }
        return res;
    }

}
