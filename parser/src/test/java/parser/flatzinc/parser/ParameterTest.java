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

package parser.flatzinc.parser;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 12 janv. 2010
* Since : Choco 2.1.1
* 
*/
public class ParameterTest {

    FZNParser fzn;

    @BeforeTest
    public void before() {
        fzn = new FZNParser();
    }

    @Test
    public void testBool() {
        TerminalParser.parse(fzn.PAR_VAR_DECL, "bool: bb = true;");
        Object o = fzn.map.get("bb");
        Assert.assertTrue(Boolean.class.isInstance(o));
        Boolean oi = (Boolean) o;
        Assert.assertTrue(oi);
    }

    @Test
    public void testInt() {
        TerminalParser.parse(fzn.PAR_VAR_DECL, "int: n = 4;");
        Object o = fzn.map.get("n");
        Assert.assertTrue(Integer.class.isInstance(o));
        Integer oi = (Integer) o;
        Assert.assertEquals(oi.intValue(), 4);
    }

    @Test
    public void testInt2() {
        TerminalParser.parse(fzn.PAR_VAR_DECL, "0..1: n = 4;");
        Object o = fzn.map.get("n");
        Assert.assertTrue(Integer.class.isInstance(o));
        Integer oi = (Integer) o;
        Assert.assertEquals(oi.intValue(), 4);
    }

    @Test
    public void testInt3() {
        TerminalParser.parse(fzn.PAR_VAR_DECL, "{0,68}: n = 4;");
        Object o = fzn.map.get("n");
        Assert.assertTrue(Integer.class.isInstance(o));
        Integer oi = (Integer) o;
        Assert.assertEquals(oi.intValue(), 4);
    }

//    @Test
//    public void testSet1(){
//        TerminalParser.parse(fzn.PAR_VAR_DECL, "set of int: jobs = 6 .. 8;");
//        Object o = fzn.map.get("jobs");
//        Assert.assertTrue(SetConstantVariable.class.isInstance(o));
//        SetConstantVariable oi = (SetConstantVariable)o;
//        Assert.assertArrayEquals(new int[]{6,7,8}, oi.getValues());
//    }
//
//    @Test
//    public void testSet2(){
//        TerminalParser.parse(fzn.PAR_VAR_DECL, "set of int: jobs = {6,88,99};");
//        Object o = fzn.map.get("jobs");
//        Assert.assertTrue(SetConstantVariable.class.isInstance(o));
//        SetConstantVariable oi = (SetConstantVariable)o;
//        Assert.assertArrayEquals(new int[]{6,88,99}, oi.getValues());
//    }

    @Test
    public void testArray1() {
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array[1 .. 10] of int: job_task_duration = [ 23, 82, 84, 45, 38, 50, 41, 29, 18, 21];");
        Object o = fzn.map.get("job_task_duration");
        Assert.assertTrue(o.getClass().isArray());
        int[] oi = (int[]) o;
        Assert.assertEquals(oi, new int[]{23, 82, 84, 45, 38, 50, 41, 29, 18, 21});
    }

}
