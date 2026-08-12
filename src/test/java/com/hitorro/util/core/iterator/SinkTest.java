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

import com.hitorro.util.core.iterator.sinks.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Sink")
class SinkTest {

    private static <E> AbstractIterator<E> iterOf(List<E> items) {
        return new Iterator2AbstractIterator<>(items.iterator());
    }

    @Nested
    @DisplayName("SinkList")
    class SinkListTests {

        @Test
        @DisplayName("Should collect items into list")
        void shouldCollectItems() throws IOException {
            SinkList<Integer> sink = new SinkList<>();
            iterOf(Arrays.asList(1, 2, 3)).sink(sink);
            assertThat(sink.getList()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should clear list on start")
        void shouldClearOnStart() throws IOException {
            SinkList<Integer> sink = new SinkList<>();
            iterOf(Arrays.asList(1, 2)).sink(sink);
            assertThat(sink.getList()).hasSize(2);
            iterOf(Arrays.asList(3, 4, 5)).sink(sink);
            assertThat(sink.getList()).containsExactly(3, 4, 5);
        }
    }

    @Nested
    @DisplayName("Sink.of factory")
    class SinkOfFactory {

        @Test
        @DisplayName("Should create sink from consumer")
        void shouldCreateFromConsumer() throws IOException {
            List<String> collected = new ArrayList<>();
            Sink<String> sink = Sink.of(collected::add);
            iterOf(Arrays.asList("a", "b", "c")).sink(sink);
            assertThat(collected).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("CountingSink")
    class CountingSinkTests {

        @Test
        @DisplayName("Should count items")
        void shouldCountItems() throws IOException {
            CountingSink<Integer> sink = Sink.counting();
            iterOf(Arrays.asList(1, 2, 3, 4, 5)).sink(sink);
            assertThat(sink.getCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should reset count on start")
        void shouldResetOnStart() throws IOException {
            CountingSink<Integer> sink = Sink.counting();
            iterOf(Arrays.asList(1, 2)).sink(sink);
            assertThat(sink.getCount()).isEqualTo(2);
            iterOf(Arrays.asList(1, 2, 3)).sink(sink);
            assertThat(sink.getCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("MappingSink")
    class MappingSinkTests {

        @Test
        @DisplayName("Should transform items before adding to downstream")
        void shouldTransformItems() throws IOException {
            SinkList<String> downstream = new SinkList<>();
            Sink<Integer> mappingSink = downstream.map(n -> String.valueOf(n * 2));
            iterOf(Arrays.asList(1, 2, 3)).sink(mappingSink);
            assertThat(downstream.getList()).containsExactly("2", "4", "6");
        }
    }

    @Nested
    @DisplayName("PredicatedSink")
    class PredicatedSinkTests {

        @Test
        @DisplayName("Should only add items matching predicate")
        void shouldFilterItems() throws IOException {
            SinkList<Integer> downstream = new SinkList<>();
            Sink<Integer> filtered = downstream.filter(n -> n % 2 == 0);
            iterOf(Arrays.asList(1, 2, 3, 4, 5)).sink(filtered);
            assertThat(downstream.getList()).containsExactly(2, 4);
        }
    }

    @Nested
    @DisplayName("TeeSink")
    class TeeSinkTests {

        @Test
        @DisplayName("Should send items to both sinks")
        void shouldBroadcast() throws IOException {
            SinkList<Integer> sink1 = new SinkList<>();
            SinkList<Integer> sink2 = new SinkList<>();
            Sink<Integer> tee = sink1.tee(sink2);
            iterOf(Arrays.asList(1, 2, 3)).sink(tee);
            assertThat(sink1.getList()).containsExactly(1, 2, 3);
            assertThat(sink2.getList()).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("Sink Composition")
    class SinkComposition {

        @Test
        @DisplayName("Should chain filter then tee")
        void shouldChainOperations() throws IOException {
            SinkList<Integer> primary = new SinkList<>();
            SinkList<Integer> mirror = new SinkList<>();

            // filter(n!=2) wraps primary, tee sends to both filtered-primary and mirror
            Sink<Integer> pipeline = primary.filter(n -> n != 2).tee(mirror);

            iterOf(Arrays.asList(1, 2, 3)).sink(pipeline);

            // primary gets filtered items (through PredicatedSink)
            assertThat(primary.getList()).containsExactly(1, 3);
            // mirror gets all items (tee sends to both independently)
            assertThat(mirror.getList()).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("Iterator sink terminal")
    class IteratorSinkTerminal {

        @Test
        @DisplayName("Should return count from sink")
        void shouldReturnCount() throws IOException {
            SinkList<Integer> sink = new SinkList<>();
            int count = iterOf(Arrays.asList(1, 2, 3)).sink(sink);
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("Should support windowed sink")
        void shouldSupportWindowedSink() throws IOException {
            SinkList<Integer> sink = new SinkList<>();
            iterOf(Arrays.asList(10, 20, 30, 40, 50)).sink(sink, 1, 2);
            assertThat(sink.getList()).containsExactly(20, 30);
        }
    }
}
