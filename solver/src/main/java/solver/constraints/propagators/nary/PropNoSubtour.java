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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 03/10/11
 * Time: 19:56
 */

package solver.constraints.propagators.nary;

import choco.kernel.ESat;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import java.util.BitSet;

/**
 * Simple nocircuit contraint (from NoSubtour of Pesant or noCycle of Caseaux/Laburthe)
 */
public class PropNoSubtour<V extends IntVar> extends Propagator<V> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    int n;
    private IStateInt[] origin, end, size;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that graph has no subcircuit, with Caseaux/Laburthe/Pesant algorithm
     * runs incrementally in O(1) per instantiation event
     *
     * @param vars
     * @param constraint
     * @param solver
     */
    public PropNoSubtour(V[] vars, Solver solver, Constraint constraint) {
        super(vars, solver, constraint, PropagatorPriority.UNARY, true);
        n = vars.length;
        origin = new IStateInt[n];
        end = new IStateInt[n];
        size = new IStateInt[n];
        for (int i = 0; i < n; i++) {
            origin[i] = environment.makeInt(i);
            end[i] = environment.makeInt(i);
            size[i] = environment.makeInt(1);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        TIntArrayList fixedVar = new TIntArrayList();
        for (int i = 0; i < n; i++) {
            if (vars[i].instantiated()) {
                fixedVar.add(i);
            }
        }
        for (int i = 0; i < fixedVar.size(); i++) {
            varInstantiated(fixedVar.get(i), vars[fixedVar.get(i)].getValue());
        }
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        varInstantiated(idxVarInProp, vars[idxVarInProp].getValue());
    }

    private void varInstantiated(int var, int val) throws ContradictionException {
        if (!vars[var].instantiated()) {
            throw new UnsupportedOperationException();
        }
        int last = end[val].get();
        int start = origin[var].get();
        if (origin[val].get() != val) {
            contradiction(vars[var], "");
        }
        if (val == start) {
            if (size[start].get() != n) {
                contradiction(vars[var], "");
            }
        } else {
            size[start].add(size[val].get());
            if (size[start].get() == n) {
                vars[last].instantiateTo(start, this, false);
            }
            if (size[start].get() < n) {
                vars[last].removeValue(start, this, false);
            }
            origin[last].set(start);
            end[start].set(last);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask;
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            if (!vars[i].instantiated()) {
                return ESat.UNDEFINED;
            }
        }
        BitSet visited = new BitSet(n);
        int i = 0;
        int size = 0;
        while (size != n) {
            size++;
            i = vars[i].getValue();
            if (visited.get(i)) {
                return ESat.FALSE;
            }
            visited.set(i);
        }
        if (i == 0) {
            return ESat.TRUE;
        } else {
            return ESat.FALSE;
        }
    }
}
