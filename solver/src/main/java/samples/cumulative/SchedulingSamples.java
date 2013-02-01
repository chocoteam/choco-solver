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


		int nbInstances = 1;
		int timeout = 60000;
		int nbTasks = 500;

		double density = 0.5;
		int capacity = 15;
		int minHeight = 1;
		int maxHeight = 3;
		int minDuration = 2;
		int maxDuration = 5;

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
			chk = new Builder(false,true,false,timeout,"minsize");
			a = chk.buildNSolve(pb.n, pb.capacity, pb.ls, pb.us, pb.d, pb.d, pb.le, pb.ue, pb.h);
            //b = chk.buildNSolve(pb.n, pb.capacity, pb.ls, pb.us, pb.d, pb.d, pb.le, pb.ue, pb.h, Cumulative.Type.EDGE_FINDING);
            //assert(a.getSolutionCount() == b.getSolutionCount());
			nbInstances--;
		}

		
        
	}
	
	
	public static class OneFileFilter implements FilenameFilter {

        public static String fn;

		@Override
		public boolean accept(File arg0, String arg1) {
			if (arg1.contains("3014_9") && arg1.endsWith(".sm")) return true;
            //if (arg1.contains("model_m1000_p5000_s0.1_0.txt") && arg1.endsWith("")) return true;
			//if (arg1.equals(fn)) return true;
            return false;
		}
	}
	
	
}

