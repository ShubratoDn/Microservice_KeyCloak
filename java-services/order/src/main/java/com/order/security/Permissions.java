package com.order.security;

public final class Permissions {

    public static final String ORDER_ADD = "ORDER_ADD";
    public static final String ORDER_VIEW = "ORDER_VIEW";
    public static final String ORDER_EDIT = "ORDER_EDIT";
    public static final String ORDER_DELETE = "ORDER_DELETE";

    public static final String HAS_ORDER_ADD = "hasAuthority('" + ORDER_ADD + "')";
    public static final String HAS_ORDER_VIEW = "hasAuthority('" + ORDER_VIEW + "')";
    public static final String HAS_ORDER_EDIT = "hasAuthority('" + ORDER_EDIT + "')";
    public static final String HAS_ORDER_DELETE = "hasAuthority('" + ORDER_DELETE + "')";

    private Permissions() {
    }
}
