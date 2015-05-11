/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.real;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * The cyclo hexan problem.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/07/12
 */
public class CycloHexan extends AbstractProblem {

    RealVar[] vars;
    RealVar x, y, z;

    @Override
    public void printDescription() {

        System.out.println("The CycloHexan problem consists in finding the 3D configuration of a cyclohexane molecule.\n" +
                "It is decribed with a system of three non linear equations : \n" + " y^2 * (1 + z^2) + z * (z - 24 * y) = -13 \n" +
                " x^2 * (1 + y^2) + y * (y - 24 * x) = -13 \n" +
                " z^2 * (1 + x^2) + x * (x - 24 * z) = -13 \n" + "This example comes from the Elisa project (LINA) examples. \n");
    }

    @Override
    public void createSolver() {
        solver = new Solver("CycloHexan");
    }

    @Override
    public void buildModel() {
        double precision = 1.0e-6;
        x = VariableFactory.real("x", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, precision, solver);
        y = VariableFactory.real("y", -1.0e8, 1.0e8, precision, solver);
        z = VariableFactory.real("z", -1.0e8, 1.0e8, precision, solver);

        vars = new RealVar[]{x, y, z};
		solver.post(new RealConstraint("RealConstraint",
        /*rcons.addFunction("{1}^2 * (1 + {2}^2) + {2} * ({2} - 24 * {1}) = -13", vars, Ibex.COMPO);
        rcons.addFunction("{0}^2 * (1 + {1}^2) + {1} * ({1} - 24 * {0}) = -13", vars, Ibex.COMPO);
        rcons.addFunction("{2}^2 * (1 + {0}^2) + {0} * ({0} - 24 * {2}) = -13", vars);*/
        		"{1}^2 * (1 + {2}^2) + {2} * ({2} - 24 * {1}) = -13;" +
                "{0}^2 * (1 + {1}^2) + {1} * ({1} - 24 * {0}) = -13;" +
                "{2}^2 * (1 + {0}^2) + {0} * ({0} - 24 * {2}) = -13",
                Ibex.HC4_NEWTON, vars)
		);


    }

    @Override
    public void configureSearch() {
        solver.set(new RealStrategy(vars, new Cyclic(), new RealDomainMiddle()));
    }

    @Override
    public void solve() {
		solver.plugMonitor((IMonitorSolution) new IMonitorSolution() {
            @Override
            public void onSolution() {
                StringBuilder st = new StringBuilder();
                st.append("\t");
                for (int i = 0; i < vars.length; i++) {
                    st.append(String.format("%s : [%f, %f]\n\t", vars[i].getName(), vars[i].getLB(), vars[i].getUB()));
                }
                System.out.println("CycloHexan");
                System.out.println(st.toString());
            }
        });
        solver.findAllSolutions();
		solver.getIbex().release();
    }

    @Override
    public void prettyOut() {}

    public static void main(String[] args) {
        new CycloHexan().execute(args);
    }
}
