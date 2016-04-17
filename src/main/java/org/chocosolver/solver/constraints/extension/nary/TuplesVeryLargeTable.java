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

import gnu.trove.map.hash.TIntObjectHashMap;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

/**
 * A LargeRelation for cases where domain are too big to be stored in a single array.
 * Then, we store it in a chunk bitsets
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public class TuplesVeryLargeTable extends LargeRelation {

    /**
     * the number of dimensions of the considered tuples
     */
    private final int n;

    private final boolean feasible;

    private final TIntObjectHashMap<TIntObjectHashMap> supports;

    public TuplesVeryLargeTable(Tuples tuples, IntVar[] vars) {
        n = vars.length;
        feasible = tuples.isFeasible();
        supports = new TIntObjectHashMap<>();
        int nt = tuples.nbTuples();
        for (int i = 0; i < nt; i++) {
            int[] tuple = tuples.get(i);
            if (valid(tuple, vars)) {
                setTuple(tuple);
            }
        }
    }

    public boolean checkTuple(int[] tuple) {
        TIntObjectHashMap<TIntObjectHashMap> current = supports;
        int i = 0;
        while (i < n - 1) {
            current = current.get(tuple[i++]);
            if (current == null) {
                return false;
            }
        }
        current = current.get(tuple[i]);
        return current != null;
    }

    public boolean isConsistent(int[] tuple) {
        return checkTuple(tuple) == feasible;
    }

    @SuppressWarnings("unchecked")
    private void setTuple(int[] tuple) {
        TIntObjectHashMap<TIntObjectHashMap> current = supports;
        for (int i = 0; i < tuple.length; i++) {
            TIntObjectHashMap<TIntObjectHashMap> _current = current.get(tuple[i]);
            if (_current == null) {
                _current = new TIntObjectHashMap<>();
                current.put(tuple[i], _current);
            }
            current = _current;
        }
    }
}
