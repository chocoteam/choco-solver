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

package solver.variables;

import solver.ICause;
import solver.explanations.Explanation;
import solver.requests.list.IRequestList;
import solver.variables.domain.delta.IDelta;
import solver.requests.IRequest;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: xlorca
 */
public interface Variable<D extends IDelta> extends solver.Observable<ICause, EventType>, Serializable {

    //todo: to complete
    void updateEntailment(IRequest request);

    /**
     * Indicates wether <code>this</code> is instantiated (see implemetations to know what instantiation means).
     *
     * @return <code>true</code> if <code>this</code> is instantiated
     */
    boolean instantiated();

    @Override
    String toString();

    /**
     * Returns the name of <code>this</code>
     *
     * @return a String reprensenting the name of <code>this</code>
     */
    String getName();

    //todo : to complete

    void addRequest(IRequest request);

    //todo : to complete

    void deleteRequest(IRequest request);

    IRequest[] getRequests();

    int nbRequests();

    /**
     * Returns the number of constraints involving <code>this</code>
     * TODO: MostConstrained: count requests instead of constraints
     *
     * @return the number of constraints of <code>this</code>
     */
    int nbConstraints();

    Explanation explain();

    D getDelta();

}
