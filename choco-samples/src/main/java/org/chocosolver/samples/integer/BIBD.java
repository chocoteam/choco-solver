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
package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.ESat;
import org.kohsuke.args4j.Option;

import static java.lang.String.format;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.flatten;

/**
 * CSPLib prob028:<br/>
 * "A Balanced Incomplete Block Design (BIBD) is defined as an arrangement of
 * v distinct objects into b blocks such that
 * each block contains exactly k distinct objects,
 * each object occurs in exactly r different blocks,
 * and every two distinct objects occur together in exactly lambda blocks.
 * <br/>
 * Another way of defining a BIBD is in terms of its incidence matrix,
 * which is a v by b binary matrix with exactly r ones per row,
 * k ones per column,
 * and with a scalar product of lambda between any pair of distinct rows.
 * <br/>
 * A BIBD is therefore specified by its parameters (v,b,r,k,lambda)."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/08/11
 */
public class BIBD extends AbstractProblem {

    @Option(name = "-v", usage = "matrix first dimension.", required = false)
    private int v = 7;

    @Option(name = "-k", usage = "ones per column.", required = false)
    private int k = 3;

    @Option(name = "-p", usage = "scalar product.", required = false)
    private int l = 20;

    @Option(name = "-b", usage = "matrix second dimension.", required = false)
    private int b = -1;

    @Option(name = "-r", usage = "ones per row.", required = false)
    private int r = -1;


    BoolVar[][] vars, _vars;

    @Override
    public void buildModel() {
        model = new Model("BIBD");
        if (b == -1) {
            b = (v * (v - 1) * l) / (k * (k - 1));
        }
        if (r == -1) {
            r = (l * (v - 1)) / (k - 1);
        }
        vars = new BoolVar[v][b];
        _vars = new BoolVar[b][v];
        for (int i = 0; i < v; i++) {
            for (int j = 0; j < b; j++) {
                vars[i][j] = model.boolVar("V(" + i + "," + j + ")");
                _vars[j][i] = vars[i][j];
            }

        }
        // r ones per row
        for (int i = 0; i < v; i++) {
            model.sum(vars[i], "=", r).post();
        }
        // k ones per column
        for (int j = 0; j < b; j++) {
            model.sum(_vars[j], "=", k).post();
        }

        // Exactly l ones in scalar product between two different rows
        for (int i1 = 0; i1 < v; i1++) {
            for (int i2 = i1 + 1; i2 < v; i2++) {
                BoolVar[] score = model.boolVarArray(format("row(%d,%d)", i1, i2), b);
                for (int j = 0; j < b; j++) {
                    model.times(_vars[j][i1], _vars[j][i2], score[j]).post();
                }
                model.sum(score, "=", l).post();
            }
        }
        // Symmetry breaking
        BoolVar[][] rev = new BoolVar[v][];
        for (int i = 0; i < v; i++) {
            rev[i] = vars[v - 1 - i];
        }
        model.lexChainLessEq(rev).post();
        BoolVar[][] _rev = new BoolVar[b][];
        for (int i = 0; i < b; i++) {
            _rev[i] = _vars[b - 1 - i];
        }
        model.lexChainLessEq(_rev).post();
    }


    @Override
    public void configureSearch() {
        model.getSolver().set(inputOrderLBSearch(flatten(vars)));
    }

    @Override
    public void solve() {
        model.solve();
    }

    @Override
    public void prettyOut() {
        System.out.println(String.format("BIBD(%d,%d,%d,%d,%d)", v, b, r, k, l));
        StringBuilder st = new StringBuilder();
        if (model.getSolver().isFeasible() == ESat.TRUE) {
            for (int i = 0; i < v; i++) {
                st.append("\t");
                for (int j = 0; j < b; j++) {
                    st.append(_vars[j][i].getValue()).append(" ");
                }
                st.append("\n");
            }
        } else {
            st.append("\tINFEASIBLE");
        }
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new BIBD().execute(args);
    }
}
