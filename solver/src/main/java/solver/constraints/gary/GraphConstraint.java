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

import java.util.LinkedList;
import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.*;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**Constraint for working on graph properties
 * @author Jean-Guillaume Fages
 */
public class GraphConstraint<V extends Variable> extends Constraint<V, Propagator<V>>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	GraphVar graph;
	IntVar[] inputVars;
	IntVar[] parameterVars;
	boolean directed;
	LinkedList<GraphProperty> properties;


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public GraphConstraint(IntVar[] vars, Solver solver, PropagatorPriority storeThreshold, boolean directed) {
		super((V[]) vars, solver, storeThreshold);
		int n = vars.length;
		this.directed = directed;
		if(directed){
			graph = new UndirectedGraphVar(solver.getEnvironment(), n, GraphType.DENSE, GraphType.SPARSE);
		}else{
			graph = new UndirectedGraphVar(solver.getEnvironment(), n, GraphType.DENSE, GraphType.SPARSE);
		}
		for(int v=0; v<vars.length; v++){
			for(int w=0; w<vars.length; w ++){
				graph.getEnvelopGraph().addEdge(v, w);
			}
		}
		properties = new LinkedList<GraphProperty>();
	}

	//***********************************************************************************
	// GRAPH PROPERTIES
	//***********************************************************************************
	
	public enum GraphProperty{
		// Trivial cases
		/** All nodes will figure in the final graph
		 * BEWARE By definition, each node must have at least one arc in the final graph
		 * - No parameter */
		ALL_NODES {
			@Override
			protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
				return new Propagator[]{new PropAllNodes(cons.graph, cons.solver.getEnvironment(), cons)};
			}
		},
		/** All loops will be removed from the envelope graph
		 * - No parameter */
		NO_LOOPS {
			@Override
			protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
				return new Propagator[]{new PropNoLoop(cons.graph, cons.solver.getEnvironment(), cons)};
			}
		},
		// With parameter
		/** Restrict the number of nodes in the final graph
		 * - One parameter : an IntVar representing the expected number of nodes*/
		K_NODES {
			@Override
			protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
				if(parameters.length==0){
					throw new UnsupportedOperationException("K_NODES require a parameter (K)");
				}
				IntVar k = parameters[0];
				if(k.getLB()>=cons.graph.getEnvelopOrder()){
					throw new UnsupportedOperationException("K must be < number of variables, if equality expected, use ALL_NODES instead");
				}
				return new Propagator[]{new PropKNodes(cons.graph, cons.solver.getEnvironment(), cons,k)};
			}
		},
		/** Restrict the number of neighbors of each node in the final graph
		 * - One parameter : an IntVar representing the expected number of neighbors per node*/
		K_NEIGHBORS {
			@Override
			protected Propagator[] getPropagators(GraphConstraint cons, IntVar... parameters) {
				throw new UnsupportedOperationException("Property not implemented yet");
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
				throw new UnsupportedOperationException("Property not implemented yet");
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
	
	/**Add a graph property to the constraint
	 * @param prop property required on the graph
	 * @param parameters eventually required parameters (see the description of the graph property)
	 */
	public void addProperty(GraphProperty prop, IntVar... parameters){
		if(properties.contains(prop)){
			throw new UnsupportedOperationException("GraphProperty "+prop+" already loaded");
		}else{
			properties.add(prop);
			setPropagators(ArrayUtils.append(propagators, prop.getPropagators(this, parameters)));
		}
	}

	//***********************************************************************************
	// CONSTRAINT METHODS
	//***********************************************************************************

	@Override
	public ESat isSatisfied() {
		return isEntailed();
	}

	@Override
	public HeuristicVal getIterator(String name, V var) {
		throw new UnsupportedOperationException("NTree does not provide such a service");
	}
}
