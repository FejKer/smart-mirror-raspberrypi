package me.omigo.raspberryconfigurer.news.query;

import java.util.List;

public interface NewsQueryService {
    List<NewsQueryDto> getNews();
}
