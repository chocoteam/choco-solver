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
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

/**
 * Constraints over set variables
 *
 * Deprecated : constraint creation should be done through the {@code Solver} object which extends {@code IModeler}
 *
 * @author Jean-Guillaume Fages
 */
@Deprecated
public class SetConstraintsFactory {

    SetConstraintsFactory() {}

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
        return UNION.getSolver().union(SETS,UNION);
    }

    /**
     * Constraint which ensures that the intersection of sets in SET_VAR is equal to the set SET_INTER
     *
     * @param SETS         set variables
     * @param INTERSECTION set variable representing the intersection of SET_VARS
     * @return A constraint ensuring that the intersection of sets is equal to set intersection
     */
    public static Constraint intersection(SetVar[] SETS, SetVar INTERSECTION) {
        return INTERSECTION.getSolver().intersection(SETS, INTERSECTION);
    }

    /**
     * Constraint which ensures that i<j <=> SET_VARS[i] subseteq SET_VARS[j]
     *
     * @param SETS set variables
     * @return A constraint which ensures that i<j <=> SET_VARS[i] subseteq SET_VARS[j]
     */
    public static Constraint subsetEq(SetVar... SETS) {
        return SETS[0].getSolver().subsetEq(SETS);
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
        return SET.getSolver().cardinality(SET, CARD);
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
        return NB_EMPTY_SETS.getSolver().nbEmpty(SETS, NB_EMPTY_SETS);
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
        return SET_1.getSolver().offSet(SET_1,SET_2,OFFSET);
    }

    /**
     * Prevents SET to be empty
     *
     * @param SET a SetVar
     * @return a constraint ensuring that SET is not empty
     */
    public static Constraint notEmpty(SetVar SET) {
        return SET.getSolver().notEmpty(SET);
    }

    //***********************************************************************************
    // SUM - MAX - MIN
    //***********************************************************************************

    /**
     * Sums elements of a SET
     * sum{i | i in set} = SUM
     *
     * @param SET       a set variable
     * @param SUM       an integer variable representing sum{i | i in SET}
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
     * @param INDEXES   set variables
     * @param WEIGHTS   integers
     * @param OFFSET    offset index : should be 0 by default
     *                  but generally 1 with MiniZinc API
     *                  which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
     * @param SUM       an integer variable representing sum{WEIGHTS[i-OFFSET] | i in INDEXES}
     * @param NOT_EMPTY true : the set variable cannot be empty
     *                  false : the set may be empty (if so, the SUM constraint is not applied)
     * @return a constraint ensuring that sum{WEIGHTS[i-OFFSET] | i in INDEXES} = SUM
     */
    public static Constraint sum(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar SUM, boolean NOT_EMPTY) {
        return INDEXES.getSolver().sum(INDEXES,WEIGHTS,OFFSET,SUM,NOT_EMPTY);
    }

    /**
     * Retrieves the maximum element MAX_ELEMENT_VALUE of SET
     * max{i | i in set} = MAX_ELEMENT_VALUE
     *
     * @param SET               a set variable
     * @param MAX_ELEMENT_VALUE an integer variable representing max{i | i in SET}
     * @param NOT_EMPTY         true : the set variable cannot be empty
     *                          false : the set may be empty (if so, the MAX constraint is not applied)
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
     * @param NOT_EMPTY         true : the set variable cannot be empty
     *                          false : the set may be empty (if so, the MAX constraint is not applied)
     * @return a constraint ensuring that max{WEIGHTS[i-OFFSET] | i in INDEXES} = MAX_ELEMENT_VALUE
     */
    public static Constraint max(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar MAX_ELEMENT_VALUE, boolean NOT_EMPTY) {
        return INDEXES.getSolver().max(INDEXES,WEIGHTS,OFFSET,MAX_ELEMENT_VALUE,NOT_EMPTY);
    }

    /**
     * Retrieves the minimum element MIN_ELEMENT_VALUE of SET:
     * min{i | i in SET} = MIN_ELEMENT_VALUE
     *
     * @param SET               a set variable
     * @param MIN_ELEMENT_VALUE an integer variable representing min{i | i in SET}
     * @param NOT_EMPTY         true : the set variable cannot be empty
     *                          false : the set may be empty (if so, the MIN constraint is not applied)
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
     * @param NOT_EMPTY         true : the set variable cannot be empty
     *                          false : the set may be empty (if so, the MIN constraint is not applied)
     * @return a constraint ensuring that min{WEIGHTS[i-OFFSET] | i in INDEXES} = MIN_ELEMENT_VALUE
     */
    public static Constraint min(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar MIN_ELEMENT_VALUE, boolean NOT_EMPTY) {
        return INDEXES.getSolver().min(INDEXES,WEIGHTS,OFFSET,MIN_ELEMENT_VALUE,NOT_EMPTY);
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
        return SET.getSolver().setBoolsChanneling(BOOLEANS,SET,OFFSET);
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
        return SETS[0].getSolver().setsIntsChanneling(SETS,INTEGERS,OFFSET_1,OFFSET_2);
    }

    /**
     * Channeling constraint ensuring that
     * VALUES is exactly the set of values taken by VARS,
     *
     * @param VARS   integer variables
     * @param VALUES a set variable
     * @return a channeling constraint ensuring that VALUES = {value(x) | x in VARS}
     */
    public static Constraint int_values_union(IntVar[] VARS, SetVar VALUES) {
        return VALUES.getSolver().union(VARS,VALUES);
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
        return all_disjoint(SET_1, SET_2);
    }

    /**
     * Sets in SETS are all disjoint
     * Note that there can be multiple empty sets
     *
     * @param SETS disjoint set variables
     * @return a constraint ensuring that non-empty sets are all disjoint
     */
    public static Constraint all_disjoint(SetVar... SETS) {
        return SETS[0].getSolver().allDisjoint(SETS);
    }

    /**
     * Sets in SETS are all different (not necessarily disjoint)
     * Note that there cannot be more than two empty sets
     *
     * @param SETS different set variables
     * @return a constraint ensuring that SETS are all different
     */
    public static Constraint all_different(SetVar... SETS) {
        return SETS[0].getSolver().allDifferent(SETS);
    }

    /**
     * SETS are all equal
     *
     * @param SETS set variables to be equals
     * @return a constraint ensuring that all sets in SETS are equal
     */
    public static Constraint all_equal(SetVar... SETS) {
        return SETS[0].getSolver().allEqual(SETS);
    }

    /**
     * Partitions UNIVERSE into disjoint sets, SETS
     *
     * @param SETS     set variables whose values are subsets of UNIVERSE
     * @param UNIVERSE a set variable representing union(SETS)
     * @return a constraint which ensures that SETS form a partition of UNIVERSE
     */
    public static Constraint partition(SetVar[] SETS, SetVar UNIVERSE) {
        return SETS[0].getSolver().partition(SETS,UNIVERSE);
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
        return SETS[0].getSolver().inverseSet(SETS, INVERSE_SETS, OFFSET_1, OFFSET_2);
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
        return SETS[0].getSolver().symmetric(SETS, OFFSET);
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
        return SETS[0].getSolver().element(INDEX,SETS,OFFSET,SET);
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
        return SETS[0].getSolver().member(SETS, SET);
    }

    /**
     * Member constraint over an IntVar and a SetVar
     * states that INTEGER is included in SET
     *
     * @param INTEGER an integer variables which takes its values in SET
     * @param SET     a set variables representing possible values of INTEGER
     * @return a constraint ensuring that INTEGER belongs to SET
     */
    public static Constraint member(final IntVar INTEGER, final SetVar SET) {
        return SET.getSolver().member(INTEGER,SET);
    }

    /**
     * NotMember constraint over an IntVar and a SetVar
     * states that INTEGER is not included in SET
     *
     * @param INTEGER an integer variables which does not take its values in SET
     * @param SET     a set variables representing impossible values of INTEGER
     * @return a constraint ensuring that INTEGER does not belong to SET
     */
    public static Constraint not_member(final IntVar INTEGER, final SetVar SET) {
        return SET.getSolver().notMember(INTEGER,SET);
    }
}
