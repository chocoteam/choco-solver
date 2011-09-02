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

import com.sun.istack.internal.NotNull;
import solver.ICause;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.propagation.engines.group.Group;
import solver.requests.IRequest;
import solver.variables.Variable;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 28 oct. 2010
 */
public interface IPropagationEngine extends Serializable {

    public static enum Deal {
        SEQUENCE, QUEUE
    }


    void init();

    void addConstraint(Constraint constraint);

    void setDeal(Deal deal);

    void addGroup(Group group);

    void deleteGroups();

    void fixPoint() throws ContradictionException;

    /**
     * Forces clearing of internal structure
     */
    void flushAll();

    /**
     * Define the action to apply on a state changes of observable objects.
     * @param request
     */
    void update(IRequest request);

    void remove(IRequest request);

    int getNbRequests();

    /**
     * Returns <code>true</code> if <code>this</code> is initialized, <code>false</code> otherwise.
     * @return <code>true</code> if <code>this</code> is initialized, <code>false</code> otherwise
     */
    boolean initialized();

    /**
     * Set and throw a ContradictionException based on the 3-uple: <cause, variable, message>
     * @param cause ICause object that causes the exception (if any, null otherwise)
     * @param variable Variable object that causes the exception (if any, null otherwise)
     * @param message detailed message of the exception reason
     * @throws ContradictionException expected behavior
     */
    void fails(@NotNull ICause cause, Variable variable, String message) throws ContradictionException;

    ContradictionException getContradictionException();
}
