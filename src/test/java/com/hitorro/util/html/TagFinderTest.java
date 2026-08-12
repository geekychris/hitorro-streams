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

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TagFinder")
class TagFinderTest {

    private void init(TagFinder tf, String buffer) {
        // maxPos must exceed buffer length so the loop can reach the closing '>'
        tf.set(buffer, buffer.length() + 1);
    }

    @Nested
    @DisplayName("Tag Iteration")
    class TagIteration {

        @Test
        @DisplayName("Should find tags in buffer")
        void shouldFindTagsInBuffer() {
            TagFinder tf = new TagFinder();
            init(tf, "<html><head></head><body></body></html>");

            int count = 0;
            while (tf.next()) {
                count++;
            }
            assertThat(count).isGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("Should handle no tags")
        void shouldHandleNoTags() {
            TagFinder tf = new TagFinder();
            init(tf, "just plain text");
            assertThat(tf.next()).isFalse();
        }

        @Test
        @DisplayName("Should respect max position")
        void shouldRespectMaxPosition() {
            TagFinder tf = new TagFinder();
            tf.set("<a><b><c>", 5);
            int count = 0;
            while (tf.next()) {
                count++;
            }
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return correct tag name")
        void shouldReturnCorrectTagName() {
            TagFinder tf = new TagFinder();
            init(tf, "<div class=\"test\">content</div>");
            assertThat(tf.next()).isTrue();
            assertThat(tf.getTag()).isEqualTo("div");
        }
    }

    @Nested
    @DisplayName("Tag Name Matching")
    class TagNameMatching {

        @Test
        @DisplayName("Should match tag name case insensitively")
        void shouldMatchCaseInsensitively() {
            TagFinder tf = new TagFinder();
            init(tf, "<META http-equiv=\"Content-Type\" content=\"text/html\"> ");
            assertThat(tf.next()).isTrue();
            assertThat(tf.isTagEqualIgnoreCase("meta")).isTrue();
            assertThat(tf.isTagEqualIgnoreCase("META")).isTrue();
            assertThat(tf.isTagEqualIgnoreCase("Meta")).isTrue();
        }

        @Test
        @DisplayName("Should not match different tag name")
        void shouldNotMatchDifferentTagName() {
            TagFinder tf = new TagFinder();
            init(tf, "<div>content</div>");
            assertThat(tf.next()).isTrue();
            assertThat(tf.isTagEqualIgnoreCase("span")).isFalse();
        }
    }

    @Nested
    @DisplayName("Attribute Parsing")
    class AttributeParsing {

        @Test
        @DisplayName("Should find attribute with quoted value")
        void shouldFindAttributeWithQuotedValue() {
            TagFinder tf = new TagFinder();
            init(tf, "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"> ");
            assertThat(tf.next()).isTrue();
            assertThat(tf.findAttribute("content")).isTrue();
            assertThat(tf.getAttributeValue()).isEqualTo("text/html; charset=UTF-8");
        }

        @Test
        @DisplayName("Should find attribute case insensitively")
        void shouldFindAttributeCaseInsensitively() {
            TagFinder tf = new TagFinder();
            init(tf, "<meta HTTP-EQUIV=\"Refresh\" CONTENT=\"5\"> ");
            assertThat(tf.next()).isTrue();
            assertThat(tf.findAttribute("http-equiv")).isTrue();
            assertThat(tf.getAttributeValue()).isEqualTo("Refresh");
        }

        @Test
        @DisplayName("Should handle valueless attributes")
        void shouldHandleValuelessAttributes() {
            TagFinder tf = new TagFinder();
            init(tf, "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\"> ");
            assertThat(tf.next()).isTrue();
            assertThat(tf.findAttribute("html")).isTrue();
        }

        @Test
        @DisplayName("Should return false for missing attribute")
        void shouldReturnFalseForMissingAttribute() {
            TagFinder tf = new TagFinder();
            init(tf, "<div class=\"test\"> ");
            assertThat(tf.next()).isTrue();
            assertThat(tf.findAttribute("id")).isFalse();
        }

        @Test
        @DisplayName("Should get all attributes as map")
        void shouldGetAllAttributesAsMap() {
            TagFinder tf = new TagFinder();
            init(tf, "<a href=\"http://example.com\" title=\"Example\" class=\"link\"> ");
            assertThat(tf.next()).isTrue();
            Map<String, String> attrs = tf.getAttributes();
            assertThat(attrs).containsEntry("href", "http://example.com");
            assertThat(attrs).containsEntry("title", "Example");
            assertThat(attrs).containsEntry("class", "link");
        }

        @Test
        @DisplayName("Should handle single-quoted attribute values")
        void shouldHandleSingleQuotedValues() {
            TagFinder tf = new TagFinder();
            init(tf, "<div class='test'> ");
            assertThat(tf.next()).isTrue();
            Map<String, String> attrs = tf.getAttributes();
            assertThat(attrs).containsEntry("class", "test");
        }
    }
}
