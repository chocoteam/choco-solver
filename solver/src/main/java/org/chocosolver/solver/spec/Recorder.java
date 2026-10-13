/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.sat.Literalizer;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.cnf.ILogical;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.search.SearchState;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * Journalizes the construction of a {@link RecordingModel} into a {@link ModelSpec}.
 * <p>
 * Only top-level operations are journalized: a depth counter ensures that the operations triggered internally by a
 * factory (decompositions, intermediate variables, internal posts...) are not, since replaying the top-level
 * operation replays them too.
 * <p>
 * An operation that cannot be journalized (unknown object, unsupported argument...) does not interrupt the
 * construction of the model: it is reported, and {@link #snapshot()} fails.
 * <p>
 * The recorder is also the {@link Resolver} of the recorded model: identifiers of the spec map to its objects.
 * <p>
 * Not thread-safe: a model is built by a single thread.
 *
 * @author Charles Prud'homme
 */
public final class Recorder extends IdTable {

    /**
     * An operation which could not be journalized.
     *
     * @param step      index of the step at which the operation occurred
     * @param operation the operation
     * @param reason    why it could not be journalized
     */
    public record Unsupported(int step, String operation, String reason) {
        @Override
        public String toString() {
            return "#" + step + " " + operation + ": " + reason;
        }
    }

    /**
     * Thrown when an argument cannot be captured.
     */
    private static final class Uncapturable extends Exception {
        Uncapturable(String message) {
            super(message, null, false, false);
        }
    }

    private static final FactoryMethod INT_CONSTANT = FactoryMethod.of("intVar", int.class);
    private static final FactoryMethod BOOL_NOT_VIEW = FactoryMethod.of("boolNotView", BoolVar.class);

    private Header header;
    private final List<Step> steps = new ArrayList<>();
    private final List<Unsupported> unsupported = new ArrayList<>();
    /**
     * Reverse mapping, maintained eagerly: it is queried for each argument recorded.
     */
    private final IdentityHashMap<Object, Integer> ids = new IdentityHashMap<>();
    private int depth;

    Recorder(Model model, Header header) {
        super(model);
        this.header = header;
    }

    /**
     * @return <i>true</i> when an operation is being journalized: nested operations must not be journalized
     */
    public boolean busy() {
        return depth > 0;
    }

    /**
     * @return the operations which could not be journalized so far
     */
    public List<Unsupported> unsupported() {
        return Collections.unmodifiableList(unsupported);
    }

    /**
     * @param o an object of the model
     * @return the identifier of <i>o</i> in the spec, or -1 if <i>o</i> is unknown
     */
    @Override
    public int idOf(Object o) {
        return ids.getOrDefault(o, -1);
    }

    /**
     * @param vars variables of the recorded model
     * @return their identifiers in the spec, e.g. to declare a {@link SearchDecl}
     * @throws SpecException if a variable is unknown
     */
    public int[] idsOf(Variable... vars) {
        int[] r = new int[vars.length];
        for (int i = 0; i < vars.length; i++) {
            r[i] = idOf(vars[i]);
            if (r[i] < 0) {
                throw new SpecException("Unknown variable " + vars[i].getName());
            }
        }
        return r;
    }

    /**
     * @return the spec of the model built so far
     * @throws SpecException if some operations could not be journalized, or if the resolution has started
     */
    public ModelSpec snapshot() {
        if (model().getSolver().getSearchState() != SearchState.NEW) {
            throw new SpecException("Cannot snapshot a model whose resolution has started");
        }
        if (!unsupported.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(unsupported.size()).append(" operation(s) cannot be journalized:");
            unsupported.stream().limit(10).forEach(u -> sb.append("\n\t").append(u));
            if (unsupported.size() > 10) {
                sb.append("\n\t...");
            }
            throw new SpecException(sb.toString());
        }
        return new ModelSpec(header, steps);
    }

    //***********************************************************************************
    // OPERATIONS
    //***********************************************************************************

    /**
     * Journalize a call to a factory method.
     *
     * @param method the factory method
     * @param body   the actual call
     * @param args   the arguments of the call
     * @return the result of the call
     */
    <R> R call(FactoryMethod method, Supplier<R> body, Object[] args) {
        Arg[] captured = new Arg[args.length];
        String failure = null;
        try {
            for (int i = 0; i < args.length; i++) {
                captured[i] = capture(args[i]);
            }
        } catch (Uncapturable e) {
            failure = e.getMessage();
        }
        R result = nested(body);
        int[] out = outputs(result);
        if (failure == null) {
            steps.add(new Step.Call(out, method, captured));
        } else {
            unsupported(method.toString(), failure);
        }
        return result;
    }

    /**
     * Journalize a call to a factory method which returns nothing.
     */
    void run(FactoryMethod method, Runnable body, Object[] args) {
        call(method, () -> {
            body.run();
            return null;
        }, args);
    }

    void post(Constraint[] cstrs, Runnable body) {
        int[] cids = constraintIds("post", cstrs);
        nested(body);
        if (cids != null) {
            steps.add(new Step.Post(cids));
        }
    }

    void unpost(Constraint[] cstrs, Runnable body) {
        int[] cids = constraintIds("unpost", cstrs);
        nested(body);
        if (cids != null) {
            steps.add(new Step.Unpost(cids));
        }
    }

    /**
     * Journalize {@link Constraint#reify()}.
     */
    public BoolVar reify(Constraint cstr, Supplier<BoolVar> body) {
        int cid = ref("reify", cstr);
        BoolVar b = nested(body);
        int out = idFor(b);
        if (cid >= 0) {
            steps.add(new Step.Reify(cid, out));
        }
        return b;
    }

    /**
     * Journalize {@link Constraint#reifyWith}, {@link Constraint#implies} or {@link Constraint#impliedBy}.
     */
    public void link(Step.Link.Kind kind, Constraint cstr, BoolVar bool, Runnable body) {
        int[] r = refs(kind.name(), cstr, bool);
        nested(body);
        if (r != null) {
            steps.add(new Step.Link(kind, r[0], r[1]));
        }
    }

    /**
     * Journalize the creation of a task with its canonical constructor (the task is already built).
     *
     * @param performed the performed variable of an optional task, null for a mandatory task
     */
    public void newTask(Task task, IntVar start, IntVar duration, IntVar end, BoolVar performed) {
        if (busy()) {
            return;
        }
        int[] r = performed == null
                ? refs("new Task", start, duration, end)
                : refs("new OptionalTask", start, duration, end, performed);
        int out = idFor(task);
        if (r != null) {
            steps.add(new Step.NewTask(out, r[0], r[1], r[2], performed == null ? -1 : r[3]));
        }
    }

    void hook(String name, Object value, Runnable body) {
        Arg captured = tryCapture("addHook(" + name + ")", value);
        nested(body);
        if (captured != null) {
            steps.add(new Step.Hook(name, captured));
        }
    }

    void removeAllHooks(Runnable body) {
        nested(body);
        steps.add(new Step.Hook(null, new Arg.Const(null)));
    }

    void name(String name, Runnable body) {
        nested(body);
        header = header.withName(name);
    }

    void group(String name, Variable[] vars, Runnable body) {
        Arg captured = tryCapture("addGroup(" + name + ")", vars);
        nested(body);
        if (captured instanceof Arg.RefArray a) {
            steps.add(new Step.Group(name, a));
        } else if (captured != null) {
            unsupported("addGroup(" + name + ")", "not an array of variables");
        }
    }

    void seed(long seed, Runnable body) {
        nested(body);
        steps.add(new Step.Seed(seed));
    }

    void objective(boolean maximize, Variable objective, Runnable body) {
        int id = objective == null ? -1 : ref("setObjective", objective);
        nested(body);
        if (objective == null || id >= 0) {
            steps.add(new Step.Objective(maximize, id));
        }
    }

    /**
     * Run <i>body</i> without journalizing what it does.
     */
    <R> R silently(Supplier<R> body) {
        return nested(body);
    }

    void unsupported(String operation, String reason) {
        unsupported.add(new Unsupported(steps.size(), operation, reason));
    }

    //***********************************************************************************
    // INTERNALS
    //***********************************************************************************

    private <R> R nested(Supplier<R> body) {
        depth++;
        try {
            return body.get();
        } finally {
            depth--;
        }
    }

    private void nested(Runnable body) {
        nested(() -> {
            body.run();
            return null;
        });
    }

    /**
     * @return the captured argument, or null (and the operation is reported) if it cannot be captured
     */
    private Arg tryCapture(String operation, Object value) {
        try {
            return capture(value);
        } catch (Uncapturable e) {
            unsupported(operation, e.getMessage());
            return null;
        }
    }

    /**
     * @return the identifier of <i>o</i>, or -1 (and the operation is reported) if <i>o</i> is unknown
     */
    private int ref(String operation, Object o) {
        try {
            return refOf(o);
        } catch (Uncapturable e) {
            unsupported(operation, e.getMessage());
            return -1;
        }
    }

    /**
     * @return the identifiers of <i>objects</i>, or null (and the operation is reported) if one is unknown
     */
    private int[] refs(String operation, Object... objects) {
        int[] r = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            r[i] = ref(operation, objects[i]);
            if (r[i] < 0) {
                return null;
            }
        }
        return r;
    }

    private int[] constraintIds(String operation, Constraint[] cstrs) {
        return cstrs == null ? Specs.NO_INT : refs(operation, (Object[]) cstrs);
    }

    private static boolean isModelType(Class<?> c) {
        return Variable.class.isAssignableFrom(c) || Constraint.class.isAssignableFrom(c) || Task.class.isAssignableFrom(c);
    }

    private static boolean isModelObject(Object o) {
        return o != null && isModelType(o.getClass());
    }

    private Arg capture(Object v) throws Uncapturable {
        if (isModelObject(v)) {
            return new Arg.Ref(refOf(v));
        }
        if (v instanceof LogOp op) {
            return logic(op);
        }
        if (v instanceof Literalizer.BoolLit l) {
            return new Arg.Literal(Arg.Literal.Kind.BOOL, refOf(l.cpVar), 0);
        }
        if (v instanceof Literalizer.IntEqLit l) {
            return new Arg.Literal(Arg.Literal.Kind.INT_EQ, refOf(l.cpVar), l.val);
        }
        if (v instanceof Literalizer.IntLeLit l) {
            return new Arg.Literal(Arg.Literal.Kind.INT_LE, refOf(l.cpVar), l.val);
        }
        if (v instanceof List<?> list) {
            int[] lids = new int[list.size()];
            for (int i = 0; i < lids.length; i++) {
                if (!isModelObject(list.get(i))) {
                    throw new Uncapturable("list of " + (list.get(i) == null ? "null" : list.get(i).getClass().getSimpleName()));
                }
                lids[i] = refOf(list.get(i));
            }
            return new Arg.RefList(lids);
        }
        if (v != null && v.getClass().isArray()) {
            Class<?> ct = v.getClass().getComponentType();
            if (isModelType(ct)) {
                return new Arg.RefArray(ct, refsOf((Object[]) v));
            }
            if (ct.isArray() && isModelType(ct.getComponentType())) {
                Object[][] m = (Object[][]) v;
                int[][] mids = new int[m.length][];
                for (int i = 0; i < m.length; i++) {
                    mids[i] = refsOf(m[i]);
                }
                return new Arg.RefMatrix(ct.getComponentType(), mids);
            }
        }
        try {
            return new Arg.Const(v);
        } catch (SpecException e) {
            throw new Uncapturable(e.getMessage());
        }
    }

    private Arg.Logic logic(LogOp op) throws Uncapturable {
        ILogical[] children = op.getChildren();
        Arg[] args = new Arg[children.length];
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof LogOp sub) {
                args[i] = logic(sub);
            } else if (children[i] instanceof BoolVar) {
                args[i] = new Arg.Ref(refOf(children[i]));
            } else {
                throw new Uncapturable("logical tree with a " + children[i].getClass().getSimpleName());
            }
        }
        return new Arg.Logic(op.is(LogOp.Operator.AND), op.isNot(), args);
    }

    private int[] refsOf(Object[] objects) throws Uncapturable {
        if (objects == null) {
            throw new Uncapturable("null array");
        }
        int[] r = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            r[i] = refOf(objects[i]);
        }
        return r;
    }

    /**
     * @return the identifier of <i>o</i>; an unknown object is derived, when possible, from known objects
     * (cached constant, negation of a known boolean variable)
     */
    private int refOf(Object o) throws Uncapturable {
        if (o == null) {
            throw new Uncapturable("null object");
        }
        Integer id = ids.get(o);
        if (id != null) {
            return id;
        }
        if (o instanceof Variable v) {
            if (v.getModel() != model()) {
                throw new Uncapturable("variable " + v.getName() + " belongs to another model");
            }
            if (v instanceof IntVar iv && v.isAConstant()) {
                int value = iv.getValue();
                if (model().getCachedConstants().get(value) == v) {
                    return derive(INT_CONSTANT, v, new Arg.Const(value));
                }
            }
            if (v instanceof BoolVar b && b.hasNot()) {
                Integer nid = ids.get(b.not());
                if (nid != null) {
                    return derive(BOOL_NOT_VIEW, v, new Arg.Ref(nid));
                }
            }
            throw new Uncapturable("unknown variable " + v.getName()
                    + " (created outside of the factories, or internally by a factory)");
        }
        if (o instanceof Constraint c) {
            throw new Uncapturable("unknown constraint " + c.getName()
                    + " (custom constraint not built with Model.custom(...), or created internally by a factory)");
        }
        throw new Uncapturable("unknown " + o.getClass().getSimpleName());
    }

    /**
     * Journalize a call which returns the already existing object <i>o</i> (e.g., a cached constant).
     */
    private int derive(FactoryMethod method, Object o, Arg arg) {
        int id = idFor(o);
        steps.add(new Step.Call(new int[]{id}, method, new Arg[]{arg}));
        return id;
    }

    /**
     * @return the identifier of <i>o</i>, a new one if <i>o</i> is unknown
     */
    private int idFor(Object o) {
        Integer id = ids.get(o);
        if (id == null) {
            id = capacity();
            ids.put(o, id);
            put(id, o);
        }
        return id;
    }

    /**
     * @return the identifiers of the model objects returned by a call (flattened for arrays)
     */
    private int[] outputs(Object result) {
        if (isModelObject(result)) {
            return new int[]{idFor(result)};
        }
        if (result instanceof Object[] array && isModelType(Specs.elementType(array.getClass()))) {
            List<Object> flat = new ArrayList<>();
            Specs.flatten(array, flat);
            int[] out = new int[flat.size()];
            for (int i = 0; i < out.length; i++) {
                out[i] = flat.get(i) == null ? -1 : idFor(flat.get(i));
            }
            return out;
        }
        return Specs.NO_INT;
    }
}
