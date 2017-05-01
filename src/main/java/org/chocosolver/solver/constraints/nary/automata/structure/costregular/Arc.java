/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.structure.costregular;

import org.chocosolver.memory.structure.IndexedObject;
import org.chocosolver.solver.constraints.nary.automata.structure.Node;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Oct 30, 2009
 * Time: 3:48:11 PM
 */
public class Arc extends DefaultWeightedEdge implements IndexedObject {

    public int id;
    public Node orig;
    public Node dest;
    public int value;
    public double cost;


    public Arc(Node orig, Node dest, int value, int id, double cost) {
        this.id = id;
        this.orig = orig;
        this.dest = dest;
        this.value = value;
        this.cost = cost;
    }

    public Arc(Node orig, Node dest, int value) {
        this(orig, dest, value, Integer.MIN_VALUE, Double.POSITIVE_INFINITY);
    }

    public double getWeight() {
        return this.cost;
    }


    public String toString() {
        return value + "";
    }

    @Override
    public int getObjectIdx() {
        return orig.state;
    }


    public static class ArcFacroty implements EdgeFactory<Node, Arc> {

        public Arc createEdge(Node node, Node node1) {
            return new Arc(node, node1, 0, 0, 0.0);
        }
    }

    @Override
    public Arc clone() {
        Arc arc = (Arc) super.clone();
        arc.id = id;
        try {
            arc.orig = orig.clone();
            arc.dest = dest.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        arc.value = value;
        arc.cost = cost;
        return arc;
    }
}
