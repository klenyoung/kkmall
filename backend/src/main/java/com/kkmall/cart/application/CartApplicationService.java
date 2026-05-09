package com.kkmall.cart.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kkmall.cart.infrastructure.CartItemMapper;
import com.kkmall.cart.infrastructure.CartItemPo;
import com.kkmall.cart.interfaces.dto.CartItemDto;
import com.kkmall.cart.interfaces.dto.CartOperationResultDto;
import com.kkmall.cart.interfaces.dto.CartViewDto;
import com.kkmall.catalog.application.CatalogApplicationService;
import com.kkmall.catalog.domain.ProductStatus;
import com.kkmall.catalog.infrastructure.ProductMapper;
import com.kkmall.catalog.infrastructure.ProductPo;
import com.kkmall.catalog.infrastructure.SkuMapper;
import com.kkmall.catalog.infrastructure.SkuPo;
import com.kkmall.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 购物车应用服务。
 */
@Service
public class CartApplicationService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final CatalogApplicationService catalogApplicationService;

    public CartApplicationService(CartItemMapper cartItemMapper, ProductMapper productMapper,
                                  SkuMapper skuMapper, CatalogApplicationService catalogApplicationService) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
        this.catalogApplicationService = catalogApplicationService;
    }

    @Transactional
    public CartOperationResultDto add(Long userId, Long skuId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("QUANTITY_INVALID");
        }
        SkuPo sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("SKU_NOT_FOUND");
        }
        ProductPo product = productMapper.selectById(sku.getProductId());
        if (product == null || !ProductStatus.ON_SALE.name().equals(product.getStatus())) {
            throw new BusinessException("PRODUCT_OFF_SALE");
        }
        CartItemPo existing = cartItemMapper.selectOne(
                new QueryWrapper<CartItemPo>().eq("user_id", userId).eq("sku_id", skuId).last("LIMIT 1"));
        int nextQty = (existing == null ? 0 : existing.getQuantity()) + quantity;
        if (nextQty > sku.getStock()) {
            throw new BusinessException("SKU_STOCK_NOT_ENOUGH");
        }
        if (existing == null) {
            existing = new CartItemPo();
            existing.setUserId(userId);
            existing.setProductId(sku.getProductId());
            existing.setSkuId(skuId);
            existing.setQuantity(nextQty);
            cartItemMapper.insert(existing);
        } else {
            existing.setQuantity(nextQty);
            cartItemMapper.updateById(existing);
        }
        CartOperationResultDto result = new CartOperationResultDto();
        result.setId(existing.getId());
        result.setSkuId(skuId);
        result.setQuantity(nextQty);
        return result;
    }

    public CartOperationResultDto update(Long userId, Long id, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("QUANTITY_INVALID");
        }
        CartItemPo item = cartItem(userId, id);
        SkuPo sku = skuMapper.selectById(item.getSkuId());
        if (sku == null) {
            throw new BusinessException("SKU_NOT_FOUND");
        }
        if (quantity > sku.getStock()) {
            throw new BusinessException("SKU_STOCK_NOT_ENOUGH");
        }
        item.setQuantity(quantity);
        cartItemMapper.updateById(item);
        CartOperationResultDto result = new CartOperationResultDto();
        result.setId(id);
        result.setSkuId(item.getSkuId());
        result.setQuantity(quantity);
        return result;
    }

    public void delete(Long userId, Long id) {
        cartItem(userId, id);
        cartItemMapper.deleteById(id);
    }

    public CartViewDto list(Long userId) {
        List<CartItemPo> rows = cartItemMapper.selectList(
                new QueryWrapper<CartItemPo>().eq("user_id", userId).orderByDesc("id"));
        List<CartItemDto> items = new ArrayList<>();
        long productAmount = 0;
        for (CartItemPo row : rows) {
            SkuPo sku = skuMapper.selectById(row.getSkuId());
            ProductPo product = sku == null ? null : productMapper.selectById(sku.getProductId());
            boolean settleable = sku != null && product != null
                    && ProductStatus.ON_SALE.name().equals(product.getStatus())
                    && sku.getStock() >= row.getQuantity();
            long subtotal = sku == null ? 0 : sku.getPrice() * row.getQuantity();
            if (settleable) {
                productAmount += subtotal;
            }
            CartItemDto itemDto = new CartItemDto();
            itemDto.setId(row.getId());
            itemDto.setProductId(row.getProductId());
            itemDto.setSkuId(row.getSkuId());
            itemDto.setTitle(product == null ? "-" : product.getTitle());
            itemDto.setImage(product == null ? null : catalogApplicationService.firstImage(product.getImages()));
            itemDto.setSpecText(sku == null ? "-" : sku.getSpecName() + "：" + sku.getSpecValue());
            itemDto.setPrice(sku == null ? 0L : sku.getPrice());
            itemDto.setQuantity(row.getQuantity());
            itemDto.setStock(sku == null ? 0 : sku.getStock());
            itemDto.setSubtotal(subtotal);
            itemDto.setSettleable(settleable);
            itemDto.setUnsettleableReason(settleable ? null : "不可结算");
            items.add(itemDto);
        }
        CartViewDto view = new CartViewDto();
        view.setItems(items);
        view.setProductAmount(productAmount);
        return view;
    }

    public List<CartItemPo> settleableItems(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return cartItemMapper.selectList(new QueryWrapper<CartItemPo>().eq("user_id", userId).in("id", ids));
    }

    private CartItemPo cartItem(Long userId, Long id) {
        CartItemPo item = cartItemMapper.selectOne(
                new QueryWrapper<CartItemPo>().eq("id", id).eq("user_id", userId).last("LIMIT 1"));
        if (item == null) {
            throw new BusinessException("CART_ITEM_NOT_FOUND");
        }
        return item;
    }
}
