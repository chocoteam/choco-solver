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
				if(line.contains("constraint bool2int")){
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
