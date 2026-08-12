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

import java.net.URL;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Link")
class LinkTest {

    @Nested
    @DisplayName("URL Expansion")
    class UrlExpansion {

        @Test
        @DisplayName("Should resolve relative URL against source")
        void shouldResolveRelativeUrl() throws Exception {
            URL source = new URL("http://example.com/dir/page.html");
            Link link = new Link("/other", Link.LinkType.Anchor, "title", "", source);
            assertThat(link.getUrl()).isEqualTo("http://example.com/other");
        }

        @Test
        @DisplayName("Should keep absolute URL unchanged")
        void shouldKeepAbsoluteUrl() throws Exception {
            URL source = new URL("http://example.com");
            Link link = new Link("http://other.com/page", Link.LinkType.Anchor, "title", "", source);
            assertThat(link.getUrl()).isEqualTo("http://other.com/page");
            assertThat(link.isRelativeUrl()).isFalse();
        }

        @Test
        @DisplayName("Should handle blocked protocols")
        void shouldHandleBlockedProtocols() throws Exception {
            URL source = new URL("http://example.com");
            Link link = new Link("javascript:void(0)", Link.LinkType.Anchor, "title", "", source);
            assertThat(link.getUrl()).isEqualTo("javascript:void(0)");
        }

        @Test
        @DisplayName("Should handle mailto protocol")
        void shouldHandleMailtoProtocol() throws Exception {
            URL source = new URL("http://example.com");
            Link link = new Link("mailto:test@example.com", Link.LinkType.Anchor, "title", "", source);
            assertThat(link.getUrl()).isEqualTo("mailto:test@example.com");
        }

        @Test
        @DisplayName("Should handle null source URL")
        void shouldHandleNullSourceUrl() {
            Link link = new Link("http://example.com", Link.LinkType.Anchor, "title", "", (URL) null);
            assertThat(link.getUrl()).isEqualTo("http://example.com");
        }
    }

    @Nested
    @DisplayName("Link Type")
    class LinkTypeTests {

        @Test
        @DisplayName("Should have correct names for all link types")
        void shouldHaveCorrectNames() {
            assertThat(Link.LinkType.Anchor.getName()).isEqualTo("a");
            assertThat(Link.LinkType.Alternate.getName()).isEqualTo("al");
            assertThat(Link.LinkType.PingBack.getName()).isEqualTo("pb");
            assertThat(Link.LinkType.StyleSheet.getName()).isEqualTo("ss");
            assertThat(Link.LinkType.ShortCutIcon.getName()).isEqualTo("sc");
            assertThat(Link.LinkType.Image.getName()).isEqualTo("im");
            assertThat(Link.LinkType.EditURI.getName()).isEqualTo("eu");
            assertThat(Link.LinkType.Unknown.getName()).isEqualTo("u");
        }

        @Test
        @DisplayName("Should lookup type by short name")
        void shouldLookupTypeByShortName() {
            assertThat(Link.LinkType.getTypeName("a")).isEqualTo(Link.LinkType.Anchor);
            assertThat(Link.LinkType.getTypeName("al")).isEqualTo(Link.LinkType.Alternate);
            assertThat(Link.LinkType.getTypeName("pb")).isEqualTo(Link.LinkType.PingBack);
            assertThat(Link.LinkType.getTypeName("im")).isEqualTo(Link.LinkType.Image);
        }

        @Test
        @DisplayName("Should have distinct short names for Anchor and Alternate")
        void shouldHaveDistinctShortNames() {
            assertThat(Link.LinkType.Anchor.getName()).isNotEqualTo(Link.LinkType.Alternate.getName());
        }

        @Test
        @DisplayName("Should have correct size for type map")
        void shouldHaveCorrectSizeForTypeMap() {
            assertThat(Link.LinkType.size()).isEqualTo(Link.LinkType.values().length);
        }
    }

    @Nested
    @DisplayName("Comparators")
    class Comparators {

        @Test
        @DisplayName("UrlOnlyLinkComparitor should compare by URL hash")
        void urlOnlyComparitorShouldCompareByHash() throws Exception {
            URL source = new URL("http://example.com");
            Link link1 = new Link("http://a.com", Link.LinkType.Anchor, "", "", source);
            Link link2 = new Link("http://b.com", Link.LinkType.Anchor, "", "", source);
            Link link3 = new Link("http://a.com", Link.LinkType.Anchor, "", "", source);

            int cmp = Link.URLOnlyComparitor.compare(link1, link3);
            assertThat(cmp).isEqualTo(0);

            int cmpDiff = Link.URLOnlyComparitor.compare(link1, link2);
            assertThat(cmpDiff).isNotEqualTo(0);
        }

        @Test
        @DisplayName("UrlAndTypeComparitor should sort by URL then type")
        void urlAndTypeComparitorShouldSortByUrlThenType() throws Exception {
            URL source = new URL("http://example.com");
            Link anchor = new Link("http://same.com", Link.LinkType.Anchor, "", "", source);
            Link image = new Link("http://same.com", Link.LinkType.Image, "", "", source);
            Link differentUrl = new Link("http://other.com", Link.LinkType.Anchor, "", "", source);

            // Same URL, different type - should sort by type ordinal
            int cmpSameUrl = Link.UrlAndTypeComparitor.compare(anchor, image);
            assertThat(cmpSameUrl).isNotEqualTo(0);

            // Different URL - should sort by URL
            int cmpDiffUrl = Link.UrlAndTypeComparitor.compare(anchor, differentUrl);
            assertThat(cmpDiffUrl).isNotEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Properties")
    class Properties {

        @Test
        @DisplayName("Should store and retrieve source URL")
        void shouldStoreAndRetrieveSourceUrl() {
            Link link = new Link();
            link.setSource("http://source.com");
            assertThat(link.getSourceUrl()).isEqualTo("http://source.com");
            assertThat(link.getSourceHash()).isNotEqualTo(0);
        }

        @Test
        @DisplayName("Should store and retrieve published date")
        void shouldStoreAndRetrievePublishedDate() {
            Link link = new Link();
            link.setPublishedDate(123456789L);
            assertThat(link.getPublishedDate()).isEqualTo(123456789L);
        }

        @Test
        @DisplayName("Should compute URL hash")
        void shouldComputeUrlHash() throws Exception {
            URL source = new URL("http://example.com");
            Link link = new Link("http://test.com/page", Link.LinkType.Anchor, "title", "", source);
            assertThat(link.getUrlHash()).isNotEqualTo(0);
        }

        @Test
        @DisplayName("Should implement compareTo by URL")
        void shouldImplementCompareToByUrl() throws Exception {
            URL source = new URL("http://example.com");
            Link link1 = new Link("http://a.com", Link.LinkType.Anchor, "", "", source);
            Link link2 = new Link("http://b.com", Link.LinkType.Anchor, "", "", source);
            assertThat(link1.compareTo(link2)).isLessThan(0);
            assertThat(link2.compareTo(link1)).isGreaterThan(0);
            assertThat(link1.compareTo(link1)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should have toString return URL")
        void shouldHaveToStringReturnUrl() throws Exception {
            URL source = new URL("http://example.com");
            Link link = new Link("http://test.com", Link.LinkType.Anchor, "title", "", source);
            assertThat(link.toString()).isEqualTo("http://test.com");
        }
    }
}
