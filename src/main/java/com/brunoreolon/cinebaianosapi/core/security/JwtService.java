package com.brunoreolon.cinebaianosapi.core.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    private Key privateKey;
    private PublicKey publicKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        try {
            Resource resource = new ClassPathResource(jwtProperties.getPath());
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = resource.getInputStream()) {
                keyStore.load(is, jwtProperties.getKeypass().toCharArray());
            }
            this.privateKey = (PrivateKey) keyStore.getKey(jwtProperties.getAlias(), jwtProperties.getKeypass().toCharArray());
            this.publicKey = keyStore.getCertificate(jwtProperties.getAlias()).getPublicKey();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load keys from keystore", e);
        }
    }

    public String generateToken(String username, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(jwtProperties.getAccessTokenExpirationMinutes() * 60)))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(publicKey).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

}
