package com.weady.weady.global.jwt;

import com.weady.weady.domain.user.entity.User;
import com.weady.weady.global.constant.JwtConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(User user) {
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + JwtConstant.ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("provider", user.getProvider().name())
                .expiration(accessTokenExpiresIn)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(User user) {
        long now = (new Date()).getTime();
        Date refreshTokenExpiresIn = new Date(now + JwtConstant.REFRESH_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .subject(user.getId().toString())
                .expiration(refreshTokenExpiresIn)
                .signWith(key)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.info("잘못된 JWT 토큰입니다. reason: {}", e.getMessage());
        }
        return false;
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(JwtConstant.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtConstant.BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}