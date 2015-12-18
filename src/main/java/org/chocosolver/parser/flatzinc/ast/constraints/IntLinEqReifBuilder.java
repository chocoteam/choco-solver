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

package org.chocosolver.parser.flatzinc.ast.constraints;

import org.chocosolver.parser.flatzinc.FznSettings;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.sum.IntLinCombFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.StringUtils;

import java.util.List;

/**
 * (&#8721; i &#8712; 1..n: as[i].bs[i] = c) &#8660; r where n is the common length of as and bs
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 26/01/11
 */
public class IntLinEqReifBuilder implements IBuilder {

    @Override
    public void build(Solver solver, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        int[] as = exps.get(0).toIntArray();
        IntVar[] bs = exps.get(1).toIntVarArray(solver);
        IntVar c = exps.get(2).intVarValue(solver);
        BoolVar r = exps.get(3).boolVarValue(solver);

        if (bs.length > 0) {
            if (((FznSettings) solver.getSettings()).adhocReification()) {
                // detect boolSumEq bool reified
                int n = bs.length;
                boolean boolSum = c.isBool();
                for (int i = 0; i < n; i++) {
                    boolSum &= bs[i].isBool();
                    boolSum &= as[i] == 1;
                }
                if (boolSum && c.isInstantiatedTo(0)) {
                    BoolVar[] bbs = new BoolVar[n + 1];
                    for (int i = 0; i < n; i++) {
                        bbs[i] = (BoolVar) bs[i];
                    }
                    bbs[bs.length] = r;
                    solver.post(new Constraint("BoolSumLeq0Reif", new PropBoolSumEq0Reif(bbs)));
                    return;
                }
            }
            if (((FznSettings) solver.getSettings()).enableDecompositionOfLinearCombination()) {
                int[] tmp = IntLinCombFactory.getScalarBounds(bs, as);
                IntVar scal = VF.bounded(StringUtils.randomName(), tmp[0], tmp[1], solver);
                Constraint cstr = ICF.scalar(bs, as, "=", scal);
                ICF.arithm(scal, "=", c).reifyWith(r);
                solver.post(cstr);
            } else {
                ICF.scalar(bs, as, "=", c).reifyWith(r);
            }
        }
    }

    private static class PropBoolSumEq0Reif extends Propagator<BoolVar> {

        public PropBoolSumEq0Reif(BoolVar... vs) {
            super(vs);
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            int n = vars.length - 1;
            if (vars[n].getLB() == 1) {
                for (int i = 0; i < n; i++) {
                    vars[i].setToFalse(this);
                }
                setPassive();
                return;
            }
            int firstOne = -1;
            int secondOne = -1;
            for (int i = 0; i < n; i++) {
                if (vars[i].getLB() == 1) {
                    vars[n].setToFalse(this);
                    setPassive();
                    return;
                }
                if (vars[i].getUB() == 1) {
                    if (firstOne == -1) {
                        firstOne = i;
                    } else if (secondOne == -1) {
                        secondOne = i;
                    }
                }
            }
            if (firstOne == -1) {
                vars[n].setToTrue(this);
                setPassive();
            } else if (secondOne == -1 && vars[n].getUB() == 0) {
                vars[firstOne].setToTrue(this);
            }
        }

        @Override
        public ESat isEntailed() {
            return ESat.TRUE;
        }
    }
}
