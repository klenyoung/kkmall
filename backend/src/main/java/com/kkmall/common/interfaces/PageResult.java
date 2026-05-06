package com.kkmall.common.interfaces;

import java.util.List;

public final class PageResult<T> {
    public final List<T> items;
    public final long total;
    public final int page;
    public final int pageSize;

    public PageResult(List<T> items, long total, int page, int pageSize) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }
}
