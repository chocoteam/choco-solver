/*
 * This file is part of choco-sat, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import static org.chocosolver.sat.SatSolver.makeLiteral;
import static org.chocosolver.sat.SatSolver.negated;

/**
 * <p>
 * Project: choco-sat.
 *
 * @author Charles Prud'homme
 * @since 07/03/2016.
 */
public interface SatFactory {

    SatSolver _me();
    /**
     * Ensures that the clause defined by POSLITS and NEGLITS is satisfied.
     *
     * @param POSVARS positive variables
     * @param NEGVARS negative variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addClause(int[] POSVARS, int[] NEGVARS) {
        TIntList lits = new TIntArrayList(POSVARS.length + NEGVARS.length);
        for(int p : POSVARS){
            lits.add(makeLiteral(p, true));
        }
        for(int n : NEGVARS){
            lits.add(makeLiteral(n, false));
        }
        return _me().addClause(lits);
    }

    /**
     * Add a unit clause stating that BOOLVAR must be true
     *
     * @param BOOLVAR a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addTrue(int BOOLVAR) {
        int lit = makeLiteral(BOOLVAR, true);
        return _me().addClause(lit);
    }

    /**
     * Add a unit clause stating that BOOLVAR must be false
     *
     * @param BOOLVAR a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addFalse(int BOOLVAR) {
        int lit = makeLiteral(BOOLVAR, false);
        return _me().addClause(lit);
    }

    /**
     * Add a clause stating that: LEFT == RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolEq(int LEFT, int RIGHT) {
        int left_lit = makeLiteral(LEFT, true);
        int right_lit = makeLiteral(RIGHT, true);
        boolean add = _me().addClause(SatSolver.negated(left_lit), right_lit);
        add &= _me().addClause(left_lit, SatSolver.negated(right_lit));
        return add;
    }

    /**
     * Add a clause stating that: LEFT &le; RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolLe(int LEFT, int RIGHT) {
        int left_lit = makeLiteral(LEFT, false);
        int right_lit = makeLiteral(RIGHT, true);
        return _me().addClause(left_lit, right_lit);
    }

    /**
     * Add a clause stating that: LEFT < RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolLt(int LEFT, int RIGHT) {
        int left_lit = makeLiteral(LEFT, false);
        int right_lit = makeLiteral(RIGHT, true);
        return _me().addClause(right_lit)
                & _me().addClause(left_lit, SatSolver.negated(right_lit));
    }

    /**
     * Add a clause stating that: LEFT != RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolNot(int LEFT, int RIGHT) {
        int left_lit = makeLiteral(LEFT, true);
        int right_lit = makeLiteral(RIGHT, true);
        return _me().addClause(SatSolver.negated(left_lit), SatSolver.negated(right_lit))
                & _me().addClause(left_lit, right_lit);
    }

    /**
     * Add a clause stating that: (BOOLVARS<sub>1</sub>&or;BOOLVARS<sub>2</sub>&or;...&or;BOOLVARS<sub>n</sub>) &hArr; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET   the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolOrArrayEqVar(int[] BOOLVARS, int TARGET) {
        int target_lit = makeLiteral(TARGET, true);
        TIntList lits = new TIntArrayList(BOOLVARS.length + 1);
        for (int i = 0; i < BOOLVARS.length; i++) {
            lits.add(makeLiteral(BOOLVARS[i], true));
        }
        lits.add(SatSolver.negated(target_lit));

        boolean add = _me().addClause(lits);
        for (int i = 0; i < BOOLVARS.length; i++) {
            add &= _me().addClause(target_lit, makeLiteral(BOOLVARS[i], false));
        }
        return add;
    }

    /**
     * Add a clause stating that: (BOOLVARS<sub>1</sub>&and;BOOLVARS<sub>2</sub>&and;...&and;BOOLVARS<sub>n</sub>) &hArr; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET   the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolAndArrayEqVar(int[] BOOLVARS, int TARGET) {
        int target_lit = makeLiteral(TARGET, true);
        TIntList lits = new TIntArrayList(BOOLVARS.length + 1);
        for (int i = 0; i < BOOLVARS.length; i++) {
            lits.add(makeLiteral(BOOLVARS[i], false));
        }
        lits.add(target_lit);
        boolean add = _me().addClause(lits);
        for (int i = 0; i < BOOLVARS.length; i++) {
            add &= _me().addClause(negated(target_lit), makeLiteral(BOOLVARS[i], true));
        }
        return add;
    }

    /**
     * Add a clause stating that: (LEFT &or; RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolOrEqVar(int LEFT, int RIGHT, int TARGET) {
        int left_lit = makeLiteral(LEFT, true);
        int right_lit = makeLiteral(RIGHT, true);
        int target_lit = makeLiteral(TARGET, true);
        boolean add = _me().addClause(left_lit, right_lit, SatSolver.negated(target_lit));
        add &= _me().addClause(SatSolver.negated(left_lit), target_lit);
        add &= _me().addClause(SatSolver.negated(right_lit), target_lit);
        return add;
    }

    /**
     * Add a clause stating that: (LEFT &and; RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolAndEqVar(int LEFT, int RIGHT, int TARGET) {
        int left_lit = makeLiteral(LEFT, true);
        int right_lit = makeLiteral(RIGHT, true);
        int target_lit = makeLiteral(TARGET, true);
        boolean add = _me().addClause(SatSolver.negated(left_lit), SatSolver.negated(right_lit), target_lit);
        add &= _me().addClause(left_lit, SatSolver.negated(target_lit));
        add &= _me().addClause(right_lit, SatSolver.negated(target_lit));
        return add;
    }

    /**
     * Add a clause stating that: (LEFT &oplus; RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolXorEqVar(int LEFT, int RIGHT, int TARGET) {
        return addBoolIsNeqVar(LEFT, RIGHT, TARGET);
    }

    /**
     * Add a clause stating that: (LEFT == RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolIsEqVar(int LEFT, int RIGHT, int TARGET) {
        int left_lit = makeLiteral(LEFT, true);
        int right_lit = makeLiteral(RIGHT, true);
        int target_lit = makeLiteral(TARGET, true);
        boolean add = _me().addClause(SatSolver.negated(left_lit), right_lit, SatSolver.negated(target_lit));
        add &= _me().addClause(left_lit, SatSolver.negated(right_lit), SatSolver.negated(target_lit));
        add &= _me().addClause(left_lit, right_lit, target_lit);
        add &= _me().addClause(SatSolver.negated(left_lit), SatSolver.negated(right_lit), target_lit);
        return add;
    }

    /**
     * Add a clause stating that: (LEFT &ne; RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolIsNeqVar(int LEFT, int RIGHT, int TARGET) {
        int left_lit = makeLiteral(LEFT, true);
        int right_lit = makeLiteral(RIGHT, true);
        int target_lit = makeLiteral(TARGET, true);
        boolean add = _me().addClause(SatSolver.negated(left_lit), right_lit, target_lit);
        add &= _me().addClause(left_lit, SatSolver.negated(right_lit), target_lit);
        add &= _me().addClause(left_lit, right_lit, SatSolver.negated(target_lit));
        add &= _me().addClause(SatSolver.negated(left_lit), SatSolver.negated(right_lit), SatSolver.negated(target_lit));
        return add;
    }

    /**
     * Add a clause stating that: (LEFT &le; RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolIsLeVar(int LEFT, int RIGHT, int TARGET) {
        int left_lit = makeLiteral(LEFT, true);
        int right_lit = makeLiteral(RIGHT, true);
        int target_lit = makeLiteral(TARGET, true);
        boolean add = _me().addClause(SatSolver.negated(left_lit), right_lit, SatSolver.negated(target_lit));
        add &= _me().addClause(left_lit, target_lit);
        add &= _me().addClause(SatSolver.negated(right_lit), target_lit);
        return add;
    }


    /**
     * Add a clause stating that: (LEFT < RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolIsLtVar(int LEFT, int RIGHT, int TARGET) {
        int left_lit = makeLiteral(LEFT, true);
        int right_lit = makeLiteral(RIGHT, true);
        int target_lit = makeLiteral(TARGET, true);
        boolean add =_me().addClause(left_lit, right_lit, SatSolver.negated(target_lit));
        add &= _me().addClause(SatSolver.negated(left_lit), right_lit, SatSolver.negated(target_lit));
        add &= _me().addClause(left_lit, SatSolver.negated(right_lit), target_lit);
        add &= _me().addClause(SatSolver.negated(left_lit), SatSolver.negated(right_lit), SatSolver.negated(target_lit));
        return add;
    }

    /**
     * Add a clause stating that: BOOLVARS<sub>1</sub>&or;BOOLVARS<sub>2</sub>&or;...&or;BOOLVARS<sub>n</sub>
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolOrArrayEqualTrue(int... BOOLVARS) {
        TIntList lits = new TIntArrayList(BOOLVARS.length);
        for (int i = 0; i < BOOLVARS.length; i++) {
            lits.add(makeLiteral(BOOLVARS[i], true));
        }
        return _me().addClause(lits);
    }

    /**
     * Add a clause stating that: BOOLVARS<sub>1</sub>&and;BOOLVARS<sub>2</sub>&and;...&and;BOOLVARS<sub>n</sub>
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addBoolAndArrayEqualFalse(int... BOOLVARS) {
        return addAtMostNMinusOne(BOOLVARS);
    }

    /**
     * Add a clause stating that: &sum; BOOLVARS<sub>i</sub> &le; 1
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addAtMostOne(int... BOOLVARS) {
        TIntList lits = new TIntArrayList(BOOLVARS.length);
        for (int i = 0; i < BOOLVARS.length; i++) {
            lits.add(SatSolver.negated(makeLiteral(BOOLVARS[i], true)));
        }
        boolean add = true;
        for (int i = 0; i < lits.size() - 1; i++) {
            for (int j = i + 1; j < lits.size(); ++j) {
                add &= _me().addClause(lits.get(i), lits.get(j));
            }
        }
        return add;
    }

    /**
     * Add a clause stating that: &sum; BOOLVARS<sub>i</sub> &le; n-1
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addAtMostNMinusOne(int... BOOLVARS) {
        TIntList lits = new TIntArrayList(BOOLVARS.length);
        for (int i = 0; i < BOOLVARS.length; i++) {
            lits.add(makeLiteral(BOOLVARS[i], false));
        }
        return _me().addClause(lits);
    }

    /**
     * Add a clause stating that: sum(BOOLVARS<sub>i</sub>) &ge; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET   a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addSumBoolArrayGreaterEqVar(int[] BOOLVARS, int TARGET) {
        TIntList lits = new TIntArrayList(BOOLVARS.length + 1);
        for (int i = 0; i < BOOLVARS.length; ++i) {
            lits.add(makeLiteral(BOOLVARS[i], true));
        }
        lits.add(makeLiteral(TARGET, false));
        return _me().addClause(lits);
    }

    /**
     * Add a clause stating that: max(BOOLVARS<sub>i</sub>) &le; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET   a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addMaxBoolArrayLessEqVar(int[] BOOLVARS, int TARGET) {
        int tlit = makeLiteral(TARGET, true);
        boolean add = true;
        for (int i = 0; i < BOOLVARS.length; ++i) {
            add &= _me().addClause(makeLiteral(BOOLVARS[i], false), tlit);
        }
        return true;
    }

    /**
     * Add a clause stating that: sum(BOOLVARS<sub>i</sub>) &le; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET   a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addSumBoolArrayLessEqVar(int[] BOOLVARS, int TARGET) {
        if (BOOLVARS.length == 1) {
            return addBoolLe(BOOLVARS[0], TARGET);
        }
        int extra = _me().newVariable();
        int tlit = makeLiteral(TARGET, true);
        int elit = makeLiteral(extra, true);
        TIntList lits = new TIntArrayList(BOOLVARS.length + 1);
        for (int i = 0; i < BOOLVARS.length; ++i) {
            lits.add(makeLiteral(BOOLVARS[i], true));
        }
        lits.add(SatSolver.negated(elit));
        boolean add = _me().addClause(lits);
        for (int i = 0; i < BOOLVARS.length; ++i) {
            add &= _me().addClause(elit, makeLiteral(BOOLVARS[i], false));
        }
        add &= _me().addClause(SatSolver.negated(elit), tlit);
        return add;
    }
}
