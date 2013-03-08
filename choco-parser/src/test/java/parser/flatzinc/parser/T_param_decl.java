/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package parser.flatzinc.parser;

import gnu.trove.map.hash.THashMap;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.testng.Assert;
import org.testng.annotations.Test;
import parser.flatzinc.FlatzincParser;
import parser.flatzinc.FlatzincWalker;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_param_decl extends GrammarTest {


    public THashMap<String, Object> param_decl(FlatzincParser parser) throws RecognitionException {
        FlatzincParser.param_decl_return r = parser.param_decl();
        CommonTree t = (CommonTree) r.getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        FlatzincWalker walker = new FlatzincWalker(nodes);
        walker.map = new THashMap<String, Object>();
        walker.param_decl();
        return walker.map;
    }

    @Test(groups = "1s")
    public void test1() throws IOException {
        FlatzincParser fp = parser("bool: beer_is_good = true;");
        THashMap<String, Object> map = null;
        try {
            map = param_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        Object o = map.get("beer_is_good");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Boolean);
        Assert.assertEquals(true, ((Boolean) o).booleanValue());
    }

    @Test(groups = "1s")
    public void test2() throws IOException {
        FlatzincParser fp = parser("int: n = 4;");
        THashMap<String, Object> map = null;
        try {
            map = param_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        Object o = map.get("n");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Integer);
        Assert.assertEquals(4, ((Integer) o).intValue());
    }


    @Test(groups = "1s")
    public void test3() throws IOException {
        FlatzincParser fp = parser("array [1..7] of int: fib = [1,1,2,3,5,8,13];");
        THashMap<String, Object> map = null;
        try {
            map = param_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        Object o = map.get("fib");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof int[]);
        Assert.assertEquals(new int[]{1, 1, 2, 3, 5, 8, 13}, o);
    }

    @Test(groups = "1s")
    public void test4() throws IOException {
        FlatzincParser fp = parser("array [1..3] of set of int: suc = [{5, 10, 14}, {}, {}];");
        try {
            param_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
    }
}
