package com.order.mapper;

import com.order.client.dto.ProductResponse;
import com.order.dto.OrderItemResponse;
import com.order.dto.OrderRequest;
import com.order.dto.OrderResponse;
import com.order.dto.OrderSummaryResponse;
import com.order.entity.Order;
import com.order.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public Order toEntity(OrderRequest request, String orderNumber) {
        return Order.builder()
                .orderNumber(orderNumber)
                .customerId(request.customerId())
                .shippingAddress(request.shippingAddress().trim())
                .notes(request.notes() == null ? null : request.notes().trim())
                .build();
    }

    public OrderItem toItem(ProductResponse product, int quantity) {
        return OrderItem.builder()
                .productId(product.id())
                .productName(product.name())
                .sku(product.sku())
                .unitPrice(product.price())
                .quantity(quantity)
                .build();
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getNotes(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public OrderSummaryResponse toSummaryResponse(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getSku(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }
}
