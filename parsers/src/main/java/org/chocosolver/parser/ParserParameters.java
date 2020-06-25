/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import org.chocosolver.cutoffseq.GeometricalCutoffStrategy;
import org.chocosolver.cutoffseq.LubyCutoffStrategy;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.restart.MonotonicRestartStrategy;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/2020
 */
public class ParserParameters {

    public enum VarH {
        ABS {
            @Override
            void declare(Solver solver, IntVar[] vars, ValH valueSelector) {
                Model model = solver.getModel();
                vars[0].getModel().getSolver().setSearch(
                        new ActivityBased(model,
                                vars,
                                valueSelector == ValH.UNDEF ? null : valueSelector.make(solver),
                                0.999d,
                                0.2d,
                                8,
                                1,
                                model.getSeed())
                );
            }
        },
        DWDEG {
            @Override
            void declare(Solver solver, IntVar[] vars, ValH valueSelector) {
                vars[0].getModel().getSolver().setSearch(
                        new DomOverWDeg(vars,
                                solver.getModel().getSeed(),
                                valueSelector.make(solver))
                );
            }
        },
        DWDEGR {
            @Override
            void declare(Solver solver, IntVar[] vars, ValH valueSelector) {
                vars[0].getModel().getSolver().setSearch(
                        new DomOverWDegRef(vars,
                                solver.getModel().getSeed(),
                                valueSelector.make(solver),
                                "CACD")
                );
            }
        },
        CHS {
            @Override
            void declare(Solver solver, IntVar[] vars, ValH valueSelector) {
                vars[0].getModel().getSolver().setSearch(
                        new ConflictHistorySearch(vars,
                                solver.getModel().getSeed(),
                                valueSelector.make(solver))
                );
            }
        },
        IBS {
            @Override
            void declare(Solver solver, IntVar[] vars, ValH valueSelector) {
                vars[0].getModel().getSolver().setSearch(
                        new ImpactBased(vars,
                                valueSelector == ValH.UNDEF ? null : valueSelector.make(solver),
                                2,
                                512,
                                2048,
                                solver.getModel().getSeed(),
                                false)
                );
            }
        },
        INPUT {
            @Override
            void declare(Solver solver, IntVar[] vars, ValH valueSelector) {
                vars[0].getModel().getSolver().setSearch(
                        Search.intVarSearch(
                                new InputOrder<>(solver.getModel()),
                                valueSelector.make(solver),
                                vars)
                );
            }
        },
        UNDEF{
            @Override
            void declare(Solver solver, IntVar[] vars, ValH valueSelector) {
                // void
            }
        };

        abstract void declare(Solver solver, IntVar[] vars, ValH valueSelector);
    }

    public enum ValH {
        BEST {
            @Override
            IntValueSelector make(Solver solver) {
                if (solver.getModel().getResolutionPolicy() == ResolutionPolicy.SATISFACTION) {
                    return MIN.make(solver);
                }
                return new IntDomainBest();
            }
        },
        LAST {
            @Override
            IntValueSelector make(Solver solver) {
                Model model = solver.getModel();
                if (model.getResolutionPolicy() == ResolutionPolicy.SATISFACTION) {
                    return MIN.make(solver);
                }
                IntValueSelector valueSelector = new IntDomainBest();
                Solution lastSolution = new Solution(model, model.retrieveIntVars(true));
                model.getSolver().attach(lastSolution);
                return new IntDomainLast(lastSolution, valueSelector, null);
            }
        },
        MAX {
            @Override
            IntValueSelector make(Solver solver) {
                return new IntDomainMax();
            }
        },
        MIN {
            @Override
            IntValueSelector make(Solver solver) {
                return new IntDomainMin();
            }
        },
        RAND {
            @Override
            IntValueSelector make(Solver solver) {
                return new IntDomainRandom(solver.getModel().getSeed());
            }
        },
        UNDEF {
            @Override
            IntValueSelector make(Solver solver) {
                return MIN.make(solver);
            }
        };

        abstract IntValueSelector make(Solver solver);
    }

    public static class EnumConf {
        final VarH varh;
        final ValH valh;

        public EnumConf(VarH varh, ValH valh) {
            this.varh = varh;
            this.valh = valh;
        }
    }

    public enum ResPol {
        UNDEF {
            @Override
            void make(Solver solver, int cutoff, double geo, int offset) {
                // nothing to do
            }
        },
        MONOTONIC {
            @Override
            void make(Solver solver, int cutoff, double geo, int offset) {
                solver.setRestarts(
                        count -> solver.getFailCount() >= count,
                        new MonotonicRestartStrategy(cutoff),
                        offset
                );
                solver.setNoGoodRecordingFromRestarts();
            }
        },
        LUBY {
            @Override
            void make(Solver solver, int cutoff, double geo, int offset) {
                solver.setRestarts(
                        count -> solver.getFailCount() >= count,
                        new LubyCutoffStrategy(cutoff),
                        offset
                );
                solver.setNoGoodRecordingFromRestarts();
            }
        },
        GEOMETRIC {
            @Override
            void make(Solver solver, int cutoff, double geo, int offset) {
                solver.setRestarts(
                        count -> solver.getFailCount() >= count,
                        new GeometricalCutoffStrategy(cutoff, geo),
                        offset
                );
                solver.setNoGoodRecordingFromRestarts();
            }
        };

        abstract void make(Solver solver, int cutoff, double geo, int offset);
    }

    public static class ResConf {
        final ResPol pol;
        final int cutoff;
        final int offset;
        final double geo;

        public ResConf(ResPol pol, int cutoff, double geo, int offset) {
            this.pol = pol;
            this.cutoff = cutoff;
            this.offset = offset;
            this.geo = geo;
        }

        public ResConf(ResPol pol, int cutoff, int offset) {
            this(pol, cutoff, 1d, offset);
        }

        public void declare(Solver solver) {
            pol.make(solver, cutoff, geo, offset);
        }
    }


    public enum MetaH {
        NONE,
        LC1,
        LC2,
        LC3,
        LC4,
        LC5,
        LC6,
        LC7,
        LC8,
        LC9,
        COS;

        public void declare(Solver solver) {
            switch (this) {
                case NONE:
                default:
                    break;
                case COS:
                    solver.setSearch(Search.conflictOrderingSearch(solver.getSearch()));
                    break;
                case LC1:
                case LC2:
                case LC3:
                    int c = Integer.parseInt(this.toString().substring(2));
                    solver.setSearch(Search.lastConflict(solver.getSearch(), c));
                    break;
            }
        }
    }

    public static class LimConf{
        final long time; // in ms
        final int sols;
        final int runs;

        public LimConf(long time, int sols, int runs) {
            this.time = time;
            this.sols = sols;
            this.runs = runs;
        }
    }
}
