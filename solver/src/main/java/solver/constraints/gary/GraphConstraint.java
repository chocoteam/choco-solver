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
import choco.kernel.memory.IEnvironment;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.*;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**Constraint for working on graph properties
 * @author Jean-Guillaume Fages
 */
public class GraphConstraint<V extends Variable> extends Constraint<V, Propagator<V>>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	GraphVar graph;
	IntVar[] inputVars; 	// nodes of the graph
	BoolVar[][] relations;  // arcs of the graph
	IntVar[] parameterVars; // graph properties parameters
	boolean directed;		
	LinkedList<GraphProperty> properties;


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public GraphConstraint(IntVar[] vars, BoolVar[][] rel, Solver solver, PropagatorPriority storeThreshold, boolean directed) {
		super(solver, storeThreshold);
		
		int n = vars.length;
		this.relations = rel;
		this.inputVars = vars;
		this.directed = directed;
		if(directed){
			graph = new DirectedGraphVar(solver.getEnvironment(), n, GraphType.DENSE, GraphType.SPARSE);
		}else{
			graph = new UndirectedGraphVar(solver.getEnvironment(), n, GraphType.DENSE, GraphType.SPARSE);
		}
		for(int v=0; v<vars.length; v++){
			for(int w=0; w<vars.length; w ++){
				graph.getEnvelopGraph().addEdge(v, w);
			}
		}
		properties = new LinkedList<GraphProperty>();
		setPropagators(new PropBoolGraphChanneling(graph, relations, solver.getEnvironment(), this, storeThreshold));
	}

	//***********************************************************************************
	// ADDING GRAPH PROPERTIES
	//***********************************************************************************
	
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
			this.vars = (V[])ArrayUtils.append(inputVars,parameterVars,ArrayUtils.flatten(relations)).clone();
		}
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	public IEnvironment getEnvironment(){
		return solver.getEnvironment();
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
