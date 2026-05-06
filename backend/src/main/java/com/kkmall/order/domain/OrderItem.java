package com.kkmall.order.domain;

import com.kkmall.common.domain.Money;

public final class OrderItem {
    private final Long productId;
    private final Long skuId;
    private final String titleSnapshot;
    private final String skuSnapshot;
    private final Money unitPrice;
    private final int quantity;
    private final String imageSnapshot;

    public OrderItem(Long productId, Long skuId, String titleSnapshot, String skuSnapshot, Money unitPrice, int quantity, String imageSnapshot) {
        if (quantity < 1) throw new IllegalArgumentException("QUANTITY_INVALID");
        this.productId = productId;
        this.skuId = skuId;
        this.titleSnapshot = titleSnapshot;
        this.skuSnapshot = skuSnapshot;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.imageSnapshot = imageSnapshot;
    }

    public Long productId() { return productId; }
    public Long skuId() { return skuId; }
    public String titleSnapshot() { return titleSnapshot; }
    public String skuSnapshot() { return skuSnapshot; }
    public Money unitPrice() { return unitPrice; }
    public int quantity() { return quantity; }
    public String imageSnapshot() { return imageSnapshot; }
    public Money subtotal() { return unitPrice.multiply(quantity); }
}
