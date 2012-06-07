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
package samples;

import org.kohsuke.args4j.Option;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PArc;
import solver.propagation.generator.PCoarse;
import solver.propagation.generator.Queue;
import solver.propagation.generator.Sort;
import solver.propagation.generator.predicate.InCstrSet;
import solver.propagation.generator.predicate.Predicate;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28/03/12
 */
public class Ordering extends AbstractProblem {

    @Option(name = "-n", aliases = "--number", usage = "number of variables.", required = false)
    int n = 1000;

    IntVar[] vars;
    Constraint[] cstrs;

    @Override
    public void createSolver() {
        solver = new Solver("Ordering " + n);
    }

    @Override
    public void buildModel() {
        vars = VariableFactory.boundedArray("v", n, 1, n, solver);
        cstrs = new Constraint[n - 1];
        for (int i = 0; i < n - 1; i++) {
            cstrs[i] = ConstraintFactory.lt(vars[i], vars[i + 1], solver);
        }
        solver.post(cstrs);
    }

    @Override
    public void configureSearch() {
    }

    @Override
    public void configureEngine() {
        IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
        PArc[] arc1 = new PArc[n - 1];
        for (int i = 0; i < n - 1; i++) {
            arc1[i] = new PArc(propagationEngine, new IntVar[]{vars[i]}, new Predicate[]{new InCstrSet(cstrs[i])});
        }
        Sort s1 = new Sort(arc1);
        PArc[] arc2 = new PArc[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            arc2[n - 2 - i] = new PArc(propagationEngine, new IntVar[]{vars[i + 1]}, new Predicate[]{new InCstrSet(cstrs[i])});
        }
        Sort s2 = new Sort(arc2);

        solver.set(
                propagationEngine.set(
                        new Sort(
                                s1.loopOut(),
                                s2.loopOut(),
                                new Queue(new PCoarse(propagationEngine, cstrs)).clearOut()
                        )
                )
        );
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new Ordering().execute(args);
    }
}
