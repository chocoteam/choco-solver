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
package org.chocosolver.solver.search.solution;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Class which stores the value of each variable in a solution
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 * @since 05/06/2013
 */
public class Solution implements Serializable, ICause {

    private static final int NO_ENTRY = Integer.MAX_VALUE;

    TIntIntHashMap intmap;
    TIntObjectHashMap<double[]> realmap;
    TIntObjectHashMap<int[]> setmap;
    TIntHashSet dvars;
    boolean empty;

    @SuppressWarnings("unchecked")
    public Solution() {
        intmap = new TIntIntHashMap(16, .5f, NO_ENTRY, NO_ENTRY);
        realmap = new TIntObjectHashMap(16, 05f, NO_ENTRY);
        setmap = new TIntObjectHashMap(16, 05f, NO_ENTRY);
        dvars = new TIntHashSet(16, .5f, -1);
        empty = true;
    }

    /**
     * Records the current solution of the solver
     * clears all previous recordings
     *
     * @param solver a solver
     */
    public void record(Solver solver) {
        if (empty) {
            Variable[] _dvars = solver.getStrategy().getVariables();
            for (int i = 0; i < _dvars.length; i++) {
                dvars.add(_dvars[i].getId());
            }
            empty = false;
        }
        boolean warn = false;
        intmap.clear();
        realmap.clear();
        setmap.clear();
        Variable[] vars = solver.getVars();
        for (int i = 0; i < vars.length; i++) {
            if ((vars[i].getTypeAndKind() & Variable.TYPE) != Variable.CSTE) {
                int kind = vars[i].getTypeAndKind() & Variable.KIND;
                if (!vars[i].isInstantiated()) {
                    if (dvars.contains(vars[i].getId())) {
                        throw new SolverException(vars[i] + " is not instantiated when recording a solution.");
                    } else {
                        warn = true;
                    }
                } else {
                    switch (kind) {
                        case Variable.INT:
                        case Variable.BOOL:
                            IntVar v = (IntVar) vars[i];
                            intmap.put(v.getId(), v.getValue());
                            break;
                        case Variable.REAL:
                            RealVar r = (RealVar) vars[i];
                            realmap.put(r.getId(), new double[]{r.getLB(), r.getUB()});
                            break;
                        case Variable.SET:
                            SetVar s = (SetVar) vars[i];
                            setmap.put(s.getId(), s.getValues());
                            break;
                    }
                }
            }
        }
        if (warn && solver.getSettings().warnUser()) {
            Chatterbox.err.printf("Some non decision variables are not instantiated in the current solution.");
        }
    }

    /**
     * Set all variables to their respective value in the solution
     * Throws an exception is this empties a domain (i.e. this domain does not contain
     * the solution value)
     * <p>
     * BEWARE: A restart might be required so that domains contain the solution values
     */
    public void restore(Solver solver) throws ContradictionException {
        if (empty) {
            throw new UnsupportedOperationException("Empty solution. No solution found");
        }
        Variable[] vars = solver.getVars();
        for (int i = 0; i < vars.length; i++) {
            if ((vars[i].getTypeAndKind() & Variable.TYPE) != Variable.CSTE) {
                int kind = vars[i].getTypeAndKind() & Variable.KIND;
                switch (kind) {
                    case Variable.INT:
                    case Variable.BOOL:
                        IntVar v = (IntVar) vars[i];
                        int value = intmap.get(v.getId());
                        if(value != NO_ENTRY){
                            v.instantiateTo(value, this);
                        } // otherwise, this is not a decision variable
                        break;
                    case Variable.REAL:
                        RealVar r = (RealVar) vars[i];
                        double[] bounds = realmap.get(r.getId());
                        if(bounds != null){
                            r.updateBounds(bounds[0], bounds[1], this);
                        }  // otherwise, this is not a decision variable
                        break;
                    case Variable.SET:
                        SetVar s = (SetVar) vars[i];
                        int[]values = setmap.get(s.getId());
                        if(values != null){
                            s.instantiateTo(values, Cause.Null);
                        } // otherwise, this is not a decision variable
                        break;
                }
            }
        }
    }

    public String toString(Solver solver) {
        Variable[] vars = solver.getVars();
        StringBuilder st = new StringBuilder("Solution: ");
        for (int i = 0; i < vars.length; i++) {
            if ((vars[i].getTypeAndKind() & Variable.TYPE) != Variable.CSTE) {
                int kind = vars[i].getTypeAndKind() & Variable.KIND;
                switch (kind) {
                    case Variable.INT:
                    case Variable.BOOL:
                        IntVar v = (IntVar) vars[i];
                        st.append(v.getName()).append("=").append(intmap.get(v.getId())).append(", ");
                        break;
                    case Variable.REAL:
                        RealVar r = (RealVar) vars[i];
                        double[] bounds = realmap.get(r.getId());
                        st.append(r.getName()).append("=[").append(bounds[0]).append(",").append(bounds[1]).append("], ");
                        break;
                    case Variable.SET:
                        SetVar s = (SetVar) vars[i];
                        st.append(s.getName()).append("=").append(Arrays.toString(setmap.get(s.getId()))).append(", ");
                        break;
                }
            }
        }
        return st.toString();
    }

    /**
     * Get the value of variable v in this solution
     *
     * @param v IntVar (or BoolVar)
     * @return the value of variable v in this solution, or null if the variable is not instantiated in the solution
     */
    public Integer getIntVal(IntVar v) {
        if (empty) {
            throw new UnsupportedOperationException("Empty solution. No solution found");
        }
        if (intmap.containsKey(v.getId())) {
            return intmap.get(v.getId());
        } else {
            if ((v.getTypeAndKind() & Variable.TYPE) == Variable.CSTE) {
                return v.getValue();
            } else {
                return null;
            }
        }
    }

    /**
     * Get the value of variable s in this solution
     *
     * @param s SetVar
     * @return the value of variable s in this solution, or null if the variable is not instantiated in the solution
     */
    public int[] getSetVal(SetVar s) {
        if (empty) {
            throw new UnsupportedOperationException("Empty solution. No solution found");
        }
        if (setmap.containsKey(s.getId())) {
            return setmap.get(s.getId());
        } else if ((s.getTypeAndKind() & Variable.TYPE) == Variable.CSTE) {
            return s.getValues();
        } else {
            return null;
        }
    }

    /**
     * Get the bounds of r in this solution
     *
     * @param r RealVar
     * @return the bounds of r in this solution, or null if the variable is not instantiated in the solution
     */
    public double[] getRealBounds(RealVar r) {
        if (empty) {
            throw new UnsupportedOperationException("Empty solution. No solution found");
        }
        if (realmap.containsKey(r.getId())) {
            return realmap.get(r.getId());
        } else {
            if ((r.getTypeAndKind() & Variable.TYPE) == Variable.CSTE) {
                return new double[]{r.getLB(), r.getUB()};
            } else {
                return null;
            }
        }
    }

    /**
     * @return true iff this is a valid solution
     */

    public boolean hasBeenFound() {
        return !empty;
    }
}
