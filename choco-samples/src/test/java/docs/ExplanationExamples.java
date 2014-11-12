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
package docs;

import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.ICF;
import solver.explanations.ExplanationFactory;
import solver.messages.Chatterbox;
import solver.search.strategy.ISF;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VF;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 03/10/2014
 */
public class ExplanationExamples {

    @Test
    public void dummy() {
        Solver solver = new Solver();
        BoolVar[] bvars = VF.boolArray("B", 4, solver);
        solver.post(ICF.arithm(bvars[2], "=", bvars[3]));
        solver.post(ICF.arithm(bvars[2], "!=", bvars[3]));
        solver.set(ISF.lexico_LB(bvars));
        ExplanationFactory.CBJ.plugin(solver, true);
        Chatterbox.showStatistics(solver);
        Chatterbox.showSolutions(solver);
        Chatterbox.showDecisions(solver);
        solver.findAllSolutions();
    }

    @Test
    public void pigeon() {
        Solver solver = new Solver();
        IntVar[] pigeon = VF.enumeratedArray("p", 5, 1, 4, solver);
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 5; j++) {
                solver.post(ICF.arithm(pigeon[i], "!=", pigeon[j]));
            }
        }
        solver.set(ISF.lexico_LB(pigeon));
        ExplanationFactory.CBJ.plugin(solver, true);
        Chatterbox.showStatistics(solver);
        Chatterbox.showSolutions(solver);
        Chatterbox.showDecisions(solver);
        solver.findAllSolutions();
    }
}
