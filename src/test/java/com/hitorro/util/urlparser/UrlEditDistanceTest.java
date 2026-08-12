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
package com.hitorro.util.urlparser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UrlEditDistance")
class UrlEditDistanceTest {

    private final UrlEditDistance distance = new UrlEditDistance();

    @Nested
    @DisplayName("Distance Calculation")
    class DistanceCalculation {

        @Test
        @DisplayName("Should return 0 for identical URLs")
        void shouldReturnZeroForIdentical() {
            assertThat(distance.determineUrlDistance(
                    "http://www.cnn.com/world/food",
                    "http://www.cnn.com/world/food"
            )).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return -1 for different hosts")
        void shouldReturnNegativeForDifferentHosts() {
            assertThat(distance.determineUrlDistance(
                    "http://www.cnn.com/page",
                    "http://www.bbc.com/page"
            )).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should compute positive distance for different paths")
        void shouldComputePositiveDistance() {
            int d = distance.determineUrlDistance(
                    "http://www.cnn.com/world/food",
                    "http://www.cnn.com/world/health"
            );
            assertThat(d).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should handle same host different path lengths")
        void shouldHandleDifferentLengths() {
            int d = distance.determineUrlDistance(
                    "http://example.com/a",
                    "http://example.com/a/b/c"
            );
            assertThat(d).isGreaterThan(0);
        }
    }
}
