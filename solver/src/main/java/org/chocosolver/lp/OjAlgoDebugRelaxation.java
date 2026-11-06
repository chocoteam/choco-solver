/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import gnu.trove.map.hash.TIntDoubleHashMap;
import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Access1D;

/**
 * {@link ExpressionsBasedModel}-backed {@link ILinearRelaxation}, intended for debugging and model export
 * rather than performance.
 * <p>
 * Unlike {@link OjAlgoRelaxation}, which builds the low-level always-minimisation solver, this one builds
 * ojAlgo's high-level model: variables and constraints carry names, the optimisation direction is native (so
 * {@link #getObjectiveValue()} and {@link #getReducedCosts()} are already in the natural sense), and the
 * model can be inspected via {@link #toString()} or exported with {@link #writeTo(File)} /
 * {@link #getModel()}.
 * <p>
 * Use it by selecting {@link Solver#OJALGO_EBM}, then casting the relaxation back to this type to reach the
 * export methods.
 */
public class OjAlgoDebugRelaxation implements ILinearRelaxation {

    private static final Optimisation.Environment ENV = Optimisation.newEnvironment();
    private static final Optimisation.Options OPTIONS = new Optimisation.Options();

    static {
        // Clear to make it behave more like OjAlgoRelaxation,
        // but it's still not exactly the same.
        ENV.clearPresolvers();
        OPTIONS.linear().dual();
    }

    private boolean maximise = false;
    private final ExpressionsBasedModel model;
    private final double[] reducedCosts;
    private final double[] solution;
    private transient Optimisation.Result result = null;
    private final Variable[] variables;

    /**
     * @param nbVars the number of variables (columns)
     */
    public OjAlgoDebugRelaxation(int nbVars) {
        super();
        this.model = ENV.newModel(OPTIONS);
        this.variables = new Variable[nbVars];
        for (int j = 0; j < nbVars; j++) {
            this.variables[j] = model.addVariable();
        }
        this.reducedCosts = new double[nbVars];
        this.solution = new double[nbVars];
    }

    @Override
    public RowMutator addEqualityConstraint(double rhs) {
        Expression expr = model.addExpression().level(rhs);
        return accumulate(expr);
    }

    @Override
    public RowMutator addInequalityConstraint(double rhs) {
        Expression expr = model.addExpression().upper(rhs);
        return accumulate(expr);
    }

    private static RowMutator accumulate(Expression expr) {
        TIntDoubleHashMap coefficients = new TIntDoubleHashMap();
        return (column, value) -> expr.set(column, coefficients.adjustOrPutValue(column, value, value));
    }

    /**
     * @return the underlying ojAlgo model, for inspection and debugging
     */
    public ExpressionsBasedModel getModel() {
        return model;
    }

    @Override
    public double getObjectiveValue() {
        if (result == null) {
            throw new IllegalStateException();
        }
        return result.getValue();
    }

    @Override
    public double[] getReducedCosts() {
        if (result == null) {
            throw new IllegalStateException();
        }
        Optional<Supplier<Access1D<?>>> reducedGradient = result.getReducedGradient();
        if (reducedGradient.isPresent()) {
            Access1D<?> gradient = reducedGradient.get().get();
            for (int j = 0; j < reducedCosts.length; j++) {
                reducedCosts[j] = gradient.doubleValue(j);
            }
        } else {
            Arrays.fill(reducedCosts, 0.);
        }
        return reducedCosts;
    }

    @Override
    public double[] getSolution() {
        if (result == null) {
            throw new IllegalStateException();
        }
        for (int j = 0; j < solution.length; j++) {
            solution[j] = result.doubleValue(j);
        }
        return solution;
    }

    @Override
    public void setObjective(int var, boolean maximise) {
        this.maximise = maximise;
        variables[var].weight(BigDecimal.ONE);
    }

    @Override
    public Status solve() {
        ExpressionsBasedModel snapshot = model.snapshot();
        result = maximise ? snapshot.maximise() : snapshot.minimise();
        Optimisation.State state = result.getState();
        if (state == Optimisation.State.INFEASIBLE) {
            return Status.INFEASIBLE;
        }
        if (state == Optimisation.State.OPTIMAL || state == Optimisation.State.DISTINCT) {
            return Status.OPTIMAL;
        }
        return Status.UNKNOWN;
    }
 
    @Override
    public String toString() {
        return model.toString();
    }

    @Override
    public void updateRange(int var, int lower, int upper) {
        variables[var].lower(lower).upper(upper);
    }

    /**
     * Write the model to {@code file} in ojAlgo's EBM file format.
     *
     * @param file the destination file
     */
    public void writeTo(File file) {
        model.writeTo(file);
    }
}
