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
package samples.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class VRPInstance {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int[][] distMatrix;
	private int[] openings;
	private int[] closures;
	
	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	public VRPInstance(String instanceFile){
		loadInstance(instanceFile);
	}

	//***********************************************************************************
	// IMPORT
	//***********************************************************************************

	private void loadInstance(String instanceFile){
    	File file = new File(instanceFile);
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			int n = Integer.parseInt(line);
			distMatrix = new int[n][n];
			openings = new int[n];
			closures = new int[n];
			String[] distLine;
			for(int i=0;i<n;i++){
				line = buf.readLine();
				distLine = line.split(" ");
				for(int j=0;j<n;j++){
					distMatrix[i][j] = (int)Double.parseDouble(distLine[j]);
				}
			}
			for(int i=0;i<n;i++){
				line = buf.readLine();
				line = line.replaceAll(" +", " ");
				distLine = line.split(" ");
				openings[i] = Integer.parseInt(distLine[0]);
				closures[i] = Integer.parseInt(distLine[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************
	
	public int[][] getDistMatrix() {
		return distMatrix;
	}
	public int[] getOpenings() {
		return openings;
	}
	public int[] getClosures() {
		return closures;
	}
	public int getNbCustomers(){
		return closures.length-1; // (the first one is the depot)
	}
	public int getDepotOpening() {
		return openings[0];
	}
	public int getDepotClosure() {
		return closures[0];
	}
}
