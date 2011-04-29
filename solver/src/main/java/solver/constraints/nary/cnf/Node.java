/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.nary.cnf;

import choco.kernel.common.util.tools.ArrayUtils;
import solver.variables.BoolVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 nov. 2010
 */
public final class Node extends ALogicTree {


    Operator operator;

    ALogicTree[] children;

    BoolVar[] varsAsArray;

    protected Node(Operator operator, Type type, ALogicTree... children) {
        super(type);
        this.operator = operator;
        if (children == null) {
            this.children = new ALogicTree[0];
        } else {
            this.children = children;
        }
    }


    public static Node and(ALogicTree... children) {
        return new Node(Operator.AND, Type.POSITIVE, children);
    }

    public static Node ifOnlyIf(ALogicTree a, ALogicTree b) {
        return and(implies(a, b), implies(b, a));
    }

    public static Node ifThenElse(ALogicTree a, ALogicTree b, ALogicTree c) {
        try {
            ALogicTree na = a.clone();
            na.type = Type.flip(a.type);
            return or(and(a, b), and(na, c));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Node implies(ALogicTree a, ALogicTree b) {
        try {
            ALogicTree na = a.clone();
            na.type = Type.flip(a.type);
            return or(na, b);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Node reified(Literal b, ALogicTree tree) {
        Literal nb = null;
        ALogicTree ntree = null;
        try {
            nb = b.clone();
            nb.type = Type.flip(b.type);
            ntree = tree.clone();
            ntree.type = Type.flip(tree.type);

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return or(and(b, tree), and(nb, ntree));
    }

    public static Node or(ALogicTree... children) {
        return new Node(Operator.OR, Type.POSITIVE, children);
    }

    public static Node nand(ALogicTree... children) {
        return new Node(Operator.AND, Type.NEGATIVE, children);
    }

    public static Node nor(ALogicTree... children) {
        return new Node(Operator.OR, Type.NEGATIVE, children);
    }

    public static Node xor(ALogicTree a, ALogicTree b) {
        try {
            ALogicTree na = a.clone();
            ALogicTree nb = b.clone();
            na.type = Type.flip(a.type);
            nb.type = Type.flip(b.type);
            return or(and(a, nb), and(b, na));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    boolean is(Operator op) {
        return op.equals(operator);
    }

    @Override
    boolean isNot() {
        return type.equals(Type.NEGATIVE);
    }

    @Override
    boolean isLit() {
        return false;
    }

    @Override
    int getNbChildren() {
        return children.length;
    }

    @Override
    boolean hasOrChild() {
        for (int i = 0; i < children.length; i++) {
            if (children[i].is(Operator.OR)) {
                return true;
            }
        }
        return false;
    }

    @Override
    boolean hasAndChild() {
        for (int i = 0; i < children.length; i++) {
            if (children[i].is(Operator.AND)) {
                return true;
            }
        }
        return false;
    }

    @Override
    void addChild(ALogicTree child) {
        ALogicTree[] tmp = children;
        children = new ALogicTree[tmp.length + 1];
        System.arraycopy(tmp, 0, children, 0, tmp.length);
        children[tmp.length] = child;
        varsAsArray = null; // force recomputation of varsArray
    }

    @Override
    void removeChild(ALogicTree child) {
        int i = 0;
        for (; i < children.length && children[i] != child; i++) {
        }
        if (i == children.length) return;
        ALogicTree[] tmp = children;
        children = new ALogicTree[tmp.length - 1];
        System.arraycopy(tmp, 0, children, 0, i);
        System.arraycopy(tmp, i + 1, children, i, tmp.length - i - 1);
        varsAsArray = null; // force recomputation of varsArray
    }

    @Override
    ALogicTree[] getChildren() {
        return children;
    }

    @Override
    ALogicTree getAndChild() {
        for (int i = 0; i < children.length; i++) {
            if (children[i].is(Operator.AND)) {
                return children[i];
            }
        }
        return null;
    }

    @Override
    ALogicTree getChildBut(ALogicTree child) {
        for (int i = 0; i < children.length; i++) {
            if (children[i] != child) {
                return children[i];
            }
        }
        return null;
    }

    @Override
    void flip() {
        type = Type.flip(type);
        operator = Operator.flip(operator);
        for (int i = 0; i < children.length; i++) {
            children[i].deny();
        }
    }


    @Override
    void deny() {
        operator = Operator.flip(operator);

        for (int i = 0; i < children.length; i++) {
            children[i].deny();
        }
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append('(');
//        st.append(Type.POSITIVE.equals(type) ? "(" : "not(");

        String op = (Type.POSITIVE.equals(type) ? "" : "n") + (Operator.AND.equals(operator) ? "and " : "or ");
        for (int i = 0; i < children.length; i++) {
            ALogicTree child = children[i];
            st.append(child.toString()).append(" ")
                    .append(op);
        }
        st.replace(st.length() - (op.length() + 1), st.length(), "");
        st.append(')');
        return st.toString();
    }


    @Override
    public Node clone() throws CloneNotSupportedException {
        Node node = (Node) super.clone();
        node.type = this.type;
        node.operator = this.operator;
        node.children = new ALogicTree[this.children.length];
        for (int c = 0; c < children.length; c++) {
            node.children[c] = this.children[c].clone();
        }
        return node;
    }

    @Override
    public BoolVar[] flattenBoolVar() {
        if (varsAsArray == null) {
            buildVarsArray();
        }
        return varsAsArray;
    }

    private void buildVarsArray() {
        final BoolVar[][] childrenVars = new BoolVar[children.length][];
        for (int i = 0; i < children.length; i++) {
            childrenVars[i] = children[i].flattenBoolVar();
        }
        varsAsArray = ArrayUtils.flatten(childrenVars);
    }

    @Override
    public int getNbPositiveLiterals() {
        int sum = 0;
        for (int c = 0; c < children.length; c++) {
            sum += this.children[c].getNbPositiveLiterals();
        }
        return sum;
    }
}
