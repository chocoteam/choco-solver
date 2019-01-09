/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.memory.Except_0;
import org.chocosolver.memory.ICondition;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.util.ESat;

import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * A concrete implementation of Settings that enables to modify settings programmatically.
 *
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 14/12/2017.
 */
public class DefaultSettings implements Settings {

    /**
     * Default welcome message
     */
    private static final String DEFAULT_WELCOME_MESSAGE =
        "** Choco 4.10.0 (2018-12) : Constraint Programming Solver, Copyright (c) 2010-2018";

    private static final String DEFAULT_PREFIX = "TMP_";

    private String welcomeMessage = DEFAULT_WELCOME_MESSAGE;

    private Predicate<Solver> modelChecker = s -> !ESat.FALSE.equals(s.isSatisfied());

    private boolean enableViews = true;

    private int maxDomSizeForEnumerated = 32_768;

    private int minCardForSumDecomposition = 1024;

    private boolean enableTableSubstitution = true;

    private int maxTupleSizeForSubstitution = 10_000;

    private double MCRDecimalPrecision = 1e-4d;

    private boolean sortPropagatorActivationWRTPriority = true;

    private Function<Model, AbstractStrategy> defaultSearch = Search::defaultSearch;

    private ICondition environmentHistorySimulationCondition = new Except_0();

    private boolean warnUser = false;

    private boolean enableDecompositionOfBooleanSum = false;

    private IntPredicate enableIncrementalityOnBoolSum = i -> i > 10;

    private boolean cloneVariableArrayInPropagator = true;

    private boolean enableACOnTernarySum = false;

    private String defaultPrefix = DEFAULT_PREFIX;

    private boolean enableSAT = false;

    private boolean swapOnPassivate = false;

    private boolean checkDeclaredConstraints = false;

    private byte hybridEngine = 0b00;

    private int nbMaxLearnt = 100_000;

    private int maxLearntCardinlity = Integer.MAX_VALUE / 100;

    private float clauseReductionRatio = .5f;

    private int dominancePerimeter = 4;

    private boolean explainGlobalFailureInSum = true;

    private Function<Model, Solver> initSolver = Solver::new;


    public DefaultSettings() {
    }

    @Override
    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    @Override
    public DefaultSettings setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
        return this;
    }

    @Override
    public boolean checkModel(Solver solver) {
        return modelChecker.test(solver);
    }


    @Override
    public DefaultSettings setModelChecker(Predicate<Solver> modelChecker) {
        this.modelChecker = modelChecker;
        return this;
    }

    @Override
    public boolean enableViews() {
        return enableViews;
    }


    @Override
    public DefaultSettings setEnableViews(boolean enableViews) {
        this.enableViews = enableViews;
        return this;
    }

    @Override
    public int getMaxDomSizeForEnumerated() {
        return maxDomSizeForEnumerated;
    }

    @Override
    public DefaultSettings setMaxDomSizeForEnumerated(int maxDomSizeForEnumerated) {
        this.maxDomSizeForEnumerated = maxDomSizeForEnumerated;
        return this;
    }

    @Override
    public int getMinCardForSumDecomposition() {
        return minCardForSumDecomposition;
    }

    @Override
    public Settings setMinCardinalityForSumDecomposition(int defaultMinCardinalityForSumDecomposition) {
        this.minCardForSumDecomposition = defaultMinCardinalityForSumDecomposition;
        return this;
    }

    @Override
    public boolean enableTableSubstitution() {
        return enableTableSubstitution;
    }

    @Override
    public DefaultSettings setEnableTableSubstitution(boolean enableTableSubstitution) {
        this.enableTableSubstitution = enableTableSubstitution;
        return this;
    }

    @Override
    public int getMaxTupleSizeForSubstitution() {
        return maxTupleSizeForSubstitution;
    }

    @Override
    public double getMCRDecimalPrecision() {
        return MCRDecimalPrecision;
    }

    @Override
    public Settings setMCRDecimalPrecision(double precision) {
        this.MCRDecimalPrecision = precision;
        return this;
    }

    @Override
    public DefaultSettings setMaxTupleSizeForSubstitution(int maxTupleSizeForSubstitution) {
        this.maxTupleSizeForSubstitution = maxTupleSizeForSubstitution;
        return this;
    }

    @Override
    public boolean sortPropagatorActivationWRTPriority() {
        return sortPropagatorActivationWRTPriority;
    }

    @Override
    public DefaultSettings setSortPropagatorActivationWRTPriority(boolean sortPropagatorActivationWRTPriority) {
        this.sortPropagatorActivationWRTPriority = sortPropagatorActivationWRTPriority;
        return this;
    }

    @Override
    public AbstractStrategy makeDefaultSearch(Model model) {
        return defaultSearch.apply(model);
    }

    @Override
    public DefaultSettings setDefaultSearch(Function<Model, AbstractStrategy> defaultSearch) {
        this.defaultSearch = defaultSearch;
        return this;
    }

    @Override
    public ICondition getEnvironmentHistorySimulationCondition() {
        return environmentHistorySimulationCondition;
    }

    @Override
    public DefaultSettings setEnvironmentHistorySimulationCondition(ICondition environmentHistorySimulationCondition) {
        this.environmentHistorySimulationCondition = environmentHistorySimulationCondition;
        return this;
    }

    @Override
    public boolean warnUser() {
        return warnUser;
    }

    @Override
    public DefaultSettings setWarnUser(boolean warnUser) {
        this.warnUser = warnUser;
        return this;
    }

    @Override
    public boolean enableDecompositionOfBooleanSum() {
        return enableDecompositionOfBooleanSum;
    }

    @Override
    public DefaultSettings setEnableDecompositionOfBooleanSum(boolean enableDecompositionOfBooleanSum) {
        this.enableDecompositionOfBooleanSum = enableDecompositionOfBooleanSum;
        return this;
    }

    @Override
    public boolean enableIncrementalityOnBoolSum(int nbvars) {
        return enableIncrementalityOnBoolSum.test(nbvars);
    }

    @Override
    public DefaultSettings setEnableIncrementalityOnBoolSum(IntPredicate enableIncrementalityOnBoolSum) {
        this.enableIncrementalityOnBoolSum = enableIncrementalityOnBoolSum;
        return this;
    }

    @Override
    public boolean cloneVariableArrayInPropagator() {
        return cloneVariableArrayInPropagator;
    }

    @Override
    public DefaultSettings setCloneVariableArrayInPropagator(boolean cloneVariableArrayInPropagator) {
        this.cloneVariableArrayInPropagator = cloneVariableArrayInPropagator;
        return this;
    }

    @Override
    public boolean enableACOnTernarySum() {
        return enableACOnTernarySum;
    }

    @Override
    public Settings setEnableACOnTernarySum(boolean enable) {
        this.enableACOnTernarySum = enable;
        return this;
    }

    @Override
    public String defaultPrefix() {
        return defaultPrefix;
    }

    @Override
    public DefaultSettings setDefaultPrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
        return this;
    }

    @Override
    public boolean enableSAT() {
        return enableSAT;
    }

    @Override
    public DefaultSettings setEnableSAT(boolean enableSAT) {
        this.enableSAT = enableSAT;
        return this;
    }

    @Override
    public boolean swapOnPassivate() {
        return swapOnPassivate;
    }

    @Override
    public DefaultSettings setSwapOnPassivate(boolean swapOnPassivate) {
        this.swapOnPassivate = swapOnPassivate;
        return this;
    }

    @Override
    public boolean checkDeclaredConstraints() {
        return checkDeclaredConstraints;
    }

    @Override
    public DefaultSettings setCheckDeclaredConstraints(boolean checkDeclaredConstraints) {
        this.checkDeclaredConstraints = checkDeclaredConstraints;
        return this;
    }

    @Override
    public Solver initSolver(Model model) {
        return initSolver.apply(model);
    }


    @Override
    public DefaultSettings setInitSolver(Function<Model, Solver> initSolver) {
        this.initSolver = initSolver;
        return this;
    }

    @Override
    public byte enableHybridizationOfPropagationEngine() {
        return hybridEngine;
    }

    @Override
    public Settings setHybridizationOfPropagationEngine(byte hybrid) {
        this.hybridEngine = hybrid;
        return this;
    }

    @Override
    public int getNbMaxLearntClauses() {
        return nbMaxLearnt;
    }

    @Override
    public Settings setNbMaxLearntClauses(int n) {
        this.nbMaxLearnt = n;
        return this;
    }

    @Override
    public float getRatioForClauseStoreReduction() {
        return this.clauseReductionRatio;
    }

    @Override
    public Settings setRatioForClauseStoreReduction(float f) {
        this.clauseReductionRatio = f;
        return this;
    }

    @Override
    public int getMaxLearntClauseCardinality() {
        return maxLearntCardinlity;
    }

    @Override
    public Settings setMaxLearntClauseCardinality(int n) {
        maxLearntCardinlity = n;
        return this;
    }

    @Override
    public int getLearntClausesDominancePerimeter() {
        return dominancePerimeter;
    }

    @Override
    public Settings setLearntClausesDominancePerimeter(int n) {
        this.dominancePerimeter = n;
        return this;
    }

    @Override
    public boolean explainGlobalFailureInSum() {
        return explainGlobalFailureInSum;
    }

    @Override
    public Settings explainGlobalFailureInSum(boolean b) {
        this.explainGlobalFailureInSum = b;
        return this;
    }
}
