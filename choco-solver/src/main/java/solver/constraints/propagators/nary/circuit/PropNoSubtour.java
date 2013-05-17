/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 03/10/11
 * Time: 19:56
 */

package solver.constraints.propagators.nary.circuit;

import gnu.trove.list.array.TIntArrayList;
import memory.IStateInt;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Simple nocircuit contraint (from NoSubtour of Pesant or noCycle of Caseaux/Laburthe)
 */
public class PropNoSubtour extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    int n;
    int offset = 0; // lower bound
    private IStateInt[] origin, end, size;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that graph has no subcircuit, with Caseaux/Laburthe/Pesant algorithm
     * runs incrementally in O(1) per instantiation event
     *
     * @param vars
     */
    public PropNoSubtour(IntVar[] vars) {
        this(vars, 0);
    }

    /**
     * Ensures that graph has no subcircuit, with Caseaux/Laburthe/Pesant algorithm
     * runs incrementally in O(1) per instantiation event
     *
     * @param variables
     * @param offset
     */
    public PropNoSubtour(IntVar[] variables, int offset) {
        super(variables, PropagatorPriority.UNARY, true);
        n = vars.length;
        origin = new IStateInt[n];
        end = new IStateInt[n];
        size = new IStateInt[n];
        for (int i = 0; i < n; i++) {
            origin[i] = environment.makeInt(i);
            end[i] = environment.makeInt(i);
            size[i] = environment.makeInt(1);
        }
        this.offset = offset;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        TIntArrayList fixedVar = new TIntArrayList();
        for (int i = 0; i < n; i++) {
            vars[i].removeValue(i + offset, aCause);
            vars[i].updateLowerBound(offset, aCause);
            vars[i].updateUpperBound(n - 1 + offset, aCause);
            if (vars[i].instantiated()) {
                fixedVar.add(i);
            }
        }
        for (int i = 0; i < fixedVar.size(); i++) {
            varInstantiated(fixedVar.get(i), vars[fixedVar.get(i)].getValue() - offset);
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        varInstantiated(idxVarInProp, vars[idxVarInProp].getValue() - offset);
    }

    /**
     * var in [0,n-1] and val in [0,n-1]
     *
     * @param var origin
     * @param val dest
     * @throws ContradictionException
     */
    private void varInstantiated(int var, int val) throws ContradictionException {
        int last = end[val].get(); // last in [0,n-1]
        int start = origin[var].get(); // start in [0,n-1]
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
                vars[last].instantiateTo(start + offset, aCause);
            }
            if (size[start].get() < n) {
                vars[last].removeValue(start + offset, aCause);
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
            i = vars[i].getValue() - offset;
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

    @Override
    public String toString() {
        return "PropNoSubTour(" + Arrays.toString(vars) + ")";
    }
}
