package com.product.security;

import com.product.controller.ProductController;
import com.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, SecurityErrorHandler.class})
class ProductControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void requestWithoutTokenIsRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void validTokenWithoutTheRequiredRoleIsRejectedWith403() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ORDER_VIEW"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    void validTokenWithTheViewRoleCanRead() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority(Permissions.PRODUCT_VIEW))))
                .andExpect(status().isOk());
    }

    @Test
    void viewRoleAloneCannotChangeData() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority(Permissions.PRODUCT_VIEW))))
                .andExpect(status().isForbidden());
    }
}
