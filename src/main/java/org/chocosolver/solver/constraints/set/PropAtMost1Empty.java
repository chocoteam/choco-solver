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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package org.chocosolver.solver.constraints.set;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;

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
     * @param sets array of set variables
     */
    public PropAtMost1Empty(SetVar[] sets) {
        super(sets, PropagatorPriority.UNARY, true);
        emptySetIndex = model.getEnvironment().makeInt(-1);
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
        if (vars[v].getUB().getSize() == 0) {
            if (emptySetIndex.get() != -1) {
                contradiction(vars[v], "");
            } else {
                emptySetIndex.set(v);
                for (int i = 0; i < vars.length; i++) {
                    int s = vars[i].getUB().getSize();
                    if (i != v && s != vars[i].getLB().getSize()) {
                        if (s == 0) {
                            contradiction(vars[i], "");
                        } else if (s == 1) {
                            vars[i].force(vars[i].getUB().iterator().next(), this);
                        }
                    }
                }
            }
        }
        if (vars[v].getUB().getSize() == 1 && emptySetIndex.get() != -1) {
            vars[v].force(vars[v].getUB().iterator().next(), this);
        }
    }

    @Override
    public ESat isEntailed() {
        boolean none = true;
        boolean allInstantiated = true;
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].getUB().getSize() == 0) {
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

}
