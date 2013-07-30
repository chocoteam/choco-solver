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
package solver.constraints;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.nary.cnf.*;
import solver.variables.BoolVar;

/**
 * A factory dedicated to SAT.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/07/13
 */
public class SatFactory {

    private SatFactory() {
    }

    private static boolean buildOnLogicalOperator(LogOp logOp, Solver solver) {
        PropSat sat = solver.getMinisat().propagators[0];
        ILogical tree = LogicTreeToolBox.toCNF(logOp, solver);
        if (solver.ONE.equals(tree)) {
            return addTrue(solver.ZERO);
        } else if (solver.ZERO.equals(tree)) {
            return addTrue(solver.ONE);
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
                    ret &= addTrue(bv);
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
     * Ensures that the clauses defined in the Boolean logic formula TREE are satisfied.
     *
     * @param TREE   the syntactic tree
     * @param SOLVER solver is required, as the TREE can be declared without any variables
     * @return true if the clause has been added to the clause store
     */
    public static boolean addClauses(LogOp TREE, Solver SOLVER) {
        return buildOnLogicalOperator(TREE, SOLVER);
    }

    /**
     * Ensures that the clause defined by POSLITS and NEGLITS is satisfied.
     *
     * @param POSLITS positive literals
     * @param NEGLITS negative literals
     * @return true if the clause has been added to the clause store
     */
    public static boolean addClauses(BoolVar[] POSLITS, BoolVar[] NEGLITS) {
        Solver solver = POSLITS.length > 0 ? POSLITS[0].getSolver() : NEGLITS[0].getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
        TIntList lits = new TIntArrayList(POSLITS.length + NEGLITS.length);
        for (int i = 0; i < POSLITS.length; i++) {
            lits.add(SatSolver.negated(sat.Literal(POSLITS[i])));
        }
        for (int i = 0; i < NEGLITS.length; i++) {
            lits.add(sat.Literal(POSLITS[i]));
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
    public static boolean addTrue(BoolVar BOOLVAR) {
        Solver solver = BOOLVAR.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
        int lit = sat.Literal(BOOLVAR);
        sat.addClause(SatSolver.negated(lit));
        return true;
    }

    /**
     * Add a unit clause stating that BOOLVAR must be false
     *
     * @param BOOLVAR a boolean variable
     * @return true if the clause has been added to the clause store
     */
    public static boolean addFalse(BoolVar BOOLVAR) {
        Solver solver = BOOLVAR.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
        int lit = sat.Literal(BOOLVAR);
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
    public static boolean addBoolEq(BoolVar LEFT, BoolVar RIGHT) {
        Solver solver = LEFT.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolLe(BoolVar LEFT, BoolVar RIGHT) {
        Solver solver = LEFT.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolLt(BoolVar LEFT, BoolVar RIGHT) {
        Solver solver = LEFT.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolNot(BoolVar LEFT, BoolVar RIGHT) {
        Solver solver = LEFT.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolOrArrayEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        Solver solver = TARGET.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolAndArrayEqVar(BoolVar[] BOOLVARS, BoolVar TARGET) {
        Solver solver = TARGET.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolOrEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        Solver solver = TARGET.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolAndEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        Solver solver = TARGET.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolXorEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        return addBoolIsNEqVar(LEFT, RIGHT, TARGET);
    }

    /**
     * Add a clause stating that: (LEFT == RIGHT) &hArr; TARGET
     *
     * @param LEFT   a boolean variable
     * @param RIGHT  another boolean variable
     * @param TARGET the reified boolean variable
     * @return true if the clause has been added to the clause store
     */
    public static boolean addBoolIsEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        Solver solver = TARGET.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolIsNEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        Solver solver = TARGET.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolIsLeVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        Solver solver = TARGET.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolIsLtVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET) {
        Solver solver = TARGET.getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolOrArrayEqualTrue(BoolVar[] BOOLVARS) {
        Solver solver = BOOLVARS[0].getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    public static boolean addBoolAndArrayEqualFalse(BoolVar[] BOOLVARS) {
        Solver solver = BOOLVARS[0].getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
        TIntList lits = new TIntArrayList(BOOLVARS.length);
        for (int i = 0; i < BOOLVARS.length; i++) {
            lits.add(SatSolver.negated(sat.Literal(BOOLVARS[i])));
        }
        sat.addClause(lits);
        return true;
    }

    /**
     * Add a clause stating that: &sum; BOOLVARS<sub>i</sub> &le; 1
     *
     * @param BOOLVARS a list of boolean variables
     * @return true if the clause has been added to the clause store
     */
    public static boolean addAtMostOne(BoolVar[] BOOLVARS) {
        Solver solver = BOOLVARS[0].getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
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
    boolean AddAtMostNMinusOne(BoolVar[] BOOLVARS) {
        Solver solver = BOOLVARS[0].getSolver();
        PropSat sat = solver.getMinisat().propagators[0];
        TIntList lits = new TIntArrayList(BOOLVARS.length);
        for (int i = 0; i < BOOLVARS.length; i++) {
            lits.add(SatSolver.negated(sat.Literal(BOOLVARS[i])));
        }
        sat.addClause(lits);
        return true;
    }

//    public static boolean addArrayXor(PropSat sat, BoolVar[] vars) {
//        return false;
//    }
}
