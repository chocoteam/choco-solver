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

import static org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils.unionOf;

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

    /**
     * Return the index of an IntVar
     *
     * @param pivot
     */
    private  int getInd(IntVar pivot){
        int ind = -1;
        for(int i = 0; i<vars.length;i++){
            if(vars[i] == pivot){
                ind = i;
            }
        }
        if(ind==-1)throw new UnsupportedOperationException("Unfindable pivot variable ");
        return ind;
    }
    /**
     * Add to the explanation the lower bound event greater or equal than t (the real event added is inverted because only disjunctions are allowed for explanation)
     * @param var variable to set the event on
     * @param t value
     */
    private void explainSupit(ExplanationForSignedClause e, IntVar var, int t) {
        var.unionLit(e.root(var).min(),t-1,e);
    }
    /**
     * Add to the explanation the upper bound event strictly smaller than t (the real event added is inverted because only disjunctions are allowed for explanation)
     * @param var variable to set the event on
     * @param t value
     */
    private void explainInfit(ExplanationForSignedClause e, IntVar var, int t) {
        var.unionLit(t,e.root(var).max(),e);
    }
    /**
     * Find in the implication graph and add to the explanation all the lower bound event greater or equal than t (the real events added are inverted because only disjunctions are allowed for explanation)
     * @param t value
     */
    private void explainSupForallit(ExplanationForSignedClause e, int[] indexes, int t) {
        for(int i: indexes){
            if(e.domain(vars[i]).max()>=t){
                explainSupit(e,vars[i],t);
            }
        }
    }
    /**
     * Find in the implication graph and add to the explanation all the upper bound event strictly smaller than t (the real events added are inverted because only disjunctions are allowed for explanation)
     * @param t value
     */
    private void explainInfForallit(ExplanationForSignedClause e, int[] indexes, int t) {
        for(int i: indexes){
            if(e.domain(vars[i]).max()<t){
                explainInfit(e,vars[i],t);
            }
        }
    }
    /**
     * Find in the implication graph and add to the explanation all the lower bound event greater or equal than t-duration (the real events added are inverted because only disjunctions are allowed for explanation)
     * @param indD index array to access durations
     * @param t value
     */
    private void explainSupForallitMinusci(ExplanationForSignedClause e, int[] indexes, int t, int[] indD) {
        for(int i: indexes){
            if(e.domain(vars[i]).min()>=t-e.domain(vars[indD[i]]).min()){
                explainSupit(e,vars[i],t-e.domain(vars[indD[i]]).min());
            }
        }
    }
    /**
     * Find in the implication graph and add to the explanation all the upper bound event strictly smaller than t-duration (the real events added are inverted because only disjunctions are allowed for explanation)
     * @param indD index array to access durations
     * @param t value
     */
    private void explainInfForallitMinusci(ExplanationForSignedClause e, int[] indexes, int t, int[] indD) {
        for(int i: indexes){
            if(e.domain(vars[i]).max()<t-e.domain(vars[indD[i]]).min()){
                explainInfit(e,vars[i],t-e.domain(vars[indD[i]]).min());
            }
        }
    }
    private void explainInf(ExplanationForSignedClause e, int[] indexes, int t, int[] indD) {
        for(int i: indexes){
            if(e.domain(vars[i]).max()<=t && e.domain(vars[i]).min()>t-e.domain(vars[indD[i]]).min()){
                explainInfit(e,vars[i],t);
                explainSupit(e,vars[i],t-e.domain(vars[indD[i]]).min());
            }
        }
    }
    private void explainSup(ExplanationForSignedClause e, int[] indexes, int t, int[] indD) {
        for(int i: indexes){
            if(e.domain(vars[i]).max()<=t && e.domain(vars[i]).min()>t-e.domain(vars[indD[i]]).min()){
                explainInfit(e,vars[i],t);
                explainSupit(e,vars[i],t-e.domain(vars[indD[i]]).min());
            }
        }
    }

    /**
     * Detect and explain the event at pivot variable p
     * @param p pivot variable
     */
    @Override
    public void explain(int p, ExplanationForSignedClause e) {
        IntVar pivot = e.readVar(p);
        int[] X = IntStream.range(0, n).filter(i->vars[i]!=pivot).toArray();
        int[] indD = IntStream.range(n, n * 2).toArray();
        int[] indE = IntStream.range(n * 2, n * 3).toArray();
        int[] indH = IntStream.range(n * 3, n * 4).toArray();
        int t = e.readValue(p);
        int ipivot = getInd(pivot);
        int dpivot = e.domain(vars[ipivot+n]).min();
        switch (e.readMask(p)) {
            case 2://INCLOW
                if (ipivot < n){
                    explainInf(e, X, t, indD);

                    IntIterableRangeSet set = new IntIterableRangeSet(t,e.root(pivot).max());
                    if(t - dpivot-1>=e.root(pivot).min()) {
                        IntIterableRangeSet set2 = new IntIterableRangeSet(e.root(pivot).min(),t - dpivot-1);
                        unionOf(set,set2);
                    }
                    pivot.intersectLit(set, e);
                } else {//capa ou autres
                    throw new UnsupportedOperationException("Unknown event type explanation");
                    //defaultExplain(this, p, e);
                }
                break;
            case 4://DECUPP
                if (ipivot < n){
                    explainSup(e, X, t + dpivot, indD);

                    IntIterableRangeSet set = new IntIterableRangeSet(e.root(pivot).min(),t-1);
                    if(t + dpivot<e.root(pivot).max()){
                        IntIterableRangeSet set2 = new IntIterableRangeSet(t + dpivot,e.root(pivot).max());
                        unionOf(set,set2);
                    }
                    pivot.intersectLit(set, e);
                }else {//capa ou autres
                    throw new UnsupportedOperationException("Unknown event type explanation");
                    //defaultExplain(this, p, e);
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
