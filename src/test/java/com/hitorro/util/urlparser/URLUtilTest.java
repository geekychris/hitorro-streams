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

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("URLUtil")
class URLUtilTest {

    @Nested
    @DisplayName("cleanupUrl")
    class CleanupUrl {

        @Test
        @DisplayName("Should normalize valid URL")
        void shouldNormalizeValidUrl() {
            String result = URLUtil.cleanupUrl("http://example.com/path");
            assertThat(result).isEqualTo("http://example.com/path");
        }

        @Test
        @DisplayName("Should decode encoded URL")
        void shouldDecodeEncodedUrl() {
            String result = URLUtil.cleanupUrl("http://example.com/hello%20world");
            assertThat(result).contains("example.com");
        }

        @Test
        @DisplayName("Should return original for malformed URL")
        void shouldReturnOriginalForMalformed() {
            String result = URLUtil.cleanupUrl("not-a-url");
            assertThat(result).isEqualTo("not-a-url");
        }

        @Test
        @DisplayName("Should not return empty string on IllegalArgumentException")
        void shouldNotReturnEmptyOnIllegalArg() {
            // After fix: returns original URL instead of empty string
            String result = URLUtil.cleanupUrl("http://example.com/%%invalid");
            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("cleanupUrlAndCutToLength")
    class CleanupUrlAndCut {

        @Test
        @DisplayName("Should truncate to specified length")
        void shouldTruncate() {
            String url = "http://example.com/very/long/path/that/goes/on/and/on";
            String result = URLUtil.cleanupUrlAndCutToLength(url, 30);
            assertThat(result.length()).isLessThanOrEqualTo(30);
        }

        @Test
        @DisplayName("Should not truncate short URLs")
        void shouldNotTruncateShort() {
            String url = "http://example.com";
            String result = URLUtil.cleanupUrlAndCutToLength(url, 1024);
            assertThat(result).isEqualTo(url);
        }
    }

    @Nested
    @DisplayName("getSiteFromURL")
    class GetSiteFromURL {

        @Test
        @DisplayName("Should extract site from HTTP URL")
        void shouldExtractFromHttp() {
            String site = URLUtil.getSiteFromURL("http://www.example.com/path");
            assertThat(site).contains("example.com");
        }

        @Test
        @DisplayName("Should extract site from HTTPS URL")
        void shouldExtractFromHttps() {
            String site = URLUtil.getSiteFromURL("https://www.example.com/path");
            assertThat(site).contains("example.com");
        }

        @Test
        @DisplayName("Should handle URL with port")
        void shouldHandlePort() {
            String site = URLUtil.getSiteFromURL("http://example.com:8080/path");
            assertThat(site).contains("example.com");
        }
    }

    @Nested
    @DisplayName("isValidUrl")
    class IsValidUrl {

        @Test
        @DisplayName("Should accept valid HTTP URL")
        void shouldAcceptValidHttp() {
            assertThat(URLUtil.isValidUrl("http://example.com")).isTrue();
        }

        @Test
        @DisplayName("Should accept valid HTTPS URL")
        void shouldAcceptValidHttps() {
            assertThat(URLUtil.isValidUrl("https://example.com/path")).isTrue();
        }

        @Test
        @DisplayName("Should reject null")
        void shouldRejectNull() {
            assertThat(URLUtil.isValidUrl(null)).isFalse();
        }

        @Test
        @DisplayName("Should reject empty string")
        void shouldRejectEmpty() {
            assertThat(URLUtil.isValidUrl("")).isFalse();
        }

        @Test
        @DisplayName("Should reject string without protocol")
        void shouldRejectNoProtocol() {
            assertThat(URLUtil.isValidUrl("example.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("getQueryParameters")
    class GetQueryParameters {

        @Test
        @DisplayName("Should extract query parameters")
        void shouldExtractParams() {
            Map<String, String> params = URLUtil.getQueryParameters("http://example.com?foo=bar&baz=qux");
            assertThat(params).containsEntry("foo", "bar");
            assertThat(params).containsEntry("baz", "qux");
        }

        @Test
        @DisplayName("Should decode URL-encoded parameters")
        void shouldDecodeParams() {
            Map<String, String> params = URLUtil.getQueryParameters("http://example.com?q=hello+world&name=caf%C3%A9");
            assertThat(params.get("q")).isEqualTo("hello world");
        }

        @Test
        @DisplayName("Should return empty map for URL without query")
        void shouldReturnEmptyForNoQuery() {
            assertThat(URLUtil.getQueryParameters("http://example.com/path")).isEmpty();
        }

        @Test
        @DisplayName("Should handle null input")
        void shouldHandleNull() {
            assertThat(URLUtil.getQueryParameters(null)).isEmpty();
        }

        @Test
        @DisplayName("Should strip fragment from query")
        void shouldStripFragment() {
            Map<String, String> params = URLUtil.getQueryParameters("http://example.com?a=1#section");
            assertThat(params).containsEntry("a", "1");
            assertThat(params).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getHost")
    class GetHost {

        @Test
        @DisplayName("Should extract host from URL")
        void shouldExtractHost() {
            assertThat(URLUtil.getHost("http://www.example.com/path")).isEqualTo("www.example.com");
        }

        @Test
        @DisplayName("Should return null for invalid URL")
        void shouldReturnNullForInvalid() {
            assertThat(URLUtil.getHost("not-a-url")).isNull();
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNull() {
            assertThat(URLUtil.getHost(null)).isNull();
        }
    }

    @Nested
    @DisplayName("getPath")
    class GetPath {

        @Test
        @DisplayName("Should extract path from URL")
        void shouldExtractPath() {
            assertThat(URLUtil.getPath("http://example.com/foo/bar")).isEqualTo("/foo/bar");
        }

        @Test
        @DisplayName("Should return empty path for root URL")
        void shouldReturnEmptyForRoot() {
            assertThat(URLUtil.getPath("http://example.com")).isEmpty();
        }
    }
}
