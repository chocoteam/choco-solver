package samples.cumulative;

public class MyGenerator {

	
	private int[] ls; // min starts
	private int[] us; // max starts
	private int[] d; // durations
	private int[] le; // min ends
	private int[] ue; // max ends
	private int[] h; // heights
	
	
	private int nbTasks;
	private double density;
	private int capacity;
	private long makespan;
	private int curNbTasks;
	
	private double curDensity; // densite
	private double curEnergy; // surface totale occupee
	private double totalEnergy; // makespan * capacity
	
	
	private int minHeight = 1;
	private int maxHeight = 5;
	private int minDuration = 2;
	private int maxDuration = 15;
	private int cpPercentage = 5; // % to have a cp 10
	private double avgEnergy = ((maxDuration+minDuration)/2)*((minHeight+maxHeight)/2);
	/** */
	
	public MyGenerator(int _nbTasks, double _density, int _capacity) {
		nbTasks = _nbTasks;
		density = _density;
		capacity = _capacity;
		curNbTasks = 0;
		curDensity = 0;
		ls = new int[nbTasks];
		us = new int[nbTasks];
		d = new int[nbTasks];
		le = new int[nbTasks];
		ue = new int[nbTasks];
		h = new int[nbTasks];
	}
	
	public void init(int _minHeight, int _maxHeight, int _minDuration, int _maxDuration) {
		minDuration = _minDuration;
		maxDuration = _maxDuration;
		minHeight = _minHeight;
		maxHeight = _maxHeight;
	}
	
	public void setCpPercentage(int p) {
		this.cpPercentage = p;
	}
	
	public void preprocessing() {
		avgEnergy = ((maxDuration+minDuration)/2)*((minHeight+maxHeight)/2);
		totalEnergy = (long) ((avgEnergy * nbTasks) / density);
		makespan = (long) (totalEnergy / capacity);
		//System.out.println("Energie moyenne par tache = "+avgEnergy);
		//System.out.println("Total energie             = "+totalEnergy);
		//System.out.println("Makespan                  = "+makespan);
	}
	
	
	public int[] generateTask(int cp) { // cp pourcentage d'avoir une cp.
		int[] task = new int[6]; // ls, us, d, le, ue, h
		int _d, _h, _energy;
		_d = randomInt(minDuration, maxDuration);
		_h = randomInt(minHeight, maxHeight);
		_energy = _d * _h;
		return task;
	}
	
	/**
	 * Generate cumulative instance
	 */
	public void generateCumulative() {
		this.preprocessing();
		int _ls, _us, _d=-1, _h=-1;
		double tEnergy, futurEnergy, avgFuturEnergy;
		while (curNbTasks < nbTasks) {
			avgFuturEnergy = (curNbTasks+1)*avgEnergy;
			boolean isOk = false;
			while (!isOk) { 
				_d = randomInt(minDuration, maxDuration);
				_h = randomInt(minHeight, maxHeight);
				tEnergy = _d * _h;
				futurEnergy = curEnergy + tEnergy;
				if (futurEnergy > avgFuturEnergy*(1.02)) {
					
				} else if (futurEnergy < avgFuturEnergy*(0.98)) {
					
				} else {
					isOk = true;
				}
			}
			_us = randomInt(0, (int) (makespan-_d));
			if (randomInt(1, 100) < cpPercentage) { // ~% to have a compulsory part
				if (_us-_d+1 > 0) _ls = randomInt(_us-_d+1, _us); else _ls = randomInt(0, _us);
			} else {
				_ls = randomInt(0,Math.max(_us-_d+1,0));
			}
			addTask(_ls, _us, _d, _h);
		}
	}
	/*
	public void generateCumulativeFromSquare() {
		int area = (nbTasks*(nbTasks+1)*(2*nbTasks+1))/6;
		int maxHnM = (int) (Math.sqrt(area) * 1.15);
		this.capacity = maxHnM;
		for(int t=0;t<nbTasks;t++) {
			this.h[t] = t+1;
			this.ls[t] = 0;
			this.le[t] = t+1;
			this.d[t] = t+1;
			this.us[t] = maxHnM - (t+1);
			this.ue[t] = maxHnM;
		}
	}
	*/
	
	/**
	 * Generate bin packing instance 
	 */
	public void generateBinPacking() {
		this.preprocessing();
		int _d=1, _h=-1;
		double tEnergy, futurEnergy, avgFuturEnergy;
		while (curNbTasks < nbTasks) {
			avgFuturEnergy = (curNbTasks+1)*avgEnergy;
			boolean isOk = false;
			while (!isOk) {
				_h = randomInt(minHeight, maxHeight);
				tEnergy = _d * _h;
				futurEnergy = curEnergy + tEnergy;
				if (futurEnergy > avgFuturEnergy*(1.2)) {
				
				} else if (futurEnergy < avgFuturEnergy*(0.8)) {
				
				} else {
					isOk = true;
				}
			}
			addTask(0, (int)(makespan-1), _d, _h);
		}
	}
	
	
	public void addTask(int _ls, int _us, int _d, int _h) {
		ls[curNbTasks] = _ls;
		us[curNbTasks] = _us;
		d[curNbTasks] = _d;
		h[curNbTasks] = _h;
		le[curNbTasks] = _ls+_d;
		ue[curNbTasks] = _us+_d;
		curEnergy += _d*_h;
		curDensity = curEnergy/makespan;
		curNbTasks++;
	}
	
	
	public static int randomInt(int lowB, int upB) {
		assert(lowB<=upB);
		int result = (int) Math.round(((upB-lowB) * Math.random() + lowB));
		return result;
	}
	
	public int[] ls() {
		return ls;
	}
	public int[] us() {
		return us;
	}
	public int[] d() {
		return d;
	}
	public int[] le() {
		return le;
	}
	public int[] ue() {
		return ue;
	}
	public int[] h() {
		return h;
	}
	
	public Instance getInstance() {
		Instance inst = new Instance(capacity, ls, us, d, le, ue, h);
		return inst;	
	}
	
}
