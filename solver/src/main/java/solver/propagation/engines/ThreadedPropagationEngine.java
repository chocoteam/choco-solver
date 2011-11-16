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

package solver.propagation.engines;

import choco.kernel.memory.trailing.EnvironmentTrailing;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.propagation.engines.comparators.IncrPriorityP;
import solver.propagation.engines.concurrent.ISequencer;
import solver.propagation.engines.concurrent.LonelySequencer;
import solver.propagation.engines.group.Group;
import solver.requests.IRequest;
import solver.variables.Variable;

import java.util.Arrays;

/**
 * An implementation of <code>IPropagationEngine</code>.
 * It deals with 2 main types of <code>IRequest</code>s.
 * Ones are intialized one (at the end of the list).
 * <p/>
 * This deals with thread-like propagation of request.
 * At the beginning, the number of available threads is given and each thread feed itself.
 * <p/>
 * Things to know about parallelisation of choco:
 * <br/>- EnvironmentTrailing does not allow concurrent modification of stored object (that's why it throws an error at construction),
 * <p/>
 * Created by IntelliJ IDEA.
 * User: cprudhom
 * Date: 28 oct. 2010
 */
public final class ThreadedPropagationEngine implements IPropagationEngine {


    protected final Solver solver; // the declaring solver -- usefull to get the correct number of variables in #init()

    public IRequest[] requests = new IRequest[16]; // list of requests of the problem

    protected int offset = 0; // index of the last "common" request -- after that index, one can find initialization requests

    protected int size = 0; // number of requests of the problem

    protected IRequest lastPoppedRequest; // a object to avoid stacking request when necessary

    protected final ContradictionException exception; // the exception of the prop. engine

    private boolean init = false; // is the prop. engine intialized

    protected int nbThreads; // number of threads available

    public ISequencer sequencer; // a sequencer to provide the next request to propagate

    public ThreadedPropagationEngine(Solver solver, int nbThreads) {
        if (solver.getEnvironment() instanceof EnvironmentTrailing) {
            throw new SolverException("EnvironmentTrailing does not allow concurrent modification");
        }
        this.solver = solver;
        this.exception = new ContradictionException();
        this.nbThreads = nbThreads;
        throw new UnsupportedOperationException("Do not support PropRequest in a group");
    }

    /**
     * Initialize this <code>IPropagationEngine</code> object with the array of <code>Constraint</code> and <code>Variable</code> objects.
     * It automatically pushes an event (call to <code>propagate</code>) for each constraints, the initial awake.
     */
    public void init() {
        if (init) {
            throw new SolverException("PropagationEngine.init() has already been called once");
        }
        IRequest[] tmp = requests;
        requests = new IRequest[size];
        System.arraycopy(tmp, 0, requests, 0, size);

        Arrays.sort(requests, offset, size, IncrPriorityP.get()); // first, sort initialization requests
        IRequest request;
        int i;
        // initialization requests are also enqued to be treated at initial propagation
        for (i = offset; i < size; i++) {
            request = requests[i];
            requests[i].setIndex(IRequest.GROUP_ID, 0);
            request.enqueue();
        }
        for (i = 0; i < offset; i++) {
            request = requests[i];
            requests[i].setIndex(IRequest.GROUP_ID, 0);
        }
        init = false;
        sequencer = new LonelySequencer(this, offset, solver.getNbVars(), nbThreads);
    }

    @Override
    public boolean initialized() {
        return init;
    }

    @Override
    public synchronized void fails(ICause cause, Variable variable, String message) throws ContradictionException {
        throw exception.set(cause, variable, message);
    }

    @Override
    public void addConstraint(Constraint constraint) {
        int nbV = 0;
        int nbI = 0;
        Propagator[] props = constraint.propagators;
        nbI += props.length;
        for (int p = 0; p < nbI; p++) {
            nbV += props[p].nbRequests();
        }
        IRequest[] tmp = requests;
        // ensure capacity
        while (requests.length < size + (nbV + nbI)) {
            requests = new IRequest[tmp.length * 2];
            System.arraycopy(tmp, 0, requests, 0, size);
            tmp = requests;
        }
        System.arraycopy(tmp, offset, requests, offset + nbV, size - offset);

        int k = size + nbV;
        for (int p = 0; p < props.length; p++, k++) {
            Propagator prop = props[p];
            // "common" requests
            for (int i = 0; i < prop.nbRequests(); i++, offset++) {
                requests[offset] = prop.getRequest(i);
                requests[offset].setPropagationEngine(this);
            }
            // intitialization request
            requests[k] = prop.getRequest(-1);
            requests[k].setPropagationEngine(this);
        }
        size = k;
    }

    @Override
    public void addGroup(Group group) {
        LoggerFactory.getLogger("solver").error("addGroup : empty");
    }

    @Override
    public void deleteGroups() {
        LoggerFactory.getLogger("solver").error("delete : empty");
    }

    @Override
    public void setDeal(Deal deal) {
        LoggerFactory.getLogger("solver").error("setDeal : empty");
    }

    @Override
    public void fixPoint() throws ContradictionException {
        sequencer.fixpoint();
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (sequencer.hasFailed()) {
            throw exception;
        }
    }

    @Override
    public void schedule(IRequest request) {
        if (!request.enqueued()) {
            sequencer.set(request.getIndex(IRequest.IN_GROUP), true);
            request.enqueue();
        }
    }

    @Override
    public void remove(IRequest request) {
        sequencer.set(request.getIndex(IRequest.IN_GROUP), false);
        request.deque();
    }

    @Override
    public void flushAll() {
        sequencer.flushAll();
    }

    public String toString() {
        return "thread PE";
    }


    @Override
    public int getNbRequests() {
        return size;
    }

    @Override
    public ContradictionException getContradictionException() {
        return exception;
    }
}
