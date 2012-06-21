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

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.propagators.binary.*;
import solver.constraints.propagators.unary.PropEqualXC;
import solver.constraints.propagators.unary.PropGreaterOrEqualXC;
import solver.constraints.propagators.unary.PropLessOrEqualXC;
import solver.constraints.propagators.unary.PropNotEqualXC;
import solver.exception.SolverException;
import solver.variables.IntVar;

/**
 * A constraint dedicated to arithmetic operations.
 * <br/>
 * There are three available definitions:
 * <li>
 * <ul>VAR op CSTE,</ul>
 * <ul>VAR op VAR,</ul>
 * <ul>VAR op VAR op CSTE</ul>
 * </li>
 * where VAR is a variable, CSTE a constante and op is an operator among {"==", "=/=","<", ">", "<=, ">="} or{"+", "-"}.
 *
 * @author Charles Prud'homme
 * @since 21/06/12
 */
public class Arithmetic extends IntConstraint<IntVar> {
    public static final String
            eq = "=",
            nq = "!=",
            gq = ">=",
            gt = ">",
            lq = "<=",
            lt = "<",
            plus = "+",
            minus = "-";

    private static final String none = "#";

    protected final String op1, op2; // operators.
    protected final int cste;
    protected final boolean isBinary; // to distinct unary and binary formula

    private static boolean isOperation(String operator) {
        return operator.equals(plus) || operator.equals(minus);
    }


    public Arithmetic(IntVar var, String op, int cste, Solver solver) {
        super(new IntVar[]{var}, solver);
        this.op1 = op;
        this.op2 = none;
        this.cste = cste;
        this.isBinary = false;
        if (op1.equals(eq)) {
            setPropagators(new PropEqualXC(var, cste, solver, this));
        } else if (op1.equals(nq)) {
            setPropagators(new PropNotEqualXC(var, cste, solver, this));
        } else if (op1.equals(gq)) {
            setPropagators(new PropGreaterOrEqualXC(var, cste, solver, this));
        } else if (op1.equals(gt)) {
            setPropagators(new PropGreaterOrEqualXC(var, cste + 1, solver, this));
        } else if (op1.equals(lq)) {
            setPropagators(new PropLessOrEqualXC(var, cste, solver, this));
        } else if (op1.equals(lt)) {
            setPropagators(new PropLessOrEqualXC(var, cste - 1, solver, this));
        } else {
            throw new SolverException("Incorrect formula; operator should be one of those:{==, =/=, >=, >, <=, <}");
        }
    }

    public Arithmetic(IntVar var1, String op, IntVar var2, Solver solver) {
        super(new IntVar[]{var1, var2}, solver);
        this.op1 = op;
        this.op2 = plus;
        this.cste = 0;
        this.isBinary = true;
        if (op1.equals(eq)) {
            setPropagators(new PropEqualX_Y(var1, var2, solver, this));
        } else if (op1.equals(nq)) {
            setPropagators(new PropNotEqualX_Y(var1, var2, solver, this));
        } else if (op1.equals(gq)) {
            setPropagators(new PropGreaterOrEqualX_Y(vars, solver, this));
        } else if (op1.equals(gt)) {
            setPropagators(new PropGreaterOrEqualX_YC(vars, 1, solver, this));
        } else if (op1.equals(lq)) {
            setPropagators(new PropGreaterOrEqualX_Y(new IntVar[]{var2, var1}, solver, this));
        } else if (op1.equals(lt)) {
            setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -1, solver, this));
        } else {
            throw new SolverException("Incorrect formula; operator should be one of those:{==, =/=, >=, >, <=, <}");
        }
    }

    public Arithmetic(IntVar var1, String op1, IntVar var2, String op2, int cste, Solver solver) {
        super(new IntVar[]{var1, var2}, solver);
        this.op1 = op1;
        this.op2 = op2;
        if (isOperation(op1) == isOperation(op2)) {
            throw new SolverException("Incorrect formula; operators must be different!");
        }
        this.cste = cste;
        this.isBinary = true;
        if (op1.equals(plus)) {
            if (op2.equals(eq)) {
                setPropagators(new PropEqualXY_C(vars, cste, solver, this));
            } else if (op2.equals(nq)) {
                setPropagators(new PropNotEqualXY_C(vars, cste, solver, this));
            } else if (op2.equals(gq)) {
                setPropagators(new PropGreaterOrEqualXY_C(vars, cste, solver, this));
            } else if (op2.equals(gt)) {
                setPropagators(new PropGreaterOrEqualXY_C(vars, cste + 1, solver, this));
            } else if (op2.equals(lq)) {
                setPropagators(new PropLessOrEqualXY_C(vars, cste, solver, this));
            } else if (op2.equals(lt)) {
                setPropagators(new PropLessOrEqualXY_C(vars, cste - 1, solver, this));
            } else {
                throw new SolverException("Incorrect formula; operator should be one of those:{==, =/=, >=, >, <=, <}");
            }
        } else if (op1.equals(minus)) {
            if (op2.equals(eq)) {
                setPropagators(new PropEqualX_YC(vars, cste, solver, this));
            } else if (op2.equals(nq)) {
                setPropagators(new PropNotEqualX_YC(vars, cste, solver, this));
            } else if (op2.equals(gq)) {
                setPropagators(new PropGreaterOrEqualX_YC(vars, cste, solver, this));
            } else if (op2.equals(gt)) {
                setPropagators(new PropGreaterOrEqualX_YC(vars, cste + 1, solver, this));
            } else if (op2.equals(lq)) {
                setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -cste, solver, this));
            } else if (op2.equals(lt)) {
                setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -cste + 1, solver, this));
            } else {
                throw new SolverException("Incorrect formula; operator should be one of those:{==, =/=, >=, >, <=, <}");
            }
        } else {
            int _cste = cste * (op2.equals(plus) ? 1 : -1);
            if (op1.equals(eq)) {
                setPropagators(new PropEqualX_YC(vars, _cste, solver, this));
            } else if (op1.equals(nq)) {
                setPropagators(new PropNotEqualX_YC(vars, _cste, solver, this));
            } else if (op1.equals(gq)) {
                setPropagators(new PropGreaterOrEqualX_YC(vars, _cste, solver, this));
            } else if (op1.equals(gt)) {
                setPropagators(new PropGreaterOrEqualX_YC(vars, _cste + 1, solver, this));
            } else if (op1.equals(lq)) {
                setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -_cste, solver, this));
            } else if (op1.equals(lt)) {
                setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -_cste + 1, solver, this));
            } else {
                throw new SolverException("Incorrect formula; operator should be one of those:{==, =/=, >=, >, <=, <}");
            }
        }
    }


    @Override
    public ESat isSatisfied(int[] tuple) {
        if (!isBinary) {
            if (op1.equals(eq)) {
                return ESat.eval(tuple[0] == cste);
            } else if (op1.equals(nq)) {
                return ESat.eval(tuple[0] != cste);
            } else if (op1.equals(gq)) {
                return ESat.eval(tuple[0] >= cste);
            } else if (op1.equals(gt)) {
                return ESat.eval(tuple[0] > cste);
            } else if (op1.equals(lq)) {
                return ESat.eval(tuple[0] <= cste);
            } else if (op1.equals(lt)) {
                return ESat.eval(tuple[0] < cste);
            }
            return ESat.UNDEFINED;
        } else {
            if (op1.equals(plus)) {
                if (op2.equals(eq)) {
                    return ESat.eval(tuple[0] + tuple[1] == cste);
                } else if (op2.equals(nq)) {
                    return ESat.eval(tuple[0] + tuple[1] != cste);
                } else if (op2.equals(gq)) {
                    return ESat.eval(tuple[0] + tuple[1] >= cste);
                } else if (op2.equals(gt)) {
                    return ESat.eval(tuple[0] + tuple[1] > cste);
                } else if (op2.equals(lq)) {
                    return ESat.eval(tuple[0] + tuple[1] <= cste);
                } else if (op2.equals(lt)) {
                    return ESat.eval(tuple[0] + tuple[1] < cste);
                }
                return ESat.UNDEFINED;
            } else if (op1.equals(minus)) {
                if (op2.equals(eq)) {
                    return ESat.eval(tuple[0] == tuple[1] + cste);
                } else if (op2.equals(nq)) {
                    return ESat.eval(tuple[0] != tuple[1] + cste);
                } else if (op2.equals(gq)) {
                    return ESat.eval(tuple[0] >= tuple[1] + cste);
                } else if (op2.equals(gt)) {
                    return ESat.eval(tuple[0] > tuple[1] + cste);
                } else if (op2.equals(lq)) {
                    return ESat.eval(tuple[0] <= tuple[1] + cste);
                } else if (op2.equals(lt)) {
                    return ESat.eval(tuple[0] < tuple[1] + cste);
                }
                return ESat.UNDEFINED;
            } else {
                int _cste = cste * (op2.equals(plus) ? 1 : -1);
                if (op1.equals(eq)) {
                    return ESat.eval(tuple[0] == tuple[1] + _cste);
                } else if (op1.equals(nq)) {
                    return ESat.eval(tuple[0] != tuple[1] + _cste);
                } else if (op1.equals(gq)) {
                    return ESat.eval(tuple[0] >= tuple[1] + _cste);
                } else if (op1.equals(gt)) {
                    return ESat.eval(tuple[0] > tuple[1] + _cste);
                } else if (op1.equals(lq)) {
                    return ESat.eval(tuple[0] <= tuple[1] + _cste);
                } else if (op1.equals(lt)) {
                    return ESat.eval(tuple[0] < tuple[1] + _cste);
                }
                return ESat.UNDEFINED;
            }
        }
    }
}
