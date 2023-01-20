/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.flow;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.IntCircularQueue;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/01/2022
 */
public class PropMinCostMaxFlow extends Propagator<IntVar> {
    private final int offset;
    private final int[] starts;
    private final int[] ends;
    private final int[] balances;
    private final int[] weights;
    private final IntVar[] flows;
    private final IntVar cost;
    private final Residual g;
    private final BitSet toCheck;

    public PropMinCostMaxFlow(int[] starts, int[] ends, int[] balances, int[] weights, IntVar[] flows, IntVar cost, int offset) {
        super(ArrayUtils.append(flows, new IntVar[]{cost}), PropagatorPriority.QUADRATIC, true);
        this.offset = offset;
        this.starts = starts;
        this.ends = ends;
        this.balances = balances;
        this.weights = weights;
        this.flows = flows;
        this.cost = cost;
        this.g = new Residual();
        this.toCheck = new BitSet(flows.length);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            toCheck.clear();
            toCheck.set(0, flows.length);
        }
        g.refresh(-1, 0);
        int minCost = minCostFlow(-1, 0);
        if (minCost == -1) {
            toCheck.clear();
            this.fails();
        }
        this.cost.updateLowerBound(minCost, this);

        int maxcost = this.cost.getUB();
        try {
            for (int i = toCheck.nextSetBit(0); i > -1; i = toCheck.nextSetBit(i + 1)) {
                updateFlow4(i, maxcost);
            }
        } finally {
            toCheck.clear();
        }
    }

    private void updateFlow0(int i, int minCost, int maxcost) throws ContradictionException {
        int l = flows[i].getLB();
        int u = flows[i].getUB();
        int m = u;
        g.refresh(i, m);
        int c = minCostFlow(i, m);
        if (c > maxcost || c == -1) {
            while (l <= u) {
                m = (l + u) >>> 1;
                g.refresh(i, m);
                c = minCostFlow(i, m);
                if (c == -1) {
                    u = m - 1;
                } else if (c > maxcost) {
                    u = m - 1;
                } else if (c < maxcost) {
                    l = m + 1;
                } else break;
            }
            flows[i].updateUpperBound(m, this);
        }
    }

    private void updateFlow1(int i, int minCost, int maxcost) throws ContradictionException {
        int c;
        int l;
        do {
            l = flows[i].getLB();
            g.refresh(i, l);
            c = minCostFlow(i, l);
        } while ((c == -1 || c < minCost) && flows[i].updateLowerBound(l + 1, this));
        int u;
        do {
            u = flows[i].getUB();
            g.refresh(i, u);
            c = minCostFlow(i, u);
        } while ((c == -1 || c > maxcost) && flows[i].updateUpperBound(u - 1, this));
    }

    private void updateFlow4(int i, int maxcost) throws ContradictionException {
        int l = flows[i].getLB();
        int u = flows[i].getUB();
        int m = u;
        g.refresh(i, m);
        int c = minCostFlow(i, m);
        if (c > maxcost || c == -1) {
            while (l <= u) {
                m = (l + u) >>> 1;
                g.refresh(i, m);
                c = minCostFlow(i, m);
                if (c == -1) {
                    u = m - 1;
                } else if (c > maxcost) {
                    u = m - 1;
                } else if (c < maxcost) {
                    l = m + 1;
                } else break;
            }
            flows[i].updateUpperBound(m, this);
            if (l == u + 1) {
                u = flows[i].getUB();
                g.refresh(i, u);
                c = minCostFlow(i, u);
                if (c == -1 || c > maxcost) {
                    flows[i].updateUpperBound(u - 1, this);
                }
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < flows.length) {
            toCheck.set(idxVarInProp);
        } else {
            toCheck.set(0, flows.length);
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    ////////////////////////
    private final static class Edge {
        final int id;
        final int from;
        final int to;
        int capacity;
        int flow;
        final int cost;
        final int rId;

        public Edge(int id, int from, int to, int capacity, int cost, int rId) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.cost = cost;
            this.rId = rId;
        }

        @Override
        public String toString() {
            return "E#" + id + ": (" + from + "," + to + ") ca=" + capacity + ", co=" + cost + " -- " + rId;
        }
    }

    private final class Residual {
        final int n;
        final int n2;
        final int m;
        final int s;
        final int t;
        final Edge[] edges;
        final List<Edge>[] adj;
        final int[] d; // distance
        final int[] p; // predecessor
        final int[] b; // balance or demand on each vertex
        final BitSet inqueue;
        final IntCircularQueue queue;
        int D;

        public Residual() {
            this.s = balances.length; // add a super source
            this.t = s + 1; // add super sink
            this.n = balances.length + 2; // nb of nodes
            this.n2 = n - 2;
            this.d = new int[n];
            this.p = new int[n];
            this.inqueue = new BitSet(n);
            this.queue = new IntCircularQueue(n);
            this.adj = new ArrayList[n];
            this.b = new int[n];
            for (int i = 0; i < n; i++) {
                this.adj[i] = new ArrayList<>();
                if (i < n - 2) {
                    if (balances[i] > 0) {
                        b[s] += balances[i];
                    } else if (balances[i] < 0) {
                        b[t] += balances[i];
                    }
                }
            }
            this.m = starts.length + 2 * n2; // 2*(n-2) for extra edges to deal with demand and l(e)
            this.edges = new Edge[m * 2];
            for (int i = 0; i < starts.length; i++) {
                int f = starts[i] - offset;
                int t = ends[i] - offset;
                int cost = weights[i];
                edges[i] = new Edge(i, f, t, 0, cost, i + m);
                this.adj[f].add(edges[i]);
                edges[i + m] = new Edge(i + m, t, f, 0, -cost, i);
                this.adj[t].add(edges[i + m]);
            }
            for (int i = 0, k = starts.length; i < n2; i++, k++) {
                // from super source to i
                edges[k] = new Edge(k, s, i, 0, 0, k + m);
                this.adj[s].add(edges[k]);
                edges[k + m] = new Edge(k + m, i, s, 0, 0, k);
                this.adj[i].add(edges[k + m]);
                // from super i to super sink
                edges[k + n2] = new Edge(k + n2, i, t, 0, 0, k + n2 + m);
                this.adj[i].add(edges[k + n2]);
                edges[k + n2 + m] = new Edge(k + n2 + m, t, i, 0, 0, k + n2);
                this.adj[t].add(edges[k + n2 + m]);
            }
        }

        // https://ocw.tudelft.nl/wp-content/uploads/Algoritmiek_Extensions_to_Max_Flow_-_circulations_01.pdf
        public void refresh(int a, int v) {
            Arrays.fill(b, 0);
            System.arraycopy(balances, 0, b, 0, balances.length);
            for (int i = 0; i < starts.length; i++) {
                int f = starts[i] - offset;
                int t = ends[i] - offset;
                int lowerCap = flows[i].getLB();
                int upperCap = flows[i].getUB();
                if (a == i) {
                    lowerCap = v;
                }
                b[f] -= lowerCap;
                b[t] += lowerCap;
                edges[i].capacity = upperCap - lowerCap;
                edges[i + m].capacity = 0;
                edges[i].flow = edges[i + m].flow = 0;
            }
            // fill super edges, if needed
            int dIn = 0, dOut = 0;
            for (int i = 0, k = starts.length; i < n2; i++, k++) {
                edges[k].capacity = edges[k + m].capacity = edges[k + n2].capacity = edges[k + n2 + m].capacity = 0;
                edges[k].flow = edges[k + m].flow = edges[k + n2].flow = edges[k + n2 + m].flow = 0;
                if (b[i] > 0) {
                    int b_ = b[i];
                    b[edges[k].from] += b_;
                    b[edges[k].to] -= b_;
                    edges[k].capacity = b_;
                    dIn += b_;
                } else if (b[i] < 0) {
                    int b_ = b[i];
                    b[edges[k + n2].from] -= b_;
                    b[edges[k + n2].to] += b_;
                    edges[k + n2].capacity = -b_;
                    dOut -= b_;
                }
            }
            assert dIn == dOut;
            D = dIn;
        }
    }

    private void shortest_paths(int s) {
        Arrays.fill(g.d, Integer.MAX_VALUE);
        Arrays.fill(g.p, -1);
        g.d[s] = 0;
        g.inqueue.clear();
        g.queue.clear();
        g.queue.addLast(s);
        while (!g.queue.isEmpty()) {
            int u = g.queue.pollFirst();
            g.inqueue.clear(u);
            for (int i = 0; i < g.adj[u].size(); i++) {
                Edge e = g.adj[u].get(i);
                int v = e.to;
                if (e.capacity > 0 && g.d[v] > g.d[u] + e.cost) {
                    g.d[v] = g.d[u] + e.cost;
                    g.p[v] = e.id; // not the predecessor but the arc
                    if (!g.inqueue.get(v)) {
                        g.inqueue.set(v);
                        g.queue.addLast(v);
                    }
                }
            }
        }
    }

    private int minCostFlow(int a, int v) {
        int fl = 0;
        int cost = 0;

        while (fl < g.D) {
            shortest_paths(g.s);
            if (g.d[g.t] == Integer.MAX_VALUE)
                break;
            // find max flow on that path
            int f = g.D - fl;
            int cur = g.p[g.t];
            Edge e;
            while (cur != -1) {
                e = g.edges[cur];
                f = Math.min(f, e.capacity);
                cur = g.p[e.from];
            }
            // apply flow
            fl += f;
            cost += f * g.d[g.t];
            cur = g.p[g.t];
            while (cur != -1) {
                e = g.edges[cur];
                e.capacity -= f;
                g.edges[e.rId].capacity += f;
                cur = g.p[e.from];
            }
        }
        if (fl < g.D) {
            return -1;
        }
        for (int i = 0; i < starts.length; i++) {
            if (a == i) {
                cost += v * g.edges[i].cost;
            } else {
                cost += flows[i].getLB() * g.edges[i].cost;
            }
        }
        return cost;
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }
}
