// src/main/java/com/tradingpt/tpt_api/global/web/cookie/CookieUtils.java
package com.tradingpt.tpt_api.global.web.cookie;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CookieUtils {

    /** 커스텀 옵션으로 즉시 만료 */
    public static void expire(HttpServletResponse res, String name, boolean httpOnly, CookieProps props) {
        // var 또는 ResponseCookie.ResponseCookieBuilder 사용
        var builder = ResponseCookie.from(name, "")
                .path(props.path() == null ? "/" : props.path())
                .httpOnly(httpOnly)
                .maxAge(0);

        if (props.domain() != null) builder.domain(props.domain());
        if (props.sameSite() != null) builder.sameSite(props.sameSite());
        if (props.secure()) builder.secure(true);

        res.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }
    /** 자주 쓰는 묶음 만료 */
    public static void expireAuthCookies(HttpServletResponse res, CookieProps props) {
        expire(res, AppCookie.SESSION.cookieName(), true, props);      // ★
        expire(res, AppCookie.REMEMBER_ME.cookieName(), true, props);  // ★
        expire(res, AppCookie.XSRF.cookieName(), false, props);
    }
}
