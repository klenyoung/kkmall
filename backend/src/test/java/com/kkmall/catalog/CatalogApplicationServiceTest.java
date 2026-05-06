package com.kkmall.catalog;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.kkmall.catalog.application.CatalogApplicationService;
import com.kkmall.catalog.infrastructure.ProductPo;
import com.kkmall.catalog.infrastructure.SkuMapper;
import com.kkmall.catalog.infrastructure.SkuPo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogApplicationServiceTest {
    @Test
    void productDetailViewIncludesTmallLiteDecisionData() {
        SkuMapper skuMapper = mock(SkuMapper.class);
        SkuPo enabledSku = new SkuPo();
        enabledSku.id = 11L;
        enabledSku.productId = 7L;
        enabledSku.skuCode = "SKU-7-1";
        enabledSku.specName = "颜色";
        enabledSku.specValue = "红色";
        enabledSku.specs = "{\"颜色\":\"红色\"}";
        enabledSku.price = 9900L;
        enabledSku.marketPrice = 12900L;
        enabledSku.stock = 8;
        enabledSku.enabled = 1;

        SkuPo disabledSku = new SkuPo();
        disabledSku.id = 12L;
        disabledSku.productId = 7L;
        disabledSku.skuCode = "SKU-7-2";
        disabledSku.specName = "颜色";
        disabledSku.specValue = "灰色";
        disabledSku.specs = "{\"颜色\":\"灰色\"}";
        disabledSku.price = 10900L;
        disabledSku.marketPrice = 13900L;
        disabledSku.stock = 0;
        disabledSku.enabled = 0;

        when(skuMapper.selectList(any(Wrapper.class))).thenReturn(List.of(enabledSku, disabledSku));

        CatalogApplicationService service = new CatalogApplicationService(null, null, skuMapper);
        ProductPo product = new ProductPo();
        product.id = 7L;
        product.categoryId = 2L;
        product.title = "轻量防泼水通勤双肩包";
        product.brand = "KKMall 自营";
        product.subtitle = "通勤出行轻巧收纳";
        product.description = "商品描述";
        product.sellingPoints = "[\"满99包邮\",\"7天无理由\"]";
        product.unit = "件";
        product.detailHtml = "<p>详情</p>";
        product.attributes = "{\"材质\":\"尼龙\"}";
        product.images = "[\"/uploads/bag.png\"]";
        product.mainImage = "/uploads/bag.png";
        product.salesCount = 128;
        product.status = "ON_SALE";

        Map<String, Object> view = service.productDetailView(product);

        assertThat(view).containsKeys(
                "priceRange",
                "promotions",
                "servicePromises",
                "parameters",
                "reviewSummary",
                "reviews",
                "storeRecommendations",
                "relatedRecommendations"
        );
        assertThat((Map<String, Object>) view.get("priceRange")).containsEntry("minPrice", 9900L).containsEntry("maxPrice", 10900L);
        List<Map<String, Object>> skus = (List<Map<String, Object>>) view.get("skus");
        assertThat(skus.get(0)).containsEntry("sellable", true).containsEntry("originPrice", 12900L);
        assertThat(skus.get(1)).containsEntry("sellable", false);
    }
}
