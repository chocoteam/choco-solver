/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

    int s, t, n;
    IStateInt a, b, g;

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