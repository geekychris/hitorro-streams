/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests for {@link JsonFileSink}. Covers:
 * <ul>
 *   <li>Empty file → empty array {@code []}</li>
 *   <li>Multi-row output is a syntactically valid JSON array parseable
 *       back to identical trees</li>
 *   <li>Nested structures preserved (rows aren't flattened)</li>
 *   <li>{@code pretty=true} produces indented output</li>
 *   <li>Gzip compression on {@code .gz} suffix</li>
 *   <li>{@link Sink#count()} tracks rows written</li>
 * </ul>
 */
class JsonFileSinkTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void emptyRunEmitsEmptyArray(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.json");
        try (JsonFileSink sink = new JsonFileSink(out.toString(), false)) {
            sink.start();
        }
        JsonNode tree = JSON.readTree(out.toFile());
        assertTrue(tree.isArray());
        assertEquals(0, tree.size());
    }

    @Test
    void multipleRowsRoundTrip(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.json");
        try (JsonFileSink sink = new JsonFileSink(out.toString(), false)) {
            sink.start();
            sink.add(row("id", 1, "name", "alpha"));
            sink.add(row("id", 2, "name", "beta"));
            sink.add(row("id", 3, "name", "gamma"));
        }
        JsonNode tree = JSON.readTree(out.toFile());
        assertTrue(tree.isArray(), "top-level must be a JSON array");
        assertEquals(3, tree.size());
        assertEquals("alpha", tree.get(0).get("name").asText());
        assertEquals(3,       tree.get(2).get("id").asInt());
    }

    @Test
    void nestedStructuresPreserved(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.json");
        ObjectNode nested = JSON.createObjectNode();
        nested.put("id", 42);
        nested.putObject("meta").put("tag", "x").put("n", 7);
        nested.putArray("labels").add("a").add("b").add("c");
        try (JsonFileSink sink = new JsonFileSink(out.toString(), false)) {
            sink.start();
            sink.add(nested);
        }
        JsonNode tree = JSON.readTree(out.toFile());
        assertEquals(1, tree.size());
        JsonNode r = tree.get(0);
        assertEquals(42, r.get("id").asInt());
        assertEquals("x", r.get("meta").get("tag").asText());
        assertEquals(3,   r.get("labels").size());
        assertEquals("b", r.get("labels").get(1).asText());
    }

    @Test
    void prettyPrintingProducesIndentedOutput(@TempDir Path tmp) throws Exception {
        Path outPretty = tmp.resolve("pretty.json");
        Path outCompact = tmp.resolve("compact.json");
        try (JsonFileSink sink = new JsonFileSink(outPretty.toString(), true)) {
            sink.start();
            sink.add(row("k", "v"));
        }
        try (JsonFileSink sink = new JsonFileSink(outCompact.toString(), false)) {
            sink.start();
            sink.add(row("k", "v"));
        }
        long prettyLen  = Files.size(outPretty);
        long compactLen = Files.size(outCompact);
        assertTrue(prettyLen > compactLen,
                "pretty output should be larger than compact — got pretty=" + prettyLen
                + " compact=" + compactLen);
        String pretty = Files.readString(outPretty);
        assertTrue(pretty.contains("\n"), "pretty output must contain newlines: " + pretty);
    }

    @Test
    void gzipOnGzSuffix(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.json.gz");
        try (JsonFileSink sink = new JsonFileSink(out.toString(), false)) {
            sink.start();
            sink.add(row("id", 1));
        }
        try (var in = new GZIPInputStream(Files.newInputStream(out))) {
            JsonNode tree = JSON.readTree(in);
            assertTrue(tree.isArray());
            assertEquals(1, tree.get(0).get("id").asInt());
        }
    }

    @Test
    void countReflectsRowsAdded(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.json");
        try (JsonFileSink sink = new JsonFileSink(out.toString(), false)) {
            sink.start();
            assertEquals(0, sink.count());
            sink.add(row("a", 1));
            sink.add(row("a", 2));
            assertEquals(2, sink.count());
        }
    }

    // ---------- helpers ----------
    private ObjectNode row(Object... kv) {
        ObjectNode n = JSON.createObjectNode();
        for (int i = 0; i < kv.length; i += 2) {
            String k = (String) kv[i];
            Object v = kv[i + 1];
            if (v instanceof Integer x) n.put(k, x);
            else n.put(k, String.valueOf(v));
        }
        return n;
    }
}
