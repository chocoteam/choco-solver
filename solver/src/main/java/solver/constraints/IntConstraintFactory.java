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
import solver.constraints.unary.Member;
import solver.constraints.unary.NotMember;
import solver.exception.SolverException;
import solver.variables.IntVar;

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

    //##################################################################################################################
    // UNARIES #########################################################################################################
    //##################################################################################################################

    /**
     * Ensures VAR takes its values in TABLE
     *
     * @param VAR    an integer variable
     * @param TABLE  an array of values
     * @param solver SOLVER
     */
    public static Member member(IntVar VAR, int[] TABLE, Solver solver) {
        return new Member(VAR, TABLE, solver);
    }

    /**
     * Ensures VAR takes its values in [LB, UB]
     *
     * @param VAR    an integer variable
     * @param LB     the lower bound of the interval
     * @param UB     the upper bound of the interval
     * @param solver SOLVER
     */
    public static Member member(IntVar VAR, int LB, int UB, Solver solver) {
        return new Member(VAR, LB, UB, solver);
    }

    /**
     * Ensures VAR does not take its values in TABLE
     *
     * @param VAR    an integer variable
     * @param TABLE  an array of values
     * @param solver SOLVER
     */
    public static NotMember not_member(IntVar VAR, int[] TABLE, Solver solver) {
        return new NotMember(VAR, TABLE, solver);
    }

    /**
     * Ensures VAR does not take its values in [LB, UB]
     *
     * @param VAR    an integer variable
     * @param LB     the lower bound of the interval
     * @param UB     the upper bound of the interval
     * @param solver SOLVER
     */
    public static NotMember not_member(IntVar VAR, int LB, int UB, Solver solver) {
        return new NotMember(VAR, LB, UB, solver);
    }

    //##################################################################################################################
    //BINARIES #########################################################################################################
    //##################################################################################################################

    /**
     * Enforces VAR1 = |VAR2|
     */
    public static Absolute absolute(IntVar VAR1, IntVar VAR2, Solver solver) {
        return new Absolute(VAR1, VAR2, solver);
    }

    /**
     * Ensures: <br/>
     * |VAR1-VAR2| OP CSTE
     * <br/>
     * where OP can take its value among {"=", ">", "<", "!="}
     */
    public static DistanceXYC distance(IntVar VAR1, IntVar VAR2, String OP, int CSTE, Solver solver) {
        Operator op = Operator.get(OP);
        if (op != Operator.EQ && op != Operator.GT && op != Operator.LT && op != Operator.MN) {
            throw new SolverException("Unexpected operator for distance");
        }
        return new DistanceXYC(VAR1, VAR2, op, CSTE, solver);
    }

    /**
     * Build ELEMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param value  VALUE
     * @param table  TABLE
     * @param index  INDEX
     * @param offset offset matching INDEX.LB and TABLE[0]
     * @param solver the attached solver
     */
    public static Element element(IntVar value, int[] table, IntVar index, int offset, Solver solver) {
        return new Element(value, table, index, offset, "none", solver);
    }

    /**
     * Build ELEMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param value  VALUE
     * @param table  TABLE
     * @param index  INDEX
     * @param offset offset matching INDEX.LB and TABLE[0]
     * @param sort   "asc","desc", detect" : values are sorted wrt <code>sort</code>
     * @param solver the attached solver
     */
    public static Element element(IntVar value, int[] table, IntVar index, int offset, String sort, Solver solver) {
        return new Element(value, table, index, offset, sort, solver);
    }

    /**
     * Build ELEMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param value  VALUE
     * @param table  TABLE
     * @param index  INDEX
     * @param solver the attached solver
     */
    public static Element element(IntVar value, int[] table, IntVar index, Solver solver) {
        return new Element(value, table, index, 0, "none", solver);
    }

    /**
     * Build ELEMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param value  VALUE
     * @param table  TABLE
     * @param index  INDEX
     * @param sort   "asc","desc", detect" : values are sorted wrt <code>sort</code>
     * @param solver the attached solver
     */
    public static Element element(IntVar value, int[] table, IntVar index, String sort, Solver solver) {
        return new Element(value, table, index, 0, sort, solver);
    }

    /**
     * Build an ELEMENT constraint: VALUE = TABLE[INDEX] where TABLE is an array of variables.
     *
     * @param value  VALUE
     * @param table  TABLE
     * @param index  INDEX
     * @param solver the attached solver
     */
    public static Element element(IntVar value, IntVar[] table, IntVar index, int offset, Solver solver) {
        return new Element(value, table, index, offset, solver);
    }

    /**
     * Enforces VAR1 = VAR2^2
     */
    public static Square square(IntVar VAR1, IntVar VAR2, Solver solver) {
        return new Square(VAR1, VAR2, solver);
    }


    //##################################################################################################################
    //TERNARIES ########################################################################################################
    //##################################################################################################################


    //##################################################################################################################
    //GLOBALS ##########################################################################################################
    //##################################################################################################################


}
