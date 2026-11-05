/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl.lazyness;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.impl.IntVarLazyLit;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Links a CP bounded variable to the SAT variables of its bound literals, created on demand.
 * It uses a strong chaining:
 * when a bound is modified, the channeling is done with all known values between the previous and the new bound.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/09/2024
 */
public class StrongBound {

    private static class Node {
        int var;
        int val;
        int prev;
        int next;

        public Node(int var, int val, int prev, int next) {
            this.var = var;
            this.val = val;
            this.prev = prev;
            this.next = next;
        }
    }

    // store the declared sat variables, from the beginning
    final ArrayList<Node> ld;
    // pointer to the current position of the lower bound SAT variable
    final IStateInt li;
    // pointer to the current position of the upper bound SAT variable
    final IStateInt hi;
    // values of the nodes, in increasing order (an index on the chain)
    private int[] sortedVals = new int[16];
    // node indices, in the same order as sortedVals
    private int[] sortedNodes = new int[16];
    // number of nodes in the index
    private int size;
    // position in the index of the node returned by the last call to getNi
    private int pos;

    public StrongBound(Model model, int lb, int ub) {
        ld = new ArrayList<>();
        ld.add(new Node(0, lb - 1, -1, 1));
        ld.add(new Node(1, ub, 0, -1));
        li = model.getEnvironment().makeInt(0);
        hi = model.getEnvironment().makeInt(1);
        sortedVals[0] = lb - 1;
        sortedNodes[0] = 0;
        sortedVals[1] = ub;
        sortedNodes[1] = 1;
        size = 2;
    }

    /**
     * @return the SAT variable that represents the current lower bound
     */
    public int currentMinVar() {
        return ld.get(li.get()).var;
    }

    /**
     * @return the SAT variable that represents the current upper bound
     */
    public int currentMaxVar() {
        return ld.get(hi.get()).var;
    }

    /**
     * Return or create the SAT variable that represents the value of the variable
     *
     * @param value the value to represent
     * @param cvar  the CP variable to link
     * @param sat   the SAT solver
     * @return the SAT variable that represents the value
     */
    public int getSATVar(int value, IntVarLazyLit cvar, MiniSat sat) {
        int ni = getNi(value);
        if (ld.get(ni).val == value) {
            return ld.get(ni).var;
        } else {
            // create new var and insert before ni
            Node node = getNode(value, ni, cvar, sat);
            return node.var;
        }
    }

    /**
     * Channel the lower bound of the variable to the SAT solver
     *
     * @param value the new lower bound
     * @param sat   the SAT solver
     * @param r     the reason why the bound has been modified
     */
    public void channelMin(int value, MiniSat sat, Reason r) {
        int ni;
        for (ni = ld.get(li.get()).next; ld.get(ni).val < value; ni = ld.get(ni).next) {
            sat.cEnqueue(MiniSat.makeLiteral(ld.get(ni).var, true), r);
        }
        assert (ld.get(ni).val == value);
        li.set(ni);
    }

    /**
     * Channel the upper bound of the variable to the SAT solver
     *
     * @param value the new upper bound
     * @param sat   the SAT solver
     * @param r     the reason why the bound has been modified
     */
    public void channelMax(int value, MiniSat sat, Reason r) {
        int ni;
        for (ni = ld.get(hi.get()).prev; ld.get(ni).val > value; ni = ld.get(ni).prev) {
            sat.cEnqueue(MiniSat.makeLiteral(ld.get(ni).var, false), r);
        }
        assert (ld.get(ni).val == value);
        hi.set(ni);
    }

    private Node getNode(int value, int ni, IntVarLazyLit cvar, MiniSat sat) {
        int mi = getLitNode();
        Node node = ld.get(mi);
        node.var = sat.newVariable(new MiniSat.ChannelInfo(cvar, 1, 1, value)); // todo recycle lits
        node.val = value;
        node.next = ni;
        node.prev = ld.get(ni).prev;
        ld.get(ni).prev = mi;
        ld.get(node.prev).next = mi;
        // insert the new node in the index, before the one of ni
        if (size == sortedVals.length) {
            sortedVals = Arrays.copyOf(sortedVals, size * 2);
            sortedNodes = Arrays.copyOf(sortedNodes, size * 2);
        }
        System.arraycopy(sortedVals, pos, sortedVals, pos + 1, size - pos);
        System.arraycopy(sortedNodes, pos, sortedNodes, pos + 1, size - pos);
        sortedVals[pos] = value;
        sortedNodes[pos] = mi;
        size++;
        return node;
    }

    /**
     * Returns the index of the first node whose value is greater than or equal to {@code prev}.
     * The whole chain is sorted by value and nodes are never removed, so this node is found by a
     * binary search on {@link #sortedVals}. Its position in the index is kept in {@link #pos},
     * for a possible insertion by {@link #getNode}.
     */
    private int getNi(int prev) {
        assert ld.get(li.get()).val <= prev && prev < ld.get(hi.get()).val;
        int lo = 0;
        int up = size - 1;
        while (lo < up) {
            int mid = (lo + up) >>> 1;
            if (sortedVals[mid] < prev) {
                lo = mid + 1;
            } else {
                up = mid;
            }
        }
        pos = lo;
        return sortedNodes[lo];
    }

    private int getLitNode() {
        ld.add(new Node(-1, -1, -1, -1));
        return ld.size() - 1;
    }
}
