package org.chocosolver.solver.expression.discrete.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.expression.discrete.relational.UnCReExpression;
import org.chocosolver.solver.variables.SetVar;

public interface SetExpression {
    default SetExpression union(SetExpression y){
        return new BiArSetExpression(SetOperator.UNION, this, y);
    }

    default SetExpression intersection(SetExpression y){
        return new BiArSetExpression(SetOperator.INTERSECTION, this, y);
    }
    default ReExpression subSet(SetExpression y) {
        return new BiReSetExpression(SetOperator.SUBSET, this, y);
    }
    default ReExpression eq(SetExpression y) {
        return new BiReSetExpression(SetOperator.EQ, this, y);
    }
    default ReExpression ne(SetExpression y) {
        return new BiReSetExpression(SetOperator.NE, this, y);
    }
    default ReExpression contains(SetExpression y) {
        return new BiReSetExpression(SetOperator.CONTAINS, this, y);
    }

    default ReExpression notContains(SetExpression y) {
        return new BiReSetExpression(SetOperator.NC, this, y);
    }


    default ReExpression subSet(int...y) {
        return new UnReSetExpression(SetOperator.SUBSET, this, y);
    }
    default ReExpression eq(int...y) {
        return new UnReSetExpression(SetOperator.EQ, this, y);
    }

    default ReExpression notContains(int...y) {
        return new UnReSetExpression(SetOperator.NC, this, y);
    }

    default ReExpression contains(int...y) {
        return new UnReSetExpression(SetOperator.CONTAINS, this, y);
    }

    default ReExpression ne(int...y) {
        return new UnReSetExpression(SetOperator.NE, this, y);
    }

    default Constraint notEmpty(){
        SetVar set = this.getSetVar();
        return getModel().notEmpty(set);
    }

    default UnCReExpression setCard(int valueCard){
        SetVar set = this.getSetVar();
        return set.getCard().eq(valueCard);
    }

    SetVar getSetVar();

    default Model getModel() {
        throw new UnsupportedOperationException("getModel() is not supported in this context");
    }

}