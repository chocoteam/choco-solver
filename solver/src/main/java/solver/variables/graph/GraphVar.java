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

package solver.variables.graph;

import choco.kernel.memory.IEnvironment;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.domain.delta.GraphDelta;
import solver.variables.domain.delta.IGraphDelta;

/**
 * Created by IntelliJ IDEA.
 * User: chameau, Jean-Guillaume Fages
 * Date: 7 févr. 2011
 */
public abstract class GraphVar<E extends IStoredGraph> extends AbstractVariable implements Variable<IGraphDelta>, IVariableGraph {

    //////////////////////////////// GRAPH PART /////////////////////////////////////////
    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected E envelop, kernel;
    protected IEnvironment environment;
    protected IGraphDelta delta;
    ///////////// Attributes related to Variable ////////////
    protected boolean reactOnModification;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************
    public GraphVar(Solver solver) {
    	super("",solver);
        this.environment = solver.getEnvironment();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean instantiated() {
        if (getEnvelopOrder() != getKernelOrder()) {
            return false;
        }
        INeighbors nei;
        for (int n = envelop.getActiveNodes().nextValue(0); n >= 0; n = envelop.getActiveNodes().nextValue(n + 1)) {
            nei = envelop.getNeighborsOf(n);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (!kernel.edgeExists(n, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean removeNode(int x, ICause cause) throws ContradictionException {
        if (kernel.getActiveNodes().isActive(x)) {
            this.contradiction(cause, "remove mandatory node");
            return true;
        }
        if (envelop.desactivateNode(x)) {
            if (reactOnModification) {
                delta.getNodeRemovalDelta().add(x);
            }
            EventType e = EventType.REMOVENODE;
            notifyPropagators(e, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean enforceNode(int x, ICause cause) throws ContradictionException {
        if (envelop.getActiveNodes().isActive(x)) {
            if (kernel.activateNode(x)) {
                if (reactOnModification) {
                    delta.getNodeEnforcingDelta().add(x);
                }
                EventType e = EventType.ENFORCENODE;
                notifyPropagators(e, cause);
                INeighbors neig = getEnvelopGraph().getNeighborsOf(x);
                if (neig.neighborhoodSize() == 1) {
                    enforceArc(x, neig.getFirstElement(), null);
                }
                if (neig.neighborhoodSize() == 0) {
                    this.contradiction(null, "cannot enforce nodes with no arcs");
                }
                return true;
            }
            return false;
        }
        this.contradiction(cause, "enforce node which is not in the domain");
        return true;
    }

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    @Override
    public int getEnvelopOrder() {
        return envelop.getActiveNodes().nbActive();
    }

    @Override
    public int getKernelOrder() {
        return kernel.getActiveNodes().nbActive();
    }

    @Override
    public IStoredGraph getKernelGraph() {
        return kernel;
    }

    @Override
    public IStoredGraph getEnvelopGraph() {
        return envelop;
    }

    /**Used for the tree search
     * TODO Ugly
     * @return the next arc to enforce (decision)
     */
    public abstract int nextArc();

    //***********************************************************************************
    // VARIABLE STUFF
    //***********************************************************************************

    @Override
    public Explanation explain() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IGraphDelta getDelta() {
        return delta;
    }

    @Override
    public void addPropagator(Propagator observer, int idxInProp) {
        modificationEvents |= observer.getPropagationConditions(idxInProp);
        if (!reactOnModification) {
            reactOnModification = true;
            delta = new GraphDelta();
        }
    }

    @Override
    public void contradiction(ICause cause, String message) throws ContradictionException {
        engine.fails(cause, this, message);
    }
}
