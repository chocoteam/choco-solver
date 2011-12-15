package solver.constraints.propagators.gary.tsp.heaps;

import solver.variables.graph.GraphType;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.IDirectedGraph;

public class VerySimpleHeap implements Heap{

	int n;
	int root;
	double[] keys;
	private int[] mate;
	public IDirectedGraph path;

	public VerySimpleHeap(int nbNodes){
		n = nbNodes;
		root = -1;
		mate = new int[n];
		keys = new double[n];
		path = new DirectedGraph(n, GraphType.LINKED_LIST);
	}

	@Override
	public void add(int element, double element_key, int i) {
		if(path.getActiveNodes().isActive(element)){
			decreaseKey(element,element_key,i);
		}else{
			mate[element] = i;
			path.activateNode(element);
			keys[element]  = element_key;
			if(root==-1){
				root = element;
				return;
			}
			if(element_key<=keys[root]){
				path.addArc(element,root);
				root = element;
			}else{
				int p = root;
				int tmp = p;
				while(tmp!=-1 && keys[tmp]<element_key){
					p = tmp;
					tmp = path.getSuccessorsOf(tmp).getFirstElement();
					if(tmp == p){
						throw new UnsupportedOperationException();
					}
				}
				path.addArc(p,element);
				if(p==tmp){
					throw new UnsupportedOperationException(p+" : "+root);
				}
				if(tmp!=-1){
					path.removeArc(p,tmp);
					path.addArc(element,tmp);
				}
			}
		}
	}

	@Override
	public void decreaseKey(int element, double new_element_key, int newMate) {
		if(keys[element]<=new_element_key){
			return;
		}
		mate[element] = newMate;
		keys[element] = new_element_key;
		if(root != element){
			pullNode(element, new_element_key);
		}
	}

	private void pullNode(int element, double key) {
		int p = path.getPredecessorsOf(element).getFirstElement();
		int pp= -1;
		int ppp = p;
		if(p==element){
			throw new UnsupportedOperationException();
		}
		if(p!=-1){
			if(key<keys[ppp]){
				while(ppp!=-1 && key<keys[ppp]){
					pp = ppp;
					ppp= path.getPredecessorsOf(ppp).getFirstElement();
					if(ppp==pp){
						throw new UnsupportedOperationException("cycle");
					}
				}
				path.removeArc(p,element);
				if(ppp!=-1){
					path.addArc(ppp,element);
					path.removeArc(ppp,pp);
				}else{
					root = element;
				}
				int s = path.getSuccessorsOf(element).getFirstElement();
				if(s!=-1){
					path.removeArc(element,s);
					path.addArc(p,s);
				}
				path.addArc(element,pp);
			}
		}else{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public int pop() {
		if(isEmpty()){
			throw new UnsupportedOperationException();
		}
		int toReturn = root;
		root = path.getSuccessorsOf(toReturn).getFirstElement();
		path.desactivateNode(toReturn);
		return toReturn;
	}

	@Override
	public void clear() {
		root = -1;
		IActiveNodes act = path.getActiveNodes();
		for(int i=act.getFirstElement();i>=0;i=act.getNextElement()){
			act.desactivate(i);
		}
	}

	public int getMate(int i){
		return mate[i];
	}

	@Override
	public void remove(int element) {
		if(path.getActiveNodes().isActive(element)){
			if(root==element){
				root = path.getSuccessorsOf(element).getFirstElement();
				path.desactivateNode(element);
			}else{
				int p = path.getPredecessorsOf(element).getFirstElement();
				if(p==-1){
					throw new UnsupportedOperationException();
				}
				path.addArc(p, path.getSuccessorsOf(element).getFirstElement());
				path.desactivateNode(element);
			}
		}
	}

	public boolean isEmpty() {
		return root==-1;
	}
}
