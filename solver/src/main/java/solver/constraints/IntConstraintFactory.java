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

import solver.Solver;
import solver.constraints.binary.Absolute;
import solver.constraints.binary.DistanceXYC;
import solver.constraints.binary.Element;
import solver.constraints.binary.Square;
import solver.constraints.extension.BinCSP;
import solver.constraints.extension.LargeCSP;
import solver.constraints.nary.cnf.ALogicTree;
import solver.constraints.nary.cnf.ConjunctiveNormalForm;
import solver.constraints.nary.cnf.Literal;
import solver.constraints.nary.cnf.Node;
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
import solver.exception.SolverException;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * A Factory to declare constraint based on integer variables (only).
 * One can call directly the constructor of constraints, but it is recommended
 * to use the Factory, because signatures and javadoc are ensured to be up-to-date.
 * <br/>
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
        if (op != Operator.EQ && op != Operator.GT && op != Operator.LT && op != Operator.NQ) {
            throw new SolverException("Unexpected operator for distance");
        }
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
        if (op != Operator.EQ && op != Operator.GT && op != Operator.LT) {
            throw new SolverException("Unexpected operator for distance");
        }
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
     * Ensures: VAR1 = MAX(VAR2, VAR3)
     *
     * @param VAR1 result
     * @param VAR2 first variable
     * @param VAR3 second variable
     */
    public static Max max(IntVar VAR1, IntVar VAR2, IntVar VAR3) {
        return new Max(VAR1, VAR2, VAR3, VAR1.getSolver());
    }

    /**
     * Ensures:  VAR1 = MIN(VAR2, VAR3)
     *
     * @param VAR1 result
     * @param VAR2 first variable
     * @param VAR3 second variable
     */
    public static Min min(IntVar VAR1, IntVar VAR2, IntVar VAR3) {
        return new Min(VAR1, VAR2, VAR3, VAR1.getSolver());
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
