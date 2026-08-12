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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.html.constraint.TypeLinkConstraint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("HTMLParser")
class HTMLParserTest {

    private HTMLParser parse(String html) throws IOException {
        HTMLParser parser = new HTMLParser();
        parser.setHtmlPage(html);
        return parser;
    }

    @Nested
    @DisplayName("Body Text Extraction")
    class BodyTextExtraction {

        @Test
        @DisplayName("Should extract text from body")
        void shouldExtractTextFromBody() throws IOException {
            var parser = parse("<html><body><p>Hello World</p></body></html>");
            assertThat(parser.getBodyText()).contains("Hello World");
        }

        @Test
        @DisplayName("Should filter out script tags")
        void shouldFilterOutScriptTags() throws IOException {
            var parser = parse("<html><body><script>var x = 1;</script><p>Visible</p></body></html>");
            String text = parser.getBodyText();
            assertThat(text).contains("Visible");
            assertThat(text).doesNotContain("var x");
        }

        @Test
        @DisplayName("Should filter out style tags")
        void shouldFilterOutStyleTags() throws IOException {
            var parser = parse("<html><body><style>.foo { color: red; }</style><p>Visible</p></body></html>");
            String text = parser.getBodyText();
            assertThat(text).contains("Visible");
            assertThat(text).doesNotContain("color");
        }

        @Test
        @DisplayName("Should handle empty body")
        void shouldHandleEmptyBody() throws IOException {
            var parser = parse("<html><body></body></html>");
            assertThat(parser.getBodyText()).isEmpty();
        }

        @Test
        @DisplayName("Should extract text from nested elements")
        void shouldExtractTextFromNestedElements() throws IOException {
            var parser = parse("<html><body><div><p>First</p><p>Second</p></div></body></html>");
            String text = parser.getBodyText();
            assertThat(text).contains("First");
            assertThat(text).contains("Second");
        }

        @Test
        @DisplayName("Should use custom separator")
        void shouldUseCustomSeparator() throws IOException {
            HTMLParser parser = new HTMLParser(' ');
            parser.setHtmlPage("<html><body><p>A</p><p>B</p></body></html>");
            String text = parser.getBodyText();
            assertThat(text).contains("A");
            assertThat(text).contains("B");
            assertThat(text).doesNotContain("\n");
        }
    }

    @Nested
    @DisplayName("Title Extraction")
    class TitleExtraction {

        @Test
        @DisplayName("Should extract title text")
        void shouldExtractTitleText() throws IOException {
            var parser = parse("<html><head><title>My Page</title></head><body></body></html>");
            assertThat(parser.getTitleText()).contains("My Page");
        }

        @Test
        @DisplayName("Should handle missing title")
        void shouldHandleMissingTitle() throws IOException {
            var parser = parse("<html><head></head><body></body></html>");
            assertThat(parser.getTitleText()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Meta Tag Extraction")
    class MetaTagExtraction {

        @Test
        @DisplayName("Should extract meta keywords")
        void shouldExtractMetaKeywords() throws IOException {
            var parser = parse("<html><head><meta name=\"keywords\" content=\"java, html, parser\"></head><body></body></html>");
            assertThat(parser.getMetaKeywords()).contains("java, html, parser");
        }

        @Test
        @DisplayName("Should extract meta description")
        void shouldExtractMetaDescription() throws IOException {
            var parser = parse("<html><head><meta name=\"description\" content=\"A great page\"></head><body></body></html>");
            assertThat(parser.getMetaDescription()).contains("A great page");
        }

        @Test
        @DisplayName("Should handle missing meta tags")
        void shouldHandleMissingMetaTags() throws IOException {
            var parser = parse("<html><head></head><body></body></html>");
            assertThat(parser.getMetaKeywords()).isEmpty();
            assertThat(parser.getMetaDescription()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Link Extraction")
    class LinkExtraction {

        @Test
        @DisplayName("Should extract anchor links")
        void shouldExtractAnchorLinks() throws IOException {
            var parser = parse("<html><body><a href=\"http://example.com\">Example</a></body></html>");
            List<Link> links = parser.getLinks("HTML", new URL("http://test.com"));
            assertThat(links).hasSize(1);
            assertThat(links.get(0).getUrl()).isEqualTo("http://example.com");
            assertThat(links.get(0).getLinkType()).isEqualTo(Link.LinkType.Anchor);
        }

        @Test
        @DisplayName("Should extract anchor link title from text content")
        void shouldExtractAnchorLinkTitle() throws IOException {
            var parser = parse("<html><body><a href=\"http://example.com\">Click Me</a></body></html>");
            List<Link> links = parser.getLinks("HTML", new URL("http://test.com"));
            assertThat(links).hasSize(1);
            assertThat(links.get(0).getTitle()).isEqualTo("Click Me");
        }

        @Test
        @DisplayName("Should extract image links")
        void shouldExtractImageLinks() throws IOException {
            var parser = parse("<html><body><img src=\"http://example.com/img.png\"></body></html>");
            List<Link> links = parser.getLinks("HTML", new URL("http://test.com"));
            assertThat(links).anyMatch(l -> l.getLinkType() == Link.LinkType.Image);
        }

        @Test
        @DisplayName("Should recognize stylesheet link type")
        void shouldRecognizeStylesheetLinkType() throws IOException {
            var parser = parse("<html><head><link rel=\"stylesheet\" href=\"style.css\"></head><body></body></html>");
            List<Link> links = parser.getLinks("HTML", new URL("http://test.com"));
            assertThat(links).anyMatch(l -> l.getLinkType() == Link.LinkType.StyleSheet);
        }

        @Test
        @DisplayName("Should resolve relative URLs")
        void shouldResolveRelativeUrls() throws IOException {
            var parser = parse("<html><body><a href=\"/page\">Link</a></body></html>");
            List<Link> links = parser.getLinks("HTML", new URL("http://example.com"));
            assertThat(links.get(0).getUrl()).isEqualTo("http://example.com/page");
        }

        @Test
        @DisplayName("Should extract links with constraint")
        void shouldExtractLinksWithConstraint() throws IOException {
            var parser = parse("<html><body><a href=\"http://a.com\">A</a><img src=\"http://b.com/img.png\"></body></html>");
            var constraint = new TypeLinkConstraint(Link.LinkType.Anchor);
            List<Link> links = parser.getLinks("HTML", new URL("http://test.com"), constraint, null);
            assertThat(links).allMatch(l -> l.getLinkType() == Link.LinkType.Anchor);
        }

        @Test
        @DisplayName("Should handle page with no links")
        void shouldHandlePageWithNoLinks() throws IOException {
            var parser = parse("<html><body><p>No links here</p></body></html>");
            List<Link> links = parser.getLinks("HTML", new URL("http://test.com"));
            assertThat(links).isEmpty();
        }

        @Test
        @DisplayName("Should return string list from simple getLinks")
        void shouldReturnStringListFromSimpleGetLinks() throws IOException {
            var parser = parse("<html><body><a href=\"http://a.com\">A</a><a href=\"http://b.com\">B</a></body></html>");
            List<String> links = parser.getLinks("HTML");
            assertThat(links).containsExactly("http://a.com", "http://b.com");
        }
    }

    @Nested
    @DisplayName("Redirect Detection")
    class RedirectDetection {

        @Test
        @DisplayName("Should detect meta refresh redirect")
        void shouldDetectMetaRefreshRedirect() throws Exception {
            var parser = parse("<html><head><meta http-equiv=\"Refresh\" content=\"5; URL=http://example.com/new\"></head><body></body></html>");
            String redirect = parser.getRedirectURL("http://example.com/old");
            assertThat(redirect).isEqualTo("http://example.com/new");
        }

        @Test
        @DisplayName("Should resolve relative redirect URL")
        void shouldResolveRelativeRedirectUrl() throws Exception {
            var parser = parse("<html><head><meta http-equiv=\"Refresh\" content=\"0; url=newpage.html\"></head><body></body></html>");
            String redirect = parser.getRedirectURL("http://example.com/dir/old.html");
            assertThat(redirect).isEqualTo("http://example.com/dir/newpage.html");
        }

        @Test
        @DisplayName("Should return null when no redirect")
        void shouldReturnNullWhenNoRedirect() throws Exception {
            var parser = parse("<html><head></head><body></body></html>");
            String redirect = parser.getRedirectURL("http://example.com");
            assertThat(redirect).isNull();
        }
    }

    @Nested
    @DisplayName("Structured Data Extraction")
    class StructuredDataExtraction {

        @Test
        @DisplayName("Should extract JSON-LD structured data")
        void shouldExtractJsonLd() throws IOException {
            var parser = parse("<html><head><script type=\"application/ld+json\">{\"@type\":\"Organization\",\"name\":\"Test\"}</script></head><body></body></html>");
            List<JsonNode> data = parser.getStructuredData();
            assertThat(data).hasSize(1);
            assertThat(data.get(0).get("@type").asText()).isEqualTo("Organization");
            assertThat(data.get(0).get("name").asText()).isEqualTo("Test");
        }

        @Test
        @DisplayName("Should handle multiple JSON-LD blocks")
        void shouldHandleMultipleJsonLdBlocks() throws IOException {
            var parser = parse("<html><head>" +
                    "<script type=\"application/ld+json\">{\"@type\":\"A\"}</script>" +
                    "<script type=\"application/ld+json\">{\"@type\":\"B\"}</script>" +
                    "</head><body></body></html>");
            List<JsonNode> data = parser.getStructuredData();
            assertThat(data).hasSize(2);
        }

        @Test
        @DisplayName("Should handle malformed JSON-LD gracefully")
        void shouldHandleMalformedJsonLd() throws IOException {
            var parser = parse("<html><head><script type=\"application/ld+json\">{broken json</script></head><body></body></html>");
            List<JsonNode> data = parser.getStructuredData();
            assertThat(data).isEmpty();
        }

        @Test
        @DisplayName("Should ignore non-JSON-LD script tags")
        void shouldIgnoreNonJsonLdScripts() throws IOException {
            var parser = parse("<html><head><script type=\"text/javascript\">var x = 1;</script></head><body></body></html>");
            List<JsonNode> data = parser.getStructuredData();
            assertThat(data).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when no page parsed")
        void shouldReturnEmptyWhenNoPageParsed() throws IOException {
            var parser = parse("<html><body>No JSON-LD here</body></html>");
            List<JsonNode> data = parser.getStructuredData();
            assertThat(data).isEmpty();
        }
    }

    @Nested
    @DisplayName("Open Graph Tags")
    class OpenGraphTags {

        @Test
        @DisplayName("Should extract OG meta tags")
        void shouldExtractOgTags() throws IOException {
            var parser = parse("<html><head>" +
                    "<meta property=\"og:title\" content=\"My Title\">" +
                    "<meta property=\"og:description\" content=\"My Description\">" +
                    "<meta property=\"og:image\" content=\"http://example.com/img.png\">" +
                    "</head><body></body></html>");
            Map<String, String> og = parser.getOpenGraphTags();
            assertThat(og).containsEntry("og:title", "My Title");
            assertThat(og).containsEntry("og:description", "My Description");
            assertThat(og).containsEntry("og:image", "http://example.com/img.png");
        }

        @Test
        @DisplayName("Should return empty map when no OG tags")
        void shouldReturnEmptyMapWhenNoOgTags() throws IOException {
            var parser = parse("<html><head></head><body></body></html>");
            assertThat(parser.getOpenGraphTags()).isEmpty();
        }

        @Test
        @DisplayName("Should ignore non-OG meta tags")
        void shouldIgnoreNonOgMetaTags() throws IOException {
            var parser = parse("<html><head><meta property=\"twitter:card\" content=\"summary\"></head><body></body></html>");
            assertThat(parser.getOpenGraphTags()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Canonical URL")
    class CanonicalUrl {

        @Test
        @DisplayName("Should extract canonical URL")
        void shouldExtractCanonicalUrl() throws IOException {
            var parser = parse("<html><head><link rel=\"canonical\" href=\"http://example.com/page\"></head><body></body></html>");
            assertThat(parser.getCanonicalUrl()).isEqualTo("http://example.com/page");
        }

        @Test
        @DisplayName("Should return null when no canonical link")
        void shouldReturnNullWhenNoCanonical() throws IOException {
            var parser = parse("<html><head></head><body></body></html>");
            assertThat(parser.getCanonicalUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("Headings Extraction")
    class HeadingsExtraction {

        @Test
        @DisplayName("Should extract headings by level")
        void shouldExtractHeadingsByLevel() throws IOException {
            var parser = parse("<html><body><h1>Title</h1><h2>Subtitle</h2><h3>Section</h3></body></html>");
            List<GenericKeyValue<String, String>> headings = parser.getHeadings();
            assertThat(headings).hasSize(3);
            assertThat(headings.get(0).getKey()).isEqualTo("h1");
            assertThat(headings.get(0).getValue()).isEqualTo("Title");
            assertThat(headings.get(1).getKey()).isEqualTo("h2");
            assertThat(headings.get(1).getValue()).isEqualTo("Subtitle");
            assertThat(headings.get(2).getKey()).isEqualTo("h3");
            assertThat(headings.get(2).getValue()).isEqualTo("Section");
        }

        @Test
        @DisplayName("Should return empty list when no headings")
        void shouldReturnEmptyListWhenNoHeadings() throws IOException {
            var parser = parse("<html><body><p>No headings</p></body></html>");
            assertThat(parser.getHeadings()).isEmpty();
        }

        @Test
        @DisplayName("Should skip empty headings")
        void shouldSkipEmptyHeadings() throws IOException {
            var parser = parse("<html><body><h1>Real</h1><h2>  </h2></body></html>");
            List<GenericKeyValue<String, String>> headings = parser.getHeadings();
            assertThat(headings).hasSize(1);
            assertThat(headings.get(0).getValue()).isEqualTo("Real");
        }
    }

    @Nested
    @DisplayName("Texts By Tag")
    class TextsByTag {

        @Test
        @DisplayName("Should extract individual text blocks per tag")
        void shouldExtractIndividualTextBlocks() throws IOException {
            var parser = parse("<html><body><p>First</p><p>Second</p><p>Third</p></body></html>");
            List<String> texts = parser.getTextsByTag("p");
            assertThat(texts).containsExactly("First", "Second", "Third");
        }

        @Test
        @DisplayName("Should skip empty tags")
        void shouldSkipEmptyTags() throws IOException {
            var parser = parse("<html><body><p>Content</p><p></p><p>More</p></body></html>");
            List<String> texts = parser.getTextsByTag("p");
            assertThat(texts).containsExactly("Content", "More");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle malformed HTML")
        void shouldHandleMalformedHtml() throws IOException {
            var parser = parse("<html><body><p>Unclosed paragraph<div>Another</div></body></html>");
            String text = parser.getBodyText();
            assertThat(text).contains("Unclosed paragraph");
            assertThat(text).contains("Another");
        }

        @Test
        @DisplayName("Should handle empty HTML")
        void shouldHandleEmptyHtml() throws IOException {
            var parser = parse("<html></html>");
            assertThat(parser.getBodyText()).isEmpty();
        }

        @Test
        @DisplayName("Should handle reset and reparse")
        void shouldHandleResetAndReparse() throws IOException {
            var parser = parse("<html><body><p>First</p></body></html>");
            assertThat(parser.getBodyText()).contains("First");
            parser.setHtmlPage("<html><body><p>Second</p></body></html>");
            assertThat(parser.getBodyText()).contains("Second");
        }

        @Test
        @DisplayName("Should handle reset on fresh parser")
        void shouldHandleResetOnFreshParser() {
            HTMLParser parser = new HTMLParser();
            assertThatCode(parser::reset).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle plain text source")
        void shouldHandlePlainTextSource() throws IOException {
            HTMLParser parser = new HTMLParser();
            parser.setSourceFromPlainText("Just some text");
            assertThat(parser.getBodyText()).contains("Just some text");
        }

        @Test
        @DisplayName("testForXML should detect XML tags")
        void testForXmlShouldDetectTags() {
            assertThat(HTMLParser.testForXML("<html>")).isTrue();
            assertThat(HTMLParser.testForXML("<div class=\"test\">")).isTrue();
            assertThat(HTMLParser.testForXML("no tags here")).isFalse();
            assertThat(HTMLParser.testForXML("1 < 2 but no close")).isFalse();
        }
    }
}
