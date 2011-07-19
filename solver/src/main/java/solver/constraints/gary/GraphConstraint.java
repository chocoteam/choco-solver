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
import solver.constraints.*;
import solver.constraints.gary.relations.GraphRelation;
import solver.constraints.propagators.*;
import solver.constraints.propagators.gary.*;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;

/**Constraint for working on graph properties
 * TODO : change properties management
 * @author Jean-Guillaume Fages
 */
public class GraphConstraint<V extends Variable> extends Constraint<V, Propagator<V>>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	GraphVar graph;
	V[] inputVars; 	// nodes of the graph
	IntVar[] parameterVars; // graph properties parameters
	GraphRelation<V> relation;
	LinkedList<GraphProperty> properties;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** Please use ConstraintFactory.makeConstraint(...) 
	 *
	 * create a new generic graph constraint
	 * @param vars (nodes)
	 * @param solver
	 * @param storeThreshold
	 * @param relation (arc meaning)
	 */
	GraphConstraint(V[] vars, Solver solver, PropagatorPriority storeThreshold, GraphRelation relation) {
		super(solver, storeThreshold);
		this.inputVars = vars;
		this.relation   = relation;
		this.graph = relation.generateInitialGraph(vars, solver);
		this.properties = new LinkedList<GraphProperty>();
		solver.associates(graph);
		Propagator pr = new PropRelation(vars, graph, solver, this, relation);
		setPropagators(new Propagator[]{pr});
		for(GraphProperty gp:relation.getGraphProperties()){
			addProperty(gp);
		}
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
			if(parameters != null && parameters.length>0){
				if(vars == null){
					this.vars = (V[])ArrayUtils.append(vars,parameters);
				}else{
					boolean notIn;
					for(IntVar var:parameters){
						notIn = true;
						for(V v:vars){
							if(v==var){
								notIn = false;
								break;
							}
						}
						if(notIn){
							this.vars = (V[])ArrayUtils.append(vars,new IntVar[]{var});
						}
					}
					//				this.vars = (V[])ArrayUtils.append(vars,parameters);
				}
			}
			if(propagators==null){
				setPropagators(prop.getPropagators(this, parameters));
			}else{
				addPropagators(prop.getPropagators(this, parameters));
			}
		}
	}

	/**Add a graph propagator to the constraint
	 * @param p
	 */
	public void addAdHocProp(GraphPropagator p){
		if(propagators==null){
			setPropagators(p);
		}else{
			addPropagators(p);
		}
//			setPropagators(ArrayUtils.append(propagators, new Propagator[]{p}));
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	public IEnvironment getEnvironment(){
		return solver.getEnvironment();
	}

	public Solver getSolver(){
		return solver;
	}

	/**get the graph representing the constraint
	 * @return graph
	 */
	public GraphVar getGraph(){
		return graph;
	}

	/**Get the meaning of an arc
	 * @return relation
	 */
	public GraphRelation<V> getRelation(){
		return relation;
	}

	//***********************************************************************************
	// CONSTRAINT METHODS
	//***********************************************************************************

	@Override
	public ESat isSatisfied() {
		if(true)throw new UnsupportedOperationException("error ");

		return isEntailed();
	}

	@Override
	public HeuristicVal getIterator(String name, V var) {
		throw new UnsupportedOperationException("GraphConstraint does not provide such a service");
	}
}
