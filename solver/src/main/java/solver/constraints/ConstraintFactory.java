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

package solver.constraints;

import solver.Solver;
import solver.constraints.propagators.nary.PropIndexValue;
import solver.constraints.propagators.nary.PropNoSubtour;
import solver.constraints.propagators.nary.PropSubcircuit;
import solver.constraints.propagators.nary.alldifferent.PropAllDiffAC;
import solver.constraints.propagators.nary.sum.PropSumEq;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * A factory to simplify creation of <code>Constraint</code> objects, waiting for a model package.
 * This <code>ConstraintFactory</code> is not complete and does not tend to be. It only help users in declaring
 * basic and often-used constraints.
 *
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @since 0.01
 */
public class ConstraintFactory {

    protected ConstraintFactory() {
    }


    //*****************************************************************************
    // GLOBAL CONSTRAINTS
    //*****************************************************************************

    /**
     * Create an empty constraint to be filled with propagators
     *
     * @param solver
     * @return an empty constraint to be filled with propagators
     */
    public static Constraint makeEmptyConstraint(Solver solver) {
        return new Constraint(solver);
    }

    /**
     * Creates a circuit constraint which ensures that
     * <p/>
     * the elements of vars define a covering circuit
     * where vars[i] = j means that j is the successor of i.
     *
     * @param vars
     * @param solver
     * @return a circuit constraint
     */
    public static Constraint circuit(IntVar[] vars, int offset, Solver solver) {
        Constraint c = new Constraint(solver);
        c.setPropagators(
                new PropAllDiffAC(vars, c, solver),
                new PropNoSubtour(vars, offset, solver, c));
        return c;
    }

    /**
     * Creates a circuit constraint which ensures that
     * <p/>
     * the elements of vars define a covering circuit
     * where vars[i] = j means that j is the successor of i.
     *
     * @param vars
     * @param solver
     * @return a circuit constraint
     */
    public static Constraint circuit(IntVar[] vars, Solver solver) {
        return circuit(vars, 0, solver);
    }

    /**
     * Creates a subcircuit constraint which ensures that
     * <p/>
     * the elements of vars define a single circuit of subcircuitSize nodes
     * where vars[i] = j means that j is the successor of i.
     * and vars[i] = i means that i is not part of the circuit
     *
     * @param vars
     * @param offset
     * @param subcircuitSize expected number of nodes in the circuit
     * @param solver
     * @return a circuit constraint
     */
    public static Constraint subcircuit(IntVar[] vars, int offset, IntVar subcircuitSize, Solver solver) {
        int n = vars.length;
        IntVar nbLoops = VariableFactory.bounded("nLoops", 0, n, solver);
        Constraint c = new Constraint(solver);
        c.addPropagators(new PropSumEq(new IntVar[]{nbLoops, subcircuitSize}, new int[]{1, 1}, 2, n, solver, c));
        c.addPropagators(new PropAllDiffAC(vars, c, solver));
        c.addPropagators(new PropIndexValue(vars, offset, nbLoops, c, solver));
        c.addPropagators(new PropSubcircuit(vars, offset, subcircuitSize, c, solver));
        return c;
    }

    /**
     * Creates a subcircuit constraint which ensures that
     * <p/>
     * the elements of vars define a single circuit of subcircuitSize nodes
     * where vars[i] = j means that j is the successor of i.
     * and vars[i] = i means that i is not part of the circuit
     *
     * @param vars
     * @param subcircuitSize expected number of nodes in the circuit
     * @param solver
     * @return a circuit constraint
     */
    public static Constraint subcircuit(IntVar[] vars, IntVar subcircuitSize, Solver solver) {
        return subcircuit(vars, 0, subcircuitSize, solver);
    }

    /**
     * Creates a subcircuit constraint which ensures that
     * <p/>
     * the elements of vars define a single circuit
     * where vars[i] = j means that j is the successor of i.
     * and vars[i] = i means that i is not part of the circuit
     *
     * @param vars
     * @param offset
     * @param solver
     * @return a circuit constraint
     */
    public static Constraint subcircuit(IntVar[] vars, int offset, Solver solver) {
        return subcircuit(vars, offset, VariableFactory.bounded("subcircuit length", 0, vars.length, solver), solver);
    }

    /**
     * Creates a subcircuit constraint which ensures that
     * <p/>
     * the elements of vars define a single circuit
     * where vars[i] = j means that j is the successor of i.
     * and vars[i] = i means that i is not part of the circuit
     *
     * @param vars
     * @param solver
     * @return a circuit constraint
     */
    public static Constraint subcircuit(IntVar[] vars, Solver solver) {
        return subcircuit(vars, 0, VariableFactory.bounded("subcircuit length", 0, vars.length, solver), solver);
    }

}
