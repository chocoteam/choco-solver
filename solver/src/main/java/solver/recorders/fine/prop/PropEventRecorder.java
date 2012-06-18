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
package solver.recorders.fine.prop;

import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.propagation.IPropagationEngine;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.Arrays;

/**
 * An event recorder associated with one propagator and its variables.
 * On a variable modification, the propagators is scheduled for FULL_PROPAGATION
 * <br/>
 *
 * @author Charles Prud'homme
 * @revision 05/24/12 remove deltamonitoring
 * @since 24/01/12
 */
public class PropEventRecorder<V extends Variable> extends AbstractFineEventRecorder<V> {

    protected final int[] v2i; // a kind of hash map to retrieve idx of variable in this data structures
    protected final int[] varIdx; // an array of indices helping retrieving data of a variable, thanks to v2i and var.id
    protected final int nbVar;// number of variable
    protected final int[] idxVs; // index of this within variable structures -- mutable
    protected final int offset;


    public PropEventRecorder(V[] variables, Propagator<V> propagator, Solver solver, IPropagationEngine engine) {
        super(variables.clone(), new Propagator[]{propagator}, solver, engine);
        nbVar = variables.length;
        // first estimate amplitude of IDs
        int id = variables[0].getId();
        int idM = id,idm = id;
        for (int i = 1; i < variables.length; i++) {
            id = variables[i].getId();
            if (id > idM)idM = id;
            if (id < idm)idm = id;
        }
        // then create max size arrays
        v2i = new int[idM - idm + 1];
        Arrays.fill(v2i, -1);
        offset = idm;

        varIdx = new int[nbVar];
        this.idxVs = new int[nbVar];
        V variable;
        for (int i = 0; i < nbVar; i++) {
            variable = variables[i];
            int vid = variable.getId();
            v2i[vid - offset] = i;
            varIdx[i] = i;
        }
    }

    @Override
    public boolean execute() throws ContradictionException {
        throw new SolverException("PropEventRecorder#execute() is empty and should not be called (nor scheduled)!");
    }

    @Override
    public void afterUpdate(int vIdx, EventType evt, ICause cause) {
        // Only notify constraints that filter on the specific event received
        assert cause != null : "should be Cause.Null instead";
        if (cause != propagators[PINDEX]) { // due to idempotency of propagator, it should not schedule itself
            // schedule the coarse event recorder associated to thos
            propagators[PINDEX].forcePropagate(EventType.FULL_PROPAGATION);
        }
    }

    @Override
    public int getIdx(V variable) {
        return idxVs[v2i[variable.getId() - offset]];
    }

    @Override
    public void setIdx(V variable, int idx) {
        idxVs[v2i[variable.getId() - offset]] = idx;
    }

    @Override
    public void flush() {
        // void
    }

    @Override
    public void virtuallyExecuted(Propagator propagator) {
        // void
    }

    @Override
    public String toString() {
        return "<< " + Arrays.toString(variables) + "::" + propagators[PINDEX].toString() + " >>";
    }

    @Override
    public void enqueue() {
        enqueued = true;
        propagators[PINDEX].incNbRecorderEnqued();
    }

    @Override
    public void deque() {
        enqueued = false;
        propagators[PINDEX].decNbRecrodersEnqued();
    }
}
