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
package org.chocosolver.solver.constraints.nary.allen;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 *
 * <p>
 * Project: choco.
 * @author Charles Prud'homme
 * @since 05/02/2016.
 */
public class PropAllenGAC extends Propagator<IntVar> {

    /**
     * Fitlering algorithm, ensuring GAC
     */
    protected final AllenRelation ar;

    /**
     * Set up this Allen relation filtering algorithm.
     *
     * @param Rel   integer variable (domain should not exceed [1,13])
     * @param Oi    origin of the first interval
     * @param Li    length of the first interval
     * @param Oj    origin of the second interval
     * @param Lj    length of th second interval
     * @param absinst use abstract instruction mode
     */
    public PropAllenGAC(IntVar Rel, IntVar Oi, IntVar Li, IntVar Oj, IntVar Lj, boolean absinst) {
        super(new IntVar[]{Rel, Oi, Li, Oj, Lj}, PropagatorPriority.LINEAR, false);
        if(absinst){
            ar = new AllenRelationMats(Rel, Oi, Li, Oj, Lj, this);
        }else{
            ar = new AllenRelationMe(Rel, Oi, Li, Oj, Lj, this);
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ar.filter();
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            return ESat.eval(ar.check());
        }
        return ESat.UNDEFINED;
    }
}
