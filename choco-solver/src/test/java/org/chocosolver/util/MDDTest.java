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
package org.chocosolver.util;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Created by cprudhom on 04/11/14.
 * Project: Choco3
 */
public class MDDTest {

    @Test(groups="1s", timeOut=60000)
    public void test0() {
        Solver solver = new Solver();
        IntVar[] vars = VariableFactory.enumeratedArray("X", 4, 0, 2, solver);
        Tuples tuples = new Tuples();
        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples);
        Assert.assertEquals(mdd.getDiagram(), new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        mdd = mdd.duplicate();
        Assert.assertEquals(mdd.getDiagram(), new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    }

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Solver solver = new Solver();
        IntVar[] vars = VariableFactory.enumeratedArray("X", 4, 0, 2, solver);
        Tuples tuples = new Tuples();
        tuples.add(0, 0, 0, 0);
        tuples.add(0, 0, 0, 1);
        tuples.add(0, 0, 1, 0);
        tuples.add(0, 0, 1, 1);
        tuples.add(0, 1, 0, 0);
        tuples.add(0, 1, 0, 1);
        tuples.add(0, 1, 1, 0);
        tuples.add(0, 1, 1, 1);
        tuples.add(2, 2, 2, 2);
        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples);
        Assert.assertEquals(mdd.getDiagram(), new int[]{3, 0, 12, 6, 6, 0, 9, 9, 0, -1, -1, 0, 0, 0, 15, 0, 0, 18, 0, 0, -1});
        mdd = mdd.duplicate();
        Assert.assertEquals(mdd.getDiagram(), new int[]{3, 0, 12, 6, 6, 0, 9, 9, 0, -1, -1, 0, 0, 0, 15, 0, 0, 18, 0, 0, -1});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Solver solver = new Solver();
        IntVar[] vars = VariableFactory.enumeratedArray("X", 3, 0, 1, solver);
        Tuples tuples = new Tuples();
        tuples.add(0, 0, 0);
        tuples.add(0, 0, 1);
        tuples.add(0, 1, 0);
        tuples.add(0, 1, 1);
        tuples.add(1, 0, 0);
        tuples.add(1, 0, 1);
        tuples.add(1, 1, 0);
        tuples.add(1, 1, 1);
        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples);
        Assert.assertEquals(mdd.getDiagram(), new int[]{2, 2, 4, 4, -1, -1});
        mdd = mdd.duplicate();
        Assert.assertEquals(mdd.getDiagram(), new int[]{2, 2, 4, 4, -1, -1});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Solver solver = new Solver();
        IntVar[] vars = new IntVar[2];
        vars[0] = VariableFactory.enumerated("X", -1, 0, solver);
        vars[1] = VariableFactory.enumerated("Y", new int[]{-1, 2}, solver);
        Tuples tuples = new Tuples();
        tuples.add(0, -1);
        tuples.add(-1, 2);
        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples);
        Assert.assertEquals(mdd.getDiagram(), new int[]{6, 2, -1, 0, 0, 0, 0, 0, 0, -1});
        mdd = mdd.duplicate();
        Assert.assertEquals(mdd.getDiagram(), new int[]{6, 2, -1, 0, 0, 0, 0, 0, 0, -1});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Solver solver = new Solver();
        IntVar[] vars = new IntVar[2];
        vars[0] = VariableFactory.enumerated("X", 0, 1, solver);
        vars[1] = VariableFactory.enumerated("Y", new int[]{-1, 1}, solver);
        Tuples tuples = new Tuples();
        tuples.add(0, -1);
        tuples.add(1, -1);
        tuples.add(0, 1);
        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples);
        Assert.assertEquals(mdd.getDiagram(), new int[]{2, 5, -1, 0, -1, -1, 0, 0});
        mdd = mdd.duplicate();
        Assert.assertEquals(mdd.getDiagram(), new int[]{2, 5, -1, 0, -1, -1, 0, 0});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test5() {
        Solver solver = new Solver();
        IntVar[] vars = new IntVar[3];
        vars[0] = VariableFactory.enumerated("V0", -1, 1, solver);
        vars[1] = VariableFactory.enumerated("V1", -1, 1, solver);
        vars[2] = VariableFactory.enumerated("V2", -1, 1, solver);
        Tuples tuples = new Tuples();
        tuples.add(0, -1, -1);
        tuples.add(-1, 0, -1);
        tuples.add(1, -1, 0);
        tuples.add(0, 0, 0);
        tuples.add(-1, 1, 0);
        tuples.add(1, 0, 1);
        tuples.add(0, 1, 1);

        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples, true, false);
        System.out.printf("%s\n", Arrays.toString(mdd.getDiagram()));
        Assert.assertEquals(mdd.getDiagram(), new int[]{6, 3, 12, 9, 15, 18, 0, 9, 15, -1, 0, 0, 15, 18, 0, 0, -1, 0, 0, 0, -1});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }

        mdd = new MultivaluedDecisionDiagram(vars, tuples, false, false);
        System.out.printf("%s\n", Arrays.toString(mdd.getDiagram()));
        Assert.assertEquals(mdd.getDiagram(), new int[]{6, 3, 12, 9, 15, 18, 0, 9, 15, -1, 0, 0, 15, 18, 0, 0, -1, 0, 0, 0, -1});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }

        mdd = new MultivaluedDecisionDiagram(vars, tuples, true, true);
        System.out.printf("%s\n", Arrays.toString(mdd.getDiagram()));
        Assert.assertEquals(mdd.getDiagram(), new int[]{3, 12, 18, 0, 6, 9, -1, 0, 0, 0, -1, 0, 6, 9, 15, 0, 0, -1, 9, 15, 0});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }

        mdd = new MultivaluedDecisionDiagram(vars, tuples, false, true);
        System.out.printf("%s\n", Arrays.toString(mdd.getDiagram()));
        Assert.assertEquals(mdd.getDiagram(), new int[]{3, 12, 18, 0, 6, 9, -1, 0, 0, 0, -1, 0, 6, 9, 15, 0, 0, -1, 9, 15, 0});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }
    }

}
