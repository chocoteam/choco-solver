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
package org.chocosolver.solver;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
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

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    /** No entry value for maps */
    private static final int NO_ENTRY = Integer.MAX_VALUE;

    // SOLUTION
    /** Set to <tt>true</tt> when this object is empty */
    boolean empty;
    /** Maps of value for integer variable (id - value) */
    TIntIntHashMap intmap;
    /** Maps of value for real variable (id - value) */
    TIntObjectHashMap<double[]> realmap;
    /** Maps of value for set variable (id - values) */
    TIntObjectHashMap<int[]> setmap;

    // INPUT
    /** Model to store */
    Model model;
	/** Variables to store; */
    private Variable[] varsToStore;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Create an empty solution object
     * able to store the value of each variable in <code>varsToStore</code> when calling <code>record()</code>
     *
     * @param model model of the solution
     * @param varsToStore variables to store in this object
     */
    public Solution(Model model, Variable... varsToStore) {
        this.varsToStore = varsToStore;
        empty = true;
        this.model = model;
        assert varsToStore != null;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    /**
     * Records the current solution of the solver
     * clears all previous recordings
     * @return this object
     */
    public Solution record() {
        empty = false;
        boolean warn = false;
        if(varsToStore.length == 0) {
            varsToStore = model.getSolver().getStrategy().getVariables();
        }
        assert varsToStore.length>0;
        if(intmap != null) intmap.clear();
        if(realmap != null)realmap.clear();
        if(setmap != null) setmap.clear();
        for (Variable var:varsToStore) {
            if ((var.getTypeAndKind() & Variable.TYPE) != Variable.CSTE) {
                int kind = var.getTypeAndKind() & Variable.KIND;
                if (var.isInstantiated()) {
                    switch (kind) {
                        case Variable.INT:
                        case Variable.BOOL:
                            if(intmap == null) intmap = new TIntIntHashMap(16, .5f, NO_ENTRY, NO_ENTRY);
                            IntVar v = (IntVar) var;
                            intmap.put(v.getId(), v.getValue());
                            break;
                        case Variable.REAL:
                            if(realmap == null) realmap = new TIntObjectHashMap<>(16, 05f, NO_ENTRY);
                            RealVar r = (RealVar) var;
                            realmap.put(r.getId(), new double[]{r.getLB(), r.getUB()});
                            break;
                        case Variable.SET:
                            if(setmap == null) setmap = new TIntObjectHashMap<>(16, 05f, NO_ENTRY);
                            SetVar s = (SetVar) var;
                            setmap.put(s.getId(), s.getValue().toArray());
                            break;
                    }
                }else{
                    warn = true;
                }
            }
        }
        if (warn && varsToStore[0].getModel().getSettings().warnUser()) {
            model.getSolver().getOut().printf("Some non decision variables are not instantiated in the current solution.");
        }
        return this;
    }

    @Override
    public String toString() {
        if(empty)return "Empty solution. No solution recorded yet";
        StringBuilder st = new StringBuilder("Solution: ");
        for (Variable var:varsToStore) {
            if ((var.getTypeAndKind() & Variable.TYPE) != Variable.CSTE) {
                int kind = var.getTypeAndKind() & Variable.KIND;
                switch (kind) {
                    case Variable.INT:
                    case Variable.BOOL:
                        IntVar v = (IntVar) var;
                        st.append(v.getName()).append("=").append(intmap.get(v.getId())).append(", ");
                        break;
                    case Variable.REAL:
                        RealVar r = (RealVar) var;
                        double[] bounds = realmap.get(r.getId());
                        st.append(r.getName()).append("=[").append(bounds[0]).append(",").append(bounds[1]).append("], ");
                        break;
                    case Variable.SET:
                        SetVar s = (SetVar) var;
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
            throw new UnsupportedOperationException("Empty solution. No solution recorded yet");
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
            throw new UnsupportedOperationException("Empty solution. No solution recorded yet");
        }
        if (setmap.containsKey(s.getId())) {
            return setmap.get(s.getId());
        } else if ((s.getTypeAndKind() & Variable.TYPE) == Variable.CSTE) {
            return s.getValue().toArray();
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
            throw new UnsupportedOperationException("Empty solution. No solution recorded yet");
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
}
