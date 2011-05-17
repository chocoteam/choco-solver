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

package solver.variables;

import solver.Solver;
import solver.exception.SolverException;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.variables.domain.BitSetIntDomain;
import solver.variables.domain.BooleanDomain;
import solver.variables.domain.IntervalIntDomain;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import solver.variables.image.IntVarAddCste;
import solver.variables.image.IntVarTimesPosCste;
import solver.variables.image.OppIntVar;

import java.util.BitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public class VariableFactory {

    //TODO : build domain in Variable

    protected VariableFactory() {
    }

    private static void checkIntVar(String name, int min, int max) {
        if (min - Integer.MIN_VALUE == 0 || max - Integer.MAX_VALUE == 0) {
            throw new SolverException(name+": consider reducing the bounds to avoid unexpected results");
        }
        if (min - max > 0) {
            throw new SolverException(name+": wrong domain definition, lower bound > upper bound");
        }
    }

    /**
     * Build a boolean variable, ie domain is [0,1]
     *
     * @param name   name of variable
     * @param solver solver involving the variable
     * @return a BoolVar
     */
    public static BoolVar bool(String name, Solver solver) {
        BoolVarImpl var = new BoolVarImpl(name, solver);
        var.domain = new BooleanDomain(solver.getEnvironment());
        var.heuristicVal = HeuristicValFactory.presetI(var);
        solver.associates(var);
        return var;
    }

    public static BoolVar[] boolArray(String name, int size, Solver solver) {
        BoolVar[] vars = new BoolVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = bool(name + "_" + i, solver);
        }
        return vars;
    }

    public static IntVar bounded(String name, int min, int max, Solver solver) {
        checkIntVar(name, min, max);
        if (min == max) {
            return fixed(name, min);
        } else {
            IntVarImpl var = new IntVarImpl(name, solver);
            var.domain = new IntervalIntDomain(min, max, solver.getEnvironment());
            var.heuristicVal = HeuristicValFactory.presetI(var);
            solver.associates(var);
            return var;
        }
    }

    public static IntVar[] boundedArray(String name, int size, int min, int max, Solver solver) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = bounded(name + "_" + i, min, max, solver);
        }
        return vars;
    }

    public static IntVar enumerated(String name, int min, int max, Solver solver) {
        checkIntVar(name, min, max);
        if (min == max) {
            return fixed(name, min);
        } else {
            IntVarImpl var = new IntVarImpl(name, solver);
            var.domain = new BitSetIntDomain(min, max, solver.getEnvironment());
            var.heuristicVal = HeuristicValFactory.presetI(var);
            solver.associates(var);
            return var;
        }
    }

    public static IntVar[] enumeratedArray(String name, int size, int min, int max, Solver solver) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = enumerated(name + "_" + i, min, max, solver);
        }
        return vars;
    }

    public static IntVar enumerated(String name, int[] values, Solver solver) {
        checkIntVar(name, values[0], values[values.length - 1]);
        if (values.length == 1) {
            return fixed(name, values[0]);
        } else {
            IntVarImpl var = new IntVarImpl(name, solver);
            var.domain = new BitSetIntDomain(values, solver.getEnvironment());
            var.heuristicVal = HeuristicValFactory.presetI(var);
            solver.associates(var);
            return var;
        }
    }

    public static IntVar[] enumeratedArray(String name, int size, int[] values, Solver solver) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = enumerated(name + "_" + i, values, solver);
        }
        return vars;
    }


    public static IntVar fixed(int value) {
        return fixed("cste -- " + value, value);
    }

    public static IntVar fixed(String name, int value) {
        if (value == 0 || value == 1) {
            BoolCste var = new BoolCste(name, value);
//            solver.associates(var);
            return var;
        } else {
            IntCste var = new IntCste("cste -- " + value, value);
//            solver.associates(var);
            return var;
        }
    }

    public static IntVar addCste(IntVar ivar, int cste) {
        return new IntVarAddCste(ivar, cste);
    }

    public static IntVar timesPosCste(IntVar ivar, int cste) {
        IntVar var;
        if (cste < 0) {
            //var = new OppIntVar(new IntVarTimesPosCste(ivar, -cste));
            throw new UnsupportedOperationException("timesPosCste required positive coefficient! " +
                    "(due to opposite var bug...)");
        } else {
            var = new IntVarTimesPosCste(ivar, cste);
        }
        return var;
    }

    public static IntVar opposite(IntVar ivar) {
        return new OppIntVar(ivar);
    }

    public static IntVar[] toIntVar(Variable... variables) {
        IntVar[] ivars = new IntVar[variables.length];
        for (int i = 0; i < variables.length; i++) {
            ivars[i] = (IntVar) variables[i];
        }
        return ivars;
    }

    public static DirectedGraphVar digraph(String string, int n, GraphType type, Solver solver, String options) {
        return new DirectedGraphVar(solver.getEnvironment(), n, type, options);
    }

    public static DirectedGraphVar digraph(String string, BitSet[] data, GraphType typeEnv, GraphType typeKer, Solver solver) {
        return new DirectedGraphVar(solver.getEnvironment(), data, typeEnv, typeKer);
    }

    public static DirectedGraphVar digraph(String string, BitSet[] data, GraphType type, Solver solver) {
        return digraph(string, data, type, type, solver);
    }

    public static UndirectedGraphVar undirectedGraph(String string, BitSet[] data, GraphType typeEnv, GraphType typeKer, Solver solver) {
        return new UndirectedGraphVar(solver.getEnvironment(), data, typeEnv, typeKer);
    }
}
