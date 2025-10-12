package com.xreadup.ai.articleservice.model.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装类
 */
@Data
public class PageResult<T> {
    private List<T> list;
    private Long total;
    private Long pages;
    private Long current;
    private Long size;
}