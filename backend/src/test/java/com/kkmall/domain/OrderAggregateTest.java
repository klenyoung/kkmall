package com.kkmall.domain;

import com.kkmall.common.domain.Money;
import com.kkmall.order.domain.AddressSnapshot;
import com.kkmall.order.domain.Order;
import com.kkmall.order.domain.OrderItem;
import com.kkmall.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderAggregateTest {
    @Test
    void createsPendingOrderAndCalculatesFreeShipping() {
        Order order = Order.create(
            1L,
            "202605030001",
            Collections.singletonList(new OrderItem(1L, 101L, "T", "颜色：白色", Money.ofCent(9900), 1, "T")),
            new AddressSnapshot("张三", "13800138000", "上海市", "世纪大道 1 号")
        );

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.productAmount().cent()).isEqualTo(9900);
        assertThat(order.shippingFee().cent()).isZero();
        assertThat(order.payableAmount().cent()).isEqualTo(9900);
    }

    @Test
    void onlyPaidPendingShipmentOrderCanBeShipped() {
        Order order = Order.create(
            1L,
            "202605030002",
            Collections.singletonList(new OrderItem(1L, 101L, "T", "颜色：白色", Money.ofCent(8900), 1, "T")),
            new AddressSnapshot("张三", "13800138000", "上海市", "世纪大道 1 号")
        );

        assertThatThrownBy(order::ship).isInstanceOf(IllegalStateException.class);

        order.markPaid();
        order.ship();

        assertThat(order.status()).isEqualTo(OrderStatus.SHIPPED);
    }
}
