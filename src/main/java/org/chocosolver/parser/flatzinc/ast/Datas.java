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
package org.chocosolver.parser.flatzinc.ast;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.chocosolver.parser.flatzinc.ast.declaration.Declaration;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.parser.flatzinc.layout.FZNLayout;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import java.util.Arrays;
import java.util.List;

/**
 * An object to maintain a link between the model and the solver, during the parsing phase.
 *
 * @author Charles Prud'homme
 * @since 17/05/13
 */
public class Datas {

    final THashMap<String, Object> map;

    final THashSet<IntVar> intsearchVariables;
    final THashSet<SetVar> setsearchVariables;
    final THashSet<Variable> outputVariables;

    FZNLayout mLayout;

    public Datas() {
        this.map = new THashMap<>();
        intsearchVariables = new THashSet<>();
        setsearchVariables = new THashSet<>();
        outputVariables = new THashSet<>();
    }

    public void register(String name, Object o) {
        map.put(name, o);
    }

    public Object get(String id) {
        return map.get(id);
    }

    public void addSearchVars(Variable... vars) {
        for (int i = 0; i < vars.length; i++) {
            if ((vars[i].getTypeAndKind() & Variable.INT) != 0) {

                intsearchVariables.addAll(Arrays.asList((IntVar) vars[i]));
            } else {
                setsearchVariables.addAll(Arrays.asList((SetVar) vars[i]));
            }
        }
    }

    public IntVar[] getIntSearchVars() {
        return intsearchVariables.toArray(new IntVar[intsearchVariables.size()]);
    }

    public SetVar[] getSetSearchVars() {
        return setsearchVariables.toArray(new SetVar[setsearchVariables.size()]);
    }

    public Variable[] getOutputVars() {
        return outputVariables.toArray(new Variable[outputVariables.size()]);
    }

    public void declareOutput(String name, Variable variable, Declaration type) {
        mLayout.addOutputVar(name, variable, type);
        outputVariables.add(variable);
    }

    public void declareOutput(String name, Variable[] variables, List<Expression> indices, Declaration type) {
        mLayout.addOutputArrays(name, variables, indices, type);
        outputVariables.addAll(Arrays.asList(variables));
    }

    public void setLayout(FZNLayout layout) {
        this.mLayout = layout;
    }

    public void clear() {
        map.clear();
        intsearchVariables.clear();
        setsearchVariables.clear();
    }
}
