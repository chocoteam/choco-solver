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

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

/**
 * <b>First fail</b> variable selector.
 * It chooses the variable with the smallest domain (instantiated variables are ignored).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class FirstFail implements VariableSelector<IntVar>, VariableEvaluator<IntVar> {

    private IStateInt lastIdx; // index of the last non-instantiated variable

    /**
     * <b>First fail</b> variable selector.
     * @param model reference to the model (does not define the variable scope)
     */
    public FirstFail(Model model){
        lastIdx = model.getEnvironment().makeInt(0);
    }

    @Override
    public IntVar getVariable(IntVar[] variables) {
        int small_idx = -1;
        int small_dsize = Integer.MAX_VALUE;
        boolean got = false;
        for (int idx = lastIdx.get(); idx < variables.length; idx++) {
            int dsize = variables[idx].getDomainSize();

            if (!got && !variables[idx].isInstantiated()) {
                //got is just to call 'set' at most once
                lastIdx.set(idx);
                got = true;
            }

            if (dsize > 1 && dsize < small_dsize) {
                small_dsize = dsize;
                small_idx = idx;
            }

            if (small_dsize == 2) {
                break;
            }
        }
        return small_idx > -1 ? variables[small_idx] : null;
    }

    @Override
    public double evaluate(IntVar variable) {
        return variable.getDomainSize();
    }
}
