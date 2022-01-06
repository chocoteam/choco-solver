/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * A propagator for the IntValuePrecede constraint, based on:
 * "Y. C. Law, J. H. Lee,
 * Global Constraints for Integer and Set Value Precedence
 * Principles and Practice of Constraint Programming (CP'2004) LNCS 3258 Springer-Verlag M. G. Wallace, 362–376 2004"
 * <p>
 * Created by cprudhom on 03/07/15.
 * Project: choco.
 */
public class PropIntValuePrecedeChain extends Propagator<IntVar> {

    private final int s;
    private final int t;
    private final int n;
    private final IStateInt a;
    private final IStateInt b;
    private final IStateInt g;

    public PropIntValuePrecedeChain(IntVar[] vars, int s, int t) {
        super(vars, PropagatorPriority.LINEAR, true);
        this.s = s;
        this.t = t;
        this.n = vars.length;
        IEnvironment env = vars[0].getEnvironment();
        a = env.makeInt();
        b = env.makeInt();
        g = env.makeInt();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // initial propagation only
        int _a = a.get();
        while (_a < n && !vars[_a].contains(s)) {
            vars[_a].removeValue(t, this);
            _a++;
        }
        a.set(_a);
        b.set(_a);
        g.set(_a);
        int _g = _a;
        if (_a < n) {
            vars[_a].removeValue(t, this);
            do {
                _g++;
            } while (_g < n && !vars[_g].isInstantiatedTo(t));
            g.set(_g);
            updateB();
        }
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        int _b = b.get();
        if (_b <= g.get()) {
            int _a = a.get();
            if (i == _a && !vars[i].contains(s)) {
                _a++;
                while (_a < _b) {
                    vars[_a].removeValue(t, this);
                    _a++;
                }
                while (_a < n && !vars[_a].contains(s)) {
                    vars[_a].removeValue(t, this);
                    _a++;
                }
                if (_a < n) {
                    vars[_a].removeValue(t, this);
                }
                a.set(_a);
                b.set(_a);
                if (_a < n) {
                    updateB();
                }
            } else if (i == _b && !vars[i].contains(s)) {
                updateB();
            }
        }
        checkC(i);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int is = -1, it = -1;
            for (int i = 0; i < vars.length; i++) {
                if (is == -1 && vars[i].isInstantiatedTo(s)) {
                    is = i;
                }
                if (it == -1 && vars[i].isInstantiatedTo(t)) {
                    it = i;
                }
            }
            if (it == -1) {
                return ESat.TRUE;
            }
            return ESat.eval(is > -1 && is < it);
        }
        return ESat.UNDEFINED;
    }

    private void updateB() throws ContradictionException {
        int _b = b.get();
        do {
            _b++;
        } while (_b < n && !vars[_b].contains(s));
        if (_b > g.get()) {
            vars[a.get()].instantiateTo(s, this);
            setPassive();
        }
        b.set(_b);
    }

    private void checkC(int i) throws ContradictionException {
        if (b.get() < g.get() && i < g.get() && vars[i].isInstantiatedTo(t)) {
            g.set(i);
            if (b.get() > i) {
                vars[a.get()].instantiateTo(s, this);
                setPassive();
            }
        }
    }
}