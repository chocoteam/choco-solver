package solver.constraints.propagators.nary.scheduling.dataStructures;


public class Event {
	
	public final static int SCP = 0; // start of a compulsory part of a fixed task.
	public final static int ECP = 1; // end of a compulsory part of a non-fixed task.
	public final static int PR = 2; // earliest start of a task.
	public final static int CCP = 3; // Latest start of a task initially without compulsory part.
	
	public final static int FSCP = 4; // Start of a compulsory part of a fixed task.
	public final static int FECP = 5; // End of a compulsory part of a fixed task.
	
	public final static int AP = 6; // Aggregation event
	
	/*
	public int date;
	public int task;
	public int type;
	public int dec;
	
	
	public Event(int _date, int _task, int _type, int _dec) {
		this.date = _date;
		this.task = _task;
		this.type = _type;
		this.dec = _dec;
	}*/
	
	/*
	public int compareTo(Event o) {
		if (this.date < o.date) return -1;
		else if (this.date > o.date) return 1;
		else return 0;
	}*/
	
	/*
	public String toString() {
		String res = "<"+date+", "+task+", ";
		switch(type) {
			case SCP : res += "SCP"; break;
			case ECP : res += "ECP"; break;
			case PR : res += "PR"; break;
			case CCP : res += "CCP"; break;
			case FSCP : res += "FSCP"; break;
			case FECP : res += "FECP"; break;
			case AP : res += "AP"; break;
		}
		res += ", "+dec+">";
		return res;
	}
	*/
	
}
