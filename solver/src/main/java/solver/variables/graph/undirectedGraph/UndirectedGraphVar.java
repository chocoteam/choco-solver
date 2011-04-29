package solver.variables.graph.undirectedGraph;

import choco.kernel.memory.IEnvironment;
import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;

/**
 * Created by IntelliJ IDEA.
 * User: chameau, Jean-Guillaume Fages
 * Date: 7 févr. 2011
 */
public class UndirectedGraphVar extends GraphVar<StoredUndirectedGraph> {

	//////////////////////////////// GRAPH PART /////////////////////////////////////////
	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	
	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public UndirectedGraphVar(IEnvironment env, int nbNodes, GraphType type) {
		super(env);
    	envelop = new StoredUndirectedGraph(environment, nbNodes, type);
    	kernel = new StoredUndirectedGraph(environment, nbNodes, type);
    	kernel.activeIdx.clear();
    }

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public boolean removeArc(int x, int y, ICause cause) throws ContradictionException {
    	if(kernel.edgeExists(x, y)){
    		ContradictionException.throwIt(cause, this, "remove mandatory arc");
        	return false;
    	}
        if (envelop.removeEdge(x, y)){
        	if (reactOnModification){
        		delta.getArcRemovalDelta().add((x+1)*getEnvelopGraph().getNbNodes()+y);
        	}
        	EventType e = EventType.REMOVEARC;
        	notifyObservers(e, cause);
        	return true;
        }return false;
    }
    public boolean enforceArc(int x, int y, ICause cause) throws ContradictionException {
    	enforceNode(x, cause);
    	enforceNode(y, cause);
    	if(envelop.edgeExists(x, y)){
        	if (kernel.addEdge(x, y)){
        		if (reactOnModification){
            		delta.getArcEnforcingDelta().add((x+1)*getEnvelopGraph().getNbNodes()+y);
            	}
            	EventType e = EventType.ENFORCEARC;
            	notifyObservers(e, cause);
            	return true;
        	}
    	}
    	ContradictionException.throwIt(cause, this, "enforce arc which is not in the domain");
    	return false;
    }
    
    //***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	@Override
	public StoredUndirectedGraph getKernelGraph() {
		return kernel;
	}

	@Override
	public StoredUndirectedGraph getEnvelopGraph() {
		return envelop;
	}
}
