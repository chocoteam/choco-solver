package samples.cumulative;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.scheduling.Cumulative;
import solver.constraints.nary.scheduling.Cumulative.Type;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;



public class Builder {

	private boolean solveAll;
	private boolean printSolution;
	private boolean printChoices;
	private int timeout;
	private String branchingStrategy;

	
	public Builder(boolean solveAll, boolean printSolution, boolean printChoices, int to, String bs) {
		this.solveAll = solveAll;
		this.printSolution = printSolution;
		this.printChoices = printChoices;
		this.timeout = to;
		this.branchingStrategy = bs;
	}
	
	

	public void classic(int n, int capa, int[] ls, int[] us,
			int[] ld, int[] ud, int[] le, int[] ue, int[] h) {
        IntVar[] starts = new IntVar[n];
		IntVar[] durations = new IntVar[n];
		IntVar[] ends = new IntVar[n];
		IntVar[] heights = new IntVar[n];
		Solver solver = new Solver();
		for(int i=0;i<n;i++) {
			IntVar[] task = VariableFactory.task("t"+i, ls[i], us[i], ld[i], ld[i], le[i], ue[i], solver);
			starts[i] = task[0];
			durations[i] = task[1];
			ends[i] = task[2];
			heights[i] = VariableFactory.bounded("h"+i, h[i], h[i], solver);
		}
		Constraint c = new Cumulative(starts, durations, ends, heights, capa, solver, Type.SWEEP);
		solver.post(c);
		if (this.branchingStrategy.equals("minsize")) {
			solver.set(StrategyFactory.minDomMinVal(starts, solver.getEnvironment()));
		} else if (this.branchingStrategy.equals("minvalue")) {
			solver.set(StrategyFactory.minDomLowBound(starts, solver.getEnvironment()));
		} else {
			assert(false);
		}
		SearchMonitorFactory.log(solver, printSolution, printChoices);
		solver.getSearchLoop().getLimitsBox().setTimeLimit(timeout);
		solver.findSolution();
	}


	public void dynamic(int n, int capa, int[] ls, int[] us,
			int[] ld, int[] ud, int[] le, int[] ue, int[] h) {
		IntVar[] starts = new IntVar[n];
		IntVar[] durations = new IntVar[n];
		IntVar[] ends = new IntVar[n];
		IntVar[] heights = new IntVar[n];
		Solver solver = new Solver();
		for(int i=0;i<n;i++) {
			IntVar[] task = VariableFactory.task("t"+i, ls[i], us[i], ld[i], ld[i], le[i], ue[i], solver);
			starts[i] = task[0];
			durations[i] = task[1];
			ends[i] = task[2];
			heights[i] = VariableFactory.bounded("h"+i, h[i], h[i], solver);
		}
		Constraint c = new Cumulative(starts, durations, ends, heights, capa, solver, Type.DYNAMIC_SWEEP);
		solver.post(c);
		if (this.branchingStrategy.equals("minsize")) {
			solver.set(StrategyFactory.minDomMinVal(starts, solver.getEnvironment()));
		} else if (this.branchingStrategy.equals("minvalue")) {
			solver.set(StrategyFactory.minDomLowBound(starts, solver.getEnvironment()));
		} else {
			assert(false);
		}
		SearchMonitorFactory.log(solver, printSolution, printChoices);
		solver.getSearchLoop().getLimitsBox().setTimeLimit(timeout);
		solver.findSolution();
	}
	
	
	public void greedy(int n, int capa, int[] ls, int[] us,
			int[] ld, int[] ud, int[] le, int[] ue, int[] h) {
		IntVar[] starts = new IntVar[n];
		IntVar[] durations = new IntVar[n];
		IntVar[] ends = new IntVar[n];
		IntVar[] heights = new IntVar[n];
		Solver solver = new Solver();
		for(int i=0;i<n;i++) {
			IntVar[] task = VariableFactory.task("t"+i, ls[i], us[i], ld[i], ld[i], le[i], ue[i], solver);
			starts[i] = task[0];
			durations[i] = task[1];
			ends[i] = task[2];
			heights[i] = VariableFactory.bounded("h"+i, h[i], h[i], solver);
		}
		Constraint c = new Cumulative(starts, durations, ends, heights, capa, solver, Type.GREEDY);
		solver.post(c);
		if (this.branchingStrategy.equals("minsize")) {
			solver.set(StrategyFactory.minDomMinVal(starts, solver.getEnvironment()));
		} else if (this.branchingStrategy.equals("minvalue")) {
			solver.set(StrategyFactory.minDomLowBound(starts, solver.getEnvironment()));
		} else {
			assert(false);
		}
		SearchMonitorFactory.log(solver, printSolution, printChoices);
		solver.findSolution();
	}
	
	/*
	public boolean pack(int n, int capa, int[] ls, int[] us,
			int[] ld, int[] ud, int[] le, int[] ue, int[] h) {

	}*/
	
	public void custom(int n, int capa, int[] ls, int[] us,
			int[] ld, int[] ud, int[] le, int[] ue, int[] h, Type type) {
        IntVar[] starts = new IntVar[n];
		IntVar[] durations = new IntVar[n];
		IntVar[] ends = new IntVar[n];
		IntVar[] heights = new IntVar[n];
		Solver solver = new Solver();
		for(int i=0;i<n;i++) {
			IntVar[] task = VariableFactory.task("t"+i, ls[i], us[i], ld[i], ld[i], le[i], ue[i], solver);
			starts[i] = task[0];
			durations[i] = task[1];
			ends[i] = task[2];
			heights[i] = VariableFactory.bounded("h"+i, h[i], h[i], solver);
		}
		Constraint c = new Cumulative(starts, durations, ends, heights, capa, solver, type);
		solver.post(c);
		if (this.branchingStrategy.equals("minsize")) {
			solver.set(StrategyFactory.minDomMinVal(starts, solver.getEnvironment()));
		} else if (this.branchingStrategy.equals("minvalue")) {
			solver.set(StrategyFactory.minDomLowBound(starts, solver.getEnvironment()));
		} else {
			assert(false);
		}
		SearchMonitorFactory.log(solver, printSolution, printChoices);
		solver.getSearchLoop().getLimitsBox().setTimeLimit(timeout);
		solver.findSolution();
	}
	
	public static boolean checkSolution(int n, int capa, int[] s, int[] d, int[] e, int[] h) {
		int height;
		int minTime = Integer.MAX_VALUE;
		int maxTime = Integer.MIN_VALUE;
		for(int t=0;t<n;t++) {
			if (s[t] < minTime) { minTime = s[t]; }
			if (e[t] > maxTime) { maxTime = e[t]; }
		}
		for (int i=minTime;i<=maxTime;i++) {
			height = 0;
			for (int t=0;t<n;t++) {
				if ((i >= s[t]) && (i < e[t])) height += h[t];
			}
			if (height > capa) return false;
		}
		return true;
	}
	
}
