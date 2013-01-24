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
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.VariableFactory;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.graph.GraphVar;

/**
 * Constraints over set variables
 * instead of from 0 to n-1 (Java stupid standard)
 * @author Jean-Guillaume Fages
 */
public final class SetConstraintsFactory {

	private SetConstraintsFactory(){}

	//***********************************************************************************
	// BASICS : union/inter/subset/card
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

	/**
	 * Constraint which ensures that i<j <=> sets[i] subseteq sets[j]
	 * @param sets
	 * @param solver
	 * @return A constraint which ensures that i<j <=> sets[i] subseteq sets[j]
	 */
	public static Constraint subsetEq(SetVar[] sets, Solver solver) {
		Constraint c = new Constraint(sets,solver);
		for(int i=0;i<sets.length-1;i++){
			c.addPropagators(new PropSubsetEq(sets[i], sets[i+1], solver, c));
		}
		return c;
	}

	/**
	 * Cardinality constraint: |set| = card
	 * @param set
	 * @param card
	 * @param solver
	 * @return A constraint ensuring that |set| = card
	 */
	public static Constraint cardinality(SetVar set, IntVar card, Solver solver) {
		Constraint c = new Constraint(solver);
		c.setPropagators(new PropCardinality(set, card, solver, c));
		return c;
	}

	// TODO nbEmpty

	// TODO offSet (passer par une vue?)

	//***********************************************************************************
	// SUM - MAX - MIN
	//***********************************************************************************

	/**
	 * Sums elements of a set
	 * SUM{i | i in set} = sum
	 * @param set
	 * @param sum
	 * @param solver
	 * @return
	 */
	public static Constraint sum(SetVar set, IntVar sum, Solver solver) {
		return sum(set,null,0,sum,solver);
	}

	/**
	 * Sums weights given by a set of indexes
	 * SUM(weights[i-offset] | i in indexes) = sum
	 * @param indexes
	 * @param weights
	 * @param offset (0 by default but generally 1 with MiniZinc API)
	 * @param sum
	 * @param solver
	 * @return
	 */
	public static Constraint sum(SetVar indexes, int[] weights, int offset, IntVar sum, Solver solver) {
		Constraint c = new Constraint(solver);
		c.setPropagators(new PropSumOfElements(indexes, weights, offset, sum, solver, c));
		return c;
	}

	/**
	 * Retrieves the maximum element of the set
	 * MAX{i | i in set} = maxElement
	 * @param set
	 * @param maxElement
	 * @param solver
	 * @return
	 */
	public static Constraint max(SetVar set, IntVar maxElement, Solver solver) {
		return max(set, null, 0, maxElement, solver);
	}

	/**
	 * Retrieves the maximum element induced by a set
	 * MAX{weights[i-offSet] |Êi in set} = maxElement
	 * @param set
	 * @param weights
	 * @param offSet (0 by default but generally 1 with MiniZinc API)
	 * @param maxElement
	 * @param solver
	 * @return
	 */
	public static Constraint max(SetVar set, int[] weights, int offSet, IntVar maxElement, Solver solver) {
		Constraint c = new Constraint(solver);
		c.setPropagators(new PropMaxElement(set, weights, offSet, maxElement, solver, c));
		return c;
	}

	/**
	 * Retrieves the minimum element of the set
	 * MIN{i | i in set} = minElement
	 * @param set
	 * @param minElement
	 * @param solver
	 * @return
	 */
	public static Constraint min(SetVar set, IntVar minElement, Solver solver) {
		return min(set, null, 0, minElement, solver);
	}

	/**
	 * Retrieves the minimum element induced by set
	 * MIN{weights[i-offSet] | i in set} = minElement
	 * @param set
	 * @param weights
	 * @param offSet (0 by default but generally 1 with MiniZinc API)
	 * @param minElement
	 * @param solver
	 * @return
	 */
	public static Constraint min(SetVar set, int[] weights, int offSet, IntVar minElement, Solver solver) {
		Constraint c = new Constraint(solver);
		c.setPropagators(new PropMinElement(set, weights, offSet, minElement, solver, c));
		return c;
	}

	//***********************************************************************************
	// CHANNELING CONSTRAINTS : bool/int/graph
	//***********************************************************************************

	/**
	 * Channeling between a set variable and boolean variables
	 * i in set <=> bools[i-offSet] = TRUE
	 * @param set
	 * @param bools
	 * @param offSet (0 by default but generally 1 with MiniZinc API)
	 * @param solver
	 * @return
	 */
	public static Constraint bool_channel(SetVar set, BoolVar[] bools, int offSet, Solver solver) {
		Constraint c = new Constraint(solver);
		c.setPropagators(new PropBoolChannel(set, bools, offSet, solver, c));
		return c;
	}

	/**
	 * Channeling between set variables and integer variables
	 * x in sets[y-offSet1] <=> ints[x-offSet2] = y
	 * @param sets
	 * @param ints
	 * @param offSet1 (0 by default but generally 1 with MiniZinc API)
	 * @param offSet2 (0 by default but generally 1 with MiniZinc API)
	 * @param solver
	 * @return
	 */
	public static Constraint int_channel(SetVar[] sets, IntVar[] ints, int offSet1, int offSet2, Solver solver) {
		Constraint c = new Constraint(solver);
		c.setPropagators(new PropIntChannel(sets, ints, offSet1, offSet2, solver, c));
		return c;
	}

	/**
	 * Channeling between a graph variable and set variables
	 * representing either node neighbors or node successors
	 * @param sets
	 * @param g
	 * @param solver
	 * @return
	 */
	public static Constraint graph_channel(SetVar[] sets, GraphVar g, Solver solver) {
		Constraint c = new Constraint(solver);
		c.setPropagators(new PropGraphChannel(sets, g, solver, c));
		if(!g.isDirected()){
			c.addPropagators(new PropSymmetric(sets,0,solver,c));
		}
		return c;
	}

	/**
	 * Channeling between a directed graph variable and set variables
	 * representing node successors and predecessors
	 * @param succs
	 * @param preds
	 * @param g
	 * @param solver
	 * @return
	 */
	public static Constraint graph_channel(SetVar[] succs, SetVar[] preds, DirectedGraphVar g, Solver solver) {
		Constraint c = graph_channel(succs, g, solver);
		c.addPropagators(new PropInverse(succs,preds,0,0,solver,c));
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
				new PropAtMost1Empty(sets,solver,c));// TODO really?
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
		c.setPropagators(	new PropAllDiff(sets,solver,c),
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

	/**
	 * Inverse set constraint
	 * x in sets[y-offSet1] <=> y in inverses[x-offSet2]
	 * @param sets
	 * @param inverses
	 * @param offSet1 (0 by default but generally 1 with MiniZinc API)
	 * @param offSet2 (0 by default but generally 1 with MiniZinc API)
	 * @param solver
	 * @return
	 */
	public static Constraint inverse_set(SetVar[] sets, SetVar[] inverses, int offSet1, int offSet2, Solver solver) {
		Constraint c = new Constraint(solver);
		c.setPropagators(new PropInverse(sets, inverses, offSet1, offSet2, solver, c));
		return c;
	}

	/**
	 * Symmetric sets constraint
	 * x in sets[y-offSet] <=> y in sets[x-offSet]
	 * @param sets
	 * @param offSet (0 by default but generally 1 with MiniZinc API)
	 * @param solver
	 * @return
	 */
	public static Constraint symmetric(SetVar[] sets, int offSet, Solver solver) {
		Constraint c = new Constraint(solver);
		c.setPropagators(new PropSymmetric(sets, offSet, solver, c));
		return c;
	}

	/**
	 * Element constraint over sets
	 * states that array[index-offSet] = set
	 * @param index
	 * @param array
	 * @param offSet (0 by default but generally 1 with MiniZinc API)
	 * @param set
	 * @param solver
	 * @return
	 */
	public static Constraint element(IntVar index, SetVar[] array, int offSet, SetVar set, Solver solver) {
		Constraint c = new Constraint(solver);
		c.setPropagators(new PropElement(index, array, offSet, set, solver, c));
		return c;
	}

	/**
	 * Member constraint over sets
	 * states that set belongs to array
	 * @param array
	 * @param set
	 * @param solver
	 * @return
	 */
	public static Constraint member(SetVar[] array, SetVar set, Solver solver) {
		IntVar index = VariableFactory.enumerated("idx_tmp",0,array.length-1,solver);
		return element(index, array, 0, set, solver);
	}

	/**
	 * Member constraint over an IntVar and a SetVar
	 * states that intVar is in SetVar
	 * @param setVar
	 * @param intVar
	 * @param solver
	 * @return
	 */
	public static Constraint member(SetVar setVar, IntVar intVar, Solver solver) {
		Constraint c = new Constraint(solver);
		c.setPropagators(new PropIntMemberSet(setVar, intVar, solver, c));
		return c;
	}
}
