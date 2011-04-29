/**
 * Copyright (c) 1999-2010, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.nary.cnf;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.nary.cnf.PropClause;
import solver.constraints.propagators.nary.cnf.PropFalse;
import solver.constraints.propagators.nary.cnf.PropTrue;
import solver.variables.BoolVar;
import solver.variables.EventType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22 nov. 2010
 */
public class ConjunctiveNormalForm extends IntConstraint<BoolVar> {

    HashMap<BoolVar, HashSet<PropClause>> v2p = new HashMap<BoolVar, HashSet<PropClause>>();

    static BoolVar[] nonReundantBoolVars(ALogicTree tree) {
        LinkedHashSet<BoolVar> nonRedundantBs = new LinkedHashSet<BoolVar>();
        BoolVar[] cvars = tree.flattenBoolVar();
        for (int j = 0; j < cvars.length; j++) {
            if (!nonRedundantBs.contains(cvars[j])) {
                nonRedundantBs.add(cvars[j]);
            }
        }
        return nonRedundantBs.toArray(new BoolVar[nonRedundantBs.size()]);
    }


    public ConjunctiveNormalForm(ALogicTree tree, Solver solver) {
        this(tree, solver, PropagatorPriority.LINEAR);
    }


    public ConjunctiveNormalForm(ALogicTree tree, Solver solver, PropagatorPriority storeThreshold) {
        super(nonReundantBoolVars(tree), solver, storeThreshold);

        tree = LogicTreeToolBox.toCNF(tree);

        if (Singleton.TRUE.equals(tree)) {
            setPropagators(new PropTrue(solver.getEnvironment(), this));
        } else if (Singleton.FALSE.equals(tree)) {
            setPropagators(new PropFalse(solver.getEnvironment(), this));
        } else {

            ALogicTree[] clauses;
            if (tree.is(ALogicTree.Operator.AND)) {
                clauses = tree.getChildren();
            } else {
                clauses = new ALogicTree[]{tree};
            }
            // init internal structures
            PropClause[] propClauses = new PropClause[clauses.length];
            for (int i = 0; i < clauses.length; i++) {
                ALogicTree clause = clauses[i];
                // create the propagator, based on the i^th clause
                propClauses[i] = new PropClause(clause, solver.getEnvironment(), this);
                // create the link between the variables and the propagator,
                // required for #filterOnView() step
                BoolVar[] bvars = clause.flattenBoolVar();
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
            setPropagators(propClauses);
            // the propagator does not react on bound events, but the constraint has to simulate them.
            // (convertion from bound events to remaval events).
            setPropagationConditions(EventType.ALL_MASK());
        }
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        return null;
    }

    @Override
    public ESat isSatisfied() {
        ESat so = ESat.UNDEFINED;
        for(int i = 0 ; i < propagators.length; i++){
            so = propagators[i].isEntailed();
            if(!so.equals(ESat.TRUE)){
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
