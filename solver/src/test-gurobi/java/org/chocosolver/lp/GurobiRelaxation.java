/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import com.gurobi.gurobi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Gurobi-backed {@link ILinearRelaxation}.
 * <p>
 * Gurobi optimises in the sense given to {@link #setObjective(int, boolean)}, so the objective value and the
 * reduced costs are already reported in the natural sense. Constraints are accumulated as {@link GRBLinExpr}
 * and materialised on the model lazily at the first {@link #solve()} (Gurobi snapshots the expression when
 * the constraint is added, so the row must be fully populated first).
 */
public class GurobiRelaxation implements ILinearRelaxation {

    private static final class PendingRow {
        final GRBLinExpr expr;
        final char sense;
        final double rhs;

        PendingRow(GRBLinExpr expr, char sense, double rhs) {
            this.expr = expr;
            this.sense = sense;
            this.rhs = rhs;
        }
    }

    private final GRBModel model;
    private final GRBVar[] vars;
    private final List<PendingRow> pending = new ArrayList<>();
    private int nextConstr = 0;

    public GurobiRelaxation(int nbVars) {
        try {
            GRBEnv env = new GRBEnv(true);
            env.set(GRB.IntParam.OutputFlag, 0);
            env.set(GRB.IntParam.LogToConsole, 0);
            env.start();
            model = new GRBModel(env);
            vars = new GRBVar[nbVars];
            for (int i = 0; i < nbVars; i++) {
                vars[i] = model.addVar(0., GRB.INFINITY, 0., GRB.CONTINUOUS, "x_" + i);
            }
            model.update();
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setObjective(int var, boolean maximise) {
        try {
            GRBLinExpr expr = new GRBLinExpr();
            expr.addTerm(1., vars[var]);
            model.setObjective(expr, maximise ? GRB.MAXIMIZE : GRB.MINIMIZE);
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RowMutator addEqualityConstraint(double rhs) {
        GRBLinExpr expr = new GRBLinExpr();
        pending.add(new PendingRow(expr, GRB.EQUAL, rhs));
        return (column, value) -> expr.addTerm(value, vars[column]);
    }

    @Override
    public RowMutator addInequalityConstraint(double rhs) {
        GRBLinExpr expr = new GRBLinExpr();
        pending.add(new PendingRow(expr, GRB.LESS_EQUAL, rhs));
        return (column, value) -> expr.addTerm(value, vars[column]);
    }

    @Override
    public void updateRange(int var, int lower, int upper) {
        try {
            vars[var].set(GRB.DoubleAttr.LB, lower);
            vars[var].set(GRB.DoubleAttr.UB, upper);
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Status solve() {
        try {
            if (!pending.isEmpty()) {
                for (PendingRow row : pending) {
                    model.addConstr(row.expr, row.sense, row.rhs, "c_" + nextConstr++);
                }
                pending.clear();
                model.update();
            }
            model.optimize();
            int status = model.get(GRB.IntAttr.Status);
            switch (status) {
                case GRB.Status.OPTIMAL:
                    return Status.OPTIMAL;
                case GRB.Status.INFEASIBLE:
                    return Status.INFEASIBLE;
                default:
                    // sub-optimal, interrupted, infeasible-or-unbounded...: nothing is proven
                    return Status.UNKNOWN;
            }
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getObjectiveValue() {
        try {
            return model.get(GRB.DoubleAttr.ObjVal);
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double[] getReducedCosts() {
        try {
            return model.get(GRB.DoubleAttr.RC, vars);
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double[] getSolution() {
        try {
            return model.get(GRB.DoubleAttr.X, vars);
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }
}
