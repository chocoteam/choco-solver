/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.sat.PropSat;
import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.cnf.ILogical;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.constraints.nary.cnf.LogicTreeToolBox;
import org.chocosolver.solver.constraints.reification.LocalConstructiveDisjunction;
import org.chocosolver.solver.constraints.reification.PropConDis;
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
        PropSat sat = _me().getMinisat().getPropSat();
        ILogical tree = LogicTreeToolBox.toCNF(TREE, _me());
        sat.beforeAddingClauses();
        boolean ret = true;
        if (_me().boolVar(true).equals(tree)) {
            ret = addClauseTrue(_me().boolVar(true));
        } else if (_me().boolVar(false).equals(tree)) {
            ret = addClauseTrue(_me().boolVar(false));
        } else {
            ILogical[] clauses;
            if (!tree.isLit() && ((LogOp) tree).is(LogOp.Operator.AND)) {
                clauses = ((LogOp) tree).getChildren();
            } else {
                clauses = new ILogical[]{tree};
            }
            // init internal structures
            for (int i = 0; i < clauses.length; i++) {
                ILogical clause = clauses[i];
                if (clause.isLit()) {
                    BoolVar bv = (BoolVar) clause;
                    ret &= addClauseTrue(bv);
                } else {
                    LogOp n = (LogOp) clause;
                    BoolVar[] bvars = n.flattenBoolVar();
                    TIntList lits = new TIntArrayList(bvars.length);
                    for (int j = 0; j < bvars.length; j++) {
                        lits.add(sat.makeLiteral(bvars[j], true));
                    }
                    // TODO: pass by satsolver directly
                    ret &= sat.addClause(lits);
                }
            }
        }
        sat.afterAddingClauses();
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
        PropSat sat = _me().getMinisat().getPropSat();
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
    }

    /**
     * Add a unit clause stating that BOOLVAR must be true
     *
     * @param BOOLVAR a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClauseTrue(BoolVar BOOLVAR) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add = sat.getSatSolver().addTrue(sat.makeVar(BOOLVAR));
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Add a unit clause stating that BOOLVAR must be false
     *
     * @param BOOLVAR a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClauseFalse(BoolVar BOOLVAR) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add = sat.getSatSolver().addFalse(sat.makeVar(BOOLVAR));
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Add a clause stating that: LEFT == RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolEq(BoolVar LEFT, BoolVar RIGHT) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add = sat.getSatSolver().addBoolEq(sat.makeVar(LEFT), sat.makeVar(RIGHT));
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Add a clause stating that: LEFT &le; RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolLe(BoolVar LEFT, BoolVar RIGHT) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add = sat.getSatSolver().addBoolLe(sat.makeVar(LEFT), sat.makeVar(RIGHT));
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Add a clause stating that: LEFT < RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolLt(BoolVar LEFT, BoolVar RIGHT) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add = sat.getSatSolver().addBoolLt(sat.makeVar(LEFT), sat.makeVar(RIGHT));
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Add a clause stating that: LEFT != RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolNot(BoolVar LEFT, BoolVar RIGHT) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add = sat.getSatSolver().addBoolNot(sat.makeVar(LEFT), sat.makeVar(RIGHT));
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Add a clause stating that: (BOOLVARS<sub>1</sub>&or;BOOLVARS<sub>2</sub>&or;...&or;BOOLVARS<sub>n</sub>) &hArr; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET   the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolOrArrayEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        int[] vars = new int[BOOLVARS.length];
        for (int i = 0; i < BOOLVARS.length; i++) {
            vars[i] = sat.makeVar(BOOLVARS[i]);
        }
        boolean add = sat.getSatSolver().addBoolOrArrayEqVar(vars, sat.makeVar(TARGET));
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Add a clause stating that: (BOOLVARS<sub>1</sub>&and;BOOLVARS<sub>2</sub>&and;...&and;BOOLVARS<sub>n</sub>) &hArr; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET   the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolAndArrayEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        int[] vars = new int[BOOLVARS.length];
        for (int i = 0; i < BOOLVARS.length; i++) {
            vars[i] = sat.makeVar(BOOLVARS[i]);
        }
        boolean add = sat.getSatSolver().addBoolAndArrayEqVar(vars, sat.makeVar(TARGET));
        sat.afterAddingClauses();
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
    default boolean addClausesBoolOrEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add = sat.getSatSolver().addBoolOrEqVar(sat.makeVar(LEFT), sat.makeVar(RIGHT), sat.makeVar(TARGET));
        sat.afterAddingClauses();
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
    default boolean addClausesBoolAndEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add = sat.getSatSolver().addBoolAndEqVar(sat.makeVar(LEFT), sat.makeVar(RIGHT), sat.makeVar(TARGET));
        sat.afterAddingClauses();
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
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add = sat.getSatSolver().addBoolIsEqVar(sat.makeVar(LEFT), sat.makeVar(RIGHT), sat.makeVar(TARGET));
        sat.afterAddingClauses();
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
    default boolean addClausesBoolIsNeqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add = sat.getSatSolver().addBoolIsNeqVar(sat.makeVar(LEFT), sat.makeVar(RIGHT), sat.makeVar(TARGET));
        sat.afterAddingClauses();
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
    default boolean addClausesBoolIsLeVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add = sat.getSatSolver().addBoolIsLeVar(sat.makeVar(LEFT), sat.makeVar(RIGHT), sat.makeVar(TARGET));
        sat.afterAddingClauses();
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
    default boolean addClausesBoolIsLtVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add = sat.getSatSolver().addBoolIsLtVar(sat.makeVar(LEFT), sat.makeVar(RIGHT), sat.makeVar(TARGET));
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Add a clause stating that: BOOLVARS<sub>1</sub>&or;BOOLVARS<sub>2</sub>&or;...&or;BOOLVARS<sub>n</sub>
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolOrArrayEqualTrue(BoolVar[] BOOLVARS) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        int[] vars = new int[BOOLVARS.length];
        for (int i = 0; i < BOOLVARS.length; i++) {
            vars[i] = sat.makeVar(BOOLVARS[i]);
        }
        boolean add = sat.getSatSolver().addBoolOrArrayEqualTrue(vars);
        sat.afterAddingClauses();
        return add;
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
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        int[] vars = new int[BOOLVARS.length];
        for (int i = 0; i < BOOLVARS.length; i++) {
            vars[i] = sat.makeVar(BOOLVARS[i]);
        }
        boolean add = sat.getSatSolver().addAtMostOne(vars);
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Add a clause stating that: &sum; BOOLVARS<sub>i</sub> &le; n-1
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesAtMostNMinusOne(BoolVar[] BOOLVARS) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        int[] vars = new int[BOOLVARS.length];
        for (int i = 0; i < BOOLVARS.length; i++) {
            vars[i] = sat.makeVar(BOOLVARS[i]);
        }
        boolean add = sat.getSatSolver().addAtMostNMinusOne(vars);
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Add a clause stating that: sum(BOOLVARS<sub>i</sub>) &ge; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesSumBoolArrayGreaterEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        int[] vars = new int[BOOLVARS.length];
        for (int i = 0; i < BOOLVARS.length; i++) {
            vars[i] = sat.makeVar(BOOLVARS[i]);
        }
        boolean add = sat.getSatSolver().addSumBoolArrayGreaterEqVar(vars, sat.makeVar(TARGET));
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Add a clause stating that: max(BOOLVARS<sub>i</sub>) &le; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesMaxBoolArrayLessEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        int[] vars = new int[BOOLVARS.length];
        for (int i = 0; i < BOOLVARS.length; i++) {
            vars[i] = sat.makeVar(BOOLVARS[i]);
        }
        boolean add = sat.getSatSolver().addMaxBoolArrayLessEqVar(vars, sat.makeVar(TARGET));
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Add a clause stating that: sum(BOOLVARS<sub>i</sub>) &le; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesSumBoolArrayLessEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        PropSat sat = _me().getMinisat().getPropSat();
        sat.beforeAddingClauses();
        boolean add;
        if (BOOLVARS.length == 1) {
            add = addClausesBoolLe(BOOLVARS[0], TARGET);
        }
        int[] vars = new int[BOOLVARS.length];
        for (int i = 0; i < BOOLVARS.length; i++) {
            vars[i] = sat.makeVar(BOOLVARS[i]);
        }
        add = sat.getSatSolver().addSumBoolArrayLessEqVar(vars, sat.makeVar(TARGET));
        sat.afterAddingClauses();
        return add;
    }

    /**
     * Make an constructive disjunction constraint
     *
     * @param global set to <tt>true</tt> to enable constructive disjunction over all the constraint network of the CSP
     *               (presumably filters more values, but slower),
     *               set to <tt>false</tt> to restrict propagation to variables directly involved in <i>cstrs</i>.
     *               In the latter, make sure at least one variable is shared by all constraints otherwise no filtering
     *               will happen.
     * @param cstrs constraint in disjunction
     * @return <tt>true</tt> if the disjunction has been added to the constructive disjunction store.
     */
    default boolean addConstructiveDisjunction(boolean global, Constraint... cstrs) {
        Model model = cstrs[0].propagators[0].getModel();
        if(global) {
            PropConDis condis = model.getConDisStore().getPropCondis();
            condis.addDisjunction(cstrs);
        }else{
            new LocalConstructiveDisjunction(cstrs).post();
        }
        return true;
    }

}
