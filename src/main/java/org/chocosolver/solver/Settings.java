/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver;

import org.chocosolver.memory.Except_0;
import org.chocosolver.memory.ICondition;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.search.strategy.SearchStrategyFactory;
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
     * List possible reaction to lack of propagator's idempotency.
     */
    enum Idem {
        /**
         * Does nothing.
         */
        disabled,
        /**
         * Prints an error message when a propagator is not guaranteed to be idempotent -- for debug only
         */
        error,
        /**
         * Extra call to Propagator.propagate(FULL_PROPAGATION) when no more event is available
         */
        force
    }

    /**
     * @return the welcome message
     */
    default String getWelcomeMessage() {
        return "** Choco 3.3.3 (2015-12) : Constraint Programming Solver, Copyleft (c) 2010-2015";
    }

    /**
     * Define how to react when a propagator is not ensured to be idempotent ({@link org.chocosolver.solver.Settings.Idem}).
     * <ul>
     * <li>disabled : does not anything</li>
     * <li>error: print an error message when a propagator is not guaranteed to be idempotent -- for debugging purpose only</li>
     * <li>force : extra call to Propagator.propagate(FULL_PROPAGATION) when no more event is available</li>
     * </ul>
     * @return the idempotency strategy
     */
    default Idem getIdempotencyStrategy() {
        return Idem.disabled;
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
     * @see SearchStrategyFactory#defaultSearch(Model)
     */
    default AbstractStrategy makeDefaultSearch(Model model) {
        return SearchStrategyFactory.defaultSearch(model);
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
     * Note that enabling colors may be helpful (see {@link #outputWithANSIColors()})
     * @return true if all events are output in the console
     */
    default boolean debugPropagation(){
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
     * If your terminal support ANSI colors (Windows terminals don't), you can set this to true.
     * @return enable output with colors
     */
    default boolean outputWithANSIColors(){
        return false;
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

}
