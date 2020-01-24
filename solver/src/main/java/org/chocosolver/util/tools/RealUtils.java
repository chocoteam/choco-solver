/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
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
 * @since 23/01/2020
 */
public class RealUtils {

    private static final double ZERO = 0.0;
    
    private static final double NEG_ZER0 = 0.0 * -1.0;

    public static double nextFloat(double x) {
        if (x < 0)
            return Double.longBitsToDouble(Double.doubleToLongBits(x) - 1);
        else if (x == 0)
            return Double.longBitsToDouble(1);
        else if (x < Double.POSITIVE_INFINITY)
            return Double.longBitsToDouble(Double.doubleToLongBits(x) + 1);
        else
            return x; // nextFloat(infty) = infty
    }

    public static double prevFloat(double x) {
        if (x == 0.0)
            return -nextFloat(0.0);
        else
            return -nextFloat(-x);
    }

    public static RealInterval add(RealInterval x, RealInterval y) {
        return new RealIntervalConstant(prevFloat(x.getLB() + y.getLB()), nextFloat(x.getUB() + y.getUB()));
    }

    public static RealInterval sub(RealInterval x, RealInterval y) {
        return new RealIntervalConstant(prevFloat(x.getLB() - y.getUB()), nextFloat(x.getUB() - y.getLB()));
    }

    public static RealInterval mul(RealInterval x, RealInterval y) {
        double i, s;

        if ((x.getLB() == 0.0 && x.getUB() == 0.0) || (y.getLB() == 0.0 && y.getUB() == 0.0)) {
            i = 0.0;
            s = NEG_ZER0; // Ca peut etre utile pour rejoindre des intervalles : si on veut aller de -5 a 0,
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
     * y should not contain 0 !
     *
     * @param x
     * @param y
     * @return TODO
     */
    public static RealInterval odiv(RealInterval x, RealInterval y) {
        if (y.getLB() <= 0.0 && y.getUB() >= 0.0) {
            throw new UnsupportedOperationException();
        } else {
            double yl = y.getLB();
            double yh = y.getUB();
            double i, s;
            if (yh == 0.0) yh = NEG_ZER0;

            if (x.getLB() >= 0.0) {
                if (yl >= 0.0) {
                    i = Math.max(ZERO, prevFloat(x.getLB() / yh));
                    s = nextFloat(x.getUB() / yl);
                } else { // yh <= 0
                    i = prevFloat(x.getUB() / yh);
                    s = Math.min(ZERO, nextFloat(x.getLB() / yl));
                }
            } else if (x.getUB() <= 0.0) {
                if (yl >= 0.0) {
                    i = prevFloat(x.getLB() / yl);
                    s = Math.min(ZERO, nextFloat(x.getUB() / yh));
                } else {
                    i = Math.max(ZERO, prevFloat(x.getUB() / yl));
                    s = nextFloat(x.getLB() / yh);
                }
            } else {
                if (yl >= 0.0) {
                    i = prevFloat(x.getLB() / yl);
                    s = nextFloat(x.getUB() / yl);
                } else {
                    i = prevFloat(x.getUB() / yh);
                    s = nextFloat(x.getLB() / yh);
                }
            }
            return new RealIntervalConstant(i, s);
        }
    }

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

    public static boolean isCanonical(RealInterval i, double precision) {
        double inf = i.getLB();
        double sup = i.getUB();
        if (sup - inf < precision) return true;
        if (nextFloat(inf) >= sup) return true;
        return false;
    }

    public static RealInterval firstHalf(RealInterval i) {
        double inf = i.getLB();
        if (inf == Double.NEGATIVE_INFINITY) inf = -Double.MAX_VALUE;
        double sup = i.getUB();
        if (sup == Double.POSITIVE_INFINITY) sup = Double.MAX_VALUE;
        return new RealIntervalConstant(i.getLB(), inf + sup / 2.0 - inf / 2.0);
    }

    public static RealInterval secondHalf(RealInterval i) {
        double inf = i.getLB();
        if (inf == Double.NEGATIVE_INFINITY) inf = -Double.MAX_VALUE;
        double sup = i.getUB();
        if (sup == Double.POSITIVE_INFINITY) sup = Double.MAX_VALUE;
        return new RealIntervalConstant(inf + sup / 2.0 - inf / 2.0, i.getUB());
    }

    public static double iPower_lo(double x, int p) {   // TODO : to check !
        // x >= 0 et p > 1 entier
        if (x == 0) return 0;
        if (x == 1) return 1;
        return prevFloat(Math.exp(prevFloat(p * prevFloat(Math.log(x)))));
    }

    public static double iPower_up(double x, int p) {
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

    public static RealInterval oddIPower(RealInterval i, int p) {
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

    public static double iRoot_lo(double x, int p) { // TODO : to check !!
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

    public static double iRoot_up(double x, int p) {
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

    public static RealInterval evenIRoot(RealInterval i, int p, RealInterval res) {
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

    public static RealInterval oddIRoot(RealInterval i, int p) {
        double inf, sup;
        if (i.getLB() >= 0)
            inf = iRoot_lo(i.getLB(), p);
        else
            inf = -iRoot_up(-i.getLB(), p);

        if (i.getUB() >= 0)
            sup = iRoot_up(i.getUB(), p);
        else
            sup = -iRoot_lo(-i.getUB(), p);
        return new RealIntervalConstant(inf, sup);
    }

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

    public static RealInterval sinRange(int a, int b) {
        switch (4 * a + b) {
            case 0:
                System.err.println("Erreur !");
                return null;
            case 1:
                return new RealIntervalConstant(1.0, 1.0);
            case 2:
                return new RealIntervalConstant(0.0, 1.0);
            case 3:
                System.err.println("Erreur !");
                return null;
            case 4:
                System.err.println("Erreur !");
                return null;
            case 5:
                System.err.println("Erreur !");
                return null;
            case 6:
                return new RealIntervalConstant(0.0, 0.0);
            case 7:
                return new RealIntervalConstant(-1.0, 0.0);
            case 8:
                return new RealIntervalConstant(-1.0, 0.0);
            case 9:
                System.err.println("Erreur !");
                return null;
            case 10:
                System.err.println("Erreur !");
                return null;
            case 11:
                return new RealIntervalConstant(-1.0, -1.0);
            case 12:
                return new RealIntervalConstant(0.0, 0.0);
            case 13:
                return new RealIntervalConstant(0.0, 1.0);
            case 14:
                System.err.println("Erreur !");
                return null;
            case 15:
                System.err.println("Erreur !");
                return null;
        }
        throw new UnsupportedOperationException();
    }

    public static RealInterval cos(RealInterval interval) {
        if (interval.getUB() - interval.getLB() > prevFloat(1.5 * prevFloat(Math.PI))) {
            return new RealIntervalConstant(-1.0, 1.0);
        }
        int nlo, nup;
        if (interval.getLB() >= 0)
            nlo = (int) Math.floor(prevFloat(prevFloat(interval.getLB() * 2.0) / nextFloat(Math.PI)));
        else
            nlo = (int) Math.floor(prevFloat(prevFloat(interval.getLB() * 2.0) / prevFloat(Math.PI)));
        if (interval.getUB() >= 0)
            nup = (int) Math.floor(nextFloat(nextFloat(interval.getUB() * 2.0) / prevFloat(Math.PI)));
        else
            nup = (int) Math.floor(nextFloat(nextFloat(interval.getUB() * 2.0) / nextFloat(Math.PI)));

        if ((((nup - nlo) % 4) + 4) % 4 == 3) return new RealIntervalConstant(-1.0, 1.0);

        double clo = Math.min(prevFloat(Math.cos(interval.getLB())), prevFloat(Math.cos(interval.getUB())));
        double cup = Math.max(nextFloat(Math.cos(interval.getLB())), nextFloat(Math.cos(interval.getUB())));

        if ((((nup - nlo) % 4) + 4) % 4 == 0) return new RealIntervalConstant(clo, cup);

        RealInterval mask = sinRange((((nlo + 1) % 4) + 4) % 4, (((nup + 1) % 4) + 4) % 4);
        if (mask.getLB() < clo) clo = mask.getLB();
        if (mask.getUB() > cup) cup = mask.getUB();

        return new RealIntervalConstant(clo, cup);
    }

    public static RealInterval sin(RealInterval interval) {
        if (interval.getUB() - interval.getLB() > prevFloat(1.5 * prevFloat(Math.PI))) {
            return new RealIntervalConstant(-1.0, 1.0);
        }
        int nlo, nup;
        if (interval.getLB() >= 0)
            nlo = (int) Math.floor(prevFloat(prevFloat(interval.getLB() * 2.0) / nextFloat(Math.PI)));
        else
            nlo = (int) Math.floor(prevFloat(prevFloat(interval.getLB() * 2.0) / prevFloat(Math.PI)));
        if (interval.getUB() >= 0)
            nup = (int) Math.floor(nextFloat(nextFloat(interval.getUB() * 2.0) / prevFloat(Math.PI)));
        else
            nup = (int) Math.floor(nextFloat(nextFloat(interval.getUB() * 2.0) / nextFloat(Math.PI)));

        if ((((nup - nlo) % 4) + 4) % 4 == 3) return new RealIntervalConstant(-1.0, 1.0);

        double clo = Math.min(prevFloat(Math.sin(interval.getLB())), prevFloat(Math.sin(interval.getUB())));
        double cup = Math.max(nextFloat(Math.sin(interval.getLB())), nextFloat(Math.sin(interval.getUB())));

        if ((((nup - nlo) % 4) + 4) % 4 == 0) return new RealIntervalConstant(clo, cup);

        RealInterval mask = sinRange(((nlo % 4) + 4) % 4, ((nup % 4) + 4) % 4);
        if (mask.getLB() < clo) clo = mask.getLB();
        if (mask.getUB() > cup) cup = mask.getUB();

        return new RealIntervalConstant(clo, cup);
    }

    public static RealInterval asin_wrt(RealInterval interval, RealInterval res) {
        double retSup = Double.POSITIVE_INFINITY, retInf = Double.NEGATIVE_INFINITY;
        double asinl = prevFloat(Math.asin(interval.getLB()));
        double asinu = nextFloat(Math.asin(interval.getUB()));

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

        if (interval.getLB() > -1.0) {
            if ((res.getLB() > nextFloat(nextFloat(-Math.PI) - asinl + decSup)) &&
                    (res.getLB() < prevFloat(asinl + decInf))) {
                retInf = prevFloat(asinl + decInf);
            }
            if ((res.getLB() > nextFloat(nextFloat(Math.PI) - asinl + decSup)) &&
                    (res.getLB() < prevFloat(asinl + 2 * prevFloat(Math.PI) + decInf))) {
                retInf = prevFloat(asinl + 2 * prevFloat(Math.PI) + decInf);
            }
        }

        if (interval.getUB() < 1.0) {
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

        if (interval.getLB() > -1.0) {
            if ((res.getUB() > nextFloat(nextFloat(-Math.PI) - asinl + decSup)) &&
                    (res.getUB() < prevFloat(asinl + decInf))) {
                retSup = nextFloat(nextFloat(-Math.PI) - asinl + decSup);
            }
            if ((res.getUB() > nextFloat(nextFloat(Math.PI) - asinl + decSup)) &&
                    (res.getUB() < prevFloat(asinl + 2 * prevFloat(Math.PI) + decInf))) {
                retSup = nextFloat(nextFloat(Math.PI) - asinl + decSup);
            }
        }

        if (interval.getUB() < 1.0) {
            if ((res.getUB() > asinu + decSup) &&
                    (res.getUB() < prevFloat(prevFloat(Math.PI) - asinu) + decInf)) {
                retSup = asinu + decSup;
            }
        }

        return new RealIntervalConstant(retInf, retSup);
    }

    public static RealInterval acos_wrt(RealInterval interval, RealInterval res) {
        double retSup = Double.POSITIVE_INFINITY, retInf = Double.NEGATIVE_INFINITY;
        double acosl = prevFloat(Math.acos(interval.getUB()));
        double acosu = nextFloat(Math.acos(interval.getLB()));

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

        if (interval.getUB() < 1.0) {
            if ((res.getLB() > nextFloat(decSup - acosl)) &&
                    (res.getLB() < prevFloat(decInf + acosl))) {
                retInf = prevFloat(decInf + acosl);
            }
            if ((res.getLB() > nextFloat(2 * nextFloat(Math.PI) - acosl + decSup)) &&
                    (res.getLB() < prevFloat(2 * prevFloat(Math.PI) + acosl + decInf))) {
                retInf = prevFloat(2 * prevFloat(Math.PI) + acosl + decInf);
            }
        }

        if (interval.getLB() > -1.0) {
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

        if (interval.getUB() < 1.0) {
            if ((res.getUB() > nextFloat(decSup - acosl)) &&
                    (res.getUB() < prevFloat(decInf + acosl))) {
                retSup = nextFloat(decSup - acosl);
            }
            if ((res.getUB() > nextFloat(2 * nextFloat(Math.PI) - acosl + decSup)) &&
                    (res.getUB() < prevFloat(2 * prevFloat(Math.PI) + acosl + decInf))) {
                retSup = nextFloat(2 * nextFloat(Math.PI) - acosl + decSup);
            }
        }

        if (interval.getLB() > -1.0) {
            if ((res.getUB() > nextFloat(acosu + decSup)) &&
                    (res.getUB() < prevFloat(2 * prevFloat(Math.PI) - acosu + decInf))) {
                retSup = nextFloat(acosu + decSup);
            }
        }

        return new RealIntervalConstant(retInf, retSup);
    }

}
