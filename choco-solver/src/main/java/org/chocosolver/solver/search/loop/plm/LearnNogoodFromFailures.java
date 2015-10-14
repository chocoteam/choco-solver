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

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.cnf.PropNogoods;
import org.chocosolver.solver.constraints.nary.cnf.SatSolver;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.variables.IntVar;

/**
 * Only relevant when dealing with LDS.
 * It aims at avoiding rediscovering the same sub-tree again and again when increasing the discrepancy.
 * <br/>
 * Beware :
 * - Only works for integer variables
 * - Only works if branching decisions are assignments (no domain split)
 * <p>
 * On forget, compute and add the nogood.
 * Created by cprudhom on 05/10/15.
 * Project: choco.
 *
 * @author Charles Prud'homme
 */
public class LearnNogoodFromFailures implements Learn {

    final PropNogoods png;
    final TIntArrayList lits;

    /**
     * Record nogoods on restart..
     * Beware :
     * - Only works for integer variables
     * - Only works if branching decisions are assignments (neither domain split nor value removal)
     *
     * @param solver solver to observe
     */
    public LearnNogoodFromFailures(Solver solver) {
        png = solver.getNogoodStore().getPropNogoods();
        lits = new TIntArrayList();
    }

    @Override
    public void record(SearchDriver searchDriver) {
        lits.resetQuick();
        Decision dec = searchDriver.decision;
        while (dec != RootDecision.ROOT) {
            assert dec instanceof IntDecision :
                    "LearnNogoodFromFailures can only deal with IntDecision (ie, IntVar)";
            IntDecision id = (IntDecision) dec;
            boolean asg;
            if (id.getDecOp() == DecisionOperator.int_eq) {
                asg = id.hasNext();
            } else if (id.getDecOp() == DecisionOperator.int_neq) {
                asg = !id.hasNext();
            } else {
                throw new SolverException("LearnNogoodFromFailures can only deal with assignment or refutation");
            }
            IntVar var = id.getDecisionVariables();
            int val = id.getDecisionValue();
            if (asg) {
                lits.add(SatSolver.negated(png.Literal(var, val)));
            } else {
                lits.add(png.Literal(var, val));
            }
            dec = dec.getPrevious();
        }
        if (lits.size() > 0) {
            png.addLearnt(lits.toArray());
        }
    }

    @Override
    public void forget(SearchDriver searchDriver) {
    }
}
