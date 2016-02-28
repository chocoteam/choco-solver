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
package org.chocosolver.samples.todo.tests;

import org.chocosolver.memory.Environments;
import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.samples.todo.problems.integer.AllIntervalSeries;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/11/11
 */
public class AllTestFactory {

    @Test
    public void testDifferentConfigurations() {

        AbstractProblem[] problems = new AbstractProblem[]{
                new AllIntervalSeries()
        };

        String[][] arguments = new String[][]{
                {"-seed", "1234"},
                {"-seed", "1236"},
        };

        long[] nbSol = new long[]{
                6, 18
        };

        Environments[] envFact = new Environments[]{
                Environments.TRAIL,
                Environments.COPY
        };

        List<Object> lresult = new ArrayList<>(12);

        PropagationEngineFactory[] pol = PropagationEngineFactory.values();

        for (int p = 0; p < problems.length; p++) {
            System.out.println(problems[p].getClass().getSimpleName());
            for (String exp : new String[]{"NONE", "CBJ", "DBT"}) {
                System.out.println("\t"+exp);
                for (Environments e : envFact) {
                    System.out.println("\t\t"+e);
                    for (PropagationEngineFactory st : pol) {
                        System.out.println("\t\t\t"+pol);
                        lresult.add(new AllTest(problems[p], arguments[p], e.make(), st, exp, nbSol[p]));
                        System.out.println("\t\t\tDONE");
                    }
                    System.out.println("\t\tDONE");
                }
                System.out.println("\tDONE");
            }
            System.out.println("DONE");
        }
    }
}
