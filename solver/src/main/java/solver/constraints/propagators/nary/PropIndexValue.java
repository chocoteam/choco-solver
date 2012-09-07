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

package solver.constraints.propagators.nary;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * PropIndexValue(vars,nb) ensures that the number of variable such that vars[i] = i is equal to nb
 * <p/>
 * can be used within the SubCircuit constraint for instance: a subcircuit of length k involves n-k loops
 *
 * @author Jean-Guillaume Fages
 */
public class PropIndexValue extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
    private int offset; // lower bound
    private IntVar nb;
    private IStateInt minLoops, maxLoops;
    private IStateBool[] possible;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropIndexValue(IntVar[] vars, IntVar nb, Constraint constraint, Solver solver) {
        this(vars, 0, nb, constraint, solver);
    }

    public PropIndexValue(IntVar[] vars, int offset, IntVar nb, Constraint constraint, Solver solver) {
        super(ArrayUtils.append(vars, new IntVar[]{nb}), solver, constraint, PropagatorPriority.LINEAR, true);
        n = vars.length;
        this.nb = nb;
        this.offset = offset;
        minLoops = environment.makeInt();
        maxLoops = environment.makeInt();
        possible = new IStateBool[n];
        for (int i = 0; i < n; i++) {
            possible[i] = environment.makeBool(false);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int min = 0;
        int max = 0;
        boolean b;
        for (int i = 0; i < n; i++) {
            b = vars[i].contains(i + offset);
            possible[i].set(b);
            if (b) {
                max++;
                if (vars[i].instantiated()) {
                    min++;
                }
            }
        }
        minLoops.set(min);
        maxLoops.set(max);
        filter();
    }

    private void filter() throws ContradictionException {
        int min = minLoops.get();
        int max = maxLoops.get();
        nb.updateLowerBound(min, aCause);
        nb.updateUpperBound(max, aCause);
        if (min != max && nb.instantiated()) {
            if (min == nb.getValue()) {
                for (int i = 0; i < n; i++) {
                    if (!vars[i].instantiated()) {
                        vars[i].removeValue(i + offset, aCause);
                    }
                }
                maxLoops.set(min);
                setPassive();
            } else if (max == nb.getValue()) {
                for (int i = 0; i < n; i++) {
                    if (vars[i].contains(i)) {
                        vars[i].instantiateTo(i + offset, aCause);
                    }
                }
                minLoops.set(max);
                setPassive();
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < n) {
            // il avait une boucle avant
            if (possible[idxVarInProp].get()) {
                IntVar v = vars[idxVarInProp];
                if (v.instantiated() && v.getValue() == idxVarInProp + offset) {
                    minLoops.add(1);
                }
                boolean b = v.contains(idxVarInProp + offset);
                if (!b) {
                    maxLoops.add(-1);
                    possible[idxVarInProp].set(false);
                }
            }
        }
        filter();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    @Override
    public ESat isEntailed() {
        int min = 0;
        int max = 0;
        for (int i = 0; i < n; i++) {
            if (vars[i].contains(i + offset)) {
                max++;
                if (vars[i].instantiated()) {
                    min++;
                }
            }
        }
        if (min > nb.getUB() || max < nb.getLB()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
