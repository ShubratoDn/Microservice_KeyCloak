package com.order.service;

import com.order.dto.OrderRequest;
import com.order.dto.OrderResponse;
import com.order.dto.OrderSummaryResponse;
import com.order.dto.PagedResponse;
import com.order.entity.OrderStatus;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse create(OrderRequest request);

    OrderResponse getById(Long id);

    PagedResponse<OrderSummaryResponse> search(Long customerId, OrderStatus status, Pageable pageable);

    OrderResponse updateStatus(Long id, OrderStatus status);
}
