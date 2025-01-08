package org.chocosolver.solver.expression.discrete.set;


import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import java.util.HashSet;

import static org.chocosolver.solver.constraints.real.Ibex.TRUE;

/**
 * BiReSetExpression - Binary Relational Set Expression
 *
 * This class represents a binary relational expression between two SetVarExpression instances.
 * It supports relational operations such as equality (EQ), inequality (NE), subset (SUBSET),
 * and containment (CONTAINS). The operations can be composed and posted as constraints.
 *
 * Example:
 * setA.eq(setB).post();
 * setA.ne(setB).post();
 */
public class BiReSetExpression implements ReExpression {

    private SetOperator op;
    private SetExpression x;
    private SetExpression y;
    private Model model;
    private SetVar xSet, ySet;

    private Constraint constraint;

    private BoolVar result;

    public BiReSetExpression(SetOperator operator, SetExpression x, SetExpression y) {
        this.op = operator;
        this.model = x.getModel();
        this.x = x;
        this.y = y;
        this.xSet = x.getSetVar();
        this.ySet = y.getSetVar();
    }

    public Model getModel() {
        return this.model;
    }

    @Override
    public BoolVar boolVar() {
        if (result == null) {
            switch (op) {
                case SUBSET:
                    result = model.subsetEq(xSet, ySet).reify().eq(TRUE).boolVar();
                    break;
                case EQ:
                    result = model.allEqual(xSet, ySet).reify().eq(TRUE).boolVar();
                    break;
                case NE:
                    result = model.allDifferent(xSet, ySet).reify().eq(TRUE).boolVar();
                    break;
                case CONTAINS:
                    result = model.subsetEq(ySet, xSet).reify().eq(TRUE).boolVar();
                    break;
                case NC:
                    result = model.disjoint(ySet, xSet).reify().eq(TRUE).boolVar();
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported SetOperator: " + op);
            }
        }
        return result;
    }

    @Override
    public void extractVar(HashSet<IntVar> variables) {
    }

    @Override
    public Constraint decompose() {
        switch (op) {
            case SUBSET:
                constraint = model.subsetEq(xSet, ySet);
                break;
            case EQ:
                constraint = model.allEqual(xSet, ySet);
                break;
            case NE:
                constraint = model.allDifferent(xSet, ySet);
                break;
            case CONTAINS:
                constraint = model.subsetEq(ySet, xSet);
                break;
            case NC:
                constraint = model.disjoint(xSet,ySet);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported SetOperator: " + op);
        }
        return constraint;
    }
}