/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.solver.constraints.nary.cnf;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Logical Operator, to ease clause definition.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 nov. 2010
 */
public final class LogOp implements ILogical {

    public enum Operator {
        OR, AND;

        public static Operator flip(Operator operator) {
            if (Operator.OR.equals(operator)) {
                return Operator.AND;
            } else {
                return Operator.OR;
            }
        }
    }

    public enum Type {
        POSITIVE, NEGATIVE;

        public static Type flip(Type type) {
            if (Type.POSITIVE.equals(type)) {
                return Type.NEGATIVE;
            } else {
                return Type.POSITIVE;
            }
        }
    }

    Type type;

    Operator operator;

    ILogical[] children;

    BoolVar[] varsAsArray;

    protected LogOp(Operator operator, Type type, ILogical... children) {
        this.type = type;
        this.operator = operator;
        if (children == null) {
            this.children = new ILogical[0];
        } else {
            this.children = children;
        }
    }


    public static LogOp and(ILogical... children) {
        return new LogOp(Operator.AND, Type.POSITIVE, children);
    }

    public static LogOp ifOnlyIf(ILogical a, ILogical b) {
        return and(implies(a, b), implies(b, a));
    }

    public static LogOp ifThenElse(ILogical a, ILogical b, ILogical c) {
        try {
            ILogical na = negate(a);
            return or(and(a, b), and(na, c));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LogOp implies(ILogical a, ILogical b) {
        try {
            ILogical na = negate(a);
            return or(na, b);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LogOp reified(BoolVar b, ILogical tree) {
        try {
            BoolVar nb = b.not();
            ILogical ntree = negate(tree);
            return or(and(b, tree), and(nb, ntree));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LogOp or(ILogical... children) {
        return new LogOp(Operator.OR, Type.POSITIVE, children);
    }

    public static LogOp nand(ILogical... children) {
        return new LogOp(Operator.AND, Type.NEGATIVE, children);
    }

    public static LogOp nor(ILogical... children) {
        return new LogOp(Operator.OR, Type.NEGATIVE, children);
    }

    public static LogOp xor(ILogical a, ILogical b) {
        try {
            ILogical na = negate(a);
            ILogical nb = negate(b);
            return or(and(a, nb), and(b, na));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ILogical negate(ILogical l) throws CloneNotSupportedException {
        if (l.isLit()) {
            return ((BoolVar) l).not();
        } else {
            LogOp n = (LogOp) l;
            LogOp na = n.clone();
            na.type = Type.flip(and().type);
            return na;
        }
    }


    /**
     * Current tree is rooted with the logical operator <code>op</code>
     *
     * @param op operator checked
     * @return <code>true</code> if <code>this</code> is <code>op</code>
     */
    public boolean is(Operator op) {
        return op.equals(operator);
    }

    /**
     * Current tree is rooted with NOT logical operator
     *
     * @return <code>true</code> if <code>this</code> is NOT
     */
    public boolean isNot() {
        return type.equals(Type.NEGATIVE);
    }

    @Override
    public boolean isLit() {
        return false;
    }

	@Override
	public void setNot(boolean isNot){
		throw new UnsupportedOperationException();
	}

    /**
     * Returns the number of direct children of <code>this</code>
     *
     * @return number of children
     */
    int getNbChildren() {
        return children.length;
    }

    /**
     * Check if at least one children is an OR logic tree
     *
     * @return <code>true</code> if <code>this</code> contains one OR logic tree
     */
    boolean hasOrChild() {
        for (int i = 0; i < children.length; i++) {
            if (!children[i].isLit() && ((LogOp) children[i]).is(Operator.OR)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if at least one children is an AND logic tree
     *
     * @return <code>true</code> if <code>this</code> contains one AND logic tree
     */
    boolean hasAndChild() {
        for (int i = 0; i < children.length; i++) {
            if (!children[i].isLit() && ((LogOp) children[i]).is(Operator.AND)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds <code>child</code> to the current list of children of <code>this</code>
     *
     * @param child the logic tree to add
     */
    public void addChild(ILogical child) {
        ILogical[] tmp = children;
        children = new ILogical[tmp.length + 1];
        System.arraycopy(tmp, 0, children, 0, tmp.length);
        children[tmp.length] = child;
        varsAsArray = null; // force recomputation of varsArray
    }

    /**
     * Removes <code>child</code> from the current list of children of <code>this</code>
     *
     * @param child the logic tree to remove
     */
    public void removeChild(ILogical child) {
        int i = 0;
        for (; i < children.length && children[i] != child; i++) {
        }
        if (i == children.length) return;
        ILogical[] tmp = children;
        children = new ILogical[tmp.length - 1];
        System.arraycopy(tmp, 0, children, 0, i);
        System.arraycopy(tmp, i + 1, children, i, tmp.length - i - 1);
        varsAsArray = null; // force recomputation of varsArray
    }

    /**
     * Returns the array of children of <code>this</code>.
     * <code>null</code> is a valid return value.
     *
     * @return an array of logic trees, <code>null</code> otherwise
     */
    public ILogical[] getChildren() {
        return children;
    }

    /**
     * Returns the first AND logic tree within the list of children.
     * <code>null</code> is a valid return value.
     *
     * @return a AND logic tree if exists, <code>null</code> otherwise
     */
    public ILogical getAndChild() {
        for (int i = 0; i < children.length; i++) {
            if (!children[i].isLit() && ((LogOp) children[i]).is(Operator.AND)) {
                return children[i];
            }
        }
        return null;
    }

    /**
     * Returns the first child within the list of children, different from <code>child</code>.
     * <code>null</code> is a valid return value.
     *
     * @param child node to avoid
     * @return the first logic tree different from <code>child</code> if exists, <code>null</code> otherwise
     */
    public ILogical getChildBut(ILogical child) {
        for (int i = 0; i < children.length; i++) {
            if (children[i] != child) {
                return children[i];
            }
        }
        return null;
    }

    /**
     * Flip the boolean evaluation of <code>this</code>  (recursive).
     */
    public void flip() {
        type = Type.flip(type);
        operator = Operator.flip(operator);
        for (int i = 0; i < children.length; i++) {
            if (children[i].isLit()) {
                children[i] = ((BoolVar) children[i]).not();
            } else {
                ((LogOp) children[i]).deny();
            }
        }
    }


    /**
     * Flip the boolean operator of <code>this</code> (recursive).
     */
    public void deny() {
        operator = Operator.flip(operator);
        for (int i = 0; i < children.length; i++) {
            if (children[i].isLit()) {
                children[i] = ((BoolVar) children[i]).not();
            } else {
                ((LogOp) children[i]).deny();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append('(');
//        st.append(Type.POSITIVE.equals(type) ? "(" : "not(");

        String op = (Type.POSITIVE.equals(type) ? "" : "n") + (Operator.AND.equals(operator) ? "and " : "or ");
        for (int i = 0; i < children.length; i++) {
            ILogical child = children[i];
            if (child.isLit()) {
                st.append(((BoolVar) child).getName());
            } else {
                st.append(child.toString());
            }
            st.append(" ").append(op);

        }
        st.replace(st.length() - (op.length() + 1), st.length(), "");
        st.append(')');
        return st.toString();
    }


    @Override
    public LogOp clone() throws CloneNotSupportedException {
        LogOp logOp = (LogOp) super.clone();
        logOp.type = this.type;
        logOp.operator = this.operator;
        logOp.children = new ILogical[this.children.length];
        for (int c = 0; c < children.length; c++) {
            if (children[c].isLit()) {
                logOp.children[c] = children[c];
            } else {
                logOp.children[c] = ((LogOp) children[c]).clone();
            }
        }
        return logOp;
    }

    /**
     * Extracts and returns the flatten array of BoolVar contained in <code>this</code>.
     * WARNING : a variable may appear more than once, redundancy is not checked!
     *
     * @return array of bool variables
     */
    public BoolVar[] flattenBoolVar() {
//        if (varsAsArray == null) {
        buildVarsArray();
//        }
        return varsAsArray;
    }

    public void cleanFlattenBoolVar() {
        for (int i = 0; i < children.length; i++) {
            if (!children[i].isLit()) {
                ((LogOp) children[i]).cleanFlattenBoolVar();
            }
        }
        varsAsArray = null;
    }

    private void buildVarsArray() {
        final BoolVar[][] childrenVars = new BoolVar[children.length][];
        for (int i = 0; i < children.length; i++) {
            if (children[i].isLit()) {
                childrenVars[i] = new BoolVar[]{(BoolVar) children[i]};
            } else {
                childrenVars[i] = ((LogOp) children[i]).flattenBoolVar();
            }
        }
        varsAsArray = ArrayUtils.flatten(childrenVars);
    }
}
