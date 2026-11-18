/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.constraints.*;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A global propagator that solves a linear relaxation of the model.
 * <p>
 * The relaxation is made of the linear forms the propagators give of themselves, see
 * {@link Propagator#addLinearExpression(ILinearRelaxation, TObjectIntHashMap)}; reified and half-reified
 * constraints are left out (a half-reified propagator posted on its own gives its own linear form). Its columns are
 * the integer variables of these propagators, with their current bounds, and its objective is the objective variable
 * of the model, if any. When the bounds change and the last LP solution no longer fits in them, the LP is solved
 * again, unless the backoff frequency skips it (see {@link #FREQUENCY_PROPERTY}):
 * <ul>
 * <li>an infeasible LP makes the propagator fail;</li>
 * <li>the optimal value of the LP bounds the objective variable;</li>
 * <li>the reduced costs, together with the best solution found so far, filter the bounds of the other
 * variables.</li>
 * </ul>
 * <p>
 * The LP is built once per resolution, at the root node. A constraint posted during the search is not added to it,
 * which is safe; if a constraint is unposted during the search, the LP is left aside until the next resolution.
 * Usages that are not tested with a linear relaxation: parallel portfolio, LNS, resuming a search stopped by a
 * limit, constraints posted temporarily ({@code Model#postTemp}).
 * <p>
 * Explanations, for lazy clause generation: the LP only sees the bounds of the variables, so the reasons are
 * made of bound literals only.
 * <ul>
 * <li>A bound on the objective, or a bound filtered with the reduced costs, is explained by the dual solution:
 * a variable with a non-zero reduced cost contributes the one bound it is sitting at, a variable with a zero
 * reduced cost contributes nothing (see {@link #dualProof(boolean, boolean[], boolean[], IntVar)}).</li>
 * <li>An infeasible LP is explained by the bounds its certificate relies on, when the backend provides one
 * (see {@link ILinearRelaxation#getInfeasibilityCertificate()}); otherwise, by both bounds of all the variables
 * of the LP.</li>
 * </ul>
 *
 * @author Charles Prud'homme
 * @since 03/03/2025
 */
@Explained(partial = true, comment = "without a certificate, an infeasible LP is explained by all the bounds")
public class PropSimplex extends Propagator<IntVar> {

    private static final Logger LOGGER = Logger.getLogger(PropSimplex.class.getName());

    /**
     * Factory for the relaxation backend. The relaxation itself is built lazily in {@link #initialize()},
     * once the number of variables is known.
     */
    private final IntFunction<ILinearRelaxation> solverFactory;
    /**
     * The relaxation, built on first {@link #initialize()}. Stays {@code null} when the model contains no
     * linear-relaxable variables.
     */
    private ILinearRelaxation linrel = null;
    /**
     * The column of each variable of the LP
     */
    private final TObjectIntHashMap<IntVar> add_var;

    /**
     * The objective variable in the linear program, or {@code null} when the LP is solved for feasibility only
     */
    private IntVar objective = null;
    /**
     * The variable of each column of the LP. The first {@code vars.length} ones are watched by this propagator.
     */
    private final List<IntVar> columns = new ArrayList<>();
    /**
     * Number of the last build of the LP, 0 when it has never been built
     */
    private int buildId = 0;
    /**
     * The number of the build that is valid for the current resolution, restored on backtrack
     */
    private final IStateInt builtHere;
    /**
     * {@link Model#getNbUnpostedCstrs()} when the LP was built
     */
    private int nbUnpostedCstrs;
    /**
     * Number of rows of the LP
     */
    private int nbRows = 0;
    /**
     * Last [LB, UB] asserted to the relaxation per variable (aligned with {@link #vars}). Lets
     * {@link #initialize()} re-assert only the variables whose bounds actually changed since the previous
     * propagation instead of all of them every time.
     */
    private int[] cachedLB = null;
    private int[] cachedUB = null;
    /**
     * The reduced costs of the last solve, or {@code null} if they have not been read yet
     */
    private double[] lastReducedCosts = null;
    /**
     * Store the last LP solution
     */
    private double[] lastSol = null;
    /**
     * Number of builds of the LP, of propagations that left the LP aside because a constraint was unposted during
     * the search, of propagations, of those that skipped the LP, of LP solves, of infeasible LPs, of LP solves with
     * an unknown outcome, of updates of the objective bound, of bounds filtered with the reduced costs, and time spent solving the LP.
     */
    private long nbBuilds = 0;
    private long nbBackedOff = 0;
    private long nbStale = 0;
    private long nbPropagations = 0;
    private long nbSkipped = 0;
    private long nbSolves = 0;
    private long nbInfeasible = 0;
    private long nbCertified = 0;
    /**
     * Number of bounds the certified infeasible LPs were explained by, out of the bounds of all their variables
     */
    private long nbCertifiedBounds = 0;
    private long nbCertifiedAllBounds = 0;
    private long nbUnknown = 0;
    private long nbObjectiveUpdates = 0;
    private long nbFilteredBounds = 0;
    private long solveNanos = 0;
    /**
     * System property to choose when the LP is solved: {@code backoff} (default), which skips an exponentially
     * growing number of propagations after each solve that deduces nothing (see {@link #BACKOFF_MAX_PROPERTY}), or
     * {@code always}, at each propagation where the bounds no longer contain its last solution.
     */
    public static final String FREQUENCY_PROPERTY = "choco.lp.frequency";
    /**
     * System property to set the largest number of propagations skipped in a row by the {@code backoff} frequency
     * (default is 1024).
     */
    public static final String BACKOFF_MAX_PROPERTY = "choco.lp.backoffMax";
    /**
     * With the {@code backoff} frequency: after a solve that deduces nothing, the next {@code interval - 1}
     * propagations do not solve the LP and the interval doubles, up to {@link #maxInterval}; after a solve that
     * deduces something, the interval is reset to 1. Skipping a solve is always safe, so this state is not restored
     * on backtrack.
     */
    private final boolean backoff = !"always".equals(System.getProperty(FREQUENCY_PROPERTY, "backoff"));
    private final int maxInterval = Integer.getInteger(BACKOFF_MAX_PROPERTY, 1024);
    private int interval = 1;
    private int countdown = 0;
    private long lastSolutionCount = -1;
    /**
     * Below this value, a reduced cost is considered as zero.
     */
    private static final double RC_EPSILON = 1e-9;
    /**
     * A value computed by the LP solver is trusted up to this absolute error, or to this relative error when it is
     * larger: an absolute tolerance alone is too tight for large values.
     */
    private static final double ABSOLUTE_TOLERANCE = 1e-8;
    private static final double RELATIVE_TOLERANCE = 1e-9;
    /**
     * For tests only: when set, is told about every explanation produced.
     */
    ExplanationObserver observer = null;

    /**
     * For tests only: receives every explanation produced by a {@link PropSimplex}.
     */
    interface ExplanationObserver {
        /**
         * @param vars     the variables of the LP
         * @param useLB    whether the current lower bound of each variable belongs to the premises
         * @param useUB    whether the current upper bound of each variable belongs to the premises
         * @param target   the variable whose bound is deduced, or {@code null} for a failure
         * @param lower    whether the deduced bound is a lower bound
         * @param newBound the deduced bound
         */
        void explained(IntVar[] vars, boolean[] useLB, boolean[] useUB, IntVar target, boolean lower, int newBound);
    }

    /**
     * Create a PropSimplex for one of the backends listed in {@link ILinearRelaxation.Solver}. The propagator is
     * not posted.
     *
     * @param model            the model
     * @param linearRelaxation the LP solver
     */
    public PropSimplex(Model model, ILinearRelaxation.Solver linearRelaxation) {
        this(model, linearRelaxation::create);
    }

    /**
     * Create a PropSimplex with a relaxation backend of its own, for instance one that is not listed in
     * {@link ILinearRelaxation.Solver}. The propagator is not posted.
     *
     * @param model   the model
     * @param factory creates a relaxation sized for the number of columns it is given
     */
    public PropSimplex(Model model, IntFunction<ILinearRelaxation> factory) {
        // this propagator initially has no variable
        // adds solver.ONE to fit to the super constructor
        super(new IntVar[]{model.boolVar(false)}, PropagatorPriority.VERY_SLOW, false, false);
        this.vars = new IntVar[0];
        // a variable without column must not be mistaken for the one of column 0
        this.add_var = new TObjectIntHashMap<>(16, 0.5f, -1);
        this.solverFactory = factory;
        this.builtHere = model.getEnvironment().makeInt(0);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Simplex declaration");
        }
    }

    /**
     * (Re)build the LP from the constraints currently posted. This is done once per resolution, at the root node:
     * the rows may depend on the current domains (see {@code PropAllDiffInst}), which are then facts, valid for
     * the whole search and in no need of an explanation. {@link #builtHere} is restored when a new resolution
     * starts, which triggers the next build.
     */
    private void build() {
        nbBuilds++;
        buildId++;
        builtHere.set(buildId);
        nbUnpostedCstrs = model.getNbUnpostedCstrs();
        nbRows = 0;
        linrel = null;
        objective = null;
        cachedLB = null;
        cachedUB = null;
        lastSol = null;
        // 1. assign a column to every IntVar appearing in a linear-relaxable propagator; the columns of a
        // previous build are kept
        List<Propagator<?>> propagators = new ArrayList<>();
        for (Constraint c : model.getCstrs()) {
            if (isLeftOut(c)) continue;
            for (Propagator<?> p : c.getPropagators()) {
                if (p == this || Arrays.stream(p.getVars()).anyMatch(v -> !(v instanceof IntVar))) {
                    continue;
                }
                propagators.add(p);
                for (Variable v : p.getVars()) {
                    if (!add_var.containsKey(v)) {
                        add_var.put((IntVar) v, columns.size());
                        columns.add((IntVar) v);
                    }
                }
            }
        }
        if (columns.isEmpty()) {
            return;
        }
        // 2. build the relaxation directly on the implementation - no intermediate structures
        ILinearRelaxation lp = solverFactory.apply(columns.size());
        for (Propagator<?> p : propagators) {
            nbRows += p.addLinearExpression(lp, add_var);
        }
        if (nbRows == 0) {
            // no linear-relaxable constraints in the model (e.g. pure AllDifferent under condition): nothing to solve
            return;
        }
        linrel = lp;
        // 3. objective: a single target variable, or feasibility-only when not optimising
        if (model.getSolver().getObjectiveManager().isOptimization()
                && model.getObjective() instanceof IntVar
                && add_var.containsKey(model.getObjective().asIntVar())) {
            objective = model.getObjective().asIntVar();
            linrel.setObjective(add_var.get(objective),
                    model.getResolutionPolicy().equals(ResolutionPolicy.MAXIMIZE));
        }
        // else: the LP is solved for feasibility only, because the model is a satisfaction one, or because its
        // objective variable is not an integer one, or belongs to no constraint the LP can see
        // 4. watch the variables that are not yet watched
        if (vars.length < columns.size()) {
            addVariable(columns.subList(vars.length, columns.size()).toArray(new IntVar[0]));
        }
        // the column of vars[i] is i: no lookup is needed in the loops over the variables
        assert java.util.stream.IntStream.range(0, vars.length).allMatch(i -> vars[i] == columns.get(i));
    }

    /**
     * A constraint that does not always hold, or that is disabled, cannot contribute to the relaxation.
     * TODO: handle reified constraints
     */
    private static boolean isLeftOut(Constraint c) {
        return c instanceof ReificationConstraint || c instanceof ImpliedConstraint || c.isReified() || !c.isEnabled();
    }

    private boolean needResolve() {
        // Re-assert bounds to the relaxation, but only for variables whose [LB, UB] actually changed
        if (cachedLB == null || cachedLB.length != vars.length) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Re-assert all (%d) bounds", vars.length));
            }
            cachedLB = new int[vars.length];
            cachedUB = new int[vars.length];
            for (int i = 0; i < vars.length; i++) {
                int lb = vars[i].getLB();
                int ub = vars[i].getUB();
                linrel.updateRange(i, lb, ub);
                cachedLB[i] = lb;
                cachedUB[i] = ub;
            }
            return true;
        } else {
            int modified = 0;
            int fixed = 0;
            boolean solutionFeasible = true;
            for (int i = 0; i < vars.length; i++) {
                int lb = vars[i].getLB();
                int ub = vars[i].getUB();
                if (lb != cachedLB[i] || ub != cachedUB[i]) {
                    modified++;
                    linrel.updateRange(i, lb, ub);
                    cachedLB[i] = lb;
                    cachedUB[i] = ub;
                    if (lb == ub) fixed++;
                    if (lastSol != null && (lb > lastSol[i] + 1e-9 || lastSol[i] - 1e-9 > ub)) {
                        solutionFeasible = false;
                    }
                }
            }
            // the LP is not solved again while its last solution fits in the current bounds. When the bounds have
            // only been reduced since, that solution is still optimal. When they have been widened (on backtrack),
            // it may not be, and some filtering is missed until the solution no longer fits; skipping a solve is
            // always safe, though.
            boolean needsResolve = lastSol == null || !solutionFeasible;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Re-assert %d bounds, %d fixed, %d total (%s)",
                        modified, fixed, vars.length, needsResolve));
            }
            return needsResolve;
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return vars.length == 0 ? IntEventType.VOID.getMask() : IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("[node %d, fail %d] Start propagation",
                    model.getSolver().getNodeCount(), model.getSolver().getFailCount()));
        }

        if (buildId == 0 || builtHere.get() != buildId) {
            // first propagation of a resolution, which takes place at the root node
            build();
        } else if (model.getNbUnpostedCstrs() != nbUnpostedCstrs) {
            // a constraint was unposted during the search: the LP may not be a relaxation of the model any more.
            // It is left aside until the next resolution.
            nbStale++;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Constraints unposted during the search: LP left aside");
            }
            return;
        }
        // constraints posted during the search are not in the LP, which remains a relaxation of the model
        if (linrel == null) {
            // no linear-relaxable constraints in the model: nothing for the LP to do
            return;
        }
        nbPropagations++;
        if (backoff) {
            long solutions = model.getSolver().getSolutionCount();
            if (model.getSolver().getDecisionPath().size() <= 1 || solutions != lastSolutionCount) {
                // at the root node, and after a new solution (the reduced costs may start filtering), the LP is
                // always solved, and the interval starts again from 1
                lastSolutionCount = solutions;
                interval = 1;
                countdown = 0;
            } else if (countdown > 0) {
                countdown--;
                nbBackedOff++;
                return;
            }
        }
        if(!needResolve()){
            // no need to call the LP solver
            nbSkipped++;
            return;
        }
        boolean useful = true;
        long deductions = nbInfeasible + nbObjectiveUpdates + nbFilteredBounds;
        try {
            solveAndDeduce();
            useful = nbInfeasible + nbObjectiveUpdates + nbFilteredBounds > deductions;
        } finally {
            // a failure, raised by the LP or by one of its deductions, counts as useful
            if (backoff) {
                interval = useful ? 1 : Math.min(2 * interval, maxInterval);
                countdown = interval - 1;
            }
        }
    }

    /**
     * Solve the LP and apply what it proves: a failure, a bound on the objective, bounds filtered with the reduced
     * costs.
     */
    private void solveAndDeduce() throws ContradictionException {
        long start = System.nanoTime();
        ILinearRelaxation.Status status = linrel.solve();
        lastReducedCosts = null;
        long duration = System.nanoTime() - start;
        nbSolves++;
        solveNanos += duration;
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("linrel.solve() : %.6fs", duration / 1_000_000_000.));
        }
        if (status == ILinearRelaxation.Status.UNKNOWN) {
            // nothing is proven, so nothing can be deduced; forget the last solution to solve again next time
            nbUnknown++;
            lastSol = null;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("LP outcome unknown");
            }
            return;
        }
        if (status == ILinearRelaxation.Status.INFEASIBLE) {
            nbInfeasible++;
            // the relaxation is proven infeasible => the subtree fails
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("LP infeasible");
            }
            this.fails(explainInfeasibility());
            return;
        }
        // a feasible solution exists, and it is optimal
        if (objective == null) {
            // feasibility only: no bound to deduce
            rememberSolution();
            return;
        }
        // 1. update the objective variable
        double obj = linrel.getObjectiveValue();
        double slack;
        boolean maximize = model.getResolutionPolicy() == ResolutionPolicy.MAXIMIZE;
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("LP obj=%.6f, %s, objective=%s",
                    obj, maximize ? "MAX" : "MIN", objective));
        }
        if (maximize) {
            int lb = objective.getLB();
            slack = obj - lb;
            int newUB = (int) Math.floor(obj + tolerance(obj));
            if (LOGGER.isLoggable(Level.FINE) && newUB < objective.getUB()) {
                LOGGER.fine(String.format("  obj UB: %d -> %d (slack=%.6f)",
                        objective.getUB(), newUB, slack));
            }
            if (newUB < objective.getUB()
                    && objective.updateUpperBound(newUB, this, explainObjective(maximize, false, newUB))) {
                nbObjectiveUpdates++;
            }
        } else {
            int ub = objective.getUB();
            slack = ub - obj;
            int newLB = (int) Math.ceil(obj - tolerance(obj));
            if (LOGGER.isLoggable(Level.FINE) && newLB > objective.getLB()) {
                LOGGER.fine(String.format("  obj LB: %d -> %d (slack=%.6f)",
                        objective.getLB(), newLB, slack));
            }

            if (newLB > objective.getLB()
                    && objective.updateLowerBound(newLB, this, explainObjective(maximize, true, newLB))) {
                nbObjectiveUpdates++;
            }
        }
        // Reduced-cost filtering (MAX convention):
        //   rc > 0  => var non-basic at its UB; decreasing by Δ costs Δ·rc,
        //              so Δ ≤ slack/rc => newLB = UB - slack/rc.
        //   rc < 0  => var non-basic at its LB; increasing by Δ costs -Δ·rc,
        //              so Δ ≤ -slack/rc => newUB = LB - slack/rc.
        // slack ≥ 0 in both senses (obj_LP - incumbent.LB for MAX, incumbent.UB - obj_LP
        // for MIN); rc signs invert between conventions (at LB: rc ≤ 0 in MAX, rc ≥ 0 in
        // MIN), so for MIN we negate rc to reuse the MAX formula.
        // Clamp slack ≥ 0 to absorb LP FP noise; skip near-zero rc to avoid huge spurious
        // bound updates from numerical degeneracy.
        double[] coeffs = reducedCosts();
        double s = Math.max(slack, 0.);
        rememberSolution();
        for (int i = 0; i < vars.length; i++) {
            int idx = i;
            if (vars[i] == objective) {
                continue;
            }
            double rc = maximize ? coeffs[idx] : -coeffs[idx];
            if (rc > 1e-9) {
                double newLB = vars[i].getUB() - s / rc;
                // the error on the slack is the one on the optimal value, and it is divided by the reduced cost
                int ceil = (int) Math.ceil(newLB - tolerance(Math.max(Math.abs(obj) / rc, Math.abs(newLB))));
                if (ceil > vars[i].getLB()) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("  %s LB: %d -> %d (rc=%.6f)",
                                vars[i].getName(), vars[i].getLB(), ceil, rc));
                    }
                }
                if (ceil > vars[i].getLB()
                        && vars[i].updateLowerBound(ceil, this, explainReducedCost(maximize, vars[i], true, ceil))) {
                    nbFilteredBounds++;
                }
            } else if (rc < -1e-9) {
                double newUB = vars[i].getLB() - s / rc;
                int floor = (int) Math.floor(newUB + tolerance(Math.max(Math.abs(obj) / -rc, Math.abs(newUB))));
                if (floor < vars[i].getUB()) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("  %s UB: %d -> %d (rc=%.6f)",
                                vars[i].getName(), vars[i].getUB(), floor, rc));
                    }
                }
                if (floor < vars[i].getUB()
                        && vars[i].updateUpperBound(floor, this, explainReducedCost(maximize, vars[i], false, floor))) {
                    nbFilteredBounds++;
                }
            }
        }
    }

    /**
     * @param magnitude the order of magnitude of a value computed from the LP
     * @return the error that value may carry, to be given up when rounding it to an integer bound
     */
    static double tolerance(double magnitude) {
        return Math.max(ABSOLUTE_TOLERANCE, RELATIVE_TOLERANCE * Math.abs(magnitude));
    }

    /**
     * @return the reduced costs of the last solve, read once from the relaxation
     */
    private double[] reducedCosts() {
        if (lastReducedCosts == null) {
            lastReducedCosts = linrel.getReducedCosts();
        }
        return lastReducedCosts;
    }

    private void rememberSolution() {
        if (lastSol == null) {
            lastSol = new double[vars.length];
        }
        double[] curSol = linrel.getSolution();
        for (int i = 0; i < vars.length; i++) {
            lastSol[i] = curSol[i];
        }
    }

    /**
     * The bounds the optimal value of the LP relies on, read from the dual solution. By weak duality, the
     * objective cannot be better than the optimal value as long as every variable with a non-zero reduced
     * cost keeps the bound it is sitting at: its upper bound when the reduced cost is positive, its lower bound
     * when it is negative (in the maximisation convention used by {@link #propagate(int)}). The variables with
     * a zero reduced cost play no role, whatever their bounds.
     * <p>
     * The variable the deduction is about is left out: the same inequality holds whatever its own bounds, its
     * reduced cost being applied to its value instead.
     *
     * @param maximize the optimisation sense
     * @param useLB    filled with the variables whose lower bound is used
     * @param useUB    filled with the variables whose upper bound is used
     * @param target   the variable the deduction is about
     */
    private void dualProof(boolean maximize, boolean[] useLB, boolean[] useUB, IntVar target) {
        double[] coeffs = reducedCosts();
        for (int i = 0; i < vars.length; i++) {
            if (vars[i] == target) {
                continue;
            }
            double rc = maximize ? coeffs[i] : -coeffs[i];
            // a tiny reduced cost still matters on a wide domain
            double weight = Math.abs(rc) * Math.max(1., (double) vars[i].getUB() - vars[i].getLB());
            if (weight > RC_EPSILON) {
                if (rc > 0) {
                    useUB[i] = true;
                } else {
                    useLB[i] = true;
                }
            }
        }
    }

    private Reason toReason(boolean[] useLB, boolean[] useUB, IntVar target, boolean lower, int newBound) {
        if (observer != null) {
            observer.explained(vars, useLB, useUB, target, lower, newBound);
        }
        if (!lcg()) {
            return Reason.undef();
        }
        TIntArrayList ps = new TIntArrayList();
        ps.add(0); // place for the modified literal
        for (int i = 0; i < vars.length; i++) {
            if (useLB[i]) {
                ps.add(vars[i].getMinLit());
            }
            if (useUB[i]) {
                ps.add(vars[i].getMaxLit());
            }
        }
        // a reason of more than two literals keeps its first slot for the deduced literal: pad with false literals
        while (ps.size() < 3) {
            ps.add(0);
        }
        return r(ps);
    }

    /**
     * Explain an infeasible LP. With a certificate (see {@link ILinearRelaxation#getInfeasibilityCertificate()}),
     * by the bounds it relies on; otherwise, by both bounds of all the variables of the LP.
     */
    private Reason explainInfeasibility() {
        if (!lcg() && observer == null) {
            return Reason.undef();
        }
        boolean[] useLB = new boolean[vars.length];
        boolean[] useUB = new boolean[vars.length];
        double[] g = linrel.getInfeasibilityCertificate();
        if (g != null) {
            nbCertified++;
            nbCertifiedAllBounds += 2L * vars.length;
            for (int i = 0; i < vars.length; i++) {
                double gi = g[i];
                if (gi > 0) {
                    useLB[i] = true;
                    nbCertifiedBounds++;
                } else if (gi < 0) {
                    useUB[i] = true;
                    nbCertifiedBounds++;
                }
            }
        } else {
            Arrays.fill(useLB, true);
            Arrays.fill(useUB, true);
        }
        return toReason(useLB, useUB, null, false, 0);
    }

    /**
     * Explain a bound of the objective variable deduced from the optimal value of the LP.
     */
    private Reason explainObjective(boolean maximize, boolean lower, int newBound) {
        if (!lcg() && observer == null) {
            return Reason.undef();
        }
        boolean[] useLB = new boolean[vars.length];
        boolean[] useUB = new boolean[vars.length];
        dualProof(maximize, useLB, useUB, objective);
        return toReason(useLB, useUB, objective, lower, newBound);
    }

    /**
     * Explain a bound deduced from a reduced cost: on top of what explains the optimal value of the LP, it relies
     * on the bound of the objective variable the search is trying to improve.
     */
    private Reason explainReducedCost(boolean maximize, IntVar target, boolean lower, int newBound) {
        if (!lcg() && observer == null) {
            return Reason.undef();
        }
        boolean[] useLB = new boolean[vars.length];
        boolean[] useUB = new boolean[vars.length];
        dualProof(maximize, useLB, useUB, target);
        for (int i = 0; i < vars.length; i++) {
            if (vars[i] == objective) {
                if (maximize) {
                    useLB[i] = true;
                } else {
                    useUB[i] = true;
                }
            }
        }
        return toReason(useLB, useUB, target, lower, newBound);
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }

    /**
     * @return the relaxation this propagator solves, or {@code null} when the model has nothing to relax or
     *         before the first propagation
     */
    public ILinearRelaxation getRelaxation() {
        return linrel;
    }

    /**
     * A one-line summary of the work done so far: builds of the LP, propagations, propagations that skipped the LP, LP solves and
     * the time they took, infeasible LPs, LP solves with an unknown outcome, updates of the objective bound and bounds filtered with the reduced
     * costs.
     *
     * @return the summary
     */
    public String getStatistics() {
        return String.format("columns=%d, builds=%d, stale=%d, propagations=%d, skipped=%d, backedOff=%d, solves=%d (%.3fs), infeasible=%d "
                        + "(certified=%d, certifiedBounds=%.3f), "
                        + "unknown=%d, objectiveUpdates=%d, filteredBounds=%d",
                vars.length, nbBuilds, nbStale, nbPropagations, nbSkipped, nbBackedOff, nbSolves, solveNanos / 1_000_000_000.,
                nbInfeasible, nbCertified, certifiedShare(), nbUnknown, nbObjectiveUpdates, nbFilteredBounds);
    }

    /**
     * The same figures as {@link #getStatistics()}, plus the number of rows of the LP, as a JSON object; the time
     * spent solving the LP is in seconds.
     *
     * @return the statistics as a JSON object
     */
    public String getStatisticsAsJson() {
        return String.format(Locale.US, "{\"columns\":%d, \"rows\":%d, \"builds\":%d, \"stale\":%d, "
                        + "\"propagations\":%d, \"skipped\":%d, \"backedOff\":%d, \"solves\":%d, \"time\":%.3f, \"infeasible\":%d, "
                        + "\"certified\":%d, \"certifiedBounds\":%.3f, \"unknown\":%d, \"objectiveUpdates\":%d, \"filteredBounds\":%d}",
                vars.length, nbRows, nbBuilds, nbStale, nbPropagations, nbSkipped, nbBackedOff, nbSolves,
                solveNanos / 1_000_000_000., nbInfeasible, nbCertified, certifiedShare(), nbUnknown,
                nbObjectiveUpdates, nbFilteredBounds);
    }

    /**
     * @return the share of the bounds of the LP that the certified infeasible LPs were explained by, on average
     */
    private double certifiedShare() {
        return nbCertifiedAllBounds == 0 ? 0. : (double) nbCertifiedBounds / nbCertifiedAllBounds;
    }

    /**
     * Look for the linear relaxation propagator of a model.
     *
     * @param model a model
     * @return its linear relaxation propagator, or {@code null} if linear relaxation is not enabled
     */
    public static PropSimplex find(Model model) {
        for (Constraint c : model.getCstrs()) {
            for (Propagator<?> p : c.getPropagators()) {
                if (p instanceof PropSimplex) {
                    return (PropSimplex) p;
                }
            }
        }
        return null;
    }

}
