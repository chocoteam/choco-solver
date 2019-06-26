/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.vilim2009;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Data structure for the cumulative constraint, described in the following thesis and paper :
 * Vilim, P.: Global constraints in scheduling. Ph.D. thesis, Charles University in Prague (2007). http://vilim.eu/petr/disertace.pdf
 * Vilim, P.: Edge finding filtering algorithm for discrete cumulative resources in O(k n log(n)). In: Proceedings of the 15th International Conference on Principles and Practice of Constraint Programming (CP 2009), pp. 802-816 (2009). https://doi.org/10.1007/978-3-642-04244-7_62
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class ThetaLambdaTree {
    static final int MINF = -Integer.MAX_VALUE/10;
    private static int ID = -1;

    class Node {
        int taskIdx;
        Node parent, left, right, responsibleELambda, responsibleEnvLambda;
        int e, env, envC; // Theta parameters
        int eLambda, envLambda; // Lambda parameters
        boolean inTheta, inLambda;

        private Node(Node left, Node right) {
            taskIdx = ID--;
            inTheta = false;
            inLambda = false;
            this.left = left;
            this.right = right;
            left.parent = this;
            right.parent = this;
            this.update();
        }

        private Node(int taskIdx) {
            this.taskIdx = taskIdx;
            inTheta = false;
            inLambda = false;
            init();
        }

        void init() {
            e = (inTheta ? tasks[taskIdx].getDuration().getLB()*heights[taskIdx].getLB() : 0);
            eLambda = (inLambda ? tasks[taskIdx].getDuration().getLB()*heights[taskIdx].getLB() : MINF);
            env = (inTheta ? capacity.getUB()*tasks[taskIdx].getStart().getLB()+e : MINF);
            envC = (inTheta ? (capacity.getUB()-c)*tasks[taskIdx].getStart().getLB()+e : MINF);
            envLambda = (inLambda ? capacity.getUB()*tasks[taskIdx].getStart().getLB()+ eLambda : MINF);
            responsibleELambda = this;
            responsibleEnvLambda = this;
        }

        boolean isLeaf() {
            return left==null && right==null;
        }

//        void update() {
//            if(!isLeaf()) {
//                updateValues();
//            } else {
//                init();
//            }
//            if(parent != null) {
//                parent.update();
//            }
//        }

        void update() {
            init();
            Node current = this.parent;
            while(current != null) {
                current.updateValues();
                current = current.parent;
            }
        }

        void updateDown() {
            if(!isLeaf()) {
                if(left != null) {
                    left.updateDown();
                }
                if(right != null) {
                    right.updateDown();
                }
                updateValues();
            } else {
                init();
            }
        }

        void updateValues() {
            e = left.e + right.e;
            if(plus(left.eLambda, right.e) > plus(left.e, right.eLambda)) {
                eLambda = plus(left.eLambda, right.e);
                responsibleELambda = left.responsibleELambda;
            } else {
                eLambda = plus(left.e, right.eLambda);
                responsibleELambda = right.responsibleELambda;
            }
            env = Math.max(plus(left.env, right.e), right.env);
            envC = Math.max(plus(left.envC, right.e), right.envC);

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
        }

        @Override
        public String toString() {
            if(taskIdx >= 0) {
                return "Node("+taskIdx+") in "+(inTheta?"Theta": "")+(inLambda?":Lambda" : "");
            } else {
                return "Node("+left.toString()+","+right.toString()+")";
            }
        }

        public String toFullString() {
            String s = "";
            s += "taskIdx: "+taskIdx+"\n";
            s += "e: "+e+"\n";
            s += "eLambda: "+ eLambda +"\n";
            s += "env: "+env+"\n";
            s += "envC: "+envC+"\n";
            s += "envLambda: "+ envLambda +"\n";
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
    }

    private Task[] tasks;
    private IntVar[] heights;
    private IntVar capacity;
    private int c;

    Node[] nodes;
    int[] correspondingNodes;
    Node root;
    private Integer[] indexes;

    public ThetaLambdaTree(Task[] tasks, IntVar[] heights, IntVar capacity) {
        this.tasks = tasks;
        this.heights = heights;
        this.capacity = capacity;

        nodes = new Node[tasks.length];
        indexes = new Integer[tasks.length];
        for(int i = 0; i<tasks.length; i++) {
            nodes[i] = new Node(i);
            indexes[i] = i;
        }
        correspondingNodes = new int[tasks.length];

        buildTree();
    }

    public void buildTree() {
        LinkedList<Node> current = new LinkedList<>();
        LinkedList<Node> next = new LinkedList<>();
        for(int i = 0; i<nodes.length; i++) {
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

    private int compare(int a, int b) {
        if(tasks[a].getStart().getLB() == tasks[b].getStart().getLB()) {
            if(tasks[a].getEnd().getUB() == tasks[b].getEnd().getUB()) {
                return Integer.compare(a, b);
            }
            return Integer.compare(tasks[a].getEnd().getUB(), tasks[b].getEnd().getUB());
        }
        return Integer.compare(tasks[a].getStart().getLB(), tasks[b].getStart().getLB());
    }

    public void initializeTree(boolean full) {
        Arrays.sort(indexes, this::compare);
        for(int i = 0; i<nodes.length; i++) {
            nodes[i].taskIdx = indexes[i];
            nodes[i].inTheta = full;
            nodes[i].inLambda = false;
            correspondingNodes[nodes[i].taskIdx] = i;
        }
        root.updateDown();
    }

    public void setC(int c) {
        this.c = c;
    }

    private int getIdxInNodes(int taskIdx) {
        return correspondingNodes[taskIdx];
    }

    public void addToTheta(int taskIdx) {
        int i = getIdxInNodes(taskIdx);
        if(!nodes[i].inTheta) {
            nodes[i].inTheta = true;
            nodes[i].update();
        }
    }

    public void removeFromTheta(int taskIdx) {
        int i = getIdxInNodes(taskIdx);
        if(nodes[i].inTheta) {
            nodes[i].inTheta = false;
            nodes[i].update();
        }
    }

    public void addToLambda(int taskIdx) {
        int i = getIdxInNodes(taskIdx);
        if(!nodes[i].inLambda) {
            nodes[i].inLambda = true;
            nodes[i].update();
        }
    }

    public void removeFromLambda(int taskIdx) {
        int i = getIdxInNodes(taskIdx);
        if(nodes[i].inLambda) {
            nodes[i].inLambda = false;
            nodes[i].update();
        }
    }

    public void addToLambdaAndRemoveFromTheta(int taskIdx) {
        int i = getIdxInNodes(taskIdx);
        if(nodes[i].inTheta) {
            nodes[i].inLambda = true;
            nodes[i].inTheta = false;
            nodes[i].update();
        }
    }

    public int envC() {
        return root.envC;
    }

    public int envThetaLambda() {
        return root.envLambda;
    }

    public int getResponsible() {
        return root.responsibleEnvLambda.taskIdx;
    }

    Node maxest(int j, int c) {
        Node v = root;
        int E = 0;
        while(!v.isLeaf()) { // while v is not a leaf
            if(plus(v.right.envC, E) > (capacity.getUB()-c)*tasks[j].getEnd().getUB()) {
                v = v.right;
            } else {
                E += v.right.e;
                v = v.left;
            }
        }
        return v;
    }

    public int computeEnvJC(int j, int c) {
        Node v = maxest(j,c);
        int eAlpha = v.e;
        int envAlpha = v.env;
        int eBeta = 0;
        while(v.parent != null) { // while v is not root
            if(v.taskIdx == v.parent.left.taskIdx) { // if v is left
                eBeta += v.parent.right.e;
            } else {
                envAlpha = Math.max(plus(v.parent.left.env, eAlpha), envAlpha);
                eAlpha += v.parent.left.e;
            }
            v = v.parent;
        }

        return plus(envAlpha, eBeta);
    }

    public int plus(int a, int b) {
        if(a==MINF || b==MINF) {
            return MINF;
        }
        return a+b;
    }

}
