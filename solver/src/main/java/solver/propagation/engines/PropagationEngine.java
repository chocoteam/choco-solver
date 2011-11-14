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

import solver.ICause;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.propagation.engines.comparators.IncrArityP;
import solver.propagation.engines.comparators.predicate.Predicates;
import solver.propagation.engines.group.Group;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.Arrays;
import java.util.Comparator;

/**
 * An implementation of <code>IPropagationEngine</code>.
 * It deals with 2 main types of <code>IRequest</code>s.
 * Ones are intialized one (at the end of the list).
 * <p/>
 * Created by IntelliJ IDEA.
 * User: cprudhom
 * Date: 28 oct. 2010
 */
public final class PropagationEngine implements IPropagationEngine {


    protected IRequest[] requests = new IRequest[16];

    protected int offset = 0; // index of the last "common" request -- after that index, one can find initialization requests

    protected int size = 0;

    protected IRequest lastPoppedRequest;

    protected Comparator<IRequest> comparator = IncrArityP.get();

    protected Policy policy = Policy.FIXPOINT;

    protected Group[] groups = new Group[8];

    protected IEngine engine;

    protected int nbGroup = 0;

    protected Deal deal = Deal.SEQUENCE;

    protected final ContradictionException exception;

    public PropagationEngine() {
        exception = new ContradictionException();
    }

    /**
     * Initialize this <code>IPropagationEngine</code> object with the array of <code>Constraint</code> and <code>Variable</code> objects.
     * It automatically pushes an event (call to <code>propagate</code>) for each constraints, the initial awake.
     */
    public void init() {
        if (engine != null) {
            throw new SolverException("PropagationEngine.init() has already been called once");
        }
        final IRequest[] tmp = requests;
        requests = new IRequest[size];
        System.arraycopy(tmp, 0, requests, 0, size);

        // FIRST: sort requests, give them a unique group
        // build a default group
        addGroup(Group.buildQueue(Predicates.all(), Policy.FIXPOINT));

//        eval();
        extract();


        switch (deal) {
            case SEQUENCE:
                engine = new WhileEngine();
                break;
            case QUEUE:
                engine = new OldestEngine();
                break;
        }
        engine.setGroups(Arrays.copyOfRange(groups, 0, nbGroup));

        // FINALLY, post initial propagation event for every heavy requests
        for (int i = offset; i < size; i++) {
            requests[i].update(EventType.PROPAGATE); // post initial propagation
        }

    }

    private void eval() {
        int i, j;
        for (i = 0; i < size; i++) {
            lastPoppedRequest = requests[i];
            j = 0;
            // look for the first right group
            while (!groups[j].getPredicate().eval(lastPoppedRequest)) {
                j++;
            }
            groups[j].addRequest(lastPoppedRequest);
        }
        for (j = 0; j < nbGroup; j++) {
            if (groups[j].isEmpty()) {
                groups[j] = groups[nbGroup - 1];
                groups[j].setIndex(j);
                groups[nbGroup - 1] = null;
                nbGroup--;
                j--;
            } else {
                groups[j].make();
            }
        }
    }


    private void extract() {
        int i, j;
        for (i = 0; i < size; i++) {
            requests[i].setIndexinGroup(i);
        }
        for (j = 0; j < nbGroup; j++) {
            int[] indices = groups[j].getPredicate().extract(requests);
            Arrays.sort(indices);
            for (i = 0; i < indices.length; i++) {
                if (requests[indices[i]].getGroup() < 0) {
                    groups[j].addRequest(requests[indices[i]]);
                }
            }
            if (groups[j].isEmpty()) {
                groups[j] = groups[nbGroup - 1];
                groups[j].setIndex(j);
                groups[nbGroup - 1] = null;
                nbGroup--;
                j--;
            } else {
                groups[j].make();
            }
        }
    }


    @Override
    public boolean initialized() {
        return engine != null;
    }

    @Override
    public void fails(ICause cause, Variable variable, String message) throws ContradictionException {
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
        if (groups.length == nbGroup) {
            Group[] tmp = groups;
            groups = new Group[nbGroup * 2];
            System.arraycopy(tmp, 0, groups, 0, nbGroup);
        }
        groups[nbGroup] = group;
        group.setIndex(nbGroup++);
    }

    @Override
    public void deleteGroups() {
        nbGroup = 0;
    }

    @Override
    public void setDeal(Deal deal) {
        this.deal = deal;
    }


    @Override
    public void fixPoint() throws ContradictionException {
        engine.fixPoint();
    }

    @Override
    public void update(IRequest request) {
        engine.update(request);
    }

    @Override
    public void remove(IRequest request) {
        engine.remove(request);
    }

    @Override
    public void flushAll() {
        engine.flushAll();
    }

    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append(groups[0].toString());
        for (int i = 1; i < nbGroup; i++) {
            st.append(",").append(groups[i].toString());
        }
        return st.toString();
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
