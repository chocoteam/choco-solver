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

package solver.constraints.nary.cumulative;

import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.objects.setDataStructures.ISet;

import java.util.Arrays;

/**
 * Time-based filtering (compute the profile over every point in time)
 * @author Jean-Guillaume Fages
 */
public class TimeCumulFilter extends CumulFilter {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected int[] time = new int[31];

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public TimeCumulFilter(int nbMaxTasks, Propagator cause) {
		super(nbMaxTasks, cause);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks) throws ContradictionException {
		int min = Integer.MAX_VALUE / 2;
		int max = Integer.MIN_VALUE / 2;
		for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
			if (s[i].getUB() < e[i].getLB()) {
				min = Math.min(min, s[i].getUB());
				max = Math.max(max, e[i].getLB());
			}
		}
		if (min < max) {
			if(max-min>time.length){
				time = new int[max-min];
			}
			else{
				Arrays.fill(time, 0, max - min, 0);
			}
			int capaMax = capa.getUB();
			// fill mandatory parts and filter capacity
			int elb,hlb;
			int maxC=0;
			for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
				elb = e[i].getLB();
				hlb = h[i].getLB();
				for (int t = s[i].getUB(); t < elb; t++) {
					time[t - min] += hlb;
					maxC = Math.max(maxC,time[t - min]);
				}
			}
			capa.updateLowerBound(maxC, aCause);
			// filter max height
			int minH;
			for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
				if(!h[i].instantiated()){
					minH = h[i].getUB();
					elb = e[i].getLB();
					hlb = h[i].getLB();
					for (int t = s[i].getUB(); t < elb; t++) {
						minH = Math.min(minH,capaMax-(time[t-min]-hlb));
					}
					h[i].updateUpperBound(minH,aCause);
				}
			}
			for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
				if (d[i].getLB() > 0 && h[i].getLB() > 0) {
					// filters
					if (s[i].getLB() + d[i].getLB() > min) {
						filterInf(s[i],d[i].getLB(),h[i].getLB(), min, max, time, capaMax);
					}
					if (e[i].getUB() - d[i].getLB() < max) {
						filterSup(e[i],d[i].getLB(),h[i].getLB(), min, max, time, capaMax);
					}
				}
			}
		}
	}

	protected void filterInf(IntVar start, int dlb, int hlb, int min, int max, int[] time, int capaMax) throws ContradictionException {
		int nbOk = 0;
		int sub = start.getUB();
		for (int t = start.getLB(); t < sub; t++) {
			if (t < min || t >= max || hlb + time[t - min] <= capaMax) {
				nbOk++;
				if (nbOk == dlb) {
					return;
				}
			} else {
				nbOk = 0;
				start.updateLowerBound(t + 1, aCause);
			}
		}
	}

	protected void filterSup(IntVar end, int dlb, int hlb, int min, int max, int[] time, int capaMax) throws ContradictionException {
		int nbOk = 0;
		int elb = end.getLB();
		for (int t = end.getUB(); t > elb; t--) {
			if (t - 1 < min || t - 1 >= max || hlb + time[t - min - 1] <= capaMax) {
				nbOk++;
				if (nbOk == dlb) {
					return;
				}
			} else {
				nbOk = 0;
				end.updateUpperBound(t - 1, aCause);
			}
		}
	}
}
