package com.tisqra.event.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Location value object
 * Embedded in Event entity
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String zipCode;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 500)
    private String mapsUrl;

    // Business methods
    public Double getDistance(Location other) {
        if (this.latitude == null || this.longitude == null || 
            other.latitude == null || other.longitude == null) {
            return null;
        }

        // Haversine formula for distance calculation
        double earthRadius = 6371; // kilometers
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(this.latitude)) * 
                   Math.cos(Math.toRadians(other.latitude)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    public String getGoogleMapsUrl() {
        if (latitude != null && longitude != null) {
            return String.format("https://www.google.com/maps?q=%f,%f", latitude, longitude);
        }
        return mapsUrl;
    }

    public String getFullAddress() {
        return String.format("%s, %s, %s %s, %s", 
            address, city, state != null ? state : "", zipCode != null ? zipCode : "", country);
    }
}
