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


package solver.constraints.set;

import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.setDataStructures.ISet;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.set.*;
import solver.variables.SetVar;
import solver.variables.VariableFactory;

/**
 * @author Jean-Guillaume Fages
 */
public final class SetConstraintsFactory {

	private SetConstraintsFactory(){}

	//***********************************************************************************
	// BASICS
	//***********************************************************************************

	/**
	 * The union of sets is equal to union
	 * @param sets
	 * @param union
	 * @param solver
	 * @return A constraint ensuring that the union of sets is equal to set union
	 */
	public static Constraint union(SetVar[] sets, SetVar union, Solver solver) {
		Constraint c = new Constraint(ArrayUtils.append(sets,new SetVar[]{union}),solver);
		c.setPropagators(new PropUnion(sets, union, solver, c));
		return c;
	}

	/**
	 * The intersection of sets is equal to intersection
	 * @param sets
	 * @param intersection
	 * @param solver
	 * @return A constraint ensuring that the intersection of sets is equal to set intersection
	 */
	public static Constraint intersection(SetVar[] sets, SetVar intersection, Solver solver) {
		Constraint c = new Constraint(ArrayUtils.append(sets,new SetVar[]{intersection}),solver);
		c.setPropagators(new PropIntersection(sets, intersection, solver, c));
		return c;
	}

	//***********************************************************************************
	// MINIZINC API
	//***********************************************************************************

	/**
	 * set1 and set2 are disjoint, they cannot contain the same element
	 * @param set1
	 * @param set2
	 * @param solver
	 * @return a constraint ensuring that set1 and set2 are disjoint
	 */
	public static Constraint disjoint(SetVar set1, SetVar set2, Solver solver) {
		return all_disjoint(new SetVar[]{set1,set2},solver);
	}

	/**
	 * Sets are all disjoints
	 * there cannot be more than one empty sets
	 * @param sets
	 * @param solver
	 * @return a constraint ensuring that sets are all disjoint
	 */
	public static Constraint all_disjoint(SetVar[] sets, Solver solver) {
		Constraint c = new Constraint(sets, solver);
		c.setPropagators(	new PropAllDisjoint(sets,solver,c),
							new PropAtMost1Empty(sets,solver,c));
		return c;
	}

	/**
	 * Sets are all different (not necessarily disjoint)
	 * there cannot be more than two empty sets
	 * @param sets
	 * @param solver
	 * @return a constraint ensuring that sets are all different
	 */
	public static Constraint all_different(SetVar[] sets, Solver solver) {
		Constraint c = new Constraint(sets, solver);
		c.setPropagators(	new PropAllDiff_Set(sets,solver,c),
							new PropAtMost1Empty(sets,solver,c));
		return c;
	}

	/**
	 * Sets are all equal
	 * @param sets
	 * @param solver
	 * @return a constraint ensuring that sets are all equal
	 */
	public static Constraint all_equal(SetVar[] sets, Solver solver) {
		Constraint c = new Constraint(sets, solver);
		c.setPropagators(new PropAllEqual(sets,solver,c));
		return c;
	}

	/**
	 * Partition universe into disjoint sets
	 * @param sets
	 * @param universe
	 * @param solver
	 * @return a constraint which ensures that sets form a partition of universe
	 */
	public static Constraint partition(SetVar[] sets, ISet universe, Solver solver) {
		SetVar union = VariableFactory.set("union",universe,universe,solver);
		Constraint c = all_disjoint(sets,solver);
		c.setPropagators(new PropUnion(sets, union, solver, c));
		return c;
	}
}
