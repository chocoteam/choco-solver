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
import org.chocosolver.solver.Model;


import org.chocosolver.solver.constraints.nary.sum.IntLinCombFactory;
import org.chocosolver.solver.variables.IntVar;

import org.chocosolver.util.tools.StringUtils;

import java.util.List;

import static org.chocosolver.solver.constraints.nary.sum.IntLinCombFactory.getScalarBounds;
import static org.chocosolver.util.tools.StringUtils.randomName;

/**
 * &#8721; i &#8712; 1..n: as[i].bs[i] &#8804; c where n is the common length of as and bs
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
public class IntLinLeBuilder implements IBuilder {

    @Override
    public void build(Model model, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        int[] as = exps.get(0).toIntArray();
        IntVar[] bs = exps.get(1).toIntVarArray(model);
        IntVar c = exps.get(2).intVarValue(model);
        if (bs.length > 0) {
            if (c.isInstantiated()) {
                if (bs.length == 1) {
                    if (as[0] == -1) {
                        model.arithm(bs[0], ">=", -c.getValue()).post();
                        return;
                    }
                    if (as[0] == 1) {
                        model.arithm(bs[0], "<=", c.getValue()).post();
                        return;
                    }
                }
                if (bs.length == 2) {
                    if (as[0] == -1 && as[1] == 1) {
                        model.arithm(bs[1], "<=", bs[0], "+", c.getValue()).post();
                        return;
                    }
                    if (as[0] == 1 && as[1] == -1) {
                        model.arithm(bs[0], "<=", bs[1], "+", c.getValue()).post();
                        return;
                    }
                }
            }
            if (((FznSettings) model.getSettings()).enableDecompositionOfLinearCombination()) {
                int[] tmp = getScalarBounds(bs, as);
                IntVar scal = model.intVar(randomName(), tmp[0], tmp[1], true);
                model.scalar(bs, as, "=", scal).post();
                model.arithm(scal, "<=", c).post();
            } else {
                model.scalar(bs, as, "<=", c).post();
            }
        }

    }
}
