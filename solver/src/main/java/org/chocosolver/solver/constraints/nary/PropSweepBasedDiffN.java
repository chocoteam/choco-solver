/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Implementation of a sweep-based algorithm for k-dimensional diffN constraint.
 * <br/>
 * This is based on the following <a href="https://uu.diva-portal.org/smash/get/diva2:1117103/FULLTEXT01.pdf">master thesis</a>.
 * In particular, the algorithms described in Section 3 serve as a basis of this class.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/08/2022
 */
public class PropSweepBasedDiffN extends Propagator<IntVar> {

    // Number of orthotopes
    private final int nbOrthotopes;
    // Collections of orthotopes to consider
    private final Orthotope[] os;
    // Store unfixed orthotopes
    private final IStateBitSet unfixed;
    // Store overlapping orthotopes, updated in getOutBoxes
    private final UndirectedGraph overlapping;
    // maximal length in dimension d over all objects
    private final int[] maxl;
    // Number of dimensions
    private final int nbDimensions;
    // Array of forbidden regions
    private final Forbidden[] fs;
    // supports for enumerated domain
    private final IntIterableRangeSet supports;
    private final int[] c;
    private final int[] j;
    private final int[] dl;
    private final int[] ur;
    private final int[] ls;
    private final int options;
    private final boolean dom;

    public PropSweepBasedDiffN(IntVar[][] x, int[][] l) {
        super(ArrayUtils.flatten(x), PropagatorPriority.QUADRATIC, true);
        this.dom = (int) (model.getHookOrDefault("diffn", 2)) == 2;
        this.options = (int) (model.getHookOrDefault("diffnOpt", 1));
        this.nbDimensions = x[0].length;
        this.nbOrthotopes = x.length;
        this.os = new Orthotope[nbOrthotopes];
        this.overlapping = new UndirectedGraph(model, os.length, SetType.BITSET, true);
        this.maxl = new int[nbDimensions];
        this.c = new int[nbDimensions];
        this.j = new int[nbDimensions];
        this.dl = new int[nbDimensions];
        this.ur = new int[nbDimensions];
        this.ls = new int[nbDimensions];
        this.supports = new IntIterableRangeSet();
        this.unfixed = getModel().getEnvironment().makeBitSet(nbOrthotopes);
        this.unfixed.set(0, nbOrthotopes);
        for (int i = 0; i < nbOrthotopes; i++) {
            this.os[i] = new Orthotope(x[i], l[i], true);
            for (int d = 0; d < nbDimensions; d++) {
                maxl[d] = Math.max(maxl[d], l[i][d]);
            }
        }

        this.fs = new Forbidden[os.length - 1];
        for (int i = 0; i < fs.length; i++) {
            fs[i] = new Forbidden(nbDimensions);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            for (int i = 0; i < nbOrthotopes; i++) {
                for (int j = 0; j < i; j++) {
                    if (os[i].mayOverlap(os[j])) {
                        overlapping.addEdge(j, i);
                    }
                }
            }
        }
        filter();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        int i = idxVarInProp / nbDimensions;
        os[i].checkSkippable(maxl);
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < os.length; i++) {
            if (os[i].assignedInAllDimensions()) {
                for (int j = i + 1; j < os.length; j++) {
                    if (os[j].assignedInAllDimensions()) {
                        if (os[i].mayOverlap(os[j])) {
                            return ESat.FALSE;
                        }
                    }
                }
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    private void filter() throws ContradictionException {
        boolean nonfix = true;
        boolean allFixed = true;
        boolean checkArea = false;
        while (nonfix) {
            nonfix = false;
            allFixed = true;
            for (int i = unfixed.nextSetBit(0); i > -1; i = unfixed.nextSetBit(i + 1)) {
                Orthotope o = os[i];
                o.modified = false;
                int nbF = getOutBoxes(os, nbDimensions, o, i);
                if (o.assignedInAllDimensions()) {
                    if (nbF > 0) {
                        this.fails("Cond 1");
                    }
                } else {
                    for (int d = 0; d < nbDimensions && nbF > 0; d++) {
                        if (!o.assignedInDimension(d)) {
                            pruneMin(o, d, nbF);
                            pruneMax(o, d, nbF);
                            if (o.enumeratedOnDimension(d) && dom) {
                                pruneDom(o, d, nbF);
                            }
                            if (o.modified) {
                                nonfix = true;
                            }
                        }
                    }
                }
                if (!o.assignedInAllDimensions()) {
                    allFixed = false;
                } else {
                    unfixed.clear(i);
                }
                //checkEnergy(i);
                if (options == 1 || options == 3) {
                    checkArea = checkEnergy(i);// && this.areaCheck;
                }
            }
        }
        if (options == 2 || (checkArea && options == 3)) {
            checkArea();
        }
        if (allFixed) {
            setPassive();
        }// else return FIXPOINT
    }

    /**
     * For generating the forbidden regions of an orthotope o, according to other orthotopes.
     *
     * @param os collection of orthotopes
     * @param k  number of dimensions
     * @param o  the orthotope whose forbidden regions are to be calculated
     * @param i  index of o in os
     * @return a set of k-dimensional forbidden regions for the orthotope o relative to the orthotopes in os.
     */
    private int getOutBoxes(Orthotope[] os, int k, Orthotope o, int i) {
        int m = 0;
        ISetIterator iter = overlapping.getNeighborsOf(i).iterator();
        while (iter.hasNext()) {
            int j = iter.nextInt();
            Orthotope o2 = os[j];
            //if (o2.isSkippable()) continue; // TODO check
            Forbidden f = fs[m];
            boolean exists = true;
            boolean overlap = true;
            for (int d = 0; d < k; d++) {
                overlap &= o.mayOverlap(o2, d);
                if (o2.x[d].getUB() - o.l[d] + 1 <= o2.x[d].getLB() + o2.l[d] - 1) {
                    f.set(d, o2.x[d].getUB() - o.l[d] + 1, o2.x[d].getLB() + o2.l[d] - 1);
                } else exists = false;
            }
            if (exists && overlaps(o, f)) {
                m++;
            }
            if (!overlap) {
                overlapping.removeEdge(i, j);
            }
        }
        return m;
    }

    /**
     * Used for checking if the domain of orthotope o overlaps the forbidden region f.
     *
     * @param o orthotope
     * @param f forbidden region
     * @return <code>true</code> iff the domain of o overlaps the forbidden region f
     */
    private boolean overlaps(Orthotope o, Forbidden f) {
        for (int d = 0; d < nbDimensions; d++) {
            if (o.x[d].getUB() < f.min(d) || o.x[d].getLB() > f.max(d)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Used for pruning the lower bound of the dth coordinate of an orthotope o.
     *
     * @param o   the orthotope that is being filtered
     * @param d   dimension in which filtering is being performed
     * @param nbF number of forbidden regions to read in fs
     *            The function returns <code>false</code> if no feasible minimum point is obtainable in dimension d for o,
     *            <code>true</code> otherwise.
     * @implSpec Tightens the lower bound of o.x[d] so that o and o' do not overlap, if possible.
     */
    private void pruneMin(Orthotope o, int d, int nbF) throws ContradictionException {
        boolean b = true;
        for (int i = 0; i < nbDimensions; i++) {
            c[i] = o.x[i].getLB();
            j[i] = o.x[i].getUB() + 1;
        }
        Forbidden f = getFR(c, nbF);
        while (b && f != null) {
            for (int i = 0; i < j.length; i++) {
                j[i] = Math.min(j[i], f.max(i) + 1);
            }
            b = adjust(c, j, o, d, true);
            f = getFR(c, nbF);
        }
        o.modified |= o.x[d].updateLowerBound(c[d], this);
        o.checkSkippable(maxl);
    }

    /**
     * Used for pruning the upper bound of the dth coordinate of an orthotope o.
     *
     * @param o   the orthotope that is being filtered
     * @param d   dimension in which filtering is being performed
     * @param nbF number of forbidden regions to read in fs
     *            The function returns <code>false</code> if no feasible minimum point is obtainable in dimension d for o,
     *            <code>true</code> otherwise.
     * @implSpec Tightens the upper bound of o.x[d] so that o and o' do not overlap, if possible.
     */
    private void pruneMax(Orthotope o, int d, int nbF) throws ContradictionException {
        boolean b = true;
        for (int i = 0; i < nbDimensions; i++) {
            c[i] = o.x[i].getUB();
            j[i] = o.x[i].getLB() - 1;
        }
        Forbidden f = getFR(c, nbF);
        while (b && f != null) {
            for (int i = 0; i < j.length; i++) {
                j[i] = Math.max(j[i], f.min(i) - 1);
            }
            b = adjust(c, j, o, d, false);
            f = getFR(c, nbF);
        }
        o.modified |= o.x[d].updateUpperBound(c[d], this);
        o.checkSkippable(maxl);
    }

    /**
     * Used for pruning the domain of the dth coordinate of an orthotope o.
     *
     * @param o   the orthotope that is being filtered
     * @param d   dimension in which filtering is being performed
     * @param nbF number of forbidden regions to read in fs
     *            The function returns <code>false</code> if no feasible minimum point is obtainable in dimension d for o,
     *            <code>true</code> otherwise.
     * @implSpec Tightens the upper bound of o.x[d] so that o and o' do not overlap, if possible.
     */
    private void pruneDom(Orthotope o, int d, int nbF) throws ContradictionException {
        boolean b = true;
        supports.clear();
        for (int i = 0; i < nbDimensions; i++) {
            c[i] = o.x[i].getLB();
            j[i] = o.x[i].getUB() + 1;
        }
        Forbidden f = getFR(c, nbF);
        while (b) {
            while (b && f != null) {
                for (int i = 0; i < j.length; i++) {
                    j[i] = Math.min(j[i], f.max(i) + 1);
                }
                b = adjustDom(c, j, o, d, false);
                f = getFR(c, nbF);
            }
            if (b) {
                supports.add(c[d]);
                b = adjustDom(c, j, o, d, true);
                f = getFR(c, nbF);
            }
        }
        o.modified |= o.x[d].removeAllValuesBut(supports, this);
        o.checkSkippable(maxl);
    }

    /**
     * Used for obtaining a forbidden region according to the current sweep point c and the orthotope being pruned o.
     *
     * @param c   sweep point
     * @param nbF number of forbidden regions to read in fs
     * @return a forbidden region which overlaps c
     */
    private Forbidden getFR(int[] c, int nbF) {
        int i = 0;
        while (i < nbF && isFeasible(fs[i], c)) {
            i++;
        }
        if (i < nbF) {
            return fs[i];
        } else return null;
        //return Arrays.stream(fs).limit(nbF).filter(f -> !isFeasible(f, k, c)).findFirst().orElse(null);
    }

    /**
     * Used for checking if c is feasible according to the forbidden region f
     *
     * @param f forbidden region
     * @param c the sweep point c
     * @return <code>true</code> iff c does not overlap with the input forbidden region f
     */
    private boolean isFeasible(Forbidden f, int[] c) {
        for (int j = 0; j < nbDimensions; j++) {
            if (c[j] < f.min(j) || c[j] > f.max(j)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Moves the sweep point c to the next feasible position based on the jump vector j.
     *
     * @param c       the sweep point
     * @param j       the jump vector
     * @param o       the orthotope being filtered
     * @param d       the dimension in which filtering occurs
     * @param minimum whether the minimum or the maximum is being tightened
     * @return a Boolean indicating whether a candidate new sweep point was found or not
     * @implSpec the sweep point c is updated, the jump vector is reset
     */
    private boolean adjust(int[] c, int[] j, Orthotope o, int d, boolean minimum) {
        if (minimum) {
            for (int i = nbDimensions - 1; i >= 0; i--) {
                int r = (i + d) % nbDimensions;
                c[r] = j[r];
                j[r] = o.x[r].getUB() + 1;
                if (c[r] <= o.x[r].getUB()) {
                    return true;
                } else {
                    c[r] = o.x[r].getLB();
                }
            }
        } else {
            for (int i = nbDimensions - 1; i >= 0; i--) {
                int r = (i + d) % nbDimensions;
                c[r] = j[r];
                j[r] = o.x[r].getLB() - 1;
                if (c[r] >= o.x[r].getLB()) {
                    return true;
                } else {
                    c[r] = o.x[r].getUB();
                }
            }
        }
        c[d] = j[d];
        return false;
    }

    /**
     * Moves the sweep point c to the next feasible position based on the jump vector j.
     *
     * @param c the sweep point
     * @param j the jump vector
     * @param o the orthotope being filtered
     * @param d the dimension in which filtering occurs
     * @return a Boolean indicating whether a candidate new sweep point was found or not
     * @implSpec the sweep point c is updated, the jump vector is reset
     */
    private boolean adjustDom(int[] c, int[] j, Orthotope o, int d, boolean next) {
        if (next) {
            for (int i = nbDimensions - 1; i > 0; i--) {
                int r = (i + d) % nbDimensions;
                c[r] = o.x[r].getLB();
                j[r] = o.x[r].getUB() + 1;
            }
            c[d] = o.x[d].nextValue(c[d]);
            j[d] = o.x[d].getUB() + 1;
            if (c[d] <= j[d] - 1) {
                return true;
            } else {
                c[d] = o.x[d].getLB();
                return false;
            }
        } else {
            for (int i = nbDimensions - 1; i >= 0; i--) {
                int r = (i + d) % nbDimensions;
                c[r] = j[r];
                int u = o.x[r].getUB();
                j[r] = u + 1;
                if (c[r] <= u) {
                    return true;
                } else {
                    c[r] = o.x[r].getLB();
                }
            }
        }
        c[d] = j[d]; // TODO: check if useful
        return false;
    }

    /**
     * Check energy for orthotope i relatively to overlapping orthotopes.
     *
     * @param i index of orthotope to check
     * @return true if area must be checked too.
     * @throws ContradictionException if minimum energy conditions not met
     */
    private boolean checkEnergy(int i) throws ContradictionException {
        long am = os[i].al;
        for (int d = 0; d < nbDimensions; d++) {
            dl[d] = os[i].x[d].getLB();
            ur[d] = os[i].x[d].getUB() + os[i].l[d];
            ls[d] = os[i].l[d];
        }
        ISet neigh = overlapping.getNeighborsOf(i);
        //ISetIterator iter = neigh.iterator();
        //while (iter.hasNext()) {
        //    int j = iter.nextInt();
        for (int j : neigh.toArray()) {
            long ar = 1;
            for (int d = 0; d < nbDimensions; d++) {
                dl[d] = Math.min(dl[d], os[j].x[d].getLB());
                ur[d] = Math.max(ur[d], os[j].x[d].getUB() + os[j].l[d]);
                ls[d] = Math.min(ls[d], os[j].l[d]);
                ar *= ur[d] - dl[d];
            }
            am += os[j].al;
            if (am > ar) {
                //System.out.printf("(%d,%d) : %d > %d ?\n", i, j, am, ar);
                this.fails("Cond 2");
            }
        }
        if (Arrays.stream(ls).min().orElse(0) > 0) {
            double nbOr = 1.;
            for (int d = 0; d < nbDimensions; d++) {
                nbOr *= (ur[d] - dl[d]) * 1. / ls[d];
            }
            if (nbOr <= neigh.size()) {
                this.fails("Cond 3");
            }
        }
        return os[i].area() < am;
    }

    /**
     * Check area for all orthotopes, considering distance to overlapping ones.
     *
     * @throws ContradictionException if area condition is not met
     */
    private void checkArea() throws ContradictionException {
        for (int i = 0; i < os.length; i++) {
            long limArea = os[i].area();
            long area_i = os[i].al;
            ISet neigh = overlapping.getNeighborsOf(i);
            ISetIterator iter = neigh.iterator();
            while (iter.hasNext() && area_i <= limArea) {
                int j = iter.nextInt();
                area_i += computeArea(i, j);
            }
            if (area_i > limArea) {
                this.fails("Cond 4");// TODO: could be more precise, for explanation purpose
            }
        }
    }

    /**
     * Compute the common area between two orthotopes
     *
     * @param i index of an orthotope
     * @param j index of another orthotope
     * @return common area
     */
    private long computeArea(int i, int j) {
        long area_ks = 1;//
        for (int d = 0; d < this.nbDimensions && area_ks > 0; d++) {
            long aa = computeAreaForDim(i, j, d);
            area_ks *= aa;
        }
        return area_ks;
    }

    private long computeAreaForDim(int i, int j, int d) {
        int min_i = os[i].x[d].getLB();
        int max_i = os[i].x[d].getUB() + os[i].l[d];
        long ld = os[j].l[d];
        if (os[j].x[d].getLB() <= min_i) { // j starts before starting of i
            if (os[j].x[d].getUB() + os[j].l[d] <= max_i) { // j ends before ending of i
                int dist = os[j].x[d].getLB() + os[j].l[d] - min_i;
                ld = Math.max(dist, 0);
            } else { // j ends after ending of i
                int d1 = os[j].x[d].getLB() + os[j].l[d] - min_i;
                int d2 = -os[j].x[d].getUB() + max_i;
                d1 = Math.min(d1, max_i - min_i);
                d2 = Math.min(d2, max_i - min_i);
                if (d1 < d2)
                    ld = Math.max(d1, 0);
                else if (d2 > 0) {
                    if (d2 < os[j].l[d])
                        ld = d2;
                } else
                    ld = 0;
            }
        } else if (os[j].x[d].getUB() + os[j].l[d] > max_i) { // j ends after i
            int distance2 = -os[j].x[d].getUB() + os[i].x[d].getLB() + os[i].l[d];
            if (distance2 > 0) {
                if (distance2 < os[j].l[d])
                    ld = distance2;
            } else
                ld = 0;
        }
        return ld;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static class Orthotope {

        final IntVar[] x;
        final int[] l;
        final long al; // area
        boolean modified = false;
        boolean skippable;

        public Orthotope(IntVar[] x, int[] l, boolean sk) {
            this.x = x;
            this.l = l;
            this.al = IntStream.of(l).reduce((i, j) -> i * j).orElse(0);
            this.skippable = sk;
        }

        public boolean isSkippable() {
            return skippable;
        }

        private int dsize(int d) {
            return x[d].getUB() - x[d].getLB() - l[d];
        }

        private void checkSkippable(int[] maxl) {
            //TODO fix issue with skippable
            skippable = true;
            for (int d = 0; d < x.length && skippable; d++) {
                if(dsize(d) <= maxl[d] - 2){
                    skippable = false;
                }
            }
        }

        boolean assignedInAllDimensions() {
            int i = 0;
            while (i < x.length && x[i].isInstantiated()) {
                i++;
            }
            return i == x.length;
        }

        boolean assignedInDimension(int k) {
            return x[k].isInstantiated();
        }

        boolean enumeratedOnDimension(int k) {
            return x[k].hasEnumeratedDomain();
        }

        long area() {
            long a = 1;
            for (int i = 0; i < x.length; i++) {
                a *= x[i].getUB() - x[i].getLB() + l[i];
            }
            return a;
        }

        public boolean mayOverlap(Orthotope o2) {
            boolean overlap = true;
            for (int d = 0; d < x.length && overlap; d++) {
                overlap = mayOverlap(o2, d);
            }
            return overlap;
        }

        public boolean mayOverlap(Orthotope o, int d) {
            return x[d].getLB() < o.x[d].getUB() + o.l[d] &&
                    o.x[d].getLB() < x[d].getUB() + l[d];
        }
    }

    private static class Forbidden {

        private final int[][] f;

        public Forbidden(int k) {
            f = new int[k][2];
        }

        public int min(int i) {
            return f[i][0];
        }

        public int max(int i) {
            return f[i][1];
        }

        public void set(int d, int fmin, int fmax) {
            f[d][0] = fmin;
            f[d][1] = fmax;
        }

        @Override
        public String toString() {
            StringBuilder st = new StringBuilder("FR [");
            for (int d = 0; d < f.length; d++) {
                st.append('(').append(f[d][0]).append(',').append(f[d][1]).append(')');
            }
            st.append("]");
            return st.toString();
        }
    }

}