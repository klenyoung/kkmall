package com.kkmall.catalog.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kkmall.catalog.infrastructure.*;
import com.kkmall.catalog.interfaces.dto.AdminProductCardDto;
import com.kkmall.catalog.interfaces.dto.DetailConfigDto;
import com.kkmall.catalog.interfaces.dto.IdResultDto;
import com.kkmall.catalog.interfaces.dto.ProductDetailDto;
import com.kkmall.catalog.interfaces.dto.StatusResultDto;
import com.kkmall.common.application.Jsons;
import com.kkmall.common.exception.BusinessException;
import lombok.Data;
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

    public List<AdminProductCardDto> products(String status) {
        QueryWrapper<ProductPo> wrapper = new QueryWrapper<ProductPo>().orderByDesc("id");
        if (status != null && !status.trim().isEmpty()) wrapper.eq("status", status);
        List<AdminProductCardDto> result = new ArrayList<>();
        for (ProductPo product : productMapper.selectList(wrapper)) {
            List<SkuPo> skus = skuMapper.selectList(new QueryWrapper<SkuPo>().eq("product_id", product.getId()));
            long minPrice = skus.stream().mapToLong(sku -> sku.getPrice()).min().orElse(0L);
            int totalStock = skus.stream().mapToInt(sku -> sku.getStock() == null ? 0 : sku.getStock()).sum();
            AdminProductCardDto dto = new AdminProductCardDto();
            dto.setId(product.getId());
            dto.setTitle(product.getTitle());
            dto.setBrand(product.getBrand());
            dto.setSubtitle(product.getSubtitle());
            dto.setCategoryId(product.getCategoryId());
            dto.setStatus(product.getStatus());
            dto.setImages(Jsons.readStringList(product.getImages()));
            dto.setMainImage(catalogApplicationService.coverImage(product));
            dto.setCoverImage(catalogApplicationService.coverImage(product));
            dto.setMinPrice(minPrice);
            dto.setTotalStock(totalStock);
            dto.setSalesCount(product.getSalesCount() == null ? 0 : product.getSalesCount());
            dto.setSortOrder(product.getSortOrder() == null ? 0 : product.getSortOrder());
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public IdResultDto saveProduct(Long id, ProductRequest request) {
        ProductPo product = id == null ? new ProductPo() : productMapper.selectById(id);
        if (product == null) product = new ProductPo();
        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getTitle());
        product.setBrand(request.getBrand());
        product.setSubtitle(request.getSubtitle());
        product.setDescription(request.getDescription());
        product.setSellingPoints(Jsons.write(request.getSellingPoints() == null ? Collections.emptyList() : request.getSellingPoints()));
        product.setUnit(request.getUnit() == null || request.getUnit().trim().isEmpty() ? "件" : request.getUnit());
        product.setDetailHtml(request.getDetailHtml());
        product.setAttributes(Jsons.write(request.getAttributes() == null ? Collections.emptyMap() : request.getAttributes()));
        product.setImages(Jsons.write(request.getImages() == null ? Collections.emptyList() : request.getImages()));
        product.setMainImage(request.getMainImage() == null || request.getMainImage().trim().isEmpty()
                ? (request.getImages() == null || request.getImages().isEmpty() ? null : request.getImages().get(0))
                : request.getMainImage());
        product.setSalesCount(request.getSalesCount() == null ? 0 : request.getSalesCount());
        product.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        product.setStatus(request.getStatus() == null ? "DRAFT" : request.getStatus());
        if (product.getId() == null) productMapper.insert(product); else productMapper.updateById(product);

        if (id != null) {
            for (SkuPo sku : skuMapper.selectList(new QueryWrapper<SkuPo>().eq("product_id", product.getId()))) {
                skuMapper.deleteById(sku.getId());
            }
        }
        List<SkuRequest> skuRequests = request.getSkus() == null || request.getSkus().isEmpty() ? Collections.singletonList(new SkuRequest()) : request.getSkus();
        for (SkuRequest skuRequest : skuRequests) {
            SkuPo sku = new SkuPo();
            sku.setProductId(product.getId());
            sku.setSkuCode(blank(skuRequest.getSkuCode()) ? "SKU-" + product.getId() + "-" + UUID.randomUUID().toString().substring(0, 8) : skuRequest.getSkuCode());
            sku.setSpecName(blank(skuRequest.getSpecName()) ? "规格" : skuRequest.getSpecName());
            sku.setSpecValue(blank(skuRequest.getSpecValue()) ? "默认" : skuRequest.getSpecValue());
            sku.setSpecs(Jsons.write(skuRequest.getSpecs() == null ? Collections.singletonMap(sku.getSpecName(), sku.getSpecValue()) : skuRequest.getSpecs()));
            sku.setImageUrl(skuRequest.getImageUrl());
            sku.setPrice(skuRequest.getPrice() == null ? 0L : skuRequest.getPrice());
            sku.setMarketPrice(skuRequest.getMarketPrice() == null ? sku.getPrice() : skuRequest.getMarketPrice());
            sku.setCostPrice(skuRequest.getCostPrice() == null ? sku.getPrice() : skuRequest.getCostPrice());
            sku.setStock(skuRequest.getStock() == null ? 0 : skuRequest.getStock());
            sku.setWeightGrams(skuRequest.getWeightGrams());
            sku.setBarcode(skuRequest.getBarcode());
            sku.setEnabled(skuRequest.getEnabled() == null || skuRequest.getEnabled() ? 1 : 0);
            skuMapper.insert(sku);
        }
        return IdResultDto.of(product.getId());
    }

    public StatusResultDto updateStatus(Long id, String status) {
        ProductPo product = productMapper.selectById(id);
        if (product == null) throw new BusinessException("PRODUCT_NOT_FOUND");
        product.setStatus(status);
        productMapper.updateById(product);
        return StatusResultDto.of(id, status);
    }

    public ProductDetailDto productDetail(Long id) {
        ProductPo product = productMapper.selectById(id);
        if (product == null) throw new BusinessException("PRODUCT_NOT_FOUND");
        return catalogApplicationService.productDetailView(product);
    }

    public DetailConfigDto detailConfig(Long productId) {
        ProductPo product = productMapper.selectById(productId);
        if (product == null) throw new BusinessException("PRODUCT_NOT_FOUND");
        DetailConfigDto dto = new DetailConfigDto();
        dto.setProduct(catalogApplicationService.productDetailView(product));
        dto.setParameters(parameterRows(productId));
        dto.setServicePromises(servicePromiseRows(productId));
        dto.setPromotions(promotionRows(productId));
        dto.setReviews(reviewRows(productId));
        dto.setStoreRecommendationIds(recommendationIds(productId, "STORE_RECOMMEND"));
        dto.setRelatedRecommendationIds(recommendationIds(productId, "RELATED_RECOMMEND"));
        return dto;
    }

    @Transactional
    public IdResultDto saveDetailConfig(Long productId, DetailConfigRequest request) {
        ProductPo product = productMapper.selectById(productId);
        if (product == null) throw new BusinessException("PRODUCT_NOT_FOUND");
        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getSubtitle() != null) product.setSubtitle(request.getSubtitle());
        if (request.getSellingPoints() != null) product.setSellingPoints(Jsons.write(request.getSellingPoints()));
        if (request.getDetailHtml() != null) product.setDetailHtml(request.getDetailHtml());
        if (request.getAttributes() != null) product.setAttributes(Jsons.write(request.getAttributes()));
        if (request.getSalesCount() != null) product.setSalesCount(request.getSalesCount());
        productMapper.updateById(product);

        if (request.getSkus() != null) updateSkuDetail(productId, request.getSkus());
        replaceParameters(productId, request.getParameters());
        replaceServicePromises(productId, request.getServicePromises());
        replacePromotions(productId, request.getPromotions());
        replaceRecommendations(productId, "STORE_RECOMMEND", request.getStoreRecommendationIds());
        replaceRecommendations(productId, "RELATED_RECOMMEND", request.getRelatedRecommendationIds());
        return IdResultDto.of(productId);
    }

    @Transactional
    public IdResultDto saveReview(Long productId, Long reviewId, ReviewRequest request) {
        ProductPo product = productMapper.selectById(productId);
        if (product == null) throw new BusinessException("PRODUCT_NOT_FOUND");
        ProductReviewPo row = reviewId == null ? new ProductReviewPo() : reviewMapper.selectById(reviewId);
        if (row == null) row = new ProductReviewPo();
        row.setProductId(productId);
        row.setSkuId(request.getSkuId());
        row.setUserNickname(blank(request.getUserNickname()) ? "匿名买家" : request.getUserNickname());
        row.setRating(request.getRating() == null ? 5 : Math.max(1, Math.min(5, request.getRating())));
        row.setContent(request.getContent() == null ? "" : request.getContent());
        row.setImageUrlsJson(Jsons.write(request.getImageUrls() == null ? Collections.emptyList() : request.getImageUrls()));
        row.setTagsJson(Jsons.write(request.getTags() == null ? Collections.emptyList() : request.getTags()));
        row.setReviewedAt(request.getReviewedAt() == null ? LocalDateTime.now() : request.getReviewedAt());
        row.setStatus(request.getStatus() == null ? "VISIBLE" : request.getStatus());
        row.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        if (row.getId() == null) reviewMapper.insert(row); else reviewMapper.updateById(row);
        return IdResultDto.of(row.getId());
    }

    public IdResultDto deleteReview(Long productId, Long reviewId) {
        ProductReviewPo row = reviewMapper.selectById(reviewId);
        if (row == null || !Objects.equals(row.getProductId(), productId)) throw new BusinessException("REVIEW_NOT_FOUND");
        reviewMapper.deleteById(reviewId);
        return IdResultDto.of(reviewId);
    }

    private void updateSkuDetail(Long productId, List<SkuRequest> rows) {
        for (SkuRequest request : rows) {
            if (request.getId() == null) continue;
            SkuPo sku = skuMapper.selectById(request.getId());
            if (sku == null || !Objects.equals(sku.getProductId(), productId)) continue;
            if (request.getImageUrl() != null) sku.setImageUrl(request.getImageUrl());
            if (request.getMarketPrice() != null) sku.setMarketPrice(request.getMarketPrice());
            if (request.getEnabled() != null) sku.setEnabled(request.getEnabled() ? 1 : 0);
            skuMapper.updateById(sku);
        }
    }

    private List<DetailConfigDto.AdminParameterDto> parameterRows(Long productId) {
        List<DetailConfigDto.AdminParameterDto> result = new ArrayList<>();
        for (ProductParameterPo row : parameterMapper.selectList(new QueryWrapper<ProductParameterPo>().eq("product_id", productId).orderByAsc("sort_order", "id"))) {
            DetailConfigDto.AdminParameterDto dto = new DetailConfigDto.AdminParameterDto();
            dto.setId(row.getId());
            dto.setName(row.getName());
            dto.setValue(row.getValue());
            dto.setSortOrder(row.getSortOrder());
            dto.setEnabled(row.getEnabled());
            result.add(dto);
        }
        return result;
    }

    private List<DetailConfigDto.AdminServicePromiseDto> servicePromiseRows(Long productId) {
        List<DetailConfigDto.AdminServicePromiseDto> result = new ArrayList<>();
        for (ProductServicePromisePo row : servicePromiseMapper.selectList(new QueryWrapper<ProductServicePromisePo>().eq("product_id", productId).orderByAsc("sort_order", "id"))) {
            DetailConfigDto.AdminServicePromiseDto dto = new DetailConfigDto.AdminServicePromiseDto();
            dto.setId(row.getId());
            dto.setTitle(row.getTitle());
            dto.setDescription(row.getDescription());
            dto.setIcon(row.getIcon());
            dto.setSortOrder(row.getSortOrder());
            dto.setEnabled(row.getEnabled());
            result.add(dto);
        }
        return result;
    }

    private List<DetailConfigDto.AdminPromotionDto> promotionRows(Long productId) {
        List<DetailConfigDto.AdminPromotionDto> result = new ArrayList<>();
        for (ProductPromotionTextPo row : promotionTextMapper.selectList(new QueryWrapper<ProductPromotionTextPo>().eq("product_id", productId).orderByAsc("sort_order", "id"))) {
            DetailConfigDto.AdminPromotionDto dto = new DetailConfigDto.AdminPromotionDto();
            dto.setId(row.getId());
            dto.setTitle(row.getTitle());
            dto.setDescription(row.getDescription());
            dto.setLabel(row.getLabel());
            dto.setStartAt(row.getStartAt());
            dto.setEndAt(row.getEndAt());
            dto.setSortOrder(row.getSortOrder());
            dto.setEnabled(row.getEnabled());
            result.add(dto);
        }
        return result;
    }

    private List<DetailConfigDto.AdminReviewDto> reviewRows(Long productId) {
        List<DetailConfigDto.AdminReviewDto> result = new ArrayList<>();
        for (ProductReviewPo row : reviewMapper.selectList(new QueryWrapper<ProductReviewPo>().eq("product_id", productId).orderByAsc("sort_order", "id"))) {
            DetailConfigDto.AdminReviewDto dto = new DetailConfigDto.AdminReviewDto();
            dto.setId(row.getId());
            dto.setSkuId(row.getSkuId());
            dto.setUserNickname(row.getUserNickname());
            dto.setRating(row.getRating());
            dto.setContent(row.getContent());
            dto.setImageUrls(Jsons.readStringList(row.getImageUrlsJson()));
            dto.setTags(Jsons.readStringList(row.getTagsJson()));
            dto.setReviewedAt(row.getReviewedAt());
            dto.setStatus(row.getStatus());
            dto.setSortOrder(row.getSortOrder());
            result.add(dto);
        }
        return result;
    }

    private List<Long> recommendationIds(Long productId, String scene) {
        List<Long> result = new ArrayList<>();
        for (ProductRecommendationPo row : recommendationMapper.selectList(new QueryWrapper<ProductRecommendationPo>().eq("source_product_id", productId).eq("scene", scene).eq("enabled", 1).orderByAsc("sort_order", "id"))) {
            result.add(row.getTargetProductId());
        }
        return result;
    }

    private void replaceParameters(Long productId, List<ParameterRequest> rows) {
        if (rows == null) return;
        parameterMapper.delete(new QueryWrapper<ProductParameterPo>().eq("product_id", productId));
        int index = 0;
        for (ParameterRequest request : rows) {
            if (blank(request.getName())) continue;
            ProductParameterPo row = new ProductParameterPo();
            row.setProductId(productId);
            row.setName(request.getName());
            row.setValue(request.getValue() == null ? "" : request.getValue());
            row.setSortOrder(request.getSortOrder() == null ? index * 10 : request.getSortOrder());
            row.setEnabled(request.getEnabled() == null || request.getEnabled() ? 1 : 0);
            parameterMapper.insert(row);
            index++;
        }
    }

    private void replaceServicePromises(Long productId, List<ServicePromiseRequest> rows) {
        if (rows == null) return;
        servicePromiseMapper.delete(new QueryWrapper<ProductServicePromisePo>().eq("product_id", productId));
        int index = 0;
        for (ServicePromiseRequest request : rows) {
            if (blank(request.getTitle())) continue;
            ProductServicePromisePo row = new ProductServicePromisePo();
            row.setProductId(productId);
            row.setTitle(request.getTitle());
            row.setDescription(request.getDescription());
            row.setIcon(request.getIcon());
            row.setSortOrder(request.getSortOrder() == null ? index * 10 : request.getSortOrder());
            row.setEnabled(request.getEnabled() == null || request.getEnabled() ? 1 : 0);
            servicePromiseMapper.insert(row);
            index++;
        }
    }

    private void replacePromotions(Long productId, List<PromotionRequest> rows) {
        if (rows == null) return;
        promotionTextMapper.delete(new QueryWrapper<ProductPromotionTextPo>().eq("product_id", productId));
        int index = 0;
        for (PromotionRequest request : rows) {
            if (blank(request.getTitle())) continue;
            ProductPromotionTextPo row = new ProductPromotionTextPo();
            row.setProductId(productId);
            row.setTitle(request.getTitle());
            row.setDescription(request.getDescription());
            row.setLabel(request.getLabel());
            row.setStartAt(request.getStartAt());
            row.setEndAt(request.getEndAt());
            row.setSortOrder(request.getSortOrder() == null ? index * 10 : request.getSortOrder());
            row.setEnabled(request.getEnabled() == null || request.getEnabled() ? 1 : 0);
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
            row.setSourceProductId(productId);
            row.setTargetProductId(targetId);
            row.setScene(scene);
            row.setSortOrder(index * 10);
            row.setEnabled(1);
            recommendationMapper.insert(row);
            index++;
        }
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Data
    public static class ProductRequest {
        private Long categoryId;
        private String title;
        private String brand;
        private String subtitle;
        private String description;
        private List<String> sellingPoints;
        private String unit;
        private String detailHtml;
        private Map<String, Object> attributes;
        private List<String> images;
        private String mainImage;
        private Integer salesCount;
        private Integer sortOrder;
        private String status;
        private List<SkuRequest> skus = new ArrayList<>();
    }

    @Data
    public static class SkuRequest {
        private Long id;
        private String skuCode;
        private String specName;
        private String specValue;
        private Map<String, Object> specs;
        private String imageUrl;
        private Long price;
        private Long marketPrice;
        private Long costPrice;
        private Integer stock;
        private Integer weightGrams;
        private String barcode;
        private Boolean enabled;
    }

    @Data
    public static class DetailConfigRequest {
        private String brand;
        private String subtitle;
        private List<String> sellingPoints;
        private String detailHtml;
        private Map<String, Object> attributes;
        private Integer salesCount;
        private List<SkuRequest> skus;
        private List<ParameterRequest> parameters;
        private List<ServicePromiseRequest> servicePromises;
        private List<PromotionRequest> promotions;
        private List<Long> storeRecommendationIds;
        private List<Long> relatedRecommendationIds;
    }

    @Data
    public static class ParameterRequest {
        private String name;
        private String value;
        private Integer sortOrder;
        private Boolean enabled;
    }

    @Data
    public static class ServicePromiseRequest {
        private String title;
        private String description;
        private String icon;
        private Integer sortOrder;
        private Boolean enabled;
    }

    @Data
    public static class PromotionRequest {
        private String title;
        private String description;
        private String label;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private Integer sortOrder;
        private Boolean enabled;
    }

    @Data
    public static class ReviewRequest {
        private Long skuId;
        private String userNickname;
        private Integer rating;
        private String content;
        private List<String> imageUrls;
        private List<String> tags;
        private LocalDateTime reviewedAt;
        private String status;
        private Integer sortOrder;
    }
}
