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
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.procedure.UnaryIntProcedure;

/**
 * | X - Y | op C <br/> op = {"==", "<", ">", "=/="} <br/>
 *
 * @author Charles Prud'homme
 * @since 21/03/12
 */
public class PropDistanceXYC extends Propagator<IntVar> {

    protected final Operator operator;

    protected final int cste;

    protected final RemProc remproc;

    protected final IIntDeltaMonitor[] idms;

    public PropDistanceXYC(IntVar[] vars, Operator operator, int cste) {
        super(vars, PropagatorPriority.BINARY, true);
        if (operator == Operator.EQ) {
            this.idms = new IIntDeltaMonitor[this.vars.length];
            for (int i = 0; i < this.vars.length; i++) {
                idms[i] = vars[i].hasEnumeratedDomain() ? this.vars[i].monitorDelta(this) : IIntDeltaMonitor.Default.NONE;
            }
        } else {
            this.idms = new IIntDeltaMonitor[0];
        }
        this.operator = operator;
        this.cste = cste;
        this.remproc = new RemProc(this);
    }

    @Override
    public int getPropagationConditions(int idx) {
        if (vars[idx].hasEnumeratedDomain()) {
            return IntEventType.all();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        //cste < 0, and |vars[0]-vars[1]| always >= 0
        if (cste < 0) {
            switch (operator) {
                case EQ:
                case LT:
                    this.contradiction(null, "< or = to 0");
                    break;
                case NQ:
                case GT:
                    this.setPassive();
                    break;
            }
        }
        if (operator == Operator.EQ) {
            if (vars[0].hasEnumeratedDomain()) {
                filterFromVarToVar(vars[0], vars[1]);
            } else {
                vars[0].updateLowerBound(vars[1].getLB() - cste, aCause);
                vars[0].updateUpperBound(vars[1].getUB() + cste, aCause);
            }
            if (vars[1].hasEnumeratedDomain()) {
                filterFromVarToVar(vars[1], vars[0]);
            } else {
                vars[1].updateLowerBound(vars[0].getLB() - cste, aCause);
                vars[1].updateUpperBound(vars[0].getUB() + cste, aCause);
            }
        } else if (operator == Operator.GT) {
            filterGT();
        } else if (operator == Operator.LT) {
            filterLT();
        } else {
            filterNeq();
        }
        for (int i = 0; i < idms.length; i++) {
            idms[i].unfreeze();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        int idx2 = varIdx == 0 ? 1 : 0;
        switch (operator) {
            case EQ:
                if (IntEventType.isInstantiate(mask)) {
                    filterOnInst(vars[idx2], vars[varIdx].getValue());
                } else {
                    if (IntEventType.isRemove(mask) && vars[varIdx].hasEnumeratedDomain()) {
                        idms[varIdx].freeze();
                        idms[varIdx].forEachRemVal(remproc.set(varIdx));
                        idms[varIdx].unfreeze();
                    }
                    if (IntEventType.isInclow(mask)) {
                        filterOnInf(vars[varIdx], vars[idx2]);
                    }
                    if (IntEventType.isDecupp(mask)) {
                        filterOnSup(vars[varIdx], vars[idx2]);
                    }
                }
                break;
            case NQ:
                filterNeq();
                break;
            case GT:
                if (IntEventType.isInstantiate(mask)) {
                    filterGTonVar(vars[varIdx], vars[idx2]);
                } else if (IntEventType.isBound(mask)) {
                    filterGTonVar(vars[varIdx], vars[idx2]);
                }
                break;
            case LT:
                if (IntEventType.isInstantiate(mask)) {
                    filterLTonVar(vars[varIdx], vars[idx2]);
                } else if (IntEventType.isBound(mask)) {
                    filterLTonVar(vars[varIdx], vars[idx2]);
                }
                break;
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (operator == Operator.EQ) {
                return ESat.eval(Math.abs(vars[0].getValue() - vars[1].getValue()) == cste);
            } else if (operator == Operator.LT) {
                return ESat.eval(Math.abs(vars[0].getValue() - vars[1].getValue()) < cste);
            } else if (operator == Operator.GT) {
                return ESat.eval(Math.abs(vars[0].getValue() - vars[1].getValue()) > cste);
            } else if (operator == Operator.NQ) {
                return ESat.eval(Math.abs(vars[0].getValue() - vars[1].getValue()) != cste);
            } else {
                throw new SolverException("operator not known");
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("|").append(vars[0].getName()).append(" - ").append(vars[1].getName()).append("|");
        switch (operator) {
            case EQ:
                st.append("=");
                break;
            case GT:
                st.append(">");
                break;
            case LT:
                st.append("<");
                break;
            case NQ:
                st.append("=/=");
                break;
        }
        st.append(cste);
        return st.toString();
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        return ruleStore.addPropagatorActivationRule(this)
                | ruleStore.addFullDomainRule((var == vars[0]) ? vars[1] : vars[0]);
    }


    //*************************************************************//
//        Methods for filtering                                //
//*************************************************************//


    /**
     * Initial propagation in case of EQ and enumerated domains
     *
     * @throws ContradictionException
     */
    public void filterFromVarToVar(IntVar var1, IntVar var2) throws ContradictionException {
        DisposableRangeIterator it = var1.getRangeIterator(true);
        try {
            while (it.hasNext()) {
                int from = it.min();
                int to = it.max();
                for (int value = from; value <= to; value++)
                    if (!var2.contains(value - cste) && !var2.contains(value + cste)) {
                        var1.removeValue(value, aCause);
                    }
                it.next();
            }
        } finally {
            it.dispose();
        }
    }

    /**
     * In case of a GT
     */
    public void filterGT() throws ContradictionException {
        if (cste >= 0) {
            int lbv0 = vars[1].getUB() - cste;
            int ubv0 = vars[1].getLB() + cste;
            // remove interval [lbv0, ubv0] from domain of vars[0]
            vars[0].removeInterval(lbv0, ubv0, aCause);
            int lbv1 = vars[0].getUB() - cste;
            int ubv1 = vars[0].getLB() + cste;
            // remove interval [lbv1, ubv1] from domain of vars[1]
            vars[1].removeInterval(lbv1, ubv1, aCause);
        } else {
            this.setPassive();
        }
    }

    /**
     * In case of a GT, due to a modification on v0 domain
     */
    public void filterGTonVar(IntVar v0, IntVar v1) throws ContradictionException {
        if (cste >= 0) {
            int lbv0 = v0.getUB() - cste;
            int ubv0 = v0.getLB() + cste;
            // remove interval [lbv0, ubv0] from domain of vars[0]
            v1.removeInterval(lbv0, ubv0, aCause);
        } else {
            this.setPassive();
        }
    }

    /**
     * In case of a LT
     */
    public void filterLT() throws ContradictionException {
        vars[0].updateLowerBound(vars[1].getLB() - cste + 1, aCause);
        vars[0].updateUpperBound(vars[1].getUB() + cste - 1, aCause);
        vars[1].updateLowerBound(vars[0].getLB() - cste + 1, aCause);
        vars[1].updateUpperBound(vars[0].getUB() + cste - 1, aCause);
    }

    /**
     * In case of a LT, due to a modification on v0 domain
     */
    public void filterLTonVar(IntVar v0, IntVar v1) throws ContradictionException {
        v1.updateLowerBound(v0.getLB() - cste + 1, aCause);
        v1.updateUpperBound(v0.getUB() + cste - 1, aCause);
    }

    /**
     * In case of a EQ, due to a modification of the lower bound of v0
     */
    public void filterOnInf(IntVar v0, IntVar v1) throws ContradictionException {
        if (v1.hasEnumeratedDomain()) {
            int end = v0.getLB() + cste;
            for (int val = v0.getLB(); val <= end; val = v1.nextValue(val)) {
                if (!v0.contains(val - cste) && !v0.contains(val + cste)) {
                    v1.removeValue(val, aCause);
                }
            }
        } else {
            v1.updateLowerBound(v0.getLB() - cste, aCause);
        }
    }

    /**
     * In case of a EQ, due to a modification of the upper bound of v0
     */
    public void filterOnSup(IntVar v0, IntVar v1) throws ContradictionException {
        if (v1.hasEnumeratedDomain()) {
            int initval;
            if (v0.getUB() - cste > v1.getLB()) {
                initval = v1.nextValue(v0.getUB() - cste - 1);
            } else {
                initval = v1.getLB();
            }
            int val = initval;
            do {
                if (!v0.contains(val - cste) && !v0.contains(val + cste)) {
                    v1.removeValue(val, aCause);
                }
                val = v1.nextValue(val);
            } while (val <= v1.getUB() && val > initval); //todo : pourquoi besoin du deuxieme currentElement ?
        } else {
            v1.updateUpperBound(v0.getUB() + cste, aCause);
        }
    }

    /**
     * In case of a EQ, due to the instantion to one variable to val
     */
    public void filterOnInst(IntVar v, int val) throws ContradictionException {
        if (!v.contains(val + cste)) {
            v.instantiateTo(val - cste, aCause);
        } else if (!v.contains(val - cste)) {
            v.instantiateTo(val + cste, aCause);
        } else {
            if (v.hasEnumeratedDomain()) {
                DisposableRangeIterator rit = v.getRangeIterator(true);
                try {
                    while (rit.hasNext()) {
                        int from = rit.min();
                        int to = rit.max();
                        for (int value = from; value <= to; value++) {
                            if (value != (val - cste) && value != (val + cste)) {
                                v.removeValue(value, aCause);
                            }
                        }
                        rit.next();
                    }
                } finally {
                    rit.dispose();
                }
            } else {
                v.updateLowerBound(val - cste, aCause);
                v.updateUpperBound(val + cste, aCause);
            }
        }
    }

    public void filterNeq() throws ContradictionException {
        if (cste >= 0) {
            if (vars[0].isInstantiated()) {
                vars[1].removeValue(vars[0].getValue() + cste, aCause);
                vars[1].removeValue(vars[0].getValue() - cste, aCause);
            }
            if (vars[1].isInstantiated()) {
                vars[0].removeValue(vars[1].getValue() + cste, aCause);
                vars[0].removeValue(vars[1].getValue() - cste, aCause);
            }
        } else {
            this.setPassive();
        }
    }

    private static class RemProc implements UnaryIntProcedure<Integer> {

        int idx;
        final PropDistanceXYC p;

        public RemProc(PropDistanceXYC p) {
            this.p = p;
        }

        @Override
        public UnaryIntProcedure set(Integer integer) {
            this.idx = integer;
            return this;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            if (idx == 0) {
                if (!p.vars[0].contains(i + 2 * p.cste)) {
                    p.vars[1].removeValue(i + p.cste, this.p);
                }
                if (!p.vars[0].contains(i - 2 * p.cste)) {
                    p.vars[1].removeValue(i - p.cste, this.p);
                }
            } else {
                if (!p.vars[1].contains(i + 2 * p.cste)) {
                    p.vars[0].removeValue(i + p.cste, this.p);
                }
                if (!p.vars[1].contains(i - 2 * p.cste)) {
                    p.vars[0].removeValue(i - p.cste, this.p);
                }
            }
        }
    }

}
