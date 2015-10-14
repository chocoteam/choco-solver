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
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.set.SCF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VF;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/03/2014
 */
public class ConstraintTest {

    @Test(groups = "1s")
    public void testBooleanChannelingJL() {
        //#issue 190
        Solver solver = new Solver();
        BoolVar[] bs = VF.boolArray("bs", 3, solver);
        SetVar s1 = VF.set("s1", -3, 3, solver);
        SetVar s2 = VF.set("s2", -3, 3, solver);
        solver.post(LCF.or(SCF.all_equal(new SetVar[]{s1, s2}), SCF.bool_channel(bs, s1, 0)));
        solver.findAllSolutions();
        Assert.assertEquals(2040, solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void testDependencyConditions() {
        Solver solver = new Solver();
        IntVar[] ivs = VF.enumeratedArray("X", 4, 0, 10, solver);
        solver.post(ICF.alldifferent(ivs, "BC")); // boundAndInst()
        solver.post(ICF.arithm(ivs[0], "+", ivs[1], "=", 4)); // all()
        solver.post(ICF.arithm(ivs[0], ">=", ivs[2])); // INST + UB or INST + LB
        solver.post(ICF.arithm(ivs[0], "!=", ivs[3])); // instantiation()

        solver.set(ISF.random_value(ivs, 0));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 48);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 100);
    }

    @Test(groups = "1s")
    public void testDependencyConditions2() {
        Solver solver = new Solver();
        IntVar[] ivs = VF.enumeratedArray("X", 4, 0, 10, solver);
        solver.post(ICF.alldifferent(ivs, "BC")); // boundAndInst()
        solver.post(ICF.arithm(ivs[0], "+", ivs[1], "=", 4)); // all()
        Constraint cr = ICF.arithm(ivs[0], ">=", ivs[2]);
        solver.post(cr); // INST + UB or INST + LB
        solver.post(ICF.arithm(ivs[0], "!=", ivs[3])); // instantiation()
        solver.unpost(cr);
    }

}
