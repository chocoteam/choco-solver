/**
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
package org.chocosolver.parser.flatzinc.ast.constraints;

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.constraints.set.SCF;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.tools.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * a < b
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco-parsers
 * @since 13/10/2014
 */
public class SetLtBuilder implements IBuilder {
    @Override
    public void build(Solver solver, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        SetVar a = exps.get(0).setVarValue(solver);
        SetVar b = exps.get(1).setVarValue(solver);

        SetVar ab = a.duplicate();
        SetVar ba = b.duplicate();

        TIntHashSet values = new TIntHashSet();
        for (int i = a.getEnvelopeFirst(); i != SetVar.END; i = a.getEnvelopeNext()) {
            values.add(i);
        }
        for (int i = b.getEnvelopeFirst(); i != SetVar.END; i = b.getEnvelopeNext()) {
            values.add(i);
        }
        int[] env = values.toArray();
        Arrays.sort(env);
        SetVar c = VF.set(StringUtils.randomName(), env, solver);
        IntVar min = VF.integer(StringUtils.randomName(), env[0], env[env.length - 1], solver);

        BoolVar _b1 = SCF.subsetEq(new SetVar[]{a, b}).reif();
        BoolVar _b2 = SCF.all_different(new SetVar[]{a, b}).reif();

        solver.post(SCF.partition(new SetVar[]{ab, b}, a),
                SCF.partition(new SetVar[]{ba, a}, b),
                SCF.union(new SetVar[]{ab, ba}, c));
        SCF.min(c, min, false);
        BoolVar _b3 = SCF.member(min, a).reif();

        SatFactory.addClauses(
                LogOp.or(_b3, LogOp.and(_b1, _b2)), solver
        );
    }
}
