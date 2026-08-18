/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.core.iterator.sinks;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

/**
 * Writes a single JSON array — {@code [row, row, row]} — to a local
 * file. Streaming: rows are written to the generator as they arrive,
 * so peak memory stays constant regardless of row count.
 *
 * <p>Compression follows the URL extension ({@code .gz} → gzip).
 * {@code pretty=true} switches to Jackson's default indenting printer —
 * useful for hand-inspecting small outputs; leave off for large runs.</p>
 *
 * <p>Sibling to {@link NdjsonFileSink} — pick JSON array when a
 * downstream reader wants one parseable JSON document; pick NDJSON
 * when you want line-per-row for streaming/tail-ability.</p>
 */
public final class JsonFileSink extends JsonNodeSinkBase {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final String url;
    private final boolean pretty;
    private JsonGenerator gen;
    private OutputStream out;

    public JsonFileSink(String url, boolean pretty) {
        this.url = url;
        this.pretty = pretty;
    }

    @Override
    public boolean start() throws IOException {
        Path path = resolvePath(url);
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        out = url.endsWith(".gz")
                ? new GZIPOutputStream(Files.newOutputStream(path))
                : Files.newOutputStream(path);
        gen = JSON.getFactory().createGenerator(out);
        if (pretty) gen.setPrettyPrinter(new DefaultPrettyPrinter());
        gen.writeStartArray();
        return true;
    }

    @Override
    protected void writeRow(JsonNode row) throws IOException {
        if (gen == null) start();
        JSON.writeTree(gen, row);
    }

    @Override
    public void close() throws IOException {
        if (gen != null) {
            try {
                gen.writeEndArray();
                gen.flush();
            } finally {
                gen.close();
                if (out != null) out.close();
                gen = null;
                out = null;
            }
        }
    }

    private static Path resolvePath(String url) {
        if (url.startsWith("file:")) return Path.of(URI.create(url));
        return Path.of(url);
    }
}
