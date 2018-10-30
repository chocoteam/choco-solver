/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * X = MAX(Y,Z)
 * <br/>
 * ensures bound consistency
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class PropMaxBC extends Propagator<IntVar> {

    private IntVar BST, v1, v2;

    public PropMaxBC(IntVar X, IntVar Y, IntVar Z) {
        super(new IntVar[]{X, Y, Z}, PropagatorPriority.TERNARY, false);
        this.BST = vars[0];
        this.v1 = vars[1];
        this.v2 = vars[2];
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }

    private void filter() throws ContradictionException {
        int c = 0;
        c += (vars[0].isInstantiated() ? 1 : 0);
        c += (vars[1].isInstantiated() ? 2 : 0);
        c += (vars[2].isInstantiated() ? 4 : 0);
        switch (c) {
            case 7: // everything is instantiated
            case 6:// Z and Y are instantiated
                vars[0].instantiateTo(Math.max(vars[1].getValue(), vars[2].getValue()), this);
                break;
            case 5: //  X and Z are instantiated
            {
                int best = vars[0].getValue();
                int val2 = vars[2].getValue();
                if (best > val2) {
                    vars[1].instantiateTo(best, this);
                } else if (best < val2) {
                    fails(); // TODO: could be more precise, for explanation purpose
                } else { // X = Z
                    vars[1].updateUpperBound(best, this);
                }
            }
            break;
            case 4: // Z is instantiated
            {
                int val = vars[2].getValue();
                if (val > vars[1].getUB()) { // => X = Z
                    if (vars[0].instantiateTo(val, this)) {
                        setPassive();
                    }
                } else {
                    _filter();
                }
            }
            break;
            case 3://  X and Y are instantiated
            {
                int best = vars[0].getValue();
                int val1 = vars[1].getValue();
                if (best > val1) {
                    vars[2].instantiateTo(best, this);
                } else if (best < val1) {
                    fails(); // TODO: could be more precise, for explanation purpose
                } else { // X = Y
                    vars[2].updateUpperBound(best, this);
                }
            }
            break;
            case 2: // Y is instantiated
            {
                int val = vars[1].getValue();
                if (val > vars[2].getUB()) { // => X = Y
                    if (vars[0].instantiateTo(val, this)) {
                        setPassive();
                    }
                } else { // val in Z
                    _filter();
                }
            }
            break;
            case 1: // X is instantiated
            {
                int best = vars[0].getValue();
                if (!vars[1].contains(best) && !vars[2].contains(best)) {
                    fails(); // TODO: could be more precise, for explanation purpose
                }
                if (vars[1].getUB() < best) {
                    if (vars[2].instantiateTo(best, this)) {
                        setPassive();
                    }
                } else if (vars[2].getUB() < best) {
                    if (vars[1].instantiateTo(best, this)) {
                        setPassive();
                    }
                } else {
                    if (vars[1].updateUpperBound(best, this) | vars[2].updateUpperBound(best, this)) {
                        filter(); // to ensure idempotency for "free"
                    }
                }
            }

            break;
            case 0: // otherwise
                _filter();
                break;
            default:
                throw new SolverException("Unexpected mask " + c);
        }
    }

    private void _filter() throws ContradictionException {
        boolean change;
        do {
            change = vars[0].updateLowerBound(Math.max(vars[1].getLB(), vars[2].getLB()), this);
            change |= vars[0].updateUpperBound(Math.max(vars[1].getUB(), vars[2].getUB()), this);
            change |= vars[1].updateUpperBound(vars[0].getUB(), this);
            change |= vars[2].updateUpperBound(vars[0].getUB(), this);
            if (vars[2].getUB() < vars[0].getLB()) {
                change |= vars[1].updateLowerBound(vars[0].getLB(), this);
            }
            if (vars[1].getUB() < vars[0].getLB()) {
                change |= vars[2].updateLowerBound(vars[0].getLB(), this);
            }
        } while (change);
    }

    @Override
    public ESat isEntailed() {
        int ub = vars[0].getUB();
        if (vars[1].getLB() > ub || vars[2].getLB() > ub) {
            return ESat.FALSE;
        }
        if (Math.max(vars[1].getUB(), vars[2].getUB()) < vars[0].getLB()) {
            return ESat.FALSE;
        }
        if (vars[1].getUB() > ub || vars[2].getUB() > ub) {
            return ESat.UNDEFINED;
        }
        if (vars[0].isInstantiated()
                && (vars[1].isInstantiatedTo(ub) || vars[2].isInstantiatedTo(ub))) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return BST.toString() + ".MAX(" + v1.toString() + "," + v2.toString() + ")";
    }

}
