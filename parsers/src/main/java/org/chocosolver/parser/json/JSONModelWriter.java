/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json;

import com.google.gson.stream.JsonWriter;
import org.chocosolver.parser.json.constraints.JSONConstraintWriter;
import org.chocosolver.parser.json.variables.JSONVariableWriter;
import org.chocosolver.writer.ModelWriter;

import java.io.IOException;

/**
 * Utility class to write a model with JSON format.
 *
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 20/09/2017.
 */
public class JSONModelWriter extends ModelWriter {

    /**
     * A JSON writer
     */
    protected final JsonWriter writer;

    /**
     * Create a JSON model writer
     */
    public JSONModelWriter(JsonWriter writer) {
        super(new JSONVariableWriter(writer), new JSONConstraintWriter(writer));
        this.writer = writer;
    }


    @Override
    public void beginModel() throws IOException {
        writer.beginObject();
    }

    @Override
    public void writeName(String name) throws IOException {
        writer.name("name");
        writer.value(name);
    }

    @Override
    public void writeModelPrecision(double precision) throws IOException {
        writer.name("precision");
        writer.value(precision);
    }

    @Override
    public void beginVariables() throws IOException {
        writer.name("variables");
        writer.beginArray();
    }

    @Override
    public void endVariables()  throws IOException {
        writer.endArray();
    }

    @Override
    public void beginConstraints()  throws IOException {
        writer.name("constraints");
        writer.beginArray();
    }

    @Override
    public void endConstraints() throws IOException  {
        writer.endArray();
    }

    @Override
    public void writeObjective(String objName, int objId, boolean maximize) throws IOException {
        writer.name("objective");
        writer.beginObject();
        writer.name("name");
        writer.value(objName);
        writer.name("id");
        writer.value(JSONHelper.varId(objId));
        writer.name("maximize");
        writer.value(maximize);
        writer.endObject();
    }

    @Override
    public void endModel() throws IOException {
        writer.endObject();
    }
}
