package com.xreadup.ai.articleservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xreadup.ai.articleservice.mapper.ContentFilterLogMapper;
import com.xreadup.ai.articleservice.model.entity.ContentFilterLog;
import com.xreadup.ai.articleservice.model.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容过滤日志管理服务
 * 负责敏感词拦截记录的增删改查
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ContentFilterLogService {
    
    private final ContentFilterLogMapper contentFilterLogMapper;
    
    /**
     * 记录内容过滤日志
     * @param articleId 文章ID
     * @param filterType 过滤类型
     * @param matchedContent 匹配到的内容
     * @param filterReason 过滤原因
     * @param severityLevel 严重程度
     * @param actionTaken 采取的行动
     * @param adminId 管理员ID
     * @return 是否成功
     */
    public boolean logContentFilter(Long articleId, String filterType, String matchedContent, 
                                   String filterReason, String severityLevel, String actionTaken, Long adminId) {
        try {
            ContentFilterLog record = new ContentFilterLog();
            record.setArticleId(articleId);
            record.setFilterType(filterType);
            record.setMatchedContent(matchedContent);
            record.setFilterReason(filterReason);
            record.setSeverityLevel(severityLevel);
            record.setActionTaken(actionTaken);
            record.setAdminId(adminId);
            record.setStatus("active");
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            
            int rows = contentFilterLogMapper.insert(record);
            if (rows > 0) {
                ContentFilterLog saved = record;
                printSavedLog(saved, filterType, actionTaken);
                return true;
            }
            log.warn("内容过滤日志插入返回0行，articleId={}, filterType={}, actionTaken={}", articleId, filterType, actionTaken);
            return false;
        } catch (Exception e) {
            log.error("记录内容过滤日志失败: articleId={}, filterType={}, actionTaken={}, err={}", 
                    articleId, filterType, actionTaken, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 统一的自动流程日志封装（用于本服务内自动任务）
     * 约定：
     * - matched_content: 内容片段（统一截断长度）
     * - filter_reason: 结构化原因，如 "命中高风险词: bomb | system_auto" 或 "内容已通过检测 | system_auto"
     * - severity_level: high/medium/low
     * - action_taken: blocked/allowed
     */
    public boolean logAutoContentFilter(Long articleId, String contentOrTitle,
                                        String filterReason, String severityLevel, String actionTaken) {
        final int snippetMaxLen = 160; // 统一截断长度
        String base = contentOrTitle == null ? "" : contentOrTitle.trim();
        String snippet = base.length() > snippetMaxLen ? base.substring(0, snippetMaxLen) : base;
        return logContentFilter(
                articleId,
                "sensitive_word",
                snippet,
                filterReason,
                severityLevel,
                actionTaken,
                null
        );
    }

    private void printSavedLog(ContentFilterLog saved, String filterType, String actionTaken) {
        try {
            log.info("✅ 内容过滤日志已保存: id={}, articleId={}, type={}, action={}",
                    saved.getId(), saved.getArticleId(), filterType, actionTaken);
        } catch (Exception ignore) {
            // 打印日志失败不影响主流程
        }
    }
    
    /**
     * 获取文章的内容过滤记录
     * @param articleId 文章ID
     * @return 过滤记录列表
     */
    public List<ContentFilterLog> getArticleFilterLogs(Long articleId) {
        LambdaQueryWrapper<ContentFilterLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentFilterLog::getArticleId, articleId)
                   .orderByDesc(ContentFilterLog::getCreatedAt);
        
        return contentFilterLogMapper.selectList(queryWrapper);
    }
    
    /**
     * 获取内容过滤记录分页列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param filterType 过滤类型
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    public IPage<ContentFilterLog> getFilterLogsPage(int page, int pageSize, String filterType, 
                                                    String status, LocalDateTime startDate, LocalDateTime endDate) {
        Page<ContentFilterLog> pageParam = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<ContentFilterLog> queryWrapper = new LambdaQueryWrapper<>();
        if (filterType != null && !filterType.isEmpty()) {
            queryWrapper.eq(ContentFilterLog::getFilterType, filterType);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(ContentFilterLog::getStatus, status);
        }
        if (startDate != null) {
            queryWrapper.ge(ContentFilterLog::getCreatedAt, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(ContentFilterLog::getCreatedAt, endDate);
        }
        
        queryWrapper.orderByDesc(ContentFilterLog::getCreatedAt);
        
        return contentFilterLogMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 更新过滤记录状态
     * @param logId 记录ID
     * @param status 新状态
     * @param adminId 管理员ID
     * @return 是否成功
     */
    public boolean updateFilterLogStatus(Long logId, String status, Long adminId) {
        try {
            ContentFilterLog log = contentFilterLogMapper.selectById(logId);
            if (log != null) {
                log.setStatus(status);
                log.setAdminId(adminId);
                log.setUpdatedAt(LocalDateTime.now());
                return contentFilterLogMapper.updateById(log) > 0;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 删除过滤记录
     * @param logId 记录ID
     * @return 是否成功
     */
    public boolean deleteFilterLog(Long logId) {
        try {
            return contentFilterLogMapper.deleteById(logId) > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取过滤统计信息
     * @return 统计信息
     */
    public ApiResponse<Object> getFilterStatistics() {
        try {
            // 分组计数：按filter_type
            QueryWrapper<ContentFilterLog> typeQuery = new QueryWrapper<>();
            typeQuery.select("filter_type", "COUNT(*) AS cnt").groupBy("filter_type");
            List<java.util.Map<String, Object>> typeCounts = contentFilterLogMapper.selectMaps(typeQuery);

            // 分组计数：按status
            QueryWrapper<ContentFilterLog> statusQuery = new QueryWrapper<>();
            statusQuery.select("status", "COUNT(*) AS cnt").groupBy("status");
            List<java.util.Map<String, Object>> statusCounts = contentFilterLogMapper.selectMaps(statusQuery);

            // 今日新增记录数量
            LambdaQueryWrapper<ContentFilterLog> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.ge(ContentFilterLog::getCreatedAt, LocalDateTime.now().toLocalDate().atStartOfDay());
            long todayCountValue = contentFilterLogMapper.selectCount(todayWrapper);

            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("typeCounts", typeCounts);
            statistics.put("statusCounts", statusCounts);
            statistics.put("todayCount", todayCountValue);

            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取统计信息失败: " + e.getMessage());
        }
    }
}
