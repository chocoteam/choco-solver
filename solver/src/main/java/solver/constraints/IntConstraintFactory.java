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

import choco.kernel.common.util.tools.StringUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import solver.Solver;
import solver.constraints.binary.Absolute;
import solver.constraints.binary.DistanceXYC;
import solver.constraints.binary.Element;
import solver.constraints.binary.Square;
import solver.constraints.extension.BinCSP;
import solver.constraints.extension.LargeCSP;
import solver.constraints.nary.*;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.nary.automata.CostRegular;
import solver.constraints.nary.automata.FA.CostAutomaton;
import solver.constraints.nary.automata.FA.IAutomaton;
import solver.constraints.nary.automata.FA.ICostAutomaton;
import solver.constraints.nary.automata.MultiCostRegular;
import solver.constraints.nary.automata.Regular;
import solver.constraints.nary.channeling.DomainChanneling;
import solver.constraints.nary.channeling.InverseChanneling;
import solver.constraints.nary.cnf.ALogicTree;
import solver.constraints.nary.cnf.ConjunctiveNormalForm;
import solver.constraints.nary.cnf.Literal;
import solver.constraints.nary.cnf.Node;
import solver.constraints.nary.lex.Lex;
import solver.constraints.nary.lex.LexChain;
import solver.constraints.propagators.extension.binary.BinRelation;
import solver.constraints.propagators.extension.nary.LargeRelation;
import solver.constraints.propagators.nary.PropIndexValue;
import solver.constraints.propagators.nary.PropNoSubtour;
import solver.constraints.propagators.nary.PropSubcircuit;
import solver.constraints.propagators.nary.alldifferent.PropAllDiffAC;
import solver.constraints.propagators.nary.sum.PropSumEq;
import solver.constraints.reified.ReifiedConstraint;
import solver.constraints.ternary.*;
import solver.constraints.unary.Member;
import solver.constraints.unary.NotMember;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.Arrays;

/**
 * A Factory to declare constraint based on integer variables (only).
 * One can call directly the constructor of constraints, but it is recommended
 * to use the Factory, because signatures and javadoc are ensured to be up-to-date.
 * <br/>
 * As much as possible, the API names of global constraints must match
 * those define in the <a href="http://www.emn.fr/z-info/sdemasse/gccat/index.html">Global Constraint Catalog</a>.
 *
 * @author Charles Prud'homme
 * @since 21/01/13
 */
public enum IntConstraintFactory {
    ;


    // BEWARE: PLEASE, keep signatures sorted in alphabetical order!!

    /**
     * Create an empty constraint to be filled with propagators
     *
     * @param solver
     * @return an empty constraint to be filled with propagators
     */
    public static Constraint makeEmptyConstraint(Solver solver) {
        return new Constraint(solver);
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
    public static Arithmetic arithm(IntVar VAR, String OP, int CSTE) {
        Operator op = Operator.get(OP);
        return new Arithmetic(VAR, op, CSTE, VAR.getSolver());
    }


    /**
     * Ensures VAR takes its values in TABLE
     *
     * @param VAR   an integer variable
     * @param TABLE an array of values
     */
    public static Member member(IntVar VAR, int[] TABLE) {
        return new Member(VAR, TABLE, VAR.getSolver());
    }

    /**
     * Ensures VAR takes its values in [LB, UB]
     *
     * @param VAR an integer variable
     * @param LB  the lower bound of the interval
     * @param UB  the upper bound of the interval
     */
    public static Member member(IntVar VAR, int LB, int UB) {
        return new Member(VAR, LB, UB, VAR.getSolver());
    }

    /**
     * Ensures VAR does not take its values in TABLE
     *
     * @param VAR   an integer variable
     * @param TABLE an array of values
     */
    public static NotMember not_member(IntVar VAR, int[] TABLE) {
        return new NotMember(VAR, TABLE, VAR.getSolver());
    }

    /**
     * Ensures VAR does not take its values in [LB, UB]
     *
     * @param VAR an integer variable
     * @param LB  the lower bound of the interval
     * @param UB  the upper bound of the interval
     */
    public static NotMember not_member(IntVar VAR, int LB, int UB) {
        return new NotMember(VAR, LB, UB, VAR.getSolver());
    }

    //##################################################################################################################
    //BINARIES #########################################################################################################
    //##################################################################################################################

    /**
     * Enforces VAR1 = |VAR2|
     */
    public static Absolute absolute(IntVar VAR1, IntVar VAR2) {
        assert VAR1.getSolver() == VAR2.getSolver();
        return new Absolute(VAR1, VAR2, VAR1.getSolver());
    }

    /**
     * Ensures: VAR1 OP VAR2, where OP in {"=", "!=", ">","<",">=","<="}
     *
     * @param VAR1 first variable
     * @param OP   an operator
     * @param VAR2 second variable
     */
    public static Arithmetic arithm(IntVar VAR1, String OP, IntVar VAR2) {
        Operator op = Operator.get(OP);
        return new Arithmetic(VAR1, op, VAR2, VAR1.getSolver());
    }

    /**
     * Ensures: <br/>
     * |VAR1-VAR2| OP CSTE
     * <br/>
     * where OP can take its value among {"=", ">", "<", "!="}
     */
    public static DistanceXYC distance(IntVar VAR1, IntVar VAR2, String OP, int CSTE) {
        assert VAR1.getSolver() == VAR2.getSolver();
        Operator op = Operator.get(OP);
        return new DistanceXYC(VAR1, VAR2, op, CSTE, VAR1.getSolver());
    }

    /**
     * Build ELEMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param VALUE  value variable
     * @param TABLE  array of int
     * @param INDEX  index variable
     * @param OFFSET offset matching INDEX.LB and TABLE[0]
     */
    public static Element element(IntVar VALUE, int[] TABLE, IntVar INDEX, int OFFSET) {
        assert VALUE.getSolver() == INDEX.getSolver();
        return new Element(VALUE, TABLE, INDEX, OFFSET, "none", VALUE.getSolver());
    }

    /**
     * Build ELEMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param VALUE  VALUE
     * @param TABLE  TABLE
     * @param INDEX  INDEX
     * @param OFFSET offset matching INDEX.LB and TABLE[0]
     * @param SORT   "asc","desc", detect" : values are sorted wrt <code>sort</code>
     */
    public static Element element(IntVar VALUE, int[] TABLE, IntVar INDEX, int OFFSET, String SORT) {
        return new Element(VALUE, TABLE, INDEX, OFFSET, SORT, VALUE.getSolver());
    }

    /**
     * Build ELEMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param VALUE value variable
     * @param TABLE array of int
     * @param INDEX index variable
     */
    public static Element element(IntVar VALUE, int[] TABLE, IntVar INDEX) {
        return new Element(VALUE, TABLE, INDEX, 0, "none", VALUE.getSolver());
    }

    /**
     * Build ELEMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param VALUE value variable
     * @param TABLE array of int
     * @param INDEX index variable
     * @param SORT  "asc","desc", detect" : values are sorted wrt <code>sort</code>
     */
    public static Element element(IntVar VALUE, int[] TABLE, IntVar INDEX, String SORT) {
        return new Element(VALUE, TABLE, INDEX, 0, SORT, VALUE.getSolver());
    }

    /**
     * Build an ELEMENT constraint: VALUE = TABLE[INDEX] where TABLE is an array of variables.
     *
     * @param VALUE value variable
     * @param TABLE array of variables
     * @param INDEX index variable
     */
    public static Element element(IntVar VALUE, IntVar[] TABLE, IntVar INDEX, int offset) {
        return new Element(VALUE, TABLE, INDEX, offset, VALUE.getSolver());
    }

    /**
     * Enforces VAR1 = VAR2^2
     */
    public static Square square(IntVar VAR1, IntVar VAR2) {
        assert VAR1.getSolver() == VAR2.getSolver();
        return new Square(VAR1, VAR2, VAR1.getSolver());
    }


    //##################################################################################################################
    //TERNARIES ########################################################################################################
    //##################################################################################################################

    /**
     * Ensures: VAR1 OP VAR2, where OP in {"=", "!=", ">","<",">=","<="}
     *
     * @param VAR1 first variable
     * @param OP1  an operator
     * @param VAR2 second variable
     * @param OP2  another operator
     * @param CSTE an operator
     */
    public static Arithmetic arithm(IntVar VAR1, String OP1, IntVar VAR2, String OP2, int CSTE) {
        Operator op1 = Operator.get(OP1);
        Operator op2 = Operator.get(OP2);
        return new Arithmetic(VAR1, op1, VAR2, op2, CSTE, VAR1.getSolver());
    }

    /**
     * Ensures: <br/>
     * |VAR1-VAR2| OP CSTE
     * <br/>
     * where OP can take its value among {"=", ">", "<"}
     *
     * @param VAR1 first variable
     * @param VAR2 second variable
     * @param OP   an operator
     * @param VAR3 resulting variable
     */
    public static DistanceXYZ distance(IntVar VAR1, IntVar VAR2, String OP, IntVar VAR3) {
        Operator op = Operator.get(OP);
        return new DistanceXYZ(VAR1, VAR2, op, VAR3, VAR1.getSolver());

    }

    /**
     * Ensures VAR1 / VAR2 = VAR 3, rounding towards 0 -- Euclidean division
     *
     * @param VAR1 dividend
     * @param VAR2 divisor
     * @param VAR3 result
     */
    public static DivXYZ eucl_div(IntVar VAR1, IntVar VAR2, IntVar VAR3) {
        return new DivXYZ(VAR1, VAR2, VAR3, VAR1.getSolver());
    }

    /**
     * Ensures: MAX = MAX(VAR1, VAR2)
     *
     * @param MAX  a variable
     * @param VAR1 a variable
     * @param VAR2 a variable
     */
    public static Max maximum(IntVar MAX, IntVar VAR1, IntVar VAR2) {
        return new Max(MAX, VAR1, VAR2, MAX.getSolver());
    }

    /**
     * Ensures:  VAR1 = MIN(VAR2, VAR3)
     *
     * @param MIN  result
     * @param VAR1 result
     * @param VAR2 first variable
     */
    public static Min minimum(IntVar MIN, IntVar VAR1, IntVar VAR2) {
        return new Min(MIN, VAR1, VAR2, MIN.getSolver());
    }

    /**
     * Ensures VAR1 % VAR2 = VAR 3,
     * <br/>i.e.:<br/>
     * - VAR1 / VAR2 = T1 and,<br/>
     * - T1 * VAR2 = T2 and,<br/>
     * - Z + T2 = VAR1<br/>
     * <br/>
     * where T1 = T2 = [-|VAR1|, |VAR1|]
     *
     * @param VAR1 first variable
     * @param VAR2 second variable
     * @param VAR3 result
     */
    public static ModXYZ mod(IntVar VAR1, IntVar VAR2, IntVar VAR3) {
        return new ModXYZ(VAR1, VAR2, VAR3, VAR1.getSolver());
    }

    /**
     * Ensures: VAR1 * VAR2 = VAR3
     *
     * @param VAR1 first variable
     * @param VAR2 second variable
     * @param VAR3 result
     */
    public static Times times(IntVar VAR1, IntVar VAR2, IntVar VAR3) {
        return new Times(VAR1, VAR2, VAR3, VAR1.getSolver());
    }

    //##################################################################################################################
    //GLOBALS ##########################################################################################################
    //##################################################################################################################

    /**
     * Ensures that all variables from VARS take a different value (Arc Consistency algorithm).
     * <br/>
     * Uses Regin algorithm
     * Runs in O(m.n) worst case time for the initial propagation and then in O(n+m) time
     * per arc removed from the support.
     * Has a good average behavior in practice
     *
     * @param VARS list of variables
     */
    public static AllDifferent alldifferent_ac(IntVar[] VARS) {
        return new AllDifferent(VARS, VARS[0].getSolver(), AllDifferent.Type.AC);
    }

    /**
     * Ensures that all variables from VARS take a different value (Bound Consistency algorithm).
     * <br/>
     * Based on: </br>
     * "A Fast and Simple Algorithm for Bounds Consistency of the AllDifferent Constraint"</br>
     * A. Lopez-Ortiz, CG. Quimper, J. Tromp, P.van Beek
     * <br/>
     *
     * @param VARS list of variables
     */
    public static AllDifferent alldifferent_bc(IntVar[] VARS) {
        return new AllDifferent(VARS, VARS[0].getSolver(), AllDifferent.Type.BC);
    }

    /**
     * NVAR is the number of variables of the collection VARIABLES that take their value in VALUE.
     *
     * @param NVAR  a variable
     * @param VARS  vector of variables
     * @param VALUE a value
     */
    public static Among among(IntVar NVAR, IntVar[] VARS, int VALUE) {
        return new Among(NVAR, VARS, VALUE, NVAR.getSolver());
    }

    /**
     * NVAR is the number of variables of the collection VARIABLES that take their value in VALUES.
     *
     * @param NVAR   a variable
     * @param VARS   vector of variables
     * @param VALUES set of values
     */
    public static Among among(IntVar NVAR, IntVar[] VARS, int[] VALUES) {
        return new Among(NVAR, VARS, VALUES, NVAR.getSolver());
    }

    /**
     * The number of distinct values taken by the variables of the collection VARS is greater than or equal to NVAL.
     * Performs Generalized Arc Consistency based on Maximum Bipartite Matching.
     * The worst case time complexity is O(nm) but this is very pessimistic.
     * In practice it is more like O(m) where m is the number of variable-value pairs.
     *
     * @param NVAL a variable
     * @param VARS a vector of variables
     */
    public static AtLeastNValues atleast_nvalues(IntVar NVAL, IntVar[] VARS) {
        return new AtLeastNValues(NVAL, VARS, NVAL.getSolver());
    }

    /**
     * The number of distinct values taken by the variables of the collection VARS is less than or equal to NVAL.
     * <br/>
     * Performs Bound Consistency in O(n+d) with
     * n = |VARS|
     * d = maxValue - minValue (from initial domains)
     * <p/>
     * => very appropriate when d <= n It is indeed much better than the usual time complexity of O(n.log(n))
     * =>  not appropriate when d >> n (you should encode another data structure and a quick sort algorithm)
     *
     * @param NVAL a variable
     * @param VARS a vector of variables
     */
    public static AtMostNValues atmost_nvalues(IntVar NVAL, IntVar[] VARS) {
        return new AtMostNValues(VARS, NVAL, NVAL.getSolver(), AtMostNValues.Algo.BC);
    }

    /**
     * The number of distinct values taken by the variables of the collection VARS is less than or equal to NVAL.
     * <br/>
     * Performs Greedy algorithm.
     * No level of consistency but better than BC in general (for enumerated domains with holes)
     *
     * @param NVAL a variable
     * @param VARS a vector of variables
     */
    public static AtMostNValues atmost_nvalues_greedy(IntVar NVAL, IntVar[] VARS) {
        return new AtMostNValues(VARS, NVAL, NVAL.getSolver(), AtMostNValues.Algo.Greedy);
    }

    /**
     * Maps the boolean assignments variables BVARS with the standard assignment variable VAR.
     * VAR = i <-> BVARS[i] = 1
     *
     * @param BVARS array of boolean variables
     * @param VAR   observed variable
     */
    public static DomainChanneling channeling(BoolVar[] BVARS, IntVar VAR) {
        return new DomainChanneling(BVARS, VAR, VAR.getSolver());
    }

    /**
     * Make an inverse channeling between VARS1 and VARS2:
     * VARS1[i] = j <=> VARS2[j] = i
     * Performs AC if domains are enumerated.
     * If not, then it works on bounds without guaranteeing BC
     * (enumerated domains are strongly recommended)
     *
     * @param VARS1 vector of variables
     * @param VARS2 vector of variables
     */
    public static InverseChanneling channeling(IntVar[] VARS1, IntVar[] VARS2) {
        return new InverseChanneling(VARS1, VARS2, 0, 0, VARS1[0].getSolver());
    }

    /**
     * Make an inverse channeling between VARS1 and VARS2:
     * VARS1[i] = j <=> VARS2[j] = i
     * Performs AC if domains are enumerated.
     * If not, then it works on bounds without guaranteeing BC
     * (enumerated domains are strongly recommended)
     *
     * @param VARS1 vector of variables
     * @param MIN1  lowest value in VARS1 if not 0
     * @param VARS2 vector of variables
     * @param MIN1  lowest value in VARS2 if not 0
     */
    public static InverseChanneling channeling(IntVar[] VARS1, int MIN1, IntVar[] VARS2, int MIN2) {
        return new InverseChanneling(VARS1, VARS2, MIN1, MIN2, VARS1[0].getSolver());
    }

    /**
     * Creates a circuit constraint which ensures that
     * <p/>
     * the elements of vars define a covering circuit
     * where vars[i] = j means that j is the successor of i.
     *
     * @param vars
     * @return a circuit constraint
     */
    public static Constraint circuit(IntVar[] vars, int offset) {
        Solver solver = vars[0].getSolver();
        Constraint c = new Constraint(solver);
        c.setPropagators(
                new PropAllDiffAC(vars, c, solver),
                new PropNoSubtour<IntVar>(vars, offset, solver, c));
        return c;
    }

    /**
     * Creates a circuit constraint which ensures that
     * <p/>
     * the elements of vars define a covering circuit
     * where vars[i] = j means that j is the successor of i.
     *
     * @param vars
     * @return a circuit constraint
     */
    public static Constraint circuit(IntVar[] vars) {
        return circuit(vars, 0);
    }


    /**
     * Ensures that the clauses defined in the Boolean logic formula TREE are satisfied.
     *
     * @param TREE   the syntactic tree
     * @param SOLVER solver is required, as the TREE can be declared without any variables
     * @return
     */
    public static ConjunctiveNormalForm clauses(ALogicTree TREE, Solver SOLVER) {
        return new ConjunctiveNormalForm(TREE, SOLVER);
    }

    /**
     * Ensures that the clauses defined in the Boolean logic formula TREE are satisfied.
     *
     * @param POSLITS positive literals
     * @param NEGLITS negative literals
     */
    public static ConjunctiveNormalForm clauses(BoolVar[] POSLITS, BoolVar[] NEGLITS) {
        Solver solver;
        if (POSLITS.length > 0) {
            solver = POSLITS[0].getSolver();
        } else {
            solver = NEGLITS[0].getSolver();
        }
        Literal[] lits = new Literal[POSLITS.length + NEGLITS.length];
        int i = 0;
        for (; i < POSLITS.length; i++) {
            lits[i] = Literal.pos(POSLITS[i]);
        }
        for (int j = 0; j < NEGLITS.length; j++) {
            lits[j + i] = Literal.neg(NEGLITS[j]);
        }
        ALogicTree tree = Node.or(lits);
        return new ConjunctiveNormalForm(tree, solver);
    }

    /**
     * Let N be the number of variables of the VARIABLES collection assigned to value VALUE;
     * Enforce condition N RELOP LIMIT to hold.
     *
     * @param VALUE an int
     * @param VARS  a vector of variables
     * @param RELOP an operator, among {"=", ">=", "<="}
     * @param LIMIT a variable
     */
    public static Count count(int VALUE, IntVar[] VARS, String RELOP, IntVar LIMIT) {
        Operator op = Operator.get(RELOP);
        return new Count(VALUE, VARS, op, LIMIT, LIMIT.getSolver());
    }


    /**
     * Let N be the number of variables of the VARIABLES collection assigned to value VALUE;
     * Enforce condition N RELOP LIMIT to hold.
     *
     * @param VALUE an int
     * @param VARS  a vector of variables
     * @param RELOP an operator, among {"=", ">=", "<="}
     * @param LIMIT an int
     */
    public static Count count(int VALUE, IntVar[] VARS, String RELOP, int LIMIT) {
        Operator op = Operator.get(RELOP);
        return new Count(VALUE, VARS, op, LIMIT, VARS[0].getSolver());
    }

    /**
     * Ensures that the assignment of a sequence of VARS is recognized by AUTOMATON, a deterministic finite automaton,
     * and that the sum of the COSTS associated to each assignment is bounded by the COST variable.
     *
     * @param VARS      sequence of variables
     * @param COST      cost variable
     * @param AUTOMATON a deterministic finite automaton defining the regular language
     * @param COSTS     assignments costs
     */
    public static CostRegular cost_regular(IntVar[] VARS, IntVar COST, IAutomaton AUTOMATON, int[][] COSTS) {
        return new CostRegular(VARS, COST,
                CostAutomaton.makeSingleResource(AUTOMATON, COSTS, COST.getLB(), COST.getUB()),
                VARS[0].getSolver());
    }

    /**
     * Ensures that the assignment of a sequence of VARS is recognized by AUTOMATON, a deterministic finite automaton,
     * and that the sum of the COSTS associated to each assignment is bounded by the COST variable.
     * This version allows to specify different costs according to the automaton state at which the assignment occurs
     * (i.e. the transition starts)
     * <p/>
     *
     * @param VARS      sequence of variables
     * @param COST      cost variable
     * @param AUTOMATON a deterministic finite automaton defining the regular language
     * @param COSTS     assignments costs
     */
    public static CostRegular cost_regular(IntVar[] VARS, IntVar COST, IAutomaton AUTOMATON, int[][][] COSTS) {
        return new CostRegular(VARS, COST,
                CostAutomaton.makeSingleResource(AUTOMATON, COSTS, COST.getLB(), COST.getUB()),
                VARS[0].getSolver());
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
     */
    public static CostRegular cost_regular(IntVar[] VARS, IntVar COST, ICostAutomaton CAUTOMATON) {
        return new CostRegular(VARS, COST, CAUTOMATON, VARS[0].getSolver());
    }


    /**
     * Each values VALUES[i] should be taken exactly OCCURRENCES[i] variables of VARS.
     * <br/>
     * Ensures Bound Consistency.
     *
     * @param VARS        collection of variables
     * @param VALUES      collection of constrained values
     * @param OCCURRENCES collection of cardinality variables
     * @param CLOSED      restricts domains of VARS to VALUES if set to true
     */
    public static GlobalCardinality global_cardinality_bc(IntVar[] VARS, int[] VALUES, IntVar[] OCCURRENCES, boolean CLOSED) {
        Solver solver = VARS[0].getSolver();

        TIntObjectHashMap<IntVar> map = new TIntObjectHashMap<IntVar>(VALUES.length);
        for (int i = 0; i < VALUES.length; i++) {
            map.put(VALUES[i], OCCURRENCES[i]);
        }

        int n = VARS.length;
        Arrays.sort(VALUES);
        int min = VALUES[0];
        int max = VALUES[VALUES.length - 1];

        for (int v = 0; v < VARS.length; v++) {
            IntVar var = VARS[v];
            if (min > var.getLB()) {
                min = var.getLB();
            }
            if (max < var.getUB()) {
                max = var.getUB();
            }
        }

        IntVar[] cards = new IntVar[max - min + 1];
        int[] values = new int[max - min + 1];
        for (int i = min; i <= max; i++) {
            values[i - min] = i;
            if (map.containsKey(i)) {
                cards[i - min] = map.get(i);
            } else {
                if (CLOSED) {
                    cards[i - min] = Views.fixed(0, solver);
                } else {
                    cards[i - min] = VariableFactory.bounded(StringUtils.randomName(), 0, n, solver);
                }
            }
        }
        return new GlobalCardinality(VARS, values, cards, GlobalCardinality.Consistency.BC, solver);

    }

    /**
     * Each values VALUES[i] should be taken by at least LOWS[i] and at most UPS[i] variables of VARS.
     * <br/>
     * Ensures Bound Consistency.
     *
     * @param VARS   collection of variables
     * @param VALUES collection of constrained values
     * @param LOWS   minimum occurrences of each values of VALUES
     * @param UPS    maximum occurrences of each values of VALUES
     * @param CLOSED restricts domains of VARS to VALUES if set to true
     */
    public static GlobalCardinalityLowUp global_cardinality_low_up_bc(IntVar[] VARS, int[] VALUES, int[] LOWS, int[] UPS,
                                                                      boolean CLOSED) {
        Solver solver = VARS[0].getSolver();

        TIntObjectHashMap<int[]> map = new TIntObjectHashMap<int[]>(VALUES.length);
        for (int i = 0; i < VALUES.length; i++) {
            map.put(VALUES[i], new int[]{LOWS[i], UPS[i]});
        }

        int n = VARS.length;
        Arrays.sort(VALUES);
        int min = VALUES[0];
        int max = VALUES[VALUES.length - 1];

        for (int v = 0; v < VARS.length; v++) {
            IntVar var = VARS[v];
            if (min > var.getLB()) {
                min = var.getLB();
            }
            if (max < var.getUB()) {
                max = var.getUB();
            }
        }

        int[] mOCC = new int[max - min + 1];
        int[] MOCC = new int[max - min + 1];
        int[] values = new int[max - min + 1];
        for (int i = min; i <= max; i++) {
            values[i - min] = i;
            if (map.containsKey(i)) {
                int[] lu = map.get(i);
                mOCC[i - min] = lu[0];
                MOCC[i - min] = lu[1];
            } else {
                if (CLOSED) {
                    mOCC[i - min] = 0;
                    MOCC[i - min] = 0;
                } else {
                    mOCC[i - min] = 0;
                    MOCC[i - min] = n;
                }
            }
        }
        return new GlobalCardinalityLowUp(VARS, values, mOCC, MOCC, GlobalCardinalityLowUp.Consistency.BC, solver);

    }

    /**
     * Each values VALUES[i] should be taken exactly OCCURRENCES[i] variables of VARS.
     * <br/>
     * Ensures Arc Consistency.
     *
     * @param VARS        collection of variables
     * @param VALUES      collection of constrained values
     * @param OCCURRENCES collection of cardinality variables
     * @param CLOSED      restricts domains of VARS to VALUES if set to true
     * @param AC_ON_CARDS ensures AC on cards too, usually faster if false
     */
    public static GlobalCardinality global_cardinality_ac(IntVar[] VARS, int[] VALUES, IntVar[] OCCURRENCES, boolean CLOSED, boolean AC_ON_CARDS) {
        Solver solver = VARS[0].getSolver();

        TIntObjectHashMap<IntVar> map = new TIntObjectHashMap<IntVar>(VALUES.length);
        for (int i = 0; i < VALUES.length; i++) {
            map.put(VALUES[i], OCCURRENCES[i]);
        }

        int n = VARS.length;
        Arrays.sort(VALUES);
        int min = VALUES[0];
        int max = VALUES[VALUES.length - 1];

        for (int v = 0; v < VARS.length; v++) {
            IntVar var = VARS[v];
            if (min > var.getLB()) {
                min = var.getLB();
            }
            if (max < var.getUB()) {
                max = var.getUB();
            }
        }

        IntVar[] cards = new IntVar[max - min + 1];
        int[] values = new int[max - min + 1];
        for (int i = min; i <= max; i++) {
            values[i - min] = i;
            if (map.containsKey(i)) {
                cards[i - min] = map.get(i);
            } else {
                if (CLOSED) {
                    cards[i - min] = Views.fixed(0, solver);
                } else {
                    cards[i - min] = VariableFactory.bounded(StringUtils.randomName(), 0, n, solver);
                }
            }
        }
        return new GlobalCardinality(VARS, values, cards,
                AC_ON_CARDS ? GlobalCardinality.Consistency.AC_ON_CARDS : GlobalCardinality.Consistency.AC, solver);

    }

    /**
     * Each values VALUES[i] should be taken by at least LOWS[i] and at most UPS[i] variables of VARS.
     * <br/>
     * Ensures Arc Consistency.
     *
     * @param VARS   collection of variables
     * @param VALUES collection of constrained values
     * @param LOWS   minimum occurrences of each values of VALUES
     * @param UPS    maximum occurrences of each values of VALUES
     * @param CLOSED restricts domains of VARS to VALUES if set to true
     */
    public static GlobalCardinalityLowUp global_cardinality_low_up_ac(IntVar[] VARS, int[] VALUES, int[] LOWS, int[] UPS,
                                                                      boolean CLOSED) {
        Solver solver = VARS[0].getSolver();

        TIntObjectHashMap<int[]> map = new TIntObjectHashMap<int[]>(VALUES.length);
        for (int i = 0; i < VALUES.length; i++) {
            map.put(VALUES[i], new int[]{LOWS[i], UPS[i]});
        }

        int n = VARS.length;
        Arrays.sort(VALUES);
        int min = VALUES[0];
        int max = VALUES[VALUES.length - 1];

        for (int v = 0; v < VARS.length; v++) {
            IntVar var = VARS[v];
            if (min > var.getLB()) {
                min = var.getLB();
            }
            if (max < var.getUB()) {
                max = var.getUB();
            }
        }

        int[] mOCC = new int[max - min + 1];
        int[] MOCC = new int[max - min + 1];
        int[] values = new int[max - min + 1];
        for (int i = min; i <= max; i++) {
            values[i - min] = i;
            if (map.containsKey(i)) {
                int[] lu = map.get(i);
                mOCC[i - min] = lu[0];
                MOCC[i - min] = lu[1];
            } else {
                if (CLOSED) {
                    mOCC[i - min] = 0;
                    MOCC[i - min] = 0;
                } else {
                    mOCC[i - min] = 0;
                    MOCC[i - min] = n;
                }
            }
        }
        return new GlobalCardinalityLowUp(VARS, values, mOCC, MOCC, GlobalCardinalityLowUp.Consistency.AC, solver);
    }


    /**
     * Ensures that :
     * <br/>- OCCURRENCES[i] * WEIGHT[i] &#8804; CAPA
     * <br/>- OCCURRENCES[i] * ENERGY[i] = POWER
     * <br/>and maximizing the value of POWER.
     *
     * @param OCCURRENCES number of occurrences of an item
     * @param CAPA        capacity of the knapsack
     * @param POWER       variable to maximize
     * @param WEIGHT      weight of each item
     * @param ENERGY      energy of each item
     */
    public static Knapsack knapsack(IntVar[] OCCURRENCES, IntVar CAPA, IntVar POWER,
                                    int[] WEIGHT, int[] ENERGY) {
        return new Knapsack(OCCURRENCES, CAPA, POWER, WEIGHT, ENERGY, CAPA.getSolver());
    }

    /**
     * For each pair of consecutive vectors VARS<sub>i</sub> and VARS<sub>i+1</sub> of the VARS collection
     * VARS<sub>i</sub> is lexicographically strictly less than than VARS<sub>i+1</sub>
     *
     * @param VARS collection of vectors of variables
     */
    public static LexChain lex_chain_less(IntVar[]... VARS) {
        return new LexChain(true, VARS[0][0].getSolver(), VARS);
    }


    /**
     * For each pair of consecutive vectors VARS<sub>i</sub> and VARS<sub>i+1</sub> of the VARS collection
     * VARS<sub>i</sub> is lexicographically less or equal than than VARS<sub>i+1</sub>
     *
     * @param VARS collection of vectors of variables
     */
    public static LexChain lex_chain_less_eq(IntVar[]... VARS) {
        return new LexChain(false, VARS[0][0].getSolver(), VARS);
    }

    /**
     * Ensures that VARS1 is lexicographically strictly less than VARS2.
     *
     * @param VARS1 vector of variables
     * @param VARS2 vector of variables
     */
    public static Lex lex_less(IntVar[] VARS1, IntVar[] VARS2) {
        return new Lex(VARS1, VARS2, true, VARS1[0].getSolver());
    }

    /**
     * Ensures that VARS1 is lexicographically less or equal than VARS2.
     *
     * @param VARS1 vector of variables
     * @param VARS2 vector of variables
     */
    public static Lex lex_less_eq(IntVar[] VARS1, IntVar[] VARS2) {
        return new Lex(VARS1, VARS2, false, VARS1[0].getSolver());
    }

    /**
     * MAX is the maximum value of the collection of domain variables VARS
     *
     * @param MAX  a variable
     * @param VARS a vector of variables
     */
    public static MaxOfAList maximum(IntVar MAX, IntVar[] VARS) {
        return new MaxOfAList(MAX, VARS, MAX.getSolver());
    }

    /**
     * MIN is the minimum value of the collection of domain variables VARS
     *
     * @param MIN  a variable
     * @param VARS a vector of variables
     */
    public static MinOfAList minimum(IntVar MIN, IntVar[] VARS) {
        return new MinOfAList(MIN, VARS, MIN.getSolver());
    }

    /**
     * Ensures that the assignment of a sequence of VARS is recognized by AUTOMATON, a deterministic finite automaton,
     * and that the sum of the cost vector COSTS associated to each assignment is bounded by the variable vector CVARS.
     *
     * @param VARS      sequence of variables
     * @param CVARS     cost variables
     * @param AUTOMATON a deterministic finite automaton defining the regular language
     * @param COSTS     assignments costs
     */
    public static MultiCostRegular multicost_regular(IntVar[] VARS, IntVar[] CVARS, IAutomaton AUTOMATON, int[][][] COSTS) {
        return new MultiCostRegular(VARS, CVARS,
                CostAutomaton.makeMultiResources(AUTOMATON, COSTS, CVARS),
                VARS[0].getSolver());
    }

    /**
     * Ensures that the assignment of a sequence of VARS is recognized by AUTOMATON, a deterministic finite automaton,
     * and that the sum of the cost vector COSTS associated to each assignment is bounded by the variable vector CVARS.
     * This version allows to specify different costs according to the automaton state at which the assignment occurs
     * (i.e. the transition starts)
     *
     * @param VARS      sequence of variables
     * @param CVARS     cost variables
     * @param AUTOMATON a deterministic finite automaton defining the regular language
     * @param COSTS     assignments costs
     */
    public static MultiCostRegular multicost_regular(IntVar[] VARS, IntVar[] CVARS, IAutomaton AUTOMATON, int[][][][] COSTS) {
        return new MultiCostRegular(VARS, CVARS,
                CostAutomaton.makeMultiResources(AUTOMATON, COSTS, CVARS),
                VARS[0].getSolver());
    }

    /**
     * Ensures that the assignment of a sequence of VARS is recognized by AUTOMATON, a deterministic finite automaton,
     * and that the sum of the cost vector COSTS associated to each assignment is bounded by the variable vector CVARS.
     * This version allows to specify different costs according to the automaton state at which the assignment occurs
     * (i.e. the transition starts)
     *
     * @param VARS       sequence of variables
     * @param CVARS      cost variables
     * @param CAUTOMATON a deterministic finite automaton defining the regular language and the costs
     */
    public static MultiCostRegular multicost_regular(IntVar[] VARS, IntVar[] CVARS, ICostAutomaton CAUTOMATON) {
        return new MultiCostRegular(VARS, CVARS, CAUTOMATON, VARS[0].getSolver());
    }

    /**
     * Ensures that graph defined by VARS has no subcircuit.
     *
     * @param VARS collection of variables
     */
    public static NoSubTours no_sub_tours(IntVar[] VARS) {
        return new NoSubTours(VARS, VARS[0].getSolver());
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
    public static Regular regular(IntVar[] VARS, IAutomaton AUTOMATON) {
        return new Regular(VARS, AUTOMATON, VARS[0].getSolver());
    }

    /**
     * Ensures:<br/>
     * - BVAR = 1 <=>  CSTR1 is satisfied, <br/>
     * - BVAR = 0 <=>  CSTR2 is satisfied<br/>
     * <p/>
     * Most of the time, CSTR2 is the negation of CSTR2, but this is not mandatory.
     * Example of use: <br/>
     * - <code>reified(b1, arithm(v1, "=", 2), arithm(v1, "!=", 2));</code>:
     * b1 is equal to 1 <=> v1 = 2, b1 is equal to 0 <=> v1 != 2.
     *
     * @param BVAR  variable of reification
     * @param CSTR1 the constraint to be satisfied when BVAR = 1
     * @param CSTR2 the constraint to be satisfied when BVAR = 0
     */
    public static ReifiedConstraint reified(BoolVar BVAR, Constraint CSTR1, Constraint CSTR2) {
        return new ReifiedConstraint(BVAR, CSTR1, CSTR2, BVAR.getSolver());
    }


    /**
     * Creates a subcircuit constraint which ensures that
     * <p/>
     * the elements of vars define a single circuit of subcircuitSize nodes
     * where vars[i] = j means that j is the successor of i.
     * and vars[i] = i means that i is not part of the circuit
     *
     * @param vars
     * @param offset
     * @param subcircuitSize expected number of nodes in the circuit
     * @return a circuit constraint
     */
    public static Constraint subcircuit(IntVar[] vars, int offset, IntVar subcircuitSize) {
        int n = vars.length;
        Solver solver = vars[0].getSolver();
        IntVar nbLoops = VariableFactory.bounded("nLoops", 0, n, solver);
        Constraint c = new Constraint(solver);
        c.addPropagators(new PropSumEq(new IntVar[]{nbLoops, subcircuitSize}, new int[]{1, 1}, 2, n, solver, c));
        c.addPropagators(new PropAllDiffAC(vars, c, solver));
        c.addPropagators(new PropIndexValue(vars, offset, nbLoops, c, solver));
        c.addPropagators(new PropSubcircuit(vars, offset, subcircuitSize, c, solver));
        return c;
    }

    /**
     * Creates a subcircuit constraint which ensures that
     * <p/>
     * the elements of vars define a single circuit of subcircuitSize nodes
     * where vars[i] = j means that j is the successor of i.
     * and vars[i] = i means that i is not part of the circuit
     *
     * @param vars
     * @param subcircuitSize expected number of nodes in the circuit
     * @return a circuit constraint
     */
    public static Constraint subcircuit(IntVar[] vars, IntVar subcircuitSize) {
        return subcircuit(vars, 0, subcircuitSize);
    }

    /**
     * Creates a subcircuit constraint which ensures that
     * <p/>
     * the elements of vars define a single circuit
     * where vars[i] = j means that j is the successor of i.
     * and vars[i] = i means that i is not part of the circuit
     *
     * @param vars
     * @param offset
     * @return a circuit constraint
     */
    public static Constraint subcircuit(IntVar[] vars, int offset) {
        Solver solver = vars[0].getSolver();
        return subcircuit(vars, offset, VariableFactory.bounded("subcircuit length", 0, vars.length, solver));
    }

    /**
     * Creates a subcircuit constraint which ensures that
     * <p/>
     * the elements of vars define a single circuit
     * where vars[i] = j means that j is the successor of i.
     * and vars[i] = i means that i is not part of the circuit
     *
     * @param vars
     * @return a circuit constraint
     */
    public static Constraint subcircuit(IntVar[] vars) {
        Solver solver = vars[0].getSolver();
        return subcircuit(vars, 0, VariableFactory.bounded("subcircuit length", 0, vars.length, solver));
    }

    /**
     * Enforces that &#8721;<sub>i in |VARS|</sub>VARS<sub>i</sub> OP SUM.
     *
     * @param VARS a vector of variables
     * @param OP   an operator among {"=", "!=", ">=","<="}
     * @param SUM  an int
     */
    public static Sum sum(IntVar[] VARS, String OP, int SUM) {
        return Sum.build(VARS, SUM, Operator.get(OP), VARS[0].getSolver());
    }

    /**
     * Enforces that &#8721;<sub>i in |VARS|</sub>VARS<sub>i</sub> OP SUM.
     *
     * @param VARS a vector of variables
     * @param OP   an operator among {"=", "!=", ">=","<="}
     * @param SUM  a variable
     */
    public static Sum sum(IntVar[] VARS, String OP, IntVar SUM) {
        return Sum.build(VARS, SUM, Operator.get(OP), VARS[0].getSolver());
    }

    /**
     * Enforces that &#8721;<sub>i in |VARS|</sub>COEFFS<sub>i</sub> * VARS<sub>i</sub> OP SUM.
     *
     * @param VARS   a vector of variables
     * @param COEFFS a vector of int
     * @param OP     an operator among {"=", "!=", ">=","<="}
     * @param SUM    an int
     */
    public static Sum scalar(IntVar[] VARS, int[] COEFFS, String OP, int SUM) {
        return Sum.build(VARS, COEFFS, Operator.get(OP), SUM, VARS[0].getSolver());
    }

    /**
     * Enforces that &#8721;<sub>i in |VARS|</sub>COEFFS<sub>i</sub> * VARS<sub>i</sub> OP COEFF * SUM.
     *
     * @param VARS   a vector of variables
     * @param COEFFS a vector of int
     * @param OP     an operator among {"=", "!=", ">=","<="}
     * @param COEFF  an int
     * @param SUM    a variable
     */
    public static Sum scalar(IntVar[] VARS, int[] COEFFS, String OP, IntVar SUM, int COEFF) {
        return Sum.build(VARS, COEFFS, SUM, COEFF, Operator.get(OP), VARS[0].getSolver());
    }

    /**
     * Create a table constraint (AC2001: Arc Consistency version 2001) over a couple of variables VAR1 and VAR2, .
     *
     * @param VAR1     first variable
     * @param VAR2     second variable
     * @param relation the relation between the two variables
     */
    public static BinCSP table_ac2001(IntVar VAR1, IntVar VAR2, BinRelation relation) {
        return new BinCSP(VAR1, VAR2, relation);
    }

    /**
     * Create a table constraint (AC2001: Arc Consistency version 2001) over a list of variables VARS.
     *
     * @param VARS     first variable
     * @param relation the relation between the two variables
     */
    public static LargeCSP table_ac2001(IntVar[] VARS, LargeRelation relation) {
        return new LargeCSP(VARS, relation, LargeCSP.Type.AC2001, VARS[0].getSolver());
    }

    /**
     * Create a table constraint (AC32: Arc Consistency version 32) over a list of variables VARS.
     *
     * @param VARS     first variable
     * @param relation the relation between the two variables
     */
    public static LargeCSP table_ac32(IntVar[] VARS, LargeRelation relation) {
        return new LargeCSP(VARS, relation, LargeCSP.Type.AC32, VARS[0].getSolver());
    }

    /**
     * Create a table constraint (FC: Forward Checking) over a list of variables VARS.
     *
     * @param VARS     first variable
     * @param relation the relation between the two variables
     */
    public static LargeCSP table_fc(IntVar[] VARS, LargeRelation relation) {
        return new LargeCSP(VARS, relation, LargeCSP.Type.FC, VARS[0].getSolver());
    }


}
