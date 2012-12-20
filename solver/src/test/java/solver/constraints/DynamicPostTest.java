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
package solver.constraints;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.propagation.PropagationEngines;
import solver.search.loop.monitors.IMonitorOpenNode;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/12/12
 */
public class DynamicPostTest {

    PropagationEngines engine;

    public DynamicPostTest(PropagationEngines engine) {
        this.engine = engine;
    }

    public DynamicPostTest() {
        this(PropagationEngines.CONSTRAINTDRIVEN);
    }

    @Test(groups = "1s")
    public void test0() {
        final Solver solver = new Solver();
        final IntVar X = VariableFactory.enumerated("X", 1, 2, solver);
        final IntVar Y = VariableFactory.enumerated("Y", 1, 2, solver);
        final IntVar Z = VariableFactory.enumerated("Z", 1, 2, solver);
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        Assert.assertEquals(8, solver.getMeasures().getSolutionCount());
    }


    @Test(groups = "1s")
    public void test1() {
        final Solver solver = new Solver();
        final IntVar X = VariableFactory.enumerated("X", 1, 2, solver);
        final IntVar Y = VariableFactory.enumerated("Y", 1, 2, solver);
        final IntVar Z = VariableFactory.enumerated("Z", 1, 2, solver);
        solver.getSearchLoop().plugSearchMonitor(new IMonitorOpenNode() {
            @Override
            public void beforeOpenNode() {
            }

            @Override
            public void afterOpenNode() {
                if (solver.getMeasures().getNodeCount() == 1) {
                    solver.post(new Arithmetic(X, "=", Y, solver));
                    solver.post(new Arithmetic(Y, "=", Z, solver));
                }
            }
        });
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        Assert.assertEquals(5, solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test2() {
        final Solver solver = new Solver();
        final IntVar X = VariableFactory.enumerated("X", 1, 2, solver);
        final IntVar Y = VariableFactory.enumerated("Y", 1, 2, solver);
        final IntVar Z = VariableFactory.enumerated("Z", 1, 2, solver);
        solver.getSearchLoop().plugSearchMonitor(new IMonitorOpenNode() {
            @Override
            public void beforeOpenNode() {
            }

            @Override
            public void afterOpenNode() {
                if (solver.getMeasures().getNodeCount() == 1) {
                    solver.postCut(new Arithmetic(X, "=", Y, solver));
                    solver.postCut(new Arithmetic(Y, "=", Z, solver));
                }
            }
        });
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        Assert.assertEquals(2, solver.getMeasures().getSolutionCount());
    }
}
