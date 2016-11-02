/**
 * Copyright (c) 2014, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the {organization} nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.parser.flatzinc.parser;

import org.antlr.runtime.RecognitionException;
import org.chocosolver.parser.flatzinc.Flatzinc4Parser;
import org.chocosolver.parser.flatzinc.ast.declaration.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_var_type extends GrammarTest {

    @Test(groups = "1s")
    public void test1() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var bool");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DBool);
        Assert.assertEquals(DBool.me, d);
    }

    @Test(groups = "1s")
    public void test2() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var float");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DFloat);
        Assert.assertEquals(DFloat.me, d);
    }

    @Test(groups = "1s")
    public void test3() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var int");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DInt);
        Assert.assertEquals(DInt.me, d);
    }

    @Test(groups = "1s")
    public void test4() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var 1 .. 4");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DInt2);
    }

    @Test(groups = "1s")
    public void test5() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var {1,2,3}");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DManyInt);
    }

    @Test(groups = "1s")
    public void test6() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var set of 1 .. 4");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DSet);
        Assert.assertTrue(((DSet) d).getWhat() instanceof DInt2);
    }

    @Test(groups = "1s")
    public void test7() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var set of {1,2,3}");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DSet);
        Assert.assertTrue(((DSet) d).getWhat() instanceof DManyInt);
    }

    @Test(groups = "1s")
    public void test8() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of var bool");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertEquals(DBool.me, ((DArray) d).getWhat());
    }

    @Test(groups = "1s")
    public void test10() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of var int");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertEquals(DInt.me, ((DArray) d).getWhat());
    }

    @Test(groups = "1s")
    public void test11() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of var 1..3");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertTrue(((DArray) d).getWhat() instanceof DInt2);
    }

    @Test(groups = "1s")
    public void test12() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of var {1,2,3}");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertTrue(((DArray) d).getWhat() instanceof DManyInt);
    }


    @Test(groups = "1s")
    public void test13() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of var set of 1..3");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertTrue(((DArray) d).getWhat() instanceof DSet);
    }

    @Test(groups = "1s")
    public void test14() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of var set of {1,2,3}");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertTrue(((DArray) d).getWhat() instanceof DSet);
    }


}
