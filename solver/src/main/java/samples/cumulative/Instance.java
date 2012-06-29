package samples.cumulative;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.StringTokenizer;


public class Instance {

	public int n;
	public int makespan;
	public int capacity;
	public int[] ls;
	public int[] us;
	public int[] d;
	public int[] le;
	public int[] ue; 
	public int[] h;
	
	
	public Instance(int n, int makespan, int capacity) {
		this.n = n;
		this.makespan = makespan;
		this.capacity = capacity;
		this.ls = new int[n];
		this.us = new int[n];
		this.d = new int[n];
		this.le = new int[n];
		this.ue = new int[n];
		this.h = new int[n];
	}
	
	public Instance(int capacity, int[] ls, int[] us, int[] d, int[] le, int[] ue, int[] h) {
		this.n = ls.length;
		this.ls = ls;
		this.us = us;
		this.d = d;
		this.le = le;
		this.ue = ue;
		this.h = h;
		this.capacity = capacity;
	}
	
	public String toString() {
		String res = "";
		res += "capacity="+capacity+", nb tasks="+n+", ----- ="+makespan;
		res += "\n";
		for(int i=0;i<n;i++) {
			res += "t"+i+":["+ls[i]+".."+us[i]+" + "+d[i]+" -> "+le[i]+".."+ue[i]+"] \t";
			res += "h="+h[i]+"\n";
		}
		return res;
	}
	
	public void backupIntoFile(String dir, String fileName) {
		
		try {
			File f = new File(dir+"/"+fileName);
			PrintWriter out = new PrintWriter(f);
			
			out.print(n+" "+makespan+" "+capacity+"\n");
			for(int i=0;i<n;i++) {
				out.print(d[i]+" "+ls[i]+" "+ue[i]+" "+h[i]+" 0\n");
			}
			out.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static String instanceToRRaw(Instance inst) {
		StringBuilder result = new StringBuilder(4*inst.n); // ~borne min
		// First line.
		result.append(inst.n+" ");
		result.append(inst.makespan+" ");
		result.append(inst.capacity+"\n");
		// Tasks.
		for (int t=0;t<inst.n;t++) {
			result.append(inst.d[t]+" ");
			result.append(inst.ls[t]+" ");
			result.append(inst.ue[t]+" ");
			result.append(inst.h[t]+" 0 \n"); // 0 -> weight
		}
		return result.toString();
	}
	
	// TODO to check
	public static Instance rawToInstance(String raw) {
		StringTokenizer st = new StringTokenizer(raw);
		int n = Integer.valueOf(st.nextToken());
		int makespan = Integer.valueOf(st.nextToken());
		int capacity = Integer.valueOf(st.nextToken());
		Instance inst = new Instance(n, makespan, capacity);
		for (int t=0;t<n;t++) {
			inst.d[t] = Integer.valueOf(st.nextToken());
			inst.ls[t] = Integer.valueOf(st.nextToken());
			inst.ue[t] = Integer.valueOf(st.nextToken());
			inst.h[t] = Integer.valueOf(st.nextToken());
			st.nextToken(); // weight
			inst.le[t] = inst.ls[t] + inst.d[t];
			inst.us[t] = inst.ue[t] - inst.d[t];
		}
		return inst;
	}
	
	public boolean check() {
		return (n == ls.length && n == us.length && n == d.length && n == le.length && n == ue.length && n == h.length);
	}
	
	
}
