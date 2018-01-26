/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json.constraints;

import com.google.gson.stream.JsonWriter;

import org.chocosolver.parser.json.JSONHelper;
import org.chocosolver.writer.constraints.ConstraintWriter;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.io.IOException;

/**
 * Utility class to write constraint in JSON format <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 20/09/2017.
 */
public class JSONConstraintWriter extends ConstraintWriter {

    /**
     * A JSON writer
     */
    final JsonWriter writer;

    /**
     * Prefix of constraint ID
     */

    public JSONConstraintWriter(JsonWriter writer) {
        super();
        this.writer = writer;
    }

    private void writeVar(int id) throws IOException {
        writer.value(JSONHelper.varId(id));
    }

    private void writeIntArray(int[] values) throws IOException {
        writer.beginArray();
        for (int i = 0; i < values.length; i++) {
            writer.value(values[i]);
        }
        writer.endArray();
    }

    private void writeVarArray(int[] ids) throws IOException {
        writer.beginArray();
        for (int i = 0; i < ids.length; i++) {
            writeVar(ids[i]);
        }
        writer.endArray();
    }

    private void writeFullVarParams(String type, int... ids) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(type);
        writer.name("params");
        writer.beginArray();
        for (int id : ids) {
            writeVar(id);
        }
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void beginReification(int id) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("reif");
        writer.name("by");
        writeVar(id);
        writer.name("of");
    }

    @Override
    public void endReification() throws IOException {
        writer.endObject();
    }

    @Override
    public void beginOpposite() throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("opp");
        writer.name("of");
    }

    @Override
    public void endOpposite() throws IOException {
        writer.endObject();
    }

    @Override
    public void writeArithm1(int id, Operator op, int cste) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("arithm");
        writer.name("params");
        writer.beginArray();
        writeVar(id);
        writer.value(op.toString());
        writer.value(cste);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeArithm2(int id1, Operator op, int id2) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("arithm");
        writer.name("params");
        writer.beginArray();
        writeVar(id1);
        writer.value(op.toString());
        writeVar(id2);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeMember(int id, int a, int b) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("member");
        writer.name("params");
        writer.beginArray();
        writeVar(id);
        writer.value(a);
        writer.value(b);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeNotMember(int id, int a, int b) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("notmember");
        writer.name("params");
        writer.beginArray();
        writeVar(id);
        writer.value(a);
        writer.value(b);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeMember(int id, int[] values) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("member");
        writer.name("params");
        writer.beginArray();
        writeVar(id);
        writeIntArray(values);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeNotMember(int id, int[] values) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("notmember");
        writer.name("params");
        writer.beginArray();
        writeVar(id);
        writeIntArray(values);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeArithm3(int id1, Operator op, int id2, int cste) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("arithm");
        writer.name("params");
        writer.beginArray();
        writeVar(id1);
        writer.value(op.toString());
        writeVar(id2);
        writer.value(cste);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeArithm3(int id1, int id2, Operator op, int cste) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("arithm");
        writer.name("params");
        writer.beginArray();
        writeVar(id1);
        writeVar(id2);
        writer.value(op.toString());
        writer.value(cste);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeAbsolute(int id1, int id2) throws IOException {
        writeFullVarParams("absolute", id1, id2);
    }

    @Override
    public void writeDistance2(int id1, int id2, Operator op, int factor) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("distance");
        writer.name("params");
        writer.beginArray();
        writeVar(id1);
        writeVar(id2);
        writer.value(op.toString());
        writer.value(factor);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeDistance3(int id1, int id2, Operator op, int id3) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("distance");
        writer.name("params");
        writer.beginArray();
        writeVar(id1);
        writeVar(id2);
        writer.value(op.toString());
        writeVar(id3);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeDivision(int id1, int id2, int id3) throws IOException {
        writeFullVarParams("division", id1, id2, id3);
    }

    @Override
    public void writeElement(int id1, int[] values, int id2, int offset) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("element");
        writer.name("params");
        writer.beginArray();
        writeVar(id1);
        writeIntArray(values);
        writeVar(id2);
        writer.value(offset);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeElementVar(int id1, int[] vIds, int id2, int offset) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("element");
        writer.name("params");
        writer.beginArray();
        writeVar(id1);
        writeVarArray(vIds);
        writeVar(id2);
        writer.value(offset);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeMax(int id1, int... ids) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("max");
        writer.name("params");
        writer.beginArray();
        writeVar(id1);
        writeVarArray(ids);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeMin(int id1, int... ids) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("min");
        writer.name("params");
        writer.beginArray();
        writeVar(id1);
        writeVarArray(ids);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSquare(int id1, int id2) throws IOException {
        writeFullVarParams("square", id1, id2);
    }

    @Override
    public void writeScale(int id1, int factor, int id2) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("scale");
        writer.name("params");
        writer.beginArray();
        writeVar(id1);
        writer.value(factor);
        writeVar(id2);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeTimes(int id1, int id2, int id3) throws IOException {
        writeFullVarParams("times", id1, id2, id3);
    }

    @Override
    public void writeAlldifferent(String consistency, int... ids) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("alldifferent");
        writer.name("params");
        writer.beginArray();
        writer.value(consistency);
        writeVarArray(ids);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeAmong(int id, int[] values, int[] ids) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("among");
        writer.name("params");
        writer.beginArray();
        writeVar(id);
        writeIntArray(values);
        writeVarArray(ids);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeAtleastnvalues(int[] ids, int id, boolean ac) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("atleastnvalues");
        writer.name("params");
        writer.beginArray();
        writeVarArray(ids);
        writeVar(id);
        writer.value(ac);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeAtmostnvalues(int[] ids, int id, boolean ac) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("atmostnvalues");
        writer.name("params");
        writer.beginArray();
        writeVarArray(ids);
        writeVar(id);
        writer.value(ac);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeBinpacking(int[] itemIds, int[] itemSizes, int[] binLoadsIds, int offset) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("binpacking");
        writer.name("params");
        writer.beginArray();
        writeVarArray(itemIds);
        writeIntArray(itemSizes);
        writeVarArray(binLoadsIds);
        writer.value(offset);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeBoolchanneling(int[] bIds, int vId, int offset) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("boolchanneling");
        writer.name("params");
        writer.beginArray();
        writeVarArray(bIds);
        writeVar(vId);
        writer.value(offset);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeBitschanneling(int octet, int[] bits) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("bitschanneling");
        writer.name("params");
        writer.beginArray();
        writeVar(octet);
        writeVarArray(bits);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeCircuit(int[] ids, int offset, String conf) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("circuit");
        writer.name("params");
        writer.beginArray();
        writeVarArray(ids);
        writer.value(offset);
        writer.value(conf);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeCount(int[] ids, int value, int occId) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("count");
        writer.name("params");
        writer.beginArray();
        writer.value(value);
        writeVarArray(ids);
        writeVar(occId);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeCountVar(int[] ids, int value, int occId) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("count");
        writer.name("params");
        writer.beginArray();
        writeVar(value);
        writeVarArray(ids);
        writeVar(occId);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeCumulative(int[] sIds, int[] dIds, int[] eIds, int[] hIds, int C, boolean inc, String[] filters) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("cumulative");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sIds);
        writeVarArray(dIds);
        writeVarArray(eIds);
        writeVarArray(hIds);
        writeVar(C);
        writer.value(inc);
        writer.beginArray();
        for (int i = 0; i < filters.length; i++) {
            writer.value(filters[i]);
        }
        writer.endArray();
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeDiffn(int[] xIdx, int[] yIds, int[] wIds, int[] hIds) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("diffn");
        writer.name("params");
        writer.beginArray();
        writeVarArray(xIdx);
        writeVarArray(yIds);
        writeVarArray(wIds);
        writeVarArray(hIds);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeGcc(int[] vIds, int[] values, int[] oIds) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("gcc");
        writer.name("params");
        writer.beginArray();
        writeVarArray(vIds);
        writeIntArray(values);
        writeVarArray(oIds);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeInversechanneling(int[] xids, int[] yids, int ox, int oy) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("inverse");
        writer.name("params");
        writer.beginArray();
        writeVarArray(xids);
        writeVarArray(yids);
        writer.value(ox);
        writer.value(oy);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeKnapsack(int[] oIds, int cId, int pId, int[] ws, int[] es) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("knapsack");
        writer.name("params");
        writer.beginArray();
        writeVarArray(oIds);
        writeVar(cId);
        writeVar(pId);
        writeIntArray(ws);
        writeIntArray(es);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeLexchain(int[] vIds, int n, boolean strict) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("lexchain");
        writer.name("params");
        writer.beginArray();
        writeVarArray(vIds);
        writer.value(n);
        writer.value(strict);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeLex(int[] xIds, int[] yIds, boolean strict) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("lex");
        writer.name("params");
        writer.beginArray();
        writeVarArray(xIds);
        writeVarArray(yIds);
        writer.value(strict);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeNvalues(int[] ids, int id) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("nvalues");
        writer.name("params");
        writer.beginArray();
        writeVarArray(ids);
        writeVar(id);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSum(int[] vids, int p, String o, int b) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("sum");
        writer.name("params");
        writer.beginArray();
        writeVarArray(vids);
        writer.value(p);
        writer.value(o);
        writer.value(b);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeScalar(int[] vids, int[] cs, int pos, String o, int b) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("scalar");
        writer.name("params");
        writer.beginArray();
        writeVarArray(vids);
        writeIntArray(cs);
        writer.value(pos);
        writer.value(o);
        writer.value(b);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSubcircuit(int[] ids, int id, int offset) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("subcircuit");
        writer.name("params");
        writer.beginArray();
        writeVarArray(ids);
        writeVar(id);
        writer.value(offset);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeTable(int[] ids, int[][] tuples, String algo, boolean feasible) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("table");
        writer.name("params");
        writer.beginArray();
        writeVarArray(ids);
        writer.beginArray();
        for (int i = 0; i < tuples.length; i++) {
            writeIntArray(tuples[i]);
        }
        writer.endArray();
        writer.value(feasible);
        writer.value(algo);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeTree(int[] ids, int id, int offset) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("tree");
        writer.name("params");
        writer.beginArray();
        writeVarArray(ids);
        writeVar(id);
        writer.value(offset);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetAlldifferent(int[] sIds) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setalldifferent");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sIds);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetAlldisjoint(int[] sIds) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setalldisjoint");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sIds);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetAllequal(int[] sIds) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setallequal");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sIds);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetBoolchanneling(int[] sIds, int id, int offSet) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setboolchanneling");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sIds);
        writeVar(id);
        writer.value(offSet);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetCard(int sid, int iid) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setcard");
        writer.name("params");
        writer.beginArray();
        writeVar(sid);
        writeVar(iid);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetElement(int iid, int[] sids, int sid, int offSet) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setelement");
        writer.name("params");
        writer.beginArray();
        writeVar(iid);
        writeVarArray(sids);
        writeVar(sid);
        writer.value(offSet);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetIntchanneling(int[] sids, int[] iids, int offSet1, int offSet2) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setintchanneling");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sids);
        writeVarArray(iids);
        writer.value(offSet1);
        writer.value(offSet2);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetIntersection(int[] sids, int sid, boolean b) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setintersection");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sids);
        writeVar(sid);
        writer.value(b);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetIntvaluesunion(int[] iids, int sid) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setintunion");
        writer.name("params");
        writer.beginArray();
        writeVarArray(iids);
        writeVar(sid);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetInverse(int[] sids1, int[] sids2, int offSet1, int offSet2) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setinverse");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sids1);
        writeVarArray(sids2);
        writer.value(offSet1);
        writer.value(offSet2);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetMax(int sid, int iid, int[] weights, int offSet, boolean notEmpty) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setmax");
        writer.name("params");
        writer.beginArray();
        writeVar(sid);
        writeVar(iid);
        if(weights == null){
            writer.nullValue();
        }else {
            writeIntArray(weights);
        }
        writer.value(offSet);
        writer.value(notEmpty);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetMember(int sid, int cst) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setmember");
        writer.name("params");
        writer.beginArray();
        writeVar(sid);
        writer.value(cst);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetMemberV(int sid, int iid) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setmember");
        writer.name("params");
        writer.beginArray();
        writeVar(sid);
        writeVar(iid);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetMin(int sid, int iid, int[] weights, int offSet, boolean notEmpty) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setmin");
        writer.name("params");
        writer.beginArray();
        writeVar(sid);
        writeVar(iid);
        if(weights == null){
            writer.nullValue();
        }else {
            writeIntArray(weights);
        }
        writer.value(offSet);
        writer.value(notEmpty);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetNbempty(int[] sids, int id) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setnbempty");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sids);
        writeVar(id);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetNotempty(int sid) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setnotempty");
        writer.name("params");
        writer.beginArray();
        writeVar(sid);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetNotmember(int sid, int cst) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setnotmember");
        writer.name("params");
        writer.beginArray();
        writeVar(sid);
        writer.value(cst);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetNotmemberV(int sid, int iid) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setnotmember");
        writer.name("params");
        writer.beginArray();
        writeVar(sid);
        writeVar(iid);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetOffset(int sid1, int sid2, int offSet) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setoffset");
        writer.name("params");
        writer.beginArray();
        writeVar(sid1);
        writeVar(sid2);
        writer.value(offSet);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetPartition(int[] sids, int sid) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setpartition");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sids);
        writeVar(sid);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetSubseteq(int[] sids) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setsubseteq");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sids);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetSum(int sid, int iid, int[] weights, int offSet) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setsum");
        writer.name("params");
        writer.beginArray();
        writeVar(sid);
        writeVar(iid);
        writeIntArray(weights);
        writer.value(offSet);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetSymmetric(int[] sids, int offSet) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setsymmetric");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sids);
        writer.value(offSet);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeSetUnion(int[] sids, int sid) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("setunion");
        writer.name("params");
        writer.beginArray();
        writeVarArray(sids);
        writeVar(sid);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeInteqreal(int[] iids, int[] rids, double epsilon) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("inteqreal");
        writer.name("params");
        writer.beginArray();
        writeVarArray(iids);
        writeVarArray(rids);
        writer.value(epsilon);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeRealConstraint(int[] rids, String function) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("realcstr");
        writer.name("params");
        writer.beginArray();
        writeVarArray(rids);
        writer.value(function);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeRealConstraint(int[] rids, String function, int bid) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("realcstr");
        writer.name("params");
        writer.beginArray();
        writeVarArray(rids);
        writer.value(function);
        writeVar(bid);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeBasicreification1(int vid, String op, int cste, int bid) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value("rarithm");
        writer.name("params");
        writer.beginArray();
        writeVar(vid);
        writer.value(op);
        writer.value(cste);
        writeVar(bid);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeBasicreification1(int vid, String op, IntIterableRangeSet values, int bid) throws IOException{
        writer.beginObject();
        writer.name("type");
        writer.value("rarithm");
        writer.name("params");
        writer.beginArray();
        writeVar(vid);
        writer.value(op);
        writer.value(values.toSmartString());
        writeVar(bid);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeBasicreification2(int vid1, String op, int vid2, int bid) throws IOException{
        writer.beginObject();
        writer.name("type");
        writer.value("rarithm");
        writer.name("params");
        writer.beginArray();
        writeVar(vid1);
        writer.value(op);
        writeVar(vid2);
        writeVar(bid);
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void writeBasicreification2(int vid1, String op, int vid2, int cste, int bid) throws IOException{
        writer.beginObject();
        writer.name("type");
        writer.value("rarithm");
        writer.name("params");
        writer.beginArray();
        writeVar(vid1);
        writer.value(op);
        writeVar(vid2);
        writer.value(cste);
        writeVar(bid);
        writer.endArray();
        writer.endObject();
    }
}
