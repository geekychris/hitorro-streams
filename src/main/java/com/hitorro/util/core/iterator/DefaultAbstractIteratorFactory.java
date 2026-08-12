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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultAbstractIteratorFactory implements AbstractIteratorFactory {

    @Override
    public <E, OUT> AbstractIterator<OUT> fillIterator(AbstractIterator<E> source,
                                                       int batchSize,
                                                       FillBufferHandler<E, OUT> transformer) {
        return new FillBufferIterator<>(source, batchSize, transformer);
    }

    @Override
    public <E> AbstractIterator<E> count(AbstractIterator<E> source, AtomicLong counter, long reportEvery) {
        return new CountingIterator<>(source, counter, reportEvery);
    }

    @Override
    public <E> AbstractIterator<E> sort(AbstractIterator<E> source, Comparator<E> comparator) {
        return new SortIterator<>(source, comparator);
    }

    @Override
    public <E, F> AbstractIterator<GenericKeyValue<E, F>> combine(AbstractIterator<E> source,
                                                                   AbstractIterator<F> other) {
        return new TupleIterator<>(source, other);
    }

    @Override
    public <E, OUT> AbstractIterator<OUT> map(AbstractIterator<E> source, Function<E, OUT> mapper) {
        return new MappingIterator<>(source, mapper);
    }

    @Override
    public <E> AbstractIterator<E> skipNTake(AbstractIterator<E> source,
                                             long skip,
                                             long take,
                                             Predicate<E> excludeInTake,
                                             boolean expungeRemainder) {
        return new SkipNTakeM<>(source, skip, take, excludeInTake, expungeRemainder);
    }

    @Override
    public <E, O> AbstractIterator<O> mapRemoveNull(AbstractIterator<E> source, Mapper<E, O> mapper) {
        return new NullRemovingIterator<>(new MappingIterator<>(source, mapper));
    }

    @Override
    public <E, OUT> AbstractIterator<OUT> mapParallel(AbstractIterator<E> source,
                                                      Function<E, OUT> mapper,
                                                      int threads,
                                                      int inputQueueSize,
                                                      int outputQueueSize,
                                                      String name) {
        return new ParIterator<>(source, mapper, threads, inputQueueSize, outputQueueSize, name);
    }

    @Override
    public <E> AbstractIterator<E> timeLimiting(AbstractIterator<E> source, long timeLimitInMillis) {
        return new TimeLimitingIterator<>(timeLimitInMillis, source);
    }

    @Override
    public <E> AbstractIterator<E> rateLimiting(AbstractIterator<E> source, int rpm) {
        return new RateLimitingIterator<>(rpm, source);
    }

    @Override
    public <E> AbstractIterator<E> filter(AbstractIterator<E> source, Predicate<E> predicate) {
        return new FilteringIterator<>(source, predicate);
    }

    @Override
    public <E> AbstractIterator<E> filterToSink(AbstractIterator<E> source,
                                                Predicate<E> predicate,
                                                Sink<E> sink) {
        return new FilterToSinkIterator<>(source, predicate, sink);
    }

    @Override
    public <E> AbstractIterator<E> removing(AbstractIterator<E> source,
                                            Comparator<E> comparator,
                                            BiFunction<E, E, E> merger) {
        return new LikeRowMergingIterator<>(source, comparator, merger);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E, O> AbstractIterator<O> nest(AbstractIterator<E> source,
                                           Mapper<E, AbstractIterator<O>> mapper,
                                           NestingIteratorErrorHandler<E, O> handler) {
        return new NestingIterator(source, mapper, handler);
    }

    @Override
    public <E> AbstractIterator<E> takeWhile(AbstractIterator<E> source, Predicate<E> predicate) {
        return new TakeWhileIterator<>(source, predicate);
    }

    @Override
    public <E> AbstractIterator<E> dropWhile(AbstractIterator<E> source, Predicate<E> predicate) {
        return new DropWhileIterator<>(source, predicate);
    }

    @Override
    public <E> AbstractIterator<E> fromIterator(Iterator<E> iterator) {
        return new Iterator2AbstractIterator<>(iterator);
    }

    @Override
    public <E> AbstractIterator<E> fromCollection(Collection<E> collection) {
        return new CollectionIterator<>(collection);
    }
}
