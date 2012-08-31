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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 08/08/12
 * Time: 15:27
 */

package samples.graph;

import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.constraints.propagators.gary.IRelaxation;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.variables.graph.GraphVar;

public class GraphStrategyBench extends ArcStrategy {


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	int n;
	int mode;
	int[][] costs;
	IRelaxation relax;
	final int LEX = 0;
	final int MIN_DEGREE = 1;
	final int MAX_DEGREE = 2;
	final int MIN_COST = 3;
	final int MAX_COST = 4;
	final int IN_SUPPORT_LEX = 5;
	final int OUT_SUPPORT_LEX = 6;
	IStateInt prev;
//	TIntArrayList stack;
	boolean usetrick;

	public GraphStrategyBench(GraphVar graphVar, int[][] costMatrix, IRelaxation relaxation, int policy,boolean useTrick) {
		super(graphVar);
		this.usetrick = useTrick;
		mode = policy;
		costs = costMatrix;
		relax = relaxation;
		n = graphVar.getEnvelopGraph().getNbNodes();
		prev = graphVar.getSolver().getEnvironment().makeInt(-1);
//		stack = new TIntArrayList();
//		usetrick = false;
//		this.usetrick = false;
	}

	@Override
	public boolean computeNextArc() {
//		from = prev.get();
//		if(stack.size()>0){
//			from = stack.removeAt(stack.size()-1);
//		}
//		if(g.getSolver().getMeasures().getSolutionCount()==0){
//			mode = 3;
//		}else{
//			mode = 4;
//		}
//		constructivePath();
		boolean b = nextArc();
//		prev.set(from);
//		stack.add(from);
		return b;
	}

	public boolean nextArc() {
		if(usetrick)
		if(computeTrickyNextArc()){
			return true;
		}
		from = to = -1;
		int value = -1;
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(g.getEnvelopGraph().arcExists(i,j)&&!g.getKernelGraph().arcExists(i,j)){
					switch (mode){
						case LEX:
							from = i;
							to = j;
							return true;
						case MIN_DEGREE:
						case MAX_DEGREE:
							int v = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()
									+ g.getEnvelopGraph().getPredecessorsOf(j).neighborhoodSize();
							if(from==-1 || (v<value && mode==MIN_DEGREE) || (v>value && mode==MAX_DEGREE)){
								value = v;
								from = i;
								to = j;
							}break;
						case MIN_COST:
						case MAX_COST:
							int c = costs[i][j];
							if(from==-1 || (c<value && mode==MIN_COST) || (c>value && mode==MAX_COST)){
								value = c;
								from = i;
								to = j;
							}break;
						case IN_SUPPORT_LEX:
							if(relax.contains(i,j)){
								from = i;
								to = j;
								return true;
							}break;
						case OUT_SUPPORT_LEX:
							if(!relax.contains(i,j)){
								from = i;
								to = j;
								return true;
							}break;
						default : throw new UnsupportedOperationException("mode "+mode+" does not exist");
					}
				}
			}
		}
		return from!=-1;
	}

	public boolean computeTrickyNextArc() {
//		while(stack.size()>0 && from != -1 && g.getEnvelopGraph().getSuccessorsOf(from).neighborhoodSize()==
//				         g.getKernelGraph().getSuccessorsOf(from).neighborhoodSize()){
//			from = stack.removeAt(stack.size()-1);
//		}
		if(from == -1 || g.getEnvelopGraph().getSuccessorsOf(from).neighborhoodSize()==
				         g.getKernelGraph().getSuccessorsOf(from).neighborhoodSize()){
			return false;
		}
		to = -1;
		int value = -1;
		int i = from;
		for(int j=0;j<n;j++){
			if(g.getEnvelopGraph().arcExists(i,j)&&!g.getKernelGraph().arcExists(i,j)){
				switch (mode){
					case LEX:
						from = i;
						to = j;
						return true;
					case MIN_DEGREE:
					case MAX_DEGREE:
						int v = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()
								+ g.getEnvelopGraph().getPredecessorsOf(j).neighborhoodSize();
						if(to==-1 || (v<value && mode==MIN_DEGREE) || (v>value && mode==MAX_DEGREE)){
							value = v;
							from = i;
							to = j;
						}break;
					case MIN_COST:
					case MAX_COST:
						int c = costs[i][j];
						if(to==-1 || (c<value && mode==MIN_COST) || (c>value && mode==MAX_COST)){
							value = c;
							from = i;
							to = j;
						}break;
					case IN_SUPPORT_LEX:
						if(relax.contains(i,j)){
							from = i;
							to = j;
							return true;
						}break;
					case OUT_SUPPORT_LEX:
						if(!relax.contains(i,j)){
							from = i;
							to = j;
							return true;
						}break;
					default : throw new UnsupportedOperationException("mode "+mode+" does not exist");
				}
			}
		}
		return to!=-1;
	}

	private void constructivePath(){
		int x = 0;
		int y = g.getKernelGraph().getSuccessorsOf(x).getFirstElement();
		while(y!=-1){
			x = y;
			y = g.getKernelGraph().getSuccessorsOf(x).getFirstElement();
		}
		from = x;
	}
}
