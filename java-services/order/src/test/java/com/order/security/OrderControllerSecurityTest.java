package com.order.security;

import com.order.controller.OrderController;
import com.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, SecurityErrorHandler.class})
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void requestWithoutTokenIsRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void validTokenWithoutTheRequiredRoleIsRejectedWith403() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .with(jwt().authorities(new SimpleGrantedAuthority("CUSTOMER_VIEW"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    void validTokenWithTheViewRoleCanRead() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .with(jwt().authorities(new SimpleGrantedAuthority(Permissions.ORDER_VIEW))))
                .andExpect(status().isOk());
    }

    @Test
    void viewRoleAloneCannotChangeData() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMED\"}")
                        .with(jwt().authorities(new SimpleGrantedAuthority(Permissions.ORDER_VIEW))))
                .andExpect(status().isForbidden());
    }
}
