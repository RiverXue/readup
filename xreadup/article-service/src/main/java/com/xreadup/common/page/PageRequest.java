package com.xreadup.common.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * 统一分页请求参数
 */
@Data
public class PageRequest {
    
    /**
     * 当前页码，从1开始
     */
    @Min(value = 1, message = "页码必须大于0")
    private int pageNum = 1;
    
    /**
     * 每页显示条数
     */
    @Min(value = 1, message = "每页条数必须大于0")
    @Max(value = 100, message = "每页条数不能超过100")
    private int pageSize = 10;
    
    /**
     * 排序字段
     */
    private String sortField;
    
    /**
     * 排序方式：ASC/DESC
     */
    private String sortOrder = "ASC";
    
    /**
     * 获取偏移量（用于数据库查询）
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
    
    /**
     * 获取排序SQL片段
     */
    public String getOrderBy() {
        if (!StringUtils.hasText(sortField)) {
            return "";
        }
        
        String order = "ASC".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
        return sortField + " " + order;
    }
}