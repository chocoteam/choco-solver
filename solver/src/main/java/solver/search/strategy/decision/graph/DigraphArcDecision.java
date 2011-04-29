package solver.search.strategy.decision.graph;

import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.search.strategy.assignments.Assignment;
import solver.search.strategy.decision.AbstractDecision;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.graph.GraphVar;
import solver.variables.graph.directedGraph.DirectedGraphVar;

public class DigraphArcDecision extends AbstractDecision<DirectedGraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	int branch;
	Assignment<GraphVar> assignment;
	int fromTo;
	DirectedGraphVar g;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************
	
    public DigraphArcDecision(DirectedGraphVar variable, int fromTo, Assignment<GraphVar> graph_ass) {
		g = variable;
		this.fromTo = fromTo;
		assignment = graph_ass;
		branch = 0;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
    public boolean hasNext() {
        return branch < 2;
    }

    @Override
    public void buildNext() {
        branch++;
    }

	@Override
	public void apply() throws ContradictionException {
		 if (branch == 1) {
			 assignment.apply(g, fromTo, this);
	     } else if (branch == 2) {
	    	 assignment.unapply(g, fromTo, this);
	     }
	}

	@Override
	public void free() {
		// TODO
	}

	@Override
	public Explanation explain(IntVar v, Deduction d) {
		return null;
	}

	@Override
	public boolean reactOnPromotion() {
		return false;
	}

	@Override
	public int getPropagationConditions() {
		return EventType.VOID.mask;
	}

	@Override
	@Deprecated
	public void set(DirectedGraphVar var, int value, Assignment<DirectedGraphVar> assignment) {
		throw new UnsupportedOperationException();		
	}
}
