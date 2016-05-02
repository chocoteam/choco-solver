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
package org.chocosolver.solver.constraints.extension.binary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Hadrien Cambazard
 * @since 08/06/11
 */
public abstract class PropBinCSP extends Propagator<IntVar> {

    protected BinRelation relation;
    protected IntVar v0, v1;

    protected PropBinCSP(IntVar x, IntVar y, BinRelation relation) {
        super(ArrayUtils.toArray(x, y), PropagatorPriority.BINARY, true);
        this.relation = relation;
        this.v0 = x;
        this.v1 = y;
    }

    public final BinRelation getRelation() {
        return relation;
    }

    @Override
    public ESat isEntailed() {
        int nbCons = 0;

        int ub0 = vars[0].getUB();
        for (int val0 = vars[0].getLB(); val0 <= ub0; val0 = vars[0].nextValue(val0)) {
            int nbS = 0;
            int ub1 = vars[1].getUB();
            for (int val1 = vars[1].getLB(); val1 <= ub1; val1 = vars[1].nextValue(val1)) {
                if (relation.isConsistent(val0, val1)) {
                    nbS++;
                }
            }
            if (nbS > 0 && nbS < vars[1].getDomainSize()) {
                return ESat.UNDEFINED;
            }
            nbCons += nbS;

        }
        if (nbCons == 0) {
            return ESat.FALSE;
        } else if (nbCons == vars[0].getDomainSize() * vars[1].getDomainSize()) {
            return ESat.TRUE;
        }
        return null;
    }
}
