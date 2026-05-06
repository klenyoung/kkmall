package com.kkmall.common.domain;

public final class Money {
    private final long cent;

    private Money(long cent) {
        if (cent < 0) throw new IllegalArgumentException("MONEY_NEGATIVE");
        this.cent = cent;
    }

    public static Money ofCent(long cent) {
        return new Money(cent);
    }

    public long cent() {
        return cent;
    }

    public Money add(Money other) {
        return new Money(this.cent + other.cent);
    }

    public Money multiply(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("QUANTITY_INVALID");
        return new Money(this.cent * quantity);
    }
}
