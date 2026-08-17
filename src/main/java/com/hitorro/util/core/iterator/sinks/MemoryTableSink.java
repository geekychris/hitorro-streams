/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * In-process row buffer keyed by a name. Rows go into an
 * externally-provided {@code List<JsonNode>} so other code (typically
 * a pipeline runtime keeping a registry of memory tables) can iterate
 * them after the sink closes.
 *
 * <p>General-purpose: any caller that wants "collect rows into a list"
 * as a Sink drop-in can use this. The mesh pipelines runtime uses it
 * to wire cross-node {@code source: {kind: ref, node: X}} refs — but
 * the sink itself has no mesh dependency.</p>
 *
 * <p>The caller controls the backing list — a synchronized list if
 * concurrent readers matter, plain if single-threaded.</p>
 */
public final class MemoryTableSink extends JsonNodeSinkBase {

    private final String name;
    private final List<JsonNode> buffer;

    public MemoryTableSink(String name) {
        this(name, Collections.synchronizedList(new java.util.ArrayList<>()));
    }

    public MemoryTableSink(String name, List<JsonNode> buffer) {
        this.name = name;
        this.buffer = buffer;
    }

    public String name()            { return name; }
    public List<JsonNode> buffer()  { return buffer; }

    @Override
    protected void writeRow(JsonNode row) throws IOException {
        buffer.add(row);
    }
}
