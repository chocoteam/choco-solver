/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.ternary;

import gnu.trove.map.hash.THashMap;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.ESat;

/**
 * X/Y = Z
 * A propagator for the constraint Z = X / Y where X, Y and Z are integer, possibly negative, variables
 * The filtering algorithm both supports bounded and enumerated integer variables
 */
public class PropDivXYZ extends Propagator<IntVar> {

    // Z = X/Y
    final IntVar X, Y, Z;

    // Abs views of respectively X, Y and Z
    final IntVar absX, absY, absZ;


    public PropDivXYZ(IntVar x, IntVar y, IntVar z) {
        this(x, y, z, VariableFactory.abs(x), VariableFactory.abs(y), VariableFactory.abs(z));
    }

    private PropDivXYZ(IntVar x, IntVar y, IntVar z, IntVar ax, IntVar ay, IntVar az) {
        super(new IntVar[]{x, y, z}, PropagatorPriority.TERNARY, false);
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
     * @param evtmask: is it the initial propagation or not?
     * @throws ContradictionException
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

            hasChanged = Y.removeValue(0, aCause);
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
                        hasChanged |= Z.instantiateTo(0, aCause);
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
                    if (vy != 0) {
                        if (inInterval(Z, vz, vz)) return; // entail
                    }
                    break;
                case 4: // Z is instanciated
                    hasChanged |= updateAbsX();
                    hasChanged |= updateAbsY();
                    // sZ = 0 && sX!=0 => |x| < |y|
                    if (Z.isInstantiatedTo(0) && !X.contains(0)) {
                        hasChanged |= absX.updateUpperBound(absY.getUB() - 1, aCause);
                    }
                    break;
                case 5: // X and Z are instanciated
                    vx = X.getValue();
                    vz = Z.getValue();
                    hasChanged |= updateAbsX();
                    hasChanged |= updateAbsZ();
                    if (vz != 0 && vx == 0) {
                        this.contradiction(X, "");
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
                        contradiction(Z, "");
                    } else {
                        return;
                    }
                    break;
            }
            //------ update sign ---------
            // at this step, Y != 0 => sY != 0
            if (absX.getUB() < absY.getLB()) {
                // sX!=0 && |X|<|Y| => sZ=0
                hasChanged |= Z.instantiateTo(0, aCause);
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
     * @throws solver.exception.ContradictionException
     */
    private boolean inInterval(IntVar v, int lb, int ub) throws ContradictionException {
        if (v.getLB() >= lb && v.getUB() <= ub) {
            setPassive(); // v is already included
            return true;
        } else {
            if (v.getLB() > ub || v.getUB() < lb) {
                contradiction(v, ""); // v is excluded from [lb;ub]
            } else {
                v.updateLowerBound(lb, aCause);
                v.updateUpperBound(ub, aCause);
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
     * @throws solver.exception.ContradictionException
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
                    contradiction(v, ""); // v is included in [lb;ub]
                } else {
                    if (v.getLB() >= lb) {
                        v.updateLowerBound(ub + 1, aCause);
                    } else {
                        if (v.getUB() <= ub) {
                            v.updateUpperBound(lb - 1, aCause);
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
        return absX.updateLowerBound(absZ.getLB() * absY.getLB(), aCause) | absX.updateUpperBound((absZ.getUB() * absY.getUB()) + absY.getUB() - 1, aCause);
    }

    /**
     * Update upper and lower bounds of variable Y
     *
     * @return true iff a modification occurred
     * @throws ContradictionException
     */
    private boolean updateAbsY() throws ContradictionException {
        boolean res = absZ.getLB() != 0 && absY.updateUpperBound((int) Math.floor(absX.getUB() / absZ.getLB()), aCause);
        int zlb = absZ.getLB();
        int zub = absZ.getUB();
        int xlb = absX.getLB();
        int yub = absY.getUB();
        int num = xlb - (yub - 1);
        if (num >= 0 && zub != 0) {
            res |= absY.updateLowerBound((int) Math.ceil(num / zub), aCause);
        } else {
            res |= zlb != 0 && absY.updateLowerBound(-(int) Math.floor((-xlb + (yub - 1)) / zlb), aCause);
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
        boolean res = absY.getLB() != 0 && absZ.updateUpperBound((int) Math.floor(absX.getUB() / absY.getLB()), aCause);
        int xlb = absX.getLB();
        int ylb = absY.getLB();
        int yub = absY.getUB();
        int num = xlb - (yub - 1);
        if (num >= 0 && yub != 0) {
            res |= absZ.updateLowerBound((int) Math.ceil(num / yub), aCause);
        } else {
            res |= ylb != 0 && absZ.updateLowerBound(-(int) Math.floor((-xlb + (yub - 1)) / ylb), aCause);
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
            res = a.updateLowerBound(0, aCause);
        }
        if (b.getUB() <= 0) {
            res |= a.updateUpperBound(0, aCause);
        }
        if (!b.contains(0)) {
            res |= a.removeValue(0, aCause);
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
            res = a.updateUpperBound(0, aCause);
        }
        if (b.getUB() <= 0) {
            res |= a.updateLowerBound(0, aCause);
        }
        if (b.contains(0)) {
            res |= a.removeValue(0, aCause);
        }
        return res;
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            this.vars[0].duplicate(solver, identitymap);
            IntVar X = (IntVar) identitymap.get(this.vars[0]);
            this.vars[1].duplicate(solver, identitymap);
            IntVar Y = (IntVar) identitymap.get(this.vars[1]);
            this.vars[2].duplicate(solver, identitymap);
            IntVar Z = (IntVar) identitymap.get(this.vars[2]);

            this.absX.duplicate(solver, identitymap);
            IntVar absX = (IntVar) identitymap.get(this.absX);
            this.absY.duplicate(solver, identitymap);
            IntVar absY = (IntVar) identitymap.get(this.absY);
            this.absZ.duplicate(solver, identitymap);
            IntVar absZ = (IntVar) identitymap.get(this.absZ);


            identitymap.put(this, new PropDivXYZ(X, Y, Z, absX, absY, absZ));
        }
    }
}
