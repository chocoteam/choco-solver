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
package org.chocosolver.solver.constraints.nary.alldifferent;

import gnu.trove.stack.array.TIntArrayStack;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * Propagator for AllDifferent that only reacts on instantiation
 *
 * @author Charles Prud'homme
 */
public class PropAllDiffInst extends Propagator<IntVar> {

    protected final int n;
    protected TIntArrayStack toCheck = new TIntArrayStack();

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AllDifferent constraint for integer variables
     * enables to control the cardinality of the matching
     *
     * @param variables array of integer variables
     */
    public PropAllDiffInst(IntVar[] variables) {
        super(variables, PropagatorPriority.UNARY, true);
        n = vars.length;
    }


    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.instantiation();
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        toCheck.clear();
        for (int v = 0; v < n; v++) {
            if (vars[v].isInstantiated()) {
                toCheck.push(v);
            }
        }
        fixpoint();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        toCheck.clear();
        toCheck.push(varIdx);
        fixpoint();
    }

    protected void fixpoint() throws ContradictionException {
        while (toCheck.size() > 0) {
            int vidx = toCheck.pop();
            int val = vars[vidx].getValue();
            for (int i = 0; i < n; i++) {
                if (i != vidx) {
                    if (vars[i].removeValue(val, this) && vars[i].isInstantiated()) {
                        toCheck.push(i);
                    }
                }
            }
        }
    }


    @Override
    public ESat isEntailed() {
        int nbInst = 0;
        for (int i = 0; i < n; i++) {
            if (vars[i].isInstantiated()) {
                nbInst++;
                for (int j = i + 1; j < n; j++) {
                    if (vars[j].isInstantiatedTo(vars[i].getValue())) {
                        return ESat.FALSE;
                    }
                }
            }
        }
        if (nbInst == vars.length) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        // to deal with BoolVar: any event is automatically promoted to INSTANTIATE
        if (evt == IntEventType.INSTANTIATE) {
            assert var.isBool() : "BoolVar excepted";
            value = 1 - var.getValue();
        }
        if (evt == IntEventType.REMOVE) {
            for (int i = 0, j = vars.length - 1; i <= j; i++, j--) {
                if (vars[i] != var && vars[i].isInstantiatedTo(value)) {
                    newrules |= ruleStore.addFullDomainRule(vars[i]);
                    return newrules;
                }
                if (vars[j] != var && vars[j].isInstantiatedTo(value)) {
                    newrules |= ruleStore.addFullDomainRule(vars[j]);
                    return newrules;
                }
            }
        } else {
            newrules |= super.why(ruleStore, var, evt, value);
        }
        return newrules;
    }
}
