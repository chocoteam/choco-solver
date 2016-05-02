/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.todo.input;

import gnu.trove.list.array.TIntArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Parses and generates Hamiltonian Cycle Problem instances
 * including the Knight's Tour Problem (open or closed)
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class HCP_Utils {

    //***********************************************************************************
    // KING TOUR
    //***********************************************************************************

    public static boolean[][] generateOpenKingTourInstance(int size) {
        boolean[][] m1 = generateKingTourInstance(size);
        int n = size * size;
        boolean[][] m2 = new boolean[n + 1][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(m1[i], 0, m2[i], 0, n);
        }
        for (int i = 0; i < n; i++) {
            m2[n][i] = m2[i][n] = true;
        }
        return m2;
    }

    public static boolean[][] generateKingTourInstance(int size) {
        int n = size * size;
        int node, next, a, b;
        boolean[][] matrix = new boolean[n][n];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                node = i * size + j;
                // move
                a = i + 1;
                b = j + 2;
                next = a * size + (b);
                if (next >= 0 && next < n) {
                    matrix[node][next] = inChessboard(a, b, size);
                    matrix[next][node] = matrix[node][next];
                }
                // move
                a = i + 1;
                b = j - 2;
                next = a * size + (b);
                if (next >= 0 && next < n) {
                    matrix[node][next] = inChessboard(a, b, size);
                    matrix[next][node] = matrix[node][next];
                }
                // move
                a = i + 2;
                b = j + 1;
                next = a * size + (b);
                if (next >= 0 && next < n) {
                    matrix[node][next] = inChessboard(a, b, size);
                    matrix[next][node] = matrix[node][next];
                }
                // move
                a = i + 2;
                b = j - 1;
                next = a * size + (b);
                if (next >= 0 && next < n) {
                    matrix[node][next] = inChessboard(a, b, size);
                    matrix[next][node] = matrix[node][next];
                }
            }
        }
        return matrix;
    }

	public static TIntArrayList[] generateKingTourInstance_LightMemory(int size) {
		int n = size * size;
		int node, next, a, b;
		TIntArrayList[] matrix = new TIntArrayList[n];
		for (int i = 0; i < n; i++) {
			matrix[i] = new TIntArrayList(8);
		}
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				node = i * size + j;
				// move
				a = i + 1;
				b = j + 2;
				next = a * size + (b);
				if (next >= 0 && next < n) {
					if(inChessboard(a, b, size)){
						matrix[node].add(next);
						matrix[next].add(node);
					}
				}
				// move
				a = i + 1;
				b = j - 2;
				next = a * size + (b);
				if (next >= 0 && next < n) {
					if(inChessboard(a, b, size)){
						matrix[node].add(next);
						matrix[next].add(node);
					}
				}
				// move
				a = i + 2;
				b = j + 1;
				next = a * size + (b);
				if (next >= 0 && next < n) {
					if(inChessboard(a, b, size)){
						matrix[node].add(next);
						matrix[next].add(node);
					}
				}
				// move
				a = i + 2;
				b = j - 1;
				next = a * size + (b);
				if (next >= 0 && next < n) {
					if(inChessboard(a, b, size)){
						matrix[node].add(next);
						matrix[next].add(node);
					}
				}
			}
		}
		return matrix;
	}

    private static boolean inChessboard(int a, int b, int n) {
        return !(a < 0 || a >= n || b < 0 || b >= n);
    }

    //***********************************************************************************
    // TSPLIB "/Users/jfages07/Documents/code/ALL_hcp"
    //***********************************************************************************

    public static boolean[][] parseTSPLIBInstance(String url) {
        File file = new File(url);
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line = buf.readLine();
            String name = line.split(":")[1].replaceAll(" ", "");
            System.out.println("parsing instance " + name + "...");
            buf.readLine();
            buf.readLine();
            buf.readLine();
            int n = Integer.parseInt(line.split(":")[1].replaceAll(" ", ""));
            boolean[][] matrix = new boolean[n][n];
            buf.readLine();
            buf.readLine();
            buf.readLine();
            String[] lineNumbers;
            int i, j;
            while (!line.equals("-1")) {
                line = line.replaceAll(" +", ";");
                lineNumbers = line.split(";");
                i = Integer.parseInt(lineNumbers[1]) - 1;
                j = Integer.parseInt(lineNumbers[2]) - 1;
                matrix[i][j] = true;
                matrix[j][i] = true;
                line = buf.readLine();
            }
			buf.close();
            return matrix;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new UnsupportedOperationException();
    }

    //***********************************************************************************
    // Cycle->Path
    //***********************************************************************************

    public static boolean[][] transformCycleToPath(boolean[][] m) {
        int n = m.length + 1;
        boolean[][] matrix = new boolean[n][n];
        for (int i = 0; i < n - 1; i++) {
            System.arraycopy(m[i], 1, matrix[i], 1, n - 1 - 1);
            matrix[i][n - 1] = m[i][0];
            matrix[i][0] = false;
        }
        matrix[0][n - 1] = false;
        return matrix;
    }
}
