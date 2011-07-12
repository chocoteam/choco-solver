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

package solver.search.strategy.strategy.graph;

import java.util.Random;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.graph.GraphDecision;
import solver.search.strategy.selectors.graph.arcs.LexArc;
import solver.search.strategy.selectors.graph.nodes.LexNode;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.assignments.Assignment;
import solver.variables.graph.GraphVar;

/**
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 1 April 2011
 */
public class GraphStrategy extends AbstractStrategy<GraphVar> {

	protected GraphVar g;
	protected NodeStrategy nodeStrategy;
	protected ArcStrategy arcStrategy;
	protected NodeArcPriority priority;

	public enum NodeArcPriority{
		NODES_THEN_ARCS{
			@Override
			protected int getNext(GraphStrategy gs){
				int fromTo = gs.nextNode();
				if(fromTo==-1){
					fromTo = gs.nextArc();
				}
				return fromTo;
			}
		},
		ARCS{
			@Override
			protected int getNext(GraphStrategy gs){
				return gs.nextArc();
			}
		},
		RANDOM{
			@Override
			protected int getNext(GraphStrategy gs){
				Random rd = new Random();
				if(rd.nextBoolean()){
					return NODES_THEN_ARCS.getNext(gs);
				}else{
					return ARCS.getNext(gs);
				}
			}
		};

		protected abstract int getNext(GraphStrategy gs);
	}

	public GraphStrategy(GraphVar g, NodeStrategy ns, ArcStrategy as, NodeArcPriority priority) {
		super(new GraphVar[]{g});
		this.g = g;
		this.nodeStrategy = ns;
		this.arcStrategy  = as;
		this.priority 	  = priority;
	}

	public GraphStrategy(GraphVar g) {
		this(g,new LexNode(g), new LexArc(g), NodeArcPriority.NODES_THEN_ARCS);
	}

	@Override
	public void init() {}

	@Override
	public Decision getDecision() {
		int fromTo = priority.getNext(this);
		if(fromTo == -1){
			return null;
		}
		return new GraphDecision(g, fromTo, Assignment.graph_enforcer);
	}

	public int nextNode(){
		return nodeStrategy.nextNode();
	}

	public int nextArc(){
		return arcStrategy.nextArc();
	}
}
