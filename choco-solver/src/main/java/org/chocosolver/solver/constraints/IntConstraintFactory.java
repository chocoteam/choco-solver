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

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.binary.*;
import org.chocosolver.solver.constraints.binary.element.ElementFactory;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.constraints.extension.binary.*;
import org.chocosolver.solver.constraints.extension.nary.*;
import org.chocosolver.solver.constraints.nary.PropDiffN;
import org.chocosolver.solver.constraints.nary.PropIntValuePrecedeChain;
import org.chocosolver.solver.constraints.nary.PropKLoops;
import org.chocosolver.solver.constraints.nary.PropKnapsack;
import org.chocosolver.solver.constraints.nary.alldifferent.AllDifferent;
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.Condition;
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.PropCondAllDiffInst;
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.PropCondAllDiff_AC;
import org.chocosolver.solver.constraints.nary.among.PropAmongGAC_GoodImpl;
import org.chocosolver.solver.constraints.nary.automata.CostRegular;
import org.chocosolver.solver.constraints.nary.automata.FA.IAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.PropMultiCostRegular;
import org.chocosolver.solver.constraints.nary.automata.PropRegular;
import org.chocosolver.solver.constraints.nary.channeling.*;
import org.chocosolver.solver.constraints.nary.circuit.*;
import org.chocosolver.solver.constraints.nary.count.PropCountVar;
import org.chocosolver.solver.constraints.nary.count.PropCount_AC;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.constraints.nary.element.PropElementV_fast;
import org.chocosolver.solver.constraints.nary.globalcardinality.GlobalCardinality;
import org.chocosolver.solver.constraints.nary.lex.PropLex;
import org.chocosolver.solver.constraints.nary.lex.PropLexChain;
import org.chocosolver.solver.constraints.nary.min_max.PropBoolMax;
import org.chocosolver.solver.constraints.nary.min_max.PropBoolMin;
import org.chocosolver.solver.constraints.nary.min_max.PropMax;
import org.chocosolver.solver.constraints.nary.min_max.PropMin;
import org.chocosolver.solver.constraints.nary.nValue.PropAMNV;
import org.chocosolver.solver.constraints.nary.nValue.PropAtLeastNValues;
import org.chocosolver.solver.constraints.nary.nValue.PropAtLeastNValues_AC;
import org.chocosolver.solver.constraints.nary.nValue.PropAtMostNValues;
import org.chocosolver.solver.constraints.nary.nValue.amnv.differences.AutoDiffDetection;
import org.chocosolver.solver.constraints.nary.nValue.amnv.graph.Gci;
import org.chocosolver.solver.constraints.nary.nValue.amnv.mis.MDRk;
import org.chocosolver.solver.constraints.nary.nValue.amnv.rules.R;
import org.chocosolver.solver.constraints.nary.nValue.amnv.rules.R1;
import org.chocosolver.solver.constraints.nary.nValue.amnv.rules.R3;
import org.chocosolver.solver.constraints.nary.sort.PropKeysorting;
import org.chocosolver.solver.constraints.nary.sum.IntLinCombFactory;
import org.chocosolver.solver.constraints.nary.tree.PropAntiArborescences;
import org.chocosolver.solver.constraints.ternary.*;
import org.chocosolver.solver.constraints.unary.Member;
import org.chocosolver.solver.constraints.unary.NotMember;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.StringUtils;

import java.util.Arrays;

/**
 * A Factory to declare constraint based on integer variables (only).
 * One can call directly the constructor of constraints, but it is recommended
 * to use the Factory, because signatures and javadoc are ensured to be up-to-date.
 * <br/>
 * As much as possible, the API names of global constraints must match
 * those define in the <a href="http://www.emn.fr/z-info/sdemasse/gccat/index.html">Global Constraint Catalog</a>.
 * <p>
 * Note that, for the sack of readability, the Java naming convention is not respected for methods arguments.
 * <p>
 * Constraints are ordered as the following:
 * 1) Unary	constraints
 * 2) Binary constraints
 * 3) Terary constraints
 * 4) Global constraints
 *
 * @author Charles Prud'homme
 * @since 21/01/13
 */
@SuppressWarnings("UnusedDeclaration")
public class IntConstraintFactory {
    IntConstraintFactory() {
    }

    // BEWARE: PLEASE, keep signatures sorted by increasing arity and alphabetical order!!

    //##################################################################################################################
    // ZEROARIES #######################################################################################################
    //##################################################################################################################

    /**
     * Ensures the TRUE constraint
     *
     * @param solver a solver
     * @return a true constraint
     */
    public static Constraint TRUE(Solver solver) {
        return solver.TRUE();
    }

    /**
     * Ensures the FALSE constraint
     *
     * @param solver a solver
     * @return a false constraint
     */
    public static Constraint FALSE(Solver solver) {
        return solver.FALSE();
    }

    //##################################################################################################################
    // UNARIES #########################################################################################################
    //##################################################################################################################

    /**
     * Ensures: VAR OP CSTE, where OP in {"=", "!=", ">","<",">=","<="}
     *
     * @param VAR  a variable
     * @param OP   an operator
     * @param CSTE a constant
     */
    public static Constraint arithm(IntVar VAR, String OP, int CSTE) {
        Operator op = Operator.get(OP);
        return new Arithmetic(VAR, op, CSTE);
    }

    /**
     * Ensures VAR takes its values in TABLE
     *
     * @param VAR   an integer variable
     * @param TABLE an array of values
     */
    public static Constraint member(IntVar VAR, int[] TABLE) {
        return new Member(VAR, TABLE);
    }

    /**
     * Ensures VAR takes its values in [LB, UB]
     *
     * @param VAR an integer variable
     * @param LB  the lower bound of the interval
     * @param UB  the upper bound of the interval
     */
    public static Constraint member(IntVar VAR, int LB, int UB) {
        return new Member(VAR, LB, UB);
    }

    /**
     * Ensures VAR does not take its values in TABLE
     *
     * @param VAR   an integer variable
     * @param TABLE an array of values
     */
    public static Constraint not_member(IntVar VAR, int[] TABLE) {
        return new NotMember(VAR, TABLE);
    }

    /**
     * Ensures VAR does not take its values in [LB, UB]
     *
     * @param VAR an integer variable
     * @param LB  the lower bound of the interval
     * @param UB  the upper bound of the interval
     */
    public static Constraint not_member(IntVar VAR, int LB, int UB) {
        return new NotMember(VAR, LB, UB);
    }

    //##################################################################################################################
    //BINARIES #########################################################################################################
    //##################################################################################################################

    /**
     * Enforces VAR1 = |VAR2|
     */
    public static Constraint absolute(IntVar VAR1, IntVar VAR2) {
        assert VAR1.getSolver() == VAR2.getSolver();
        return new Constraint("Absolute", new PropAbsolute(VAR1, VAR2));
    }

    /**
     * Ensures: VAR1 OP VAR2, where OP in {"=", "!=", ">","<",">=","<="}
     *
     * @param VAR1 first variable
     * @param OP   an operator
     * @param VAR2 second variable
     */
    public static Constraint arithm(IntVar VAR1, String OP, IntVar VAR2) {
        if (VAR2.isInstantiated()) {
            return arithm(VAR1, OP, VAR2.getValue());
        }
        if (VAR1.isInstantiated()) {
            return arithm(VAR2, Operator.getFlip(OP), VAR1.getValue());
        }
        return new Arithmetic(VAR1, Operator.get(OP), VAR2);
    }

    /**
     * Ensures: VAR1 OP VAR2, where OP in {"=", "!=", ">","<",">=","<="} or {"+", "-"}
     *
     * @param VAR1 first variable
     * @param OP1  an operator
     * @param VAR2 second variable
     * @param OP2  another operator
     * @param CSTE an operator
     */
    public static Constraint arithm(IntVar VAR1, String OP1, IntVar VAR2, String OP2, int CSTE) {
        if (VAR2.isInstantiated()) {
            if (OP1.equals("+")) {
                return arithm(VAR1, OP2, CSTE - VAR2.getValue());
            } else if (OP1.equals("-")) {
                return arithm(VAR1, OP2, CSTE + VAR2.getValue());
            }
        }
        if (VAR1.isInstantiated()) {
            if (OP1.equals("+")) {
                return arithm(VAR2, OP2, CSTE - VAR1.getValue());
            } else if (OP1.equals("-")) {
                return arithm(VAR2, Operator.getFlip(OP2), VAR1.getValue() - CSTE);
            }
        }
        Operator op1 = Operator.get(OP1);
        Operator op2 = Operator.get(OP2);
        return new Arithmetic(VAR1, op1, VAR2, op2, CSTE);
    }

    /**
     * Ensures: <br/>
     * |VAR1-VAR2| OP CSTE
     * <br/>
     * where OP can take its value among {"=", ">", "<", "!="}
     */
    public static Constraint distance(IntVar VAR1, IntVar VAR2, String OP, int CSTE) {
        assert VAR1.getSolver() == VAR2.getSolver();
        return new DistanceXYC(VAR1, VAR2, Operator.get(OP), CSTE);
    }

    /**
     * Build ELEMENT constraint: VALUE = TABLE[INDEX-OFFSET]
     *
     * @param VALUE  an integer variable taking its value in TABLE
     * @param TABLE  an array of integer values
     * @param INDEX  an integer variable representing the value of VALUE in TABLE
     * @param OFFSET offset matching INDEX.LB and TABLE[0] (Generally 0)
     * @param SORT   defines ordering properties of TABLE:
     *               <p/> "none" if TABLE is not sorted
     *               <p/> "asc" if TABLE is sorted in the increasing order
     *               <p/> "desc" if TABLE is sorted in the decreasing order
     *               <p/> "detect" Let the constraint detects the ordering of TABLE, if any
     */
    public static Constraint element(IntVar VALUE, int[] TABLE, IntVar INDEX, int OFFSET, String SORT) {
        return ElementFactory.detect(VALUE, TABLE, INDEX, OFFSET);
    }

    /**
     * Build ELEMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param VALUE an integer variable taking its value in TABLE
     * @param TABLE an array of integer values
     * @param INDEX an integer variable representing the value of VALUE in TABLE
     */
    public static Constraint element(IntVar VALUE, int[] TABLE, IntVar INDEX) {
        return element(VALUE, TABLE, INDEX, 0, "detect");
    }

    /**
     * Enforces VAR1 = VAR2^2
     */
    public static Constraint square(IntVar VAR1, IntVar VAR2) {
        assert VAR1.getSolver() == VAR2.getSolver();
        return new Constraint("Square", new PropSquare(VAR1, VAR2));
    }

    /**
     * Create a table constraint over a couple of variables VAR1 and VAR2
     *
     * Uses AC3rm algorithm by default
     *
     * @param VAR1   first variable
     * @param VAR2   second variable
     */
    public static Constraint table(IntVar VAR1, IntVar VAR2, Tuples TUPLES) {
        return table(VAR1,VAR2,TUPLES,"AC3rm");
    }

    /**
     * Create a table constraint over a couple of variables VAR1 and VAR2:<br/>
     * - <b>AC2001</b>: table constraint which applies the AC2001 algorithm,<br/>
     * - <b>AC3</b>: table constraint which applies the AC3 algorithm,<br/>
     * - <b>AC3rm</b>: table constraint which applies the AC3 rm algorithm,<br/>
     * - <b>AC3bit+rm</b> (default): table constraint which applies the AC3 bit+rm algorithm,<br/>
     * - <b>FC</b>: table constraint which applies forward checking algorithm.<br/>
     *
     * @param VAR1   first variable
     * @param VAR2   second variable
     * @param TUPLES the relation between the two variables, among {"AC3", "AC3rm", "AC3bit+rm", "AC2001", "FC"}
     */
    public static Constraint table(IntVar VAR1, IntVar VAR2, Tuples TUPLES, String ALGORITHM) {
        Propagator p;
        switch (ALGORITHM) {
            case "AC2001": p = new PropBinAC2001(VAR1, VAR2, TUPLES);
                break;
            case "FC": p = new PropBinFC(VAR1, VAR2, TUPLES);
                break;
            case "AC3": p = new PropBinAC3(VAR1, VAR2, TUPLES);
                break;
            case "AC3rm": p = new PropBinAC3rm(VAR1, VAR2, TUPLES);
                break;
            case "AC3bit+rm": p = new PropBinAC3bitrm(VAR1, VAR2, TUPLES);
                break;
            default: throw new SolverException("Table algorithm "+ALGORITHM+" is unkown");
        }
        return new Constraint("TableBin(" + ALGORITHM + ")", p);
    }

    //##################################################################################################################
    //TERNARIES ########################################################################################################
    //##################################################################################################################

    /**
     * Ensures: <br/>
     * |VAR1-VAR2| OP VAR3
     * <br/>
     * where OP can take its value among {"=", ">", "<"}
     *
     * @param VAR1 first variable
     * @param VAR2 second variable
     * @param OP   an operator
     * @param VAR3 resulting variable
     */
    public static Constraint distance(IntVar VAR1, IntVar VAR2, String OP, IntVar VAR3) {
        return new DistanceXYZ(VAR1, VAR2, Operator.get(OP), VAR3);
    }

    /**
     * Ensures DIVIDEND / DIVISOR = RESULT, rounding towards 0 -- Euclidean division
     * Also ensures DIVISOR != 0
     *
     * @param DIVIDEND dividend
     * @param DIVISOR  divisor
     * @param RESULT   result
     */
    public static Constraint eucl_div(IntVar DIVIDEND, IntVar DIVISOR, IntVar RESULT) {
        return new Constraint("DivisionEucl", new PropDivXYZ(DIVIDEND, DIVISOR, RESULT));
    }

    /**
     * Ensures: MAX = MAX(VAR1, VAR2)
     * (Bound Consistency)
     *
     * @param MAX  a variable
     * @param VAR1 a variable
     * @param VAR2 a variable
     */
    public static Constraint maximum(IntVar MAX, IntVar VAR1, IntVar VAR2) {
        return new Constraint("Max", new PropMaxBC(MAX, VAR1, VAR2));
    }

    /**
     * Ensures:  VAR1 = MIN(VAR2, VAR3)
     * (Bound Consistency)
     *
     * @param MIN  result
     * @param VAR1 result
     * @param VAR2 first variable
     */
    public static Constraint minimum(IntVar MIN, IntVar VAR1, IntVar VAR2) {
        return new Constraint("Min", new PropMinBC(MIN, VAR1, VAR2));
    }

    /**
     * Ensures X % Y = Z,
     * <br/>i.e.:<br/>
     * - X / Y = T1 and,<br/>
     * - T1 * Y = T2 and,<br/>
     * - Z + T2 = X<br/>
     * <br/>
     * where T1 = T2 = [-|X|, |X|]
     *
     * @param X first variable
     * @param Y second variable
     * @param Z result
     */
    public static Constraint mod(IntVar X, IntVar Y, IntVar Z) {
        int xl = Math.abs(X.getLB());
        int xu = Math.abs(X.getUB());
        int b = Math.max(xl, xu);
        Solver solver = X.getSolver();
        IntVar t1 = VF.bounded(StringUtils.randomName(), -b, b, solver);
        IntVar t2 = VF.bounded(StringUtils.randomName(), -b, b, solver);
        solver.post(eucl_div(X, Y, t1));
        solver.post(times(t1, Y, t2));
        return sum(new IntVar[]{Z, t2}, X);
    }

    /**
     * Ensures: X * Y = Z
     *
     * @param X first variable
     * @param Y second variable
     * @param Z result variable
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static Constraint times(IntVar X, IntVar Y, IntVar Z) {
        if (Y.isInstantiated()) {
            return times(X, Y.getValue(), Z);
        } else if (X.isInstantiated()) {
            return times(Y, X.getValue(), Z);
        } else if (tupleIt(X, Y, Z)) {
            return table(new IntVar[]{X, Y, Z}, TuplesFactory.times(X, Y, Z));
        } else {
            return new Times(X, Y, Z);
        }
    }

    /**
     * Ensures: X * Y = Z
     *
     * @param X first variable
     * @param Y a constant
     * @param Z result variable
     */
    public static Constraint times(IntVar X, int Y, IntVar Z) {
        if (Y == 0) {
            return arithm(Z, "=", 0);
        } else if (Y == 1) {
            return arithm(X, "=", Z);
        } else if (Y < 0) {
            return times(VF.minus(X), -Y, Z);
        } else {
            return new Constraint("Times", new PropScale(X, Y, Z));
        }
    }

    //##################################################################################################################
    //GLOBALS ##########################################################################################################
    //##################################################################################################################

    /**
     * Ensures that all variables from VARS take a different value.
     * Uses BC plus a probabilistic AC propagator to get a compromise between BC and AC
     *
     * @param VARS list of variables
     */
    public static Constraint alldifferent(IntVar[] VARS) {
        return alldifferent(VARS, "DEFAULT");
    }

    /**
     * Ensures that all variables from VARS take a different value.
     * The consistency level should be chosen among "BC", "AC" and "DEFAULT".
     *
     * @param VARS        list of variables
     * @param CONSISTENCY consistency level, among {"BC", "AC"}
     *                    <p>
     *                    <b>BC</b>:
     *                    Based on: "A Fast and Simple Algorithm for Bounds Consistency of the AllDifferent Constraint"</br>
     *                    A. Lopez-Ortiz, CG. Quimper, J. Tromp, P.van Beek
     *                    <br/>
     *                    <b>AC</b>:
     *                    Uses Regin algorithm
     *                    Runs in O(m.n) worst case time for the initial propagation and then in O(n+m) on average.
     *                    <p>
     *                    <b>DEFAULT</b>:
     *                    <br/>
     *                    Uses BC plus a probabilistic AC propagator to get a compromise between BC and AC
     */
    public static Constraint alldifferent(IntVar[] VARS, String CONSISTENCY) {
        return new AllDifferent(VARS, CONSISTENCY);
    }

    /**
     * Alldifferent holds on the subset of VARS which satisfies the given CONDITION
     *
     * @param VARS      collection of variables
     * @param CONDITION condition defining which variables should be constrained
     * @param AC        specifies is AC filtering should be established
     */
    public static Constraint alldifferent_conditionnal(IntVar[] VARS, Condition CONDITION, boolean AC) {
        if (AC) {
            return new Constraint("AllDifferent" + CONDITION,
                    new PropCondAllDiffInst(VARS, CONDITION),
                    new PropCondAllDiff_AC(VARS, CONDITION)
            );
        }
        return new Constraint("AllDifferent" + CONDITION, new PropCondAllDiffInst(VARS, CONDITION));
    }

    /**
     * Alldifferent holds on the subset of VARS which satisfies the given CONDITION
     *
     * @param VARS      collection of variables
     * @param CONDITION condition defining which variables should be constrained
     */
    public static Constraint alldifferent_conditionnal(IntVar[] VARS, Condition CONDITION) {
        return alldifferent_conditionnal(VARS, CONDITION, false);
    }

    /**
     * Variables in VARS must either be different or equal to 0
     *
     * @param VARS collection of variables
     */
    public static Constraint alldifferent_except_0(IntVar[] VARS) {
        return alldifferent_conditionnal(VARS, Condition.EXCEPT_0);
    }

    /**
     * NVAR is the number of variables of the collection VARIABLES that take their value in VALUES.
     * <br/><a href="http://www.emn.fr/x-info/sdemasse/gccat/Camong.html">gccat among</a>
     * <br/>
     * Propagator :
     * C. Bessiere, E. Hebrard, B. Hnich, Z. Kiziltan, T. Walsh,
     * Among, common and disjoint Constraints
     * CP-2005
     *
     * @param NVAR   a variable
     * @param VARS   vector of variables
     * @param VALUES set of values
     */
    public static Constraint among(IntVar NVAR, IntVar[] VARS, int[] VALUES) {
        int[] values = new TIntHashSet(VALUES).toArray(); // remove double occurrences
        Arrays.sort(values);                              // sort
        return new Constraint("Among", new PropAmongGAC_GoodImpl(ArrayUtils.append(VARS, new IntVar[]{NVAR}), values));
    }

    /**
     * Let N be the number of distinct values assigned to the variables of the VARS collection.
     * Enforce condition N >= NVALUES to hold.
     * <p>
     * This embeds a light propagator by default.
     * Additional filtering algorithms can be added.
     *
     * @param VARS    collection of variables
     * @param NVALUES limit variable
     * @param AC      additional filtering algorithm, domain filtering algorithm derivated from (Soft)AllDifferent
     */
    public static Constraint atleast_nvalues(IntVar[] VARS, IntVar NVALUES, boolean AC) {
        TIntArrayList vals = getDomainUnion(VARS);
        if (AC) {
            return new Constraint("AtLeastNValues", new PropAtLeastNValues(VARS, vals, NVALUES), new PropAtLeastNValues_AC(VARS, NVALUES));
        } else {
            return new Constraint("AtLeastNValues", new PropAtLeastNValues(VARS, vals, NVALUES));
        }
    }

    /**
     * Let N be the number of distinct values assigned to the variables of the VARS collection.
     * Enforce condition N <= NVALUES to hold.
     * <p>
     * This embeds a light propagator by default.
     * Additional filtering algorithms can be added.
     *
     * @param VARS    collection of variables
     * @param NVALUES limit variable
     * @param STRONG  "AMNV<Gci|MDRk|R13>" Filters the conjunction of AtMostNValue and disequalities
     *                (see Fages and Lap&egrave;gue Artificial Intelligence 2014)
     *                automatically detects disequalities and alldifferent constraints.
     *                Presumably useful when NVALUES must be minimized.
     */
    public static Constraint atmost_nvalues(IntVar[] VARS, IntVar NVALUES, boolean STRONG) {
        TIntArrayList vals = getDomainUnion(VARS);
        if (STRONG) {
            Gci gci = new Gci(VARS, new AutoDiffDetection(VARS));
            R[] rules = new R[]{new R1(), new R3(VARS.length, NVALUES.getSolver())};
            return new Constraint("AtMostNValues", new PropAtMostNValues(VARS, vals, NVALUES),
                    new PropAMNV(VARS, NVALUES, gci, new MDRk(gci), rules));
        } else {
            return new Constraint("AtMostNValues", new PropAtMostNValues(VARS, vals, NVALUES));
        }
    }

    /**
     * Bin Packing formulation:
     * forall b in [0,BIN_LOAD.length-1],
     * BIN_LOAD[b]=sum(ITEM_SIZE[i] | i in [0,ITEM_SIZE.length-1], ITEM_BIN[i] = b+OFFSET
     * forall i in [0,ITEM_SIZE.length-1], ITEM_BIN is in [OFFSET,BIN_LOAD.length-1+OFFSET],
     *
     * @param ITEM_BIN  IntVar representing the bin of each item
     * @param ITEM_SIZE int representing the size of each item
     * @param BIN_LOAD  IntVar representing the load of each bin (i.e. the sum of the size of the items in it)
     * @param OFFSET    0 by default but typically 1 if used within MiniZinc
     *                  (which counts from 1 to n instead of from 0 to n-1)
     */
    public static Constraint[] bin_packing(IntVar[] ITEM_BIN, int[] ITEM_SIZE, IntVar[] BIN_LOAD, int OFFSET) {
        int nbBins = BIN_LOAD.length;
        int nbItems = ITEM_BIN.length;
        Solver s = ITEM_BIN[0].getSolver();
        BoolVar[][] xbi = VF.boolMatrix("xbi", nbBins, nbItems, s);
        int sum = 0;
        for (int is : ITEM_SIZE) {
            sum += is;
        }
        IntVar sumView = VF.fixed(sum, s);
        // constraints
        Constraint[] bpcons = new Constraint[nbItems + nbBins + 1];
        for (int i = 0; i < nbItems; i++) {
            bpcons[i] = ICF.boolean_channeling(ArrayUtils.getColumn(xbi, i), ITEM_BIN[i], OFFSET);
        }
        for (int b = 0; b < nbBins; b++) {
            bpcons[nbItems + b] = ICF.scalar(xbi[b], ITEM_SIZE, BIN_LOAD[b]);
        }
        bpcons[nbItems + nbBins] = ICF.sum(BIN_LOAD, sumView);
        return bpcons;
    }

    /**
     * Maps the boolean assignments variables BVARS with the standard assignment variable VAR.
     * VAR = i <-> BVARS[i-OFFSET] = 1
     *
     * @param BVARS  array of boolean variables
     * @param VAR    observed variable. Should presumably have an enumerated domain
     * @param OFFSET 0 by default but typically 1 if used within MiniZinc
     *               (which counts from 1 to n instead of from 0 to n-1)
     */
    public static Constraint boolean_channeling(BoolVar[] BVARS, IntVar VAR, int OFFSET) {
        if (VAR.hasEnumeratedDomain()) {
            return new Constraint("DomainChanneling", new PropEnumDomainChanneling(BVARS, VAR, OFFSET));
        } else {
            IntVar enumV = VF.enumerated(VAR.getName() + "_enumImage", VAR.getLB(), VAR.getUB(), VAR.getSolver());
            return new Constraint("BoolChanneling",
                    new PropEnumDomainChanneling(BVARS, enumV, OFFSET),
                    new PropEqualX_Y(VAR, enumV)
            );
        }
    }

    /**
     * Ensures that VAR = 2<sup>0</sup>*BIT_1 + 2<sup>1</sup>*BIT_2 + ... 2<sup>n-1</sup>*BIT_n.
     * <br/>
     * BIT_1 is related to the first bit of OCTET (2^0),
     * BIT_2 is related to the first bit of OCTET (2^1), etc.
     * <br/>
     * The upper bound of VAR is given by 2<sup>n</sup>, where n is the size of the array BITS.
     *
     * @param BITS the array of bits
     * @param VAR  the numeric value
     */
    public static Constraint bit_channeling(BoolVar[] BITS, IntVar VAR) {
        return new Constraint("bit_channeling", new PropBitChanneling(VAR, BITS));
    }

    /**
     * Link each value from the domain of VAR to two boolean variable:
     * one reifies the equality to the i^th value of the variable domain,
     * the other reifies the less-or-equality to the i^th value of the variable domain.
     * Contract: EVARS.lenght == LVARS.length == VAR.getUB() - VAR.getLB() + 1
     * Contract: VAR is not a boolean variable
     *
     * @param VAR   an Integer variable
     * @param EVARS array of EQ boolean variables
     * @param LVARS array of LQ boolean variables
     */
    public static Constraint clause_channeling(IntVar VAR, BoolVar[] EVARS, BoolVar[] LVARS) {
        return new Constraint("clause_channeling", new PropClauseChanneling(VAR, EVARS, LVARS));
    }

    /**
     * Creates a circuit constraint which ensures that
     * <p/> the elements of vars define a covering circuit
     * <p/> where VARS[i] = OFFSET+j means that j is the successor of i.
     * <p>
     * Filtering algorithms:
     * <p/> subtour elimination : Caseau & Laburthe (ICLP'97)
     * <p/> allDifferent GAC algorithm: R&eacute;gin (AAAI'94)
     * <p/> dominator-based filtering: Fages & Lorca (CP'11)
     * <p/> Strongly Connected Components based filtering (Cambazard & Bourreau JFPC'06 and Fages and Lorca TechReport'12)
     * <p/> See Fages PhD Thesis (2014) for more information
     *
     * @param VARS   vector of variables which take their value in [OFFSET,OFFSET+|VARS|-1]
     * @param OFFSET 0 by default but typically 1 if used within MiniZinc
     *               (which counts from 1 to n instead of from 0 to n-1)
     * @param CONF   filtering options
     * @return a circuit constraint
     */
    public static Constraint circuit(IntVar[] VARS, int OFFSET, CircuitConf CONF) {
        Propagator[] props;
        if (CONF == CircuitConf.LIGHT) {
            props = new Propagator[]{new PropNoSubtour(VARS, OFFSET)};
        } else {
            props = new Propagator[]{
                    new PropNoSubtour(VARS, OFFSET),
                    new PropCircuit_ArboFiltering(VARS, OFFSET, CONF),
                    new PropCircuit_AntiArboFiltering(VARS, OFFSET, CONF),
                    new PropCircuitSCC(VARS, OFFSET, CONF)
            };
        }
        return new Constraint("Circuit", ArrayUtils.append(alldifferent(VARS, "AC").propagators, props));
    }

    /**
     * Creates a circuit constraint which ensures that
     * <p/> the elements of vars define a covering circuit
     * <p/> where VARS[i] = OFFSET+j means that j is the successor of i.
     * <p>
     * Filtering algorithms:
     * <p/> subtour elimination : Caseau & Laburthe (ICLP'97)
     * <p/> allDifferent GAC algorithm: R&eacute;gin (AAAI'94)
     * <p/> dominator-based filtering: Fages & Lorca (CP'11)
     * <p/> Strongly Connected Components based filtering (Cambazar & Bourreau JFPC'06 and Fages and Lorca TechReport'12)
     *
     * @param VARS   vector of variables which take their value in [OFFSET,OFFSET+|VARS|-1]
     * @param OFFSET 0 by default but typically 1 if used within MiniZinc
     *               (which counts from 1 to n instead of from 0 to n-1)
     * @return a circuit constraint
     */
    public static Constraint circuit(IntVar[] VARS, int OFFSET) {
        return circuit(VARS, OFFSET, CircuitConf.RD);
    }

    /**
     * Ensures that the assignment of a sequence of variables is recognized by CAUTOMATON, a deterministic finite automaton,
     * and that the sum of the costs associated to each assignment is bounded by the cost variable.
     * This version allows to specify different costs according to the automaton state at which the assignment occurs
     * (i.e. the transition starts)
     *
     * @param VARS       sequence of variables
     * @param COST       cost variable
     * @param CAUTOMATON a deterministic finite automaton defining the regular language and the costs
     *                   Can be built with method CostAutomaton.makeSingleResource(...)
     */
    public static Constraint cost_regular(IntVar[] VARS, IntVar COST, ICostAutomaton CAUTOMATON) {
        return new CostRegular(VARS, COST, CAUTOMATON);
    }

    /**
     * Let N be the number of variables of the VARIABLES collection assigned to value VALUE;
     * Enforce condition N = LIMIT to hold.
     * <p>
     *
     * @param VALUE an int
     * @param VARS  a vector of variables
     * @param LIMIT a variable
     */
    public static Constraint count(int VALUE, IntVar[] VARS, IntVar LIMIT) {
        return new Constraint("Count", new PropCount_AC(VARS, VALUE, LIMIT));
    }

    /**
     * Let N be the number of variables of the VARIABLES collection assigned to value VALUE;
     * Enforce condition N = LIMIT to hold.
     * <p>
     *
     * @param VALUE a variable
     * @param VARS  a vector of variables
     * @param LIMIT a variable
     */
    public static Constraint count(IntVar VALUE, IntVar[] VARS, IntVar LIMIT) {
        if (VALUE.isInstantiated()) {
            return count(VALUE.getValue(), VARS, LIMIT);
        } else if (VALUE.hasEnumeratedDomain()) {
            return new Constraint("Count", new PropCountVar(VARS, VALUE, LIMIT));
        } else {
            IntVar EVALUE = VF.enumerated(StringUtils.randomName(), VALUE.getLB(), VALUE.getUB(), VALUE.getSolver());
            return new Constraint("Count",
                    new PropEqualX_Y(EVALUE, VALUE),
                    new PropCountVar(VARS, EVALUE, LIMIT));
        }
    }

    /**
     * Cumulative constraint: Enforces that at each point in time,
     * the cumulated height of the set of tasks that overlap that point
     * does not exceed a given limit.
     *
     * @param TASKS    TASK objects containing start, duration and end variables
     * @param HEIGHTS  integer variables representing the resource consumption of each task
     * @param CAPACITY integer variable representing the resource capacity
     * @return a cumulative constraint
     */
    public static Constraint cumulative(Task[] TASKS, IntVar[] HEIGHTS, IntVar CAPACITY) {
        return cumulative(TASKS, HEIGHTS, CAPACITY, TASKS.length > 500);
    }

    /**
     * Cumulative constraint: Enforces that at each point in time,
     * the cumulated height of the set of tasks that overlap that point
     * does not exceed a given limit.
     *
     * @param TASKS       TASK objects containing start, duration and end variables
     * @param HEIGHTS     integer variables representing the resource consumption of each task
     * @param CAPACITY    integer variable representing the resource capacity
     * @param INCREMENTAL specifies if an incremental propagation should be applied
     * @return a cumulative constraint
     */
    public static Constraint cumulative(Task[] TASKS, IntVar[] HEIGHTS, IntVar CAPACITY, boolean INCREMENTAL) {
        // Cumulative.Filter.HEIGHTS is useless if all HEIGHTS are already instantiated
        boolean addHeights = false;
        int nbUseFull = 0;
        for (int h = 0; h < HEIGHTS.length; h++) {
            if (!HEIGHTS[h].isInstantiated()) {
                addHeights = true;
            }
            if (!(HEIGHTS[h].isInstantiatedTo(0) || TASKS[h].getDuration().isInstantiatedTo(0))) {
                nbUseFull++;
            }
        }
        // remove tasks which have no impact on resource
        if (nbUseFull < TASKS.length) {
            if (nbUseFull == 0) return arithm(CAPACITY, ">=", 0);
            Task[] T2 = new Task[nbUseFull];
            IntVar[] H2 = new IntVar[nbUseFull];
            int idx = 0;
            for (int h = 0; h < HEIGHTS.length; h++) {
                if (!(HEIGHTS[h].isInstantiatedTo(0) || TASKS[h].getDuration().isInstantiatedTo(0))) {
                    T2[idx] = TASKS[h];
                    H2[idx] = HEIGHTS[h];
                    idx++;
                }
            }
            TASKS = T2;
            HEIGHTS = H2;
        }
        Cumulative.Filter[] filters = new Cumulative.Filter[]{Cumulative.Filter.TIME, Cumulative.Filter.NRJ};
        if (addHeights) {
            filters = ArrayUtils.append(filters, new Cumulative.Filter[]{Cumulative.Filter.HEIGHTS});
        }
        return new Cumulative(TASKS, HEIGHTS, CAPACITY, INCREMENTAL, filters);
    }

    /**
     * Constrains each rectangle<sub>i</sub>, given by their origins X<sub>i</sub>,Y<sub>i</sub>
     * and sizes WIDTH<sub>i</sub>,HEIGHT<sub>i</sub>, to be non-overlapping.
     *
     * @param X         collection of coordinates in first dimension
     * @param Y         collection of coordinates in second dimension
     * @param WIDTH     collection of width (each duration should be > 0)
     * @param HEIGHT    collection of height (each height should be >= 0)
     * @param USE_CUMUL indicates whether or not redundant cumulative constraints should be put on each dimension (advised)
     * @return a non-overlapping constraint
     */
    public static Constraint[] diffn(IntVar[] X, IntVar[] Y, IntVar[] WIDTH, IntVar[] HEIGHT, boolean USE_CUMUL) {
        Solver solver = X[0].getSolver();
        Constraint diffNCons = new Constraint(
                "DiffN",
                new PropDiffN(X, Y, WIDTH, HEIGHT, false),
                new PropDiffN(X, Y, WIDTH, HEIGHT, false)
        );
        if (USE_CUMUL) {
            IntVar[] EX = new IntVar[X.length];
            IntVar[] EY = new IntVar[X.length];
            Task[] TX = new Task[X.length];
            Task[] TY = new Task[X.length];
            int minx = Integer.MAX_VALUE / 2;
            int maxx = Integer.MIN_VALUE / 2;
            int miny = Integer.MAX_VALUE / 2;
            int maxy = Integer.MIN_VALUE / 2;
            for (int i = 0; i < X.length; i++) {
                EX[i] = VF.bounded(StringUtils.randomName("diffn"), X[i].getLB() + WIDTH[i].getLB(), X[i].getUB() + WIDTH[i].getUB(), solver);
                EY[i] = VF.bounded(StringUtils.randomName("diffn"), Y[i].getLB() + HEIGHT[i].getLB(), Y[i].getUB() + HEIGHT[i].getUB(), solver);
                TX[i] = VF.task(X[i], WIDTH[i], EX[i]);
                TY[i] = VF.task(Y[i], HEIGHT[i], EY[i]);
                minx = Math.min(minx, X[i].getLB());
                miny = Math.min(miny, Y[i].getLB());
                maxx = Math.max(maxx, X[i].getUB() + WIDTH[i].getUB());
                maxy = Math.max(maxy, Y[i].getUB() + HEIGHT[i].getUB());
            }
            IntVar maxX = VF.bounded(StringUtils.randomName("diffn"), minx, maxx, solver);
            IntVar minX = VF.bounded(StringUtils.randomName("diffn"), minx, maxx, solver);
            IntVar diffX = VF.bounded(StringUtils.randomName("diffn"), 0, maxx - minx, solver);
            IntVar maxY = VF.bounded(StringUtils.randomName("diffn"), miny, maxy, solver);
            IntVar minY = VF.bounded(StringUtils.randomName("diffn"), miny, maxy, solver);
            IntVar diffY = VF.bounded(StringUtils.randomName("diffn"), 0, maxy - miny, solver);
            return new Constraint[]{
                    diffNCons,
                    minimum(minX, X), maximum(maxX, EX), scalar(new IntVar[]{maxX, minX}, new int[]{1, -1}, diffX),
                    cumulative(TX, HEIGHT, diffY, true),
                    minimum(minY, Y), maximum(maxY, EY), scalar(new IntVar[]{maxY, minY}, new int[]{1, -1}, diffY),
                    cumulative(TY, WIDTH, diffX, true)
            };
        }
        return new Constraint[]{diffNCons};
    }

    /**
     * Build an ELEMENT constraint: VALUE = TABLE[INDEX-OFFSET] where TABLE is an array of variables.
     *
     * @param VALUE  value variable
     * @param TABLE  array of variables
     * @param INDEX  index variable in range [OFFSET,OFFSET+|TABLE|-1]
     * @param OFFSET int offset, generally 0
     */
    public static Constraint element(IntVar VALUE, IntVar[] TABLE, IntVar INDEX, int OFFSET) {
        // uses two propagator to perform a fix point
        return new Constraint(
                "Element",
                new PropElementV_fast(VALUE, TABLE, INDEX, OFFSET, true),
                new PropElementV_fast(VALUE, TABLE, INDEX, OFFSET, true));
    }

    /**
     * Global Cardinality constraint (GCC):
     * Each value VALUES[i] should be taken by exactly OCCURRENCES[i] variables of VARS.
     * <br/>
     * This constraint does not ensure any well-defined level of consistency, yet.
     *
     * @param VARS        collection of variables
     * @param VALUES      collection of constrained values
     * @param OCCURRENCES collection of cardinality variables
     * @param CLOSED      restricts domains of VARS to VALUES if set to true
     */
    public static Constraint global_cardinality(IntVar[] VARS, int[] VALUES, IntVar[] OCCURRENCES, boolean CLOSED) {
        assert VALUES.length == OCCURRENCES.length;
        if (!CLOSED) {
            return new GlobalCardinality(VARS, VALUES, OCCURRENCES);
        } else {
            TIntArrayList toAdd = new TIntArrayList();
            TIntSet givenValues = new TIntHashSet();
            for (int i : VALUES) {
                assert !givenValues.contains(i);
                givenValues.add(i);
            }
            for (IntVar var : VARS) {
                int ub = var.getUB();
                for (int k = var.getLB(); k <= ub; k = var.nextValue(k)) {
                    if (!givenValues.contains(k)) {
                        if (!toAdd.contains(k)) {
                            toAdd.add(k);
                        }
                    }
                }
            }
            if (toAdd.size() > 0) {
                int n2 = VALUES.length + toAdd.size();
                int[] values = new int[n2];
                IntVar[] cards = new IntVar[n2];
                System.arraycopy(VALUES, 0, values, 0, VALUES.length);
                System.arraycopy(OCCURRENCES, 0, cards, 0, VALUES.length);
                for (int i = VALUES.length; i < n2; i++) {
                    values[i] = toAdd.get(i - VALUES.length);
                    cards[i] = VariableFactory.fixed(0, VARS[0].getSolver());
                }
                return new GlobalCardinality(VARS, values, cards);
            } else {
                return new GlobalCardinality(VARS, VALUES, OCCURRENCES);
            }
        }
    }

    /**
     * Make an inverse channeling between VARS1 and VARS2:
     * VARS1[i-OFFSET2] = j <=> VARS2[j-OFFSET1] = i
     * Performs AC if domains are enumerated.
     * If not, then it works on bounds without guaranteeing BC
     * (enumerated domains are strongly recommended)
     * <p>
     * Beware you should have |VARS1| = |VARS2|
     *
     * @param VARS1   vector of variables which take their value in [OFFSET1,OFFSET1+|VARS2|-1]
     * @param VARS2   vector of variables which take their value in [OFFSET2,OFFSET2+|VARS1|-1]
     * @param OFFSET1 lowest value in VARS1 (most often 0)
     * @param OFFSET2 lowest value in VARS2 (most often 0)
     */
    public static Constraint inverse_channeling(IntVar[] VARS1, IntVar[] VARS2, int OFFSET1, int OFFSET2) {
        if (VARS1.length != VARS2.length)
            throw new UnsupportedOperationException(Arrays.toString(VARS1) + " and " + Arrays.toString(VARS2) + " should have same size");
        boolean allEnum = true;
        for (int i = 0; i < VARS1.length && allEnum; i++) {
            if (!(VARS1[i].hasEnumeratedDomain() && VARS2[i].hasEnumeratedDomain())) {
                allEnum = false;
            }
        }
        Propagator ip = allEnum ? new PropInverseChannelAC(VARS1, VARS2, OFFSET1, OFFSET2)
                : new PropInverseChannelBC(VARS1, VARS2, OFFSET1, OFFSET2);
        return new Constraint("InverseChanneling", ArrayUtils.append(
                alldifferent(VARS1, "").getPropagators(),
                alldifferent(VARS2, "").getPropagators(),
                new Propagator[]{ip}
        ));
    }

    /**
     * Ensure that if there exists <code>j</code> such that X[j] = T, then, there must exist <code>i</code> < <code>j</code> such that
     * X[i] = S.
     *
     * @param X an array of variables
     * @param S a value
     * @param T another value
     */
    public static Constraint int_value_precede_chain(IntVar[] X, int S, int T) {
        return new Constraint("int_value_precede", new PropIntValuePrecedeChain(X, S, T));
    }

    /**
     * Ensure that, for each pair of V[k] and V[l] of values in V, such that k < l,
     * if there exists <code>j</code> such that X[j] = V[l], then, there must exist <code>i</code> < <code>j</code> such that
     * X[i] = V[k].
     *
     * @param X array of variables
     * @param V array of (distinct) values
     */
    public static Constraint int_value_precede_chain(IntVar[] X, int[] V) {
        if (V.length > 1) {
            TIntHashSet values = new TIntHashSet();
            PropIntValuePrecedeChain[] ps = new PropIntValuePrecedeChain[V.length - 1];
            values.add(V[0]);
            for (int i = 1; i < V.length; i++) {
                if (values.contains(V[i])) {
                    throw new SolverException("\"int_value_precede\" requires V to be made of distinct values");
                }
                values.add(V[i]);
                ps[i - 1] = new PropIntValuePrecedeChain(X, V[i - 1], V[i]);
            }
            return new Constraint("int_value_precede", ps);
        } else {
            return X[0].getSolver().TRUE();
        }
    }

    /**
     * Ensures that :
     * <br/>- OCCURRENCES[i] * WEIGHT[i] = SUM_WEIGHT
     * <br/>- OCCURRENCES[i] * ENERGY[i] = SUM_ENERGY
     * <br/>and maximizing the value of SUM_ENERGY.
     * <p>
     * <p>
     * A knapsack constraint
     * <a href="http://en.wikipedia.org/wiki/Knapsack_problem">wikipedia</a>:<br/>
     * "Given a set of items, each with a weight and an energy value,
     * determine the count of each item to include in a collection so that
     * the total weight is less than or equal to a given limit and the total value is as large as possible.
     * It derives its name from the problem faced by someone who is constrained by a fixed-size knapsack
     * and must fill it with the most useful items."
     * The limit over SUM_WEIGHT has to be specified either in its domain or with an additional constraint:
     * <pre>
     *     solver.post(ICF.arithm(SUM_WEIGHT, "<=", limit);
     * </pre>
     *
     * @param OCCURRENCES  number of occurrences of an item
     * @param SUM_WEIGHT capacity of the knapsack
     * @param SUM_ENERGY variable to maximize
     * @param WEIGHT       weight of each item
     * @param ENERGY       energy of each item
     */
    public static Constraint knapsack(IntVar[] OCCURRENCES, IntVar SUM_WEIGHT, IntVar SUM_ENERGY,
                                      int[] WEIGHT, int[] ENERGY) {
        return new Constraint("Knapsack", ArrayUtils.append(
                scalar(OCCURRENCES, WEIGHT, "=",SUM_WEIGHT).propagators,
                scalar(OCCURRENCES, ENERGY, "=", SUM_ENERGY).propagators,
                new Propagator[]{new PropKnapsack(OCCURRENCES, SUM_WEIGHT, SUM_ENERGY, WEIGHT, ENERGY)}
        ));
    }

    /**
     * Creates a keysorting constraint which ensures that the variables of SORTEDVARS correspond to the variables
     * of VARS according to a permutation stored in PERMVARS (optional, can be null).
     * The variables of SORTEDVARS are also sorted in increasing order wrt to K-size tuples.
     * The sort is stable, that is, ties are broken using the position of the tuple in VARS.
     * <p>
     * <p>
     * For example:<br/>
     * - VARS= (<4,2,2>,<2,3,1>,<4,2,1><1,3,0>)<br/>
     * - SORTEDVARS= (<1,3,0>,<2,3,1>,<4,2,2>,<4,2,1>)<br/>
     * - PERMVARS= (2,1,3,0)<br/>
     * - K = 2<br/>
     *
     * @param VARS       a tuple of array of variables
     * @param PERMVARS   array of permutation variables, domains should be [1,VARS.length]  -- Can be null
     * @param SORTEDVARS a tuple of array of variables sorted in increasing order
     * @param K          key perfixes size (0 &le; k &le; m, where m is the size of the array of variable)
     * @return a keysorting constraint
     */
    public static Constraint keysorting(IntVar[][] VARS, IntVar[] PERMVARS, IntVar[][] SORTEDVARS, int K) {
        if (PERMVARS == null) {
            int n = VARS.length;
            PERMVARS = new IntVar[n];
            for (int p = 0; p < n; p++) {
                PERMVARS[p] = VF.bounded("p_" + (p + 1), 1, n, VARS[0][0].getSolver());
            }
        }
        return new Constraint("keysorting", new PropKeysorting(VARS, SORTEDVARS, PERMVARS, K));
    }

    /**
     * For each pair of consecutive vectors VARS<sub>i</sub> and VARS<sub>i+1</sub> of the VARS collection
     * VARS<sub>i</sub> is lexicographically strictly less than than VARS<sub>i+1</sub>
     *
     * @param VARS collection of vectors of variables
     */
    public static Constraint lex_chain_less(IntVar[]... VARS) {
        return new Constraint("LexChain(<) ", new PropLexChain(VARS, true));
    }

    /**
     * For each pair of consecutive vectors VARS<sub>i</sub> and VARS<sub>i+1</sub> of the VARS collection
     * VARS<sub>i</sub> is lexicographically less or equal than than VARS<sub>i+1</sub>
     *
     * @param VARS collection of vectors of variables
     */
    public static Constraint lex_chain_less_eq(IntVar[]... VARS) {
        return new Constraint("LexChain(<=)", new PropLexChain(VARS, false));
    }

    /**
     * Ensures that VARS1 is lexicographically strictly less than VARS2.
     *
     * @param VARS1 vector of variables
     * @param VARS2 vector of variables
     */
    public static Constraint lex_less(IntVar[] VARS1, IntVar[] VARS2) {
        return new Constraint("Lex(<)", new PropLex(VARS1, VARS2, true));
    }

    /**
     * Ensures that VARS1 is lexicographically less or equal than VARS2.
     *
     * @param VARS1 vector of variables
     * @param VARS2 vector of variables
     */
    public static Constraint lex_less_eq(IntVar[] VARS1, IntVar[] VARS2) {
        return new Constraint("Lex(<=)", new PropLex(VARS1, VARS2, false));
    }

    /**
     * MAX is the maximum value of the collection of domain variables VARS
     *
     * @param MAX  a variable
     * @param VARS a vector of variables
     */
    public static Constraint maximum(IntVar MAX, IntVar[] VARS) {
        return new Constraint("Max", new PropMax(VARS, MAX));
    }

    /**
     * MAX is the maximum value of the collection of boolean variables VARS
     *
     * @param MAX  a boolean variable
     * @param VARS a vector of boolean variables
     */
    public static Constraint maximum(BoolVar MAX, BoolVar[] VARS) {
        return new Constraint("MinOverBools", new PropBoolMax(VARS, MAX));
    }

    /**
     * Create a constraint where solutions (tuples) are encoded by a multi-valued decision diagram.
     * The order of the variables in VARS is important and must refer to the MDD.
     *
     * @param VARS the array of variables
     * @param MDD  the multi-valued decision diagram encoding solutions
     */
    public static Constraint mddc(IntVar[] VARS, MultivaluedDecisionDiagram MDD) {
        return new Constraint("mddc", new PropLargeMDDC(MDD, VARS));
    }

    /**
     * MIN is the minimum value of the collection of domain variables VARS
     *
     * @param MIN  a variable
     * @param VARS a vector of variables
     */
    public static Constraint minimum(IntVar MIN, IntVar[] VARS) {
        return new Constraint("Min", new PropMin(VARS, MIN));
    }

    /**
     * MIN is the minimum value of the collection of boolean variables VARS
     *
     * @param MIN  a boolean variable
     * @param VARS a vector of boolean variables
     */
    public static Constraint minimum(BoolVar MIN, BoolVar[] VARS) {
        return new Constraint("MinOverBools", new PropBoolMin(VARS, MIN));
    }

    /**
     * Ensures that the assignment of a sequence of VARS is recognized by CAUTOMATON, a deterministic finite automaton,
     * and that the sum of the cost vector COSTS associated to each assignment is bounded by the variable vector CVARS.
     * This version allows to specify different costs according to the automaton state at which the assignment occurs
     * (i.e. the transition starts)
     *
     * @param VARS       sequence of variables
     * @param CVARS      cost variables
     * @param CAUTOMATON a deterministic finite automaton defining the regular language and the costs
     *                   Can be built from method CostAutomaton.makeMultiResources(...)
     */
    public static Constraint multicost_regular(IntVar[] VARS, IntVar[] CVARS, ICostAutomaton CAUTOMATON) {
        return new Constraint("MultiCostRegular", new PropMultiCostRegular(VARS, CVARS, CAUTOMATON));
    }

    /**
     * Let N be the number of distinct values assigned to the variables of the VARS collection.
     * Enforce condition N = NVALUES to hold.
     * <p>
     * This embeds a light propagator by default.
     * Additional filtering algorithms can be added.
     * <p>
     * see atleast_nvalue and atmost_nvalue
     *
     * @param VARS    collection of variables
     * @param NVALUES limit variable
     * @return the conjunction of atleast_nvalue and atmost_nvalue
     */
    public static Constraint[] nvalues(IntVar[] VARS, IntVar NVALUES) {
        return new Constraint[]{atleast_nvalues(VARS, NVALUES, false), atmost_nvalues(VARS, NVALUES, true)};
    }

    /**
     * Creates a path constraint which ensures that
     * <p/> the elements of VARS define a covering path from START to END
     * <p/> where VARS[i] = OFFSET+j means that j is the successor of i.
     * <p/> Moreover, VARS[END-OFFSET] = |VARS|+OFFSET
     * <p/> Requires : |VARS|>0
     * <p>
     * Filtering algorithms: see circuit constraint
     *
     * @param VARS   vector of variables which take their value in [OFFSET,OFFSET+|VARS|]
     * @param START  variable indicating the index of the first variable in the path
     * @param END    variable indicating the index of the last variable in the path
     * @param OFFSET 0 by default but typically 1 if used within MiniZinc
     *               (which counts from 1 to n instead of from 0 to n-1)
     * @return a path constraint
     */
    public static Constraint[] path(IntVar[] VARS, IntVar START, IntVar END, int OFFSET) {
        assert START != null && END != null && VARS != null;
        switch (VARS.length) {
            case 0:
                throw new UnsupportedOperationException("|VARS| Should be strictly greater than 0");
            case 1:
                return new Constraint[]{
                        arithm(START, "=", OFFSET),
                        arithm(END, "=", OFFSET),
                        arithm(VARS[0], "=", 1 + OFFSET),
                };
            default:
                if (START == END) {
                    return new Constraint[]{START.getSolver().FALSE()};
                } else {
                    return new Constraint[]{
                            arithm(START, "!=", END),
                            circuit(ArrayUtils.append(VARS, new IntVar[]{START}), OFFSET),
                            element(VF.fixed(VARS.length + OFFSET, END.getSolver()), VARS, END, OFFSET)
                    };
                }
        }
    }

    /**
     * Enforces the sequence of VARS to be a word
     * recognized by the deterministic finite automaton AUTOMATON.
     * For example regexp = "(1|2)(3*)(4|5)";
     * The same dfa can be used for different propagators.
     *
     * @param VARS      sequence of variables
     * @param AUTOMATON a deterministic finite automaton defining the regular language
     */
    public static Constraint regular(IntVar[] VARS, IAutomaton AUTOMATON) {
        return new Constraint("Regular", new PropRegular(VARS, AUTOMATON));
    }

    /**
     * Enforces that &#8721;<sub>i in |VARS|</sub>COEFFS<sub>i</sub> * VARS<sub>i</sub> = SCALAR.
     *
     * @param VARS   a vector of variables
     * @param COEFFS a vector of int
     * @param SCALAR a variable
     */
    public static Constraint scalar(IntVar[] VARS, int[] COEFFS, IntVar SCALAR) {
        return scalar(VARS, COEFFS, "=", SCALAR);
    }

    /**
     * A scalar constraint which ensures that Sum(VARS[i]*COEFFS[i]) OPERATOR SCALAR
     *
     * @param VARS     a collection of IntVar
     * @param COEFFS   a collection of int, for which |VARS|=|COEFFS|
     * @param OPERATOR an operator in {"=", "!=", ">","<",">=","<="}
     * @param SCALAR   an IntVar
     * @return a scalar constraint
     */
    public static Constraint scalar(IntVar[] VARS, int[] COEFFS, String OPERATOR, IntVar SCALAR) {
        return IntLinCombFactory.reduce(VARS, COEFFS, Operator.get(OPERATOR), SCALAR, SCALAR.getSolver());
    }

    /**
     * Creates a sort constraint which ensures that the variables of SORTEDVARS correspond to the variables
     * of VARS according to a permutation. The variables of SORTEDVARS are also sorted in increasing order.
     * <p>
     * <p>
     * For example:<br/>
     * - X= (4,2,1,3)<br/>
     * - Y= (1,2,3,4)
     *
     * @param VARS       an array of variables
     * @param SORTEDVARS an array of variables sorted in increasing order
     * @return a sort constraint
     */
    public static Constraint sort(IntVar[] VARS, IntVar[] SORTEDVARS) {
//        return new Constraint("Sort", new PropSort(VARS, SORTEDVARS));
        IntVar[][] X = new IntVar[VARS.length][1];
        IntVar[][] Y = new IntVar[SORTEDVARS.length][1];
        for (int i = 0; i < VARS.length; i++) {
            X[i][0] = VARS[i];
            Y[i][0] = SORTEDVARS[i];
        }
        return keysorting(X, null, Y, 1);
    }


    /**
     * Creates a subcircuit constraint which ensures that
     * <p/> the elements of vars define a single circuit of subcircuitSize nodes where
     * <p/> VARS[i] = OFFSET+j means that j is the successor of i.
     * <p/> and VARS[i] = OFFSET+i means that i is not part of the circuit
     * <p/> the constraint ensures that |{VARS[i] =/= OFFSET+i}| = SUBCIRCUIT_SIZE
     * <p>
     * <p/> Filtering algorithms:
     * <p/> subtour elimination : Caseau & Laburthe (ICLP'97)
     * <p/> allDifferent GAC algorithm: R&eacute;gin (AAAI'94)
     * <p/> dominator-based filtering: Fages & Lorca (CP'11)
     * <p/> SCC-based filtering
     *
     * @param VARS            a vector of variables
     * @param OFFSET          0 by default but 1 if used within MiniZinc
     *                        (which counts from 1 to n instead of from 0 to n-1)
     * @param SUBCIRCUIT_SIZE expected number of nodes in the circuit
     * @return a subcircuit constraint
     */
    public static Constraint subcircuit(IntVar[] VARS, int OFFSET, IntVar SUBCIRCUIT_SIZE) {
        int n = VARS.length;
        Solver solver = VARS[0].getSolver();
        IntVar nbLoops = VariableFactory.bounded("nLoops", 0, n, solver);
        return new Constraint("SubCircuit", ArrayUtils.append(
                alldifferent(VARS).getPropagators(),
                ArrayUtils.toArray(
                        new PropEqualXY_C(new IntVar[]{nbLoops, SUBCIRCUIT_SIZE}, n),
                        new PropKLoops(VARS, OFFSET, nbLoops),
                        new PropSubcircuit(VARS, OFFSET, SUBCIRCUIT_SIZE),
                        new PropSubcircuit_AntiArboFiltering(VARS, OFFSET),
                        new PropSubCircuitSCC(VARS, OFFSET)
                )
        ));
    }

    /**
     * Creates a subpath constraint which ensures that
     * <p/> the elements of VARS define a path of SIZE vertices, leading from START to END
     * <p/> where VARS[i] = OFFSET+j means that j is the successor of i.
     * <p/> where VARS[i] = OFFSET+i means that vertex i is excluded from the path.
     * <p/> Moreover, VARS[END-OFFSET] = |VARS|+OFFSET
     * <p/> Requires : |VARS|>0
     * <p>
     * Filtering algorithms: see subcircuit constraint
     *
     * @param VARS   vector of variables which take their value in [OFFSET,OFFSET+|VARS|]
     * @param START  variable indicating the index of the first variable in the path
     * @param END    variable indicating the index of the last variable in the path
     * @param OFFSET 0 by default but typically 1 if used within MiniZinc
     *               (which counts from 1 to n instead of from 0 to n-1)
     * @param SIZE   variable indicating the number of variables to belong to the path
     * @return a subpath constraint
     */
    public static Constraint[] subpath(IntVar[] VARS, IntVar START, IntVar END, int OFFSET, IntVar SIZE) {
        assert START != null && END != null && VARS != null;
        switch (VARS.length) {
            case 0:
                throw new UnsupportedOperationException("|VARS| Should be strictly greater than 0");
            case 1:
                return new Constraint[]{
                        arithm(START, "=", OFFSET),
                        arithm(END, "=", OFFSET),
                        arithm(VARS[0], "=", 1 + OFFSET),
                        arithm(SIZE, "=", 1)
                };
            default:
                return new Constraint[]{
                        arithm(START, "<", VARS.length + OFFSET),
                        subcircuit(ArrayUtils.append(VARS, new IntVar[]{START}), OFFSET, VF.offset(SIZE, 1)),
                        element(VF.fixed(VARS.length + OFFSET, END.getSolver()), VARS, END, OFFSET)
                };
        }
    }

    /**
     * Enforces that &#8721;<sub>i in |VARS|</sub>VARS<sub>i</sub> = SUM.
     *
     * @param VARS a vector of variables
     * @param SUM  a variable
     */
    public static Constraint sum(IntVar[] VARS, IntVar SUM) {
        return sum(VARS, "=", SUM);
    }

    /**
     * Enforces that &#8721;<sub>i in |VARS|</sub>VARS<sub>i</sub> OPERATOR SUM.
     *
     * @param VARS     a collection of IntVar
     * @param OPERATOR operator in {"=", "!=", ">","<",">=","<="}
     * @param SUM      an IntVar
     * @return a sum constraint
     */
    public static Constraint sum(IntVar[] VARS, String OPERATOR, IntVar SUM) {
        return IntLinCombFactory.reduce(VARS, Operator.get(OPERATOR), SUM, SUM.getSolver());
    }

    /**
     * Enforces that &#8721;<sub>i in |VARS|</sub>VARS<sub>i</sub> = SUM.
     * This constraint is much faster than the one over integer variables
     *
     * @param VARS a vector of boolean variables
     * @param SUM  a variable
     */
    public static Constraint sum(BoolVar[] VARS, IntVar SUM) {
        return IntLinCombFactory.reduce(VARS, Operator.EQ, SUM, SUM.getSolver());
    }

    /**
     * Enforces that &#8721;<sub>i in |VARS|</sub>VARS<sub>i</sub> OPERATOR SUM.
     * This constraint is much faster than the one over integer variables
     *
     * @param VARS a vector of boolean variables
     * @param SUM  a variable
     */
    public static Constraint sum(BoolVar[] VARS, String OPERATOR, IntVar SUM) {
        if (OPERATOR.equals("=")) {
            return sum(VARS, SUM);
        }
        int lb = 0;
        int ub = 0;
        for (BoolVar v : VARS) {
            lb += v.getLB();
            ub += v.getUB();
        }
        IntVar p = VF.bounded(StringUtils.randomName(), lb, ub, SUM.getSolver());
        SUM.getSolver().post(sum(VARS, p));
        return arithm(p, OPERATOR, SUM);
    }

    /**
     * Create a table constraint specifying that the sequence of variables VARS must belong to the list of tuples
     * (or must NOT belong in case of infeasible tuples)
     *
     * Default configuration with GACSTR+ algorithm for feasible tuples and GAC3rm otherwise
     *
     * @param VARS      variables forming the tuples
     * @param TUPLES    the relation between the variables (list of allowed/forbidden tuples)
     */
    public static Constraint table(IntVar[] VARS, Tuples TUPLES) {
        return table(VARS,TUPLES,TUPLES.isFeasible()?"GACSTR+":"GAC3rm");
    }

    /**
     * Create a table constraint, with the specified algorithm defined ALGORITHM
     * <p>
     * - <b>GAC2001</b>: Arc Consistency version 2001 for tuples,
     * <br/>
     * - <b>GAC2001+</b>: Arc Consistency version 2001 for allowed tuples,
     * <br/>
     * - <b>GAC3rm</b>: Arc Consistency version AC3 rm for tuples,
     * <br/>
     * - <b>GAC3rm+</b> (default): Arc Consistency version 3rm for allowed tuples,
     * <br/>
     * - <b>GACSTR+</b>: Arc Consistency version STR for allowed tuples,
     * <br/>
     * - <b>STR2+</b>: Arc Consistency version STR2 for allowed tuples,
     * <br/>
     * - <b>FC</b>: Forward Checking.
     * <br/>
     * - <b>MDD</b>: uses a multi-valued decision diagram (see mddc constraint),
     *
     * @param VARS      variables forming the tuples
     * @param TUPLES    the relation between the variables (list of allowed/forbidden tuples)
     * @param ALGORITHM to choose among {"GAC3rm", "GAC2001", "GACSTR", "GAC2001+", "GAC3rm+", "FC", "STR2+"}
     */
    public static Constraint table(IntVar[] VARS, Tuples TUPLES, String ALGORITHM) {
        if (VARS.length == 2) {
            table(VARS[0], VARS[1], TUPLES);
        }
        if(ALGORITHM.contains("+") && !TUPLES.isFeasible()){
            throw new SolverException(ALGORITHM+" table algorithm cannot be used with forbidden tuples.");
        }
        Propagator p;
        switch (ALGORITHM) {
            case "MDD": p = new PropLargeMDDC(new MultivaluedDecisionDiagram(VARS, TUPLES), VARS);
                break;
            case "FC": p = new PropLargeFC(VARS, TUPLES);
                break;
            case "GAC3rm": p = new PropLargeGAC3rm(VARS, TUPLES);
                break;
            case "GAC2001": p = new PropLargeGAC2001(VARS, TUPLES);
                break;
            case "GACSTR+": p = new PropLargeGACSTRPos(VARS, TUPLES);
                break;
            case "GAC2001+": p = new PropLargeGAC2001Positive(VARS, TUPLES);
                break;
            case "GAC3rm+": p = new PropLargeGAC3rmPositive(VARS, TUPLES);
                break;
            case "STR2+": p = new PropTableStr2(VARS, TUPLES.toMatrix());
                break;
            default: throw new SolverException("Table algorithm "+ALGORITHM+" is unkown");
        }
        return new Constraint("Table(" + ALGORITHM + ")", p);
    }

    /**
     * Partition SUCCS variables into NBTREES (anti) arborescences
     * <p/> SUCCS[i] = OFFSET+j means that j is the successor of i.
     * <p/> and SUCCS[i] = OFFSET+i means that i is a root
     * <p>
     * <p/> dominator-based filtering: Fages & Lorca (CP'11)
     * <p/> However, the filtering over NBTREES is quite light here
     *
     * @param SUCCS   successors variables
     * @param NBTREES number of arborescences (=number of loops)
     * @param OFFSET  0 by default but 1 if used within MiniZinc
     *                (which counts from 1 to n instead of from 0 to n-1)
     * @return a tree constraint
     */
    public static Constraint tree(IntVar[] SUCCS, IntVar NBTREES, int OFFSET) {
        return new Constraint("tree",
                new PropAntiArborescences(SUCCS, OFFSET, false),
                new PropKLoops(SUCCS, OFFSET, NBTREES)
        );
    }

    /**
     * A constraint for the Traveling Salesman Problem :
     * Enforces SUCCS to form a hamiltonian circuit of value COST
     *
     * @param SUCCS       successors variables
     * @param COST        cost of the cycle
     * @param COST_MATRIX symmetric cost matrix
     * @return a CP model for the TSP
     */
    public static Constraint[] tsp(IntVar[] SUCCS, IntVar COST, int[][] COST_MATRIX) {
        int n = SUCCS.length;
        assert n > 1;
        assert n == COST_MATRIX.length && n == COST_MATRIX[0].length;
        IntVar[] costOf = new IntVar[n];
        for (int i = 0; i < n; i++) {
            costOf[i] = VF.enumerated("costOf(" + i + ")", COST_MATRIX[i], COST.getSolver());
        }
        Constraint[] model = new Constraint[n + 2];
        for (int i = 0; i < n; i++) {
            model[i] = element(costOf[i], COST_MATRIX[i], SUCCS[i]);
        }
        model[n] = sum(costOf, COST);
        model[n + 1] = circuit(SUCCS, 0);
        return model;
    }

    public static TIntArrayList getDomainUnion(IntVar[] vars) {
        TIntArrayList values = new TIntArrayList();
        for (IntVar v : vars) {
            int ub = v.getUB();
            for (int i = v.getLB(); i <= ub; i = v.nextValue(i)) {
                if (!values.contains(i)) {
                    values.add(i);
                }
            }
        }
        return values;
    }

    // ###################

    /**
     * Check whether the intension constraint to extension constraint substitution is enabled and can be achieved
     *
     * @param VARS list of variables involved
     * @return a boolean
     */
    public static boolean tupleIt(IntVar... VARS) {
        Settings settings = VARS[0].getSolver().getSettings();
        if (!settings.enableTableSubstitution()) {
            return false;
        }
        long doms = 1;
        for (int i = 0; i < VARS.length && doms < settings.getMaxTupleSizeForSubstitution(); i++) {
            if (!VARS[i].hasEnumeratedDomain()) {
                return false;
            }
            doms *= VARS[i].getDomainSize();
        }
        return (doms < settings.getMaxTupleSizeForSubstitution());
    }
}
