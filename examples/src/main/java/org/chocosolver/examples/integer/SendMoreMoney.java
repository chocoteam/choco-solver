/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer; /**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;


/**
 * <b>The famous SEND + MORE = MONEY problem.</b></br>
 * The Send More Money Problem consists in finding distinct digits for the letters D, E, M, N, O, R, S, Y
 * such that S and M are different from zero (no leading zeros) and the equation SEND + MORE = MONEY is satisfied.
 *
 */
public class SendMoreMoney extends AbstractProblem {

    IntVar S, E, N, D, M, O, R, Y;
    IntVar[] ALL;


    @Override
    public void buildModel() {
        model = new Model();
        S = model.intVar("S", 0, 9, false);
        E = model.intVar("E", 0, 9, false);
        N = model.intVar("N", 0, 9, false);
        D = model.intVar("D", 0, 9, false);
        M = model.intVar("M", 0, 9, false);
        O = model.intVar("O", 0, 9, false);
        R = model.intVar("R", 0, 9, false);
        Y = model.intVar("Y", 0, 9, false);

        model.arithm(S, "!=", 0).post();
        model.arithm(M, "!=", 0).post();
        model.allDifferent(new IntVar[]{S, E, N, D, M, O, R, Y}, "BC").post();

        ALL = new IntVar[]{
                S, E, N, D,
                M, O, R, E,
                M, O, N, E, Y};
        int[] COEFFS = new int[]{
                1000, 100, 10, 1,
                1000, 100, 10, 1,
                -10000, -1000, -100, -10, -1
        };
//        model.scalar(ALL, COEFFS, "=", 0).post();
//        ArExpression SEND = S.mul(1000).add(E.mul(100)).add(N.mul(10)).add(D);
//        ArExpression MORE = M.mul(1000).add(O.mul(100)).add(R.mul(10)).add(E);
//        ArExpression MONEY = M.mul(10000).add(O.mul(1000)).add(N.mul(100)).add(E.mul(10)).add(Y);
//        SEND.add(MORE).eq(MONEY).decompose().post();
//
        IntVar[] r = model.boolVarArray(3);
        D.add(E).eq(Y.add(r[0].mul(10))).post();
        r[0].add(N).add(R).eq(E.add(r[1].mul(10))).post();
        r[1].add(E).add(O).eq(N.add(r[2].mul(10))).post();
        r[2].add(S).add(M).eq(O.add(M.mul(10))).post();

    }

    @Override
    public void configureSearch() {
    }

    @Override
    public void solve() {
        model.getSolver().setSearch(Search.minDomLBSearch(S, E, N, D, M, O, R, Y));
        model.getSolver().showStatistics();
        model.getSolver().showSolutions();
        while (model.getSolver().solve()) {
//            System.out.printf("%s = %d\n", S.getName(), S.getValue());
//            System.out.printf("%s = %d\n", E.getName(), E.getValue());
//        }
//            StringBuilder st = new StringBuilder();
//            st.append("\t");
//            for (int i = 0; i < ALL.length; i++) {
//                st.append(String.format("%s : %d\n\t", ALL[i].getName(), ALL[i].getValue()));
//            }
//            System.out.println(st.toString());
        }
    }

    public static void main(String[] args) {
        new SendMoreMoney().execute(args);
    }

}


