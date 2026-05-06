package com.kkmall.order;

import com.kkmall.order.application.OrderApplicationService;
import com.kkmall.common.exception.BusinessException;
import com.kkmall.order.infrastructure.AddressMapper;
import com.kkmall.order.infrastructure.AddressPo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AddressManagementTest {
    @Test
    void firstAddressBecomesDefaultEvenWhenRequestDoesNotAskForIt() {
        AddressMapper addressMapper = mock(AddressMapper.class);
        when(addressMapper.selectList(any())).thenReturn(Collections.emptyList());
        OrderApplicationService service = new OrderApplicationService(addressMapper, null, null, null, null, null, null, null, null);

        OrderApplicationService.AddressRequest request = new OrderApplicationService.AddressRequest();
        request.receiverName = "张三";
        request.receiverPhone = "13800138000";
        request.region = "上海市 浦东新区";
        request.detail = "世纪大道 100 号";
        request.isDefault = false;

        service.createAddress(1L, request);

        ArgumentCaptor<AddressPo> captor = ArgumentCaptor.forClass(AddressPo.class);
        verify(addressMapper).insert(captor.capture());
        assertThat(captor.getValue().isDefault).isEqualTo(1);
    }

    @Test
    void rejectsCreatingMoreThanTwentyAddresses() {
        AddressMapper addressMapper = mock(AddressMapper.class);
        when(addressMapper.selectList(any())).thenReturn(addresses(20));
        OrderApplicationService service = new OrderApplicationService(addressMapper, null, null, null, null, null, null, null, null);

        OrderApplicationService.AddressRequest request = validRequest();

        assertThatThrownBy(() -> service.createAddress(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("ADDRESS_LIMIT_EXCEEDED");
    }

    @Test
    void settingDefaultAddressClearsPreviousDefault() {
        AddressMapper addressMapper = mock(AddressMapper.class);
        AddressPo oldDefault = address(1L, 1L, 1);
        AddressPo target = address(2L, 1L, 0);
        when(addressMapper.selectById(2L)).thenReturn(target);
        when(addressMapper.selectList(any())).thenReturn(List.of(oldDefault, target));
        OrderApplicationService service = new OrderApplicationService(addressMapper, null, null, null, null, null, null, null, null);

        service.setDefaultAddress(1L, 2L);

        assertThat(oldDefault.isDefault).isEqualTo(0);
        assertThat(target.isDefault).isEqualTo(1);
        verify(addressMapper).updateById(oldDefault);
        verify(addressMapper).updateById(target);
    }

    private OrderApplicationService.AddressRequest validRequest() {
        OrderApplicationService.AddressRequest request = new OrderApplicationService.AddressRequest();
        request.receiverName = "张三";
        request.receiverPhone = "13800138000";
        request.region = "上海市 浦东新区";
        request.detail = "世纪大道 100 号";
        request.isDefault = false;
        return request;
    }

    private List<AddressPo> addresses(int count) {
        List<AddressPo> result = new ArrayList<>();
        for (long i = 1; i <= count; i++) result.add(address(i, 1L, i == 1 ? 1 : 0));
        return result;
    }

    private AddressPo address(Long id, Long userId, Integer isDefault) {
        AddressPo address = new AddressPo();
        address.id = id;
        address.userId = userId;
        address.receiverName = "张三";
        address.receiverPhone = "13800138000";
        address.region = "上海市 浦东新区";
        address.detail = "世纪大道 100 号";
        address.isDefault = isDefault;
        return address;
    }
}
