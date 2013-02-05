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
package solver.constraints.nary;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.Operator;
import solver.constraints.propagators.nary.PropCount;
import solver.exception.SolverException;
import solver.variables.IntVar;
import solver.variables.view.Views;

/**
 * count(VALUE,VARIABLES,RELOP,LIMIT)
 * <br/>syn.: occurencemax, occurencemin, occurrence
 * <p/>
 * <br/>Let N be the number of variables of the VARIABLES collection assigned to value VALUE;
 * <br/>Enforce condition NRELOPLIMIT to hold.
 * <br/><a href="http://www.emn.fr/z-info/sdemasse/gccat/Ccount.html">count in GCCAT</a>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public class Count extends IntConstraint<IntVar> {

    public final boolean leq;    // >=
    public final boolean geq;    // <=
    private final int occval;

    public Count(int value, IntVar[] vars, Operator relop, IntVar limit, Solver solver) {
        super(ArrayUtils.append(vars, new IntVar[]{limit}), solver);
        this.occval = value;
        switch (relop) {
            case GE:
                leq = true;
                geq = false;
                break;
            case LE:
                leq = false;
                geq = true;
                break;
            case EQ:
                leq = true;
                geq = true;
                break;
            default:
                throw new SolverException("Unexpected operator for Count");
        }
        setPropagators(new PropCount(value, this.vars, leq, geq, solver, this));
    }

    public Count(int value, IntVar[] vars, Operator relop, int limit, Solver solver) {
        super(ArrayUtils.append(vars, new IntVar[]{Views.fixed(limit, solver)}), solver);
        this.occval = value;
        switch (relop) {
            case GE:
                leq = true;
                geq = false;
                break;
            case LE:
                leq = false;
                geq = true;
                break;
            case EQ:
                leq = true;
                geq = true;
                break;
            default:
                throw new SolverException("Unexpected operator for Count");
        }
        //CPRU  double to simulate idempotency
        setPropagators(new PropCount(value, this.vars, leq, geq, solver, this),
                new PropCount(value, this.vars, leq, geq, solver, this));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        int nbVars = vars.length - 1;
        int cptVal = 0;
        for (int i = 0; i < nbVars; i++) {
            if (tuple[i] == occval) cptVal++;
        }
        if (leq & geq)
            return ESat.eval(cptVal == tuple[nbVars]);
        else if (leq)
            return ESat.eval(cptVal >= tuple[nbVars]);
        else
            return ESat.eval(cptVal <= tuple[nbVars]);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("occur([");
        for (int i = 0; i < vars.length - 2; i++) {
            s.append(vars[i]).append(",");
        }
        s.append(vars[vars.length - 2]).append("], ").append(occval).append(")");
        if (leq && geq)
            s.append(" = ");
        else if (leq)
            s.append(" >= ");
        else
            s.append(" <= ");
        s.append(vars[vars.length - 1]);
        return s.toString();
    }
}
