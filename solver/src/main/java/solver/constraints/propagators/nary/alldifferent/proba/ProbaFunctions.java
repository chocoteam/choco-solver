package solver.constraints.propagators.nary.alldifferent.proba;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 */
public class ProbaFunctions {

    public static final double[] fact = fact(26);

    private static double[] fact(int n) {
        double[] tabRes = new double[n];
        tabRes[0] = 1;
        int idx = 1;
        while (idx < n) {
            tabRes[idx] = tabRes[idx - 1] * (idx + 1);
            idx++;
        }
        return tabRes;
    }

    public static double probaAfterOther(double m, double n) {
        return (m - n < (2 * Math.sqrt(m))) ? 0 : 1;
    }

    public static double probaAfterInst(double m, double n, double v, double al, double be) {
        if (v < 0 || al < 0 || be < 0 || m < n || v > m - 1 || al > m - 1 || be > m - 1) {
            return 0; // propage
        } else {
            if (m - n < (2 * Math.sqrt(m))) {
                assert al <= be && al <= v && v <= be : "case 2: "+ m+" - "+n+" - "+v+" - "+al+" - "+be;
                double value  = probaCase2(m, (m - n), v, al, be);
                assert value <= 1 : "case 2: " + value + " - "+m+" - "+n+" - "+v+" - "+al+" - "+be;
                if (value < 0) {
                    return 0;
                } else {
                    return value;
                }
            } else {
                assert al <= be && al <= v && v <= be : "case 1: " + m+" - "+n+" - "+v+" - "+al+" - "+be;
                double value = probaCase1(m, (n / m), v, al, be);
                assert value <= 1 : "case 1: " + value + " - "+m+" - "+n+" - "+v+" - "+al+" - "+be;
                if (value < 0) {
                    return 0;
                } else {
                    return value;
                }
            }
        }
    }

    private static double probaCase1(double m, double l, double v, double al, double be) {
        return 1 - fi(m, 1, v, al, be) * ((2 * l * (1 - Math.exp(-4 * l))) / m);
    }

    private static double probaCase2(double m, double l, double v, double al, double be) {
        double sum1 = 0;
        double max = Math.min((m - l - 1), 26);
        for (int j = 1; j <= max; j++) {
            sum1 += fi(m, m - l - j - 1, v, al, be) * Math.log(1 - f(l, j));
        }
        double sum2 = 0;
        for (int j = 1; j <= max; j++) {
            sum2 += fi(m, m - l - j - 1, v, al, be) * ((g(l, j)) / (1 - f(l, j)));
        }
        return Math.exp(sum1) * (1 - (1 / m) * (fi(m, 1, v, al, be) * 2 * (1 - Math.exp(-4)) + sum2));
    }

    private static int heavyiside(double x) {
        return x >= 0 ? 1 : 0;
    }

    private static double f(double i, int j) {
        double val = (Math.pow((i + j), j) * Math.pow(2, j) * Math.exp(-(2 * (i + j)))) / fact[j - 1];
        double val2 = (1 + (j / (2 * (i + j))));
        return val * val2;
    }

    private static double g(double i, int j) {
        double val = (Math.pow((i + j), j) * Math.pow(2, j) * Math.exp(-(2 * (i + j)))) / fact[j - 1];
        double val2 = ((i + 1) * ((2 * i) + j - 1) * j) / (4 * (i + j));
        double val3 = (2 * i * (i + 1) + j * (i + 2)) / 2;
        return val * (val2 + val3);
    }

    private static double fi(double m, double l, double v, double al, double be) {
        return Math.min(v, m - l - 1) - Math.max(0, v - l) + 1 - heavyiside(l - (be - al)) * (Math.min(al, m - l - 1) - Math.max(0, be - l) + 1);
    }

    public static void main(String[] args) {
        System.out.println(fi(0,0,0,1,0));
        System.out.println(Math.log(1 - f(0, 1)));
        System.out.println(fi(0,0,0,1,0)*Math.log(1 - f(0, 1)));
    }

}
