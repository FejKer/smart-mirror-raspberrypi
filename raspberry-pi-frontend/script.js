const weatherEndpoint = `http://${window.location.hostname}:8080/api/v1/weather`;
const newsEndpoint = `http://${window.location.hostname}:8080/api/v1/news`;

let lastWeatherData = null;
let lastNewsData = null;

setInterval(function() {
    fetchWeather();
    fetchNews();
}, 2 * 1000); // 60 * 1000 milsec

async function fetchWeather() {
  try {
    const response = await fetch(weatherEndpoint);
    if (!response.ok) throw new Error(`Failed to fetch weather data: ${response.status}`);
    const weatherData = await response.json();

    // Only update the UI if the weather data has changed
    if (JSON.stringify(weatherData) !== JSON.stringify(lastWeatherData)) {
      lastWeatherData = weatherData;

      // Update Weather Section
      document.getElementById("city").textContent = `Miasto: ${weatherData.city}`;
      document.getElementById("temperature").textContent = `Temperatura: ${weatherData.temperature.toFixed(1)}Â°C`;
      document.getElementById("weather-type").textContent = `Pogoda: ${weatherData.weatherType}`;
      document.getElementById("humidity").textContent = `Wilgotność: ${weatherData.humidity}%`;
      document.getElementById("wind-speed").textContent = `Wiatr: ${weatherData.windSpeed} m/s`;
      document.getElementById("weather-icon").src = weatherData.urlToIcon;
    }
  } catch (error) {
    console.error(error);
  }
}

async function fetchNews() {
  try {
    const response = await fetch(newsEndpoint);
    if (!response.ok) throw new Error(`Failed to fetch news data: ${response.status}`);
    const newsData = await response.json();

    // Only update the UI if the news data has changed
    if (JSON.stringify(newsData) !== JSON.stringify(lastNewsData)) {
      lastNewsData = newsData;

      const carousel = document.getElementById("carousel");
      carousel.innerHTML = ''; // Clear existing carousel items

      newsData.forEach((news, index) => {
        const newsItem = document.createElement("div");
        newsItem.classList.add("carousel-item");

        // Add 'active' class only to the first item
        if (index === 0) {
          newsItem.classList.add("active");
        }

        newsItem.innerHTML = `
          <img src="${news.urlToImage}" alt="${news.title}" class="d-block w-100">
          <div class="carousel-caption d-none d-md-block bg-dark bg-opacity-50 rounded">
            <h5>${news.title}</h5>
            <p>${news.description}</p>
            <small><strong>Źródło:</strong> ${news.source}</small>
          </div>
        `;
        carousel.appendChild(newsItem);
      });
    }
  } catch (error) {
    console.error(error);
  }
}

document.addEventListener("DOMContentLoaded", () => {
  fetchWeather();
  fetchNews();
});

