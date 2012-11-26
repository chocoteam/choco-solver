/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package generator;

import org.cojen.classfile.*;
import solver.Solver;
import solver.propagation.generator.PropagationStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/07/12
 */
public class MakeQueue {

    public static final String C_GENERATOR = "solver.propagation.generator.Generator";
    public static final String C_ISCHEDULABLE = "solver.propagation.ISchedulable";
    public static final String C_ISCHEDULER = "solver.propagation.IScheduler";
    public static final String C_QUEUE = "solver.propagation.queues.CircularQueue";
    public static final String C_AQUEUE = C_QUEUE;//"solver.propagation.queues.AQueue";
    public static final String C_PROPSTRAT = "solver.propagation.generator.PropagationStrategy";

    public static final String P_DATASTRUCT = "toPropagate";
    public static final String P_LAST = "lastPopped";
    public static final String P_ELEMENTS = "elements";

    public static final String M_ADD_QUEUE = "addLast";
    public static final String M_ADD_STACK = "addLast";
    public static final String M_ENQUE = "enqueue";
    public static final String M_POP = "pollFirst";
    public static final String M_EMPTY = "isEmpty";

    public static RuntimeClassFile createClassFile(String path, String className, boolean stack, boolean clearout) {
        String fullName = path + "." + className;
        RuntimeClassFile cf = new RuntimeClassFile(fullName,
                C_PROPSTRAT,
                PropagationStrategy.class.getClassLoader(), null, true);
        cf.setTarget("1.6");
        cf.setSourceFile(className + ".java");
        cf.setModifiers(Modifiers.PUBLIC.toFinal(true));

        createStaticInitializer(fullName, cf);

        //
        // Create fields
        //

        cf.addField(Modifiers.PROTECTED, P_LAST, TypeDesc.forClass(C_ISCHEDULABLE));

        cf.addField(Modifiers.PROTECTED, P_DATASTRUCT, TypeDesc.forClass(C_AQUEUE));

        cf.addField(Modifiers.NONE.toStatic(true).toFinal(true), "$assertionsDisabled", TypeDesc.BOOLEAN);

        //
        // Create constructors
        //
        // public transient void <init>(solver.propagation.generator.Generator...)
        createConstructor_1(cf);

        // public void <init>(solver.propagation.ISchedulable[])
        createConstructor_2(cf);

        //
        // Create methods
        //

        // public solver.propagation.ISchedulable[] getElements()
        createMethod_1(cf);

        // public void schedule(solver.propagation.ISchedulable)
        createMethod_2(cf, stack);

        // public void remove(solver.propagation.ISchedulable)
        createMethod_3(cf);

        // protected boolean _pickOne()
        if (clearout) {
            createMethod_7(cf);
        } else {
            createMethod_4(cf);
        }

        // public void flush()
        createMethod_8(cf);

        // public boolean isEmpty()
        createMethod_9(cf);

        // public int size()
        createMethod_10(cf);

        return cf;
    }

    private static void createStaticInitializer(String className, ClassFile cf) {
        MethodInfo mi = cf.addInitializer();
        CodeBuilder b = new CodeBuilder(mi);

        b.mapLineNumber(44);
        TypeDesc type_1 = TypeDesc.forClass(className);
        b.loadConstant(type_1);
        b.invokeVirtual("java.lang.Class", "desiredAssertionStatus", TypeDesc.BOOLEAN, null);
        Label label_1 = b.createLabel();
        b.ifZeroComparisonBranch(label_1, "!=");
        b.loadConstant(1);
        Label label_2 = b.createLabel();
        b.branch(label_2);
        label_1.setLocation();
        b.loadConstant(0);
        label_2.setLocation();
        b.storeStaticField("$assertionsDisabled", TypeDesc.BOOLEAN);
        b.returnVoid();
    }

    // public transient void <init>(solver.propagation.generator.Generator...)
    private static void createConstructor_1(ClassFile cf) {
        MethodInfo mi = cf.addConstructor(Modifiers.PUBLIC, new TypeDesc[]{TypeDesc.forClass(C_GENERATOR).toArrayType()});
        CodeBuilder b = new CodeBuilder(mi);

        LocalVariable var_1 = b.getParameter(0);

        b.mapLineNumber(53);
        b.loadThis();
        b.loadLocal(var_1);
        TypeDesc type_2 = TypeDesc.forClass(C_GENERATOR);
        TypeDesc type_1 = type_2.toArrayType();
        TypeDesc[] params_1 = new TypeDesc[]{type_1};
        b.invokeSuperConstructor(params_1);

        b.mapLineNumber(54);
        b.loadConstant(0);
        LocalVariable var_2 = b.createLocalVariable(null, TypeDesc.INT);
        b.storeLocal(var_2);

        b.mapLineNumber(55);
        b.loadConstant(0);
        LocalVariable var_3 = b.createLocalVariable(null, TypeDesc.INT);
        b.storeLocal(var_3);
        Label label_1 = b.createLabel();
        label_1.setLocation();
        b.loadLocal(var_3);
        b.loadThis();
        TypeDesc type_4 = TypeDesc.forClass(C_ISCHEDULABLE);
        TypeDesc type_3 = type_4.toArrayType();
        b.loadField(P_ELEMENTS, type_3);
        b.arrayLength();
        Label label_2 = b.createLabel();
        b.ifComparisonBranch(label_2, ">=");

        b.mapLineNumber(56);
        b.loadThis();
        b.loadField(P_ELEMENTS, type_3);
        b.loadLocal(var_3);
        b.loadFromArray(TypeDesc.OBJECT);
        b.loadThis();
        b.loadLocal(var_3);
        TypeDesc type_5 = TypeDesc.forClass(C_ISCHEDULER);
        TypeDesc[] params_2 = new TypeDesc[]{type_5, TypeDesc.INT};
        b.invokeInterface(C_ISCHEDULABLE, "setScheduler", null, params_2);

        b.mapLineNumber(57);
        b.integerIncrement(var_2, 1);

        b.mapLineNumber(55);
        b.integerIncrement(var_3, 1);
        b.branch(label_1);

        b.mapLineNumber(62);
        label_2.setLocation();
        b.loadThis();
        TypeDesc type_6 = TypeDesc.forClass(C_QUEUE);
        b.newObject(type_6);
        b.dup();
        b.loadLocal(var_2);
        b.loadConstant(2);
        b.math(Opcode.IDIV);
        b.loadConstant(1);
        b.math(Opcode.IADD);
        TypeDesc[] params_3 = new TypeDesc[]{TypeDesc.INT};
        b.invokeConstructor(C_QUEUE, params_3);
        TypeDesc type_7 = TypeDesc.forClass(C_AQUEUE);
        b.storeField(P_DATASTRUCT, type_7);

        b.mapLineNumber(64);
        b.returnVoid();
    }

    // public void <init>(solver.propagation.ISchedulable[])
    private static void createConstructor_2(ClassFile cf) {
        MethodInfo mi = cf.addConstructor(Modifiers.PUBLIC, new TypeDesc[]{TypeDesc.forClass(C_ISCHEDULABLE).toArrayType()});
        CodeBuilder b = new CodeBuilder(mi);

        LocalVariable var_1 = b.getParameter(0);

        b.mapLineNumber(67);
        b.loadThis();
        b.loadLocal(var_1);
        TypeDesc type_2 = TypeDesc.forClass(C_ISCHEDULABLE);
        TypeDesc type_1 = type_2.toArrayType();
        TypeDesc[] params_1 = new TypeDesc[]{type_1};
        b.invokeSuperConstructor(params_1);

        b.mapLineNumber(68);
        b.loadConstant(0);
        LocalVariable var_2 = b.createLocalVariable(null, TypeDesc.INT);
        b.storeLocal(var_2);

        b.mapLineNumber(69);
        b.loadConstant(0);
        LocalVariable var_3 = b.createLocalVariable(null, TypeDesc.INT);
        b.storeLocal(var_3);
        Label label_1 = b.createLabel();
        label_1.setLocation();
        b.loadLocal(var_3);
        b.loadThis();
        b.loadField(P_ELEMENTS, type_1);
        b.arrayLength();
        Label label_2 = b.createLabel();
        b.ifComparisonBranch(label_2, ">=");

        b.mapLineNumber(70);
        b.loadThis();
        b.loadField(P_ELEMENTS, type_1);
        b.loadLocal(var_3);
        b.loadFromArray(TypeDesc.OBJECT);
        b.loadThis();
        b.loadLocal(var_3);
        TypeDesc type_3 = TypeDesc.forClass(C_ISCHEDULER);
        TypeDesc[] params_2 = new TypeDesc[]{type_3, TypeDesc.INT};
        b.invokeInterface(C_ISCHEDULABLE, "setScheduler", null, params_2);

        b.mapLineNumber(71);
        b.integerIncrement(var_2, 1);

        b.mapLineNumber(69);
        b.integerIncrement(var_3, 1);
        b.branch(label_1);

        b.mapLineNumber(77);
        label_2.setLocation();
        b.loadThis();
        TypeDesc type_4 = TypeDesc.forClass(C_QUEUE);
        b.newObject(type_4);
        b.dup();
        b.loadLocal(var_2);
        b.loadConstant(2);
        b.math(Opcode.IDIV);
        b.loadConstant(1);
        b.math(Opcode.IADD);
        TypeDesc[] params_3 = new TypeDesc[]{TypeDesc.INT};
        b.invokeConstructor(C_QUEUE, params_3);
        TypeDesc type_5 = TypeDesc.forClass(C_AQUEUE);
        b.storeField(P_DATASTRUCT, type_5);

        b.mapLineNumber(79);
        b.returnVoid();
    }

    // public solver.propagation.ISchedulable[] getElements()
    private static void createMethod_1(ClassFile cf) {
        MethodInfo mi = cf.addMethod(Modifiers.PUBLIC, "getElements", TypeDesc.forClass(C_ISCHEDULABLE).toArrayType(), null);
        CodeBuilder b = new CodeBuilder(mi);

        b.mapLineNumber(84);
        b.loadConstant(1);
        TypeDesc type_2 = TypeDesc.forClass(C_ISCHEDULABLE);
        TypeDesc type_1 = type_2.toArrayType();
        b.newObject(type_1);
        b.dup();
        b.loadConstant(0);
        b.loadThis();
        b.storeToArray(TypeDesc.OBJECT);
        b.checkCast(type_1);
        b.returnValue(TypeDesc.OBJECT);
    }

    // public void schedule(solver.propagation.ISchedulable)
    private static void createMethod_2(ClassFile cf, boolean stack) {
        MethodInfo mi = cf.addMethod(Modifiers.PUBLIC, "schedule", null, new TypeDesc[]{TypeDesc.forClass(C_ISCHEDULABLE)});
        CodeBuilder b = new CodeBuilder(mi);

        LocalVariable var_1 = b.getParameter(0);

        b.mapLineNumber(92);
        b.loadStaticField("$assertionsDisabled", TypeDesc.BOOLEAN);
        Label label_1 = b.createLabel();
        b.ifZeroComparisonBranch(label_1, "!=");
        b.loadLocal(var_1);
        b.invokeInterface(C_ISCHEDULABLE, "enqueued", TypeDesc.BOOLEAN, null);
        b.ifZeroComparisonBranch(label_1, "==");
        TypeDesc type_1 = TypeDesc.forClass("java.lang.AssertionError");
        b.newObject(type_1);
        b.dup();
        b.invokeConstructor("java.lang.AssertionError", null);
        b.throwObject();

        b.mapLineNumber(93);
        label_1.setLocation();
        b.loadThis();
        TypeDesc type_2 = TypeDesc.forClass(C_AQUEUE);
        b.loadField(P_DATASTRUCT, type_2);
        b.loadLocal(var_1);
        TypeDesc[] params_1 = new TypeDesc[]{TypeDesc.OBJECT};
        b.invokeVirtual(C_AQUEUE, stack ? M_ADD_STACK : M_ADD_QUEUE, TypeDesc.BOOLEAN, params_1);
        b.pop();

        b.mapLineNumber(94);
        b.loadLocal(var_1);
        b.invokeInterface(C_ISCHEDULABLE, M_ENQUE, null, null);

        b.mapLineNumber(95);
        b.loadThis();
        b.loadField("enqueued", TypeDesc.BOOLEAN);
        Label label_2 = b.createLabel();
        b.ifZeroComparisonBranch(label_2, "!=");

        b.mapLineNumber(96);
        b.loadThis();
        TypeDesc type_3 = TypeDesc.forClass(C_ISCHEDULER);
        b.loadField("scheduler", type_3);
        b.loadThis();
        TypeDesc type_4 = TypeDesc.forClass(C_ISCHEDULABLE);
        TypeDesc[] params_2 = new TypeDesc[]{type_4};
        b.invokeInterface(C_ISCHEDULER, "schedule", null, params_2);

        b.mapLineNumber(98);
        label_2.setLocation();
        b.returnVoid();
    }

    // public void remove(solver.propagation.ISchedulable)
    private static void createMethod_3(ClassFile cf) {
        MethodInfo mi = cf.addMethod(Modifiers.PUBLIC, "remove", null, new TypeDesc[]{TypeDesc.forClass(C_ISCHEDULABLE)});
        CodeBuilder b = new CodeBuilder(mi);

        LocalVariable var_1 = b.getParameter(0);

        b.mapLineNumber(102);
        b.loadThis();
        TypeDesc type_1 = TypeDesc.forClass(C_AQUEUE);
        b.loadField(P_DATASTRUCT, type_1);
        b.loadLocal(var_1);
        TypeDesc[] params_1 = new TypeDesc[]{TypeDesc.OBJECT};
        b.invokeVirtual(C_AQUEUE, "remove", TypeDesc.BOOLEAN, params_1);
        b.pop();

        b.mapLineNumber(103);
        b.loadLocal(var_1);
        b.invokeInterface(C_ISCHEDULABLE, "deque", null, null);

        b.mapLineNumber(104);
        b.loadThis();
        b.loadField("enqueued", TypeDesc.BOOLEAN);
        Label label_1 = b.createLabel();
        b.ifZeroComparisonBranch(label_1, "==");
        b.loadThis();
        b.loadField(P_DATASTRUCT, type_1);
        b.invokeVirtual(C_AQUEUE, M_EMPTY, TypeDesc.BOOLEAN, null);
        b.ifZeroComparisonBranch(label_1, "==");

        b.mapLineNumber(105);
        b.loadThis();
        TypeDesc type_2 = TypeDesc.forClass(C_ISCHEDULER);
        b.loadField("scheduler", type_2);
        b.loadThis();
        TypeDesc type_3 = TypeDesc.forClass(C_ISCHEDULABLE);
        TypeDesc[] params_2 = new TypeDesc[]{type_3};
        b.invokeInterface(C_ISCHEDULER, "remove", null, params_2);

        b.mapLineNumber(107);
        label_1.setLocation();
        b.returnVoid();
    }

    // protected boolean _pickOne()
    private static void createMethod_4(ClassFile cf) {
        MethodInfo mi = cf.addMethod(Modifiers.PUBLIC, "execute", TypeDesc.BOOLEAN, null);
        mi.addException(TypeDesc.forClass("solver.exception.ContradictionException"));
        CodeBuilder b = new CodeBuilder(mi);

        b.mapLineNumber(111);
        b.loadThis();
        TypeDesc type_1 = TypeDesc.forClass(C_AQUEUE);
        b.loadField(P_DATASTRUCT, type_1);
        b.invokeVirtual(C_AQUEUE, M_EMPTY, TypeDesc.BOOLEAN, null);
        Label label_1 = b.createLabel();
        b.ifZeroComparisonBranch(label_1, "!=");

        b.mapLineNumber(112);
        b.loadThis();
        b.loadThis();
        b.loadField(P_DATASTRUCT, type_1);
        b.invokeVirtual(C_AQUEUE, M_POP, TypeDesc.OBJECT, null);
        TypeDesc type_2 = TypeDesc.forClass(C_ISCHEDULABLE);
        b.checkCast(type_2);
        b.storeField(P_LAST, type_2);

        b.mapLineNumber(113);
        b.loadThis();
        b.loadField(P_LAST, type_2);
        b.invokeInterface(C_ISCHEDULABLE, "deque", null, null);

        b.mapLineNumber(114);
        b.loadThis();
        b.loadField(P_LAST, type_2);
        b.invokeInterface(C_ISCHEDULABLE, "execute", TypeDesc.BOOLEAN, null);
        b.ifZeroComparisonBranch(label_1, "!=");
        b.loadThis();
        b.loadField(P_LAST, type_2);
        b.invokeInterface(C_ISCHEDULABLE, "enqueued", TypeDesc.BOOLEAN, null);
        b.ifZeroComparisonBranch(label_1, "!=");

        b.mapLineNumber(115);
        b.loadThis();
        b.loadThis();
        b.loadField(P_LAST, type_2);
        TypeDesc[] params_1 = new TypeDesc[]{type_2};
        b.invokeVirtual("schedule", null, params_1);

        b.mapLineNumber(118);
        label_1.setLocation();
        b.loadThis();
        b.loadField(P_DATASTRUCT, type_1);
        b.invokeVirtual(C_AQUEUE, M_EMPTY, TypeDesc.BOOLEAN, null);
        b.returnValue(TypeDesc.INT);
    }

    // protected boolean _clearOut()
    private static void createMethod_7(ClassFile cf) {
        MethodInfo mi = cf.addMethod(Modifiers.PUBLIC, "execute", TypeDesc.BOOLEAN, null);
        mi.addException(TypeDesc.forClass("solver.exception.ContradictionException"));
        MyCodeBuilder b = new MyCodeBuilder(mi);

        b.mapLineNumber(132);
        Label label_1 = b.createLabel();
        label_1.setLocation();
        b.loadThis();
        TypeDesc type_1 = TypeDesc.forClass(C_AQUEUE);
        b.loadField(P_DATASTRUCT, type_1);
        b.invokeVirtual(C_AQUEUE, M_EMPTY, TypeDesc.BOOLEAN, null);
        Label label_2 = b.createLabel();
        b.ifZeroComparisonBranch(label_2, "!=");

        b.mapLineNumber(133);
        b.loadThis();
        b.loadThis();
        b.loadField(P_DATASTRUCT, type_1);
        b.invokeVirtual(C_AQUEUE, M_POP, TypeDesc.OBJECT, null);
        TypeDesc type_2 = TypeDesc.forClass(C_ISCHEDULABLE);
        b.checkCast(type_2);
        b.storeField(P_LAST, type_2);

        b.mapLineNumber(134);
        b.loadThis();
        b.loadField(P_LAST, type_2);
        b.invokeInterface(C_ISCHEDULABLE, "deque", null, null);

        b.mapLineNumber(135);
        //b.loadThis();
        //b.loadField(P_LAST, type_2);
        Solver solver;
//        b.inline(new Arc<V>(null,null, 0, solver = new Solver(),
//                new PropagationEngine(solver)),"execute");
        //b.invokeInterface(C_ISCHEDULABLE, "execute", TypeDesc.BOOLEAN, null);
        b.ifZeroComparisonBranch(label_1, "!=");
        b.loadThis();
        b.loadField(P_LAST, type_2);
        b.invokeInterface(C_ISCHEDULABLE, "enqueued", TypeDesc.BOOLEAN, null);
        b.ifZeroComparisonBranch(label_1, "!=");

        b.mapLineNumber(136);
        b.loadThis();
        b.loadThis();
        b.loadField(P_LAST, type_2);
        TypeDesc[] params_1 = new TypeDesc[]{type_2};
        b.invokeVirtual("schedule", null, params_1);
        b.branch(label_1);

        b.mapLineNumber(139);
        label_2.setLocation();
        b.loadConstant(1);
        b.returnValue(TypeDesc.INT);
    }

    // public void flush()
    private static void createMethod_8(ClassFile cf) {
        MethodInfo mi = cf.addMethod(Modifiers.PUBLIC, "flush", null, null);
        CodeBuilder b = new CodeBuilder(mi);

        b.mapLineNumber(144);
        b.loadThis();
        TypeDesc type_1 = TypeDesc.forClass(C_ISCHEDULABLE);
        b.loadField(P_LAST, type_1);
        Label label_1 = b.createLabel();
        b.ifNullBranch(label_1, true);

        b.mapLineNumber(145);
        b.loadThis();
        b.loadField(P_LAST, type_1);
        b.invokeInterface(C_ISCHEDULABLE, "flush", null, null);

        b.mapLineNumber(147);
        label_1.setLocation();
        b.loadThis();
        TypeDesc type_2 = TypeDesc.forClass(C_AQUEUE);
        b.loadField(P_DATASTRUCT, type_2);
        b.invokeVirtual(C_AQUEUE, M_EMPTY, TypeDesc.BOOLEAN, null);
        Label label_2 = b.createLabel();
        b.ifZeroComparisonBranch(label_2, "!=");

        b.mapLineNumber(148);
        b.loadThis();
        b.loadThis();
        b.loadField(P_DATASTRUCT, type_2);
        b.invokeVirtual(C_AQUEUE, M_POP, TypeDesc.OBJECT, null);
        b.checkCast(type_1);
        b.storeField(P_LAST, type_1);

        b.mapLineNumber(150);
        b.loadThis();
        b.loadField(P_LAST, type_1);
        b.invokeInterface(C_ISCHEDULABLE, "flush", null, null);

        b.mapLineNumber(152);
        b.loadThis();
        b.loadField(P_LAST, type_1);
        b.invokeInterface(C_ISCHEDULABLE, "deque", null, null);
        b.branch(label_1);

        b.mapLineNumber(154);
        label_2.setLocation();
        b.returnVoid();
    }

    // public boolean isEmpty()
    private static void createMethod_9(ClassFile cf) {
        MethodInfo mi = cf.addMethod(Modifiers.PUBLIC, M_EMPTY, TypeDesc.BOOLEAN, null);
        CodeBuilder b = new CodeBuilder(mi);

        b.mapLineNumber(158);
        b.loadThis();
        TypeDesc type_1 = TypeDesc.forClass(C_AQUEUE);
        b.loadField(P_DATASTRUCT, type_1);
        b.invokeVirtual(C_AQUEUE, M_EMPTY, TypeDesc.BOOLEAN, null);
        b.returnValue(TypeDesc.INT);
    }

    // public int size()
    private static void createMethod_10(ClassFile cf) {
        MethodInfo mi = cf.addMethod(Modifiers.PUBLIC, "size", TypeDesc.INT, null);
        CodeBuilder b = new CodeBuilder(mi);

        b.mapLineNumber(163);
        b.loadThis();
        TypeDesc type_1 = TypeDesc.forClass(C_AQUEUE);
        b.loadField(P_DATASTRUCT, type_1);
        b.invokeVirtual(C_AQUEUE, "size", TypeDesc.INT, null);
        b.returnValue(TypeDesc.INT);
    }

    // public solver.propagation.generator.Queue duplicate()
    private static void createMethod_11(String className, ClassFile cf) {
        MethodInfo mi = cf.addMethod(Modifiers.PUBLIC, "duplicate", TypeDesc.forClass(className), null);
        CodeBuilder b = new CodeBuilder(mi);

        b.mapLineNumber(171);
        TypeDesc type_1 = TypeDesc.forClass(className);
        b.newObject(type_1);
        b.dup();
        b.loadConstant(0);
        TypeDesc type_3 = TypeDesc.forClass(C_GENERATOR);
        TypeDesc type_2 = type_3.toArrayType();
        b.newObject(type_2);
        TypeDesc[] params_1 = new TypeDesc[]{type_2};
        b.invokeConstructor(params_1);
        b.returnValue(TypeDesc.OBJECT);
    }

    // public volatile solver.propagation.generator.PropagationStrategy duplicate()
    private static void createMethod_12(String className, ClassFile cf) {
        MethodInfo mi = cf.addMethod(Modifiers.PUBLIC.toVolatile(true), "duplicate", TypeDesc.forClass(C_PROPSTRAT), null);
        CodeBuilder b = new CodeBuilder(mi);

        b.mapLineNumber(44);
        b.loadThis();
        TypeDesc type_1 = TypeDesc.forClass(className);
        b.invokeVirtual("duplicate", type_1, null);
        b.returnValue(TypeDesc.OBJECT);
    }


    public static class MyCodeBuilder extends CodeBuilder {

        public MyCodeBuilder(MethodInfo info) {
            super(info);
        }

        public MyCodeBuilder(MethodInfo info, boolean saveLineNumberInfo, boolean saveLocalVariableInfo) {
            super(info, saveLineNumberInfo, saveLocalVariableInfo);
        }

        public void inline(Object code, String mName) {
            // First load the class for the inlined code.

            Class codeClass = code.getClass();
            String className = codeClass.getName().replace('.', '/') + ".class";
            ClassLoader loader = codeClass.getClassLoader();

            InputStream in;
            if (loader == null) {
                in = ClassLoader.getSystemResourceAsStream(className);
            } else {
                in = loader.getResourceAsStream(className);
            }

            if (in == null) {
                throw new MissingResourceException("Unable to find class file", className, null);
            }

            ClassFile cf;
            try {
                cf = ClassFile.readFrom(in);
            } catch (IOException e) {
                MissingResourceException e2 = new MissingResourceException
                        ("Error loading class file: " + e.getMessage(), className, null);
                try {
                    e2.initCause(e);
                } catch (NoSuchMethodError e3) {
                }
                throw e2;
            }

            // Now find the single "define" method.
            MethodInfo defineMethod = null;

            MethodInfo[] methods = cf.getMethods();
            for (int i = 0; i < methods.length; i++) {
                MethodInfo method = methods[i];
                if (mName.equals(method.getName())) {
                    if (defineMethod != null) {
                        throw new IllegalArgumentException("Multiple " + mName + " methods found");
                    } else {
                        defineMethod = method;
                    }
                }
            }

            if (defineMethod == null) {
                throw new IllegalArgumentException("No " + mName + " method found");
            }

            // Copy stack arguments to expected local variables.
            TypeDesc[] paramTypes = defineMethod.getMethodDescriptor().getParameterTypes();
            LocalVariable[] paramVars = new LocalVariable[paramTypes.length];
            for (int i = paramVars.length; --i >= 0; ) {
                LocalVariable paramVar = createLocalVariable(paramTypes[i]);
                storeLocal(paramVar);
                paramVars[i] = paramVar;
            }

            Label returnLocation = createLabel();
            CodeDisassembler cd = new CodeDisassembler(defineMethod);
            cd.disassemble(this, paramVars, returnLocation);
            returnLocation.setLocation();
        }
    }
}
