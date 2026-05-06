package com.kkmall.fulfillment.interfaces;

import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.fulfillment.application.FulfillmentApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class FulfillmentController {
    private final FulfillmentApplicationService service;

    public FulfillmentController(FulfillmentApplicationService service) {
        this.service = service;
    }

    @PostMapping("/{id}/shipment")
    public ApiResponse<Map<String, Object>> ship(@PathVariable Long id, @RequestBody FulfillmentApplicationService.ShipmentRequest request) {
        return ApiResponse.ok(service.ship(id, request));
    }
}
