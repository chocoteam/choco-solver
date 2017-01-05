/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
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
