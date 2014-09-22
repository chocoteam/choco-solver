/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.VariableFactory;
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
        return new Constraint("SetUnion",new PropUnion(SETS, UNION), new PropUnion(SETS, UNION));
    }

    /**
     * Constraint which ensures that the intersection of sets in SET_VAR is equal to the set SET_INTER
     *
     * @param SETS         set variables
     * @param INTERSECTION set variable representing the intersection of SET_VARS
     * @return A constraint ensuring that the intersection of sets is equal to set intersection
     */
    public static Constraint intersection(SetVar[] SETS, SetVar INTERSECTION) {
        return new Constraint("SetIntersection",new PropIntersection(SETS, INTERSECTION), new PropIntersection(SETS, INTERSECTION));
    }

    /**
     * Constraint which ensures that i<j <=> SET_VARS[i] subseteq SET_VARS[j]
     *
     * @param SETS set variables
     * @return A constraint which ensures that i<j <=> SET_VARS[i] subseteq SET_VARS[j]
     */
    public static Constraint subsetEq(SetVar[] SETS) {
		Propagator[] props = new Propagator[SETS.length-1];
        for (int i = 0; i < SETS.length - 1; i++) {
            props[i] = new PropSubsetEq(SETS[i], SETS[i + 1]);
        }
        return new Constraint("SetSubsetEq",props);
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
		return new Constraint("SetCard",new PropCardinality(SET, CARD));
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
		return new Constraint("SetNbEmpty",new PropNbEmpty(SETS, NB_EMPTY_SETS));
    }

    /**
     * links SET_1 and SET_2 with OFFSET
     * x in SET_1 <=> x+OFFSET in SET_2
     *
     * @param SET_1  a set variable
     * @param SET_2  a set variable
     * @param OFFSET offset index
     * @return a constraint ensuring that x in SET_1 <=> x+OFFSET in SET_2
     */
    public static Constraint offSet(SetVar SET_1, SetVar SET_2, int OFFSET) {
		return new Constraint("SetOffset",new PropOffSet(SET_1, SET_2, OFFSET));
    }

	/**
	 * Prevents SET to be empty
	 * @param SET a SetVar
	 * @return a constraint ensuring that SET is not empty
	 */
	public static Constraint notEmpty(SetVar SET){
		return new Constraint("SetNotEmpty",new PropNotEmpty(SET));
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
	 * @param NOT_EMPTY true : the set variable cannot be empty
	 *                  false : the set may be empty (if so, the SUM constraint is not applied)
     * @return a constraint ensuring that sum{i | i in set} = SUM
     */
    public static Constraint sum(SetVar SET, IntVar SUM, boolean NOT_EMPTY) {
        return sum(SET, null, 0, SUM, NOT_EMPTY);
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
	 * @param NOT_EMPTY true : the set variable cannot be empty
	 *                  false : the set may be empty (if so, the SUM constraint is not applied)
     * @return a constraint ensuring that sum{WEIGHTS[i-OFFSET] | i in INDEXES} = SUM
     */
    public static Constraint sum(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar SUM, boolean NOT_EMPTY) {
		if (NOT_EMPTY) {
			return new Constraint("SetSum_NotEmpty",new PropNotEmpty(INDEXES),new PropSumOfElements(INDEXES, WEIGHTS, OFFSET, SUM, NOT_EMPTY));
		}else{
			return new Constraint("SetSum",new PropSumOfElements(INDEXES, WEIGHTS, OFFSET, SUM, NOT_EMPTY));
		}
    }

    /**
     * Retrieves the maximum element MAX_ELEMENT_VALUE of SET
     * max{i | i in set} = MAX_ELEMENT_VALUE
     *
     * @param SET               a set variable
     * @param MAX_ELEMENT_VALUE an integer variable representing max{i | i in SET}
	 * @param NOT_EMPTY true : the set variable cannot be empty
	 *                  false : the set may be empty (if so, the MAX constraint is not applied)
     * @return a constraint ensuring that max{i | i in set} = MAX_ELEMENT_VALUE
     */
    public static Constraint max(SetVar SET, IntVar MAX_ELEMENT_VALUE, boolean NOT_EMPTY) {
        return max(SET, null, 0, MAX_ELEMENT_VALUE, NOT_EMPTY);
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
	 * @param NOT_EMPTY true : the set variable cannot be empty
	 *                  false : the set may be empty (if so, the MAX constraint is not applied)
     * @return a constraint ensuring that max{WEIGHTS[i-OFFSET] | i in INDEXES} = MAX_ELEMENT_VALUE
     */
    public static Constraint max(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar MAX_ELEMENT_VALUE, boolean NOT_EMPTY) {
		if (NOT_EMPTY) {
			return new Constraint("SetMax_NotEmpty",new PropNotEmpty(INDEXES),new PropMaxElement(INDEXES, WEIGHTS, OFFSET, MAX_ELEMENT_VALUE, NOT_EMPTY));
		}else{
			return new Constraint("SetMax",new PropMaxElement(INDEXES, WEIGHTS, OFFSET, MAX_ELEMENT_VALUE, NOT_EMPTY));
		}
    }

    /**
     * Retrieves the minimum element MIN_ELEMENT_VALUE of SET:
     * min{i | i in SET} = MIN_ELEMENT_VALUE
     *
     * @param SET               a set variable
     * @param MIN_ELEMENT_VALUE an integer variable representing min{i | i in SET}
	 * @param NOT_EMPTY true : the set variable cannot be empty
	 *                  false : the set may be empty (if so, the MIN constraint is not applied)
     * @return a constraint ensuring that min{i | i in SET} = MIN_ELEMENT_VALUE
     */
    public static Constraint min(SetVar SET, IntVar MIN_ELEMENT_VALUE, boolean NOT_EMPTY) {
        return min(SET, null, 0, MIN_ELEMENT_VALUE, NOT_EMPTY);
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
	 * @param NOT_EMPTY true : the set variable cannot be empty
	 *                  false : the set may be empty (if so, the MIN constraint is not applied)
     * @return a constraint ensuring that min{WEIGHTS[i-OFFSET] | i in INDEXES} = MIN_ELEMENT_VALUE
     */
    public static Constraint min(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar MIN_ELEMENT_VALUE, boolean NOT_EMPTY) {
		if (NOT_EMPTY) {
			return new Constraint("SetMin_NotEmpty",new PropNotEmpty(INDEXES),new PropMinElement(INDEXES, WEIGHTS, OFFSET, MIN_ELEMENT_VALUE, NOT_EMPTY));
		}else{
			return new Constraint("SetMin",new PropMinElement(INDEXES, WEIGHTS, OFFSET, MIN_ELEMENT_VALUE, NOT_EMPTY));
		}
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
		return new Constraint("SetBoolChanneling",new PropBoolChannel(SET, BOOLEANS, OFFSET));
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
		return new Constraint("SetIntChanneling",
				new PropIntChannel(SETS, INTEGERS, OFFSET_1, OFFSET_2),
                new PropIntChannel(SETS, INTEGERS, OFFSET_1, OFFSET_2),new PropAllDisjoint(SETS)
		);
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
     * Sets in SETS are all disjoint
     * Note that there can be multiple empty sets
     *
     * @param SETS disjoint set variables
     * @return a constraint ensuring that non-empty sets are all disjoint
     */
    public static Constraint all_disjoint(SetVar[] SETS) {
		return new Constraint("SetAllDisjoint",new PropAllDisjoint(SETS));
    }

    /**
     * Sets in SETS are all different (not necessarily disjoint)
     * Note that there cannot be more than two empty sets
     *
     * @param SETS different set variables
     * @return a constraint ensuring that SETS are all different
     */
    public static Constraint all_different(SetVar[] SETS) {
		return new Constraint("SetAllDifferent",new PropAllDiff(SETS),
				new PropAllDiff(SETS),new PropAtMost1Empty(SETS)
		);
    }

    /**
     * SETS are all equal
     *
     * @param SETS set variables to be equals
     * @return a constraint ensuring that all sets in SETS are equal
     */
    public static Constraint all_equal(SetVar[] SETS) {
		return new Constraint("SetAllEqual",new PropAllEqual(SETS));
    }

    /**
     * Partitions UNIVERSE into disjoint sets, SETS
     *
     * @param SETS     set variables whose values are subsets of UNIVERSE
     * @param UNIVERSE a set variable representing union(SETS)
     * @return a constraint which ensures that SETS form a partition of UNIVERSE
     */
    public static Constraint partition(SetVar[] SETS, SetVar UNIVERSE) {
        return new Constraint("SetPartition",ArrayUtils.append(
				all_disjoint(SETS).getPropagators(),
				new Propagator[]{new PropUnion(SETS, UNIVERSE), new PropUnion(SETS, UNIVERSE)}
		));
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
		return new Constraint("SetInverse",new PropInverse(SETS, INVERSE_SETS, OFFSET_1, OFFSET_2));
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
		return new Constraint("SetSymmetric",new PropSymmetric(SETS, OFFSET));
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
		return new Constraint("SetElement",new PropElement(INDEX, SETS, OFFSET, SET), new PropElement(INDEX, SETS, OFFSET, SET));
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
		return new Constraint("SetMember",new PropIntMemberSet(SET, INTEGER));
    }
}
