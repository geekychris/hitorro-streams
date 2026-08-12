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

@DisplayName("HTMLUtil")
class HTMLUtilTest {

    @Nested
    @DisplayName("Strip Before HTML")
    class StripBeforeHTML {

        @Test
        @DisplayName("Should strip content before html tag")
        void shouldStripContentBeforeHtmlTag() {
            String input = "<!DOCTYPE html>\n<html><body>test</body></html>";
            String result = HTMLUtil.getStripBeforeHTML(input, 1000);
            assertThat(result).startsWith("<html>");
        }

        @Test
        @DisplayName("Should keep string when html is at start")
        void shouldKeepStringWhenHtmlAtStart() {
            String input = "<html><body>test</body></html>";
            String result = HTMLUtil.getStripBeforeHTML(input, 1000);
            assertThat(result).isEqualTo(input);
        }

        @Test
        @DisplayName("Should handle html tag with attributes")
        void shouldHandleHtmlTagWithAttributes() {
            String input = "junk<html lang=\"en\"><body>test</body></html>";
            String result = HTMLUtil.getStripBeforeHTML(input, 1000);
            assertThat(result).startsWith("<html lang=\"en\">");
        }

        @Test
        @DisplayName("Should handle null input")
        void shouldHandleNullInput() {
            assertThat(HTMLUtil.getStripBeforeHTML(null, 1000)).isNull();
        }

        @Test
        @DisplayName("Should handle no html tag within search limit")
        void shouldHandleNoHtmlTagWithinLimit() {
            String input = "no html tag here just text";
            String result = HTMLUtil.getStripBeforeHTML(input, 1000);
            assertThat(result).isEqualTo(input);
        }
    }

    @Nested
    @DisplayName("Strip After HTML")
    class StripAfterHTML {

        @Test
        @DisplayName("Should strip content after closing html tag")
        void shouldStripContentAfterClosingHtmlTag() {
            String input = "<html><body>test</body></html>\nsome trailing junk";
            String result = HTMLUtil.getStripAfterHTML(input, 1000);
            assertThat(result).endsWith("</html>");
        }

        @Test
        @DisplayName("Should keep string when html ends at end")
        void shouldKeepStringWhenHtmlEndsAtEnd() {
            String input = "<html><body>test</body></html>";
            String result = HTMLUtil.getStripAfterHTML(input, 1000);
            assertThat(result).isEqualTo(input);
        }

        @Test
        @DisplayName("Should handle null input")
        void shouldHandleNullInput() {
            assertThat(HTMLUtil.getStripAfterHTML(null, 1000)).isNull();
        }
    }

    @Nested
    @DisplayName("Full HTML Stripping")
    class FullStripping {

        @Test
        @DisplayName("Should strip both before and after html")
        void shouldStripBothBeforeAndAfter() {
            String input = "junk before<html><body>test</body></html>junk after";
            String result = HTMLUtil.getHTMLStripped(input);
            assertThat(result).startsWith("<html>");
            assertThat(result).endsWith("</html>");
        }

        @Test
        @DisplayName("Should handle clean html")
        void shouldHandleCleanHtml() {
            String input = "<html><body>test</body></html>";
            String result = HTMLUtil.getHTMLStripped(input);
            assertThat(result).isEqualTo(input);
        }
    }

    @Nested
    @DisplayName("Encoding Detection")
    class EncodingDetection {

        @Test
        @DisplayName("Should detect charset from meta content-type")
        void shouldDetectCharsetFromMetaContentType() {
            StringBuilder sb = new StringBuilder("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"></head>");
            String encoding = HTMLUtil.getEncodingFromXMLSnippit(sb, sb.length());
            assertThat(encoding).isEqualToIgnoringCase("iso-8859-1");
        }

        @Test
        @DisplayName("Should return UTF-8 when no charset found")
        void shouldReturnUtf8WhenNoCharsetFound() {
            StringBuilder sb = new StringBuilder("<html><head><title>No charset</title></head>");
            String encoding = HTMLUtil.getEncodingFromXMLSnippit(sb, sb.length());
            assertThat(encoding).isEqualTo("UTF-8");
        }

        @Test
        @DisplayName("Should return UTF-8 for empty content")
        void shouldReturnUtf8ForEmptyContent() {
            StringBuilder sb = new StringBuilder("");
            String encoding = HTMLUtil.getEncodingFromXMLSnippit(sb, 0);
            assertThat(encoding).isEqualTo("UTF-8");
        }
    }
}
