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
package org.chocosolver.solver.constraints.nary.allen;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;

/**
 * A constraint for ALLEN relations between a set of tasks (variables) and a set of intervals (fixed) wrt a set of relations.
 * Created by cprudhom on 18/02/15.
 * Project: allen.
 * @author Alban Derrien, Thierry Petit, Charles Prud'homme, Mats Carlsson
 */
public class AllenTaskRel extends Constraint {

    public AllenTaskRel(IntVar[] vars, int[] fixed, String[] rel) {
        super("Allen", new PropAllenTaskRel(vars, fixed, rel));
    }

    public static void decomposition(IntVar[] vars, int[] fixed, String[] rel) {
        Model model = vars[0].getModel();
        int n, m;
        int[] i_sta = Arrays.copyOfRange(fixed, 0, fixed.length / 2);
        int[] i_end = Arrays.copyOfRange(fixed, fixed.length / 2, fixed.length);
        n = i_sta.length;

        IntVar[] t_sta = Arrays.copyOfRange(vars, 0, vars.length / 3);
        IntVar[] t_dur = Arrays.copyOfRange(vars, vars.length / 3, 2 * (vars.length / 3));
        IntVar[] t_end = Arrays.copyOfRange(vars, 2 * (vars.length / 3), vars.length);
        m = t_sta.length;

        for (int t = 0; t < m; t++) {
            if (t > 0) {
                model.post(model.arithm(t_end[t - 1], "<=", t_sta[t]));
            }


            int k = 0;
            BoolVar[] bvars = model.boolVarArray("R_" + t, n * rel.length);
            model.addClausesBoolOrArrayEqualTrue(bvars);
            for (String r : rel) {
                for (int i = 0; i < n; i++) {
                    switch (r) {
                        case "p":
                            model.arithm(t_end[t], "<", i_sta[i]).reifyWith(bvars[k++]);
                            break;
                        case "m":
                            model.arithm(t_end[t], "=", i_sta[i]).reifyWith(bvars[k++]);
                            break;
                        case "o":
                            if (i_end[i] - i_sta[i] > 1) {
                                BoolVar[] or = model.boolVarArray("R_o" + t, 4);
                                model.arithm(t_end[t], "<", i_end[i]).reifyWith(or[0]);
                                model.arithm(t_end[t], ">", i_sta[i]).reifyWith(or[1]);
                                model.arithm(t_sta[t], "<", i_sta[i]).reifyWith(or[2]);
                                model.arithm(t_dur[t], ">", 1).reifyWith(or[3]);
                                model.addClausesBoolAndArrayEqVar(or, bvars[k++]);
                            } else {
                                model.addClauseFalse(bvars[k++]);
                            }
                            break;
                        case "s":
                            BoolVar[] sr = model.boolVarArray("R_s" + t, 3);

                            model.arithm(t_sta[t], "=", i_sta[i]).reifyWith(sr[0]);
                            model.arithm(t_dur[t], "<", i_end[i] - i_sta[i]).reifyWith(sr[1]);
                            model.arithm(t_end[t], "<", i_end[i]).reifyWith(sr[2]);

                            model.addClausesBoolAndArrayEqVar(sr, bvars[k++]);
                            break;
                        case "d":
                            BoolVar[] dr = model.boolVarArray("R_d" + t, 3);

                            model.arithm(t_sta[t], ">", i_sta[i]).reifyWith(dr[0]);
                            model.arithm(t_end[t], "<", i_end[i]).reifyWith(dr[1]);
                            model.arithm(t_dur[t], "<", i_end[i] - i_sta[i] - 1).reifyWith(dr[2]);

                            model.addClausesBoolAndArrayEqVar(dr, bvars[k++]);
                            break;
                        case "f":
                            BoolVar[] fr = model.boolVarArray("R_f" + t, 3);

                            model.arithm(t_sta[t], ">", i_sta[i]).reifyWith(fr[0]);
                            model.arithm(t_end[t], "=", i_end[i]).reifyWith(fr[1]);
                            model.arithm(t_dur[t], "<", i_end[i] - i_sta[i]).reifyWith(fr[2]);

                            model.addClausesBoolAndArrayEqVar(fr, bvars[k++]);
                            break;
                        case "pi":
                            model.arithm(t_sta[t], ">", i_end[i]).reifyWith(bvars[k++]);
                            break;
                        case "mi":
                            model.arithm(t_sta[t], "=", i_end[i]).reifyWith(bvars[k++]);
                            break;
                        case "oi":
                            if (i_end[i] - i_sta[i] > 1) {
                                BoolVar[] or = model.boolVarArray("R_oi" + t, 4);
                                model.arithm(t_sta[t], ">", i_sta[i]).reifyWith(or[0]);
                                model.arithm(t_sta[t], "<", i_end[i]).reifyWith(or[1]);
                                model.arithm(t_end[t], ">", i_end[i]).reifyWith(or[2]);
                                model.arithm(t_dur[t], ">", 1).reifyWith(or[3]);
                                model.addClausesBoolAndArrayEqVar(or, bvars[k++]);
                            } else {
                                model.addClauseFalse(bvars[k++]);
                            }
                            break;
                        case "si":
                            BoolVar[] sir = model.boolVarArray("R_si" + t, 3);

                            model.arithm(t_sta[t], "=", i_sta[i]).reifyWith(sir[0]);
                            model.arithm(t_dur[t], ">", i_end[i] - i_sta[i]).reifyWith(sir[1]);
                            model.arithm(t_end[t], ">", i_end[i]).reifyWith(sir[2]);

                            model.addClausesBoolAndArrayEqVar(sir, bvars[k++]);
                            break;
                        case "di":
                            BoolVar[] dir = model.boolVarArray("R_di" + t, 3);

                            model.arithm(t_sta[t], "<", i_sta[i]).reifyWith(dir[0]);
                            model.arithm(t_end[t], ">", i_end[i]).reifyWith(dir[1]);
                            model.arithm(t_dur[t], ">", i_end[i] - i_sta[i] + 1).reifyWith(dir[2]);

                            model.addClausesBoolAndArrayEqVar(dir, bvars[k++]);
                            break;
                        case "fi":
                            BoolVar[] fir = model.boolVarArray("R_fi" + t, 3);

                            model.arithm(t_end[t], "=", i_end[i]).reifyWith(fir[0]);
                            model.arithm(t_sta[t], "<", i_sta[i]).reifyWith(fir[1]);
                            model.arithm(t_dur[t], ">", i_end[i] - i_sta[i]).reifyWith(fir[2]);

                            model.addClausesBoolAndArrayEqVar(fir, bvars[k++]);
                            break;
                        case "eq":
                            BoolVar[] eqr = model.boolVarArray("R_eq" + t, 3);

                            model.arithm(t_sta[t], "=", i_sta[i]).reifyWith(eqr[0]);
                            model.arithm(t_end[t], "=", i_end[i]).reifyWith(eqr[1]);
                            model.arithm(t_dur[t], "=", i_end[i] - i_sta[i]).reifyWith(eqr[2]);

                            model.addClausesBoolAndArrayEqVar(eqr, bvars[k++]);
                            break;
                        default:
                            throw new UnsupportedOperationException("Unknown relation: " + r);
                    }
                }
            }
            assert k == bvars.length;
        }

    }

    public static void gac(IntVar[] vars, int[] fixed, String[] rel, boolean global) {
        Model model = vars[0].getModel();
        int n, m;
        int[] i_sta = Arrays.copyOfRange(fixed, 0, fixed.length / 2);
        int[] i_end = Arrays.copyOfRange(fixed, fixed.length / 2, fixed.length);
        n = i_sta.length;

        IntVar[] t_sta = Arrays.copyOfRange(vars, 0, vars.length / 3);
        IntVar[] t_dur = Arrays.copyOfRange(vars, vars.length / 3, 2 * (vars.length / 3));
        IntVar[] t_end = Arrays.copyOfRange(vars, 2 * (vars.length / 3), vars.length);
        m = t_sta.length;

        for (int t = 0; t < m; t++) {
            if (t > 0) {
                model.post(model.arithm(t_end[t - 1], "<=", t_sta[t]));
            }
            Constraint[] disjunctions = new Constraint[n];
            for (int i = 0; i < n; i++) {
                IntVar relV = Allen.buildDomainFromRelation(rel, model);
                disjunctions[i] = new Constraint(String.format("T%d %s T%d", t, Arrays.toString(rel), n),
                        new PropAllenGAC(relV,
                                t_sta[t], t_dur[t],
                                model.intVar(i_sta[i]), model.intVar(i_end[i] - i_sta[i]),
                                false));
            }
            model.addConstructiveDisjunction(global, disjunctions);
        }
    }
}
