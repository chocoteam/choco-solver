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
package org.chocosolver.solver.search.loop.monitors;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.nary.cnf.PropNogoods;
import org.chocosolver.solver.constraints.nary.cnf.SatSolver;
import org.chocosolver.solver.variables.IntVar;

/**
 * Avoid exploring same solutions (useful with restart on solution)
 * Beware :
 * - Must be plugged as a monitor
 * - Only works for integer variables
 * <p>
 * This can be used to remove similar/symmetric solutions
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 20/06/13
 */
public class NogoodFromSolutions implements IMonitorSolution {

    final PropNogoods png;
    final protected IntVar[] decisionVars;
    final protected TIntList ps;

    /**
     * Avoid exploring same solutions (useful with restart on solution)
     * Beware :
     * - Must be posted as a constraint AND plugged as a monitor as well
     * - Cannot be reified
     * - Only works for integer variables
     * <p>
     * This can be used to remove similar/symmetric solutions
     *
     * @param vars all decision variables which define a solution (can be a subset of variables)
     */
    public NogoodFromSolutions(IntVar[] vars) {
        decisionVars = vars;
        png = vars[0].getModel().getNogoodStore().getPropNogoods();
        ps = new TIntArrayList();
    }

    @Override
    public void onSolution() {
        int n = decisionVars.length;
        ps.clear();
        for (int i = 0; i < n; i++) {
            ps.add(SatSolver.negated(png.Literal(decisionVars[i], decisionVars[i].getValue(), true)));
        }
        png.addLearnt(ps.toArray());
    }

}
