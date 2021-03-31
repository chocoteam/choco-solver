package org.chocosolver.solver.variables.view.graph.undirected;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.graph.UndirectedGraphView;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Undirected graph view G'(V', E') over an undirected graph variable G(V, E) such that:
 * V' = V \ S, with S a fixed set of nodes.
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class SubgraphExcludedNodesView extends UndirectedGraphView<UndirectedGraphVar> {

    protected UndirectedGraph lb;
    protected UndirectedGraph ub;

    protected UndirectedGraphVar graphVar;
    protected ISet excludedNodes;

    /**
     * Creates a graph view.
     *
     * @param name      name of the view
     * @param graphVar observed variable
     */
    public SubgraphExcludedNodesView(String name, UndirectedGraphVar graphVar, ISet excludedNodes) {
        super(name, new UndirectedGraphVar[] {graphVar});
        this.excludedNodes = excludedNodes;
        this.graphVar = graphVar;
        this.lb = GraphFactory.makeSubgraphExcludedNodes(getModel(), graphVar.getLB(), excludedNodes);
        this.ub = GraphFactory.makeSubgraphExcludedNodes(getModel(), graphVar.getUB(), excludedNodes);
    }

    @Override
    public UndirectedGraph getLB() {
        return lb;
    }

    @Override
    public UndirectedGraph getUB() {
        return ub;
    }

    @Override
    public int getNbMaxNodes() {
        return graphVar.getNbMaxNodes();
    }

    @Override
    public boolean isDirected() {
        return graphVar.isDirected();
    }

    @Override
    public void instantiateTo(UndirectedGraph value, ICause cause) throws ContradictionException {
        // TODO
    }

    @Override
    public boolean isInstantiated() {
        if (getPotentialNodes().size() != getMandatoryNodes().size()) {
            return false;
        }
        ISet suc;
        for (int i : getUB().getNodes()) {
            suc = getPotentialNeighborsOf(i);
            if (suc.size() != getLB().getNeighborsOf(i).size()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean doRemoveNode(int node) throws ContradictionException {
        return graphVar.removeNode(node, this);
    }

    @Override
    protected boolean doEnforceNode(int node) throws ContradictionException {
        return graphVar.enforceNode(node, this);
    }

    @Override
    protected boolean doRemoveEdge(int from, int to) throws ContradictionException {
        return graphVar.removeEdge(from, to, this);
    }

    @Override
    protected boolean doEnforceEdge(int from, int to) throws ContradictionException {
        return graphVar.enforceEdge(from, to, this);
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        notifyPropagators(event, this);
    }
}
