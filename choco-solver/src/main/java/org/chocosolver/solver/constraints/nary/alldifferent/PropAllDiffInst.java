/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

import gnu.trove.map.hash.THashMap;
import gnu.trove.stack.array.TIntArrayStack;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.*;
import org.chocosolver.solver.explanations.arlil.RuleStore;
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
                    if (vars[i].removeValue(val, aCause) && vars[i].isInstantiated()) {
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
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            IntVar[] aVars = new IntVar[this.vars.length];
            for (int i = 0; i < this.vars.length; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (IntVar) identitymap.get(this.vars[i]);
            }

            identitymap.put(this, new PropAllDiffInst(aVars));
        }
    }

    @Override
    public void explain(ExplanationEngine xengine, Deduction d, Explanation e) {
        e.add(xengine.getPropagatorActivation(this));
        if (d.getmType() == Deduction.Type.ValRem) {
            int i = 0;
            while (i < vars.length && !vars[i].isInstantiatedTo(((ValueRemoval) d).getVal())) {
                i++;
            }
            vars[i].explain(xengine, VariableState.DOM, e);
        } else {
            super.explain(xengine, d, e);
        }
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
            int i = 0;
            while (i < vars.length && !vars[i].isInstantiatedTo(value)) {
                i++;
            }
            newrules |= ruleStore.addFullDomainRule(vars[i]);
        } else {
            newrules |= super.why(ruleStore, var, evt, value);

        }
        return newrules;
    }
}
