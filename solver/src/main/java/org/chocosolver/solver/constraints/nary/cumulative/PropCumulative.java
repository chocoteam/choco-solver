/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Cumulative propagator
 * Performs energy checking and mandatory part based filtering
 * BEWARE : not idempotent, use two propagators to get the fix point
 *
 * @author Jean-Guillaume Fages
 * @since 31/01/13
 */
public class PropCumulative extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected final int n;
    protected final IntVar[] s, d, e, h;
    protected final IntVar capa;
    protected CumulFilter[] filters;
    protected ISet allTasks;
    protected final IStateInt lastCapaMax;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * protected constructor, should not be called by a user
     */
    protected PropCumulative(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa,
                             boolean reactToFineEvt, CumulFilter... filters) {
        super(ArrayUtils.append(s, d, e, h, new IntVar[]{capa}), PropagatorPriority.QUADRATIC, reactToFineEvt);
        this.n = s.length;
        if (!(n == d.length && n == e.length && n == h.length)) {
            throw new UnsupportedOperationException();
        }
        this.s = Arrays.copyOfRange(vars, 0, n);
        this.d = Arrays.copyOfRange(vars, n, n * 2);
        this.e = Arrays.copyOfRange(vars, n * 2, n * 3);
        this.h = Arrays.copyOfRange(vars, n * 3, n * 4);
        this.capa = this.vars[4 * n];
        this.filters = filters;
        lastCapaMax = model.getEnvironment().makeInt(capa.getUB() + 1);
        allTasks = SetFactory.makeStoredSet(SetType.BIPARTITESET,0,getModel());
        for(int t=0;t<n;t++){
            allTasks.add(t);
        }
    }

    /**
     * Classical cumulative propagator
     *
     * @param s       start 		variables
     * @param d       duration	variables
     * @param e       end			variables
     * @param h       height		variables
     * @param capa    capacity	variable
     *                (only reacts to instantiation events)
     * @param filters filtering algorithm to use
     */
    public PropCumulative(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa,
                          CumulFilter... filters) {
        this(s, d, e, h, capa, false, filters);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int idx) {
        if (idx == vars.length - 1) {
            return IntEventType.combine(IntEventType.INSTANTIATE, IntEventType.DECUPP);
        }
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            propIni();
        }
        updateMaxCapa();
        filter(allTasks);
    }

    protected void propIni() throws ContradictionException {
        for (int i = 0; i < n; i++) {
            d[i].updateLowerBound(0, this);
            h[i].updateLowerBound(0, this);
            s[i].updateBounds(e[i].getLB() - d[i].getUB(), e[i].getUB() - d[i].getLB(), this);
            e[i].updateBounds(s[i].getLB() + d[i].getLB(), s[i].getUB() + d[i].getUB(), this);
            d[i].updateBounds(e[i].getLB() - s[i].getUB(), e[i].getUB() - s[i].getLB(), this);
        }
    }

    protected void updateMaxCapa() throws ContradictionException {
        if (lastCapaMax.get() != capa.getUB()) {
            int capaMax = capa.getUB();
            lastCapaMax.set(capaMax);
            for (int i = 0; i < n; i++) {
                if(d[i].getLB()>0) {
                    h[i].updateUpperBound(capaMax, this);
                }else if(h[i].getLB()>capaMax){
                    d[i].instantiateTo(0,this);
                }
            }
        }
    }

    public void filter(ISet tasks) throws ContradictionException {
        ISetIterator tIter = tasks.iterator();
        while (tIter.hasNext()){
            int t = tIter.nextInt();
            if(h[t].getUB()==0 || d[t].getUB()==0){
                tasks.remove(t);
            }
        }
        for (CumulFilter cf : filters) {
            cf.filter(s, d, e, h, capa, tasks, this);
        }
    }

    @Override
    public ESat isEntailed() {
        int min = s[0].getUB();
        int max = e[0].getLB();
        // check start + duration = end
        for (int i = 0; i < n; i++) {
            min = Math.min(min, s[i].getUB());
            max = Math.max(max, e[i].getLB());
            if (s[i].getLB() + d[i].getLB() > e[i].getUB()
                    || s[i].getUB() + d[i].getUB() < e[i].getLB()) {
                return ESat.FALSE;
            }
        }
        // check capacity
        int maxLoad = 0;
        if (min <= max) {
            int capamax = capa.getUB();
            int[] consoMin = new int[max - min];
            for (int i = 0; i < n; i++) {
                for (int t = s[i].getUB(); t < e[i].getLB(); t++) {
                    consoMin[t - min] += h[i].getLB();
                    if (consoMin[t - min] > capamax) {
                        return ESat.FALSE;
                    }
                    maxLoad = Math.max(maxLoad, consoMin[t - min]);
                }
            }
        }
        // check variables are instantiated
        for (int i = 0; i < vars.length - 1; i++) {
            if (!vars[i].isInstantiated()) {
                return ESat.UNDEFINED;
            }
        }
        assert min <= max;
        // capacity check entailed
        if (maxLoad <= vars[4 * n].getLB()) {
            return ESat.TRUE;
        }
        // capacity not instantiated
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName() + "(");
        for (int i = 0; i < n; i++) {
            sb.append("[").append(vars[i].toString());
            sb.append(",").append(vars[i + n].toString());
            sb.append(",").append(vars[i + 2 * n].toString());
            sb.append(",").append(vars[i + 3 * n].toString()).append("],");
        }
        sb.append(vars[4 * n].toString()).append(")");
        return sb.toString();
    }

    private  IntVar pivotDuration(int[] ind, IntVar pivot){
        IntVar duration = pivot;
        for(int i : ind){
            if(vars[i] == pivot){
                duration = vars[i+n];
            }
        }
        return duration;
    }
    private  int getInd(IntVar pivot){
        int ind = -1;
        for(int i = 0; i<vars.length;i++){
            if(vars[i] == pivot){
                ind = i;
            }
        }
        if(ind==-1){throw new UnsupportedOperationException("Unfindable variable ");}
        return ind;
    }
    private void explainInc(ExplanationForSignedClause explanation, IntVar pivot, int[] indS, int[] indD, int[] indE, int val){
        boolean flag = false;
        for (int i : indS) {
            if(explanation.readDom(pivot).min() >= (val - explanation.readDom(pivotDuration(indD,pivot)).min())
                    && explanation.readDom(vars[i]).min() >= (val - explanation.readDom(vars[indD[i]]).min())
                    && explanation.readDom(vars[i]).max() < val){
                vars[i].unionLit(IntIterableRangeSet.MIN, val - explanation.readDom(vars[indD[i]]).min(), explanation);
                vars[i].unionLit(val-1, IntIterableRangeSet.MAX, explanation);
                flag = true;
            }
        }
        if(flag){
            IntIterableRangeSet set = explanation.universe();
            set.removeBetween(val - explanation.readDom(pivotDuration(indS,pivot)).min(),val-1);//pivot apparais 2 fois, on fais l'union des sets
            pivot.intersectLit(set, explanation);
        }
    }
    private void explainDec(ExplanationForSignedClause explanation, IntVar pivot, int[] indS, int[] indD, int[] indE, int val){
        boolean flag = false;
        for (int i : indS) {
            if(explanation.readDom(pivot).min() < (val + explanation.readDom(pivotDuration(indS,pivot)).min())
                    && explanation.readDom(vars[i]).min() >= (val - explanation.readDom(vars[indD[i]]).min() + explanation.readDom(pivotDuration(indS,pivot)).min())
                    && explanation.readDom(vars[i]).max() < (val + explanation.readDom(pivotDuration(indS,pivot)).min())){
                IntIterableRangeSet set = explanation.universe();
                set.removeBetween(val - explanation.readDom(vars[indD[i]]).min() + explanation.readDom(pivotDuration(indS,pivot)).min(),val + explanation.readDom(pivotDuration(indS,pivot)).min()-1);
                vars[i].unionLit(set, explanation);
                flag = true;
            }
        }
        if(flag){
            IntIterableRangeSet set = explanation.universe();
            set.removeBetween(val,val + explanation.readDom(pivotDuration(indS,pivot)).min()-1);//pivot apparais 2 fois, on fais l'union des sets
            pivot.intersectLit(set, explanation);
        }
    }
    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        int[] indS = IntStream.range(0, n).toArray();
        int[] indD = IntStream.range(n, n * 2).toArray();
        int[] indE = IntStream.range(n * 2, n * 3).toArray();
        int[] indH = IntStream.range(n * 3, n * 4).toArray();
        IntVar pivot = explanation.readVar(p);
        int val;
        switch (explanation.readMask(p)) {
            case 2://INCLOW
                if(getInd(pivot)<n){
                    if(explanation.readDom(p).cardinality()>0){
                        val = explanation.readDom(p).min();
                    }else{
                        val = explanation.readDom(pivot).max()+1;
                        throw new UnsupportedOperationException("Unknown val");
                    }
                    explainInc(explanation, pivot, indS, indD, indE, val);
                    System.out.println("inc : "+explanation.toString());
                }else{
                    System.out.println(n+"   "+getInd(pivot));
                    Propagator.defaultExplain(this, p, explanation);
                    System.out.println("inc default "+explanation.toString());
                }
                break;
            case 4://DECUPP
                if(getInd(pivot)<3*n&&getInd(pivot)>=2*n) {
                    if (explanation.readDom(p).cardinality() > 0) {
                        val = explanation.readDom(p).max() + 1 - explanation.readDom(pivotDuration(indS,pivot)).min();
                    } else {
                        val = explanation.readDom(pivot).min() - explanation.readDom(pivotDuration(indS,pivot)).min();
                        throw new UnsupportedOperationException("Unknown val");
                    }
                    explainDec(explanation, pivot, indS, indD, indE, val);
                    System.out.println("dec : "+explanation.toString());
                }else{
                    System.out.println(n+"   "+getInd(pivot));
                    Propagator.defaultExplain(this, p, explanation);
                    System.out.println("dec default " + explanation.toString());
                }
                break;
            case 8://INSTANTIATE
            case 1://REMOVE
            case 0://VOID
            case 6://BOUND inclow+decup
            default:
                throw new UnsupportedOperationException("Unknown event type explanation");
        }
    }
}
