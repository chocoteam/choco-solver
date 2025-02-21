package org.chocosolver.solver.expression.discrete.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.SetVar;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * BiArSetExpression - Binary Arithmetic Set Expression
 *
 * This class represents a binary arithmetic expression between two SetVarExpression instances.
 * It supports set operations such as union (UNION) and intersection (INTERSECTION),
 * returning a new SetVar as the result of the operation.
 *
 * Example:
 * setA.eq(setB.union(setC)).post();
 * setA.eq(setB.intersection(setC)).post();
 */

public class BiArSetExpression implements SetExpression {

    private SetOperator op;
    private SetExpression x;
    private SetExpression y;
    private Model model;
    private SetVar xSet, ySet, resultSet;

    public BiArSetExpression(SetOperator operator, SetExpression x, SetExpression y) {
        this.op = operator;
        this.x = x;
        this.y = y;
        this.xSet = x.getSetVar();
        this.ySet = y.getSetVar();

        this.model = xSet.getModel();
    }

    @Override
    public SetVar getSetVar() {
        if (resultSet == null) {
            int[] setDomain = IntStream.concat(Arrays.stream(xSet.getUB().toArray()), Arrays.stream(ySet.getUB().toArray())).toArray();
            resultSet = model.setVar(model.generateName("aux_setvar_"), new int[]{}, setDomain);

            switch (op) {
                case UNION:
                    model.union(new SetVar[]{xSet, ySet}, resultSet).post();
                    break;
                case INTERSECTION:
                    model.intersection(new SetVar[]{xSet, ySet}, resultSet).post();
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported SetOperator: " + op);
            }
        }
        return resultSet;
    }
}
