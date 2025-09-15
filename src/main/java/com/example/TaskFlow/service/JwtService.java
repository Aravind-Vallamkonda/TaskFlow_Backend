package com.example.TaskFlow.service;

import com.example.TaskFlow.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.List;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final String issuer;
    private final long accessTokenValidity; // in milliseconds
    private final long refreshTokenValidity; // in milliseconds


    // The @Value Annotations take the data from the Application.properties file
    // Syntax - @Value("${variable_name}")
    // If we want a default value we use colon to add it a the end of the variable name
    public JwtService( @Value("${app.jwt.secret}") String base64Secret,
                       @Value("${app.jwt.issuer}") String issuer,
                       @Value("${app.jwt.access-ttl-min:15}") long accessTokenValidity,
                       @Value("${app.jwt.refresh-ttl-min:10080}") long refreshTokenValidity
                       ){
        this.issuer = issuer;
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }
    // getter to access the Refesh Token Validity
    public long getRefreshTokenValidity(){
        return refreshTokenValidity;
    }

    // Creates JWT Access Token
    // Used by the refresh-token and login apis
    // It uses the Jwts class to build the Access Token, Sign and compress it.
    public String createAccessToken(String username, List<String> roles){
        Instant now = Instant.now();
        Instant expire = now.plusSeconds( accessTokenValidity * 60 );

        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expire))
                .claim(Constants.TYPE, Constants.ACCESS_TOKEN_CLAIM)
                .claim(Constants.ROLES,roles)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // Creates JWT Refresh Token
    // Used to create new accesstokens when the older ones expires
    // Most probably we will set this in the http only Cookie. If an attacker has this he gets access until token is expired
    // It uses the Jwts class to build the Access Token, Sign and compress it.
    public String createRefreshToken(String username, List<String> roles){
        Instant now = Instant.now();
        Instant expire = now.plusSeconds(refreshTokenValidity * 60);
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .issuedAt(Date.from(expire))
                .claim(Constants.TYPE,Constants.REFRESH_TOKEN_CLAIM)
                .claim(Constants.ROLES,roles)
                .signWith(secretKey,Jwts.SIG.HS256)
                .compact();
    }

    // Returns the Claims of the token.
    // Uses Jwts.parser to get the Claims out of a token.
    // Before that it verifies against the secretkey used to sign the accesstoken.
    public Claims parse(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Used to extract Auth Token(JWT) from the request header
    // It returns an Optional String
    // Optional returntypes make developer to check before getting value.
    public  Optional<String> extractTokenFromHeader(String authHeader){
        if(authHeader == null || authHeader.trim().isEmpty() || !authHeader.startsWith(Constants.BEARER_PREFIX)){
            return Optional.empty();
        }
        // Extract token by removing "Bearer " prefix
        return Optional.of(authHeader.substring(7).trim());
    }

    // Returns the username from the Claims Object
    public String extractUsername(Claims claims){
        return claims.getSubject();
    }
    //Returns whether the Token is a Refresh Token or not
    public boolean isRefreshToken(Claims claims){
        return Constants.REFRESH_TOKEN_CLAIM.equals(claims.get(Constants.REFRESH_TOKEN_CLAIM));
    }
    //Returns whether the Token is a Access Token or not
    public boolean isActiveToken(Claims claims){
        return Constants.ACCESS_TOKEN_CLAIM.equals(claims.get(Constants.ACCESS_TOKEN_CLAIM));
    }

    @SuppressWarnings("unchecked")
    public List<String> roles(Claims c){
        Object v = c.get("roles");
        return (v instanceof List) ? (List<String>) v : List.of() ;
    }

    // Checks if the token is expired
    public boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }
}
