/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.measure.Measures;
import org.chocosolver.solver.search.measure.MeasuresRecorder;
import org.chocosolver.solver.search.strategy.decision.DecisionMakerTest;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 *
 * @author Arnaud Malapert
 */
public class MeasuresTest {


    private void testMeasuresCreation(IMeasures meas) {
        Assert.assertEquals(meas.getSearchState(), SearchState.NEW);
        Assert.assertEquals(meas.getModelName(), "Test");
        Assert.assertEquals(meas.getTimeCountInNanoSeconds(), 0);
        Assert.assertEquals(meas.getTimeCount(), 0, 0.001);
        Assert.assertFalse(meas.hasObjective());
        Assert.assertFalse(meas.isObjectiveOptimal());
    }

    private void testMeasuresExport(IMeasures meas) {
        Assert.assertNotNull(meas.toArray());
        Assert.assertNotNull(meas.toCSV());
        Assert.assertNotNull(meas.toDimacsString());
        Assert.assertNotNull(meas.toMultiLineString());
        Assert.assertNotNull(meas.toOneLineString());
        Assert.assertNotNull(meas.toString());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void measuresTest() {
        Measures meas = new Measures("Test");
        testMeasuresCreation(meas);
        testMeasuresExport(meas);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class, groups = "1s", timeOut = 60000)
    public void measExceptionTest() {
        Measures meas = new Measures("Test");
        meas.getBestSolutionValue();
    }
    

    @Test(groups = "1s", timeOut = 60000)
    public void measRecTest() throws InterruptedException {
        MeasuresRecorder meas = new MeasuresRecorder("Test");
        testMeasuresCreation(meas);
        meas.incBackTrackCount();
        Assert.assertEquals(meas.getBackTrackCount(), 1);
        meas.incNodeCount();
        meas.incNodeCount();
        Assert.assertEquals(meas.getNodeCount(), 2);
        // start watch
        meas.startStopwatch();
        Thread.sleep(200);
        final int ms2ns = 1000 * 1000;
        Assert.assertTrue(meas.getTimeCountInNanoSeconds() > 100 * ms2ns);
        //stop
        meas.stopStopwatch();
        long timeCount1 = meas.getTimeCountInNanoSeconds();
        Measures mSaved = new Measures(meas);
        Thread.sleep(50);
        Assert.assertEquals(meas.getTimeCountInNanoSeconds(), timeCount1);
        // restart
        meas.startStopwatch();
        Assert.assertTrue(meas.getTimeCountInNanoSeconds() < 100 * ms2ns);
        //stop
        meas.stopStopwatch();
        long timeCount2 = meas.getTimeCountInNanoSeconds();
        Thread.sleep(50);
        Assert.assertEquals(mSaved.getTimeCountInNanoSeconds(), timeCount1);
        Assert.assertEquals(meas.getTimeCountInNanoSeconds(), timeCount2);
        testMeasuresExport(meas);
    }
    
    private static void assertEqualMeasures(IMeasures actual, IMeasures expected) {
        Assert.assertEquals(actual.getModelName(), expected.getModelName());
        Assert.assertEquals(actual.getSearchState(), expected.getSearchState());
       Assert.assertEquals(actual.getBackTrackCount(), expected.getBackTrackCount());
        Assert.assertEquals(actual.getCurrentDepth(),expected.getCurrentDepth());
        Assert.assertEquals(actual.getFailCount(), expected.getFailCount());
        Assert.assertEquals(actual.getMaxDepth(), expected.getMaxDepth());
        Assert.assertEquals(actual.getNodeCount(), expected.getNodeCount());
        Assert.assertEquals(actual.getReadingTimeCount(), expected.getReadingTimeCount());
        Assert.assertEquals(actual.getRestartCount(), expected.getRestartCount());
        Assert.assertEquals(actual.getSolutionCount(), expected.getSolutionCount());
        Assert.assertEquals(actual.getTimeCountInNanoSeconds(), expected.getTimeCountInNanoSeconds());
        if(expected.hasObjective()) {
            Assert.assertEquals(actual.getBestSolutionValue(), expected.getBestSolutionValue());
            Assert.assertEquals(actual.isObjectiveOptimal(), expected.isObjectiveOptimal());
        } else {
            Assert.assertEquals(actual.getBoundsManager(), expected.getBoundsManager());
        }  
    }
    
    @Test(groups = "1s", timeOut = 60000)
    public void measSerializeTest() throws ClassNotFoundException, IOException, InterruptedException {
        MeasuresRecorder mr1 = new MeasuresRecorder("Test");
        mr1.startStopwatch();
        Thread.sleep(10);
        mr1.incBackTrackCount();
        mr1.incFailCount();
        mr1.incNodeCount();
        mr1.incSolutionCount();
        mr1.incSolutionCount();
        mr1.stopStopwatch();
        mr1.setSearchState(SearchState.TERMINATED);
        MeasuresRecorder mr2 = (MeasuresRecorder) DecisionMakerTest.doSerialize(mr1);
        assertEqualMeasures(mr2, mr1);
        Measures m1= new Measures(mr1);
        Measures m2 = (Measures) DecisionMakerTest.doSerialize(m1);
        assertEqualMeasures(m2, m1);        
    }

    @Test(groups="1s", timeOut=60000)
    public void testReading(){
        Model model = new Model();
        int n = 100_000;
        IntVar[] vars= model.intVarArray(n, 0, 10);
        model.arithm(vars[0], "=", 5).post();
        model.arithm(vars[0], "!=", 5).post();
        for(int i = 0 ; i < n; i++){
            model.arithm(vars[i], "<", 5).post();
        }
        model.getSolver().limitNode(0);
        model.getSolver().solve();

        Assert.assertTrue(model.getSolver().getReadingTimeCountInNanoSeconds() > 0);
        Assert.assertTrue(model.getSolver().getReadingTimeCount() > 0.0);
    }
}
