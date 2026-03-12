package com.devbookmark.resource;

import com.devbookmark.resource.dto.LinkPreviewResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;

@Service
public class LinkPreviewService {

    public LinkPreviewResponse preview(String rawUrl) {
        String url = normalizeAndValidateUrl(rawUrl);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; DevBookmarkBot/1.0)")
                    .timeout((int) Duration.ofSeconds(6).toMillis())
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .get();

            String ogTitle = content(doc, "meta[property=og:title]");
            String ogDesc  = content(doc, "meta[property=og:description]");
            String ogImage = content(doc, "meta[property=og:image]");
            String ogSite  = content(doc, "meta[property=og:site_name]");
            String ogType  = content(doc, "meta[property=og:type]");

            String title = firstNonBlank(ogTitle, doc.title());
            String desc  = firstNonBlank(ogDesc, content(doc, "meta[name=description]"));
            String img   = absolutize(url, ogImage);

            return LinkPreviewResponse.builder()
                    .url(url)
                    .title(title)
                    .description(desc)
                    .image(img)
                    .siteName(ogSite)
                    .type(ogType)
                    .build();

        } catch (Exception e) {
            // Don’t fail resource creation because preview failed
            return LinkPreviewResponse.builder()
                    .url(url)
                    .title(null)
                    .description(null)
                    .image(null)
                    .siteName(null)
                    .type(null)
                    .build();
        }
    }

    private String content(Document doc, String cssQuery) {
        var el = doc.selectFirst(cssQuery);
        if (el == null) return null;
        String v = el.attr("content");
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a.trim();
        if (b != null && !b.isBlank()) return b.trim();
        return null;
    }

    private String absolutize(String baseUrl, String maybeUrl) {
        if (maybeUrl == null || maybeUrl.isBlank()) return null;
        try {
            URI u = URI.create(maybeUrl);
            if (u.isAbsolute()) return maybeUrl;
            return URI.create(baseUrl).resolve(maybeUrl).toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    // Basic safety: allow only http/https and valid URI
    private String normalizeAndValidateUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) throw new IllegalArgumentException("url is required");
        String trimmed = rawUrl.trim();

        URI uri = URI.create(trimmed);
        String scheme = uri.getScheme();
        if (scheme == null) throw new IllegalArgumentException("Invalid url");
        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))
            throw new IllegalArgumentException("Only http/https allowed");

        return uri.toString();
    }
}