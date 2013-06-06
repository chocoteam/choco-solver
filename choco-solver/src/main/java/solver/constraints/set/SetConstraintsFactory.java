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

import solver.Solver;
import solver.constraints.Constraint;
import solver.variables.*;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.graph.GraphVar;
import util.tools.ArrayUtils;

/**
 * Constraints over set variables
 *
 * @author Jean-Guillaume Fages
 */
public class SetConstraintsFactory {

    SetConstraintsFactory() {
    }

    //***********************************************************************************
    // BASICS : union/inter/subset/card
    //***********************************************************************************

    /**
     * Constraint which ensures that the union of sets in SET_VARS is equal to the set SET_UNION
     *
     * @param SETS  set variables
     * @param UNION set variable representing the union of SET_VARS
     * @return A constraint ensuring that the union of SET_VARS is equal to SET_UNION
     */
    public static Constraint union(SetVar[] SETS, SetVar UNION) {
        Constraint c = new Constraint(ArrayUtils.append(SETS, new SetVar[]{UNION}), UNION.getSolver());
        c.setPropagators(new PropUnion(SETS, UNION), new PropUnion(SETS, UNION));
        return c;
    }

    /**
     * Constraint which ensures that the intersection of sets in SET_VAR is equal to the set SET_INTER
     *
     * @param SETS         set variables
     * @param INTERSECTION set variable representing the intersection of SET_VARS
     * @return A constraint ensuring that the intersection of sets is equal to set intersection
     */
    public static Constraint intersection(SetVar[] SETS, SetVar INTERSECTION) {
        Constraint c = new Constraint(ArrayUtils.append(SETS, new SetVar[]{INTERSECTION}), INTERSECTION.getSolver());
        c.setPropagators(new PropIntersection(SETS, INTERSECTION), new PropIntersection(SETS, INTERSECTION));
        return c;
    }

    /**
     * Constraint which ensures that i<j <=> SET_VARS[i] subseteq SET_VARS[j]
     *
     * @param SETS set variables
     * @return A constraint which ensures that i<j <=> SET_VARS[i] subseteq SET_VARS[j]
     */
    public static Constraint subsetEq(SetVar[] SETS) {
        Constraint c = new Constraint(SETS, SETS[0].getSolver());
        for (int i = 0; i < SETS.length - 1; i++) {
            c.addPropagators(new PropSubsetEq(SETS[i], SETS[i + 1]));
        }
        return c;
    }

    /**
     * Cardinality constraint: |SET_VAR| = CARD
     *
     * @param SET  a set variable
     * @param CARD an integer variable representing SET_VAR's cardinality
     *             (i.e. the number of elements in it)
     * @return A constraint ensuring that |SET_VAR| = CARD
     */
    public static Constraint cardinality(SetVar SET, IntVar CARD) {
        Constraint c = new Constraint(new Variable[]{SET, CARD}, CARD.getSolver());
        c.setPropagators(new PropCardinality(SET, CARD));
        return c;
    }

    /**
     * Restricts the number of empty sets in SETS
     * |{s in SETS such that |s|=0}| = NB_EMPTY_SETS
     *
     * @param SETS          set variables
     * @param NB_EMPTY_SETS integer variable restricting the number of empty sets in SETS
     * @return A constraint ensuring that |{s in SETS such that |s|=0}| = NB_EMPTY_SETS
     */
    public static Constraint nbEmpty(SetVar[] SETS, IntVar NB_EMPTY_SETS) {
        Constraint c = new Constraint(ArrayUtils.append(SETS, new Variable[]{NB_EMPTY_SETS}), NB_EMPTY_SETS.getSolver());
        c.setPropagators(new PropNbEmpty(SETS, NB_EMPTY_SETS));
        return c;
    }

    /**
     * links SET_1 and SET_2 with OFFSET
     * x in SET_1 <=> x+offSet in SET_2
     *
     * @param SET_1  a set variable
     * @param SET_2  a set variable
     * @param OFFSET offset index
     * @return a constraint ensuring that x in SET_1 <=> x+offSet in SET_2
     */
    public static Constraint offSet(SetVar SET_1, SetVar SET_2, int OFFSET) {
        Constraint c = new Constraint(new SetVar[]{SET_1, SET_2}, SET_1.getSolver());
        c.setPropagators(new PropOffSet(SET_1, SET_2, OFFSET));
        return c;
    }

	/**
	 * Prevents SET to be empty
	 * @param SET a SetVar
	 * @return a constraint ensuring that SET is not empty
	 */
	public static Constraint notEmpty(SetVar SET){
		Constraint c = new Constraint(new SetVar[]{SET}, SET.getSolver());
		c.setPropagators(new PropNotEmpty(SET));
		return c;
	}

    //***********************************************************************************
    // SUM - MAX - MIN
    //***********************************************************************************

    /**
     * Sums elements of a SET
     * sum{i | i in set} = SUM
     *
     * @param SET a set variable
     * @param SUM an integer variable representing sum{i | i in SET}
     * @return a constraint ensuring that sum{i | i in set} = SUM
     */
    public static Constraint sum(SetVar SET, IntVar SUM) {
        return sum(SET, null, 0, SUM);
    }

    /**
     * Sums weights given by a set of indexes INDEXES:
     * sum{WEIGHTS[i-OFFSET] | i in INDEXES} = SUM
     *
     * @param INDEXES set variables
     * @param WEIGHTS integers
     * @param OFFSET  offset index : should be 0 by default
     *                but generally 1 with MiniZinc API
     *                which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
     * @param SUM     an integer variable representing sum{WEIGHTS[i-OFFSET] | i in INDEXES}
     * @return a constraint ensuring that sum{WEIGHTS[i-OFFSET] | i in INDEXES} = SUM
     */
    public static Constraint sum(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar SUM) {
        Constraint c = new Constraint(new Variable[]{INDEXES, SUM}, SUM.getSolver());
        c.setPropagators(
				new PropNotEmpty(INDEXES),
				new PropSumOfElements(INDEXES, WEIGHTS, OFFSET, SUM));
        return c;
    }

    /**
     * Retrieves the maximum element MAX_ELEMENT_VALUE of SET
     * max{i | i in set} = MAX_ELEMENT_VALUE
     *
     * @param SET               a set variable
     * @param MAX_ELEMENT_VALUE an integer variable representing max{i | i in SET}
     * @return a constraint ensuring that max{i | i in set} = MAX_ELEMENT_VALUE
     */
    public static Constraint max(SetVar SET, IntVar MAX_ELEMENT_VALUE) {
        return max(SET, null, 0, MAX_ELEMENT_VALUE);
    }

    /**
     * Retrieves the maximum element MAX_ELEMENT_VALUE induced by INDEXES
     * max{WEIGHTS[i-OFFSET] | i in INDEXES} = MAX_ELEMENT_VALUE
     *
     * @param INDEXES           a set variable containing elements in range [OFFSET,WEIGHTS.length-1+OFFSET]
     * @param WEIGHTS           integers
     * @param OFFSET            offset index : should be 0 by default
     *                          but generally 1 with MiniZinc API
     *                          which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
     * @param MAX_ELEMENT_VALUE an integer variable representing max{WEIGHTS[i-OFFSET] | i in INDEXES}
     * @return a constraint ensuring that max{WEIGHTS[i-OFFSET] | i in INDEXES} = MAX_ELEMENT_VALUE
     */
    public static Constraint max(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar MAX_ELEMENT_VALUE) {
        Constraint c = new Constraint(new Variable[]{INDEXES, MAX_ELEMENT_VALUE}, INDEXES.getSolver());
        c.setPropagators(
				new PropNotEmpty(INDEXES),
				new PropMaxElement(INDEXES, WEIGHTS, OFFSET, MAX_ELEMENT_VALUE));
        return c;
    }

    /**
     * Retrieves the minimum element MIN_ELEMENT_VALUE of SET:
     * min{i | i in SET} = MIN_ELEMENT_VALUE
     *
     * @param SET               a set variable
     * @param MIN_ELEMENT_VALUE an integer variable representing min{i | i in SET}
     * @return a constraint ensuring that min{i | i in SET} = MIN_ELEMENT_VALUE
     */
    public static Constraint min(SetVar SET, IntVar MIN_ELEMENT_VALUE) {
        return min(SET, null, 0, MIN_ELEMENT_VALUE);
    }

    /**
     * Retrieves the minimum element MIN_ELEMENT_VALUE induced by INDEXES
     * min{WEIGHTS[i-OFFSET] | i in INDEXES} = MIN_ELEMENT_VALUE
     *
     * @param INDEXES           a set variable containing elements in range [OFFSET,WEIGHTS.length-1+OFFSET]
     * @param WEIGHTS           integers
     * @param OFFSET            offset index : should be 0 by default
     *                          but generally 1 with MiniZinc API
     *                          which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
     * @param MIN_ELEMENT_VALUE integer variable representing min{WEIGHTS[i-OFFSET] | i in INDEXES}
     * @return a constraint ensuring that min{WEIGHTS[i-OFFSET] | i in INDEXES} = MIN_ELEMENT_VALUE
     */
    public static Constraint min(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar MIN_ELEMENT_VALUE) {
        Constraint c = new Constraint(new Variable[]{INDEXES, MIN_ELEMENT_VALUE}, INDEXES.getSolver());
        c.setPropagators(
				new PropNotEmpty(INDEXES),
				new PropMinElement(INDEXES, WEIGHTS, OFFSET, MIN_ELEMENT_VALUE));
        return c;
    }

    //***********************************************************************************
    // CHANNELING CONSTRAINTS : bool/int/graph
    //***********************************************************************************

    /**
     * Channeling between a set variable SET and boolean variables BOOLEANS
     * i in SET <=> BOOLEANS[i-OFFSET] = TRUE
     *
     * @param BOOLEANS boolean variables
     * @param SET      set variables
     * @param OFFSET   offset index : should be 0 by default
     *                 but generally 1 with MiniZinc API
     *                 which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
     * @return a constraint ensuring that i in SET <=> BOOLEANS[i-OFFSET] = TRUE
     */
    public static Constraint bool_channel(BoolVar[] BOOLEANS, SetVar SET, int OFFSET) {
        Constraint c = new Constraint(ArrayUtils.append(BOOLEANS, new Variable[]{SET}), SET.getSolver());
        c.setPropagators(new PropBoolChannel(SET, BOOLEANS, OFFSET));
        return c;
    }

    /**
     * Channeling between set variables SETS and integer variables INTEGERS
     * x in SETS[y-OFFSET_1] <=> INTEGERS[x-OFFSET_2] = y
     *
     * @param SETS     set variables
     * @param INTEGERS integer variables
     * @param OFFSET_1 offset index : should be 0 by default
     *                 but generally 1 with MiniZinc API
     *                 which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
     * @param OFFSET_2 offset index : should be 0 by default
     *                 but generally 1 with MiniZinc API
     *                 which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
     * @return a constraint ensuring that x in SETS[y-OFFSET_1] <=> INTEGERS[x-OFFSET_2] = y
     */
    public static Constraint int_channel(SetVar[] SETS, IntVar[] INTEGERS, int OFFSET_1, int OFFSET_2) {
        Constraint c = new Constraint(ArrayUtils.append(SETS, INTEGERS), SETS[0].getSolver());
        c.setPropagators(new PropIntChannel(SETS, INTEGERS, OFFSET_1, OFFSET_2),
                new PropIntChannel(SETS, INTEGERS, OFFSET_1, OFFSET_2));
        c.addPropagators(new PropAllDisjoint(SETS));
        return c;
    }

    /**
     * Channeling between a graph variable GRAPH and set variables SETS
     * representing either node neighbors or node successors
     * <p/> arc (i,j) in GRAPH <=> j in SETS[i]
     *
     * @param SETS  set variables representing nodes neighbors (or successors if directed) in GRAPH
     * @param GRAPH a graph variable
     * @return a constraint ensuring that arc (i,j) in GRAPH <=> j in SETS[i]
     */
    public static Constraint graph_channel(SetVar[] SETS, GraphVar GRAPH) {
        Constraint c = new Constraint(ArrayUtils.append(SETS, new Variable[]{GRAPH}), GRAPH.getSolver());
        c.setPropagators(new PropGraphChannel(SETS, GRAPH));
        if (!GRAPH.isDirected()) {
            c.addPropagators(new PropSymmetric(SETS, 0));
        }
        return c;
    }

    /**
     * Channeling between a directed graph variable GRAPH and set variables SUCCESSORS and PREDECESSORS
     * representing node successors and predecessors:
     * <p/> arc (i,j) in GRAPH <=> j in SUCCESSORS[i] and i in PREDECESSORS[j]
     *
     * @param SUCCESSORS   set variables representing nodes' successors in GRAPH
     * @param PREDECESSORS set variables representing nodes' predecessors in GRAPH
     * @param GRAPH        a graph variable
     * @return a constraint ensuring that arc (i,j) in GRAPH <=> j in SUCCESSORS[i] and i in PREDECESSORS[j]
     */
    public static Constraint graph_channel(SetVar[] SUCCESSORS, SetVar[] PREDECESSORS, DirectedGraphVar GRAPH) {
        Constraint c = graph_channel(SUCCESSORS, GRAPH);
        c.addPropagators(new PropInverse(SUCCESSORS, PREDECESSORS, 0, 0));
        return c;
    }

    //***********************************************************************************
    // MINIZINC API
    //***********************************************************************************

    /**
     * SET_1 and SET_2 are disjoint, i.e. they cannot contain the same element.
     * Note that they can be both empty
     *
     * @param SET_1 a set variable
     * @param SET_2 a set variable
     * @return a constraint ensuring that set1 and set2 are disjoint
     */
    public static Constraint disjoint(SetVar SET_1, SetVar SET_2) {
        return all_disjoint(new SetVar[]{SET_1, SET_2});
    }

    /**
     * Sets in SETS are all disjoints
     * Note that there can be multiple empty sets
     *
     * @param SETS disjoint set variables
     * @return a constraint ensuring that non-empty sets are all disjoint
     */
    public static Constraint all_disjoint(SetVar[] SETS) {
        Constraint c = new Constraint(SETS, SETS[0].getSolver());
        c.setPropagators(new PropAllDisjoint(SETS));
        return c;
    }

    /**
     * Sets in SETS are all different (not necessarily disjoint)
     * Note that there cannot be more than two empty sets
     *
     * @param SETS different set variables
     * @return a constraint ensuring that SETS are all different
     */
    public static Constraint all_different(SetVar[] SETS) {
        Solver solver = SETS[0].getSolver();
        Constraint c = new Constraint(SETS, solver);
        c.setPropagators(new PropAllDiff(SETS), new PropAllDiff(SETS),
                new PropAtMost1Empty(SETS));
        return c;
    }

    /**
     * SETS are all equal
     *
     * @param SETS set variables to be equals
     * @return a constraint ensuring that all sets in SETS are equal
     */
    public static Constraint all_equal(SetVar[] SETS) {
        Solver solver = SETS[0].getSolver();
        Constraint c = new Constraint(SETS, solver);
        c.setPropagators(new PropAllEqual(SETS));
        return c;
    }

    /**
     * Partitions UNIVERSE into disjoint sets, SETS
     *
     * @param SETS     set variables whose values are subsets of UNIVERSE
     * @param UNIVERSE a set variable representing union(SETS)
     * @return a constraint which ensures that SETS form a partition of UNIVERSE
     */
    public static Constraint partition(SetVar[] SETS, SetVar UNIVERSE) {
        Constraint c = all_disjoint(SETS);
        c.addPropagators(new PropUnion(SETS, UNIVERSE), new PropUnion(SETS, UNIVERSE));
        return c;
    }

    /**
     * Inverse set constraint
     * x in SETS[y-OFFSET_1] <=> y in INVERSE_SETS[x-OFFSET_2]
     *
     * @param SETS         set variables
     * @param INVERSE_SETS set variables
     * @param OFFSET_1     offset index : should be 0 by default
     *                     but generally 1 with MiniZinc API
     *                     which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
     * @param OFFSET_2     offset index : should be 0 by default
     *                     but generally 1 with MiniZinc API
     *                     which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
     * @return a constraint ensuring that x in SETS[y-OFFSET_1] <=> y in INVERSE_SETS[x-OFFSET_2]
     */
    public static Constraint inverse_set(SetVar[] SETS, SetVar[] INVERSE_SETS, int OFFSET_1, int OFFSET_2) {
        Solver solver = SETS[0].getSolver();
        Constraint c = new Constraint(ArrayUtils.append(SETS, INVERSE_SETS), solver);
        c.setPropagators(new PropInverse(SETS, INVERSE_SETS, OFFSET_1, OFFSET_2));
        return c;
    }

    /**
     * Symmetric sets constraint
     * x in SETS[y-OFFSET] <=> y in SETS[x-OFFSET]
     *
     * @param SETS   set variables
     * @param OFFSET offset index : should be 0 by default
     *               but generally 1 with MiniZinc API
     *               which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
     * @return a constraint ensuring that x in SETS[y-OFFSET] <=> y in SETS[x-OFFSET]
     */
    public static Constraint symmetric(SetVar[] SETS, int OFFSET) {
        Solver solver = SETS[0].getSolver();
        Constraint c = new Constraint(solver);
        c.setPropagators(new PropSymmetric(SETS, OFFSET));
        return c;
    }

    /**
     * Element constraint over sets
     * states that SETS[INDEX-OFFSET] = SET
     *
     * @param INDEX  an integer variable pointing to SET's index into array SETS
     * @param SETS   set variables representing possible values for SET
     * @param OFFSET offset index : should be 0 by default
     *               but generally 1 with MiniZinc API
     *               which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
     * @param SET    a set variable which takes its value in SETS
     * @return a constraint ensuring that SETS[INDEX-OFFSET] = SET
     */
    public static Constraint element(IntVar INDEX, SetVar[] SETS, int OFFSET, SetVar SET) {
        Solver solver = INDEX.getSolver();
        Constraint c = new Constraint(solver);
        c.setPropagators(new PropElement(INDEX, SETS, OFFSET, SET), new PropElement(INDEX, SETS, OFFSET, SET));
        return c;
    }

    /**
     * Member constraint over sets
     * states that SET belongs to SETS
     *
     * @param SETS set variables representing possible values for SET
     * @param SET  a set variable which takes its value in SETS
     * @return a constraint ensuring that SET belongs to SETS
     */
    public static Constraint member(SetVar[] SETS, SetVar SET) {
        IntVar index = VariableFactory.enumerated("idx_tmp", 0, SETS.length - 1, SET.getSolver());
        return element(index, SETS, 0, SET);
    }

    /**
     * Member constraint over an IntVar and a SetVar
     * states that INTEGER is included in SET
     *
     * @param INTEGER an integer variables which takes its values in SET
     * @param SET     a set variables representing possible values of INTEGER
     * @return a constraint ensuring that INTEGER belongs to SET
     */
    public static Constraint member(IntVar INTEGER, SetVar SET) {
        Constraint c = new Constraint(new Variable[]{SET, INTEGER}, INTEGER.getSolver());
        c.setPropagators(new PropIntMemberSet(SET, INTEGER));
        return c;
    }
}
