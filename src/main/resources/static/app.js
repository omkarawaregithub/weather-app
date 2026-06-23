const state = {
  weather: null,
  isLoading: false,
  error: null,
  city: 'Pune',
  mode: 'auto',
};

const themeMap = {
  Thunderstorm: 'thunderstorm',
  Drizzle: 'rainy',
  Rain: 'rainy',
  Snow: 'snow',
  Mist: 'fog',
  Smoke: 'fog',
  Haze: 'fog',
  Dust: 'fog',
  Fog: 'fog',
  Sand: 'fog',
  Ash: 'fog',
  Clouds: 'cloudy',
  Clear: 'clear',
};

const icons = {
  '01d': '☀️',
  '01n': '🌙',
  '02d': '⛅',
  '02n': '🌙',
  '03d': '☁️',
  '03n': '☁️',
  '04d': '☁️',
  '04n': '☁️',
  '09d': '🌧️',
  '09n': '🌧️',
  '10d': '🌦️',
  '10n': '🌦️',
  '11d': '⛈️',
  '11n': '⛈️',
  '13d': '❄️',
  '13n': '❄️',
  '50d': '🌫️',
  '50n': '🌫️',
};

const $ = selector => document.querySelector(selector);

const applyTheme = (condition, night) => {
  const theme = themeMap[condition] || 'clear';
  if (state.mode === 'auto') {
    document.documentElement.dataset.theme = theme;
  }
  document.documentElement.dataset.night = night ? 'true' : 'false';
};

const formatForecastItems = (items, daily = false) => {
  if (!Array.isArray(items)) return '';
  return items.map(item => {
    const iconSymbol = icons[item.icon] || '❔';
    return `
      <div class="forecast-card ${daily ? 'daily' : ''}">
        <div class="forecast-time">${daily ? item.day : item.time}</div>
        <div class="forecast-icon">${iconSymbol}</div>
        <div class="forecast-detail">${daily ? `${item.minTemperatureCelsius}° / ${item.maxTemperatureCelsius}°` : `${item.temperatureCelsius}°`}</div>
        <div class="forecast-cond">${item.condition}</div>
      </div>
    `;
  }).join('');
};

const renderWeather = () => {
  const weatherCard = $('#weather-card');
  if (!state.weather) {
    weatherCard.classList.add('hidden');
    return;
  }

  const display = state.weather;
  applyTheme(display.condition, display.night);
  weatherCard.classList.remove('hidden');

  $('#city-name').textContent = `${display.city}${display.country ? ', ' + display.country : ''}`;
  $('#date-time').textContent = display.localTime || 'Current conditions';
  $('#current-temp').textContent = `${display.temperatureCelsius}°`;
  $('#weather-icon').textContent = icons[display.icon] || '☀️';
  $('#weather-desc').textContent = display.description || '--';
  $('#condition-tag').textContent = display.condition;
  $('#cache-tag').textContent = display.cached ? 'Cached' : 'Live';
  $('#feels-like').textContent = `${display.feelsLikeCelsius}°`;
  $('#humidity').textContent = `${display.humidity}%`;
  $('#wind').textContent = `${display.windSpeedKph} km/h`;
  $('#pressure').textContent = `${display.pressure} hPa`;
  $('#visibility').textContent = `${display.visibilityKm} km`;
  $('#uv-index').textContent = `${display.uvIndex}`;
  $('#sunrise').textContent = display.sunrise || '--';
  $('#sunset').textContent = display.sunset || '--';

  $('#hourly-forecast').innerHTML = formatForecastItems(display.hourlyForecast);
  $('#daily-forecast').innerHTML = formatForecastItems(display.dailyForecast, true);
};

const setLoading = loading => {
  state.isLoading = loading;
  $('#loader').classList.toggle('hidden', !loading);
};

const setError = message => {
  state.error = message;
  $('#error-message').textContent = message || '';
  $('#error-panel').classList.toggle('hidden', !message);
};

const getGeoLocation = () => new Promise((resolve, reject) => {
  if (!navigator.geolocation) return reject(new Error('Geolocation not supported by your browser.'));
  // Request high accuracy when available for more precise coordinates
  const options = { enableHighAccuracy: true, timeout: 12000, maximumAge: 0 };
  navigator.geolocation.getCurrentPosition(
    position => {
      const coords = position.coords;
      console.debug('Geolocation obtained:', coords.latitude, coords.longitude, 'accuracy:', coords.accuracy);
      resolve(coords);
    },
    error => {
      console.warn('Geolocation error', error);
      switch (error.code) {
        case error.PERMISSION_DENIED:
          reject(new Error('Location permission denied. Please allow location access and try again.'));
          break;
        case error.POSITION_UNAVAILABLE:
          reject(new Error('Position unavailable. Try again or check your device settings.'));
          break;
        case error.TIMEOUT:
          reject(new Error('Location request timed out. Try again.'));
          break;
        default:
          reject(new Error('Unable to determine location.'));
      }
    },
    options
  );
});

const fetchWeather = async (city, useLocation = false) => {
  setError(null);
  setLoading(true);
  try {
    let query = '';
    if (useLocation) {
      const coords = await getGeoLocation();
      query = `?lat=${coords.latitude}&lon=${coords.longitude}`;
    } else {
      const cityValue = city || $('#city-input').value.trim();
      if (!cityValue) throw new Error('Please enter a city name.');
      query = `?city=${encodeURIComponent(cityValue)}`;
    }

    const response = await fetch(`/api/weather${query}`);
    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || 'Weather service unavailable.');
    }

    state.weather = await response.json();
    renderWeather();
  } catch (error) {
    console.error(error);
    setError(error.message || 'Failed to load weather data.');
  } finally {
    setLoading(false);
  }
};

const setThemeMode = mode => {
  state.mode = mode;
  document.documentElement.dataset.mode = mode;
  $('#theme-button').textContent = mode === 'dark' ? 'Dark mode' : mode === 'light' ? 'Light mode' : 'Auto theme';
  if (state.weather) {
    applyTheme(state.weather.condition, state.weather.night);
  }
};

const initEventListeners = () => {
  $('#search-form').addEventListener('submit', event => {
    event.preventDefault();
    fetchWeather($('#city-input').value.trim());
  });
  $('#search-button').addEventListener('click', event => {
    event.preventDefault();
    fetchWeather($('#city-input').value.trim());
  });
  $('#current-location-button').addEventListener('click', () => fetchWeather('', true));
  $('#refresh-button').addEventListener('click', () => fetchWeather($('#city-input').value.trim() || state.city));
  $('#clear-button').addEventListener('click', () => {
    $('#city-input').value = '';
    state.weather = null;
    $('#weather-card').classList.add('hidden');
    setError(null);
  });
  $('#theme-button').addEventListener('click', () => {
    const next = state.mode === 'dark' ? 'light' : state.mode === 'light' ? 'auto' : 'dark';
    setThemeMode(next);
  });
  $('#close-error').addEventListener('click', () => setError(null));
};

const initialize = () => {
  setThemeMode('auto');
  initEventListeners();
  fetchWeather(state.city);
};

window.addEventListener('DOMContentLoaded', initialize);
