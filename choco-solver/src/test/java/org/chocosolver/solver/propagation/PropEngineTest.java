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
package org.chocosolver.solver.propagation;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import org.chocosolver.solver.propagation.hardcoded.TwoBucketPropagationEngine;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/06/12
 */
public class PropEngineTest {

    @Test(groups = "1s")
    public void test1() {
        Solver solver = new Solver("t1");
        IntVar x = VariableFactory.bounded("X", 1, 3, solver);
        IntVar y = VariableFactory.bounded("Y", 1, 3, solver);
        solver.post(IntConstraintFactory.arithm(x, ">=", y));
        solver.post(IntConstraintFactory.arithm(x, "<=", 2));

        solver.findSolution();
    }

    @Test(groups = "1s")
    public void test2() {
        Solver solver = new Solver();
        IntVar[] VARS = VF.enumeratedArray("X", 2, 0, 2, solver);
        Constraint CSTR = ICF.arithm(VARS[0], "+", VARS[1], "=", 2);
        solver.post(CSTR, CSTR);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 3);
        solver.getSearchLoop().reset();
        solver.unpost(CSTR);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 9);
    }

    // test clone in propagators
    @Test(groups = "1s", expectedExceptions = AssertionError.class)
    public void testClone() throws ContradictionException {
        Solver solver = new Solver();
        solver.set(new Settings() {
            @Override
            public boolean cloneVariableArrayInPropagator() {
                return false;
            }
        });
        IntVar[] vars = VF.enumeratedArray("V", 3, 0, 4, solver);
        solver.post(ICF.alldifferent(vars));
        Arrays.sort(vars, (o1, o2) -> o2.getId() - o1.getId());

        solver.propagate();
        vars[0].instantiateTo(0, Cause.Null);
        solver.propagate();
        Assert.assertFalse(vars[0].isInstantiatedTo(0));
    }

    public static void main(String[] args) {
        for(int i =1; i < 15; i++) {
            System.out.printf("%d -> %d \n", i, Integer.lowestOneBit(i));
            System.out.printf("%d -> %d \n", i, Integer.lowestOneBit(i>>2));
        }
    }

    @Test(groups="1s")
    public void test3(){
        Solver solver = ProblemMaker.makeNQueenWithBinaryConstraints(8);
        solver.set(new SevenQueuesPropagatorEngine(solver));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 92);
    }

    @Test(groups="1s")
    public void test4(){
        Solver solver = ProblemMaker.makeNQueenWithBinaryConstraints(8);
        solver.set(new TwoBucketPropagationEngine(solver));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 92);
    }

    @Test(groups="1s")
    public void test5(){
        Solver solver = ProblemMaker.makeGolombRuler(10);
        solver.set(new SevenQueuesPropagatorEngine(solver));
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(solver.getSolutionRecorder().getLastSolution().getIntVal((IntVar)solver.getObjectives()[0]).intValue(), 55);
    }

    @Test(groups="1s")
    public void test6(){
        Solver solver = ProblemMaker.makeGolombRuler(10);
        solver.set(new TwoBucketPropagationEngine(solver));
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(solver.getSolutionRecorder().getLastSolution().getIntVal((IntVar)solver.getObjectives()[0]).intValue(), 55);
    }
}
