package com.kareem.GitMatch.harvester;

import com.kareem.GitMatch.core.entity.NewsItem;
import com.kareem.GitMatch.core.enums.ContentSource;
import com.kareem.GitMatch.core.repository.NewsItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class NewsHarvester {

    private static final Logger log = LoggerFactory.getLogger(NewsHarvester.class);


    private static final int STARTUP_LIMIT = 5;

    private static final int SCHEDULED_LIMIT = 10;


    private static final Pattern OG_IMAGE_PATTERN = Pattern.compile(
            "<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']+)[\"']"
            + "|<meta[^>]*content=[\"']([^\"']+)[\"'][^>]*property=[\"']og:image[\"']",
            Pattern.CASE_INSENSITIVE);

    private final NewsItemRepository newsRepository;
    private final ObjectMapper objectMapper;
    private final RestClient newsRestClient;

    public NewsHarvester(NewsItemRepository newsRepository,
                         ObjectMapper objectMapper) {
        this.newsRepository = newsRepository;
        this.objectMapper = objectMapper;
        this.newsRestClient = RestClient.builder().build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("App started — triggering small seed news harvest...");
        harvestNews(STARTUP_LIMIT);
        backfillMissingImages();
    }

    @Scheduled(cron = "${gitmatch.harvester.cron.news}")
    public void scheduledHarvest() {
        log.info("Scheduled news harvest starting...");
        harvestNews(SCHEDULED_LIMIT);
    }

    private void harvestNews(int limit) {
        harvestDevTo(limit);
        harvestHackerNews(limit);
        harvestMediumRss(limit);
        harvestGoogleNewsRss(limit);
        harvestResearchLabRss(limit);
        harvestEngineeringAlphaRss(limit);
        harvestIndustryPulseRss(limit);
        log.info("News harvest complete from all sources.");
    }


    private void backfillMissingImages() {
        List<NewsItem> missing = newsRepository.findByImageUrlIsNull();
        if (missing.isEmpty()) return;
        log.info("Backfilling og:image for {} news items with no image...", missing.size());
        int filled = 0;
        for (NewsItem item : missing) {
            String img = scrapeOgImage(item.getSourceUrl());
            if (img != null) {
                item.setImageUrl(img);
                newsRepository.save(item);
                filled++;
            }
        }
        log.info("Backfill complete: {}/{} items now have images", filled, missing.size());
    }

    // ─── Dev.to ────────────────────────────────────────────────

    private void harvestDevTo(int limit) {
        try {
            String response = newsRestClient.get()
                    .uri("https://dev.to/api/articles?per_page=" + limit + "&top=7")
                    .retrieve()
                    .body(String.class);

            JsonNode articles = objectMapper.readTree(response);
            if (!articles.isArray()) return;

            int newCount = 0;
            for (JsonNode article : articles) {
                if (newCount >= limit) break;
                String url = article.path("url").asText();
                if (newsRepository.existsBySourceUrl(url)) continue;

                String imageUrl = article.path("cover_image").asText(null);
                if (imageUrl == null) {
                    imageUrl = article.path("social_image").asText(null);
                }
                if (imageUrl == null) {
                    imageUrl = scrapeOgImage(url);
                }

                NewsItem newsItem = new NewsItem(
                        url,
                        article.path("title").asText(),
                        article.path("user").path("name").asText(null),
                        LocalDateTime.now(),
                        false,
                        ContentSource.DEVTO,
                        imageUrl
                );
                newsRepository.save(newsItem);
                newCount++;
            }
            log.info("Dev.to harvest: {} new articles added", newCount);
        } catch (Exception e) {
            log.error("Dev.to harvest failed: {}", e.getMessage());
        }
    }

    // ─── Hacker News ───────────────────────────────────────────

    private void harvestHackerNews(int limit) {
        try {
            String idsJson = newsRestClient.get()
                    .uri("https://hacker-news.firebaseio.com/v0/topstories.json")
                    .retrieve()
                    .body(String.class);

            JsonNode ids = objectMapper.readTree(idsJson);
            if (!ids.isArray()) return;

            int newCount = 0;
            int maxCheck = Math.min(limit * 3, ids.size()); // check more since some may be duplicates

            for (int i = 0; i < maxCheck && newCount < limit; i++) {
                try {
                    long storyId = ids.get(i).asLong();
                    String storyJson = newsRestClient.get()
                            .uri("https://hacker-news.firebaseio.com/v0/item/" + storyId + ".json")
                            .retrieve()
                            .body(String.class);

                    JsonNode story = objectMapper.readTree(storyJson);
                    String url = story.path("url").asText(null);
                    String title = story.path("title").asText(null);
                    if (url == null || title == null || url.isEmpty()) continue;
                    if (newsRepository.existsBySourceUrl(url)) continue;

                    String imageUrl = scrapeOgImage(url);

                    NewsItem newsItem = new NewsItem(
                            url, title, story.path("by").asText(null),
                            LocalDateTime.now(), false, ContentSource.HACKERNEWS, imageUrl
                    );
                    newsRepository.save(newsItem);
                    newCount++;
                } catch (Exception e) {
                    log.warn("Failed to fetch HN story at index {}: {}", i, e.getMessage());
                }
            }
            log.info("Hacker News harvest: {} new articles added", newCount);
        } catch (Exception e) {
            log.error("Hacker News harvest failed: {}", e.getMessage());
        }
    }

    // ─── Medium (RSS) ──────────────────────────────────────────

    private void harvestMediumRss(int limit) {
        List<String> mediumFeeds = List.of(
                "https://medium.com/feed/tag/programming",
                "https://medium.com/feed/tag/software-engineering"
        );

        int totalNew = 0;
        int perFeed = Math.max(limit / mediumFeeds.size(), 2);
        for (String feedUrl : mediumFeeds) {
            try {
                totalNew += harvestRssFeed(feedUrl, ContentSource.MEDIUM, perFeed);
            } catch (Exception e) {
                log.error("Medium RSS harvest failed for {}: {}", feedUrl, e.getMessage());
            }
        }
        log.info("Medium harvest: {} new articles added", totalNew);
    }

    // ─── Google News (RSS) ─────────────────────────────────────

    private void harvestGoogleNewsRss(int limit) {
        List<String> googleFeeds = List.of(
                "https://news.google.com/rss/search?q=software+development&hl=en-US&gl=US&ceid=US:en",
                "https://news.google.com/rss/search?q=github+release+notes&hl=en-US&gl=US&ceid=US:en"
        );

        int totalNew = 0;
        int perFeed = Math.max(limit / googleFeeds.size(), 2);
        for (String feedUrl : googleFeeds) {
            try {
                totalNew += harvestRssFeed(feedUrl, ContentSource.RELEASE_NOTES, perFeed);
            } catch (Exception e) {
                log.error("Google News RSS harvest failed for {}: {}", feedUrl, e.getMessage());
            }
        }
        log.info("Google News harvest: {} new articles added", totalNew);
    }

    // ─── Research & Lab (AI/ML primary sources) ───────────────

    private void harvestResearchLabRss(int limit) {
        record FeedDef(String url, ContentSource source) {}
        List<FeedDef> feeds = List.of(
                new FeedDef("https://huggingface.co/blog/feed.xml", ContentSource.HUGGINGFACE),
                new FeedDef("https://openai.com/news/rss.xml", ContentSource.OPENAI),
                new FeedDef("https://rss.arxiv.org/rss/cs.AI", ContentSource.ARXIV)
        );

        int totalNew = 0;
        int perFeed = Math.max(limit / feeds.size(), 2);
        for (FeedDef fd : feeds) {
            try {
                totalNew += harvestRssFeed(fd.url(), fd.source(), perFeed);
            } catch (Exception e) {
                log.error("Research & Lab RSS harvest failed for {}: {}", fd.url(), e.getMessage());
            }
        }
        log.info("Research & Lab harvest: {} new articles added", totalNew);
    }

    // ─── Engineering Alpha (Professional Dev) ──────────────────

    private void harvestEngineeringAlphaRss(int limit) {
        record FeedDef(String url, ContentSource source) {}
        List<FeedDef> feeds = List.of(
                new FeedDef("https://feed.infoq.com/ai-ml-data-eng/articles", ContentSource.INFOQ),
                new FeedDef("https://blog.pragmaticengineer.com/rss/", ContentSource.PRAGMATIC_ENGINEER)
        );

        int totalNew = 0;
        int perFeed = Math.max(limit / feeds.size(), 2);
        for (FeedDef fd : feeds) {
            try {
                totalNew += harvestRssFeed(fd.url(), fd.source(), perFeed);
            } catch (Exception e) {
                log.error("Engineering Alpha RSS harvest failed for {}: {}", fd.url(), e.getMessage());
            }
        }
        log.info("Engineering Alpha harvest: {} new articles added", totalNew);
    }

    // ─── Industry Pulse (Curated Tech) ─────────────────────────

    private void harvestIndustryPulseRss(int limit) {
        record FeedDef(String url, ContentSource source) {}
        List<FeedDef> feeds = List.of(
                new FeedDef("https://www.theverge.com/rss/ai-artificial-intelligence/index.xml", ContentSource.THE_VERGE),
                new FeedDef("https://techcrunch.com/category/artificial-intelligence/feed/", ContentSource.TECHCRUNCH)
        );

        int totalNew = 0;
        int perFeed = Math.max(limit / feeds.size(), 2);
        for (FeedDef fd : feeds) {
            try {
                totalNew += harvestRssFeed(fd.url(), fd.source(), perFeed);
            } catch (Exception e) {
                log.error("Industry Pulse RSS harvest failed for {}: {}", fd.url(), e.getMessage());
            }
        }
        log.info("Industry Pulse harvest: {} new articles added", totalNew);
    }

    // ─── Shared RSS parser ─────────────────────────────────────

    private int harvestRssFeed(String feedUrl, ContentSource contentSource, int limit) {
        try {
            String xml = newsRestClient.get()
                    .uri(feedUrl)
                    .header("User-Agent", "GitMatch/1.0")
                    .retrieve()
                    .body(String.class);

            if (xml == null || xml.isBlank()) return 0;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            NodeList items = doc.getElementsByTagName("item");
            int newCount = 0;

            for (int i = 0; i < items.getLength() && newCount < limit; i++) {
                try {
                    Element item = (Element) items.item(i);
                    String title = getElementText(item, "title");
                    String link = getElementText(item, "link");
                    String author = getElementText(item, "dc:creator");
                    if (author == null) author = getElementText(item, "author");

                    if (link == null || title == null || link.isBlank()) continue;
                    if (newsRepository.existsBySourceUrl(link)) continue;

                    boolean isRelease = title.toLowerCase().matches(".*\\b(release|v\\d|update|changelog|what's new)\\b.*");
                    String pubDate = getElementText(item, "pubDate");
                    LocalDateTime published = parseRssDate(pubDate);

                    // Try to extract image from media tags, fall back to og:image
                    String imageUrl = getMediaImage(item);
                    if (imageUrl == null) {
                        imageUrl = scrapeOgImage(link);
                    }

                    NewsItem newsItem = new NewsItem(
                            link, title, author,
                            published != null ? published : LocalDateTime.now(),
                            isRelease, contentSource, imageUrl
                    );
                    newsRepository.save(newsItem);
                    newCount++;
                } catch (Exception e) {
                    log.warn("Failed to parse RSS item at index {} from {}: {}", i, feedUrl, e.getMessage());
                }
            }
            return newCount;
        } catch (Exception e) {
            log.error("RSS feed parsing failed for {}: {}", feedUrl, e.getMessage());
            return 0;
        }
    }


    private String getMediaImage(Element item) {
        // Try media:content
        NodeList mediaContent = item.getElementsByTagName("media:content");
        if (mediaContent.getLength() > 0) {
            Element media = (Element) mediaContent.item(0);
            String url = media.getAttribute("url");
            if (url != null && !url.isBlank()) return url;
        }
        // Try media:thumbnail
        NodeList mediaThumbnail = item.getElementsByTagName("media:thumbnail");
        if (mediaThumbnail.getLength() > 0) {
            Element thumb = (Element) mediaThumbnail.item(0);
            String url = thumb.getAttribute("url");
            if (url != null && !url.isBlank()) return url;
        }
        // Try enclosure (used by some feeds for images)
        NodeList enclosures = item.getElementsByTagName("enclosure");
        if (enclosures.getLength() > 0) {
            Element enc = (Element) enclosures.item(0);
            String type = enc.getAttribute("type");
            if (type != null && type.startsWith("image")) {
                String url = enc.getAttribute("url");
                if (url != null && !url.isBlank()) return url;
            }
        }
        return null;
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0 && nodeList.item(0).getTextContent() != null) {
            String text = nodeList.item(0).getTextContent().trim();
            return text.isEmpty() ? null : text;
        }
        return null;
    }


    private String scrapeOgImage(String articleUrl) {
        if (articleUrl == null || articleUrl.isBlank()) return null;
        try {
            String html = newsRestClient.get()
                    .uri(articleUrl)
                    .header("User-Agent", "GitMatch/1.0")
                    .header("Accept", "text/html")
                    .retrieve()
                    .body(String.class);

            if (html == null || html.isBlank()) return null;

            // Only search the first 50KB (the <head> section)
            String head = html.length() > 50_000 ? html.substring(0, 50_000) : html;
            Matcher matcher = OG_IMAGE_PATTERN.matcher(head);
            if (matcher.find()) {
                String url = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                if (url != null && !url.isBlank() && url.startsWith("http")) {
                    return url;
                }
            }
        } catch (Exception e) {
            log.debug("Could not scrape og:image from {}: {}", articleUrl, e.getMessage());
        }
        return null;
    }

    private LocalDateTime parseRssDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            DateTimeFormatter rssFormatter = DateTimeFormatter.ofPattern(
                    "EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            return LocalDateTime.parse(dateStr.trim(), rssFormatter);
        } catch (Exception e) {
            try {
                DateTimeFormatter altFormatter = DateTimeFormatter.ofPattern(
                        "EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
                return LocalDateTime.parse(dateStr.trim(), altFormatter);
            } catch (Exception e2) {
                log.debug("Could not parse date '{}', using current time", dateStr);
                return null;
            }
        }
    }
}
