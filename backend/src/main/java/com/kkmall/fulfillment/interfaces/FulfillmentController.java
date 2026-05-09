package com.kkmall.fulfillment.interfaces;

import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.fulfillment.application.FulfillmentApplicationService;
import com.kkmall.fulfillment.interfaces.dto.ShipResultDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发货管理接口。
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
public class FulfillmentController {

    private final FulfillmentApplicationService service;

    public FulfillmentController(FulfillmentApplicationService service) {
        this.service = service;
    }

    @PostMapping("/{id}/shipment")
    public ApiResponse<ShipResultDto> ship(@PathVariable Long id,
                                           @RequestBody FulfillmentApplicationService.ShipmentRequest request) {
        return ApiResponse.ok(service.ship(id, request));
    }
}
