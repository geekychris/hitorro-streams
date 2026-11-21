package com.hitorro.util.json;

import com.hitorro.util.core.iterator.JSONIterator;
import com.hitorro.util.core.iterator.sinks.JsonFileSink;
import com.hitorro.util.core.iterator.sinks.NdjsonFileSink;
import com.hitorro.util.json.iterators.HTJSONIterator;
import com.hitorro.util.json.mapper.Json2StringMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins hitorro-streams' JSON input and output to what hitorro-streams 3.0.2 (Jackson 2.18) did.
 *
 * <p>{@code jackson2-streams-golden.txt} was produced by running the same calls against the
 * published hitorro-streams 3.0.2 and hitorro-core 3.0.2 jars with Jackson 2.18.2 (generator:
 * {@code ~/hitorro-j3/tools/Jackson2StreamsGolden.java.txt}). A difference means files written
 * by the sinks, or trees read by the iterators, changed: fix the code, not the golden file.
 */
class Jackson3FormatCompatibilityTest {

    private static Map<String, String> golden;

    @TempDir
    Path dir;

    @BeforeAll
    static void load() throws Exception {
        golden = new LinkedHashMap<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                Jackson3FormatCompatibilityTest.class.getResourceAsStream("jackson2-streams-golden.txt"),
                StandardCharsets.UTF_8))) {
            r.lines().forEach(l -> {
                int i = l.indexOf('=');
                golden.put(l.substring(0, i), l.substring(i + 1));
            });
        }
    }

    private static String flat(Object v) {
        return String.valueOf(v).replace("\n", "\\n");
    }

    private static String tryIt(Callable<Object> c) {
        try {
            return String.valueOf(c.call());
        } catch (Throwable t) {
            return "!" + t.getClass().getSimpleName();
        }
    }

    private static JsonNode row1() {
        return JsonMappers.json().readTree("{\"id\":1,\"name\":\"a\",\"tags\":[\"x\",\"y\"],\"meta\":{},\"u\":\"é\\u2028/\"}");
    }

    private static JsonNode row2() {
        return JsonMappers.json().readTree("{\"id\":2,\"v\":1.5,\"n\":null,\"big\":12345678901234567890,\"e\":[]}");
    }

    @Test
    @DisplayName("JsonFileSink writes the same compact and pretty-printed files")
    void jsonFileSink() throws Exception {
        for (boolean pretty : new boolean[]{false, true}) {
            Path f = dir.resolve("o" + pretty + ".json");
            JsonFileSink s = new JsonFileSink(f.toString(), pretty);
            s.start();
            s.add(row1());
            s.add(row2());
            s.close();
            assertThat(flat(Files.readString(f))).isEqualTo(golden.get("jsonFileSink." + pretty));
        }
    }

    @Test
    @DisplayName("NdjsonFileSink writes the same lines")
    void ndjsonFileSink() throws Exception {
        Path f = dir.resolve("o.ndjson");
        NdjsonFileSink s = new NdjsonFileSink(f.toString());
        s.start();
        s.add(row1());
        s.add(row2());
        s.close();
        assertThat(flat(Files.readString(f))).isEqualTo(golden.get("ndjsonFileSink"));
    }

    @Test
    @DisplayName("JSONIterator reads the same trees and node types (including its BIG_INTEGER-to-int quirk)")
    void jsonIterator() {
        String in = "{\"a\":1,\"b\":[1,2.5,\"s\",true,false,null,{}],\"c\":{},\"l\":12345678901} [] "
                + "{\"big\":123456789012345678901234567890,\"dec\":1.5e3,\"neg\":-0.0} \"str\" 42";
        JSONIterator it = new JSONIterator(new StringReader(in));
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            JsonNode n = it.next();
            sb.append(n).append(" | ");
            if (n.isObject()) {
                StringBuilder t = new StringBuilder();
                n.properties().forEach(e -> t.append(e.getKey()).append(':')
                        .append(e.getValue().getClass().getSimpleName()).append(','));
                sb.append(t).append(" | ");
            }
        }
        assertThat(flat(sb)).isEqualTo(golden.get("jsonIterator"));
    }

    @Test
    @DisplayName("JSONIterator stops at invalid JSON instead of throwing")
    void jsonIteratorBad() {
        String got = tryIt(() -> {
            JSONIterator bi = new JSONIterator(new StringReader("{\"a\":1} {\"b\": }"));
            StringBuilder x = new StringBuilder();
            while (bi.hasNext()) {
                x.append(bi.next()).append(';');
            }
            return x;
        });
        assertThat(got).isEqualTo(golden.get("jsonIteratorBad"));
    }

    @Test
    @DisplayName("HTJSONIterator elements write the same JSON through a generator")
    void htJsonIterator() {
        HTJSONIterator hi = new HTJSONIterator(new StringReader(
                "{\"z\":1,\"a\":{\"k\":[1,\"two\",null,true]},\"d\":2.5} {\"x\":\"y\"}"));
        StringBuilder hs = new StringBuilder();
        while (hi.hasNext()) {
            JSONElement e = hi.next();
            StringWriter w = new StringWriter();
            JsonGenerator g = JsonMappers.json().createGenerator(w);
            e.writeJSONGraph(g);
            g.flush();
            hs.append(w).append(" | ");
        }
        assertThat(flat(hs)).isEqualTo(golden.get("htJsonIterator"));
    }

    @Test
    @DisplayName("string/JSON mappers: same output; invalid input throws JSONParseException as before")
    void mappers() {
        assertThat(tryIt(() -> new com.hitorro.util.json.mapper.String2JsonMapper().apply("{\"a\":[1,2],\"b\":\"c\"}")))
                .isEqualTo(golden.get("string2Json.mapper"));
        assertThat(tryIt(() -> new com.hitorro.util.json.mapper.String2JsonMapper().apply("{\"a\": ]")))
                .isEqualTo(golden.get("string2Json.mapper.bad"));
        assertThat(tryIt(() -> new String2JsonMapper().apply("{\"a\":[1,2],\"b\":\"c\"}")))
                .isEqualTo(golden.get("string2Json"));
        assertThat(tryIt(() -> new String2JsonMapper().apply("{\"a\": ]")))
                .isEqualTo(golden.get("string2Json.bad"));
        assertThat(flat(new Json2StringMapper().apply(row1()))).isEqualTo(golden.get("json2String"));
        assertThat(flat(new String(new Json2ByteArrayMapper().apply(row2()), StandardCharsets.UTF_8)))
                .isEqualTo(golden.get("json2Bytes"));
    }
}
