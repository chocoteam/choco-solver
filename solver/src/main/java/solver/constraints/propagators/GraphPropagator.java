package solver.constraints.propagators;

import solver.constraints.Constraint;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import solver.requests.GraphRequest;
import solver.requests.IRequest;
import solver.requests.PropRequest;
import choco.kernel.memory.IEnvironment;

public abstract class GraphPropagator<V extends Variable> extends Propagator<V>{

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	protected GraphPropagator(V[] vars, IEnvironment environment,
			Constraint<V, Propagator<V>> constraint,
			PropagatorPriority priority, boolean reactOnPromotion) {
		super(vars, environment, constraint, priority, reactOnPromotion);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@SuppressWarnings({"unchecked"})
	@Override
	protected void linkToVariables() {
		requests = new IRequest[vars.length];
		for (int i = 0; i < vars.length; i++) {
			vars[i].addObserver(this);
			if (vars[i] instanceof GraphVar) {
				requests[i] = new GraphRequest(this, (GraphVar) vars[i], i);
			}else{
				requests[i] = new PropRequest<V, Propagator<V>>(this, vars[i], i);
			}
			vars[i].addRequest(requests[i]);
		}
	}
}
