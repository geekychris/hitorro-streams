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

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Skips elements while the predicate holds true, then passes through all remaining elements.
 */
public class DropWhileIterator<E> extends AbstractIterator<E> {
    private final Iterator<E> source;
    private final Predicate<E> predicate;
    private E nextItem;
    private boolean hasNextItem;
    private boolean dropping = true;

    public DropWhileIterator(Iterator<E> source, Predicate<E> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public boolean hasNext() {
        if (hasNextItem) return true;
        while (source.hasNext()) {
            nextItem = source.next();
            if (dropping) {
                if (predicate.test(nextItem)) {
                    continue;
                }
                dropping = false;
            }
            hasNextItem = true;
            return true;
        }
        return false;
    }

    @Override
    public E next() {
        if (!hasNextItem && !hasNext()) {
            throw new java.util.NoSuchElementException();
        }
        hasNextItem = false;
        return nextItem;
    }

    @Override
    public void remove() {
    }

    @Override
    public void close() throws Exception {
        AbstractIterator.attemptClose(source);
    }
}
