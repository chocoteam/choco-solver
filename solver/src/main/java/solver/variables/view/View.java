/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables.view;

import choco.kernel.common.util.objects.IList;
import com.sun.istack.internal.NotNull;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.recorders.fine.IModifier;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.IntVar;
import solver.variables.delta.IntDelta;

/**
 * "A view implements the same operations as a variable. A view stores a reference to a variable.
 * Invoking an operation on the view exectutes the appropriate operation on the view's varaible."
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public abstract class View<IV extends IntVar> implements IntVar {

    protected final String name;

    protected final IV var;

    protected int uniqueID;

    protected final Solver solver;

    public View(String name, IV var, Solver solver) {
        this.name = name;
        this.var = var;
        this.solver = solver;
    }

    public IV getVariable(){
        return var;
    }

    /**
     * Return the modifier, if any, to consider during the event recordre creation.
     *
     * @return an event modifier or null, if not required
     */
    public IModifier getModifier() {
        return IModifier.Default.NO;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    @Override
    public int getDomainSize() {
        return var.getDomainSize();
    }

    @Override
    public void setHeuristicVal(HeuristicVal heuristicVal) {
        //useless: based on var heuristic val
    }

    @Override
    public HeuristicVal getHeuristicVal() {
        return var.getHeuristicVal();
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return var.hasEnumeratedDomain();
    }

    @Override
    public IntDelta getDelta() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void activate(IVariableMonitor monitor) {
        var.activate(monitor);
    }

    @Override
    public void desactivate(IVariableMonitor monitor) {
        var.desactivate(monitor);
    }

    @Override
    public boolean instantiated() {
        return var.instantiated();
    }

    @Override
    public void addMonitor(IVariableMonitor monitor) {
        var.addMonitor(monitor);
    }

    @Override
    public void removeMonitor(IVariableMonitor monitor) {
        var.removeMonitor(monitor);
    }

    @Override
    public void subscribeView(IView view) {
        var.subscribeView(view);
    }

    @Override
    public IList getMonitors() {
        return var.getMonitors();
    }

    @Override
    public int nbConstraints() {
        return var.nbConstraints();
    }

    @Override
    public Explanation explain(VariableState what) {
        return var.explain(what);
    }

    @Override
    public int nbMonitors() {
        return var.nbMonitors();
    }

    @Override
    public void updatePropagationConditions(Propagator propagator, int idxInProp) {
        var.updatePropagationConditions(propagator, idxInProp);
    }

    @Override
    public void notifyMonitors(EventType event, ICause cause) throws ContradictionException {
        var.notifyMonitors(event, cause);
    }

    @Override
    public void notifyViews(EventType event, @NotNull ICause cause) throws ContradictionException {
        var.notifyViews(event, cause);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
        var.contradiction(cause, event, message);
    }


    @Override
    public Solver getSolver() {
        return solver;
    }
}
