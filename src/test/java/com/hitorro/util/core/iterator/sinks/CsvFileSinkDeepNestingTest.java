/*
 * Copyright (c) 2006-2025 Chris Collins
 */
package com.hitorro.util.core.iterator.sinks;

import com.hitorro.util.json.JsonMappers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A container cell is written as compact JSON, as on 3.x, also when it nests 600 levels deep
 * (Jackson 2's limit was 1000; Jackson 3's {@code JsonNode.toString()} throws past 500).
 */
class CsvFileSinkDeepNestingTest {

    @Test
    void deepContainerCellIsCompactJson(@TempDir Path tmp) throws Exception {
        String deep = "[".repeat(600) + "]".repeat(600);
        Path out = tmp.resolve("out.csv");
        try (CsvFileSink sink = new CsvFileSink(out.toString(), List.of("a", "b"))) {
            sink.start();
            sink.add(JsonMappers.json().readTree("{\"a\":\"x\",\"b\":" + deep + "}"));
            sink.add(JsonMappers.json().readTree("{\"a\":\"y\",\"b\":{\"k\":[1,\"s\"]}}"));
        }
        assertEquals(List.of("a,b", "x," + deep, "y,\"{\"\"k\"\":[1,\"\"s\"\"]}\""), Files.readAllLines(out));
    }
}
