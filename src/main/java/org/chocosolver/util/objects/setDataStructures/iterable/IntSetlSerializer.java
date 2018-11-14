/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.iterable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Utility class to serialize and deserialize IntVar. <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 13/09/2017.
 */
public class IntSetlSerializer implements JsonDeserializer<IntIterableRangeSet> {

    @Override
    public IntIterableRangeSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        int[] values = context.deserialize(json, int[].class);
        IntIterableRangeSet set = new IntIterableRangeSet();
        // todo improve
        for(int i = 0 ; i < values.length; i+=2){
            for(int j = values[i]; j <= values[i + 1]; j++){
                set.add(j);
            }
        }
        return set;
    }
}
