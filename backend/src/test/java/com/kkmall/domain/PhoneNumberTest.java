package com.kkmall.domain;

import com.kkmall.account.domain.PhoneNumber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTest {
    @Test
    void acceptsMainlandMobileNumber() {
        assertThat(PhoneNumber.of("13800138000").value()).isEqualTo("13800138000");
    }

    @Test
    void rejectsInvalidMobileNumber() {
        assertThatThrownBy(() -> PhoneNumber.of("123")).isInstanceOf(IllegalArgumentException.class);
    }
}
