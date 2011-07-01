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
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.engines.IPropagationEngine;
import solver.requests.IRequest;
import solver.requests.list.IRequestList;
import solver.requests.list.RequestListBuilder;
import solver.variables.domain.delta.IDelta;

/**
 * Class used to factorise code
 * The subclass must implement Variable interface
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 30 june 2011
 */
public abstract class AbstractVariable  {

    private static final long serialVersionUID = 1L;
    /**
     * Reference to the solver containing this variable.
     */
    protected final Solver solver;

    protected final String name;

    /**
     * List of requests
     */
    protected final IRequestList<IRequest> requests;

    protected int modificationEvents;

    protected int uniqueID;

    protected final IPropagationEngine engine;

    //////////////////////////////////////////////////////////////////////////////////////

    protected AbstractVariable(String name, Solver solver) {
        this.name = name;
        this.solver = solver;
        this.engine = solver.getEngine();
        this.requests = RequestListBuilder.preset(solver.getEnvironment());
    }
    
    public abstract IDelta getDelta();

    public int getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    public void updateEntailment(IRequest request) {
        requests.setPassive(request);
    }

    public String getName() {
        return this.name;
    }

    ////////////////////////////////////////////////////////////////
    ///// 	methodes 		de 	  l'interface 	  Variable	   /////
    ////////////////////////////////////////////////////////////////

    public void addPropagator(Propagator observer, int idxInProp) {
        modificationEvents |= observer.getPropagationConditions(idxInProp);
    }

    public void deletePropagator(Propagator observer) {
        throw new UnsupportedOperationException();
    }

    public void notifyPropagators(EventType e, ICause cause) throws ContradictionException {
        if ((modificationEvents & e.mask) != 0) {
            requests.notifyButCause(cause, e, getDelta());
        }
    }

    public void addRequest(IRequest request) {
        requests.addRequest(request);
    }

    public void deleteRequest(IRequest request) {
        requests.deleteRequest(request);
    }

    public IRequestList getRequests() { 
        return requests;
    }

    public int nbConstraints() {
        return requests.size();
    }

    public int nbRequests() {
        return requests.cardinality();
    }

    public Solver getSolver() {
        return solver;
    }
}
