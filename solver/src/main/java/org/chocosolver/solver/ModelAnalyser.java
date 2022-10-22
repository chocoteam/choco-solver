/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.ESat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ModelAnalyser {
    private final Model model;

    private static final Class[] VARS_TYPES = new Class[]{BoolVar.class, GraphVar.class, IntVar.class, RealVar.class, SetVar.class};
    private Map<Class, Map<String, List<Variable>>> mapTypeClassVars = new HashMap<>();
    private Map<String, Map<String, List<Propagator>>> mapTypeClassCstrs = new HashMap<>();

    public ModelAnalyser(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

    private static String getClassName(Class c) {
        String[] sp = c.toString().split("\\.");
        return sp[sp.length - 1];
    }

    public ModelAnalysis analyse() {
        // Variables analysis
        mapTypeClassVars.clear();
        for (Class c : VARS_TYPES) {
            Map<String, List<Variable>> typeMap = Arrays.stream(model.getVars())
                    .filter(Objects::nonNull)
                    .filter(c::isInstance)
                    .filter(var -> !c.equals(IntVar.class) || !(var instanceof BoolVar)) // to distinct BoolVar from IntVar
                    .collect(Collectors.groupingBy(var -> getClassName(var.getClass())));
            if (!typeMap.isEmpty()) {
                mapTypeClassVars.put(c, typeMap);
            }
        }
        // Constraints analysis
        String[] cstrsTypes = Arrays.stream(model.getCstrs()).filter(Objects::nonNull).map(Constraint::getName).distinct().toArray(String[]::new);
        mapTypeClassCstrs.clear();
        for (String cstrName : cstrsTypes) {
            Map<String, List<Propagator>> typeMap = Arrays.stream(model.getCstrs())
                    .filter(Objects::nonNull)
                    .filter(cstr -> cstrName.equals(cstr.getName()))
                    .flatMap(cstr -> Arrays.stream(cstr.getPropagators()))
                    .collect(Collectors.groupingBy(var -> getClassName(var.getClass())));
            if (!typeMap.isEmpty()) {
                mapTypeClassCstrs.put(cstrName, typeMap);
            }
        }
        VariableTypeStatistics[] varsTypeStats = getVariableTypes().stream()
                .map(c -> getVariableClassNamesOfType(c).stream().map(varType -> createVariableTypeStatistics(c, varType)).collect(Collectors.toList()))
                .flatMap(List::stream)
                .toArray(VariableTypeStatistics[]::new);
        ConstraintTypeStatistics[] cstrsTypeStats = getConstraintTypes().stream()
                .map(c -> getConstraintClassNamesOfType(c).stream().map(propType -> createConstraintTypeStatistics(c, propType)).collect(Collectors.toList()))
                .flatMap(List::stream)
                .toArray(ConstraintTypeStatistics[]::new);
        return new ModelAnalysis(varsTypeStats, cstrsTypeStats);
    }

    public List<Class> getVariableTypes() {
        return mapTypeClassVars.keySet().stream().filter(c -> !mapTypeClassVars.get(c).isEmpty())
                .sorted(Comparator.comparing(ModelAnalyser::getClassName))
                .collect(Collectors.toList());
    }

    public List<String> getVariableClassNamesOfType(Class variableType) {
        if (mapTypeClassVars.containsKey(variableType)) {
            return mapTypeClassVars.get(variableType).keySet().stream().sorted().collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public List<String> getConstraintTypes() {
        return mapTypeClassCstrs.keySet().stream().filter(c -> !mapTypeClassCstrs.get(c).isEmpty())
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getConstraintClassNamesOfType(String constraintType) {
        if (mapTypeClassCstrs.containsKey(constraintType)) {
            return mapTypeClassCstrs.get(constraintType).keySet().stream().sorted().collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////     VariableTypeStatistics     //////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class VariableTypeStatistics {
        public final String varType;
        public final String classVarType;
        public final int nbVariables;
        public final int nbInstantiatedVariables;
        public final int nbConstantVariables;
        public final LinkedHashMap<String, Integer> byDomainSize;
        public final Map<Integer, Integer> byNbPropagators;
        public final Map<Integer, Integer> byNbViews;

        private VariableTypeStatistics(String varType, String classVarType,
                                       int nbVariables, int nbInstantiatedVariables, int nbConstantVariables,
                                      LinkedHashMap<String, Integer> byDomainSize, Map<Integer, Integer> byNbPropagators,
                                      Map<Integer, Integer> byNbViews) {
            this.varType = varType;
            this.classVarType = classVarType;
            this.nbVariables = nbVariables;
            this.nbInstantiatedVariables = nbInstantiatedVariables;
            this.nbConstantVariables = nbConstantVariables;
            this.byDomainSize = byDomainSize;
            this.byNbPropagators = byNbPropagators;
            this.byNbViews = byNbViews;
        }

        @Override
        public String toString() {
            return toString(false, true, false);
        }

        public String toString(boolean addInitialTab, boolean addVarType, boolean printAllStats) {
            StringBuilder sb = new StringBuilder(addInitialTab ? "\t" : "");
            if (addVarType) {
                sb.append(varType).append(".");
            }
            sb.append(classVarType).append(" = ").append(nbVariables).append("\n");
            if (printAllStats || nbInstantiatedVariables > 0) {
                sb.append(addInitialTab ? "\t" : "");
                sb.append("\t- Nb instantiated: ").append(nbInstantiatedVariables).append("\n");
            }
            if (printAllStats || nbConstantVariables > 0) {
                sb.append(addInitialTab ? "\t" : "");
                sb.append("\t- Nb constants: ").append(nbConstantVariables).append("\n");
            }
            if (printAllStats || byNbPropagators.containsKey(0)) {
                sb.append(addInitialTab ? "\t" : "");
                int nbUnconstrained = byNbPropagators.getOrDefault(0, 0);
                sb.append("\t- Nb unconstrained: ").append(nbUnconstrained).append("\n");
            }
            sb.append(addInitialTab ? "\t" : "");
            sb.append("\t- By domain size: ").append(prettyObjSizeMap(byDomainSize));
            if (printAllStats || byNbPropagators.keySet().size() != 1 || !byNbPropagators.containsKey(0)) {
                sb.append("\n");
                sb.append(addInitialTab ? "\t" : "");
                sb.append("\t- By number of propagators: ").append(prettyIntSizeMap(byNbPropagators));
            }
            if (printAllStats || byNbViews.keySet().size() != 1 || !byNbViews.containsKey(0)) {
                sb.append("\n");
                sb.append(addInitialTab ? "\t" : "");
                sb.append("\t- By number of views: ").append(prettyIntSizeMap(byNbViews));
            }
            return sb.toString();
        }

        private static VariableTypeStatistics createVariableTypeStatistics(Map<Class, Map<String, List<Variable>>> mapTypeClassNbVars, Class varType, String classNameOfType) {
            return VariableTypeStatistics.createVariableTypeStatistics(mapTypeClassNbVars, varType, classNameOfType, true);
        }

        private static VariableTypeStatistics createVariableTypeStatistics(Map<Class, Map<String, List<Variable>>> mapTypeClassNbVars, Class varType, String classNameOfType, boolean showByRange) {
            List<Variable> list;
            if (mapTypeClassNbVars.containsKey(varType) && mapTypeClassNbVars.get(varType).containsKey(classNameOfType)) {
                list = mapTypeClassNbVars.get(varType).get(classNameOfType);
            } else {
                list = new ArrayList<>();
            }
            int nbVariables = list.size();
            int nbInstantiatedVariables = (int) list.stream().filter(Variable::isInstantiated).count();
            int nbConstantVariables = (int) list.stream().filter(Variable::isAConstant).count();
            LinkedHashMap<String, Integer> byDomainSize = new LinkedHashMap<>();
            Map<Integer, List<Variable>> byDomainIntSize = Collections.unmodifiableMap(
                    new LinkedHashMap<>(list.stream().collect(Collectors.groupingBy(Variable::getDomainSize)))
            );
            if (showByRange) {
                int[] domainRangeLb = new int[]{1, 2, 3, 4, 10, 101, 1001};
                int[] domainRangeUb = new int[]{1, 2, 3, 9, 100, 1000, Integer.MAX_VALUE};
                for (int i = 0; i < domainRangeLb.length; i++) {
                    int rangeLb = domainRangeLb[i];
                    int rangeUb = domainRangeUb[i];
                    String domainRangeName;
                    if (rangeUb == Integer.MAX_VALUE) {
                        domainRangeName = ">" + rangeLb;
                    } else if (rangeLb == rangeUb) {
                        domainRangeName = Integer.toString(rangeLb);
                    } else {
                        domainRangeName = rangeLb + "-" + rangeUb;
                    }
                    List<Variable> vars = byDomainIntSize.entrySet().stream()
                            .filter(entry -> rangeLb <= entry.getKey() && entry.getKey() <= rangeUb)
                            .map(Map.Entry::getValue)
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    if (!vars.isEmpty()) {
                        byDomainSize.put(domainRangeName, vars.size());
                    }
                }
            } else {
                List<Integer> domainSize = byDomainIntSize.keySet().stream().sorted().collect(Collectors.toList());
                for (Integer size : domainSize) {
                    if (!byDomainIntSize.get(size).isEmpty()) {
                        byDomainSize.put(size.toString(), byDomainIntSize.get(size).size());
                    }
                }
            }
            Map<Integer, List<Variable>> byNbPropagatorsList = Collections.unmodifiableMap(
                    new LinkedHashMap<>(list.stream().collect(Collectors.groupingBy(Variable::getNbProps)))
            );
            Map<Integer, Integer> byNbPropagators = new LinkedHashMap<>();
            List<Integer> nbPropsList = byNbPropagatorsList.keySet().stream().sorted().collect(Collectors.toList());
            for (Integer nbProps : nbPropsList) {
                if (!byNbPropagatorsList.get(nbProps).isEmpty()) {
                    byNbPropagators.put(nbProps, byNbPropagatorsList.get(nbProps).size());
                }
            }
            Map<Integer, List<Variable>> byNbViewsList = Collections.unmodifiableMap(
                    new LinkedHashMap<>(list.stream().collect(Collectors.groupingBy(Variable::getNbViews)))
            );
            Map<Integer, Integer> byNbViews = new LinkedHashMap<>();
            List<Integer> nbViewsList = byNbViewsList.keySet().stream().sorted().collect(Collectors.toList());
            for (Integer nbViews : nbViewsList) {
                if (!byNbViewsList.get(nbViews).isEmpty()) {
                    byNbViews.put(nbViews, byNbViewsList.get(nbViews).size());
                }
            }
            return new VariableTypeStatistics(
                    getClassName(varType), classNameOfType,
                    nbVariables, nbInstantiatedVariables, nbConstantVariables,
                    byDomainSize, byNbPropagators, byNbViews
            );
        }
    }

    public VariableTypeStatistics createVariableTypeStatistics(Class varType, String classNameOfType) {
        return VariableTypeStatistics.createVariableTypeStatistics(mapTypeClassVars, varType, classNameOfType);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////     ConstraintTypeStatistics     /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class ConstraintTypeStatistics {
        public final String cstrType;
        public final String propType;
        public final int nbPropagators;
        public final int nbEntailedPropagators;
        public final int nbPassivePropagators;
        public final int nbCompletelyInstantiatedPropagators;
        public final int nbReifiedPropagators;
        public final Map<Integer, Integer> byArity;

        private ConstraintTypeStatistics(String cstrType, String propType,
                int nbPropagators, int nbEntailedPropagators, int nbPassivePropagators, int nbCompletelyInstantiatedPropagators,
                int nbReifiedPropagators, Map<Integer, Integer> byArity) {
            this.cstrType = cstrType;
            this.propType = propType;
            this.nbPropagators = nbPropagators;
            this.nbEntailedPropagators = nbEntailedPropagators;
            this.nbPassivePropagators = nbPassivePropagators;
            this.nbCompletelyInstantiatedPropagators = nbCompletelyInstantiatedPropagators;
            this.nbReifiedPropagators = nbReifiedPropagators;
            this.byArity = byArity;
        }

        @Override
        public String toString() {
            return toString(false, true, false);
        }

        public String toString(boolean addInitialTab, boolean addCstrType, boolean printAllStats) {
            StringBuilder sb = new StringBuilder(addInitialTab ? "\t" : "");
            if (addCstrType) {
                sb.append(cstrType).append(".");
            }
            sb.append(propType).append(" = ").append(nbPropagators).append("\n");
            if (printAllStats || nbEntailedPropagators > 0) {
                sb.append(addInitialTab ? "\t" : "");
                sb.append("\t- Nb entailed: ").append(nbEntailedPropagators).append("\n");
            }
            if (printAllStats || nbPassivePropagators > 0) {
                sb.append(addInitialTab ? "\t" : "");
                sb.append("\t- Nb passive: ").append(nbPassivePropagators).append("\n");
            }
            if (printAllStats || nbCompletelyInstantiatedPropagators > 0) {
                sb.append(addInitialTab ? "\t" : "");
                sb.append("\t- Nb completely instantiated: ").append(nbCompletelyInstantiatedPropagators).append("\n");
            }
            if (printAllStats || nbReifiedPropagators > 0) {
                sb.append(addInitialTab ? "\t" : "");
                sb.append("\t- Nb reified: ").append(nbReifiedPropagators).append("\n");
            }
            sb.append(addInitialTab ? "\t" : "");
            sb.append("\t- By arity: ").append(prettyIntSizeMap(byArity));
            return sb.toString();
        }

        private static ConstraintTypeStatistics createConstraintTypeStatistics(Map<String, Map<String, List<Propagator>>> mapTypeClassNbCstrs, String cstrType, String classNameOfType) {
            List<Propagator> list;
            if (mapTypeClassNbCstrs.containsKey(cstrType) && mapTypeClassNbCstrs.get(cstrType).containsKey(classNameOfType)) {
                list = mapTypeClassNbCstrs.get(cstrType).get(classNameOfType);
            } else {
                list = new ArrayList<>();
            }
            int nbPropagators = list.size();
            int nbEntailedPropagators = (int) list.stream().filter(p -> p.isEntailed().equals(ESat.TRUE)).count();
            int nbPassivePropagators = (int) list.stream().filter(Propagator::isPassive).count();
            int nbCompletelyInstantiatedPropagators = (int) list.stream().filter(Propagator::isCompletelyInstantiated).count();
            int nbReifiedPropagators = (int) list.stream().filter(Propagator::isReified).count();
            Map<Integer, List<Propagator>> byArityList = list.stream().collect(Collectors.groupingBy(Propagator::arity));
            Map<Integer, Integer> byArity = new LinkedHashMap<>();
            List<Integer> arityList = byArityList.keySet().stream().sorted().collect(Collectors.toList());
            for (Integer arity : arityList) {
                if (!byArityList.get(arity).isEmpty()) {
                    byArity.put(arity, byArityList.get(arity).size());
                }
            }
            return new ConstraintTypeStatistics(
                    cstrType, classNameOfType,
                    nbPropagators, nbEntailedPropagators, nbPassivePropagators, nbCompletelyInstantiatedPropagators,
                    nbReifiedPropagators, byArity
            );
        }
    }

    public ConstraintTypeStatistics createConstraintTypeStatistics(String cstrType, String classNameOfType) {
        return ConstraintTypeStatistics.createConstraintTypeStatistics(mapTypeClassCstrs, cstrType, classNameOfType);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////     ModelAnalysis     ///////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class ModelAnalysis {
        public final VariableTypeStatistics[] varsTypeStats;
        public final ConstraintTypeStatistics[] cstrsTypeStats;

        public ModelAnalysis(VariableTypeStatistics[] varsTypeStats, ConstraintTypeStatistics[] cstrsTypeStats) {
            this.varsTypeStats = varsTypeStats;
            this.cstrsTypeStats = cstrsTypeStats;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////     PRINTING     /////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static <V> String prettyIntSizeMap(Map<Integer, Integer> map) {
        StringBuilder sb = new StringBuilder("{");
        List<Integer> sortedArities = map.keySet().stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < sortedArities.size(); i++) {
            sb.append(sortedArities.get(i)).append(": ").append(map.get(sortedArities.get(i)));
            if (i + 1 < sortedArities.size()) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static <K, V> String prettyObjSizeMap(LinkedHashMap<K, Integer> map) {
        StringBuilder sb = new StringBuilder("{");
        ArrayList<K> sortedArities = new ArrayList<>(map.keySet());
        for (int i = 0; i < sortedArities.size(); i++) {
            sb.append(sortedArities.get(i)).append(": ").append(map.get(sortedArities.get(i)));
            if (i + 1 < sortedArities.size()) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public void printVariableAnalysis() {
        printVariableAnalysis(System.out);
    }

    public void printVariableAnalysis(PrintStream ps) {
        ps.println("################################################");
        ps.println("######### BEGIN OF VARIABLES ANALYSIS ##########");
        ps.println("################################################");
        ps.println();
        for (Class c : getVariableTypes()) {
            ps.println(getClassName(c));
            List<String> list = getVariableClassNamesOfType(c);
            for (int i = 0; i < list.size(); i++) {
                String varType = list.get(i);
                VariableTypeStatistics varTypeStats = createVariableTypeStatistics(c, varType);
                ps.println(varTypeStats.toString(true, false, true));
                if (i + 1 < list.size()) {
                    ps.println();
                }
            }
            ps.println();
            ps.println();
        }
        ps.println("################################################");
        ps.println("########## END OF VARIABLES ANALYSIS ###########");
        ps.println("################################################");
    }

    public void printConstraintAnalysis() {
        printConstraintAnalysis(System.out);
    }

    public void printConstraintAnalysis(PrintStream ps) {
        ps.println("################################################");
        ps.println("######### BEGIN OF CONSTRAINTS ANALYSIS ########");
        ps.println("################################################");
        ps.println();
        for (String cstrType : getConstraintTypes()) {
            ps.println(cstrType);
            List<String> list = getConstraintClassNamesOfType(cstrType);
            for (int i = 0; i < list.size(); i++) {
                String propType = list.get(i);
                ConstraintTypeStatistics cstrTypeStats = createConstraintTypeStatistics(cstrType, propType);
                ps.println(cstrTypeStats.toString(true, false, true));
                if (i + 1 < list.size()) {
                    ps.println();
                }
            }
            ps.println();
            ps.println();
        }
        ps.println("################################################");
        ps.println("########## END OF CONSTRAINTS ANALYSIS #########");
        ps.println("################################################");
    }

    public void printAnalysis() {
        printAnalysis(System.out);
    }

    public void printAnalysis(PrintStream ps) {
        ps.println();
        ps.println("################################################################################################");
        ps.println("#################################### BEGIN OF MODEL ANALYSIS ###################################");
        ps.println("################################################################################################");
        ps.println();
        printVariableAnalysis(ps);
        ps.println();
        ps.println();
        printConstraintAnalysis(ps);
        ps.println();
        ps.println("################################################################################################");
        ps.println("##################################### END OF MODEL ANALYSIS ####################################");
        ps.println("################################################################################################");
        ps.println();
    }
}
