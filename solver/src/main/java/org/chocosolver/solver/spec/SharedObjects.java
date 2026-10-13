/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.nary.automata.FA.utils.ICounter;

import java.lang.invoke.MethodHandle;
import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Diagnostic tool: finds the objects shared by two object graphs (typically, two models instantiated from the same
 * {@link ModelSpec}), which must be immutable for the models to be solved concurrently.
 * <p>
 * Objects are compared by identity. The traversal follows instance fields (static fields are ignored), array
 * elements, the elements of collections and maps, and referents. Objects of classes which cannot be introspected
 * (e.g., of the JDK) are compared but not traversed.
 * <p>
 * Are neither reported nor traversed: immutable objects (classes, methods, the values shared according to the policy
 * of {@link Values}), and the data shared by design ({@link Settings}, {@link ICounter}, {@link FactoryMethod}, frozen
 * tuples and diagrams). What the latter own is not reported either, even when reached by another path.
 *
 * @author Charles Prud'homme
 */
public final class SharedObjects {

    private static final Map<Class<?>, Field[]> FIELDS = new ConcurrentHashMap<>();

    private SharedObjects() {
    }

    /**
     * A shared object.
     *
     * @param type type of the object
     * @param path a path to the object from the root of the second graph
     */
    public record Shared(Class<?> type, String path) {
        @Override
        public String toString() {
            return type.getName() + " at " + path;
        }
    }

    /**
     * @param a       root of the first graph
     * @param b       root of the second graph
     * @param allowed types whose instances may be shared (neither reported nor traversed)
     * @return the mutable objects reachable from both <i>a</i> and <i>b</i>, one example per type
     */
    public static List<Shared> between(Object a, Object b, Set<Class<?>> allowed) {
        return between(a, b, allowed, List.of());
    }

    /**
     * @param a        root of the first graph
     * @param b        root of the second graph
     * @param allowed  types whose instances may be shared (neither reported nor traversed)
     * @param byDesign objects which may be shared, e.g. the constants of a {@link ModelSpec} (see
     *                 {@link #constantsOf(ModelSpec)}): neither they nor what they own are reported
     * @return the mutable objects reachable from both <i>a</i> and <i>b</i>, one example per type
     */
    public static List<Shared> between(Object a, Object b, Set<Class<?>> allowed, Collection<?> byDesign) {
        Set<Object> common = common(a, b, allowed, byDesign);
        if (common.isEmpty()) {
            return List.of();
        }
        // a path to one shared object per type
        Map<Class<?>, Shared> shared = new LinkedHashMap<>();
        traverse(b, allowed, identitySet(), identitySet(), (o, node) -> {
            if (common.contains(o)) {
                shared.putIfAbsent(o.getClass(), new Shared(o.getClass(), node.path()));
            }
        }, true);
        return new ArrayList<>(shared.values());
    }

    /**
     * @return the mutable objects reachable from both <i>a</i> and <i>b</i>, but those shared by design and what they
     * own (see {@link #between(Object, Object, Set, Collection)})
     */
    public static Set<Object> common(Object a, Object b, Set<Class<?>> allowed, Collection<?> byDesign) {
        Set<Object> designed = identitySet();
        designed.addAll(byDesign);
        // no path kept, to save memory
        Set<Object> fromA = identitySet();
        traverse(a, allowed, fromA, designed, null, false);
        Set<Object> common = identitySet();
        traverse(b, allowed, identitySet(), designed, (o, node) -> {
            if (fromA.contains(o)) {
                common.add(o);
            }
        }, false);
        fromA.clear();
        // what the data shared by design own may be reached by other paths (e.g., the matrix of frozen tuples
        // referenced by a propagator): it is shared by design too
        if (!common.isEmpty()) {
            Set<Object> owned = identitySet();
            for (Object d : designed) {
                traverse(d, null, owned, null, null, false);
            }
            common.removeAll(owned);
        }
        return common;
    }

    /**
     * @return the constants of <i>spec</i>, which are shared by design by its instances
     */
    public static List<Object> constantsOf(ModelSpec spec) {
        List<Object> constants = new ArrayList<>();
        for (Step s : spec.steps()) {
            if (s instanceof Step.Call c) {
                for (Arg arg : c.rawArgs()) {
                    if (arg instanceof Arg.Const k && k.rawValue() != null) {
                        constants.add(k.rawValue());
                    }
                }
            }
        }
        return constants;
    }

    private static Set<Object> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    private record Node(Object object, Node parent, String edge) {
        String path() {
            return parent == null ? edge : parent.path() + edge;
        }
    }

    /**
     * Traverse the graph of <i>root</i>. When <i>allowed</i> is not null, immutable objects are skipped; when
     * <i>byDesign</i> is not null, the data shared by design are skipped and collected in it. Paths from the root
     * are kept only if <i>paths</i> is set.
     */
    private static void traverse(Object root, Set<Class<?>> allowed, Set<Object> seen, Set<Object> byDesign,
                                 BiConsumer<Object, Node> visitor, boolean paths) {
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(new Node(root, null, root.getClass().getSimpleName()));
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            Object o = node.object;
            if (o == null || !seen.add(o)) {
                continue;
            }
            if (allowed != null && isImmutable(o, allowed)) {
                continue;
            }
            if (byDesign != null && (byDesign.contains(o) || isSharedByDesign(o))) {
                byDesign.add(o);
                continue;
            }
            if (visitor != null) {
                visitor.accept(o, node);
            }
            Node parent = paths ? node : null;
            Class<?> c = o.getClass();
            if (c.isArray()) {
                if (!c.getComponentType().isPrimitive()) {
                    int n = Array.getLength(o);
                    for (int i = 0; i < n; i++) {
                        stack.push(new Node(Array.get(o, i), parent, paths ? "[" + i + "]" : null));
                    }
                }
            } else if (o instanceof Collection<?> col) {
                try {
                    for (Object e : col) {
                        stack.push(new Node(e, parent, paths ? "{}" : null));
                    }
                } catch (RuntimeException ignored) {
                    // concurrent or lazy collection: elements not traversed
                }
            } else if (o instanceof Reference<?> ref) {
                stack.push(new Node(ref.get(), parent, paths ? ".get()" : null));
            } else if (o instanceof Map<?, ?> map) {
                try {
                    for (Map.Entry<?, ?> e : map.entrySet()) {
                        stack.push(new Node(e.getKey(), parent, paths ? "{key}" : null));
                        stack.push(new Node(e.getValue(), parent, paths ? "{" + e.getKey() + "}" : null));
                    }
                } catch (RuntimeException ignored) {
                    // elements not traversed
                }
            } else {
                for (Field f : fields(c)) {
                    try {
                        stack.push(new Node(f.get(o), parent, paths ? "." + f.getName() : null));
                    } catch (IllegalAccessException ignored) {
                        // not traversed
                    }
                }
            }
        }
    }

    private static boolean isSharedByDesign(Object o) {
        return o instanceof Settings || o instanceof ICounter || o instanceof FactoryMethod || Values.isShared(o);
    }

    private static boolean isImmutable(Object o, Set<Class<?>> allowed) {
        if (o instanceof String || o instanceof Number || o instanceof Boolean || o instanceof Character
                || o instanceof Enum || o instanceof Class || o instanceof Method || o instanceof MethodHandle) {
            return true;
        }
        for (Class<?> a : allowed) {
            if (a.isInstance(o)) {
                return true;
            }
        }
        return !(o instanceof Collection) && !(o instanceof Map) && Specs.isStateless(o.getClass());
    }

    /**
     * @return the accessible instance fields of <i>c</i> and its superclasses
     */
    private static Field[] fields(Class<?> c) {
        return FIELDS.computeIfAbsent(c, k -> {
            List<Field> list = new ArrayList<>();
            for (Class<?> x = k; x != null; x = x.getSuperclass()) {
                for (Field f : x.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive() && f.trySetAccessible()) {
                        list.add(f);
                    }
                }
            }
            return list.toArray(new Field[0]);
        });
    }
}
