package com.tradingpt.tpt_api.global.web.cookie;

public enum AppCookie {
    SESSION("SESSION", true),
    REMEMBER_ME("remember-me", true),
    XSRF("XSRF-TOKEN", false);

    private final String name;
    private final boolean defaultHttpOnly;

    AppCookie(String name, boolean defaultHttpOnly) {
        this.name = name;
        this.defaultHttpOnly = defaultHttpOnly;
    }

    public String cookieName() { return name; }
    public boolean defaultHttpOnly() { return defaultHttpOnly; }
}
