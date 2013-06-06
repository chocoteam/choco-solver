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
package solver.constraints;

import solver.Solver;
import solver.constraints.binary.*;
import solver.constraints.unary.PropEqualXC;
import solver.constraints.unary.PropGreaterOrEqualXC;
import solver.constraints.unary.PropLessOrEqualXC;
import solver.constraints.unary.PropNotEqualXC;
import solver.exception.SolverException;
import solver.variables.IntVar;
import util.ESat;

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
public class Arithmetic extends IntConstraint<IntVar> {

    protected final Operator op1, op2; // operators.
    protected final int cste;
    protected final boolean isBinary; // to distinct unary and binary formula

    private static boolean isOperation(Operator operator) {
        return operator.equals(Operator.PL) || operator.equals(Operator.MN);
    }

    public Arithmetic(IntVar var, Operator op, int cste, Solver solver) {
        super(new IntVar[]{var}, solver);
        this.op1 = op;
        this.op2 = Operator.NONE;
        this.cste = cste;
        this.isBinary = false;
        switch (op1) {
            case EQ: // X = C
                setPropagators(new PropEqualXC(var, cste));
                break;
            case NQ: // X =/= C
                setPropagators(new PropNotEqualXC(var, cste));
                break;
            case GE: // X >= C
                setPropagators(new PropGreaterOrEqualXC(var, cste));
                break;
            case GT: // X > C -->  X >= C + 1
                setPropagators(new PropGreaterOrEqualXC(var, cste + 1));
                break;
            case LE: // X <= C
                setPropagators(new PropLessOrEqualXC(var, cste));
                break;
            case LT: // X < C --> X <= C - 1
                setPropagators(new PropLessOrEqualXC(var, cste - 1));
                break;
            default:
                throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
        }
    }

    public Arithmetic(IntVar var1, Operator op, IntVar var2, Solver solver) {
        super(new IntVar[]{var1, var2}, solver);
        this.op1 = op;
        this.op2 = Operator.PL;
        this.cste = 0;
        this.isBinary = true;
        switch (op1) {
            case EQ: // X = Y
                setPropagators(new PropEqualX_Y(var1, var2));
                break;
            case NQ: // X =/= Y
                setPropagators(new PropNotEqualX_Y(var1, var2));
                break;
            case GE: //  X >= Y
                setPropagators(new PropGreaterOrEqualX_Y(vars));
                break;
            case GT: //  X > Y --> X >= Y + 1
                setPropagators(new PropGreaterOrEqualX_YC(vars, 1));
                break;
            case LE: //  X <= Y --> Y >= X
                setPropagators(new PropGreaterOrEqualX_Y(new IntVar[]{var2, var1}));
                break;
            case LT: //  X < Y --> Y >= X + 1
                setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, 1));
                break;
            default:
                throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
        }
    }

    public Arithmetic(IntVar var1, Operator op1, IntVar var2, Operator op2, int cste, Solver solver) {
        super(new IntVar[]{var1, var2}, solver);
        this.op1 = op1;
        this.op2 = op2;
        if (isOperation(op1) == isOperation(op2)) {
            throw new SolverException("Incorrect formula; operators must be different!");
        }
        this.cste = cste;
        this.isBinary = true;
        if (op1 == Operator.PL) {
            switch (op2) {
                case EQ: // X+Y = C
                    setPropagators(new PropEqualXY_C(vars, cste));
                    break;
                case NQ: // X+Y != C
                    setPropagators(new PropNotEqualXY_C(vars, cste));
                    break;
                case GE: // X+Y >= C
                    setPropagators(new PropGreaterOrEqualXY_C(vars, cste));
                    break;
                case GT: // X+Y > C --> X+Y >= C+1
                    setPropagators(new PropGreaterOrEqualXY_C(vars, cste + 1));
                    break;
                case LE: // X+Y <= C
                    setPropagators(new PropLessOrEqualXY_C(vars, cste));
                    break;
                case LT: // X+Y < C --> X+Y <= C-1
                    setPropagators(new PropLessOrEqualXY_C(vars, cste - 1));
                    break;
                default:
                    throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
            }
        } else if (op1 == Operator.MN) {
            switch (op2) {
                case EQ: // X-Y = C --> X = Y+C
                    setPropagators(new PropEqualX_YC(vars, cste));
                    break;
                case NQ: // X-Y != C --> X != Y+C
                    setPropagators(new PropNotEqualX_YC(vars, cste));
                    break;
                case GE: // X-Y >= C --> X >= Y+C
                    setPropagators(new PropGreaterOrEqualX_YC(vars, cste));
                    break;
                case GT: // X-Y > C --> X >= Y+C+1
                    setPropagators(new PropGreaterOrEqualX_YC(vars, cste + 1));
                    break;
                case LE:// X-Y <= C --> Y >= X-C
                    setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -cste));
                    break;
                case LT:// X-Y < C --> Y >= X-C+1
                    setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -cste + 1));
                    break;
                default:
                    throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
            }
        } else {
            int _cste = cste * (op2 == Operator.PL ? 1 : -1);
            switch (op1) {
                case EQ:// X = Y + C
                    setPropagators(new PropEqualX_YC(vars, _cste));
                    break;
                case NQ:// X =/= Y + C
                    setPropagators(new PropNotEqualX_YC(vars, _cste));
                    break;
                case GE:// X >= Y + C
                    setPropagators(new PropGreaterOrEqualX_YC(vars, _cste));
                    break;
                case GT:// X > Y + C --> X >= Y + C + 1
                    setPropagators(new PropGreaterOrEqualX_YC(vars, _cste + 1));
                    break;
                case LE:// X <= Y + C --> Y >= X - C
                    setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -_cste));
                    break;
                case LT:// X < Y + C --> Y > X - C + 1
                    setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -_cste + 1));
                    break;
                default:
                    throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
            }
        }
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        if (!isBinary) {
            switch (op1) {
                case EQ:
                    return ESat.eval(tuple[0] == cste);
                case NQ:
                    return ESat.eval(tuple[0] != cste);
                case GE:
                    return ESat.eval(tuple[0] >= cste);
                case GT:
                    return ESat.eval(tuple[0] > cste);
                case LE:
                    return ESat.eval(tuple[0] <= cste);
                case LT:
                    return ESat.eval(tuple[0] < cste);
                default:
                    return ESat.UNDEFINED;

            }
        } else {
            if (op1 == Operator.PL) {
                switch (op2) {
                    case EQ:
                        return ESat.eval(tuple[0] + tuple[1] == cste);
                    case NQ:
                        return ESat.eval(tuple[0] + tuple[1] != cste);
                    case GE:
                        return ESat.eval(tuple[0] + tuple[1] >= cste);
                    case GT:
                        return ESat.eval(tuple[0] + tuple[1] > cste);
                    case LE:
                        return ESat.eval(tuple[0] + tuple[1] <= cste);
                    case LT:
                        return ESat.eval(tuple[0] + tuple[1] < cste);
                    default:
                        return ESat.UNDEFINED;
                }
            } else if (op1 == Operator.MN) {
                switch (op2) {
                    case EQ:
                        return ESat.eval(tuple[0] == tuple[1] + cste);
                    case NQ:
                        return ESat.eval(tuple[0] != tuple[1] + cste);
                    case GE:
                        return ESat.eval(tuple[0] >= tuple[1] + cste);
                    case GT:
                        return ESat.eval(tuple[0] > tuple[1] + cste);
                    case LE:
                        return ESat.eval(tuple[0] <= tuple[1] + cste);
                    case LT:
                        return ESat.eval(tuple[0] < tuple[1] + cste);
                    default:
                        return ESat.UNDEFINED;
                }
            } else {
                int _cste = cste * (op2 == Operator.PL ? 1 : -1);
                switch (op1) {
                    case EQ:
                        return ESat.eval(tuple[0] == tuple[1] + _cste);
                    case NQ:
                        return ESat.eval(tuple[0] != tuple[1] + _cste);
                    case GE:
                        return ESat.eval(tuple[0] >= tuple[1] + _cste);
                    case GT:
                        return ESat.eval(tuple[0] > tuple[1] + _cste);
                    case LE:
                        return ESat.eval(tuple[0] <= tuple[1] + _cste);
                    case LT:
                        return ESat.eval(tuple[0] < tuple[1] + _cste);
                    default:
                        return ESat.UNDEFINED;
                }
            }
        }
    }

    @Override
    public String toString() {
        if (isBinary) {
            return vars[0].getName() + " " + op1 + " " + vars[1].getName() + " " + op2 + " " + cste;
        } else {
            return vars[0].getName() + " " + op1 + " " + cste;
        }
    }

	@Override
	public Constraint makeOpposite(){
		if(vars.length==1){
			switch (op1) {
				case EQ:
					return new Arithmetic(vars[0],Operator.NQ,cste,solver);
				case NQ:
					return new Arithmetic(vars[0],Operator.EQ,cste,solver);
				case GE:
					return new Arithmetic(vars[0],Operator.LT,cste,solver);
				case GT:
					return new Arithmetic(vars[0],Operator.LE,cste,solver);
				case LE:
					return new Arithmetic(vars[0],Operator.GT,cste,solver);
				case LT:
					return new Arithmetic(vars[0],Operator.GE,cste,solver);
				default:
					throw new UnsupportedOperationException();
			}
		}else{
			assert vars.length==2;
			switch (op1) {
				case EQ:
					return new Arithmetic(vars[0],Operator.NQ,vars[1],op2,cste,solver);
				case NQ:
					return new Arithmetic(vars[0],Operator.EQ,vars[1],op2,cste,solver);
				case GE:
					return new Arithmetic(vars[0],Operator.LT,vars[1],op2,cste,solver);
				case GT:
					return new Arithmetic(vars[0],Operator.LE,vars[1],op2,cste,solver);
				case LE:
					return new Arithmetic(vars[0],Operator.GT,vars[1],op2,cste,solver);
				case LT:
					return new Arithmetic(vars[0],Operator.GE,vars[1],op2,cste,solver);
				default:
					switch (op2) {
						case EQ:
							return new Arithmetic(vars[0],op1,vars[1],Operator.NQ,cste,solver);
						case NQ:
							return new Arithmetic(vars[0],op1,vars[1],Operator.EQ,cste,solver);
						case GE:
							return new Arithmetic(vars[0],op1,vars[1],Operator.LT,cste,solver);
						case GT:
							return new Arithmetic(vars[0],op1,vars[1],Operator.LE,cste,solver);
						case LE:
							return new Arithmetic(vars[0],op1,vars[1],Operator.GT,cste,solver);
						case LT:
							return new Arithmetic(vars[0],op1,vars[1],Operator.GE,cste,solver);
						default:
							throw new UnsupportedOperationException();
					}
			}
		}
	}
}