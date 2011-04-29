package solver.constraints.propagators;

import solver.constraints.Constraint;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import solver.views.GraphView;
import solver.views.IView;
import solver.views.PropView;
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
		views = new IView[vars.length];
		for (int i = 0; i < vars.length; i++) {
			vars[i].addObserver(this);
			if (vars[i] instanceof GraphVar) {
				views[i] = new GraphView(this, (GraphVar) vars[i], i);
			}else{
				views[i] = new PropView<V, Propagator<V>>(this, vars[i], i);
			}
			vars[i].addView(views[i]);
		}
	}
}
