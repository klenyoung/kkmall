package com.kkmall.catalog.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kkmall.catalog.domain.ProductStatus;
import com.kkmall.catalog.infrastructure.*;
import com.kkmall.common.application.Jsons;
import com.kkmall.common.exception.BusinessException;
import com.kkmall.common.interfaces.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class CatalogApplicationService {
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final ProductParameterMapper parameterMapper;
    private final ProductServicePromiseMapper servicePromiseMapper;
    private final ProductPromotionTextMapper promotionTextMapper;
    private final ProductReviewMapper reviewMapper;
    private final ProductRecommendationMapper recommendationMapper;

    public CatalogApplicationService(CategoryMapper categoryMapper, ProductMapper productMapper, SkuMapper skuMapper) {
        this(categoryMapper, productMapper, skuMapper, null, null, null, null, null);
    }

    @Autowired
    public CatalogApplicationService(CategoryMapper categoryMapper,
                                     ProductMapper productMapper,
                                     SkuMapper skuMapper,
                                     ProductParameterMapper parameterMapper,
                                     ProductServicePromiseMapper servicePromiseMapper,
                                     ProductPromotionTextMapper promotionTextMapper,
                                     ProductReviewMapper reviewMapper,
                                     ProductRecommendationMapper recommendationMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
        this.parameterMapper = parameterMapper;
        this.servicePromiseMapper = servicePromiseMapper;
        this.promotionTextMapper = promotionTextMapper;
        this.reviewMapper = reviewMapper;
        this.recommendationMapper = recommendationMapper;
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
        long minPrice = 0L;
        long maxPrice = 0L;
        for (SkuPo sku : skus) {
            boolean sellable = sku.enabled != null && sku.enabled == 1 && sku.stock != null && sku.stock > 0;
            if (minPrice == 0L || sku.price < minPrice) minPrice = sku.price;
            if (sku.price > maxPrice) maxPrice = sku.price;
            skuViews.add(mapOf(
                    "id", sku.id,
                    "skuCode", sku.skuCode,
                    "specName", sku.specName,
                    "specValue", sku.specValue,
                    "specs", Jsons.readMap(sku.specs),
                    "imageUrl", sku.imageUrl,
                    "price", sku.price,
                    "marketPrice", sku.marketPrice,
                    "originPrice", sku.marketPrice,
                    "costPrice", sku.costPrice,
                    "stock", sku.stock,
                    "weightGrams", sku.weightGrams,
                    "barcode", sku.barcode,
                    "enabled", sku.enabled,
                    "sellable", sellable
            ));
        }
        List<String> images = Jsons.readStringList(product.images);
        Map<String, Object> reviewSummary = reviewSummary(product.id);
        List<Map<String, Object>> storeRecommendations = recommendations(product.id, "STORE_RECOMMEND");
        List<Map<String, Object>> relatedRecommendations = recommendations(product.id, "RELATED_RECOMMEND");
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
                "images", images,
                "imageUrls", images,
                "mainImage", coverImage(product),
                "mainImageUrl", coverImage(product),
                "coverImage", coverImage(product),
                "salesCount", valueOrZero(product.salesCount),
                "sortOrder", product.sortOrder,
                "status", product.status,
                "tags", tags(product),
                "priceRange", mapOf("minPrice", minPrice, "maxPrice", maxPrice, "currency", "CNY"),
                "skus", skuViews,
                "promotions", promotions(product.id),
                "servicePromises", servicePromises(product.id),
                "parameters", parameters(product),
                "reviewSummary", reviewSummary,
                "reviews", reviews(product.id),
                "storeRecommendations", storeRecommendations,
                "relatedRecommendations", relatedRecommendations
        );
    }

    private List<String> tags(ProductPo product) {
        List<String> result = new ArrayList<>();
        if (product.brand != null && product.brand.contains("自营")) result.add("自营");
        result.addAll(Jsons.readStringList(product.sellingPoints));
        return result.stream().filter(Objects::nonNull).distinct().limit(6).collect(Collectors.toList());
    }

    private List<Map<String, Object>> promotions(Long productId) {
        if (promotionTextMapper == null) return Collections.emptyList();
        LocalDateTime now = LocalDateTime.now();
        List<ProductPromotionTextPo> rows = promotionTextMapper.selectList(new QueryWrapper<ProductPromotionTextPo>()
                .eq("product_id", productId)
                .eq("enabled", 1)
                .orderByAsc("sort_order", "id"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductPromotionTextPo row : rows) {
            boolean started = row.startAt == null || !row.startAt.isAfter(now);
            boolean notEnded = row.endAt == null || !row.endAt.isBefore(now);
            if (started && notEnded) {
                result.add(mapOf("id", row.id, "title", row.title, "description", row.description, "label", row.label, "type", "TEXT"));
            }
        }
        return result;
    }

    private List<Map<String, Object>> servicePromises(Long productId) {
        if (servicePromiseMapper == null) return Collections.emptyList();
        List<ProductServicePromisePo> rows = servicePromiseMapper.selectList(new QueryWrapper<ProductServicePromisePo>()
                .eq("product_id", productId)
                .eq("enabled", 1)
                .orderByAsc("sort_order", "id"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductServicePromisePo row : rows) {
            result.add(mapOf("id", row.id, "title", row.title, "description", row.description, "icon", row.icon));
        }
        return result;
    }

    private List<Map<String, Object>> parameters(ProductPo product) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (parameterMapper != null) {
            List<ProductParameterPo> rows = parameterMapper.selectList(new QueryWrapper<ProductParameterPo>()
                    .eq("product_id", product.id)
                    .eq("enabled", 1)
                    .orderByAsc("sort_order", "id"));
            for (ProductParameterPo row : rows) {
                result.add(mapOf("id", row.id, "name", row.name, "value", row.value));
            }
        }
        if (result.isEmpty()) {
            Jsons.readMap(product.attributes).forEach((key, value) -> result.add(mapOf("name", key, "value", String.valueOf(value))));
        }
        return result;
    }

    private Map<String, Object> reviewSummary(Long productId) {
        List<Map<String, Object>> reviews = reviews(productId);
        if (reviews.isEmpty()) return mapOf("reviewCount", 0, "goodRate", 0, "averageRating", 0, "tags", Collections.emptyList());
        int count = reviews.size();
        int ratingSum = 0;
        int good = 0;
        Set<String> tags = new LinkedHashSet<>();
        for (Map<String, Object> review : reviews) {
            Integer rating = (Integer) review.get("rating");
            ratingSum += rating == null ? 0 : rating;
            if (rating != null && rating >= 4) good++;
            Object reviewTags = review.get("tags");
            if (reviewTags instanceof List) {
                for (Object tag : (List<?>) reviewTags) tags.add(String.valueOf(tag));
            }
        }
        double average = Math.round((ratingSum * 10.0 / count)) / 10.0;
        return mapOf("reviewCount", count, "goodRate", good * 100 / count, "averageRating", average, "tags", new ArrayList<>(tags));
    }

    private List<Map<String, Object>> reviews(Long productId) {
        if (reviewMapper == null) return Collections.emptyList();
        List<ProductReviewPo> rows = reviewMapper.selectList(new QueryWrapper<ProductReviewPo>()
                .eq("product_id", productId)
                .eq("status", "VISIBLE")
                .orderByAsc("sort_order")
                .orderByDesc("reviewed_at")
                .last("LIMIT 5"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductReviewPo row : rows) {
            result.add(mapOf(
                    "id", row.id,
                    "skuId", row.skuId,
                    "userNickname", row.userNickname,
                    "rating", row.rating,
                    "content", row.content,
                    "imageUrls", Jsons.readStringList(row.imageUrlsJson),
                    "tags", Jsons.readStringList(row.tagsJson),
                    "reviewedAt", row.reviewedAt
            ));
        }
        return result;
    }

    private List<Map<String, Object>> recommendations(Long productId, String scene) {
        if (recommendationMapper == null || productMapper == null) return Collections.emptyList();
        List<ProductRecommendationPo> rows = recommendationMapper.selectList(new QueryWrapper<ProductRecommendationPo>()
                .eq("source_product_id", productId)
                .eq("scene", scene)
                .eq("enabled", 1)
                .orderByAsc("sort_order", "id")
                .last("LIMIT 8"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductRecommendationPo row : rows) {
            if (Objects.equals(row.targetProductId, productId)) continue;
            ProductPo target = productMapper.selectById(row.targetProductId);
            if (target == null || !ProductStatus.ON_SALE.name().equals(target.status)) continue;
            List<SkuPo> skus = skuMapper.selectList(new QueryWrapper<SkuPo>().eq("product_id", target.id).eq("enabled", 1));
            long minPrice = skus.stream().mapToLong(sku -> sku.price).min().orElse(0L);
            result.add(mapOf(
                    "id", target.id,
                    "title", target.title,
                    "brand", target.brand,
                    "coverImage", coverImage(target),
                    "mainImage", coverImage(target),
                    "minPrice", minPrice,
                    "salesCount", valueOrZero(target.salesCount)
            ));
        }
        return result;
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
