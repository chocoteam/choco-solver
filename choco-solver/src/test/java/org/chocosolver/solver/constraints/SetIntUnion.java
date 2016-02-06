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
/**
 * @author Jean-Guillaume Fages
 * @since 08/10/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.annotations.Test;

import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.lexico_LB;
import static org.chocosolver.solver.trace.Chatterbox.showSolutions;
import static org.chocosolver.solver.trace.Chatterbox.showStatistics;
import static org.testng.Assert.assertEquals;

public class SetIntUnion {

	@Test(groups="1s", timeOut=60000)
	public void test1() {
        Model s = new Model();
        IntVar[] x = s.intVarArray("ints", 4, 0, 5, false);
        SetVar values = s.setVar("values", new int[]{0, 1, 4});
        s.union(x, values).post();
        showStatistics(s);
        showSolutions(s);
        s.set(lexico_LB(x));
        while (s.solve()) ;
    }

	@Test(groups="1s", timeOut=60000)
	public void test2() {
        Model s = new Model();
        IntVar[] x = new IntVar[]{
                s.intVar(0)
                , s.intVar(2)
                , s.intVar(5)
                , s.intVar(0)
                , s.intVar(2)
        };
        SetVar values = s.setVar("values", new int[]{0, 1, 4});
        s.union(x, values).post();
        showStatistics(s);
        showSolutions(s);
        s.set(lexico_LB(x));
        while (s.solve()) ;
        assertEquals(s.getMeasures().getSolutionCount(), 0);
    }

	@Test(groups="1s", timeOut=60000)
	public void test3() {
        Model s = new Model();
        IntVar[] x = new IntVar[]{
                s.intVar(0)
                , s.intVar(2)
                , s.intVar(5)
                , s.intVar(0)
                , s.intVar(2)
        };
        SetVar values = s.setVar("values", new int[]{}, new int[]{-1, 0, 1, 2, 3, 4, 5, 6});
        s.union(x, values).post();
        showStatistics(s);
        showSolutions(s);
        s.set(lexico_LB(x));
        while (s.solve()) ;
        out.println(values);
        assertEquals(s.getMeasures().getSolutionCount(), 1);
    }
}
