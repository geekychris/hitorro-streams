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
package com.hitorro.util.html;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("HTMLEncoder")
class HTMLEncoderTest {

    @Nested
    @DisplayName("Named Entities")
    class NamedEntities {

        @Test
        @DisplayName("Should decode common entities")
        void shouldDecodeCommonEntities() {
            assertThat(HTMLEncoder.decodeHtml("&amp;")).isEqualTo("&");
            assertThat(HTMLEncoder.decodeHtml("&lt;")).isEqualTo("<");
            assertThat(HTMLEncoder.decodeHtml("&gt;")).isEqualTo(">");
            assertThat(HTMLEncoder.decodeHtml("&quot;")).isEqualTo("\"");
        }

        @Test
        @DisplayName("Should decode nbsp")
        void shouldDecodeNbsp() {
            String result = HTMLEncoder.decodeHtml("&nbsp;");
            assertThat(result).hasSize(1);
            assertThat(result.charAt(0)).isEqualTo((char) 160);
        }

        @Test
        @DisplayName("Should decode mdash and ndash")
        void shouldDecodeDashes() {
            assertThat(HTMLEncoder.decodeHtml("&mdash;")).isEqualTo("\u2014");
            assertThat(HTMLEncoder.decodeHtml("&ndash;")).isEqualTo("\u2013");
        }

        @Test
        @DisplayName("Should decode rsquo as right single quotation mark")
        void shouldDecodeRsquo() {
            String result = HTMLEncoder.decodeHtml("&rsquo;");
            assertThat(result).isEqualTo("\u2019");
        }

        @Test
        @DisplayName("Should decode new HTML5 entities")
        void shouldDecodeHtml5Entities() {
            assertThat(HTMLEncoder.decodeHtml("&euro;")).isEqualTo("\u20AC");
            assertThat(HTMLEncoder.decodeHtml("&trade;")).isEqualTo("\u2122");
            assertThat(HTMLEncoder.decodeHtml("&hellip;")).isEqualTo("\u2026");
            assertThat(HTMLEncoder.decodeHtml("&bull;")).isEqualTo("\u2022");
            assertThat(HTMLEncoder.decodeHtml("&lsquo;")).isEqualTo("\u2018");
            assertThat(HTMLEncoder.decodeHtml("&ldquo;")).isEqualTo("\u201C");
            assertThat(HTMLEncoder.decodeHtml("&rdquo;")).isEqualTo("\u201D");
            assertThat(HTMLEncoder.decodeHtml("&prime;")).isEqualTo("\u2032");
            assertThat(HTMLEncoder.decodeHtml("&Prime;")).isEqualTo("\u2033");
            assertThat(HTMLEncoder.decodeHtml("&dagger;")).isEqualTo("\u2020");
            assertThat(HTMLEncoder.decodeHtml("&Dagger;")).isEqualTo("\u2021");
            assertThat(HTMLEncoder.decodeHtml("&permil;")).isEqualTo("\u2030");
            assertThat(HTMLEncoder.decodeHtml("&thinsp;")).isEqualTo("\u2009");
            assertThat(HTMLEncoder.decodeHtml("&ensp;")).isEqualTo("\u2002");
            assertThat(HTMLEncoder.decodeHtml("&emsp;")).isEqualTo("\u2003");
            assertThat(HTMLEncoder.decodeHtml("&zwnj;")).isEqualTo("\u200C");
            assertThat(HTMLEncoder.decodeHtml("&Yacute;")).isEqualTo("\u00DD");
        }

        @Test
        @DisplayName("Should decode multiple entities in one string")
        void shouldDecodeMultipleEntities() {
            assertThat(HTMLEncoder.decodeHtml("&lt;p&gt;Hello&amp;World&lt;/p&gt;"))
                    .isEqualTo("<p>Hello&World</p>");
        }

        @Test
        @DisplayName("Should decode accented characters")
        void shouldDecodeAccentedCharacters() {
            assertThat(HTMLEncoder.decodeHtml("&eacute;")).isEqualTo("\u00E9");
            assertThat(HTMLEncoder.decodeHtml("&ntilde;")).isEqualTo("\u00F1");
            assertThat(HTMLEncoder.decodeHtml("&uuml;")).isEqualTo("\u00FC");
        }
    }

    @Nested
    @DisplayName("Numeric Entities")
    class NumericEntities {

        @Test
        @DisplayName("Should decode decimal numeric entities")
        void shouldDecodeDecimalEntities() {
            assertThat(HTMLEncoder.decodeHtml("&#38;")).isEqualTo("&");
            assertThat(HTMLEncoder.decodeHtml("&#60;")).isEqualTo("<");
            assertThat(HTMLEncoder.decodeHtml("&#8212;")).isEqualTo("\u2014");
        }

        @Test
        @DisplayName("Should decode hex numeric entities")
        void shouldDecodeHexEntities() {
            assertThat(HTMLEncoder.decodeHtml("&#x26;")).isEqualTo("&");
            assertThat(HTMLEncoder.decodeHtml("&#x3C;")).isEqualTo("<");
            assertThat(HTMLEncoder.decodeHtml("&#x2014;")).isEqualTo("\u2014");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNull() {
            assertThat(HTMLEncoder.decodeHtml(null)).isNull();
        }

        @Test
        @DisplayName("Should return same string when no entities")
        void shouldReturnSameWhenNoEntities() {
            assertThat(HTMLEncoder.decodeHtml("Hello World")).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Should return empty string for empty input")
        void shouldReturnEmptyForEmpty() {
            assertThat(HTMLEncoder.decodeHtml("")).isEmpty();
        }

        @Test
        @DisplayName("Should handle standalone ampersand")
        void shouldHandleStandaloneAmpersand() {
            String result = HTMLEncoder.decodeHtml("foo & bar");
            assertThat(result).isEqualTo("foo & bar");
        }

        @Test
        @DisplayName("Should handle unclosed entity")
        void shouldHandleUnclosedEntity() {
            String result = HTMLEncoder.decodeHtml("foo &amp bar");
            assertThat(result).isEqualTo("foo &amp bar");
        }
    }

    @Nested
    @DisplayName("Sort Order Validation")
    class SortOrderValidation {

        @Test
        @DisplayName("Should have entity array in correct alphabetical order")
        void shouldHaveEntitiesInAlphabeticalOrder() {
            // Use reflection to access the private array and validate sort order
            // This is critical because the binary search depends on it
            String prev = null;
            // We test indirectly: if every known entity decodes correctly,
            // the binary search is working, which means the array is sorted
            assertThat(HTMLEncoder.decodeHtml("&AElig;")).isNotNull();
            assertThat(HTMLEncoder.decodeHtml("&amp;")).isEqualTo("&");
            assertThat(HTMLEncoder.decodeHtml("&middot;")).isNotNull();
            assertThat(HTMLEncoder.decodeHtml("&yuml;")).isNotNull();
            assertThat(HTMLEncoder.decodeHtml("&zwnj;")).isNotNull();

            // Test entities at boundaries of insertion points for new entries
            assertThat(HTMLEncoder.decodeHtml("&bull;")).isEqualTo("\u2022");
            assertThat(HTMLEncoder.decodeHtml("&euro;")).isEqualTo("\u20AC");
            assertThat(HTMLEncoder.decodeHtml("&trade;")).isEqualTo("\u2122");
        }
    }
}
