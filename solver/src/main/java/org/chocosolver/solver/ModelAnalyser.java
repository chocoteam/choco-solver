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

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.ESat;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The <code>ModelAnalyser</code> is a class providing methods to analyse the <code>Model</code>.
 * Most especially, it gives tools to analyse the list of <code>Variable</code> and the list of <code>Propagator</code>
 * of the <code>Model</code> depending on their types and class.
 *
 * @author Arthur Godet
 * @see org.chocosolver.solver.Model
 * @see org.chocosolver.solver.variables.Variable
 * @see org.chocosolver.solver.constraints.Constraint
 */
public class ModelAnalyser {
    private final Model model;

    private static final Class[] VARS_TYPES = new Class[]{BoolVar.class, GraphVar.class, IntVar.class, RealVar.class, SetVar.class};
    private final Map<String, Map<String, List<Variable>>> mapTypeClassVars = new HashMap<>();
    private final Map<String, Map<String, List<Propagator>>> mapTypeClassCstrs = new HashMap<>();

    /**
     * Creates a ModelAnalyser for the given <code>Model</code>
     * @param model the Model
     * @see Model
     */
    public ModelAnalyser(Model model) {
        this.model = model;
    }

    /**
     * Get the <code>Model</code> analysed by <code>this</code> ModelAnalyser.
     * @return the model
     * @see Model
     */
    public Model getModel() {
        return model;
    }

    private static String getClassName(Class c) {
        String[] sp = c.toString().split("\\.");
        return sp[sp.length - 1];
    }

    private void retrieveVariableData() {
        mapTypeClassVars.clear();
        for (Class c : VARS_TYPES) {
            Map<String, List<Variable>> typeMap = Arrays.stream(model.getVars())
                    .filter(Objects::nonNull)
                    .filter(c::isInstance)
                    .filter(var -> !c.equals(IntVar.class) || !(var instanceof BoolVar)) // to distinct BoolVar from IntVar
                    .collect(Collectors.groupingBy(var -> getClassName(var.getClass())));
            if (!typeMap.isEmpty()) {
                mapTypeClassVars.put(getClassName(c), typeMap);
            }
        }
    }

    private void retrievePropagatorData() {
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
    }

    private void retrieveModelData() {
        // Retrieve Variable data
        retrieveVariableData();
        // Retrieve Constraint and Propagator data
        retrievePropagatorData();
    }

    /**
     * Analyse the variables of the <code>Model</code> of <code>this</code>
     *
     * @return the <code>VariableTypeStatistics</code> resulting of the variables analysis
     * @see VariableTypeStatistics
     * @see ModelAnalyser#getVariableTypes()
     * @see ModelAnalyser#getVariableClassNamesOfType(String)
     */
    public VariableTypeStatistics[] analyseVariables() {
        retrieveVariableData();
        return getVariableTypes().stream()
                .map(c -> getVariableClassNamesOfType(c).stream().map(varType -> createVariableTypeStatistics(c, varType)).collect(Collectors.toList()))
                .flatMap(List::stream)
                .toArray(VariableTypeStatistics[]::new);
    }

    /**
     * Analyse the propagators of the <code>Model</code> of <code>this</code>
     *
     * @return the <code>ConstraintTypeStatistics</code> resulting of the propagators analysis
     * @see ConstraintTypeStatistics
     * @see ModelAnalyser#getConstraintTypes()
     * @see ModelAnalyser#getConstraintClassNamesOfType(String)
     */
    public ConstraintTypeStatistics[] analyseConstraints() {
        retrievePropagatorData();
        return getConstraintTypes().stream()
                .map(c -> getConstraintClassNamesOfType(c).stream().map(propType -> createConstraintTypeStatistics(c, propType)).collect(Collectors.toList()))
                .flatMap(List::stream)
                .toArray(ConstraintTypeStatistics[]::new);
    }

    /**
     * Analyse the <code>Model</code> of <code>this</code>
     *
     * @return the <code>ModelAnalysis</code> resulting of the model analysis
     */
    public ModelAnalysis analyseModel() {
        VariableTypeStatistics[] varsTypeStats = analyseVariables();
        ConstraintTypeStatistics[] cstrsTypeStats = analyseConstraints();
        return new ModelAnalysis(varsTypeStats, cstrsTypeStats);
    }

    /**
     * Return the list of Variable types present in the <code>Model</code>.
     * Method retrieveVariableData() must have been called prior to this for it to work.
     *
     * @return list of variables types
     * @see ModelAnalyser#VARS_TYPES
     */
    public List<String> getVariableTypes() {
        return mapTypeClassVars.keySet().stream().filter(c -> !mapTypeClassVars.get(c).isEmpty())
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Return the list of Variable classes present in the <code>Model</code> for the given variable type.
     * Method retrieveVariableData() must have been called prior to this for it to work.
     *
     * @param variableType the type of the variables
     * @return list of variables classes
     * @see ModelAnalyser#VARS_TYPES
     */
    public List<String> getVariableClassNamesOfType(String variableType) {
        if (mapTypeClassVars.containsKey(variableType)) {
            return mapTypeClassVars.get(variableType).keySet().stream().sorted().collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<Variable> retrieveVariablesWithProperty(Predicate<Variable> predicate) {
        return Arrays.stream(this.model.getVars())
                .filter(predicate)
                .collect(Collectors.toList());
    }

    private List<Variable> getVariablesWithPropertyOfType(Predicate<Variable> predicate, String type) {
        return this.mapTypeClassVars.computeIfAbsent(type, s -> new HashMap<>())
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Return the list of unconstrained variables in the <code>Model</code>
     *
     * @return the list of unconstrained variables
     */
    public List<Variable> getUnconstrainedVariables() {
        return retrieveVariablesWithProperty(v -> v.getNbProps() == 0);
    }

    /**
     * Return the list of unconstrained variables in the <code>Model</code> of the given type
     * Method retrieveVariableData() must have been called prior to this for it to work.
     *
     * @return the list of unconstrained variables of the given type
     * @see ModelAnalyser#getVariableTypes()
     */
    public List<Variable> getUnconstrainedVariables(String type) {
        return getVariablesWithPropertyOfType(v -> v.getNbProps() == 0, type);
    }

    /**
     * Return the list of variables with at least one view in the <code>Model</code>
     *
     * @return the list of variables with at least one view
     */
    public List<Variable> getVariablesWithViews() {
        return retrieveVariablesWithProperty(v -> v.getNbViews() > 0);
    }

    /**
     * Return the list of variables with at least one view in the <code>Model</code> of the given type
     * Method retrieveVariableData() must have been called prior to this for it to work.
     *
     * @return the list of variables of the given type with at least one view
     * @see ModelAnalyser#getVariableTypes()
     */
    public List<Variable> getVariablesWithViews(String type) {
        return getVariablesWithPropertyOfType(v -> v.getNbViews() > 0, type);
    }

    /**
     * Return the list of Constraint types present in the <code>Model</code>.
     * Method retrievePropagatorData() must have been called prior to this for it to work.
     *
     * @return list of constraints types
     * @see Constraint#getName()
     */
    public List<String> getConstraintTypes() {
        return mapTypeClassCstrs.keySet().stream().filter(c -> !mapTypeClassCstrs.get(c).isEmpty())
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Return the list of Propagator types present in the <code>Model</code> for the given Constraint type.
     * Method retrievePropagatorData() must have been called prior to this for it to work.
     *
     * @param constraintType the type of the Constraint
     * @return list of propagators classes
     * @see Constraint#getName()
     * @see ModelAnalyser#getConstraintTypes()
     */
    public List<String> getConstraintClassNamesOfType(String constraintType) {
        if (mapTypeClassCstrs.containsKey(constraintType)) {
            return mapTypeClassCstrs.get(constraintType).keySet().stream().sorted().collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<Propagator> retrievePropagatorWithProperty(Predicate<Propagator> predicate) {
        return Arrays.stream(this.model.getCstrs())
                .map(Constraint::getPropagators)
                .flatMap(Arrays::stream)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    private List<Propagator> getPropagatorsWithPropertyOfType(Predicate<Propagator> predicate, String type) {
        return this.mapTypeClassCstrs.computeIfAbsent(type, s -> new HashMap<>())
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Return the list of entailed propagators in the <code>Model</code>
     *
     * @return the list of entailed propagators
     */
    public List<Propagator> getEntailedPropagators() {
        return retrievePropagatorWithProperty(p -> p.isEntailed().equals(ESat.TRUE));
    }

    /**
     * Return the list of entailed propagators in the <code>Model</code> of the given type
     * Method retrievePropagatorData() must have been called prior to this for it to work.
     *
     * @return the list of entailed propagators of the given type
     * @see ModelAnalyser#getConstraintTypes()
     */
    public List<Propagator> getEntailedPropagators(String type) {
        return getPropagatorsWithPropertyOfType(p -> p.isEntailed().equals(ESat.TRUE), type);
    }

    /**
     * Return the list of passive propagators in the <code>Model</code>
     *
     * @return the list of passive propagators
     */
    public List<Propagator> getPassivePropagators() {
        return retrievePropagatorWithProperty(Propagator::isPassive);
    }

    /**
     * Return the list of passive propagators in the <code>Model</code> of the given type
     * Method retrievePropagatorData() must have been called prior to this for it to work.
     *
     * @return the list of passive propagators of the given type
     * @see ModelAnalyser#getConstraintTypes()
     */
    public List<Propagator> getPassivePropagators(String type) {
        return getPropagatorsWithPropertyOfType(Propagator::isPassive, type);
    }

    /**
     * Return the list of completely instantiated propagators in the <code>Model</code>
     *
     * @return the list of completely instantiated propagators
     */
    public List<Propagator> getCompletelyInstantiatedPropagators() {
        return retrievePropagatorWithProperty(Propagator::isCompletelyInstantiated);
    }

    /**
     * Return the list of completely instantiated propagators in the <code>Model</code> of the given type
     * Method retrievePropagatorData() must have been called prior to this for it to work.
     *
     * @return the list of completely instantiated propagators of the given type
     * @see ModelAnalyser#getConstraintTypes()
     */
    public List<Propagator> getCompletelyInstantiatedPropagators(String type) {
        return getPropagatorsWithPropertyOfType(Propagator::isCompletelyInstantiated, type);
    }

    /**
     * Return the list of reified propagators in the <code>Model</code>
     *
     * @return the list of reified propagators
     */
    public List<Propagator> getReifiedPropagators() {
        return retrievePropagatorWithProperty(Propagator::isReified);
    }

    /**
     * Return the list of reified propagators in the <code>Model</code> of the given type
     * Method retrievePropagatorData() must have been called prior to this for it to work.
     *
     * @return the list of reified propagators of the given type
     * @see ModelAnalyser#getConstraintTypes()
     */
    public List<Propagator> getReifiedPropagators(String type) {
        return getPropagatorsWithPropertyOfType(Propagator::isReified, type);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////     VariableTypeStatistics     //////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The <code>VariableTypeStatistics</code> is the result of the analysis of variables of a given type and of a
     * given class. It gives several keys on the properties of the variables: whether they are instantiated,
     * constants, their domain's range, etc.
     *
     * @see org.chocosolver.solver.ModelAnalyser
     * @see org.chocosolver.solver.variables.Variable
     */
    public static class VariableTypeStatistics {
        /**
         * The type of the analysed variables
         * @see ModelAnalyser#VARS_TYPES
         */
        public final String varType;
        /**
         * The class' String of the analysed variables
         */
        public final String classVarType;
        public final int nbVariables;
        public final int nbInstantiatedVariables;
        /**
         * Map indicating the number of analysed variables of the type and class by domains' range
         */
        public final LinkedHashMap<String, Integer> byDomainSize;
        /**
         * Map indicating the number of analysed variables of the type and class by number of propagators in which they are in the scope
         */
        public final Map<Integer, Integer> byNbPropagators;
        /**
         * Map indicating the number of analysed variables of the type and class by the number of views pointing to them
         */
        public final Map<Integer, Integer> byNbViews;

        private VariableTypeStatistics(String varType, String classVarType,
                                       int nbVariables, int nbInstantiatedVariables,
                                      LinkedHashMap<String, Integer> byDomainSize, Map<Integer, Integer> byNbPropagators,
                                      Map<Integer, Integer> byNbViews) {
            this.varType = varType;
            this.classVarType = classVarType;
            this.nbVariables = nbVariables;
            this.nbInstantiatedVariables = nbInstantiatedVariables;
            this.byDomainSize = byDomainSize;
            this.byNbPropagators = byNbPropagators;
            this.byNbViews = byNbViews;
        }

        @Override
        public String toString() {
            return toString(false, true, false);
        }

        /**
         * Return a String describing the results of the analysis on the variables of given type and class.
         *
         * @param addInitialTab a boolean indicating whether to add a tabular at the beginning of lines (for pretty printing)
         * @param addVarType a boolean indicating whether to add the type of the analysed variables in the String
         * @param printAllStats a boolean indicating whether to add all the statistics to the String (if false, some criterion won't be added, for example nbInstantiatedVariables if equals to zero)
         * @return a String describing the VariableTypeStatistics
         * @see ModelAnalyser#printVariableAnalysis(PrintStream)
         */
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

        private static VariableTypeStatistics createVariableTypeStatistics(Map<String, Map<String, List<Variable>>> mapTypeClassNbVars, String varType, String classNameOfType) {
            return VariableTypeStatistics.createVariableTypeStatistics(mapTypeClassNbVars, varType, classNameOfType, true);
        }

        private static VariableTypeStatistics createVariableTypeStatistics(Map<String, Map<String, List<Variable>>> mapTypeClassNbVars, String varType, String classNameOfType, boolean showByRange) {
            List<Variable> list;
            if (mapTypeClassNbVars.containsKey(varType) && mapTypeClassNbVars.get(varType).containsKey(classNameOfType)) {
                list = mapTypeClassNbVars.get(varType).get(classNameOfType);
            } else {
                list = new ArrayList<>();
            }
            int nbVariables = list.size();
            int nbInstantiatedVariables = (int) list.stream().filter(Variable::isInstantiated).count();
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
                    varType, classNameOfType,
                    nbVariables, nbInstantiatedVariables,
                    byDomainSize, byNbPropagators, byNbViews
            );
        }
    }

    /**
     * Generate a VariableTypeStatistics object for the given type and class of variables, based on the analysis.
     * Method retrieveVariableData() should have been called prior the call to this method for it to work correctly.
     *
     * @param varType the type of variables to analyse
     * @param classNameOfType the class of variables to analyse
     * @return the statistics on the type and class of variables
     * @see ModelAnalyser#retrieveModelData()
     * @see ModelAnalyser#analyseVariables()
     */
    private VariableTypeStatistics createVariableTypeStatistics(String varType, String classNameOfType) {
        return VariableTypeStatistics.createVariableTypeStatistics(mapTypeClassVars, varType, classNameOfType);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////     ConstraintTypeStatistics     /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The <code>ConstraintTypeStatistics</code> is the result of the analysis of propagators of a given constraint
     * type and of a given class. It gives several keys on the properties of the propagators: whether they are entailed,
     * passive, etc.
     *
     * @see org.chocosolver.solver.ModelAnalyser
     * @see org.chocosolver.solver.constraints.Constraint
     * @see org.chocosolver.solver.constraints.Propagator
     */
    public static class ConstraintTypeStatistics {
        /**
         * The constraint type of the analysed propagators
         * @see Constraint#getName()
         */
        public final String cstrType;
        /**
         * The class of the analysed propagators
         */
        public final String propType;
        public final int nbPropagators;
        public final int nbEntailedPropagators;
        public final int nbPassivePropagators;
        public final int nbCompletelyInstantiatedPropagators;
        public final int nbReifiedPropagators;
        /**
         * Map indicating the number of analysed propagators of the type of constraint  and class by the number of variables in their scope
         */
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

        /**
         * Return a String describing the results of the analysis on the propagators of given constraint type and propagator class.
         *
         * @param addInitialTab a boolean indicating whether to add a tabular at the beginning of lines (for pretty printing)
         * @param addCstrType a boolean indicating whether to add the type of constraint of the analysed propagators in the String
         * @param printAllStats a boolean indicating whether to add all the statistics to the String (if false, some criterion won't be added, for example nbEntailedPropagators if equals to zero)
         * @return a String describing the ConstraintTypeStatistics
         * @see ModelAnalyser#printConstraintAnalysis(PrintStream)
         */
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

    /**
     * Generate a ConstraintTypeStatistics object for the given constraint type and class of propagators, based on the analysis.
     * Method retrievePropagatorData() should have been called prior the call to this method for it to work correctly.
     *
     * @param cstrType the type of constraint of the propagators to analyse
     * @param classNameOfType the class of propagators to analyse
     * @return the statistics on the type of constraint and class of propagators
     * @see ModelAnalyser#retrieveModelData()
     * @see ModelAnalyser#analyseConstraints()
     */
    private ConstraintTypeStatistics createConstraintTypeStatistics(String cstrType, String classNameOfType) {
        return ConstraintTypeStatistics.createConstraintTypeStatistics(mapTypeClassCstrs, cstrType, classNameOfType);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////     ModelAnalysis     ///////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The <code>ModelAnalysis</code> is the result of the analysis of the <code>Model</code>. Analysis are given
     * through VariableTypeStatistics and ConstraintTypeStatistics objects.
     *
     * @see org.chocosolver.solver.ModelAnalyser
     * @see org.chocosolver.solver.ModelAnalyser.VariableTypeStatistics
     * @see org.chocosolver.solver.ModelAnalyser.ConstraintTypeStatistics
     */
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

    /**
     * Print the analysis on variables in System.out.
     *
     * @see ModelAnalyser#printVariableAnalysis(PrintStream)
     * @see ModelAnalyser#printAnalysis()
     */
    public void printVariableAnalysis() {
        printVariableAnalysis(System.out);
    }

    /**
     * Print the analysis on variables in the given <code>PrintStream</code>.
     *
     * @param ps the <code>PrintStream</code> in which to print the variables analysis
     * @see ModelAnalyser#printAnalysis(PrintStream)
     * @see java.io.PrintStream
     */
    public void printVariableAnalysis(PrintStream ps) {
        retrieveVariableData();
        ps.println("################################################");
        ps.println("######### BEGIN OF VARIABLES ANALYSIS ##########");
        ps.println("################################################");
        ps.println();
        for (String c : getVariableTypes()) {
            ps.println(c);
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

    /**
     * Print the analysis on propagators in System.out.
     *
     * @see ModelAnalyser#printConstraintAnalysis(PrintStream)
     * @see ModelAnalyser#printAnalysis()
     */
    public void printConstraintAnalysis() {
        printConstraintAnalysis(System.out);
    }

    /**
     * Print the analysis on propagators in the given <code>PrintStream</code>.
     *
     * @param ps the <code>PrintStream</code> in which to print the propagators analysis
     * @see ModelAnalyser#printAnalysis(PrintStream)
     * @see java.io.PrintStream
     */
    public void printConstraintAnalysis(PrintStream ps) {
        retrievePropagatorData();
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

    /**
     * Print the analysis of the <code>Model</code> in System.out.
     *
     * @see ModelAnalyser#printAnalysis(PrintStream)
     */
    public void printAnalysis() {
        printAnalysis(System.out);
    }

    /**
     * Print the analysis of the <code>Model</code> in the given <code>PrintStream</code>.
     *
     * @param ps the <code>PrintStream</code> in which to print the <code>Model</code> analysis
     * @see java.io.PrintStream
     */
    public void printAnalysis(PrintStream ps) {
        retrieveModelData();
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
