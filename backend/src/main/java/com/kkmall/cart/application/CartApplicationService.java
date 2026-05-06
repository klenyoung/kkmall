package com.kkmall.cart.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kkmall.cart.infrastructure.CartItemMapper;
import com.kkmall.cart.infrastructure.CartItemPo;
import com.kkmall.catalog.application.CatalogApplicationService;
import com.kkmall.catalog.domain.ProductStatus;
import com.kkmall.catalog.infrastructure.ProductMapper;
import com.kkmall.catalog.infrastructure.ProductPo;
import com.kkmall.catalog.infrastructure.SkuMapper;
import com.kkmall.catalog.infrastructure.SkuPo;
import com.kkmall.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CartApplicationService {
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final CatalogApplicationService catalogApplicationService;

    public CartApplicationService(CartItemMapper cartItemMapper, ProductMapper productMapper, SkuMapper skuMapper, CatalogApplicationService catalogApplicationService) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
        this.catalogApplicationService = catalogApplicationService;
    }

    @Transactional
    public Map<String, Object> add(Long userId, Long skuId, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("QUANTITY_INVALID");
        SkuPo sku = skuMapper.selectById(skuId);
        if (sku == null) throw new BusinessException("SKU_NOT_FOUND");
        ProductPo product = productMapper.selectById(sku.productId);
        if (product == null || !ProductStatus.ON_SALE.name().equals(product.status)) throw new BusinessException("PRODUCT_OFF_SALE");
        CartItemPo existing = cartItemMapper.selectOne(new QueryWrapper<CartItemPo>().eq("user_id", userId).eq("sku_id", skuId).last("LIMIT 1"));
        int nextQty = (existing == null ? 0 : existing.quantity) + quantity;
        if (nextQty > sku.stock) throw new BusinessException("SKU_STOCK_NOT_ENOUGH");
        if (existing == null) {
            existing = new CartItemPo();
            existing.userId = userId;
            existing.productId = sku.productId;
            existing.skuId = skuId;
            existing.quantity = nextQty;
            cartItemMapper.insert(existing);
        } else {
            existing.quantity = nextQty;
            cartItemMapper.updateById(existing);
        }
        return CatalogApplicationService.mapOf("id", existing.id, "skuId", skuId, "quantity", nextQty);
    }

    public Map<String, Object> update(Long userId, Long id, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("QUANTITY_INVALID");
        CartItemPo item = cartItem(userId, id);
        SkuPo sku = skuMapper.selectById(item.skuId);
        if (sku == null) throw new BusinessException("SKU_NOT_FOUND");
        if (quantity > sku.stock) throw new BusinessException("SKU_STOCK_NOT_ENOUGH");
        item.quantity = quantity;
        cartItemMapper.updateById(item);
        return CatalogApplicationService.mapOf("id", id, "quantity", quantity);
    }

    public void delete(Long userId, Long id) {
        cartItem(userId, id);
        cartItemMapper.deleteById(id);
    }

    public Map<String, Object> list(Long userId) {
        List<CartItemPo> rows = cartItemMapper.selectList(new QueryWrapper<CartItemPo>().eq("user_id", userId).orderByDesc("id"));
        List<Map<String, Object>> items = new ArrayList<>();
        long productAmount = 0;
        for (CartItemPo row : rows) {
            SkuPo sku = skuMapper.selectById(row.skuId);
            ProductPo product = sku == null ? null : productMapper.selectById(sku.productId);
            boolean settleable = sku != null && product != null && ProductStatus.ON_SALE.name().equals(product.status) && sku.stock >= row.quantity;
            long subtotal = sku == null ? 0 : sku.price * row.quantity;
            if (settleable) productAmount += subtotal;
            items.add(CatalogApplicationService.mapOf(
                "id", row.id, "productId", row.productId, "skuId", row.skuId,
                "title", product == null ? "-" : product.title,
                "image", product == null ? null : catalogApplicationService.firstImage(product.images),
                "specText", sku == null ? "-" : sku.specName + "：" + sku.specValue,
                "price", sku == null ? 0 : sku.price,
                "quantity", row.quantity,
                "stock", sku == null ? 0 : sku.stock,
                "subtotal", subtotal,
                "settleable", settleable,
                "unsettleableReason", settleable ? null : "不可结算"
            ));
        }
        return CatalogApplicationService.mapOf("items", items, "productAmount", productAmount);
    }

    public List<CartItemPo> settleableItems(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return cartItemMapper.selectList(new QueryWrapper<CartItemPo>().eq("user_id", userId).in("id", ids));
    }

    private CartItemPo cartItem(Long userId, Long id) {
        CartItemPo item = cartItemMapper.selectOne(new QueryWrapper<CartItemPo>().eq("id", id).eq("user_id", userId).last("LIMIT 1"));
        if (item == null) throw new BusinessException("CART_ITEM_NOT_FOUND");
        return item;
    }
}
