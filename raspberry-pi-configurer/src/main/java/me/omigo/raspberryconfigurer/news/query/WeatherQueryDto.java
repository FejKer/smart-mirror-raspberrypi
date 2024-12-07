package me.omigo.raspberryconfigurer.news.query;

public record WeatherQueryDto(String city, Double temperature, String weatherType, String urlToIcon, Double humidity, Double windSpeed) {
}
