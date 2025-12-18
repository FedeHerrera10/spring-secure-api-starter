package com.fedeherrera.spring_secure_api_starter.service;

import java.security.Key;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fedeherrera.spring_secure_api_starter.entity.User;
import com.fedeherrera.spring_secure_api_starter.entity.UserPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // 1. Extraer el nombre de usuario (subject)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 2. Método genérico para extraer cualquier Claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 3. Generar token con claims extra (opcional)
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 4. VALIDACIÓN MATEMÁTICA (Sin base de datos)
    // Se usa en el filtro para descartar basura rápidamente
    public boolean isTokenSignatureValid(String token) {
        try {
            extractAllClaims(token); // Si el token es inválido o expiró, lanza excepción
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 5. VALIDACIÓN FINAL (Con base de datos)
    public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    final Date issuedAt = extractClaim(token, Claims::getIssuedAt);
    
   if (userDetails instanceof UserPrincipal principal) {
        User user = principal.getUser(); // Obtenemos la entidad User
        
        if (user.getPasswordChangedAt() != null) {
            Date lastPasswordChange = Date.from(user.getPasswordChangedAt()
                    .atZone(ZoneId.systemDefault()).toInstant());
            
            // Si el token se emitió ANTES del cambio de password, es inválido
            if (issuedAt.before(lastPasswordChange)) {
                log.warn("Token rechazado para el usuario {}: emitido antes del último cambio de contraseña.", username);
                return false; 
            }
        }
    }

    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
}

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 6. El "Corazón": Abre el token usando la firma
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}