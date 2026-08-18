/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests for {@link CsvFileSink}. Covers:
 * <ul>
 *   <li>Header + rows, correct column ordering</li>
 *   <li>Fixed column set — missing columns emit empty, extra keys ignored</li>
 *   <li>RFC 4180 quoting for commas, quotes, newlines</li>
 *   <li>Nested JSON serialised as compact JSON in the cell</li>
 *   <li>Null cells emit empty</li>
 *   <li>Gzip compression on {@code .gz} suffix</li>
 *   <li>Row count exposed via {@link Sink#count()}</li>
 *   <li>Rejects empty {@code cols} up front</li>
 * </ul>
 */
class CsvFileSinkTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void writesHeaderAndRows(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.csv");
        try (CsvFileSink sink = new CsvFileSink(out.toString(), List.of("a", "b"))) {
            sink.start();
            sink.add(row("a", "1", "b", "2"));
            sink.add(row("a", "3", "b", "4"));
        }
        assertEquals(List.of("a,b", "1,2", "3,4"), Files.readAllLines(out));
    }

    @Test
    void columnOrderFollowsSpec_notRowInsertionOrder(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.csv");
        try (CsvFileSink sink = new CsvFileSink(out.toString(), List.of("z", "a"))) {
            sink.start();
            sink.add(row("a", "first", "z", "second"));
        }
        assertEquals(List.of("z,a", "second,first"), Files.readAllLines(out));
    }

    @Test
    void missingColumnEmitsEmpty(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.csv");
        try (CsvFileSink sink = new CsvFileSink(out.toString(), List.of("a", "b", "c"))) {
            sink.start();
            sink.add(row("a", "x", "c", "z"));   // no 'b'
        }
        assertEquals(List.of("a,b,c", "x,,z"), Files.readAllLines(out));
    }

    @Test
    void extraColumnsInRowIgnored(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.csv");
        try (CsvFileSink sink = new CsvFileSink(out.toString(), List.of("a"))) {
            sink.start();
            sink.add(row("a", "kept", "b", "dropped", "c", "dropped"));
        }
        assertEquals(List.of("a", "kept"), Files.readAllLines(out));
    }

    @Test
    void quotesFieldsContainingCommaOrQuoteOrNewline(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.csv");
        try (CsvFileSink sink = new CsvFileSink(out.toString(), List.of("s"))) {
            sink.start();
            sink.add(row("s", "hi, there"));            // comma
            sink.add(row("s", "he said \"ok\""));       // embedded quote
            sink.add(row("s", "line1\nline2"));         // newline
        }
        String text = Files.readString(out);
        assertTrue(text.contains("\"hi, there\""),        "comma → quoted: " + text);
        assertTrue(text.contains("\"he said \"\"ok\"\"\""),"quote → doubled: " + text);
        assertTrue(text.contains("\"line1\nline2\""),      "newline → quoted: " + text);
    }

    @Test
    void nestedObjectSerialisedAsCompactJson(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.csv");
        ObjectNode row = JSON.createObjectNode();
        row.putObject("obj").put("k", "v");
        try (CsvFileSink sink = new CsvFileSink(out.toString(), List.of("obj"))) {
            sink.start();
            sink.add(row);
        }
        // {"k":"v"} contains a comma-free quote-heavy payload — must be quoted.
        assertEquals(List.of("obj", "\"{\"\"k\"\":\"\"v\"\"}\""),
                Files.readAllLines(out));
    }

    @Test
    void nullCellEmitsEmpty(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.csv");
        ObjectNode row = JSON.createObjectNode();
        row.putNull("a");
        row.put("b", "present");
        try (CsvFileSink sink = new CsvFileSink(out.toString(), List.of("a", "b"))) {
            sink.start();
            sink.add(row);
        }
        assertEquals(List.of("a,b", ",present"), Files.readAllLines(out));
    }

    @Test
    void gzipOnGzSuffix(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.csv.gz");
        try (CsvFileSink sink = new CsvFileSink(out.toString(), List.of("n"))) {
            sink.start();
            sink.add(row("n", "1"));
        }
        try (var in = new GZIPInputStream(Files.newInputStream(out))) {
            assertEquals("n\n1\n", new String(in.readAllBytes()));
        }
    }

    @Test
    void countReflectsRowsAdded(@TempDir Path tmp) throws Exception {
        Path out = tmp.resolve("out.csv");
        try (CsvFileSink sink = new CsvFileSink(out.toString(), List.of("a"))) {
            sink.start();
            assertEquals(0, sink.count());
            sink.add(row("a", "1"));
            sink.add(row("a", "2"));
            sink.add(row("a", "3"));
            assertEquals(3, sink.count());
        }
    }

    @Test
    void rejectsEmptyCols() {
        assertThrows(IllegalArgumentException.class,
                () -> new CsvFileSink("/tmp/x.csv", List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new CsvFileSink("/tmp/x.csv", null));
    }

    // ---------- helpers ----------
    private ObjectNode row(String... kv) {
        ObjectNode n = JSON.createObjectNode();
        for (int i = 0; i < kv.length; i += 2) n.put(kv[i], kv[i + 1]);
        return n;
    }
}
