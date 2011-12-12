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

package solver.variables;

import choco.kernel.common.util.objects.IList;
import choco.kernel.common.util.procedure.TernaryProcedure;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.recorders.list.VariableMonitorListBuilder;
import solver.variables.view.IView;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Class used to factorise code
 * The subclass must implement Variable interface
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 30 june 2011
 */
public abstract class AbstractVariable<V extends Variable> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String
            MSG_REMOVE = "remove last value";
    public static final String MSG_EMPTY = "empty domain";
    public static final String MSG_INST = "already instantiated";
    public static final String MSG_UNKNOWN = "unknown value";
    public static final String MSG_UPP = "new lower bound is greater than upper bound";
    public static final String MSG_LOW = "new upper bound is lesser than lower bound";

    protected static final String NO_NAME = "";

    /**
     * Reference to the solver containing this variable.
     */
    protected final Solver solver;

    protected Constraint[] constraints = new Constraint[8];
    protected int cLast = 0;

    protected final String name;

    /**
     * List of variable monitors
     */
    protected IList<V, IVariableMonitor<V>> records;


    protected IView[] views; // views to inform of domain modification
    protected int vIdx; // index of the last view not null in views -- not backtrable


    protected int modificationEvents;

    protected int uniqueID;

    protected final OnBeforeProc beforeModification = new OnBeforeProc();
    protected final OnAfterProc afterModification = new OnAfterProc();
    protected final OnContradiction onContradiction = new OnContradiction();

    //////////////////////////////////////////////////////////////////////////////////////

    protected AbstractVariable(Solver solver) {
        this(NO_NAME, solver);
    }

    protected AbstractVariable(String name, Solver solver) {
        this.name = name;
        this.solver = solver;
        views = new IView[2];
    }

    protected void makeList(V variable) {
        this.records = VariableMonitorListBuilder.preset(variable, solver.getEnvironment());
    }

    /**
     * Returns the array of constraints <code>this</code> appears in.
     *
     * @return array of constraints
     */
    public Constraint[] getConstraints() {
        if(cLast < constraints.length){
            constraints = Arrays.copyOfRange(constraints, 0, cLast);
        }
        return constraints;
    }

    /**
     * Link a constraint within a variable
     *
     * @param constraint a constraint
     */
    public void declareIn(Constraint constraint) {
        if (cLast >= constraints.length) {
            Constraint[] tmp = constraints;
            constraints = new Constraint[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, constraints, 0, cLast);
        }
        constraints[cLast++] = constraint;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    public void activate(IVariableMonitor monitor) {
        records.setActive(monitor);
    }

    public void desactivate(IVariableMonitor monitor) {
        records.setPassive(monitor);
    }

    public String getName() {
        return this.name;
    }

    ////////////////////////////////////////////////////////////////
    ///// 	methodes 		de 	  l'interface 	  Variable	   /////
    ////////////////////////////////////////////////////////////////

    public void notifyViews(EventType event, ICause cause) throws ContradictionException {
        if (cause == Cause.Null) {
            for (int i = vIdx - 1; i >= 0; i--) {
                views[i].backPropagate(event, cause);
            }
        } else {
            for (int i = vIdx - 1; i >= 0; i--) {
                if (views[i] != cause) { // reference is enough
                    views[i].backPropagate(event, cause);
                }
            }
        }
    }

    public void addMonitor(IVariableMonitor monitor) {
        records.add(monitor, false);
    }

    public void removeMonitor(IVariableMonitor monitor) {
        records.remove(monitor);
    }

    public void subscribeView(IView view) {
        if (vIdx == views.length) {
            IView[] tmp = views;
            views = new IView[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, views, 0, vIdx);
        }
        views[vIdx++] = view;
    }

    public IList getMonitors() {
        return records;
    }

    public int nbConstraints() {
        return records.size();
    }

    public int nbMonitors() {
        return records.cardinality();
    }

    public Solver getSolver() {
        return solver;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected static abstract class Monitoring implements TernaryProcedure<IVariableMonitor, Variable, EventType, ICause> {
        Variable var;
        EventType evt;
        ICause cause;

        @Override
        public TernaryProcedure set(Variable variable, EventType eventType, ICause cause) {
            this.var = variable;
            this.evt = eventType;
            this.cause = cause;
            return this;
        }
    }

    protected static class OnBeforeProc extends Monitoring {
        @Override
        public void execute(IVariableMonitor monitor) throws ContradictionException {
            monitor.beforeUpdate(var, evt, cause);
        }
    }

    protected static class OnAfterProc extends Monitoring {
        @Override
        public void execute(IVariableMonitor monitor) throws ContradictionException {
            monitor.afterUpdate(var, evt, cause);
        }
    }

    protected static class OnContradiction extends Monitoring {
        @Override
        public void execute(IVariableMonitor monitor) throws ContradictionException {
            monitor.contradict(var, evt, cause);
        }
    }

}
