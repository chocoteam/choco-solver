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
package sandbox;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/02/11
 */
public class FixSizeCircularQueueTest {

//    public static String testList(AQueue list) {
//        StringBuffer result = new StringBuffer(1024);
//        list.add("ABCD");
//        list.add("EFGH");
//        list.add("IJKL");
//        list.add("ABCD");
//        list.add("EFGH");
//        list.add("IJKL");
//        result.append(list);
//        result.append(list.contains("IJKL"));
//        result.append(list.containsAll(new ArrayList() {{
//            add("ABCD");
//            add("EFGH");
//        }}));
//        result.append(list.equals(new ArrayList(list)));
//        for (int i = 0; i < 6; i++)
//            result.append(list.get(i));
//        result.append(list.indexOf("EFGH"));
//        result.append(list.isEmpty());
//        result.append(list.lastIndexOf("EFGH"));
//        for (int i = 0; i < 3; i++) result.append(list.remove(3));
//        for (int i = 0; i < 3; i++) result.append(list.remove(0));
//        for (int i = 0; i < 6; i++) list.add(Integer.toString(i));
//        for (int i = 0; i < 6; i++) result.append(list.get(i));
//        Object[] els = list.toArray();
//        for (int i = 0; i < els.length; i++) result.append(els[i]);
//        String[] strs = (String[]) list.toArray(new String[0]);
//        for (int i = 0; i < strs.length; i++) result.append(strs[i]);
//        for (int i = 0; i < 32; i++) {
//            list.add(Integer.toHexString(i));
//            result.append(list.remove(0));
//        }
//        result.append(list);
//        return result.toString();
//    }
//
//    public static void testPerformance(AQueue list, int length) {
//        Object job = new Object();
//        int iterations = 0;
//        for (int j = 0; j < length; j++) list.add(job);
//        long time = -System.currentTimeMillis();
//        while (time + System.currentTimeMillis() < 2000) {
//            iterations++;
//            for (int j = 0; j < 100; j++) {
//                list.pop();
//                list.add(job);
//            }
//        }
//        time += System.currentTimeMillis();
//        System.out.println(list.getClass() + " managed " +
//                iterations + " iterations in " + time + "ms");
//    }
//
//    public static void testCorrectness() {
//        String al = testList(new ArrayList(6));
//        String cal = testList(new ReverseFixSizeCircularQueue(6));
//        if (al.equals(cal)) System.out.println("Correctness Passed");
//        else {
//            System.out.println("Expected:");
//            System.out.println(al);
//            System.out.println("But got:");
//            System.out.println(cal);
//        }
//    }
//
//    public static void testPerformance(int length) {
//        System.out.println("Performance with queue length = " + length);
////        testPerformance(new ArrayList(), length);
////        testPerformance(new LinkedList(), length);
//        testPerformance(new ReverseFixSizeCircularQueue(length), length);
//        testPerformance(new LinkedList(length), length);
//    }
//
//    public static void main(String[] args) {
////        testCorrectness();
//        testPerformance(1);
//        testPerformance(10);
//        testPerformance(100);
//        testPerformance(1000);
//        testPerformance(10000);
//        testPerformance(100000);
//    }
}
