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

package choco.kernel.common.util.tools;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17 aožt 2010
 *
 * * TODO duplicate with {@code solver.probabilities.ProbaUtils}
 */
public class StatisticUtils {

    protected StatisticUtils() {}

    public static int sum(int... values){
        int sum = 0;
        for(int i = 0; i < values.length; i++){
            sum+= values[i];
        }
        return sum;
    }

    public static long sum(long... values){
        long sum = 0L;
        for(int i = 0; i < values.length; i++){
            sum+= values[i];
        }
        return sum;
    }

    public static float sum(float... values){
        float sum = 0.0f;
        for(int i = 0; i < values.length; i++){
            sum+= values[i];
        }
        return sum;
    }

    public static double sum(double... values){
        double sum = 0.0;
        for(int i = 0; i < values.length; i++){
            sum+= values[i];
        }
        return sum;
    }

    public static double mean(int... values){
        return sum(values)/values.length;
    }

    public static float mean(long... values){
        return sum(values)/values.length;
    }

    public static double mean(float... values){
        return sum(values)/values.length;
    }

    public static double mean(double... values){
        return sum(values)/values.length;
    }


    public static double standarddeviation(int... values){
        double mean = mean(values);
        double[] psd = new double[values.length];
        for(int i = 0 ; i < values.length; i++){
            psd[i] = Math.pow(values[i] - mean, 2.0);
        }
        return Math.sqrt(mean(psd));
    }

    public static double standarddeviation(long... values){
        double mean = mean(values);
        double[] psd = new double[values.length];
        for(int i = 0 ; i < values.length; i++){
            psd[i] = Math.pow(values[i] - mean, 2.0);
        }
        return Math.sqrt(mean(psd));
    }

    public static float standarddeviation(float... values){
        double mean = mean(values);
        double[] psd = new double[values.length];
        for(int i = 0 ; i < values.length; i++){
            psd[i] = Math.pow(values[i] - mean, 2.0);
        }
        return (float)Math.sqrt(mean(psd));
    }

    public static int[] prepare(int... values){
        Arrays.sort(values);
        int[] back = new int[values.length - 2];
        System.arraycopy(values, 1, back, 0, back.length);
        return back;
    }

    public static long[] prepare(long... values){
        Arrays.sort(values);
        long[] back = new long[values.length - 2];
        System.arraycopy(values, 1, back, 0, back.length);
        return back;
    }

    public static float[] prepare(float... values){
        Arrays.sort(values);
        float[] back = new float[values.length - 2];
        System.arraycopy(values, 1, back, 0, back.length);
        return back;
    }

}
