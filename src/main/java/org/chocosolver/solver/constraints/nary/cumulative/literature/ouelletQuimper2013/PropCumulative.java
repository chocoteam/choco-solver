/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.ouelletQuimper2013;

import org.chocosolver.solver.constraints.nary.cumulative.literature.CumulativeFilter;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.*;

/**
 * Cumulative constraint filtering algorithms described in the following paper :
 * Ouellet, P., Quimper, C.-G.: Time-table-extended-edge-finding for the cumulative constraint. In: Proceedings of the 19th International Conference on Principles and Practice of Constraint Programming (CP 2013), pp. 562-577 (2013). https://doi.org/10.1007/978-3-642-40627-0_42
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class PropCumulative extends CumulativeFilter {
    class DecomposedTask {
        final int idxTask, est, lct, h, p;
        DecomposedTask(int idxTask, int est, int lct, int h, int p) {
            this.idxTask = idxTask;
            this.est = est;
            this.lct = lct;
            this.h = h;
            this.p = p;
        }

        @Override
        public String toString() {
            return "["+est+","+lct+","+h+","+p+"]";
        }
    }

    private OmegaLambdaPhiGammaTree tree;
    private TIntArrayList eligibleHeights, r;
    private int[] c;
    private int cSize;
    private ArrayList<DecomposedTask> T, F, extEdgeFind;
    private BitSet S; // TODO : check if complexities are the same as for an AVL Tree
    private int min;

    public PropCumulative(Task[] tasks, IntVar[] heights, IntVar capacity, boolean timeTable, boolean edgeFinding, boolean timetableExtendedEdgeFinding) {
        super(tasks, heights, capacity);
        this.timeTable = timeTable;
        this.edgeFinding = edgeFinding;
        this.timeTableExtendedEdgeFinding = timetableExtendedEdgeFinding;

        eligibleHeights = new TIntArrayList(heights.length);
        r = new TIntArrayList(tasks.length*4);
        c = new int[4*tasks.length];
        T = new ArrayList<>(tasks.length);
        F = new ArrayList<>(4*tasks.length);
        extEdgeFind = new ArrayList<>(5*tasks.length);
        int min = tasks[0].getStart().getLB(), max = tasks[0].getEnd().getUB();
        for(int i = 1; i<tasks.length; i++) {
            min = Math.min(min, tasks[i].getStart().getLB());
            max = Math.max(max, tasks[i].getEnd().getUB());
        }
        S = new BitSet(max-min+1);
        this.min = min;
    }

    private boolean extendedEdgeFinding(ArrayList<DecomposedTask> list) throws ContradictionException {
        boolean hasFiltered = false;
        fillEligibleHeights();
        int hor = tasks[0].getEnd().getUB();
        for(int k = 1; k<tasks.length; k++) {
            hor = Math.max(hor, tasks[k].getEnd().getUB());
        }
        list.sort(Comparator.comparingInt(dt -> -dt.lct));
        for(int ih = 0; ih<eligibleHeights.size(); ih++) {
            int h = eligibleHeights.getQuick(ih);
            tree = new OmegaLambdaPhiGammaTree(list.toArray(new DecomposedTask[]{}), capacity.getUB());
            tree.setHor(hor);
            tree.buildTree(true, h);
            for(int j = 0; j<list.size(); j++) {
                int lctj = list.get(j).lct;
                if(tree.root.env > capacity.getUB()*lctj) {
                    aCause.fails();
                }
                tree.updateLambdaPhi(j);
                int m;
                do {
                    int sigmaEFw = OmegaLambdaPhiGammaTree.plus(tree.root.envLambda,-capacity.getUB()*lctj),
                            sigmaEEFw = OmegaLambdaPhiGammaTree.plus(tree.root.envXLambda,-capacity.getUB()*lctj),
                            sigmaEFs = OmegaLambdaPhiGammaTree.plus(tree.root.envPhi,-capacity.getUB()*lctj-h*(hor-lctj)),
                            sigmaEEFs = OmegaLambdaPhiGammaTree.plus(tree.root.envXPhi,-capacity.getUB()*lctj-h*(hor-lctj));
                    m = Math.max(Math.max(Math.max(sigmaEFw, sigmaEEFw), sigmaEFs), sigmaEEFs);
                    if(m>0) {
                        int i;
                        Integer v = null;
                        if(m == sigmaEEFw) {
                            i = tree.root.responsibleEnvXLambda.taskIdx;
                            if(heights[list.get(i).idxTask].getLB()!=0) {
                                int k = tree.root.responsibleEnvH.taskIdx;
                                v = lctj-(list.get(i).est+list.get(i).p-list.get(k).est)+(int)(Math.ceil(1.0*sigmaEEFw/heights[list.get(i).idxTask].getLB()));
                            }
                            tree.removeFromLambda(i);
                        } else if(m == sigmaEEFs) {
                            i = tree.root.responsibleEnvXPhi.taskIdx;
                            if(heights[list.get(i).idxTask].getLB()!=0) {
                                int k = tree.root.responsibleEnvH.taskIdx;
                                v = list.get(k).est+(int)(Math.ceil(1.0*sigmaEEFs/heights[list.get(i).idxTask].getLB()));
                            }
                            tree.removeFromPhi(i);
                        } else if(m == sigmaEFw) {
                            i = tree.root.responsibleEnvLambda.taskIdx;
                            if(heights[list.get(i).idxTask].getLB()!=0) {
                                v = lctj-list.get(i).p+(int)(1.0*Math.ceil(sigmaEFw/heights[list.get(i).idxTask].getLB()));
                            }
                            tree.removeFromLambda(i);
                        } else {
                            i = tree.root.responsibleEnvPhi.taskIdx;
                            if(heights[list.get(i).idxTask].getLB()!=0) {
                                v = list.get(i).est+(int)(Math.ceil(1.0*sigmaEFs/heights[list.get(i).idxTask].getLB()));
                            }
                            tree.removeFromPhi(i);
                        }
                        if(v!=null && list.get(i).idxTask >= 0) {
                            int lsti = tasks[list.get(i).idxTask].getStart().getUB();
                            hasFiltered |= tasks[list.get(i).idxTask].getStart().updateLowerBound(Math.min(v, lsti), aCause);
                        }
                    }
                } while(m > 0);
                if(h == list.get(j).h && list.get(j).est+list.get(j).p<list.get(j).lct) {
                    tree.addToLambda(j);
                }
                tree.removeFromOmega(j);
            }
        }
        return hasFiltered;
    }

    @Override
    public boolean edgeFinding() throws ContradictionException {
        timeTableTaskDecomposition();
        extEdgeFind.clear();
        extEdgeFind.addAll(T);
        return extendedEdgeFinding(extEdgeFind);
    }

    private void fillEligibleHeights() {
        eligibleHeights.clear(heights.length);
        for(DecomposedTask dt : extEdgeFind) {
            if(!eligibleHeights.contains(dt.h) && dt.est+dt.p<dt.lct && dt.h!=0) {
                eligibleHeights.add(dt.h);
            }
        }
    }

    private void fillR() {
        r.clear(4*tasks.length);
        for(Task t : tasks) {
            if(!r.contains(t.getStart().getLB())) {
                r.add(t.getStart().getLB());
            }
            if(!r.contains(t.getStart().getUB())) {
                r.add(t.getStart().getUB());
            }
            if(!r.contains(t.getEnd().getLB())) {
                r.add(t.getEnd().getLB());
            }
            if(!r.contains(t.getEnd().getUB())) {
                r.add(t.getEnd().getUB());
            }
        }
        r.sort();
    }

    private int indexOf(int t) {
        int b = 0, e = r.size(), mid;
        while(e-b > 1) {
            mid = (e-b)/2+b;
            if(r.getQuick(mid) == t) {
                return mid;
            } else if(r.getQuick(mid) < t) {
                b = mid+1;
            } else {
                e = mid;
            }
        }
        return b;
    }

    private void timeTableTaskDecomposition() throws ContradictionException {
        fillR();
        cSize = r.size();
        Arrays.fill(c, 0, cSize, 0);
        T.clear();
        F.clear();
        for(int i = 0; i<tasks.length; i++) {
            int ect = tasks[i].getEnd().getLB(), lst = tasks[i].getStart().getUB();
            if(ect > lst) {
                int a = indexOf(lst), b = indexOf(ect);
                c[a] += heights[i].getLB();
                c[b] -= heights[i].getLB();
                T.add(new DecomposedTask(i, tasks[i].getStart().getLB(), tasks[i].getEnd().getUB(), heights[i].getLB(), tasks[i].getDuration().getLB()-ect+lst));
            } else {
                T.add(new DecomposedTask(i, tasks[i].getStart().getLB(), tasks[i].getEnd().getUB(), heights[i].getLB(), tasks[i].getDuration().getLB()));
            }
        }

        for(int l = 1; l<cSize; l++) {
            c[l] += c[l-1];
            if(c[l-1] > capacity.getUB()) {
                aCause.fails();
            } else if(c[l-1] > 0) {
                F.add(new DecomposedTask(-1, r.getQuick(l-1), r.getQuick(l), c[l-1], r.getQuick(l)-r.getQuick(l-1)));
            }
        }
    }

    private boolean filterTimeTabling() throws ContradictionException {
        boolean hasFiltered = false;

        timeTableTaskDecomposition();
        F.sort(Comparator.comparingInt(dt -> -dt.h));
        T.sort(Comparator.comparingInt(dt -> dt.h));
        int j = 0;
        S.clear();
        Integer smallest = null, largest = null;

        for(DecomposedTask dt : T) {
            while(j<F.size() && F.get(j).h>capacity.getUB()-dt.h) {
                S.set(F.get(j).est-min, F.get(j).lct-min);
                smallest = (smallest==null ? F.get(j).est : Math.min(F.get(j).est, smallest));
                largest = (largest==null ? F.get(j).lct-1 : Math.max(F.get(j).lct-1, largest));
                j++;
            }
            if(largest!=null && largest>=dt.est) {
                int b = S.nextClearBit(Math.max(smallest+1-min, dt.est-min))+min;
                if(b<largest+2) {
                    int a = S.previousClearBit(b-min)+min+1;
                    if(dt.est+dt.p > a) {
                        int i = dt.idxTask;
                        if(tasks[i].getStart().getUB() >= tasks[i].getEnd().getLB()) {
                            hasFiltered |= tasks[i].getStart().updateLowerBound(b, aCause);
                        } else {
                            hasFiltered |= tasks[i].getStart().updateLowerBound(Math.min(tasks[i].getStart().getUB(), b), aCause);
                        }
                    }
                }
            }
        }
        return hasFiltered;
    }

    @Override
    public boolean timeTable() throws ContradictionException {
        return filterTimeTabling();
    }

    @Override
    public boolean timeTableExtendedEdgeFinding() throws ContradictionException {
        boolean hasFiltered = false;
        boolean fixPoint = false;

        while(!fixPoint) {
            boolean tmp = filterTimeTabling();
            if(tmp) {
                hasFiltered = true;
            }
            fixPoint = !tmp;
        }
        timeTableTaskDecomposition();
        extEdgeFind.clear();
        extEdgeFind.addAll(T);
        extEdgeFind.addAll(F);
        hasFiltered |= extendedEdgeFinding(extEdgeFind);

        return hasFiltered;
    }
}
