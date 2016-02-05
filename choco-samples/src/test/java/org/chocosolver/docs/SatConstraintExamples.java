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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
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
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 4);
        SatFactory.addAtMostNMinusOne(BVARS);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testattmostone() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 4);
        SatFactory.addAtMostOne(BVARS);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolandarrayequalfalse() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 4);
        SatFactory.addBoolAndArrayEqualFalse(BVARS);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolorarrayequaltrue() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 4);
        SatFactory.addBoolOrArrayEqualTrue(BVARS);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolandeqvar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        SatFactory.addBoolAndEqVar(L, R, T);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testbooleq() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        SatFactory.addBoolEq(L, R);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testbooliseqvar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        SatFactory.addBoolIsEqVar(L, R, T);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolislevar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        SatFactory.addBoolIsLeVar(L, R, T);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolisltvar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        SatFactory.addBoolIsLtVar(L, R, T);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolisneqvar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        SatFactory.addBoolIsNeqVar(L, R, T);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolle() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        SatFactory.addBoolLe(L, R);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboollt() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        SatFactory.addBoolLt(L, R);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolnot() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        SatFactory.addBoolNot(L, R);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolandarrayequalvar() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 4);
        BoolVar T = model.boolVar("T");
        SatFactory.addBoolAndArrayEqVar(BVARS, T);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolorarrayequalvar() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 4);
        BoolVar T = model.boolVar("T");
        SatFactory.addBoolOrArrayEqVar(BVARS, T);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testbooloreqvar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        SatFactory.addBoolOrEqVar(L, R, T);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolxoreqvar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        SatFactory.addBoolXorEqVar(L, R, T);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolclauseslop() {
        Model model = new Model();
        BoolVar C1 = model.boolVar("C1");
        BoolVar C2 = model.boolVar("C2");
        BoolVar R = model.boolVar("R");
        BoolVar AR = model.boolVar("AR");
        SatFactory.addClauses(
                LogOp.ifThenElse(LogOp.nand(C1, C2), R, AR),
                model);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolclauses() {
        Model model = new Model();
        BoolVar P1 = model.boolVar("P1");
        BoolVar P2 = model.boolVar("P2");
        BoolVar P3 = model.boolVar("P3");
        BoolVar N = model.boolVar("N");
        SatFactory.addClauses(new BoolVar[]{P1, P2, P3}, new BoolVar[]{N});
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolfalse() {
        Model model = new Model();
        BoolVar B = model.boolVar("B");
        SatFactory.addFalse(B);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testmaxboolarraylesseqvar() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 3);
        BoolVar T = model.boolVar("T");
        SatFactory.addMaxBoolArrayLessEqVar(BVARS, T);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsumboolarraygreatereqvar() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 3);
        BoolVar T = model.boolVar("T");
        SatFactory.addSumBoolArrayGreaterEqVar(BVARS, T);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsumboolarraylesseqvar() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 3);
        BoolVar T = model.boolVar("T");
        SatFactory.addSumBoolArrayLessEqVar(BVARS, T);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testbooltrue() {
        Model model = new Model();
        BoolVar B = model.boolVar("B");
        SatFactory.addTrue(B);
        Chatterbox.showSolutions(model);
        model.findAllSolutions();
    }
}
