package com.xpto.distancelearning.payment.configs.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtProvider {

    private static final Logger log = LogManager.getLogger(JwtProvider.class);

    @Value("${dl.auth.jwtSecret}")
    private String jwtSecret;

    public String getSubjectJwt(String token) {
        // Initial implementation using deprecated methods.
        // return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();

        return Jwts.parser()
                // .setSigningKey(jwtSecret) // deprecated method
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * This method is used to get the claim name from the JWT token.
     * These claims are the ones that were set in the JWT token in the JwtProvider.generateJwt().claim() method (authuser project).
     */
    public String getClaimNameJwt(String token, String claimName) {
        // Initial implementation using deprecated methods.
        // return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().get(claimName).toString();

        return Jwts.parser()
                // .setSigningKey(jwtSecret) // deprecated method
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get(claimName).toString();
    }

    public boolean validateJwt(String authToken) {
        try {
            //Jwts.parser().setSigningKey(jwtSecret).build().parseClaimsJws(authToken); // working with deprecated method
            Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage(), e);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage(), e);
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage(), e);
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage(), e);
        }
        return false;
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}