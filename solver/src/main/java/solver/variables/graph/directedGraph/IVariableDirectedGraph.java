package solver.variables.graph.directedGraph;

import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.graph.IVariableGraph;

public interface IVariableDirectedGraph extends IVariableGraph{

	/**
     * Remove arc (x,y) from the mandatory graph 
     * @param x node's index
     * @param y node's index
     * @param cause algorithm which is related to the removal
     * @return true iff the removal has an effect
     * @throws ContradictionException 
     */
    boolean removeSuccessor(int x, int y, ICause cause) throws ContradictionException;

    /**
     * Enforce arc (x,y) to be in the mandatory graph 
     * @param x node's index
     * @param y node's index
     * @param cause algorithm which is related to the removal
     * @return true iff (x,y) was not already mandatory subgraph
     * @throws ContradictionException 
     */
    boolean enforceSuccessor(int x, int y, ICause cause) throws ContradictionException;
}
