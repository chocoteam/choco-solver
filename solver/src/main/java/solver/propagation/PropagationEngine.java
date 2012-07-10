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
package solver.propagation;


import choco.kernel.common.util.objects.BacktrackableArrayList;
import choco.kernel.common.util.objects.IList;
import choco.kernel.memory.IEnvironment;
import com.sun.istack.internal.NotNull;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.propagation.generator.*;
import solver.propagation.wm.IWaterMarking;
import solver.propagation.wm.WaterMarkers;
import solver.recorders.coarse.AbstractCoarseEventRecorder;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class of IPropagatioEngine.
 * It allows scheduling and propagation of ISchedulable object, like IEventRecorder or Group.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/12/11
 */
public class PropagationEngine implements IPropagationEngine {

    protected final ContradictionException exception;

    protected IPropagationStrategy propagationStrategy;

    protected IWaterMarking watermarks; // marks every pair of V-P, breaking multiple apperance of V in P

    protected TIntObjectHashMap<IList<Variable, AbstractFineEventRecorder>> fines_v;
    protected TIntObjectHashMap<List<AbstractFineEventRecorder>> fines_p;
    protected TIntObjectHashMap<AbstractCoarseEventRecorder> coarses;

    protected int pivot;

    protected boolean initialized = false; // is this already initialized

    protected boolean forceInitialPropagation= true; // is propagator activation required

    protected boolean initialPropagationDone = false; // related to forceInitialPropagation, avoid awaking up propagators 2 times

    protected boolean checkProperties= true; // skip water marking phases

    protected boolean forceActivation = false; // force activation of event recorder on creation

    protected IEnvironment environment;

    protected int default_nb_vars = 16, default_nb_props = 16;

    public PropagationEngine(IEnvironment environment) {
        this(environment, true, true, false);
    }

    /**
     * @param checkProperties         : set to false the verification of the three following properties will be skipped:
     *                                <br/>
     *                                <b>NoEventLoss</b>: no event can be lost during propagation because every pair constraint-variable (c, v) of the model is represented within the propagation engine.
     *                                <br/><b>UnitPropagation</b>: no event is propagated more than once because each pair (c, v) of the model is only represented once within the propagation engine.
     *                                <br/><b>Conformity</b>: no event that does not relate to an existing pair (c, v) of the model is represented within the propagation engine.
     *                                <br/>
     *                                As a consequence, the engine can be incomplete.
     * @param forceInitialPropagation set to true, propagators will be scheduled for initial proapation
     * @param forceActivation         set to true, fine event recorders will be directly activated within the structure.
     */
    public PropagationEngine(IEnvironment environment, boolean checkProperties, boolean forceInitialPropagation, boolean forceActivation) {
        this.exception = new ContradictionException();
        this.environment = environment;
        fines_v = new TIntObjectHashMap(default_nb_vars, 0.5f, -1);
        fines_p = new TIntObjectHashMap(default_nb_props, 0.5f, -1);
        coarses = new TIntObjectHashMap(default_nb_props, 0.5f, -1);
        this.checkProperties = checkProperties;
        this.forceInitialPropagation = forceInitialPropagation;
        this.forceActivation = forceActivation;
    }

    /**
     * Override default parameters. Should be called just after the constructor.
     *
     * @param nbVar                   estimation of the number of variables
     * @param nbProp                  estimation of the number of propagators
     * @param checkProperties         set to false the verification of the three following properties will be skipped:
     *                                <br/>
     *                                <b>NoEventLoss</b>: no event can be lost during propagation because every pair constraint-variable (c, v) of the model is represented within the propagation engine.
     *                                <br/><b>UnitPropagation</b>: no event is propagated more than once because each pair (c, v) of the model is only represented once within the propagation engine.
     *                                <br/><b>Conformity</b>: no event that does not relate to an existing pair (c, v) of the model is represented within the propagation engine.
     *                                <br/>
     * @param forceInitialPropagation set to true, propagators will be scheduled for initial propagation
     * @param forceActivation         set to true, fine event recorders will be directly activated within the structure.
     */
    public void setParameters(int nbVar, int nbProp, boolean checkProperties, boolean forceInitialPropagation, boolean forceActivation) {
        this.checkProperties = checkProperties;
        this.forceInitialPropagation = forceInitialPropagation;
        this.forceActivation = forceActivation;
        default_nb_props = nbProp;
        default_nb_vars = nbVar;
    }

    @Override
    public boolean initialized() {
        return initialized;
    }

    @Override
    public boolean forceActivation() {
        return forceActivation;
    }

    @Override
    public IPropagationEngine set(IPropagationStrategy propagationStrategy) {
        this.propagationStrategy = propagationStrategy;
        return this;
    }

    public void init(Solver solver) {
        if (checkProperties) {
            if (!initialized) {
                prepareWM(solver);
                // 1. add the default strategy if required
                if (!watermarks.isEmpty()) {
                    LoggerFactory.getLogger("solver").warn("PropagationEngine:: the defined strategy is not complete -- build default one.");
                    PropagationStrategy _default = buildDefault(solver);
                    propagationStrategy = new Sort(propagationStrategy, _default);
                    if (!watermarks.isEmpty()) {
                        throw new RuntimeException("default strategy has encountered a problem :: " + watermarks);
                    }
                }
                watermarks = null;
                initialized = true;
            }
        }
        if (forceInitialPropagation && !initialPropagationDone) {
            // 2. schedule constraints for initial propagation
            Constraint[] constraints = solver.getCstrs();
            for (int c = 0; c < constraints.length; c++) {
                Propagator[] propagators = constraints[c].propagators;
                for (int p = 0; p < propagators.length; p++) {
                    propagators[p].forcePropagate(EventType.FULL_PROPAGATION);
                }
            }
            initialPropagationDone = true;
        } else if (!forceActivation) {
            Constraint[] constraints = solver.getCstrs();
            for (int c = 0; c < constraints.length; c++) {
                Propagator[] propagators = constraints[c].propagators;
                for (int p = 0; p < propagators.length; p++) {
                    if (propagators[p].isActive()) activatePropagator(propagators[p]);
                }
            }
        }
    }

    public void prepareWM(Solver solver) {
        if (checkProperties && watermarks == null) {
            pivot = solver.getNbIdElt();
            Constraint[] constraints = solver.getCstrs();
            // 1. water mark every couple variable-propagator of the solver
            waterMark(constraints);
        }
    }

    private void waterMark(Constraint[] constraints) {
        watermarks = WaterMarkers.make(pivot);
        for (int c = 0; c < constraints.length; c++) {
            Propagator[] propagators = constraints[c].propagators;
            for (int p = 0; p < propagators.length; p++) {
                Propagator propagator = propagators[p];
                int idP = propagator.getId();
                watermarks.putMark(idP);
                int nbV = propagator.getNbVars();
                for (int v = 0; v < nbV; v++) {
                    Variable variable = propagator.getVar(v);
                    if ((variable.getTypeAndKind() & Variable.CSTE) == 0) { // this is not a constant
                        int idV = variable.getId();
                        watermarks.putMark(idV, idP);
                    }
                }
            }
        }
    }

    public void clearWatermark(int id1, int id2) {
        if (checkProperties) {
            if (id1 == 0) {// coarse case
                watermarks.clearMark(id2);
            } else if (id2 == 0) {// coarse case
                watermarks.clearMark(id1);
            } else {
                watermarks.clearMark(id1, id2);
            }
        }
    }

    public boolean isMarked(int id1, int id2) {
        if (checkProperties) {
            if (id1 == 0) {// coarse case
                return watermarks.isMarked(id2);
            } else if (id2 == 0) {// coarse case
                return watermarks.isMarked(id1);
            } else {
                return watermarks.isMarked(id1, id2);
            }
        }
        return true;
    }

    protected PropagationStrategy buildDefault(Solver solver) {
        Constraint[] constraints = solver.getCstrs();
        Queue arcs = new Queue(new PArc(this, constraints));
        Queue coarses = new Queue(new PCoarse(this, constraints));
        return new Sort(arcs.clearOut(), coarses.pickOne()).clearOut();
    }


    @Override
    public void propagate() throws ContradictionException {
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

    @Override
    public void addEventRecorder(AbstractFineEventRecorder fer) {
        Variable[] vars = fer.getVariables();
        Propagator[] props = fer.getPropagators();
        for (int i = 0; i < props.length; i++) {
            int id = props[i].getId();
            if (!fines_p.containsKey(id)) {
                ArrayList<AbstractFineEventRecorder> list = new ArrayList(default_nb_vars);
                list.add(fer);
                fines_p.put(id, list);
            } else {
                fines_p.get(id).add(fer);
            }
        }
        for (int i = 0; i < vars.length; i++) {
            int id = vars[i].getId();
            if (!fines_v.containsKey(id)) {
                IList<Variable, AbstractFineEventRecorder> list = new BacktrackableArrayList(vars[i], environment, default_nb_props);
                list.add(fer, false, forceActivation);
                fines_v.put(id, list);
            } else {
                fines_v.get(id).add(fer, false, forceActivation);
            }
        }
    }

    @Override
    public void addEventRecorder(AbstractCoarseEventRecorder er) {
        int id = er.getPropagators()[0].getId();
        coarses.put(id, er);
    }

    @Override
    public void clear() {
        watermarks = null; // CPRU : to improve
        initialized = false;
        propagationStrategy = null;
        throw new SolverException("Clearing the engine is not enough!");//CPRU: to do
    }

    @Override
    public String toString() {
        return propagationStrategy.toString();
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        int id = variable.getId();
        IList list = fines_v.get(id);
        if (list != null) {
            list.forEach(id, type, cause);
        }
    }

    @Override
    public void onPropagatorExecution(Propagator propagator) {
        int id = propagator.getId();
        List<AbstractFineEventRecorder> list = fines_p.get(id);
        if (list != null) { // to handle reified propagator, unknown from the engine
            for (int i = 0; i < list.size(); i++) {
                list.get(i).virtuallyExecuted(propagator);
            }
        }
    }

    @Override
    public void schedulePropagator(@NotNull Propagator propagator, EventType event) {
        int id = propagator.getId();
        coarses.get(id).update(event);
    }

    @Override
    public void activatePropagator(Propagator propagator) {
        int id = propagator.getId();
        List<AbstractFineEventRecorder> list = fines_p.get(id);
        if (list != null) { // to handle reified propagator, unknown from the engine
            for (int i = 0; i < list.size(); i++) {
                AbstractFineEventRecorder fer = list.get(i);
                fer.activate(propagator);
            }
        }
    }

    @Override
    public void desactivatePropagator(Propagator propagator) {
        int id = propagator.getId();
        List<AbstractFineEventRecorder> list = fines_p.get(id);
        if (list != null) { // to handle reified propagator, unknown from the engine
            for (int i = 0; i < list.size(); i++) {
                AbstractFineEventRecorder fer = list.get(i);
                fer.desactivate(propagator);
            }
        }
    }

    @Override
    public void activateFineEventRecorder(AbstractFineEventRecorder fer) {
        Variable[] vars = fer.getVariables();
        for (int j = 0; j < vars.length; j++) {
            fines_v.get(vars[j].getId()).setActive(fer);
        }
    }

    @Override
    public void desactivateFineEventRecorder(AbstractFineEventRecorder fer) {
        Variable[] vars = fer.getVariables();
        for (int j = 0; j < vars.length; j++) {
            fines_v.get(vars[j].getId()).setPassive(fer);
        }
    }
}
