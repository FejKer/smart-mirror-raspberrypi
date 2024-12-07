package me.omigo.raspberryconfigurer.news.fetcher;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import me.omigo.raspberryconfigurer.config.Attributes;
import me.omigo.raspberryconfigurer.news.domain.ConfigRefresher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsFetcher {
    private static final String NEWS_API_URL = "https://newsapi.org/v2/everything?q=%s&from=%s&language=%s&apiKey=%s";
    private final RestTemplate restTemplate;
    private final ConfigRefresher configRefresher;

    public List<NewsJson> fetchNews() {
        List<NewsJson> newsJsons = new ArrayList<>();
        for (String category : Arrays.stream(configRefresher.getAttribute(Attributes.NEWS_CATEGORIES).split(",")).limit(12).toList()) {
            NewsJson response = restTemplate.getForObject(buildUrl(category), NewsJson.class);
            response.articles = response.articles
                    .stream()
                    .filter(article -> !article.source.name.contains("Removed"))
                    .limit(10)
                    .toList();
            newsJsons.add(response);
        }
        return newsJsons;
    }

    private String buildUrl(String category) {
        String date = LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String format = String.format(
                NEWS_API_URL,
                category,
                date,
                configRefresher.getAttribute(Attributes.NEWS_LANGUAGE),
                configRefresher.getAttribute(Attributes.NEWS_API_KEY)
        );
        log.info("Executing request to {}", format);
        return format;
    }

    @ToString
    public static class Article {
        public Source source;
        public String author;
        public String title;
        public String description;
        public String url;
        public String urlToImage;
        public Date publishedAt;
        public String content;
    }

    @ToString
    public static class NewsJson {
        public String status;
        public int totalResults;
        public List<Article> articles;
    }

    @ToString
    public static class Source {
        public String id;
        public String name;
    }
}
