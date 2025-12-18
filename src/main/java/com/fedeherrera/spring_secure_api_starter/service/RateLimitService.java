package com.fedeherrera.spring_secure_api_starter.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;


@Service
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // Actualizamos la firma del método para aceptar la capacidad (limit)
    public Bucket resolveBucket(String ip, int limit) {
        // Usamos una clave combinada para que una IP pueda tener 
        // distintos baldes según el límite (ej. "192.168.1.1-5" y "192.168.1.1-50")
        String key = ip + "-" + limit;
        return cache.computeIfAbsent(key, k -> createNewBucket(limit));
    }

    private Bucket createNewBucket(int limit) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofMinutes(1))))
                .build();
    }
}