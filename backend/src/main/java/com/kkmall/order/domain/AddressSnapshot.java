package com.kkmall.order.domain;

public final class AddressSnapshot {
    private final String receiverName;
    private final String receiverPhone;
    private final String region;
    private final String detail;

    public AddressSnapshot(String receiverName, String receiverPhone, String region, String detail) {
        if (isBlank(receiverName) || isBlank(receiverPhone) || isBlank(region) || isBlank(detail)) {
            throw new IllegalArgumentException("ADDRESS_INVALID");
        }
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.region = region;
        this.detail = detail;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String receiverName() { return receiverName; }
    public String receiverPhone() { return receiverPhone; }
    public String region() { return region; }
    public String detail() { return detail; }
}
