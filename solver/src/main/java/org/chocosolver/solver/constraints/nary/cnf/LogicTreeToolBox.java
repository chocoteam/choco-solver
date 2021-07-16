/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cnf;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A tool box to convert logical expressions into CNF.
 * <br/>
 *
 * @author Charles Prud'homme, Xavier Lorca
 * @since 23 nov. 2010
 */
public class LogicTreeToolBox {

    /**
     * This class is a factory, no need to create it.
     */
    protected LogicTreeToolBox() {
    }

    /**
     * Warning: if there is a bug, please check the CNF build is like:
     * - lit OR lit ... OR lit
     * - (lit OR lit ... OR lit) AND (lit OR lit ... OR lit) ... AND (lit OR lit ... OR lit)
     *
     * @param logOp logical operator
     * @param model the model in which the logical expression will be added, useful since the expression may only be made of TRUE and FALSE.
     * @return a CNF logical expression
     */
    public static ILogical toCNF(LogOp logOp, Model model) {
        expandNot(logOp);
        logOp = distribute(logOp);
        // sort children of each clause with positive literals first
        if (logOp.is(LogOp.Operator.OR)) {
            sort(logOp);
        }
        ILogical[] children = logOp.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (!children[i].isLit()) {
                LogOp nc = (LogOp) children[i];
                if (nc.is(LogOp.Operator.OR)) {
                    sort(nc);
                }
            }
        }
        ILogical l = simplify(logOp, model);
        l = simplifySingleton(l, model);
        l = orderAndReduce(l);
        if (!l.isLit()) ((LogOp) l).cleanFlattenBoolVar();
        return l;
    }

    /**
     * Erases 'NOT' operand from the logical expression <code>n</code> by flipping the right children
     * @param n a logical expression
     */
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


    /**
     * Flattens a logical expression <code>n</code> based on operator <code>op</code>.
     * Transform from undefined depth expression to comb expression
     * @param op reference operator
     * @param n the lofical expression
     */
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

    /**
     * Moves down 'OR' operand in the logical expression <code>n</code>.
     * @param n a logical expression
     * @return the modified logical expression
     */
    @SuppressWarnings("ConstantConditions")
    public static LogOp developOr(LogOp n) {
        ILogical t1 = n.getAndChild();
        ILogical t2 = n.getChildBut(t1);
        LogOp tt = LogOp.and();
        if (!(t1 != null && t1.isLit())) {
            LogOp n1 = (LogOp) t1;
            ILogical[] t1cs = n1.getChildren();
            for (int i = 0; i < t1cs.length; i++) {
                ILogical t1c = t1cs[i];
                if (t2 != null) {
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

    /**
     * Distributes 'OR's inwards over 'AND's in <code>n</code>
     * @param n a logical expression
     * @return the modified logical expression
     */
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

    /**
     * Extracts the array of {@link BoolVar} from <code>node</code>
     * @param node a logical expression
     * @return the array of {@link BoolVar} from <code>node</code>
     */
    private static BoolVar[] extract(ILogical node) {
        if (node.isLit()) {
            return new BoolVar[]{(BoolVar) node};
        } else {
            return ((LogOp) node).flattenBoolVar();
        }
    }

    /**
     * Detects tautologies and contradictions from <code>t</code>
     * @param t a logical expression
     * @param model to get {@link Model#boolVar(boolean)}.
     * @return simplified logical expression
     */
    public static ILogical simplify(ILogical t, Model model) {
        if (t.isLit()) return t;
        // else
        LogOp n = (LogOp) t;
        ILogical[] children = n.getChildren();
        if (n.is(LogOp.Operator.OR)) {
            // OR with only LITS
            HashMap<BoolVar, ILogical> lits = new HashMap<>();
            for (int i = 0; i < children.length; i++) {
                BoolVar var = extract(children[i])[0];
                var = var.isNot() ? var.not() : var;
                if (lits.containsKey(var)) {
                    ILogical prev = lits.get(var);
                    if (prev.isNot() != children[i].isNot()) {
                        return model.boolVar(true);
                    }
                } else {
                    lits.put(var, children[i]);
                }
            }
            ILogical[] ts = lits.values().toArray(new ILogical[0]);
//            Arrays.sort(ts);
            return LogOp.or(ts);
        } else if (!n.hasOrChild()) {
            // AND with only LITS
            HashMap<BoolVar, ILogical> lits = new HashMap<>();
            for (int i = 0; i < children.length; i++) {
                BoolVar var = extract(children[i])[0];
                var = var.isNot() ? var.not() : var;
                if (lits.containsKey(var)) {
                    ILogical prev = lits.get(var);
                    if (prev.isNot() != children[i].isNot()) {
                        return model.boolVar(false);
                    }
                } else {
                    lits.put(var, children[i]);
                }
            }
            ILogical[] ts = lits.values().toArray(new ILogical[0]);
//            Arrays.sort(ts);
            return LogOp.and(ts);
        } else {
            for (int i = 0; i < children.length; i++) {
                if (!children[i].isLit()) {
                    children[i] = simplify(children[i], model);
                }
            }
        }
        return t;
    }


    /**
     * Remove tautologies from <code>l</code>
     * @param l logical expression
     * @param model to get {@link Model#intVar(int)}.
     * @return simplified logical expression
     */
    public static ILogical simplifySingleton(ILogical l, Model model) {
        if (l.isLit()) return l;
        LogOp t = (LogOp) l;
        ILogical[] children = t.getChildren();
        ArrayList<ILogical> toRemove = new ArrayList<>();
        for (int i = 0; i < children.length; i++) {
            if (model.boolVar(true).equals(children[i])) {
                toRemove.add(children[i]);
            }
        }
        toRemove.forEach(t::removeChild);
        if (t.getNbChildren() == 1) {
            return t.getChildren()[0];
        }
        return t;
    }


    /**
     * Reorder <code>l</code> in order to eliminate duplicates
     * @param t the logical expression to order and reduce
     * @return reordered logical expression
     */
    private static ILogical orderAndReduce(ILogical t) {
        if (t.isLit()) return t;
        LogOp n = (LogOp) t;
        ILogical[] children = n.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (!children[i].isLit()) {
                Arrays.sort(((LogOp)children[i]).getChildren(), LogicTreeToolBox::sameLogical);
            }
        }
        Arrays.sort(children, LogicTreeToolBox::sameLogical);
        int i  = 0;
        int k  = children.length-1;
        while(i < k ){
            if(sameLogical(children[i], children[i+1]) == 0){
                System.arraycopy(children, i+1, children, i, children.length - i - 1);
                k--;
            }else {
                i++;
            }
        }
        if(k == 0){
            return children[0];
        }else {
            return new LogOp(n.operator, n.type, Arrays.copyOf(children, k + 1));
        }
    }


    /**
     * Sort a logical expression wrt to NOT
     * @param logOp logical expression to sort
     */
    private static void sort(LogOp logOp){
        Arrays.sort(logOp.getChildren(), (o1, o2) -> {
            if (o1.isNot() == o2.isNot()) {
                return 0;
            } else if (o2.isNot()) {
                return -1;
            }
            return 1;
        });
    }

    private static int sameLogical(ILogical o1, ILogical o2){
        if (o1.isLit()) {
            if (o2.isLit()) {
                return sameLit(o1, o2);
            } else {
                return -1;
            }
        } else {
            if (o2.isLit()) {
                return 1;
            } else {
                LogOp l1 = (LogOp)o1;
                LogOp l2 = (LogOp)o2;
                return sameChild(l1, l2);
            }
        }
    }

    private static int sameLit(ILogical o1, ILogical o2){
        int diff = ((BoolVar) o1).getId() - ((BoolVar) o2).getId();
        if(diff == 0){
            if (o1.isNot() == o2.isNot()) {
                return 0;
            } else if (o2.isNot()) {
                return -1;
            }
            return 1;
        }else {
            return diff;
        }
    }

    private static int sameChild(LogOp l1, LogOp l2){
        if(l1.getNbChildren() == l2.getNbChildren()){
            // assume l1 and l2 are already sorted
            int i = 0;
            int same = 0;
            ILogical ll1, ll2;
            while(i < l1.getNbChildren() && same == 0){
                ll1 = l1.getChildren()[i];
                ll2 = l2.getChildren()[i];
                assert ll1.isLit();
                assert ll2.isLit();
                same = sameLit(ll1, ll2);
                i++;
            }
            return same;
        }else{
            return l1.getNbChildren() - l2.getNbChildren();
        }
    }


}
