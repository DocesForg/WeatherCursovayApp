package com.docesforg.bura.server.signal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RadioSignalService {
    private final RadioSignalTestRepository repository;
    private final RestClient restClient;

    public RadioSignalService(RadioSignalTestRepository repository) {
        this.repository = repository;
        this.restClient = RestClient.builder().baseUrl("https://api.open-meteo.com").build();
    }

    public record RadioSignalResponse(
            Long id,
            String cityA,
            String cityB,
            double distanceKm,
            double pathLossDb,
            String quality,
            double latencyMs,
            double speedMbps,
            Instant createdAt
    ) {
    }

    public List<RadioSignalResponse> history(long accountId) {
        return repository.findAllByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public RadioSignalResponse calculate(
            long accountId,
            String cityA,
            String cityB,
            double latitudeA,
            double longitudeA,
            double latitudeB,
            double longitudeB,
            double frequencyMhz
    ) {
        double distanceKm = haversineKm(latitudeA, longitudeA, latitudeB, longitudeB);
        double frequency = frequencyMhz <= 0 ? 900.0 : frequencyMhz;
        WeatherSnapshot weatherA = getWeather(latitudeA, longitudeA);
        WeatherSnapshot weatherB = getWeather(latitudeB, longitudeB);
        WeatherSnapshot averageWeather = averageWeather(weatherA, weatherB);

        double pathLossDb = calculatePathLossDb(distanceKm, frequency, averageWeather);
        String quality = classify(pathLossDb);
        double latencyMs = calculateLatencyMs(distanceKm);
        double speedMbps = calculateSpeedMbps(pathLossDb, latencyMs);

        RadioSignalTestEntity entity = new RadioSignalTestEntity();
        entity.setAccountId(accountId);
        entity.setCityA(cityA);
        entity.setCityB(cityB);
        entity.setDistanceKm(distanceKm);
        entity.setPathLossDb(pathLossDb);
        entity.setQuality(quality);
        entity.setCreatedAt(Instant.now());
        return toDto(repository.save(entity), latencyMs, speedMbps);
    }

    private String classify(double pathLossDb) {
        if (pathLossDb < 110) return "Excellent";
        if (pathLossDb < 125) return "Good";
        if (pathLossDb < 140) return "Fair";
        return "Poor";
    }

    private RadioSignalResponse toDto(RadioSignalTestEntity entity) {
        double latencyMs = calculateLatencyMs(entity.getDistanceKm());
        double speedMbps = calculateSpeedMbps(entity.getPathLossDb(), latencyMs);
        return toDto(entity, latencyMs, speedMbps);
    }

    private RadioSignalResponse toDto(RadioSignalTestEntity entity, double latencyMs, double speedMbps) {
        return new RadioSignalResponse(
                entity.getId(),
                entity.getCityA(),
                entity.getCityB(),
                entity.getDistanceKm(),
                entity.getPathLossDb(),
                entity.getQuality(),
                latencyMs,
                speedMbps,
                entity.getCreatedAt()
        );
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLon = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private double calculatePathLossDb(double distanceKm, double frequencyMhz, WeatherSnapshot weather) {
        double distanceMeters = Math.max(distanceKm * 1000.0, 100.0);
        double frequencyHz = Math.max(frequencyMhz * 1_000_000.0, 1.0);
        double fsplDb = 20 * Math.log10(distanceMeters) + 20 * Math.log10(frequencyHz) - 147.55;
        return fsplDb + weather.extraAttenuationDb();
    }

    private double calculateLatencyMs(double distanceKm) {
        final double refractiveIndexAir = 1.0003;
        final double speedOfLight = 299_792_458.0;
        double distanceMeters = distanceKm * 1000.0;
        double speedInAir = speedOfLight / refractiveIndexAir;
        return (distanceMeters / speedInAir) * 1000.0;
    }

    private double calculateSpeedMbps(double pathLossDb, double latencyMs) {
        double qualityFactor = Math.max(0.0, 1.0 - Math.max(pathLossDb - 90.0, 0.0) / 90.0);
        double latencyPenalty = Math.max(0.2, 1.0 - latencyMs / 1000.0);
        return 8.0 + qualityFactor * latencyPenalty * 220.0;
    }

    private WeatherSnapshot averageWeather(WeatherSnapshot a, WeatherSnapshot b) {
        return new WeatherSnapshot(
                (a.temperatureC + b.temperatureC) / 2.0,
                (a.relativeHumidity + b.relativeHumidity) / 2.0,
                (a.rainMm + b.rainMm) / 2.0,
                (a.cloudCover + b.cloudCover) / 2.0
        );
    }

    private WeatherSnapshot getWeather(double latitude, double longitude) {
        try {
            String uri = "/v1/forecast?latitude=" + encode(latitude) +
                    "&longitude=" + encode(longitude) +
                    "&current=temperature_2m,relative_humidity_2m,rain,cloud_cover";
            var root = restClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(com.fasterxml.jackson.databind.JsonNode.class);
            if (root == null || !root.has("current")) return WeatherSnapshot.defaultSnapshot();
            var current = root.get("current");
            double temperature = current.path("temperature_2m").asDouble(15.0);
            double humidity = current.path("relative_humidity_2m").asDouble(60.0);
            double rain = current.path("rain").asDouble(0.0);
            double cloudCover = current.path("cloud_cover").asDouble(50.0);
            return new WeatherSnapshot(temperature, humidity, rain, cloudCover);
        } catch (Exception ignored) {
            return WeatherSnapshot.defaultSnapshot();
        }
    }

    private String encode(double value) {
        return URLEncoder.encode(String.format(java.util.Locale.US, "%.6f", value), StandardCharsets.UTF_8);
    }

    private record WeatherSnapshot(double temperatureC, double relativeHumidity, double rainMm, double cloudCover) {
        static WeatherSnapshot defaultSnapshot() {
            return new WeatherSnapshot(15.0, 60.0, 0.0, 50.0);
        }

        double extraAttenuationDb() {
            double humidityLoss = Math.max(0.0, (relativeHumidity - 40.0) * 0.01);
            double rainLoss = rainMm * 0.18;
            double cloudLoss = cloudCover * 0.005;
            double temperatureLoss = Math.abs(temperatureC - 15.0) * 0.015;
            return humidityLoss + rainLoss + cloudLoss + temperatureLoss;
        }
    }
}
