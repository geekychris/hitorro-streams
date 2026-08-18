/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Writes CSV — header row on {@link #start()}, one comma-separated row
 * per {@link JsonNode} thereafter. Column order + set is fixed by the
 * {@code cols} list; row values missing that column serialise to empty.
 *
 * <p>Compression follows the URL extension ({@code .gz} → gzip).
 * Quoting is minimal but correct: fields containing {@code ,}, {@code "},
 * {@code \n}, or {@code \r} get double-quoted, and embedded {@code "}
 * is doubled per RFC 4180. Non-string JSON scalars serialise via
 * {@link JsonNode#asText()}; nested objects/arrays serialise as their
 * compact JSON representation.</p>
 *
 * <p>Lives in {@code hitorro-streams} — usable by any caller (mesh
 * pipelines, standalone tools, Spring Boot jobs) that needs CSV output
 * over a fixed column set. Follow the {@link NdjsonFileSink} pattern —
 * for HDFS/S3/bzip2, go through {@code BaseFile.getOutputStream()}
 * instead.</p>
 */
public final class CsvFileSink extends JsonNodeSinkBase {

    private final String url;
    private final List<String> cols;
    private BufferedWriter writer;

    public CsvFileSink(String url, List<String> cols) {
        if (cols == null || cols.isEmpty()) {
            throw new IllegalArgumentException("cols must be non-empty (defines column order + set)");
        }
        this.url = url;
        this.cols = List.copyOf(cols);
    }

    @Override
    public boolean start() throws IOException {
        Path path = resolvePath(url);
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        writer = url.endsWith(".gz")
                ? new BufferedWriter(new OutputStreamWriter(
                        new GZIPOutputStream(Files.newOutputStream(path)),
                        StandardCharsets.UTF_8))
                : Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        // Header row — column names verbatim.
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) writer.write(',');
            writer.write(escape(cols.get(i)));
        }
        writer.write('\n');
        return true;
    }

    @Override
    protected void writeRow(JsonNode row) throws IOException {
        if (writer == null) start();
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) writer.write(',');
            JsonNode v = row == null ? null : row.get(cols.get(i));
            if (v != null && !v.isNull()) writer.write(escape(cellText(v)));
        }
        writer.write('\n');
    }

    @Override
    public void close() throws IOException {
        if (writer != null) { writer.flush(); writer.close(); writer = null; }
    }

    /** Scalar → asText(); object/array → compact JSON. */
    private static String cellText(JsonNode v) {
        return v.isContainerNode() ? v.toString() : v.asText();
    }

    /** RFC 4180 minimal quoting — quote only when required. */
    private static String escape(String s) {
        boolean needsQuote = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',' || c == '"' || c == '\n' || c == '\r') { needsQuote = true; break; }
        }
        if (!needsQuote) return s;
        StringBuilder out = new StringBuilder(s.length() + 4);
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') out.append('"').append('"');
            else out.append(c);
        }
        out.append('"');
        return out.toString();
    }

    private static Path resolvePath(String url) {
        if (url.startsWith("file:")) return Path.of(URI.create(url));
        return Path.of(url);
    }
}
