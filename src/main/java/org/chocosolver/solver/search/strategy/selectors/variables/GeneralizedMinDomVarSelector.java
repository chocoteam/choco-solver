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
package org.chocosolver.solver.search.strategy.selectors.variables;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

/**
 * <b>First fail</b> variable selector generalized to all variables.
 * It chooses the variable with the smallest domain (instantiated variables are ignored).
 * <br/>
 *
 * @author Jean-Guillaume Fages
 */
public class GeneralizedMinDomVarSelector implements VariableSelector {

    private boolean least;

    /**
     * Chooses the non-instantiated variable with the smallest domain
     * <b>First fail</b> generalization to all variable kinds.
     */
    public GeneralizedMinDomVarSelector(){
        this(true);
    }

	/**
     * Chooses the non-instantiated variable with the smallest domain
     * <b>First fail</b> generalization to all variable kinds.
     * @param leastFree chooses the most Free variable if set to false
     */
    public GeneralizedMinDomVarSelector(boolean leastFree){
        this.least = leastFree;
    }

    @Override
    public Variable getVariable(Variable[] variables) {
        int small_dsize = Integer.MAX_VALUE;
        Variable nextVar = null;
        for (Variable v:variables) {
            if(!v.isInstantiated()) {
                int kind = (v.getTypeAndKind() & Variable.KIND);
                int dsize;
                if (kind == Variable.INT || kind == Variable.BOOL) {
                    dsize = ((IntVar) v).getDomainSize();
                } else if (kind == Variable.REAL) {
                    RealVar rv = (RealVar) v;
                    dsize = 2 + (int) ((rv.getUB() - rv.getLB())/rv.getPrecision());
                } else if (kind == Variable.SET) {
                    SetVar sv = (SetVar) v;
                    dsize = 1 + (sv.getUB().getSize() - sv.getLB().getSize());
                } else {
                    throw new UnsupportedOperationException("unrocognised variable kind");
                }
                if (nextVar == null) {
                    nextVar = v;
                    small_dsize = dsize;
                }else if(dsize > 1 && dsize < Integer.MAX_VALUE){
                    if (dsize < small_dsize == least) {
                        small_dsize = dsize;
                        nextVar = v;
                    }
                }
            }
        }
        return nextVar;
    }
}
