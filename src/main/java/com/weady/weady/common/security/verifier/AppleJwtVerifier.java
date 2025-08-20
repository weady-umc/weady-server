package com.weady.weady.common.security.verifier;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.JWTClaimsSet;
import com.weady.weady.common.error.errorCode.AuthErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AppleJwtVerifier {


    private final String issuer;
    private final String audience;
    private final String jwkSetUri;
    private final long clockSkewSec;

    private ConfigurableJWTProcessor<SecurityContext> processor;

    private ConfigurableJWTProcessor<SecurityContext> processor() {
        if (processor == null) {
            try {
                DefaultResourceRetriever retriever = new DefaultResourceRetriever(
                        3000,   // connect timeout ms
                        3000,   // read timeout ms
                        262_144 // entity size limit
                );
                URL jwkUrl = new URL(jwkSetUri);

                @SuppressWarnings("deprecation")
                JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(jwkUrl, retriever);

                JWSVerificationKeySelector<SecurityContext> selector =
                        new JWSVerificationKeySelector<>(JWSAlgorithm.Family.RSA, jwkSource);

                DefaultJWTProcessor<SecurityContext> p = new DefaultJWTProcessor<>();
                p.setJWSKeySelector(selector);
                processor = p;
            } catch (Exception e) {
                throw new IllegalStateException("Failed to init Apple JWKS processor", e);
            }
        }
        return processor;
    }

    public JWTClaimsSet verifyAndParse(String idToken) {
        try {
            SignedJWT signed = SignedJWT.parse(idToken);
            JWTClaimsSet claims = processor().process(signed, null);


            if (!issuer.equals(claims.getIssuer())) {
                throw new BusinessException(AuthErrorCode.INVALID_ISS);
            }

            List<String> audList = claims.getAudience();
            if (audList == null || audList.isEmpty() || audList.stream().noneMatch(audience::equals)) {
                throw new BusinessException(AuthErrorCode.INVALID_AUDIENCE);
            }
            // exp 검증 (스큐 허용)
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.toInstant().isBefore(Instant.now().minusSeconds(clockSkewSec))) {
                throw new BusinessException(AuthErrorCode.EXPIRED_IDENTITY_TOKEN);
            }
            return claims;
        } catch (ParseException e) {
            throw new BusinessException(AuthErrorCode.MALFORMED_IDENTITY_TOKEN);
        } catch (Exception e) {
            // JWS 검증 실패, 키 미스 등
            throw new BusinessException(AuthErrorCode.INVALID_IDENTITY_TOKEN);
        }
    }
}
