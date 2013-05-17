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

package solver.constraints.nary.cnf;

import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.nary.cnf.PropClause;
import solver.constraints.propagators.nary.cnf.PropFalse;
import solver.constraints.propagators.nary.cnf.PropTrue;
import solver.variables.BoolVar;
import util.ESat;
import util.VariableUtilities;

import java.util.HashMap;
import java.util.HashSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22 nov. 2010
 */
public class ConjunctiveNormalForm extends IntConstraint<BoolVar> {

    HashMap<BoolVar, HashSet<PropClause>> v2p = new HashMap<BoolVar, HashSet<PropClause>>();

    protected ConjunctiveNormalForm(BoolVar[] nonRedundantVars, LogOp tree, Solver solver) {
        super(nonRedundantVars, solver);
        setPropagators(build(tree, solver));

    }

    public ConjunctiveNormalForm(LogOp tree, Solver solver) {
        this(VariableUtilities.nonReundantVars(tree.flattenBoolVar()), tree, solver);
    }

    private PropClause[] build(LogOp logOp, Solver solver) {
        ILogical tree = LogicTreeToolBox.toCNF(logOp, solver);

        if (solver.ONE.equals(tree)) {
            return new PropClause[]{new PropTrue(solver)};
        } else if (solver.ZERO.equals(tree)) {
            return new PropClause[]{new PropFalse(solver)};
        } else {

            ILogical[] clauses;
            if (!tree.isLit() && ((LogOp) tree).is(LogOp.Operator.AND)) {
                clauses = ((LogOp) tree).getChildren();
            } else {
                clauses = new ILogical[]{tree};
            }
            // init internal structures
            PropClause[] propClauses = new PropClause[clauses.length];
            for (int i = 0; i < clauses.length; i++) {
                ILogical clause = clauses[i];
                if (clause.isLit()) {
                    BoolVar bv = (BoolVar) clause;
                    propClauses[i] = new PropClause(bv, solver);
                } else {
                    LogOp n = (LogOp) clause;
//                    int nbPos = 0;
                    // create the propagator, based on the i^th clause
                    // create the link between the variables and the propagator,
                    // required for #propagate() step
                    BoolVar[] bvars = n.flattenBoolVar();
//                    for (int j = 0; j < bvars.length; j++) {
//                        BoolVar v = bvars[j];
//                        nbPos += v.isNot() ? 0 : 1;
//                    }
                    propClauses[i] = new PropClause(n, solver);
                    for (int j = 0; j < bvars.length; j++) {
                        BoolVar v = bvars[j];
                        HashSet<PropClause> indices = v2p.get(v);
                        if (indices == null) {
                            indices = new HashSet<PropClause>();
                            v2p.put(v, indices);
                        }
                        indices.add(propClauses[i]);
                    }

                }
            }
            return propClauses;
        }
    }


    @Override
    public ESat isSatisfied(int[] tuple) {
        return null;
    }

    @Override
    public ESat isSatisfied() {
        ESat so = ESat.UNDEFINED;
        for (int i = 0; i < propagators.length; i++) {
            so = propagators[i].isEntailed();
            if (!so.equals(ESat.TRUE)) {
                return so;
            }
        }
        return so;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append('(');
        for (int p = 0; p < propagators.length; p++) {
            st.append(propagators[p].toString()).append(") and (");
        }
        st.replace(st.length() - 6, st.length(), "");
        return st.toString();
    }
}
