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

package solver.variables.view;

import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.requests.IRequest;
import solver.requests.list.IRequestList;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IntDelta;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public abstract class ImageIntVar<IV extends IntVar> extends AbstractVariable implements IntVar {

    protected final IV var;

    protected int uniqueID;

    protected final Solver solver;

    public ImageIntVar(String name, IV var, Solver solver) {
        super(name, solver);
        this.var = var;
        this.solver = solver;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    @Override
    public int getDomainSize() {
        return var.getDomainSize();
    }

    @Override
    public void setHeuristicVal(HeuristicVal heuristicVal) {
        //useless: based on var heuristic val
    }

    @Override
    public HeuristicVal getHeuristicVal() {
        return var.getHeuristicVal();
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return var.hasEnumeratedDomain();
    }

    @Override
    public IntDelta getDelta() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateEntailment(IRequest request) {
        var.updateEntailment(request);
    }

    @Override
    public boolean instantiated() {
        return var.instantiated();
    }

    @Override
    public void addRequest(IRequest request) {
        var.addRequest(request);
    }

    @Override
    public void deleteRequest(IRequest request) {
        var.deleteRequest(request);
    }

    @Override
    public void subscribeView(IView view) {
        var.subscribeView(view);
    }

    @Override
    public IRequestList getRequests() {
        return var.getRequests();
    }

    @Override
    public int nbConstraints() {
        return var.nbConstraints();
    }

    @Override
    public Explanation explain() {
        return var.explain();
    }

    @Override
    public int nbRequests() {
        return var.nbRequests();
    }

    @Override
    public void updatePropagationConditions(Propagator propagator, int idxInProp) {
        var.updatePropagationConditions(propagator, idxInProp);
    }

    @Override
    public void deletePropagator(Propagator observer) {
        var.deletePropagator(observer);
    }

    @Override
    public void notifyPropagators(EventType eventType, ICause o) throws ContradictionException {
        var.notifyPropagators(eventType, o);
    }

    @Override
    public void attachPropagator(Propagator propagator, int idxInProp) {
        var.attachPropagator(propagator, idxInProp);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void contradiction(ICause cause, String message) throws ContradictionException {
        var.contradiction(cause, message);
    }

    @Override
    public Solver getSolver() {
        return solver;
    }
}
