package org.chocosolver.solver.variables.view.graph;

import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.view.GraphView;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.UndirectedGraph;

/**
 * An abstract class for graph views over other variables
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public abstract class DirectedGraphView<V extends Variable> extends GraphView<V, DirectedGraph> implements DirectedGraphVar {

    /**
     * Creates a graph view.
     *
     * @param name      name of the view
     * @param variables observed variables
     */
    protected DirectedGraphView(String name, V[] variables) {
        super(name, variables);
    }

    @Override
    public boolean isDirected() {
        return true;
    }
}
