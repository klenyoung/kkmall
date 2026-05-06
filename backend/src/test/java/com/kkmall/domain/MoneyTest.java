package com.kkmall.domain;

import com.kkmall.common.domain.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {
    @Test
    void addsAndMultipliesCentAmounts() {
        Money unitPrice = Money.ofCent(9900);

        assertThat(unitPrice.multiply(2).add(Money.ofCent(1000)).cent()).isEqualTo(20800);
    }

    @Test
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> Money.ofCent(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
