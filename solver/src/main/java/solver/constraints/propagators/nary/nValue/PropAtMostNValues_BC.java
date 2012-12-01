/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.propagators.nary.nValue;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.util.BitSet;

/**
 * Propagator for the atMostNValues constraint
 * The number of distinct values in the set of variables vars is at most equal to nValues
 * Performs Bound Consistency in O(n+d) with
 * n = |vars|
 * d = maxValue - minValue (from initial domains)
 * <p/>
 * => very appropriate when d <= n It is indeed much better than the usual time complexity of O(n.log(n))
 * =>  not appropriate when d >> n (you should encode another data structure and a quick sort algorithm)
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtMostNValues_BC extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
    private int nbMaxValues;
    private int minValue;
	private int minIndex,maxIndex;
    private TIntArrayList[] bound;
	private TIntArrayList stamp;
    private int[] minVal, maxVal;
	private BitSet kerRepresentant;
	private int[] orderedNodes;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * The number of distinct values in vars is at most nValues
     * Performs Bound Consistency in O(n+d) with
     * n = |vars|
     * d = maxValue - minValue (from initial domains)
     * <p/>
     * => very appropriate when d <= n It is indeed much better than the usual time complexity of O(n.log(n))
     * =>  not appropriate when d >> n (you should encode another data structure and a quick sort algorithm)
     *
     * @param vars
     * @param nValues
     * @param constraint
     * @param solver
     */
    public PropAtMostNValues_BC(IntVar[] vars, IntVar nValues, Constraint constraint, Solver solver) {
        super(ArrayUtils.append(vars, new IntVar[]{nValues}), solver, constraint, PropagatorPriority.QUADRATIC);
        n = vars.length;
        minValue = vars[0].getLB();
        int maxValue = vars[0].getUB();
        for (int i = 1; i < n; i++) {
            minValue = Math.min(minValue, vars[i].getLB());
            maxValue = Math.max(maxValue, vars[i].getUB());
        }
        nbMaxValues = maxValue - minValue + 1;
        bound = new TIntArrayList[nbMaxValues];
        for (int i = 0; i < nbMaxValues; i++) {
            bound[i] = new TIntArrayList();
        }
        minVal = new int[n];
        maxVal = new int[n];
		stamp = new TIntArrayList();
		kerRepresentant = new BitSet(n);
		orderedNodes = new int[n];
    }

    //***********************************************************************************
    // Initialization and sort
    //***********************************************************************************

    private void computeBounds() throws ContradictionException {
		minIndex = vars[0].getLB();
		maxIndex = vars[0].getUB();
        for (int i = 0; i < n; i++) {
            minVal[i] = vars[i].getLB();
            maxVal[i] = vars[i].getUB();
			minIndex = Math.min(minIndex, minVal[i]);
			maxIndex = Math.max(maxIndex, maxVal[i]);
        }
		minIndex -= minValue;
		maxIndex -= minValue;
    }

    private void sortLB() {
        for (int i = 0; i < nbMaxValues; i++) {
            bound[i].clear();
        }
        for (int i = 0; i < n; i++) {
            bound[minVal[i] - minValue].add(i);
        }
    }

    private void sortUB() {
        for (int i = 0; i < nbMaxValues; i++) {
            bound[i].clear();
        }
        for (int i = 0; i < n; i++) {
            bound[maxVal[i] - minValue].add(i);
        }
    }

    //***********************************************************************************
    // PRUNING
    //***********************************************************************************

    private void pruneLB() throws ContradictionException {
        int node;
        int min = Integer.MIN_VALUE;
        int max = Integer.MIN_VALUE;
        int nbKer = 0;
		int index = 0;
		kerRepresentant.clear();
        for (int i = minIndex; i < maxIndex; i++) {
            for (int k = bound[i].size() - 1; k >= 0; k--) {
                node = bound[i].get(k);
				orderedNodes[index++] = node;
                if (min == Integer.MIN_VALUE) {
                    min = minVal[node];
                    max = maxVal[node];
                    nbKer++;
                }else if (minVal[node] <= max) {
                    min = Math.max(min, minVal[node]);
                    max = Math.min(max, maxVal[node]);
                } else {
                    min = minVal[node];
                    max = maxVal[node];
					kerRepresentant.set(node);
                    nbKer++;
                }
            }
        }
        vars[n].updateLowerBound(nbKer, aCause);
        if (nbKer == vars[n].getUB()) {
			stamp.clear();
			for(int i=0;i<n;i++){
				node = orderedNodes[i];
				if(kerRepresentant.get(node)){
					updateKer(minVal[node],true);
					stamp.clear();
				}
				stamp.add(node);
			}
			updateKer(Integer.MAX_VALUE,true);
        }
    }

    private void pruneUB() throws ContradictionException {
        int node;
        int min = Integer.MIN_VALUE;
        int max = Integer.MIN_VALUE;
        int nbKer = 0;
		kerRepresentant.clear();
		int index = 0;
        for (int i = maxIndex; i>=minIndex; i--) {
            for (int k = bound[i].size() - 1; k >= 0; k--) {
                node = bound[i].get(k);
				orderedNodes[index++] = node;
                if (min == Integer.MIN_VALUE) {
                    min = minVal[node];
                    max = maxVal[node];
                    nbKer++;
                }else if (maxVal[node] >= min) {
                    max = Math.min(max, maxVal[node]);
                    min = Math.max(min, minVal[node]);
                } else {
                    min = minVal[node];
                    max = maxVal[node];
					kerRepresentant.set(node);
                    nbKer++;
                }
            }
        }
        vars[n].updateLowerBound(nbKer, aCause);
        if (nbKer == vars[n].getUB()) {
			stamp.clear();
			for(int i=0;i<n;i++){
				node = orderedNodes[i];
				if(kerRepresentant.get(node)){
					updateKer(maxVal[node],false);
					stamp.clear();
				}
				stamp.add(node);
			}
			updateKer(Integer.MIN_VALUE,false);
        }
    }

	private void updateKer(int newVal, boolean LB) throws ContradictionException {
		if(LB){
			int min = Integer.MIN_VALUE;
			for(int i=stamp.size()-1;i>=0;i--){
				if(vars[stamp.get(i)].getUB()<newVal)
				min = Math.max(min, vars[stamp.get(i)].getLB());
			}
			for(int i=stamp.size()-1;i>=0;i--){
				if(vars[stamp.get(i)].getUB()<newVal)
				vars[stamp.get(i)].updateLowerBound(min, aCause);
			}
		}else{
			int max = Integer.MAX_VALUE;
			for(int i=stamp.size()-1;i>=0;i--){
				if(vars[stamp.get(i)].getLB()>newVal)
				max = Math.min(max, vars[stamp.get(i)].getUB());
			}
			for(int i=stamp.size()-1;i>=0;i--){
				if(vars[stamp.get(i)].getLB()>newVal)
				vars[stamp.get(i)].updateUpperBound(max, aCause);
			}
		}
	}
	
    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
		vars[n].updateLowerBound(1,aCause);
		vars[n].updateUpperBound(n,aCause);
        computeBounds();
        sortLB();
        pruneLB();
        sortUB();
        pruneUB();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate(EventType.FULL_PROPAGATION);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INCLOW.mask + EventType.INSTANTIATE.mask + EventType.DECUPP.mask;
    }

    @Override
    public ESat isEntailed() {
        BitSet values = new BitSet(nbMaxValues);
        BitSet mandatoryValues = new BitSet(nbMaxValues);
        IntVar v;
        int ub;
		int minVal = 0;
		for (int i = 0; i < n; i++) {
			if(minVal>vars[i].getLB()){
				minVal = vars[i].getLB();
			}
		}
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            if (v.instantiated()) {
                mandatoryValues.set(ub-minVal);
            }
            for (int j = v.getLB(); j <= ub; j++) {
                values.set(j-minVal);
            }
        }
        if (values.cardinality() <= vars[n].getLB()) {
            return ESat.TRUE;
        }
        if (mandatoryValues.cardinality() > vars[n].getUB()) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }
}
