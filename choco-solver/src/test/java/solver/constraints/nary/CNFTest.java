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
package solver.constraints.nary;

import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.constraints.nary.cnf.LogOp;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/04/12
 */
public class CNFTest {

    @Test(groups = "1s")
    public void testJGF() {
        for (int i = 0; i < 2; i++) {

            Solver solver = new Solver();
            BoolVar a = VariableFactory.bool("a", solver);
            BoolVar b = VariableFactory.bool("b", solver);
            IntVar x = VariableFactory.bounded("x", 0, 24, solver);
            IntVar y = VariableFactory.bounded("y", 0, 24, solver);

            if (i == 0) {
                solver.post(IntConstraintFactory.clauses(LogOp.implies(
                        a,
                        b
                ), solver));
            } else {
                solver.post(IntConstraintFactory.clauses(LogOp.implies(
                        b.not(),
                        a.not()
                ), solver));
            }
            solver.post(LogicalConstraintFactory.ifThenElse(b, IntConstraintFactory.arithm(x, ">=", y), IntConstraintFactory.arithm(x, "<", y)));
//            SearchMonitorFactory.log(solver, true, true);
            solver.findAllSolutions();
            System.out.printf("%d\n", solver.getMeasures().getSolutionCount());
        }
    }
}
