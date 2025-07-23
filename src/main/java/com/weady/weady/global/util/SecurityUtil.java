package com.weady.weady.global.util;

import com.weady.weady.global.common.error.errorCode.AuthErrorCode;
import com.weady.weady.global.common.error.exception.BusinessException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtil {

    /**
     * SecurityContext 에서 인증 정보를 가져와 현재 사용자의 ID(PK)를 반환
     * @return Long 타입의 사용자 ID
     * //@throws AuthErrorCode.UNAUTHORIZED_USER 인증 정보가 없거나 유효하지 않을 때
     */
    public static Long getCurrentUserId() {
        //SecurityContext 에서 Authentication 객체를 Optional 로 가져옴.
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)  // 인증 객체가 유효한지 필터링
                .filter(auth -> !isAnonymous(auth))
                .map(Authentication::getName) //  Authentication 객체에서 principal(이름)을 추출
                .flatMap(SecurityUtil::parseLong)// 추출된 이름(사용자 ID 문자열)을 Long 으로 변환
                .orElseThrow(() -> new BusinessException(AuthErrorCode.UNAUTHORIZED_USER));
    }

    /**
     * 문자열을 Long으로 파싱하여 Optional<Long>으로 반환
     *
     * @param principalId 파싱할 사용자 ID 문자열
     * @return Optional<Long>
     */
    private static Optional<Long> parseLong(String principalId) {
        try {
            return Optional.of(Long.parseLong(principalId));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * 인증 객체가 익명 사용자인지 확인
     *
     * @param authentication 확인할 Authentication 객체
     * @return 익명 사용자인 경우 true
     */
    private static boolean isAnonymous(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return "anonymousUser".equals(((UserDetails) principal).getUsername());
        }
        return "anonymousUser".equals(principal.toString());
    }
}