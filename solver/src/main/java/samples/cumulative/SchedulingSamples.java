package samples.cumulative;


import solver.search.measure.IMeasures;

import java.io.File;
import java.io.FilenameFilter;

public class SchedulingSamples {

	public static void main(String[] args) {
		
        /*
        IMeasures a, b;
		String dir;
		if (args.length == 0) {
			dir = "/Users/aletor11/Documents/workspace/instances_cumulative";
		} else {
			dir = args[0];
		}		
		Builder chk = new Builder(false,true,false,360000,"minsize");
		Parser p = new Parser(dir,new OneFileFilter(), Parser.PERS_CUMULATIVE);
		Instance pb = p.nextInstance();
		while ( pb != null ) {
			a = chk.buildNSolve(pb.n, pb.capacity, pb.ls, pb.us, pb.d, pb.d, pb.le, pb.ue, pb.h, Cumulative.Type.DYNAMIC_SWEEP);
            b = chk.buildNSolve(pb.n, pb.capacity, pb.ls, pb.us, pb.d, pb.d, pb.le, pb.ue, pb.h, Cumulative.Type.EDGE_FINDING);
            assert(a.getSolutionCount() == b.getSolutionCount());
			pb = p.nextInstance();
		}
        */

        IMeasures a, b;
		String dir;
		if (args.length == 0) {
			dir = "/Users/aletor11/Documents/workspace/instances_cumulative/j30";
		} else {
			dir = args[0];
		}
		Builder chk = new Builder(false,true,true,360000,"minsize");
		Parser p = new Parser(dir,new OneFileFilter(), Parser.PSP_LIB);
		InstanceKDim pb = p.nextInstanceKDim();
        while ( pb != null ) {
			a = chk.buildNSolve(pb.n, pb.k, pb.capacity, pb.ls, pb.us, pb.d, pb.d, pb.le, pb.ue, pb.h);
			pb = p.nextInstanceKDim();
		}

        /*
		int nbInstances = 100;
		int timeout = 30000;
		int nbTasks = 10;

		double density = 0.7;
		int capacity = 7;
		int minHeight = 1;
		int maxHeight = 3;
		int minDuration = 2;
		int maxDuration = 4;

		MyGenerator gen;
		Instance pb;
		String filename;
		Builder chk;

        IMeasures a, b;

		while (nbInstances > 0) {
			// generate an instance
			gen = new MyGenerator(nbTasks, density, capacity);
			gen.init(minHeight, maxHeight, minDuration, maxDuration);
			gen.generateCumulative();
			pb = gen.getInstance();
			filename = "type_n"+nbTasks+"_"+nbInstances+".dat";
			//pb.backupIntoFile("/Users/aletor11/Documents/workspace/instances_cumulative", filename); // record the instance
			System.out.println("n="+nbTasks+";density="+density+";filename="+filename);			
			chk = new Builder(true,false,false,timeout,"minsize");
			a = chk.buildNSolve(pb.n, pb.capacity, pb.ls, pb.us, pb.d, pb.d, pb.le, pb.ue, pb.h, Cumulative.Type.DYNAMIC_SWEEP);
            b = chk.buildNSolve(pb.n, pb.capacity, pb.ls, pb.us, pb.d, pb.d, pb.le, pb.ue, pb.h, Cumulative.Type.EDGE_FINDING);
            assert(a.getSolutionCount() == b.getSolutionCount());
			nbInstances--;
		}
        */

        /*
		int n = 3;
		int limit = 1;
		int[] ls = {0,0,0};
		int[] us = {0,10,10};
		int[] ld = {3,3,3};
		int[] le = {3,3,3};
		int[] ue = {3,13,13};
		int[] h = {1,1,1};
		
		IntVar[] starts = new IntVar[n];
		IntVar[] durations = new IntVar[n];
		IntVar[] ends = new IntVar[n];
		IntVar[] heights = new IntVar[n];
		Solver solver = new Solver();
		for(int i=0;i<n;i++) {
			IntVar[] task = VariableFactory.task("t" + i, ls[i], us[i], ld[i], ld[i], le[i], ue[i], solver);
			starts[i] = task[0];
			durations[i] = task[1];
			ends[i] = task[2];
			heights[i] = VariableFactory.bounded("h"+i, h[i], h[i], solver);
		}
		Constraint c = new Cumulative(starts, durations, ends, heights, limit, solver, Cumulative.Type.DYNAMIC_SWEEP);
		solver.post(c);
		solver.set(StrategyFactory.minDomMinVal(starts, solver.getEnvironment()));
		SearchMonitorFactory.log(solver, true, true);
		solver.findSolution();
        */


        /*
		int n = 3;
        int k = 3;
		int[] limit = {1,1,1};
		int[] ls = {0,0,0};
		int[] us = {0,3,6};
		int[] ld = {3,3,3};
		int[] le = {3,3,3};
		int[] ue = {3,6,9};
		int[][] h = { {1,1,1}, {1,1,1}, {1,1,1} };

		IntVar[] starts = new IntVar[n];
		IntVar[] durations = new IntVar[n];
		IntVar[] ends = new IntVar[n];
		IntVar[][] heights = new IntVar[n][k];
		Solver solver = new Solver();
		for(int i=0;i<n;i++) {
			IntVar[] task = VariableFactory.task("t" + i, ls[i], us[i], ld[i], ld[i], le[i], ue[i], solver);
			starts[i] = task[0];
			durations[i] = task[1];
			ends[i] = task[2];
            for (int r=0;r<k;r++) {
                System.out.println("i="+i+" r="+r);
			    heights[i][r] = VariableFactory.bounded("h"+i+","+r, h[i][r], h[i][r], solver);
            }
		}
		Constraint c = new CumulativeKDim(starts, durations, ends, heights, limit, solver);
		solver.post(c);
		solver.set(StrategyFactory.minDomMinVal(starts, solver.getEnvironment()));
		SearchMonitorFactory.log(solver, true, true);
		solver.findSolution();
		*/

	}
	
	
	public static class OneFileFilter implements FilenameFilter {
		@Override
		public boolean accept(File arg0, String arg1) {
			if (arg1.startsWith("j301_1") && arg1.endsWith(".sm")) return true;
			return false;
		}
	}
	
	
}
