/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.memory.ICondition;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.ISatFactory;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.util.ESat;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * Settings for Model and Solver.
 * Can be modified programmatically
 * and can be defined in a Model only on creation.
 *
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 14/12/2017.
 */
public class Settings {

    private Predicate<Solver> modelChecker = s -> !ESat.FALSE.equals(s.isSatisfied());

    private boolean cloneVariableArrayInPropagator = true;

    private boolean enableViews = true;

    private int maxDomSizeForEnumerated = 1 << 16;

    private int minCardForSumDecomposition = 50;

    private boolean enableTableSubstitution = true;

    private int maxTupleSizeForSubstitution = 10_000;

    private boolean sortPropagatorActivationWRTPriority = true;

    private int maxPropagatorPriority = PropagatorPriority.VERY_SLOW.getValue();

    private Function<Model, AbstractStrategy<?>> defaultSearch = Search::defaultSearch;

    private boolean warnUser = false;

    private boolean enableDecompositionOfBooleanSum = false;

    private IntPredicate enableIncrementalityOnBoolSum = i -> i > 10;

    private boolean enableSAT = false;

    private boolean swapOnPassivate = false;

    private boolean checkDeclaredConstraints = true;

    private boolean checkDeclaredViews = true;

    private boolean checkDeclaredMonitors = true;

    private boolean printAllUndeclaredConstraints = false;

    private byte hybridEngine = 0b00;

    private int nbMaxLearnt = 100_000;

    private int maxLearntCardinlity = Integer.MAX_VALUE / 100;

    private float clauseReductionRatio = .5f;

    private int dominancePerimeter = 4;

    private boolean explainGlobalFailureInSum = true;

    private double ibexContractionRatio = Ibex.RATIO;

    private boolean ibexRestoreRounding = Ibex.PRESERVE_ROUNDING;

    private Function<Model, Solver> initSolver = Solver::new;

    private final HashMap<String, Object> additionalSettings = new HashMap<>();

    private Settings() {
    }

    /**
     * Create a new instance of `Settings` which can then be adapted to requirements.
     *
     * @return a Settings with default values
     * @see #dev()
     * @see #prod()
     */
    public static Settings init() {
        return new Settings();
    }

    /**
     * Define and returns settings adapted to production environment.
     * All checks and warnings are turned off.
     *
     * @return a settings adapted to production environment.
     */
    public static Settings prod() {
        return Settings.init()
                .setModelChecker(s -> true)
                .setWarnUser(false)
                .setCheckDeclaredConstraints(false)
                .setCheckDeclaredViews(false)
                .setCheckDeclaredMonitors(false)
                .setPrintAllUndeclaredConstraints(false);
    }

    /**
     * Define and returns settings adapted to development environment.
     * All checks and warnings are turned on.
     *
     * @return a settings adapted to development environment.
     */
    public static Settings dev() {
        return Settings.init()
                .setModelChecker(s -> !ESat.FALSE.equals(s.isSatisfied()))
                .setWarnUser(true)
                .setCheckDeclaredConstraints(true)
                .setCheckDeclaredViews(true)
                .setCheckDeclaredMonitors(true)
                .setPrintAllUndeclaredConstraints(true);
    }

    /**
     * @param solver the solver
     * @return <tt>true</tt> if the model is OK wrt the checker, <tt>false</tt> otherwise
     */
    public boolean checkModel(Solver solver) {
        return modelChecker.test(solver);
    }

    /**
     * Define what to do when a solution is found. By default, it makes a weak check of the model:
     * <pre>
     *     {@code
     *         return !ESat.FALSE.equals(solver.isSatisfied());
     *     }
     * </pre>
     * A hard check of the model can be done like this:
     * <pre>
     *     {@code
     *     return ESat.TRUE.equals(solver.isSatisfied());
     *     }
     * </pre>
     *
     * @param modelChecker a predicate to check the solution
     * @return the current instance
     */
    public Settings setModelChecker(Predicate<Solver> modelChecker) {
        this.modelChecker = modelChecker;
        return this;
    }

    /**
     * @return true if all propagators should clone the input variable array instead of simply referencing it.
     */
    public boolean cloneVariableArrayInPropagator() {
        return cloneVariableArrayInPropagator;
    }

    /**
     * If this setting is set to true (default value), a clone of the input variable array is made in any propagator constructors.
     * This prevents, for instance, wrong behavior when permutations occurred on the input array (e.g., sorting variables).
     * Setting this to false may limit the memory consumption during modelling.
     *
     * @param cloneVariableArrayInPropagator {@code true} to clone variables array on constructor
     * @return the current instance
     */
    public Settings setCloneVariableArrayInPropagator(boolean cloneVariableArrayInPropagator) {
        this.cloneVariableArrayInPropagator = cloneVariableArrayInPropagator;
        return this;
    }

    /**
     * @return <tt>true</tt> if views are enabled.
     */
    public boolean enableViews() {
        return enableViews;
    }


    /**
     * Set to 'true' to allow the creation of views in the {@link org.chocosolver.solver.Model}.
     * Creates new variables with channeling constraints otherwise.
     *
     * @param enableViews {@code true} to enable views
     * @return the current instance
     */
    public Settings setEnableViews(boolean enableViews) {
        this.enableViews = enableViews;
        return this;
    }

    /**
     * @return maximum domain size threshold to force integer variable to be enumerated
     */
    public int getMaxDomSizeForEnumerated() {
        return maxDomSizeForEnumerated;
    }

    /**
     * Define the minimum number of cardinality threshold to a sum/scalar constraint to be decomposed in intermediate
     * sub-sums.
     *
     * @param maxDomSizeForEnumerated cardinality threshold
     * @return the current instance
     */
    public Settings setMaxDomSizeForEnumerated(int maxDomSizeForEnumerated) {
        this.maxDomSizeForEnumerated = maxDomSizeForEnumerated;
        return this;
    }

    /**
     * @return minimum number of cardinality threshold to a sum constraint to be decomposed
     */
    public int getMinCardForSumDecomposition() {
        return minCardForSumDecomposition;
    }


    /**
     * Define the default minimum number of cardinality threshold to a sum/scalar constraint to be
     * decomposed into intermediate sub-sums.
     *
     * @param defaultMinCardinalityForSumDecomposition cardinality threshold
     * @return the current instance
     */
    public Settings setMinCardinalityForSumDecomposition(int defaultMinCardinalityForSumDecomposition) {
        this.minCardForSumDecomposition = defaultMinCardinalityForSumDecomposition;
        return this;
    }


    /**
     * @return <tt>true</tt> if some intension constraints can be replaced by extension constraints
     */
    public boolean enableTableSubstitution() {
        return enableTableSubstitution;
    }


    /**
     * Define whether some intension constraints are replaced by extension constraints
     *
     * @param enableTableSubstitution enable table substitution
     * @return the current instance
     */
    public Settings setEnableTableSubstitution(boolean enableTableSubstitution) {
        this.enableTableSubstitution = enableTableSubstitution;
        return this;
    }


    /**
     * @return maximum domain size threshold to replace intension constraints by extension constraints
     */
    public int getMaxTupleSizeForSubstitution() {
        return maxTupleSizeForSubstitution;
    }

    /**
     * Define the maximum domain size threshold to replace intension constraints by extension constraints
     * Only checked when {@link #enableTableSubstitution()} returns {@code true}
     *
     * @param maxTupleSizeForSubstitution threshold to substitute intension constraint by table one.
     * @return the current instance
     */
    public Settings setMaxTupleSizeForSubstitution(int maxTupleSizeForSubstitution) {
        this.maxTupleSizeForSubstitution = maxTupleSizeForSubstitution;
        return this;
    }


    /**
     * @return {@code true} if propagators are sorted wrt their priority on initial activation.
     */
    public boolean sortPropagatorActivationWRTPriority() {
        return sortPropagatorActivationWRTPriority;
    }


    /**
     * Set whether propagators are sorted wrt their priority in {@link org.chocosolver.solver.propagation.PropagationEngine} when
     * dealing with propagator activation.
     *
     * @param sortPropagatorActivationWRTPriority {@code true} to allow sorting static propagators.
     * @return the current instance
     */
    public Settings setSortPropagatorActivationWRTPriority(boolean sortPropagatorActivationWRTPriority) {
        this.sortPropagatorActivationWRTPriority = sortPropagatorActivationWRTPriority;
        return this;
    }


    /**
     * @return the maximum priority any propagators can have (default is 7)
     */
    public int getMaxPropagatorPriority(){
        return maxPropagatorPriority;
    }

    /**
     * Increase the number of priority for propagators (default is {@link PropagatorPriority#VERY_SLOW}).
     * This directly impacts the number of queues to schedule propagators in the propagation engine.
     *
     * @param maxPropagatorPriority the new maximum prioirity any propagator can declare
     * @return the current instance
     */
    public Settings setMaxPropagatorPriority(int maxPropagatorPriority){
        this.maxPropagatorPriority = maxPropagatorPriority;
        return this;
    }


    /**
     * Creates a default search strategy for the input model
     *
     * @param model a model requiring a default search strategy
     * @return a default search strategy for model
     * @see Search#defaultSearch(Model)
     */
    public AbstractStrategy<?> makeDefaultSearch(Model model) {
        return defaultSearch.apply(model);
    }

    /**
     * Define a default search strategy for the input model
     *
     * @param defaultSearch what default search strategy should be
     * @return the current instance
     */
    public Settings setDefaultSearch(Function<Model, AbstractStrategy<?>> defaultSearch) {
        this.defaultSearch = defaultSearch;
        return this;
    }

    @Deprecated
    public ICondition getEnvironmentHistorySimulationCondition() {
        return null;
    }

    @Deprecated
    public Settings setEnvironmentHistorySimulationCondition(ICondition environmentHistorySimulationCondition) {
        return null;
    }

    /**
     * @return <tt>true</tt> if warnings detected during modeling/solving are output.
     */
    public boolean warnUser() {
        return warnUser;
    }


    /**
     * To be informed of warnings detected during modeling/solving
     *
     * @param warnUser {@code true} to be print warnings on console
     * @return the current instance
     */
    public Settings setWarnUser(boolean warnUser) {
        this.warnUser = warnUser;
        return this;
    }


    /**
     * @return {@code true} if boolean sum should be decomposed into an equality constraint and an arithmetic constraint,
     * {@code false}if a single constraint should be used instead.
     */
    public boolean enableDecompositionOfBooleanSum() {
        return enableDecompositionOfBooleanSum;
    }


    /**
     * Define if boolean sums should be decomposed into an equality constraint + arithmetic constraint
     *
     * @param enableDecompositionOfBooleanSum {@code true} to enable decomposition
     * @return the current instance
     */
    public Settings setEnableDecompositionOfBooleanSum(boolean enableDecompositionOfBooleanSum) {
        this.enableDecompositionOfBooleanSum = enableDecompositionOfBooleanSum;
        return this;
    }


    /**
     * @param nbvars number of variables in the constraint
     * @return {@code true} if the incrementality is enabled on boolean sum, based on the number of variables involved.
     */
    public boolean enableIncrementalityOnBoolSum(int nbvars) {
        return enableIncrementalityOnBoolSum.test(nbvars);
    }


    /**
     * Define the predicate to choose incremental sum, based on number variables declared
     *
     * @param enableIncrementalityOnBoolSum predicate to pick declare sum
     * @return the current instance
     */
    public Settings setEnableIncrementalityOnBoolSum(IntPredicate enableIncrementalityOnBoolSum) {
        this.enableIncrementalityOnBoolSum = enableIncrementalityOnBoolSum;
        return this;
    }

    /**
     * @return <i>true</i> when an underlying SAT solver is used to manage clauses declared through {@link ISatFactory},
     * <i>false</i> when clauses are managed with CSP constraints only.
     */
    public boolean enableSAT() {
        return enableSAT;
    }


    /**
     * Indicate if clauses are managed by a unique SAT solver.
     *
     * @param enableSAT {@code true} to rely on SAT Solver to handle clauses
     * @return the current instance
     */
    public Settings setEnableSAT(boolean enableSAT) {
        this.enableSAT = enableSAT;
        return this;
    }

    /**
     * @return <i>true</i> if, on propagator passivation, the propagator is swapped from active to passive in its variables' propagators list.
     * <i>false</i> if, on propagator passivation, only the propagator's state is set to PASSIVE.
     */
    public boolean swapOnPassivate() {
        return swapOnPassivate;
    }


    /**
     * Define if passivation of propagator swap it in variables' list
     *
     * @param swapOnPassivate {@code true} to enable swapping
     * @return the current instance
     */
    public Settings setSwapOnPassivate(boolean swapOnPassivate) {
        this.swapOnPassivate = swapOnPassivate;
        return this;
    }

    /**
     * @return <i>true</i> (default value) to check if all declared constraints are not free anymore,
     * that is either posted or reified, before running the resolution.
     * <i>false</i> to skip the control.
     */
    public boolean checkDeclaredConstraints() {
        return checkDeclaredConstraints;
    }


    /**
     * Indicate if the declared constraints are either posted or reified.
     *
     * @param checkDeclaredConstraints {@code true} to check constraints before resolution
     * @return the current instance
     */
    public Settings setCheckDeclaredConstraints(boolean checkDeclaredConstraints) {
        this.checkDeclaredConstraints = checkDeclaredConstraints;
        return this;
    }

    /**
     * @return <i>true</i> to list all undeclared constraint, <i>false</i> (default value) otherwise.
     * Only active when {@link #checkDeclaredConstraints()} is on.
     */
    public boolean printAllUndeclaredConstraints() {
        return printAllUndeclaredConstraints;
    }


    /**
     * Indicate if all undeclared constraints are listed on console when {@link #checkDeclaredConstraints()} is on.
     *
     * @param printAllUndeclaredConstraints {@code true} to list all undeclared constraints
     * @return the current instance
     */
    public Settings setPrintAllUndeclaredConstraints(boolean printAllUndeclaredConstraints) {
        this.printAllUndeclaredConstraints = printAllUndeclaredConstraints;
        return this;
    }

    /**
     * @return <i>true</i> (default value) to check prior to creation
     * if a view already semantically exists.
     */
    public boolean checkDeclaredViews() {
        return checkDeclaredViews;
    }


    /**
     * Check if a view already semantically exists before creating it.
     *
     * @param checkDeclaredViews {@code true} to check views before creation
     * @return the current instance
     */
    public Settings setCheckDeclaredViews(boolean checkDeclaredViews) {
        this.checkDeclaredViews = checkDeclaredViews;
        return this;
    }

    public Settings setCheckDeclaredMonitors(boolean check) {
        this.checkDeclaredMonitors = check;
        return this;
    }

    public boolean checkDeclaredMonitors() {
        return this.checkDeclaredMonitors;
    }

    /**
     * This method is called in {@link Model#Model(IEnvironment, String, Settings)} to create the
     * solver to associate with a model.
     *
     * @param model a model to initialize with a solver
     * @return the new solver
     */
    public Solver initSolver(Model model) {
        return initSolver.apply(model);
    }

    /**
     * Define the solver initialization
     *
     * @param initSolver function to initialize the solver
     * @return the current instance
     */
    public Settings setInitSolver(Function<Model, Solver> initSolver) {
        this.initSolver = initSolver;
        return this;
    }

    /**
     * @return <i>0b00<i/> if constraint-oriented propagation engine,
     * <i>0b01<i/> if hybridization between variable and constraint oriented and
     * <i>0b10<i/> if variable-oriented.
     */
    public byte enableHybridizationOfPropagationEngine() {
        return hybridEngine;
    }


    /**
     * Define behavior of the propagation engine.
     *
     * @param hybrid When set to '0b00', this works as a constraint-oriented propagation engine;
     *               when set to '0b01', this workds as an hybridization between variable and constraint oriented
     *               propagation engine.
     *               when set to '0b10', this workds as a variable- oriented propagation engine.
     * @return the current instance
     */
    public Settings setHybridizationOfPropagationEngine(byte hybrid) {
        this.hybridEngine = hybrid;
        return this;
    }


    /**
     * @return maximum number of learnt clauses to store. When reached, a reduction is applied.
     * @see #setNbMaxLearntClauses(int)
     * @see #setRatioForClauseStoreReduction(float)
     * @see #getRatioForClauseStoreReduction()
     * @see #setMaxLearntClauseCardinality(int)
     * @see #getMaxLearntClauseCardinality()
     */
    public int getNbMaxLearntClauses() {
        return nbMaxLearnt;
    }

    /**
     * Set the maximum of number of learnt clauses to store before running a reduction of the store.
     *
     * @param n maximum number of learnt clauses before reducing the store.
     * @return the current instance
     * @see #getNbMaxLearntClauses()
     * @see #setRatioForClauseStoreReduction(float)
     * @see #getRatioForClauseStoreReduction()
     * @see #setMaxLearntClauseCardinality(int)
     * @see #getMaxLearntClauseCardinality()
     */
    public Settings setNbMaxLearntClauses(int n) {
        this.nbMaxLearnt = n;
        return this;
    }

    /**
     * when clauses store need to be reduced, 'ratio' of them are kept (between  0.1 and .99)
     *
     * @see #setRatioForClauseStoreReduction(float)
     * @see #setNbMaxLearntClauses(int)
     * @see #getNbMaxLearntClauses()
     * @see #setMaxLearntClauseCardinality(int)
     * @see #getMaxLearntClauseCardinality()
     */
    public float getRatioForClauseStoreReduction() {
        return this.clauseReductionRatio;
    }

    /**
     * when clauses store need to be reduced, 'ratio' of them are kept (between  0.1 and .99).
     * A call to this defines 'ratio'.
     *
     * @param f ratio for clause store reduction
     * @return the current instance
     * @see #getRatioForClauseStoreReduction()
     * @see #setNbMaxLearntClauses(int)
     * @see #getNbMaxLearntClauses()
     * @see #setMaxLearntClauseCardinality(int)
     * @see #getMaxLearntClauseCardinality()
     */
    public Settings setRatioForClauseStoreReduction(float f) {
        this.clauseReductionRatio = f;
        return this;
    }

    /**
     * @return maximum learnt clause cardinality, clauses beyond this value are ignored.
     * @see #setMaxLearntClauseCardinality(int)
     * @see #setNbMaxLearntClauses(int)
     * @see #setRatioForClauseStoreReduction(float)
     * @see #getRatioForClauseStoreReduction()
     * @see #setRatioForClauseStoreReduction(float)
     */
    public int getMaxLearntClauseCardinality() {
        return maxLearntCardinlity;
    }


    /**
     * Set the maximum learnt clause cardinality, clauses beyond this value are ignored.
     *
     * @param n maximum learnt clause cardinality.
     * @return the current instance
     * @see #getMaxLearntClauseCardinality()
     * @see #getNbMaxLearntClauses()
     * @see #setRatioForClauseStoreReduction(float)
     * @see #getRatioForClauseStoreReduction()
     * @see #setRatioForClauseStoreReduction(float)
     */
    public Settings setMaxLearntClauseCardinality(int n) {
        maxLearntCardinlity = n;
        return this;
    }

    /**
     * When a clause is learnt from a conflict, it may happen that it dominates previously learnt ones.
     * The dominance will be evaluated with the <i>n</i> last learnt clauses.
     * n = 0 means no dominance check, n = {@link Integer#MAX_VALUE} means checking all clauses with the last one.
     *
     * @return dominance perimeter
     */
    public int getLearntClausesDominancePerimeter() {
        return dominancePerimeter;
    }

    /**
     * When a clause is learnt from a conflict, it may happen that it dominates previously learnt ones.
     * The dominance will be evaluated with the <i>n</i> last learnt clauses.
     * n = 0 means no dominance check, n = {@link Integer#MAX_VALUE} means checking all clauses with the last one.
     *
     * @return dominance perimeter
     */
    public Settings setLearntClausesDominancePerimeter(int n) {
        this.dominancePerimeter = n;
        return this;
    }


    /**
     * @return <i>true</i> if additional clauses can be learned from sum's global failure
     */
    public boolean explainGlobalFailureInSum() {
        return explainGlobalFailureInSum;
    }


    /**
     * Set to <i>true</i> to allow additional clauses to be learned from sum's global failure
     */
    public Settings explainGlobalFailureInSum(boolean b) {
        this.explainGlobalFailureInSum = b;
        return this;
    }

    /**
     * @return the ratio that a domains must be contracted by ibex to compute the constraint.
     */
    public double getIbexContractionRatio() {
        return ibexContractionRatio;
    }

    /**
     * Defines the ratio that real domains must be contracted by ibex
     * to compute the constraint. A contraction is considered as significant
     * when at least {@param ratio} of a domain has been reduced.
     * If the contraction is not meet, then it is considered as insufficient
     * and therefore ignored. A too small ratio can degrade the ibex performance.
     * The default value is 1% (0.01). See issue #653.
     * <p>
     * Example: given x = [0.0, 100.0], y = [0.5,0.5] and CSTR(x > y)
     * - When the ratio is 1% (0.01) bounds of X are kept as [0.0, 100.0]
     * because it's contraction is less than 1%.
     * - When the ratio is 0.1% (0.001) bounds of X are update to [0.5, 100.0]
     * because it's contraction is greater than 0.1%.
     *
     * @param ibexContractionRatio defines the ratio that a domains must be
     *                             contract to compute the constraint.
     * @implNote Supported since ibex-java version 1.2.0
     */
    public void setIbexContractionRatio(double ibexContractionRatio) {
        this.ibexContractionRatio = ibexContractionRatio;
    }

    /**
     * If preserve_rounding is true, Ibex will restore the default
     * Java rounding method when coming back from Ibex, which is
     * transparent for Java but causes a little loss of efficiency.
     * To improve the running time, ibex changes the rounding system
     * for double values during contraction. In Linux/MACOS environments
     * it leads to different results in calculations like `Math.pow(10, 6)`.
     * See issue #740.
     *
     * @param ibexRestoreRounding either Java or ibex rounding method
     * @implNote Supported since ibex-java version 1.2.0
     */
    public Settings setIbexRestoreRounding(boolean ibexRestoreRounding) {
        this.ibexRestoreRounding = ibexRestoreRounding;
        return this;
    }

    /**
     * @return if ibex must restore java rounding mode when returning a call.
     */
    public boolean getIbexRestoreRounding() {
        return ibexRestoreRounding;
    }

    public Object get(String key) {
        return additionalSettings.get(key);
    }

    public Settings set(String key, Object value) {
        this.additionalSettings.put(key, value);
        return this;
    }
}
