package com.kkmall.order.domain;

import com.kkmall.common.domain.Money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Order {
    private Long id;
    private final Long userId;
    private final String orderNo;
    private final List<OrderItem> items;
    private final AddressSnapshot addressSnapshot;
    private final Money productAmount;
    private final Money shippingFee;
    private final Money payableAmount;
    private OrderStatus status;

    private Order(Long id, Long userId, String orderNo, List<OrderItem> items, AddressSnapshot addressSnapshot,
                  Money productAmount, Money shippingFee, OrderStatus status) {
        this.id = id;
        this.userId = userId;
        this.orderNo = orderNo;
        this.items = new ArrayList<>(items);
        this.addressSnapshot = addressSnapshot;
        this.productAmount = productAmount;
        this.shippingFee = shippingFee;
        this.payableAmount = productAmount.add(shippingFee);
        this.status = status;
    }

    public static Order create(Long userId, String orderNo, List<OrderItem> items, AddressSnapshot addressSnapshot) {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("ORDER_ITEMS_EMPTY");
        Money productAmount = Money.ofCent(0);
        for (OrderItem item : items) productAmount = productAmount.add(item.subtotal());
        Money shippingFee = productAmount.cent() >= 9900 ? Money.ofCent(0) : Money.ofCent(1000);
        return new Order(null, userId, orderNo, items, addressSnapshot, productAmount, shippingFee, OrderStatus.PENDING_PAYMENT);
    }

    public static Order restore(Long id, Long userId, String orderNo, Money productAmount, Money shippingFee, OrderStatus status, AddressSnapshot snapshot) {
        return new Order(id, userId, orderNo, Collections.emptyList(), snapshot, productAmount, shippingFee, status);
    }

    public void markPaid() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            if (status == OrderStatus.PAID_PENDING_SHIPMENT || status == OrderStatus.SHIPPED || status == OrderStatus.COMPLETED) return;
            throw new IllegalStateException("ORDER_STATUS_INVALID");
        }
        status = OrderStatus.PAID_PENDING_SHIPMENT;
    }

    public void ship() {
        if (status != OrderStatus.PAID_PENDING_SHIPMENT) throw new IllegalStateException("ORDER_STATUS_INVALID");
        status = OrderStatus.SHIPPED;
    }

    public Long id() { return id; }
    public void assignId(Long id) { this.id = id; }
    public Long userId() { return userId; }
    public String orderNo() { return orderNo; }
    public List<OrderItem> items() { return Collections.unmodifiableList(items); }
    public AddressSnapshot addressSnapshot() { return addressSnapshot; }
    public Money productAmount() { return productAmount; }
    public Money shippingFee() { return shippingFee; }
    public Money payableAmount() { return payableAmount; }
    public OrderStatus status() { return status; }
}
