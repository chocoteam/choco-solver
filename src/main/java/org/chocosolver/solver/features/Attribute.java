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
package org.chocosolver.solver.features;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 04/05/2016.
 */
public enum Attribute implements IAttribute{

    /**
     * Number of variables declared
     */
    NV {
        @Override
        public double evaluate(Model model) {
            return model.getNbVars();
        }

        @Override
        public String description() {
            return "Number of variables declared";
        }
    },

    /**
     * Number of constraints posted
     */
    NC {
        @Override
        public double evaluate(Model model) {
            return model.getNbCstrs();
        }

        @Override
        public String description() {
            return "Number of constraints declared";
        }
    },

    /**
     * Normalized mean constraints per variable
     */
    NMCPV {
        @Override
        public double evaluate(Model model) {
            double m = 0.0;
            int n = 0;
            for (Variable var : model.getVars()) {
                m += (var.getNbProps() - m) / (++n);
            }
            int p = 0;
            for (Constraint c : model.getCstrs()) {
                p += c.getPropagators().length;
            }
            return m / p;
        }

        @Override
        public String description() {
            return "Normalized mean constraints per variable";
        }
    },
    /**
     * Normalized mean unary constraints above all
     */
    NMUCAA {
        @Override
        public double evaluate(Model model) {
            double u = 0.0, a = 0.0;

            for (Constraint c : model.getCstrs()) {
                for (Propagator p : c.getPropagators()) {
                    a++;
                    if (p.getNbVars() == 1) {
                        u++;
                    }
                }
            }
            return u / a;
        }

        @Override
        public String description() {
            return "Normalized mean unary constraints above all";
        }
    },
    /**
     * Normalized mean binary constraints above all
     */
    NMBCAA {
        @Override
        public double evaluate(Model model) {
            double u = 0.0, a = 0.0;

            for (Constraint c : model.getCstrs()) {
                for (Propagator p : c.getPropagators()) {
                    a++;
                    if (p.getNbVars() == 2) {
                        u++;
                    }
                }
            }
            return u / a;
        }

        @Override
        public String description() {
            return "Normalized mean binary constraints above all";
        }
    },
    /**
     * Normalized mean ternary constraints above all
     */
    NMTCAA {
        @Override
        public double evaluate(Model model) {
            double u = 0.0, a = 0.0;

            for (Constraint c : model.getCstrs()) {
                for (Propagator p : c.getPropagators()) {
                    a++;
                    if (p.getNbVars() == 3) {
                        u++;
                    }
                }
            }
            return u / a;
        }

        @Override
        public String description() {
            return "Normalized mean ternary constraints above all";
        }
    },
    /**
     * Normalized mean nary constraints above all
     */
    NMNCAA {
        @Override
        public double evaluate(Model model) {
            double u = 0.0, a = 0.0;

            for (Constraint c : model.getCstrs()) {
                for (Propagator p : c.getPropagators()) {
                    a++;
                    if (p.getNbVars() > 3) {
                        u++;
                    }
                }
            }
            return u / a;
        }

        @Override
        public String description() {
            return "Normalized mean nary constraints above all";
        }
    },
    /**
     * Normalized mean variables per constraint
     */
    NMVPC {
        @Override
        public double evaluate(Model model) {
            double m = 0.0;
            int n = 0;
            for (Constraint c : model.getCstrs()) {
                for (Propagator p : c.getPropagators()) {
                    m += (p.getNbVars() - m) / (++n);
                }
            }
            return m / model.getNbVars();
        }

        @Override
        public String description() {
            return "Normalized mean variables per constraint";
        }
    },
    /**
     * Normalized mean decisions variables
     */
    NMDV {
        @Override
        public double evaluate(Model model) {
            AbstractStrategy strat = model.getSolver().getSearch();
            if (strat != null) {
                return strat.getVariables().length * 1.0 / model.getVars().length;
            } else return 1.0;

        }

        @Override
        public String description() {
            return "Normalized mean decisions variables";
        }
    };


    /**
     * @return the set of basic attributes
     */
    public Attribute[] basicAttributes(){
        return new Attribute[]{
                NV, NC
        };
    }

}
