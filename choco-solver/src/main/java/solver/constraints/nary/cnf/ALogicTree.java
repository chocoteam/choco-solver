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

import solver.variables.BoolVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 nov. 2010
 */
public abstract class ALogicTree implements Cloneable {

    public static enum Operator {
        OR, AND;

        public static Operator flip(Operator operator) {
            if (Operator.OR.equals(operator)) {
                return Operator.AND;
            } else {
                return Operator.OR;
            }
        }
    }

    public static enum Type {
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

    protected ALogicTree(Type type) {
        this.type = type;
    }

    /**
     * Current tree is rooted with the logical operator <code>op</code>
     *
     * @param op operator checked
     * @return <code>true</code> if <code>this</code> is <code>op</code>
     */
    abstract boolean is(Operator op);

    /**
     * Current tree is rooted with NOT logical operator
     *
     * @return <code>true</code> if <code>this</code> is NOT
     */
    abstract boolean isNot();

    /**
     * Current tree is a literal
     *
     * @return <code>true</code> if <code>this</code> is a literal
     */
    abstract boolean isLit();

    /**
     * Returns the number of direct children of <code>this</code>
     *
     * @return number of children
     */
    abstract int getNbChildren();

    /**
     * Check if at least one children is an OR logic tree
     *
     * @return <code>true</code> if <code>this</code> contains one OR logic tree
     */
    abstract boolean hasOrChild();

    /**
     * Checks if at least one children is an AND logic tree
     *
     * @return <code>true</code> if <code>this</code> contains one AND logic tree
     */
    abstract boolean hasAndChild();

    /**
     * Adds <code>child</code> to the current list of children of <code>this</code>
     *
     * @param child the logic tree to add
     */
    abstract void addChild(ALogicTree child);

    /**
     * Removes <code>child</code> from the current list of children of <code>this</code>
     *
     * @param child the logic tree to remove
     */
    abstract void removeChild(ALogicTree child);

    /**
     * Returns the array of children of <code>this</code>.
     * <code>null</code> is a valid return value.
     *
     * @return an array of logic trees, <code>null</code> otherwise
     */
    abstract ALogicTree[] getChildren();

    /**
     * Returns the first AND logic tree within the list of children.
     * <code>null</code> is a valid return value.
     *
     * @return a AND logic tree if exists, <code>null</code> otherwise
     */
    abstract ALogicTree getAndChild();

    /**
     * Returns the first child within the list of children, different from <code>child</code>.
     * <code>null</code> is a valid return value.
     *
     * @param child node to avoid
     * @return the first logic tree different from <code>child</code> if exists, <code>null</code> otherwise
     */
    abstract ALogicTree getChildBut(ALogicTree child);

    /**
     * Flip the boolean evaluation of <code>this</code>  (recursive).
     */
    abstract void flip();

    /**
     * Flip the boolean operator of <code>this</code> (recursive).
     */
    abstract void deny();

    public ALogicTree clone() throws CloneNotSupportedException {
        return (ALogicTree) super.clone();
    }

    /**
     * Extracts and returns the flatten array of BoolVar contained in <code>this</code>.
     * WARNING : a variable may appear more than once, redundancy is not checked!
     *
     * @return array of bool variables
     */
    public abstract BoolVar[] flattenBoolVar();

    /**
     * Clean flatten array of BoolVar, required for toCNF
     */
    public void cleanFlattenBoolVar() {
    }

    /**
     * Computes and returns the number of positive literals contained in the direct level.
     *
     * @return number of positive literals
     */
    public abstract int getNbPositiveLiterals();

}
