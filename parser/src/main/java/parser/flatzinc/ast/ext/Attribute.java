/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package parser.flatzinc.ast.ext;

import solver.recorders.fine.arc.FineArcEventRecorder;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/10/12
 */
public enum Attribute implements IAttribute<FineArcEventRecorder> {

    VNAME {
        @Override
        public int evaluate(Pair p) {
            return p.var.getId();
        }

        @Override
        public int eval(FineArcEventRecorder p) {
            return p.getVariables()[0].getId();
        }

        @Override
        public boolean isDynamic() {
            return false;
        }

    }, VCARD {
        @Override
        public int evaluate(Pair p) {
            return p.var.getNbProps();
        }

        @Override
        public int eval(FineArcEventRecorder p) {
            return p.getVariables()[0].getNbProps();
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

    }, CNAME {
        @Override
        public int evaluate(Pair p) {
            return p.prop.getConstraint().hashCode();
        }

        @Override
        public int eval(FineArcEventRecorder p) {
            return p.getPropagators()[0].getConstraint().hashCode();
        }

        @Override
        public boolean isDynamic() {
            return false;
        }


    }, CARITY {
        @Override
        public int evaluate(Pair p) {
            return p.prop.getConstraint().getVariables().length;
        }

        @Override
        public int eval(FineArcEventRecorder p) {
            return p.getPropagators()[0].getConstraint().getVariables().length;
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

    }, PIDX {
        @Override
        public int evaluate(Pair p) {
            return p.prop.getId();
        }

        @Override
        public int eval(FineArcEventRecorder p) {
            return p.getPropagators()[0].getId();
        }

        @Override
        public boolean isDynamic() {
            return false;
        }


    }, PPRIO {
        @Override
        public int evaluate(Pair p) {
            return p.prop.getPriority().priority;
        }

        @Override
        public int eval(FineArcEventRecorder p) {
            return p.getPropagators()[0].getPriority().priority;
        }

        @Override
        public boolean isDynamic() {
            return false;
        }

    }, PARITY {
        @Override
        public int evaluate(Pair p) {
            return p.prop.getNbVars();
        }

        @Override
        public int eval(FineArcEventRecorder p) {
            return p.getPropagators()[0].getNbVars();
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

    }, PPRIOD {
        @Override
        public int evaluate(Pair p) {
            return p.prop.dynPriority();
        }

        @Override
        public int eval(FineArcEventRecorder p) {
            return p.getPropagators()[0].dynPriority();
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

    };

    public abstract int evaluate(Pair p);

    public abstract boolean isDynamic();

}
