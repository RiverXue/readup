package com.xreadup.admin.dto;

import lombok.Data;
import java.util.List;

/**
 * 通用分页结果类
 * 用于返回分页数据
 */
@Data
public class PageResult<T> {
    
    /**
     * 当前页码
     */
    private int page;
    
    /**
     * 每页大小
     */
    private int pageSize;
    
    /**
     * 总记录数
     */
    private int total;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 当前页数据列表
     */
    private List<T> items;
    
    /**
     * 构造函数
     * @param page 当前页码
     * @param pageSize 每页大小
     * @param total 总记录数
     * @param items 当前页数据列表
     */
    public PageResult(int page, int pageSize, int total, List<T> items) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.items = items;
        this.totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
    }
    
    /**
     * 构造函数
     */
    public PageResult() {
    }
}