/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.nary.sat.PropSat;
import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.cnf.ILogical;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.constraints.nary.cnf.LogicTreeToolBox;
import org.chocosolver.solver.constraints.reification.LocalConstructiveDisjunction;
import org.chocosolver.solver.variables.BoolVar;

/**
 * A factory dedicated to SAT.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/07/13
 */
public interface ISatFactory extends ISelf<Model> {

    /**
     * Ensures that the clauses defined in the Boolean logic formula TREE are satisfied.
     *
     * @param TREE   the syntactic tree
     * @return true if the clause has been added to the clause store
     */
    default boolean addClauses(LogOp TREE) {
        ILogical tree = LogicTreeToolBox.toCNF(TREE, ref());
        boolean ret = true;
        if (ref().boolVar(true).equals(tree)) {
            ret = addClauseTrue(ref().boolVar(true));
        } else if (ref().boolVar(false).equals(tree)) {
            ret = addClauseTrue(ref().boolVar(false));
        } else {
            ILogical[] clauses;
            if (!tree.isLit() && ((LogOp) tree).is(LogOp.Operator.AND)) {
                clauses = ((LogOp) tree).getChildren();
            } else {
                clauses = new ILogical[]{tree};
            }
            for (int i = 0; i < clauses.length; i++) {
                ILogical clause = clauses[i];
                if (clause.isLit()) {
                    BoolVar bv = (BoolVar) clause;
                    ret &= addClauseTrue(bv);
                } else {
                    LogOp n = (LogOp) clause;
                    BoolVar[] bvars = n.flattenBoolVar();
                    if (ref().getSettings().enableSAT()) {
                        TIntList lits = new TIntArrayList(bvars.length);
                        PropSat sat = ref().getMinisat().getPropSat();
                        // init internal structures
                        sat.beforeAddingClauses();
                        for (int j = 0; j < bvars.length; j++) {
                            lits.add(sat.makeLiteral(bvars[j], true));
                        }
                        // TODO: pass by satsolver directly
                        ret &= sat.addClause(lits);
                        sat.afterAddingClauses();
                    }else{
                        ref().sum(bvars, ">", 0).post();
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Ensures that the clause defined by POSLITS and NEGLITS is satisfied.
     *
     * @param POSLITS positive literals
     * @param NEGLITS negative literals
     * @return true if the clause has been added to the clause store
     */
    default boolean addClauses(BoolVar[] POSLITS, BoolVar[] NEGLITS) {
        if (ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            int[] pos = new int[POSLITS.length];
            for (int i = 0; i < POSLITS.length; i++) {
                pos[i] = sat.makeVar(POSLITS[i]);
            }
            int[] neg = new int[NEGLITS.length];
            for (int i = 0; i < NEGLITS.length; i++) {
                neg[i] = sat.makeVar(NEGLITS[i]);
            }
            boolean add = sat.getSatSolver().addClause(pos, neg);
            sat.afterAddingClauses();
            return add;
        }else{
            int PL = POSLITS.length;
            int NL = NEGLITS.length;
            BoolVar[] LITS = new BoolVar[PL + NL];
            System.arraycopy(POSLITS, 0, LITS, 0, PL);
            for (int i = 0; i < NL; i++) {
                LITS[i + PL] = NEGLITS[i].not();
            }
            ref().sum(LITS, ">", 0).post();
            return true;
        }
    }

    /**
     * Add a unit clause stating that BOOLVAR must be true
     *
     * @param BOOLVAR a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClauseTrue(BoolVar BOOLVAR) {
        if (ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = sat.getSatSolver().addTrue(sat.makeVar(BOOLVAR));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().arithm(BOOLVAR, "=", 1).post();
            return true;
        }
    }

    /**
     * Add a unit clause stating that BOOLVAR must be false
     *
     * @param BOOLVAR a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClauseFalse(BoolVar BOOLVAR) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = sat.getSatSolver().addFalse(sat.makeVar(BOOLVAR));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().arithm(BOOLVAR, "=", 0).post();
            return true;
        }
    }

    /**
     * Add a clause stating that: LEFT == RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolEq(BoolVar LEFT, BoolVar RIGHT) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = sat.getSatSolver().addBoolEq(sat.makeVar(LEFT), sat.makeVar(RIGHT));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().arithm(LEFT, "=", RIGHT).post();
            return true;
        }
    }

    /**
     * Add a clause stating that: LEFT &le; RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolLe(BoolVar LEFT, BoolVar RIGHT) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = sat.getSatSolver().addBoolLe(sat.makeVar(LEFT), sat.makeVar(RIGHT));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().arithm(LEFT, "<=", RIGHT).post();
            return true;
        }
    }

    /**
     * Add a clause stating that: LEFT < RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolLt(BoolVar LEFT, BoolVar RIGHT) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = sat.getSatSolver().addBoolLt(sat.makeVar(LEFT), sat.makeVar(RIGHT));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().arithm(LEFT, "<", RIGHT).post();
            return true;
        }
    }

    /**
     * Add a clause stating that: LEFT != RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolNot(BoolVar LEFT, BoolVar RIGHT) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = sat.getSatSolver().addBoolNot(sat.makeVar(LEFT), sat.makeVar(RIGHT));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().arithm(LEFT, "!=", RIGHT).post();
            return true;
        }
    }

    /**
     * Add a clause stating that: (BOOLVARS<sub>1</sub>&or;BOOLVARS<sub>2</sub>&or;...&or;BOOLVARS<sub>n</sub>) &hArr; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET   the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolOrArrayEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            int[] vars = new int[BOOLVARS.length];
            for (int i = 0; i < BOOLVARS.length; i++) {
                vars[i] = sat.makeVar(BOOLVARS[i]);
            }
            boolean add = sat.getSatSolver().addBoolOrArrayEqVar(vars, sat.makeVar(TARGET));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().max(TARGET, BOOLVARS).post();
            return true;
        }
    }

    /**
     * Add a clause stating that: (BOOLVARS<sub>1</sub>&and;BOOLVARS<sub>2</sub>&and;...&and;BOOLVARS<sub>n</sub>) &hArr; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET   the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolAndArrayEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            int[] vars = new int[BOOLVARS.length];
            for (int i = 0; i < BOOLVARS.length; i++) {
                vars[i] = sat.makeVar(BOOLVARS[i]);
            }
            boolean add = sat.getSatSolver().addBoolAndArrayEqVar(vars, sat.makeVar(TARGET));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().min(TARGET, BOOLVARS).post();
            return true;
        }
    }

    /**
     * Add a clause stating that: (LEFT &or; RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolOrEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = sat.getSatSolver().addBoolOrEqVar(sat.makeVar(LEFT), sat.makeVar(RIGHT), sat.makeVar(TARGET));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().arithm(LEFT, "+", RIGHT, ">", 0).reifyWith(TARGET);
            return true;
        }
    }

    /**
     * Add a clause stating that: (LEFT &and; RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolAndEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = sat.getSatSolver().addBoolAndEqVar(sat.makeVar(LEFT), sat.makeVar(RIGHT), sat.makeVar(TARGET));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().arithm(LEFT, "+", RIGHT, "=", 2).reifyWith(TARGET);
            return true;
        }
    }

    /**
     * Add a clause stating that: (LEFT &oplus; RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolXorEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        return addClausesBoolIsNeqVar(LEFT, RIGHT, TARGET);
    }

    /**
     * Add a clause stating that: (LEFT == RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolIsEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = sat.getSatSolver().addBoolIsEqVar(sat.makeVar(LEFT), sat.makeVar(RIGHT), sat.makeVar(TARGET));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().reifyXeqY(LEFT, RIGHT, TARGET);
            return true;
        }
    }

    /**
     * Add a clause stating that: (LEFT &ne; RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolIsNeqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = sat.getSatSolver().addBoolIsNeqVar(sat.makeVar(LEFT), sat.makeVar(RIGHT), sat.makeVar(TARGET));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().reifyXneY(LEFT, RIGHT, TARGET);
            return true;
        }
    }

    /**
     * Add a clause stating that: (LEFT &le; RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolIsLeVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = sat.getSatSolver().addBoolIsLeVar(sat.makeVar(LEFT), sat.makeVar(RIGHT), sat.makeVar(TARGET));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().reifyXleY(LEFT, RIGHT, TARGET);
            return true;
        }
    }

    /**
     * Add a clause stating that: (LEFT < RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolIsLtVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = sat.getSatSolver().addBoolIsLtVar(sat.makeVar(LEFT), sat.makeVar(RIGHT), sat.makeVar(TARGET));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().reifyXltY(LEFT, RIGHT, TARGET);
            return true;
        }
    }

    /**
     * Add a clause stating that: BOOLVARS<sub>1</sub>&or;BOOLVARS<sub>2</sub>&or;...&or;BOOLVARS<sub>n</sub>
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolOrArrayEqualTrue(BoolVar[] BOOLVARS) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            int[] vars = new int[BOOLVARS.length];
            for (int i = 0; i < BOOLVARS.length; i++) {
                vars[i] = sat.makeVar(BOOLVARS[i]);
            }
            boolean add = sat.getSatSolver().addBoolOrArrayEqualTrue(vars);
            sat.afterAddingClauses();
            return add;
        }else{
            ref().sum(BOOLVARS, ">", 0).post();
            return true;
        }
    }

    /**
     * Add a clause stating that: BOOLVARS<sub>1</sub>&and;BOOLVARS<sub>2</sub>&and;...&and;BOOLVARS<sub>n</sub>
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolAndArrayEqualFalse(BoolVar[] BOOLVARS) {
        return addClausesAtMostNMinusOne(BOOLVARS);
    }

    /**
     * Add a clause stating that: &sum; BOOLVARS<sub>i</sub> &le; 1
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesAtMostOne(BoolVar[] BOOLVARS) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            int[] vars = new int[BOOLVARS.length];
            for (int i = 0; i < BOOLVARS.length; i++) {
                vars[i] = sat.makeVar(BOOLVARS[i]);
            }
            boolean add = sat.getSatSolver().addAtMostOne(vars);
            sat.afterAddingClauses();
            return add;
        }else{
            ref().sum(BOOLVARS, "<", 2).post();
            return true;
        }
    }

    /**
     * Add a clause stating that: &sum; BOOLVARS<sub>i</sub> &le; n-1
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesAtMostNMinusOne(BoolVar[] BOOLVARS) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            int[] vars = new int[BOOLVARS.length];
            for (int i = 0; i < BOOLVARS.length; i++) {
                vars[i] = sat.makeVar(BOOLVARS[i]);
            }
            boolean add = sat.getSatSolver().addAtMostNMinusOne(vars);
            sat.afterAddingClauses();
            return add;
        }else{
            ref().sum(BOOLVARS, "<", BOOLVARS.length).post();
            return true;
        }
    }

    /**
     * Add a clause stating that: sum(BOOLVARS<sub>i</sub>) &ge; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesSumBoolArrayGreaterEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            int[] vars = new int[BOOLVARS.length];
            for (int i = 0; i < BOOLVARS.length; i++) {
                vars[i] = sat.makeVar(BOOLVARS[i]);
            }
            boolean add = sat.getSatSolver().addSumBoolArrayGreaterEqVar(vars, sat.makeVar(TARGET));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().sum(BOOLVARS, ">=", TARGET).post();
            return true;
        }
    }

    /**
     * Add a clause stating that: max(BOOLVARS<sub>i</sub>) &le; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesMaxBoolArrayLessEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            int[] vars = new int[BOOLVARS.length];
            for (int i = 0; i < BOOLVARS.length; i++) {
                vars[i] = sat.makeVar(BOOLVARS[i]);
            }
            boolean add = sat.getSatSolver().addMaxBoolArrayLessEqVar(vars, sat.makeVar(TARGET));
            sat.afterAddingClauses();
            return add;
        }else{
            BoolVar max  = ref().boolVar(ref().generateName("bool_max"));
            ref().max(max, BOOLVARS).post();
            max.le(TARGET).post();
            return true;
        }
    }

    /**
     * Add a clause stating that: sum(BOOLVARS<sub>i</sub>) &le; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesSumBoolArrayLessEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        if(ref().getSettings().enableSAT()) {
            PropSat sat = ref().getMinisat().getPropSat();
            sat.beforeAddingClauses();
            boolean add = false;
            if (BOOLVARS.length == 1) {
                add = addClausesBoolLe(BOOLVARS[0], TARGET);
            }
            int[] vars = new int[BOOLVARS.length];
            for (int i = 0; i < BOOLVARS.length; i++) {
                vars[i] = sat.makeVar(BOOLVARS[i]);
            }
            add |= sat.getSatSolver().addSumBoolArrayLessEqVar(vars, sat.makeVar(TARGET));
            sat.afterAddingClauses();
            return add;
        }else{
            ref().sum(BOOLVARS, "<=", TARGET).post();
            return true;
        }
    }

    /**
     * Make a constructive disjunction constraint
     *
     * @param cstrs constraint in disjunction
     * @return <tt>true</tt> if the disjunction has been added to the constructive disjunction store.
     */
    default boolean addConstructiveDisjunction(Constraint... cstrs) {
        new LocalConstructiveDisjunction(cstrs).post();
        return true;
    }

}
