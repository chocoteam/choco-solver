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
package solver.constraints.extension.binary;

import gnu.trove.map.hash.THashMap;
import solver.Solver;
import solver.constraints.extension.Tuples;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.iterators.DisposableValueIterator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/04/2014
 */
public class PropBinAC3 extends PropBinCSP {

    public PropBinAC3(IntVar x, IntVar y, Tuples tuples) {
        this(x, y, new CouplesBitSetTable(tuples, x, y));
    }

    private PropBinAC3(IntVar x, IntVar y, CouplesBitSetTable table) {
        super(x, y, table);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        reviseV0();
        reviseV1();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) {
            reviseV1();
        } else
            reviseV0();
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            this.vars[0].duplicate(solver, identitymap);
            IntVar X = (IntVar) identitymap.get(this.vars[0]);
            this.vars[1].duplicate(solver, identitymap);
            IntVar Y = (IntVar) identitymap.get(this.vars[1]);

            identitymap.put(this, new PropBinAC3(X, Y, (CouplesBitSetTable) relation.duplicate()));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * updates the support for all values in the domain of v1, and remove unsupported values for v1
     */
    private void reviseV1() throws ContradictionException {
        int nbs = 0;
        int left = Integer.MIN_VALUE;
        int right = left;
        DisposableValueIterator itv1 = v1.getValueIterator(true);
        while (itv1.hasNext()) {
            int val1 = itv1.next();
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            while (itv0.hasNext()) {
                int val0 = itv0.next();
                if (relation.isConsistent(val0, val1)) {
                    nbs += 1;
                    break;
                }
            }
            itv0.dispose();
            if (nbs == 0) {
                if (val1 == right + 1) {
                    right = val1;
                } else {
                    v1.removeInterval(left, right, this);
                    left = right = val1;
                }
            }
            nbs = 0;
        }
        v1.removeInterval(left, right, this);
        itv1.dispose();
    }

    /**
     * updates the support for all values in the domain of v0, and remove unsupported values for v0
     */
    private void reviseV0() throws ContradictionException {
        int nbs = 0;
        int left = Integer.MIN_VALUE;
        int right = left;
        DisposableValueIterator itv0 = v0.getValueIterator(true);
        while (itv0.hasNext()) {
            int val0 = itv0.next();
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            while (itv1.hasNext()) {
                int val1 = itv1.next();
                if (relation.isConsistent(val0, val1)) {
                    nbs += 1;
                    break;
                }
            }
            itv1.dispose();
            if (nbs == 0) {
                if (val0 == right + 1) {
                    right = val0;
                } else {
                    v0.removeInterval(left, right, this);
                    left = right = val0;
                }
            }
            nbs = 0;
        }
        v0.removeInterval(left, right, this);
        itv0.dispose();
    }

}
