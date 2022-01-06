/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import org.chocosolver.solver.expression.continuous.arithmetic.RealIntervalConstant;
import org.chocosolver.util.objects.RealInterval;

/**
 * Some tools for float computing.
 * Inspired from IAMath : interval.sourceforge.net
 * <br/>
 *
 * @author Charles Prud'homme
 * @author Guillaum Rochart
 * @since 23/01/2020
 */
public class RealUtils {

    private static final double ZERO = 0.0;

    private static final double NEG_ZER0 = 0.0 * -1.0;

    /**
     * Returns the double value just after 'x'.
     * @param x a double
     * @return the floating point just after 'x'.
     */
    public static double nextFloat(double x) {
        if (x < 0) {
            return Double.longBitsToDouble(Double.doubleToLongBits(x) - 1);
        } else if (x == 0) {
            return Double.longBitsToDouble(1);
        } else if (x < Double.POSITIVE_INFINITY) {
            return Double.longBitsToDouble(Double.doubleToLongBits(x) + 1);
        } else {
            return x; // nextFloat(infty) = infty
        }
    }

    /**
     * Returns the double value just before 'x'.
     * @param x a double
     * @return the floating point just before 'x'.
     */
    public static double prevFloat(double x) {
        if (x == 0.0) {
            return -nextFloat(0.0);
        } else {
            return -nextFloat(-x);
        }
    }

    /**
     * Returns an interval that represents the result of an addition between interval 'x' and 'y'.
     * <p>[l(x)+l(y), u(x)+u(y)]</p>
     * @param x an interval
     * @param y an interval
     * @return an interval that represents the result of the addition 'x + y'
     */
    public static RealInterval add(RealInterval x, RealInterval y) {
        return new RealIntervalConstant(prevFloat(x.getLB() + y.getLB()), nextFloat(x.getUB() + y.getUB()));
    }

    /**
     * Returns an interval that represents the result of a difference between interval 'x' and 'y'.
     * <p>[l(x)-u(y), u(x)-l(y)]</p>
     * @param x an interval
     * @param y an interval
     * @return an interval that represents the result of the difference : 'x - y'
     */
    public static RealInterval sub(RealInterval x, RealInterval y) {
        return new RealIntervalConstant(prevFloat(x.getLB() - y.getUB()), nextFloat(x.getUB() - y.getLB()));
    }

    /**
     * Returns an interval that represents the result of a multiplication between interval 'x' and 'y'.
     * The results depends on whether 'x' or 'y' overlap 0.0 or not.
     * @param x an interval
     * @param y an interval
     * @return an interval that represents the result of the multiplication : 'x * y'
     */
    public static RealInterval mul(RealInterval x, RealInterval y) {
        double i, s;

        if ((x.getLB() == 0.0 && x.getUB() == 0.0) || (y.getLB() == 0.0 && y.getUB() == 0.0)) {
            i = NEG_ZER0; // Ca peut etre utile pour rejoindre des intervalles : si on veut aller de -5 a 0,
            s = 0.0;
            // ca sera 0-.
        } else {
            if (x.getLB() >= 0.0) {
                if (y.getLB() >= 0.0) {
                    i = Math.max(ZERO, prevFloat(x.getLB() * y.getLB())); // Si x et y positifs, on ne veut pas etre n?gatif !
                    s = nextFloat(x.getUB() * y.getUB());
                } else if (y.getUB() <= 0.0) {
                    i = prevFloat(x.getUB() * y.getLB());
                    s = Math.min(ZERO, nextFloat(x.getLB() * y.getUB()));
                } else {
                    i = prevFloat(x.getUB() * y.getLB());
                    s = nextFloat(x.getUB() * y.getUB());
                }
            } else if (x.getUB() <= 0.0) {
                if (y.getLB() >= 0.0) {
                    i = prevFloat(x.getLB() * y.getUB());
                    s = Math.min(ZERO, nextFloat(x.getUB() * y.getLB()));
                } else if (y.getUB() <= 0.0) {
                    i = Math.max(ZERO, prevFloat(x.getUB() * y.getUB()));
                    s = nextFloat(x.getLB() * y.getLB());
                } else {
                    i = prevFloat(x.getLB() * y.getUB());
                    s = nextFloat(x.getLB() * y.getLB());
                }
            } else {
                if (y.getLB() >= 0.0) {
                    i = prevFloat(x.getLB() * y.getUB());
                    s = nextFloat(x.getUB() * y.getUB());
                } else if (y.getUB() <= 0.0) {
                    i = prevFloat(x.getUB() * y.getLB());
                    s = nextFloat(x.getLB() * y.getLB());
                } else {
                    i = Math.min(prevFloat(x.getLB() * y.getUB()),
                            prevFloat(x.getUB() * y.getLB()));
                    s = Math.max(nextFloat(x.getLB() * y.getLB()),
                            nextFloat(x.getUB() * y.getUB()));
                }
            }
        }
        return new RealIntervalConstant(i, s);
    }

    /**
     * Returns an interval that represents the result of a division of 'x' by 'y'.
     * The results depends on whether 'x' or 'y' overlap 0.0 or not.
     * @param x an interval
     * @param y an interval
     * @return an interval that represents the result of the division : 'x / y'.
     */
    public static RealInterval odiv(RealInterval x, RealInterval y) {
        if (y.getLB() >= 0.0 && y.getUB() <= 0.0) {
            throw new ArithmeticException("the divisor is 0");
        } else {
            double yl = y.getLB();
            double yh = y.getUB();
            double i, s;
            i = Double.NEGATIVE_INFINITY;
            s = Double.POSITIVE_INFINITY;
            if (yh == 0.0) yh = NEG_ZER0;

            if (x.getLB() >= 0.0) {
                if (yl >= 0.0) {
                    i = Math.max(ZERO, prevFloat(x.getLB() / yh));
                    s = nextFloat(x.getUB() / yl);
                } else if (yh <= 0.0) { // yh <= 0
                    i = prevFloat(x.getUB() / yh);
                    s = Math.min(ZERO, nextFloat(x.getLB() / yl));
                } // else skip : 0 in y
            } else if (x.getUB() <= 0.0) {
                if (yl >= 0.0) {
                    i = prevFloat(x.getLB() / yl);
                    s = Math.min(ZERO, nextFloat(x.getUB() / yh));
                } else if (yh <= 0.0) { // yh <= 0
                    i = Math.max(ZERO, prevFloat(x.getUB() / yl));
                    s = nextFloat(x.getLB() / yh);
                } // else skip : 0 in y
            } else {
                if (yl >= 0.0) {
                    i = prevFloat(x.getLB() / yl);
                    s = nextFloat(x.getUB() / yl);
                } else if (yh <= 0.0) { // yh <= 0
                    i = prevFloat(x.getUB() / yh);
                    s = nextFloat(x.getLB() / yh);
                } // else skip : 0 in y
            }
            return new RealIntervalConstant(i, s);
        }
    }

    /**
     * Returns an interval that represents the result of a division of 'x' by 'y'.
     * 'res' is the one that will intersect the resulting interval
     * and is given to provide sharpest interval when 0.0 is overlapped.
     * @param x an interval
     * @param y an interval
     * @return an interval that represents the result of the division : 'x / y'.
     */
    public static RealInterval odiv_wrt(RealInterval x, RealInterval y, RealInterval res) {
        if (y.getLB() > 0.0 || y.getUB() < 0.0) {  // y != 0
            return odiv(x, y);
        } else {
            double resl = res.getLB();
            double resh = res.getUB();

            if (x.getLB() >= 0.0) {
                double tmp_neg = nextFloat(x.getLB() / y.getLB()); // la plus grande valeur negative
                double tmp_pos = prevFloat(x.getLB() / y.getUB()); // la plus petite valeur positive

                if ((resl > tmp_neg || resl == 0.0) && resl < tmp_pos) resl = tmp_pos;
                if ((resh < tmp_pos || resh == 0.0) && resh > tmp_neg) resh = tmp_neg;
            } else if (x.getUB() <= 0.0) {
                double tmp_neg = nextFloat(x.getUB() / y.getUB());
                double tmp_pos = nextFloat(x.getUB() / y.getLB());

                if ((resl > tmp_neg || resl == 0.0) && resl < tmp_pos) resl = tmp_pos;
                if ((resh < tmp_pos || resh == 0.0) && resh > tmp_neg) resh = tmp_neg;
            }
            return new RealIntervalConstant(resl, resh);
        }
    }

    /**
     * Given an interval 'i = [a,b]' returns an interval [a, a + (b-a)/2].
     * @param i an interval
     * @return the first half of 'i'
     */
    public static RealInterval firstHalf(RealInterval i) {
        double inf = i.getLB();
        if (inf == Double.NEGATIVE_INFINITY) {
            inf = -Double.MAX_VALUE;
        }
        double sup = i.getUB();
        if (sup == Double.POSITIVE_INFINITY) {
            sup = Double.MAX_VALUE;
        }
        return new RealIntervalConstant(i.getLB(), inf + sup / 2.0 - inf / 2.0);
    }

    /**
     * Given an interval 'i = [a,b]' returns an interval [a + (b-a)/2, b].
     * @param i an interval
     * @return the second half of 'i'
     */
    public static RealInterval secondHalf(RealInterval i) {
        double inf = i.getLB();
        if (inf == Double.NEGATIVE_INFINITY) {
            inf = -Double.MAX_VALUE;
        }
        double sup = i.getUB();
        if (sup == Double.POSITIVE_INFINITY) {
            sup = Double.MAX_VALUE;
        }
        return new RealIntervalConstant(inf + sup / 2.0 - inf / 2.0, i.getUB());
    }

    private static double iPower_lo(double x, int p) {   // TODO : to check !
        // x >= 0 et p > 1 entier
        if (x == 0) return 0;
        if (x == 1) return 1;
        return prevFloat(Math.exp(prevFloat(p * prevFloat(Math.log(x)))));
    }

    private static double iPower_up(double x, int p) {
        if (x == 0) return 0;
        if (x == 1) return 1;
        return nextFloat(Math.exp(nextFloat(p * nextFloat(Math.log(x)))));
    }

    private static RealInterval evenIPower(RealInterval i, int p) {
        double inf, sup;
        if (i.getLB() >= 0.0) {
            if (i.getLB() == Double.POSITIVE_INFINITY) {
                inf = Double.POSITIVE_INFINITY;
                sup = Double.POSITIVE_INFINITY;
            } else {
                inf = iPower_lo(i.getLB(), p);
                if (i.getUB() == Double.POSITIVE_INFINITY) {
                    sup = Double.POSITIVE_INFINITY;
                } else {
                    sup = iPower_up(i.getUB(), p);
                }
            }
        } else if (i.getUB() <= 0.0) {
            if (i.getUB() == Double.NEGATIVE_INFINITY) {
                inf = Double.POSITIVE_INFINITY;
                sup = Double.POSITIVE_INFINITY;
            } else {
                inf = iPower_lo(-i.getUB(), p);
                if (i.getLB() == Double.NEGATIVE_INFINITY) {
                    sup = Double.POSITIVE_INFINITY;
                } else {
                    sup = iPower_up(-i.getLB(), p);
                }
            }
        } else {
            inf = 0;
            if (i.getLB() == Double.NEGATIVE_INFINITY ||
                    i.getUB() == Double.POSITIVE_INFINITY) {
                sup = Double.POSITIVE_INFINITY;
            } else {
                sup = Math.max(iPower_up(-i.getLB(), p),
                        iPower_up(i.getUB(), p));
            }
        }
        return new RealIntervalConstant(inf, sup);
    }

    private static RealInterval oddIPower(RealInterval i, int p) {
        double inf, sup;
        if (i.getLB() >= 0.0) {
            if (i.getLB() == Double.POSITIVE_INFINITY) {
                inf = Double.POSITIVE_INFINITY;
                sup = Double.POSITIVE_INFINITY;
            } else {
                inf = iPower_lo(i.getLB(), p);
                if (i.getUB() == Double.POSITIVE_INFINITY) {
                    sup = Double.POSITIVE_INFINITY;
                } else {
                    sup = iPower_up(i.getUB(), p);
                }
            }
        } else if (i.getUB() <= 0.0) {
            if (i.getUB() == Double.NEGATIVE_INFINITY) {
                inf = Double.NEGATIVE_INFINITY;
                sup = Double.NEGATIVE_INFINITY;
            } else {
                sup = -iPower_lo(-i.getUB(), p);
                if (i.getLB() == Double.NEGATIVE_INFINITY) {
                    inf = Double.NEGATIVE_INFINITY;
                } else {
                    inf = -iPower_up(-i.getLB(), p);
                }
            }
        } else {
            if (i.getLB() == Double.NEGATIVE_INFINITY) {
                inf = Double.NEGATIVE_INFINITY;
            } else {
                inf = -iPower_up(-i.getLB(), p);
            }
            if (i.getUB() == Double.POSITIVE_INFINITY) {
                sup = Double.POSITIVE_INFINITY;
            } else {
                sup = iPower_up(i.getUB(), p);
            }
        }
        return new RealIntervalConstant(inf, sup);
    }

    /**
     * Returns an interval that represents the result of 'i^p', where 'p' is an integer.
     * The results depends on whether 'p' is odd or even.
     * @param i an interval
     * @param p an integer
     * @return an interval that represents the result of : 'i^p'.
     */
    public static RealInterval iPower(RealInterval i, int p) {
        if (p <= 1) {
            throw new UnsupportedOperationException();
        }
        if (p % 2 == 0) { // pair
            return evenIPower(i, p);
        } else { // impair
            return oddIPower(i, p);
        }
    }

    private static double iRoot_lo(double x, int p) { // TODO : to check !!
        double d_lo = prevFloat(1.0 / (double) p);
        double d_hi = nextFloat(1.0 / (double) p);
        if (x == Double.POSITIVE_INFINITY) {
            return Double.POSITIVE_INFINITY;
        } else if (x == 0)
            return 0;
        else if (x == 1)
            return 1;
        else if (x < 1)
            return prevFloat(Math.exp(prevFloat(d_hi * prevFloat(Math.log(x)))));
        else
            return prevFloat(Math.exp(prevFloat(d_lo * prevFloat(Math.log(x)))));
    }

    private static double iRoot_up(double x, int p) {
        double d_lo = prevFloat(1.0 / (double) p);
        double d_hi = nextFloat(1.0 / (double) p);
        if (x == Double.POSITIVE_INFINITY) {
            return Double.POSITIVE_INFINITY;
        } else if (x == 0)
            return 0;
        else if (x == 1)
            return 1;
        else if (x < 1)
            return nextFloat(Math.exp(nextFloat(d_lo * nextFloat(Math.log(x)))));
        else
            return nextFloat(Math.exp(nextFloat(d_hi * nextFloat(Math.log(x)))));
    }

    private static RealInterval evenIRoot(RealInterval i, int p) {
        if (i.getUB() < 0) {
            System.err.println("Erreur !!");
        }
        double inf = i.getLB() < 0. ? 0. : iRoot_lo(i.getLB(), p);
        double sup = iRoot_up(i.getUB(), p);
        return new RealIntervalConstant(inf, sup);
    }

    private static RealInterval evenIRoot(RealInterval i, int p, RealInterval res) {
            if (i.getUB() < 0) {
                System.err.println("Erreur !!");
            }
            double inf, sup;
            if (i.getLB() < 0)
                inf = 0;
            else
                inf = iRoot_lo(i.getLB(), p);
            sup = iRoot_up(i.getUB(), p);

            if (res.getUB() < inf)
                return new RealIntervalConstant(-sup, -inf);
            else if (res.getLB() > sup)
                return new RealIntervalConstant(inf, sup);
            else
                return new RealIntervalConstant(-sup, sup);
        }

    private static RealInterval oddIRoot(RealInterval i, int p) {
        double inf, sup;
        if (i.getLB() >= 0) {
            inf = iRoot_lo(i.getLB(), p);
        } else {
            inf = -iRoot_up(-i.getLB(), p);
        }
        if (i.getUB() >= 0) {
            sup = iRoot_up(i.getUB(), p);
        } else {
            sup = -iRoot_lo(-i.getUB(), p);
        }
        return new RealIntervalConstant(inf, sup);
    }

    /**
     * Returns an interval that represents the result of 'i^(1/p)', where 'p' is an integer.
     * The results depends on whether 'p' is odd or even.
     * @param i an interval
     * @param p an integer
     * @return an interval that represents the result of : 'i^(1/p)'.
     */
    public static RealInterval iRoot(RealInterval i, int p) {
        if (p <= 1) {
            throw new UnsupportedOperationException();
        }
        if (p % 2 == 0) {
            return evenIRoot(i, p);
        } else {
            return oddIRoot(i, p);
        }
    }

    /**
     * Returns an interval that represents the result of 'i^(1/p)', where 'p' is an integer.
     * The results depends on whether 'p' is odd or even.
     * 'res' is the one that will intersect the resulting interval
     * and is given to provide sharpest interval when 0.0 is overlapped.
     * @param i an interval
     * @param p an integer
     * @param res interval that will intersect the resulting interval
     * @return an interval that represents the result of : 'i^(1/p)'.
     */
    public static RealInterval iRoot(RealInterval i, int p, RealInterval res) {
        if (p <= 1) {
            throw new UnsupportedOperationException();
        }
        if (p % 2 == 0) {
            return evenIRoot(i, p, res);
        } else {
            return oddIRoot(i, p);
        }
    }

    private static RealInterval sinRange(int a, int b) {
        switch (4 * a + b) {
            case 1:
                return new RealIntervalConstant(1.0, 1.0);
            case 2:
            case 13:
                return new RealIntervalConstant(0.0, 1.0);
            case 6:
            case 12:
                return new RealIntervalConstant(0.0, 0.0);
            case 7:
            case 8:
                return new RealIntervalConstant(-1.0, 0.0);
            case 11:
                return new RealIntervalConstant(-1.0, -1.0);
            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns an interval that represents the result of 'cos(i)'.
     * @param i an interval
     * @return the result of 'cos(i)'
     */
    public static RealInterval cos(RealInterval i) {
        if (i.getUB() - i.getLB() > prevFloat(1.5 * prevFloat(Math.PI))) {
            return new RealIntervalConstant(-1.0, 1.0);
        }
        int nlo, nup;
        if (i.getLB() >= 0) {
            nlo = (int) Math.floor(prevFloat(prevFloat(i.getLB() * 2.0) / nextFloat(Math.PI)));
        } else {
            nlo = (int) Math.floor(prevFloat(prevFloat(i.getLB() * 2.0) / prevFloat(Math.PI)));
        }
        if (i.getUB() >= 0) {
            nup = (int) Math.floor(nextFloat(nextFloat(i.getUB() * 2.0) / prevFloat(Math.PI)));
        } else {
            nup = (int) Math.floor(nextFloat(nextFloat(i.getUB() * 2.0) / nextFloat(Math.PI)));
        }
        if ((((nup - nlo) % 4) + 4) % 4 == 3) {
            return new RealIntervalConstant(-1.0, 1.0);
        }
        double clo = Math.min(prevFloat(Math.cos(i.getLB())), prevFloat(Math.cos(i.getUB())));
        double cup = Math.max(nextFloat(Math.cos(i.getLB())), nextFloat(Math.cos(i.getUB())));
        if ((((nup - nlo) % 4) + 4) % 4 == 0) {
            return new RealIntervalConstant(clo, cup);
        }
        RealInterval mask = sinRange((((nlo + 1) % 4) + 4) % 4, (((nup + 1) % 4) + 4) % 4);
        if (mask.getLB() < clo) {
            clo = mask.getLB();
        }
        if (mask.getUB() > cup) {
            cup = mask.getUB();
        }
        return new RealIntervalConstant(clo, cup);
    }

    /**
     * Returns an interval that represents the result of 'sin(i)'.
     * @param i an interval
     * @return the result of 'sin(i)'
     */
    public static RealInterval sin(RealInterval i) {
        if (i.getUB() - i.getLB() > prevFloat(1.5 * prevFloat(Math.PI))) {
            return new RealIntervalConstant(-1.0, 1.0);
        }
        int nlo, nup;
        if (i.getLB() >= 0) {
            nlo = (int) Math.floor(prevFloat(prevFloat(i.getLB() * 2.0) / nextFloat(Math.PI)));
        } else {
            nlo = (int) Math.floor(prevFloat(prevFloat(i.getLB() * 2.0) / prevFloat(Math.PI)));
        }
        if (i.getUB() >= 0) {
            nup = (int) Math.floor(nextFloat(nextFloat(i.getUB() * 2.0) / prevFloat(Math.PI)));
        } else {
            nup = (int) Math.floor(nextFloat(nextFloat(i.getUB() * 2.0) / nextFloat(Math.PI)));
        }
        if ((((nup - nlo) % 4) + 4) % 4 == 3) {
            return new RealIntervalConstant(-1.0, 1.0);
        }
        double clo = Math.min(prevFloat(Math.sin(i.getLB())), prevFloat(Math.sin(i.getUB())));
        double cup = Math.max(nextFloat(Math.sin(i.getLB())), nextFloat(Math.sin(i.getUB())));

        if ((((nup - nlo) % 4) + 4) % 4 == 0) {
            return new RealIntervalConstant(clo, cup);
        }
        RealInterval mask = sinRange(((nlo % 4) + 4) % 4, ((nup % 4) + 4) % 4);
        if (mask.getLB() < clo) {
            clo = mask.getLB();
        }
        if (mask.getUB() > cup) {
            cup = mask.getUB();
        }
        return new RealIntervalConstant(clo, cup);
    }

    /**
     * Returns an interval that represents the result of a division of 'asin(i)'.
     * 'res' is the one that will intersect the resulting interval
     * and is given to provide sharpest interval.
     * @param i an interval
     * @param res an interval
     * @return an interval that represents the result of the division : 'asin(i)'.
     */
    public static RealInterval asin_wrt(RealInterval i, RealInterval res) {
        double retSup = Double.POSITIVE_INFINITY, retInf = Double.NEGATIVE_INFINITY;
        double asinl = prevFloat(Math.asin(i.getLB()));
        double asinu = nextFloat(Math.asin(i.getUB()));

        // Lower bound
        int modSup = (int) Math.floor((res.getLB() + nextFloat(Math.PI)) / prevFloat(2 * Math.PI));
        double decSup, decInf;

        if (modSup < 0) {
            decSup = nextFloat(2 * modSup * prevFloat(Math.PI));
            decInf = prevFloat(2 * modSup * nextFloat(Math.PI));
        } else if (modSup > 0) {
            decSup = nextFloat(2 * modSup * nextFloat(Math.PI));
            decInf = prevFloat(2 * modSup * prevFloat(Math.PI));
        } else {
            decSup = 0.0;
            decInf = 0.0;
        }

        if (i.getLB() > -1.0) {
            if ((res.getLB() > nextFloat(nextFloat(-Math.PI) - asinl + decSup)) &&
                    (res.getLB() < prevFloat(asinl + decInf))) {
                retInf = prevFloat(asinl + decInf);
            }
            if ((res.getLB() > nextFloat(nextFloat(Math.PI) - asinl + decSup)) &&
                    (res.getLB() < prevFloat(asinl + 2 * prevFloat(Math.PI) + decInf))) {
                retInf = prevFloat(asinl + 2 * prevFloat(Math.PI) + decInf);
            }
        }

        if (i.getUB() < 1.0) {
            if ((res.getLB() > asinu + decSup) &&
                    (res.getLB() < prevFloat(prevFloat(Math.PI) - asinu) + decInf)) {
                retInf = prevFloat(prevFloat(Math.PI) - asinu) + decInf;
            }
        }

        // Upper bound
        modSup = (int) Math.floor((res.getUB() + nextFloat(Math.PI)) / prevFloat(2 * Math.PI));

        if (modSup < 0) {
            decSup = nextFloat(2 * modSup * prevFloat(Math.PI));
            decInf = prevFloat(2 * modSup * nextFloat(Math.PI));
        } else if (modSup > 0) {
            decSup = nextFloat(2 * modSup * nextFloat(Math.PI));
            decInf = prevFloat(2 * modSup * prevFloat(Math.PI));
        } else {
            decSup = 0.0;
            decInf = 0.0;
        }

        if (i.getLB() > -1.0) {
            if ((res.getUB() > nextFloat(nextFloat(-Math.PI) - asinl + decSup)) &&
                    (res.getUB() < prevFloat(asinl + decInf))) {
                retSup = nextFloat(nextFloat(-Math.PI) - asinl + decSup);
            }
            if ((res.getUB() > nextFloat(nextFloat(Math.PI) - asinl + decSup)) &&
                    (res.getUB() < prevFloat(asinl + 2 * prevFloat(Math.PI) + decInf))) {
                retSup = nextFloat(nextFloat(Math.PI) - asinl + decSup);
            }
        }

        if (i.getUB() < 1.0) {
            if ((res.getUB() > asinu + decSup) &&
                    (res.getUB() < prevFloat(prevFloat(Math.PI) - asinu) + decInf)) {
                retSup = asinu + decSup;
            }
        }

        return new RealIntervalConstant(retInf, retSup);
    }

    /**
     * Returns an interval that represents the result of a division of 'acos(i)'.
     * 'res' is the one that will intersect the resulting interval
     * and is given to provide sharpest.
     * @param i an interval
     * @param res an interval
     * @return an interval that represents the result of the division : 'acos(i)'.
     */
    public static RealInterval acos_wrt(RealInterval i, RealInterval res) {
        double retSup = Double.POSITIVE_INFINITY, retInf = Double.NEGATIVE_INFINITY;
        double acosl = prevFloat(Math.acos(i.getUB()));
        double acosu = nextFloat(Math.acos(i.getLB()));

        // Lower bound
        int modSup = (int) Math.floor(res.getLB() / prevFloat(2 * Math.PI));
        double decSup, decInf;

        if (modSup < 0) {
            decSup = nextFloat(2 * modSup * prevFloat(Math.PI));
            decInf = prevFloat(2 * modSup * nextFloat(Math.PI));
        } else if (modSup > 0) {
            decSup = nextFloat(2 * modSup * nextFloat(Math.PI));
            decInf = prevFloat(2 * modSup * prevFloat(Math.PI));
        } else {
            decSup = 0.0;
            decInf = 0.0;
        }

        if (i.getUB() < 1.0) {
            if ((res.getLB() > nextFloat(decSup - acosl)) &&
                    (res.getLB() < prevFloat(decInf + acosl))) {
                retInf = prevFloat(decInf + acosl);
            }
            if ((res.getLB() > nextFloat(2 * nextFloat(Math.PI) - acosl + decSup)) &&
                    (res.getLB() < prevFloat(2 * prevFloat(Math.PI) + acosl + decInf))) {
                retInf = prevFloat(2 * prevFloat(Math.PI) + acosl + decInf);
            }
        }

        if (i.getLB() > -1.0) {
            if ((res.getLB() > nextFloat(acosu + decSup)) &&
                    (res.getLB() < prevFloat(2 * prevFloat(Math.PI) - acosu + decInf))) {
                retInf = prevFloat(2 * prevFloat(Math.PI) - acosu + decInf);
            }
        }

        // Upper bound
        modSup = (int) Math.floor(res.getUB() / prevFloat(2 * Math.PI));

        if (modSup < 0) {
            decSup = nextFloat(2 * modSup * prevFloat(Math.PI));
            decInf = prevFloat(2 * modSup * nextFloat(Math.PI));
        } else if (modSup > 0) {
            decSup = nextFloat(2 * modSup * nextFloat(Math.PI));
            decInf = prevFloat(2 * modSup * prevFloat(Math.PI));
        } else {
            decSup = 0.0;
            decInf = 0.0;
        }

        if (i.getUB() < 1.0) {
            if ((res.getUB() > nextFloat(decSup - acosl)) &&
                    (res.getUB() < prevFloat(decInf + acosl))) {
                retSup = nextFloat(decSup - acosl);
            }
            if ((res.getUB() > nextFloat(2 * nextFloat(Math.PI) - acosl + decSup)) &&
                    (res.getUB() < prevFloat(2 * prevFloat(Math.PI) + acosl + decInf))) {
                retSup = nextFloat(2 * nextFloat(Math.PI) - acosl + decSup);
            }
        }

        if (i.getLB() > -1.0) {
            if ((res.getUB() > nextFloat(acosu + decSup)) &&
                    (res.getUB() < prevFloat(2 * prevFloat(Math.PI) - acosu + decInf))) {
                retSup = nextFloat(acosu + decSup);
            }
        }

        return new RealIntervalConstant(retInf, retSup);
    }

}
