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

package org.chocosolver.parser.flatzinc.ast.constraints.global;

import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.ast.constraints.IBuilder;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Model;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;


import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 26/01/11
 */
public class CumulativeBuilder implements IBuilder {

    @Override
    public void build(Model model, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        final IntVar[] starts = exps.get(0).toIntVarArray(model);
        final IntVar[] durations = exps.get(1).toIntVarArray(model);
        final IntVar[] resources = exps.get(2).toIntVarArray(model);
        final IntVar[] ends = new IntVar[starts.length];
        Task[] tasks = new Task[starts.length];
        final IntVar limit = exps.get(3).intVarValue(model);
        for (int i = 0; i < starts.length; i++) {
            ends[i] = model.intVar(starts[i].getName() + "_" + durations[i].getName(),
                    starts[i].getLB() + durations[i].getLB(),
                    starts[i].getUB() + durations[i].getUB(),
                    true);
            tasks[i] = new Task(starts[i], durations[i], ends[i]);
        }
        model.cumulative(tasks, resources, limit, true).post();
    }
}
