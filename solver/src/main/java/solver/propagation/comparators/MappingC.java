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

package solver.propagation.comparators;

import gnu.trove.map.hash.TObjectIntHashMap;
import solver.constraints.Constraint;
import solver.recorders.IEventRecorder;

import java.io.Serializable;
import java.util.Comparator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/04/11
 */
public class MappingC implements Comparator<IEventRecorder>, Serializable {
    final TObjectIntHashMap<Constraint> criteria;

    public MappingC(Constraint[] constraints, int[] ranks) {
        this.criteria = new TObjectIntHashMap<Constraint>(constraints.length);
        for (int i = 0; i < constraints.length; i++) {
            criteria.put(constraints[i], ranks[i]);
        }
    }

    public MappingC(TObjectIntHashMap<Constraint> ranks) {
        this.criteria = ranks;
    }

    @Override
    public int compare(IEventRecorder o1, IEventRecorder o2) {
        return 0;//criteria.get(o1.getPropagator().getConstraint()) - criteria.get(o2.getPropagator().getConstraint());
    }

    public String toString() {
        return "IncrOrderV";
    }
}
