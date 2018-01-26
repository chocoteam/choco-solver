/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
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
public interface Settings  {

    /**
     * @return the welcome message
     */
    String getWelcomeMessage() ;

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
     * Set whether propagators are sorted wrt their priority in {@link org.chocosolver.solver.propagation.PropagationTrigger} when
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
     * @return true if all events are output in the console
     */
    boolean debugPropagation();

    /**
     * When this setting returns {@code true}, a complete trace of the events is output.
     * This can be quite big, though, and it slows down the overall process.
     *
     * Note that this parameter is read once at propagation engine creation and set in a final variable.
     * @param debugPropagation {@code true} to output a complete trace of the propagated events.
     * @return the current instance
     */
    Settings setDebugPropagation(boolean debugPropagation);

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
     * @return <tt>true<tt/> if AC is enabled to filter ternary sums by default.
     */
    boolean enableACOnTernarySum();

    /**
     * If this is set to <tt>true<tt/> then AC algorithm is used to filter ternary sum by default,
     * otherwise, BC is used.
     * Note AC brings more filtering when there are holes in variable domains but this comes at a cost.
     * @param enable {@code true} to enable AC by default
     * @return the current instance
     */
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
    default boolean swapOnPassivate(){
        return false;
    }

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
}
