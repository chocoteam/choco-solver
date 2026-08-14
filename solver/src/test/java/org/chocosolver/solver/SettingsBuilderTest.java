/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.constraints.real.Ibex;
import org.kohsuke.args4j.CmdLineException;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.*;

/**
 * Test class for {@link SettingsBuilder} to ensure all arguments are correctly taken into account.
 * This includes testing default values, setters, fromProperties, fromArgs, and propagation to Settings.
 *
 * @author Mistral Vibe
 * @since 09/07/2026
 */
public class SettingsBuilderTest {

    /**
     * Test default values of SettingsBuilder
     */
    @Test
    public void testDefaultValues() {
        SettingsBuilder builder = SettingsBuilder.init();

        // Boolean defaults
        assertTrue(builder.cloneVariableArrayInPropagator(), "cloneVariableArrayInPropagator should be true by default");
        assertTrue(builder.enableViews(), "enableViews should be true by default");
        assertFalse(builder.enableTableSubstitution(), "enableTableSubstitution should be false by default");
        assertTrue(builder.sortPropagatorActivationWRTPriority(), "sortPropagatorActivationWRTPriority should be true by default");
        assertFalse(builder.warnUser(), "warnUser should be false by default");
        assertFalse(builder.enableDecompositionOfBooleanSum(), "enableDecompositionOfBooleanSum should be false by default");
        assertFalse(builder.enableSAT(), "enableSAT should be false by default");
        assertTrue(builder.swapOnPassivate(), "swapOnPassivate should be true by default");
        assertTrue(builder.checkDeclaredConstraints(), "checkDeclaredConstraints should be true by default");
        assertTrue(builder.checkDeclaredViews(), "checkDeclaredViews should be true by default");
        assertTrue(builder.checkDeclaredMonitors(), "checkDeclaredMonitors should be true by default");
        assertFalse(builder.printAllUndeclaredConstraints(), "printAllUndeclaredConstraints should be false by default");
        assertFalse(builder.enableIntVarLazyLitWithWeakBounds(), "intVarLazyLitWithWeakBounds should be false by default");
        assertEquals(builder.getIbexRestoreRounding(), Ibex.PRESERVE_ROUNDING, "ibexRestoreRounding should match Ibex.PRESERVE_ROUNDING");
        assertFalse(builder.isLCG(), "lcg should be false by default");
        assertTrue(builder.sortLitsOnSolution(), "sortLitsOnSolution should be true by default");
        assertFalse(builder.lcgExtractFromVariablesOnSolution(), "lcgExtractFromVariablesOnSolution should be false by default");
//        assertTrue(builder.nogoodFromRestartWithSAT(), "nogoodFromRestartWithSAT should be true by default");
//        assertFalse(builder.nogoodFromRestartMinimize(), "nogoodFromRestartMinimize should be false by default");

        // Integer defaults
        assertEquals(builder.getEnumeratedDomainSizeThreshold(), 1 << 16, "enumeratedDomainSizeThreshold should be 65536 by default");
        assertEquals(builder.getMinCardForSumDecomposition(), 50, "minCardForSumDecomposition should be 50 by default");
        assertEquals(builder.getMaxTupleSizeForSubstitution(), 10_000, "maxTupleSizeForSubstitution should be 10000 by default");
        assertEquals(builder.getMaxSizeInMBToUseCompactTable(), 1024L, "maxSizeInMBToUseCompactTable should be 1024 by default");
        assertEquals(builder.getIncrementalityOnBoolSumThreshold(), 10, "incrementalityOnBoolSumThreshold should be 10 by default");
        assertEquals(builder.setPropagationEngineType(), (byte) 0b00, "propagationEngineType should be 0b00 by default");
        assertEquals(builder.getNbMaxLearntClauses(), 100_000, "nbMaxLearntClauses should be 100000 by default");
        assertEquals(builder.getReduceLearntClausesBase(), 1_000, "reduceLearntClausesBase should be 1000 by default");
        assertEquals(builder.getReduceLearntClausesFactor(), 100, "reduceLearntClausesFactor should be 100 by default");
        assertEquals(builder.getReasonManager(), 2, "reasonManager should be 2 by default");
        assertEquals(builder.getSatCCMinMode(), 0, "satCCMinMode should be 0 by default");

        // Double defaults
        assertEquals(builder.getIbexContractionRatio(), Ibex.RATIO, "ibexContractionRatio should match Ibex.RATIO");

        // Map defaults
        assertTrue(builder.getAdditionalSettings().isEmpty(), "additionalSettings should be empty by default");
    }

    /**
     * Test that all setters correctly modify their respective fields
     */
    @Test
    public void testSetters() {
        SettingsBuilder builder = SettingsBuilder.init();

        // Test boolean setters
        builder.setCloneVariableArrayInPropagator(false);
        assertFalse(builder.cloneVariableArrayInPropagator(), "cloneVariableArrayInPropagator should be false after setter");

        builder.setEnableViews(false);
        assertFalse(builder.enableViews(), "enableViews should be false after setter");

        builder.setEnableTableSubstitution(true);
        assertTrue(builder.enableTableSubstitution(), "enableTableSubstitution should be true after setter");

        builder.setSortPropagatorActivationWRTPriority(false);
        assertFalse(builder.sortPropagatorActivationWRTPriority(), "sortPropagatorActivationWRTPriority should be false after setter");

        builder.setWarnUser(true);
        assertTrue(builder.warnUser(), "warnUser should be true after setter");

        builder.setEnableDecompositionOfBooleanSum(true);
        assertTrue(builder.enableDecompositionOfBooleanSum(), "enableDecompositionOfBooleanSum should be true after setter");

        builder.setEnableSAT(true);
        assertTrue(builder.enableSAT(), "enableSAT should be true after setter");

        builder.setSwapOnPassivate(false);
        assertFalse(builder.swapOnPassivate(), "swapOnPassivate should be false after setter");

        builder.setCheckDeclaredConstraints(false);
        assertFalse(builder.checkDeclaredConstraints(), "checkDeclaredConstraints should be false after setter");

        builder.setCheckDeclaredViews(false);
        assertFalse(builder.checkDeclaredViews(), "checkDeclaredViews should be false after setter");

        builder.setCheckDeclaredMonitors(false);
        assertFalse(builder.checkDeclaredMonitors(), "checkDeclaredMonitors should be false after setter");

        builder.setPrintAllUndeclaredConstraints(true);
        assertTrue(builder.printAllUndeclaredConstraints(), "printAllUndeclaredConstraints should be true after setter");

        builder.setIntVarLazyLitWithWeakBounds(true);
        assertTrue(builder.enableIntVarLazyLitWithWeakBounds(), "intVarLazyLitWithWeakBounds should be true after setter");

        builder.setIbexRestoreRounding(true);
        assertTrue(builder.getIbexRestoreRounding(), "ibexRestoreRounding should be true after setter");

        builder.setLCG(true);
        assertTrue(builder.isLCG(), "lcg should be true after setter");
        assertTrue(builder.enableSAT(), "enableSAT should be true when lcg is set to true");

        builder.setSortLitsOnSolution(false);
        assertFalse(builder.sortLitsOnSolution(), "sortLitsOnSolution should be false after setter");

        builder.setLcgExtractFromVariablesOnSolution(true);
        assertTrue(builder.lcgExtractFromVariablesOnSolution(), "lcgExtractFromVariablesOnSolution should be true after setter");

//        builder.setNogoodFromRestartWithSAT(false);
//        assertFalse(builder.nogoodFromRestartWithSAT(), "nogoodFromRestartWithSAT should be false after setter");

//        builder.setNogoodFromRestartMinimize(true);
//        assertTrue(builder.nogoodFromRestartMinimize(), "nogoodFromRestartMinimize should be true after setter");

        // Test integer setters
        builder.setEnumeratedDomainSizeThreshold(1000);
        assertEquals(builder.getEnumeratedDomainSizeThreshold(), 1000, "enumeratedDomainSizeThreshold should be 1000 after setter");

        builder.setMinCardinalityForSumDecomposition(100);
        assertEquals(builder.getMinCardForSumDecomposition(), 100, "minCardForSumDecomposition should be 100 after setter");

        builder.setMaxTupleSizeForSubstitution(5000);
        assertEquals(builder.getMaxTupleSizeForSubstitution(), 5000, "maxTupleSizeForSubstitution should be 5000 after setter");

        builder.setMaxSizeInMBToUseCompactTable(2048);
        assertEquals(builder.getMaxSizeInMBToUseCompactTable(), 2048L, "maxSizeInMBToUseCompactTable should be 2048 after setter");

        builder.setIncrementalityOnBoolSumThreshold(20);
        assertEquals(builder.getIncrementalityOnBoolSumThreshold(), 20, "incrementalityOnBoolSumThreshold should be 20 after setter");

        builder.getPropagationEnginType((byte) 0b10);
        assertEquals(builder.setPropagationEngineType(), (byte) 0b10, "propagationEngineType should be 0b10 after setter");

        builder.setNbMaxLearntClauses(200_000);
        assertEquals(builder.getNbMaxLearntClauses(), 200_000, "nbMaxLearntClauses should be 200000 after setter");

        builder.setReduceLearntClausesBase(2000);
        assertEquals(builder.getReduceLearntClausesBase(), 2000, "reduceLearntClausesBase should be 2000 after setter");

        builder.setReduceLearntClausesFactor(200);
        assertEquals(builder.getReduceLearntClausesFactor(), 200, "reduceLearntClausesFactor should be 200 after setter");

        builder.setReasonManager(1);
        assertEquals(builder.getReasonManager(), 1, "reasonManager should be 1 after setter");

        builder.setSatCCMinMode(2);
        assertEquals(builder.getSatCCMinMode(), 2, "satCCMinMode should be 2 after setter");

        // Test double setters
        builder.setIbexContractionRatio(0.05);
        assertEquals(builder.getIbexContractionRatio(), 0.05, 0.0001, "ibexContractionRatio should be 0.05 after setter");

        // Test additional settings
        builder.set("custom.setting", "custom.value");
        assertEquals(builder.getAdditionalSettings().get("custom.setting"), "custom.value", "custom setting should be stored");
        assertEquals(builder.get("custom.setting").orElse(null), "custom.value", "custom setting should be retrievable");
    }

    /**
     * Test that prod() method sets correct values for production environment
     */
    @Test
    public void testProdSettings() {
        SettingsBuilder builder = SettingsBuilder.prod();

        // In production, checks and warnings should be turned off
        assertTrue(builder.getModelChecker().test(null), "modelChecker should always return true in prod");
        assertFalse(builder.warnUser(), "warnUser should be false in prod");
        assertFalse(builder.checkDeclaredConstraints(), "checkDeclaredConstraints should be false in prod");
        assertFalse(builder.printAllUndeclaredConstraints(), "printAllUndeclaredConstraints should be false in prod");

        // Other settings should keep their default values
        assertTrue(builder.cloneVariableArrayInPropagator(), "cloneVariableArrayInPropagator should keep default in prod");
        assertTrue(builder.enableViews(), "enableViews should keep default in prod");
    }

    /**
     * Test that dev() method sets correct values for development environment
     */
    @Test
    public void testDevSettings() {
        SettingsBuilder builder = SettingsBuilder.dev();

        // In development, checks and warnings should be turned on
        assertTrue(builder.warnUser(), "warnUser should be true in dev");
        assertTrue(builder.printAllUndeclaredConstraints(), "printAllUndeclaredConstraints should be true in dev");

        // Other settings should keep their default values
        assertTrue(builder.cloneVariableArrayInPropagator(), "cloneVariableArrayInPropagator should keep default in dev");
        assertTrue(builder.enableViews(), "enableViews should keep default in dev");
    }

    /**
     * Test that fromProperties correctly sets all fields
     */
    @Test
    public void testFromProperties() {
        Properties props = new Properties();

        // Boolean properties
        props.setProperty("cloneVariableArrayInPropagator", "false");
        props.setProperty("enableViews", "false");
        props.setProperty("enableTableSubstitution", "true");
        props.setProperty("sortPropagatorActivationWRTPriority", "false");
        props.setProperty("warnUser", "true");
        props.setProperty("enableDecompositionOfBooleanSum", "true");
        props.setProperty("enableSAT", "true");
        props.setProperty("swapOnPassivate", "false");
        props.setProperty("checkDeclaredConstraints", "false");
        props.setProperty("checkDeclaredViews", "false");
        props.setProperty("checkDeclaredMonitors", "false");
        props.setProperty("printAllUndeclaredConstraints", "true");
        props.setProperty("intVarLazyLitWithWeakBounds", "true");
        props.setProperty("ibexRestoreRounding", "true");
        props.setProperty("lcg", "true");
        props.setProperty("sortLitsOnSolution", "false");
        props.setProperty("lcgExtractFromVariablesOnSolution", "true");
        props.setProperty("nogoodFromRestartWithSAT", "false");
        props.setProperty("nogoodFromRestartMinimize", "true");

        // Integer properties
        props.setProperty("enumeratedDomainSizeThreshold", "2000");
        props.setProperty("minCardForSumDecomposition", "200");
        props.setProperty("maxTupleSizeForSubstitution", "8000");
        props.setProperty("maxSizeInMBToUseCompactTable", "2048");
        props.setProperty("incrementalityOnBoolSumThreshold", "30");
        props.setProperty("propagationEngineType", "2");
        props.setProperty("nbMaxLearntClauses", "200000");
        props.setProperty("reduceLearntClausesBase", "2000");
        props.setProperty("reduceLearntClausesFactor", "200");
        props.setProperty("reasonManager", "1");
        props.setProperty("satCCMinMode", "1");

        // Double properties
        props.setProperty("ibexContractionRatio", "0.05");

        // Additional property
        props.setProperty("custom.property", "custom.value");

        SettingsBuilder builder = SettingsBuilder.init().fromProperties(props);

        // Verify boolean properties
        assertFalse(builder.cloneVariableArrayInPropagator());
        assertFalse(builder.enableViews());
        assertTrue(builder.enableTableSubstitution());
        assertFalse(builder.sortPropagatorActivationWRTPriority());
        assertTrue(builder.warnUser());
        assertTrue(builder.enableDecompositionOfBooleanSum());
        assertTrue(builder.enableSAT());
        assertFalse(builder.swapOnPassivate());
        assertFalse(builder.checkDeclaredConstraints());
        assertFalse(builder.checkDeclaredViews());
        assertFalse(builder.checkDeclaredMonitors());
        assertTrue(builder.printAllUndeclaredConstraints());
        assertTrue(builder.enableIntVarLazyLitWithWeakBounds());
        assertTrue(builder.getIbexRestoreRounding());
        assertTrue(builder.isLCG());
        assertFalse(builder.sortLitsOnSolution());
        assertTrue(builder.lcgExtractFromVariablesOnSolution());
//        assertFalse(builder.nogoodFromRestartWithSAT());
//        assertTrue(builder.nogoodFromRestartMinimize());

        // Verify integer properties
        assertEquals(builder.getEnumeratedDomainSizeThreshold(), 2000);
        assertEquals(builder.getMinCardForSumDecomposition(), 200);
        assertEquals(builder.getMaxTupleSizeForSubstitution(), 8000);
        assertEquals(builder.getMaxSizeInMBToUseCompactTable(), 2048L);
        assertEquals(builder.getIncrementalityOnBoolSumThreshold(), 30);
        assertEquals(builder.setPropagationEngineType(), (byte) 2);
        assertEquals(builder.getNbMaxLearntClauses(), 200000);
        assertEquals(builder.getReduceLearntClausesBase(), 2000);
        assertEquals(builder.getReduceLearntClausesFactor(), 200);
        assertEquals(builder.getReasonManager(), 1);
        assertEquals(builder.getSatCCMinMode(), 1);

        // Verify double properties
        assertEquals(builder.getIbexContractionRatio(), 0.05, 0.0001);

        // Verify additional property
        assertEquals(builder.get("custom.property").orElse(null), "custom.value");
    }

    /**
     * Test that fromArgs correctly sets all fields
     */
    @Test
    public void testFromArgs() throws CmdLineException {
        String[] args = {
                "--cloneVariableArrayInPropagator", "false",
                "--enableViews", "false",
                "--model.enumeratedDomainSizeThreshold", "3000",
                "--model.minCardForSumDecomposition", "300",
                "--model.enableTableSubstitution", "true",
                "--model.maxTupleSizeForSubstitution", "9000",
                "--model.maxSizeInMBToUseCompactTable", "3072",
                "--prop.sortPropagatorActivationWRTPriority", "false",
                "--model.warnUser", "true",
                "--model.enableDecompositionOfBooleanSum", "true",
                "--model.incrementalityOnBoolSumThreshold", "40",
                "--model.enableSAT", "true",
                "--prop.swapOnPassivate", "false",
                "--model.checkDeclaredConstraints", "false",
                "--model.checkDeclaredViews", "false",
                "--model.checkDeclaredMonitors", "false",
                "--model.printAllUndeclaredConstraints", "true",
                "--prop.propagationEngineType", "1",
                "--sat.nbMaxLearntClauses", "300000",
                "--sat.reduceLearntClausesBase", "3000",
                "--sat.reduceLearntClausesFactor", "300",
                "--sat.intVarLazyLitWithWeakBounds", "true",
                "--ibex.contractionRatio", "0.1",
                "--ibex.restoreRounding", "true",
                "-lcg",
//                "-ngmin", "true",
                "--sat.reasonManager", "0",
//                "--nogood.withSAT", "false",
                "--model.lcg.sortlits", "false",
                "-ccmin", "2",
                "--model.lcg.extractFromVariables", "true",
                "-P", "custom.arg=arg.value"
        };

        SettingsBuilder builder = SettingsBuilder.init().fromArgs(args);

        // Verify boolean properties
        assertFalse(builder.cloneVariableArrayInPropagator());
        assertFalse(builder.enableViews());
        assertTrue(builder.enableTableSubstitution());
        assertFalse(builder.sortPropagatorActivationWRTPriority());
        assertTrue(builder.warnUser());
        assertTrue(builder.enableDecompositionOfBooleanSum());
        assertTrue(builder.enableSAT());
        assertFalse(builder.swapOnPassivate());
        assertFalse(builder.checkDeclaredConstraints());
        assertFalse(builder.checkDeclaredViews());
        assertFalse(builder.checkDeclaredMonitors());
        assertTrue(builder.printAllUndeclaredConstraints());
        assertTrue(builder.enableIntVarLazyLitWithWeakBounds());
        assertTrue(builder.getIbexRestoreRounding());
        assertTrue(builder.isLCG());
        assertFalse(builder.sortLitsOnSolution());
        assertTrue(builder.lcgExtractFromVariablesOnSolution());
//        assertFalse(builder.nogoodFromRestartWithSAT());
//        assertTrue(builder.nogoodFromRestartMinimize());

        // Verify integer properties
        assertEquals(builder.getEnumeratedDomainSizeThreshold(), 3000);
        assertEquals(builder.getMinCardForSumDecomposition(), 300);
        assertEquals(builder.getMaxTupleSizeForSubstitution(), 9000);
        assertEquals(builder.getMaxSizeInMBToUseCompactTable(), 3072L);
        assertEquals(builder.getIncrementalityOnBoolSumThreshold(), 40);
        assertEquals(builder.setPropagationEngineType(), (byte) 1);
        assertEquals(builder.getNbMaxLearntClauses(), 300000);
        assertEquals(builder.getReduceLearntClausesBase(), 3000);
        assertEquals(builder.getReduceLearntClausesFactor(), 300);
        assertEquals(builder.getReasonManager(), 0);
        assertEquals(builder.getSatCCMinMode(), 2);

        // Verify double properties
        assertEquals(builder.getIbexContractionRatio(), 0.1, 0.0001);

        // Verify additional settings from -P argument
        assertEquals(builder.get("custom.arg").orElse(null), "arg.value");
    }

    /**
     * Test that values are correctly propagated from SettingsBuilder to Settings
     */
    @Test
    public void testBuildPropagation() {
        SettingsBuilder builder = SettingsBuilder.init()
                .setCloneVariableArrayInPropagator(false)
                .setEnableViews(false)
                .setEnumeratedDomainSizeThreshold(5000)
                .setMinCardinalityForSumDecomposition(250)
                .setEnableTableSubstitution(true)
                .setMaxTupleSizeForSubstitution(15000)
                .setMaxSizeInMBToUseCompactTable(4096)
                .setSortPropagatorActivationWRTPriority(false)
                .setWarnUser(true)
                .setEnableDecompositionOfBooleanSum(true)
                .setIncrementalityOnBoolSumThreshold(50)
                .setEnableSAT(true)
                .setSwapOnPassivate(false)
                .setCheckDeclaredConstraints(false)
                .setCheckDeclaredViews(false)
                .setCheckDeclaredMonitors(false)
                .setPrintAllUndeclaredConstraints(true)
                .getPropagationEnginType((byte) 0b10)
                .setNbMaxLearntClauses(500000)
                .setReduceLearntClausesBase(5000)
                .setReduceLearntClausesFactor(500)
                .setIntVarLazyLitWithWeakBounds(true)
                .setIbexContractionRatio(0.2)
                .setIbexRestoreRounding(true)
                .setLCG(true)
                .setReasonManager(0)
                .setSortLitsOnSolution(false)
                .setSatCCMinMode(1)
                .setLcgExtractFromVariablesOnSolution(true)
//                .setNogoodFromRestartWithSAT(false)
//                .setNogoodFromRestartMinimize(true)
                .set("test.key", "test.value");

        Settings settings = builder.build();

        // Verify all values are correctly propagated
        assertFalse(settings.cloneVariableArrayInPropagator());
        assertFalse(settings.enableViews());
        assertEquals(settings.getEnumeratedDomainSizeThreshold(), 5000);
        assertEquals(settings.getMinCardForSumDecomposition(), 250);
        assertTrue(settings.enableTableSubstitution());
        assertEquals(settings.getMaxTupleSizeForSubstitution(), 15000);
        assertEquals(settings.getMaxSizeInMBToUseCompactTable(), 4096L);
        assertFalse(settings.sortPropagatorActivationWRTPriority());
        assertTrue(settings.warnUser());
        assertTrue(settings.enableDecompositionOfBooleanSum());
        assertEquals(settings.getIncrementalityOnBoolSumThreshold(), 50);
        assertTrue(settings.enableSAT());
        assertFalse(settings.swapOnPassivate());
        assertFalse(settings.checkDeclaredConstraints());
        assertFalse(settings.checkDeclaredViews());
        assertFalse(settings.checkDeclaredMonitors());
        assertTrue(settings.printAllUndeclaredConstraints());
        assertEquals(settings.getPropagationEnginType(), (byte) 0b10);
        assertEquals(settings.getNbMaxLearntClauses(), 500000);
        assertEquals(settings.getReduceLearntClausesBase(), 5000);
        assertEquals(settings.getReduceLearntClausesFactor(), 500);
        assertTrue(settings.enableIntVarLazyLitWithWeakBounds());
        assertEquals(settings.getIbexContractionRatio(), 0.2, 0.0001);
        assertTrue(settings.getIbexRestoreRounding());
        assertTrue(settings.isLCG());
        assertEquals(settings.getReasonManager(), 0);
        assertFalse(settings.sortLitsOnSolution());
        assertEquals(settings.getSatCCMinMode(), 1);
        assertTrue(settings.lcgExtractFromVariablesOnSolution());
//        assertFalse(settings.nogoodFromRestartWithSAT());
//        assertTrue(settings.nogoodFromRestartMinimize());
        assertEquals(settings.get("test.key").orElse(null), "test.value");
    }

    /**
     * Test that fromProperties ignores unsupported properties (modelChecker, environmentSupplier, defaultSearch)
     */
    @Test
    public void testFromPropertiesIgnoresUnsupported() {
        Properties props = new Properties();
        props.setProperty("modelChecker", "some.predicate");
        props.setProperty("environmentSupplier", "some.supplier");
        props.setProperty("defaultSearch", "some.consumer");

        SettingsBuilder builder = SettingsBuilder.init().fromProperties(props);

        // These should not be affected by the properties
        assertNotNull(builder.getModelChecker());
        assertNotNull(builder.getEnvironmentSupplier());
        assertNotNull(builder.getDefaultSearch());
    }

    /**
     * Test that additional settings are stored in additionalSettings map
     */
    @Test
    public void testAdditionalSettings() {
        SettingsBuilder builder = SettingsBuilder.init()
                .set("custom.setting1", "value1")
                .set("custom.setting2", "value2");

        Settings settings = builder.build();

        assertEquals(settings.get("custom.setting1").orElse(null), "value1");
        assertEquals(settings.get("custom.setting2").orElse(null), "value2");
    }

    /**
     * Test that LCG mode automatically enables SAT
     */
    @Test
    public void testLCGEnablesSAT() {
        SettingsBuilder builder = SettingsBuilder.init();
        assertFalse(builder.enableSAT(), "SAT should be disabled by default");

        builder.setLCG(true);
        assertTrue(builder.enableSAT(), "SAT should be automatically enabled when LCG is enabled");

        // Test that manually enabling SAT doesn't interfere
        builder.setEnableSAT(false);
        builder.setLCG(true);
        assertTrue(builder.enableSAT(), "SAT should still be enabled when LCG is enabled, even if manually disabled");
    }

    @Test
    public void testCVAPValues() throws CmdLineException {
        SettingsBuilder builder = SettingsBuilder.init();
        assertTrue(builder.cloneVariableArrayInPropagator(), "cloneVariableArrayInPropagator should be automatically set to true");
        builder.fromArgs(new String[]{"-cvap", "false"});
        assertFalse(builder.cloneVariableArrayInPropagator(), "cloneVariableArrayInPropagator should be set to false");
        builder.fromArgs(new String[]{"-cvap", "true"});
        assertTrue(builder.cloneVariableArrayInPropagator(), "cloneVariableArrayInPropagator should be set to true");
        builder.fromArgs(new String[]{"-cvap", "false"});
        assertFalse(builder.cloneVariableArrayInPropagator(), "cloneVariableArrayInPropagator should be set to false");
        builder.fromArgs(new String[]{"-cvap"});
        assertTrue(builder.cloneVariableArrayInPropagator(), "cloneVariableArrayInPropagator should be set to true");
    }

    @Test
    public void testETSValues() throws CmdLineException {
        SettingsBuilder builder = SettingsBuilder.init();
        assertFalse(builder.enableTableSubstitution(), "enableTableSubstitution should be automatically set to false");
        builder.fromArgs(new String[]{"-ets", "true"});
        assertTrue(builder.enableTableSubstitution(), "enableTableSubstitution should be set to true");
        builder.fromArgs(new String[]{"-ets", "false"});
        assertFalse(builder.enableTableSubstitution(), "cloneVariableArrayInPropagator should be set to false");
        builder.fromArgs(new String[]{"-ets"});
        assertTrue(builder.enableTableSubstitution(), "cloneVariableArrayInPropagator should be set to false");
    }

    /**
     * Test that all option aliases work correctly with fromArgs
     */
    @Test
    public void testOptionAliases() throws CmdLineException {
        // Test using aliases for all options
        String[] args = {
                "-cvap", "false",
                "-ev", "false",
                "-edst", "100",
                "-mcfssd", "10",
                "-ets", "true",
                "-mtss", "50",
                "-msmbuct", "512",
                "-pawrp", "false",
                "-wu", "true",
                "-edobs", "true",
                "-icbst", "5",
                "-esat", "true",
                "-sop", "false",
                "-cdc", "false",
                "-cdv", "false",
                "-cdm", "false",
                "-paudc", "true",
                "-pet", "1",
                "-nblc", "500",
                "-rlcb", "50",
                "-rlcf", "5",
                "-ivllwwb", "true",
                "-icr", "0.02",
                "-irr", "true",
                "-lcg",
                "-rm", "1",
                "-slos", "false",
                "-ccmin", "1",
                "-lcgefvos", "true",
//                "-ngsat", "false",
//                "-ngmin", "true"
        };

        SettingsBuilder builder = SettingsBuilder.init().fromArgs(args);

        // Verify all values are set correctly using aliases
        assertFalse(builder.cloneVariableArrayInPropagator());
        assertFalse(builder.enableViews());
        assertEquals(builder.getEnumeratedDomainSizeThreshold(), 100);
        assertEquals(builder.getMinCardForSumDecomposition(), 10);
        assertTrue(builder.enableTableSubstitution());
        assertEquals(builder.getMaxTupleSizeForSubstitution(), 50);
        assertEquals(builder.getMaxSizeInMBToUseCompactTable(), 512L);
        assertFalse(builder.sortPropagatorActivationWRTPriority());
        assertTrue(builder.warnUser());
        assertTrue(builder.enableDecompositionOfBooleanSum());
        assertEquals(builder.getIncrementalityOnBoolSumThreshold(), 5);
        assertTrue(builder.enableSAT());
        assertFalse(builder.swapOnPassivate());
        assertFalse(builder.checkDeclaredConstraints());
        assertFalse(builder.checkDeclaredViews());
        assertFalse(builder.checkDeclaredMonitors());
        assertTrue(builder.printAllUndeclaredConstraints());
        assertEquals(builder.setPropagationEngineType(), (byte) 1);
        assertEquals(builder.getNbMaxLearntClauses(), 500);
        assertEquals(builder.getReduceLearntClausesBase(), 50);
        assertEquals(builder.getReduceLearntClausesFactor(), 5);
        assertTrue(builder.enableIntVarLazyLitWithWeakBounds());
        assertEquals(builder.getIbexContractionRatio(), 0.02, 0.0001);
        assertTrue(builder.getIbexRestoreRounding());
        assertTrue(builder.isLCG());
        assertEquals(builder.getReasonManager(), 1);
        assertFalse(builder.sortLitsOnSolution());
        assertEquals(builder.getSatCCMinMode(), 1);
        assertTrue(builder.lcgExtractFromVariablesOnSolution());
//        assertFalse(builder.nogoodFromRestartWithSAT());
//        assertTrue(builder.nogoodFromRestartMinimize());
    }
}
