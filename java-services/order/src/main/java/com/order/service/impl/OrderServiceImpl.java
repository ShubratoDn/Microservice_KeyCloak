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
import com.order.dto.OrderSummaryResponse;
import com.order.dto.PagedResponse;
import com.order.entity.Order;
import com.order.entity.OrderItem;
import com.order.entity.OrderStatus;
import com.order.exception.BusinessRuleException;
import com.order.exception.ResourceNotFoundException;
import com.order.mapper.OrderMapper;
import com.order.repository.OrderRepository;
import com.order.service.OrderNumberGenerator;
import com.order.service.OrderService;
import com.order.specification.OrderSpecifications;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String ORDER = "Order";
    private static final String CUSTOMER = "Customer";
    private static final String PRODUCT = "Product";

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final CustomerClient customerClient;
    private final ProductClient productClient;

    // Not transactional on purpose: the remote calls must not hold a database
    // transaction open. The final save() runs in its own short transaction.
    @Override
    public OrderResponse create(OrderRequest request) {
        verifyCustomerCanOrder(request.customerId());

        Order order = orderMapper.toEntity(request, orderNumberGenerator.next());
        mergeQuantitiesByProduct(request.items())
                .forEach((productId, quantity) -> order.addItem(createItem(productId, quantity)));

        Order saved = orderRepository.save(order);
        log.info("Created order {} for customer {}", saved.getOrderNumber(), saved.getCustomerId());
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return orderMapper.toResponse(findOrder(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderSummaryResponse> search(Long customerId, OrderStatus status, Pageable pageable) {
        Specification<Order> specification = Specification.unrestricted();

        if (customerId != null) {
            specification = specification.and(OrderSpecifications.hasCustomer(customerId));
        }
        if (status != null) {
            specification = specification.and(OrderSpecifications.hasStatus(status));
        }

        return PagedResponse.from(
                orderRepository.findAll(specification, pageable).map(orderMapper::toSummaryResponse));
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = findOrder(id);
        OrderStatus currentStatus = order.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BusinessRuleException(
                    "Cannot change order status from %s to %s".formatted(currentStatus, newStatus));
        }

        order.setStatus(newStatus);
        log.info("Changed status of order {} from {} to {}", order.getOrderNumber(), currentStatus, newStatus);
        return orderMapper.toResponse(order);
    }

    private void verifyCustomerCanOrder(Long customerId) {
        CustomerResponse customer = fetchCustomer(customerId);
        if (customer.status() != CustomerStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Customer %s is %s and cannot place orders".formatted(customerId, customer.status()));
        }
    }

    private OrderItem createItem(Long productId, int quantity) {
        ProductResponse product = fetchProduct(productId);

        if (product.status() != ProductStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Product '%s' is %s and cannot be ordered".formatted(product.sku(), product.status()));
        }
        if (product.stockQuantity() < quantity) {
            throw new BusinessRuleException(
                    "Not enough stock for product '%s': requested %d, available %d"
                            .formatted(product.sku(), quantity, product.stockQuantity()));
        }

        return orderMapper.toItem(product, quantity);
    }

    private Map<Long, Integer> mergeQuantitiesByProduct(List<OrderItemRequest> items) {
        return items.stream().collect(Collectors.toMap(
                OrderItemRequest::productId,
                OrderItemRequest::quantity,
                Integer::sum,
                LinkedHashMap::new));
    }

    private CustomerResponse fetchCustomer(Long id) {
        try {
            return customerClient.getById(id);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException(CUSTOMER, id);
        }
    }

    private ProductResponse fetchProduct(Long id) {
        try {
            return productClient.getById(id);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException(PRODUCT, id);
        }
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER, id));
    }
}
