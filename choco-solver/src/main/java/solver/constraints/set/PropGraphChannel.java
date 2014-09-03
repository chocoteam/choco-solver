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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package solver.constraints.set;

import gnu.trove.map.hash.THashMap;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.variables.delta.IGraphDeltaMonitor;
import solver.variables.delta.ISetDeltaMonitor;
import solver.variables.graph.GraphVar;
import util.ESat;
import util.objects.setDataStructures.ISet;
import util.procedure.IntProcedure;
import util.procedure.PairProcedure;
import util.tools.ArrayUtils;

/**
 * Channeling between a graph variable and set variables
 * representing either node neighbors or node successors
 *
 * @author Jean-Guillaume Fages
 */
public class PropGraphChannel extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n, currentSet;
    private ISetDeltaMonitor[] sdm;
    private SetVar[] sets;
    private IGraphDeltaMonitor gdm;
    private GraphVar g;
    private IntProcedure elementForced, elementRemoved;
    private PairProcedure arcForced, arcRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Channeling between a graph variable and set variables
     * representing either node neighbors or node successors
     *
     * @param setsV
     * @param gV
     */
    public PropGraphChannel(SetVar[] setsV, GraphVar gV) {
        super(ArrayUtils.append(setsV, new Variable[]{gV}), PropagatorPriority.LINEAR, true);
        this.sets = new SetVar[setsV.length];
        for (int i = 0; i < setsV.length; i++) {
            this.sets[i] = (SetVar) vars[i];
        }
        n = sets.length;
        this.g = (GraphVar) vars[n];
        assert (n == g.getEnvelopGraph().getNbNodes());
        sdm = new ISetDeltaMonitor[n];
        for (int i = 0; i < n; i++) {
            sdm[i] = sets[i].monitorDelta(this);
        }
        gdm = g.monitorDelta(this);
        elementForced = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                g.enforceArc(currentSet, element, aCause);
            }
        };
        elementRemoved = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                g.removeArc(currentSet, element, aCause);
            }
        };
        arcForced = new PairProcedure() {
            @Override
            public void execute(int i, int j) throws ContradictionException {
                sets[i].addToKernel(j, aCause);
            }
        };
        arcRemoved = new PairProcedure() {
            @Override
            public void execute(int i, int j) throws ContradictionException {
                sets[i].removeFromEnvelope(j, aCause);
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx < n) {
            return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
        } else {
            return EventType.ENFORCEARC.mask + EventType.REMOVEARC.mask;
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            for (int j = sets[i].getKernelFirst(); j != SetVar.END; j = sets[i].getKernelNext()) {
                g.enforceArc(i, j, aCause);
            }
            ISet tmp = g.getKernelGraph().getSuccsOrNeigh(i);
            for (int j = tmp.getFirstElement(); j >= 0; j = tmp.getNextElement()) {
                sets[i].addToKernel(j, aCause);
            }
            for (int j = sets[i].getEnvelopeFirst(); j != SetVar.END; j = sets[i].getEnvelopeNext()) {
                if (!g.getEnvelopGraph().isArcOrEdge(i, j)) {
                    sets[i].removeFromEnvelope(j, aCause);
                }
            }
            tmp = g.getEnvelopGraph().getSuccsOrNeigh(i);
            for (int j = tmp.getFirstElement(); j >= 0; j = tmp.getNextElement()) {
                if (!sets[i].envelopeContains(j)) {
                    g.removeArc(i, j, aCause);
                }
            }
        }
        for (int i = 0; i < n; i++) {
            sdm[i].unfreeze();
        }
        gdm.unfreeze();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == n) {
            gdm.freeze();
            gdm.forEachArc(arcForced, EventType.ENFORCEARC);
            gdm.forEachArc(arcRemoved, EventType.REMOVEARC);
            gdm.unfreeze();
        } else {
            currentSet = idxVarInProp;
            sdm[currentSet].freeze();
            sdm[currentSet].forEach(elementForced, EventType.ADD_TO_KER);
            sdm[currentSet].forEach(elementRemoved, EventType.REMOVE_FROM_ENVELOPE);
            sdm[currentSet].unfreeze();
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            for (int j = sets[i].getKernelFirst(); j != SetVar.END; j = sets[i].getKernelNext()) {
                if (!g.getEnvelopGraph().isArcOrEdge(i, j)) {
                    return ESat.FALSE;
                }
            }
            ISet tmp = g.getKernelGraph().getSuccsOrNeigh(i);
            for (int j = tmp.getFirstElement(); j >= 0; j = tmp.getNextElement()) {
                if (!sets[i].envelopeContains(j)) {
                    return ESat.FALSE;
                }
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length - 1;
            SetVar[] aVars = new SetVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (SetVar) identitymap.get(this.vars[i]);
            }
            g.duplicate(solver, identitymap);
            GraphVar G = (GraphVar) identitymap.get(g);

            identitymap.put(this, new PropGraphChannel(aVars, G));
        }
    }
}
