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

import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.gary.PropKCC;
import solver.constraints.propagators.gary.PropKLoops;
import solver.constraints.propagators.gary.PropKNodes;
import solver.constraints.propagators.gary.PropEachNodeHasLoop;
import solver.variables.IntVar;

public enum GraphProperty {

	// Trivial cases
	/** Each node in the solution must have a loop
	 * - No parameter */
	EACH_NODE_HAS_A_LOOP {
		@Override
		protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			return new Propagator[]{new PropEachNodeHasLoop(cons.graph, cons.getEnvironment(), cons)};
		}
	},
	// With parameter
	/** Restrict the number of nodes in the final graph
	 * - One parameter : an IntVar representing the expected number of nodes*/
	K_NODES {
		@Override
		protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(parameters.length==0){
				throw new UnsupportedOperationException("K_NODES requires a parameter (K)");
			}
			IntVar k = parameters[0];
			if(k.getLB()>cons.graph.getEnvelopGraph().getNbNodes()){
				throw new UnsupportedOperationException("K must be <= number of nodes");
			}
			return new Propagator[]{new PropKNodes(cons.graph, cons.getEnvironment(), cons,k)};
		}
	},
	/** Restrict the number of loops in the final graph
	 * - One parameter : an IntVar representing the expected number of nodes*/
	K_LOOPS {
		@Override
		protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(parameters.length==0){
				throw new UnsupportedOperationException("K_LOOPS requires a parameter (K)");
			}
			IntVar k = parameters[0];
			if(k.getLB()>cons.graph.getEnvelopGraph().getNbNodes()){
				throw new UnsupportedOperationException("K must be <= number of nodes");
			}
			return new Propagator[]{new PropKLoops(cons.graph, cons.getEnvironment(), cons,k)};
		}
	},
	/** Restrict the number of neighbors of each node in the final graph
	 * - One parameter : an IntVar representing the expected number of neighbors per node*/
	K_NEIGHBORS {
		@Override
		protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(parameters.length==0){
				throw new UnsupportedOperationException("K_NEIGHBORS requires a parameter (K)");
			}
			IntVar k = parameters[0];
			if(k.getLB()>=cons.graph.getEnvelopGraph().getNbNodes()){
				throw new UnsupportedOperationException("K must be < number of nodes, if equality expected, use ALL_NODES instead");
			}
			return new Propagator[]{new PropKNodes(cons.graph, cons.getEnvironment(), cons,k)};
		}
	},
	/** Restrict the number of successor of each node in the final graph
	 * - One parameter : an IntVar representing the expected number of successors per node
	 * BEWARE the graph must be directed*/
	K_SUCCESSORS {
		@Override
		protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(!cons.directed){
				throw new UnsupportedOperationException("cannot have "+this.name()+" on an undirected graph");
			}
			throw new UnsupportedOperationException("Property not implemented yet");
		}
	},
	/** Restrict the number of predecessors of each node in the final graph
	 * - One parameter : an IntVar representing the expected number of predecessors per node
	 * BEWARE the graph must be directed*/
	K_PREDECESSORS {
		@Override
		protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(!cons.directed){
				throw new UnsupportedOperationException("cannot have "+this.name()+" on an undirected graph");
			}
			throw new UnsupportedOperationException("Property not implemented yet");
		}
	},
	/** Restrict the number of connected components in the final graph
	 * - One parameter : an IntVar representing the expected number of connected components*/
	K_CC {
		@Override
		protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(parameters.length==0){
				throw new UnsupportedOperationException("K_CC requires a parameter (K)");
			}
			IntVar k = parameters[0];
			return new Propagator[]{new PropKCC(cons.graph, cons.getEnvironment(), cons,k)};
		}
	},
	/** Restrict the number of strongly connected components in the final graph
	 * - One parameter : an IntVar representing the expected number of strongly connected components
	 * BEWARE the graph must be directed*/
	K_SCC {
		@Override
		protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			if(!cons.directed){
				throw new UnsupportedOperationException("cannot have "+this.name()+" on an undirected graph");
			}
			throw new UnsupportedOperationException("Property not implemented yet");
		}
	},
	// Partitioning
	/** Restrict the final graph to be a clique partition
	 * - One parameter : an IntVar representing the expected number of cliques in the final graph
	 * BEWARE in such a case it is useless and thus depreciated to use a directed graph*/
	CLIQUE_PARTITIONNING {
		@Override
		protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			throw new UnsupportedOperationException("Property not implemented yet");
		}
	},
	/** Restrict the final graph to be a tree partition
	 * - One parameter : an IntVar representing the expected number of trees in the final graph
	 * BEWARE the graph must be undirected*/
	TREE_PARTITIONNING {
		@Override
		protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			throw new UnsupportedOperationException("Property not implemented yet");
		}
	},
	/** Restrict the final graph to be an anti-arborescence partition
	 * - One parameter : an IntVar representing the expected number of anti-arborescences in the final graph
	 * BEWARE the graph must be directed*/
	ANTI_ARBORESCENCE_PARTITIONING {
		@Override
		protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
			throw new UnsupportedOperationException("Property not implemented yet");
		}
	};

	protected abstract Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters);
}
