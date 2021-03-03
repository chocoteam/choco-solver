package org.chocosolver.solver.variables.delta;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * Interface for Graph Delta Monitor.
 */
public interface IGraphDeltaMonitor {

    /**
     * Applies proc to every vertex which has just been removed or enforced, depending on evt.
     *
     * @param proc an incremental procedure over vertices
     * @param evt  either ENFORCENODE or REMOVENODE
     * @throws ContradictionException if a failure occurs
     */
    void forEachNode(IntProcedure proc, GraphEventType evt) throws ContradictionException;

    /**
     * Applies proc to every arc which has just been removed or enforced, depending on evt.
     *
     * @param proc an incremental procedure over arcs
     * @param evt  either ENFORCEARC or REMOVEARC
     * @throws ContradictionException if a failure occurs
     */
    void forEachArc(PairProcedure proc, GraphEventType evt) throws ContradictionException;
}