package com.kkmall.catalog.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kkmall.catalog.infrastructure.*;
import com.kkmall.common.application.Jsons;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminCatalogApplicationService {
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final CatalogApplicationService catalogApplicationService;
    private final ProductParameterMapper parameterMapper;
    private final ProductServicePromiseMapper servicePromiseMapper;
    private final ProductPromotionTextMapper promotionTextMapper;
    private final ProductReviewMapper reviewMapper;
    private final ProductRecommendationMapper recommendationMapper;

    public AdminCatalogApplicationService(ProductMapper productMapper,
                                          SkuMapper skuMapper,
                                          CatalogApplicationService catalogApplicationService,
                                          ProductParameterMapper parameterMapper,
                                          ProductServicePromiseMapper servicePromiseMapper,
                                          ProductPromotionTextMapper promotionTextMapper,
                                          ProductReviewMapper reviewMapper,
                                          ProductRecommendationMapper recommendationMapper) {
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
        this.catalogApplicationService = catalogApplicationService;
        this.parameterMapper = parameterMapper;
        this.servicePromiseMapper = servicePromiseMapper;
        this.promotionTextMapper = promotionTextMapper;
        this.reviewMapper = reviewMapper;
        this.recommendationMapper = recommendationMapper;
    }

    public List<Map<String, Object>> products(String status) {
        QueryWrapper<ProductPo> wrapper = new QueryWrapper<ProductPo>().orderByDesc("id");
        if (status != null && !status.trim().isEmpty()) wrapper.eq("status", status);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductPo product : productMapper.selectList(wrapper)) {
            List<SkuPo> skus = skuMapper.selectList(new QueryWrapper<SkuPo>().eq("product_id", product.id));
            long minPrice = skus.stream().mapToLong(sku -> sku.price).min().orElse(0L);
            int totalStock = skus.stream().mapToInt(sku -> sku.stock == null ? 0 : sku.stock).sum();
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
            sku.skuCode = blank(skuRequest.skuCode) ? "SKU-" + product.id + "-" + UUID.randomUUID().toString().substring(0, 8) : skuRequest.skuCode;
            sku.specName = blank(skuRequest.specName) ? "规格" : skuRequest.specName;
            sku.specValue = blank(skuRequest.specValue) ? "默认" : skuRequest.specValue;
            sku.specs = Jsons.write(skuRequest.specs == null ? CatalogApplicationService.mapOf(sku.specName, sku.specValue) : skuRequest.specs);
            sku.imageUrl = skuRequest.imageUrl;
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

    public Map<String, Object> detailConfig(Long productId) {
        ProductPo product = productMapper.selectById(productId);
        if (product == null) throw new IllegalArgumentException("PRODUCT_NOT_FOUND");
        return CatalogApplicationService.mapOf(
                "product", catalogApplicationService.productDetailView(product),
                "parameters", parameterRows(productId),
                "servicePromises", servicePromiseRows(productId),
                "promotions", promotionRows(productId),
                "reviews", reviewRows(productId),
                "storeRecommendationIds", recommendationIds(productId, "STORE_RECOMMEND"),
                "relatedRecommendationIds", recommendationIds(productId, "RELATED_RECOMMEND")
        );
    }

    @Transactional
    public Map<String, Object> saveDetailConfig(Long productId, DetailConfigRequest request) {
        ProductPo product = productMapper.selectById(productId);
        if (product == null) throw new IllegalArgumentException("PRODUCT_NOT_FOUND");
        if (request.brand != null) product.brand = request.brand;
        if (request.subtitle != null) product.subtitle = request.subtitle;
        if (request.sellingPoints != null) product.sellingPoints = Jsons.write(request.sellingPoints);
        if (request.detailHtml != null) product.detailHtml = request.detailHtml;
        if (request.attributes != null) product.attributes = Jsons.write(request.attributes);
        if (request.salesCount != null) product.salesCount = request.salesCount;
        productMapper.updateById(product);

        if (request.skus != null) updateSkuDetail(productId, request.skus);
        replaceParameters(productId, request.parameters);
        replaceServicePromises(productId, request.servicePromises);
        replacePromotions(productId, request.promotions);
        replaceRecommendations(productId, "STORE_RECOMMEND", request.storeRecommendationIds);
        replaceRecommendations(productId, "RELATED_RECOMMEND", request.relatedRecommendationIds);
        return CatalogApplicationService.mapOf("id", productId);
    }

    @Transactional
    public Map<String, Object> saveReview(Long productId, Long reviewId, ReviewRequest request) {
        ProductPo product = productMapper.selectById(productId);
        if (product == null) throw new IllegalArgumentException("PRODUCT_NOT_FOUND");
        ProductReviewPo row = reviewId == null ? new ProductReviewPo() : reviewMapper.selectById(reviewId);
        if (row == null) row = new ProductReviewPo();
        row.productId = productId;
        row.skuId = request.skuId;
        row.userNickname = blank(request.userNickname) ? "匿名买家" : request.userNickname;
        row.rating = request.rating == null ? 5 : Math.max(1, Math.min(5, request.rating));
        row.content = request.content == null ? "" : request.content;
        row.imageUrlsJson = Jsons.write(request.imageUrls == null ? Collections.emptyList() : request.imageUrls);
        row.tagsJson = Jsons.write(request.tags == null ? Collections.emptyList() : request.tags);
        row.reviewedAt = request.reviewedAt == null ? LocalDateTime.now() : request.reviewedAt;
        row.status = request.status == null ? "VISIBLE" : request.status;
        row.sortOrder = request.sortOrder == null ? 0 : request.sortOrder;
        if (row.id == null) reviewMapper.insert(row); else reviewMapper.updateById(row);
        return CatalogApplicationService.mapOf("id", row.id);
    }

    public Map<String, Object> deleteReview(Long productId, Long reviewId) {
        ProductReviewPo row = reviewMapper.selectById(reviewId);
        if (row == null || !Objects.equals(row.productId, productId)) throw new IllegalArgumentException("REVIEW_NOT_FOUND");
        reviewMapper.deleteById(reviewId);
        return CatalogApplicationService.mapOf("id", reviewId);
    }

    private void updateSkuDetail(Long productId, List<SkuRequest> rows) {
        for (SkuRequest request : rows) {
            if (request.id == null) continue;
            SkuPo sku = skuMapper.selectById(request.id);
            if (sku == null || !Objects.equals(sku.productId, productId)) continue;
            if (request.imageUrl != null) sku.imageUrl = request.imageUrl;
            if (request.marketPrice != null) sku.marketPrice = request.marketPrice;
            if (request.enabled != null) sku.enabled = request.enabled ? 1 : 0;
            skuMapper.updateById(sku);
        }
    }

    private List<Map<String, Object>> parameterRows(Long productId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductParameterPo row : parameterMapper.selectList(new QueryWrapper<ProductParameterPo>().eq("product_id", productId).orderByAsc("sort_order", "id"))) {
            result.add(CatalogApplicationService.mapOf("id", row.id, "name", row.name, "value", row.value, "sortOrder", row.sortOrder, "enabled", row.enabled));
        }
        return result;
    }

    private List<Map<String, Object>> servicePromiseRows(Long productId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductServicePromisePo row : servicePromiseMapper.selectList(new QueryWrapper<ProductServicePromisePo>().eq("product_id", productId).orderByAsc("sort_order", "id"))) {
            result.add(CatalogApplicationService.mapOf("id", row.id, "title", row.title, "description", row.description, "icon", row.icon, "sortOrder", row.sortOrder, "enabled", row.enabled));
        }
        return result;
    }

    private List<Map<String, Object>> promotionRows(Long productId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductPromotionTextPo row : promotionTextMapper.selectList(new QueryWrapper<ProductPromotionTextPo>().eq("product_id", productId).orderByAsc("sort_order", "id"))) {
            result.add(CatalogApplicationService.mapOf("id", row.id, "title", row.title, "description", row.description, "label", row.label, "startAt", row.startAt, "endAt", row.endAt, "sortOrder", row.sortOrder, "enabled", row.enabled));
        }
        return result;
    }

    private List<Map<String, Object>> reviewRows(Long productId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductReviewPo row : reviewMapper.selectList(new QueryWrapper<ProductReviewPo>().eq("product_id", productId).orderByAsc("sort_order", "id"))) {
            result.add(CatalogApplicationService.mapOf("id", row.id, "skuId", row.skuId, "userNickname", row.userNickname, "rating", row.rating, "content", row.content, "imageUrls", Jsons.readStringList(row.imageUrlsJson), "tags", Jsons.readStringList(row.tagsJson), "reviewedAt", row.reviewedAt, "status", row.status, "sortOrder", row.sortOrder));
        }
        return result;
    }

    private List<Long> recommendationIds(Long productId, String scene) {
        List<Long> result = new ArrayList<>();
        for (ProductRecommendationPo row : recommendationMapper.selectList(new QueryWrapper<ProductRecommendationPo>().eq("source_product_id", productId).eq("scene", scene).eq("enabled", 1).orderByAsc("sort_order", "id"))) {
            result.add(row.targetProductId);
        }
        return result;
    }

    private void replaceParameters(Long productId, List<ParameterRequest> rows) {
        if (rows == null) return;
        parameterMapper.delete(new QueryWrapper<ProductParameterPo>().eq("product_id", productId));
        int index = 0;
        for (ParameterRequest request : rows) {
            if (blank(request.name)) continue;
            ProductParameterPo row = new ProductParameterPo();
            row.productId = productId;
            row.name = request.name;
            row.value = request.value == null ? "" : request.value;
            row.sortOrder = request.sortOrder == null ? index * 10 : request.sortOrder;
            row.enabled = request.enabled == null || request.enabled ? 1 : 0;
            parameterMapper.insert(row);
            index++;
        }
    }

    private void replaceServicePromises(Long productId, List<ServicePromiseRequest> rows) {
        if (rows == null) return;
        servicePromiseMapper.delete(new QueryWrapper<ProductServicePromisePo>().eq("product_id", productId));
        int index = 0;
        for (ServicePromiseRequest request : rows) {
            if (blank(request.title)) continue;
            ProductServicePromisePo row = new ProductServicePromisePo();
            row.productId = productId;
            row.title = request.title;
            row.description = request.description;
            row.icon = request.icon;
            row.sortOrder = request.sortOrder == null ? index * 10 : request.sortOrder;
            row.enabled = request.enabled == null || request.enabled ? 1 : 0;
            servicePromiseMapper.insert(row);
            index++;
        }
    }

    private void replacePromotions(Long productId, List<PromotionRequest> rows) {
        if (rows == null) return;
        promotionTextMapper.delete(new QueryWrapper<ProductPromotionTextPo>().eq("product_id", productId));
        int index = 0;
        for (PromotionRequest request : rows) {
            if (blank(request.title)) continue;
            ProductPromotionTextPo row = new ProductPromotionTextPo();
            row.productId = productId;
            row.title = request.title;
            row.description = request.description;
            row.label = request.label;
            row.startAt = request.startAt;
            row.endAt = request.endAt;
            row.sortOrder = request.sortOrder == null ? index * 10 : request.sortOrder;
            row.enabled = request.enabled == null || request.enabled ? 1 : 0;
            promotionTextMapper.insert(row);
            index++;
        }
    }

    private void replaceRecommendations(Long productId, String scene, List<Long> ids) {
        if (ids == null) return;
        recommendationMapper.delete(new QueryWrapper<ProductRecommendationPo>().eq("source_product_id", productId).eq("scene", scene));
        int index = 0;
        for (Long targetId : ids) {
            if (targetId == null || Objects.equals(targetId, productId)) continue;
            ProductRecommendationPo row = new ProductRecommendationPo();
            row.sourceProductId = productId;
            row.targetProductId = targetId;
            row.scene = scene;
            row.sortOrder = index * 10;
            row.enabled = 1;
            recommendationMapper.insert(row);
            index++;
        }
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
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
        public Long id;
        public String skuCode;
        public String specName;
        public String specValue;
        public Map<String, Object> specs;
        public String imageUrl;
        public Long price;
        public Long marketPrice;
        public Long costPrice;
        public Integer stock;
        public Integer weightGrams;
        public String barcode;
        public Boolean enabled;
    }

    public static class DetailConfigRequest {
        public String brand;
        public String subtitle;
        public List<String> sellingPoints;
        public String detailHtml;
        public Map<String, Object> attributes;
        public Integer salesCount;
        public List<SkuRequest> skus;
        public List<ParameterRequest> parameters;
        public List<ServicePromiseRequest> servicePromises;
        public List<PromotionRequest> promotions;
        public List<Long> storeRecommendationIds;
        public List<Long> relatedRecommendationIds;
    }

    public static class ParameterRequest {
        public String name;
        public String value;
        public Integer sortOrder;
        public Boolean enabled;
    }

    public static class ServicePromiseRequest {
        public String title;
        public String description;
        public String icon;
        public Integer sortOrder;
        public Boolean enabled;
    }

    public static class PromotionRequest {
        public String title;
        public String description;
        public String label;
        public LocalDateTime startAt;
        public LocalDateTime endAt;
        public Integer sortOrder;
        public Boolean enabled;
    }

    public static class ReviewRequest {
        public Long skuId;
        public String userNickname;
        public Integer rating;
        public String content;
        public List<String> imageUrls;
        public List<String> tags;
        public LocalDateTime reviewedAt;
        public String status;
        public Integer sortOrder;
    }
}
