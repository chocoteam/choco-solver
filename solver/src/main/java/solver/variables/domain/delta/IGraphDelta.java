package solver.variables.domain.delta;


public interface IGraphDelta extends IDelta{

	IntDelta getNodeRemovalDelta();
	
	IntDelta getNodeEnforcingDelta();
	
	IntDelta getArcRemovalDelta();
	
	IntDelta getArcEnforcingDelta();
}
