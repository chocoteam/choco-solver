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
	
	private int nextInstance;

	
	public Parser(String dir, int _type) {
		directory = dir;
		File f = new File(dir);
		filter = new DefaultFilter();
		filesInDir = f.list(filter);
		nextInstance = 0;
		this.type = _type;
	}
	
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
