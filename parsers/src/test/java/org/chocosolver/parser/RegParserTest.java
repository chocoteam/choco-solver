/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import org.chocosolver.solver.search.strategy.SearchParams;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/06/2020
 */
public class RegParserTest {

    RegParser parser;

    @BeforeMethod(alwaysRun = true)
    public void before() {
        parser = new RegParser("test") {

            @Override
            public void createSettings() {
            }

            @Override
            public void createSolver() {

            }

            @Override
            public Thread actionOnKill() {
                return null;
            }

            @Override
            public void buildModel() {

            }

            @Override
            protected void singleThread() {

            }

            @Override
            protected void manyThread() {

            }
        };
    }

    @Test(groups = "1s")
    public void testFile1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        try {
            p.parseArgument();
            Assert.fail();
        } catch (CmdLineException ignored) {
        }
        Assert.assertFalse(parser.free);
        p.parseArgument("/file");
    }

    @Test(groups = "1s")
    public void testLimit1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.limits.getTime(), -1);
        p.parseArgument("-limit=[1]", "/file");
        Assert.assertEquals(parser.limits.getTime(), 1);
        p.parseArgument("-limit=[1s]", "/file");
        Assert.assertEquals(parser.limits.getTime(), 1000);
        p.parseArgument("-limit=[1m1s]", "/file");
        Assert.assertEquals(parser.limits.getTime(), 61000);
        p.parseArgument("-limit=[1h1m1s]", "/file");
        Assert.assertEquals(parser.limits.getTime(), 3661000);
    }

    @Test(groups = "1s")
    public void testLimit2() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.limits.getSols(), -1);
        p.parseArgument("-limit=[2sols]", "/file");
        Assert.assertEquals(parser.limits.getSols(), 2);
    }

    @Test(groups = "1s")
    public void testLimit3() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.limits.getRuns(), -1);
        p.parseArgument("-limit=[2runs]", "/file");
        Assert.assertEquals(parser.limits.getRuns(), 2);
    }


    @Test(groups = "1s")
    public void testLimit4() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-limit=[2s,2sols,2runs]", "/file");
        Assert.assertEquals(parser.limits.getTime(), 2000);
        Assert.assertEquals(parser.limits.getSols(), 2);
        Assert.assertEquals(parser.limits.getRuns(), 2);
    }

    @Test(groups = "1s")
    public void testStat1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.level, Level.COMPET);
        p.parseArgument("-lvl", "INFO", "/file");
        Assert.assertEquals(parser.level, Level.INFO);
    }

    @Test(groups = "1s")
    public void testCsv1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertFalse(parser.csv);
        p.parseArgument("-csv", "/file");
        Assert.assertTrue(parser.csv);
    }

    @Test(groups = "1s")
    public void testVarh1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.varH, SearchParams.VariableSelection.DOMWDEG_CACD);
        p.parseArgument("-f", "-varh", "chs", "/file");
        Assert.assertEquals(parser.varH, SearchParams.VariableSelection.CHS);
    }

    @Test(groups = "1s", expectedExceptions = CmdLineException.class)
    public void testVarh2() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-varh", "input", "/file");
    }

    @Test(groups = "1s")
    public void testVarsel1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertNull(parser.varsel);
        p.parseArgument("-f", "-varsel", "[CHS,LARGEST_DOMAIN,64]", "/file");
        Assert.assertEquals(parser.varsel, new SearchParams.VarSelConf(
                SearchParams.VariableSelection.CHS, 64));
    }

    @Test(groups = "1s")
    public void testVarl1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.valH, SearchParams.ValueSelection.MIN);
        p.parseArgument("-f", "-valh", "max", "/file");
        Assert.assertEquals(parser.valH, SearchParams.ValueSelection.MAX);
    }

    @Test(groups = "1s", expectedExceptions = CmdLineException.class)
    public void testVarl2() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-varl", "max", "/file");
    }

    @Test(groups = "1s")
    public void testValsel1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertNull(parser.valsel);
        p.parseArgument("-f", "-valsel", "[MAX,true,32,true]", "/file");
        Assert.assertEquals(parser.valsel, new SearchParams.ValSelConf(SearchParams.ValueSelection.MAX, true, 32, true));
    }

    @Test(groups = "1s")
    public void test() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
    }


    @Test(groups = "1s")
    public void testFree1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertFalse(parser.free);
        p.parseArgument("-f", "/file");
        Assert.assertTrue(parser.free);
    }

    @Test(groups = "1s", expectedExceptions = CmdLineException.class)
    public void testLc1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-lc", "2", "/file");
    }

    @Test(groups = "1s")
    public void testLc2() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.lc, 0);
        p.parseArgument("-f", "-lc", "2", "/file");
        Assert.assertEquals(parser.lc, 2);
    }

    @Test(groups = "1s", expectedExceptions = CmdLineException.class)
    public void testLc3() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-f", "-cos", "-lc", "2", "/file");
    }

    @Test(groups = "1s", expectedExceptions = CmdLineException.class)
    public void testCos1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-cos", "/file");
        Assert.assertTrue(parser.cos);
    }

    @Test(groups = "1s")
    public void testCos2() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-f", "-cos", "/file");
        Assert.assertTrue(parser.cos);
    }

}