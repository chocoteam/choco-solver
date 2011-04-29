package benchmarks.graph;

/**Tricky class, used to get the value of an integer after modification inside a void method
 * @author info */
public class IntObj {

	//****************************************************************************************
	// VARIABLES 
	//****************************************************************************************

	private int val;
	
	//****************************************************************************************
	// CONSTRUCTOR 
	//****************************************************************************************

	public IntObj(int v){
		val = v;
	}

	//****************************************************************************************
	// ACCESSORS 
	//****************************************************************************************

	public int getVal() {
		return val;
	}
	public void setVal(int val) {
		this.val = val;
	}
	public void incr() {
		this.val++;
	}
}
