/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.propagation;


import memory.IEnvironment;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.propagation.generator.Arc;
import solver.propagation.hardcoded.util.AId2AbId;
import solver.propagation.hardcoded.util.IId2AbId;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class of IPropagatioEngine.
 * It allows scheduling and propagation of ISchedulable object, like Arc or Group.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/12/11
 */
public class DSLEngine implements IPropagationEngine {

    protected final ContradictionException exception;

    protected IPropagationStrategy propagationStrategy;

    protected final Variable[] variables;
    protected int[] vcidx;
    protected final Propagator[] propagators;
    protected int[] pcidx;

    protected Arc[][] fines_v;
    protected Arc[][] fines_p;
    protected final IId2AbId v2i; // mapping between propagator ID and its absolute index
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index

    protected IEnvironment environment;

    private boolean init;

    final PropagationTrigger trigger; // an object that starts the propagation


    public DSLEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();
        this.trigger = new PropagationTrigger(this, solver);

        variables = solver.getVars();
        int nbVar = 0;
        int m = Integer.MAX_VALUE, M = Integer.MIN_VALUE;
        for (; nbVar < variables.length; nbVar++) {
            int id = variables[nbVar].getId();
            m = Math.min(m, id);
            M = Math.max(M, id);
        }
        v2i = new AId2AbId(m, M, -1);
        fines_v = new Arc[nbVar][];
        for (int i = 0; i < nbVar; i++) {
            int id = variables[i].getId();
            v2i.set(id, i);
            fines_v[i] = new Arc[variables[i].getNbProps()];
        }
        vcidx = new int[nbVar];
        List<Propagator> _propagators = new ArrayList<Propagator>();
        Constraint[] constraints = solver.getCstrs();
        int nbProp = 0;
        m = Integer.MAX_VALUE;
        M = Integer.MIN_VALUE;
        for (int c = 0; c < constraints.length; c++) {
            Propagator[] cprops = constraints[c].getPropagators();
            for (int j = 0; j < cprops.length; j++, nbProp++) {
                _propagators.add(cprops[j]);
                int id = cprops[j].getId();
                m = Math.min(m, id);
                M = Math.max(M, id);
            }
        }
        propagators = _propagators.toArray(new Propagator[_propagators.size()]);
        trigger.addAll(propagators);

        p2i = new AId2AbId(m, M, -1);
        //        p2i = new MId2AbId(M - m + 1, -1);
        fines_p = new Arc[nbProp][];
        for (int j = 0; j < propagators.length; j++) {
            p2i.set(propagators[j].getId(), j);
            fines_p[j] = new Arc[propagators[j].getNbVars()];
        }
        pcidx = new int[nbProp];
        init = true;
    }

    /**
     * Attach a strategy to <code>this</code>.
     * Override previously defined one.
     *
     * @param propagationStrategy a group
     * @return this
     */
    public IPropagationEngine set(IPropagationStrategy propagationStrategy) {
        this.propagationStrategy = propagationStrategy;
        return this;
    }

    @Override
    public boolean isInitialized() {
        return init;
    }

    @Override
    public void propagate() throws ContradictionException {
        if (trigger.needToRun()) {
            trigger.propagate();
        }
        propagationStrategy.execute();
        assert propagationStrategy.isEmpty();
    }

    @Override
    public void flush() {
        propagationStrategy.flush();
    }

    @Override
    public void fails(ICause cause, Variable variable, String message) throws ContradictionException {
        throw exception.set(cause, variable, message);
    }

    @Override
    public ContradictionException getContradictionException() {
        return exception;
    }

    public void declareArc(Arc arc) {
        Variable var = arc.var;
        Propagator prop = arc.prop;

        int id = p2i.get(prop.getId());
        fines_p[id][pcidx[id]++] = arc;
        id = v2i.get(var.getId());
        fines_v[id][vcidx[id]++] = arc;
    }

    @Override
    public void clear() {
        throw new SolverException("Clearing the engine is not enough!");//CPRU: to do
    }

    @Override
    public String toString() {
        return propagationStrategy.toString();
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        if (Configuration.PRINT_VAR_EVENT) {
            IPropagationEngine.Trace.printModification(variable, type, cause);
        }
        int id = v2i.get(variable.getId());
        int to = vcidx[id];
        Arc arc;
        for (int i = 0; i < to; i++) {
            arc = fines_v[id][i];
            if (arc.prop != cause && arc.prop.isActive() && arc.prop.advise(arc.idxVinP, type.mask)) {
                arc.update(type);
            }
        }
    }

    @Override
    public void delayedPropagation(Propagator propagator, EventType type) throws ContradictionException {
        if (propagator.getNbPendingEvt() == 0) {
            if (Configuration.PRINT_PROPAGATION) {
                IPropagationEngine.Trace.printPropagation(null, propagator);
            }
            //coarseERcalls++;
            propagator.propagate(type.getStrengthenedMask());
        }
    }

    @Override
    public void onPropagatorExecution(Propagator propagator) {
        desactivatePropagator(propagator);
    }

    /**
     * Set the propagator as inactivated within the propagation engine
     *
     * @param propagator propagator to desactivate
     */
    public void desactivatePropagator(Propagator propagator) {
        int id = p2i.get(propagator.getId());
        int to = pcidx[id];
        for (int i = 0; i < to; i++) {
            // we don't remove the element from its master to avoid costly operations
            fines_p[id][i].flush();
        }
    }

    @Override
    public void dynamicAddition(Constraint c, boolean permanent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dynamicDeletion(Constraint c) {
        throw new UnsupportedOperationException();
    }
}
