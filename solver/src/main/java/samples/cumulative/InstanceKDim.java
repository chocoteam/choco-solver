package samples.cumulative;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Arnaud Letort
 * Date: 27/07/12
 * Time: 10:41
 */
public class InstanceKDim {

    public int n;
    public int k;
	public int makespan;
	public int[] capacity;
	public int[] ls;
	public int[] us;
	public int[] d;
	public int[] le;
	public int[] ue;
	public int[][] h;


	public InstanceKDim(int n, int k, int makespan) {
		this.n = n;
		this.k = k;
        this.makespan = makespan;
		this.ls = new int[n];
		this.us = new int[n];
		this.d = new int[n];
		this.le = new int[n];
		this.ue = new int[n];
        this.capacity = new int[k];
		this.h = new int[n][k];
	}

	public InstanceKDim(int[] capacity, int[] ls, int[] us, int[] d, int[] le, int[] ue, int[][] h) {
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
		res += "nb tasks="+n+", nb resources="+k+", horizon="+makespan+"\n";
        res += "capacities = {";
        for (int i=0;i<k;i++) res += " "+capacity[i];
		res += " }\n";
		for(int i=0;i<n;i++) {
			res += "t"+i+" = ["+ls[i]+".."+us[i]+" + "+d[i]+" -> "+le[i]+".."+ue[i]+"] \t";
            res += "h = {";
			for (int j=0;j<k;j++) res += " "+h[i][j];
            res += " } \n";
		}
		return res;
	}

	public void backupIntoFile(String dir, String fileName) {

		try {
			File f = new File(dir+"/"+fileName);
			PrintWriter out = new PrintWriter(f);

			out.print(n+" "+k+" "+makespan+" ");
            for (int i=0;i<k;i++) out.print(capacity[i]+" ");
            out.print("\n");
			for(int i=0;i<n;i++) {
				out.print(d[i]+" "+ls[i]+" "+ue[i]+" "+h[i]+" ");
                for (int j=0;j<k;j++) out.print(h[i][j]+" ");
			}
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


}
