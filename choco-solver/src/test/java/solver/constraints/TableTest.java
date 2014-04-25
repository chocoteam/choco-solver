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


/**
 * @author Jean-Guillaume Fages
 * @since 10/04/14
 * Created by IntelliJ IDEA.
 */
package solver.constraints;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.extension.Tuples;
import solver.search.strategy.ISF;
import solver.variables.IntVar;
import solver.variables.VF;

public class TableTest {

    @Test(groups = "1s")
    public void test1() {
        Tuples tuples = new Tuples(true);
        tuples.add(0, 0, 0);
        tuples.add(1, 1, 1);
        tuples.add(2, 2, 2);

        Solver solver = new Solver();
        IntVar[] vars = VF.enumeratedArray("X", 3, 0, 1, solver);
        Constraint tableConstraint = ICF.table(vars, tuples, "AC2001");
        solver.post(tableConstraint);

        solver.findSolution();
    }


    @Test(groups = "1s")
    public void test2() {
        for (int i = 0; i < 10; i++) {
            Tuples tuples = new Tuples(true);
            tuples.add(-2, -2);
            tuples.add(-1, -1);
            tuples.add(0, 0);
            tuples.add(1, 1);

            Solver solver = new Solver();
            IntVar[] vars = VF.enumeratedArray("X", 2, -1, 1, solver);
            solver.post(ICF.table(vars[0], vars[1], tuples, "AC3bit+rm"));

            solver.set(ISF.random_value(vars));
            Assert.assertEquals(solver.findAllSolutions(), 3);
        }
    }

    @Test(groups = "1s")
    public void test3() {
        for (int i = 0; i < 10; i++) {
            Tuples tuples = new Tuples(true);
            tuples.add(-2, -2);
            tuples.add(-1, -1);
            tuples.add(0, 0);
            tuples.add(1, 1);

            Solver solver = new Solver();
            IntVar[] vars = VF.enumeratedArray("X", 2, -1, 1, solver);
            solver.post(ICF.table(vars[0], vars[1], tuples, "AC2001"));

            solver.set(ISF.random_value(vars));
            Assert.assertEquals(solver.findAllSolutions(), 3);
        }
    }

}
