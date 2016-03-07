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
package org.chocosolver.solver.constraints;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.cnf.*;
import org.chocosolver.solver.constraints.reification.PropConDis;
import org.chocosolver.solver.variables.BoolVar;

import static org.chocosolver.util.tools.StringUtils.randomName;

/**
 * A factory dedicated to SAT.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/07/13
 */
public interface ISatFactory {

    Model _me();

    /**
     * Ensures that the clauses defined in the Boolean logic formula TREE are satisfied.
     *
     * @param TREE   the syntactic tree
     * @return true if the clause has been added to the clause store
     */
    default boolean addClauses(LogOp TREE) {
        Model model = _me();
        PropSat sat = model.getMinisat().getPropSat();
        ILogical tree = LogicTreeToolBox.toCNF(TREE, model);
        if (model.ONE().equals(tree)) {
            return addClauseTrue(model.ONE());
        } else if (model.ZERO().equals(tree)) {
            return addClauseTrue(model.ZERO());
        } else {
            ILogical[] clauses;
            if (!tree.isLit() && ((LogOp) tree).is(LogOp.Operator.AND)) {
                clauses = ((LogOp) tree).getChildren();
            } else {
                clauses = new ILogical[]{tree};
            }
            boolean ret = true;
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
                        lits.add(sat.Literal(bvars[j]));
                    }
                    ret &= sat.addClause(lits);
                }
            }
            return ret;
        }
    }

    /**
     * Ensures that the clause defined by POSLITS and NEGLITS is satisfied.
     *
     * @param POSLITS positive literals
     * @param NEGLITS negative literals
     * @return true if the clause has been added to the clause store
     */
    default boolean addClauses(BoolVar[] POSLITS, BoolVar[] NEGLITS) {
        Model model = POSLITS.length > 0 ? POSLITS[0].getModel() : NEGLITS[0].getModel();
        PropSat sat = model.getMinisat().getPropSat();
        TIntList lits = new TIntArrayList(POSLITS.length + NEGLITS.length);
        for (int i = 0; i < POSLITS.length; i++) {
            lits.add(sat.Literal(POSLITS[i]));
        }
        for (int i = 0; i < NEGLITS.length; i++) {
            lits.add(SatSolver.negated(sat.Literal(NEGLITS[i])));
        }
        sat.addClause(lits);
        return true;
    }

    /**
     * Add a unit clause stating that BOOLVAR must be true
     *
     * @param BOOLVAR a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClauseTrue(BoolVar BOOLVAR) {
        Model model = BOOLVAR.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int lit = sat.Literal(BOOLVAR);
        sat.addClause(lit);
        return true;
    }

    /**
     * Add a unit clause stating that BOOLVAR must be false
     *
     * @param BOOLVAR a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClauseFalse(BoolVar BOOLVAR) {
        Model model = BOOLVAR.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int lit = SatSolver.negated(sat.Literal(BOOLVAR));
        sat.addClause(lit);
        return true;
    }

    /**
     * Add a clause stating that: LEFT == RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolEq(BoolVar LEFT, BoolVar RIGHT) {
        Model model = LEFT.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int left_lit = sat.Literal(LEFT);
        int right_lit = sat.Literal(RIGHT);
        sat.addClause(SatSolver.negated(left_lit), right_lit);
        sat.addClause(left_lit, SatSolver.negated(right_lit));
        return true;
    }

    /**
     * Add a clause stating that: LEFT &le; RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolLe(BoolVar LEFT, BoolVar RIGHT) {
        Model model = LEFT.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int left_lit = sat.Literal(LEFT);
        int right_lit = sat.Literal(RIGHT);
        sat.addClause(SatSolver.negated(left_lit), right_lit);
        return true;
    }

    /**
     * Add a clause stating that: LEFT < RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolLt(BoolVar LEFT, BoolVar RIGHT) {
        Model model = LEFT.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int left_lit = sat.Literal(LEFT);
        int right_lit = sat.Literal(RIGHT);
        sat.addClause(right_lit);
        sat.addClause(SatSolver.negated(left_lit), SatSolver.negated(right_lit));
        return true;
    }

    /**
     * Add a clause stating that: LEFT != RIGHT
     *
     * @param LEFT  a boolean variable
     * @param RIGHT another boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolNot(BoolVar LEFT, BoolVar RIGHT) {
        Model model = LEFT.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int left_lit = sat.Literal(LEFT);
        int right_lit = sat.Literal(RIGHT);
        sat.addClause(SatSolver.negated(left_lit), SatSolver.negated(right_lit));
        sat.addClause(left_lit, right_lit);
        return true;
    }

    /**
     * Add a clause stating that: (BOOLVARS<sub>1</sub>&or;BOOLVARS<sub>2</sub>&or;...&or;BOOLVARS<sub>n</sub>) &hArr; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET   the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolOrArrayEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        Model model = TARGET.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int target_lit = sat.Literal(TARGET);
        TIntList lits = new TIntArrayList(BOOLVARS.length + 1);
        for (int i = 0; i < BOOLVARS.length; i++) {
            lits.add(sat.Literal(BOOLVARS[i]));
        }
        lits.add(SatSolver.negated(target_lit));
        sat.addClause(lits);
        for (int i = 0; i < BOOLVARS.length; i++) {
            sat.addClause(target_lit, SatSolver.negated(sat.Literal(BOOLVARS[i])));
        }
        return true;
    }

    /**
     * Add a clause stating that: (BOOLVARS<sub>1</sub>&and;BOOLVARS<sub>2</sub>&and;...&and;BOOLVARS<sub>n</sub>) &hArr; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET   the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolAndArrayEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        Model model = TARGET.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int target_lit = sat.Literal(TARGET);
        TIntList lits = new TIntArrayList(BOOLVARS.length + 1);
        for (int i = 0; i < BOOLVARS.length; i++) {
            lits.add(SatSolver.negated(sat.Literal(BOOLVARS[i])));
        }
        lits.add(target_lit);
        sat.addClause(lits);
        for (int i = 0; i < BOOLVARS.length; i++) {
            sat.addClause(SatSolver.negated(target_lit), sat.Literal(BOOLVARS[i]));
        }
        return true;
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
        Model model = TARGET.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int left_lit = sat.Literal(LEFT);
        int right_lit = sat.Literal(RIGHT);
        int target_lit = sat.Literal(TARGET);
        sat.addClause(left_lit, right_lit, SatSolver.negated(target_lit));
        sat.addClause(SatSolver.negated(left_lit), target_lit);
        sat.addClause(SatSolver.negated(right_lit), target_lit);
        return true;
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
        Model model = TARGET.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int left_lit = sat.Literal(LEFT);
        int right_lit = sat.Literal(RIGHT);
        int target_lit = sat.Literal(TARGET);
        sat.addClause(SatSolver.negated(left_lit), SatSolver.negated(right_lit), target_lit);
        sat.addClause(left_lit, SatSolver.negated(target_lit));
        sat.addClause(right_lit, SatSolver.negated(target_lit));
        return true;
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
        Model model = TARGET.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int left_lit = sat.Literal(LEFT);
        int right_lit = sat.Literal(RIGHT);
        int target_lit = sat.Literal(TARGET);
        sat.addClause(SatSolver.negated(left_lit), right_lit, SatSolver.negated(target_lit));
        sat.addClause(left_lit, SatSolver.negated(right_lit), SatSolver.negated(target_lit));
        sat.addClause(left_lit, right_lit, target_lit);
        sat.addClause(SatSolver.negated(left_lit), SatSolver.negated(right_lit), target_lit);
        return true;
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
        Model model = TARGET.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int left_lit = sat.Literal(LEFT);
        int right_lit = sat.Literal(RIGHT);
        int target_lit = sat.Literal(TARGET);
        sat.addClause(SatSolver.negated(left_lit), right_lit, target_lit);
        sat.addClause(left_lit, SatSolver.negated(right_lit), target_lit);
        sat.addClause(left_lit, right_lit, SatSolver.negated(target_lit));
        sat.addClause(SatSolver.negated(left_lit), SatSolver.negated(right_lit), SatSolver.negated(target_lit));
        return true;
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
        Model model = TARGET.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int left_lit = sat.Literal(LEFT);
        int right_lit = sat.Literal(RIGHT);
        int target_lit = sat.Literal(TARGET);
        sat.addClause(SatSolver.negated(left_lit), right_lit, SatSolver.negated(target_lit));
        sat.addClause(left_lit, target_lit);
        sat.addClause(SatSolver.negated(right_lit), target_lit);
        return true;
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
        Model model = TARGET.getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int left_lit = sat.Literal(LEFT);
        int right_lit = sat.Literal(RIGHT);
        int target_lit = sat.Literal(TARGET);
        sat.addClause(left_lit, right_lit, SatSolver.negated(target_lit));
        sat.addClause(SatSolver.negated(left_lit), right_lit, SatSolver.negated(target_lit));
        sat.addClause(left_lit, SatSolver.negated(right_lit), target_lit);
        sat.addClause(SatSolver.negated(left_lit), SatSolver.negated(right_lit), SatSolver.negated(target_lit));
        return true;
    }

    /**
     * Add a clause stating that: BOOLVARS<sub>1</sub>&or;BOOLVARS<sub>2</sub>&or;...&or;BOOLVARS<sub>n</sub>
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesBoolOrArrayEqualTrue(BoolVar[] BOOLVARS) {
        Model model = BOOLVARS[0].getModel();
        PropSat sat = model.getMinisat().getPropSat();
        TIntList lits = new TIntArrayList(BOOLVARS.length);
        for (int i = 0; i < BOOLVARS.length; i++) {
            lits.add(sat.Literal(BOOLVARS[i]));
        }
        sat.addClause(lits);
        return true;
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
        Model model = BOOLVARS[0].getModel();
        PropSat sat = model.getMinisat().getPropSat();
        TIntList lits = new TIntArrayList(BOOLVARS.length);
        for (int i = 0; i < BOOLVARS.length; i++) {
            lits.add(SatSolver.negated(sat.Literal(BOOLVARS[i])));
        }
        for (int i = 0; i < lits.size() - 1; i++) {
            for (int j = i + 1; j < lits.size(); ++j) {
                sat.addClause(lits.get(i), lits.get(j));
            }
        }
        return true;
    }

    /**
     * Add a clause stating that: &sum; BOOLVARS<sub>i</sub> &le; n-1
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesAtMostNMinusOne(BoolVar[] BOOLVARS) {
        Model model = BOOLVARS[0].getModel();
        PropSat sat = model.getMinisat().getPropSat();
        TIntList lits = new TIntArrayList(BOOLVARS.length);
        for (int i = 0; i < BOOLVARS.length; i++) {
            lits.add(SatSolver.negated(sat.Literal(BOOLVARS[i])));
        }
        sat.addClause(lits);
        return true;
    }

    /**
     * Add a clause stating that: sum(BOOLVARS<sub>i</sub>) &ge; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesSumBoolArrayGreaterEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        Model model = BOOLVARS[0].getModel();
        PropSat sat = model.getMinisat().getPropSat();
        TIntList lits = new TIntArrayList(BOOLVARS.length + 1);
        for (int i = 0; i < BOOLVARS.length; ++i) {
            lits.add(sat.Literal(BOOLVARS[i]));
        }
        lits.add(SatSolver.negated(sat.Literal(TARGET)));
        sat.addClause(lits);
        return true;
    }

    /**
     * Add a clause stating that: max(BOOLVARS<sub>i</sub>) &le; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesMaxBoolArrayLessEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        Model model = BOOLVARS[0].getModel();
        PropSat sat = model.getMinisat().getPropSat();
        int tlit = sat.Literal(TARGET);
        for (int i = 0; i < BOOLVARS.length; ++i) {
            sat.addClause(SatSolver.negated(sat.Literal(BOOLVARS[i])), tlit);
        }
        return true;
    }

    /**
     * Add a clause stating that: sum(BOOLVARS<sub>i</sub>) &le; TARGET
     *
     * @param BOOLVARS a list of boolean variables
     * @param TARGET a boolean variable
     * @return true if the clause has been added to the clause store
     */
    default boolean addClausesSumBoolArrayLessEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {

        Model model = BOOLVARS[0].getModel();
        PropSat sat = model.getMinisat().getPropSat();
        if (BOOLVARS.length == 1) {
            return addClausesBoolLe(BOOLVARS[0], TARGET);
        }

        BoolVar extra = model.boolVar(randomName());
        int tlit = sat.Literal(TARGET);
        int elit = sat.Literal(extra);
        TIntList lits = new TIntArrayList(BOOLVARS.length + 1);
        for (int i = 0; i < BOOLVARS.length; ++i) {
            lits.add(sat.Literal(BOOLVARS[i]));
        }
        lits.add(SatSolver.negated(elit));
        sat.addClause(lits);
        for (int i = 0; i < BOOLVARS.length; ++i) {
            sat.addClause(elit, SatSolver.negated(sat.Literal(BOOLVARS[i])));
        }
        sat.addClause(SatSolver.negated(elit), tlit);
        return true;
    }

    /**
     * Make an constructive disjunction constraint
     *
     * @param BOOLS an array of boolean variable
     * @return <tt>true</tt> if the disjunction has been added to the constructive disjunction store.
     */
    default boolean addConstructiveDisjunction(BoolVar... BOOLS) {
        Model model = BOOLS[0].getModel();
        PropConDis condis = model.getConDisStore().getPropCondis();
        condis.addDisjunction(BOOLS);
        return true;
    }
}
