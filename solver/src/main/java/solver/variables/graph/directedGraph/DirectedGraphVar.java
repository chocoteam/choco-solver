package solver.variables.graph.directedGraph;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Random;

import choco.kernel.memory.IEnvironment;
import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;

/**
 * Created by IntelliJ IDEA.
 * User: chameau, Jean-Guillaume Fages
 * Date: 7 févr. 2011
 */
public class DirectedGraphVar extends GraphVar<StoredDirectedGraph> {

	////////////////////////////////// GRAPH PART ///////////////////////////////////////
	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	public static long seed = 1;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public DirectedGraphVar(IEnvironment env, BitSet[] data, GraphType typeEnv, GraphType typeKer) {
		super(env);
		envelop = new StoredDirectedGraph(environment, data, typeEnv);
		kernel = new StoredDirectedGraph(environment, data.length, typeKer);
		kernel.getActiveNodes().clear();
	}
	public DirectedGraphVar(IEnvironment env, BitSet[] data, GraphType type) {
		this(env,data,type,type);
	}
	public DirectedGraphVar(IEnvironment env, int nbNodes, GraphType typeEnv, GraphType typeKer) {
		super(env);
		envelop = new StoredDirectedGraph(environment, nbNodes, typeEnv);
		kernel = new StoredDirectedGraph(environment, nbNodes, typeKer);
		kernel.getActiveNodes().clear();
	}
	public DirectedGraphVar(IEnvironment env, int nbNodes, GraphType type) {
		this(env,nbNodes,type,type);
	}
	

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public DirectedGraphVar(IEnvironment environment, int n, GraphType dense, String options) {
		this(environment,n,dense);
		if(options.equals("clique")){
			for (int i=0;i<n;i++){
				for(int j = 0;j<n ; j++){
					envelop.addArc(i, j);
				}
			}
		}
	}

	@Override
	public boolean instantiated() {
		for(int n=envelop.activeIdx.nextValue(0); n>=0; n=envelop.activeIdx.nextValue(n+1)){
			if(!kernel.activeIdx.isActive(n)){
				return false;
			}
			AbstractNeighborsIterator<INeighbors> iter = envelop.successorsIteratorOf(n);
			while (iter.hasNext()){
				if(!kernel.arcExists(n, iter.next())){
					return false;
				}
			}
		}
		return true;
	}
	@Override
	public boolean removeArc(int x, int y, ICause cause) throws ContradictionException {
		if(kernel.arcExists(x, y)){
			ContradictionException.throwIt(cause, this, "remove mandatory arc");
			return false;
		}
		if (envelop.removeArc(x, y)){
			if (reactOnModification){
				delta.getArcRemovalDelta().add((x+1)*getEnvelopGraph().getNbNodes()+y);
			}
			EventType e = EventType.REMOVEARC;
			notifyObservers(e, cause);
			return true;
		}return false;
	}
	@Override
	public boolean enforceArc(int x, int y, ICause cause) throws ContradictionException {
		enforceNode(x, cause);
		enforceNode(y, cause);
		if(envelop.arcExists(x, y)){
			if (kernel.addArc(x, y)){
				if (reactOnModification){
					delta.getArcEnforcingDelta().add((x+1)*getEnvelopGraph().getNbNodes()+y);
				}
				EventType e = EventType.ENFORCEARC;
				notifyObservers(e, cause);
				return true;
			}return false;
		}
		ContradictionException.throwIt(cause, this, "enforce arc which is not in the domain");
		return false;
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	@Override
	public StoredDirectedGraph getKernelGraph() {
		return kernel;
	}

	@Override
	public StoredDirectedGraph getEnvelopGraph() {
		return envelop;
	}

	///////////////////////////////////////////////////////////////////////

	/**UGLY
	 * @return a randomly choosen arc 
	 */
	public int nextArc() {
		return nextArcRandom2();
		//		return nextArcLexicographic();
	}
	private int nextArcLexicographic() {
		int n = getEnvelopGraph().getNbNodes();
		for (int i=getEnvelopGraph().getActiveNodes().nextValue(0);i>=0;i=getEnvelopGraph().getActiveNodes().nextValue(i+1)){
			if(envelop.successors[i].neighborhoodSize() != kernel.successors[i].neighborhoodSize()){
				AbstractNeighborsIterator<INeighbors> iter = envelop.successors[i].iterator();
				int j;
				while(iter.hasNext()){
					j = iter.next();
					if (!kernel.arcExists(i, j)){
						return (i+1)*n+j;
					}
				}
			}
		}
		return -1;
	}
	private int nextArcRandom() {
		int n = getEnvelopGraph().getNbNodes();
		LinkedList<Integer> arcs = new LinkedList<Integer>();
		for (int i=getEnvelopGraph().getActiveNodes().nextValue(0);i>=0;i=getEnvelopGraph().getActiveNodes().nextValue(i+1)){
			if(envelop.successors[i].neighborhoodSize() != kernel.successors[i].neighborhoodSize()){
				AbstractNeighborsIterator<INeighbors> iter = envelop.successors[i].iterator();
				int j;
				while(iter.hasNext()){
					j = iter.next();
					if (!kernel.arcExists(i, j)){
						arcs.addFirst((i+1)*n+j);
					}
				}
			}
		}
		if(arcs.size()==0)return -1;
		Random rd = new Random(seed);
		return arcs.get(rd.nextInt(arcs.size()));
	}
	private int nextArcRandom2() {
		int n = getEnvelopGraph().getNbNodes();
		LinkedList<Integer> arcs = new LinkedList<Integer>();
		for (int i=getEnvelopGraph().getActiveNodes().nextValue(0);i>=0;i=getEnvelopGraph().getActiveNodes().nextValue(i+1)){
			if(envelop.successors[i].neighborhoodSize() != kernel.successors[i].neighborhoodSize()){
				if(kernel.successors[i].neighborhoodSize()>0){
					throw new UnsupportedOperationException("error in 1-succ filtering");
				}
				arcs.add(i);
			}
		}
		if(arcs.size()==0)return -1;
		Random rd = new Random(seed);
		int node = arcs.get(rd.nextInt(arcs.size()));
		arcs.clear();
		AbstractNeighborsIterator<INeighbors> iter = envelop.successors[node].iterator();
		int j;
		while(iter.hasNext()){
			j = iter.next();
			if (!kernel.arcExists(node, j)){
				arcs.addFirst((node+1)*n+j);
			}
		}
		return arcs.get(rd.nextInt(arcs.size()));
	}
}
