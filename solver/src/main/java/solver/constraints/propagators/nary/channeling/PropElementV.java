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
package solver.constraints.propagators.nary.channeling;

import choco.kernel.ESat;
import choco.kernel.common.util.VariableUtilities;
import choco.kernel.common.util.procedure.UnaryIntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateBool;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * A class implementing the constraint VALUE = TABLE[INDEX],
 * <br/>with INDEX and VALUE being IntVars and VALUES an array of IntVars:
 * <br/>
 *
 * @author Hadrien Cambazard
 * @author Charles Prud'homme
 * @since 04/08/11
 */
public class PropElementV extends Propagator<IntVar> {
    private final int offset;

    protected IStateBool valueUpdateNeeded;
    protected IStateBool indexUpdateNeeded;

    protected final RemProc rem_proc;

    public PropElementV(IntVar value, IntVar[] values, IntVar index, int offset,
                        Solver solver, Constraint<IntVar, Propagator<IntVar>> constraint) {
        super(ArrayUtils.append(values, new IntVar[]{index, value}),
                solver, constraint, PropagatorPriority.QUADRATIC, true);
        this.offset = offset;
        valueUpdateNeeded = environment.makeBool(true);
        indexUpdateNeeded = environment.makeBool(true);
        rem_proc = new RemProc(this);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int n = vars.length;
        IntVar idxVar = getIndexVar();
        idxVar.updateLowerBound(0 - offset, this, false);
        idxVar.updateUpperBound(n - 3 - offset, this, false);
        if (indexUpdateNeeded.get()) {
            updateIndexFromValue();
        }
        if (getIndexVar().instantiated()) {
            equalityBehaviour();
        } else if (valueUpdateNeeded.get()) {
            updateValueFromIndex();
        }
    }

    @Override
    public void propagateOnRequest(IRequest<IntVar> request, int vIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            awakeOnInst(vIdx);
        }
        if (EventType.isInclow(mask)) {
            awakeOnInf(vIdx);
        }
        if (EventType.isDecupp(mask)) {
            awakeOnSup(vIdx);
        }
        if (EventType.isRemove(mask)) {
            request.forEach(rem_proc.set(vIdx));
        }
    }

    @Override
    public ESat isEntailed() {
        ESat isEntailed = ESat.UNDEFINED;
        IntVar idxVar = getIndexVar();
        IntVar valVar = getValueVar();
        if ((valVar.instantiated()) &&
                (idxVar.getLB() + this.offset >= 0) &&
                (idxVar.getUB() + this.offset < vars.length - 2)) {
            boolean allEqualToValVar = true;
            int ub = idxVar.getUB();
            for (int val = idxVar.getLB(); val <= ub; val = idxVar.nextValue(val)) {
                int feasibleIndex = val + this.offset;
                if (!vars[feasibleIndex].instantiatedTo(valVar.getValue())) {
                    allEqualToValVar = false;
                }
            }
            if (allEqualToValVar) {
                isEntailed = ESat.TRUE;
            }
        }
        if (isEntailed != ESat.TRUE) {
            boolean existsSupport = false;
            int ub = idxVar.getUB();
            for (int val = idxVar.getLB(); val <= ub; val = idxVar.nextValue(val)) {
                int feasibleIndex = val + this.offset;
                if ((feasibleIndex >= 0) && (feasibleIndex < vars.length - 2)
                        && (!VariableUtilities.emptyUnion(valVar, vars[feasibleIndex]))) {
                    existsSupport = true;
                }
            }
            if (!existsSupport) isEntailed = ESat.FALSE;
        }
        return isEntailed;
    }

    //--------------------------------------------

    private IntVar getIndexVar() {
        return vars[vars.length - 2];
    }

    private IntVar getValueVar() {
        return vars[vars.length - 1];
    }

    private void updateValueFromIndex() throws ContradictionException {
        IntVar idxVar = getIndexVar();
        IntVar valVar = getValueVar();
        int minval = Integer.MAX_VALUE;
        int maxval = Integer.MIN_VALUE;
        int ub = idxVar.getUB();
        for (int val = idxVar.getLB(); val <= ub; val = idxVar.nextValue(val)) {
            minval = Math.min(minval, vars[val + offset].getLB());
            maxval = Math.max(maxval, vars[val + offset].getUB());
        }
        // further optimization:
        // I should consider for the min, the minimum value in domain(c.vars[feasibleIndex) that is >= to valVar.inf
        // (it can be greater than valVar.inf if there are holes in domain(c.vars[feasibleIndex]))
        valVar.updateLowerBound(minval, this, false);
        valVar.updateUpperBound(maxval, this, false);
        // v1.0: propagate on holes when valVar has an enumerated domain
        if (valVar.hasEnumeratedDomain()) {
            int ubV = valVar.getUB();
            for (int v = valVar.getLB(); v <= ubV; v = valVar.nextValue(v)) {
                boolean possibleV = false;
                int ub1 = idxVar.getUB();
                for (int vv = idxVar.getLB(); vv <= ub1 && !possibleV; vv = idxVar.nextValue(vv)) {
                    //      for (int tentativeIdx = idxVar.getLB(); tentativeIdx <= idxVar.getUB(); tentativeIdx = idxVar.getNextDomainValue(tentativeIdx)) {
                    if (vars[vv + offset].contains(v)) {
                        possibleV = true;
                        break;
                    }
                }
                if (!possibleV) {
                    valVar.removeValue(v, this, false);
                }
            }
        }
        valueUpdateNeeded.set(false);
    }

    private void updateIndexFromValue() throws ContradictionException {
        IntVar idxVar = getIndexVar();
        IntVar valVar = getValueVar();
        int minFeasibleIndex = Math.max(0 - offset, idxVar.getLB());
        int maxFeasibleIndex = Math.min(idxVar.getUB(), vars.length - 3 - offset);

        while (idxVar.contains(minFeasibleIndex) &&
                VariableUtilities.emptyUnion(valVar, vars[minFeasibleIndex + offset])) {
            minFeasibleIndex++;
        }
        idxVar.updateLowerBound(minFeasibleIndex, this, false);


        while (idxVar.contains(maxFeasibleIndex) &&
                (VariableUtilities.emptyUnion(valVar, vars[maxFeasibleIndex + offset]))) {
            maxFeasibleIndex--;
        }
        idxVar.updateUpperBound(maxFeasibleIndex, this, false);

        if (idxVar.hasEnumeratedDomain()) { //those remVal would be ignored for variables using an interval approximation for domain
            for (int i = minFeasibleIndex + 1; i < maxFeasibleIndex - 1; i++) {
                if (idxVar.contains(i) && VariableUtilities.emptyUnion(valVar, vars[i + offset])) {
                    idxVar.removeValue(i, this, false);
                }
            }
        }
        // if the domain of idxVar has been reduced to one element, then it behaves like an equality
        if (idxVar.instantiated()) {
            equalityBehaviour();
        }
        indexUpdateNeeded.set(false);
    }

    // Once the index is known, the constraints behaves like an equality : valVar == c.vars[idxVar.value]
// This method must only be called when the value of idxVar is known.
    private void equalityBehaviour() throws ContradictionException {
        assert (getIndexVar().instantiated());
        int indexVal = getIndexVar().getValue();
        IntVar valVar = getValueVar();
        IntVar targetVar = vars[indexVal + offset];
        // code similar to awake@Equalxyc
        valVar.updateLowerBound(targetVar.getLB(), this, false);
        valVar.updateUpperBound(targetVar.getUB(), this, false);
        targetVar.updateLowerBound(valVar.getLB(), this, false);
        targetVar.updateUpperBound(valVar.getUB(), this, false);
        if (targetVar.hasEnumeratedDomain()) {
            int left = Integer.MIN_VALUE;
            int right = left;
            int ub = valVar.getUB();
            for (int val = valVar.getLB(); val <= ub; val = valVar.nextValue(val)) {
                if (!targetVar.contains(val)) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        valVar.removeInterval(left, right, this, false);
                        left = val;
                        right = val;
                    }
                    //valVar.removeValue(val, this);
                }
            }
            valVar.removeInterval(left, right, this, false);
        }
        if (valVar.hasEnumeratedDomain()) {
            int left = Integer.MIN_VALUE;
            int right = left;
            int ub = targetVar.getUB();
            for (int val = targetVar.getLB(); val <= ub; val = targetVar.nextValue(val)) {
                if (!valVar.contains(val)) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        targetVar.removeInterval(left, right, this, false);
                        left = val;
                        right = val;
                    }
//                    targetVar.removeValue(val, this);
                }
            }
            targetVar.removeInterval(left, right, this, false);
        }
    }

    private void awakeOnInf(int idx) throws ContradictionException {
        IntVar idxVar = getIndexVar();
        IntVar valVar = getValueVar();
        if (idx == vars.length - 2) {        // the event concerns idxVar
            if (idxVar.instantiated()) {
                equalityBehaviour();
            } else {
                updateValueFromIndex();
            }
        } else if (idx == vars.length - 1) { // the event concerns valVar
            if (idxVar.instantiated()) {
                int idxVal = idxVar.getValue();
                vars[idxVal + offset].updateLowerBound(valVar.getLB(), this, false);
            } else {
                updateIndexFromValue();
            }
        } else {                            // the event concerns a variable from the array
            if (idxVar.instantiated()) {
                int idxVal = idxVar.getValue();
                if (idx == idxVal + offset) {
                    valVar.updateLowerBound(vars[idx].getLB(), this, false);
                }
            } else if (idxVar.contains(idx - offset)) {  //otherwise the variable is not in scope
                if (VariableUtilities.emptyUnion(valVar, vars[idx])) {
                    idxVar.removeValue(idx - offset, this, true);
                    // NOCAUSE because if it changes the domain of IndexVar (what is not sure if idxVar
                    // uses an interval approximated domain) then it must cause updateValueFromIndex(c)
                } else if (vars[idx].getLB() > valVar.getLB()) {
                    // only the inf can change if the index is not removed
                    int minval = Integer.MAX_VALUE;
                    int ub = idxVar.getUB();
                    for (int val = idxVar.getLB(); val <= ub; val = idxVar.nextValue(val)) {
                        int feasibleIndex = val + this.offset;
                        minval = Math.min(minval, vars[feasibleIndex].getLB());
                    }
                    valVar.updateLowerBound(minval, this, true);
                    // NOCAUSE because if valVar takes a new min, then it can have consequence
                    // on the constraint itself (ie remove indices such that l[i].sup < value.inf)
                }
            }
        }
    }

    private void awakeOnSup(int idx) throws ContradictionException {
        IntVar idxVar = getIndexVar();
        IntVar valVar = getValueVar();
        if (idx == vars.length - 2) {        // the event concerns idxVar
            if (idxVar.instantiated()) {
                equalityBehaviour();
            } else {
                updateValueFromIndex();
            }
        } else if (idx == vars.length - 1) {  // the event concerns valVar
            if (idxVar.instantiated()) {
                int idxVal = idxVar.getValue();
                vars[idxVal + offset].updateUpperBound(valVar.getUB(), this, false);
            } else {
                updateIndexFromValue();
            }
        } else {                            // the event concerns a variable from the array
            if (idxVar.instantiated()) {
                int idxVal = idxVar.getValue();
                if (idx == idxVal + offset) {
                    valVar.updateUpperBound(vars[idx].getUB(), this, false);
                }
            } else if (idxVar.contains(idx - offset)) {  //otherwise the variable is not in scope
                if (VariableUtilities.emptyUnion(valVar, vars[idx])) {
                    idxVar.removeValue(idx - offset, this, true);
                    // NOCAUSE because if it changes the domain of IndexVar (what is not sure if idxVar
                    // uses an interval approximated domain) then it must cause updateValueFromIndex(c)
                } else if (vars[idx].getUB() < valVar.getUB()) {
                    // only the sup can change if the index is not removed
                    int maxval = Integer.MIN_VALUE;
                    int ub = idxVar.getUB();
                    for (int val = idxVar.getLB(); val <= ub; val = idxVar.nextValue(val)) {
                        int feasibleIndex = val + this.offset;
                        maxval = Math.max(maxval, vars[feasibleIndex].getUB());
                    }
                    valVar.updateUpperBound(maxval, this, true);
                    // NOCAUSE because if valVar takes a new min, then it can have consequence
                    // on the constraint itself (ie remove indices such that l[i].sup < value.inf)
                }
            }
        }
    }

    private void awakeOnInst(int idx) throws ContradictionException {
        IntVar idxVar = getIndexVar();
        IntVar valVar = getValueVar();
        if (idx == vars.length - 2) {        // the event concerns idxVar
            equalityBehaviour();
        } else if (idx == vars.length - 1) {  // the event concerns valVar
            if (idxVar.instantiated()) {
                int idxVal = idxVar.getValue();
                vars[idxVal + offset].instantiateTo(valVar.getValue(), this, false);
            } else {
                updateIndexFromValue();
            }
        } else {                            // the event concerns a variable from the array
            if (idxVar.instantiated()) {
                int idxVal = idxVar.getValue();
                if (idx == idxVal + offset) {
                    valVar.instantiateTo(vars[idx].getValue(), this, false);
                }
            } else if (idxVar.contains(idx - offset)) {  //otherwise the variable is not in scope
                if (VariableUtilities.emptyUnion(valVar, vars[idx])) {
                    idxVar.removeValue(idx - offset, this, true);
                    // NOCAUSE because if it changes the domain of IndexVar (what is not sure if idxVar
                    // uses an interval approximated domain) then it must cause updateValueFromIndex(c)
                } else {
                    updateValueFromIndex(); // both the min and max may have changed
                }
            }
        }
    }

    private void awakeOnRem(int idx, int x) throws ContradictionException {
        IntVar idxVar = getIndexVar();
        IntVar valVar = getValueVar();
        if (idx == vars.length - 2) {        // the event concerns idxVar
            updateValueFromIndex();
        } else if (idx == vars.length - 1) {  // the event concerns valVar
            if (idxVar.instantiated()) {
                int idxVal = idxVar.getValue();
                vars[idxVal + offset].removeValue(x, this, false);
            } else {
                updateIndexFromValue();
            }
        } else {                            // the event concerns a variable from the array
            if (idxVar.instantiated()) {
                int idxVal = idxVar.getValue();
                if (idx == idxVal + offset) {
                    valVar.removeValue(x, this, false);
                }
            } else if ((idxVar.contains(idx - offset)) && (valVar.hasEnumeratedDomain())) {
                boolean existsSupport = false;
                int ub = idxVar.getUB();
                for (int val = idxVar.getLB(); val <= ub; val = idxVar.nextValue(val)) {
                    int feasibleIndex = val + this.offset;
                    if (vars[feasibleIndex].contains(x)) {
                        existsSupport = true;
                    }
                }
                if (!existsSupport) {
                    valVar.removeValue(x, this, true);
                }
            }
        }
    }

    private static class RemProc implements UnaryIntProcedure<Integer> {

        private final PropElementV p;
        private int idxVar;

        public RemProc(PropElementV p) {
            this.p = p;
        }

        @Override
        public UnaryIntProcedure set(Integer idxVar) {
            this.idxVar = idxVar;
            return this;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            p.awakeOnRem(idxVar, i);
        }
    }
}
