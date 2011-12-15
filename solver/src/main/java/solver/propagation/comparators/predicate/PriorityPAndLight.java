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

package solver.propagation.comparators.predicate;

import solver.constraints.propagators.PropagatorPriority;
import solver.recorders.IEventRecorder;

/**
 * Prop.priority <= threshold (inclusive)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/04/11
 */
public class PriorityPAndLight implements Predicate {

    int[] cached;

    final PropagatorPriority threshold;

    PriorityPAndLight(PropagatorPriority threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean eval(IEventRecorder evtrec) {
        return false;//evtrec.getPropagator().getPriority().priority >= threshold.priority && evtrec.getVariable() != null;
    }

    @Override
    public int[] extract(IEventRecorder[] all) {
        /*if (cached == null) {
            TIntHashSet tmp = new TIntHashSet();
            for (int i = 0; i < all.length; i++) {
                if (all[i].getPropagator().getPriority().priority >= threshold.priority) {
                    if (all[i].getVariable() == null) {
                        tmp.add(all[i].getIndex(IRequest.IN_GROUP));
                    }
                }
            }
            cached = tmp.toArray();
        }*/
        return cached;
    }
}
