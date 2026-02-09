/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.memory.EnvironmentBuilder;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.ISatFactory;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.search.strategy.BlackBoxConfigurator;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.impl.IntVarLazyLit;
import org.chocosolver.util.ESat;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.MapOptionHandler;

import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A Settings builder to define the behavior of the solver and the modeling process.
 * Settings are used in {@link Model} to define the behavior of the solver and the modeling process.
 * Settings are immutable and should be defined before creating the model.
 * This builder allows to create a Settings instance with custom values.
 * It provides default values for all settings, and allows to change them through fluent API.
 *
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 06/02/2026.
 */
public class SettingsBuilder {

    private Predicate<Solver> modelChecker = s -> !ESat.FALSE.equals(s.isSatisfied());

    @Option(name = "--cloneVariableArrayInPropagator",
            aliases = {"--prop.cloneVarArray", "-cvap"},
            usage = "if true, a clone of the input variable array is made in any propagator constructors (default is true). " +
                    "This prevents, for instance, wrong behavior when permutations occurred on the input array (e.g., sorting variables). " +
                    "Setting this to false may limit the memory consumption during modelling but may cause wrong behavior in some cases.")
    private boolean cloneVariableArrayInPropagator = true;

    @Option(name = "--enableViews",
            aliases = {"--model.enableViews", "-ev"},
            usage = "if true, views are enabled. Creates new variables with channeling constraints otherwise (default is true).")
    private boolean enableViews = true;

    @Option(name = "--maxDomSizeForEnumerated",
            aliases = {"--model.maxDomSizeForEnumerated", "-mdsfe"},
            usage = "maximum domain size threshold to force integer variable to be enumerated (default is 65536).")
    private int maxDomSizeForEnumerated = 1 << 16;

    @Option(name = "--minCardForSumDecomposition",
            aliases = {"--model.minCardForSumDecomposition", "-mcfssd"},
            usage = "minimum cardinality threshold to a sum constraint to be decomposed (default is 50).")
    private int minCardForSumDecomposition = 50;

    @Option(name = "--enableTableSubstitution",
            aliases = {"--model.enableTableSubstitution", "-ets"},
            usage = "if true, some intension constraints can be replaced by extension constraints (default is true).")
    private boolean enableTableSubstitution = true;

    @Option(name = "--maxTupleSizeForSubstitution",
            aliases = {"--model.maxTupleSizeForSubstitution", "-mtss"},
            usage = "maximum domain size threshold to replace intension constraints by extension constraints (default is 10000). " +
                    "Only checked when enableTableSubstitution is true.",
            depends = "model.enableTableSubstitution")
    private int maxTupleSizeForSubstitution = 10_000;

    @Option(name = "--maxSizeInMBToUseCompactTable",
            aliases = {"--model.maxSizeInMBToUseCompactTable", "-msmbuct"},
            usage = "maximum estimated size, in MB, of the table to use compact table representation (default is 1024).")
    private long maxSizeInMBToUseCompactTable = 1024L;

    @Option(name = "--sortPropagatorActivationWRTPriority",
            aliases = {"--prop.sortPropagatorActivationWRTPriority", "-pawrp"},
            usage = "if true, propagators are sorted wrt their priority on initial activation. " +
                    "Otherwise, they are activated in the order they have been declared in the model (default is true).")
    private boolean sortPropagatorActivationWRTPriority = true;

    private Consumer<Model> defaultSearch = m -> BlackBoxConfigurator.init().make(m);

    @Option(name = "--warnUser",
            aliases = {"--model.warnUser", "-wu"},
            usage = "if true, warnings detected during modeling/solving are output (default is false).")
    private boolean warnUser = false;

    @Option(name = "--enableDecompositionOfBooleanSum",
            aliases = {"--model.enableDecompositionOfBooleanSum", "-edobs"},
            usage = "if true, boolean sum should be decomposed into an equality constraint and an arithmetic constraint, " +
                    "if false, a single constraint should be used instead (default is false).")
    private boolean enableDecompositionOfBooleanSum = false;

    @Option(name = "--thresholdForIncrementalityOnBoolSum",
            aliases = {"--model.thresholdForIncrementalityOnBoolSum", "-tfibss"},
            usage = "the threshold on the number of variables declared in a boolean sum constraint to choose incremental sum (default is 10).")
    private int thresholdForIncrementalityOnBoolSum = 10;

    @Option(name = "--enableSAT",
            aliases = {"--model.enableSAT", "-esat"},
            usage = "when true, an underlying SAT solver is used to manage clauses declared through ISatFactory, " +
                    "when false, clauses are managed with CSP constraints only (default is false).")
    private boolean enableSAT = false;

    @Option(name = "--swapOnPassivate",
            aliases = {"--prop.swapOnPassivate", "-sop"},
            usage = "when true, on propagator passivation, the propagator is swapped from active to passive in its variables' propagators list. " +
                    "when false, on propagator passivation, only the propagator's state is set to PASSIVE (default is true).")
    private boolean swapOnPassivate = true;

    @Option(name = "--checkDeclaredConstraints",
            aliases = {"--model.checkDeclaredConstraints", "-cdc"},
            usage = "when true, check if all declared constraints are not free anymore, " +
                    "that is either posted or reified, before running the resolution. " +
                    "when false, skip the control (default is true).")
    private boolean checkDeclaredConstraints = true;

    @Option(name = "--checkDeclaredViews",
            aliases = {"--model.checkDeclaredViews", "-cdv"},
            usage = "when true, check if a view already semantically exists before creating it (default is true).")
    private boolean checkDeclaredViews = true;

    @Option(name = "--checkDeclaredMonitors",
            aliases = {"--model.checkDeclaredMonitors", "-cdm"},
            usage = "when true, check if a monitor already semantically exists before creating it (default is true).")
    private boolean checkDeclaredMonitors = true;

    @Option(name = "--printAllUndeclaredConstraints",
            aliases = {"--model.printAllUndeclaredConstraints", "-paudc"},
            usage = "when true, list all undeclared constraint, when false (default value) otherwise. " +
                    "Only active when checkDeclaredConstraints is on (default is false).",
            depends = "checkDeclaredConstraints")
    private boolean printAllUndeclaredConstraints = false;

    @Option(name = "--hybridEngine",
            aliases = {"--prop.hybridEngine", "-he"},
            usage = "when set to '0b00', this works as a constraint-oriented propagation engine; " +
                    "when set to '0b01', this workds as an hybridization between variable and constraint oriented propagation engine; " +
                    "when set to '0b10', this workds as a variable- oriented propagation engine (default is 0b00).")
    private byte hybridEngine = 0b00;

    @Option(name = "--nbMaxLearntClauses",
            aliases = {"--sat.nbMaxLearntClauses", "-nblc"},
            usage = "maximum number of learnt clauses to store. When reached, a reduction is applied (default is 100000).")
    private int nbMaxLearnt = 100_000;

    @Option(name = "--intVarLazyLitWithWeakBounds",
            aliases = {"--sat.intVarLazyLitWithWeakBounds", "-ivllwwb"},
            usage = "when true, the IntVarLazyLit propagator uses weak bounds: when a bound is modified, the channeling is done only with the previous value. " +
                    "It provides smaller reasons, which are faster to compute but weaker in terms of explanation generation. " +
                    "when false, the IntVarLazyLit propagator uses strong bounds: when a bound is modified, the channeling is done with all known values between the previous and the new bound. " +
                    "It provides stronger reasons, which are slower to compute but more informative (default is false).")
    private boolean intVarLazyLitWithWeakBounds = false;

    @Option(name = "--ibexContractionRatio",
            aliases = {"--ibex.contractionRatio", "-icr"},
            usage = "the ratio that real domains must be contracted by ibex to compute the constraint. " +
                    "A contraction is considered as significant when at least ratio of a domain has been reduced. " +
                    "If the contraction is not meet, then it is considered as insufficient and therefore ignored. " +
                    "A too small ratio can degrade the ibex performance (default 0.01.")
    private double ibexContractionRatio = Ibex.RATIO;

    @Option(name = "--ibexRestoreRounding",
            aliases = {"--ibex.restoreRounding", "-irr"},
            usage = "when true, defines that the rounding mode of the current thread is restored after each call to ibex. " +
                    "This is useful to avoid side effects on other code using different rounding modes. " +
                    "However, it can degrade the performance of ibex. (default is true.")
    private boolean ibexRestoreRounding = Ibex.PRESERVE_ROUNDING;

    @Option(name = "-lcg",
            aliases = {"--model.lcg", "--lazyClauseGeneration"},
            usage = "when true, set the solver to be in Lazy Clause Generation mode (in opposition to the full CP mode). " +
                    "This is a shortcut for setting enableSAT to true and relying on the SAT solver to handle clauses management (default is false).")
    private boolean lcg = false;

    private Supplier<IEnvironment> environmentSupplier = () -> new EnvironmentBuilder().fromFlat().build();

    @SuppressWarnings("FieldMayBeFinal") // mutable on purpose, to allow setting values through command line arguments
    @Option(name = "-P",
            handler = MapOptionHandler.class,
            usage = "additional settings that can be set through command line arguments, with the syntax -Dkey=value. " +
                    "These settings are stored in the additionalSettings map and can be retrieved with the get method. " +
                    "This allows to set custom settings that are not defined in this class, but that can be used in the code with the get method.")
    private HashMap<String, String> additionalSettings = new HashMap<>();

    /**
     * Create a new instance of `Settings` which can then be adapted to requirements.
     *
     * @return a Settings with default values
     * @see #dev()
     * @see #prod()
     * @see #fromProperties(Properties)
     * @see #fromArgs(String[])
     */
    public static SettingsBuilder init() {
        return new SettingsBuilder();
    }

    /**
     * Define and returns settings adapted to production environment.
     * Default values are kept for most of the settings, but some checks and warnings are turned off to improve performance.
     * The following settings are turned off or silently ignored in production environment:
     * <ul>
     *     <li>modelChecker</li>
     *     <li>warnUser</li>
     *     <li>checkDeclaredConstraints</li>
     *     <li>printAllUndeclaredConstraints</li>
     * </ul>
     *
     * @return a settings adapted to production environment.
     */
    public static SettingsBuilder prod() {
        return SettingsBuilder.init()
                .setModelChecker(s -> true)
                .setWarnUser(false)
                .setCheckDeclaredConstraints(false)
                .setPrintAllUndeclaredConstraints(false);
    }

    /**
     * Define and returns settings adapted to development environment.
     * Default values are kept for most of the settings, but some checks and warnings are turned on to help developers to detect potential issues in their model.
     * The following settings are turned on in development environment:
     * <ul>
     *     <li>modelChecker</li>
     *     <li>warnUser</li>
     *     <li>printAllUndeclaredConstraints</li>
     * </ul>
     *
     * @return a settings adapted to development environment.
     */
    public static SettingsBuilder dev() {
        return SettingsBuilder.init()
                .setModelChecker(s -> !ESat.FALSE.equals(s.isSatisfied()))
                .setWarnUser(true)
                .setPrintAllUndeclaredConstraints(true);
    }

    /**
     * Load settings from a properties file.
     * Properties with a corresponding field in `SettingsBuilder` are used to set the value of that field.
     * Other properties are stored in the `additionalSettings` map.
     * If a property has already been set with a specific setter, the value from the properties file will override it.
     * The following properties can not be set through the properties file and will be ignored if provided:
     * <ul>
     *     <li>modelChecker</li>
     *     <li>environmentSupplier</li>
     *     <li>defaultSearch</li>
     *  </ul>
     *
     * @param properties the properties to create the `Settings` from
     * @return the current instance of `SettingsBuilder` with fields set according to the provided properties
     */
    public SettingsBuilder fromProperties(Properties properties) {
        properties.forEach((k, v) -> {
            String key = k.toString();
            String value = v.toString();
            switch (key) {
                case "modelChecker":
                case "environmentSupplier":
                case "defaultSearch":
                    // not supported
                    break;
                case "cloneVariableArrayInPropagator":
                    this.setCloneVariableArrayInPropagator(Boolean.parseBoolean(value));
                    break;
                case "enableViews":
                    this.setEnableViews(Boolean.parseBoolean(value));
                    break;
                case "maxDomSizeForEnumerated":
                    this.setMaxDomSizeForEnumerated(Integer.parseInt(value));
                    break;
                case "minCardForSumDecomposition":
                    this.setMinCardinalityForSumDecomposition(Integer.parseInt(value));
                    break;
                case "enableTableSubstitution":
                    this.setEnableTableSubstitution(Boolean.parseBoolean(value));
                    break;
                case "maxTupleSizeForSubstitution":
                    this.setMaxTupleSizeForSubstitution(Integer.parseInt(value));
                    break;
                case "maxSizeInMBToUseCompactTable":
                    this.setMaxSizeInMBToUseCompactTable(Integer.parseInt(value));
                    break;
                case "sortPropagatorActivationWRTPriority":
                    this.setSortPropagatorActivationWRTPriority(Boolean.parseBoolean(value));
                    break;
                case "warnUser":
                    this.setWarnUser(Boolean.parseBoolean(value));
                    break;
                case "enableDecompositionOfBooleanSum":
                    this.setEnableDecompositionOfBooleanSum(Boolean.parseBoolean(value));
                    break;
                case "thresholdForIncrementalityOnBoolSum":
                    this.setThresholdForIncrementalityOnBoolSum(Integer.parseInt(value));
                    break;
                case "enableSAT":
                    this.setEnableSAT(Boolean.parseBoolean(value));
                    break;
                case "swapOnPassivate":
                    this.setSwapOnPassivate(Boolean.parseBoolean(value));
                    break;
                case "checkDeclaredConstraints":
                    this.setCheckDeclaredConstraints(Boolean.parseBoolean(value));
                    break;
                case "checkDeclaredViews":
                    this.setCheckDeclaredViews(Boolean.parseBoolean(value));
                    break;
                case "checkDeclaredMonitors":
                    this.setCheckDeclaredMonitors(Boolean.parseBoolean(value));
                    break;
                case "printAllUndeclaredConstraints":
                    this.setPrintAllUndeclaredConstraints(Boolean.parseBoolean(value));
                    break;
                case "hybridEngine":
                    this.setHybridizationOfPropagationEngine(Byte.parseByte(value));
                    break;
                case "nbMaxLearntClauses":
                    this.setNbMaxLearntClauses(Integer.parseInt(value));
                    break;
                case "intVarLazyLitWithWeakBounds":
                    this.setIntVarLazyLitWithWeakBounds(Boolean.parseBoolean(value));
                    break;
                case "ibexContractionRatio":
                    this.ibexContractionRatio = Double.parseDouble(value);
                    break;
                case "ibexRestoreRounding":
                    this.ibexRestoreRounding = Boolean.parseBoolean(value);
                    break;
                case "lcg":
                    this.setLCG(Boolean.parseBoolean(value));
                    break;
                default:
                    this.set(key, value);
            }
        });
        return this;
    }

    /**
     * Load settings from command line arguments.
     * A command line parser is used to parse the arguments and set the corresponding fields in `SettingsBuilder`.
     * If a property has already been set with a specific setter, the value from the properties file will override it.
     * The following properties can not be set through the properties file and will be ignored if provided:
     * <ul>
     *     <li>modelChecker</li>
     *     <li>environmentSupplier</li>
     *     <li>defaultSearch</li>
     *  </ul>
     *
     * @param args the command line arguments to load the settings from
     * @return the current instance of `SettingsBuilder` with fields set according to the provided command line arguments
     * @see org.kohsuke.args4j.CmdLineParser
     */
    public SettingsBuilder fromArgs(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (org.kohsuke.args4j.CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }
        return this;
    }

    /**
     * Create a new instance of `Settings` which can then be adapted to requirements.
     *
     * @return a Settings with default values
     * @see #dev()
     * @see #prod()
     */
    public Settings build() {
        return new Settings(this);
    }

    /**
     * Get the predicate to check the solution of the model before running the resolution.
     * The default value is a predicate that checks if the model is not unsatisfiable.
     *
     * @param modelChecker the predicate to check the solution of the model before running the resolution.
     * @return the current instance
     */
    public SettingsBuilder setModelChecker(Predicate<Solver> modelChecker) {
        this.modelChecker = modelChecker;
        return this;
    }

    /**
     * Get the predicate to check the solution of the model before running the resolution.
     * The default value is a predicate that checks if the model is not unsatisfiable.
     *
     * @return the current instance
     */
    public Predicate<Solver> getModelChecker() {
        return this.modelChecker;
    }

    /**
     * Set the environment to be used
     *
     * @param environmentSupplier provide an environment
     * @return the current instance
     */
    public SettingsBuilder setEnvironmentSupplier(Supplier<IEnvironment> environmentSupplier) {
        this.environmentSupplier = environmentSupplier;
        return this;
    }

    /**
     * @return the environment builder
     */
    public Supplier<IEnvironment> getEnvironmentSupplier() {
        return environmentSupplier;
    }

    /**
     * @return true if all propagators should clone the input variable array instead of simply referencing it.
     */
    public boolean cloneVariableArrayInPropagator() {
        return cloneVariableArrayInPropagator;
    }

    /**
     * If this setting is set to true (default value), a clone of the input variable array is made in any propagator constructors.
     * This prevents, for instance, wrong behavior when permutations occurred on the input array (e.g., sorting variables).
     * Setting this to false may limit the memory consumption during modelling.
     *
     * @param cloneVariableArrayInPropagator {@code true} to clone variables array on constructor
     * @return the current instance
     */
    public SettingsBuilder setCloneVariableArrayInPropagator(boolean cloneVariableArrayInPropagator) {
        this.cloneVariableArrayInPropagator = cloneVariableArrayInPropagator;
        return this;
    }

    /**
     * @return <tt>true</tt> if views are enabled.
     */
    public boolean enableViews() {
        return enableViews;
    }

    /**
     * Set to 'true' to allow the creation of views in the {@link Model}.
     * Creates new variables with channeling constraints otherwise.
     *
     * @param enableViews {@code true} to enable views
     * @return the current instance
     */
    public SettingsBuilder setEnableViews(boolean enableViews) {
        this.enableViews = enableViews;
        return this;
    }

    /**
     * @return maximum domain size threshold to force integer variable to be enumerated
     */
    public int getMaxDomSizeForEnumerated() {
        return maxDomSizeForEnumerated;
    }

    /**
     * Define the minimum number of cardinality threshold to a sum/scalar constraint to be decomposed in intermediate
     * sub-sums.
     *
     * @param maxDomSizeForEnumerated cardinality threshold
     * @return the current instance
     */
    public SettingsBuilder setMaxDomSizeForEnumerated(int maxDomSizeForEnumerated) {
        this.maxDomSizeForEnumerated = maxDomSizeForEnumerated;
        return this;
    }

    /**
     * @return minimum number of cardinality threshold to a sum constraint to be decomposed
     */
    public int getMinCardForSumDecomposition() {
        return minCardForSumDecomposition;
    }

    /**
     * Define the default minimum number of cardinality threshold to a sum/scalar constraint to be
     * decomposed into intermediate sub-sums.
     *
     * @param defaultMinCardinalityForSumDecomposition cardinality threshold
     * @return the current instance
     */
    public SettingsBuilder setMinCardinalityForSumDecomposition(int defaultMinCardinalityForSumDecomposition) {
        this.minCardForSumDecomposition = defaultMinCardinalityForSumDecomposition;
        return this;
    }

    /**
     * @return <tt>true</tt> if some intension constraints can be replaced by extension constraints
     */
    public boolean enableTableSubstitution() {
        return enableTableSubstitution;
    }

    /**
     * Define whether some intension constraints are replaced by extension constraints
     *
     * @param enableTableSubstitution enable table substitution
     * @return the current instance
     */
    public SettingsBuilder setEnableTableSubstitution(boolean enableTableSubstitution) {
        this.enableTableSubstitution = enableTableSubstitution;
        return this;
    }

    /**
     * @return maximum domain size threshold to replace intension constraints by extension constraints
     */
    public int getMaxTupleSizeForSubstitution() {
        return maxTupleSizeForSubstitution;
    }

    /**
     * Define the maximum domain size threshold to replace intension constraints by extension constraints
     * Only checked when {@link #enableTableSubstitution()} returns {@code true}
     *
     * @param maxTupleSizeForSubstitution threshold to substitute intension constraint by table one.
     * @return the current instance
     */
    public SettingsBuilder setMaxTupleSizeForSubstitution(int maxTupleSizeForSubstitution) {
        this.maxTupleSizeForSubstitution = maxTupleSizeForSubstitution;
        return this;
    }

    /**
     * @return maximum estimated size, in MB, of the table to use compact table representation
     */
    public long getMaxSizeInMBToUseCompactTable() {
        return maxSizeInMBToUseCompactTable;
    }

    /**
     * Define the maximum estimated size, in MB, of the table to use compact table representation.
     *
     * @param maxSizeInMBToUseCompactTable size threshold (in MB) to use compact table representation
     * @return the current instance
     */
    public SettingsBuilder setMaxSizeInMBToUseCompactTable(int maxSizeInMBToUseCompactTable) {
        this.maxSizeInMBToUseCompactTable = maxSizeInMBToUseCompactTable;
        return this;
    }

    /**
     * @return {@code true} if propagators are sorted wrt their priority on initial activation.
     */
    public boolean sortPropagatorActivationWRTPriority() {
        return sortPropagatorActivationWRTPriority;
    }

    /**
     * Set whether propagators are sorted wrt their priority in {@link org.chocosolver.solver.propagation.PropagationEngine} when
     * dealing with propagator activation.
     *
     * @param sortPropagatorActivationWRTPriority {@code true} to allow sorting static propagators.
     * @return the current instance
     */
    public SettingsBuilder setSortPropagatorActivationWRTPriority(boolean sortPropagatorActivationWRTPriority) {
        this.sortPropagatorActivationWRTPriority = sortPropagatorActivationWRTPriority;
        return this;
    }

    /**
     * Set default search strategy for the input model
     *
     * @see Search#defaultSearch(Model)
     */
    public Consumer<Model> getDefaultSearch() {
        return this.defaultSearch;
    }

    /**
     * Define a default search strategy for the input model
     *
     * @param defaultSearch what default search strategy should be
     * @return the current instance
     */
    public SettingsBuilder setDefaultSearch(Consumer<Model> defaultSearch) {
        this.defaultSearch = defaultSearch;
        return this;
    }

    /**
     * @return <tt>true</tt> if warnings detected during modeling/solving are output.
     */
    public boolean warnUser() {
        return warnUser;
    }

    /**
     * To be informed of warnings detected during modeling/solving
     *
     * @param warnUser {@code true} to be print warnings on console
     * @return the current instance
     */
    public SettingsBuilder setWarnUser(boolean warnUser) {
        this.warnUser = warnUser;
        return this;
    }

    /**
     * @return {@code true} if boolean sum should be decomposed into an equality constraint and an arithmetic constraint,
     * {@code false}if a single constraint should be used instead.
     */
    public boolean enableDecompositionOfBooleanSum() {
        return enableDecompositionOfBooleanSum;
    }

    /**
     * Define if boolean sums should be decomposed into an equality constraint + arithmetic constraint
     *
     * @param enableDecompositionOfBooleanSum {@code true} to enable decomposition
     * @return the current instance
     */
    public SettingsBuilder setEnableDecompositionOfBooleanSum(boolean enableDecompositionOfBooleanSum) {
        this.enableDecompositionOfBooleanSum = enableDecompositionOfBooleanSum;
        return this;
    }

    /**
     * @return the threshold on the number of variables declared in a boolean sum constraint to choose incremental sum (default is 10).
     */
    public int thresholdForIncrementalityOnBoolSum() {
        return this.thresholdForIncrementalityOnBoolSum;
    }

    /**
     * Define the threshold on the number of variables declared in a boolean sum constraint to choose incremental sum (default is 10).
     *
     * @param thresholdForIncrementalityOnBoolSum threshold on the number of variables declared in a boolean sum constraint to choose incremental sum
     * @return the current instance
     */
    public SettingsBuilder setThresholdForIncrementalityOnBoolSum(int thresholdForIncrementalityOnBoolSum) {
        this.thresholdForIncrementalityOnBoolSum = thresholdForIncrementalityOnBoolSum;
        return this;
    }

    /**
     * @return <i>true</i> when an underlying SAT solver is used to manage clauses declared through {@link ISatFactory},
     * <i>false</i> when clauses are managed with CSP constraints only.
     */
    public boolean enableSAT() {
        return enableSAT;
    }

    /**
     * Indicate if clauses are managed by a unique SAT solver.
     *
     * @param enableSAT {@code true} to rely on SAT Solver to handle clauses
     * @return the current instance
     */
    public SettingsBuilder setEnableSAT(boolean enableSAT) {
        this.enableSAT = enableSAT;
        return this;
    }

    /**
     * @return <i>true</i> if, on propagator passivation, the propagator is swapped from active to passive in its variables' propagators list.
     * <i>false</i> if, on propagator passivation, only the propagator's state is set to PASSIVE.
     */
    public boolean swapOnPassivate() {
        return swapOnPassivate;
    }

    /**
     * Define if passivation of propagator swap it in variables' list
     *
     * @param swapOnPassivate {@code true} to enable swapping
     * @return the current instance
     */
    public SettingsBuilder setSwapOnPassivate(boolean swapOnPassivate) {
        this.swapOnPassivate = swapOnPassivate;
        return this;
    }

    /**
     * @return <i>true</i> (default value) to check if all declared constraints are not free anymore,
     * that is either posted or reified, before running the resolution.
     * <i>false</i> to skip the control.
     */
    public boolean checkDeclaredConstraints() {
        return checkDeclaredConstraints;
    }

    /**
     * Indicate if the declared constraints are either posted or reified.
     *
     * @param checkDeclaredConstraints {@code true} to check constraints before resolution
     * @return the current instance
     */
    public SettingsBuilder setCheckDeclaredConstraints(boolean checkDeclaredConstraints) {
        this.checkDeclaredConstraints = checkDeclaredConstraints;
        return this;
    }

    /**
     * @return <i>true</i> to list all undeclared constraint, <i>false</i> (default value) otherwise.
     * Only active when {@link #checkDeclaredConstraints()} is on.
     */
    public boolean printAllUndeclaredConstraints() {
        return printAllUndeclaredConstraints;
    }

    /**
     * Indicate if all undeclared constraints are listed on console when {@link #checkDeclaredConstraints()} is on.
     *
     * @param printAllUndeclaredConstraints {@code true} to list all undeclared constraints
     * @return the current instance
     */
    public SettingsBuilder setPrintAllUndeclaredConstraints(boolean printAllUndeclaredConstraints) {
        this.printAllUndeclaredConstraints = printAllUndeclaredConstraints;
        return this;
    }

    /**
     * @return <i>true</i> (default value) to check prior to creation
     * if a view already semantically exists.
     */
    public boolean checkDeclaredViews() {
        return checkDeclaredViews;
    }

    /**
     * Check if a view already semantically exists before creating it.
     *
     * @param checkDeclaredViews {@code true} to check views before creation
     * @return the current instance
     */
    public SettingsBuilder setCheckDeclaredViews(boolean checkDeclaredViews) {
        this.checkDeclaredViews = checkDeclaredViews;
        return this;
    }

    public SettingsBuilder setCheckDeclaredMonitors(boolean check) {
        this.checkDeclaredMonitors = check;
        return this;
    }

    public boolean checkDeclaredMonitors() {
        return this.checkDeclaredMonitors;
    }

    /**
     * @return <i>0b00<i/> if constraint-oriented propagation engine,
     * <i>0b01<i/> if hybridization between variable and constraint oriented and
     * <i>0b10<i/> if variable-oriented.
     */
    public byte enableHybridizationOfPropagationEngine() {
        return hybridEngine;
    }

    /**
     * Define behavior of the propagation engine.
     *
     * @param hybrid When set to '0b00', this works as a constraint-oriented propagation engine;
     *               when set to '0b01', this workds as an hybridization between variable and constraint oriented
     *               propagation engine.
     *               when set to '0b10', this workds as a variable- oriented propagation engine.
     * @return the current instance
     */
    public SettingsBuilder setHybridizationOfPropagationEngine(byte hybrid) {
        this.hybridEngine = hybrid;
        return this;
    }

    /**
     * Set the solver to be in Lazy Clause Generation mode (in opposition to the full CP mode).
     *
     * @param isLCG true to set the solver in LCG mode
     * @return the current instance
     */
    public SettingsBuilder setLCG(boolean isLCG) {
        this.lcg = isLCG;
        this.setEnableSAT(lcg || enableSAT);
        return this;
    }

    /**
     * @return true if the solver is in Lazy Clause Generation mode (in opposition to the full CP mode).
     */
    public boolean isLCG() {
        return this.lcg;
    }

    /**
     * @return maximum number of learnt clauses to store. When reached, a reduction is applied.
     * @see #setNbMaxLearntClauses(int)
     */
    public int getNbMaxLearntClauses() {
        return nbMaxLearnt;
    }

    /**
     * Set the maximum of number of learnt clauses to store before running a reduction of the store.
     *
     * @param n maximum number of learnt clauses before reducing the store.
     * @return the current instance
     * @see #getNbMaxLearntClauses()
     */
    public SettingsBuilder setNbMaxLearntClauses(int n) {
        this.nbMaxLearnt = n;
        return this;
    }

    /**
     * @return <tt>true</tt> if the {@link IntVarLazyLit} propagator uses weak bounds.
     * @see #setIntVarLazyLitWithWeakBounds(boolean)
     */
    public boolean intVarLazyLitWithWeakBounds() {
        return intVarLazyLitWithWeakBounds;
    }

    /**
     * Set to <tt>true</tt> to use a weak chaining:
     * when a bound is modified, the channeling is done only with the previous value.
     * It provides smaller reasons, which are faster to compute but weaker in terms of explanation generation.
     * <p>
     * Set to <tt>false</tt> to use a strong chaining:
     * when a bound is modified, the channeling is done with all known values between the previous and the new bound.
     * It provides stronger reasons, which are slower to compute but more informative.
     *
     * @param intVarLazyLitWithWeakBounds weak chaining or not
     * @return the current instance
     */
    public SettingsBuilder setIntVarLazyLitWithWeakBounds(boolean intVarLazyLitWithWeakBounds) {
        this.intVarLazyLitWithWeakBounds = intVarLazyLitWithWeakBounds;
        return this;
    }

    /**
     * @return the ratio that a domains must be contracted by ibex to compute the constraint.
     */
    public double getIbexContractionRatio() {
        return ibexContractionRatio;
    }

    /**
     * Defines the ratio that real domains must be contracted by ibex
     * to compute the constraint. A contraction is considered as significant
     * when at least {@param ratio} of a domain has been reduced.
     * If the contraction is not meet, then it is considered as insufficient
     * and therefore ignored. A too small ratio can degrade the ibex performance.
     * The default value is 1% (0.01). See issue #653.
     * <p>
     * Example: given x = [0.0, 100.0], y = [0.5,0.5] and CSTR(x > y)
     * - When the ratio is 1% (0.01) bounds of X are kept as [0.0, 100.0]
     * because it's contraction is less than 1%.
     * - When the ratio is 0.1% (0.001) bounds of X are update to [0.5, 100.0]
     * because it's contraction is greater than 0.1%.
     *
     * @param ibexContractionRatio defines the ratio that a domains must be
     *                             contract to compute the constraint.
     * @implNote Supported since ibex-java version 1.2.0
     */
    public SettingsBuilder setIbexContractionRatio(double ibexContractionRatio) {
        this.ibexContractionRatio = ibexContractionRatio;
        return this;
    }

    /**
     * If preserve_rounding is true, Ibex will restore the default
     * Java rounding method when coming back from Ibex, which is
     * transparent for Java but causes a little loss of efficiency.
     * To improve the running time, ibex changes the rounding system
     * for double values during contraction. In Linux/MACOS environments
     * it leads to different results in calculations like `Math.pow(10, 6)`.
     * See issue #740.
     *
     * @param ibexRestoreRounding either Java or ibex rounding method
     * @implNote Supported since ibex-java version 1.2.0
     */
    public SettingsBuilder setIbexRestoreRounding(boolean ibexRestoreRounding) {
        this.ibexRestoreRounding = ibexRestoreRounding;
        return this;
    }

    /**
     * @return if ibex must restore java rounding mode when returning a call.
     */
    public boolean getIbexRestoreRounding() {
        return ibexRestoreRounding;
    }

    /**
     * Get an additional setting that can be used to store any custom setting not already defined in this class.
     *
     * @param key the key of the setting
     * @return an optional containing the value of the setting if it exists, an empty optional otherwise
     */
    public Optional<String> get(String key) {
        return Optional.of(additionalSettings.get(key));
    }

    /**
     * Set an additional setting that can be used to store any custom setting not already defined in this class.
     *
     * @param key   the key of the setting
     * @param value the value of the setting
     * @return the current instance
     */
    public SettingsBuilder set(String key, String value) {
        this.additionalSettings.put(key, value);
        return this;
    }

    /**
     * @return a map of additional settings that can be used to store any custom setting not already defined in this class.
     */
    public HashMap<String, String> getAdditionalSettings() {
        return this.additionalSettings;
    }
}
