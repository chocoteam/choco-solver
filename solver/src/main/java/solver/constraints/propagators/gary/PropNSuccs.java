package solver.constraints.propagators.gary;

import java.util.LinkedList;
import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IEnvironment;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.domain.delta.IntDelta;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.requests.GraphRequest;
import solver.requests.IRequest;

/**
 * @author Jean-Guillaume Fages
 * 
 * Ensures that each node in the kernel has exactly NSUCCS successor
 *
 * @param <V>
 */
public class PropNSuccs<V extends DirectedGraphVar> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int nSuccs;
	RemProc rem;
	EnfProc enf;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNSuccs(
			V graph,
			IEnvironment environment,
			Constraint<V, Propagator<V>> constraint,
			PropagatorPriority priority, boolean reactOnPromotion, int nbSuccs) {
		super((V[]) new DirectedGraphVar[]{graph}, environment, constraint, priority, reactOnPromotion);
		g = graph;
		nSuccs = nbSuccs;
		rem = new RemProc(this);
		enf = new EnfProc(this);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {check();}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		if (request instanceof GraphRequest) {
			GraphRequest gv = (GraphRequest) request;
			if ((mask & EventType.REMOVEARC.mask)!=0){
				IntDelta d = (IntDelta) g.getDelta().getArcRemovalDelta();
				d.forEach(rem, gv.fromArcRemoval(), gv.toArcRemoval());
			}if ((mask & EventType.ENFORCEARC.mask) != 0){
				IntDelta d = (IntDelta) g.getDelta().getArcEnforcingDelta();
				d.forEach(enf, gv.fromArcEnforcing(), gv.toArcEnforcing());
			}
		}
	}

	@Override
	public int getPropagationConditions() {
		return EventType.REMOVEARC.mask+EventType.ENFORCEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	/** Enable to add arcs to the kernel when only NSUCCS arcs remain in the envelop */
	private static class RemProc implements IntProcedure {

		private final PropNSuccs p;

		public RemProc(PropNSuccs p) {
			this.p = p;
		}

		@Override
		public void execute(int i) throws ContradictionException {
			int n = p.g.getEnvelopGraph().getNbNodes();
			if (i>=n){
				int from = i/n-1;
				INeighbors succs = p.g.getEnvelopGraph().getSuccessorsOf(from);
				if(succs.neighborhoodSize()==p.nSuccs && p.g.getKernelGraph().getSuccessorsOf(from).neighborhoodSize()!=p.nSuccs){
					AbstractNeighborsIterator<INeighbors> iter = succs.iterator();
					while (iter.hasNext()){
						p.g.enforceArc(from, iter.next(), p);
					}
				}
			}else{
				throw new UnsupportedOperationException();
			}
		}
	}

	/** Enable to remove useless outgoing arcs of a node when the kernel contains NSUCCS outgoing arcs */
	private static class EnfProc implements IntProcedure {

		private final PropNSuccs p;

		public EnfProc(PropNSuccs p) {
			this.p = p;
		}

		@Override
		public void execute(int i) throws ContradictionException {
			int n = p.g.getEnvelopGraph().getNbNodes();
			if (i>=n){
				int from = i/n-1;
				int eto   = i%n;
				INeighbors succs = p.g.getEnvelopGraph().getSuccessorsOf(from);
				if(succs.neighborhoodSize()>p.nSuccs && p.g.getKernelGraph().getSuccessorsOf(from).neighborhoodSize()==p.nSuccs){
					AbstractNeighborsIterator<INeighbors> iter = succs.iterator();
					LinkedList<Integer> toRemove = new LinkedList<Integer>();
					while (iter.hasNext()){
						eto = iter.next();
						if (!p.g.getKernelGraph().arcExists(from, eto)){
							toRemove.addFirst(eto);
						}
					}
					for(int next:toRemove){
						p.g.removeArc(from, next, p);
					}
				}
			}else{
				throw new UnsupportedOperationException();
			}
		}
	}

	private void check() throws ContradictionException {
		int n = g.getEnvelopGraph().getNbNodes();
		int k;
		LinkedList<Integer> arcs = new LinkedList<Integer>();
		for (int i=g.getEnvelopGraph().getActiveNodes().nextValue(0);i>=0;i=g.getEnvelopGraph().getActiveNodes().nextValue(i+1)){
			k = g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize();
			if(k>nSuccs){
				ContradictionException.throwIt(this, g, "more than one successor");
			}
			if(g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()<nSuccs){
				ContradictionException.throwIt(this, g, "not enough successors");
			}
			if(k==nSuccs && g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize() != k){
				AbstractNeighborsIterator<INeighbors> iter = g.getEnvelopGraph().getSuccessorsOf(i).iterator();
				int j;
				while(iter.hasNext()){
					j = iter.next();
					if (!g.getKernelGraph().arcExists(i, j)){
						arcs.addFirst((i+1)*n+j);
					}
				}
			}
			if(k<nSuccs && g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize() == nSuccs){
				AbstractNeighborsIterator<INeighbors> iter = g.getEnvelopGraph().getSuccessorsOf(i).iterator();
				int j;
				while(iter.hasNext()){
					j = iter.next();
					if (!g.getKernelGraph().arcExists(i, j)){
						arcs.addFirst((i+1)*n+j);
					}
					g.enforceArc(i, j, this);
				}
			}
		}
		for(int next:arcs){
			g.removeArc(next/n-1, next%n, this);
		}
	}
}
