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

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import memory.IStateInt;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.EventType;
import util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/13
 */
public class PropSat extends Propagator<BoolVar> {

    SatSolver sat_;
    TObjectIntHashMap<BoolVar> indices_;

    IStateInt sat_trail_;

    TIntList early_deductions_;

    public PropSat(Solver solver) {
		// this propagator initially has no variable
        super(new BoolVar[]{solver.ONE}, PropagatorPriority.VERY_SLOW, true);// adds solver.ONE to fit to the super constructor
		this.vars = new BoolVar[0];	// erase solver.ONE from the variable scope

        this.indices_ = new TObjectIntHashMap<BoolVar>();
        sat_ = new SatSolver();
        early_deductions_ = new TIntArrayList();
        sat_trail_ = solver.getEnvironment().makeInt();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((EventType.FULL_PROPAGATION.mask & evtmask) != 0) {
            sat_.initPropagator();
            applyEarlyDeductions();
            for (int i = 0; i < vars.length; ++i) {
                BoolVar var = vars[i];
                if (var.isInstantiated()) {
                    VariableBound(i);
                }
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        VariableBound(idxVarInProp);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            for (SatSolver.Clause c : sat_.clauses) {
                int cnt = 0;
                for (int i = 0; i < c.size(); i++) {
                    int lit = c._g(i);
                    boolean sign = SatSolver.sign(lit);
                    int var = SatSolver.var(lit);
                    int val = vars[var].getValue();
                    if (val == (sign ? 0 : 1)) cnt++; // if the lit is ok
                    else break;
                }
                if (cnt == c.size()) return ESat.FALSE;
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    public SatSolver getSatSolver() {
        return sat_;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public int Literal(BoolVar expr) {
        boolean expr_negated = false;
        if (indices_.containsKey(expr)) {
            return SatSolver.makeLiteral(indices_.get(expr), !expr_negated);
        } else {
            int var = sat_.newVariable();
            assert (vars.length == var);
            addVariable(expr);
            indices_.put(expr, var);
            return SatSolver.makeLiteral(var, !expr_negated);
        }
    }

    void VariableBound(int index) throws ContradictionException {
        if (sat_trail_.get() < sat_.trailMarker()) {
            sat_.cancelUntil(sat_trail_.get());
            assert (sat_trail_.get() == sat_.trailMarker());
        }
        int var = index;
        boolean new_value = vars[index].getValue() != 0;
        int lit = SatSolver.makeLiteral(var, new_value);
        if (!sat_.propagateOneLiteral(lit)) {
            this.contradiction(null, "clause unsat");
        } else {
            sat_trail_.set(sat_.trailMarker());
            for (int i = 0; i < sat_.touched_variables_.size(); ++i) {
                lit = sat_.touched_variables_.get(i);
                var = SatSolver.var(lit);
                boolean assigned_bool = SatSolver.sign(lit);
//                demons_[var.value()].inhibit(solver());
                vars[var].instantiateTo(assigned_bool ? 1 : 0, this);
            }
        }
    }


    // Add a clause to the solver, clears the vector.
    public boolean addClause(TIntList lits) {
        boolean result = sat_.addClause(lits);
        storeEarlyDeductions();
        return result;
    }

    // Add the empty clause, making the solver contradictory.
    public boolean addEmptyClause() {
        return sat_.addEmptyClause();
    }

    // Add a unit clause to the solver.
    public boolean addClause(int p) {
        boolean result = sat_.addClause(p);
        storeEarlyDeductions();
        return result;
    }

    // Add a binary clause to the solver.
    public boolean addClause(int p, int q) {
        boolean result = sat_.addClause(p, q);
        storeEarlyDeductions();
        return result;
    }

    // Add a ternary clause to the solver.
    public boolean addClause(int p, int q, int r) {
        boolean result = sat_.addClause(p, q, r);
        storeEarlyDeductions();
        return result;
    }


    private void storeEarlyDeductions() {
        for (int i = 0; i < sat_.touched_variables_.size(); ++i) {
            int lit = sat_.touched_variables_.get(i);
            early_deductions_.add(lit);
        }
        sat_.touched_variables_.clear();
    }

    void applyEarlyDeductions() throws ContradictionException {
        for (int i = 0; i < early_deductions_.size(); ++i) {
            int lit = early_deductions_.get(i);
            int var = SatSolver.var(lit);
            boolean assigned_bool = SatSolver.sign(lit);
//            demons_[var.value()].inhibit(solver());
            vars[var].instantiateTo(assigned_bool ? 1 : 0, this);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void declareVariable(PropSat sat, BoolVar var) {
        //CHECK(sat.Check(var));
        sat.Literal(var);
    }

}
