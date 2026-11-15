package com.order.service.impl;

import com.order.client.CustomerClient;
import com.order.client.ProductClient;
import com.order.client.dto.CustomerResponse;
import com.order.client.dto.CustomerStatus;
import com.order.client.dto.ProductResponse;
import com.order.client.dto.ProductStatus;
import com.order.dto.OrderItemRequest;
import com.order.dto.OrderRequest;
import com.order.dto.OrderResponse;
import com.order.entity.Order;
import com.order.entity.OrderStatus;
import com.order.exception.BusinessRuleException;
import com.order.exception.ResourceNotFoundException;
import com.order.mapper.OrderMapper;
import com.order.repository.OrderRepository;
import com.order.service.OrderNumberGenerator;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final Long CUSTOMER_ID = 1L;
    private static final Long MOUSE_ID = 10L;
    private static final Long KEYBOARD_ID = 11L;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerClient customerClient;
    @Mock
    private ProductClient productClient;
    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderRepository, new OrderMapper(), orderNumberGenerator, customerClient, productClient);
    }

    @Test
    void create_savesOrderWithPriceSnapshotAndTotal() {
        givenCustomer(CustomerStatus.ACTIVE);
        givenProduct(MOUSE_ID, "MOUSE-001", "24.99", 50, ProductStatus.ACTIVE);
        givenProduct(KEYBOARD_ID, "KEYB-001", "50.00", 5, ProductStatus.ACTIVE);
        when(orderNumberGenerator.next()).thenReturn("ORD-20261115-ABCD1234");
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.create(orderRequest(
                new OrderItemRequest(MOUSE_ID, 2),
                new OrderItemRequest(KEYBOARD_ID, 1)));

        assertThat(response.orderNumber()).isEqualTo("ORD-20261115-ABCD1234");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.items()).hasSize(2);
        assertThat(response.totalAmount()).isEqualByComparingTo("99.98");
    }

    @Test
    void create_mergesDuplicateProductsIntoOneItem() {
        givenCustomer(CustomerStatus.ACTIVE);
        givenProduct(MOUSE_ID, "MOUSE-001", "10.00", 50, ProductStatus.ACTIVE);
        when(orderNumberGenerator.next()).thenReturn("ORD-1");
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.create(orderRequest(
                new OrderItemRequest(MOUSE_ID, 2),
                new OrderItemRequest(MOUSE_ID, 3)));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(5);
        assertThat(response.totalAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void create_rejectsCustomerThatIsNotActive() {
        givenCustomer(CustomerStatus.BLOCKED);

        assertThatThrownBy(() -> orderService.create(orderRequest(new OrderItemRequest(MOUSE_ID, 1))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("BLOCKED");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_rejectsUnknownCustomer() {
        when(customerClient.getById(CUSTOMER_ID)).thenThrow(notFound());

        assertThatThrownBy(() -> orderService.create(orderRequest(new OrderItemRequest(MOUSE_ID, 1))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer");
    }

    @Test
    void create_rejectsUnknownProduct() {
        givenCustomer(CustomerStatus.ACTIVE);
        when(orderNumberGenerator.next()).thenReturn("ORD-1");
        when(productClient.getById(MOUSE_ID)).thenThrow(notFound());

        assertThatThrownBy(() -> orderService.create(orderRequest(new OrderItemRequest(MOUSE_ID, 1))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");
    }

    @Test
    void create_rejectsProductThatIsNotActive() {
        givenCustomer(CustomerStatus.ACTIVE);
        givenProduct(MOUSE_ID, "MOUSE-001", "24.99", 50, ProductStatus.DISCONTINUED);
        when(orderNumberGenerator.next()).thenReturn("ORD-1");

        assertThatThrownBy(() -> orderService.create(orderRequest(new OrderItemRequest(MOUSE_ID, 1))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("DISCONTINUED");
    }

    @Test
    void create_rejectsQuantityAboveStock() {
        givenCustomer(CustomerStatus.ACTIVE);
        givenProduct(MOUSE_ID, "MOUSE-001", "24.99", 3, ProductStatus.ACTIVE);
        when(orderNumberGenerator.next()).thenReturn("ORD-1");

        assertThatThrownBy(() -> orderService.create(orderRequest(new OrderItemRequest(MOUSE_ID, 4))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Not enough stock");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateStatus_movesOrderToNextAllowedStatus() {
        Order order = Order.builder().orderNumber("ORD-1").status(OrderStatus.PENDING).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateStatus_rejectsInvalidTransition() {
        Order order = Order.builder().orderNumber("ORD-1").status(OrderStatus.DELIVERED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.CANCELLED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("DELIVERED to CANCELLED");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void getById_throwsWhenOrderDoesNotExist() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesOrderWithAllItems() {
        givenCustomer(CustomerStatus.ACTIVE);
        givenProduct(MOUSE_ID, "MOUSE-001", "24.99", 50, ProductStatus.ACTIVE);
        when(orderNumberGenerator.next()).thenReturn("ORD-1");
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.create(orderRequest(new OrderItemRequest(MOUSE_ID, 2)));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();
        assertThat(saved.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(saved.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getOrder()).isSameAs(saved);
            assertThat(item.getProductName()).isEqualTo("Product MOUSE-001");
        });
    }

    private void givenCustomer(CustomerStatus status) {
        when(customerClient.getById(CUSTOMER_ID))
                .thenReturn(new CustomerResponse(CUSTOMER_ID, "Rahim", "Uddin", status));
    }

    private void givenProduct(Long id, String sku, String price, int stock, ProductStatus status) {
        when(productClient.getById(id))
                .thenReturn(new ProductResponse(id, "Product " + sku, sku, new BigDecimal(price), stock, status));
    }

    private OrderRequest orderRequest(OrderItemRequest... items) {
        return new OrderRequest(CUSTOMER_ID, "12 Lake Road, Dhaka", null, List.of(items));
    }

    private FeignException.NotFound notFound() {
        return mock(FeignException.NotFound.class);
    }
}
