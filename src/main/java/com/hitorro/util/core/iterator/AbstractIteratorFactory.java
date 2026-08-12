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
package com.hitorro.util.core.iterator;

import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.iterator.sinks.Sink;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Constructs the concrete {@link AbstractIterator} implementations that its fluent-chaining
 * methods return. Introduced so that {@code AbstractIterator} itself has no compile-time
 * dependency on the concrete iterator classes it produces — the dependency lives in the
 * factory implementation instead. This enables moving concrete implementations into a
 * separate module while keeping {@code AbstractIterator} in a lower layer.
 *
 * <p>{@link DefaultAbstractIteratorFactory} is the built-in implementation. Callers
 * generally do not interact with this interface; it exists to support future module
 * boundaries.
 */
public interface AbstractIteratorFactory {

    <E, OUT> AbstractIterator<OUT> fillIterator(AbstractIterator<E> source,
                                                int batchSize,
                                                FillBufferHandler<E, OUT> transformer);

    <E> AbstractIterator<E> count(AbstractIterator<E> source, AtomicLong counter, long reportEvery);

    <E> AbstractIterator<E> sort(AbstractIterator<E> source, Comparator<E> comparator);

    <E, F> AbstractIterator<GenericKeyValue<E, F>> combine(AbstractIterator<E> source,
                                                           AbstractIterator<F> other);

    <E, OUT> AbstractIterator<OUT> map(AbstractIterator<E> source, Function<E, OUT> mapper);

    <E> AbstractIterator<E> skipNTake(AbstractIterator<E> source,
                                      long skip,
                                      long take,
                                      Predicate<E> excludeInTake,
                                      boolean expungeRemainder);

    <E, O> AbstractIterator<O> mapRemoveNull(AbstractIterator<E> source, Mapper<E, O> mapper);

    <E, OUT> AbstractIterator<OUT> mapParallel(AbstractIterator<E> source,
                                               Function<E, OUT> mapper,
                                               int threads,
                                               int inputQueueSize,
                                               int outputQueueSize,
                                               String name);

    <E> AbstractIterator<E> timeLimiting(AbstractIterator<E> source, long timeLimitInMillis);

    <E> AbstractIterator<E> rateLimiting(AbstractIterator<E> source, int rpm);

    <E> AbstractIterator<E> filter(AbstractIterator<E> source, Predicate<E> predicate);

    <E> AbstractIterator<E> filterToSink(AbstractIterator<E> source,
                                         Predicate<E> predicate,
                                         Sink<E> sink);

    <E> AbstractIterator<E> removing(AbstractIterator<E> source,
                                     Comparator<E> comparator,
                                     BiFunction<E, E, E> merger);

    <E, O> AbstractIterator<O> nest(AbstractIterator<E> source,
                                    Mapper<E, AbstractIterator<O>> mapper,
                                    NestingIteratorErrorHandler<E, O> handler);

    <E> AbstractIterator<E> takeWhile(AbstractIterator<E> source, Predicate<E> predicate);

    <E> AbstractIterator<E> dropWhile(AbstractIterator<E> source, Predicate<E> predicate);

    <E> AbstractIterator<E> fromIterator(Iterator<E> iterator);

    <E> AbstractIterator<E> fromCollection(java.util.Collection<E> collection);
}
