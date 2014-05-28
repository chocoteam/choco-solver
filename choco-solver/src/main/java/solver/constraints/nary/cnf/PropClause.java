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

package solver.constraints.nary.cnf;

import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.EventType;
import util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22 nov. 2010
 * @deprecated replaced by {@link PropSat}, a miniSat solver
 */
@Deprecated
public class PropClause extends Propagator<BoolVar> {

    int watchLit1, watchLit2;
    int nbvars;

    @SuppressWarnings({"unchecked"})
    public PropClause(LogOp t) {
        super(t.flattenBoolVar(), PropagatorPriority.LINEAR, true);
        nbvars = vars.length;
    }

    public PropClause(BoolVar bv) {
        super(new BoolVar[]{bv}, PropagatorPriority.UNARY, true);
        nbvars = 1;
    }

    void awakeOnInst(int index) throws ContradictionException {
        int val = vars[index].getValue();
        if ((index < nbvars && val == 1)) {
            setPassive();
            return;
        }
        if (watchLit1 == index) {
            setWatchLiteral(watchLit2);
        } else if (watchLit2 == index) {
            setWatchLiteral(watchLit1);
        }
        //HACK
        //propagate();
    }

    /**
     * Search a watchLiteral. A watchLiteral (or wL) is pointing out one variable not yet instantiated.
     * If every variables are instantiated, get out.
     * Otherwise, set the new not yet instantiated wL.
     *
     * @param otherWL previous known wL
     * @throws ContradictionException if a contradiction occurs
     */
    private void setWatchLiteral(int otherWL) throws ContradictionException {
        int i = 0;
        int cnt = 0;

        BoolVar bv;
        for (; i < nbvars; i++) {
            bv = vars[i];
            if (bv.isInstantiated()) {
                if (bv.getValue() == 1) {
                    setPassive();
                    return;
                } else {
                    cnt++;
                }
            } else if (i != otherWL) {
                watchLit1 = i;
                watchLit2 = otherWL;
                return;
            }
        }
        if (cnt == vars.length) {
            this.contradiction(null, "Inconsistent");
        }
        if (i == vars.length) {
            vars[otherWL].instantiateTo(1, aCause);
            setPassive();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (vars.length == 1) {
            vars[0].instantiateTo(1, aCause);
            setPassive();
        } else {
            // search for watch literals and check the clause
            int n = vars.length;
            int i = 0, wl = 0, cnt = 0;
            while (i < n && wl < 2) {
                BoolVar bv = vars[i];
                if (bv.isInstantiated()) {
                    if (bv.getValue() == 1) {
                        setPassive();
                        return;
                    } else {
                        cnt++;
                    }
                } else {
                    watchLit2 = watchLit1;
                    watchLit1 = i;
                    wl++;
                }
                i++;
            }
            if (cnt == n) {
                this.contradiction(null, "Inconsistent");
            } else if (cnt == n - 1) {
                setWatchLiteral(watchLit1);
            }
        }
    }


    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            this.awakeOnInst(varIdx);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        int i = 0;
        for (; i < nbvars; i++) {
            st.append(vars[i].getName()).append(" or ");
        }
        st.replace(st.length() - 4, st.length(), "");
        return st.toString();
    }

    @Override
    public ESat isEntailed() {
       int cnt = vars.length;
        for (int i = 0; i < nbvars; i++) {
            if (vars[i].isInstantiated()) {
                if (vars[i].getValue() == 1) {
                    return ESat.TRUE;
                } else {
                    cnt--;
                }
            }
        }
        if (cnt == 0) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }
}
