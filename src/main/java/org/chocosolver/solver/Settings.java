/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.memory.Except_0;
import org.chocosolver.memory.ICondition;
import org.chocosolver.solver.constraints.ISatFactory;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;


/**
 * Settings for {@link Model}.
 * Since java8, acts as default settings.
 * <p>
 * Created by cprudhom on 25/11/14.
 * Project: choco.
 * @author Charles Prud'homme
 */
public interface Settings  {

    /**
     * @return the welcome message
     */
    default String getWelcomeMessage() {
        return "** Choco 4.0.4 (2017-04) : Constraint Programming Solver, Copyleft (c) 2010-2017";
    }

    /**
     * Define what to do when a solution is found.
     * By default, it makes a weak check of the model:
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
     * @param solver the solver
     * @return <tt>true</tt> if the model is OK wrt the checker, <tt>false</tt> otherwise
     */
    default boolean checkModel(Solver solver){
        return !ESat.FALSE.equals(solver.isSatisfied());
    }

    /**
     * Set to 'true' to allow the creation of views in the {@link org.chocosolver.solver.Model}.
     * Creates new variables with channeling constraints otherwise.
     * @return <tt>true</tt> if views are enabled.
     */
    default boolean enableViews() {
        return true;
    }

    /**
     * Define the maximum domain size threshold to force integer variable to be enumerated
     * instead of bounded while calling {@link org.chocosolver.solver.Model#intVar(String, int, int)}.
     * @return maximum domain size threshold to force integer variable to be enumerated
     */
    default int getMaxDomSizeForEnumerated() {
        return 32768;
    }

    /**
     * Define the minimum number of cardinality threshold to a sum/scalar constraint to be decomposed in intermediate
     * sub-sums.
     * @return minimum number of cardinality threshold to a sum constraint to be decomposed
     */
    default int getMinCardForSumDecomposition() {
        return 100;
    }

    /**
     * Set to true to replace some intension constraints by extension constraints
     * @return <tt>true</tt> if some intension constraints can be replaced by extension constraints
     */
    default boolean enableTableSubstitution() {
        return true;
    }

    /**
     * Define the maximum domain size threshold to replace intension constraints by extension constraints
     * Only checked when ENABLE_TABLE_SUBS is set to true
     * @return maximum domain size threshold to replace intension constraints by extension constraints
     */
    default int getMaxTupleSizeForSubstitution() {
        return 10000;
    }

    /**
     * Defines the smallest used double for {@link org.chocosolver.solver.Model#multiCostRegular(IntVar[], IntVar[], ICostAutomaton)} algorithm
     * @return the smallest used double for {@link org.chocosolver.solver.Model#multiCostRegular(IntVar[], IntVar[], ICostAutomaton)} algorithm
     */
    default double getMCRDecimalPrecision() {
        return 0.0001d;
    }

    /**
     * Defines, for fine events, for each priority, the queue in which a propagator of such a priority should be scheduled in
     * /!\ for advanced usage only
     * @return the index of queue in which a propagator of a given priority should be scheduled in
     */
    default short[] getFineEventPriority() {
        return new short[]{0, 0, 0, 1, 2, 2, 2};
    }

    /**
     * Defines, for coarse events, for each priority, the queue in which a propagator of such a priority should be scheduled in
     * /!\ for advanced usage only
     * @return the index of queue in which a propagator of a given priority should be scheduled in
     */
    default short[] getCoarseEventPriority() {
        return new short[]{0, 0, 0, 0, 1, 2, 3};
    }

    /**
     * Indicates if propagators can be sorted wrt their priority in {@link org.chocosolver.solver.propagation.PropagationTrigger} when
     * dealing with propagator activation. Set to <tt>true</tt> to allow sorting static propagators.
     * <tt>false</tt> is the default value.
     * @return whether or not propagators are sorted wrt their priority on initial activation.
     */
    default boolean sortPropagatorActivationWRTPriority(){
        return true;
    }

    /**
     * Creates a default search strategy for the input model
     *
     * @param model a model requiring a default search strategy
     * @return a default search strategy for model
     * @see Search#defaultSearch(Model)
     */
    default AbstractStrategy makeDefaultSearch(Model model) {
        return Search.defaultSearch(model);
    }

    /**
     * Return the condition to satisfy when rebuilding history of backtrackable objects is needed.
     * Building "fake" history is needed when a backtrackable object is created during the search, in order to restore a correct state upon backtrack.
     * The default condition is "at least one env.worldPush() has been called since the creation of the bck object".
     * The condition can be set to {@link org.chocosolver.memory.ICondition#FALSE} if no backtrackable object is created during the search.
     * @return the condition to satisfy when rebuilding history of backtrackable objects is needed.
     */
    default ICondition getEnvironmentHistorySimulationCondition() {
        return new Except_0();
    }

    /**
     * Return true if one wants to be informed of warnings detected during modeling/solving (default value is false)
     * @return <tt>true</tt> if warnings detected during modeling/solving are output.
     */
    default boolean warnUser() {
        return false;
    }

    /**
     * When this setting returns true, a complete trace of the events is output.
     * This can be quite big, though, and it slows down the overall process.
     *
     * Note that this parameter is read once at propagation engine creation and set in a final variable.
     * @return true if all events are output in the console
     */
    default boolean debugPropagation(){
        return false;
    }

    /**
     * Return true if boolean sum should be decomposed into an equality constraint and an arithmetic constraint,
     * return false if a single constraint should be used instead.
     * @return <tt>false</tt>
     */
    default boolean enableDecompositionOfBooleanSum(){
        return false;
    }

    /**
     * Return true if the incrementality is enabled on boolean sum, based on the number of variables involved.
     * Default condition is : nbvars > 10
     * @param nbvars number of variables in the constraint
     * @return <tt>true</tt>
     */
    default boolean enableIncrementalityOnBoolSum(int nbvars) {
        return nbvars > 10;
    }

    /**
     * If this setting is set to true (default value), a clone of the input variable array is made in any propagator constructors.
     * This prevents, for instance, wrong behavior when permutations occurred on the input array (e.g., sorting variables).
     * Setting this to false may limit the memory consumption during modelling.
     * @return true if all propagators should clone the input variable array instead of simply referencing it.
     */
    default boolean cloneVariableArrayInPropagator(){
        return true;
    }


    /**
     * If this is set to <tt>true<tt/> then AC algorithm is used to filter ternary sum,
     * otherwise, BC is used.
     * Note AC brings more filtering when there are holes in variable domains but this comes at a cost.
     * @return <tt>true<tt/> if AC is enabled to filter ternary sums.
     */
    default boolean enableACOnTernarySum(){
        return false;
    }

    /**
     * Define the prefix of internally created variables (through a call to {@link Model#generateName()}
     * @return the prefix of all internally created variables
     */
    default String defaultPrefix(){
        return "TMP_";
    }

    /**
     * @return <i>true</i> when an underlying SAT solver is used to manage clauses declared through {@link ISatFactory},
     *         <i>false</i> when clauses are managed with CSP constraints only.
     */
    default boolean enableSAT(){
        return false;
    }

    /**
     * @return <i>true</i> if, on propagator passivation, the propagator is swapped from active to passive in its variables' propagators list.
     * <i>false</i> if, on propagator passivation, only the propagator's state is set to PASSIVE.
     */
    default boolean swapOnPassivate(){
        return false;
    }

}
