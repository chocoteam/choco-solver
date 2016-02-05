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

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import org.chocosolver.solver.propagation.hardcoded.TwoBucketPropagationEngine;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.util.Arrays.sort;
import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.constraints.PropagatorPriority.UNARY;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.random;
import static org.chocosolver.solver.variables.events.IEventType.ALL_EVENTS;
import static org.chocosolver.solver.variables.events.IntEventType.VOID;
import static org.chocosolver.util.ESat.TRUE;
import static org.testng.Assert.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/06/12
 */
public class PropEngineTest {

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Model model = new Model("t1");
        IntVar x = model.intVar("X", 1, 3, true);
        IntVar y = model.intVar("Y", 1, 3, true);
        model.arithm(x, ">=", y).post();
        model.arithm(x, "<=", 2).post();

        model.findSolution();
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Model model = new Model();
        IntVar[] VARS = model.intVarArray("X", 2, 0, 2, false);
        Constraint CSTR = model.arithm(VARS[0], "+", VARS[1], "=", 2);
        model.post(CSTR, CSTR);
        model.findAllSolutions();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 3);
        model.getResolver().reset();
        model.unpost(CSTR);
        model.findAllSolutions();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 9);
    }

    // test clone in propagators
    @Test(groups="1s", timeOut=60000, expectedExceptions = AssertionError.class)
    public void testClone() throws ContradictionException {
        Model model = new Model();
        model.set(new Settings() {
            @Override
            public boolean cloneVariableArrayInPropagator() {
                return false;
            }
        });
        IntVar[] vars = model.intVarArray("V", 3, 0, 4, false);
        model.allDifferent(vars).post();
        sort(vars, (o1, o2) -> o2.getId() - o1.getId());

        model.propagate();
        vars[0].instantiateTo(0, Null);
        model.propagate();
        assertFalse(vars[0].isInstantiatedTo(0));
    }

    public static void main(String[] args) {
        for(int i =1; i < 15; i++) {
            System.out.printf("%d -> %d \n", i, Integer.lowestOneBit(i));
            System.out.printf("%d -> %d \n", i, Integer.lowestOneBit(i>>2));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test3(){
        Model model = ProblemMaker.makeNQueenWithBinaryConstraints(8);
        model.set(new SevenQueuesPropagatorEngine(model));
        model.findAllSolutions();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 92);
    }

    @Test(groups="1s", timeOut=60000)
    public void test4(){
        Model model = ProblemMaker.makeNQueenWithBinaryConstraints(8);
        model.set(new TwoBucketPropagationEngine(model));
        model.findAllSolutions();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 92);
    }

    @Test(groups="10s", timeOut=60000)
    public void test5(){
        Model model = ProblemMaker.makeGolombRuler(10);
        model.set(new SevenQueuesPropagatorEngine(model));
        model.findOptimalSolution(ResolutionPolicy.MINIMIZE);
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(model.getSolutionRecorder().getLastSolution().getIntVal((IntVar) model.getObjectives()[0]).intValue(), 55);
    }

    @Test(groups="10s", timeOut=60000)
    public void test6(){
        Model model = ProblemMaker.makeGolombRuler(10);
        model.set(new TwoBucketPropagationEngine(model));
        model.findOptimalSolution(ResolutionPolicy.MINIMIZE);
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(model.getSolutionRecorder().getLastSolution().getIntVal((IntVar) model.getObjectives()[0]).intValue(), 55);
    }
    
    @Test(groups="1s", timeOut=60000)
    public void testGregy41(){
        for(int i = 0 ; i < 20; i++) {
            Model model = new Model("Propagation condition");
            IntVar[] X = model.intVarArray("X", 2, 0, 2, false);
            new Constraint("test", new Propagator(X, UNARY, true) {

                @Override
                public int getPropagationConditions(int vIdx) {
                    if (vIdx == 0) {
                        return VOID.getMask();
                    } else {
                        return ALL_EVENTS;
                    }
                }

                @Override
                public void propagate(int evtmask) throws ContradictionException {
                    // initial propagation
                }

                @Override
                public void propagate(int idxVarInProp, int mask) throws ContradictionException {
                    if (idxVarInProp == 0) {
                        fail();
                    }
                }

                @Override
                public ESat isEntailed() {
                    return TRUE;
                }
            }).post();
            model.set(random(X, i));
            model.findAllSolutions();
            assertEquals(model.getMeasures().getSolutionCount(), 9);
        }
    }
}
