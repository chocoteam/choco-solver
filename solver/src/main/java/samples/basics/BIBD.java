/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package samples.basics;

import common.ESat;
import common.util.tools.ArrayUtils;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

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

    @Option(name = "-l", usage = "scalar product.", required = false)
    private int l = 20;

    @Option(name = "-b", usage = "matrix second dimension.", required = false)
    private int b = -1;

    @Option(name = "-r", usage = "ones per row.", required = false)
    private int r = -1;


    BoolVar[][] vars, _vars;

    @Override
    public void createSolver() {
        solver = new Solver("BIBD");
    }

    @Override
    public void buildModel() {
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
                vars[i][j] = VariableFactory.bool("V(" + i + "," + j + ")", solver);
                _vars[j][i] = vars[i][j];
            }

        }
        // r ones per row
        IntVar R = VariableFactory.fixed(r, solver);
        for (int i = 0; i < v; i++) {
            solver.post(IntConstraintFactory.sum(vars[i], R));
        }
        // k ones per column
        IntVar K = VariableFactory.fixed(k, solver);
        for (int j = 0; j < b; j++) {
            solver.post(IntConstraintFactory.sum(_vars[j], K));
        }

        // Exactly l ones in scalar product between two different rows
        IntVar L = VariableFactory.fixed(l, solver);
        for (int i1 = 0; i1 < v; i1++) {
            for (int i2 = i1 + 1; i2 < v; i2++) {
                BoolVar[] score = VariableFactory.boolArray(String.format("row(%d,%d)", i1, i2), b, solver);
                for (int j = 0; j < b; j++) {
                    solver.post(IntConstraintFactory.times(_vars[j][i1], _vars[j][i2], score[j]));
                }
                solver.post(IntConstraintFactory.sum(score, L));
            }
        }
        // Symmetry breaking
		BoolVar[][] rev = new BoolVar[v][];
		for (int i = 0; i < v; i++) {
			rev[i] = vars[v-1-i];
		}
		solver.post(IntConstraintFactory.lex_chain_less_eq(rev));
		BoolVar[][] _rev = new BoolVar[b][];
		for (int i = 0; i < b; i++) {
			_rev[i] = _vars[b-1-i];
		}
		solver.post(IntConstraintFactory.lex_chain_less_eq(_rev));
    }


    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.inputOrder_InDomainMin(ArrayUtils.flatten(vars)));
    }

    @Override
    public void configureEngine() {}

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("BIBD({},{},{},{},{})", new Object[]{v, b, r, k, l});
        StringBuilder st = new StringBuilder();
        if (solver.isFeasible() == ESat.TRUE) {
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
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new BIBD().execute(args);
    }
}
