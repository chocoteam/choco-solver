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
package org.chocosolver.solver.constraints.extension.binary;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.iterators.DisposableValueIterator;

/**
 * Forward checking algorithm for table constraint
 * <br/>
 *
 * @author Charles Prud'homme, Hadrien Cambazard
 * @since 24/04/2014
 */
public class PropBinFC extends PropBinCSP {

    public PropBinFC(IntVar x, IntVar y, Tuples tuples) {
        this(x, y, new CouplesTable(tuples, x, y));
    }

    private PropBinFC(IntVar x, IntVar y, CouplesTable table) {
        super(x, y, table);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.instantiation();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (v0.isInstantiated())
            onInstantiation0();
        if (v1.isInstantiated())
            onInstantiation1();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) onInstantiation0();
        else onInstantiation1();
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            this.vars[0].duplicate(solver, identitymap);
            IntVar X = (IntVar) identitymap.get(this.vars[0]);
            this.vars[1].duplicate(solver, identitymap);
            IntVar Y = (IntVar) identitymap.get(this.vars[1]);

            identitymap.put(this, new PropBinFC(X, Y, (CouplesTable) relation.duplicate()));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void onInstantiation0() throws ContradictionException {
        int left, right;
        int value = v0.getValue();
        DisposableValueIterator values = v1.getValueIterator(true);
        left = right = Integer.MIN_VALUE;
        try {
            while (values.hasNext()) {
                int val = values.next();
                if (!relation.isConsistent(value, val)) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        v1.removeInterval(left, right, this);
                        left = right = val;
                    }
                }
            }
            v1.removeInterval(left, right, this);
        } finally {
            values.dispose();
        }
    }

    private void onInstantiation1() throws ContradictionException {
        int left, right;
        int value = v1.getValue();
        DisposableValueIterator values = v0.getValueIterator(true);
        left = right = Integer.MIN_VALUE;
        try {
            while (values.hasNext()) {
                int val = values.next();
                if (!relation.isConsistent(val, value)) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        v0.removeInterval(left, right, this);
                        left = right = val;
                    }
                }
            }
            v0.removeInterval(left, right, this);
        } finally {
            values.dispose();
        }
    }
}
