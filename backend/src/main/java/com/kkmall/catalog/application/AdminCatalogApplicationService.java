package com.kkmall.catalog.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kkmall.catalog.infrastructure.ProductMapper;
import com.kkmall.catalog.infrastructure.ProductPo;
import com.kkmall.catalog.infrastructure.SkuMapper;
import com.kkmall.catalog.infrastructure.SkuPo;
import com.kkmall.common.application.Jsons;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AdminCatalogApplicationService {
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final CatalogApplicationService catalogApplicationService;

    public AdminCatalogApplicationService(ProductMapper productMapper, SkuMapper skuMapper, CatalogApplicationService catalogApplicationService) {
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
        this.catalogApplicationService = catalogApplicationService;
    }

    public List<Map<String, Object>> products(String status) {
        QueryWrapper<ProductPo> wrapper = new QueryWrapper<ProductPo>().orderByDesc("id");
        if (status != null && !status.trim().isEmpty()) wrapper.eq("status", status);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductPo product : productMapper.selectList(wrapper)) {
            List<SkuPo> skus = skuMapper.selectList(new QueryWrapper<SkuPo>().eq("product_id", product.id));
            long minPrice = skus.stream().mapToLong(sku -> sku.price).min().orElse(0L);
            int totalStock = skus.stream().mapToInt(sku -> sku.stock).sum();
            result.add(CatalogApplicationService.mapOf(
                    "id", product.id,
                    "title", product.title,
                    "brand", product.brand,
                    "subtitle", product.subtitle,
                    "categoryId", product.categoryId,
                    "status", product.status,
                    "images", Jsons.readStringList(product.images),
                    "mainImage", catalogApplicationService.coverImage(product),
                    "coverImage", catalogApplicationService.coverImage(product),
                    "minPrice", minPrice,
                    "totalStock", totalStock,
                    "salesCount", product.salesCount == null ? 0 : product.salesCount,
                    "sortOrder", product.sortOrder == null ? 0 : product.sortOrder
            ));
        }
        return result;
    }

    @Transactional
    public Map<String, Object> saveProduct(Long id, ProductRequest request) {
        ProductPo product = id == null ? new ProductPo() : productMapper.selectById(id);
        if (product == null) product = new ProductPo();
        product.categoryId = request.categoryId;
        product.title = request.title;
        product.brand = request.brand;
        product.subtitle = request.subtitle;
        product.description = request.description;
        product.sellingPoints = Jsons.write(request.sellingPoints == null ? Collections.emptyList() : request.sellingPoints);
        product.unit = request.unit == null || request.unit.trim().isEmpty() ? "件" : request.unit;
        product.detailHtml = request.detailHtml;
        product.attributes = Jsons.write(request.attributes == null ? Collections.emptyMap() : request.attributes);
        product.images = Jsons.write(request.images == null ? Collections.emptyList() : request.images);
        product.mainImage = request.mainImage == null || request.mainImage.trim().isEmpty()
                ? (request.images == null || request.images.isEmpty() ? null : request.images.get(0))
                : request.mainImage;
        product.salesCount = request.salesCount == null ? 0 : request.salesCount;
        product.sortOrder = request.sortOrder == null ? 0 : request.sortOrder;
        product.status = request.status == null ? "DRAFT" : request.status;
        if (product.id == null) productMapper.insert(product); else productMapper.updateById(product);
        if (id != null) {
            for (SkuPo sku : skuMapper.selectList(new QueryWrapper<SkuPo>().eq("product_id", product.id))) {
                skuMapper.deleteById(sku.id);
            }
        }
        List<SkuRequest> skuRequests = request.skus == null || request.skus.isEmpty() ? Collections.singletonList(new SkuRequest()) : request.skus;
        for (SkuRequest skuRequest : skuRequests) {
            SkuPo sku = new SkuPo();
            sku.productId = product.id;
            sku.skuCode = skuRequest.skuCode == null || skuRequest.skuCode.trim().isEmpty()
                    ? "SKU-" + product.id + "-" + UUID.randomUUID().toString().substring(0, 8)
                    : skuRequest.skuCode;
            sku.specName = skuRequest.specName == null || skuRequest.specName.trim().isEmpty() ? "规格" : skuRequest.specName;
            sku.specValue = skuRequest.specValue == null || skuRequest.specValue.trim().isEmpty() ? "默认" : skuRequest.specValue;
            sku.specs = Jsons.write(skuRequest.specs == null ? CatalogApplicationService.mapOf(sku.specName, sku.specValue) : skuRequest.specs);
            sku.price = skuRequest.price == null ? 0L : skuRequest.price;
            sku.marketPrice = skuRequest.marketPrice == null ? sku.price : skuRequest.marketPrice;
            sku.costPrice = skuRequest.costPrice == null ? sku.price : skuRequest.costPrice;
            sku.stock = skuRequest.stock == null ? 0 : skuRequest.stock;
            sku.weightGrams = skuRequest.weightGrams;
            sku.barcode = skuRequest.barcode;
            sku.enabled = skuRequest.enabled == null || skuRequest.enabled ? 1 : 0;
            skuMapper.insert(sku);
        }
        return CatalogApplicationService.mapOf("id", product.id);
    }

    public Map<String, Object> updateStatus(Long id, String status) {
        ProductPo product = productMapper.selectById(id);
        if (product == null) throw new IllegalArgumentException("PRODUCT_NOT_FOUND");
        product.status = status;
        productMapper.updateById(product);
        return CatalogApplicationService.mapOf("id", id, "status", status);
    }

    public Map<String, Object> productDetail(Long id) {
        ProductPo product = productMapper.selectById(id);
        if (product == null) throw new IllegalArgumentException("PRODUCT_NOT_FOUND");
        return catalogApplicationService.productDetailView(product);
    }

    public static class ProductRequest {
        public Long categoryId;
        public String title;
        public String brand;
        public String subtitle;
        public String description;
        public List<String> sellingPoints;
        public String unit;
        public String detailHtml;
        public Map<String, Object> attributes;
        public List<String> images;
        public String mainImage;
        public Integer salesCount;
        public Integer sortOrder;
        public String status;
        public List<SkuRequest> skus = new ArrayList<>();
    }

    public static class SkuRequest {
        public String skuCode;
        public String specName;
        public String specValue;
        public Map<String, Object> specs;
        public Long price;
        public Long marketPrice;
        public Long costPrice;
        public Integer stock;
        public Integer weightGrams;
        public String barcode;
        public Boolean enabled;
    }
}
