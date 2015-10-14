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
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.solver.variables.ranges.IntIterableBitSet;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public class PropLargeFC extends PropLargeCSP<LargeRelation> {

    protected final int[] currentTuple;
    protected final IntIterableSet vrms;

    private PropLargeFC(IntVar[] vars, LargeRelation relation) {
        super(vars, relation);
        this.currentTuple = new int[vars.length];
        vrms = new IntIterableBitSet();
    }

    public PropLargeFC(IntVar[] vars, Tuples tuples) {
        this(vars, makeRelation(tuples, vars));
    }

    private static LargeRelation makeRelation(Tuples tuples, IntVar[] vars) {
        long totalSize = 1;
        for (int i = 0; i < vars.length && totalSize > 0; i++) { // to prevent from long overflow
            totalSize *= vars[i].getDomainSize();
        }
        if (totalSize < 0) {
            return new TuplesVeryLargeTable(tuples, vars);
        }
        if (totalSize / 8 > 50 * 1024 * 1024) {
            return new TuplesLargeTable(tuples, vars);
        }
        return new TuplesTable(tuples, vars);
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate(PropagatorEventType.FULL_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int[] tuple = new int[vars.length];
            for (int i = 0; i < vars.length; i++) {
                tuple[i] = vars[i].getValue();
            }
            return ESat.eval(relation.isConsistent(tuple));
        }
        return ESat.UNDEFINED;
//        return ESat.TRUE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CSPLarge({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(vars[i]).append(", ");
        }
        sb.append("})");
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void filter() throws ContradictionException {
        boolean stop = false;
        int nbUnassigned = 0;
        int index = -1, i = 0;
        while (!stop && i < vars.length) {
            if (!vars[i].isInstantiated()) {
                nbUnassigned++;
                index = i;
            } else {
                currentTuple[i] = vars[i].getValue();
            }
            if (nbUnassigned > 1) {
                stop = true;
            }
            i++;
        }
        if (!stop) {
            if (nbUnassigned == 1) {
                vrms.clear();
                vrms.setOffset(vars[index].getLB());

                int ub = vars[index].getUB();
                for (int val = vars[index].getLB(); val <= ub; val = vars[index].nextValue(val)) {
                    currentTuple[index] = val;
                    if (!relation.isConsistent(currentTuple)) {
                        vrms.add(val);
                    }
                }
                vars[index].removeValues(vrms, this);
            } else {
                if (!relation.isConsistent(currentTuple)) {
                    fails();
                }
            }
        }
    }

}
