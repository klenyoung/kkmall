package com.kkmall.order.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kkmall.cart.application.CartApplicationService;
import com.kkmall.cart.infrastructure.CartItemMapper;
import com.kkmall.cart.infrastructure.CartItemPo;
import com.kkmall.catalog.application.CatalogApplicationService;
import com.kkmall.catalog.domain.ProductStatus;
import com.kkmall.catalog.infrastructure.ProductMapper;
import com.kkmall.catalog.infrastructure.ProductPo;
import com.kkmall.catalog.infrastructure.SkuMapper;
import com.kkmall.catalog.infrastructure.SkuPo;
import com.kkmall.common.application.Jsons;
import com.kkmall.common.domain.Money;
import com.kkmall.common.exception.BusinessException;
import com.kkmall.common.interfaces.PageResult;
import com.kkmall.fulfillment.infrastructure.ShipmentMapper;
import com.kkmall.fulfillment.infrastructure.ShipmentPo;
import com.kkmall.order.domain.AddressSnapshot;
import com.kkmall.order.domain.Order;
import com.kkmall.order.domain.OrderItem;
import com.kkmall.order.infrastructure.*;
import com.kkmall.order.interfaces.dto.AddressDto;
import com.kkmall.order.interfaces.dto.CreateOrderResultDto;
import com.kkmall.order.interfaces.dto.IdResultDto;
import com.kkmall.order.interfaces.dto.OrderDetailDto;
import com.kkmall.order.interfaces.dto.OrderSummaryDto;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderApplicationService {
    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final AddressMapper addressMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final CartApplicationService cartApplicationService;
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final ShipmentMapper shipmentMapper;
    private final CatalogApplicationService catalogApplicationService;

    public OrderApplicationService(AddressMapper addressMapper, OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                                   CartItemMapper cartItemMapper, CartApplicationService cartApplicationService,
                                   ProductMapper productMapper, SkuMapper skuMapper, ShipmentMapper shipmentMapper,
                                   CatalogApplicationService catalogApplicationService) {
        this.addressMapper = addressMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartItemMapper = cartItemMapper;
        this.cartApplicationService = cartApplicationService;
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
        this.shipmentMapper = shipmentMapper;
        this.catalogApplicationService = catalogApplicationService;
    }

    public List<AddressDto> addresses(Long userId) {
        List<AddressPo> rows = addressMapper.selectList(new QueryWrapper<AddressPo>().eq("user_id", userId).orderByDesc("is_default").orderByDesc("updated_at").orderByDesc("id"));
        List<AddressDto> result = new ArrayList<>();
        for (AddressPo row : rows) {
            AddressDto dto = new AddressDto();
            dto.setId(row.getId());
            dto.setReceiverName(row.getReceiverName());
            dto.setReceiverPhone(row.getReceiverPhone());
            dto.setRegion(row.getRegion());
            dto.setDetail(row.getDetail());
            dto.setIsDefault(row.getIsDefault());
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public IdResultDto createAddress(Long userId, AddressRequest request) {
        validateAddress(request);
        List<AddressPo> existing = addressMapper.selectList(new QueryWrapper<AddressPo>().eq("user_id", userId));
        if (existing.size() >= 20) throw new BusinessException("ADDRESS_LIMIT_EXCEEDED");
        boolean defaultAddress = existing.isEmpty() || Boolean.TRUE.equals(request.getIsDefault());
        if (defaultAddress) clearDefault(existing);
        AddressPo po = new AddressPo();
        po.setUserId(userId);
        po.setReceiverName(request.getReceiverName());
        po.setReceiverPhone(request.getReceiverPhone());
        po.setRegion(request.getRegion());
        po.setDetail(request.getDetail());
        po.setIsDefault(defaultAddress ? 1 : 0);
        addressMapper.insert(po);
        log.info("地址创建成功，addressId={}, userId={}", po.getId(), userId);
        return IdResultDto.of(po.getId());
    }

    @Transactional
    public IdResultDto updateAddress(Long userId, Long addressId, AddressRequest request) {
        validateAddress(request);
        AddressPo po = userAddress(userId, addressId);
        po.setReceiverName(request.getReceiverName());
        po.setReceiverPhone(request.getReceiverPhone());
        po.setRegion(request.getRegion());
        po.setDetail(request.getDetail());
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefault(addressMapper.selectList(new QueryWrapper<AddressPo>().eq("user_id", userId)));
            po.setIsDefault(1);
        }
        addressMapper.updateById(po);
        return IdResultDto.of(po.getId());
    }

    @Transactional
    public IdResultDto deleteAddress(Long userId, Long addressId) {
        AddressPo po = userAddress(userId, addressId);
        boolean wasDefault = Integer.valueOf(1).equals(po.getIsDefault());
        addressMapper.deleteById(addressId);
        if (wasDefault) {
            List<AddressPo> remaining = addressMapper.selectList(new QueryWrapper<AddressPo>().eq("user_id", userId).orderByDesc("updated_at").orderByDesc("id"));
            if (!remaining.isEmpty()) {
                AddressPo nextDefault = remaining.get(0);
                nextDefault.setIsDefault(1);
                addressMapper.updateById(nextDefault);
            }
        }
        log.info("地址删除，addressId={}, userId={}, wasDefault={}", addressId, userId, wasDefault);
        return IdResultDto.of(addressId);
    }

    @Transactional
    public IdResultDto setDefaultAddress(Long userId, Long addressId) {
        AddressPo target = userAddress(userId, addressId);
        clearDefault(addressMapper.selectList(new QueryWrapper<AddressPo>().eq("user_id", userId)));
        target.setIsDefault(1);
        addressMapper.updateById(target);
        return IdResultDto.of(target.getId());
    }

    @Transactional
    public CreateOrderResultDto createOrder(Long userId, CreateOrderRequest request) {
        AddressPo address = addressMapper.selectOne(new QueryWrapper<AddressPo>().eq("id", request.getAddressId()).eq("user_id", userId).last("LIMIT 1"));
        if (address == null) throw new BusinessException("ADDRESS_NOT_FOUND");
        List<CartItemPo> cartItems = cartApplicationService.settleableItems(userId, request.getCartItemIds());
        if (cartItems.isEmpty()) throw new BusinessException("CART_ITEM_NOT_FOUND");
        List<OrderItem> domainItems = new ArrayList<>();
        for (CartItemPo cartItem : cartItems) {
            SkuPo sku = skuMapper.selectById(cartItem.getSkuId());
            ProductPo product = sku == null ? null : productMapper.selectById(sku.getProductId());
            if (sku == null) throw new BusinessException("SKU_NOT_FOUND");
            if (product == null || !ProductStatus.ON_SALE.name().equals(product.getStatus())) throw new BusinessException("PRODUCT_OFF_SALE");
            if (sku.getStock() < cartItem.getQuantity()) throw new BusinessException("SKU_STOCK_NOT_ENOUGH");
            domainItems.add(new OrderItem(product.getId(), sku.getId(), product.getTitle(), sku.getSpecName() + "：" + sku.getSpecValue(), Money.ofCent(sku.getPrice()), cartItem.getQuantity(), catalogApplicationService.firstImage(product.getImages())));
        }
        AddressSnapshot snapshot = new AddressSnapshot(address.getReceiverName(), address.getReceiverPhone(), address.getRegion(), address.getDetail());
        Order order = Order.create(userId, orderNo(), domainItems, snapshot);
        OrderPo orderPo = toPo(order);
        orderMapper.insert(orderPo);
        order.assignId(orderPo.getId());
        for (OrderItem item : order.items()) {
            OrderItemPo po = new OrderItemPo();
            po.setOrderId(orderPo.getId());
            po.setProductId(item.productId());
            po.setSkuId(item.skuId());
            po.setTitleSnapshot(item.titleSnapshot());
            po.setImageSnapshot(item.imageSnapshot());
            po.setSkuSnapshot(item.skuSnapshot());
            po.setUnitPrice(item.unitPrice().cent());
            po.setQuantity(item.quantity());
            po.setSubtotal(item.subtotal().cent());
            orderItemMapper.insert(po);
        }
        for (CartItemPo item : cartItems) cartItemMapper.deleteById(item.getId());
        log.info("订单创建成功，orderId={}, userId={}, orderNo={}", orderPo.getId(), userId, order.orderNo());
        CreateOrderResultDto dto = new CreateOrderResultDto();
        dto.setId(orderPo.getId());
        dto.setOrderNo(order.orderNo());
        dto.setStatus(order.status().name());
        dto.setProductAmount(order.productAmount().cent());
        dto.setShippingFee(order.shippingFee().cent());
        dto.setPayableAmount(order.payableAmount().cent());
        return dto;
    }

    public PageResult<OrderSummaryDto> userOrders(Long userId, String status, int page, int pageSize) {
        QueryWrapper<OrderPo> wrapper = new QueryWrapper<OrderPo>().eq("user_id", userId).orderByDesc("id");
        if (status != null && !status.trim().isEmpty()) wrapper.eq("status", status);
        return orderPage(wrapper, page, pageSize);
    }

    public PageResult<OrderSummaryDto> adminOrders(String status, int page, int pageSize) {
        QueryWrapper<OrderPo> wrapper = new QueryWrapper<OrderPo>().orderByDesc("id");
        if (status != null && !status.trim().isEmpty()) wrapper.eq("status", status);
        return orderPage(wrapper, page, pageSize);
    }

    public OrderDetailDto userOrderDetail(Long userId, Long orderId) {
        OrderPo order = orderMapper.selectOne(new QueryWrapper<OrderPo>().eq("id", orderId).eq("user_id", userId).last("LIMIT 1"));
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND");
        return detail(order);
    }

    public OrderDetailDto adminOrderDetail(Long orderId) {
        OrderPo order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND");
        return detail(order);
    }

    private PageResult<OrderSummaryDto> orderPage(QueryWrapper<OrderPo> wrapper, int page, int pageSize) {
        Page<OrderPo> p = orderMapper.selectPage(new Page<>(Math.max(page, 1), Math.max(pageSize, 1)), wrapper);
        List<OrderSummaryDto> items = new ArrayList<>();
        for (OrderPo order : p.getRecords()) {
            OrderSummaryDto dto = new OrderSummaryDto();
            dto.setId(order.getId());
            dto.setOrderNo(order.getOrderNo());
            dto.setProductAmount(order.getProductAmount());
            dto.setShippingFee(order.getShippingFee());
            dto.setPayableAmount(order.getPayableAmount());
            dto.setStatus(order.getStatus());
            dto.setCreatedAt(order.getCreatedAt());
            items.add(dto);
        }
        return new PageResult<>(items, p.getTotal(), page, pageSize);
    }

    private OrderDetailDto detail(OrderPo order) {
        List<OrderDetailDto.OrderItemDto> items = new ArrayList<>();
        for (OrderItemPo item : orderItemMapper.selectList(new QueryWrapper<OrderItemPo>().eq("order_id", order.getId()))) {
            OrderDetailDto.OrderItemDto itemDto = new OrderDetailDto.OrderItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProductId());
            itemDto.setSkuId(item.getSkuId());
            itemDto.setTitleSnapshot(item.getTitleSnapshot());
            itemDto.setImageSnapshot(item.getImageSnapshot());
            itemDto.setSkuSnapshot(item.getSkuSnapshot());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setSubtotal(item.getSubtotal());
            items.add(itemDto);
        }
        ShipmentPo shipment = shipmentMapper.selectOne(new QueryWrapper<ShipmentPo>().eq("order_id", order.getId()).last("LIMIT 1"));
        OrderDetailDto.ShipmentDto shipmentDto = null;
        if (shipment != null) {
            shipmentDto = new OrderDetailDto.ShipmentDto();
            shipmentDto.setLogisticsCompany(shipment.getLogisticsCompany());
            shipmentDto.setTrackingNo(shipment.getTrackingNo());
            shipmentDto.setShippedAt(shipment.getShippedAt());
        }
        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setProductAmount(order.getProductAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setPayableAmount(order.getPayableAmount());
        dto.setStatus(order.getStatus());
        dto.setAddressSnapshot(Jsons.readMap(order.getAddressSnapshot()));
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaidAt(order.getPaidAt());
        dto.setShippedAt(order.getShippedAt());
        dto.setItems(items);
        dto.setShipment(shipmentDto);
        return dto;
    }

    private OrderPo toPo(Order order) {
        OrderPo po = new OrderPo();
        po.setOrderNo(order.orderNo());
        po.setUserId(order.userId());
        po.setProductAmount(order.productAmount().cent());
        po.setShippingFee(order.shippingFee().cent());
        po.setPayableAmount(order.payableAmount().cent());
        po.setStatus(order.status().name());
        Map<String, Object> addressMap = new LinkedHashMap<>(4);
        addressMap.put("receiverName", order.addressSnapshot().receiverName());
        addressMap.put("receiverPhone", order.addressSnapshot().receiverPhone());
        addressMap.put("region", order.addressSnapshot().region());
        addressMap.put("detail", order.addressSnapshot().detail());
        po.setAddressSnapshot(Jsons.write(addressMap));
        return po;
    }

    private String orderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", java.util.concurrent.ThreadLocalRandom.current().nextInt(10000));
    }

    @Data
    public static class AddressRequest {
        private String receiverName;
        private String receiverPhone;
        private String region;
        private String detail;
        private Boolean isDefault;
    }

    @Data
    public static class CreateOrderRequest {
        private Long addressId;
        private List<Long> cartItemIds;
    }

    private AddressPo userAddress(Long userId, Long addressId) {
        AddressPo po = addressMapper.selectById(addressId);
        if (po == null || !Objects.equals(po.getUserId(), userId)) throw new BusinessException("ADDRESS_NOT_FOUND");
        return po;
    }

    private void validateAddress(AddressRequest request) {
        if (request == null) throw new BusinessException("ADDRESS_INVALID");
        if (isBlank(request.getReceiverName())) throw new BusinessException("ADDRESS_RECEIVER_NAME_REQUIRED");
        if (request.getReceiverPhone() == null || !request.getReceiverPhone().matches("^1\\d{10}$")) throw new BusinessException("ADDRESS_RECEIVER_PHONE_INVALID");
        if (isBlank(request.getRegion())) throw new BusinessException("ADDRESS_REGION_REQUIRED");
        if (isBlank(request.getDetail())) throw new BusinessException("ADDRESS_DETAIL_REQUIRED");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void clearDefault(List<AddressPo> addresses) {
        for (AddressPo item : addresses) {
            if (Integer.valueOf(1).equals(item.getIsDefault())) {
                item.setIsDefault(0);
                addressMapper.updateById(item);
            }
        }
    }
}
