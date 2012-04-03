/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
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

package solver.constraints.nary.geost.internalConstraints;

import choco.cp.solver.constraints.global.geost.Constants;
import choco.cp.solver.constraints.global.geost.Setup;
import choco.cp.solver.constraints.global.geost.geometricPrim.Point;
import choco.cp.solver.constraints.global.geost.geometricPrim.Region;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

//import com.sun.xml.internal.xsom.impl.scd.Iterators;

/**
 * Created by IntelliJ IDEA.
 * User: szampelli
 * Date: 4 févr. 2009
 * Time: 10:11:25
 * To change this template use File | Settings | File Templates.
 */
public final class DistLeqIC extends ForbiddenRegion {
    private static final Logger LOGGER = ChocoLogging.getEngineLogger();

    public int q, D, s1, s2, o1, o2;
    public Setup stp;
    public IntDomainVar DVar = null; //DVar is the distance variable


    public DistLeqIC(Setup stp_, int q_, int D_, int s1_, int s2_, int o1_, int o2_) {
        this.setIctrID(Constants.DIST_LEQ_FR);

        stp = stp_;
        q = q_;
        D = D_;
        s1 = s1_;
        s2 = s2_;
        o1 = o1_;
        o2 = o2_;
        if (q != 2) {
            throw new SolverException("DistLeqIC:Only norm 2 is supported for now.");
        }
    }

    public DistLeqIC(Setup stp_, int q_, int D_, int s1_, int s2_, int o1_, int o2_, IntDomainVar DVar_) {
        this.setIctrID(Constants.DIST_LEQ_FR);

        stp = stp_;
        q = q_;
        D = D_;
        s1 = s1_;
        s2 = s2_;
        o1 = o1_;
        o2 = o2_;
        if (q != 2) {
            throw new SolverException("DistLeqIC:Only norm 2 is supported for now.");
        }
        DVar = DVar_;
    }


    /* sweep.tex r108 Chapter 7 - Algorithm 156 'InsideForbidden'
     * returns true if p belongs to the forbidden region F and false otherwise */
    public boolean insideForbidden(Point p) {
        boolean save_stp_debug = stp.opt.debug;
        stp.opt.debug = false;
        if (stp.opt.debug) LOGGER.info("/*debug*/segInsideForbidden(" + p + ")");

        if (DVar != null) D = DVar.getSup();
//           LOGGER.info("segInsideForbidden("+p+") call");
//           LOGGER.info("o1:"+stp.getObject(o1));
//           LOGGER.info("o2:"+stp.getObject(o2));

        int k = p.getCoords().length;
        Point m = new Point(k);
        for (int i = 0; i < k; i++) { /*line 1*/
            int p_i = p.getCoord(i);
            int s1_t_i = stp.getShape(s1).get(0).getOffset(i); //suppose there is only one shifted box
            int s2_t_i = stp.getShape(s2).get(0).getOffset(i); //suppose there is only one shifted box
            int o2_x_i_lb = stp.getObject(o2).getCoord(i).getInf();
            int o2_x_i_ub = stp.getObject(o2).getCoord(i).getSup();
            int s1_l_i = stp.getShape(s1).get(0).getSize(i); //suppose there is only one shifted box
            int s2_l_i = stp.getShape(s2).get(0).getSize(i); //suppose there is only one shifted box
            int m1 = Math.max(p_i + s1_t_i, o2_x_i_lb + s2_t_i); /*line 3:m1<-...*/ //+ gd debut
            int m2 = Math.min(p_i + s1_t_i + s1_l_i, o2_x_i_ub + s2_t_i + s2_l_i); /*line 4:m2<-...*/ //+ petite fin
            m.setCoord(i, Math.max(0, m1 - m2)); /*line 5:m[i]<-...*/
        }

        if (stp.opt.debug) LOGGER.info("/*debug*/segInsideForbidden(): " + m + " norm(m,q)=" + norm(m) + ">D=" + D);
        if (stp.opt.debug) LOGGER.info("/*debug*/segInsideForbidden() returns " + (norm(m) > D));
        stp.opt.debug = save_stp_debug;
        return (norm(m) > D); /*line 7-11*/
    }

    //Algorithm 157 p.249 sweep.pdf r108
    public int maximizeSizeOfFBox(boolean min, int d, int k, Region f) {

        //Preconfition : c is inside the forbidden Region
        //if (f.isPoint()) if (!segInsideForbidden(f.point())) {LOGGER.info("Error precondition in DistLeqIC"); };
        if (stp.opt.debug) {
            if (!insideForbidden(f.pointMin())) {
                throw new SolverException("Error precondition pointMin in DistLeqIC");
            }
            if (!insideForbidden(f.pointMax())) {
                throw new SolverException("Error precondition pointMax in DistLeqIC");
            }
        }
        if (DVar != null) D = DVar.getSup();
        //LOGGER.info("ENTERING maximizeSizeOfFBox");
        Point m = new Point(k);
//        LOGGER.info("compute m k:"+k);

        for (int i = 0; i < k; i++) { /*line 1*/
            int f_min_i = f.getMinimumBoundary(i);
            int f_max_i = f.getMaximumBoundary(i);
            int s1_t_i = stp.getShape(s1).get(0).getOffset(i); //suppose there is only one shifted box
            int s2_t_i = stp.getShape(s2).get(0).getOffset(i); //suppose there is only one shifted box
            int o2_x_i_lb = stp.getObject(o2).getCoord(i).getInf();
            int o2_x_i_ub = stp.getObject(o2).getCoord(i).getSup();
            int s1_l_i = stp.getShape(s1).get(0).getSize(i); //suppose there is only one shifted box
            int s2_l_i = stp.getShape(s2).get(0).getSize(i); //suppose there is only one shifted box
            int m1 = Math.max(f_min_i + s1_t_i, o2_x_i_lb + s2_t_i); /*line 3:m1<-...*/
            int m2 = Math.min(f_max_i + s1_t_i + s1_l_i, o2_x_i_ub + s2_t_i + s2_l_i); /*line 4:m2<-...*/

//            LOGGER.info("s1:"+s1+";s2:"+s2+";s1_l_i:"+s1_l_i+";s2_l_i:"+s2_l_i+";m1:"+m1+";m2:"+m2);

            m.setCoord(i, Math.max(0, m1 - m2)); /*line 5:m[i]<-...*/
        }
//        LOGGER.info("end compute m:"+m);


        int f_min_d = f.getMinimumBoundary(d);
        int f_max_d = f.getMaximumBoundary(d);
        int s1_t_d = stp.getShape(s1).get(0).getOffset(d); //suppose there is only one shifted box
        int s2_t_d = stp.getShape(s2).get(0).getOffset(d); //suppose there is only one shifted box
        int o2_x_d_lb = stp.getObject(o2).getCoord(d).getInf();
        int o2_x_d_ub = stp.getObject(o2).getCoord(d).getSup();
        int s1_l_d = stp.getShape(s1).get(0).getSize(d); //suppose there is only one shifted box
        int s2_l_d = stp.getShape(s2).get(0).getSize(d); //suppose there is only one shifted box

        int plus_infinity = stp.getObject(o1).getCoord(d).getSup();
        int minus_infinity = stp.getObject(o1).getCoord(d).getInf();
        double q_sum = q_sum(m, d);
        double norm = sqrt(q_sum);
        checkSqrt(q_sum, norm);
        double term = sqrt(Math.pow(D, q) - q_sum);
        if (term >= 0) checkSqrt(Math.pow(D, q) - q_sum, term); //otherwise norm>D.

        //int term_down = (int) Math.floor(term);
        //int term_up=term_down;
        //if (((double)term_down) != term) term_up++;

        //LOGGER.info("f:"+f+";m:"+m+";o2_x_d_lb:"+o2_x_d_lb+";o2_x_d_ub:"+o2_x_d_ub+";d:"+d+";D:"+D+";q_sum:"+q_sum+";norm:"+norm+";term:"+term+";norm>D:"+(norm>D)+";plus_inf:"+plus_infinity+";minus_inf:"+minus_infinity);

        if (min) { /*line 8*/
            if ((norm > D) || (f_min_d + s1_t_d >= o2_x_d_lb + s2_t_d - s1_l_d)) /*line 9*/ {
                int result = plus_infinity;
                if (stp.opt.debug) {
                    if (result < f_min_d) {
                        throw new SolverException("Error1 in DistLeqIC");
                    }
                }
                return plus_infinity; /*line 10:return +infinity*/
            } else {
                int result = ((int) Math.ceil(o2_x_d_lb + s1_t_d - s1_l_d - term)) - 1 - s1_t_d; /*line 12*/
                if (stp.opt.debug) {
                    if (result < f_min_d) {
                        throw new SolverException("Error2 in DistLeqIC");
                    }
                }
                return ((int) Math.ceil(o2_x_d_lb + s1_t_d - s1_l_d - term)) - 1 - s1_t_d; /*line 12*/
            }
        }

        if ((norm > D) || (f_max_d + s1_t_d <= o2_x_d_ub + s2_t_d - s1_l_d)) /*line 15*/
            return minus_infinity; /*line 16:return -infinity*/
        else
            return ((int) Math.floor(o2_x_d_ub + s2_t_d + s2_l_d + term)) + 1 - s1_t_d; /*line 18*/
    }

    private double q_sum(Point m, int d) {
        int k = m.getCoords().length;
        double sum = 0;
        for (int i = k - 1; i >= 0; i--) {
            if (i != d) {
                double r = 1;
                for (int j = 0; j < q; j++) {
                    r *= Math.abs(m.getCoord(i));
                    if (r == Double.POSITIVE_INFINITY) {
                        throw new SolverException("DestLeqIC:q_sum():r:double limit reached");
                    }
                }

                sum += r;
                if (sum == Double.POSITIVE_INFINITY) {
                    throw new SolverException("DestLeqIC:q_sum():sum:double limit reached");
                }

            }
        }

        return (double) sum;

    }

    private double q_sum(double[] m, int d) {
        int k = m.length;

        double sum = 0;
        for (int i = k - 1; i >= 0; i--) {
            if (i != d) {
                double r = 1;
                for (int j = 0; j < q; j++) {
                    r *= Math.abs(m[i]);
                    if (r == Double.POSITIVE_INFINITY) {
                        throw new SolverException("DestLeqIC:q_sum():r:double limit reached");
                    }
                }
                sum += r;
                if (sum == Double.POSITIVE_INFINITY) {
                    throw new SolverException("DestLeqIC:q_sum():sum:double limit reached");
                }

            }
        }

        return sum;

    }

    private double sqrt(double sum) {
        return Math.sqrt(sum);
    }

    private double norm(Point m) {
        return sqrt(q_sum(m, -1));

    }

    private double norm(double[] m) {
        return sqrt(q_sum(m, -1));

    }


    private void checkSqrt(double value, double result) {
        //First check if the value to be square rooted is an integer
        double ivalue = Math.floor(value);
        if (ivalue != value) {
            throw new SolverException("DistLeqIC.checkSqrt(): sqrt value is not an integer");

        }

        long lb = (int) Math.floor(result);
        long ub = (int) Math.ceil(result);

        if (lb * lb > ivalue) {
            throw new SolverException("DistLeqIC.checkSqrt(): lb is wrong:value:" + value + " result:" + result + " lb:" + lb + " ivalue=" + ivalue);

        }
        if (ub * ub < ivalue) {
            throw new SolverException("DistLeqIC.checkSqrt(): ub is wrong:value:" + value + " result:" + result + " ub:" + ub + " ub*ub:" + (ub * ub));

        }
    }

    private double[] PiecesOfLin(Point c0, Point c1) {
        int k = c0.getCoords().length;
        double[] result = new double[3 * k];
        result[0] = 0.0;
        result[1] = 1.0;
        int realSize = 2;
        for (int i = 0; i < k; i++) {
            double c0_i = (double) c0.getCoord(i);
            double c1_i = (double) c1.getCoord(i);
            double o2_x_i_lb = (double) stp.getObject(o2).getCoord(i).getInf();
            double o2_x_i_ub = (double) stp.getObject(o2).getCoord(i).getSup();
            double s2_l_i = (double) stp.getShape(s2).get(0).getSize(i); //suppose there is only one shifted box
            double den = (c1_i - c0_i);
            double tmp;
            double epsilon = 0.000000001;  //E-9
            if (den != 0.0) tmp = ((o2_x_i_lb - c0_i) / (den));
            else tmp = 2.0;
            boolean condition1 = (-tmp <= epsilon);
            boolean condition2 = (tmp - 1.0 <= epsilon);
            if ((condition1) && (condition2)) {
                result[realSize++] = tmp;
            }
            if (den != 0.0) tmp = ((o2_x_i_ub + s2_l_i - c0_i) / (den));
            else tmp = 2.0;
            condition1 = (-tmp <= epsilon);
            condition2 = (tmp - 1.0 <= epsilon);
            if ((condition1) && (condition2)) {
                result[realSize++] = tmp;
            }
        }

        //sort and clip
        int n = realSize;


        //uniques: because int the pseudo-code, an union is considered
        HashMap<Double, Boolean> inIt = new HashMap<Double, Boolean>();
        for (int i = 0; i < n; i++){
            if (inIt.get(result[i]) == null){
                inIt.put(result[i], true);
            }
        }
        int i = 0;
        for (Double d : inIt.keySet()) {
            result[i] = d;
            i++;
        }

        //sort
        Arrays.sort(result, 0, inIt.size());//second argument is exclusive//"Returns the number of key-value mappings in this map."

        double[] result2 = new double[inIt.size()];
        for (i = 0; i < inIt.size(); i++){
            result2[i] = result[i];
        }

        return result2;
    }

    private double cOf(int i, double beta, Point c0, Point c1) {
        //return (((1.0-beta)*c0.getCoord(i))+(beta*c1.getCoord(i)));
        return mult((1.0 - beta), c0.getCoord(i)) + mult(beta, c1.getCoord(i));
    }

    public boolean insideForbidden_withDoubles(Point c0, Point c1) {
        double[] beta = PiecesOfLin(c0, c1);
        if (DVar != null) D = DVar.getSup();

        int n = beta.length;
        int k = c0.getCoords().length;
        //Pe parce que les a et b sont approximés?
        //la conversion vers int est faite trop tot, notamment ds cOf?

        for (int j = 0; j < n - 1; j++) {   //for a given beta,
            Point a = new Point(k);
            Point b = new Point(k);
            for (int i = 0; i < k; i++) { //compute a and b
                int c0_i = c0.getCoord(i);
                int c1_i = c1.getCoord(i);
                int o2_x_i_lb = stp.getObject(o2).getCoord(i).getInf();
                int o2_x_i_ub = stp.getObject(o2).getCoord(i).getSup();
                int s2_l_i = stp.getShape(s2).get(0).getSize(i); //suppose there is only one shifted box


                double c_beta_j = cOf(i, beta[j], c0, c1);
                double c_beta_j_plus_1 = cOf(i, beta[j + 1], c0, c1);
                if (Math.max(c_beta_j, c_beta_j_plus_1) < o2_x_i_lb) {
                    a.setCoord(i, c0_i - c1_i);
                    b.setCoord(i, o2_x_i_lb - c0_i);
                } else {
                    if (Math.min(c_beta_j, c_beta_j_plus_1) > o2_x_i_ub - s2_l_i) {
                        a.setCoord(i, c1_i - c0_i);
                        b.setCoord(i, c0_i - o2_x_i_ub - s2_l_i);
                    } else {
                        a.setCoord(i, 0);
                        b.setCoord(i, 0);
                    }
                }
            } //end of the 'for i'; a and b are computed
            double num = 0;
            for (int i = 0; i < k; i++){
                num += (mult(((double) a.getCoord(i)), ((double) b.getCoord(i))));
            }
            double den = 0;
            for (int i = 0; i < k; i++){
                den += (mult(((double) a.getCoord(i)), ((double) a.getCoord(i))));
            }
            double beta_star;
            double beta_j = beta[j];
            double beta_j_plus_1 = beta[j + 1];
            if (den != 0.0) {
                beta_star = -(num / den);
            } else {
                beta_star = beta_j - 1.0;
            }


            if ((beta_star >= beta_j) && (beta_star <= beta_j_plus_1)) {
                double[] V_beta_star = new double[k];
                for (int i = 0; i < k; i++){
                    V_beta_star[i] = (mult(((double) a.getCoord(i)), beta_star) + b.getCoord(i));
                }
                if (norm(V_beta_star) <= D) {
                    return false;
                }
            } else {
                double[] V_beta_j_plus_1 = new double[k];
                for (int i = 0; i < k; i++){
                    V_beta_j_plus_1[i] = mult(((double) a.getCoord(i)), beta_j_plus_1) + b.getCoord(i);
                }
                if (norm(V_beta_j_plus_1) <= D) {
                    return false;
                }
            }
        }
        return true;

    }

    public double[] vOf(double alpha, Point c0, Point c1, double beta_j, double beta_j_plus_1) {
        int k = c0.getCoords().length;
        double[] result = new double[k];
        for (int i = 0; i < k; i++) {
            int c0_i = c0.getCoord(i);
            int c1_i = c1.getCoord(i);
            int o2_x_i_lb = stp.getObject(o2).getCoord(i).getInf();
            int o2_x_i_ub = stp.getObject(o2).getCoord(i).getSup();
            int s2_l_i = stp.getShape(s2).get(0).getSize(i); //suppose there is only one shifted box
            double epsilon = 0.000000001;  //E-9

            double c_beta_j = cOf(i, beta_j, c0, c1);
            double c_beta_j_plus_1 = cOf(i, beta_j_plus_1, c0, c1);
            double toCompare = Math.max(c_beta_j, c_beta_j_plus_1);
            boolean condition = (toCompare - ((double) o2_x_i_lb) <= epsilon);
            //LOGGER.info("vOf():condition max: toCompare:"+toCompare+" <= o2_x_i_lb:"+((double)o2_x_i_lb)+"="+condition);
            if (condition) {      //Here ????
                double a = o2_x_i_lb;
                double b = -c0_i;
                double c = c1_i - c0_i;
                double r = a + b - (alpha * c);
                result[i] = r;
            } else {
                toCompare = Math.min(c_beta_j, c_beta_j_plus_1);
                condition = (((double) o2_x_i_ub) - ((double) s2_l_i)) - toCompare <= epsilon;//toCompare>=o2_x_i_ub-s2_l_i
                //LOGGER.info("vOf():condition min: toCompare:"+toCompare+" >= o2_x_i_ub:"+(((double)o2_x_i_ub)-((double)s2_l_i))+"="+condition);
                if (condition) { //Here ????

                    double a = c0_i;
                    double b = (c1_i - c0_i);
                    double c = -o2_x_i_ub - s2_l_i;
                    double r = a + (alpha * b) + c;
                    result[i] = r;

                } else {
                    result[i] = 0.0;
                }
            }

        }
        return result;

    }


    public boolean segInsideForbidden(Point c0, Point c1) {
        if (stp.opt.debug) LOGGER.info("/*debug*/segInsideForbiden(c0=" + c0 + ",c1=" + c1 + ")");
        if (DVar != null) D = DVar.getSup();

        if ((!insideForbidden(c0)) || (!insideForbidden(c1))) {
            if (stp.opt.debug) LOGGER.info("/*debug*/segInsideForbiden returns " + false);
            return false;
        }

        //Does the order of c0, c1 matter?
        double[] beta = PiecesOfLin(c0, c1);
        if (stp.opt.debug) LOGGER.info("/*debug*/segInsideForbiden: beta[" + beta.length + "]=[");
        if (stp.opt.debug) {
            for (int ind = 0; ind < beta.length; ind++) {
                LOGGER.info(beta[ind] + " ");
            }
        }
        if (stp.opt.debug) {
            LOGGER.info("]");
        }

//        double[] beta2=PiecesOfLin(c1,c0);
//        double[] beta = new double[beta1.length+beta2.length-2];
//        HashMap<Double,Object> tmp = new HashMap<Double,Object>();
//        for (int i=0;i<beta1.length;i++) tmp.put(beta1[i],null);
//        for (int i=0;i<beta2.length;i++) tmp.put(beta2[i],null);
//        int ind=0;
//        for (Double d:tmp.keySet()) {beta[ind]=d;ind++;}
//        Arrays.sort(beta,0,beta.length);

        double epsilon = 0.000000001;  //E-9
        //LOGGER.info("InsideForibidden("+c0+","+c1+")");
        //for (int ind=0; ind<beta.length;ind++) LOGGER.info(beta[ind]+" ");
        //LOGGER.info();

        int n = beta.length;
        int k = c0.getCoords().length;
        //Pe parce que les a et b sont approximés?
        //la conversion vers int est faite trop tot, notamment ds cOf?
        Point a = new Point(k);
        Point b = new Point(k);
        for (int j = 0; j < n - 1; j++) {   //for a given beta,
            //LOGGER.info("b_j:"+beta[j]+" b_{j+1}:"+beta[j+1]);

            for (int i = 0; i < k; i++) { //compute a and b
                int c0_i = c0.getCoord(i);
                int c1_i = c1.getCoord(i);
                int o2_x_i_lb = stp.getObject(o2).getCoord(i).getInf();
                int o2_x_i_ub = stp.getObject(o2).getCoord(i).getSup();
                int s2_l_i = stp.getShape(s2).get(0).getSize(i); //suppose there is only one shifted box


                double c_beta_j = cOf(i, beta[j], c0, c1);
                double c_beta_j_plus_1 = cOf(i, beta[j + 1], c0, c1);
                //LOGGER.info("c_beta_j:"+c_beta_j+" c_beta_j_plus_1:"+c_beta_j_plus_1);

//                if (Math.max(c_beta_j,c_beta_j_plus_1)<=o2_x_i_lb) {      //Here ????
                double toCompare = Math.max(c_beta_j, c_beta_j_plus_1);
                boolean condition = (toCompare - ((double) o2_x_i_lb) <= epsilon);
                //LOGGER.info("condition max: toCompare:"+toCompare+" <= o2_x_i_lb:"+((double)o2_x_i_lb)+"="+condition);
                if (condition) {      //Here ????
                    //LOGGER.info("a["+i+"]<-"+"c0_i:"+c0_i+"-c1_i:"+c1_i+"="+(c0_i-c1_i));
                    a.setCoord(i, c0_i - c1_i);
                    //LOGGER.info("b["+i+"]<-"+"o2_x_i_lb:"+o2_x_i_lb+"-c0_i:"+c0_i+"="+(o2_x_i_lb-c0_i));
                    b.setCoord(i, o2_x_i_lb - c0_i);
                } else {
                    toCompare = Math.min(c_beta_j, c_beta_j_plus_1);
                    condition = (((double) o2_x_i_ub) - ((double) s2_l_i)) - toCompare <= epsilon;//toCompare>=o2_x_i_ub-s2_l_i
                    //LOGGER.info("condition min: toCompare:"+toCompare+" >= o2_x_i_ub:"+(((double)o2_x_i_ub)-((double)s2_l_i))+"="+condition);
                    if (condition) { //Here ????
                        a.setCoord(i, c1_i - c0_i);
                        //LOGGER.info("a["+i+"]<-"+"c1_i:"+c1_i+"-c0_i:"+c0_i+"="+(c1_i-c0_i));
                        b.setCoord(i, c0_i - o2_x_i_ub - s2_l_i);
                        //LOGGER.info("b["+i+"]<-"+"c0_i:"+c0_i+"-o2_x_i_ub:"+o2_x_i_ub+"="+b.getCoord(i));

                    } else {
                        a.setCoord(i, 0);
                        b.setCoord(i, 0);
                    }
                }
            } //end of the 'for i'; a and b are computed
            if (stp.opt.debug) LOGGER.info("/*debug*/segInsideForbidden(): a=" + a + " b=" + b);
            int num = 0;
            for (int i = 0; i < k; i++){
                num += a.getCoord(i) * b.getCoord(i);
            }
            int den = 0;
            for (int i = 0; i < k; i++){
                den += a.getCoord(i) * a.getCoord(i);
            }
            double beta_star;
            double beta_j = beta[j];
            double beta_j_plus_1 = beta[j + 1];
            if (stp.opt.debug)
                LOGGER.info("/*debug*/segInsideForbidden(): num=" + num + ", den=" + den + ", beta_j=" + beta_j + ", beta_j_plus_1=" + beta_j_plus_1);

            if (den != 0) {
                beta_star = -(((double) num) / ((double) den));
            } else {
                beta_star = beta_j_plus_1 + 1.0;
            }
            if (stp.opt.debug) LOGGER.info("/*debug*/segInsideForbidden(): beta_star=" + beta_star);

            //LOGGER.info("beta_star:"+beta_star);
            //LOGGER.info("b_j:"+beta[j]+" b_{j+1}:"+beta[j+1]);


//            if ((beta_star>=beta_j) && (beta_star<=beta_j_plus_1)) {
            if (stp.opt.debug)
                LOGGER.info("/*debug*/segInsideForbidden(): beta_star:" + beta_star + " >= beta_j:" + beta_j + ":" + (beta_j - beta_star <= epsilon));
            if (stp.opt.debug)
                LOGGER.info("/*debug*/segInsideForbidden(): beta_star:" + beta_star + " <= beta_j_plus_1:" + beta_j_plus_1 + ":" + (beta_star - beta_j_plus_1 <= epsilon));

            if ((beta_j - beta_star <= epsilon) && (beta_star - beta_j_plus_1 <= epsilon)) {
                double[] V_beta_star = vOf(beta_star, c0, c1, beta_j, beta_j_plus_1);
                //for (int i=0; i<k; i++) V_beta_star.setCoord(i,((int) (mult(((double)a.getCoord(i)),beta_star)+b.getCoord(i))));
                if (stp.opt.debug) LOGGER.info("/*debug*/segInsideForbidden(): norm(V_beta_star:[");
                if (stp.opt.debug) {
                    for (int ind = 0; ind < V_beta_star.length; ind++) {
                        LOGGER.info(V_beta_star[ind] + " ");
                    }
                }
                if (stp.opt.debug) LOGGER.info("],2)=" + norm(V_beta_star) + "<=D:" + D);

                if (norm(V_beta_star) <= D) {
                    if (stp.opt.debug) LOGGER.info("/*debug*/segInsideForbidden() returns false");
                    return false;
                }
            } else {
                double[] V_beta_j_plus_1 = vOf(beta_j_plus_1, c0, c1, beta_j, beta_j_plus_1);
                //for (int i=0; i<k; i++) V_beta_j_plus_1.setCoord(i,((int) (mult(((double)a.getCoord(i)),beta_j_plus_1)+b.getCoord(i))));
                if (stp.opt.debug)
                    LOGGER.info("/*debug*/segInsideForbidden(): beta_j_plus_1:" + beta_j_plus_1 + ";norm(V_beta_j_plus_1:[" + V_beta_j_plus_1[0] + "," + V_beta_j_plus_1[1] + "],2)=" + norm(V_beta_j_plus_1) + "<=D:" + D);

                if (norm(V_beta_j_plus_1) <= D) {
                    if (stp.opt.debug) LOGGER.info("/*debug*/segInsideForbidden() returns false");
                    return false;
                }
            }
        }
        if (stp.opt.debug) LOGGER.info("/*debug*/segInsideForbidden() returns true");

        return true;
    }

//    boolean BetaMax(Point c0 , Point c1 , int[] betaj_ , int[] betaj_plus_1_ , int integer_, int i) {
//        int k=c0.getCoords().length;
//        //First copy to bigintegers the input
//        List<BigInteger> betaj = new ArrayList<BigInteger>();
//        for (int j=0; j<k; j++) betaj.add(new BigInteger(""+betaj_[j]));
//        List<BigInteger> betaj_plus_1 = new ArrayList<BigInteger>();
//        for (int j=0; j<k; j++) betaj_plus_1.add(new BigInteger(""+betaj_plus_1_[j]));
//
//        //1: b1 ? ?j [0]; b2 ? ?j [1]; b3 ? ?j +1 [0]; b4 ? ?j +1 [1];
//        BigInteger b1=betaj.get(0); BigInteger b2=betaj.get(1);
//        BigInteger b3=betaj_plus_1.get(0); BigInteger b4=betaj_plus_1.get(1);
//        //3: sameSign ? ((b2 ≥ 0) ? (b4 ≥ 0)) ? ((b2 < 0) ? (b4 < 0))
//        boolean sameSign = ((b2.compareTo(BigInteger.ZERO)>=0) && (b4.compareTo(BigInteger.ZERO)>=0))
//                || ((b2.compareTo(BigInteger.ZERO)<0) && (b4.compareTo(BigInteger.ZERO)<0));
//        //4: left ? b4 ? [((b2 ? b1 ) ? c0 [i]) + (b1 ? c1 [i])]
//        BigInteger left=b4.multiply(b2.subtract(b1).multiply( new BigInteger(""+c0.getCoord(i))));
//        //5: right ? b2 ? [((b4 ? b3 ) ? c0 [i]) + (b3 ? c1 [i]))]
//        int right = b2 *(((b4 - b3 ) * c0.getCoord(i)) + (b3 * c1.getCoord(i)));
//        //6: if sameSign then
//        if (sameSign) {
//            //7: if left ≤ right then
//            if (left<=right) {
//            //8: b1 ? b3 ; b2 ? b4 ;
//                b1=b3; b2=b4;
//            }
//            //10: else
//            else {
//                //11: if ¬(left ≥ right ) then
//                if (!(left>=right)) {
//                //12: b1 ? b3 ; b2 ? b4 ;
//                    b1=b3;b2=b4;
//                }
//            }
//        }
//        //16: left ? b2 ? [((b2 ? b1 ) ? c0 [i]) + (b1 ? c1 [i])]
//        left=b2 * (((b2 - b1 ) * c0.getCoord(i)) + (b1*c1.getCoord(i)));
//        //17: right ? b2 ? integer
//        right=b2*integer;
//        //18: if b2 ≥ 0 then
//        if (b2>=0)
//            //19: return left ≤ right
//            return left<=right;
//            //20: else
//        else
//            //21: return left ≥ right
//            return left>=right;
//    }


    double mult(double a, double b) {
        if (!(Math.abs(a) <= (Double.MAX_VALUE / Math.abs(b)))) {
            throw new SolverException("DistLeqIC:mult():Double.MAX_VALUE overflow");
        }
        return a * b;
    }

    public String toString() {
        StringBuilder r = new StringBuilder();
        if (DVar == null) {
            r.append("LeqIC(D=").append(D).append(",q=").append(q).append(",o1=")
                    .append(o1).append(",o2=").append(o2).append(")");
        } else {
            r.append("LeqIC(D=[").append(DVar.getInf()).append(",").append(DVar.getSup())
                    .append("],q=").append(q).append(",o1=").append(o1).append(",o2=").append(o2).append(")");
        }

        return r.toString();
    }

    public int EvaluateMinimumDistance(int k) {
        double dist = 0.0;
        for (int d = 0; d < k; d++) {
            int o1_inf = stp.getObject(o1).getCoord(d).getInf();
            int o2_inf = stp.getObject(o2).getCoord(d).getInf();
            int o1_sup = stp.getObject(o1).getCoord(d).getSup();
            int o2_sup = stp.getObject(o2).getCoord(d).getSup();

            double max = Math.max(0, Math.max(o1_inf, o2_inf) - Math.min(o1_sup, o2_sup));
            dist += max * max;
        }

        return (int) Math.ceil(Math.sqrt(dist));

    }


    public boolean updateDistance(int k) throws ContradictionException {
        if (DVar != null) {
            int oldInf = DVar.getInf();
            int newInf = EvaluateMinimumDistance(k);
            if (oldInf >= newInf) return false;
            DVar.updateInf(newInf, this.stp.g_constraint, false);
            if (stp.opt.debug) {
                LOGGER.info("DistLeqIC:" + this + " updateDistance:[" + DVar.getInf() + "," + DVar.getSup() + "]");
            }
            if ((DVar.getInf() > DVar.getSup()) || (DVar.getSup() < DVar.getInf())) {
                stp.propagationEngine.raiseContradiction(null);
            }
            return true;
        }
        return false;
    }

    public boolean hasDistanceVar() {
        return (DVar != null);
    }

    public IntDomainVar getDistanceVar() {
        return DVar;
    }


}
