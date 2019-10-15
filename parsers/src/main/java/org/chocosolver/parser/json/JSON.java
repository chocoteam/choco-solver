/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import org.chocosolver.parser.json.constraints.ConstraintDeserializer;
import org.chocosolver.parser.json.variables.IntVarDeserializer;
import org.chocosolver.parser.json.variables.RealVarDeserializer;
import org.chocosolver.parser.json.variables.SetVarDeserializer;
import org.chocosolver.parser.json.variables.views.IntViewDeserializer;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.view.IView;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.writer.util.IntSetlSerializer;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility class to ease the serialization and the deserialization of choco model entities.
 * Solver part is not supported.
 *
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @author Fabien Hermenier
 * @since 12/09/2017.
 */
public class JSON {

    @SuppressWarnings("WeakerAccess")
    public static final GsonBuilder gbuilder = new GsonBuilder();

    static {
        // int set
        gbuilder.registerTypeAdapter(IntIterableRangeSet.class, new IntSetlSerializer());
        // model
        gbuilder.registerTypeAdapter(Model.class, new ModelDeserializer());
        // BoolVar and IntVar
        gbuilder.registerTypeHierarchyAdapter(IntVar.class, new IntVarDeserializer());
        // SetVar
        gbuilder.registerTypeHierarchyAdapter(SetVar.class, new SetVarDeserializer());
        // RealVar
        gbuilder.registerTypeHierarchyAdapter(RealVar.class, new RealVarDeserializer());
        // views
        gbuilder.registerTypeHierarchyAdapter(IView.class, new IntViewDeserializer());

        // constraints
        gbuilder.registerTypeHierarchyAdapter(Constraint.class, new ConstraintDeserializer());

        gbuilder.setPrettyPrinting();
        gbuilder.disableHtmlEscaping();
    }

    private JSON() {
    }

    private static InputStreamReader makeIn(File f) throws IOException {
        if (f.getName().endsWith(".gz")) {
            return new InputStreamReader(new GZIPInputStream(new FileInputStream(f)), UTF_8);
        }
        return new InputStreamReader(new FileInputStream(f), UTF_8);
    }

    private static OutputStreamWriter makeOut(File f) throws IOException {
        if (f.getName().endsWith(".gz")) {
            return new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(f)), UTF_8);
        }
        return new OutputStreamWriter(new FileOutputStream(f), UTF_8);
    }

    /**
     * Read an instance from a file. A file ending with '.gz' is uncompressed first
     *
     * @param f the file to parse
     * @return the resulting instance
     * @throws IllegalArgumentException if an error occurred while reading the file
     */
    public static Model readInstance(File f) {
        try (Reader in = makeIn(f)) {
            return readInstance(in);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Read an instance.
     *
     * @param r the stream to read
     * @return the resulting instance
     * @throws IllegalArgumentException if an error occurred while reading the json
     */
    public static Model readInstance(Reader r) {
        return gbuilder.create().fromJson(r, Model.class);
    }

    /**
     * Write a model.
     *
     * @param model the model to write
     * @param f     the output file. If it ends with '.gz' it will be gzipped
     * @throws IllegalArgumentException if an error occurred while writing the json
     */
    public static void write(Model model, File f) {
        try (OutputStreamWriter out = makeOut(f)) {
            write(model, out);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Write a model
     *
     * @param model the model to write
     * @param a     the stream to write on.
     * @throws IllegalArgumentException if an error occurred while writing the json
     */
    public static void write(Model model, Appendable a) {
        try {
            JsonWriter writer = gbuilder.create().newJsonWriter(Streams.writerForAppendable(a));
            JSONModelWriter modelWriter = new JSONModelWriter(writer);
            modelWriter.write(model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Serialise a model.
     *
     * @param model the instance to write
     * @throws IllegalArgumentException if an error occurred while writing the json
     */
    public static String toString(Model model) {
        StringWriter writer = new StringWriter();
        write(model, writer);
        return writer.toString();
    }

    /**
     * Deserialize a model.
     *
     * @param s the input string
     * @throws IllegalArgumentException if an error occurred while writing the json
     */
    public static Model fromString(String s) {
        StringReader reader = new StringReader(s);
        return readInstance(reader);
    }

}
