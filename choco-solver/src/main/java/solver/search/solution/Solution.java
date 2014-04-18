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

package solver.search.solution;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.variables.IntVar;
import solver.variables.RealVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Class which stores the value of each variable in a solution
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 * @since 05/06/2013
 */
public class Solution implements ICause {

    HashMap<IntVar, Integer> intmap = new HashMap<>();
    HashMap<RealVar, double[]> realmap = new HashMap<>();
    HashMap<SetVar, int[]> setmap = new HashMap<>();
    HashMap<GraphVar, boolean[][]> graphmap = new HashMap<>();
    boolean empty = true;

    public Solution() {
    }

    /**
     * Records the current solution of the solver
     * clears all previous recordings
     *
     * @param solver
     */
    public void record(Solver solver) {
        empty = false;
        intmap.clear();
        realmap.clear();
        setmap.clear();
        graphmap.clear();
        Variable[] vars = solver.getVars();
        for (int i = 0; i < vars.length; i++) {
            int kind = vars[i].getTypeAndKind() & Variable.KIND;
            assert (vars[i].isInstantiated()) :
                    vars[i] + " is not instantiated when recording a solution.";
            switch (kind) {
                case Variable.INT:
                case Variable.BOOL:
                    IntVar v = (IntVar) vars[i];
                    intmap.put(v, v.getValue());
                    break;
                case Variable.REAL:
                    RealVar r = (RealVar) vars[i];
                    realmap.put(r, new double[]{r.getLB(), r.getUB()});
                    break;
                case Variable.SET:
                    SetVar s = (SetVar) vars[i];
                    setmap.put(s, s.getValue());
                    break;
                case Variable.GRAPH:
                    GraphVar g = (GraphVar) vars[i];
                    graphmap.put(g, g.getValue());
                    break;
            }
        }
    }

    /**
     * Set all variables to their respective value in the solution
     * Throws an exception is this empties a domain (i.e. this domain does not contain
     * the solution value)
     * <p/>
     * BEWARE: A restart might be required so that domains contain the solution values
     */
    public void restore() throws ContradictionException {
        if (empty) {
            throw new UnsupportedOperationException("Empty solution. No solution found");
        }
        for (IntVar i : intmap.keySet()) {
            i.instantiateTo(intmap.get(i), this);
        }
        for (SetVar s : setmap.keySet()) {
            s.instantiateTo(setmap.get(s), this);
        }
        for (GraphVar g : graphmap.keySet()) {
            g.instantiateTo(graphmap.get(g), this);
        }
        for (RealVar r : realmap.keySet()) {
            double[] bounds = realmap.get(r);
            r.updateBounds(bounds[0], bounds[1], this);
        }
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        e.add(Explanation.SYSTEM.get());
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder("Solution: ");
        for (IntVar i : intmap.keySet()) {
            st.append(i.getName()).append("=").append(intmap.get(i)).append(", ");
        }
        for (SetVar s : setmap.keySet()) {
            st.append(s.getName()).append("=").append(Arrays.toString(setmap.get(s))).append(", ");
        }
        for (GraphVar g : graphmap.keySet()) {
            st.append(g.getName()).append("=").append(Arrays.toString(graphmap.get(g))).append(", ");
        }
        for (RealVar r : realmap.keySet()) {
            double[] bounds = realmap.get(r);
            st.append(r.getName()).append("=[").append(bounds[0]).append(",").append(bounds[1]).append("], ");
        }

        return st.toString();
    }

    /**
     * Get the value of variable v in this solution
     *
     * @param v IntVar (or BoolVar)
     * @return the value of variable v in this solution
     */
    public int getIntVal(IntVar v) {
        if (empty) {
            throw new UnsupportedOperationException("Empty solution. No solution found");
        }
        return intmap.get(v);
    }

    ;

    /**
     * Get the value of variable s in this solution
     *
     * @param s SetVar
     * @return the value of variable s in this solution
     */
    public int[] getSetVal(SetVar s) {
        if (empty) {
            throw new UnsupportedOperationException("Empty solution. No solution found");
        }
        return setmap.get(s);
    }

    ;

    /**
     * Get the value of variable g in this solution
     *
     * @param g GraphVar
     * @return the value of variable g in this solution
     */
    public boolean[][] getGraphVal(GraphVar g) {
        if (empty) {
            throw new UnsupportedOperationException("Empty solution. No solution found");
        }
        return graphmap.get(g);
    }

    ;

    /**
     * Get the bounds of r in this solution
     *
     * @param r RealVar
     * @return the bounds of r in this solution
     */
    public double[] getRealBounds(RealVar r) {
        if (empty) {
            throw new UnsupportedOperationException("Empty solution. No solution found");
        }
        return realmap.get(r);
    }

    ;

    /**
     * @return true iff this is a valid solution
     */
    public boolean hasBeenFound() {
        return !empty;
    }
}
