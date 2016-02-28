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
package org.chocosolver.samples.todo.docs;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.testng.annotations.Test;

import static org.chocosolver.solver.trace.Chatterbox.showSolutions;

/**
 * BEWARE: 5_elements.rst SHOULD BE UPDATED ANYTIME THIS CLASS IS CHANGED
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 16/09/2014
 */
public class IntConstraintExamples2 {

    @Test(groups="1s", timeOut=60000)
    public void mddc() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("X", 2, -2, 2, false);
        Tuples tuples = new Tuples();
        tuples.add(0, -1);
        tuples.add(1, -1);
        tuples.add(0, 1);
        model.mddc(vars, new MultivaluedDecisionDiagram(vars, tuples)).post();
        showSolutions(model);
        while (model.solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void clause_channeling() {
        Model model = new Model();
        IntVar iv = model.intVar("iv", 1, 3, false);
        BoolVar[] eqs = model.boolVarArray("eq", 3);
        BoolVar[] lqs = model.boolVarArray("lq", 3);
        model.clausesIntChanneling(iv, eqs, lqs).post();
        showSolutions(model);
        while (model.solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void int_value_precede_chain() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 3, 1, 3, false);
        model.intValuePrecedeChain(X, 1, 2).post();
        showSolutions(model);
        while (model.solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void int_value_precede_chain2() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 3, 1, 3, false);
        model.intValuePrecedeChain(X, new int[]{2, 3, 1}).post();
        showSolutions(model);
        while (model.solve()) ;
    }
}
