/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl.lazyness;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.impl.IntVarLazyLit;

import java.util.ArrayList;

/**
 * will use a strong chaining:
 * when a bound is modified, the channeling is done with all known values between the previous and the new bound.
 * It provides stronger reasons, which are slower to compute but more informative.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/09/2024
 */
public class StrongBound implements ILazyBound {

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

    public StrongBound(Model model, int lb, int ub) {
        ld = new ArrayList<>();
        ld.add(new Node(0, lb - 1, -1, 1));
        ld.add(new Node(1, ub, 0, -1));
        li = model.getEnvironment().makeInt(0);
        hi = model.getEnvironment().makeInt(1);
    }

    @Override
    public int currentMinVar() {
        return ld.get(li.get()).var;
    }

    @Override
    public int currentMaxVar() {
        return ld.get(hi.get()).var;
    }

    @Override
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

    @Override
    public void channelMin(int value, MiniSat sat, Reason r) {
        int ni;
        for (ni = ld.get(li.get()).next; ld.get(ni).val < value; ni = ld.get(ni).next) {
            sat.cEnqueue(MiniSat.makeLiteral(ld.get(ni).var, true), r);
        }
        assert (ld.get(ni).val == value);
        li.set(ni);
    }

    @Override
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
        return node;
    }

    private int getNi(int prev) {
        int ni = li.get();
        while (ld.get(ni).val < prev) {
            ni = ld.get(ni).next;
            assert (0 <= ni && ni < ld.size());
        }
        return ni;
    }


    private int getLitNode() {
        ld.add(new Node(-1, -1, -1, -1));
        return ld.size() - 1;
    }
}
