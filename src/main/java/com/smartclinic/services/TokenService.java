package com.smartclinic.services;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Service
public class TokenService {
    private final String secret;
    public TokenService(@Value("${clinic.jwt.secret}") String secret) { this.secret=secret; }
    public SecretKey getSigningKey() { return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
    public String generateToken(String email,String role) {
        return Jwts.builder().subject(email).claim("role",role).issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis()+3600000)).signWith(getSigningKey()).compact();
    }
    public Claims validate(String authorization,String... roles) {
        try {
            if(authorization==null || !authorization.startsWith("Bearer ")) throw new IllegalArgumentException();
            Claims c=Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authorization.substring(7)).getPayload();
            if(java.util.Arrays.stream(roles).noneMatch(r->r.equals(c.get("role",String.class))))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Role not permitted");
            return c;
        } catch(JwtException|IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Valid Bearer token required");
        }
    }
}
