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
package org.chocosolver.choco.propagation.event;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.set.SCF;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.*;
import org.chocosolver.solver.variables.delta.EnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 sept. 2010
 */
public class DeltaTest {

    @Test(groups = "1s")
    public void testAdd() {
        Solver sol = new Solver();
        EnumDelta d = new EnumDelta(sol.getEnvironment());
        for (int i = 1; i < 40; i++) {
            d.add(i, Cause.Null);
            Assert.assertEquals(d.size(), i);
        }
    }

    @Test(groups = "1s")
    public void testEq() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = VariableFactory.enumerated("X", 1, 6, solver);
        IntVar y = VariableFactory.enumerated("Y", 1, 6, solver);

        solver.post(IntConstraintFactory.arithm(x, "=", y));

        solver.propagate();

        x.removeValue(4, Cause.Null);

        solver.propagate();

        Assert.assertFalse(y.contains(4));

    }

    @Test(groups = "1s")
    public void testJL() {
        Solver solver = new Solver();
        final SetVar s0 = VF.set("s0", 0, 1, solver);
        final BoolVar b0 = VF.bool("b0", solver);
        final BoolVar b1 = VF.bool("b1", solver);
        final IntVar i0 = VF.bool("i0", solver);
        solver.set(ISF.lexico_LB(i0));
        solver.post(SCF.bool_channel(new BoolVar[]{b0, b1}, s0, 0));
        solver.post(SCF.cardinality(s0, VF.fixed(0, solver)));

        solver.findSolution();
        solver.getSearchLoop().reset();
        solver.findSolution();
    }


    @Test(groups = "1s")
    public void testJL2() {
        for (int k = 0; k < 50; k++) {
            Solver s = new Solver();
            final IntVar i = VF.enumerated("i", -2, 2, s);
            final IntVar j = VF.enumerated("j", -2, 2, s);
            //Chatterbox.showDecisions(s);
            //Chatterbox.showSolutions(s);
            s.set(ISF.random_value(new IntVar[]{i, j}));
            s.post(new Constraint("Constraint", new PropTestDM1(i, j), new PropTestDM2(i, j)));
            s.findAllSolutions();
        }
    }

    @Test(groups = "1s")
    public void testJL3() {
        for (int k = 0; k < 10; k++) {
            Solver s = new Solver();
            final IntVar i = VF.bounded("i", -2, 2, s);
            final IntVar j = VF.bounded("j", -2, 2, s);
            //Chatterbox.showDecisions(s);
            //Chatterbox.showSolutions(s);
            s.set(ISF.random_bound(new IntVar[]{i, j}));
            s.post(new Constraint("Constraint", new PropTestDM1(i, j), new PropTestDM2(i, j)));
            s.findAllSolutions();
        }
    }

    @Test(groups = "1s")
    public void testJL4() {
        for (int k = 0; k < 10; k++) {
            Solver s = new Solver();
            final IntVar i = VF.bool("i", s);
            final IntVar j = VF.bool("j", s);
            //Chatterbox.showDecisions(s);
            //Chatterbox.showSolutions(s);
            s.set(ISF.random_value(new IntVar[]{i, j}));
            s.post(new Constraint("Constraint", new PropTestDM1(i, j), new PropTestDM2(i, j)));
            s.findAllSolutions();
        }
    }

    private static class PropTestDM1 extends Propagator<IntVar> {
        IntVar i, j;
        IIntDeltaMonitor iD;
        IIntDeltaMonitor jD;

        private PropTestDM1(IntVar i, IntVar j) {
            super(new IntVar[]{i, j}, PropagatorPriority.UNARY, true);
            this.i = i;
            this.j = j;
            iD = i.monitorDelta(this);
            jD = j.monitorDelta(this);
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            iD.unfreeze();
            jD.unfreeze();
        }

        @Override
        public void propagate(int idxVarInProp, int mask) throws ContradictionException {
            if (idxVarInProp == 0) {
                iD.freeze();
                iD.forEachRemVal((IntProcedure) x -> {
                    System.out.println("Delta monitor for variable says that the value " + x + " is removed from variable i");
                    if (i.contains(x)) {
                        throw new Error("Delta monitor lied to me.");
                    }
                });
                iD.unfreeze();
            } else {
                jD.freeze();
                jD.forEachRemVal((IntProcedure) x -> {
                    System.out.println("Delta monitor for variable says that the value " + x + " is removed from variable j");
                    if (j.contains(x)) {
                        throw new Error("Delta monitor lied to me.");
                    }
                });
                jD.unfreeze();
            }
        }

        @Override
        public ESat isEntailed() {
            return ESat.TRUE;
        }
    }

    private static class PropTestDM2 extends Propagator<IntVar> {
        IntVar i, j;

        private PropTestDM2(IntVar i, IntVar j) {
            super(new IntVar[]{i, j}, PropagatorPriority.UNARY, false);
            this.i = i;
            this.j = j;
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            if (j.isInstantiatedTo(1)) {
                i.removeValue(1, aCause);
            }
        }

        @Override
        public ESat isEntailed() {
            return ESat.TRUE;
        }

    }
}
