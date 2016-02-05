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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 03/10/11
 * Time: 19:56
 */

package org.chocosolver.solver.constraints.nary.circuit;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Simple nocircuit contraint (from NoSubtour of Pesant or noCycle of Caseaux/Laburthe)
 *
 * @author Jean-Guillaume Fages
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
     * Ensures that graph has no subCircuit, with Caseaux/Laburthe/Pesant algorithm
     * runs incrementally in O(1) per instantiation event
     *
     * @param variables array of integer variables
     * @param offset offset
     */
    public PropNoSubtour(IntVar[] variables, int offset) {
        super(variables, PropagatorPriority.UNARY, true);
        n = vars.length;
        origin = new IStateInt[n];
        end = new IStateInt[n];
        size = new IStateInt[n];
        IEnvironment environment = solver.getEnvironment();
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
            vars[i].removeValue(i + offset, this);
            vars[i].updateBounds(offset, n - 1 + offset, this);
            if (vars[i].isInstantiated()) {
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
        if (isPassive()) {
            return;
        }
        int last = end[val].get(); // last in [0,n-1]
        int start = origin[var].get(); // start in [0,n-1]
        if (origin[val].get() != val) {
            contradiction(vars[var], "");
        }
        if (end[var].get() != var) {
            contradiction(vars[var], "");
        }
        if (val == start) {
            if (size[start].get() != n) {
                contradiction(vars[var], "");
            }
        } else {
            size[start].add(size[val].get());
            if (size[start].get() == n) {
                vars[last].instantiateTo(start + offset, this);
                setPassive();
            }
            boolean isInst = false;
            if (size[start].get() < n) {
                if (vars[last].removeValue(start + offset, this)) {
                    isInst = vars[last].isInstantiated();
                }
            }
            origin[last].set(start);
            end[start].set(last);
            if (isInst) {
                varInstantiated(last, vars[last].getValue() - offset);
            }
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.instantiation();
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            if (!vars[i].isInstantiated()) {
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
