/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package solver.constraints.set;

import gnu.trove.map.hash.THashMap;
import memory.IStateInt;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.SetVar;
import solver.variables.events.SetEventType;
import util.ESat;

/**
 * At most one set can be empty
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtMost1Empty extends Propagator<SetVar> {

    private IStateInt emptySetIndex;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * At most one set in the array sets can be empty
     *
     * @param sets
     */
    public PropAtMost1Empty(SetVar[] sets) {
        super(sets, PropagatorPriority.UNARY, true);
        emptySetIndex = solver.getEnvironment().makeInt(-1);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return SetEventType.REMOVE_FROM_ENVELOPE.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < vars.length; i++) {
            propagate(i, 0);
        }
    }

    @Override
    public void propagate(int v, int mask) throws ContradictionException {
        if (vars[v].getEnvelopeSize() == 0) {
            if (emptySetIndex.get() != -1) {
                contradiction(vars[v], "");
            } else {
                emptySetIndex.set(v);
                for (int i = 0; i < vars.length; i++) {
                    int s = vars[i].getEnvelopeSize();
                    if (i != v && s != vars[i].getKernelSize()) {
                        if (s == 0) {
                            contradiction(vars[i], "");
                        } else if (s == 1) {
                            vars[i].addToKernel(vars[i].getEnvelopeFirst(), aCause);
                        }
                    }
                }
            }
        }
        if (vars[v].getEnvelopeSize() == 1 && emptySetIndex.get() != -1) {
            vars[v].addToKernel(vars[v].getEnvelopeFirst(), aCause);
        }
    }

    @Override
    public ESat isEntailed() {
        boolean none = true;
        boolean allInstantiated = true;
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].getEnvelopeSize() == 0) {
                if (!none) {
                    return ESat.FALSE;
                }
                none = false;
            } else if (!vars[i].isInstantiated()) {
                allInstantiated = false;
            }
        }
        if (allInstantiated) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length;
            SetVar[] aVars = new SetVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (SetVar) identitymap.get(this.vars[i]);
            }
            identitymap.put(this, new PropAtMost1Empty(aVars));
        }
    }
}
