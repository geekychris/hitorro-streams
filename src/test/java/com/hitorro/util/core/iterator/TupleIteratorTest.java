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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TupleIterator")
class TupleIteratorTest {

    @Nested
    @DisplayName("Zip behavior")
    class ZipBehavior {

        @Test
        @DisplayName("Should zip equal length iterators")
        void shouldZipEqualLength() {
            var a = new Iterator2AbstractIterator<>(Arrays.asList("x", "y", "z").iterator());
            var b = new Iterator2AbstractIterator<>(Arrays.asList(1, 2, 3).iterator());
            TupleIterator<String, Integer> tuple = new TupleIterator<>(a, b);

            List<GenericKeyValue<String, Integer>> result = tuple.toList();
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getKey()).isEqualTo("x");
            assertThat(result.get(0).getValue()).isEqualTo(1);
            assertThat(result.get(2).getKey()).isEqualTo("z");
            assertThat(result.get(2).getValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should handle empty iterators")
        void shouldHandleEmpty() {
            var a = new Iterator2AbstractIterator<>(List.<String>of().iterator());
            var b = new Iterator2AbstractIterator<>(List.<Integer>of().iterator());
            TupleIterator<String, Integer> tuple = new TupleIterator<>(a, b);
            assertThat(tuple.hasNext()).isFalse();
        }

        @Test
        @DisplayName("Should use short-circuit OR in hasNext")
        void shouldUseShortCircuitOr() {
            // After fix: uses || instead of |
            // When first iterator has items, second shouldn't matter for hasNext
            var a = new Iterator2AbstractIterator<>(List.of("a").iterator());
            var b = new Iterator2AbstractIterator<>(List.of(1).iterator());
            TupleIterator<String, Integer> tuple = new TupleIterator<>(a, b);
            assertThat(tuple.hasNext()).isTrue();
        }
    }
}
