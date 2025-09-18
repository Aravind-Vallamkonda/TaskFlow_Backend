package com.example.TaskFlow.service;

import com.example.TaskFlow.Constants.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtServiceTest {
    private static final String SECRET = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    private JwtService jwtService;
    private String userName = "Alice";
    private String tokenIssuer = "taskflow-test";
    private List<String> roles = List.of("USER","ADMIN");

    @BeforeEach
    void setUp(){
        jwtService = new JwtService(SECRET, tokenIssuer,5L,60L);
    }

    @Test
    void createAccessTokenShouldEmbedExcpectedClaims(){
        String accessToken = jwtService.createAccessToken(userName,roles);
        Claims claims = jwtService.parse(accessToken);

        assertThat(claims.getSubject()).isEqualTo(userName);
        assertThat(claims.getIssuer()).isEqualTo(tokenIssuer);
        assertThat(claims.get(Constants.TYPE)).isEqualTo(Constants.ACCESS_TOKEN_CLAIM);
        assertThat(jwtService.roles(claims)).containsExactlyElementsOf(roles);
        assertThat(claims.getExpiration().toInstant()).isAfter(Instant.now());
    }

    @Test
    void createRefreshTokenShouldBeRecognizableAsRefreshToken(){
        String refreshToken = jwtService.createRefreshToken(userName,roles);
        Claims claims = jwtService.parse(refreshToken);

        assertThat(claims.getSubject()).isEqualTo(userName);
        assertThat(claims.get(Constants.TYPE)).isEqualTo(Constants.REFRESH_TOKEN_CLAIM);
        assertThat(jwtService.roles(claims)).containsExactlyElementsOf(roles);
        assertThat(claims.getExpiration().toInstant()).isAfter(Instant.now());

    }

    @Test
    void extractTokenFromHeaderShouldReturnTokenValue(){
        String rawToken = "abc.efg.ghj";
        assertThat(jwtService.extractTokenFromHeader("Bearer "+rawToken).get()).isEqualTo(rawToken);
        assertThat(jwtService.extractTokenFromHeader(null)).isEmpty();
        assertThat(jwtService.extractTokenFromHeader("")).isEmpty();
        assertThat(jwtService.extractTokenFromHeader("Token  "+rawToken)).isEmpty();
    }

    @Test
    void rolesShouldReturnEmptyWhenClaimMissing(){
        Claims claims = Jwts.claims().build();

        assertThat(jwtService.roles(claims)).isEmpty();
    }

    @Test
    void isTokenExpiredShouldDetectPastAndFutureExpirations() {
        Claims expired = Jwts.claims()
                .expiration(Date.from(Instant.now().minusSeconds(5)))
                .build();

        Claims active = Jwts.claims().
                expiration(Date.from(Instant.now().plusSeconds(60)))
                .build();

        assertThat(jwtService.isTokenExpired(expired)).isTrue();
        assertThat(jwtService.isTokenExpired(active)).isFalse();
    }

}
