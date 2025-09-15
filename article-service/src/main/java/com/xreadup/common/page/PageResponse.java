package com.xreadup.common.page;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 统一分页响应结果
 * @param <T> 数据类型
 */
@Data
public class PageResponse<T> {
    
    /**
     * 数据列表
     */
    private List<T> records;
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 当前页码
     */
    private int pageNum;
    
    /**
     * 每页显示条数
     */
    private int pageSize;
    
    /**
     * 总页数
     */
    private int pages;
    
    /**
     * 构造函数
     */
    public PageResponse(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records == null ? Collections.emptyList() : records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
    }
    
    /**
     * 空分页结果
     */
    public static <T> PageResponse<T> empty(int pageNum, int pageSize) {
        return new PageResponse<>(Collections.emptyList(), 0, pageNum, pageSize);
    }
}