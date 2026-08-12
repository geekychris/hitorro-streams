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
package com.hitorro.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultSinkFactory implements SinkFactory {

    @Override
    public <T> Sink<T> from(Consumer<T> consumer) {
        return new BaseSink<T>() {
            @Override public boolean init(JsonNode map) { return true; }
            @Override public boolean start() { return true; }
            @Override public boolean add(T o) { consumer.accept(o); return true; }
            @Override public boolean stop() { return true; }
        };
    }

    @Override
    public <T> CountingSink<T> counting() {
        return new CountingSink<>();
    }

    @Override
    public <T> Sink<T> maxPerTransaction(Sink<T> delegate, long max) {
        return new MaxItemsPerTransactionSink<>(delegate, max);
    }

    @Override
    public <T> Sink<T> filter(Sink<T> delegate, Predicate<T> predicate) {
        return new PredicatedSink<>(delegate, predicate);
    }

    @Override
    public <I, T> Sink<I> map(Sink<T> delegate, Function<I, T> function) {
        return new MappingSink<>(delegate, function);
    }

    @Override
    public <T> Sink<T> tee(Sink<T> a, Sink<T> b) {
        return new TeeSink<>(a, b);
    }
}
