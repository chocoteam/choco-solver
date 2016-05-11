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
package org.chocosolver.samples.todo.docs;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.nary.cnf.LogOp.ifThenElse;
import static org.chocosolver.solver.constraints.nary.cnf.LogOp.nand;

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
        model.addClausesAtMostNMinusOne(BVARS);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testattmostone() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 4);
        model.addClausesAtMostOne(BVARS);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolandarrayequalfalse() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 4);
        model.addClausesBoolAndArrayEqualFalse(BVARS);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolorarrayequaltrue() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 4);
        model.addClausesBoolOrArrayEqualTrue(BVARS);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolandeqvar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        model.addClausesBoolAndEqVar(L, R, T);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testbooleq() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        model.addClausesBoolEq(L, R);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testbooliseqvar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        model.addClausesBoolIsEqVar(L, R, T);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolislevar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        model.addClausesBoolIsLeVar(L, R, T);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolisltvar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        model.addClausesBoolIsLtVar(L, R, T);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolisneqvar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        model.addClausesBoolIsNeqVar(L, R, T);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolle() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        model.addClausesBoolLe(L, R);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboollt() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        model.addClausesBoolLt(L, R);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolnot() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        model.addClausesBoolNot(L, R);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolandarrayequalvar() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 4);
        BoolVar T = model.boolVar("T");
        model.addClausesBoolAndArrayEqVar(BVARS, T);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolorarrayequalvar() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 4);
        BoolVar T = model.boolVar("T");
        model.addClausesBoolOrArrayEqVar(BVARS, T);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testbooloreqvar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        model.addClausesBoolOrEqVar(L, R, T);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolxoreqvar() {
        Model model = new Model();
        BoolVar L = model.boolVar("L");
        BoolVar R = model.boolVar("R");
        BoolVar T = model.boolVar("T");
        model.addClausesBoolXorEqVar(L, R, T);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolclauseslop() {
        Model model = new Model();
        BoolVar C1 = model.boolVar("C1");
        BoolVar C2 = model.boolVar("C2");
        BoolVar R = model.boolVar("R");
        BoolVar AR = model.boolVar("AR");
        model.addClauses(ifThenElse(nand(C1, C2), R, AR));
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolclauses() {
        Model model = new Model();
        BoolVar P1 = model.boolVar("P1");
        BoolVar P2 = model.boolVar("P2");
        BoolVar P3 = model.boolVar("P3");
        BoolVar N = model.boolVar("N");
        model.addClauses(new BoolVar[]{P1, P2, P3}, new BoolVar[]{N});
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolfalse() {
        Model model = new Model();
        BoolVar B = model.boolVar("B");
        model.addClauseFalse(B);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testmaxboolarraylesseqvar() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 3);
        BoolVar T = model.boolVar("T");
        model.addClausesMaxBoolArrayLessEqVar(BVARS, T);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testsumboolarraygreatereqvar() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 3);
        BoolVar T = model.boolVar("T");
        model.addClausesSumBoolArrayGreaterEqVar(BVARS, T);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testsumboolarraylesseqvar() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BS", 3);
        BoolVar T = model.boolVar("T");
        model.addClausesSumBoolArrayLessEqVar(BVARS, T);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testbooltrue() {
        Model model = new Model();
        BoolVar B = model.boolVar("B");
        model.addClauseTrue(B);
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) ;
    }
}
