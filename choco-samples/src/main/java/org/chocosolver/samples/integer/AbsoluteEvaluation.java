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
package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;

import java.util.Random;

/**
 * Example showing how to use absolute constraint
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 31/05/12
 * Time: 11:39
 */
public class AbsoluteEvaluation extends AbstractProblem {

    IntVar[] vars;

    @Override
    public void createSolver() {
        solver = new Solver("AbsoluteEvaluation");
    }

    @Override
    public void buildModel() {
        Random rand = new Random(seed);

        int minX = -20 + rand.nextInt(40);
        int maxX = minX + rand.nextInt(40);

        int minY = -20 + rand.nextInt(40);
        int maxY = minY + rand.nextInt(40);

        vars = new IntVar[2];

        vars[0] = solver.intVar("X", minX, maxX, true);
        vars[1] = solver.intVar("Y", minY, maxY, true);

        solver.absolute(vars[0], vars[1]).post();
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.minDom_LB(vars));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        System.out.println("AbsoluteEvaluation({})");
        StringBuilder st = new StringBuilder();
        st.append("\t");
        for (int i = 0; i < vars.length - 1; i++) {
            st.append(String.format("%d ", vars[i].getValue()));
            if (i % 10 == 9) {
                st.append("\n\t");
            }
        }
        st.append(String.format("%d", vars[vars.length - 1].getValue()));
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new AbsoluteEvaluation().execute(args);
    }
}
