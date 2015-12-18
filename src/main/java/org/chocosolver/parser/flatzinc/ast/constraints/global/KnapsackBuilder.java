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

package org.chocosolver.parser.flatzinc.ast.constraints.global;

import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.ast.constraints.IBuilder;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.BitSet;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/07/2015
 */
public class KnapsackBuilder implements IBuilder {

    @Override
    public void build(Solver solver, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        int[] w = exps.get(0).toIntArray();
        int[] p = exps.get(1).toIntArray();
        IntVar[] x = exps.get(2).toIntVarArray(solver);
        IntVar W = exps.get(3).intVarValue(solver);
        IntVar P = exps.get(4).intVarValue(solver);

        solver.post(ICF.scalar(x, w, W),ICF.scalar(x, p, P));
        solver.post(new Constraint("knapsack", new Propagator<IntVar>(ArrayUtils.append(x,new IntVar[]{W,P})) {

            private int[] order;
            private double[] ratio;

            @Override
            public void propagate(int evtmask) throws ContradictionException {
                // initial sort
                if(order == null){
                    order = new int[w.length];
                    ratio = new double[w.length];
                    for (int i = 0; i < w.length; i++) {
                        ratio[i] = (double) (p[i]) / (double) (w[i]);
                    }
                    BitSet in = new BitSet(w.length);
                    double best = -1;
                    int index = 0;
                    for (int i = 0; i < w.length; i++) {
                        int item = -1;
                        for (int o = in.nextClearBit(0); o < w.length; o = in.nextClearBit(o + 1)) {
                            if (item == -1 || w[i] == 0 || ratio[o] > best) {
                                best = ratio[o];
                                item = o;
                            }
                        }
                        in.set(item);
                        if (item == -1) {
                            throw new UnsupportedOperationException();
                        } else {
                            order[index++] = item;
                        }
                    }
                }
                // filtering algorithm
                int pomin = 0;
                int pomax = 0;
                int cmin = 0;
                int cmax = 0;
                for (int i = 0; i < w.length; i++) {
                    pomin += p[i] * vars[i].getLB();
                    pomax += p[i] * vars[i].getUB();
                    cmin += w[i] * vars[i].getLB();
                    cmax += w[i] * vars[i].getUB();
                }
                P.updateLowerBound(pomin, this);
                P.updateUpperBound(pomax, this);
                W.updateLowerBound(cmin, this);
                W.updateUpperBound(cmax, this);

                {
                    cmax = Math.min(cmax, W.getUB());
                    for(int idx:order) {
                        if (vars[idx].getUB() > vars[idx].getLB()) {
                            int deltaW = w[idx] * (vars[idx].getUB() - vars[idx].getLB());
                            if (cmin + deltaW <= cmax) {
                                pomin += p[idx] * (vars[idx].getUB() - vars[idx].getLB());
                                cmin += deltaW;
                            } else {
                                pomin += Math.ceil((cmax-cmin) * ratio[idx]);
                                break;
                            }
                        }
                    }
                    P.updateUpperBound(pomin, this);
                }
            }

            @Override
            public ESat isEntailed() {
                return ESat.TRUE;
            }
        }));
    }
}
