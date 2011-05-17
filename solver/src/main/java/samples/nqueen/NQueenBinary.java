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

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.propagation.engines.IPropagationEngine;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.IncrOrderV;
import solver.propagation.engines.comparators.predicate.Predicate;
import solver.propagation.engines.group.Group;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.HashSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class NQueenBinary extends AbstractNQueen {

    HashSet<Constraint> set = new HashSet<Constraint>();

    @Override
    public void buildModel() {
        solver = new Solver();
        vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, 1, n, solver);
        }


        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = ConstraintFactory.neq(vars[i], vars[j], solver);
                solver.post(neq);
                set.add(neq);
                solver.post(ConstraintFactory.neq(vars[i], vars[j], -k, solver));
                solver.post(ConstraintFactory.neq(vars[i], vars[j], k, solver));
            }
        }
    }

    @Override
    public void configureSolver() {
        solver.set(StrategyFactory.minDomMinVal(vars, solver.getEnvironment()));

        IntVar[] orderedVars = orederIt2();
        IPropagationEngine engine = solver.getEngine();
        // default group
        engine.addGroup(
                Group.buildGroup(
                        Predicate.TRUE,
                        new IncrOrderV(orderedVars),
                        Policy.FIXPOINT
                ));

    }

    public static void main(String[] args) {
        new NQueenBinary().execute(args);
    }
}
