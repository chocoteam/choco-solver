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
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * A system of equations provided by N. Beldiceanu.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 10/07/12
 */
public class Eq5 extends AbstractProblem {

    IntVar[] vars;

    @Override
    public void createSolver() {
        solver = new Solver("Eq5");
    }

    @Override
    public void buildModel() {
        vars = new IntVar[15];
        vars[0] = solver.intVar("A90397", -20, 20, false);
        vars[1] = solver.intVar("A90429", -20, 20, false);
        vars[2] = solver.intVar("A90461", -20, 20, false);
        vars[3] = solver.intVar("A90493", -20, 20, false);
        vars[4] = solver.intVar("A90525", -20, 20, false);
        vars[5] = solver.intVar("A90557", -20, 20, false);
        vars[6] = solver.intVar("A90589", -20, 20, false);
        vars[7] = solver.intVar("A90621", -20, 20, false);
        vars[8] = solver.intVar("A90653", -20, 20, false);
        vars[9] = solver.intVar("A90685", -20, 20, false);
        vars[10] = solver.intVar("A90717", -20, 20, false);
        vars[11] = solver.intVar("A90749", -20, 20, false);
        vars[12] = solver.intVar("A90781", -20, 20, false);
        vars[13] = solver.intVar("A90813", -20, 20, false);
        vars[14] = solver.intVar("A90845", -20, 20, false);

//        A90397 + A90429 * 3 + A90461 * 9 + A90493 * 27 + A90525 * 81 + A90557 + A90589 * 3 + A90621 * 9 +
//                A90653 * 27 + A90685 + A90717 * 3 + A90749 * 9 + A90781 + A90813 * 3 + A90845 = 380
        solver.post(solver.scalar(vars, new int[]{1, 3, 9, 27, 81, 1, 3, 9, 27, 1, 3, 9, 1, 3, 1}, "=", 380));

//        A90397 * 81 + A90429 * 108 + A90461 * 144 + A90493 * 192 + A90525 * 256 + A90557 * 27 + A90589 * 36 + A90621 * 48 +
//                A90653 * 64 + A90685 * 9 + A90717 * 12 + A90749 * 16 + A90781 * 3 + A90813 * 4 + A90845 = 1554
        solver.post(solver.scalar(vars, new int[]{81, 108, 144, 192, 256, 27, 36, 48, 64, 9, 12, 16, 3, 4, 1}, "=", 1554));
//        A90397 * 1296 + A90429 * 1080 + A90461 * 900 + A90493 * 750 + A90525 * 625 + A90557 * 216 + A90589 * 180 + A90621 * 150 +
//                A90653 * 125 + A90685 * 36 + A90717 * 30 + A90749 * 25 + A90781 * 6 + A90813 * 5 + A90845 = 4392
        solver.post(solver.scalar(vars, new int[]{1296, 1080, 900, 750, 625, 216, 180, 150, 125, 36, 30, 25, 6, 5, 1}, "=", 4392));
//        A90397 * 16 + A90429 * 56 + A90461 * 196 + A90493 * 686 + A90525 * 2401 + A90557 * 8 + A90589 * 28 + A90621 * 98 +
//                A90653 * 343 + A90685 * 4 + A90717 * 14 + A90749 * 49 + A90781 * 2 + A90813 * 7 + A90845 = 16510
        solver.post(solver.scalar(vars, new int[]{16, 56, 496, 686, 2401, 8, 28, 98, 343, 4, 14, 49, 2, 7, 1}, "=", 16510));
//        A90397 * 194481 + A90429 * 55566 + A90461 * 15876 + A90493 * 4536 + A90525 * 1296 + A90557 * 9261 + A90589 * 2646 + A90621 * 756 +
//                A90653 * 216 + A90685 * 441 + A90717 * 126 + A90749 * 36 + A90781 * 21 + A90813 * 6 + A90845 = 12012
        solver.post(solver.scalar(vars, new int[]{194481, 55566, 15876, 4536, 1296, 9261, 2646, 756, 216, 441, 126, 36, 21, 6, 1}, "=", 12012));

    }

    @Override
    public void configureSearch() {
        solver.set(new IntStrategy(vars, new InputOrder<>(), new IntDomainMiddle(true)));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        System.out.println("15 equations");
        StringBuilder st = new StringBuilder();
        if (solver.isFeasible() == ESat.TRUE) {
            for (int i = 0; i < 15; i++) {
                st.append(vars[i].getValue()).append(", ");
            }
        } else {
            st.append("\tINFEASIBLE");
        }
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new Eq5().execute(args);
    }
}
