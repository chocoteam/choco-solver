/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.binPacking;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.procedure.UnaryIntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.BitSet;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * Propagator for a Bin Packing constraint
 * This propagator is an implementation of filtering rules introduced in the following paper :
 * Shaw, P. (2004). A Constraint for Bin Packing. In M. Wallace (Ed.), Principles and Practice of Constraint Programming – CP 2004 (pp. 648–662). Springer Berlin Heidelberg.
 *
 * @author Arthur Godet <arth.godet@gmail.com>, Jean-Guillaume Fages
 */
public class PropBinPacking extends Propagator<IntVar> {
    private final IntVar[] itemBin;
    protected int[] itemSize;
    protected IntVar[] binLoad;
    protected final int offset;

    private final int nbItems;
    private final int nbAvailableBins;

    protected IIntDeltaMonitor[] monitors;
    protected ISet[] P;
    protected ISet[] R;
    protected IStateInt[] sumR;
    private final IStateInt[] sumP;
    private final BitSet binsToProcess;

    // NoSum parameters and Java variables
    private final boolean useNoSumFiltering;
    private int sumA;
    private int sumB;
    private int sumC;
    private int k;
    private int kPrime;
    private final int[] indexSortedBySize;
    private final int[] X;
    private int xSize;

    @SuppressWarnings("Convert2Diamond")
    private final UnaryIntProcedure<Integer> procedure = new UnaryIntProcedure<Integer>() {
        int item;

        @Override
        public UnaryIntProcedure<Integer> set(Integer itemIdx) {
            item = itemIdx;
            return this;
        }

        @Override
        public void execute(int bin) throws ContradictionException {
            bin -= offset;
            if (bin >= 0 && bin < nbAvailableBins && P[bin].contains(item)) {
                P[bin].remove(item);
                binLoad[bin].updateUpperBound(sumP[bin].add(-itemSize[item]), PropBinPacking.this);
            }
        }
    };

    /**
     * Propagator for a Bin Packing constraint
     *
     * @param itemBin bin of every item (possibly with offset)
     * @param itemSize size of every item
     * @param binLoad total load of every bin
     * @param offset index offset: binOfItem[i] = k means item i is in bin k-offset
     */
    public PropBinPacking(IntVar[] itemBin, int[] itemSize, IntVar[] binLoad, int offset) {
        this(itemBin, itemSize, binLoad, offset, true);
    }

    /**
     * Propagator for a Bin Packing constraint
     *
     * @param itemBin bin of every item (possibly with offset)
     * @param itemSize size of every item
     * @param binLoad total load of every bin
     * @param offset index offset: binOfItem[i] = k means item i is in bin k-offset
     * @param useNoSumFiltering indicates whether to use NoSum filterings or not (should be true)
     */
    public PropBinPacking(IntVar[] itemBin, int[] itemSize, IntVar[] binLoad, int offset, boolean useNoSumFiltering) {
        super(ArrayUtils.append(itemBin, binLoad), PropagatorPriority.LINEAR, true);
        this.itemBin = itemBin;
        this.itemSize = itemSize;
        this.binLoad = binLoad;
        this.offset = offset;
        this.useNoSumFiltering = useNoSumFiltering;

        nbItems = itemBin.length;
        nbAvailableBins = binLoad.length;

        monitors = new IIntDeltaMonitor[nbItems];
        for(int i=0; i<nbItems; i++){
            monitors[i] = itemBin[i].monitorDelta(this);
        }

        P = new ISet[nbAvailableBins];
        R = new ISet[nbAvailableBins];
        sumR = new IStateInt[nbAvailableBins];
        sumP = new IStateInt[nbAvailableBins];
        for(int j = 0; j<nbAvailableBins; j++) {
            P[j] = SetFactory.makeStoredSet(SetType.BITSET, 0, itemBin[0].getModel());
            R[j] = SetFactory.makeStoredSet(SetType.BITSET, 0, itemBin[0].getModel());
            int pj = 0;
            int rj = 0;
            for(int i = 0; i<nbItems; i++) {
                if(itemBin[i].contains(j+offset)) {
                    P[j].add(i);
                    pj += itemSize[i];
                    if(itemBin[i].isInstantiated()) {
                        R[j].add(i);
                        rj += itemSize[i];
                    }
                }
            }
            sumR[j] = itemBin[0].getModel().getEnvironment().makeInt(rj);
            sumP[j] = itemBin[0].getModel().getEnvironment().makeInt(pj);
        }

        binsToProcess = new BitSet(nbAvailableBins);

        // NoSum init
        X = new int[itemSize.length];
        indexSortedBySize = IntStream.range(0, itemSize.length)
            .boxed()
            .sorted(Comparator.comparingInt(i -> -itemSize[i]))
            .mapToInt(i -> i)
            .toArray();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if(vIdx < nbItems) {
            return IntEventType.all();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////    FILTERING ALGORITHMS    //////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    private void removeItemFromBin(int binIdx, int itemIdx) throws ContradictionException {
        if(P[binIdx].contains(itemIdx)) {
            P[binIdx].remove(itemIdx);
            binLoad[binIdx].updateUpperBound(sumP[binIdx].add(-itemSize[itemIdx]), this);
            binsToProcess.set(binIdx);
            if(itemBin[itemIdx].isInstantiated()) {
                updateRAfterInstantiation(itemBin[itemIdx].getValue() - offset, itemIdx);
            }
        }
    }

    protected void updateRAfterInstantiation(int binIdx, int itemIdx) throws ContradictionException {
        if(R[binIdx].add(itemIdx)) {
            binLoad[binIdx].updateLowerBound(sumR[binIdx].add(itemSize[itemIdx]), this);
            binsToProcess.set(binIdx);
            for(int k = 0; k<nbAvailableBins; k++) {
                if(k != binIdx && P[k].remove(itemIdx)) {
                    binLoad[k].updateUpperBound(sumP[k].add(-itemSize[itemIdx]), this);
                    binsToProcess.set(k);
                }
            }
        }
    }

    private boolean singleItemEliminationAndCommitment(int j) throws ContradictionException {
        boolean hasFiltered = false;
        ISetIterator iter = P[j].iterator();
        while(iter.hasNext()) {
            int i = iter.nextInt();
            if(!itemBin[i].contains(j + offset)) {
                removeItemFromBin(j, i);
            } else if(!R[j].contains(i)) {
                if(sumP[j].get() - itemSize[i] < binLoad[j].getLB()) {
                    hasFiltered |= itemBin[i].instantiateTo(j+offset, this);
                    updateRAfterInstantiation(j, i);
                } else {
                    if(sumR[j].get() + itemSize[i] > binLoad[j].getUB() && itemBin[i].removeValue(j+offset, this)) {
                        hasFiltered = true;
                        removeItemFromBin(j, i);
                    }
                }
            }
        }
        return hasFiltered;
    }

    private void processBin(int j) throws ContradictionException {
        binsToProcess.clear(j);
        boolean hasFiltered;
        do {
            hasFiltered = singleItemEliminationAndCommitment(j);
            if(useNoSumFiltering) {
                hasFiltered |= noSumFiltering(j);
            }
        } while(hasFiltered);
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////    NO_SUM METHODS    /////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    private void fillXArrayWithCj(int j, int idxToRemove) {
        xSize = 0;
        for(int i = 0; i < nbItems; i++) {
            if(indexSortedBySize[i] != idxToRemove
                && P[j].contains(indexSortedBySize[i])
                && !R[j].contains(indexSortedBySize[i])
            ) {
                X[xSize++] = indexSortedBySize[i];
            }
        }
    }

    private void noSumInit() {
        sumA = 0;
        sumB = 0;
        sumC = 0;
        k = 0;
        kPrime = 0;
    }

    private boolean noSumComputings(int alpha, int beta) {
        while(kPrime < xSize && sumC + itemSize[X[xSize-1-kPrime]] < alpha) {
            sumC += itemSize[X[xSize-1-kPrime]];
            kPrime++;
        }
        if(kPrime < xSize) {
            sumB = itemSize[X[xSize-1-kPrime]];
        }
        while(k<xSize && sumA < alpha && sumB <= beta) {
            sumA += itemSize[X[k]];
            k++;
            if(sumA < alpha) {
                kPrime--;
                sumB += itemSize[X[xSize-1-kPrime]];
                sumC -= itemSize[X[xSize-1-kPrime]];
                while(sumA + sumC >= alpha) {
                    kPrime--;
                    sumC -= itemSize[X[xSize-1-kPrime]];
                    sumB += itemSize[X[xSize-1-kPrime]] - itemSize[X[xSize-1-kPrime-k-1]];
                }
            }
        }
        return sumA < alpha;
    }

    private boolean noSum(int j, int alpha, int beta) {
        return noSum(j, alpha, beta, -1);
    }

    private boolean noSum(int j, int alpha, int beta, int idxToRemove) {
        if(alpha <= 0 || beta >= sumP[j].get()-sumR[j].get()) {
            return false;
        }
        noSumInit();
        fillXArrayWithCj(j, idxToRemove);
        return noSumComputings(alpha, beta);
    }

    private boolean noSumFiltering(int j) throws ContradictionException {
        boolean hasFiltered = false;
        // Pruning Rule
        if(noSum(j, binLoad[j].getLB()-sumR[j].get(), binLoad[j].getUB()-sumR[j].get())) {
            fails();
        }
        // Tightening Bounds on Bin Load
        int lbVal = binLoad[j].getLB()-sumR[j].get();
        if(noSum(j, lbVal, lbVal)) {
            hasFiltered = true;
            binLoad[j].updateLowerBound(sumR[j].get() + sumB, this);
        }
        int ubVal = binLoad[j].getUB()-sumR[j].get();
        if(noSum(j, ubVal, ubVal)) {
            binLoad[j].updateUpperBound(sumR[j].get() + sumA + sumC, this);
        }
        // Elimination and Commitment of Items
        ISetIterator iter = P[j].iterator();
        while(iter.hasNext()) {
            int i = iter.nextInt();
            if(!R[j].contains(i)) {
                int lbVal2 = binLoad[j].getLB()-sumR[j].get()-itemSize[i];
                int ubVal2 = binLoad[j].getUB()-sumR[j].get()-itemSize[i];
                if(noSum(j, lbVal2, ubVal2, i)) {
                    if(itemBin[i].removeValue(j+offset, this)) {
                        hasFiltered = true;
                        removeItemFromBin(j, i);
                    }
                }
                lbVal2 += itemSize[i];
                ubVal2 += itemSize[i];
                if(noSum(j, lbVal2, ubVal2, i)) {
                    hasFiltered |= itemBin[i].instantiateTo(j+offset, this);
                    updateRAfterInstantiation(j, i);
                }
            }
        }
        return hasFiltered;
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////    PROPAGATION    ///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if(idxVarInProp < nbItems) {
            monitors[idxVarInProp].forEachRemVal(procedure.set(idxVarInProp));
            if(itemBin[idxVarInProp].isInstantiated()) {
                int j = itemBin[idxVarInProp].getValue() - offset;
                updateRAfterInstantiation(j, idxVarInProp);
                binsToProcess.set(j);
            }
        } else {
            binsToProcess.set(idxVarInProp - nbItems);
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if(PropagatorEventType.isFullPropagation(evtmask)) {
            for(int i = 0; i<itemBin.length; i++) { // Pack All
                itemBin[i].updateBounds(offset, nbAvailableBins+offset-1, this);
                if(itemBin[i].isInstantiated()) {
                    int j = itemBin[i].getValue() - offset;
                    updateRAfterInstantiation(j, i);
                } else {
                    for(int j = 0; j<nbAvailableBins; j++) {
                        if(!itemBin[i].contains(j + offset)) {
                            removeItemFromBin(j, i);
                        }
                    }
                }
            }
            binsToProcess.set(0, nbAvailableBins);
            for(int i=0; i<nbItems; i++){
                monitors[i].startMonitoring();
            }
        }
        while(!binsToProcess.isEmpty()) {
            processBin(binsToProcess.nextSetBit(0));
        }
    }

    @Override
    public ESat isEntailed() {
        for(int i=0; i<nbItems; i++){
            if(itemBin[i].isInstantiated()){
                int val = itemBin[i].getValue();
                if(val<offset || val>=nbAvailableBins+offset){
                    return ESat.FALSE;
                }
            }
        }
        for(int b = 0; b<nbAvailableBins; b++) {
            int min = 0;
            int max = 0;
            for(int i = 0; i<nbItems; i++) {
                if(itemBin[i].contains(b+offset)) {
                    max += itemSize[i];
                    if(itemBin[i].isInstantiated()) {
                        min += itemSize[i];
                    }
                }
            }
            if( min > binLoad[b].getUB() || max < binLoad[b].getLB()) {
                return ESat.FALSE;
            }
        }
        if(isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
