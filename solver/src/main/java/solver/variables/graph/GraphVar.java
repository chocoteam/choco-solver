package solver.variables.graph;

import choco.kernel.memory.IEnvironment;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.requests.IRequest;
import solver.requests.list.IRequestList;
import solver.requests.list.RequestListBuilder;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.domain.delta.GraphDelta;
import solver.variables.domain.delta.IGraphDelta;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;

/**
 * Created by IntelliJ IDEA.
 * User: chameau, Jean-Guillaume Fages
 * Date: 7 févr. 2011
 */
public abstract class GraphVar<E extends IStoredGraph> implements Variable<IGraphDelta>, IVariableGraph {

	//////////////////////////////// GRAPH PART /////////////////////////////////////////
	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected E envelop, kernel;
	protected IEnvironment environment;
	protected IGraphDelta delta;
	
	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public GraphVar(IEnvironment env) {
    	environment = env;
    	requests = RequestListBuilder.preset(env);
    }

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
    public boolean instantiated() {
    	for(int n=envelop.getActiveNodes().nextValue(0); n>=0; n=envelop.getActiveNodes().nextValue(n+1)){
    		if(!kernel.getActiveNodes().isActive(n)){
    			return false;
    		}
    		AbstractNeighborsIterator<INeighbors> iter = envelop.neighborsIteratorOf(n);
    		while (iter.hasNext()){
    			if(!kernel.edgeExists(n, iter.next())){
    				return false;
    			}
    		}
    	}
        return true;
    }
	@Override
	public boolean removeNode(int x, ICause cause) throws ContradictionException {
    	if(kernel.getActiveNodes().isActive(x)){
    		ContradictionException.throwIt(cause, this, "remove mandatory node");
        	return true;
    	}
        if (envelop.desactivateNode(x)){
        	if (reactOnModification){
        		delta.getNodeRemovalDelta().add(x);
        	}
        	EventType e = EventType.REMOVENODE;
        	notifyObservers(e, cause);
        	return true;
        }return false;
    }
    @Override
	public boolean enforceNode(int x, ICause cause) throws ContradictionException {
    	if(envelop.getActiveNodes().isActive(x)){
    		if (kernel.activateNode(x)){
    			if (reactOnModification){
            		delta.getNodeEnforcingDelta().add(x);
            	}
    			EventType e = EventType.ENFORCENODE;
            	notifyObservers(e, cause);
            	return true;
    		}return false;
    	}
    	ContradictionException.throwIt(cause, this, "enforce node which is not in the domain");
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
	
	//***********************************************************************************
	// VARIABLE STUFF
	//***********************************************************************************
	
    ///////////// Attributes related to Variable ////////////
	protected String name;
    protected Solver solver;
    protected IRequestList<IRequest> requests;
    protected int modificationEvents;
    protected boolean reactOnModification;
	/////////////////////////////////////////////////////////
	

    //////////////////// Accessors related to Variable ///////////////////
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public int nbConstraints() {
        return requests.size();
    }
    ///////////////////////////////////////////////////////////////////////



    ////////////////////// Method related to Variable /////////////////////
    @Override
    public void updateEntailment(IRequest request) {
    	requests.setPassive(request);
    }
    @Override
    public void addRequest(IRequest request) {
    	requests.addRequest(request);
    }
    @Override
    public void deleteRequest(IRequest request) {
    	requests.deleteRequest(request);
    }

    @Override
    public Explanation explain() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IGraphDelta getDelta() {
        return delta;
    }

    @Override
    public void addObserver(ICause observer) {
    	modificationEvents |= observer.getPropagationConditions();
        if(!reactOnModification){
        	reactOnModification = true;
        	delta = new GraphDelta();
        }
    }
    @Override
    public void deleteObserver(ICause observer) {
    	throw new UnsupportedOperationException();
    }
    @Override
    public void notifyObservers(EventType e, ICause cause) throws ContradictionException {
    	if ((modificationEvents & e.mask) != 0) {
            requests.notifyButCause(cause, e, getDelta());
        }
    }
    @Override
	public int nbRequests() {
		return requests.cardinality();
	}
}
