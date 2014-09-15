/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
import util.sort.ArraySort;
import util.sort.IntComparator;

/**
 * Energy based filtering (greedy)
 * @author Jean-Guillaume Fages
 */
public class NRJCumulFilter extends CumulFilter{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected int[] sor_array;
	protected ArraySort sorter;
	protected IntComparator comparator;
	protected int[] slb, dlb, eub, hlb;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public NRJCumulFilter(int n, Propagator cause){
		super(n,cause);
		sor_array = new int[n];
		sorter = new ArraySort(n,false,true);
		slb = new int[n];
		dlb = new int[n];
		eub = new int[n];
		hlb = new int[n];
		comparator = new IntComparator(){
			@Override
			public int compare(int i1, int i2) {
				int coef1 = (100*dlb[i1]*hlb[i1])/(eub[i1]-slb[i1]);
				int coef2 = (100*dlb[i2]*hlb[i2])/(eub[i2]-slb[i2]);
				return coef2 - coef1;
			}
		};
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks) throws ContradictionException {
		int idx = 0;
		for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
			if(d[i].getLB()>0){
				slb[i] = s[i].getLB();
				dlb[i] = d[i].getLB();
				eub[i] = e[i].getUB();
				hlb[i] = h[i].getLB();
				assert eub[i]>slb[i];
				sor_array[idx++] = i;
			}
		}
		sorter.sort(sor_array,idx,comparator);
		double xMin = Integer.MAX_VALUE / 2;
		double xMax = Integer.MIN_VALUE / 2;
		double surface = 0;
		double camax = capa.getUB();
		for(int k=0; k<idx; k++){
			int i = sor_array[k];
			xMax = Math.max(xMax, eub[i]);
			xMin = Math.min(xMin, slb[i]);
			if(xMax >= xMin){
				double availSurf = ((xMax-xMin)*camax-surface);
				if(dlb[i]>0)
					h[i].updateUpperBound((int)Math.floor((availSurf/(double)dlb[i])+0.01),aCause);
				if(hlb[i]>0)
					d[i].updateUpperBound((int)Math.floor((availSurf/(double)hlb[i])+0.01),aCause);
				surface += dlb[i] * hlb[i];
				if(xMax>xMin){
					capa.updateLowerBound((int)Math.ceil(surface/(xMax-xMin)-0.01),aCause);
				}if(surface>(xMax-xMin)*camax){
					aCause.contradiction(capa,"");
				}
			}
		}
	}
}
