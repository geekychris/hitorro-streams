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

@DisplayName("UrlCursor")
class UrlCursorTest {

    @Nested
    @DisplayName("URL Parsing")
    class UrlParsing {

        @Test
        @DisplayName("Should parse HTTP URL into host token")
        void shouldParseHttpHost() {
            UrlCursor cur = new UrlCursor();
            cur.setUrl("http://www.example.com/path");
            assertThat(cur.nextToken()).isTrue();
            assertThat(cur.getUrlPartType()).isEqualTo(UrlCursor.Part.Host);
            assertThat(cur.getToken()).isEqualTo("www.example.com");
        }

        @Test
        @DisplayName("Should parse HTTPS URL")
        void shouldParseHttps() {
            UrlCursor cur = new UrlCursor();
            cur.setUrl("https://secure.example.com/page");
            assertThat(cur.isSecure()).isTrue();
            assertThat(cur.nextToken()).isTrue();
            assertThat(cur.getToken()).isEqualTo("secure.example.com");
        }

        @Test
        @DisplayName("Should detect non-secure HTTP")
        void shouldDetectNonSecure() {
            UrlCursor cur = new UrlCursor();
            cur.setUrl("http://example.com/path");
            assertThat(cur.isSecure()).isFalse();
        }

        @Test
        @DisplayName("Should parse path components")
        void shouldParsePathComponents() {
            UrlCursor cur = new UrlCursor();
            cur.setUrl("http://example.com/foo/bar/baz");
            cur.nextToken(); // host
            assertThat(cur.nextToken()).isTrue();
            assertThat(cur.getUrlPartType()).isEqualTo(UrlCursor.Part.Path);
            assertThat(cur.getToken()).isEqualTo("foo");
            assertThat(cur.nextToken()).isTrue();
            assertThat(cur.getToken()).isEqualTo("bar");
            assertThat(cur.nextToken()).isTrue();
            assertThat(cur.getToken()).isEqualTo("baz");
        }

        @Test
        @DisplayName("Should parse query arguments")
        void shouldParseArguments() {
            UrlCursor cur = new UrlCursor();
            cur.setUrl("http://example.com/search?q=test&lang=en");
            cur.nextToken(); // host
            cur.nextToken(); // path: search
            assertThat(cur.nextToken()).isTrue();
            assertThat(cur.getUrlPartType()).isEqualTo(UrlCursor.Part.Argument);
            assertThat(cur.getKeyPartOfArgToken()).isEqualTo("q");
            assertThat(cur.getValuePartOfArgToken()).isEqualTo("test");
        }

        @Test
        @DisplayName("Should parse port")
        void shouldParsePort() {
            UrlCursor cur = new UrlCursor();
            cur.setUrl("http://example.com:8080/path");
            cur.nextToken(); // host
            assertThat(cur.nextToken()).isTrue();
            assertThat(cur.getUrlPartType()).isEqualTo(UrlCursor.Part.Port);
            assertThat(cur.getToken()).isEqualTo("8080");
        }

        @Test
        @DisplayName("Should parse anchor/fragment")
        void shouldParseAnchor() {
            UrlCursor cur = new UrlCursor();
            cur.setUrl("http://example.com/page#section");
            cur.nextToken(); // host
            cur.nextToken(); // path: page
            assertThat(cur.nextToken()).isTrue();
            assertThat(cur.getUrlPartType()).isEqualTo(UrlCursor.Part.Anchor);
            assertThat(cur.getToken()).isEqualTo("section");
        }
    }

    @Nested
    @DisplayName("Token comparison")
    class TokenComparison {

        @Test
        @DisplayName("Should compare token case insensitively")
        void shouldCompareCaseInsensitive() {
            UrlCursor cur = new UrlCursor();
            cur.setUrl("http://WWW.EXAMPLE.COM/path");
            cur.nextToken();
            assertThat(cur.isTokenSameIgnoreCase("www.example.com")).isTrue();
        }

        @Test
        @DisplayName("Should get token in lower case")
        void shouldGetLowerCase() {
            UrlCursor cur = new UrlCursor();
            cur.setUrl("http://WWW.EXAMPLE.COM/PATH");
            cur.nextToken();
            assertThat(cur.getTokenLowerCase()).isEqualTo("www.example.com");
        }

        @Test
        @DisplayName("Should get token in upper case")
        void shouldGetUpperCase() {
            UrlCursor cur = new UrlCursor();
            cur.setUrl("http://www.example.com/path");
            cur.nextToken();
            assertThat(cur.getTokenUpperCase()).isEqualTo("WWW.EXAMPLE.COM");
        }
    }

    @Nested
    @DisplayName("getAllToCurrentPos")
    class GetAllToCurrentPos {

        @Test
        @DisplayName("Should return URL up to current position")
        void shouldReturnUpToCurrent() {
            UrlCursor cur = new UrlCursor();
            cur.setUrl("http://www.example.com/path/page");
            cur.nextToken(); // host
            String site = cur.getAllToCurrentPos();
            assertThat(site).isEqualTo("http://www.example.com");
        }
    }

    @Nested
    @DisplayName("getSiteFromURL static")
    class GetSiteFromURL {

        @Test
        @DisplayName("Should extract site portion")
        void shouldExtractSite() {
            assertThat(UrlCursor.getSiteFromURL("http://www.cnn.com/world/news"))
                    .isEqualTo("http://www.cnn.com");
        }

        @Test
        @DisplayName("Should handle HTTPS")
        void shouldHandleHttps() {
            String site = UrlCursor.getSiteFromURL("https://secure.example.com/page");
            assertThat(site).contains("secure.example.com");
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Should reject very short URLs")
        void shouldRejectShortUrls() {
            UrlCursor cur = new UrlCursor();
            assertThat(cur.setUrl("abc")).isFalse();
        }

        @Test
        @DisplayName("Should handle URL without protocol")
        void shouldHandleNoProtocol() {
            UrlCursor cur = new UrlCursor();
            cur.setUrl("www.example.com/path");
            assertThat(cur.nextToken()).isTrue();
        }
    }
}
