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

package choco.constraints;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 sept. 2010
 */
public class RequestTest {

//    private static class Check implements Delta.Procedure2<PropRequest, int[]> {
//        int[] value;
//        PropRequest ad;
//
//
//        @Override
//        public Delta.Procedure2 set(PropRequest advisor, int[] ints) {
//            this.value = ints;
//            this.ad = advisor;
//            return this;
//        }
//
//        @Override
//        public void execute(int val, int idx) throws ContradictionException {
//            Assert.assertEquals(val, value[idx]);
//        }
//    }
//
//    private Check check = new Check();
//
//    @Test
//    public void testAddAll() throws ContradictionException {
//        PropRequest ad = new PropRequest();
//        Delta d = new Delta();
//        EventType e = EventType.REMOVE;
//        int expectecmask = EventType.REMOVE.mask;
//
//        d.add(3);
//        ad.addAll(e, d);
//
//        Assert.assertEquals(ad.getEvtmask(), expectecmask);
//        Assert.assertEquals(ad.getCurrent().size(), 1);
//        check.set(ad, new int[]{3});
//        ad.getCurrent().forEach(check);
//
//        d.clear();
//        d.add(2);
//        d.add(4);
//        ad.addAll(e, d);
//
//        Assert.assertEquals(ad.getEvtmask(), expectecmask);
//        Assert.assertEquals(ad.getCurrent().size(), 3);
//        check.set(ad, new int[]{3, 2, 4});
//        ad.getCurrent().forEach(check);
//
//        d.clear();
//        d.add(1);
//        e = EventType.INCLOW;
//        expectecmask += e.mask;
//        ad.addAll(e, d);
//
//        Assert.assertEquals(ad.getEvtmask(), expectecmask);
//        Assert.assertEquals(ad.getCurrent().size(), 4);
//        check.set(ad, new int[]{3, 2, 4, 1});
//        ad.getCurrent().forEach(check);
//
//        d.clear();
//        d.add(6);
//        e = EventType.DECUPP;
//        expectecmask += e.mask;
//        ad.addAll(e, d);
//
//        Assert.assertEquals(ad.getEvtmask(), expectecmask);
//        Assert.assertEquals(ad.getCurrent().size(), 5);
//        check.set(ad, new int[]{3, 2, 4, 1, 6});
//        ad.getCurrent().forEach(check);
//
//        d.clear();
//        d.add(5);
//        e = EventType.INSTANTIATE;
//        expectecmask += e.mask;
//        ad.addAll(e, d);
//
//        Assert.assertEquals(ad.getEvtmask(), expectecmask);
//        Assert.assertEquals(ad.getCurrent().size(), 6);
//        check.set(ad, new int[]{3, 2, 4, 1, 6, 5});
//        ad.getCurrent().forEach(check);
//    }
//
//    @Test
//    public void testFreeze() throws ContradictionException {
//        PropRequest ad = new PropRequest();
//        Delta d = new Delta();
//        EventType e = EventType.REMOVE;
//
//        d.add(1);
//        d.add(2);
//        d.add(3);
//        ad.addAll(e, d);
//
//        Assert.assertEquals(ad.getEvtmask(), EventType.REMOVE.mask);
//        Assert.assertEquals(ad.getCurrent().size(), 3);
//        check.set(ad, new int[]{1,2,3});
//        ad.getCurrent().forEach(check);
//
//        PropRequest copy = ad.freeze();
//
//        Assert.assertEquals(copy.getEvtmask(), EventType.REMOVE.mask);
//        Assert.assertEquals(copy.getCurrent().size(), 3);
//        check.set(copy, new int[]{1,2,3});
//        copy.getCurrent().forEach(check);
//
//        d.clear();
//        d.add(0);
//        e = EventType.INCLOW;
//        ad.addAll(e, d);
//
//        Assert.assertEquals(ad.getEvtmask(), EventType.INCLOW.mask);
//        Assert.assertEquals(ad.getCurrent().size(), 1);
//        check.set(ad, new int[]{0});
//        ad.getCurrent().forEach(check);
//
//        Assert.assertEquals(copy.getEvtmask(), EventType.REMOVE.mask);
//        Assert.assertEquals(copy.getCurrent().size(), 3);
//        check.set(copy, new int[]{1,2,3});
//        copy.getCurrent().forEach(check);
//
//
//    }

}
