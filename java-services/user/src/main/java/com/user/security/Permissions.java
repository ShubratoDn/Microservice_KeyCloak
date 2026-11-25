package com.user.security;

public final class Permissions {

    public static final String USER_ADD = "USER_ADD";
    public static final String USER_VIEW = "USER_VIEW";
    public static final String USER_EDIT = "USER_EDIT";
    public static final String USER_DELETE = "USER_DELETE";

    public static final String HAS_USER_ADD = "hasAuthority('" + USER_ADD + "')";
    public static final String HAS_USER_VIEW = "hasAuthority('" + USER_VIEW + "')";
    public static final String HAS_USER_EDIT = "hasAuthority('" + USER_EDIT + "')";
    public static final String HAS_USER_DELETE = "hasAuthority('" + USER_DELETE + "')";

    private Permissions() {
    }
}
