package com.kkmall.order.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.kkmall.order.domain.OrderStatus;
import com.kkmall.order.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderApplicationService {
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

    public List<Map<String, Object>> addresses(Long userId) {
        List<AddressPo> rows = addressMapper.selectList(new QueryWrapper<AddressPo>().eq("user_id", userId).orderByDesc("is_default").orderByDesc("updated_at").orderByDesc("id"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (AddressPo row : rows) {
            result.add(CatalogApplicationService.mapOf("id", row.id, "receiverName", row.receiverName, "receiverPhone", row.receiverPhone, "region", row.region, "detail", row.detail, "isDefault", row.isDefault));
        }
        return result;
    }

    @Transactional
    public Map<String, Object> createAddress(Long userId, AddressRequest request) {
        validateAddress(request);
        List<AddressPo> existing = addressMapper.selectList(new QueryWrapper<AddressPo>().eq("user_id", userId));
        if (existing.size() >= 20) throw new BusinessException("ADDRESS_LIMIT_EXCEEDED");
        boolean defaultAddress = existing.isEmpty() || Boolean.TRUE.equals(request.isDefault);
        if (defaultAddress) clearDefault(existing);
        AddressPo po = new AddressPo();
        po.userId = userId;
        po.receiverName = request.receiverName;
        po.receiverPhone = request.receiverPhone;
        po.region = request.region;
        po.detail = request.detail;
        po.isDefault = defaultAddress ? 1 : 0;
        addressMapper.insert(po);
        return CatalogApplicationService.mapOf("id", po.id);
    }

    @Transactional
    public Map<String, Object> updateAddress(Long userId, Long addressId, AddressRequest request) {
        validateAddress(request);
        AddressPo po = userAddress(userId, addressId);
        po.receiverName = request.receiverName;
        po.receiverPhone = request.receiverPhone;
        po.region = request.region;
        po.detail = request.detail;
        if (Boolean.TRUE.equals(request.isDefault)) {
            clearDefault(addressMapper.selectList(new QueryWrapper<AddressPo>().eq("user_id", userId)));
            po.isDefault = 1;
        }
        addressMapper.updateById(po);
        return CatalogApplicationService.mapOf("id", po.id);
    }

    @Transactional
    public Map<String, Object> deleteAddress(Long userId, Long addressId) {
        AddressPo po = userAddress(userId, addressId);
        boolean wasDefault = Integer.valueOf(1).equals(po.isDefault);
        addressMapper.deleteById(addressId);
        if (wasDefault) {
            List<AddressPo> remaining = addressMapper.selectList(new QueryWrapper<AddressPo>().eq("user_id", userId).orderByDesc("updated_at").orderByDesc("id"));
            if (!remaining.isEmpty()) {
                AddressPo nextDefault = remaining.get(0);
                nextDefault.isDefault = 1;
                addressMapper.updateById(nextDefault);
            }
        }
        return CatalogApplicationService.mapOf("id", addressId);
    }

    @Transactional
    public Map<String, Object> setDefaultAddress(Long userId, Long addressId) {
        AddressPo target = userAddress(userId, addressId);
        clearDefault(addressMapper.selectList(new QueryWrapper<AddressPo>().eq("user_id", userId)));
        target.isDefault = 1;
        addressMapper.updateById(target);
        return CatalogApplicationService.mapOf("id", target.id);
    }

    @Transactional
    public Map<String, Object> createOrder(Long userId, CreateOrderRequest request) {
        AddressPo address = addressMapper.selectOne(new QueryWrapper<AddressPo>().eq("id", request.addressId).eq("user_id", userId).last("LIMIT 1"));
        if (address == null) throw new BusinessException("ADDRESS_NOT_FOUND");
        List<CartItemPo> cartItems = cartApplicationService.settleableItems(userId, request.cartItemIds);
        if (cartItems.isEmpty()) throw new BusinessException("CART_ITEM_NOT_FOUND");
        List<OrderItem> domainItems = new ArrayList<>();
        for (CartItemPo cartItem : cartItems) {
            SkuPo sku = skuMapper.selectById(cartItem.skuId);
            ProductPo product = sku == null ? null : productMapper.selectById(sku.productId);
            if (sku == null) throw new BusinessException("SKU_NOT_FOUND");
            if (product == null || !ProductStatus.ON_SALE.name().equals(product.status)) throw new BusinessException("PRODUCT_OFF_SALE");
            if (sku.stock < cartItem.quantity) throw new BusinessException("SKU_STOCK_NOT_ENOUGH");
            domainItems.add(new OrderItem(product.id, sku.id, product.title, sku.specName + "：" + sku.specValue, Money.ofCent(sku.price), cartItem.quantity, catalogApplicationService.firstImage(product.images)));
        }
        AddressSnapshot snapshot = new AddressSnapshot(address.receiverName, address.receiverPhone, address.region, address.detail);
        Order order = Order.create(userId, orderNo(), domainItems, snapshot);
        OrderPo orderPo = toPo(order);
        orderMapper.insert(orderPo);
        order.assignId(orderPo.id);
        for (OrderItem item : order.items()) {
            OrderItemPo po = new OrderItemPo();
            po.orderId = orderPo.id;
            po.productId = item.productId();
            po.skuId = item.skuId();
            po.titleSnapshot = item.titleSnapshot();
            po.imageSnapshot = item.imageSnapshot();
            po.skuSnapshot = item.skuSnapshot();
            po.unitPrice = item.unitPrice().cent();
            po.quantity = item.quantity();
            po.subtotal = item.subtotal().cent();
            orderItemMapper.insert(po);
        }
        for (CartItemPo item : cartItems) cartItemMapper.deleteById(item.id);
        return CatalogApplicationService.mapOf("id", orderPo.id, "orderNo", order.orderNo(), "status", order.status().name(), "productAmount", order.productAmount().cent(), "shippingFee", order.shippingFee().cent(), "payableAmount", order.payableAmount().cent());
    }

    public PageResult<Map<String, Object>> userOrders(Long userId, String status, int page, int pageSize) {
        QueryWrapper<OrderPo> wrapper = new QueryWrapper<OrderPo>().eq("user_id", userId).orderByDesc("id");
        if (status != null && !status.trim().isEmpty()) wrapper.eq("status", status);
        return orderPage(wrapper, page, pageSize);
    }

    public PageResult<Map<String, Object>> adminOrders(String status, int page, int pageSize) {
        QueryWrapper<OrderPo> wrapper = new QueryWrapper<OrderPo>().orderByDesc("id");
        if (status != null && !status.trim().isEmpty()) wrapper.eq("status", status);
        return orderPage(wrapper, page, pageSize);
    }

    public Map<String, Object> userOrderDetail(Long userId, Long orderId) {
        OrderPo order = orderMapper.selectOne(new QueryWrapper<OrderPo>().eq("id", orderId).eq("user_id", userId).last("LIMIT 1"));
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND");
        return detail(order);
    }

    public Map<String, Object> adminOrderDetail(Long orderId) {
        OrderPo order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND");
        return detail(order);
    }

    private PageResult<Map<String, Object>> orderPage(QueryWrapper<OrderPo> wrapper, int page, int pageSize) {
        Page<OrderPo> p = orderMapper.selectPage(new Page<>(Math.max(page, 1), Math.max(pageSize, 1)), wrapper);
        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderPo order : p.getRecords()) {
            items.add(CatalogApplicationService.mapOf("id", order.id, "orderNo", order.orderNo, "productAmount", order.productAmount, "shippingFee", order.shippingFee, "payableAmount", order.payableAmount, "status", order.status, "createdAt", order.createdAt));
        }
        return new PageResult<>(items, p.getTotal(), page, pageSize);
    }

    private Map<String, Object> detail(OrderPo order) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItemPo item : orderItemMapper.selectList(new QueryWrapper<OrderItemPo>().eq("order_id", order.id))) {
            items.add(CatalogApplicationService.mapOf("id", item.id, "productId", item.productId, "skuId", item.skuId, "titleSnapshot", item.titleSnapshot, "imageSnapshot", item.imageSnapshot, "skuSnapshot", item.skuSnapshot, "unitPrice", item.unitPrice, "quantity", item.quantity, "subtotal", item.subtotal));
        }
        ShipmentPo shipment = shipmentMapper.selectOne(new QueryWrapper<ShipmentPo>().eq("order_id", order.id).last("LIMIT 1"));
        Map<String, Object> shipmentView = shipment == null ? null : CatalogApplicationService.mapOf("logisticsCompany", shipment.logisticsCompany, "trackingNo", shipment.trackingNo, "shippedAt", shipment.shippedAt);
        return CatalogApplicationService.mapOf("id", order.id, "orderNo", order.orderNo, "userId", order.userId, "productAmount", order.productAmount, "shippingFee", order.shippingFee, "payableAmount", order.payableAmount, "status", order.status, "addressSnapshot", Jsons.readMap(order.addressSnapshot), "createdAt", order.createdAt, "paidAt", order.paidAt, "shippedAt", order.shippedAt, "items", items, "shipment", shipmentView);
    }

    private OrderPo toPo(Order order) {
        OrderPo po = new OrderPo();
        po.orderNo = order.orderNo();
        po.userId = order.userId();
        po.productAmount = order.productAmount().cent();
        po.shippingFee = order.shippingFee().cent();
        po.payableAmount = order.payableAmount().cent();
        po.status = order.status().name();
        po.addressSnapshot = Jsons.write(CatalogApplicationService.mapOf("receiverName", order.addressSnapshot().receiverName(), "receiverPhone", order.addressSnapshot().receiverPhone(), "region", order.addressSnapshot().region(), "detail", order.addressSnapshot().detail()));
        return po;
    }

    private String orderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + String.format("%04d", new Random().nextInt(10000));
    }

    public static class AddressRequest {
        public String receiverName;
        public String receiverPhone;
        public String region;
        public String detail;
        public Boolean isDefault;
    }

    public static class CreateOrderRequest {
        public Long addressId;
        public List<Long> cartItemIds;
    }

    private AddressPo userAddress(Long userId, Long addressId) {
        AddressPo po = addressMapper.selectById(addressId);
        if (po == null || !Objects.equals(po.userId, userId)) throw new BusinessException("ADDRESS_NOT_FOUND");
        return po;
    }

    private void validateAddress(AddressRequest request) {
        if (request == null) throw new BusinessException("ADDRESS_INVALID");
        if (isBlank(request.receiverName)) throw new BusinessException("ADDRESS_RECEIVER_NAME_REQUIRED");
        if (request.receiverPhone == null || !request.receiverPhone.matches("^1\\d{10}$")) throw new BusinessException("ADDRESS_RECEIVER_PHONE_INVALID");
        if (isBlank(request.region)) throw new BusinessException("ADDRESS_REGION_REQUIRED");
        if (isBlank(request.detail)) throw new BusinessException("ADDRESS_DETAIL_REQUIRED");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void clearDefault(List<AddressPo> addresses) {
        for (AddressPo item : addresses) {
            if (Integer.valueOf(1).equals(item.isDefault)) {
                item.isDefault = 0;
                addressMapper.updateById(item);
            }
        }
    }
}
