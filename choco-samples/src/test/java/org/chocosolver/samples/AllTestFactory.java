/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package org.chocosolver.samples;

import org.chocosolver.memory.Environments;
import org.chocosolver.samples.integer.AbsoluteEvaluation;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.testng.annotations.Factory;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/11/11
 */
public class AllTestFactory {

    AbstractProblem[] problems = new AbstractProblem[]{
//            new AllIntervalSeries()
            new AbsoluteEvaluation(),
            new AbsoluteEvaluation()
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

    ExplanationFactory[] expFact = new ExplanationFactory[]{
            ExplanationFactory.NONE,
            ExplanationFactory.CBJ,
            ExplanationFactory.DBT,
    };


    @Factory
    public Object[] createInstances() {
        List<Object> lresult = new ArrayList<>(12);

        PropagationEngineFactory[] pol = PropagationEngineFactory.values();

        for (int p = 0; p < problems.length; p++)
            for (ExplanationFactory x : expFact)
                for (Environments e : envFact)
                    for (PropagationEngineFactory st : pol)
                        lresult.add(new AllTest(problems[p], arguments[p], e.make(), st, x, nbSol[p]));

        return lresult.toArray();
    }
}
