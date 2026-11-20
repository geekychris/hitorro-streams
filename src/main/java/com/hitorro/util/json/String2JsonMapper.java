/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.json;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.JsonNode;
import com.hitorro.util.core.iterator.json.HTJSONParser;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.io.ResetableStringReader;

import java.io.IOException;
import com.hitorro.util.json.JsonMappers;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.core.JacksonException;

public class String2JsonMapper extends BaseMapper<String, JsonNode> {
    private final static JsonMapper factory = JsonMappers.json();
    protected JsonParser parser = null;
    private ResetableStringReader reader = new ResetableStringReader("");

    public String2JsonMapper() {

    }

    public JsonNode apply(String s) {
        reader.set(s);
        try {
            HTJSONParser jnp = new HTJSONParser(factory.createParser(reader));
            return jnp.read();
        } catch (JacksonException e) {
            return null;
        }
    }

}
