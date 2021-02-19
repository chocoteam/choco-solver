/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 07/06/13
 * Time: 15:30
 */

package org.chocosolver.parser.flatzinc;

import java.io.*;
import java.util.HashMap;

public class PreprocessFZN {

	public static void processB2I(String inputFile){
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
			String line = br.readLine();
			HashMap<String,String> b2i = new HashMap();
			while(line!=null){
				if(line.contains("constraint ool2int")){
					line = line.replace('(','#');
					line = line.replace(')','#');
					String cur = line.split("#")[1];
					String[] x = cur.split(", ");
					b2i.put(x[0],x[1]);
				}line = br.readLine();
			}
			br.close();
			br = new BufferedReader(new FileReader(new File(inputFile)));
			line = br.readLine();
			File outfile = new File(inputFile+"_");
			FileWriter out = new FileWriter(outfile, false);
			out.write("");
			out.close();
			out = new FileWriter(outfile, true);
			while(line!=null){
				boolean write = true;
				if(line.contains("var ")){
					String vname = line.split(": ")[1];
					vname = vname.split(" :")[0];
					if(b2i.containsValue(vname)){
						write = false;
					}
				}
				else if(line.contains("constraint")){
					if(line.contains("bool2int")){
						write = false;
					}else{
						for(String k:b2i.keySet()){
							String vname = b2i.get(k);
							while(line.contains(vname)){
								line = line.replace(vname,k);
							}
						}
					}
				}
				if(write){
					out.write(line+"\n");
				}
				line = br.readLine();
			}
			out.flush();
			out.close();
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
