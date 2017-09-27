/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json.variables;

import com.google.gson.stream.JsonWriter;

import org.chocosolver.parser.json.JSONHelper;
import org.chocosolver.writer.variables.VariableWriter;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.io.IOException;

/**
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 20/09/2017.
 */
public class JSONVariableWriter extends VariableWriter {

    /**
     * A JSON writer
     */
    private final JsonWriter writer;


    public JSONVariableWriter(JsonWriter writer) {
        super();
        this.writer = writer;
    }

    private void writeNameAndId(String name, int id) throws IOException {
        writer.name("name");
        writer.value(name);
        writer.name("id");
        writer.value(JSONHelper.varId(id));
    }

    @Override
    protected void writeBoolVar(String name, int id) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("bvar");
        writeNameAndId(name, id);
        writer.endObject();
    }

    @Override
    protected void writeIntVar(String name, int id, int lb, int ub) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("ivar");
        writeNameAndId(name, id);
        writer.name("dom");
        writer.value("[" + lb + "," + ub + "]");
        writer.endObject();
    }

    @Override
    protected void writeIntVar(String name, int id, IntIterableRangeSet values) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("ivar");
        writeNameAndId(name, id);
        writer.name("dom");
        writer.value(values.toSmartString());
        writer.endObject();
    }

    @Override
    protected void writeIntVar(String name, int id, int value) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("ivar");
        writeNameAndId(name, id);
        writer.name("dom");
        writer.value("{" + value + "}");
        writer.endObject();
    }

    @Override
    protected void writeNotView(String tgtName, int tgtId, String srcName, int srcId) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("not");
        writeNameAndId(tgtName, tgtId);
        writer.name("of");
        writer.value(JSONHelper.varId(srcId));
        writer.endObject();
    }

    @Override
    protected void writeMinusView(String tgtName, int tgtId, String srcName, int srcId) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("minus");
        writeNameAndId(tgtName, tgtId);
        writer.name("of");
        writer.value(JSONHelper.varId(srcId));
        writer.endObject();
    }

    @Override
    protected void writeScaleView(String tgtName, int tgtId, int cste, String srcName, int srcId) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("scale");
        writeNameAndId(tgtName, tgtId);
        writer.name("factor");
        writer.value(cste);
        writer.name("of");
        writer.value(JSONHelper.varId(srcId));
        writer.endObject();
    }

    @Override
    protected void writeOffsetView(String tgtName, int tgtId, int cste, String srcName, int srcId) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("offset");
        writeNameAndId(tgtName, tgtId);
        writer.name("factor");
        writer.value(cste);
        writer.name("of");
        writer.value(JSONHelper.varId(srcId));
        writer.endObject();
    }

    @Override
    protected void writeSetVar(String name, int id, int[] lbs, int[] ubs) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("svar");
        writeNameAndId(name, id);
        writer.name("lb");
        writer.beginArray();
        for (int i = 0; i < lbs.length; i++) {
            writer.value(lbs[i]);
        }
        writer.endArray();
        writer.name("ub");
        writer.beginArray();
        for (int i = 0; i < ubs.length; i++) {
            writer.value(ubs[i]);
        }
        writer.endArray();
        writer.endObject();
    }

    @Override
    protected void writeRealVar(String name, int id, double lb, double ub, double pr) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("rvar");
        writeNameAndId(name, id);
        writer.name("dom");
        writer.value("[" + lb + "," + ub + "]");
        writer.name("pr");
        writer.value(pr);
        writer.endObject();
    }

    @Override
    protected void writeRealVar(String name, int id, double value) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("rvar");
        writeNameAndId(name, id);
        writer.name("dom");
        writer.value("{" + value + "}");
        writer.endObject();
    }

    @Override
    protected void writeRealView(String tgtName, int tgtId, double precision, String srcName, int srcId) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("real");
        writeNameAndId(tgtName, tgtId);
        writer.name("pr");
        writer.value(precision);
        writer.name("of");
        writer.value(JSONHelper.varId(srcId));
        writer.endObject();
    }
}
