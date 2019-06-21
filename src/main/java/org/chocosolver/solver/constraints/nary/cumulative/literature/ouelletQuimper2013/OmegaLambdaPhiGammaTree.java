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

import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Data structure for the cumulative constraint, described in the following paper :
 * Ouellet, P., Quimper, C.-G.: Time-table-extended-edge-finding for the cumulative constraint. In: Proceedings of the 19th International Conference on Principles and Practice of Constraint Programming (CP 2013), pp. 562-577 (2013). https://doi.org/10.1007/978-3-642-40627-0_42
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class OmegaLambdaPhiGammaTree {
    static final int MINF = -Integer.MAX_VALUE/10;
    private static int ID = -1;

    class Node implements Comparable<Node> {
        final int taskIdx;
        Node parent, left, right;
        int e, env, envH; // Omega parameters
        Node responsibleEnvH;
        int eLambda, envLambda, exLambda; // Lambda parameters
        Node responsibleELambda, responsibleEnvLambda, responsibleEnvXLambda;
        int ePhi, envPhi, exPhi; // Phi parameters
        Node responsibleEPhi, responsibleEnvPhi, responsibleEnvXPhi;

        int envXLambda, envXPhi;
        Node responsibleExLambda, responsibleExPhi;

        boolean inOmega, inLambda, inPhi;

        private Node(Node left, Node right) {
            taskIdx = ID--;
            inOmega = false;
            inLambda = false;
            this.left = left;
            this.right = right;
            left.parent = this;
            right.parent = this;
            this.update();
        }

        private Node(int taskIdx) {
            this.taskIdx = taskIdx;
            inOmega = false;
            inLambda = false;
            init();
        }

        void init() {
            int enerPhi = 0;
            for(int k = 0; k<phi.size(); k++) {
                int i = phi.getQuick(k);
                enerPhi += decomposedTasks[i].p*decomposedTasks[i].h;
            }

            e = (inOmega ? decomposedTasks[taskIdx].p*decomposedTasks[taskIdx].h : 0);
            env = (inOmega ? capacity*decomposedTasks[taskIdx].est+e : MINF);
            envH = (inOmega ? (capacity- h)*decomposedTasks[taskIdx].est+e : MINF);
            eLambda = (inLambda ? decomposedTasks[taskIdx].p*decomposedTasks[taskIdx].h : MINF);
            envLambda = (inLambda ? capacity*decomposedTasks[taskIdx].est+ eLambda : MINF);
            exLambda = (inLambda ? h*(decomposedTasks[taskIdx].est+decomposedTasks[taskIdx].p) : MINF);
            ePhi = (inPhi ? h*(hor-decomposedTasks[taskIdx].est) : MINF);
            envPhi = (inPhi ? capacity*decomposedTasks[taskIdx].est+enerPhi : MINF);
            exPhi = (inPhi ? decomposedTasks[taskIdx].h*hor : MINF);
            envXLambda = MINF;
            envXPhi = MINF;
            responsibleEnvH = this;
            responsibleELambda = this;
            responsibleEnvLambda = this;
            responsibleEnvXLambda = this;
            responsibleEPhi = this;
            responsibleEnvPhi = this;
            responsibleEnvXPhi = this;
            responsibleExLambda = this;
            responsibleExPhi = this;
        }

        boolean isLeaf() {
            return left==null && right==null && taskIdx>=0;
        }

        void update() {
            if(!isLeaf()) {
                updateValues();
            } else {
                init();
            }
            if(parent != null) {
                parent.update();
            }
        }

        void updateValues() {
            if(left==null || right==null) { // for completing nodes
                return;
            }

            e = left.e + right.e;
            env = Math.max(plus(left.env, right.e), right.env);
            if(plus(left.envH, right.e) > right.envH) {
                envH = plus(left.envH, right.e);
                responsibleEnvH = left.responsibleEnvH;
            } else {
                envH = right.envH;
                responsibleEnvH = right.responsibleEnvH;
            }

            if(plus(left.eLambda, right.e) > plus(left.e, right.eLambda)) {
                eLambda = plus(left.eLambda, right.e);
                responsibleELambda = left.responsibleELambda;
            } else {
                eLambda = plus(left.e, right.eLambda);
                responsibleELambda = right.responsibleELambda;
            }
            int A = plus(left.envLambda, right.e);
            int B = plus(left.env, right.eLambda);
            int m = Math.max(Math.max(A, B), right.envLambda);
            if(m == A) {
                envLambda = A;
                responsibleEnvLambda = left.responsibleEnvLambda;
            } else if(m == B) {
                envLambda = B;
                responsibleEnvLambda = right.responsibleELambda;
            } else {
                envLambda = right.envLambda;
                responsibleEnvLambda = right.responsibleEnvLambda;
            }
            if(left.exLambda > right.exLambda) {
                exLambda = left.exLambda;
                responsibleExLambda = left.responsibleExLambda;
            } else {
                exLambda = right.exLambda;
                responsibleExLambda = right.responsibleExLambda;
            }

            if(plus(left.ePhi, right.e) > plus(left.e, right.ePhi)) {
                ePhi = plus(left.ePhi, right.e);
                responsibleEPhi = left.responsibleEPhi;
            } else {
                ePhi = plus(left.e, right.ePhi);
                responsibleEPhi = right.responsibleEPhi;
            }
            int C = plus(left.envPhi, right.e);
            int D = plus(left.env, right.ePhi);
            m = Math.max(Math.max(C, D), right.envPhi);
            if(m == C) {
                envPhi = C;
                responsibleEnvPhi = left.responsibleEnvPhi;
            } else if(m == D) {
                envPhi = D;
                responsibleEnvPhi = right.responsibleEPhi;
            } else {
                envPhi = right.envPhi;
                responsibleEnvPhi = right.responsibleEnvPhi;
            }
            if(left.exPhi > right.exPhi) {
                exPhi = left.exPhi;
                responsibleExPhi = left.responsibleExPhi;
            } else {
                exPhi = right.exPhi;
                responsibleExPhi = right.responsibleExPhi;
            }

            int E = plus(left.envXLambda, right.e);
            int F = plus(left.exLambda, right.envH);
            m = Math.max(Math.max(E, F), right.envXLambda);
            if(m == E) {
                envXLambda = E;
                responsibleEnvXLambda = left.responsibleEnvXLambda;
            } else if(m == F) {
                envXLambda = F;
                responsibleEnvXLambda = left.responsibleExLambda;
            } else {
                envXLambda = right.envXLambda;
                responsibleEnvXLambda = right.responsibleEnvXLambda;
            }

            int G = plus(left.envXPhi, right.e);
            int H = plus(left.exPhi, right.envH);
            m = Math.max(Math.max(G, H), right.envXPhi);
            if(m == G) {
                envXPhi = G;
                responsibleEnvXPhi = left.responsibleEnvXPhi;
            } else if(m == H) {
                envXPhi = H;
                responsibleEnvXPhi = left.responsibleExPhi;
            } else {
                envXPhi = right.envXPhi;
                responsibleEnvXPhi = right.responsibleEnvXPhi;
            }
        }

        @Override
        public String toString() {
            if(taskIdx >= 0) {
                return "Node("+taskIdx+") in "+(inOmega ?"Omega": "")+(inLambda?":Lambda" : "")+(inPhi?":Phi" : "");
            } else {
                return "Node("+left.toString()+","+right.toString()+")";
            }
        }

        public String toFullString() {
            String s = "";
            s += "taskIdx: "+taskIdx+"\n";
            s += "e: "+e+"\n";
            s += "env: "+env+"\n";
            s += "envH: "+envH+"\n";
            s += "eLambda: "+ eLambda +"\n";
            s += "envLambda: "+ envLambda +"\n";
            s += "exLambda: "+ exLambda +"\n";
            s += "envXLambda: "+ envXLambda +"\n";
            s += "ePhi: "+ ePhi +"\n";
            s += "envPhi: "+ envPhi +"\n";
            s += "exPhi: "+ exPhi +"\n";
            s += "envXPhi: "+ envXPhi +"\n";
            s += "left: "+(left==null? "null" : left.taskIdx)+"\n";
            s += "right: "+(right==null? "null" : right.taskIdx)+"\n";
            return s;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof Node) {
                return ((Node)o).taskIdx == taskIdx;
            }
            return false;
        }

        @Override
        public int compareTo(Node node) {
            if(decomposedTasks[taskIdx].est == decomposedTasks[node.taskIdx].est) {
                if(decomposedTasks[taskIdx].lct == decomposedTasks[node.taskIdx].lct) {
                    return Integer.compare(taskIdx, node.taskIdx);
                }
                return Integer.compare(decomposedTasks[taskIdx].lct, decomposedTasks[node.taskIdx].lct);
            }
            return Integer.compare(decomposedTasks[taskIdx].est, decomposedTasks[node.taskIdx].est);
        }
    }

    private PropCumulative.DecomposedTask[] decomposedTasks;
    private int capacity;
    private int h, hor;

    Node[] nodes;
    Node[] leaves;
    Node root;
    private LinkedList<Node> current, next;
    TIntArrayList lambda, phi, delta, changes;

    public OmegaLambdaPhiGammaTree(PropCumulative.DecomposedTask[] decomposedTasks, int capacity) {
        this.decomposedTasks = decomposedTasks;
        this.capacity = capacity;

        lambda = new TIntArrayList(decomposedTasks.length);
        phi = new TIntArrayList(decomposedTasks.length);
        delta = new TIntArrayList(decomposedTasks.length);
        changes = new TIntArrayList(decomposedTasks.length);

        leaves = new Node[decomposedTasks.length];
        nodes = new Node[decomposedTasks.length];
        for(int i = 0; i<decomposedTasks.length; i++) {
            leaves[i] = new Node(i);
            nodes[i] = leaves[i];
        }

        current = new LinkedList<>();
        next = new LinkedList<>();
    }

    public void buildTree(boolean full, int h) {
        phi.clear(decomposedTasks.length);
        lambda.clear(decomposedTasks.length);
        this.h = h;

        Arrays.sort(nodes);
        current.clear();
        for(int i = 0; i<nodes.length; i++) {
            nodes[i].inOmega = full;
            nodes[i].inLambda = false;
            nodes[i].inPhi = false;
            nodes[i].init();
            current.addLast(nodes[i]);
        }

        while(current.size() != 1) {
            while(current.size() >= 2) {
                Node left = current.removeFirst();
                Node right = current.removeFirst();
                next.addLast(new Node(left, right));
            }
            if(current.size() == 1) { // if current's size was odd
                next.addLast(new Node(new Node(ID--), current.removeFirst()));
            }
            while(!next.isEmpty()) {
                current.addLast(next.removeFirst());
            }
        }
        root = current.removeFirst();
    }

    public void setH(int h) {
        this.h = h;
    }

    public void setHor(int hor) {
        this.hor = hor;
    }

    public void addToOmega(int i) {
        if(!leaves[i].inOmega) {
            leaves[i].inOmega = true;
            leaves[i].update();
        }
    }

    public void removeFromOmega(int i) {
        if(leaves[i].inOmega) {
            leaves[i].inOmega = false;
            leaves[i].update();
        }
    }

    public void addToLambda(int i) {
        if(!leaves[i].inLambda) {
            leaves[i].inLambda = true;
            lambda.add(i);
            leaves[i].update();
        }
    }

    public void removeFromLambda(int i) {
        if(leaves[i].inLambda) {
            leaves[i].inLambda = false;
            lambda.remove(i);
            leaves[i].update();
        }
    }

    public void addToPhi(int i) {
        if(!leaves[i].inPhi) {
            leaves[i].inPhi = true;
            phi.add(i);
            leaves[i].update();
        }
    }

    public void removeFromPhi(int i) {
        if(leaves[i].inPhi) {
            leaves[i].inPhi = false;
            phi.remove(i);
            leaves[i].update();
        }
    }

    public void updateLambdaPhi(int j) {
        changes.clear(decomposedTasks.length);
        delta.clear(decomposedTasks.length);
        for(int k = 0; k<lambda.size(); k++) {
            int i = lambda.getQuick(k);
            if(decomposedTasks[i].est+decomposedTasks[i].p >= decomposedTasks[j].lct) {
                delta.add(i);
                leaves[i].inLambda = false;
            }
        }
        lambda.removeAll(delta);
        changes.addAll(delta);
        for(int k = 0; k<delta.size(); k++) {
            int i = delta.getQuick(k);
            if(decomposedTasks[i].est < decomposedTasks[j].lct) {
                leaves[i].inPhi = true;
                phi.add(i);
            }
        }
        delta.clear();
        delta.addAll(phi);
        for(int k = 0; k<delta.size(); k++) {
            int i = delta.getQuick(k);
            if(decomposedTasks[i].est < decomposedTasks[j].lct) {
                leaves[i].inPhi = false;
                phi.remove(i);
                if(!changes.contains(i)) {
                    changes.add(i);
                }
            }
        }
        upLambdaPhi();
    }

    private void upLambdaPhi() {
        current.clear();
        next.clear();
        for(int k = 0; k<changes.size(); k++) {
            leaves[changes.getQuick(k)].init();
            current.add(leaves[changes.getQuick(k)].parent);
        }
        while(!current.isEmpty()) {
            while(!current.isEmpty()) {
                Node node = current.removeFirst();
                node.updateValues();
                if(node.parent != null && !next.contains(node.parent)) {
                    next.add(node.parent);
                }
            }
            current.addAll(next);
            next.clear();
        }
    }

    public static int plus(int a, int b) {
        if(a==MINF || b==MINF) {
            return MINF;
        }
        return a+b;
    }

}
