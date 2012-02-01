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
package solver.propagation;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.propagation.wm.IWaterMarking;
import solver.propagation.wm.WaterMarkers;
import solver.propagation.wm.WaterMarkingImpl;
import solver.propagation.wm.WaterMarkingLongImpl;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/02/12
 */
public class WaterMarkingTest {

    private void test(int size) {
        IWaterMarking wm = WaterMarkers.make(size);
        for (int i = 0; i < size; i++) {
            for (int j = size; j < 2*size; j++) {
                for (int k = 0; k < 3; k++) {
                    wm.putMark(i, j, k);
                }
            }
        }
        Assert.assertFalse(wm.isEmpty());
        for (int i = 0; i < size; i++) {
            for (int j = size; j < 2*size; j++) {
                for (int k = 0; k < 3; k++) {
                    Assert.assertTrue(wm.isMarked(i, j, k));
                    wm.clearMark(i, j, k);
                    Assert.assertFalse(wm.isMarked(i, j, k));
                }
            }
        }
    }

    @Test
    public void testSmall(){
        for(int i = 1; i < 2047; i = i<< 1){
            System.out.println("i:"+i);
            test(i);
        }
    }

    @Test
    public void testLarge(){
        IWaterMarking wm = WaterMarkers.make(6000);
        Assert.assertTrue(wm instanceof WaterMarkingImpl);
        wm = WaterMarkers.make(65000);
        Assert.assertTrue(wm instanceof WaterMarkingLongImpl);
    }

}
