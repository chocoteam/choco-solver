package org.chocosolver.solver.constraints.graph.connectivity;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Providers;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

public class ReifiedConnectivityTest {

    @DataProvider
    public Object[][] params() {
        return Providers.merge(new Object[][]{
                {SetType.BIPARTITESET},
                {SetType.SMALLBIPARTITESET},
                {SetType.LINKED_LIST},
                {SetType.BITSET},
                {SetType.RANGESET}
        }, new Object[][]{
                {2, 828, 367},
                {3, 5895, 2582},
                {4, 21179, 12384},
                {5, 38201, 40704},
                {6, 29592, 92768},
                {7, 4752, 144896}
        });
    }


    @Test(groups="10s", timeOut=120_000, dataProvider = "params")
    public void testReifiedConnectivity(SetType setType, int k, int f, int nf) {
        buildModelAndSolve(k, setType, f, nf);
    }

    /**
     * builds the model exactly as the Scala version does and enumerates all solutions
     */
    private static void buildModelAndSolve(int k, SetType setType, int nbfree, int nbtif) {
        Model model = new Model();

        /* ------------------------------------------------------------------ *
         *  1 –  Transactions (boolean variables)                              *
         * ------------------------------------------------------------------ */
        String[] transactions = {
                "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
                "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
                "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0",
                "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
                "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0",
                "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
                "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
                "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0",
                "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_CorePac2_isram_load_0",
                "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0",
                "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_isram_store_0",
                "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_CorePac3_isram_load_0",
                "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0",
                "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_isram_store_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
                "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
                "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
                "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0",
                "ioServer_SPI",
                "spi_CA",
                "spi_CB"
        };

        Map<String, BoolVar> transactionVar = new HashMap<>();
        for (String id : transactions) {
            transactionVar.put(id, model.boolVar(id));
        }

        /* ------------------------------------------------------------------ *
         *  2 –  Exclusive transaction groups (C¹)                              *
         * ------------------------------------------------------------------ */
        Map<String, String[]> exclusiveTr = new HashMap<>();
        exclusiveTr.put("KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                        "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0"
                });
        exclusiveTr.put("KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_isram_store_0",
                        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_CorePac2_isram_load_0",
                        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0",
                        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"
                });
        exclusiveTr.put("KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_isram_store_0",
                new String[]{
                        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_CorePac2_isram_load_0",
                        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0",
                        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"
                });
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                        "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"});
        exclusiveTr.put(
                "spi_CB",
                new String[]{"ioServer_SPI", "spi_CA"});
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                        "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
                        "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                        "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_isram_store_0",
                        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_CorePac2_isram_load_0",
                        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
                        "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                        "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
                        "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
                        "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                        "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                        "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_CorePac3_isram_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0",
                        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_isram_store_0",
                        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                        "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_CorePac2_isram_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_isram_store_0",
                        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0",
                        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"});
        exclusiveTr.put(
                "ioServer_SPI",
                new String[]{"spi_CA", "spi_CB"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                        "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                        "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0",
                new String[]{
                        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_isram_store_0",
                        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_CorePac2_isram_load_0",
                        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                        "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
                new String[]{
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0",
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0",
                new String[]{
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
                new String[]{
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"});
        exclusiveTr.put(
                "spi_CA",
                new String[]{"ioServer_SPI", "spi_CB"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                new String[]{
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                        "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0",
                new String[]{
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_CorePac3_isram_load_0",
                        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0",
                        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_isram_store_0",
                        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0",
                new String[]{
                        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_CorePac3_isram_load_0",
                        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_isram_store_0",
                        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0",
                new String[]{
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                        "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0",
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
                        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
                new String[]{
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                new String[]{
                        "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                        "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_isram_store_0",
                new String[]{
                        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_CorePac3_isram_load_0",
                        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                new String[]{
                        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_CorePac3_isram_load_0",
                        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_isram_store_0",
                        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0"});
        exclusiveTr.put(
                "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                new String[]{
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
                        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0"
                });

        // C¹ – exclusive constraints
        for (Map.Entry<String, String[]> e : exclusiveTr.entrySet()) {
            String tr = e.getKey();
            String[] ex = e.getValue();
            ReExpression acc = model.boolVar(true);
            if (ex.length > 0) {
                BoolVar[] notEx = Arrays.stream(ex)
                        .map(t2 -> transactionVar.get(t2).not())
                        .toArray(BoolVar[]::new);
                for (BoolVar b : notEx) {
                    acc = acc.and(b);
                }
                transactionVar.get(tr).imp(acc).post();
            }
        }

        // C² – cardinality constraint
        model.sum(transactionVar.values().toArray(new BoolVar[0]), "=", k).post();

        /* ------------------------------------------------------------------ *
         *  3 –  Graph definition (nodes, edges, upper/lower bound)           *
         * ------------------------------------------------------------------ */
        // ---- nodes --------------------------------------------------------
        String[] nodeNames = {
                "<KeystoneWithRosace_CorePac2_dsram_load$KeystoneWithRosace_CorePac2_dsram_store>",
                "<KeystoneWithRosace_TeraNet_load>",
                "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>",
                "<KeystoneWithRosace_MSMC_SRAM_Bank0_load$KeystoneWithRosace_MSMC_SRAM_Bank0_store>",
                "<KeystoneWithRosace_TeraNet_store>",
                "<KeystoneWithRosace_AXI_load$KeystoneWithRosace_AXI_store>",
                "<KeystoneWithRosace_MSMC_SRAM_Bank1_load$KeystoneWithRosace_MSMC_SRAM_Bank1_store>",
                "<KeystoneWithRosace_DDR_Bank1_load$KeystoneWithRosace_DDR_Bank1_store>",
                "<KeystoneWithRosace_CorePac3_dsram_load$KeystoneWithRosace_CorePac3_dsram_store>",
                "<KeystoneWithRosace_DDR_Bank0_load$KeystoneWithRosace_DDR_Bank0_store>",
                "<KeystoneWithRosace_SPI_load$KeystoneWithRosace_SPI_store>",
                "<KeystoneWithRosace_CorePac4_dsram_load$KeystoneWithRosace_CorePac4_dsram_store>"
        };
        Map<String, Integer> nodes = new HashMap<>();
        for (int i = 0; i < nodeNames.length; i++) {
            nodes.put(nodeNames[i], i);
        }

        // ---- edges --------------------------------------------------------
        String[][] edges = {
                {"<KeystoneWithRosace_CorePac2_dsram_load$KeystoneWithRosace_CorePac2_dsram_store>", "<KeystoneWithRosace_TeraNet_load>"},
                {"<KeystoneWithRosace_CorePac4_dsram_load$KeystoneWithRosace_CorePac4_dsram_store>", "<KeystoneWithRosace_TeraNet_load>"},
                {"<KeystoneWithRosace_CorePac3_dsram_load$KeystoneWithRosace_CorePac3_dsram_store>", "<KeystoneWithRosace_TeraNet_load>"},
                {"<KeystoneWithRosace_SPI_load$KeystoneWithRosace_SPI_store>", "<KeystoneWithRosace_TeraNet_load>"},
                {"<KeystoneWithRosace_DDR_Bank1_load$KeystoneWithRosace_DDR_Bank1_store>", "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>"},
                {"<KeystoneWithRosace_MSMC_SRAM_Bank1_load$KeystoneWithRosace_MSMC_SRAM_Bank1_store>", "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>"},
                {"<KeystoneWithRosace_AXI_load$KeystoneWithRosace_AXI_store>", "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>"},
                {"<KeystoneWithRosace_MSMC_SRAM_Bank0_load$KeystoneWithRosace_MSMC_SRAM_Bank0_store>", "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>"},
                {"<KeystoneWithRosace_DDR_Bank0_load$KeystoneWithRosace_DDR_Bank0_store>", "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>"},
                {"<KeystoneWithRosace_SPI_load$KeystoneWithRosace_SPI_store>", "<KeystoneWithRosace_TeraNet_store>"},
                {"<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>", "<KeystoneWithRosace_TeraNet_store>"}
        };

        // Upper bound graph (contains all possible nodes/edges)
        UndirectedGraph UB = new UndirectedGraph(model, nodeNames.length, setType, setType, false);
        for (int i = 0; i < nodeNames.length; i++) {
            UB.addNode(i);
        }
        for (String[] e : edges) {
            UB.addEdge(nodes.get(e[0]), nodes.get(e[1]));
        }

        // Lower bound graph (empty graph)
        UndirectedGraph LB = new UndirectedGraph(model, nodeNames.length, setType, setType, false);

        // Graph variable
        UndirectedGraphVar g = model.graphVar("q", LB, UB);

        // ---- node channeling ------------------------------------------------
        Map<String, BoolVar> nodeVars = new HashMap<>();
        for (String n : nodeNames) {
            nodeVars.put(n, model.boolVar(n));
        }
        // ordering must follow the index order used in the graph
        BoolVar[] nodeVarArray = Arrays.stream(nodeNames)
                .sorted(Comparator.comparingInt(nodes::get))
                .map(nodeVars::get)
                .toArray(BoolVar[]::new);
        model.nodesChanneling(g, nodeVarArray).post();

        // ---- edge channeling ------------------------------------------------
        Map<String, BoolVar> edgeVars = new HashMap<>();
        for (String[] e : edges) {
            String key = e[0] + "--" + e[1];
            BoolVar ev = model.boolVar(key);
            edgeVars.put(key, ev);
            model.edgeChanneling(g, ev, nodes.get(e[0]), nodes.get(e[1])).post();
        }

        /* ------------------------------------------------------------------ *
         *  4 –  Association node ↔ transactions (nodeToTr)                    *
         * ------------------------------------------------------------------ */
        Map<String, String[]> nodeToTr = new HashMap<>();
        nodeToTr.put("<KeystoneWithRosace_TeraNet_store>", new String[]{
                "spi_CA", "ioServer_SPI", "spi_CB",
                "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"
        });
        nodeToTr.put("<KeystoneWithRosace_TeraNet_load>", new String[]{
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
                "spi_CA", "ioServer_SPI", "spi_CB",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0"
        });
        nodeToTr.put("<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>", new String[]{
                "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
                "spi_CA",
                "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "spi_CB",
                "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0"
        });
        nodeToTr.put("<KeystoneWithRosace_DDR_Bank1_load$KeystoneWithRosace_DDR_Bank1_store>", new String[]{
                "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0"});
        nodeToTr.put("<KeystoneWithRosace_CorePac3_dsram_load$KeystoneWithRosace_CorePac3_dsram_store>", new String[]{
                "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0"});
        nodeToTr.put("<KeystoneWithRosace_DDR_Bank0_load$KeystoneWithRosace_DDR_Bank0_store>", new String[]{
                "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0"});
        nodeToTr.put("<KeystoneWithRosace_CorePac2_dsram_load$KeystoneWithRosace_CorePac2_dsram_store>", new String[]{
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
                "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0"});
        nodeToTr.put("<KeystoneWithRosace_AXI_load$KeystoneWithRosace_AXI_store>", new String[]{
                "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0"});
        nodeToTr.put("<KeystoneWithRosace_CorePac4_dsram_load$KeystoneWithRosace_CorePac4_dsram_store>", new String[]{
                "ioServer_SPI",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
                "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0"});
        nodeToTr.put("<KeystoneWithRosace_MSMC_SRAM_Bank0_load$KeystoneWithRosace_MSMC_SRAM_Bank0_store>", new String[]{
                "spi_CA",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
                "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0"});
        nodeToTr.put("<KeystoneWithRosace_MSMC_SRAM_Bank1_load$KeystoneWithRosace_MSMC_SRAM_Bank1_store>", new String[]{
                "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
                "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "spi_CB",
                "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
                "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0"});
        nodeToTr.put("<KeystoneWithRosace_SPI_load$KeystoneWithRosace_SPI_store>", new String[]{
                "spi_CA",
                "ioServer_SPI",
                "spi_CB",
                "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0"});

        // ---- transaction → nodes (inverse of nodeToTr) ----------------------
        Map<String, String[]> trToNode = new HashMap<>();
        for (String tr : transactions) {
            List<String> list = new ArrayList<>();
            for (Map.Entry<String, String[]> e : nodeToTr.entrySet()) {
                if (Arrays.asList(e.getValue()).contains(tr)) {
                    list.add(e.getKey());
                }
            }
            trToNode.put(tr, list.toArray(new String[0]));
        }

        /* ------------------------------------------------------------------ *
         *  5 –  Constraints on nodes (C_Node)                               *
         * ------------------------------------------------------------------ */
        for (Map.Entry<String, String[]> e : nodeToTr.entrySet()) {
            String nodeId = e.getKey();
            String[] trList = e.getValue();
            BoolVar[] trBools = Arrays.stream(trList)
                    .map(transactionVar::get)
                    .toArray(BoolVar[]::new);
            // node ↔ (sum of related transactions ≥ 2)
            ReExpression sumGe2 = model.sum("sum", trBools).ge(2);
            nodeVars.get(nodeId).eq(sumGe2).post();
        }

        /* ------------------------------------------------------------------ *
         *  6 –  Constraints on edges (C_Edge)                               *
         * ------------------------------------------------------------------ */
        // compute, for each edge, the set of transactions that use both its ends
        Map<String, BoolVar[]> edgeToTr = new HashMap<>();
        for (String[] e : edges) {
            Set<String> left = new HashSet<>(Arrays.asList(nodeToTr.get(e[0])));
            Set<String> right = new HashSet<>(Arrays.asList(nodeToTr.get(e[1])));
            left.retainAll(right);                 // common transactions
            BoolVar[] vars = left.stream()
                    .map(transactionVar::get)
                    .toArray(BoolVar[]::new);
            edgeToTr.put(e[0] + "--" + e[1], vars);
        }

        for (String[] e : edges) {
            String key = e[0] + "--" + e[1];
            BoolVar ev = edgeVars.get(key);
            BoolVar leftNode = nodeVars.get(e[0]);
            BoolVar rightNode = nodeVars.get(e[1]);
            BoolVar[] commonTr = edgeToTr.get(key);
            ReExpression trOr = model.boolVar(false);
            for (BoolVar tr : commonTr) {
                trOr = trOr.or(tr);
            }
            // edge ↔ (leftNode ∧ rightNode ∧ trOr)
            ReExpression edgeDef = leftNode.and(rightNode, trOr);
            ev.eq(edgeDef).post();
        }

        /* ------------------------------------------------------------------ *
         *  7 –  Connectivity & global constraints                           *
         * ------------------------------------------------------------------ */
        // connectivity
        Constraint isConnected = model.connected(g);
        // non‑empty graph
        ReExpression isNonEmpty = model.boolVar(false);
        for (BoolVar n : nodeVars.values()) {
            isNonEmpty = isNonEmpty.or(n);
        }
        // empty graph
        ReExpression isEmpty = isNonEmpty.not();

        // transaction contribution: a transaction must activate at least one of its nodes
        List<ReExpression> trContrib = new ArrayList<>();
        for (Map.Entry<String, String[]> e : trToNode.entrySet()) {
            BoolVar trVar = transactionVar.get(e.getKey());
            String[] nodesArr = e.getValue();
            if (nodesArr.length == 0) {
                trContrib.add(trVar.eq(model.boolVar(false)));
            } else {
                ReExpression accNode = model.boolVar(false);
                BoolVar[] nodeBools = Arrays.stream(nodesArr)
                        .map(nodeVars::get)
                        .toArray(BoolVar[]::new);
                for (BoolVar n : nodeBools) {
                    accNode = accNode.or(n);
                }
                trContrib.add(trVar.imp(accNode));
            }
        }
        ReExpression trContribCst = trContrib.get(0);
        for (int i = 1; i < trContrib.size(); i++) {
            trContribCst = trContribCst.and(trContrib.get(i));
        }

        // ITF (connected, non‑empty, and transaction contribution satisfied)
        BoolVar isITF = model.boolVar("isITF");
        ReExpression itfDef = isConnected.reify()
                .and(isNonEmpty, trContribCst);
        isITF.eq(itfDef).post();

        // Free (empty graph)
        BoolVar isFree = model.boolVar("isFree");
        isFree.eq(isEmpty).post();

        // exactly one of (Free, ITF) holds
        isFree.xor(isITF).post();

        /* ------------------------------------------------------------------ *
         *  8 –  Solve & enumerate                                           *
         * ------------------------------------------------------------------ */
        Solver solver = model.getSolver();
        int free = 0;
        int itf = 0;

        while (solver.solve()) {
            if(isFree.getValue() == 1)
                free++;
            else
                itf++;
        }
        //System.out.println("free: " + free +" itf: "+ itf);
        Assert.assertEquals(free, nbfree);
        Assert.assertEquals(itf, nbtif);
    }
}