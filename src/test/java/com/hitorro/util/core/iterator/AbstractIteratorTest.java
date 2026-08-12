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

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AbstractIterator")
class AbstractIteratorTest {

    private static <E> AbstractIterator<E> iterOf(List<E> items) {
        return new Iterator2AbstractIterator<>(items.iterator());
    }

    private static AbstractIterator<Integer> ints(Integer... values) {
        return iterOf(Arrays.asList(values));
    }

    @Nested
    @DisplayName("toList")
    class ToList {

        @Test
        @DisplayName("Should collect all items to list")
        void shouldCollectAllItems() {
            assertThat(ints(1, 2, 3).toList()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should return empty list for empty iterator")
        void shouldReturnEmptyForEmpty() {
            assertThat(ints().toList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("forEach")
    class ForEach {

        @Test
        @DisplayName("Should apply action to each element")
        void shouldApplyToEach() {
            List<Integer> collected = new ArrayList<>();
            ints(1, 2, 3).forEach(collected::add);
            assertThat(collected).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("peek")
    class Peek {

        @Test
        @DisplayName("Should execute side effect without changing elements")
        void shouldExecuteSideEffect() {
            List<Integer> sideEffects = new ArrayList<>();
            List<Integer> result = ints(1, 2, 3).peek(sideEffects::add).toList();
            assertThat(result).containsExactly(1, 2, 3);
            assertThat(sideEffects).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("distinct")
    class Distinct {

        @Test
        @DisplayName("Should remove duplicate elements")
        void shouldRemoveDuplicates() {
            assertThat(ints(1, 2, 2, 3, 1, 3).distinct().toList()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should handle already distinct elements")
        void shouldHandleAlreadyDistinct() {
            assertThat(ints(1, 2, 3).distinct().toList()).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("takeWhile")
    class TakeWhile {

        @Test
        @DisplayName("Should take while predicate holds")
        void shouldTakeWhilePredicateHolds() {
            assertThat(ints(1, 2, 3, 4, 5).takeWhile(n -> n < 4).toList())
                    .containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should return all if predicate always true")
        void shouldReturnAllIfAlwaysTrue() {
            assertThat(ints(1, 2, 3).takeWhile(n -> true).toList())
                    .containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should return empty if predicate immediately false")
        void shouldReturnEmptyIfImmediatelyFalse() {
            assertThat(ints(1, 2, 3).takeWhile(n -> false).toList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("dropWhile")
    class DropWhile {

        @Test
        @DisplayName("Should drop while predicate holds then pass through")
        void shouldDropWhilePredicateHolds() {
            assertThat(ints(1, 2, 3, 4, 5).dropWhile(n -> n < 3).toList())
                    .containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("Should return all if predicate immediately false")
        void shouldReturnAllIfImmediatelyFalse() {
            assertThat(ints(1, 2, 3).dropWhile(n -> false).toList())
                    .containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should return empty if predicate always true")
        void shouldReturnEmptyIfAlwaysTrue() {
            assertThat(ints(1, 2, 3).dropWhile(n -> true).toList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("flatMap")
    class FlatMap {

        @Test
        @DisplayName("Should flatten nested iterators")
        void shouldFlattenNested() {
            List<Integer> result = ints(1, 2, 3)
                    .flatMap(n -> ints(n, n * 10))
                    .toList();
            assertThat(result).containsExactly(1, 10, 2, 20, 3, 30);
        }

        @Test
        @DisplayName("Should flatten all non-empty inner iterators")
        void shouldFlattenAllNonEmpty() {
            List<Integer> result = ints(1, 2, 3)
                    .flatMap(n -> ints(n * 100, n * 100 + 1))
                    .toList();
            assertThat(result).containsExactly(100, 101, 200, 201, 300, 301);
        }
    }

    @Nested
    @DisplayName("map and filter")
    class MapAndFilter {

        @Test
        @DisplayName("Should map elements")
        void shouldMapElements() {
            assertThat(ints(1, 2, 3).map(n -> n * 2).toList())
                    .containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("Should filter elements")
        void shouldFilterElements() {
            Predicate<Integer> isEven = n -> n % 2 == 0;
            assertThat(ints(1, 2, 3, 4, 5).filter(isEven).toList())
                    .containsExactly(2, 4);
        }

        @Test
        @DisplayName("Should chain map and filter")
        void shouldChainMapAndFilter() {
            Predicate<Integer> isOdd = n -> n % 2 == 1;
            var result = ints(1, 2, 3, 4, 5)
                    .filter(isOdd)
                    .map(n -> "v" + n)
                    .toList();
            assertThat(result).containsExactly("v1", "v3", "v5");
        }
    }

    @Nested
    @DisplayName("sort")
    class Sort {

        @Test
        @DisplayName("Should sort elements")
        void shouldSortElements() {
            assertThat(ints(3, 1, 4, 1, 5).sort(Comparator.naturalOrder()).toList())
                    .containsExactly(1, 1, 3, 4, 5);
        }
    }

    @Nested
    @DisplayName("combine (zip)")
    class Combine {

        @Test
        @DisplayName("Should zip two iterators into tuples")
        void shouldZipIntoTuples() {
            AbstractIterator<String> names = iterOf(List.of("a", "b", "c"));
            AbstractIterator<Integer> nums = ints(1, 2, 3);
            List<GenericKeyValue<String, Integer>> result = names.combine(nums).toList();
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getKey()).isEqualTo("a");
            assertThat(result.get(0).getValue()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("skipNTakeM")
    class SkipNTakeM {

        @Test
        @DisplayName("Should limit number of items taken")
        void shouldLimitItems() {
            List<Integer> result = ints(1, 2, 3, 4, 5).skipNTakeM(0, 3, false).toList();
            // SkipNTakeM should take at most 3 items
            assertThat(result).hasSizeLessThanOrEqualTo(3);
            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("count")
    class Count {

        @Test
        @DisplayName("Should count elements passing through")
        void shouldCountElements() {
            AtomicLong counter = new AtomicLong();
            ints(1, 2, 3, 4, 5).count(counter).toList();
            assertThat(counter.get()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("devNull")
    class DevNull {

        @Test
        @DisplayName("Should consume and count")
        void shouldConsumeAndCount() {
            assertThat(ints(1, 2, 3).devNull()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getFirstItem")
    class GetFirstItem {

        @Test
        @DisplayName("Should return first item")
        void shouldReturnFirstItem() {
            assertThat(ints(10, 20, 30).getFirstItem()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should return null for empty iterator")
        void shouldReturnNullForEmpty() {
            assertThat(ints().getFirstItem()).isNull();
        }
    }

    @Nested
    @DisplayName("Stream Integration")
    class StreamIntegration {

        @Test
        @DisplayName("Should convert to stream and back")
        void shouldConvertToStreamAndBack() {
            try (Stream<Integer> stream = ints(1, 2, 3).toStream()) {
                List<Integer> result = stream.map(n -> n * 2).collect(Collectors.toList());
                assertThat(result).containsExactly(2, 4, 6);
            }
        }

        @Test
        @DisplayName("Should create from stream")
        void shouldCreateFromStream() {
            List<Integer> result = AbstractIterator.fromStream(Stream.of(1, 2, 3)).toList();
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should use collect with Collector")
        void shouldUseCollect() {
            String result = iterOf(List.of("a", "b", "c"))
                    .collect(Collectors.joining(", "));
            assertThat(result).isEqualTo("a, b, c");
        }
    }

    @Nested
    @DisplayName("reduce")
    class Reduce {

        @Test
        @DisplayName("Should reduce to list")
        void shouldReduceToList() {
            List<Integer> result = ints(1, 2, 3).reduce(iter -> {
                List<Integer> list = new ArrayList<>();
                iter.addAll(list);
                return list;
            });
            assertThat(result).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("toMap")
    class ToMap {

        @Test
        @DisplayName("Should build map from iterator")
        void shouldBuildMap() {
            Map<String, String> map = iterOf(List.of("hello", "world", "hi"))
                    .toMap(new HashMap<>(), s -> s.substring(0, 1), null, -1);
            assertThat(map).containsKeys("h", "w");
        }
    }
}
