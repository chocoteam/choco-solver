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


import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.propagation.generator.PropagationStrategy;
import solver.propagation.generator.Sort;
import solver.propagation.wm.IWaterMarking;
import solver.propagation.wm.WaterMarkers;
import solver.variables.EventType;
import solver.variables.Variable;

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

    protected PropagationStrategy propagationStrategy;

    protected IWaterMarking watermarks; // marks every pair of V-P, breaking multiple apperance of V in P

    protected int pivot;

    protected boolean initialized = false;

    public PropagationEngine() {
        this.exception = new ContradictionException();
    }

    @Override
    public boolean initialized() {
        return initialized;
    }

    @Override
    public boolean hasStrategy() {
        return propagationStrategy != null;
    }

    @Override
    public void set(PropagationStrategy propagationStrategy) {
        this.propagationStrategy = propagationStrategy;
    }

    public void init(Solver solver) {
        if (!initialized) {
            // 1. add the default strategy if required
            if (!watermarks.isEmpty()) {
                LoggerFactory.getLogger("solver").warn("PropagationEngine:: the defined strategy is not complete -- build default one.");
                PropagationStrategy _default = buildDefault(solver);
                propagationStrategy = new Sort(propagationStrategy, _default);
                if (!watermarks.isEmpty()) {
                    throw new RuntimeException("default strategy has encountered a problem :: " + watermarks);
                }
            }
            // 2. schedule constraints for initial propagation
            Constraint[] constraints = solver.getCstrs();
            for (int c = 0; c < constraints.length; c++) {
                Propagator[] propagators = constraints[c].propagators;
                for (int p = 0; p < propagators.length; p++) {
                    propagators[p].forcePropagate(EventType.FULL_PROPAGATION);
                }
            }

        }
        initialized = true;
    }

    public void prepareWM(Solver solver) {
        if (watermarks == null) {
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
                        watermarks.putMark(idV, idP, v);
                    }
                }
            }
        }
    }

    public void clearWatermark(int id1, int id2, int id3) {
        if (id1 == 0) {// coarse case
            watermarks.clearMark(id2);
        } else if (id2 == 0) {// coarse case
            watermarks.clearMark(id1);
        } else {
            watermarks.clearMark(id1, id2, id3);
        }
    }

    public boolean isMarked(int id1, int id2, int id3) {
        if (id1 == 0) {// coarse case
            return watermarks.isMarked(id2);
        } else if (id2 == 0) {// coarse case
            return watermarks.isMarked(id1);
        } else {
            return watermarks.isMarked(id1, id2, id3);
        }
    }

    protected PropagationStrategy buildDefault(Solver solver) {
        return PropagationStrategies.ONE_QUEUE_WITH_ARCS.make(solver);
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
}
