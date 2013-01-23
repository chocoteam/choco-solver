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

package samples.nqueen;

import solver.constraints.IntConstraintFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class NQueenGlobal extends AbstractNQueen {

    @Override
    public void buildModel() {
        vars = new IntVar[n];
        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];

        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, 1, n, solver);
            diag1[i] = Views.offset(vars[i], i);
            diag2[i] = Views.offset(vars[i], -i);
        }

        solver.post(IntConstraintFactory.alldifferent_bc(vars));
        solver.post(IntConstraintFactory.alldifferent_bc(diag1));
        solver.post(IntConstraintFactory.alldifferent_bc(diag2));
    }

    @Override
    public void configureSearch() {
        solver.set(StrategyFactory.minDomMinVal(vars, solver.getEnvironment()));

        IntVar[] orderedVars = orederIt2();
        /*IPropagationEngine engine = solver.getEngine();
        // default group
        engine.addGroup(
                Group.buildGroup(
                        Predicates.all(),
                        new IncrOrderV(orderedVars),
                        Policy.ITERATE
                ));*/
    }

    public static void main(String[] args) {
        new NQueenGlobal().execute(args);
    }
}
