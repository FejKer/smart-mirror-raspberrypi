package me.omigo.raspberryconfigurer.news;

import me.omigo.raspberryconfigurer.news.domain.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findFirstByOrderByCreatedDesc();
}
