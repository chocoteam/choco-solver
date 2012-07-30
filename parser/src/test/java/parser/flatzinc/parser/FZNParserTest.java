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

import junit.framework.Assert;
import org.codehaus.jparsec.Parser;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import parser.flatzinc.ast.PredParam;
import parser.flatzinc.ast.declaration.*;
import parser.flatzinc.ast.expression.*;

import java.util.List;


/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 12 janv. 2010
* Since : Choco 2.1.1
* 
*/
public class FZNParserTest {

    FZNParser fzn;
    
    @BeforeTest
    public void before(){
        fzn = new FZNParser();
    }

    @Test
    public void testBool() {
        assertResult(fzn.BOOL, "bool", DBool.class, "bool");
    }

    @Test
    public void testInt() {
        assertResult(fzn.INT, "int", DInt.class, "int");
    }

    @Test
    public void testInt2() {
        assertResult(fzn.INT2, "1..3", DInt2.class, "1..3");
    }

    @Test
    public void testManyInt() {
        assertResult(fzn.MANY_INT, "{1,2,3,5}", DManyInt.class, "{1,2,3,5}");
    }

    @Test
    public void testSetOfInt(){
        assertResult(fzn.SET_OF_INT, "set of int", DSet.class, "set of int");
        assertResult(fzn.SET_OF_INT, "set of 1..3", DSet.class, "set of 1..3");
        assertResult(fzn.SET_OF_INT, "set of {1,2,3,5}", DSet.class, "set of {1,2,3,5}");
        assertResult(fzn.SET_OF_INT, "set of {}", DSet.class, "set of {}");
    }

    @Test
    public void testArray(){
        assertResult(fzn.ARRAY_OF, "array [int] of bool", DArray.class, "array [int] of bool");
        assertResult(fzn.ARRAY_OF, "array [1..3] of bool", DArray.class, "array [1..3] of bool");
        assertResult(fzn.ARRAY_OF, "array [int] of int", DArray.class, "array [int] of int");
        assertResult(fzn.ARRAY_OF, "array [1..3] of int", DArray.class, "array [1..3] of int");
        assertResult(fzn.ARRAY_OF, "array [int] of 2..5", DArray.class, "array [int] of 2..5");
        assertResult(fzn.ARRAY_OF, "array [1..3] of 3..6", DArray.class, "array [1..3] of 3..6");
        assertResult(fzn.ARRAY_OF, "array [int] of {2,4,6}", DArray.class, "array [int] of {2,4,6}");
        assertResult(fzn.ARRAY_OF, "array [1..3] of {3,5,7}", DArray.class, "array [1..3] of {3,5,7}");
        assertResult(fzn.ARRAY_OF, "array [int] of set of int", DArray.class, "array [int] of set of int");
        assertResult(fzn.ARRAY_OF, "array [int] of set of 1..3", DArray.class, "array [int] of set of 1..3");
        assertResult(fzn.ARRAY_OF, "array [int] of set of {1,2,3,5}", DArray.class, "array [int] of set of {1,2,3,5}");
        assertResult(fzn.ARRAY_OF, "array [1..3] of set of int", DArray.class, "array [1..3] of set of int");
        assertResult(fzn.ARRAY_OF, "array [1..3] of set of 1..3", DArray.class, "array [1..3] of set of 1..3");
        assertResult(fzn.ARRAY_OF, "array [1..3] of set of {1,2,3,5}", DArray.class, "array [1..3] of set of {1,2,3,5}");
        assertResult(fzn.ARRAY_OF, "array \n [1..3] of set of {1,2,3,5}", DArray.class, "array [1..3] of set of {1,2,3,5}");

    }

    @Test
    public void testComment(){
        assertResult(fzn.BOOL_CONST, "true % comment", EBool.class, "true");
        assertResult(fzn.INT, "int % comment", DInt.class, "int");
        assertResult(fzn.INT2, "1..3 % comment", DInt2.class, "1..3");
        assertResult(fzn.MANY_INT, "{1,2,3,5} % comment", DManyInt.class, "{1,2,3,5}");
    }

    @Test
    public void testParType() {
        assertResult(fzn.TYPE, "bool", Declaration.class, "bool");
        assertResult(fzn.TYPE, "int", Declaration.class, "int");
        assertResult(fzn.TYPE, "1..3", Declaration.class, "1..3");
        assertResult(fzn.TYPE, "{1,2,3,5}", Declaration.class, "{1,2,3,5}");
        assertResult(fzn.TYPE, "set of int", Declaration.class, "set of int");
        assertResult(fzn.TYPE, "set of 1..3", Declaration.class, "set of 1..3");
        assertResult(fzn.TYPE, "set of {1,2,3,5}", Declaration.class, "set of {1,2,3,5}");
        assertResult(fzn.TYPE, "array [int] of bool", Declaration.class, "array [int] of bool");
        assertResult(fzn.TYPE, "array [1..3] of bool", Declaration.class, "array [1..3] of bool");
        assertResult(fzn.TYPE, "array [int] of int", Declaration.class, "array [int] of int");
        assertResult(fzn.TYPE, "array [1..3] of int", Declaration.class, "array [1..3] of int");
        assertResult(fzn.TYPE, "array [int] of 2..5", Declaration.class, "array [int] of 2..5");
        assertResult(fzn.TYPE, "array [1..3] of 3..6", Declaration.class, "array [1..3] of 3..6");
        assertResult(fzn.TYPE, "array [int] of {2,4,6}", Declaration.class, "array [int] of {2,4,6}");
        assertResult(fzn.TYPE, "array [1..3] of {3,5,7}", Declaration.class, "array [1..3] of {3,5,7}");
        assertResult(fzn.TYPE, "array [int] of set of int", Declaration.class, "array [int] of set of int");
        assertResult(fzn.TYPE, "array [int] of set of 1..3", Declaration.class, "array [int] of set of 1..3");
        assertResult(fzn.TYPE, "array [int] of set of {1,2,3,5}", Declaration.class, "array [int] of set of {1,2,3,5}");
        assertResult(fzn.TYPE, "array [1..3] of set of int", Declaration.class, "array [1..3] of set of int");
        assertResult(fzn.TYPE, "array [1..3] of set of 1..3", Declaration.class, "array [1..3] of set of 1..3");
        assertResult(fzn.TYPE, "array [1..3] of set of {1,2,3,5}", Declaration.class, "array [1..3] of set of {1,2,3,5}");
    }

    @Test
    public void testVarDecl(){
        assertResult(fzn.TYPE, "var bool", Declaration.class, "var bool");
        assertResult(fzn.TYPE, "var int", Declaration.class, "var int");
        assertResult(fzn.TYPE, "var 1..3", Declaration.class, "var 1..3");
        assertResult(fzn.TYPE, "var {1,2,3,5}", Declaration.class, "var {1,2,3,5}");
        assertResult(fzn.TYPE, "var set of int", Declaration.class, "var set of int");
        assertResult(fzn.TYPE, "var set of 1..3", Declaration.class, "var set of 1..3");
        assertResult(fzn.TYPE, "var set of {1,2,3,5}", Declaration.class, "var set of {1,2,3,5}");
        assertResult(fzn.TYPE, "array [int] of var bool", Declaration.class, "array [int] of var bool");
        assertResult(fzn.TYPE, "array [1..3] of var bool", Declaration.class, "array [1..3] of var bool");
        assertResult(fzn.TYPE, "array [int] of var int", Declaration.class, "array [int] of var int");
        assertResult(fzn.TYPE, "array [1..3] of var int", Declaration.class, "array [1..3] of var int");
        assertResult(fzn.TYPE, "array [int] of var 2..5", Declaration.class, "array [int] of var 2..5");
        assertResult(fzn.TYPE, "array [1..3] of var 3..6", Declaration.class, "array [1..3] of var 3..6");
        assertResult(fzn.TYPE, "array [int] of var {2,4,6}", Declaration.class, "array [int] of var {2,4,6}");
        assertResult(fzn.TYPE, "array [1..3] of var {3,5,7}", Declaration.class, "array [1..3] of var {3,5,7}");
        assertResult(fzn.TYPE, "array [int] of var set of int", Declaration.class, "array [int] of var set of int");
        assertResult(fzn.TYPE, "array [int] of var set of 1..3", Declaration.class, "array [int] of var set of 1..3");
        assertResult(fzn.TYPE, "array [int] of var set of {1,2,3,5}", Declaration.class, "array [int] of var set of {1,2,3,5}");
        assertResult(fzn.TYPE, "array [1..3] of var set of int", Declaration.class, "array [1..3] of var set of int");
        assertResult(fzn.TYPE, "array [1..3] of var set of 1..3", Declaration.class, "array [1..3] of var set of 1..3");
        assertResult(fzn.TYPE, "array [1..3] of var set of {1,2,3,5}", Declaration.class, "array [1..3] of var set of {1,2,3,5}");
    }

    @Test
    public void testBoolConst() {
        assertResult(fzn.BOOL_CONST, "true", EBool.class, "true");
        assertResult(fzn.BOOL_CONST, "false", EBool.class, "false");
    }


    @Test
    public void testIntConst() {
        assertResult(fzn.INT_CONST, "1", EInt.class, "1");
        assertResult(fzn.INT_CONST, "-1", EInt.class, "-1");
    }

    @Test
    public void testSetConst(){
        assertResult(fzn.SET_CONST_1, "1 .. 3", ESetBounds.class, "1..3");
        assertResult(fzn.SET_CONST_2, "{1,2,3,5}", ESetList.class, "{1,2,3,5}");
        assertResult(fzn.SET_CONST_2, "{}", ESetList.class, "{}");
    }

    @Test
    public void testIdArray() {
        fzn.map.put("q", new Object[]{null});
        assertResult(fzn.ID_ARRAY, "q[1]", EIdArray.class, "q[1]");
    }

    @Test
    public void testExpression(){
        assertResult(fzn.expression(), "true", EBool.class, "true");
        assertResult(fzn.expression(), "18", EInt.class, "18");
        assertResult(fzn.expression(), "1 .. 16", ESetBounds.class, "1..16");
        assertResult(fzn.expression(), "{}", ESetList.class, "{}");
//        assertResult((Parser<EIdArray>) fzn.EXPRESSION, "toto[1]", EIdArray.class, "1..16");
    }

    @Test
    public void testExpression2(){
        assertResult(fzn.expression(), "true", Expression.class, "true");
        assertResult(fzn.expression(), "[1..4]", Expression.class, "[1..4]");
        assertResult(fzn.expression(), "[1,2,24]", Expression.class, "[1,2,24]");
        fzn.map.put("q", new Object[]{null});
        assertResult(fzn.expression(), "q[1]", Expression.class, "q[1]");
    }

    @Test
    public void testT(){
        assertResult(fzn.expression(), "q", EIdentifier.class, "q");
        fzn.map.put("q", new Object[]{null});
        assertResult(fzn.expression(), "q[1]", EIdArray.class, "q[1]");
    }

    @Test
    public void testListExpression(){
        fzn.map.put("a", null);
        fzn.map.put("b", null);
        Object o = TerminalParser.parse(fzn.list(fzn.expression()), "(a,b)");
        Assert.assertTrue(List.class.isInstance(o));
        List l = (List)o;
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(Expression.class.isInstance(l.get(0)));
        Assert.assertTrue(EIdentifier.class.isInstance(l.get(0)));
    }

    @Test
    public void testListExpression2(){
        fzn.map.put("q", new Object[]{null, null});
        Object o = TerminalParser.parse(fzn.list(fzn.expression()), "(q[1],q[2])");
        Assert.assertTrue(List.class.isInstance(o));
        List l = (List)o;
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(Expression.class.isInstance(l.get(0)));
        Assert.assertTrue(EIdArray.class.isInstance(l.get(0)));
    }

    @Test
    public void testAnnotation(){
//        assertResult(fzn.ANNOTATION, "q", EAnnotation.class, "q");
        assertResult(fzn.expression(), "q(1)", EAnnotation.class, "q(1)");
        assertResult(fzn.expression(), "q([1..4])", EAnnotation.class, "q([1..4])");
    }

    @Test
    public void testAnnotations(){
        Object o = TerminalParser.parse(fzn.ANNOTATIONS, "::q([1..4])::toto");
        Assert.assertTrue(o instanceof List);
        Assert.assertEquals(2, ((List) o).size());
    }

    @Test
    public void testAnnotations2(){
        Object o = TerminalParser.parse(fzn.ANNOTATIONS, "::seq_search([int_search([a],input_order,indomain_min,complete)])");
        Assert.assertTrue(o instanceof List);
        Assert.assertEquals(1, ((List) o).size());
    }

    @Test
    public void testPredicateParam(){
        assertResult(fzn.PRED_PARAM, "array [int] of var int: y", PredParam.class, "array [int] of var int: y");
    }

    static <T> void assertResult(
            Parser<T> parser, String source, Class<? extends T> expectedType, String expectedResult) {
        assertToString(expectedType, expectedResult, TerminalParser.parse(parser, source));
    }

    static <T> void assertToString(
            Class<? extends T> expectedType, String expectedResult, T result) {
        Assert.assertTrue(expectedType.isInstance(result));
        Assert.assertEquals(expectedResult, result.toString());
    }
    
}
