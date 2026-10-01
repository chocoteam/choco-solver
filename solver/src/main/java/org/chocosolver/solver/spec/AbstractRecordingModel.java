/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.IModel;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.cnf.SatConstraint;
import org.chocosolver.solver.constraints.real.IbexHandler;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.Group;
import org.chocosolver.solver.variables.Variable;

import java.util.Set;

/**
 * The hand-written part of {@link RecordingModel}: journalization of the operations of {@link Model} which are
 * not factory methods.
 * <p>
 * {@link RecordingModel}, which journalizes the factory methods, is generated at compile time.
 *
 * @author Charles Prud'homme
 */
@GenerateRecordingModel(name = "RecordingModel", factories = IModel.class,
        excluded = {"checkIntDomainRange", "checkRealDomainRange", "toBoolVar", "getDomainUnion"})
public abstract class AbstractRecordingModel extends Model {

    /**
     * Hooks managed internally by the solver: never journalized.
     */
    private static final Set<String> INTERNAL_HOOKS = Set.of("cinstances",
            MINISAT_HOOK_NAME, IBEX_HOOK_NAME, TASK_SET_HOOK_NAME);

    private final Recorder recorder;

    protected AbstractRecordingModel(String name, Settings settings) {
        super(name, settings);
        this.recorder = new Recorder(this, new Header(name, settings, getSeed()));
    }

    protected AbstractRecordingModel(String name) {
        this(name, SettingsBuilder.init().build());
    }

    protected AbstractRecordingModel(Settings settings) {
        this("RecordingModel", settings);
    }

    protected AbstractRecordingModel() {
        this("RecordingModel");
    }

    /**
     * @return the recorder of this model
     */
    @Override
    public final Recorder getRecorder() {
        return recorder;
    }

    /**
     * @return the spec of the model built so far
     * @throws SpecException if some operations could not be journalized, or if the resolution has started
     * @see Recorder#snapshot()
     */
    public final ModelSpec snapshot() {
        return recorder.snapshot();
    }

    @Override
    public final Model duplicate() {
        return snapshot().instantiate().model();
    }

    /**
     * @return <i>true</i> if the operation must be journalized
     */
    protected final boolean topLevel() {
        // null while the constructor of Model runs
        return recorder != null && !recorder.busy();
    }

    @Override
    public void post(Constraint... cs) throws SolverException {
        if (topLevel()) {
            recorder.post(cs, () -> super.post(cs));
        } else {
            super.post(cs);
        }
    }

    @Override
    public void unpost(Constraint... constraints) throws SolverException {
        if (topLevel()) {
            recorder.unpost(constraints, () -> super.unpost(constraints));
        } else {
            super.unpost(constraints);
        }
    }

    @Override
    public void addHook(String hookName, Object hookObject) {
        if (topLevel() && !INTERNAL_HOOKS.contains(hookName)) {
            recorder.hook(hookName, hookObject, () -> super.addHook(hookName, hookObject));
        } else {
            super.addHook(hookName, hookObject);
        }
    }

    @Override
    public void removeHook(String hookName) {
        if (topLevel() && !INTERNAL_HOOKS.contains(hookName)) {
            recorder.hook(hookName, null, () -> super.removeHook(hookName));
        } else {
            super.removeHook(hookName);
        }
    }

    @Override
    public void removeAllHooks() {
        if (topLevel()) {
            recorder.removeAllHooks(super::removeAllHooks);
        } else {
            super.removeAllHooks();
        }
    }

    @Override
    public void setName(String name) {
        if (topLevel()) {
            recorder.name(name, () -> super.setName(name));
        } else {
            super.setName(name);
        }
    }

    @Override
    public <V extends Variable> void addGroup(Group<V> g) {
        if (topLevel()) {
            recorder.group(g.getName(), g.getVariables(), () -> super.addGroup(g));
        } else {
            super.addGroup(g);
        }
    }

    @Override
    public void setPrecision(double p) {
        if (topLevel()) {
            recorder.unsupported("setPrecision", "real variables are not supported");
        }
        super.setPrecision(p);
    }

    @Override
    public void setSeed(long seed) {
        if (topLevel()) {
            recorder.seed(seed, () -> super.setSeed(seed));
        } else {
            super.setSeed(seed);
        }
    }

    @Override
    public void setObjective(boolean maximize, Variable objective) {
        if (topLevel()) {
            recorder.objective(maximize, objective, () -> super.setObjective(maximize, objective));
        } else {
            super.setObjective(maximize, objective);
        }
    }

    @Override
    public void clearObjective() {
        if (topLevel()) {
            recorder.objective(false, null, super::clearObjective);
        } else {
            super.clearObjective();
        }
    }

    // The following methods are not factories: what they do internally must not be journalized.
    // Their effects are replayed by the factories which need them.

    @Override
    public SatConstraint getMinisat() {
        // also called by search monitors (nogoods), which are not part of the model: never journalized
        return topLevel() ? recorder.silently(super::getMinisat) : super.getMinisat();
    }

    @Override
    public void removeMinisat() {
        if (topLevel()) {
            recorder.unsupported("removeMinisat", "not supported");
        }
        super.removeMinisat();
    }

    @Override
    public IbexHandler getIbexHandler() {
        if (topLevel()) {
            recorder.unsupported("getIbexHandler", "real constraints are not supported");
        }
        return super.getIbexHandler();
    }
}
