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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 nov. 2010
 */
public class LogicTreeToolBox {
    protected LogicTreeToolBox() {
    }

    public static void expandNot(ALogicTree t) {
        if (!t.isLit()) {
            if (t.isNot()) {
                t.flip();
            }
            ALogicTree[] children = t.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (!children[i].isLit()) {
                    expandNot(children[i]);
                }
            }
        }
    }

    public static void merge(ALogicTree.Operator op, ALogicTree t) {
        if (!t.isLit()) {
            if (t.is(op)) {
                ALogicTree[] children = t.getChildren();
                for (int i = 0; i < children.length; i++) {
                    ALogicTree child = children[i];
                    if (child.is(op)) {
                        merge(op, child);
                        ALogicTree[] subchildren = child.getChildren();
                        t.removeChild(child);
                        for (int j = 0; j < subchildren.length; j++) {
                            t.addChild(subchildren[j]);
                        }
                    }
                }
            }
        }
    }

    public static ALogicTree developOr(ALogicTree t) {
        ALogicTree t1 = t.getAndChild();
        ALogicTree t2 = t.getChildBut(t1);
        ALogicTree tt = Node.and();
        ALogicTree[] t1cs = t1.getChildren();
        for (int i = 0; i < t1cs.length; i++) {
            ALogicTree t1c = t1cs[i];
            if (t2.isLit()) {
                tt.addChild(Node.or(t1c, t2));
            } else {
                ALogicTree[] t2cs = t2.getChildren();
                for (int j = 0; j < t2cs.length; j++) {
                    ALogicTree t2c = t2cs[j];
                    tt.addChild(Node.or(t1c, t2c));
                }
            }
        }
        t.removeChild(t1);
        t.removeChild(t2);
        if (t.getNbChildren() == 0) {
            return tt;
        } else {
            t.addChild(tt);
            return t;
        }
    }

    public static ALogicTree distribute(ALogicTree t) {
        if (!t.isLit()) {
            if (t.is(ALogicTree.Operator.AND)) {
                ALogicTree[] children = t.getChildren();
                for (int i = 0; i < children.length; i++) {
                    children[i] = distribute(children[i]);
                }
            } else {
                if (t.hasOrChild()) {
                    merge(ALogicTree.Operator.OR, t);
                }
                if (t.hasAndChild() && t.getNbChildren() > 1) {
                    t = distribute(developOr(t));
                }
            }
            merge(ALogicTree.Operator.AND, t);
        }
        return t;
    }

    public static ALogicTree simplify(ALogicTree t) {
        if (t.is(ALogicTree.Operator.OR)) {
            // OR with only LITS
            ALogicTree[] children = t.getChildren();
            HashMap<BoolVar, ALogicTree> lits = new HashMap<BoolVar, ALogicTree>();
            for (int i = 0; i < children.length; i++) {
                BoolVar var = children[i].flattenBoolVar()[0];
                if (lits.containsKey(var)) {
                    ALogicTree prev = lits.get(var);
                    if (!prev.type.equals(children[i].type)) {
                        return Singleton.TRUE;
                    }
                } else {
                    lits.put(var, children[i]);
                }
            }
        } else if (!t.hasOrChild()) {
            // AND with only LITS
            ALogicTree[] children = t.getChildren();
            HashMap<BoolVar, ALogicTree> lits = new HashMap<BoolVar, ALogicTree>();
            for (int i = 0; i < children.length; i++) {
                BoolVar var = children[i].flattenBoolVar()[0];
                if (lits.containsKey(var)) {
                    ALogicTree prev = lits.get(var);
                    if (!prev.type.equals(children[i].type)) {
                        return Singleton.FALSE;
                    }
                } else {
                    lits.put(var, children[i]);
                }
            }
        } else {
            ALogicTree[] children = t.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (!children[i].isLit()) {
                    children[i] = simplify(children[i]);
                }
            }
        }
        return t;
    }

    public static ALogicTree simplifySingleton(ALogicTree t) {
        if (!(Singleton.TRUE.equals(t) || Singleton.FALSE.equals(t) || t.isLit())) {
            ALogicTree[] children = t.getChildren();
            ArrayList<ALogicTree> toRemove = new ArrayList<ALogicTree>();
            for (int i = 0; i < children.length; i++) {
                if (Singleton.TRUE.equals(children[i])) {
                    toRemove.add(children[i]);
                }
            }
            for (ALogicTree lt : toRemove) {
                t.removeChild(lt);
            }
            if (t.getNbChildren() == 1) {
                return t.getChildren()[0];
            }
        }
        return t;
    }

    /**
     * Warning: if there is a bug, please check the CNF build is like:
     * - lit OR lit ... OR lit
     * - (lit OR lit ... OR lit) AND (lit OR lit ... OR lit) ... AND (lit OR lit ... OR lit)
     *
     * @param t
     * @return
     */
    public static ALogicTree toCNF(ALogicTree t) {
        expandNot(t);
        t = distribute(t);
        // sort children of each clause with positive literals first
        if (t.is(ALogicTree.Operator.OR)) {
            Arrays.sort(t.getChildren());
        }
        ALogicTree[] children = t.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i].is(ALogicTree.Operator.OR)) {
                Arrays.sort(children[i].getChildren());
            }
        }
        t = simplify(t);
        t = simplifySingleton(t);
        t.cleanFlattenBoolVar();
        return t;
    }


}
