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

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.ISatFactory;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.impl.IntVarLazyLit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This class gathers all settings that can be used to configure a {@link Model} and its associated {@link Solver}.
 * <br/>
 * Some settings are used to control the behavior of the model and its solver, while others are used to check the model before solving it.
 * <br/>
 * This class is immutable, and can be built using the {@link SettingsBuilder} class.
 *
 * @author Charles Prud'homme
 * @since 14/12/2017.
 */
public class Settings {

    private final Predicate<Solver> modelChecker;

    private final boolean cloneVariableArrayInPropagator;

    private final boolean enableViews;

    private final int enumeratedDomainSizeThreshold;

    private final int minCardForSumDecomposition;

    private final boolean enableTableSubstitution;

    private final int maxTupleSizeForSubstitution;

    private final long maxSizeInMBToUseCompactTable;

    private final boolean sortPropagatorActivationWRTPriority;

    private final Consumer<Model> defaultSearch;

    private final boolean warnUser;

    private final boolean enableDecompositionOfBooleanSum;

    private final int incrementalityOnBoolSumThreshold;

    private final boolean enableSAT;

    private final boolean swapOnPassivate;

    private final boolean checkDeclaredConstraints;

    private final boolean checkDeclaredViews;

    private final boolean checkDeclaredMonitors;

    private final boolean printAllUndeclaredConstraints;

    private final byte propagationEngineType;

    private final int nbMaxLearnt;

    private final boolean intVarLazyLitWithWeakBounds;

    private final double ibexContractionRatio;

    private final boolean ibexRestoreRounding;

    private final Map<String, String> additionalSettings;

    private final boolean lcg;

    private final Supplier<IEnvironment> environmentSupplier;

    protected Settings(SettingsBuilder builder) {
        this.modelChecker = builder.getModelChecker();
        this.cloneVariableArrayInPropagator = builder.cloneVariableArrayInPropagator();
        this.enableViews = builder.enableViews();
        this.enumeratedDomainSizeThreshold = builder.getEnumeratedDomainSizeThreshold();
        this.minCardForSumDecomposition = builder.getMinCardForSumDecomposition();
        this.enableTableSubstitution = builder.enableTableSubstitution();
        this.maxTupleSizeForSubstitution = builder.getMaxTupleSizeForSubstitution();
        this.maxSizeInMBToUseCompactTable = builder.getMaxSizeInMBToUseCompactTable();
        this.sortPropagatorActivationWRTPriority = builder.sortPropagatorActivationWRTPriority();
        this.defaultSearch = builder.getDefaultSearch();
        this.warnUser = builder.warnUser();
        this.enableDecompositionOfBooleanSum = builder.enableDecompositionOfBooleanSum();
        this.incrementalityOnBoolSumThreshold = builder.getIncrementalityOnBoolSumThreshold();
        this.enableSAT = builder.enableSAT();
        this.swapOnPassivate = builder.swapOnPassivate();
        this.checkDeclaredConstraints = builder.checkDeclaredConstraints();
        this.checkDeclaredViews = builder.checkDeclaredViews();
        this.checkDeclaredMonitors = builder.checkDeclaredMonitors();
        this.printAllUndeclaredConstraints = builder.printAllUndeclaredConstraints();
        this.propagationEngineType = builder.setPropagationEngineType();
        this.nbMaxLearnt = builder.getNbMaxLearntClauses();
        this.intVarLazyLitWithWeakBounds = builder.enableIntVarLazyLitWithWeakBounds();
        this.ibexContractionRatio = builder.getIbexContractionRatio();
        this.ibexRestoreRounding = builder.getIbexRestoreRounding();
        this.lcg = builder.isLCG();
        this.environmentSupplier = builder.getEnvironmentSupplier();
        this.additionalSettings = new HashMap<>(builder.getAdditionalSettings());
    }

    /**
     * @param solver the solver
     * @return <tt>true</tt> if the model is OK wrt the checker, <tt>false</tt> otherwise
     */
    public boolean checkModel(Solver solver) {
        return modelChecker.test(solver);
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
     * @return <tt>true</tt> if views are enabled.
     */
    public boolean enableViews() {
        return enableViews;
    }

    /**
     * @return maximum domain size threshold to force integer variable to be enumerated
     */
    public int getEnumeratedDomainSizeThreshold() {
        return enumeratedDomainSizeThreshold;
    }

    /**
     * @return minimum number of cardinality threshold to a sum constraint to be decomposed
     */
    public int getMinCardForSumDecomposition() {
        return minCardForSumDecomposition;
    }

    /**
     * @return <tt>true</tt> if some intension constraints can be replaced by extension constraints
     */
    public boolean enableTableSubstitution() {
        return enableTableSubstitution;
    }

    /**
     * @return maximum domain size threshold to replace intension constraints by extension constraints
     */
    public int getMaxTupleSizeForSubstitution() {
        return maxTupleSizeForSubstitution;
    }

    /**
     * @return maximum estimated size, in MB, of the table to use compact table representation
     */
    public long getMaxSizeInMBToUseCompactTable() {
        return maxSizeInMBToUseCompactTable;
    }

    /**
     * @return {@code true} if propagators are sorted wrt their priority on initial activation.
     */
    public boolean sortPropagatorActivationWRTPriority() {
        return sortPropagatorActivationWRTPriority;
    }

    /**
     * Set default search strategy for the input model
     *
     * @param model a model requiring a default search strategy
     * @see Search#defaultSearch(Model)
     */
    public void makeDefaultSearch(Model model) {
        defaultSearch.accept(model);
    }

    /**
     * @return <tt>true</tt> if warnings detected during modeling/solving are output.
     */
    public boolean warnUser() {
        return warnUser;
    }


    /**
     * @return {@code true} if boolean sum should be decomposed into an equality constraint and an arithmetic constraint,
     * {@code false}if a single constraint should be used instead.
     */
    public boolean enableDecompositionOfBooleanSum() {
        return enableDecompositionOfBooleanSum;
    }

    /**
     * @return the minimum number of boolean variables in a sum constraint to consider incrementality
     * (i.e. to use a dedicated propagator that maintains the current sum value and incrementally updates it when a variable is instantiated)
     * instead of using a non-incremental propagator that recomputes the sum from scratch at each propagation.
     */
    public int getIncrementalityOnBoolSumThreshold() {
        return incrementalityOnBoolSumThreshold;
    }

    /**
     * @return <i>true</i> when an underlying SAT solver is used to manage clauses declared through {@link ISatFactory},
     * <i>false</i> when clauses are managed with CSP constraints only.
     */
    public boolean enableSAT() {
        return enableSAT;
    }

    /**
     * @return <i>true</i> if, on propagator passivation, the propagator is swapped from active to passive in its variables' propagators list.
     * <i>false</i> if, on propagator passivation, only the propagator's state is set to PASSIVE.
     */
    public boolean swapOnPassivate() {
        return swapOnPassivate;
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
     * @return <i>true</i> to list all undeclared constraint, <i>false</i> (default value) otherwise.
     * Only active when {@link #checkDeclaredConstraints()} is on.
     */
    public boolean printAllUndeclaredConstraints() {
        return printAllUndeclaredConstraints;
    }

    /**
     * @return <i>true</i> (default value) to check prior to creation
     * if a view already semantically exists.
     */
    public boolean checkDeclaredViews() {
        return checkDeclaredViews;
    }

    /**
     * @return <i>true</i> (default value) to check prior to creation
     * if a monitor already semantically exists.
     */
    public boolean checkDeclaredMonitors() {
        return this.checkDeclaredMonitors;
    }

    /**
     * @return <i>0b00<i/> if constraint-oriented propagation engine,
     * <i>0b01<i/> if hybridization between variable and constraint oriented and
     * <i>0b10<i/> if variable-oriented.
     */
    public byte getPropagationEnginType() {
        return propagationEngineType;
    }

    /**
     * @return true if the solver is in Lazy Clause Generation mode (in opposition to the full CP mode).
     */
    public boolean isLCG() {
        return this.lcg;
    }

    /**
     * @return maximum number of learnt clauses to store. When reached, a reduction is applied.
     */
    public int getNbMaxLearntClauses() {
        return nbMaxLearnt;
    }

    /**
     * @return <tt>true</tt> if the {@link IntVarLazyLit} propagator uses weak bounds.
     */
    public boolean enableIntVarLazyLitWithWeakBounds() {
        return intVarLazyLitWithWeakBounds;
    }

    /**
     * @return the ratio that a domains must be contracted by ibex to compute the constraint.
     */
    public double getIbexContractionRatio() {
        return ibexContractionRatio;
    }

    /**
     * @return if ibex must restore java rounding mode when returning a call.
     */
    public boolean getIbexRestoreRounding() {
        return ibexRestoreRounding;
    }

    /**
     * Get the value of an additional setting.
     * The additional settings are a map of string keys to string values that can be used to store any additional setting that is not explicitly defined in this class.
     *
     * @param key the key of the setting
     * @return an optional containing the value of the setting if it exists, an empty optional otherwise
     */
    public Optional<String> get(String key) {
        return Optional.ofNullable(additionalSettings.get(key));
    }

}
