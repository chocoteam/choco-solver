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

package solver.explanations;

import solver.ICause;
import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;
import solver.variables.IntVar;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 17/10/11
 * Time: 08:43
 */
public class ExplanationMonitorList implements IExplanationMonitor {
    IExplanationMonitor[] explanationMonitors = new IExplanationMonitor[4];
    int size = 0;

    public void add(IExplanationMonitor sm) {
         if (size >= explanationMonitors.length) {
             IExplanationMonitor[] tmp = explanationMonitors;
             explanationMonitors = new IExplanationMonitor[tmp.length * 2];
             System.arraycopy(tmp, 0, explanationMonitors, 0, tmp.length);
         }
         explanationMonitors[size++] = sm;
     }

    @Override
    public void onRemoveValue(IntVar var, int val, ICause cause, Explanation explanation) {
        for (int i = 0; i < size; i++) {
            explanationMonitors[i].onRemoveValue(var, val, cause, explanation);
        }
    }

    @Override
    public void onUpdateLowerBound(IntVar intVar, int old, int value, ICause cause, Explanation explanation) {
        for (int i = 0; i < size; i++) {
            explanationMonitors[i].onUpdateLowerBound(intVar, old, value, cause, explanation);
        }
    }

    @Override
    public void onUpdateUpperBound(IntVar intVar, int old, int value, ICause cause, Explanation explanation) {
        for (int i = 0; i < size; i++) {
            explanationMonitors[i].onUpdateUpperBound(intVar, old, value, cause, explanation);
        }
    }

    @Override
    public void onInstantiateTo(IntVar var, int val, ICause cause, Explanation explanation) {
        for (int i = 0; i < size; i++) {
            explanationMonitors[i].onInstantiateTo(var, val, cause, explanation);
        }
    }

    @Override
    public void onContradiction(ContradictionException cex, Explanation explanation, int upTo, Decision decision) {
         for (int i = 0; i < size; i++) {
            explanationMonitors[i].onContradiction(cex, explanation, upTo, decision);
        }
    }
}
