/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints;

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

	protected final Operator op1, op2; // operators.
	protected final int cste;
	protected final IntVar[] vars;

	private static boolean isOperation(Operator operator) {
		return operator.equals(Operator.PL) || operator.equals(Operator.MN);
	}

	public Arithmetic(IntVar var, Operator op, int cste) {
		super("ArithmeticUnary",createProp(var,op,cste));
		this.vars = new IntVar[]{var};
		this.op1 = op;
		this.op2 = Operator.NONE;
		this.cste = cste;
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

	public Arithmetic(IntVar var1, Operator op, IntVar var2) {
		super("ArithmeticBinary",createProp(var1,op,var2));
		this.vars = new IntVar[]{var1,var2};
		this.op1 = op;
		this.op2 = Operator.PL;
		this.cste = 0;
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

	public Arithmetic(IntVar var1, Operator op1, IntVar var2, Operator op2, int cste) {
		super("ArithmeticBinary",createProp(var1,op1,var2,op2,cste));
		this.vars = new IntVar[]{var1,var2};
		this.op1 = op1;
		this.op2 = op2;
		if (isOperation(op1) == isOperation(op2)) {
			throw new SolverException("Incorrect formula; operators must be different!");
		}
		this.cste = cste;
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
		} else if (op1 == Operator.MN) {
			switch (op2) {
				case EQ: // X-Y = C --> X = Y+C
					return new PropEqualX_YC(vars, cste);
				case NQ: // X-Y != C --> X != Y+C
					return new PropNotEqualX_YC(vars, cste);
				case GE: // X-Y >= C --> X >= Y+C
					return new PropGreaterOrEqualX_YC(vars, cste);
				case GT: // X-Y > C --> X >= Y+C+1
					return new PropGreaterOrEqualX_YC(vars, cste + 1);
				case LE:// X-Y <= C --> Y >= X-C
					return new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -cste);
				case LT:// X-Y < C --> Y >= X-C+1
					return new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -cste + 1);
				default:
					throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
			}
		} else {
			int _cste = cste * (op2 == Operator.PL ? 1 : -1);
			switch (op1) {
				case EQ:// X = Y + C
					return new PropEqualX_YC(vars, _cste);
				case NQ:// X =/= Y + C
					return new PropNotEqualX_YC(vars, _cste);
				case GE:// X >= Y + C
					return new PropGreaterOrEqualX_YC(vars, _cste);
				case GT:// X > Y + C --> X >= Y + C + 1
					return new PropGreaterOrEqualX_YC(vars, _cste + 1);
				case LE:// X <= Y + C --> Y >= X - C
					return new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -_cste);
				case LT:// X < Y + C --> Y > X - C + 1
					return new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -_cste + 1);
				default:
					throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
			}
		}
	}

	@Override
	public Constraint makeOpposite(){
		if(vars.length==1){
			return ICF.arithm(vars[0],Operator.getOpposite(op1).toString(),cste);
		}else{
			assert vars.length==2;
			if(op1==Operator.PL || op1==Operator.MN){
				return ICF.arithm(vars[0],op1.toString(),vars[1],Operator.getOpposite(op2).toString(),cste);
			}else{
				return ICF.arithm(vars[0],Operator.getOpposite(op1).toString(),vars[1],op2.toString(),cste);
			}
		}
	}
}
