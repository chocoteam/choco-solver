/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.sat.Literalizer;
import org.chocosolver.solver.IModel;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.Task;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helps building a {@link ModelSpec} by hand: allocates identifiers and resolves the overload of factory methods
 * from the types of the arguments.
 * <p>
 * Arguments passed to {@code call*} methods are either {@link Arg}s or constants (wrapped in {@link Arg.Const}).
 * <pre>{@code
 * SpecBuilder b = new SpecBuilder(Header.of("demo"));
 * int[] x = b.callArray(3, "intVarArray", "x", 3, 1, 3);
 * b.post(b.call("allDifferent", b.refs(IntVar.class, x)));
 * ModelSpec spec = b.build();
 * }</pre>
 *
 * @author Charles Prud'homme
 */
public final class SpecBuilder {

    /**
     * The (non-static) methods of {@link IModel}, by name.
     */
    private static final Map<String, List<Method>> METHODS = Arrays.stream(IModel.class.getMethods())
            .filter(m -> !Modifier.isStatic(m.getModifiers()))
            .collect(Collectors.groupingBy(Method::getName));

    private final Header header;
    private final List<Step> steps = new ArrayList<>();
    /**
     * Static type of each identifier, to resolve overloads.
     */
    private final Map<Integer, Class<?>> types = new HashMap<>();
    private int nextId;
    private SearchDecl search;

    public SpecBuilder(Header header) {
        this.header = header;
    }

    /**
     * @return a builder which continues <i>spec</i>: its steps, identifiers and search are kept
     */
    public static SpecBuilder from(ModelSpec spec) {
        SpecBuilder b = new SpecBuilder(spec.header());
        b.search = spec.search();
        for (Step s : spec.steps()) {
            b.steps.add(s);
            if (s instanceof Step.Call c) {
                Class<?> t = Specs.elementType(c.method().returnType());
                for (int o : c.rawOut()) {
                    if (o >= 0) {
                        b.types.put(o, t);
                    }
                }
            } else if (s instanceof Step.Reify r) {
                b.types.put(r.out(), BoolVar.class);
            } else if (s instanceof Step.NewTask t) {
                b.types.put(t.out(), t.performed() < 0 ? Task.class : OptionalTask.class);
            } else if (s instanceof Step.Custom c) {
                b.types.put(c.out(), Constraint.class);
            }
            s.forEachDef(id -> b.nextId = Math.max(b.nextId, id + 1));
        }
        return b;
    }

    /**
     * Set the search of the spec.
     */
    public SpecBuilder search(SearchDecl search) {
        this.search = search;
        return this;
    }

    /**
     * @return a reference to identifier <i>id</i>
     */
    public Arg.Ref ref(int id) {
        return new Arg.Ref(id);
    }

    /**
     * @return a reference to an array of objects of type <i>componentType</i>
     */
    public Arg.RefArray refs(Class<?> componentType, int... ids) {
        return new Arg.RefArray(componentType, ids);
    }

    /**
     * @return a reference to a matrix of objects of type <i>componentType</i>
     */
    public Arg.RefMatrix refs(Class<?> componentType, int[][] ids) {
        return new Arg.RefMatrix(componentType, ids);
    }

    /**
     * Call a factory method whose result is ignored (or which returns nothing).
     */
    public SpecBuilder effect(String name, Object... args) {
        return effect(resolve(name, args), args);
    }

    public SpecBuilder effect(FactoryMethod m, Object... args) {
        steps.add(new Step.Call(Specs.NO_INT, m, toArgs(args)));
        return this;
    }

    /**
     * Call a factory method which returns an object (variable, constraint, task...).
     *
     * @return the identifier of the object
     */
    public int call(String name, Object... args) {
        return call(resolve(name, args), args);
    }

    public int call(FactoryMethod m, Object... args) {
        int id = define(m.returnType());
        steps.add(new Step.Call(new int[]{id}, m, toArgs(args)));
        return id;
    }

    /**
     * Call a factory method which returns an array (or a matrix, flattened row by row) of <i>size</i> objects.
     *
     * @return the identifiers of the objects
     */
    public int[] callArray(int size, String name, Object... args) {
        return callArray(size, resolve(name, args), args);
    }

    public int[] callArray(int size, FactoryMethod m, Object... args) {
        int[] out = new int[size];
        Class<?> component = Specs.elementType(m.returnType());
        for (int i = 0; i < size; i++) {
            out[i] = define(component);
        }
        steps.add(new Step.Call(out, m, toArgs(args)));
        return out;
    }

    public SpecBuilder post(int... cstrs) {
        steps.add(new Step.Post(cstrs));
        return this;
    }

    /**
     * @return the identifier of the boolean variable reifying <i>cstr</i>
     */
    public int reify(int cstr) {
        int id = define(BoolVar.class);
        steps.add(new Step.Reify(cstr, id));
        return id;
    }

    public SpecBuilder reifyWith(int cstr, int bool) {
        return link(Step.Link.Kind.REIFY_WITH, cstr, bool);
    }

    public SpecBuilder implies(int cstr, int bool) {
        return link(Step.Link.Kind.IMPLIES, cstr, bool);
    }

    public SpecBuilder impliedBy(int cstr, int bool) {
        return link(Step.Link.Kind.IMPLIED_BY, cstr, bool);
    }

    private SpecBuilder link(Step.Link.Kind kind, int cstr, int bool) {
        steps.add(new Step.Link(kind, cstr, bool));
        return this;
    }

    public SpecBuilder unpost(int... cstrs) {
        steps.add(new Step.Unpost(cstrs));
        return this;
    }

    /**
     * @return the identifier of the new task
     */
    public int newTask(int start, int duration, int end) {
        int id = define(Task.class);
        steps.add(new Step.NewTask(id, start, duration, end));
        return id;
    }

    /**
     * @return the identifier of the new optional task
     */
    public int newOptionalTask(int start, int duration, int end, int performed) {
        int id = define(OptionalTask.class);
        steps.add(new Step.NewTask(id, start, duration, end, performed));
        return id;
    }

    /**
     * @return the identifier of the custom constraint
     */
    public int custom(String name, Function<Resolver, Constraint> factory) {
        int id = define(Constraint.class);
        steps.add(new Step.Custom(id, name, factory));
        return id;
    }

    public ModelSpec build() {
        return new ModelSpec(header, steps, search);
    }

    /**
     * @return a new identifier, of static type <i>type</i>
     */
    private int define(Class<?> type) {
        int id = nextId++;
        types.put(id, type);
        return id;
    }

    private static Arg[] toArgs(Object[] args) {
        Arg[] res = new Arg[args.length];
        for (int i = 0; i < args.length; i++) {
            res[i] = args[i] instanceof Arg a ? a : new Arg.Const(args[i]);
        }
        return res;
    }

    /**
     * Resolve the overload of <i>name</i> which accepts <i>args</i>, choosing the most specific one as javac would.
     */
    private FactoryMethod resolve(String name, Object[] args) {
        List<Method> candidates = new ArrayList<>();
        for (Method m : METHODS.getOrDefault(name, List.of())) {
            if (m.getParameterCount() == args.length && accepts(m.getParameterTypes(), args)) {
                candidates.add(m);
            }
        }
        List<Method> mostSpecific = new ArrayList<>();
        for (Method m : candidates) {
            if (candidates.stream().allMatch(o -> moreSpecific(m, o))) {
                mostSpecific.add(m);
            }
        }
        if (mostSpecific.size() != 1) {
            throw new SpecException((candidates.isEmpty() ? "No" : "Ambiguous") + " factory method " + name
                    + " for arguments " + Arrays.stream(args).map(this::describe).collect(Collectors.joining(", ", "(", ")"))
                    + (candidates.isEmpty() ? "" : ": " + candidates));
        }
        return FactoryMethod.of(mostSpecific.get(0));
    }

    private static boolean moreSpecific(Method m, Method o) {
        Class<?>[] pm = m.getParameterTypes();
        Class<?>[] po = o.getParameterTypes();
        for (int i = 0; i < pm.length; i++) {
            if (!po[i].isAssignableFrom(pm[i])) return false;
        }
        return true;
    }

    private boolean accepts(Class<?>[] params, Object[] args) {
        for (int i = 0; i < params.length; i++) {
            Class<?> t = typeOf(args[i]);
            if (t == null ? params[i].isPrimitive() : !Specs.wrap(params[i]).isAssignableFrom(t)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the static type of an argument, null for a null constant
     */
    private Class<?> typeOf(Object arg) {
        if (arg instanceof Arg.Ref r) {
            Class<?> t = types.get(r.id());
            if (t == null) {
                throw new SpecException("Undefined identifier " + r.id());
            }
            return t;
        } else if (arg instanceof Arg.RefArray a) {
            return a.componentType().arrayType();
        } else if (arg instanceof Arg.RefMatrix a) {
            return a.componentType().arrayType().arrayType();
        } else if (arg instanceof Arg.RefList) {
            return List.class;
        } else if (arg instanceof Arg.Logic) {
            return LogOp.class;
        } else if (arg instanceof Arg.Literal) {
            return Literalizer.class;
        } else if (arg instanceof Arg.Const c) {
            return typeOf(c.rawValue());
        }
        return arg == null ? null : arg.getClass();
    }

    private String describe(Object arg) {
        Class<?> t = typeOf(arg);
        return t == null ? "null" : t.getSimpleName();
    }
}
