/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.IModel;
import org.chocosolver.solver.Model;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A factory method of {@link IModel}, identified by its exact signature (the overload is never ambiguous).
 * It is replayed on a {@link Model} through a {@link MethodHandle}.
 * <p>
 * Instances are immutable and can be shared among threads.
 *
 * @author Charles Prud'homme
 */
public final class FactoryMethod {

    private static final ConcurrentHashMap<Method, FactoryMethod> CACHE = new ConcurrentHashMap<>();

    private final Method method;
    private final Class<?>[] types;
    /**
     * (Model, Object[]) -> Object: the arguments are spread, the result boxed (null for void).
     */
    private final MethodHandle invoker;

    private FactoryMethod(Method method) {
        if (Modifier.isStatic(method.getModifiers())
                || !method.getDeclaringClass().isInterface()
                || !method.getDeclaringClass().isAssignableFrom(IModel.class)) {
            throw new SpecException(method + " is not a factory method of IModel");
        }
        this.method = method;
        this.types = method.getParameterTypes();
        try {
            // fixed arity: a varargs parameter receives the array built from the spec, as is
            this.invoker = MethodHandles.publicLookup().unreflect(method).asFixedArity()
                    .asSpreader(Object[].class, types.length)
                    .asType(MethodType.methodType(Object.class, Model.class, Object[].class));
        } catch (IllegalAccessException e) {
            throw new SpecException("Cannot access " + method, e);
        }
    }

    /**
     * @param method a (non-static) method declared by one of the factory interfaces of {@link IModel}
     * @return the corresponding factory method
     * @throws SpecException if <i>method</i> is not a factory method
     */
    public static FactoryMethod of(Method method) {
        return CACHE.computeIfAbsent(method, FactoryMethod::new);
    }

    /**
     * @param name       name of the factory method
     * @param paramTypes exact parameter types, which select the overload
     * @return the corresponding factory method
     * @throws SpecException if no such factory method exists
     */
    public static FactoryMethod of(String name, Class<?>... paramTypes) {
        try {
            return of(IModel.class.getMethod(name, paramTypes));
        } catch (NoSuchMethodException e) {
            throw new SpecException("No factory method " + signature(name, paramTypes), e);
        }
    }

    public String name() {
        return method.getName();
    }

    public Class<?>[] parameterTypes() {
        return types.clone();
    }

    Class<?>[] rawParameterTypes() {
        return types;
    }

    public int parameterCount() {
        return types.length;
    }

    public Class<?> returnType() {
        return method.getReturnType();
    }

    Object invoke(Model target, Object[] args) throws Throwable {
        return (Object) invoker.invokeExact(target, args);
    }

    private static String signature(String name, Class<?>[] types) {
        return name + Arrays.stream(types).map(Class::getSimpleName).collect(Collectors.joining(", ", "(", ")"));
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof FactoryMethod f && method.equals(f.method));
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public String toString() {
        return signature(method.getName(), types);
    }
}
