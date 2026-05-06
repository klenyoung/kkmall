package com.kkmall.catalog.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kkmall.catalog.domain.ProductStatus;
import com.kkmall.catalog.infrastructure.*;
import com.kkmall.common.application.Jsons;
import com.kkmall.common.exception.BusinessException;
import com.kkmall.common.interfaces.PageResult;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CatalogApplicationService {
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;

    public CatalogApplicationService(CategoryMapper categoryMapper, ProductMapper productMapper, SkuMapper skuMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
    }

    public List<Map<String, Object>> categories() {
        List<CategoryPo> rows = categoryMapper.selectList(new QueryWrapper<CategoryPo>().eq("enabled", 1).orderByAsc("sort_order", "id"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (CategoryPo row : rows) {
            result.add(mapOf("id", row.id, "name", row.name, "sortOrder", row.sortOrder, "enabled", row.enabled));
        }
        return result;
    }

    public PageResult<Map<String, Object>> products(Long categoryId, String keyword, int page, int pageSize) {
        QueryWrapper<ProductPo> wrapper = new QueryWrapper<ProductPo>().eq("status", ProductStatus.ON_SALE.name()).orderByDesc("id");
        if (categoryId != null) wrapper.eq("category_id", categoryId);
        if (keyword != null && !keyword.trim().isEmpty()) wrapper.like("title", keyword.trim());
        Page<ProductPo> p = productMapper.selectPage(new Page<>(Math.max(page, 1), Math.max(pageSize, 1)), wrapper);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ProductPo product : p.getRecords()) {
            List<SkuPo> skus = skuMapper.selectList(new QueryWrapper<SkuPo>().eq("product_id", product.id));
            long minPrice = skus.stream().mapToLong(sku -> sku.price).min().orElse(0L);
            items.add(mapOf(
                    "id", product.id,
                    "title", product.title,
                    "brand", product.brand,
                    "subtitle", product.subtitle,
                    "coverImage", coverImage(product),
                    "mainImage", coverImage(product),
                    "minPrice", minPrice,
                    "salesCount", valueOrZero(product.salesCount),
                    "status", product.status
            ));
        }
        return new PageResult<>(items, p.getTotal(), page, pageSize);
    }

    public Map<String, Object> productDetail(Long id) {
        ProductPo product = productMapper.selectOne(new QueryWrapper<ProductPo>().eq("id", id).eq("status", ProductStatus.ON_SALE.name()).last("LIMIT 1"));
        if (product == null) throw new BusinessException("PRODUCT_NOT_FOUND");
        return productDetailView(product);
    }

    public Map<String, Object> productDetailView(ProductPo product) {
        List<SkuPo> skus = skuMapper.selectList(new QueryWrapper<SkuPo>().eq("product_id", product.id).orderByAsc("id"));
        List<Map<String, Object>> skuViews = new ArrayList<>();
        for (SkuPo sku : skus) {
            skuViews.add(mapOf(
                    "id", sku.id,
                    "skuCode", sku.skuCode,
                    "specName", sku.specName,
                    "specValue", sku.specValue,
                    "specs", Jsons.readMap(sku.specs),
                    "price", sku.price,
                    "marketPrice", sku.marketPrice,
                    "costPrice", sku.costPrice,
                    "stock", sku.stock,
                    "weightGrams", sku.weightGrams,
                    "barcode", sku.barcode,
                    "enabled", sku.enabled
            ));
        }
        return mapOf(
                "id", product.id,
                "categoryId", product.categoryId,
                "title", product.title,
                "brand", product.brand,
                "subtitle", product.subtitle,
                "description", product.description,
                "sellingPoints", Jsons.readStringList(product.sellingPoints),
                "unit", product.unit,
                "detailHtml", product.detailHtml,
                "attributes", Jsons.readMap(product.attributes),
                "images", Jsons.readStringList(product.images),
                "mainImage", coverImage(product),
                "coverImage", coverImage(product),
                "salesCount", valueOrZero(product.salesCount),
                "sortOrder", product.sortOrder,
                "status", product.status,
                "skus", skuViews
        );
    }

    public String firstImage(String images) {
        List<String> list = Jsons.readStringList(images);
        return list.isEmpty() ? null : list.get(0);
    }

    public String coverImage(ProductPo product) {
        if (product.mainImage != null && !product.mainImage.trim().isEmpty()) return product.mainImage;
        return firstImage(product.images);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    public static Map<String, Object> mapOf(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) map.put(String.valueOf(entries[i]), entries[i + 1]);
        return map;
    }
}
