/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.ternary;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * X = MIN(Y,Z)
 * <br/>
 * ensures bound consistency
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class
        PropMinBC extends Propagator<IntVar> {

    IntVar BST, v1, v2;

    public PropMinBC(IntVar X, IntVar Y, IntVar Z) {
        super(new IntVar[]{X, Y, Z}, PropagatorPriority.TERNARY, true);
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


    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        propagate(0);
    }

    private void filter() throws ContradictionException {
        int c = 0;
        c += (vars[0].isInstantiated() ? 1 : 0);
        c += (vars[1].isInstantiated() ? 2 : 0);
        c += (vars[2].isInstantiated() ? 4 : 0);
        switch (c) {
            case 7: // everything is instantiated
            case 6:// Z and Y are instantiated
                vars[0].instantiateTo(Math.min(vars[1].getValue(), vars[2].getValue()), aCause);
                setPassive();
                break;
            case 5: //  X and Z are instantiated
            {
                int best = vars[0].getValue();
                int val2 = vars[2].getValue();
                if (best < val2) {
                    vars[1].instantiateTo(best, aCause);
                    setPassive();
                } else if (best > val2) {
                    contradiction(vars[2], "wrong min selected");
                } else { // X = Z
                    vars[1].updateLowerBound(best, aCause);
                }
            }
            break;
            case 4: // Z is instantiated
            {
                int val = vars[2].getValue();
                if (val < vars[1].getLB()) { // => X = Z
                    vars[0].instantiateTo(val, aCause);
                    setPassive();
                } else {
                    _filter();
                }
            }
            break;
            case 3://  X and Y are instantiated
            {
                int best = vars[0].getValue();
                int val1 = vars[1].getValue();
                if (best < val1) {
                    vars[2].instantiateTo(best, aCause);
                    setPassive();
                } else if (best > val1) {
                    contradiction(vars[1], "");
                } else { // X = Y
                    vars[2].updateLowerBound(best, aCause);
                }
            }
            break;
            case 2: // Y is instantiated
            {
                int val = vars[1].getValue();
                if (val < vars[2].getLB()) { // => X = Y
                    vars[0].instantiateTo(val, aCause);
                    setPassive();
                } else { // val in Z
                    _filter();
                }
            }
            break;
            case 1: // X is instantiated
            {
                int best = vars[0].getValue();
                if (!vars[1].contains(best) && !vars[2].contains(best)) {
                    contradiction(vars[0], null);
                }
                if (vars[1].getLB() > best) {
                    vars[2].instantiateTo(best, aCause);
                    setPassive();
                } else if (vars[2].getLB() > best) {
                    vars[1].instantiateTo(best, aCause);
                    setPassive();
                } else {
                    if (vars[1].updateLowerBound(best, aCause) | vars[2].updateLowerBound(best, aCause)) {
                        filter(); // to ensure idempotency for "free"
                    }
                }
            }

            break;
            case 0: // otherwise
                _filter();
                break;
        }
    }

    private void _filter() throws ContradictionException {
        boolean change;
        do {
            change = vars[0].updateLowerBound(Math.min(vars[1].getLB(), vars[2].getLB()), aCause);
            change |= vars[0].updateUpperBound(Math.min(vars[1].getUB(), vars[2].getUB()), aCause);
            change |= vars[1].updateLowerBound(vars[0].getLB(), aCause);
            change |= vars[2].updateLowerBound(vars[0].getLB(), aCause);
            if (vars[2].getLB() > vars[0].getUB()) {
                change |= vars[1].updateUpperBound(vars[0].getUB(), aCause);
            }
            if (vars[1].getLB() > vars[0].getUB()) {
                change |= vars[2].updateUpperBound(vars[0].getUB(), aCause);
            }
        } while (change);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (BST.getValue() != Math.min(v1.getValue(), v2.getValue())) {
                return ESat.FALSE;
            } else {
                return ESat.TRUE;
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return BST.toString() + ".MIN(" + v1.toString() + "," + v2.toString() + ")";
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

            identitymap.put(this, new PropMinBC(X, Y, Z));
        }
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        if (var == vars[0]) {
            if (IntEventType.isInstantiate(evt.getMask())) {
                for (int i = 1; i < 3; i++) {
                    // either variable 'i' is the smallest
                    if (vars[i].isInstantiatedTo(var.getValue())) {
                        newrules |= ruleStore.addFullDomainRule(vars[i]);
                    } else
                    // or variable 'i' can't be assigned to 'best'
                    {
                        newrules |= ruleStore.addLowerBoundRule(vars[i]);
                    }
                }
            } else {
                if (IntEventType.isInclow(evt.getMask())) {
                    newrules |= ruleStore.addLowerBoundRule(vars[1]);
                    newrules |= ruleStore.addLowerBoundRule(vars[2]);
                }
                if (IntEventType.isDecupp(evt.getMask())) {
                    newrules |= ruleStore.addUpperBoundRule(vars[1]);
                    newrules |= ruleStore.addUpperBoundRule(vars[2]);
                }
            }
        } else {
            if (IntEventType.isInstantiate(evt.getMask())) {
                newrules |= ruleStore.addFullDomainRule(vars[0]);
            } else {
                if (IntEventType.isInclow(evt.getMask())) {
                    newrules |= ruleStore.addLowerBoundRule(vars[0]);
                }
                if (IntEventType.isDecupp(evt.getMask())) {
                    newrules |= ruleStore.addUpperBoundRule(vars[0]);
                }
            }
        }
        return newrules;
    }
}
