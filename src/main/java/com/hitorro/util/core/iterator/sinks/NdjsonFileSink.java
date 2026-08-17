/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

/**
 * Writes newline-delimited JSON — one JSON value per line — to a local
 * file. Compression follows the URL extension ({@code .gz} → gzip;
 * anything else → plain).
 *
 * <p>Lives in {@code hitorro-streams} so any caller (mesh pipelines,
 * standalone tools, Spring Boot jobs) can drop rows to disk without
 * adopting the mesh runtime.</p>
 *
 * <p>Not compression-rich by design — for HDFS/S3, bzip2, zstd, use
 * {@code hitorro-basefile}'s {@code BaseFile.getOutputStream()} which
 * routes through {@code CompressionType} for full format coverage.
 * This sink is the "just a local file" convenience.</p>
 */
public final class NdjsonFileSink extends JsonNodeSinkBase {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final String url;
    private BufferedWriter writer;

    public NdjsonFileSink(String url) {
        this.url = url;
    }

    @Override
    public boolean start() throws IOException {
        Path path = resolvePath(url);
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        if (url.endsWith(".gz")) {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new GZIPOutputStream(Files.newOutputStream(path)),
                    StandardCharsets.UTF_8));
        } else {
            writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        }
        return true;
    }

    @Override
    protected void writeRow(JsonNode row) throws IOException {
        if (writer == null) start();
        writer.write(JSON.writeValueAsString(row));
        writer.write('\n');
    }

    @Override
    public void close() throws IOException {
        if (writer != null) { writer.flush(); writer.close(); writer = null; }
    }

    /** Accept {@code file:/…}, {@code file:///…} and bare paths. */
    private static Path resolvePath(String url) {
        if (url.startsWith("file:")) return Path.of(URI.create(url));
        return Path.of(url);
    }
}
