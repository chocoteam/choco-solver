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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

    private static ThreadLocal<LogicComparator> comp = new ThreadLocal<LogicComparator>() {
        @Override
        protected LogicComparator initialValue() {
            return new LogicComparator();
        }
    };

    public static void expandNot(LogOp n) {
        if (n.isNot()) {
            n.flip();
        }
        ILogical[] children = n.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (!children[i].isLit()) {
                expandNot((LogOp) children[i]);
            }
        }
    }

    public static void merge(LogOp.Operator op, LogOp n) {
        if (n.is(op)) {
            ILogical[] children = n.getChildren();
            for (int i = 0; i < children.length; i++) {
                ILogical child = children[i];
                if (!child.isLit()) {
                    LogOp nc = (LogOp) child;
                    if (nc.is(op)) {
                        merge(op, nc);
                        ILogical[] subchildren = nc.getChildren();
                        n.removeChild(child);
                        for (int j = 0; j < subchildren.length; j++) {
                            n.addChild(subchildren[j]);
                        }
                    }
                }
            }
        }
    }

    public static LogOp developOr(LogOp n) {
        ILogical t1 = n.getAndChild();
        ILogical t2 = n.getChildBut(t1);
        LogOp tt = LogOp.and();
        if (!t1.isLit()) {
            LogOp n1 = (LogOp) t1;
            ILogical[] t1cs = n1.getChildren();
            for (int i = 0; i < t1cs.length; i++) {
                ILogical t1c = t1cs[i];
                if (t2.isLit()) {
                    tt.addChild(LogOp.or(t1c, t2));
                } else {
                    ILogical[] t2cs = ((LogOp) t2).getChildren();
                    for (int j = 0; j < t2cs.length; j++) {
                        ILogical t2c = t2cs[j];
                        tt.addChild(LogOp.or(t1c, t2c));
                    }
                }
            }
        }
        n.removeChild(t1);
        n.removeChild(t2);
        if (n.getNbChildren() == 0) {
            return tt;
        } else {
            n.addChild(tt);
            return n;
        }
    }

    public static LogOp distribute(LogOp n) {
        if (n.is(LogOp.Operator.AND)) {
            ILogical[] children = n.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (!children[i].isLit()) {
                    children[i] = distribute((LogOp) children[i]);
                }
            }
        } else {
            if (n.hasOrChild()) {
                merge(LogOp.Operator.OR, n);
            }
            if (n.hasAndChild() && n.getNbChildren() > 1) {
                n = distribute(developOr(n));
            }
        }
        merge(LogOp.Operator.AND, n);
        return n;
    }

    private static BoolVar[] extract(ILogical node) {
        if (node.isLit()) {
            return new BoolVar[]{(BoolVar) node};
        } else {
            return ((LogOp) node).flattenBoolVar();
        }
    }


    public static ILogical simplify(ILogical t, Solver solver) {
        if (t.isLit()) return t;
        // else
        LogOp n = (LogOp) t;
        if (n.is(LogOp.Operator.OR)) {
            // OR with only LITS
            ILogical[] children = n.getChildren();
            HashMap<BoolVar, ILogical> lits = new HashMap<>();
            for (int i = 0; i < children.length; i++) {
                BoolVar var = extract(children[i])[0];
                var = var.isNot() ? var.not() : var;
                if (lits.containsKey(var)) {
                    ILogical prev = lits.get(var);
                    if (prev.isNot() != children[i].isNot()) {
                        return solver.ONE;
                    }
                } else {
                    lits.put(var, children[i]);
                }
            }
        } else if (!n.hasOrChild()) {
            // AND with only LITS
            ILogical[] children = n.getChildren();
            HashMap<BoolVar, ILogical> lits = new HashMap<>();
            for (int i = 0; i < children.length; i++) {
                BoolVar var = extract(children[i])[0];
                var = var.isNot() ? var.not() : var;
                if (lits.containsKey(var)) {
                    ILogical prev = lits.get(var);
                    if (prev.isNot() != children[i].isNot()) {
                        return solver.ZERO;
                    }
                } else {
                    lits.put(var, children[i]);
                }
            }
        } else {
            ILogical[] children = n.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (!children[i].isLit()) {
                    children[i] = simplify(children[i], solver);
                }
            }
        }
        return t;
    }


    public static ILogical simplifySingleton(ILogical l, Solver solver) {
        if (l.isLit()) return l;
        LogOp t = (LogOp) l;
        ILogical[] children = t.getChildren();
        ArrayList<ILogical> toRemove = new ArrayList<>();
        for (int i = 0; i < children.length; i++) {
            if (solver.ONE.equals(children[i])) {
                toRemove.add(children[i]);
            }
        }
        for (ILogical lt : toRemove) {
            t.removeChild(lt);
        }
        if (t.getNbChildren() == 1) {
            return t.getChildren()[0];
        }
        return t;
    }

    /**
     * Warning: if there is a bug, please check the CNF build is like:
     * - lit OR lit ... OR lit
     * - (lit OR lit ... OR lit) AND (lit OR lit ... OR lit) ... AND (lit OR lit ... OR lit)
     *
     * @param logOp logical operator
     * @return an ILogical
     */
    public static ILogical toCNF(LogOp logOp, Solver solver) {
        expandNot(logOp);
        logOp = distribute(logOp);
        // sort children of each clause with positive literals first
        if (logOp.is(LogOp.Operator.OR)) {
            Arrays.sort(logOp.getChildren(), comp.get());
        }
        ILogical[] children = logOp.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (!children[i].isLit()) {
                LogOp nc = (LogOp) children[i];
                if (nc.is(LogOp.Operator.OR)) {
                    Arrays.sort(nc.getChildren(), comp.get());
                }
            }
        }
        ILogical l = simplify(logOp, solver);
        l = simplifySingleton(l, solver);
        if (!l.isLit()) ((LogOp) l).cleanFlattenBoolVar();
        return l;
    }


    private static class LogicComparator implements Comparator<ILogical> {

        @Override
        public int compare(ILogical o1, ILogical o2) {
            if (o1.isNot() == o2.isNot()) {
                return 0;
            } else if (o2.isNot()) {
                return -1;
            }
            return 1;
        }
    }

}
