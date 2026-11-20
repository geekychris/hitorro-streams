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
package com.hitorro.util.core.iterator.json;

import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import tools.jackson.core.JacksonException;

/**
 * Created by chris on 5/16/16.
 */
public class HTJSONParser {
    protected JsonParser parser = null;

    public HTJSONParser(JsonParser parser) {
        this.parser = parser;
    }

    public JsonNode read() {
        try {
            JsonToken token = parser.nextToken();
            if (token == null || JsonToken.NOT_AVAILABLE == token) {
                return null;
            }
            return readPrivate();
        } catch (JacksonException e) {
            throw new JSONParseException(e);
        }

    }

    protected JsonNode readPrivate() {
        JsonToken tok = parser.currentToken();

        return switchOnToken(tok);
    }

    protected final JsonNode switchOnToken(final JsonToken tok) {
        switch (tok) {
            case VALUE_STRING:
                return JsonNodeFactory.instance.stringNode(parser.getText());
            case START_OBJECT:
                return readObject();
            case START_ARRAY:
                return readArray();
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
                switch (parser.getNumberType()) {
                    case BIG_DECIMAL:
                        return JsonNodeFactory.instance.numberNode(parser.getNumberValue().longValue());
                    case BIG_INTEGER:
                        return JsonNodeFactory.instance.numberNode(parser.getNumberValue().intValue());
                    case DOUBLE:
                        return JsonNodeFactory.instance.numberNode(parser.getNumberValue().doubleValue());
                    case FLOAT:
                        return JsonNodeFactory.instance.numberNode(parser.getNumberValue().floatValue());
                    case INT:
                        return JsonNodeFactory.instance.numberNode(parser.getNumberValue().intValue());
                    case LONG:
                        return JsonNodeFactory.instance.numberNode(parser.getNumberValue().longValue());
                }
            case VALUE_TRUE:
                return JsonNodeFactory.instance.booleanNode(Boolean.TRUE);
            case VALUE_FALSE:
                return JsonNodeFactory.instance.booleanNode(Boolean.FALSE);
            case VALUE_NULL:
                return JsonNodeFactory.instance.nullNode();
            default:


        }
        return null;
    }

    private final JsonNode readObject() {
        try {
            parser.nextToken();
            ObjectNode map = JsonNodeFactory.instance.objectNode();
            if (parser.currentToken() != JsonToken.END_OBJECT) {
                // not an empty object
                do {
                    String name = parser.currentName();
                    parser.nextToken();
                    map.set(name, readPrivate());
                    parser.nextToken();

                } while (parser.currentToken() != JsonToken.END_OBJECT);
            }

            return map;
        } catch (StreamReadException jpe) {
            TokenStreamLocation jl = jpe.getLocation();
            // XXX Can we put some better logging exception description here?
            throw jpe;
        }
    }

    private final ArrayNode getArrayNode() {
        return JsonNodeFactory.instance.arrayNode();
    }

    private final JsonNode readArray() {
        parser.nextToken();
        ArrayNode list = getArrayNode();
        if (parser.currentToken() != JsonToken.END_ARRAY) {
            // not an empty array
            do {
                list.add(readPrivate());
                parser.nextToken();
            } while (parser.currentToken() != JsonToken.END_ARRAY);
        }
        return list;
    }

}
