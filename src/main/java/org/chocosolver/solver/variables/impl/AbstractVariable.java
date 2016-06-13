/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.impl.scheduler.BoolEvtScheduler;
import org.chocosolver.solver.variables.impl.scheduler.IntEvtScheduler;
import org.chocosolver.solver.variables.impl.scheduler.RealEvtScheduler;
import org.chocosolver.solver.variables.impl.scheduler.SetEvtScheduler;
import org.chocosolver.solver.variables.view.IView;
import org.chocosolver.util.iterators.EvtScheduler;

import java.util.Arrays;

/**
 * Class used to factorise code
 * The subclass must implement Variable interface
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 * @since 30 june 2011
 */
public abstract class AbstractVariable implements Variable {

    /**
     * Message associated with last value removals exception.
     */
    public static final String MSG_REMOVE = "remove last value";

    /**
     * Message associated with domain wipe out exception.
     */
    public static final String MSG_EMPTY = "empty domain";

    /**
     * Message associated with double instantiation exception.
     */
    public static final String MSG_INST = "already instantiated";

    /**
     * Default exception message.
     */
    public static final String MSG_UNKNOWN = "unknown value";

    /**
     * Message associated with wrong upper bound exception.
     */
    public static final String MSG_UPP = "new lower bound is greater than upper bound";

    /**
     * Message associated with wrong lower bound exception.
     */
    public static final String MSG_LOW = "new upper bound is lesser than lower bound";

    /**
     * Message associated with wrong bounds exception.
     */
    public static final String MSG_BOUND = "new bounds are incorrect";

    /**
     * Unique ID of this variable.
     */
    private final int ID;

    /**
     * Reference to the model containing this variable (unique).
     */
    protected final Model model;

    /**
     * Name of the variable.
     */
    protected final String name;

    /**
     * Number of propagators of this variable.
     */
    private int nbPropagators;
    /**
     * List of propagators of this variable.
     */
    private Propagator[] propagators;

    /**
     * Store the index of this variable in each of its propagators.
     */
    private int[] pindices;

    /**
     * Dependency indices, for efficient scheduling purpose.
     */
    private int[] dindices;

    /**
     * List of views based on this variable.
     */
    private IView[] views;

    /**
     * Index of the last not null view in <code>views</code>.
     */
    private int vIdx;

    /**
     * List of monitors observing this variable.
     */
    protected IVariableMonitor[] monitors;

    /**
     * Index of the last not null monitor in <code>monitors</code>.
     */
    protected int mIdx;

    /**
     * The event scheduler of this variable, for efficient scheduling purpose.
     * It stores propagators wrt the propagation conditions.
     */
    private EvtScheduler scheduler;

    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create the shared data of any type of variable.
     * @param name name of the variable
     * @param model model which declares this variable
     */
    protected AbstractVariable(String name, Model model) {
        this.name = name;
        this.model = model;
        this.views = new IView[2];
        this.monitors = new IVariableMonitor[2];
        this.propagators = new Propagator[8];
        this.pindices = new int[8];
        this.dindices = new int[6];
        this.ID = this.model.nextId();
        this.model.associates(this);
        int kind = getTypeAndKind() & Variable.KIND;
        switch (kind) {
            case Variable.BOOL:
                this.scheduler = new BoolEvtScheduler();
                break;
            case Variable.INT:
                this.scheduler = new IntEvtScheduler();
                break;
            case Variable.REAL:
                this.scheduler = new RealEvtScheduler();
                break;
            case Variable.SET:
                this.scheduler = new SetEvtScheduler();
                break;
            default:
                // do not throw exception to allow extending the solver with other variable kinds (e.g. graph)
                // event scheduler may be managed using java reflexion
                break;
        }
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public int link(Propagator propagator, int idxInProp) {
        // 1. ensure capacity
        if (nbPropagators == propagators.length) {
            Propagator[] tmp = propagators;
            propagators = new Propagator[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, propagators, 0, nbPropagators);

            int[] itmp = pindices;
            pindices = new int[itmp.length * 3 / 2 + 1];
            System.arraycopy(itmp, 0, pindices, 0, nbPropagators);
            if(pindices.length != propagators.length){
                throw new UnsupportedOperationException("error: pindices.length != propagators.length in "+this);
            }

        }
        // 2. put it in the right place
        int pc = propagator.getPropagationConditions(idxInProp);
        if(pc > 0) { // deal with VOID, when the propagator should not be aware of this variable's modifications
            subscribe(propagator, idxInProp, scheduler.select(pc));
        }
        return nbPropagators++;
    }


    private void subscribe(Propagator p, int ip, int i) {
        for (int j = 4; j >= i; j--) {
            propagators[dindices[j + 1]] = propagators[dindices[j]];
            pindices[dindices[j + 1]] = pindices[dindices[j]];
            dindices[j + 1] = dindices[j + 1] + 1;
        }
        propagators[dindices[i]] = p;
        pindices[dindices[i]] = ip;
    }

    @Override
    public void unlink(Propagator propagator) {
        int i = 0;
        while (i < nbPropagators && propagators[i] != propagator) {
            i++;
        }
        // Dynamic addition of a propagator may be not considered yet, so the assertion is not correct
        if (i < nbPropagators) {
            cancel(i, scheduler.select(propagator.getPropagationConditions(pindices[i])));
            nbPropagators--;
        }
    }

    private void cancel(int pp, int i) {
        propagators[pp] = propagators[dindices[i + 1] - 1];
        for (int k = i + 1; k < 5; k++) {
            propagators[dindices[k] - 1] = propagators[dindices[k + 1] - 1];
            pindices[dindices[k] - 1] = pindices[dindices[k + 1] - 1];
            dindices[k] = dindices[k] - 1;
        }
        propagators[nbPropagators - 1] = null;
        pindices[nbPropagators - 1] = -1;
        dindices[5] = dindices[5] - 1;
    }

    @Override
    public Propagator[] getPropagators() {
        if (propagators.length > nbPropagators) {
            adaptSize();
        }
        return propagators;
    }

    @Override
    public Propagator getPropagator(int idx) {
        return propagators[idx];
    }

    @Override
    public int getNbProps() {
        return nbPropagators;
    }

    @Override
    public int[] getPIndices() {
        if (pindices.length > nbPropagators) {
            adaptSize();
        }
        return pindices;
    }

    private void adaptSize(){
        assert pindices.length == propagators.length;
        propagators = Arrays.copyOf(propagators, nbPropagators);
        pindices = Arrays.copyOf(pindices, nbPropagators);
    }

    @Override
    public int getDindex(int i) {
        return dindices[i];
    }

    @Override
    public int getIndexInPropagator(int pidx) {
        return pindices[pidx];
    }

    @Override
    public String getName() {
        return this.name;
    }

    ////////////////////////////////////////////////////////////////
    ///// 	methodes 		de 	  l'interface 	  Variable	   /////
    ////////////////////////////////////////////////////////////////

    @Override
    public void notifyPropagators(IEventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        notifyMonitors(event);
        model.getSolver().getEngine().onVariableUpdate(this, event, cause);
        notifyViews(event, cause);
    }

    @Override
    public void notifyViews(IEventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        if (cause == Cause.Null) {
            for (int i = vIdx - 1; i >= 0; i--) {
                views[i].notifyPropagators(event, cause);
            }
        } else {
            for (int i = vIdx - 1; i >= 0; i--) {
                if (views[i] != cause) { // reference is enough
                    views[i].notifyPropagators(event, cause);
                }
            }
        }
    }

    @Override
    public void addMonitor(IVariableMonitor monitor) {
        // 1. check the non redundancy of a monitor
        for (int i = 0; i < mIdx; i++) {
            if (monitors[i] == monitor) return;
        }
        // 2. then add the monitor
        if (mIdx == monitors.length) {
            IVariableMonitor[] tmp = monitors;
            monitors = new IVariableMonitor[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, monitors, 0, mIdx);
        }
        monitors[mIdx++] = monitor;
    }

    @Override
    public void removeMonitor(IVariableMonitor monitor) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void subscribeView(IView view) {
        if (vIdx == views.length) {
            IView[] tmp = views;
            views = new IView[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, views, 0, vIdx);
        }
        views[vIdx++] = view;
    }

    public Model getModel() {
        return model;
    }

    @Override
    public IView[] getViews() {
        return Arrays.copyOfRange(views, 0, vIdx);
    }

    @Override
    public int compareTo(Variable o) {
        return this.getId() - o.getId();
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * @return <tt>true</tt> if this variable has a domain included in [0,1].
     */
    public boolean isBool() {
        return (getTypeAndKind() & KIND) == BOOL;
    }

    /**
     * @return the event scheduler
     */
    @SuppressWarnings("unchecked")
    public EvtScheduler _schedIter() {
        return scheduler;
    }

}
