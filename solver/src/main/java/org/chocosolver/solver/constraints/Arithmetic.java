/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.binary.*;
import org.chocosolver.solver.constraints.unary.PropEqualXC;
import org.chocosolver.solver.constraints.unary.PropGreaterOrEqualXC;
import org.chocosolver.solver.constraints.unary.PropLessOrEqualXC;
import org.chocosolver.solver.constraints.unary.PropNotEqualXC;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;

/**
 * A constraint dedicated to arithmetic operations.
 * <br/>
 * There are three available definitions:
 * <li>
 * <ul>VAR op CSTE,</ul>
 * <ul>VAR op VAR,</ul>
 * <ul>VAR op VAR op CSTE</ul>
 * </li>
 * where VAR is a variable, CSTE a constant and op is an operator among {"=", "!=","<", ">", "<=, ">="} or{"+", "-"}.
 *
 * @author Charles Prud'homme
 * @since 21/06/12
 */
public class Arithmetic extends Constraint {

	private final Operator op1, op2; // operators.
    // required visibility to allow exportation
	protected final int cste;
	private final IntVar[] vars;

	private static boolean isOperation(Operator operator) {
		return operator.equals(Operator.PL) || operator.equals(Operator.MN);
	}

	// for JSON
	@SuppressWarnings("WeakerAccess")
	protected Arithmetic(String name, IntVar[] vars, Operator op1, Operator op2, int cste, Propagator prop){
		super(name, prop);
		this.vars = vars;
		this.op1 = op1;
		this.op2 = op2;
		this.cste = cste;
	}

	Arithmetic(IntVar var, Operator op, int cste) {
		this(ConstraintsName.ARITHM, new IntVar[]{var}, op, Operator.NONE, cste, createProp(var,op,cste));
	}

	private static Propagator createProp(IntVar var, Operator op, int cste) {
		switch (op) {
			case EQ: // X = C
				return new PropEqualXC(var, cste);
			case NQ: // X =/= C
				return new PropNotEqualXC(var, cste);
			case GE: // X >= C
				return new PropGreaterOrEqualXC(var, cste);
			case GT: // X > C -->  X >= C + 1
				return new PropGreaterOrEqualXC(var, cste + 1);
			case LE: // X <= C
				return new PropLessOrEqualXC(var, cste);
			case LT: // X < C --> X <= C - 1
				return new PropLessOrEqualXC(var, cste - 1);
			default:
				throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
		}
	}

	Arithmetic(IntVar var1, Operator op, IntVar var2) {
		this(ConstraintsName.ARITHM, new IntVar[]{var1,var2}, op, Operator.PL, 0, createProp(var1,op,var2));
	}

	private static Propagator createProp(IntVar var1, Operator op, IntVar var2) {
		switch (op) {
			case EQ: // X = Y
				return new PropEqualX_Y(var1, var2);
			case NQ: // X =/= Y
				return new PropNotEqualX_Y(var1, var2);
			case GE: //  X >= Y
				return new PropGreaterOrEqualX_Y(new IntVar[]{var1,var2});
			case GT: //  X > Y --> X >= Y + 1
				return new PropGreaterOrEqualX_YC(new IntVar[]{var1,var2}, 1);
			case LE: //  X <= Y --> Y >= X
				return new PropGreaterOrEqualX_Y(new IntVar[]{var2, var1});
			case LT: //  X < Y --> Y >= X + 1
				return new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, 1);
			default:
				throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
		}
	}

	Arithmetic(IntVar var1, Operator op1, IntVar var2, Operator op2, int cste) {
		this(ConstraintsName.ARITHM, new IntVar[]{var1,var2}, op1, op2, cste, createProp(var1,op1,var2,op2,cste));
        if (isOperation(op1) == isOperation(op2)) {
            throw new SolverException("Incorrect formula; operators must be different!");
        }
	}

	private static Propagator createProp(IntVar var1, Operator op1, IntVar var2, Operator op2, int cste) {
		if(op1==null)throw new UnsupportedOperationException();
		if(op2==null)throw new UnsupportedOperationException();
		if (isOperation(op1) == isOperation(op2)) {
			throw new SolverException("Incorrect formula; operators must be different!");
		}
		IntVar[] vars = new IntVar[]{var1,var2};
		if (op1 == Operator.PL) {
			switch (op2) {
				case EQ: // X+Y = C
					return new PropEqualXY_C(vars, cste);
				case NQ: // X+Y != C
					return new PropNotEqualXY_C(vars, cste);
				case GE: // X+Y >= C
					return new PropGreaterOrEqualXY_C(vars, cste);
				case GT: // X+Y > C --> X+Y >= C+1
					return new PropGreaterOrEqualXY_C(vars, cste + 1);
				case LE: // X+Y <= C
					return new PropLessOrEqualXY_C(vars, cste);
				case LT: // X+Y < C --> X+Y <= C-1
					return new PropLessOrEqualXY_C(vars, cste - 1);
				default:
					throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
			}
		} else{
			Operator op = op2;
			if (op1 != Operator.MN) {
				cste *= (op2 == Operator.PL ? 1 : -1);
				op = op1;
			}
			switch (op) {
				case EQ: // X-Y = C --> X = Y+C
					if(cste == 0)return new PropEqualX_Y(vars[0], vars[1]);
					return new PropEqualX_YC(vars, cste);
				case NQ: // X-Y != C --> X != Y+C
					if(cste == 0)return new PropNotEqualX_Y(vars[0], vars[1]);
					return new PropNotEqualX_YC(vars, cste);
				case GE: // X-Y >= C --> X >= Y+C
					if(cste == 0)return new PropGreaterOrEqualX_Y(vars);
					return new PropGreaterOrEqualX_YC(vars, cste);
				case GT: // X-Y > C --> X >= Y+C+1
                    if(cste  == -1)return new PropGreaterOrEqualX_Y(vars);
					return new PropGreaterOrEqualX_YC(vars, cste + 1);
				case LE:// X-Y <= C --> Y >= X-C
					if(cste == 0)return new PropGreaterOrEqualX_Y(new IntVar[]{var2, var1});
					return new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -cste);
				case LT:// X-Y < C --> Y >= X-C+1
                    if(cste == 1) return new PropGreaterOrEqualX_Y(new IntVar[]{var2, var1});
					return new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -cste + 1);
				default:
					throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
			}
		}
	}

	@Override
	public Constraint makeOpposite(){
		Model model = vars[0].getModel();
		if(vars.length==1){
			return model.arithm(vars[0],Operator.getOpposite(op1).toString(),cste);
		}else{
			assert vars.length==2;
			if(op1==Operator.PL || op1==Operator.MN){
				return model.arithm(vars[0],op1.toString(),vars[1],Operator.getOpposite(op2).toString(),cste);
			}else{
				return model.arithm(vars[0],Operator.getOpposite(op1).toString(),vars[1],op2.toString(),cste);
			}
		}
	}
}
