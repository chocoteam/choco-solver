package samples.cumulative;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Scanner;


public class Parser {

	private String directory;
	private FilenameFilter filter;
	private String[] filesInDir;
	
	private int type;
	
	public static final int PERS_CUMULATIVE = 0;
	public static final int PERS_BINPACKING = 1;
	public static final int ASCHOLL = 2;
    public static final int PSP_LIB = 3;
    public static final int PERS_CUMULATIVEKDIM = 4;
	
	private int nextInstance;

	/*
	public Parser(String dir, int _type) {
		directory = dir;
		File f = new File(dir);
		filter = new DefaultFilter();
		filesInDir = f.list(filter);
		nextInstance = 0;
		this.type = _type;
	}
	*/

	public Parser(String dir, FilenameFilter ff, int _type) {
		directory = dir;
		File f = new File(dir);
		filter = ff;
		filesInDir = f.list(filter);
		nextInstance = 0;
		this.type = _type;
	}
	
	public Instance nextInstance() {
		switch (type) {
			case PERS_CUMULATIVE : return nextCumulativeInstance();
			case PERS_BINPACKING : return nextBinPackingInstance();
			case ASCHOLL : return nextSchollInstance();
			default : return null;
		}
	}

    public InstanceKDim nextInstanceKDim() {
        switch (type) {
            case PERS_CUMULATIVEKDIM : return nextCumulativeInstanceKDim();
            case PSP_LIB : return nextPSPLIBInstance();
		    default : return null;
		}
    }

    private InstanceKDim nextCumulativeInstanceKDim() {
        if (nextInstance == -1) return null;
        InstanceKDim inst = null;
        try {
			String fname = directory+"/"+filesInDir[nextInstance];
			System.out.println("FILE - "+fname);
			File f = new File(fname);
			Scanner scan = new Scanner(f);
			int nbTasks = scan.nextInt();
            int nbResources = scan.nextInt();
			int makespan = scan.nextInt();
			inst = new InstanceKDim(nbTasks,nbResources,makespan);
            for (int k=0;k<nbResources;k++) inst.capacity[k] = scan.nextInt();
			for (int t=0;t<inst.n;t++) {
				inst.d[t] = scan.nextInt();
				inst.ls[t] = scan.nextInt();
				inst.ue[t] = scan.nextInt();
				for (int k=0;k<nbResources;k++) inst.h[t][k] = scan.nextInt();
				scan.nextInt(); // weight ... = 0
				inst.le[t] = inst.ls[t] + inst.d[t]; // min(end) = min(start)+duration
				inst.us[t] = inst.ue[t] - inst.d[t];
			}
			if (nextInstance == (filesInDir.length-1)) {
				nextInstance = -1;
			} else {
				nextInstance++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return inst;
    }

    private InstanceKDim nextPSPLIBInstance() {
        if (nextInstance == -1) return null;
        InstanceKDim inst = null;

        try {
			String fname = directory+"/"+filesInDir[nextInstance];
			System.out.println("FILE - "+fname);
			File f = new File(fname);
			Scanner scan = new Scanner(f);
            scan.useDelimiter("\n"); //scans line by line
			int nbTasks;
            int nbResources;
			int makespan;
			int[] capacity;
            String line = null;
            String[] tokens = null;
            String[] tokens2 = null;
            int value;

            while (scan.hasNext() && !(line = scan.next()).startsWith("jobs") );
            tokens = line.split(":");
            nbTasks = Integer.valueOf(tokens[1].trim()) - 2; // remove supersource + sink

            while (scan.hasNext() && !(line = scan.next()).startsWith("horizon") );
            tokens = line.split(":");
            makespan = Integer.valueOf(tokens[1].trim());

            while (scan.hasNext() && !(line = scan.next()).contains("- renewable") );
            tokens = line.split(":");
            tokens2 = tokens[1].split(" ");
            nbResources = Integer.parseInt(tokens2[2]);

			inst = new InstanceKDim(nbTasks,nbResources,makespan);

            while (scan.hasNext() && !(line = scan.next()).startsWith("REQUESTS/DURATIONS") );
            scan.next();
            scan.next();
            scan.next();
            line = scan.next();
            assert(line.startsWith("  2"));
			for (int t=0;t<inst.n;t++) {
                Scanner parseLine = new Scanner(line);
                parseLine.nextInt(); // jobnr
                parseLine.nextInt(); // mode
                inst.d[t] = parseLine.nextInt(); // duration
                inst.ls[t] = 0;
                inst.us[t] = makespan - inst.d[t];
                inst.le[t] = inst.d[t];
                inst.ue[t] = makespan;
                for (int k=0;k<inst.k;k++) {
                    inst.h[t][k] = parseLine.nextInt();
                }
                line = scan.next();
            }

            while (scan.hasNext() && !(line = scan.next()).startsWith("RESOURCEAVAILABILITIES") );
            scan.next();
            line = scan.next();
            Scanner parseLine = new Scanner(line);
            for (int k=0;k<inst.k;k++) {
                inst.capacity[k] = parseLine.nextInt();
            }


			if (nextInstance == (filesInDir.length-1)) {
				nextInstance = -1;
			} else {
				nextInstance++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

        return inst;
    }



	private Instance nextCumulativeInstance() {
		// no more instance to read
		if (nextInstance == -1) return null;
		Instance inst = null;
		try {
			String fname = directory+"/"+filesInDir[nextInstance];
			System.out.println("FILE - "+fname);
			File f = new File(fname);
			Scanner scan = new Scanner(f);
			int nbTasks = scan.nextInt();
			int makespan = scan.nextInt();
			int capacity = scan.nextInt();
			inst = new Instance(nbTasks,makespan,capacity);
			for (int t=0;t<inst.n;t++) {
				inst.d[t] = scan.nextInt();
				inst.ls[t] = scan.nextInt();
				inst.ue[t] = scan.nextInt();
				inst.h[t] = scan.nextInt();
				scan.nextInt(); // weight
				inst.le[t] = inst.ls[t] + inst.d[t]; // min(end) = min(start)+duration
				inst.us[t] = inst.ue[t] - inst.d[t];
			}
			if (nextInstance == (filesInDir.length-1)) {
				nextInstance = -1;
			} else {
				nextInstance++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return inst;
	}
	
	private Instance nextBinPackingInstance() {
		if (nextInstance == -1) return null;
		Instance inst = null;
		try {
			String fname = directory+"/"+filesInDir[nextInstance];
			System.out.println("FILE - "+fname);
			File f = new File(fname);
			Scanner scan = new Scanner(f);
			int capacity = scan.nextInt();
			int nbTasks = scan.nextInt();
			
			int makespan = scan.nextInt();// TODO 
			
			//System.out.println("\tcapacity="+capacity+", nbItems="+nbTasks+", makespan="+makespan);
			inst = new Instance(nbTasks,makespan,capacity);
			for (int t=0;t<inst.n;t++) {
				inst.d[t] = 1;
				inst.ls[t] = 0;
				inst.ue[t] = inst.makespan;
				inst.h[t] = scan.nextInt();
				inst.le[t] = 1;
				inst.us[t] = inst.makespan-1;
			}
			if (nextInstance == (filesInDir.length-1)) {
				nextInstance = -1;
			} else {
				nextInstance++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return inst;
	}
	
	private Instance nextSchollInstance() {
		if (nextInstance == -1) return null;
		Instance inst = null;
		try {
			String fname = directory+"/"+filesInDir[nextInstance];
			System.out.println("FILE - "+fname);
			File f = new File(fname);
			Scanner scan = new Scanner(f);
			int nbTasks = scan.nextInt();
			int capacity = scan.nextInt();
			int makespan = 100; // TODO 
			
			//System.out.println("\tcapacity="+capacity+", nbItems="+nbTasks+", makespan="+makespan);
			inst = new Instance(nbTasks,makespan,capacity);
			for (int t=0;t<inst.n;t++) {
				inst.d[t] = 1;
				inst.ls[t] = 0;
				inst.ue[t] = inst.makespan;
				inst.h[t] = scan.nextInt();
				inst.le[t] = 1;
				inst.us[t] = inst.makespan-1;
			}
			if (nextInstance == (filesInDir.length-1)) {
				nextInstance = -1;
			} else {
				nextInstance++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return inst;
	}
	
	public String filesList() {
		String res = "";
		for(int i=0;i<filesInDir.length;i++) {
			res += "["+filesInDir[i]+"] ";
		}
		return res;
	}
	
	
	
	
	public static class DefaultFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			if (name.startsWith("csched") && name.endsWith(".dat")) return true;
			else return false;
		}
	}
}
