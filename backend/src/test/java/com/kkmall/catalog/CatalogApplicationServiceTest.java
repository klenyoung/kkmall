package com.kkmall.catalog;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.kkmall.catalog.application.CatalogApplicationService;
import com.kkmall.catalog.infrastructure.ProductPo;
import com.kkmall.catalog.infrastructure.SkuMapper;
import com.kkmall.catalog.infrastructure.SkuPo;
import com.kkmall.catalog.interfaces.dto.ProductDetailDto;
import com.kkmall.catalog.interfaces.dto.SkuDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogApplicationServiceTest {

    @SuppressWarnings("unchecked")
    @Test
    void productDetailViewIncludesTmallLiteDecisionData() {
        SkuMapper skuMapper = mock(SkuMapper.class);

        SkuPo enabledSku = new SkuPo();
        enabledSku.setId(11L);
        enabledSku.setProductId(7L);
        enabledSku.setSkuCode("SKU-7-1");
        enabledSku.setSpecName("颜色");
        enabledSku.setSpecValue("红色");
        enabledSku.setSpecs("{\"颜色\":\"红色\"}");
        enabledSku.setPrice(9900L);
        enabledSku.setMarketPrice(12900L);
        enabledSku.setStock(8);
        enabledSku.setEnabled(1);

        SkuPo disabledSku = new SkuPo();
        disabledSku.setId(12L);
        disabledSku.setProductId(7L);
        disabledSku.setSkuCode("SKU-7-2");
        disabledSku.setSpecName("颜色");
        disabledSku.setSpecValue("灰色");
        disabledSku.setSpecs("{\"颜色\":\"灰色\"}");
        disabledSku.setPrice(10900L);
        disabledSku.setMarketPrice(13900L);
        disabledSku.setStock(0);
        disabledSku.setEnabled(0);

        when(skuMapper.selectList(any(Wrapper.class))).thenReturn(List.of(enabledSku, disabledSku));

        CatalogApplicationService service = new CatalogApplicationService(null, null, skuMapper);

        ProductPo product = new ProductPo();
        product.setId(7L);
        product.setCategoryId(2L);
        product.setTitle("轻量防泼水通勤双肩包");
        product.setBrand("KKMall 自营");
        product.setSubtitle("通勤出行轻巧收纳");
        product.setDescription("商品描述");
        product.setSellingPoints("[\"满99包邮\",\"7天无理由\"]");
        product.setUnit("件");
        product.setDetailHtml("<p>详情</p>");
        product.setAttributes("{\"材质\":\"尼龙\"}");
        product.setImages("[\"/uploads/bag.png\"]");
        product.setMainImage("/uploads/bag.png");
        product.setSalesCount(128);
        product.setStatus("ON_SALE");

        ProductDetailDto view = service.productDetailView(product);

        assertThat(view.getPriceRange()).isNotNull();
        assertThat(view.getPriceRange().getMinPrice()).isEqualTo(9900L);
        assertThat(view.getPriceRange().getMaxPrice()).isEqualTo(10900L);
        assertThat(view.getPromotions()).isNotNull();
        assertThat(view.getServicePromises()).isNotNull();
        assertThat(view.getParameters()).isNotNull();
        assertThat(view.getReviewSummary()).isNotNull();
        assertThat(view.getReviews()).isNotNull();
        assertThat(view.getStoreRecommendations()).isNotNull();
        assertThat(view.getRelatedRecommendations()).isNotNull();

        List<SkuDto> skus = view.getSkus();
        assertThat(skus.get(0).getSellable()).isTrue();
        assertThat(skus.get(0).getOriginPrice()).isEqualTo(12900L);
        assertThat(skus.get(1).getSellable()).isFalse();
    }
}
