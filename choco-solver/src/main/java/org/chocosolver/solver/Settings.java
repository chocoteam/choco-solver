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
import org.chocosolver.solver.search.bind.DefaultSearchBinder;
import org.chocosolver.solver.search.bind.ISearchBinder;

import java.io.Serializable;

/**
 * Settings for {@link org.chocosolver.solver.Solver}.
 * Since java8, acts as default settings.
 * <p>
 * Created by cprudhom on 25/11/14.
 * Project: choco.
 */
public interface Settings extends Serializable {

    enum Idem {
        disabled, // does not anything
        error, // print an error message when a propagator is not guaranteed to be idempotent -- fir debug only
        force // extra call to Propagator.propagate(FULL_PROPAGATION) when no more event is available
    }

    /**
     * Return the welcome message
     */
    default String getWelcomeMessage() {
        return "** Choco 3.3.1 (2015-05) : Constraint Programming Solver, Copyleft (c) 2010-2015";
    }

    /**
     * Define how to react when a propagator is not ensured to be idempotent ({@link org.chocosolver.solver.Settings.Idem}).
     * <ul>
     * <li>disabled : does not anything</li>
     * <li>error: print an error message when a propagator is not guaranteed to be idempotent -- for debugging purpose only</li>
     * <li>force : extra call to Propagator.propagate(FULL_PROPAGATION) when no more event is available</li>
     * </ul>
     */
    default Idem getIdempotencyStrategy() {
        return Idem.disabled;
    }

    /**
     * Set to 'true' to allow the creation of views in the {@link org.chocosolver.solver.variables.VariableFactory}.
     * Creates new variables with channeling constraints otherwise.
     */
    default boolean enableViews() {
        return false;
    }

    /**
     * Define the maximum domain size threshold to force integer variable to be enumerated
     * instead of bounded while calling {@link org.chocosolver.solver.variables.VariableFactory#integer(String, int, int, Solver)}.
     */
    default int getMaxDomSizeForEnumerated() {
        return 32768;
    }

    /**
     * Set to true to replace intension constraints by extension constraints
     */
    default boolean enableTableSubstitution() {
        return true;
    }

    /**
     * Define the maximum domain size threshold to replace intension constraints by extension constraints
     * Only checked when ENABLE_TABLE_SUBS is set to true
     */
    default int getMaxTupleSizeForSubstitution() {
        return 10000;
    }

    /**
     * Set to true to plug explanation engine in.
     */
    default boolean plugExplanationIn() {
        return true;
    }

    /**
     * Define the rounding precision for {@link org.chocosolver.solver.constraints.IntConstraintFactory#multicost_regular(org.chocosolver.solver.variables.IntVar[], org.chocosolver.solver.variables.IntVar[], org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton)} algorithm
     * MUST BE < 13 as java messes up the precisions starting from 10E-12 (34.0*0.05 == 1.70000000000005)
     */
    default double getMCRPrecision() {
        return 4;
    }

    /**
     * Defines the smallest used double for {@link org.chocosolver.solver.constraints.IntConstraintFactory#multicost_regular(org.chocosolver.solver.variables.IntVar[], org.chocosolver.solver.variables.IntVar[], org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton)} algorithm
     */
    default double getMCRDecimalPrecision() {
        return 0.0001d;
    }

    /**
     * Defines, for fine events, for each priority, the queue in which a propagator of such a priority should be scheduled in
     * /!\ for advanced usage only
     */
    default short[] getFineEventPriority() {
        return new short[]{0, 0, 0, 1, 2, 2, 2};
    }

    /**
     * Defines, for coarse events, for each priority, the queue in which a propagator of such a priority should be scheduled in
     * /!\ for advanced usage only
     */
    default short[] getCoarseEventPriority() {
        return new short[]{-1, -1, -1, 0, 1, 2, 3};
    }

    /**
     * Return the search binder
     */
    default ISearchBinder getSearchBinder() {
        return new DefaultSearchBinder();
    }

    /**
     * Return the condition to satisfy when rebuilding history of backtrackable objects is needed.
     * Building "fake" history is needed when a backtrackable object is created during the search, in order to restore a correct state upon backtrack.
     * The default condition is "at least one env.worldPush() has been called since the creation of the bck object".
     * The condition can be set to {@link org.chocosolver.memory.ICondition#FALSE} if no backtrackable object is created during the search.
     */
    default ICondition getEnvironmentHistorySimulationCondition() {
        return new Except_0();
    }

    /**
     * Return true if one wants to be informed of warnings detected during modeling/solving (default value is false)
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
     */
    default boolean enableIncrementalityOnBoolSum(int nbvars) {
        return nbvars > 10;
    }

    /**
     * If your terminal support ANSI colors (Windows terminals don't), you can set this to true.
     * @return enable output with colors
     */
    default boolean outputWithANSIColors(){
        return true;
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
}
