package com.docesforg.bura.server.signal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "radio_signal_test")
public class RadioSignalTestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private String cityA;

    @Column(nullable = false)
    private String cityB;

    @Column(nullable = false)
    private double distanceKm;

    @Column(nullable = false)
    private double pathLossDb;

    @Column(nullable = false)
    private String quality;

    @Column(nullable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getCityA() { return cityA; }
    public void setCityA(String cityA) { this.cityA = cityA; }
    public String getCityB() { return cityB; }
    public void setCityB(String cityB) { this.cityB = cityB; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public double getPathLossDb() { return pathLossDb; }
    public void setPathLossDb(double pathLossDb) { this.pathLossDb = pathLossDb; }
    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
