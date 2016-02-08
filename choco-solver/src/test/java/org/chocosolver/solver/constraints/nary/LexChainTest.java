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
package org.chocosolver.solver.constraints.nary;

/**
 * Created by IntelliJ IDEA.
 * User: Ashish
 * Date: Jun 26, 2008
 * Time: 1:31:37 PM
 * LexChain test file
 */

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.constraints.nary.cnf.ILogical;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.annotations.Test;

import java.util.Random;

import static java.lang.String.format;
import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.util.ESat.TRUE;
import static org.chocosolver.util.tools.ArrayUtils.flatten;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class LexChainTest {

    @Test(groups="10s", timeOut=60000)
    public void lexChainTest1() {
        Model s = new Model();

        IntVar[] ar1 = s.intVarArray("v1", 3, 0, 10, true);
        IntVar[] ar2 = s.intVarArray("v2", 3, -1, 9, true);

        Constraint c = s.lexChainLessEq(ar1, ar2);
        c.post();
        //SearchMonitorFactory.log(s, true, true);
        while (s.solve()) {
            assertEquals(TRUE, c.isSatisfied());
        }
    }


    private ILogical reformulate(int i, IntVar[] X, IntVar[] Y, Model model) {
        BoolVar b1 = model.boolVar("A" + i);
        model.ifThenElse(b1, model.arithm(Y[i], ">", X[i]), model.arithm(Y[i], "<=", X[i]));
        if (i == X.length - 1) {
            return b1;
        } else {
            BoolVar b2 = model.boolVar("B" + i);
            model.ifThenElse(b2, model.arithm(Y[i], "=", X[i]), model.arithm(X[i], "!=", Y[i]));
            return LogOp.or(b1, LogOp.and(b2, reformulate(i + 1, X, Y, model)));
        }
    }

    private Model reformulate(int n, int m, int k, int seed, boolean bounded) {
        Model model = new Model();
        IntVar[][] X = new IntVar[n][m];
        for (int i = 0; i < n; i++) {
            X[i] = bounded ?
                    model.intVarArray("X_" + i, m, 0, k, true) :
                    model.intVarArray("X_" + i, m, 0, k, false);
        }
        ILogical[] trees = new ILogical[n - 1];
        for (int i = 0; i < n - 1; i++) {
            trees[i] = reformulate(0, X[i], X[i + 1], model);
            //refor.post(new SatConstraint(reformulate(0, X[i], X[i + 1], refor), refor));
        }

        SatFactory.addClauses(LogOp.and(trees), model);
        model.getResolver().set(model.getResolver().randomSearch(ArrayUtils.flatten(X), seed));
        return model;
    }

    private Model lex(int n, int m, int k, int seed, boolean bounded) {
        Model model = new Model();
        IntVar[][] X = new IntVar[n][m];
        for (int i = 0; i < n; i++) {
            X[i] = bounded ?
                    model.intVarArray("X_" + i, m, 0, k, true) :
                    model.intVarArray("X_" + i, m, 0, k, false);
        }
        model.lexChainLess(X).post();
        model.getResolver().set(model.getResolver().randomSearch(flatten(X),seed));
        return model;
    }

    @Test(groups="5m", timeOut=300000)
    public void testE() {
        Random random = new Random();
        for (int seed = 0; seed < 1000; seed++) {
            random.setSeed(seed);
            int n = 2 + random.nextInt(2);
            int m = 2 + random.nextInt(2);
            int k = 1 + random.nextInt(2);

            Model refor = reformulate(n, m, k, seed, false);
            Model lex = lex(n, m, k, seed, false);

            while (refor.solve()) ;
            while (lex.solve()) ;

            assertEquals(refor.getResolver().getMeasures().getSolutionCount(), lex.getResolver().getMeasures().getSolutionCount(), format("seed:%d", seed));
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void testB() {
        Random random = new Random();
        for (int seed = 0; seed < 1000; seed++) {
            random.setSeed(seed);
            int n = 2 + random.nextInt(2);
            int m = 2 + random.nextInt(2);
            int k = 1 + random.nextInt(2);

            Model refor = reformulate(n, m, k, seed, true);
            Model lex = lex(n, m, k, seed, true);

            while (refor.solve()) ;
            while (lex.solve()) ;

            assertEquals(refor.getResolver().getMeasures().getSolutionCount(), lex.getResolver().getMeasures().getSolutionCount(), format("seed:%d", seed));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testB1() {
        int n = 3, m = 2, k = 2, seed = 47;
        Model refor = reformulate(n, m, k, seed, true);
        Model lex = lex(n, m, k, seed, true);
        while (refor.solve()) ;
        while (lex.solve()) ;
        assertEquals(refor.getResolver().getMeasures().getSolutionCount(), lex.getResolver().getMeasures().getSolutionCount(), format("seed:%d", seed));
    }

    @Test(groups="1s", timeOut=60000)
    public void testB2() {
        Model model = new Model();
        IntVar[][] X = new IntVar[3][2];
        for (int i = 0; i < 3; i++) {
            X[i] = model.intVarArray("X_" + i, 2, 0, 2, true);
        }

        model.lexChainLess(X).post();


        try {
            model.getResolver().propagate();
            X[0][0].updateLowerBound(1, Null);
            X[0][1].updateLowerBound(1, Null);
            X[1][0].updateLowerBound(1, Null);
            X[2][1].updateLowerBound(1, Null);
            model.getResolver().propagate();
            X[2][1].instantiateTo(1, Null);
            model.getResolver().propagate();
        } catch (ContradictionException e) {
            fail();
        }
    }

}


