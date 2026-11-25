package com.product.security;

public final class Permissions {

    public static final String PRODUCT_ADD = "PRODUCT_ADD";
    public static final String PRODUCT_VIEW = "PRODUCT_VIEW";
    public static final String PRODUCT_EDIT = "PRODUCT_EDIT";
    public static final String PRODUCT_DELETE = "PRODUCT_DELETE";

    public static final String HAS_PRODUCT_ADD = "hasAuthority('" + PRODUCT_ADD + "')";
    public static final String HAS_PRODUCT_VIEW = "hasAuthority('" + PRODUCT_VIEW + "')";
    public static final String HAS_PRODUCT_EDIT = "hasAuthority('" + PRODUCT_EDIT + "')";
    public static final String HAS_PRODUCT_DELETE = "hasAuthority('" + PRODUCT_DELETE + "')";

    private Permissions() {
    }
}
