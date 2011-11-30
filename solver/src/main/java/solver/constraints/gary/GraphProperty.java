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
package solver.constraints.gary;

import solver.Solver;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.*;
import solver.constraints.propagators.gary.basic.*;
import solver.constraints.propagators.gary.constraintSpecific.*;
import solver.constraints.propagators.gary.directed.*;
import solver.constraints.propagators.gary.tsp.PropOnePredBut;
import solver.constraints.propagators.gary.tsp.PropOneSuccBut;
import solver.variables.IntVar;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * @author Jean-Guillaume Fages
 * 
 * TODO : remplacer par (classe abstraite + Factory + implem)
 *
 */
public enum GraphProperty {

	// Trivial cases
	/** Each node in the solution must have a loop
	 * - No parameter */
	REFLEXIVITY {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			return new GraphPropagator[]{new PropEachNodeHasLoop(cons.graph, cons.getSolver(), cons)};
		}
	},
	/** (x,y) & (y,z) => (x,z)
	 * - No parameter */
	TRANSITIVITY {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			return new GraphPropagator[]{new PropTransitivity(cons.graph,cons.getSolver(), cons)};
		}
	},
	/** (x,y) <=> (y,x)
	 * - No parameter */
	SYMMETRY {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if (cons.graph instanceof DirectedGraphVar) {
				throw new UnsupportedOperationException("the graph is symmetric: an undirected graph should be used");
			}
			return new GraphPropagator[]{};
		}
	},
	/** (x,y) <=> !(y,x)
	 * - No parameter */
	ANTI_SYMMETRY {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if (cons.graph instanceof UndirectedGraphVar) {
				throw new UnsupportedOperationException("the graph is antisymmetric: a directed graph should be used");
			}
			return new GraphPropagator[]{new PropAntiSymmetric((DirectedGraphVar) cons.getGraph(), cons.getSolver(), cons)};
		}
	},
	// With parameter
	/** Restrict the number of nodes in the final graph
	 * - One parameter : an IntVar representing the expected number of nodes*/
	K_NODES {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(parameters.length==0){
				throw new UnsupportedOperationException("K_NODES requires a parameter K : (0 < K <= n)");
			}
			IntVar k = parameters[0];
			if(k.getLB()>cons.graph.getEnvelopGraph().getNbNodes() || k.getUB()<=0){
				throw new UnsupportedOperationException("K_NODES requires a parameter K : (0 < K <= n)");
			}
			return new GraphPropagator[]{new PropKNodes(cons.graph, cons.getSolver(), cons,k)};
		}
	},
	/** Restrict the number of loops in the final graph
	 * - One parameter : an IntVar representing the expected number of nodes*/
	K_LOOPS {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(parameters.length==0){
				throw new UnsupportedOperationException("K_LOOPS requires a parameter K : (0 <= K <= n)");
			}
			IntVar k = parameters[0];
			if(k.getLB()>cons.graph.getEnvelopGraph().getNbNodes() || k.getUB()<0){
				throw new UnsupportedOperationException("K_LOOPS requires a parameter K : (0 <= K <= n)");
			}
			return new GraphPropagator[]{new PropKLoops(cons.graph, cons.getSolver(), cons,k)};
		}
	},
	/** Restrict the number of arcs in the final graph.
	 * (If the graph is undirected it restricts the number of edges)
	 * - One parameter : an IntVar representing the expected number of arcs (or edges)*/
	K_ARCS {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(parameters.length==0){
				throw new UnsupportedOperationException("K_ARCS requires a parameter K : (0 < K <= n^2)");
			}
			IntVar k = parameters[0];
			if(k.getLB()>cons.graph.getEnvelopGraph().getNbNodes()*cons.graph.getEnvelopGraph().getNbNodes() || k.getUB()<=0){
				throw new UnsupportedOperationException("K_ARCS requires a parameter K : (0 < K <= n^2)");
			}
			if (cons.graph instanceof DirectedGraphVar) {
				DirectedGraphVar dig = (DirectedGraphVar) cons.graph;
				return new GraphPropagator[]{new PropKArcsDig(dig, cons.getSolver(), cons,k)};
			}else{
				return new GraphPropagator[]{new PropKArcsUndi((UndirectedGraphVar)cons.graph, cons.getSolver(), cons,k)};
			}
		}
	},
	/** Restrict the number of neighbors of each node in the final graph
	 * - One parameter : an IntVar representing the expected number of neighbors per node*/
	K_NEIGHBORS_PER_NODE {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			throw new UnsupportedOperationException("Property not implemented yet");
		}
	},
	/** Restrict the number of successor of each node in the final graph
	 * - One parameter : an IntVar representing the expected number of successors per node
	 * BEWARE the graph must be directed*/
	K_SUCCESSORS_PER_NODE {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(!cons.getRelation().isDirected()){
				throw new UnsupportedOperationException("cannot have "+this.name()+" on an undirected graph");
			}
			throw new UnsupportedOperationException("Property not implemented yet");
		}
	},
	/** Each node has exactly one successor in the final graph
	 * BEWARE the graph must be directed*/
	ONE_SUCCESSORS_PER_NODE {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			return new GraphPropagator[]{new PropNSuccs((DirectedGraphVar) cons.getGraph(), cons.getSolver(), cons, 1)};
		}
	},
//	/** Restrict the number of predecessors of each node in the final graph
//	 * - One parameter : an IntVar representing the expected number of predecessors per node
//	 * BEWARE the graph must be directed*/
//	K_PREDECESSORS_PER_NODE {
//		@Override
//		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
//			if(!cons.getRelation().isDirected()){
//				throw new UnsupportedOperationException("cannot have "+this.name()+" on an undirected graph");
//			}
//			if(parameters.length==0){
//				throw new UnsupportedOperationException("K_PREDECESSORS_PER_NODE requires a parameter (1 <= K <= n)");
//			}
//			IntVar k = parameters[0];
//			if(k.getLB()>cons.graph.getEnvelopGraph().getNbNodes() || k.getUB()< 1){
//				throw new UnsupportedOperationException("K_PREDECESSORS_PER_NODE requires a parameter (1 <= K <= n)");
//			}
//			return new GraphPropagator[]{new PropNPreds((DirectedGraphVar)cons.graph, cons.getSolver(), cons,k)};
//		}
//	},
	/** Restrict the number of proper predecessors of each node in the final graph (i.e. the number of predecessors without counting loops)
	 * - One parameter : an IntVar representing the expected number of proper predecessors per node
	 * BEWARE the graph must be directed*/
	K_PROPER_PREDECESSORS_PER_NODE {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(!cons.getRelation().isDirected()){
				throw new UnsupportedOperationException("cannot have "+this.name()+" on an undirected graph");
			}
			if(parameters.length==0){
				throw new UnsupportedOperationException("K_PROPER_PREDECESSORS_PER_NODE requires a parameter (0 <= K <= n)");
			}
			IntVar k = parameters[0];
			if(k.getLB()>cons.graph.getEnvelopGraph().getNbNodes() || k.getUB()< 0){
				throw new UnsupportedOperationException("K_PROPER_PREDECESSORS_PER_NODE requires a parameter (0 <= K <= n)");
			}
			return new GraphPropagator[]{new PropNProperPreds((DirectedGraphVar)cons.graph, cons.getSolver(), cons,k)};
		}
	},
	/** Restrict the number of connected components in the final graph
	 * - One parameter : an IntVar representing the expected number of connected components*/
	K_CC {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(parameters.length==0){
				throw new UnsupportedOperationException("K_CC requires a parameter (1 <= K <= n)");
			}
			IntVar k = parameters[0];
			if(k.getLB()>cons.graph.getEnvelopGraph().getNbNodes() || k.getUB()< 1){
				throw new UnsupportedOperationException("K_CC requires a parameter (1 <= K <= n)");
			}
			return new GraphPropagator[]{new PropKCC(cons.graph, cons.getSolver(), cons,k)};
		}
	},
	/** Restrict the number of strongly connected components in the final graph
	 * - One parameter : an IntVar representing the expected number of strongly connected components
	 * BEWARE the graph must be directed*/
	K_SCC {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(parameters.length==0){
				throw new UnsupportedOperationException("K_SCC requires a parameter (1 <= K <= n)");
			}
			IntVar k = parameters[0];
			if(k.getLB()>cons.graph.getEnvelopGraph().getNbNodes() || k.getUB()< 1){
				throw new UnsupportedOperationException("K_SCC requires a parameter (1 <= K <= n)");
			}
			throw new UnsupportedOperationException("Property not implemented yet");
		}
	},
	// Partitioning
	/** Restrict the final graph to be a clique partition
	 * - One parameter : an IntVar representing the expected number of cliques in the final graph
	 * BEWARE in such a case it is useless and thus depreciated to use a directed graph*/
	K_CLIQUES {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(parameters.length==0){
				throw new UnsupportedOperationException("K_CLIQUES requires a parameter K : (1 <= K <= n)");
			}
			IntVar k = parameters[0];
			if(k.getLB()>cons.graph.getEnvelopGraph().getNbNodes() || k.getUB()< 1){
				throw new UnsupportedOperationException("K_CLIQUES requires a parameter K : (1 <= K <= n)");
			}
			return new GraphPropagator[]{new PropKCC(cons.graph, cons.getSolver(), cons,k),new PropKCliques(cons.graph, cons.getSolver(), cons,k)};
		}
	},
	/** Restrict the final graph to be an anti arborescence partition of the input graph of cardinality k
	 * - One parameter : an IntVar representing the expected number of trees (anti-arborescence) in the final graph
	 * BEWARE the graph must be directed*/
	K_ANTI_ARBORESCENCES {
		@Override
		protected GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(parameters.length==0){
				throw new UnsupportedOperationException("K_ANTI_ARBORESCENCES requires a parameter K : (1 <= K <= n)");
			}
			IntVar k = parameters[0];
			if(k.getLB()>cons.graph.getEnvelopGraph().getNbNodes() || k.getUB()< 1){
				throw new UnsupportedOperationException("K_ANTI_ARBORESCENCES requires a parameter K : (1 <= K <= n)");
			}
			DirectedGraphVar graph = (DirectedGraphVar) cons.graph;
			Solver solver = cons.getSolver();
			return new GraphPropagator[]{
//					new PropNSuccs(graph, solver, cons,1),
					new PropOneSuccBut(graph, -1, cons, solver),
					new PropNLoopsTree(graph, k, solver, cons),
					new PropNTree(graph, k,solver,cons)
					};
		}
	};

	protected abstract GraphPropagator[] getPropagators(GraphConstraint cons, IntVar... parameters);
}
