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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

/**
 * @deprecated : set constraint creation should be done through the {@link Solver} object
 * which extends {@link org.chocosolver.solver.constraints.ISetConstraintFactory}
 *
 * This class will be removed in versions > 3.4.0
 */
@Deprecated
public class SetConstraintsFactory {

    SetConstraintsFactory() {}

    //***********************************************************************************
    // BASICS : union/inter/subset/card
    //***********************************************************************************

    /**
     * @deprecated : use {@link Solver#union(SetVar[], SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint union(SetVar[] SETS, SetVar UNION) {
        return UNION.getSolver().union(SETS,UNION);
    }

    /**
     * @deprecated : use {@link Solver#intersection(SetVar[], SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint intersection(SetVar[] SETS, SetVar INTERSECTION) {
        return INTERSECTION.getSolver().intersection(SETS, INTERSECTION);
    }

    /**
     * @deprecated : use {@link Solver#subsetEq(SetVar...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint subsetEq(SetVar... SETS) {
        return SETS[0].getSolver().subsetEq(SETS);
    }

    /**
     * @deprecated : use {@link Solver#cardinality(SetVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint cardinality(SetVar SET, IntVar CARD) {
        return SET.getSolver().cardinality(SET, CARD);
    }

    /**
     * @deprecated : use {@link Solver#nbEmpty(SetVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint nbEmpty(SetVar[] SETS, IntVar NB_EMPTY_SETS) {
        return NB_EMPTY_SETS.getSolver().nbEmpty(SETS, NB_EMPTY_SETS);
    }

    /**
     * @deprecated : use {@link Solver#offSet(SetVar, SetVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint offSet(SetVar SET_1, SetVar SET_2, int OFFSET) {
        return SET_1.getSolver().offSet(SET_1,SET_2,OFFSET);
    }

    /**
     * @deprecated : use {@link Solver#notEmpty(SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint notEmpty(SetVar SET) {
        return SET.getSolver().notEmpty(SET);
    }

    //***********************************************************************************
    // SUM - MAX - MIN
    //***********************************************************************************

    /**
     * @deprecated : use {@link Solver#sum(SetVar, IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(SetVar SET, IntVar SUM, boolean NOT_EMPTY) {
        return sum(SET, null, 0, SUM, NOT_EMPTY);
    }

    /**
     * @deprecated : use {@link Solver#sum(SetVar, int[], int, IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar SUM, boolean NOT_EMPTY) {
        return INDEXES.getSolver().sum(INDEXES,WEIGHTS,OFFSET,SUM,NOT_EMPTY);
    }

    /**
     * @deprecated : use {@link Solver#max(SetVar, IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint max(SetVar SET, IntVar MAX_ELEMENT_VALUE, boolean NOT_EMPTY) {
        return max(SET, null, 0, MAX_ELEMENT_VALUE, NOT_EMPTY);
    }

    /**
     * @deprecated : use {@link Solver#max(SetVar, int[], int, IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint max(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar MAX_ELEMENT_VALUE, boolean NOT_EMPTY) {
        return INDEXES.getSolver().max(INDEXES,WEIGHTS,OFFSET,MAX_ELEMENT_VALUE,NOT_EMPTY);
    }

    /**
     * @deprecated : use {@link Solver#min(SetVar, IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint min(SetVar SET, IntVar MIN_ELEMENT_VALUE, boolean NOT_EMPTY) {
        return min(SET, null, 0, MIN_ELEMENT_VALUE, NOT_EMPTY);
    }

    /**
     * @deprecated : use {@link Solver#min(SetVar, int[], int, IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint min(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar MIN_ELEMENT_VALUE, boolean NOT_EMPTY) {
        return INDEXES.getSolver().min(INDEXES,WEIGHTS,OFFSET,MIN_ELEMENT_VALUE,NOT_EMPTY);
    }

    //***********************************************************************************
    // CHANNELING CONSTRAINTS : bool/int/graph
    //***********************************************************************************

    /**
     * @deprecated : use {@link Solver#setBoolsChanneling(BoolVar[], SetVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint bool_channel(BoolVar[] BOOLEANS, SetVar SET, int OFFSET) {
        return SET.getSolver().setBoolsChanneling(BOOLEANS,SET,OFFSET);
    }

    /**
     * @deprecated : use {@link Solver#setsIntsChanneling(SetVar[], IntVar[], int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint int_channel(SetVar[] SETS, IntVar[] INTEGERS, int OFFSET_1, int OFFSET_2) {
        return SETS[0].getSolver().setsIntsChanneling(SETS,INTEGERS,OFFSET_1,OFFSET_2);
    }

    /**
     * @deprecated : use {@link Solver#union(IntVar[], SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint int_values_union(IntVar[] VARS, SetVar VALUES) {
        return VALUES.getSolver().union(VARS,VALUES);
    }

    //***********************************************************************************
    // MINIZINC API
    //***********************************************************************************

    /**
     * @deprecated : use {@link Solver#disjoint(SetVar, SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint disjoint(SetVar SET_1, SetVar SET_2) {
        return all_disjoint(SET_1, SET_2);
    }

    /**
     * @deprecated : use {@link Solver#allDisjoint(SetVar...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint all_disjoint(SetVar... SETS) {
        return SETS[0].getSolver().allDisjoint(SETS);
    }

    /**
     * @deprecated : use {@link Solver#allDifferent(SetVar...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint all_different(SetVar... SETS) {
        return SETS[0].getSolver().allDifferent(SETS);
    }

    /**
     * @deprecated : use {@link Solver#allEqual(SetVar...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint all_equal(SetVar... SETS) {
        return SETS[0].getSolver().allEqual(SETS);
    }

    /**
     * @deprecated : use {@link Solver#partition(SetVar[], SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint partition(SetVar[] SETS, SetVar UNIVERSE) {
        return SETS[0].getSolver().partition(SETS,UNIVERSE);
    }

    /**
     * @deprecated : use {@link Solver#inverseSet(SetVar[], SetVar[], int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint inverse_set(SetVar[] SETS, SetVar[] INVERSE_SETS, int OFFSET_1, int OFFSET_2) {
        return SETS[0].getSolver().inverseSet(SETS, INVERSE_SETS, OFFSET_1, OFFSET_2);
    }

    /**
     * @deprecated : use {@link Solver#symmetric(SetVar[], int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint symmetric(SetVar[] SETS, int OFFSET) {
        return SETS[0].getSolver().symmetric(SETS, OFFSET);
    }

    /**
     * @deprecated : use {@link Solver#element(IntVar, SetVar[], int, SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint element(IntVar INDEX, SetVar[] SETS, int OFFSET, SetVar SET) {
        return SETS[0].getSolver().element(INDEX,SETS,OFFSET,SET);
    }

    /**
     * @deprecated : use {@link Solver#member(SetVar[], SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint member(SetVar[] SETS, SetVar SET) {
        return SETS[0].getSolver().member(SETS, SET);
    }

    /**
     * @deprecated : use {@link Solver#member(IntVar, SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint member(final IntVar INTEGER, final SetVar SET) {
        return SET.getSolver().member(INTEGER,SET);
    }

    /**
     * @deprecated : use {@link Solver#notMember(IntVar, SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint not_member(final IntVar INTEGER, final SetVar SET) {
        return SET.getSolver().notMember(INTEGER,SET);
    }
}
