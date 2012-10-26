/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 22/10/12
 * Time: 02:28
 */

package samples.graph.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Parses Degree Constrained Minimum Spanning Tree instances
 * 
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class DCMST_Utils {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	public int[][] costs;
	public int[] dMax;
	public int n;
	public int lb,ub,optimum=-1;

	//***********************************************************************************
	// hard instances (Euclidean) T DE (Random) DR
	//***********************************************************************************

	public boolean parse_T_DE_DR(File file, int nMin, int nMax, String dirOpt, String type, String s) {
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			String[] numbers;
			n = Integer.parseInt(line);
			if(n<nMin || n>nMax){
				return false;
			}
			costs = new int[n][n];
			dMax = new int[n];
			for(int i=0;i<n;i++){
				line = buf.readLine();
				numbers = line.split(" ");
				if(Integer.parseInt(numbers[0])!=i+1){
					throw new UnsupportedOperationException();
				}
				dMax[i] = Integer.parseInt(numbers[1]);
				for(int j=0;j<n;j++){
					costs[i][j] = -1;
				}
			}
			line = buf.readLine();
			int from,to,cost;
			int min = 1000000;
			int max = 0;
			while(line!=null){
				numbers = line.split(" ");
				from = Integer.parseInt(numbers[0])-1;
				to   = Integer.parseInt(numbers[1])-1;
				cost = Integer.parseInt(numbers[2]);
				min = Math.min(min, cost);
				max = Math.max(max, cost);
				if(costs[from][to]!=-1){
					throw new UnsupportedOperationException();
				}
				costs[from][to] = costs[to][from] = cost;
				line = buf.readLine();
			}
			lb = (n-1)*min;
			ub = (n-1)*max;
			setUB(dirOpt+"/"+type,s);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		throw new UnsupportedOperationException();
	}

	private void setUB(String dir, String inst) {
		if(dir.contains("ham")){
			setHamUB(dir,inst);
			return;
		}
		File file = new File(dir+"/bounds.csv");
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			String[] numbers;
			line = buf.readLine();
			while(line!=null){
				numbers = line.split(";");
				if(n==Integer.parseInt(numbers[0])){
					if(inst.contains("0_1")){
						// nothing to do
					}else if(inst.contains("0_2")){
						line = buf.readLine();
						numbers = line.split(";");
					}else if(inst.contains("0_3")){
						line = buf.readLine();
						line = buf.readLine();
						numbers = line.split(";");
					}else if(inst.contains("0_4")){
						line = buf.readLine();
						line = buf.readLine();
						line = buf.readLine();
						numbers = line.split(";");
					}else if(inst.contains("0_5")){
						line = buf.readLine();
						line = buf.readLine();
						line = buf.readLine();
						line = buf.readLine();
						numbers = line.split(";");
					}else{
						throw new UnsupportedOperationException(inst);
					}
					optimum = Integer.parseInt(numbers[2]);
					System.out.println("optimum : "+optimum);
					return;
				}
				line = buf.readLine();
			}
			System.out.println("no bound");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void setHamUB(String dir, String inst) {
		File file = new File(dir+"/bounds.csv");
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			String[] numbers;
			line = buf.readLine();
			while(line!=null){
				numbers = line.split(";");
				if(n==Integer.parseInt(numbers[0])){
					if(inst.contains("0_0")){
						// nothing to do
					}else if(inst.contains("0_1")){
						line = buf.readLine();
						numbers = line.split(";");
					}else if(inst.contains("0_2")){
						line = buf.readLine();
						line = buf.readLine();
						numbers = line.split(";");
					}else{
						throw new UnsupportedOperationException(inst);
					}
					ub = Integer.parseInt(numbers[2]);
					System.out.println("ub : "+ub);
					return;
				}
				line = buf.readLine();
			}
			System.out.println("no bound");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	//***********************************************************************************
	// small instances
	//***********************************************************************************

	public void parseOthers(String url,String type) {
		n = Integer.parseInt(url.split(type)[1].substring(0,2));
		System.out.println("parsing instance " + url + "...");
		if(type.equals("crd")){
			parseCRD(url);
		}else{
			if(n==10){
				n = 100;
			}
			File file = new File(url);
			try {
				BufferedReader buf = new BufferedReader(new FileReader(file));
				String line;
				costs = new int[n][n];
				line = buf.readLine();
				int idxI = 1;
				int idxJ = 0;
				String[] numbers;
				while(line!=null){
					line = line.replaceAll(" * ", " ");
					numbers = line.split(" ");
					int first = 0;
					if(numbers[0].equals("")){
						first++;
					}
					for(int i=first;i<numbers.length;i++){
						costs[idxI][idxJ] = costs[idxJ][idxI] = Integer.parseInt(numbers[i]);
						idxJ++;
						if(idxJ==idxI){
							idxI++;
							idxJ = 0;
						}
					}
					line = buf.readLine();
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(min<costs[i][j]){
					min=costs[i][j];
				}
				if(max>costs[i][j]){
					max=costs[i][j];
				}
			}
		}
		lb = (n-1)*min;
		ub = (n-1)*max;
	}

	private void parseCRD(String url) {
		if(n==10){
			n = 100;
		}
		File file = new File(url);
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line;
			int[] x = new int[n];
			int[] y = new int[n];
			costs = new int[n][n];
			line = buf.readLine();
			int idx = 0;
			String[] numbers;
			while(line!=null){
				line = line.replaceAll(" * ", " ");
				numbers = line.split(" ");
				int first = 0;
				if(numbers[0].equals("")){
					first++;
				}
				for(int i=first;i<numbers.length;i+=2){
					x[idx] = Integer.parseInt(numbers[i]);
					y[idx++] = Integer.parseInt(numbers[i+1]);
				}
				line = buf.readLine();
			}
			for(int i=0;i<n;i++){
				for(int j=i+1;j<n;j++){
					int xd = x[i]-x[j];
					int yd = y[i]-y[j];
					costs[i][j] = costs[j][i] = (int) Math.sqrt((xd*xd+yd*yd));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void setMaxDegrees(int d){
		dMax = new int[n];
		for(int i=0;i<n;i++){
			dMax[i] = d;
		}
	}
}
