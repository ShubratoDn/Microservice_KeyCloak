package com.order.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.stereotype.Component;

// The order service calls customer-service and product-service with Feign.
// Those services are also protected, so they need a token too.
// This class copies ("relays") the token of the user who called the order
// service into every Feign call. So the other service sees the same user
// and checks the same roles (CUSTOMER_VIEW, PRODUCT_VIEW).
// Spring OpenFeign finds every RequestInterceptor bean and runs it before each call.
@Component
public class BearerTokenRelayInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AbstractOAuth2TokenAuthenticationToken<?> tokenAuthentication) {
            template.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAuthentication.getToken().getTokenValue());
        }
    }
}
