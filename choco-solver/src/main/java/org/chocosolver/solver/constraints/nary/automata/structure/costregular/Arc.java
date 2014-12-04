/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

    public final void setId(int id) {
        this.id = id;
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
