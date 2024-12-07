package me.omigo.raspberryconfigurer.news.in;

import lombok.RequiredArgsConstructor;
import me.omigo.raspberryconfigurer.news.query.NewsQueryDto;
import me.omigo.raspberryconfigurer.news.query.NewsQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsRestController {
    private final NewsQueryService newsQueryService;

    @GetMapping
    List<NewsQueryDto> getNews() {
        return newsQueryService.getNews();
    }

}
