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

import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.Variable;

/**
 * An abstract class of IPropagatioEngine.
 * It allows scheduling and propagation of ISchedulable object, like IEventRecorder or Group.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/12/11
 */
public abstract class PropagationEngine implements IPropagationEngine {

    protected final ContradictionException exception;

    public PropagationEngine() {
        this.exception = new ContradictionException();
    }

//    /**
//     * Initialize this <code>IPropagationEngine</code> object with the array of <code>Constraint</code> and <code>Variable</code> objects.
//     * It automatically pushes an event (call to <code>propagate</code>) for each constraints, the initial awake.
//     */
//    public void init() {
//        if (engine != null) {
//            throw new SolverException("PropagationEngine.init() has already been called once");
//        }
//        final IRequest[] tmp = requests;
//        requests = new IRequest[size];
//        System.arraycopy(tmp, 0, requests, 0, size);
//
//        // FIRST: sort requests, give them a unique group
//        // build a default group
//        addGroup(Group.buildQueue(Predicates.all(), Policy.FIXPOINT));
//
////        eval();
//        extract();
//
//
//        switch (deal) {
//            case SEQUENCE:
//                engine = new WhileEngine();
//                break;
//            case QUEUE:
//                engine = new OldestEngine();
//                break;
//        }
//        engine.setGroups(Arrays.copyOfRange(groups, 0, nbGroup));
//
//        // FINALLY, post initial propagation event for every heavy requests
//        for (int i = offset; i < size; i++) {
//            requests[i].update(EventType.FULL_PROPAGATION); // post initial propagation
//        }
//
//    }
//
//    private void eval() {
//        int i, j;
//        for (i = 0; i < size; i++) {
//            lastPoppedRequest = requests[i];
//            j = 0;
//            // look for the first right group
//            while (!groups[j].getPredicate().eval(lastPoppedRequest)) {
//                j++;
//            }
//            groups[j].addRequest(lastPoppedRequest);
//        }
//        for (j = 0; j < nbGroup; j++) {
//            if (groups[j].isEmpty()) {
//                groups[j] = groups[nbGroup - 1];
//                groups[j].setIndex(j);
//                groups[nbGroup - 1] = null;
//                nbGroup--;
//                j--;
//            } else {
//                groups[j].make();
//            }
//        }
//    }
//
//
//    private void extract() {
//        int i, j;
//        for (i = 0; i < size; i++) {
//            requests[i].setIndex(IRequest.IN_GROUP,i);
//        }
//        for (j = 0; j < nbGroup; j++) {
//            int[] indices = groups[j].getPredicate().extract(requests);
//            Arrays.sort(indices);
//            for (i = 0; i < indices.length; i++) {
//                if (requests[indices[i]].getIndex(IRequest.GROUP_ID) < 0) {
//                    groups[j].addRequest(requests[indices[i]]);
//                }
//            }
//            if (groups[j].isEmpty()) {
//                groups[j] = groups[nbGroup - 1];
//                groups[j].setIndex(j);
//                groups[nbGroup - 1] = null;
//                nbGroup--;
//                j--;
//            } else {
//                groups[j].make();
//            }
//        }
//    }


    @Override
    public void fails(ICause cause, Variable variable, String message) throws ContradictionException {
        throw exception.set(cause, variable, message);
    }

    @Override
    public ContradictionException getContradictionException() {
        return exception;
    }
}
