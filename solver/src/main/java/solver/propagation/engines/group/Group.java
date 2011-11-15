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

package solver.propagation.engines.group;

import solver.exception.ContradictionException;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.predicate.Predicate;
import solver.requests.IRequest;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A class to group requests and define a policy to propagate.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/04/11
 */
public class Group implements Serializable {

    protected Comparator<IRequest> comparator;

    protected Predicate predicate;

    protected Policy policy = Policy.FIXPOINT;

    protected IReacher reacher;

    protected int index;

    protected IRequest[] requests;
    protected int nbRequests;

    public static Group buildQueue(Predicate predicate, Policy policy) {
        return new Group(predicate, null, policy);
    }

    public static Group buildGroup(Predicate predicate, Comparator<IRequest> comparator, Policy policy) {
        return new Group(predicate, comparator, policy);
    }

    Group(Predicate predicate, Comparator<IRequest> comparator, Policy policy) {
        this.comparator = comparator;
        this.predicate = predicate;
        this.policy = policy;
        requests = new IRequest[8];
        nbRequests = 0;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public boolean fixpoint() throws ContradictionException {
        return policy.fixpoint(reacher);
    }

    public void update(IRequest request) {
        reacher.update(request);
    }


    public boolean remove(IRequest request) {
        return reacher.remove(request);
    }

    public void flushAll() {
        reacher.flushAll();
    }


    public void make() {
        requests = Arrays.copyOf(requests, nbRequests);

        if (comparator == null) {
            reacher = new QueueReacher(nbRequests);
        } else {
            reacher = new ArrayReacher(requests, comparator);
        }
        for (int i = 0; i < nbRequests; i++) {
            requests[i].setIndex(IRequest.IN_GROUP, i);
            requests[i].setIndex(IRequest.GROUP_ID, index); //HACK: usefull for Propagation.eval() when a group is deleted...
        }
    }

    public void addRequest(IRequest aRequest) {
        //ensure capacity
        if (nbRequests + 1 > requests.length) {
            IRequest[] tmp = requests;
            int size = requests.length * 2;
            requests = new IRequest[size];
            System.arraycopy(tmp, 0, requests, 0, nbRequests);
        }
        requests[nbRequests++] = aRequest;
        aRequest.setIndex(IRequest.GROUP_ID, index);
    }

    @Override
    public String toString() {
        return "[" + reacher.toString() + "]";
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isEmpty() {
        return nbRequests == 0;
    }
}
