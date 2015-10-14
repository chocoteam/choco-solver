/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
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
package org.chocosolver.solver.search.loop.plm;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.cnf.PropNogoods;
import org.chocosolver.solver.constraints.nary.cnf.SatSolver;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Related to "Nogood Recording from Restarts", C. Lecoutre et al.
 * <br/>
 * Beware :
 * - Only works for integer variables
 * - Only works if branching decisions are assignments (no domain split nor value removal)
 * <p>
 * On record, it simply store the current decision (variable, value, operator and branch)/
 * On forget, check the number of restarts and if one has been done, add the nogood.
 * Created by cprudhom on 05/10/15.
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 */
public class LearnNogoodFromRestarts implements Learn {

    IntVar[] dVars;
    int[] dVals;
    BitSet dOps; // 1 is assignment, 0 refutation
    int last, nbrestarts;
    final PropNogoods png;

    /**
     * Record nogoods on restart..
     * Beware :
     * - Only works for integer variables
     * - Only works if branching decisions are assignments (neither domain split nor value removal)
     *
     * @param solver solver to observe
     */
    public LearnNogoodFromRestarts(Solver solver) {
        png = solver.getNogoodStore().getPropNogoods();
        dVars = new IntVar[16];
        dVals = new int[16];
        dOps = new BitSet();
        last = 0;
        nbrestarts = 0;
    }

    @Override
    public void record(SearchDriver searchDriver) {
        Decision<IntVar> dec = searchDriver.getLastDecision();
        ensureCapacity(last);
        assert dec instanceof IntDecision : "LearnNogoodFromRestarts is only valid for integer variables (hence FastDecision)";
        assert dec.toString().contains(DecisionOperator.int_eq.toString()) : "LearnNogoodFromRestarts is only valid for assignment decisions";
        dVars[last] = dec.getDecisionVariables();
        dVals[last] = (Integer) dec.getDecisionValue();
        dOps.set(last, dec.hasNext());
        last++;
    }

    private void ensureCapacity(int d) {
        if (d - dVars.length > 0) {
            int oldCapacity = dVars.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            dVars = Arrays.copyOf(dVars, newCapacity);
            dVals = Arrays.copyOf(dVals, newCapacity);
        }
    }

    @Override
    public void forget(SearchDriver searchDriver) {
        if (nbrestarts == searchDriver.mSolver.getMeasures().getRestartCount() - 1) {
            nbrestarts++;
            int[] lits = new int[last];
            int i = 0;
            while (last > 0) {
                last--;
                if (dOps.get(last)) {
                    lits[i++] = SatSolver.negated(png.Literal(dVars[last], dVals[last]));
                } else {
                    if (i == 0) {
                        // value can be removed permanently from var!
                        png.addLearnt(SatSolver.negated(png.Literal(dVars[last], dVals[last])));
                    } else {
                        lits[i] = SatSolver.negated(png.Literal(dVars[last], dVals[last]));
                        png.addLearnt(Arrays.copyOf(lits, i + 1));
                    }
                }
            }
        } else {
            last--;
        }
    }
}
