package solver.variables.domain.delta;

public class GraphDelta implements IGraphDelta{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private IntDelta nodeEnf,nodeRem,arcEnf,arcRem;
	
	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public GraphDelta(){
		nodeEnf = new Delta();
		nodeRem = new Delta();
		arcEnf = new Delta();
		arcRem = new Delta();
	}
	
	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	@Override
	public IntDelta getNodeRemovalDelta() {
		return nodeRem;
	}

	@Override
	public IntDelta getNodeEnforcingDelta() {
		return nodeEnf;
	}

	@Override
	public IntDelta getArcRemovalDelta() {
		return arcRem;
	}

	@Override
	public IntDelta getArcEnforcingDelta() {
		return arcEnf;
	}
}
