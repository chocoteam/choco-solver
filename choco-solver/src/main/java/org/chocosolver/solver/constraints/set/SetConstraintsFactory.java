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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

/**
 * @deprecated : set constraint creation should be done through the {@link Model} object
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
     * @deprecated : use {@link Model#union(SetVar[], SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint union(SetVar[] SETS, SetVar UNION) {
        return UNION.getModel().union(SETS,UNION);
    }

    /**
     * @deprecated : use {@link Model#intersection(SetVar[], SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint intersection(SetVar[] SETS, SetVar INTERSECTION) {
        return INTERSECTION.getModel().intersection(SETS, INTERSECTION);
    }

    /**
     * @deprecated : use {@link Model#subsetEq(SetVar...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint subsetEq(SetVar... SETS) {
        return SETS[0].getModel().subsetEq(SETS);
    }

    /**
     * @deprecated : use {@link Model#cardinality(SetVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint cardinality(SetVar SET, IntVar CARD) {
        return SET.getModel().cardinality(SET, CARD);
    }

    /**
     * @deprecated : use {@link Model#nbEmpty(SetVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint nbEmpty(SetVar[] SETS, IntVar NB_EMPTY_SETS) {
        return NB_EMPTY_SETS.getModel().nbEmpty(SETS, NB_EMPTY_SETS);
    }

    /**
     * @deprecated : use {@link Model#offSet(SetVar, SetVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint offSet(SetVar SET_1, SetVar SET_2, int OFFSET) {
        return SET_1.getModel().offSet(SET_1,SET_2,OFFSET);
    }

    /**
     * @deprecated : use {@link Model#notEmpty(SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint notEmpty(SetVar SET) {
        return SET.getModel().notEmpty(SET);
    }

    //***********************************************************************************
    // SUM - MAX - MIN
    //***********************************************************************************

    /**
     * @deprecated : use {@link Model#sum(SetVar, IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(SetVar SET, IntVar SUM, boolean NOT_EMPTY) {
        return sum(SET, null, 0, SUM, NOT_EMPTY);
    }

    /**
     * @deprecated : use {@link Model#sum(SetVar, int[], int, IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar SUM, boolean NOT_EMPTY) {
        return INDEXES.getModel().sum(INDEXES,WEIGHTS,OFFSET,SUM,NOT_EMPTY);
    }

    /**
     * @deprecated : use {@link Model#max(SetVar, IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint max(SetVar SET, IntVar MAX_ELEMENT_VALUE, boolean NOT_EMPTY) {
        return max(SET, null, 0, MAX_ELEMENT_VALUE, NOT_EMPTY);
    }

    /**
     * @deprecated : use {@link Model#max(SetVar, int[], int, IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint max(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar MAX_ELEMENT_VALUE, boolean NOT_EMPTY) {
        return INDEXES.getModel().max(INDEXES,WEIGHTS,OFFSET,MAX_ELEMENT_VALUE,NOT_EMPTY);
    }

    /**
     * @deprecated : use {@link Model#min(SetVar, IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint min(SetVar SET, IntVar MIN_ELEMENT_VALUE, boolean NOT_EMPTY) {
        return min(SET, null, 0, MIN_ELEMENT_VALUE, NOT_EMPTY);
    }

    /**
     * @deprecated : use {@link Model#min(SetVar, int[], int, IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint min(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar MIN_ELEMENT_VALUE, boolean NOT_EMPTY) {
        return INDEXES.getModel().min(INDEXES,WEIGHTS,OFFSET,MIN_ELEMENT_VALUE,NOT_EMPTY);
    }

    //***********************************************************************************
    // CHANNELING CONSTRAINTS : bool/int/graph
    //***********************************************************************************

    /**
     * @deprecated : use {@link Model#setBoolsChanneling(BoolVar[], SetVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint bool_channel(BoolVar[] BOOLEANS, SetVar SET, int OFFSET) {
        return SET.getModel().setBoolsChanneling(BOOLEANS,SET,OFFSET);
    }

    /**
     * @deprecated : use {@link Model#setsIntsChanneling(SetVar[], IntVar[], int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint int_channel(SetVar[] SETS, IntVar[] INTEGERS, int OFFSET_1, int OFFSET_2) {
        return SETS[0].getModel().setsIntsChanneling(SETS,INTEGERS,OFFSET_1,OFFSET_2);
    }

    /**
     * @deprecated : use {@link Model#union(IntVar[], SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint int_values_union(IntVar[] VARS, SetVar VALUES) {
        return VALUES.getModel().union(VARS,VALUES);
    }

    //***********************************************************************************
    // MINIZINC API
    //***********************************************************************************

    /**
     * @deprecated : use {@link Model#disjoint(SetVar, SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint disjoint(SetVar SET_1, SetVar SET_2) {
        return all_disjoint(SET_1, SET_2);
    }

    /**
     * @deprecated : use {@link Model#allDisjoint(SetVar...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint all_disjoint(SetVar... SETS) {
        return SETS[0].getModel().allDisjoint(SETS);
    }

    /**
     * @deprecated : use {@link Model#allDifferent(SetVar...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint all_different(SetVar... SETS) {
        return SETS[0].getModel().allDifferent(SETS);
    }

    /**
     * @deprecated : use {@link Model#allEqual(SetVar...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint all_equal(SetVar... SETS) {
        return SETS[0].getModel().allEqual(SETS);
    }

    /**
     * @deprecated : use {@link Model#partition(SetVar[], SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint partition(SetVar[] SETS, SetVar UNIVERSE) {
        return SETS[0].getModel().partition(SETS,UNIVERSE);
    }

    /**
     * @deprecated : use {@link Model#inverseSet(SetVar[], SetVar[], int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint inverse_set(SetVar[] SETS, SetVar[] INVERSE_SETS, int OFFSET_1, int OFFSET_2) {
        return SETS[0].getModel().inverseSet(SETS, INVERSE_SETS, OFFSET_1, OFFSET_2);
    }

    /**
     * @deprecated : use {@link Model#symmetric(SetVar[], int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint symmetric(SetVar[] SETS, int OFFSET) {
        return SETS[0].getModel().symmetric(SETS, OFFSET);
    }

    /**
     * @deprecated : use {@link Model#element(IntVar, SetVar[], int, SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint element(IntVar INDEX, SetVar[] SETS, int OFFSET, SetVar SET) {
        return SETS[0].getModel().element(INDEX,SETS,OFFSET,SET);
    }

    /**
     * @deprecated : use {@link Model#member(SetVar[], SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint member(SetVar[] SETS, SetVar SET) {
        return SETS[0].getModel().member(SETS, SET);
    }

    /**
     * @deprecated : use {@link Model#member(IntVar, SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint member(final IntVar INTEGER, final SetVar SET) {
        return SET.getModel().member(INTEGER,SET);
    }

    /**
     * @deprecated : use {@link Model#notMember(IntVar, SetVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint not_member(final IntVar INTEGER, final SetVar SET) {
        return SET.getModel().notMember(INTEGER,SET);
    }
}
