package com.customer.security;

public final class Permissions {

    public static final String CUSTOMER_ADD = "CUSTOMER_ADD";
    public static final String CUSTOMER_VIEW = "CUSTOMER_VIEW";
    public static final String CUSTOMER_EDIT = "CUSTOMER_EDIT";
    public static final String CUSTOMER_DELETE = "CUSTOMER_DELETE";

    public static final String HAS_CUSTOMER_ADD = "hasAuthority('" + CUSTOMER_ADD + "')";
    public static final String HAS_CUSTOMER_VIEW = "hasAuthority('" + CUSTOMER_VIEW + "')";
    public static final String HAS_CUSTOMER_EDIT = "hasAuthority('" + CUSTOMER_EDIT + "')";
    public static final String HAS_CUSTOMER_DELETE = "hasAuthority('" + CUSTOMER_DELETE + "')";

    private Permissions() {
    }
}
