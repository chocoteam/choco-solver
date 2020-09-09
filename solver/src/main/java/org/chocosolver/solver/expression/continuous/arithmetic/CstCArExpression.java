package org.chocosolver.solver.expression.continuous.arithmetic;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.real.RealPowerPropagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.objects.RealInterval;
import org.chocosolver.util.tools.RealUtils;

/**
 * Unary continuous arithmetic expression with a constant
 *
 * @author João Pedro Schmitt
 * @since 08/09/2020
 */
public class CstCArExpression extends UnCArExpression {

    private static final String ERROR_MSG_OPERATION_CSTE = "Equation does not support %s with constant %.2f. Consider using Ibex instead.";

    private static final String ERROR_MSG_OPERATION = "Equation does not support %s. Consider using Ibex instead.";

    /**
     * Constant of the arithmetic expression
     */
    double constant;

    /**
     * Builds a unary expression with constant
     *
     * @param op  operator
     * @param exp an continuous arithmetic expression
     */
    public CstCArExpression(Operator op, CArExpression exp, double c) {
        super(op, exp);
        this.constant = c;
    }

    @Override
    public RealVar realVar(double p) {
        if (me == null) {
            RealVar xVar = e.realVar(p);
            switch (op) {
                case POW:
                    if (isInteger(constant)) {
                        int k = (int) constant;
                        if (constant >= 0) {
                            // y = x^k
                            RealInterval powerResult = RealUtils.iPower(xVar, k);
                            me = model.realVar(powerResult.getLB(), powerResult.getUB(), xVar.getPrecision());
                            model.post(new Constraint(model.generateName(), new RealPowerPropagator(me, xVar, k)));
                        } else {
                            // y = 1/x^k
                            k = -k;
                            RealInterval powerResult = RealUtils.iPower(xVar, k);
                            RealVar xPowerK = model.realVar(powerResult.getLB(), powerResult.getUB(), xVar.getPrecision());
                            model.post(new Constraint(model.generateName(), new RealPowerPropagator(xPowerK, xVar, k)));
                            me = model.realVar(powerResult.getLB(), powerResult.getUB(), xVar.getPrecision());
                            model.realIbexGenericConstraint("{0}=1/{1}", me, xPowerK).post();
                        }
                    } else {
                        model.realIbexGenericConstraint("{0}={1}^" + constant, me, xVar).post();
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Unary arithmetic expressions does not support " + op.name());
            }
        }
        return me;
    }

    @Override
    public void tighten() {
        throw new UnsupportedOperationException(String.format(ERROR_MSG_OPERATION, op.name()));
    }

    @Override
    public void project(ICause cause) throws ContradictionException {
        throw new UnsupportedOperationException(String.format(ERROR_MSG_OPERATION, op.name()));
    }

    private boolean isInteger(double value) {
        return Math.rint(value) == value;
    }
}
