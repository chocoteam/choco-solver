/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package solver.propagation.engines.comparators.predicate;

import choco.kernel.common.util.objects.IList;
import choco.kernel.common.util.tools.ArrayUtils;
import gnu.trove.TIntHashSet;
import solver.requests.IRequest;
import solver.variables.Variable;

import java.util.*;


public class MemberV<V extends Variable> implements Predicate {

    int[] cached;

    V[] vars;
    Set<V> s_cons;

    MemberV(V[] vars) {
        this.vars = vars.clone();
    }

    MemberV(V vars0, V... vars) {
        this(ArrayUtils.append((V[]) new Variable[]{vars0}, vars));
    }

    public boolean eval(IRequest request) {
        if (s_cons == null) {
            s_cons = new HashSet<V>(Arrays.asList(vars));
        }
        return this.s_cons.contains(request.getVariable());
    }

    @Override
    public int[] extract(IRequest[] all) {
        if (cached == null) {
            TIntHashSet tmp = new TIntHashSet();
            for (int i = 0; i < vars.length; i++) {
                IList rlist = vars[i].getMonitors();
                for (int k = 0; k < rlist.cardinality(); k++) {
                    int idx = rlist.get(k).getIndex(IRequest.IN_GROUP);
                    tmp.add(idx);
                }
            }
            cached = tmp.toArray();
        }
        return cached;
    }

}
