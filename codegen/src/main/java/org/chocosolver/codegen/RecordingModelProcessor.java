/*
 * This file is part of choco-codegen, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.codegen;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Generates the recording subclass of a class annotated with {@code org.chocosolver.solver.spec.GenerateRecordingModel}.
 * <p>
 * The generated class extends the annotated class and overrides every default method of the factory interface
 * (but the excluded ones) as follows:
 * <pre>{@code
 * public Constraint arithm(IntVar p0, String p1, int p2) {
 *     if (!topLevel()) {
 *         return super.arithm(p0, p1, p2);
 *     }
 *     return getRecorder().call(M[i], () -> super.arithm(p0, p1, p2), new Object[]{p0, p1, p2});
 * }
 * }</pre>
 * The annotated class must provide {@code topLevel()} and {@code getRecorder()}; the recorder must provide
 * {@code call(FactoryMethod, Supplier, Object[])} and {@code run(FactoryMethod, Runnable, Object[])}.
 * The generated class also declares a public constructor for each non-private constructor of the annotated class.
 *
 * @author Charles Prud'homme
 */
@SupportedAnnotationTypes(RecordingModelProcessor.ANNOTATION)
public class RecordingModelProcessor extends AbstractProcessor {

    static final String ANNOTATION = "org.chocosolver.solver.spec.GenerateRecordingModel";

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (TypeElement annotation : annotations) {
            for (Element e : env.getElementsAnnotatedWith(annotation)) {
                if (e.getKind() != ElementKind.CLASS) {
                    error(e, "@GenerateRecordingModel only applies to classes");
                    continue;
                }
                try {
                    generate((TypeElement) e, attributes(e, annotation));
                } catch (IOException ex) {
                    error(e, "Cannot generate the recording model: " + ex.getMessage());
                }
            }
        }
        return true;
    }

    private void error(Element e, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, e);
    }

    /**
     * @return the attributes of the annotation, with default values
     */
    private Map<String, Object> attributes(Element e, TypeElement annotation) {
        for (AnnotationMirror am : e.getAnnotationMirrors()) {
            if (am.getAnnotationType().asElement().equals(annotation)) {
                return processingEnv.getElementUtils().getElementValuesWithDefaults(am).entrySet().stream()
                        .collect(Collectors.toMap(v -> v.getKey().getSimpleName().toString(), v -> v.getValue().getValue()));
            }
        }
        throw new IllegalStateException("annotation not found on " + e);
    }

    private void generate(TypeElement annotated, Map<String, Object> attributes) throws IOException {
        TypeElement factories = (TypeElement) ((DeclaredType) attributes.get("factories")).asElement();
        String name = (String) attributes.get("name");
        Set<String> excluded = new HashSet<>();
        for (Object v : (List<?>) attributes.get("excluded")) {
            excluded.add((String) ((AnnotationValue) v).getValue());
        }
        String pkg = ((PackageElement) annotated.getEnclosingElement()).getQualifiedName().toString();

        List<ExecutableElement> methods = new ArrayList<>();
        for (ExecutableElement m : ElementFilter.methodsIn(processingEnv.getElementUtils().getAllMembers(factories))) {
            Set<Modifier> mods = m.getModifiers();
            if (mods.contains(Modifier.DEFAULT) && !mods.contains(Modifier.STATIC)
                    && !excluded.contains(m.getSimpleName().toString())) {
                methods.add(m);
            }
        }
        methods.sort(Comparator.comparing(this::signature));

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n")
                .append("/**\n")
                .append(" * Recording subclass of {@link ").append(annotated.getSimpleName()).append("}: overrides the ")
                .append(methods.size()).append(" factory methods of\n")
                .append(" * {@link ").append(factories.getQualifiedName()).append("} to journalize them.\n")
                .append(" * <p>\n")
                .append(" * GENERATED at compile time by {@code ").append(getClass().getName()).append("}.\n")
                .append(" */\n")
                .append("public class ").append(name).append(" extends ").append(annotated.getSimpleName()).append(" {\n\n")
                .append("    /**\n     * Names of the factory methods which are not journalized (no effect on the model).\n     */\n")
                .append("    public static final java.util.Set<String> EXCLUDED = java.util.Set.of(")
                .append(excluded.stream().sorted().map(e -> "\"" + e + "\"").collect(Collectors.joining(", ")))
                .append(");\n\n")
                .append("    private static final FactoryMethod[] M = new FactoryMethod[").append(methods.size()).append("];\n\n")
                .append("    static {\n");
        for (int i = 0; i < methods.size(); i++) {
            ExecutableElement m = methods.get(i);
            sb.append("        M[").append(i).append("] = FactoryMethod.of(\"").append(m.getSimpleName()).append('"');
            for (VariableElement p : m.getParameters()) {
                sb.append(", ").append(erasure(p.asType())).append(".class");
            }
            sb.append(");\n");
        }
        sb.append("    }\n");

        for (ExecutableElement c : ElementFilter.constructorsIn(annotated.getEnclosedElements())) {
            if (c.getModifiers().contains(Modifier.PRIVATE)) {
                continue;
            }
            sb.append("\n    public ").append(name).append('(').append(parameters(c)).append(") {\n")
                    .append("        super(").append(names(c)).append(");\n")
                    .append("    }\n");
        }

        for (int i = 0; i < methods.size(); i++) {
            ExecutableElement m = methods.get(i);
            boolean isVoid = m.getReturnType().getKind() == TypeKind.VOID;
            String superCall = "super." + m.getSimpleName() + "(" + names(m) + ")";
            String args = "new Object[]{" + names(m) + "}";
            sb.append("\n    @Override\n")
                    .append("    public ").append(typeParameters(m)).append(m.getReturnType()).append(' ')
                    .append(m.getSimpleName())
                    .append('(').append(parameters(m)).append(") {\n")
                    .append("        if (!topLevel()) {\n");
            if (isVoid) {
                sb.append("            ").append(superCall).append(";\n")
                        .append("            return;\n")
                        .append("        }\n")
                        .append("        getRecorder().run(M[").append(i).append("], () -> ").append(superCall)
                        .append(", ").append(args).append(");\n");
            } else {
                sb.append("            return ").append(superCall).append(";\n")
                        .append("        }\n")
                        .append("        return getRecorder().call(M[").append(i).append("], () -> ").append(superCall)
                        .append(", ").append(args).append(");\n");
            }
            sb.append("    }\n");
        }
        sb.append("}\n");

        try (Writer w = processingEnv.getFiler().createSourceFile(pkg + "." + name, annotated).openWriter()) {
            w.write(sb.toString());
        }
    }

    private String erasure(TypeMirror t) {
        return processingEnv.getTypeUtils().erasure(t).toString();
    }

    private String signature(ExecutableElement m) {
        return m.getSimpleName() + m.getParameters().stream().map(p -> erasure(p.asType()))
                .collect(Collectors.joining(",", "(", ")"));
    }

    /**
     * @return the declaration of the parameters of <i>m</i>, named p0, p1, ...
     */
    private static String parameters(ExecutableElement m) {
        List<? extends VariableElement> params = m.getParameters();
        return IntStream.range(0, params.size()).mapToObj(k -> {
            String t = params.get(k).asType().toString();
            if (m.isVarArgs() && k == params.size() - 1) {
                t = t.substring(0, t.length() - 2) + "...";
            }
            return t + " p" + k;
        }).collect(Collectors.joining(", "));
    }

    /**
     * @return the declaration of the type parameters of <i>m</i>, e.g. {@code <V extends Variable> }, or an empty
     * string
     */
    private static String typeParameters(ExecutableElement m) {
        if (m.getTypeParameters().isEmpty()) {
            return "";
        }
        return m.getTypeParameters().stream().map(tp -> {
            List<String> bounds = tp.getBounds().stream().map(TypeMirror::toString)
                    .filter(b -> !b.equals("java.lang.Object")).toList();
            return bounds.isEmpty() ? tp.toString() : tp + " extends " + String.join(" & ", bounds);
        }).collect(Collectors.joining(", ", "<", "> "));
    }

    private static String names(ExecutableElement m) {
        return IntStream.range(0, m.getParameters().size()).mapToObj(k -> "p" + k).collect(Collectors.joining(", "));
    }
}
