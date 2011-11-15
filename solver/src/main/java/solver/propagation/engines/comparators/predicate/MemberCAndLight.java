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

import choco.kernel.common.util.tools.ArrayUtils;
import gnu.trove.TIntHashSet;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.requests.IRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class MemberCAndLight implements Predicate {

    int[] cached;

    Constraint[] cons;
    Set<Constraint> s_cons;

    MemberCAndLight(Constraint[] cons) {
        this.cons = cons.clone();
        s_cons = new HashSet<Constraint>(Arrays.asList(cons));
    }

    MemberCAndLight(Constraint cons0, Constraint... cons) {
        this(ArrayUtils.append(new Constraint[]{cons0}, cons));
    }

    public boolean eval(IRequest request) {
        return this.s_cons.contains(request.getPropagator().getConstraint()) && request.getVariable() != null;
    }

    @Override
    public int[] extract(IRequest[] all) {
        if (cached == null) {
            TIntHashSet tmp = new TIntHashSet();
            for (int i = 0; i < cons.length; i++) {
                Constraint c = cons[i];
                for (int j = 0; j < c.propagators.length; j++) {
                    Propagator p = c.propagators[j];
                    //-1 is the big request, so we can skip it easily
                    for (int k = 0; k < p.nbRequests(); k++) {
                        IRequest r = p.getRequest(k);
                        if (r.getVariable() == null) {
                            tmp.add(r.getIndex(IRequest.IN_GROUP));
                        }
                    }
                }
            }
            cached = tmp.toArray();
        }
        return cached;
    }

    public String toString() {
        return "MemberC_light";
    }
}
