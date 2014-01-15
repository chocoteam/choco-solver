/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package parser.flatzinc.ast.constraints.global;

import org.slf4j.LoggerFactory;
import parser.flatzinc.ast.Datas;
import parser.flatzinc.ast.constraints.IBuilder;
import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.Expression;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.IntConstraintFactory;
import solver.constraints.extension.ExtensionalBinRelation;
import solver.constraints.extension.binary.CouplesTable;
import solver.constraints.extension.nary.IterTuplesTable;
import solver.constraints.extension.nary.LargeRelation;
import solver.exception.SolverException;
import solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/07/12
 */
public class TableBuilder implements IBuilder {
    @Override
    public Constraint[] build(Solver solver, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        // array[int] of var int: x, array[int, int] of int: t
        IntVar[] x = exps.get(0).toIntVarArray(solver);
        int[] f_t = exps.get(1).toIntArray();
        int d2 = x.length;
        int d1 = f_t.length / d2;
        List<int[]> t = new ArrayList<int[]>();
        for (int i = 0; i < d1; i++) {
            t.add(Arrays.copyOfRange(f_t, i * d2, (i + 1) * d2));
        }
        if (x.length == 2) {
            int[] min = new int[]{x[0].getLB(), x[1].getLB()};
            int[] max = new int[]{x[0].getUB(), x[1].getUB()};
            int n1 = max[0] - min[0] + 1;
            int n2 = max[1] - min[1] + 1;
            ExtensionalBinRelation relation;
            /*if (bitset) {
                relation = new CouplesBitSetTable(feas, min[0], min[1], n1, n2);
            } else */
            {
                relation = new CouplesTable(true, min[0], min[1], n1, n2);
            }
            for (int[] couple : t) {
                if (couple.length != 2) {
                    throw new SolverException("Wrong dimension : " + couple.length + " for a couple");
                }
                if (between(couple[0], min[0], max[0])
                        && between(couple[1], min[1], max[1])) {
                    relation.setCouple(couple[0], couple[1]);
                } else {
                    LoggerFactory.getLogger("fzn").warn("% {" + couple[0] + "," + couple[1] + "} will not be added, " +
                            "{0} is not inside [{1},{2}] or {3} is not inside [{4},{5}] ",
                            new int[]{couple[0], min[0], max[0], couple[1], min[1], max[1]});
                }
            }
            return new Constraint[]{ICF.table(x[0], x[1], relation)};
        } else {
            int[] o = new int[x.length];
            int[] d = new int[x.length];
            for (int i = 0; i < x.length; i++) {
                o[i] = x[i].getLB();
                d[i] = x[i].getUB() - o[i] + 1;
            }
            LargeRelation list_t = new IterTuplesTable(t, o, d);
            return new Constraint[]{IntConstraintFactory.table(x, list_t, "AC2001")};
        }
    }

    private static boolean between(int v, int low, int upp) {
        return (low <= v) && (v <= upp);
    }
}
