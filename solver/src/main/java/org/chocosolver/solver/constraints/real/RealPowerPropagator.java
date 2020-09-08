package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.expression.continuous.arithmetic.RealIntervalConstant;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.events.RealEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.RealInterval;
import org.chocosolver.util.tools.RealUtils;

/**
 * It's a pure java alternative to ibex that must be used to calculate
 * power operations (y = x^k) with integer positive exponents (k).
 *
 * <p>
 * Just in case, this is an external library for interval arithmetic.
 * Reference: https://jitpack.io/p/jinterval/jinterval#readme
 * <p>
 * This implementation is based from the chapter 16.1 "From Discrete to
 * Continuous Constraints", example 16.2 [1].
 * <p>
 * [1] Francesca Rossi, Peter van Beek, and Toby Walsh. 2006. Handbook of
 * Constraint Programming. Elsevier Science Inc., USA.
 *
 * @author João Pedro Schmitt
 * @since 08/09/2020
 */
public class RealPowerPropagator extends Propagator<RealVar> {

    private RealVar yVar;

    private RealVar xVar;

    private int k;

    public RealPowerPropagator(RealVar yVar, RealVar xVar, int k) {
        super(new RealVar[]{yVar, xVar}, PropagatorPriority.BINARY, false);
        this.yVar = vars[0];
        this.xVar = vars[1];
        this.k = k;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return RealEventType.BOUND.getMask();
    }

    @Override
    public ESat isEntailed() {
        ESat result = ESat.UNDEFINED;
        if (isCompletelyInstantiated()) {
            double lb = yVar.roundLB() - Math.pow(xVar.roundLB(), k);
            double ub = yVar.roundUB() - Math.pow(xVar.roundUB(), k);
            result = ESat.eval(lb <= 0 && 0 <= ub);
        }
        return result;
    }

    @Override
    public final void propagate(int evtmask) throws ContradictionException {
        boolean hasChanged = true;
        // Projection + propagation
        while (hasChanged) {
            hasChanged = kPower();
            hasChanged |= kRoot();
        }
    }

    private boolean kPower() throws ContradictionException {
        RealInterval x = new RealIntervalConstant(xVar.getLB(), xVar.getUB());
        RealInterval y = new RealIntervalConstant(yVar.getLB(), yVar.getUB());
        RealInterval power = RealUtils.iPower(x, k); // y = x^k
        // For even power, y will range [0, +Inf]
        // For odd power, y will range [-Inf, +Inf]
        // So it doesn't require special handling to update the bounds
        y = RealUtils.updateBounds(y, power);
        return yVar.updateBounds(y.getLB(), y.getUB(), this);
    }

    private boolean kRoot() throws ContradictionException {
        boolean isIntervalPositive = xVar.roundLB() >= 0;
        boolean isIntervalNegative = xVar.roundUB() <= 0;
        RealInterval x = new RealIntervalConstant(xVar.getLB(), xVar.getUB());
        RealInterval y = new RealIntervalConstant(yVar.getLB(), yVar.getUB());
        RealInterval root = RealUtils.iRoot(y, k); // x = root(y, k)
        boolean isEven = k % 2 == 0;
        // In all cases, bounds ar calculate as intersection between x and temp
        if (isEven) {
            if (isIntervalPositive) {
                x = RealUtils.updateBounds(x, root.getLB(), root.getUB());
            } else if (isIntervalNegative) {
                x = RealUtils.updateBounds(x, -root.getUB(), -root.getLB());
            } else { // It's crossing zero
                x = RealUtils.updateBounds(x, -root.getUB(), root.getUB());
            }
        } else {
            // For odd roots isn't necessary specific handling as roots can be negative naturally
            x = RealUtils.updateBounds(x, root);
        }
        return xVar.updateLowerBound(x.getLB(), this) |
                xVar.updateUpperBound(x.getUB(), this);
    }

}