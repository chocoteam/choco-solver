/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/09/2022
 */
public class Semafor {

    public static void main(String[] args) {
        new Semafor();
    }

    public static final int NB_MAX_SRVS = 3;
    public static final int NB_MAX_VMS = 3;
    public static final int NB_MAX_CPUCORE = 24;
    public static final int NB_MAX_RAM = 64; // in Go

    private static class Server {
        protected static int ID = 0;
        int uuid = ID++;
        String name;
        IntVar cpuCore;
        IntVar ram;

        SetVar hostingVMS;

        public Server(String name, Model model) {
            this.name = name;
            cpuCore = model.intVar(name + "_cpuCore", 0, NB_MAX_CPUCORE);
            ram = model.intVar(name + "_ram", 0, NB_MAX_RAM);
            // could be refined if potential VM were known at creation
            hostingVMS = model.setVar(name + "_hosts", new int[]{}, IntStream.range(0, NB_MAX_VMS).toArray()); // 4 VMs max
        }

        public void implicitConstraints(Model model, VM... vms) {
            IntVar[] usedCPU = model.intVarArray(NB_MAX_VMS, 0, 0);
            IntVar[] usedRAM = model.intVarArray(NB_MAX_VMS, 0, 0);
            BoolVar[] hostedBy = IntStream.range(0, NB_MAX_VMS).mapToObj(i -> model.boolVar(false)).toArray(BoolVar[]::new);
            for (VM vm : vms) {
                hostedBy[vm.uuid] = vm.hostedBy.eq(uuid).boolVar();
                usedCPU[vm.uuid] = hostedBy[vm.uuid].mul(vm.cpuCore).intVar();
                usedRAM[vm.uuid] = hostedBy[vm.uuid].mul(vm.ram).intVar();
            }
            model.sum(usedCPU, "<=", cpuCore).post();
            model.sum(usedRAM, "<=", ram).post();
            model.setBoolsChanneling(hostedBy, hostingVMS).post();
        }

    }

    private static class VM {
        protected static int ID = 0;
        int uuid = ID++;
        String name;
        IntVar cpuCore;
        IntVar ram;
        IntVar hostedBy;

        public VM(String name, Model model) {
            this.name = name;
            cpuCore = model.intVar(name + "_cpuCore", 0, NB_MAX_CPUCORE);
            ram = model.intVar(name + "_ram", 0, NB_MAX_RAM);
            // could be refined if potential SRV were known at creation
            hostedBy = model.intVar(name + "_hostedBy", 0, NB_MAX_SRVS);
        }

        public void mustBeHosted(Model model, Server... srvs) {
            model.member(hostedBy, Arrays.stream(srvs).mapToInt(v -> v.uuid).toArray()).post();
        }

        public void minCpuRequirement(Model model, int threshold, Server... srvs) {
            IntVar[] cpus = model.intVarArray(NB_MAX_SRVS, 0, 0);
            for (Server srv : srvs) {
                cpus[srv.uuid] = srv.cpuCore;
            }
            IntVar minCpuCore = model.intVar(name + "_req_cpuCore", threshold, NB_MAX_CPUCORE);
            model.element(minCpuCore, cpus, hostedBy, 0).post();
        }
    }


    public Semafor() {
        Model model = new Model("SeMaFoR");
        // Servers creation
        Server srv1 = new Server("SRV1", model);
        Server srv2 = new Server("SRV2", model);
        Server[] srvs = new Server[]{srv1, srv2};
        // VMs creation
        VM vm1 = new VM("VM1", model); // no replica
        VM vm2 = new VM("VM2", model); // no replica
        VM[] vms = new VM[]{vm1, vm2};

        // spec of server 1
        srv1.cpuCore.eq(9).post();
        srv1.ram.eq(4).post();
        srv1.implicitConstraints(model, vm1, vm2);

        // spec of server 2
        srv2.cpuCore.eq(2).post();
        srv2.ram.eq(2).post();
        srv2.implicitConstraints(model, vm1, vm2);

        // spec of VM1
        vm1.cpuCore.ge(3).post();
        vm1.ram.eq(1).post();
        vm1.mustBeHosted(model, srv1, srv2);
        // host.cpuCore > 5
        vm1.minCpuRequirement(model, 5, srv1, srv2);

        // spec of VM2
        vm2.cpuCore.eq(2).post();
        vm2.ram.eq(2).post();
        vm2.mustBeHosted(model, srv1, srv2);

        Solver solver = model.getSolver();
        //solver.limitSolution(3);
        while (solver.solve()) {
            System.out.printf("## SOLUTION %03d ###########\n", solver.getSolutionCount());
            for (Server s : srvs) {
                System.out.printf("Server %s :\n- cpu : %d\n- ram : %dGo\nIs hosting: ",
                        s.name, s.cpuCore.getValue(), s.ram.getValue());
                for (int v : s.hostingVMS.getValue()) {
                    System.out.printf("%s, ", vms[v].name);
                }
                System.out.print("\n");
            }
            System.out.println("=====");
            for (VM vm : vms) {
                int s = 0;
                System.out.printf("VM %s :\n- cpu : %d\n- ram : %dGo\nHosted by: %s\n",
                        vm.name, vm.cpuCore.getValue(), vm.ram.getValue(),
                        (s = vm.hostedBy.getValue()) < srvs.length ?
                                srvs[s].name : "--");
            }
            System.out.println();
        }
        if (solver.getSolutionCount() == 0) {
            System.out.println("No solution !!");
        }

    }
}
