package com.sitka.satellite.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeocodingService {

    private final GeoApiContext context;

    public GeocodingService(@Value("${GOOGLE_API_KEY}") String apiKey) {
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public LatLng getLatLng(String address) throws Exception {
        GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
        if (results.length > 0) {
            return results[0].geometry.location;
        }
        return null;
    }
}
