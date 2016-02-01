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
package org.chocosolver.docs;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.VF;
import org.testng.annotations.Test;

/**
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 22/09/2014
 */
public class SatConstraintExamples {

    @Test(groups="1s", timeOut=60000)
    public void testattmostnminusone() {
        Solver solver = new Solver();
        BoolVar[] BVARS = VF.boolArray("BS", 4, solver);
        SatFactory.addAtMostNMinusOne(BVARS);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testattmostone() {
        Solver solver = new Solver();
        BoolVar[] BVARS = VF.boolArray("BS", 4, solver);
        SatFactory.addAtMostOne(BVARS);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolandarrayequalfalse() {
        Solver solver = new Solver();
        BoolVar[] BVARS = VF.boolArray("BS", 4, solver);
        SatFactory.addBoolAndArrayEqualFalse(BVARS);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolorarrayequaltrue() {
        Solver solver = new Solver();
        BoolVar[] BVARS = VF.boolArray("BS", 4, solver);
        SatFactory.addBoolOrArrayEqualTrue(BVARS);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolandeqvar() {
        Solver solver = new Solver();
        BoolVar L = VF.bool("L", solver);
        BoolVar R = VF.bool("R", solver);
        BoolVar T = VF.bool("T", solver);
        SatFactory.addBoolAndEqVar(L, R, T);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testbooleq() {
        Solver solver = new Solver();
        BoolVar L = VF.bool("L", solver);
        BoolVar R = VF.bool("R", solver);
        SatFactory.addBoolEq(L, R);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testbooliseqvar() {
        Solver solver = new Solver();
        BoolVar L = VF.bool("L", solver);
        BoolVar R = VF.bool("R", solver);
        BoolVar T = VF.bool("T", solver);
        SatFactory.addBoolIsEqVar(L, R, T);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolislevar() {
        Solver solver = new Solver();
        BoolVar L = VF.bool("L", solver);
        BoolVar R = VF.bool("R", solver);
        BoolVar T = VF.bool("T", solver);
        SatFactory.addBoolIsLeVar(L, R, T);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolisltvar() {
        Solver solver = new Solver();
        BoolVar L = VF.bool("L", solver);
        BoolVar R = VF.bool("R", solver);
        BoolVar T = VF.bool("T", solver);
        SatFactory.addBoolIsLtVar(L, R, T);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolisneqvar() {
        Solver solver = new Solver();
        BoolVar L = VF.bool("L", solver);
        BoolVar R = VF.bool("R", solver);
        BoolVar T = VF.bool("T", solver);
        SatFactory.addBoolIsNeqVar(L, R, T);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolle() {
        Solver solver = new Solver();
        BoolVar L = VF.bool("L", solver);
        BoolVar R = VF.bool("R", solver);
        SatFactory.addBoolLe(L, R);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboollt() {
        Solver solver = new Solver();
        BoolVar L = VF.bool("L", solver);
        BoolVar R = VF.bool("R", solver);
        SatFactory.addBoolLt(L, R);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolnot() {
        Solver solver = new Solver();
        BoolVar L = VF.bool("L", solver);
        BoolVar R = VF.bool("R", solver);
        SatFactory.addBoolNot(L, R);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolandarrayequalvar() {
        Solver solver = new Solver();
        BoolVar[] BVARS = VF.boolArray("BS", 4, solver);
        BoolVar T = VF.bool("T", solver);
        SatFactory.addBoolAndArrayEqVar(BVARS, T);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolorarrayequalvar() {
        Solver solver = new Solver();
        BoolVar[] BVARS = VF.boolArray("BS", 4, solver);
        BoolVar T = VF.bool("T", solver);
        SatFactory.addBoolOrArrayEqVar(BVARS, T);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testbooloreqvar() {
        Solver solver = new Solver();
        BoolVar L = VF.bool("L", solver);
        BoolVar R = VF.bool("R", solver);
        BoolVar T = VF.bool("T", solver);
        SatFactory.addBoolOrEqVar(L, R, T);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolxoreqvar() {
        Solver solver = new Solver();
        BoolVar L = VF.bool("L", solver);
        BoolVar R = VF.bool("R", solver);
        BoolVar T = VF.bool("T", solver);
        SatFactory.addBoolXorEqVar(L, R, T);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolclauseslop() {
        Solver solver = new Solver();
        BoolVar C1 = VF.bool("C1", solver);
        BoolVar C2 = VF.bool("C2", solver);
        BoolVar R = VF.bool("R", solver);
        BoolVar AR = VF.bool("AR", solver);
        SatFactory.addClauses(
                LogOp.ifThenElse(LogOp.nand(C1, C2), R, AR),
                solver);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolclauses() {
        Solver solver = new Solver();
        BoolVar P1 = VF.bool("P1", solver);
        BoolVar P2 = VF.bool("P2", solver);
        BoolVar P3 = VF.bool("P3", solver);
        BoolVar N = VF.bool("N", solver);
        SatFactory.addClauses(new BoolVar[]{P1, P2, P3}, new BoolVar[]{N});
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolfalse() {
        Solver solver = new Solver();
        BoolVar B = VF.bool("B", solver);
        SatFactory.addFalse(B);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testmaxboolarraylesseqvar() {
        Solver solver = new Solver();
        BoolVar[] BVARS = VF.boolArray("BS", 3, solver);
        BoolVar T = VF.bool("T", solver);
        SatFactory.addMaxBoolArrayLessEqVar(BVARS, T);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsumboolarraygreatereqvar() {
        Solver solver = new Solver();
        BoolVar[] BVARS = VF.boolArray("BS", 3, solver);
        BoolVar T = VF.bool("T", solver);
        SatFactory.addSumBoolArrayGreaterEqVar(BVARS, T);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsumboolarraylesseqvar() {
        Solver solver = new Solver();
        BoolVar[] BVARS = VF.boolArray("BS", 3, solver);
        BoolVar T = VF.bool("T", solver);
        SatFactory.addSumBoolArrayLessEqVar(BVARS, T);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testbooltrue() {
        Solver solver = new Solver();
        BoolVar B = VF.bool("B", solver);
        SatFactory.addTrue(B);
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }
}
