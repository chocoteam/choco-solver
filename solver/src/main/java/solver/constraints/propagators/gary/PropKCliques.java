/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.gary;

import choco.kernel.ESat;
import gnu.trove.TIntArrayList;
import solver.Solver;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;
import java.util.BitSet;
import java.util.LinkedList;

/**Propagator that ensures that the final graph consists in K cliques
 * @author Jean-Guillaume Fages
 */
public class PropKCliques<V extends Variable> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	public static long duration;
	private GraphVar g;
	private IntVar k;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropKCliques(GraphVar graph, Solver solver, GraphConstraint constraint, IntVar k) {
		super((V[]) new Variable[]{graph,k}, solver, constraint, PropagatorPriority.LINEAR, false);//
		g = graph;
		this.k = k;
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		duration = 0;
	}

	
	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
//		homogeniousCase();
		long time = System.currentTimeMillis();
		case2();
		duration += (System.currentTimeMillis()-time);
	}
	
	private void homogeniousCase() throws ContradictionException{
		float n = g.getEnvelopGraph().getNbNodes();
		LinkedList<TIntArrayList> ccs = ConnectivityFinder.findCCOf(g.getEnvelopGraph());
		int[] ccOf = new int[(int)n];
		float[] mCC = new float[ccs.size()];
		float[] nCC = new float[ccs.size()];
		// finding CC
		TIntArrayList cc;
		for(int i=0;i<ccs.size();i++){
			cc = ccs.get(i);
			nCC[i] = cc.size();
			for(int j=0;j<cc.size();j++){
				ccOf[cc.get(j)] = i;
			}
		}
		// arcs of the CC
		INeighbors nei;
		int ccNum;
		for(int i=0;i<n;i++){
			ccNum = ccOf[i];
			nei = g.getEnvelopGraph().getNeighborsOf(i);
			for(int j=nei.getFirstElement(); j>=0; j = nei.getNextElement()){
				if(ccOf[j]==ccNum)	mCC[ccNum] ++;
			}
		}
		//	estimation nClique per CC
		int min = 0;
		for(int i=0;i<ccs.size();i++){
			mCC[i] = (mCC[i]-nCC[i])/2+nCC[i];
			float k2 = nCC[i]*nCC[i] / (nCC[i]+2*mCC[i]);
			min += Math.ceil(k2);
//			int k = 1;
//			int n1 = nCC[i]-k+1;
////			int k2 = (int) Math.ceil((double)(mCC[i]-nCC[i])/(double)nCC[i]);
//			int mMax = n1*(n1-1)/2+nCC[i];
//			if(mMax<mCC[i]){
//				System.out.println(mMax);
//				System.out.println(mCC[i]);
//				throw new UnsupportedOperationException("error ");
//			}
//			while(mMax>mCC[i]){
//				k ++;
//				n1 = nCC[i]-k+1;
//				mMax = n1*(n1-1)/2+nCC[i];
//			}
////			min += k1;
////			max += k2;
//			min += k;
		}
//		float m =0;
//		INeighbors nei;
//		int ccNum;
//		int min = 0;
//		for(int i=0;i<n;i++){
//			nei = g.getEnvelopGraph().getNeighborsOf(i);
//			m += nei.neighborhoodSize();
//		}
//		m = (m-n)/2+n;
//			int kk = 1;
//			int n1 = n-kk+1;
//			int k2 = (int) Math.ceil((double)(m-n)/(double)n);
//			int mMax = n1*(n1-1)/2+n;
//			if(mMax<m){
//				System.out.println(mMax);
//				System.out.println(m);
//				throw new UnsupportedOperationException("error ");
//			}
//			while(mMax>m){
//				kk ++;
//				n1 = n-kk+1;
//				mMax = n1*(n1-1)/2+n;
//			}
////			min += k1;
////			max += k2;
//			min += kk;
//		float k2 = n*n / (n+2*m);
//		int k2 = Math.round((float)n/(float)(1+(float)(2*m)/(float)n));
//			System.out.println(m);
//			System.out.println(n);
//			System.out.println("kkk");
//			System.out.println(k2);
//		System.out.println(min);
		k.updateLowerBound(min, this);
//		k.updateUpperBound(k2, this);
	}
	
	private void case2() throws ContradictionException{
		float n = g.getEnvelopGraph().getNbNodes();
		BitSet iter = new BitSet((int)n);
		iter.flip(0, (int)n);
		int idx = -1;
		INeighbors nei;
		int min = 0;
		while (iter.cardinality()>0){
			idx = iter.nextSetBit(idx+1);
			nei = g.getEnvelopGraph().getNeighborsOf(idx);
			iter.clear(idx);
			for(int j=nei.getFirstElement(); j>=0; j = nei.getNextElement()){
				iter.clear(j);
			}
			min ++;
		}
//		System.out.println("         :   "+min);
		k.updateLowerBound(min, this);
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVENODE.mask +  EventType.REMOVEARC.mask +  EventType.ENFORCENODE.mask +  EventType.ENFORCEARC.mask + EventType.ALL_MASK();
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
		
}
