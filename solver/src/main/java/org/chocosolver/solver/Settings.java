/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.memory.ICondition;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.ISatFactory;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;


/**
 * Settings for {@link Model}.
 *
 * <p>
 * Created by cprudhom on 25/11/14.
 * Project: choco.
 * @author Charles Prud'homme
 */
public interface Settings {

    /**
     * Load <b>some</b> settings from a property file.
     * The following settings cannot be loaded from a property file:
     * <ul>
     *    <li>{@link #setModelChecker(Predicate)}</li>
     *    <li>{@link #setDefaultSearch(Function)}</li>
     *    <li>{@link #setInitSolver(Function)}        </li>
     *    <li>{@link #setEnvironmentHistorySimulationCondition(ICondition)}</li>
     *    <li>{@link #setEnableIncrementalityOnBoolSum(IntPredicate)}           </li>
     * </ul>
     * @param properties a property file to load setting from.
     * @return the current instance
     */
    default Settings load(Properties properties) {
        this.setWelcomeMessage((String) properties.getOrDefault(
                "welcome.message", this.getWelcomeMessage()));
        this.setEnableViews(Boolean.parseBoolean(properties.getOrDefault(
                "views.activate", this.enableViews()).toString()));
        this.setMaxDomSizeForEnumerated(Integer.parseInt(properties.getOrDefault(
                "enumerated.threshold", this.getMaxDomSizeForEnumerated()).toString()));
        this.setMinCardinalityForSumDecomposition(Integer.parseInt(properties.getOrDefault(
                "sum.decomposition.threshold", this.getMinCardForSumDecomposition()).toString()));
        this.setEnableTableSubstitution(Boolean.parseBoolean(properties.getOrDefault(
                "table.substitution", enableTableSubstitution()).toString()));
        this.setMCRDecimalPrecision(Double.parseDouble(properties.getOrDefault(
                "MCR.precision", this.getMCRDecimalPrecision()).toString()));
        this.setMaxTupleSizeForSubstitution(Integer.parseInt(properties.getOrDefault(
                "tuple.threshold", this.getMaxTupleSizeForSubstitution()).toString()));
        this.setSortPropagatorActivationWRTPriority(Boolean.parseBoolean(properties.getOrDefault(
                "propagators.sort", this.sortPropagatorActivationWRTPriority()).toString()));
        this.setWarnUser(Boolean.parseBoolean(properties.getOrDefault(
                "user.warn", this.warnUser()).toString()));
        this.setEnableDecompositionOfBooleanSum(Boolean.parseBoolean(properties.getOrDefault(
                "boolsum.decomposition", this.enableDecompositionOfBooleanSum()).toString()));
        this.setCloneVariableArrayInPropagator(Boolean.parseBoolean(properties.getOrDefault(
                "propagators.clonevars", this.cloneVariableArrayInPropagator()).toString()));
        this.setDefaultPrefix((String) properties.getOrDefault(
                "variables.prefix", this.defaultPrefix()));
        this.setEnableSAT(Boolean.parseBoolean(properties.getOrDefault(
                "satsolver.activate", this.enableSAT()).toString()));
        this.setSwapOnPassivate(Boolean.parseBoolean(properties.getOrDefault(
                "propagators.swap", this.swapOnPassivate()).toString()));
        this.setCheckDeclaredConstraints(Boolean.parseBoolean(properties.getOrDefault(
                "constraints.check", this.checkDeclaredConstraints()).toString()));
        this.setHybridizationOfPropagationEngine(Byte.parseByte(properties.getOrDefault(
                "propagationEngine.hybridization", this.enableHybridizationOfPropagationEngine()).toString()));
        this.setNbMaxLearntClauses(Integer.parseInt(properties.getOrDefault(
                "learnt.nbMax", this.getNbMaxLearntClauses()).toString()));
        this.setRatioForClauseStoreReduction(Float.parseFloat(properties.getOrDefault(
                "learnt.ratio", this.getRatioForClauseStoreReduction()).toString()));
        this.setMaxLearntClauseCardinality(Integer.parseInt(properties.getOrDefault(
                "learnt.maxCard", this.getMaxLearntClauseCardinality()).toString()));
        this.setLearntClausesDominancePerimeter(Integer.parseInt(properties.getOrDefault(
                "learnt.dominance", this.getLearntClausesDominancePerimeter()).toString()));
        this.explainGlobalFailureInSum(Boolean.parseBoolean(properties.getOrDefault(
                "learnt.sum.global", this.explainGlobalFailureInSum()).toString()));
        this.setIbexContractionRatio(Double.parseDouble(properties.getOrDefault(
                "constraints.ibex.contractionRation", this.getIbexContractionRatio()).toString()));
        this.setIbexRestoreRounding(Boolean.parseBoolean(properties.getOrDefault(
                "constraints.ibex.restoreRounding", this.getIbexRestoreRounding()).toString()));
        return this;
    }

    /**
     * Load <b>some</b> settings from a property file.
     * The following settings cannot be loaded from a property file:
     * <ul>
     *    <li>{@link #setModelChecker(Predicate)}</li>
     *    <li>{@link #setDefaultSearch(Function)}</li>
     *    <li>{@link #setInitSolver(Function)}        </li>
     *    <li>{@link #setEnvironmentHistorySimulationCondition(ICondition)}</li>
     *    <li>{@link #setEnableIncrementalityOnBoolSum(IntPredicate)}           </li>
     * </ul>
     * @param      inStream   the input stream.
     * @exception IOException  if an error occurred when reading from the
     *             input stream.
     * @throws IllegalArgumentException if the input stream contains a
     *             malformed Unicode escape sequence.
     * @return the current instance
     */
    default Settings load(InputStream inStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inStream);
        load(properties);
        return this;
    }

    /**
     * Store <b>some</b> settings into a property file.
     * The following settings cannot be stored into a property file:
     * <ul>
     *    <li>{@link #setModelChecker(Predicate)}</li>
     *    <li>{@link #setDefaultSearch(Function)}</li>
     *    <li>{@link #setInitSolver(Function)}        </li>
     *    <li>{@link #setEnvironmentHistorySimulationCondition(ICondition)}</li>
     *    <li>{@link #setEnableIncrementalityOnBoolSum(IntPredicate)}           </li>
     * </ul>
     * @return the property file
     */
    default Properties store() {
        Properties properties = new Properties();
        properties.setProperty("welcome.message", this.getWelcomeMessage());
        properties.setProperty("views.activate", Boolean.toString(this.enableViews()));
        properties.setProperty("enumerated.threshold", Integer.toString(this.getMaxDomSizeForEnumerated()));
        properties.setProperty("sum.decomposition.threshold", Integer.toString(this.getMinCardForSumDecomposition()));
        properties.setProperty("table.substitution", Boolean.toString(this.enableTableSubstitution()));
        properties.setProperty("MCR.precision", Double.toString(this.getMCRDecimalPrecision()));
        properties.setProperty("tuple.threshold", Integer.toString(this.getMaxTupleSizeForSubstitution()));
        properties.setProperty("propagators.sort", Boolean.toString(this.sortPropagatorActivationWRTPriority()));
        properties.setProperty("user.warn", Boolean.toString(this.warnUser()));
        properties.setProperty("boolsum.decomposition", Boolean.toString(this.enableDecompositionOfBooleanSum()));
        properties.setProperty("propagators.clonevars", Boolean.toString(this.cloneVariableArrayInPropagator()));
        properties.setProperty("variables.prefix", this.defaultPrefix());
        properties.setProperty("satsolver.activate", Boolean.toString(enableSAT()));
        properties.setProperty("propagators.swap", Boolean.toString(swapOnPassivate()));
        properties.setProperty("constraints.check", Boolean.toString(checkDeclaredConstraints()));
        properties.setProperty("constraints.check.printall", Boolean.toString(printAllUndeclaredConstraints()));
        properties.setProperty("propagationEngine.hybridization", Byte.toString(enableHybridizationOfPropagationEngine()));
        properties.setProperty("learnt.nbMax", Integer.toString(this.getNbMaxLearntClauses()));
        properties.setProperty("learnt.ratio", Float.toString(this.getRatioForClauseStoreReduction()));
        properties.setProperty("learnt.maxCard", Integer.toString(this.getMaxLearntClauseCardinality()));
        properties.setProperty("learnt.dominance", Integer.toString(this.getLearntClausesDominancePerimeter()));
        properties.setProperty("learnt.sum.global", Boolean.toString(this.explainGlobalFailureInSum()));
        properties.setProperty("constraints.ibex.contractionRation", Double.toString(this.getIbexContractionRatio()));
        properties.setProperty("constraints.ibex.restoreRounding", Boolean.toString(this.getIbexRestoreRounding()));
        return properties;
    }

    /**
     * Store <b>some</b> settings into a property file.
     * The following settings cannot be stored into a property file:
     * <ul>
     *    <li>{@link #setModelChecker(Predicate)}</li>
     *    <li>{@link #setDefaultSearch(Function)}</li>
     *    <li>{@link #setInitSolver(Function)}        </li>
     *    <li>{@link #setEnvironmentHistorySimulationCondition(ICondition)}</li>
     *    <li>{@link #setEnableIncrementalityOnBoolSum(IntPredicate)}           </li>
     * </ul>
     * @param   out      an output stream.
     * @param   comments   a description of the property list.
     * @exception IOException if writing this property list to the specified
     *             output stream throws an <tt>IOException</tt>.
     */
    default void store(OutputStream out, String comments) throws IOException {
        store().store(out, comments);
    }

    /**
     * @return the welcome message
     */
    String getWelcomeMessage();

    /**
     * Define the welcome message, printed in the console
     *
     * @param welcomeMessage a message
     * @return the current instance
     */
    Settings setWelcomeMessage(String welcomeMessage);

    /**
     * @param solver the solver
     * @return <tt>true</tt> if the model is OK wrt the checker, <tt>false</tt> otherwise
     */
    boolean checkModel(Solver solver);

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
    Settings setModelChecker(Predicate<Solver> modelChecker);

    /**
     * @return <tt>true</tt> if views are enabled.
     */
    boolean enableViews();

    /**
     * Set to 'true' to allow the creation of views in the {@link org.chocosolver.solver.Model}.
     * Creates new variables with channeling constraints otherwise.
     *
     * @param enableViews {@code true} to enable views
     * @return the current instance
     */
    Settings setEnableViews(boolean enableViews);

    /**
     * @return maximum domain size threshold to force integer variable to be enumerated
     */
    int getMaxDomSizeForEnumerated();

    /**
     * Define the minimum number of cardinality threshold to a sum/scalar constraint to be decomposed in intermediate
     * sub-sums.
     * @param maxDomSizeForEnumerated cardinality threshold
     * @return the current instance
     */
    Settings setMaxDomSizeForEnumerated(int maxDomSizeForEnumerated);

    /**
     * @return minimum number of cardinality threshold to a sum constraint to be decomposed
     */
    int getMinCardForSumDecomposition();

    /**
     * Define the default minimum number of cardinality threshold to a sum/scalar constraint to be
     * decomposed into intermediate sub-sums.
     * @param defaultMinCardinalityForSumDecomposition cardinality threshold
     * @return the current instance
     */
    Settings setMinCardinalityForSumDecomposition(int defaultMinCardinalityForSumDecomposition);

    /**
     * @return <tt>true</tt> if some intension constraints can be replaced by extension constraints
     */
    boolean enableTableSubstitution();

    /**
     * Define whether some intension constraints are replaced by extension constraints
     *
     * @param enableTableSubstitution enable table substitution
     * @return the current instance
     */
    Settings setEnableTableSubstitution(boolean enableTableSubstitution);

    /**
     * @return maximum domain size threshold to replace intension constraints by extension constraints
     */
    int getMaxTupleSizeForSubstitution();

    /**
     * @return the smallest used double for {@link org.chocosolver.solver.Model#multiCostRegular(IntVar[], IntVar[], ICostAutomaton)} algorithm
     */
    double getMCRDecimalPrecision();

    /**
     * Defines the default smallest used double for {@link org.chocosolver.solver.Model#multiCostRegular(IntVar[], IntVar[], ICostAutomaton)} algorithm
     * @param precision default precision for MCR
     * @return the current instance
     */
    Settings setMCRDecimalPrecision(double precision);

    /**
     * Define the maximum domain size threshold to replace intension constraints by extension constraints
     * Only checked when {@link #enableTableSubstitution()} returns {@code true}
     * @param maxTupleSizeForSubstitution threshold to substitute intension constraint by table one.
     * @return the current instance
     */
    Settings setMaxTupleSizeForSubstitution(int maxTupleSizeForSubstitution);

    /**
     * @return {@code true} if propagators are sorted wrt their priority on initial activation.
     */
    boolean sortPropagatorActivationWRTPriority();

    /**
     * Set whether propagators are sorted wrt their priority in {@link org.chocosolver.solver.propagation.PropagationEngine} when
     * dealing with propagator activation.
     *
     * @param sortPropagatorActivationWRTPriority {@code true} to allow sorting static propagators.
     * @return the current instance
     */
    Settings setSortPropagatorActivationWRTPriority(boolean sortPropagatorActivationWRTPriority);

    /**
     * Creates a default search strategy for the input model
     *
     * @param model a model requiring a default search strategy
     * @return a default search strategy for model
     * @see Search#defaultSearch(Model)
     */
    AbstractStrategy makeDefaultSearch(Model model);

    /**
     * Define a default search strategy for the input model
     * @param defaultSearch what default search strategy should be
     * @return the current instance
     */
    Settings setDefaultSearch(Function<Model, AbstractStrategy> defaultSearch);

    /**
     * @return the condition to satisfy when rebuilding history of backtrackable objects is needed.
     */
    ICondition getEnvironmentHistorySimulationCondition();

    /**
     * Set the condition to satisfy when rebuilding history of backtrackable objects is needed.
     * Building "fake" history is needed when a backtrackable object is created during the search, in order to restore a correct state upon backtrack.
     * The default condition is "at least one env.worldPush() has been called since the creation of the bck object".
     * The condition can be set to {@link org.chocosolver.memory.ICondition#FALSE} if no backtrackable object is created during the search.
     *
     * @param environmentHistorySimulationCondition the condition to satisfy when rebuilding history of backtrackable objects is needed.
     * @return the current instance
     */
    Settings setEnvironmentHistorySimulationCondition(ICondition environmentHistorySimulationCondition);

    /**
     * @return <tt>true</tt> if warnings detected during modeling/solving are output.
     */
    boolean warnUser();

    /**
     * To be informed of warnings detected during modeling/solving
     * @param warnUser {@code true} to be print warnings on console
     * @return the current instance
     */
    Settings setWarnUser(boolean warnUser);

    /**
     * @return {@code true} if boolean sum should be decomposed into an equality constraint and an arithmetic constraint,
     * {@code false}if a single constraint should be used instead.
     */
    boolean enableDecompositionOfBooleanSum();

    /**
     * Define if boolean sums should be decomposed into an equality constraint + arithmetic constraint
     * @param enableDecompositionOfBooleanSum {@code true} to enable decomposition
     * @return the current instance
     */
    Settings setEnableDecompositionOfBooleanSum(boolean enableDecompositionOfBooleanSum);

    /**
     * @param nbvars number of variables in the constraint
     * @return {@code true} if the incrementality is enabled on boolean sum, based on the number of variables involved.
     */
    boolean enableIncrementalityOnBoolSum(int nbvars);

    /**
     * Define the predicate to choose incremental sum, based on number variables declared
     * @param enableIncrementalityOnBoolSum predicate to pick declare sum
     * @return the current instance
     */
    Settings setEnableIncrementalityOnBoolSum(IntPredicate enableIncrementalityOnBoolSum);

    /**
     * @return true if all propagators should clone the input variable array instead of simply referencing it.
     */
    boolean cloneVariableArrayInPropagator();

    /**
     * If this setting is set to true (default value), a clone of the input variable array is made in any propagator constructors.
     * This prevents, for instance, wrong behavior when permutations occurred on the input array (e.g., sorting variables).
     * Setting this to false may limit the memory consumption during modelling.
     * @param cloneVariableArrayInPropagator {@code true} to clone variables array on constructor
     * @return the current instance
     */
    Settings setCloneVariableArrayInPropagator(boolean cloneVariableArrayInPropagator);

    /**
     * @deprecated
     */
    @Deprecated
    boolean enableACOnTernarySum();

    /**
     * @deprecated
     */
    @Deprecated
    Settings setEnableACOnTernarySum(boolean enable);

    /**
     * Define the prefix of internally created variables (through a call to {@link Model#generateName()}
     * @return the prefix of all internally created variables
     */
    String defaultPrefix();

    /**
     * Define prefix of internally created variables
     * @param defaultPrefix prefix of internally created variables' name
     * @return the current instance
     */
    Settings setDefaultPrefix(String defaultPrefix);

    /**
     * @return <i>true</i> when an underlying SAT solver is used to manage clauses declared through {@link ISatFactory},
     *         <i>false</i> when clauses are managed with CSP constraints only.
     */
    boolean enableSAT();

    /**
     * Indicate if clauses are managed by a unique SAT solver.
     * @param enableSAT {@code true} to rely on SAT Solver to handle clauses
     * @return the current instance
     */
    Settings setEnableSAT(boolean enableSAT);

    /**
     * @return <i>true</i> if, on propagator passivation, the propagator is swapped from active to passive in its variables' propagators list.
     * <i>false</i> if, on propagator passivation, only the propagator's state is set to PASSIVE.
     */
    boolean swapOnPassivate();

    /**
     * Define if passivation of propagator swap it in variables' list
     * @param swapOnPassivate {@code true} to enable swapping
     * @return the current instance
     */
    Settings setSwapOnPassivate(boolean swapOnPassivate);

    /**
     * @return <i>true</i> (default value) to check if all declared constraints are not free anymore,
     * that is either posted or reified, before running the resolution.
     * <i>false</i> to skip the control.
     */
    boolean checkDeclaredConstraints();

    /**
     * Indicate if the declared constraints are either posted or reified.
     * @param checkDeclaredConstraints  {@code true} to check constraints before resolution
     * @return the current instance
     */
    Settings setCheckDeclaredConstraints(boolean checkDeclaredConstraints);

    /**
     * @return <i>true</i> to list all undeclared constraint, <i>false</i> (default value) otherwise.
     * Only active when {@link #checkDeclaredConstraints()} is on.
     */
    boolean printAllUndeclaredConstraints();

    /**
     * Indicate if all undeclared constraints are listed on console when {@link #checkDeclaredConstraints()} is on.
     * @param printAllUndeclaredConstraints  {@code true} to list all undeclared constraints
     * @return the current instance
     */
    Settings setPrintAllUndeclaredConstraints(boolean printAllUndeclaredConstraints);

    /**
     * This method is called in {@link Model#Model(IEnvironment, String, Settings)} to create the
     * solver to associate with a model.
     * @param model a model to initialize with a solver
     * @return the new solver
     */
    Solver initSolver(Model model);

    /**
     * Define the solver initialization
     *
     * @param initSolver function to initialize the solver
     * @return the current instance
     */
    Settings setInitSolver(Function<Model, Solver> initSolver);

    /**
     * @return <i>0b00<i/> if constraint-oriented propagation engine,
     * <i>0b01<i/> if hybridization between variable and constraint oriented and
     * <i>0b10<i/> if variable-oriented.
     */
    byte enableHybridizationOfPropagationEngine();

    /**
     * Define behavior of the propagation engine.
     * @param hybrid When set to '0b00', this works as a constraint-oriented propagation engine;
     * when set to '0b01', this workds as an hybridization between variable and constraint oriented
     * propagation engine.
     * when set to '0b10', this workds as a variable- oriented propagation engine.
     * @return the current instance
     */
    Settings setHybridizationOfPropagationEngine(byte hybrid);

    /**
     * @return maximum number of learnt clauses to store. When reached, a reduction is applied.
     * @see #setNbMaxLearntClauses(int)
     * @see #setRatioForClauseStoreReduction(float)
     * @see #getRatioForClauseStoreReduction()
     * @see #setMaxLearntClauseCardinality(int)
     * @see #getMaxLearntClauseCardinality()
     */
    int getNbMaxLearntClauses();

    /**
     * Set the maximum of number of learnt clauses to store before running a reduction of the store.
     * @param n maximum number of learnt clauses before reducing the store.
     * @see #getNbMaxLearntClauses()
     * @see #setRatioForClauseStoreReduction(float)
     * @see #getRatioForClauseStoreReduction()
     * @see #setMaxLearntClauseCardinality(int)
     * @see #getMaxLearntClauseCardinality()
     * @return the current instance
     */
    Settings setNbMaxLearntClauses(int n);

    /**
     * when clauses store need to be reduced, 'ratio' of them are kept (between  0.1 and .99)
     *
     * @see #setRatioForClauseStoreReduction(float)
     * @see #setNbMaxLearntClauses(int)
     * @see #getNbMaxLearntClauses()
     * @see #setMaxLearntClauseCardinality(int)
     * @see #getMaxLearntClauseCardinality()
     */
    float getRatioForClauseStoreReduction();

    /**
     * when clauses store need to be reduced, 'ratio' of them are kept (between  0.1 and .99).
     * A call to this defines 'ratio'.
     * @param f ratio for clause store reduction
     *
     * @see #getRatioForClauseStoreReduction()
     * @see #setNbMaxLearntClauses(int)
     * @see #getNbMaxLearntClauses()
     * @see #setMaxLearntClauseCardinality(int)
     * @see #getMaxLearntClauseCardinality()
     * @return the current instance
     */
    Settings setRatioForClauseStoreReduction(float f);

    /**
     * @return maximum learnt clause cardinality, clauses beyond this value are ignored.
     * @see #setMaxLearntClauseCardinality(int)
     * @see #setNbMaxLearntClauses(int)
     * @see #setRatioForClauseStoreReduction(float)
     * @see #getRatioForClauseStoreReduction()
     * @see #setRatioForClauseStoreReduction(float)
     */
    int getMaxLearntClauseCardinality();

    /**
     * Set the maximum learnt clause cardinality, clauses beyond this value are ignored.
     * @param n maximum learnt clause cardinality.
     * @see #getMaxLearntClauseCardinality()
     * @see #getNbMaxLearntClauses()
     * @see #setRatioForClauseStoreReduction(float)
     * @see #getRatioForClauseStoreReduction()
     * @see #setRatioForClauseStoreReduction(float)
     * @return the current instance
     */
    Settings setMaxLearntClauseCardinality(int n);

    /**
     * When a clause is learnt from a conflict, it may happen that it dominates previously learnt ones.
     * The dominance will be evaluated with the <i>n</i> last learnt clauses.
     * n = 0 means no dominance check, n = {@link Integer#MAX_VALUE} means checking all clauses with the last one.
     * @return dominance perimeter
     */
    int getLearntClausesDominancePerimeter();

    /**
     * When a clause is learnt from a conflict, it may happen that it dominates previously learnt ones.
     * The dominance will be evaluated with the <i>n</i> last learnt clauses.
     * n = 0 means no dominance check, n = {@link Integer#MAX_VALUE} means checking all clauses with the last one.
     * @return dominance perimeter
     */
    Settings setLearntClausesDominancePerimeter(int n);

    /**
     * @return <i>true</i> if additional clauses can be learned from sum's global failure
     */
    boolean explainGlobalFailureInSum();

    /**
     * Set to <i>true</i> to allow additional clauses to be learned from sum's global failure
     */
    Settings explainGlobalFailureInSum(boolean b);

    /**
     * Defines the ratio that real domains must be contracted by ibex
     * to compute the constraint. A contraction is considered as significant
     * when at least {@param ratio} of a domain has been reduced.
     * If the contraction is not meet, then it is considered as insufficient
     * and therefore ignored. A too small ratio can degrade the ibex performance.
     * The default value is 1% (0.01). See issue #653.
     *
     * Example: given x = [0.0, 100.0], y = [0.5,0.5] and CSTR(x > y)
     * - When the ratio is 1% (0.01) bounds of X are kept as [0.0, 100.0]
     *   because it's contraction is less than 1%.
     * - When the ratio is 0.1% (0.001) bounds of X are update to [0.5, 100.0]
     *   because it's contraction is greater than 0.1%.
     *
     * @implNote Supported since ibex-java version 1.2.0
     *
     * @param ibexContractionRatio defines the ratio that a domains must be
     *                             contract to compute the constraint.
     */
    void setIbexContractionRatio(double ibexContractionRatio);

    /**
     * @return the ratio that a domains must be contracted by ibex to compute the constraint.
     */
    double getIbexContractionRatio();

    /**
     * If preserve_rounding is true, Ibex will restore the default
     * Java rounding method when coming back from Ibex, which is
     * transparent for Java but causes a little loss of efficiency.
     * To improve the running time, ibex changes the rounding system
     * for double values during contraction. In Linux/MACOS environments
     * it leads to different results in calculations like `Math.pow(10, 6)`.
     * See issue #740.
     *
     * @implNote Supported since ibex-java version 1.2.0
     *
     * @param ibexRestoreRounding
     */
    void setIbexRestoreRounding(boolean ibexRestoreRounding);

    /**
     * @return if ibex must restore java rounding mode when returning a call.
     */
    boolean getIbexRestoreRounding();

}
