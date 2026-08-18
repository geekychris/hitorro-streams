/*
 * Copyright (c) 2006-2026 Chris Collins
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
package com.hitorro.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Convenience base for {@link Sink}s that consume {@link JsonNode} rows
 * and want default no-op init/start/stop plus an atomic row-count.
 *
 * <p>Subclasses only implement {@link #accept(JsonNode)} (does the actual
 * write) and {@link #close()} (releases resources). {@link Sink#count()}
 * is answered from the atomic counter; {@link #add} both delegates to
 * accept and bumps the count.</p>
 *
 * <p>Not required to use — any sink can implement {@link Sink} directly.
 * This just removes the boilerplate for the common case.</p>
 */
public abstract class JsonNodeSinkBase implements Sink<JsonNode> {

    private final AtomicLong n = new AtomicLong();

    /**
     * Called once per row, after {@link #start()}, before {@link #close()}.
     * Named to avoid clashing with {@link java.util.function.Consumer#accept(Object)}
     * which {@link Sink} already provides a default for.
     */
    protected abstract void writeRow(JsonNode row) throws IOException;

    @Override public boolean init(JsonNode node)    { return true; }
    @Override public boolean start() throws IOException { return true; }
    @Override public boolean stop()  throws IOException { return true; }

    @Override
    public final boolean add(JsonNode row) throws IOException {
        writeRow(row);
        n.incrementAndGet();
        return true;
    }

    @Override
    public long count() {
        return n.get();
    }

    /**
     * Increment the row counter without going through {@link #add}.
     * Subclasses that override {@link Sink#addIdempotent} (or otherwise
     * write rows outside the normal {@code add} path) call this to
     * keep {@link #count()} honest.
     */
    protected long bumpCount() {
        return n.incrementAndGet();
    }

    @Override
    public void close() throws IOException { /* subclasses override */ }
}
