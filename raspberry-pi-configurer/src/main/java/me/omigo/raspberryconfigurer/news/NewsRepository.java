package me.omigo.raspberryconfigurer.news;

import me.omigo.raspberryconfigurer.news.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    @Query("select n from News n where n.created between ?1 and ?2")
    List<News> findByCreatedIn(LocalDateTime nowMinus24Hours, LocalDateTime now);

    @Query("select (count(n) > 0) from News n where upper(n.title) = upper(?1) and upper(n.description) = upper(?2)")
    boolean existsByTitleIgnoreCaseAndDescriptionIgnoreCase(String title, String description);
}
