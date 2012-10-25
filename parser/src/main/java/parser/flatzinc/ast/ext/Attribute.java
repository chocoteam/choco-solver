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

import parser.flatzinc.FZNException;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/10/12
 */
public enum Attribute implements Comparator {

    VIDX {
        @Override
        public int evaluate(Pair p) {
            return p.var.getId();
        }

        @Override
        public int compare(Pair p1, Pair p2) {
            return p1.var.getId() - p2.var.getId();
        }
    }, VCARD {
        @Override
        public int evaluate(Pair p) {
            return p.var.getNbProps();
        }

        @Override
        public int compare(Pair p1, Pair p2) {
            return p1.var.getNbProps() - p2.var.getNbProps();
        }
    }, CIDX {
        @Override
        public int evaluate(Pair p) {
            return p.prop.getId();
        }

        @Override
        public int compare(Pair p1, Pair p2) {
            return p1.prop.getId() - p2.prop.getId();
        }

    }, CARITY {
        @Override
        public int evaluate(Pair p) {
            return p.prop.getConstraint().getVariables().length;
        }

        @Override
        public int compare(Pair p1, Pair p2) {
            return p1.prop.getConstraint().getVariables().length - p2.prop.getConstraint().getVariables().length;
        }
    }, PIDX {
        @Override
        public int evaluate(Pair p) {
            return p.prop.getId();
        }

        @Override
        public int compare(Pair p1, Pair p2) {
            return p1.prop.getId() - p2.prop.getId();
        }

    }, PPRIO {
        @Override
        public int evaluate(Pair p) {
            return p.prop.getPriority().priority;
        }

        @Override
        public int compare(Pair p1, Pair p2) {
            return p1.prop.getPriority().priority - p2.prop.getPriority().priority;
        }
    }, PARITY {
        @Override
        public int evaluate(Pair p) {
            return p.prop.getNbVars();
        }

        @Override
        public int compare(Pair p1, Pair p2) {
            return p1.prop.getNbVars() - p2.prop.getNbVars();
        }
    }, PPRIOD {
        @Override
        public int evaluate(Pair p) {
            return p.prop.dynPriority();
        }

        @Override
        public int compare(Pair p1, Pair p2) {
            return p1.prop.dynPriority() - p2.prop.dynPriority();
        }
    };

    public abstract int evaluate(Pair p);

    public int evaluate(ArrayList list) {
        int value = -1;
        Object o = list.get(0);
        if (o instanceof Pair) {
            value = evaluate((Pair) o);
        } else {
            value = evaluate((ArrayList) o);
        }
        for (int i = 1; i < list.size(); i++) {
            int _v = value;
            o = list.get(i);
            if (o instanceof Pair) {
                value = evaluate((Pair) o);
            } else {
                value = evaluate((ArrayList) o);
            }
            if (_v != value) {
                return -1;
            }
        }
        return value;
    }

    public int evaluate(Object o) {
        int value = -1;
        if (o instanceof Pair) {
            value = evaluate((Pair) o);
        } else {
            value = evaluate((ArrayList) o);
        }
        return value;
    }


    @Override
    public int compare(Object o1, Object o2) {
        //return 0;
        if (o1 instanceof Pair && o2 instanceof Pair) {
            return compare((Pair) o1, (Pair) o2);
        }

        throw new FZNException("cannot compare " + o1 + " and " + o2);
    }

    public int compare(ArrayList l1, ArrayList l2) {
        throw new FZNException("cannot compare " + l1 + " and " + l2);
    }

    public abstract int compare(Pair p1, Pair p2);

}
