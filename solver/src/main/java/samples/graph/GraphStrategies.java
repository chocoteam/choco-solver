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

import solver.constraints.propagators.gary.IGraphRelaxation;
import solver.search.strategy.assignments.GraphAssignment;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.graph.GraphDecision;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.graph.GraphVar;

public class GraphStrategies extends GraphStrategy {


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public static final int FIRST = 0;
	public static final int LAST = 14;
	// heuristics
	static final int LEX = 0;
	static final int MIN_P_DEGREE = 1;
	static final int MAX_P_DEGREE = 2;
	static final int MIN_M_DEGREE = 3;
	static final int MAX_M_DEGREE = 4;
	static final int MIN_COMMON = 5;
	static final int MAX_COMMON = 6;
	static final int MIN_COST = 7;
	static final int MAX_COST = 8;
	static final int IN_SUPPORT_LEX = 9;
	static final int OUT_SUPPORT_LEX = 10;
	static final int MIN_REDUCED_COST = 11;
	static final int MAX_REDUCED_COST = 12;
	static final int MIN_REPLACEMENT_COST = 13;
	static final int MAX_REPLACEMENT_COST = 14;

	// variables
	int n;
	int mode;
	int[][] costs;
	IGraphRelaxation relax;
	boolean usetrick,constructive;
	GraphAssignment decisionType;
	int from,to;
	int value;

	public GraphStrategies(GraphVar graphVar, int[][] costMatrix, IGraphRelaxation relaxation) {
		super(graphVar,null,null,null);
		costs = costMatrix;
		relax = relaxation;
		n = costMatrix.length;
	}

	public void configure(int policy, boolean enforce, boolean useTrick, boolean construct){
		this.usetrick = useTrick;
		this.constructive = construct;
		if(construct && !useTrick){
			throw new UnsupportedOperationException();
		}
		if(enforce){
			decisionType = GraphAssignment.graph_enforcer;
		}else{
			decisionType = GraphAssignment.graph_remover;
		}
		mode = policy;
	}

	@Override
	public Decision getDecision() {
		if(g.instantiated()){
			return null;
		}
		GraphDecision dec = pool.getE();
		if(dec == null){
			dec = new GraphDecision(pool);
		}
		computeNextArc();
		dec.setArc(g,from,to, decisionType);
		return dec;
	}

	public void computeNextArc() {
//		if(g.getSolver().getMeasures().getSolutionCount()==0){
//			mode = 3;
//		}else{
//			mode = 4;
//		}
		if(constructive){
			constructivePath();
		}
		if(usetrick)
			if(computeTrickyNextArc()){
				return;
			}
		from = to = -1;
		value = -1;
//		for(int i=0;i<n;i++){
//			if(DCMST.dMax[i]==1)
//			if(evaluateNeighbors(i)){
//				return;
//			}
//		}
		for(int i=0;i<n;i++){
			if(evaluateNeighbors(i)){
				return;
			}
		}
		if (to==-1){
			throw new UnsupportedOperationException();
		}
	}

	public boolean computeTrickyNextArc() {
		if(from == -1
		|| g.getKernelGraph().edgeExists(from,to)//TODO remettre
		|| g.getEnvelopGraph().getSuccessorsOf(from).getSize() == g.getKernelGraph().getSuccessorsOf(from).getSize()
				){
			return false;
		}
		to = -1;
		value = -1;
		evaluateNeighbors(from);
		return to!=-1;
	}

	private void constructivePath(){
		int x = 0;
		int y = g.getKernelGraph().getSuccessorsOf(x).getFirstElement();
		while(y!=-1){
			x = y;
			y = g.getKernelGraph().getSuccessorsOf(x).getFirstElement();
			if(y==-1){
				y = g.getKernelGraph().getSuccessorsOf(x).getNextElement();
			}
		}
		from = x;
	}

	public boolean evaluateNeighbors(int i) {
		int v;
		for(int j=0;j<n;j++){
			if(g.getEnvelopGraph().arcExists(i,j)&&!g.getKernelGraph().arcExists(i,j)){
				switch (mode){
					case LEX:
						from = i;
						to = j;
						return true;
					case MIN_P_DEGREE:
					case MAX_P_DEGREE:
						v = g.getEnvelopGraph().getSuccessorsOf(i).getSize()
								+ g.getEnvelopGraph().getPredecessorsOf(j).getSize();
						if(from==-1 || (v<value && mode==MIN_P_DEGREE) || (v>value && mode==MAX_P_DEGREE)){
							value = v;
							from = i;
							to = j;
						}break;
					case MIN_M_DEGREE:
					case MAX_M_DEGREE:
						v = g.getKernelGraph().getSuccessorsOf(i).getSize()
								+ g.getKernelGraph().getPredecessorsOf(j).getSize();
						if(from==-1 || (v<value && mode==MIN_M_DEGREE) || (v>value && mode==MAX_M_DEGREE)){
							value = v;
							from = i;
							to = j;
						}break;
					case MIN_COMMON:
					case MAX_COMMON:
						v = 0;
						for(int k=0;k<n;k++){
							if(g.getEnvelopGraph().getSuccessorsOf(k).contain(i)){
								v++;
							}
							if(g.getEnvelopGraph().getSuccessorsOf(k).contain(j)){
								v++;
							}
						}
						if(from==-1 || (v<value && mode==MIN_COMMON) || (v>value && mode==MAX_COMMON)){
							value = v;
							from = i;
							to = j;
						}break;
					case MIN_COST:
					case MAX_COST:
						v = costs[i][j];
						if(from==-1 || (v<value && mode==MIN_COST) || (v>value && mode==MAX_COST)){
							value = v;
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
					case MIN_REDUCED_COST:
					case MAX_REDUCED_COST:
						if(!relax.contains(i,j)){
							v = (int)relax.getMarginalCost(i,j);
							if(from==-1 || (v<value && mode==MIN_REDUCED_COST) || (v>value && mode==MAX_REDUCED_COST)){
								value = v;
								from = i;
								to = j;
							}
						}break;
					case MIN_REPLACEMENT_COST:
					case MAX_REPLACEMENT_COST:
						if(relax.contains(i,j)){
							v = (int)relax.getReplacementCost(i,j);
							if(from==-1 || (v<value && mode==MIN_REPLACEMENT_COST) || (v>value && mode==MAX_REPLACEMENT_COST)){
								value = v;
								from = i;
								to = j;
							}
						}break;
					default : throw new UnsupportedOperationException("mode "+mode+" does not exist");
				}
			}
		}
		return false;
	}
}
