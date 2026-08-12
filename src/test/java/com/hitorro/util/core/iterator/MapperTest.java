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
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.iterator.mappers.MapperCollection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Mapper")
class MapperTest {

    @Nested
    @DisplayName("BaseMapper combine")
    class BaseMapperCombine {

        @Test
        @DisplayName("Should chain two mappers")
        void shouldChainTwoMappers() {
            BaseMapper<String, Integer> parseInt = new BaseMapper<>() {
                @Override public Integer apply(String s) { return Integer.parseInt(s); }
            };
            BaseMapper<Integer, String> toHex = new BaseMapper<>() {
                @Override public String apply(Integer i) { return Integer.toHexString(i); }
            };

            BaseMapper<String, String> combined = parseInt.combine(toHex);
            assertThat(combined.apply("255")).isEqualTo("ff");
            assertThat(combined.apply("16")).isEqualTo("10");
        }

        @Test
        @DisplayName("Should chain three mappers via MapperCollection")
        void shouldChainThreeMappers() {
            BaseMapper<Integer, Integer> doubleIt = new BaseMapper<>() {
                @Override public Integer apply(Integer i) { return i * 2; }
            };
            BaseMapper<Integer, Integer> addTen = new BaseMapper<>() {
                @Override public Integer apply(Integer i) { return i + 10; }
            };
            BaseMapper<Integer, String> stringify = new BaseMapper<>() {
                @Override public String apply(Integer i) { return "v" + i; }
            };

            BaseMapper<Integer, String> pipeline = doubleIt.combine(addTen).combine(stringify);
            assertThat(pipeline).isInstanceOf(MapperCollection.class);
            assertThat(pipeline.apply(5)).isEqualTo("v20"); // (5*2)+10 = 20
        }
    }

    @Nested
    @DisplayName("Mapper tuple")
    class MapperTuple {

        @Test
        @DisplayName("Should create input-output tuple")
        void shouldCreateTuple() {
            Mapper<String, Integer> length = String::length;
            Mapper<String, GenericKeyValue<String, Integer>> tupled = length.tuple();
            GenericKeyValue<String, Integer> result = tupled.apply("hello");
            assertThat(result.getKey()).isEqualTo("hello");
            assertThat(result.getValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should create double-mapped tuple")
        void shouldCreateDoubleTuple() {
            Mapper<String, Integer> length = String::length;
            Mapper<String, String> upper = String::toUpperCase;
            Mapper<String, GenericKeyValue<Integer, String>> tupled = length.tuple(upper);
            GenericKeyValue<Integer, String> result = tupled.apply("hello");
            assertThat(result.getKey()).isEqualTo(5);
            assertThat(result.getValue()).isEqualTo("HELLO");
        }
    }

    @Nested
    @DisplayName("MapperCollection")
    class MapperCollectionTests {

        @Test
        @DisplayName("Should check thread safety across all mappers")
        void shouldCheckThreadSafety() {
            BaseMapper<Integer, Integer> safe = new BaseMapper<>() {
                @Override public Integer apply(Integer i) { return i; }
            };
            BaseMapper<Integer, Integer> unsafe = new BaseMapper<>() {
                @Override public Integer apply(Integer i) { return i; }
                @Override public boolean isThreadSafe() { return false; }
            };

            assertThat(safe.combine(safe).isThreadSafe()).isTrue();
            assertThat(safe.combine(unsafe).isThreadSafe()).isFalse();
        }

        @Test
        @DisplayName("Should flatten nested MapperCollections")
        void shouldFlattenNested() {
            BaseMapper<Integer, Integer> m1 = new BaseMapper<>() {
                @Override public Integer apply(Integer i) { return i + 1; }
            };
            BaseMapper<Integer, Integer> m2 = new BaseMapper<>() {
                @Override public Integer apply(Integer i) { return i * 2; }
            };
            BaseMapper<Integer, Integer> m3 = new BaseMapper<>() {
                @Override public Integer apply(Integer i) { return i - 3; }
            };

            // (1+1)*2 - 3 = 1
            BaseMapper<Integer, Integer> combined = m1.combine(m2).combine(m3);
            assertThat(combined.apply(1)).isEqualTo(1);
        }
    }
}
