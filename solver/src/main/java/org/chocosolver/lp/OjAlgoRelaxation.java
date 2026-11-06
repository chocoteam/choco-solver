/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.ojalgo.equation.Equation;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.structure.Mutate1D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ojAlgo-backed {@link ILinearRelaxation}.
 * <ul>
 * <li>The LP is always built as a minimisation on a {@link LinearSolver.Builder}; for a maximisation the
 * objective coefficient is negated and the reported objective value and reduced costs are negated back into
 * the natural sense.
 * <li>Equality and inequality constraints are kept separate.
 * <li>The low-level solver is built once; subsequent range updates are applied to the solver instance in
 * place when possible, otherwise the solver is rebuilt on the next solve.
 * </ul>
 */
public class OjAlgoRelaxation implements ILinearRelaxation {

    /**
     * System property to turn on the confirmation of infeasible verdicts (set it to {@code true}); off by default.
     *
     * @see #solve()
     */
    public static final String CONFIRM_INFEASIBLE_PROPERTY = "choco.lp.ojalgo.confirmInfeasible";

    /**
     * System property to compare every solve with a solver built from scratch (set it to {@code true}), the
     * same as the one that confirms infeasible verdicts.
     * Meant for qualifying an ojAlgo version, not for production: it adds one solve per call.
     *
     * @see #getDiagnostics()
     */
    public static final String DIAGNOSE_PROPERTY = "choco.lp.ojalgo.diagnose";

    /**
     * System property to set the time limit of a single solve, in milliseconds (default is 1000). A solve
     * that hits this limit ends as {@link Status#UNKNOWN}.
     */
    public static final String SOLVE_TIME_LIMIT_PROPERTY = "choco.lp.ojalgo.solveTimeLimit";

    /**
     * System property to set the iteration limit of a single solve, as a multiple of the size of the problem
     * (its number of variables plus its number of constraints); default is 10, and 0 means no limit. A solve
     * that hits this limit ends as {@link Status#UNKNOWN}. Unlike the time limit, which is only a safety net,
     * this one keeps the search deterministic.
     */
    public static final String SOLVE_ITERATION_FACTOR_PROPERTY = "choco.lp.ojalgo.solveIterationFactor";

    /**
     * System property to ignore the infeasibility certificates (set it to {@code false}), see
     * {@link #getInfeasibilityCertificate()}.
     */
    public static final String CERTIFICATE_PROPERTY = "choco.lp.ojalgo.certificate";

    /**
     * System property to select the store of the main solver: {@code true} for the sparse one (default),
     * {@code false} for the dense one, {@code auto} to let ojAlgo decide from the size of the problem.
     */
    public static final String SPARSE_PROPERTY = "choco.lp.ojalgo.sparse";

    /**
     * Options of the main solver.
     */
    private final Optimisation.Options options = new Optimisation.Options();
    /**
     * Above this number of entries, a dense tableau is not built: it would take {@code 8} bytes per entry.
     */
    static final long DENSE_TABLEAU_LIMIT = 4_000_000L;

    /**
     * Relative tolerance when checking an infeasibility certificate.
     */
    private static final double CERTIFICATE_TOLERANCE = 1e-9;

    /**
     * Options of the solver used to confirm an infeasible verdict: same algorithm, built from scratch, with a
     * dense store when the problem is small enough (see {@link #fitsDense(long, long)}).
     */
    private final Optimisation.Options confirmOptions = new Optimisation.Options();
    private final int iterationFactor = Integer.getInteger(SOLVE_ITERATION_FACTOR_PROPERTY, 10);

    private final int nbVars;
    /**
     * The rows of the LP, in the order the solver numbers its dual multipliers: inequalities, then equalities.
     */
    private final List<Row> inequalities = new ArrayList<>();
    private final List<Row> equalities = new ArrayList<>();
    /**
     * The solvers that concluded {@link Status#INFEASIBLE} on the last solve, which may provide a certificate.
     */
    private final List<LinearSolver> infeasibleSolvers = new ArrayList<>();
    private final boolean useCertificates =
            Boolean.parseBoolean(System.getProperty(CERTIFICATE_PROPERTY, "true"));
    private long nbCertificates = 0;
    private long nbRejectedCertificates = 0;
    private final LinearSolver.Builder builder;
    private final double[] reducedCosts;
    private final double[] solution;
    private final int[] currentLower;
    private final int[] currentUpper;
    private boolean maximise = false;
    private transient Optimisation.Result result = null;
    private transient LinearSolver solver = null;
    /**
     * The solver that produced {@link #result}: {@link #solver}, unless the verdict was overturned.
     */
    private transient LinearSolver resultSolver = null;
    private final boolean confirmInfeasible =
            Boolean.parseBoolean(System.getProperty(CONFIRM_INFEASIBLE_PROPERTY, "false"));
    private int nbOverturned = 0;
    private final boolean diagnose = Boolean.parseBoolean(System.getProperty(DIAGNOSE_PROPERTY, "false"));
    private long nbSolves = 0;
    private long nbUnknown = 0;
    private long nbWrongFeasible = 0;
    private long nbWrongValues = 0;

    /**
     * @param nbVars the number of variables (columns)
     */
    public OjAlgoRelaxation(int nbVars) {
        super();
        this.nbVars = nbVars;
        this.reducedCosts = new double[nbVars];
        this.solution = new double[nbVars];
        this.currentLower = new int[nbVars];
        this.currentUpper = new int[nbVars];
        Arrays.fill(currentLower, Integer.MIN_VALUE);
        Arrays.fill(currentUpper, Integer.MAX_VALUE);
        this.builder = LinearSolver.newBuilder(new double[nbVars]);
        long timeLimit = Long.getLong(SOLVE_TIME_LIMIT_PROPERTY, 1000L);
        options.linear().dual();
        String sparse = System.getProperty(SPARSE_PROPERTY, "true");
        options.sparse = "auto".equals(sparse) ? null : Boolean.valueOf(sparse);
        options.time_abort = timeLimit;
        confirmOptions.linear().dual();
        confirmOptions.sparse = false;
        confirmOptions.time_abort = timeLimit;
    }

    @Override
    public void setObjective(int var, boolean maximise) {
        this.maximise = maximise;
        builder.objective(var, maximise ? -1.0 : 1.0);
        solver = null;
        result = null;
    }

    @Override
    public RowMutator addEqualityConstraint(double rhs) {
        Mutate1D body = builder.equality(rhs);
        solver = null;
        result = null;
        return accumulate(body, rhs, equalities);
    }

    @Override
    public RowMutator addInequalityConstraint(double rhs) {
        Mutate1D body = builder.inequality(rhs);
        solver = null;
        result = null;
        return accumulate(body, rhs, inequalities);
    }

    /**
     * The rows of the builder can only be set: keep the coefficients of the row to add them up. They are also
     * kept to check the infeasibility certificates.
     */
    private static RowMutator accumulate(Mutate1D body, double rhs, List<Row> rows) {
        Row row = new Row(rhs);
        rows.add(row);
        return (column, value) -> body.set(column, row.coefficients.adjustOrPutValue(column, value, value));
    }

    /**
     * A row of the LP, as the builder was given it.
     */
    private static final class Row {
        final TIntDoubleHashMap coefficients = new TIntDoubleHashMap();
        final double rhs;

        Row(double rhs) {
            this.rhs = rhs;
        }
    }

    @Override
    public void updateRange(int var, int lower, int upper) {
        if (lower == currentLower[var] && upper == currentUpper[var]) {
            return;
        }
        currentLower[var] = lower;
        currentUpper[var] = upper;
        builder.range(var, lower, upper);
        if (solver != null && !solver.updateRange(var, lower, upper)) {
            solver = null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Before version 57.6.0, the dual simplex of ojAlgo could wrongly report an infeasible problem on a reused
     * solver instance: after ranges were relaxed in place
     * (<a href="https://github.com/optimatika/ojAlgo/issues/699">ojAlgo #699</a>, fixed in 57.5.0), and, with
     * the sparse store, after ranges were tightened in place
     * (<a href="https://github.com/optimatika/ojAlgo/issues/704">ojAlgo #704</a>). As a safeguard against such a
     * verdict, which the caller turns into a failure, {@link #CONFIRM_INFEASIBLE_PROPERTY} confirms it with a solver
     * built from scratch, with a dense store unless the problem is too large for it (then a sparse one). When that
     * one finds a solution, its result is returned instead and the main solver is rebuilt on the next call. This
     * costs one additional solve per infeasible verdict; it is off by default.
     * <p>
     * Each solve is limited in iterations and in time, see {@link #SOLVE_ITERATION_FACTOR_PROPERTY} and
     * {@link #SOLVE_TIME_LIMIT_PROPERTY}.
     */
    @Override
    public Status solve() {
        if (solver == null) {
            if (iterationFactor > 0) {
                int size = nbVars + builder.countEqualityConstraints() + builder.countInequalityConstraints();
                options.iterations_abort = iterationFactor * size;
                confirmOptions.iterations_abort = iterationFactor * size;
            }
            confirmOptions.sparse = !fitsDense(
                    builder.countEqualityConstraints() + builder.countInequalityConstraints(), nbVars);
            solver = builder.build(options);
        }
        result = solver.solve();
        resultSolver = solver;
        infeasibleSolvers.clear();
        nbSolves++;
        Status status = status(result);
        if (diagnose && status == Status.OPTIMAL) {
            Optimisation.Result reference = builder.build(confirmOptions).solve();
            Status expected = status(reference);
            if (expected == Status.INFEASIBLE) {
                nbWrongFeasible++;
            } else if (expected == Status.OPTIMAL && Math.abs(reference.getValue() - result.getValue())
                    > 1e-6 * Math.max(1., Math.abs(reference.getValue()))) {
                nbWrongValues++;
            }
        }
        if (status == Status.INFEASIBLE) {
            infeasibleSolvers.add(solver);
        }
        if (confirmInfeasible && status == Status.INFEASIBLE) {
            LinearSolver confirmation = builder.build(confirmOptions);
            Optimisation.Result confirmed = confirmation.solve();
            Status confirmedStatus = status(confirmed);
            if (confirmedStatus == Status.INFEASIBLE) {
                infeasibleSolvers.add(confirmation);
            }
            if (confirmedStatus != Status.INFEASIBLE) {
                // the verdict is not confirmed: the state of the main solver cannot be trusted any more
                infeasibleSolvers.clear();
                solver = null;
                if (confirmedStatus == Status.OPTIMAL) {
                    nbOverturned++;
                    result = confirmed;
                    resultSolver = confirmation;
                }
                status = confirmedStatus;
            }
        }
        if (status == Status.UNKNOWN) {
            // an interrupted or failed solver is not reused
            nbUnknown++;
            solver = null;
        }
        return status;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The certificate comes from the dual simplex of ojAlgo, which returns a Farkas dual ray when it concludes
     * that the LP is infeasible (from version 57.5.0 on; before, the values it returns are not a certificate). It is not trusted as is: the row multipliers {@code y} are read, the aggregated row
     * {@code g = yᵀA} and its right-hand side {@code yᵀb} are computed here from the rows of the LP, and the
     * certificate is only returned if {@code y} is non-negative on the inequalities and the smallest value of
     * {@code g·x} within the current ranges exceeds {@code yᵀb}.
     */
    @Override
    public double[] getInfeasibilityCertificate() {
        if (!useCertificates) {
            return null;
        }
        for (LinearSolver candidate : infeasibleSolvers) {
            double[] g = checkCertificate(candidate);
            if (g != null) {
                nbCertificates++;
                return g;
            }
        }
        nbRejectedCertificates++;
        return null;
    }

    /**
     * @return the aggregated row of the certificate of that solver, or {@code null} if it does not prove that the
     *         LP is infeasible
     */
    private double[] checkCertificate(LinearSolver candidate) {
        int nbRows = inequalities.size() + equalities.size();
        double[] y = new double[nbRows];
        try {
            for (int i = 0; i < nbRows; i++) {
                y[i] = candidate.getDualMultiplier(i);
                if (Double.isNaN(y[i]) || Double.isInfinite(y[i])) {
                    return null;
                }
            }
        } catch (RuntimeException e) {
            return null;
        }
        double[] g = new double[nbVars];
        double rhs = 0;
        double scale = 0;
        for (int i = 0; i < nbRows; i++) {
            Row row = i < inequalities.size() ? inequalities.get(i) : equalities.get(i - inequalities.size());
            double yi = y[i];
            if (i < inequalities.size()) {
                if (yi < 0) {
                    if (yi < -CERTIFICATE_TOLERANCE * Math.max(1., Math.abs(yi))) {
                        return null;
                    }
                    // a negligible negative multiplier: dropping the row keeps the aggregation valid
                    continue;
                }
            }
            if (yi == 0) {
                continue;
            }
            for (TIntDoubleIterator it = row.coefficients.iterator(); it.hasNext(); ) {
                it.advance();
                g[it.key()] += yi * it.value();
            }
            rhs += yi * row.rhs;
            scale += Math.abs(yi * row.rhs);
        }
        double min = 0;
        for (int j = 0; j < nbVars; j++) {
            if (g[j] > 0) {
                if (currentLower[j] == Integer.MIN_VALUE) {
                    return null;
                }
                min += g[j] * currentLower[j];
                scale += Math.abs(g[j] * currentLower[j]);
            } else if (g[j] < 0) {
                if (currentUpper[j] == Integer.MAX_VALUE) {
                    return null;
                }
                min += g[j] * currentUpper[j];
                scale += Math.abs(g[j] * currentUpper[j]);
            }
        }
        return min > rhs + CERTIFICATE_TOLERANCE * Math.max(1., scale) ? g : null;
    }

    /**
     * @return the number of infeasible LPs explained by a certificate, and of those for which no valid certificate
     *         was found
     */
    public long[] getCertificateCounts() {
        return new long[]{nbCertificates, nbRejectedCertificates};
    }

    /**
     * @param rows    the number of constraints
     * @param columns the number of variables
     * @return whether a dense tableau of that problem stays below {@link #DENSE_TABLEAU_LIMIT} entries; the
     *         tableau has one row per constraint plus the objective, and one column per variable and per slack
     */
    static boolean fitsDense(long rows, long columns) {
        return (rows + 1) * (columns + rows + 1) <= DENSE_TABLEAU_LIMIT;
    }

    /**
     * Only a point proven optimal gives a valid bound: a feasible point the solver stopped on does not.
     */
    private static Status status(Optimisation.Result result) {
        Optimisation.State state = result.getState();
        if (state == Optimisation.State.INFEASIBLE) {
            return Status.INFEASIBLE;
        }
        if (state == Optimisation.State.OPTIMAL || state == Optimisation.State.DISTINCT) {
            return Status.OPTIMAL;
        }
        return Status.UNKNOWN;
    }

    /**
     * @return the number of infeasible verdicts overturned by the confirmation solver since creation
     */
    public int getNbOverturnedVerdicts() {
        return nbOverturned;
    }

    /**
     * A one-line summary of how the main solver behaves and compares with a solver built from scratch:
     * the number of solves, of solves with an unknown outcome, of infeasible verdicts overturned and, when {@link #DIAGNOSE_PROPERTY} is set, of feasible
     * verdicts the reference solver contradicts and of optimal values that differ.
     *
     * @return the summary
     */
    public String getDiagnostics() {
        return String.format("solves=%d, unknown=%d, overturned=%d%s", nbSolves, nbUnknown, nbOverturned,
                diagnose ? String.format(", wrongFeasible=%d, wrongValues=%d", nbWrongFeasible, nbWrongValues)
                        : "");
    }

    @Override
    public double getObjectiveValue() {
        if (result == null) {
            throw new IllegalStateException();
        }
        return maximise ? -result.getValue() : result.getValue();
    }

    @Override
    public double[] getReducedCosts() {
        if (resultSolver == null || result == null) {
            throw new IllegalStateException();
        }
        for (int j = 0; j < nbVars; j++) {
            double reducedGradient = resultSolver.getReducedGradient(j);
            reducedCosts[j] = maximise ? -reducedGradient : reducedGradient;
        }
        return reducedCosts;
    }

    @Override
    public double[] getSolution() {
        if (result == null) {
            throw new IllegalStateException();
        }
        // The result also holds the slack variables, appended after the problem variables - skip those.
        for (int j = 0; j < nbVars; j++) {
            solution[j] = result.doubleValue(j);
        }
        return solution;
    }

    Collection<Equation> generateCutCandidates(double fractionality) {
        if (resultSolver != null && result != null) {
            boolean[] integer = new boolean[nbVars];
            Arrays.fill(integer, true);
            return resultSolver.generateCutCandidates(fractionality, integer);
        }
        return Collections.emptyList();
    }
}
