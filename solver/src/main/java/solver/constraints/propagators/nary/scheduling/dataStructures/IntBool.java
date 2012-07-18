package solver.constraints.propagators.nary.scheduling.dataStructures;

public class IntBool {
	
	public Integer date;
	public boolean pruning;
	
	public IntBool() {
		
	}
	
	public IntBool(Integer date, boolean pruning) {
		this.date = date;
		this.pruning = pruning;
	}
}
