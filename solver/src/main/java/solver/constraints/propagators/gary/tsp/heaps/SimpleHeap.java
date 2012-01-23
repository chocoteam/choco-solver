package solver.constraints.propagators.gary.tsp.heaps;

import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.IDirectedGraph;
import java.util.BitSet;

public class SimpleHeap implements Heap{


	int n;
	int[] rootOfDeg;
	int[] rootIdx;
	double[] keys;
	BitSet isRoot;
	int[] roots;
	int nbRoots;
	public IDirectedGraph forest;
	private int minimumRoot;
	private int[] mate;

	public SimpleHeap(int nbNodes){
		n = nbNodes;
		rootIdx = new int[n];
		roots = new int[n];
		rootOfDeg = new int[n];
		mate      = new int[n];
		keys = new double[n];
		isRoot = new BitSet(n);
		nbRoots= 0;
		forest = new DirectedGraph(n, GraphType.LINKED_LIST);
		minimumRoot = -1;
	}

	@Override
	public void add(int element, double element_key, int i) {
		if(forest.getActiveNodes().isActive(element)){
			decreaseKey(element,element_key,i);
			return;
		}
		mate[element] = i;
		forest.activateNode(element);
		roots[nbRoots] = element;
		keys[element]  = element_key;
		isRoot.set(element);
		rootIdx[element] = nbRoots;
		nbRoots++;
		if(nbRoots==1 || element_key < keys[minimumRoot]){
			minimumRoot = element;
		}
		rootOfDeg[0] = element;
	}

	@Override
	public void decreaseKey(int element, double new_element_key, int newMate) {
		if(keys[element]<=new_element_key){
			return;
		}
		keys[element] = new_element_key;
		if(isRoot.get(element)){
			if(new_element_key<keys[minimumRoot]){
				minimumRoot = element;
			}
		}else{
			pullNode(element);
		}
	}

	private void pullNode(int element) {
		int p = forest.getPredecessorsOf(element).getFirstElement();
		int pp= -1;
		int ppp = p;
		if(p==element){
			throw new UnsupportedOperationException();
		}
		double key = keys[element];
		if(p!=-1){
			if(key<keys[ppp]){
				while(ppp!=-1 && key<keys[ppp]){
					pp = ppp;
					ppp= forest.getPredecessorsOf(ppp).getFirstElement();
					if(ppp==pp){
						throw new UnsupportedOperationException("cycle");
					}
				}
				INeighbors nei = forest.getSuccessorsOf(element);
				for(int s=nei.getFirstElement();s>=0;s=nei.getNextElement()){
					forest.addArc(p,s);
					forest.removeArc(element,s);
				}
				forest.removeArc(p,element);
				forest.addArc(element,pp);
				if(ppp!=-1){
					forest.addArc(ppp,element);
					forest.removeArc(ppp,pp);
				}else{
					add(element,key, mate[element]);
				}
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
		INeighbors nei = forest.getSuccessorsOf(minimumRoot);
		isRoot.clear(minimumRoot);
		nbRoots--;
		if(nbRoots>0){
			roots[rootIdx[minimumRoot]] = roots[nbRoots];
			rootIdx[roots[nbRoots]] = rootIdx[minimumRoot];
		}
		int toReturn = minimumRoot;
		for(int i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
			roots[nbRoots] = i;
			rootIdx[i] = nbRoots;
			isRoot.set(i);
			nbRoots++;
		}
		forest.desactivateNode(minimumRoot);
		for(int i=0;i<nbRoots-1;i++){
			rootOfDeg[i] = -1;
		}
		for(int i=0;i<nbRoots-1;i++){
			consider(roots[i]);
		}
		if(nbRoots>0){
			minimumRoot = roots[0];
		}else{
			minimumRoot = -1;
		}
		for(int i=0;i<nbRoots-1;i++){
			if(keys[roots[i]]<keys[minimumRoot]){
				minimumRoot = roots[i];
			}
		}
		return toReturn;
	}

	@Override
	public void clear() {
		nbRoots = 0;
		for(int i=0;i<n;i++){
			forest.desactivateNode(i);
		}
	}

	public int getMate(int i){
		return mate[i];
	}

	private void consider(int r){
		if(!isRoot.get(r)){
			return;
		}
		int deg = forest.getSuccessorsOf(r).neighborhoodSize();
		int toRem;
		int toC;
		int rod = rootOfDeg[deg];
		if(rod==-1 || rod==r){
			rootOfDeg[deg] = r;
		}else{
			double kr = keys[r];
			if(r == rod){
				throw new UnsupportedOperationException();
			}
			if(keys[rod]<kr){
				forest.addArc(rod, r);
				toRem = r;
				toC   = rod;
			}else{
				forest.addArc(r,rod);
				toRem = rod;
				toC   = r;
			}
			nbRoots--;
			isRoot.clear(toRem);
			roots[rootIdx[toRem]] = roots[nbRoots];
			rootIdx[roots[nbRoots]] = rootIdx[toRem];
			rootOfDeg[deg] = -1;
			consider(roots[rootIdx[toRem]]);
			consider(toC);
		}
	}

	public void remove(int i){
		
	}
//	private int[] key; // Key !
//	private int[] nodesAt;
//	private int[] indexOf;
//	private int size;
//
//	public SimpleHeap(final EnvironmentTrailing _env, int nbNodes) {
//		this.size = 0;
//		nodesAt   = new int[nbNodes];
//		indexOf   = new int[nbNodes];
//		key		  = new int[nbNodes];
//	}
//
//	public void clear() {
//		size = 0;
//	}
//
//	public void add(int node, int node_key) {
//		int i = size;
//		int parent = parent(i);
//		while ((i>0) && (key[parent] > node_key) ) {
//			key[i] = key[parent];
//			nodesAt[i] = nodesAt[parent];
//			i = parent;
//			parent = parent(i);
//		}
//		key[i] = key[node_key];
//		nodesAt[i] = nodesAt[node];
//		size++;
//	}
//
//	public int removeFirst() {
//		if (size == 0) {
//			return -1;
//		}
//		int top= nodesAt[0];
//		size--;
//		key[0] = key[size];
//		nodesAt[0] = nodesAt[0];
//		int vk = key[0];
//		int vn = nodesAt[0];
//		int i = 0;
//		int j;
//		while (!isLeaf(i)) {
//			j = leftChild(i);
//			if (hasRightChild(i) && key[rightChild(i)] < key[leftChild(i)]) {
//				j = rightChild(i);
//			}
//			if (vk <= key[j]) break;
//			key[i] = key[j];
//			nodesAt[i] = nodesAt[j];
//			i = j;
//		}
//		key[i] =  vk;
//		nodesAt[i] = vn;
//		return top;
//	}
//
//	public int peek() {
//		if (isEmpty()) return -1;
//		else {
//			return nodesAt[0];
//		}
//	}
//
//	public boolean isEmpty() {
//		return (size == 0);
//	}
//
//	private int parent(int _child) {
//		return ((_child + 1) >> 1) - 1;
//	}
//
//	private int leftChild(int _parent) {
//		return ((_parent + 1) << 1) - 1;
//	}
//
//	private int rightChild(int _parent) {
//		return ((_parent + 1) << 1);
//	}
//
//	private boolean isLeaf(int i) {
//		return ( (((i + 1) << 1) - 1) >= size);
//	}
//
//	private boolean hasRightChild(int i) {
//		return ( ((i + 1) << 1) < size);
//	}
//
//	public int size() {
//		return size;
//	}

//	// _____FH_____
//
//	private int getParent(int i){
//		if(i<0 || i>size){
//			throw new UnsupportedOperationException("");
//		}
//		if(i==0){
//			return 0;
//		}
//		return (i-1)/2;
//	}
//
//	private void monter(int node){
//		boolean bienPlace = false;
//		int i = indexOf[node];
//		if(i<0 || i>=size){
//			throw new UnsupportedOperationException("");
//		}
//		int pIdx;
//		int parentNode;
//		while(i>0 && !bienPlace){
//			pIdx = (i-1)/2;
//			parentNode = nodesAt[pIdx];
//			if(key[parentNode]<key[node]){
//				bienPlace = true;
//			}else{
//				nodesAt[parentNode] = i;
//				indexOf[i] = parentNode;
//				i = (i-1)/2;
//			}
//		}
//		indexOf[node] = i;
//		nodesAt[i] = node;
//	}
//
//	private void descendre(int node){
//		boolean bienPlace = false;
//		boolean noFils    = false;
//		int i = indexOf[node];
//		if(i<0 || i>=size){
//			throw new UnsupportedOperationException("");
//		}
//		int pIdx;
//		int parentNode;
//		while((!noFils) && (!bienPlace)){
//			if(2*i+2 < size){
//
//			}
//		}
//	}

	public boolean isEmpty() {
		return nbRoots==0;
	}
}
