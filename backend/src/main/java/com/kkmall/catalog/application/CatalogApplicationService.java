package com.kkmall.catalog.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kkmall.catalog.domain.ProductStatus;
import com.kkmall.catalog.infrastructure.*;
import com.kkmall.catalog.interfaces.dto.*;
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

    public List<CategoryDto> categories() {
        List<CategoryPo> rows = categoryMapper.selectList(new QueryWrapper<CategoryPo>().eq("enabled", 1).orderByAsc("sort_order", "id"));
        List<CategoryDto> result = new ArrayList<>();
        for (CategoryPo row : rows) {
            CategoryDto dto = new CategoryDto();
            dto.setId(row.getId());
            dto.setName(row.getName());
            dto.setSortOrder(row.getSortOrder());
            dto.setEnabled(row.getEnabled());
            result.add(dto);
        }
        return result;
    }

    public PageResult<ProductCardDto> products(Long categoryId, String keyword, int page, int pageSize) {
        QueryWrapper<ProductPo> wrapper = new QueryWrapper<ProductPo>().eq("status", ProductStatus.ON_SALE.name()).orderByDesc("id");
        if (categoryId != null) wrapper.eq("category_id", categoryId);
        if (keyword != null && !keyword.trim().isEmpty()) wrapper.like("title", keyword.trim());
        Page<ProductPo> p = productMapper.selectPage(new Page<>(Math.max(page, 1), Math.max(pageSize, 1)), wrapper);
        List<ProductCardDto> items = new ArrayList<>();
        for (ProductPo product : p.getRecords()) {
            List<SkuPo> skus = skuMapper.selectList(new QueryWrapper<SkuPo>().eq("product_id", product.getId()));
            long minPrice = skus.stream().mapToLong(sku -> sku.getPrice()).min().orElse(0L);
            ProductCardDto dto = new ProductCardDto();
            dto.setId(product.getId());
            dto.setTitle(product.getTitle());
            dto.setBrand(product.getBrand());
            dto.setSubtitle(product.getSubtitle());
            dto.setCoverImage(coverImage(product));
            dto.setMainImage(coverImage(product));
            dto.setMinPrice(minPrice);
            dto.setSalesCount(valueOrZero(product.getSalesCount()));
            dto.setStatus(product.getStatus());
            items.add(dto);
        }
        return new PageResult<>(items, p.getTotal(), page, pageSize);
    }

    public ProductDetailDto productDetail(Long id) {
        ProductPo product = productMapper.selectOne(new QueryWrapper<ProductPo>().eq("id", id).eq("status", ProductStatus.ON_SALE.name()).last("LIMIT 1"));
        if (product == null) throw new BusinessException("PRODUCT_NOT_FOUND");
        return productDetailView(product);
    }

    public ProductDetailDto productDetailView(ProductPo product) {
        List<SkuPo> skus = skuMapper.selectList(new QueryWrapper<SkuPo>().eq("product_id", product.getId()).orderByAsc("id"));
        List<SkuDto> skuViews = new ArrayList<>();
        long minPrice = 0L;
        long maxPrice = 0L;
        for (SkuPo sku : skus) {
            boolean sellable = sku.getEnabled() != null && sku.getEnabled() == 1 && sku.getStock() != null && sku.getStock() > 0;
            if (minPrice == 0L || sku.getPrice() < minPrice) minPrice = sku.getPrice();
            if (sku.getPrice() > maxPrice) maxPrice = sku.getPrice();
            SkuDto skuDto = new SkuDto();
            skuDto.setId(sku.getId());
            skuDto.setSkuCode(sku.getSkuCode());
            skuDto.setSpecName(sku.getSpecName());
            skuDto.setSpecValue(sku.getSpecValue());
            skuDto.setSpecs(Jsons.readMap(sku.getSpecs()));
            skuDto.setImageUrl(sku.getImageUrl());
            skuDto.setPrice(sku.getPrice());
            skuDto.setMarketPrice(sku.getMarketPrice());
            skuDto.setOriginPrice(sku.getMarketPrice());
            skuDto.setCostPrice(sku.getCostPrice());
            skuDto.setStock(sku.getStock());
            skuDto.setWeightGrams(sku.getWeightGrams());
            skuDto.setBarcode(sku.getBarcode());
            skuDto.setEnabled(sku.getEnabled());
            skuDto.setSellable(sellable);
            skuViews.add(skuDto);
        }
        List<String> images = Jsons.readStringList(product.getImages());
        ReviewSummaryDto reviewSummaryDto = reviewSummary(product.getId());
        List<ProductCardDto> storeRecommendations = recommendations(product.getId(), "STORE_RECOMMEND");
        List<ProductCardDto> relatedRecommendations = recommendations(product.getId(), "RELATED_RECOMMEND");

        PriceRangeDto priceRangeDto = new PriceRangeDto();
        priceRangeDto.setMinPrice(minPrice);
        priceRangeDto.setMaxPrice(maxPrice);
        priceRangeDto.setCurrency("CNY");

        ProductDetailDto detail = new ProductDetailDto();
        detail.setId(product.getId());
        detail.setCategoryId(product.getCategoryId());
        detail.setTitle(product.getTitle());
        detail.setBrand(product.getBrand());
        detail.setSubtitle(product.getSubtitle());
        detail.setDescription(product.getDescription());
        detail.setSellingPoints(Jsons.readStringList(product.getSellingPoints()));
        detail.setUnit(product.getUnit());
        detail.setDetailHtml(product.getDetailHtml());
        detail.setAttributes(Jsons.readMap(product.getAttributes()));
        detail.setImages(images);
        detail.setImageUrls(images);
        detail.setMainImage(coverImage(product));
        detail.setMainImageUrl(coverImage(product));
        detail.setCoverImage(coverImage(product));
        detail.setSalesCount(valueOrZero(product.getSalesCount()));
        detail.setSortOrder(product.getSortOrder());
        detail.setStatus(product.getStatus());
        detail.setTags(tags(product));
        detail.setPriceRange(priceRangeDto);
        detail.setSkus(skuViews);
        detail.setPromotions(promotions(product.getId()));
        detail.setServicePromises(servicePromises(product.getId()));
        detail.setParameters(parameters(product));
        detail.setReviewSummary(reviewSummaryDto);
        detail.setReviews(reviews(product.getId()));
        detail.setStoreRecommendations(storeRecommendations);
        detail.setRelatedRecommendations(relatedRecommendations);
        return detail;
    }

    private List<String> tags(ProductPo product) {
        List<String> result = new ArrayList<>();
        if (product.getBrand() != null && product.getBrand().contains("自营")) result.add("自营");
        result.addAll(Jsons.readStringList(product.getSellingPoints()));
        return result.stream().filter(Objects::nonNull).distinct().limit(6).collect(Collectors.toList());
    }

    private List<PromotionDto> promotions(Long productId) {
        if (promotionTextMapper == null) return Collections.emptyList();
        LocalDateTime now = LocalDateTime.now();
        List<ProductPromotionTextPo> rows = promotionTextMapper.selectList(new QueryWrapper<ProductPromotionTextPo>()
                .eq("product_id", productId)
                .eq("enabled", 1)
                .orderByAsc("sort_order", "id"));
        List<PromotionDto> result = new ArrayList<>();
        for (ProductPromotionTextPo row : rows) {
            boolean started = row.getStartAt() == null || !row.getStartAt().isAfter(now);
            boolean notEnded = row.getEndAt() == null || !row.getEndAt().isBefore(now);
            if (started && notEnded) {
                PromotionDto dto = new PromotionDto();
                dto.setId(row.getId());
                dto.setTitle(row.getTitle());
                dto.setDescription(row.getDescription());
                dto.setLabel(row.getLabel());
                dto.setType("TEXT");
                result.add(dto);
            }
        }
        return result;
    }

    private List<ServicePromiseDto> servicePromises(Long productId) {
        if (servicePromiseMapper == null) return Collections.emptyList();
        List<ProductServicePromisePo> rows = servicePromiseMapper.selectList(new QueryWrapper<ProductServicePromisePo>()
                .eq("product_id", productId)
                .eq("enabled", 1)
                .orderByAsc("sort_order", "id"));
        List<ServicePromiseDto> result = new ArrayList<>();
        for (ProductServicePromisePo row : rows) {
            ServicePromiseDto dto = new ServicePromiseDto();
            dto.setId(row.getId());
            dto.setTitle(row.getTitle());
            dto.setDescription(row.getDescription());
            dto.setIcon(row.getIcon());
            result.add(dto);
        }
        return result;
    }

    private List<ParameterDto> parameters(ProductPo product) {
        List<ParameterDto> result = new ArrayList<>();
        if (parameterMapper != null) {
            List<ProductParameterPo> rows = parameterMapper.selectList(new QueryWrapper<ProductParameterPo>()
                    .eq("product_id", product.getId())
                    .eq("enabled", 1)
                    .orderByAsc("sort_order", "id"));
            for (ProductParameterPo row : rows) {
                ParameterDto dto = new ParameterDto();
                dto.setId(row.getId());
                dto.setName(row.getName());
                dto.setValue(row.getValue());
                result.add(dto);
            }
        }
        if (result.isEmpty()) {
            Jsons.readMap(product.getAttributes()).forEach((key, value) -> {
                ParameterDto dto = new ParameterDto();
                dto.setName(key);
                dto.setValue(String.valueOf(value));
                result.add(dto);
            });
        }
        return result;
    }

    private ReviewSummaryDto reviewSummary(Long productId) {
        List<ReviewDto> reviewList = reviews(productId);
        ReviewSummaryDto summary = new ReviewSummaryDto();
        if (reviewList.isEmpty()) {
            summary.setReviewCount(0);
            summary.setGoodRate(0);
            summary.setAverageRating(0.0);
            summary.setTags(Collections.emptyList());
            return summary;
        }
        int count = reviewList.size();
        int ratingSum = 0;
        int good = 0;
        Set<String> tags = new LinkedHashSet<>();
        for (ReviewDto review : reviewList) {
            Integer rating = review.getRating();
            ratingSum += rating == null ? 0 : rating;
            if (rating != null && rating >= 4) good++;
            List<String> reviewTags = review.getTags();
            if (reviewTags != null) {
                for (String tag : reviewTags) tags.add(tag);
            }
        }
        double average = Math.round((ratingSum * 10.0 / count)) / 10.0;
        summary.setReviewCount(count);
        summary.setGoodRate(good * 100 / count);
        summary.setAverageRating(average);
        summary.setTags(new ArrayList<>(tags));
        return summary;
    }

    private List<ReviewDto> reviews(Long productId) {
        if (reviewMapper == null) return Collections.emptyList();
        List<ProductReviewPo> rows = reviewMapper.selectList(new QueryWrapper<ProductReviewPo>()
                .eq("product_id", productId)
                .eq("status", "VISIBLE")
                .orderByAsc("sort_order")
                .orderByDesc("reviewed_at")
                .last("LIMIT 5"));
        List<ReviewDto> result = new ArrayList<>();
        for (ProductReviewPo row : rows) {
            ReviewDto dto = new ReviewDto();
            dto.setId(row.getId());
            dto.setSkuId(row.getSkuId());
            dto.setUserNickname(row.getUserNickname());
            dto.setRating(row.getRating());
            dto.setContent(row.getContent());
            dto.setImageUrls(Jsons.readStringList(row.getImageUrlsJson()));
            dto.setTags(Jsons.readStringList(row.getTagsJson()));
            dto.setReviewedAt(row.getReviewedAt());
            result.add(dto);
        }
        return result;
    }

    private List<ProductCardDto> recommendations(Long productId, String scene) {
        if (recommendationMapper == null || productMapper == null) return Collections.emptyList();
        List<ProductRecommendationPo> rows = recommendationMapper.selectList(new QueryWrapper<ProductRecommendationPo>()
                .eq("source_product_id", productId)
                .eq("scene", scene)
                .eq("enabled", 1)
                .orderByAsc("sort_order", "id")
                .last("LIMIT 8"));
        List<ProductCardDto> result = new ArrayList<>();
        for (ProductRecommendationPo row : rows) {
            if (Objects.equals(row.getTargetProductId(), productId)) continue;
            ProductPo target = productMapper.selectById(row.getTargetProductId());
            if (target == null || !ProductStatus.ON_SALE.name().equals(target.getStatus())) continue;
            List<SkuPo> skus = skuMapper.selectList(new QueryWrapper<SkuPo>().eq("product_id", target.getId()).eq("enabled", 1));
            long minPrice = skus.stream().mapToLong(sku -> sku.getPrice()).min().orElse(0L);
            ProductCardDto dto = new ProductCardDto();
            dto.setId(target.getId());
            dto.setTitle(target.getTitle());
            dto.setBrand(target.getBrand());
            dto.setCoverImage(coverImage(target));
            dto.setMainImage(coverImage(target));
            dto.setMinPrice(minPrice);
            dto.setSalesCount(valueOrZero(target.getSalesCount()));
            result.add(dto);
        }
        return result;
    }

    public String firstImage(String images) {
        List<String> list = Jsons.readStringList(images);
        return list.isEmpty() ? null : list.get(0);
    }

    public String coverImage(ProductPo product) {
        if (product.getMainImage() != null && !product.getMainImage().trim().isEmpty()) return product.getMainImage();
        return firstImage(product.getImages());
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * @deprecated 使用 {@link com.kkmall.common.application.Maps#of(Object...)} 替代
     */
    @Deprecated
    public static Map<String, Object> mapOf(Object... entries) {
        return com.kkmall.common.application.Maps.of(entries);
    }
}
