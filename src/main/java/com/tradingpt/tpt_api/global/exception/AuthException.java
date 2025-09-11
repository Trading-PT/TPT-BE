package com.tradingpt.tpt_api.global.exception;

import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

/**
 * 인증/인가 관련 예외 클래스
 * Trading PT 인증 시스템에서 발생하는 모든 인증 관련 오류를 처리
 */
public class AuthException extends BaseException {

    public AuthException(BaseCodeInterface baseCodeInterface) {
        super(baseCodeInterface);
    }

    public AuthException(BaseCodeInterface baseCodeInterface, String customMessage) {
        super(baseCodeInterface, customMessage);
    }



    // ========== JWT 토큰 관련 정적 메소드 ==========
    
    public static AuthException jwtAuthenticationFailed() {
        return new AuthException(AuthErrorStatus.JWT_AUTHENTICATION_FAILED);
    }
    
    public static AuthException tokenExpired() {
        return new AuthException(AuthErrorStatus.TOKEN_EXPIRED);
    }
    
    public static AuthException tokenInvalid() {
        return new AuthException(AuthErrorStatus.TOKEN_INVALID);
    }
    
    public static AuthException tokenMalformed() {
        return new AuthException(AuthErrorStatus.TOKEN_MALFORMED);
    }
    
    public static AuthException tokenSignatureInvalid() {
        return new AuthException(AuthErrorStatus.TOKEN_SIGNATURE_INVALID);
    }

    // ========== 자격 증명 관련 정적 메소드 ==========
    
    public static AuthException badCredentials() {
        return new AuthException(AuthErrorStatus.BAD_CREDENTIALS);
    }
    
    public static AuthException badCredentials(String customMessage) {
        return new AuthException(AuthErrorStatus.BAD_CREDENTIALS, customMessage);
    }
    
    public static AuthException userNotFound() {
        return new AuthException(AuthErrorStatus.USER_NOT_FOUND);
    }
    
    public static AuthException authenticationFailed() {
        return new AuthException(AuthErrorStatus.AUTHENTICATION_FAILED);
    }
    
    public static AuthException accessDenied() {
        return new AuthException(AuthErrorStatus.ACCESS_DENIED);
    }
    
    public static AuthException accountDisabled() {
        return new AuthException(AuthErrorStatus.ACCOUNT_DISABLED);
    }
    
    public static AuthException accountLocked() {
        return new AuthException(AuthErrorStatus.ACCOUNT_LOCKED);
    }

    // ========== 회원가입 관련 정적 메소드 ==========
    
    public static AuthException emailAlreadyExists() {
        return new AuthException(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
    }
    
    public static AuthException emailInvalidFormat() {
        return new AuthException(AuthErrorStatus.EMAIL_INVALID_FORMAT);
    }
    
    public static AuthException passwordTooWeak() {
        return new AuthException(AuthErrorStatus.PASSWORD_TOO_WEAK);
    }
    
    public static AuthException passwordTooShort() {
        return new AuthException(AuthErrorStatus.PASSWORD_TOO_SHORT);
    }
    
    public static AuthException passwordMismatch() {
        return new AuthException(AuthErrorStatus.PASSWORD_MISMATCH);
    }
    
    public static AuthException usernameAlreadyExists() {
        return new AuthException(AuthErrorStatus.USERNAME_ALREADY_EXISTS);
    }

    // ========== OAuth2 관련 정적 메소드 ==========
    
    public static AuthException oauth2AuthenticationFailed() {
        return new AuthException(AuthErrorStatus.OAUTH2_AUTHENTICATION_FAILED);
    }
    
    public static AuthException oauth2ProviderNotSupported() {
        return new AuthException(AuthErrorStatus.OAUTH2_PROVIDER_NOT_SUPPORTED);
    }
    
    public static AuthException oauth2UserInfoRetrievalFailed() {
        return new AuthException(AuthErrorStatus.OAUTH2_USER_INFO_RETRIEVAL_FAILED);
    }

    // ========== 토큰 관리 관련 정적 메소드 ==========
    
    public static AuthException refreshTokenNotFound() {
        return new AuthException(AuthErrorStatus.REFRESH_TOKEN_NOT_FOUND);
    }
    
    public static AuthException refreshTokenExpired() {
        return new AuthException(AuthErrorStatus.REFRESH_TOKEN_EXPIRED);
    }
    
    public static AuthException refreshTokenInvalid() {
        return new AuthException(AuthErrorStatus.REFRESH_TOKEN_INVALID);
    }
    
    public static AuthException accessTokenRequired() {
        return new AuthException(AuthErrorStatus.ACCESS_TOKEN_REQUIRED);
    }
    
    public static AuthException bearerTokenMalformed() {
        return new AuthException(AuthErrorStatus.BEARER_TOKEN_MALFORMED);
    }

    // ========== 권한 관련 정적 메소드 ==========
    
    public static AuthException insufficientPrivileges() {
        return new AuthException(AuthErrorStatus.INSUFFICIENT_PRIVILEGES);
    }
    
    public static AuthException adminRequired() {
        return new AuthException(AuthErrorStatus.ADMIN_REQUIRED);
    }
    
    public static AuthException premiumRequired() {
        return new AuthException(AuthErrorStatus.PREMIUM_REQUIRED);
    }

    // ========== 계정 상태 관련 정적 메소드 ==========
    
    public static AuthException accountNotVerified() {
        return new AuthException(AuthErrorStatus.ACCOUNT_NOT_VERIFIED);
    }
    
    public static AuthException accountSuspended() {
        return new AuthException(AuthErrorStatus.ACCOUNT_SUSPENDED);
    }
    
    public static AuthException phoneNotVerified() {
        return new AuthException(AuthErrorStatus.PHONE_NOT_VERIFIED);
    }

    // ========== 보안 관련 정적 메소드 ==========
    
    public static AuthException tooManyLoginAttempts() {
        return new AuthException(AuthErrorStatus.TOO_MANY_LOGIN_ATTEMPTS);
    }
    
    public static AuthException suspiciousActivityDetected() {
        return new AuthException(AuthErrorStatus.SUSPICIOUS_ACTIVITY_DETECTED);
    }
    
    public static AuthException ipBlocked() {
        return new AuthException(AuthErrorStatus.IP_BLOCKED);
    }
    
    public static AuthException securityViolation() {
        return new AuthException(AuthErrorStatus.SECURITY_VIOLATION);
    }

    // ========== 트레이딩 특화 인증 관련 ==========
    
    public static AuthException tradingAccountNotVerified() {
        return new AuthException(AuthErrorStatus.TRADING_ACCOUNT_NOT_VERIFIED);
    }
    
    public static AuthException kycVerificationRequired() {
        return new AuthException(AuthErrorStatus.KYC_VERIFICATION_REQUIRED);
    }
    
    public static AuthException riskAssessmentRequired() {
        return new AuthException(AuthErrorStatus.RISK_ASSESSMENT_REQUIRED);
    }
    
    public static AuthException ageVerificationFailed() {
        return new AuthException(AuthErrorStatus.AGE_VERIFICATION_FAILED);
    }

    // ========== 세션 관리 관련 ==========
    
    public static AuthException sessionExpired() {
        return new AuthException(AuthErrorStatus.SESSION_EXPIRED);
    }
    
    public static AuthException sessionInvalid() {
        return new AuthException(AuthErrorStatus.SESSION_INVALID);
    }
    
    public static AuthException concurrentSessionDetected() {
        return new AuthException(AuthErrorStatus.CONCURRENT_SESSION_DETECTED);
    }

    // ========== 인증 코드 관련 ==========
    
    public static AuthException verificationCodeExpired() {
        return new AuthException(AuthErrorStatus.VERIFICATION_CODE_EXPIRED);
    }
    
    public static AuthException verificationCodeInvalid() {
        return new AuthException(AuthErrorStatus.VERIFICATION_CODE_INVALID);
    }
    
    public static AuthException verificationCodeAttemptsExceeded() {
        return new AuthException(AuthErrorStatus.VERIFICATION_CODE_ATTEMPTS_EXCEEDED);
    }

    // ========== 레거시 메소드 (하위 호환성) ==========
    
    /**
     * @deprecated Use tokenInvalid() instead
     */
    @Deprecated
    public static AuthException invalidToken() {
        return tokenInvalid();
    }
    
    /**
     * @deprecated Use tokenExpired() instead
     */
    @Deprecated
    public static AuthException expiredToken() {
        return tokenExpired();
    }
    
    /**
     * @deprecated Use accessTokenRequired() instead
     */
    @Deprecated
    public static AuthException missingToken() {
        return accessTokenRequired();
    }
    
    /**
     * @deprecated Use authenticationFailed() instead
     */
    @Deprecated
    public static AuthException loginFailed() {
        return authenticationFailed();
    }
    
    /**
     * @deprecated Use insufficientPrivileges() instead
     */
    @Deprecated
    public static AuthException insufficientPermissions() {
        return insufficientPrivileges();
    }
}
