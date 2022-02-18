/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.channeling;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * A propagator which links an IntVar with two arrays of BoolVar, one for EQ relations, the other for LQ relations.
 * Such a propagator is needed when clauses learning is on.
 * <p>
 * Created by cprudhom on 14/01/15.
 * Project: choco.
 */
public class PropClauseChanneling extends Propagator<IntVar> {

    private final IntVar iv;
    private final boolean bounded;
    private final IIntDeltaMonitor dm;
    private final BoolVar[] eqs; // EQ bool vars
    private final BoolVar[] lqs; // LQ bool vars
    private final IStateInt LB;
    private final IStateInt UB; // keep trace of lb and ub of iv to ease propagation
    private final int OFFSET;
    private final int LENGTH;

    public PropClauseChanneling(IntVar iv, BoolVar[] eb, BoolVar[] lb) {
        super(ArrayUtils.append(new IntVar[]{iv}, eb, lb), PropagatorPriority.LINEAR, true);
        this.iv = iv;
        this.bounded = !iv.hasEnumeratedDomain();
        this.eqs = eb;
        this.lqs = lb;
        this.OFFSET = iv.getLB();
        this.LENGTH = iv.getUB() - iv.getLB() + 1;
        this.LB = model.getEnvironment().makeInt();
        this.UB = model.getEnvironment().makeInt(LENGTH);
        this.dm = iv.hasEnumeratedDomain() ? iv.monitorDelta(this) : IIntDeltaMonitor.Default.NONE;
        if (eb.length != LENGTH || lb.length != LENGTH) {
            throw new SolverException("BoolVar[] wrong dimension");
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (iv.isInstantiated()) {
            int value = iv.getValue() - OFFSET;
            eqs[value].instantiateTo(1, this);
            lqs[value].instantiateTo(1, this);
        }

        // values below current iv lb
        int lb = iv.getLB() - OFFSET;
        int ub = iv.getUB() - OFFSET;
        for (int i = 0; i < lb; i++) {
            eqs[i].instantiateTo(0, this);
            lqs[i].instantiateTo(0, this);
        }
        // values above current iv ub
        for (int i = ub + 1; i < LENGTH; i++) {
            eqs[i].instantiateTo(0, this);
            lqs[i].instantiateTo(1, this);
        }
        // first, update only eqs and iv
        while (lb < LENGTH && eqs[lb].isInstantiated()) {
            if (eqs[lb].isInstantiatedTo(0)) {
                iv.removeValue(lb + OFFSET, this);
            } else {
                iv.instantiateTo(lb + OFFSET, this);
            }
            lb++;
        }
        while (ub > -1 && eqs[ub].isInstantiated()) {
            if (eqs[ub].isInstantiatedTo(0)) {
                iv.removeValue(ub + OFFSET, this);
            } else {
                iv.instantiateTo(ub + OFFSET, this);
            }
            ub--;
        }
        if (!bounded) {
            for (int i = lb + 1; i < ub; i++) {
                if (!iv.contains(i + OFFSET)) {
                    eqs[i].instantiateTo(0, this);
                } else if (eqs[i].isInstantiated()) {
                    if (eqs[i].isInstantiatedTo(0)) {
                        iv.removeValue(i + OFFSET, this);
                    } else {
                        iv.instantiateTo(i + OFFSET, this);
                    }
                }
            }
        }

        // now iv and eqs are synchronized
        // then, second pass to update lqs
        while (lb < LENGTH && !iv.contains(lb + OFFSET)) {
            lqs[lb++].instantiateTo(0, this);
        }
        while (ub > -1 && !iv.contains(ub + OFFSET)) {
            lqs[ub--].instantiateTo(1, this);
        }
        if (ub > -1) lqs[ub].instantiateTo(1, this); // current UB needs to be instantiated to true

        LB.set(lb);
        UB.set(ub);

        // finally delta monitor
        dm.startMonitoring();
    }

    @Override
    public void propagate(int vidx, int mask) throws ContradictionException {
        if (vidx == 0) { //iv has been modified
            if (IntEventType.isInstantiate(mask)) {
                _inst(iv.getValue() - OFFSET);
            } else {
                int lb = LB.get();
                int ub = UB.get();
                if (IntEventType.isInclow(mask)) {
                    _ulb(iv.getLB() - OFFSET, lb);
                }
                if (IntEventType.isDecupp(mask)) {
                    _uub(iv.getUB() - OFFSET, ub);
                }
                // then deal with removed values
                dm.forEachRemVal((IntProcedure) value -> {
                    value -= OFFSET;
                    if (value > lb && value < ub) {
                        eqs[value].instantiateTo(0, this);
                    }
                });
            }
        } else {
            vidx--; // idx in eqs or lqs
            int act = 0;
            if (vidx < LENGTH) { // then EQ bool var
                if (eqs[vidx].getValue() != 1) {
                    if (vidx == LB.get()) {
                        act = 1;
                    } else if (vidx == UB.get()) {
                        act = 2;
                        vidx--;
                    } else {
                        act = 3;
                    }
                }
            } else { // then LQ bool var
                vidx -= LENGTH;
                if (lqs[vidx].getValue() == 1) {
                    act = 2;
                } else {
                    act = 1;
                }
            }
            switch (act) {
                case 0: // instantiation
                    iv.instantiateTo(vidx + OFFSET, this);
                    _inst(vidx);
                    break;
                case 1: // update lower bound
                    iv.updateLowerBound(vidx + OFFSET + 1, this);
                    if (iv.isInstantiated()) {
                        _inst(iv.getValue() - OFFSET);
                    } else {
                        _ulb(iv.getLB() - OFFSET, LB.get());
                    }
                    break;
                case 2: // update upper bound
                    iv.updateUpperBound(vidx + OFFSET, this);
                    if (iv.isInstantiated()) {
                        _inst(iv.getValue() - OFFSET);
                    } else {
                        _uub(iv.getUB() - OFFSET, UB.get());
                    }
                    break;
                case 3: // value removal
                    iv.removeValue(vidx + OFFSET, this);
                    if (iv.isInstantiated()) {
                        _inst(iv.getValue() - OFFSET);
                    } else {
                        _rem(vidx);
                    }
                    break;
            }
        }
    }

    /**
     * Actions to apply on int var instantiation
     *
     * @param value instantiated value (offsetted)
     * @throws ContradictionException when failure is detected
     */
    private void _inst(int value) throws ContradictionException {
        _ulb(value, LB.get());
        eqs[value].instantiateTo(1, this);
        lqs[value].instantiateTo(1, this);
        _uub(value, UB.get());
    }


    /**
     * Actions to apply on lower bound int var modification
     *
     * @param nlb new lower bound value (offsetted)
     * @param olb old lower bound (offsetted)
     * @throws ContradictionException when failure is detected
     */
    private void _ulb(int nlb, int olb) throws ContradictionException {
        for (int i = olb; i < nlb; i++) {
            eqs[i].instantiateTo(0, this);
            lqs[i].instantiateTo(0, this);
        }
        LB.set(nlb);
        if (eqs[nlb].isInstantiatedTo(0)) {
            nlb++;
            while (nlb < LENGTH && eqs[nlb].isInstantiatedTo(0)) {
                nlb++;
            }
            iv.updateLowerBound(nlb + OFFSET, this);
            if (iv.isInstantiated()) {
                _inst(iv.getValue() - OFFSET);
            } else {
                _ulb(iv.getLB() - OFFSET, LB.get());
            }
        }
    }

    /**
     * Actions to apply on upper bound int var modification
     *
     * @param nub new upper bound value (offsetted)
     * @param oub old upper bound (offsetted)
     * @throws ContradictionException when failure is detected
     */
    private void _uub(int nub, int oub) throws ContradictionException {
        for (int i = oub; i > nub; i--) {
            eqs[i].instantiateTo(0, this);
            lqs[i].instantiateTo(1, this);
        }
        lqs[nub].instantiateTo(1, this);
        UB.set(nub);
        if (eqs[nub].isInstantiatedTo(0)) {
            nub--;
            while (nub > -1 && eqs[nub].isInstantiatedTo(0)) {
                nub--;
            }
            iv.updateUpperBound(nub + OFFSET, this);
            if (iv.isInstantiated()) {
                _inst(iv.getValue() - OFFSET);
            } else {
                _uub(iv.getUB() - OFFSET, UB.get());
            }
        }
    }

    /**
     * Actions to apply on value removal from int var
     *
     * @param value removed value (offsetted)
     * @throws ContradictionException when failure is detected
     */
    private void _rem(int value) throws ContradictionException {
        eqs[value].instantiateTo(0, this);
        if (iv.isInstantiated()) {
            _inst(iv.getValue());
        }
    }


    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int value = iv.getValue() - OFFSET;
            // for all values below the current lower bound
            for (int k = 0; k < value; k++) {
                if (eqs[k].isInstantiatedTo(1) || lqs[k].isInstantiatedTo(1)) {
                    return ESat.FALSE;
                }
            }
            if (eqs[value].isInstantiatedTo(0) || lqs[value].isInstantiatedTo(0)) return ESat.FALSE;
            for (int k = value + 1; k < LENGTH; k++) {
                if (eqs[k].isInstantiatedTo(1) || lqs[k].isInstantiatedTo(0)) {
                    return ESat.FALSE;
                }
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
