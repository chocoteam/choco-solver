/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.sort.IntComparator;

/**
 * Energy based filtering (greedy)
 * @author Jean-Guillaume Fages
 */
public class NRJCumulFilter extends CumulFilter{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int[] sor_array;
	private ArraySort sorter;
	private IntComparator comparator;
	private int[] slb, dlb, eub, hlb;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public NRJCumulFilter(int n){
		super(n);
		sor_array = new int[n];
		sorter = new ArraySort(n,false,true);
		slb = new int[n];
		dlb = new int[n];
		eub = new int[n];
		hlb = new int[n];
		comparator = (i1, i2) -> {
            int coef1 = (100*dlb[i1]*hlb[i1])/(eub[i1]-slb[i1]);
            int coef2 = (100*dlb[i2]*hlb[i2])/(eub[i2]-slb[i2]);
            return coef2 - coef1;
        };
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks, Propagator<IntVar> aCause) throws ContradictionException {
		int idx = 0;
		ISetIterator tIter = tasks.iterator();
		while (tIter.hasNext()){
			int i = tIter.nextInt();
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
		double xMin = Integer.MAX_VALUE / 2d;
		double xMax = Integer.MIN_VALUE / 2d;
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
				surface += (long) dlb[i] * hlb[i]; // potential overflow
				if(xMax>xMin){
					capa.updateLowerBound((int)Math.ceil(surface/(xMax-xMin)-0.01),aCause);
				}if(surface>(xMax-xMin)*camax){
					aCause.fails(); // TODO: could be more precise, for explanation purpose
				}
			}
		}
	}
}
