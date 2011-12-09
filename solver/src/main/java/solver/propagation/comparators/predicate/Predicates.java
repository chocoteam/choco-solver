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
package solver.propagation.comparators.predicate;

import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.variables.Variable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/09/11
 */
public enum Predicates {
    ;

    public static Predicate and(Predicate p1, Predicate p2) {
        return new And(p1, p2);
    }

    /**
     * P1 and not P2
     *
     * @param p1
     * @param p2
     * @return
     */
    public static Predicate but(Predicate p1, Predicate p2) {
        return new But(p1, p2);
    }

    public static Predicate or(Predicate p1, Predicate p2) {
        return new Or(p1, p2);
    }

    public static Predicate all() {
        return new All();
    }

    public static Predicate light() {
        return new Light();
    }

    public static Predicate lhs() {
        return new LeftHandSide();
    }

    public static Predicate member(Constraint constraint) {
        return new EqualC(constraint);
    }

    public static Predicate member_light(Constraint constraint) {
        return new EqualCandLight(constraint);
    }

    public static Predicate member(Constraint[] constraint) {
        return new MemberC(constraint);
    }

    public static Predicate member_light(Constraint[] constraints) {
        return new MemberCAndLight(constraints);
    }

    public static Predicate member(Constraint cons0, Constraint... cons) {
        return new MemberC(cons0, cons);
    }

    public static Predicate member(Variable variable) {
        return new EqualV(variable);
    }

    public static Predicate member(Variable[] variables) {
        return new MemberV(variables);
    }

    public static Predicate member(Variable var0, Variable... vars) {
        return new MemberV(var0, vars);
    }

    public static Predicate member(Propagator propagator) {
        return new EqualP(propagator);
    }

    public static Predicate member(Propagator[] propagators) {
        return new MemberP(propagators);
    }

    public static Predicate member(Propagator prop0, Propagator... props) {
        return new MemberP(prop0, props);
    }

    public static Predicate priority(PropagatorPriority threhsold) {
        return new PriorityP(threhsold);
    }

    public static Predicate priority_light(PropagatorPriority threhsold) {
        return new PriorityPAndLight(threhsold);
    }
}
