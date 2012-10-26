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
 * Time: 11:26
 */

package samples.graph.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Parses Sequential Ordering Problem instances
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class SOP_Utils {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	public String instanceName;
	public int[][] distanceMatrix;
	public int n; // number of nodes
	public int noVal; // default value indicating the absence of arc
	public int optimum, initialUB;

	//***********************************************************************************
	// TSPLIB Instances
	//***********************************************************************************

	public void loadTSPLIBInstance(String url) {
		File file = new File(url);
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			instanceName = line.split(":")[1].replaceAll(" ", "");
			System.out.println("parsing instance " + instanceName + "...");
			line = buf.readLine();
			line = buf.readLine();
			line = buf.readLine();
			n = Integer.parseInt(line.split(":")[1].replaceAll(" ", ""));
			distanceMatrix = new int[n][n];
			line = buf.readLine();
			line = buf.readLine();
			line = buf.readLine();
			line = buf.readLine();
			String[] lineNumbers;
			for (int i = 0; i < n; i++) {
				int nbSuccs = 0;
				while (nbSuccs < n) {
					line = buf.readLine();
					line = line.replaceAll(" * ", " ");
					lineNumbers = line.split(" ");
					for (int j = 1; j < lineNumbers.length; j++) {
						if (nbSuccs == n) {
							i++;
							if (i == n) break;
							nbSuccs = 0;
						}
						distanceMatrix[i][nbSuccs] = Integer.parseInt(lineNumbers[j]);
						nbSuccs++;
					}
				}
			}
			int maxVal = 0;
			noVal = -1;
			distanceMatrix[0][n-1] = -1;
			for (int i = 0; i < n; i++) {
				distanceMatrix[i][i] = -1;
				for (int j = 0; j < n; j++) {
					if (distanceMatrix[i][j] > maxVal) {
						maxVal = distanceMatrix[i][j];
					}
				}
			}
			line = buf.readLine();
			line = buf.readLine();
			initialUB = maxVal*n;
			optimum = Integer.parseInt(line.replaceAll(" ", ""));
			System.out.println(optimum);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
