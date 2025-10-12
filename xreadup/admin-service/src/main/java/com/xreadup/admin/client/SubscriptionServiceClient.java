package com.xreadup.admin.client;

import com.xreadup.admin.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订阅服务Feign客户端
 * 用于调用订阅服务提供的API接口
 */
@FeignClient(name = "user-service", contextId = "subscription-service")
public interface SubscriptionServiceClient {

    /**
     * 获取订阅计划列表
     * @return 订阅计划列表
     */
    @GetMapping("/api/subscription/plans")
    ApiResponse<List<Map<String, Object>>> getSubscriptionPlans();

    /**
     * 创建订阅计划
     * @param subscriptionPlanCreateDTO 创建订阅计划的数据
     * @return 创建的订阅计划
     */
    @PostMapping("/api/subscription/plans")
    ApiResponse<SubscriptionPlanDTO> createSubscriptionPlan(@RequestBody SubscriptionPlanCreateDTO subscriptionPlanCreateDTO);

    /**
     * 更新订阅计划
     * @param planId 订阅计划ID
     * @param subscriptionPlanUpdateDTO 更新订阅计划的数据
     * @return 更新结果
     */
    @PutMapping("/api/subscription/plans/{planId}")
    ApiResponse<Boolean> updateSubscriptionPlan(
            @PathVariable("planId") Long planId,
            @RequestBody SubscriptionPlanUpdateDTO subscriptionPlanUpdateDTO);

    /**
     * 删除订阅计划
     * @param planId 订阅计划ID
     * @return 删除结果
     */
    @DeleteMapping("/api/subscription/plans/{planId}")
    ApiResponse<Boolean> deleteSubscriptionPlan(@PathVariable("planId") Long planId);

    /**
     * 获取订阅列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 订阅列表
     */
    @GetMapping("/api/subscription/list")
    ApiResponse<SubscriptionPageResult> getSubscriptionList(
            @RequestParam("page") Integer page,
            @RequestParam("pageSize") Integer pageSize);

    /**
     * 获取用户订阅列表
     * @param userId 用户ID
     * @return 用户订阅列表
     */
    @GetMapping("/api/subscription/user/{userId}")
    ApiResponse<List<UserSubscriptionDTO>> getUserSubscriptions(@PathVariable("userId") Long userId);

    /**
     * 手动创建用户订阅
     * @param userSubscriptionCreateDTO 创建用户订阅的数据
     * @return 创建的用户订阅
     */
    @PostMapping("/api/subscription/user")
    ApiResponse<UserSubscriptionDTO> createUserSubscription(@RequestBody UserSubscriptionCreateDTO userSubscriptionCreateDTO);

    /**
     * 更新用户订阅状态
     * @param subscriptionId 用户订阅ID
     * @param status 订阅状态
     * @return 更新结果
     */
    @PutMapping("/api/subscription/user/{subscriptionId}/status")
    ApiResponse<Boolean> updateUserSubscriptionStatus(
            @PathVariable("subscriptionId") Long subscriptionId,
            @RequestParam String status);

    /**
     * 订阅计划DTO类定义
     */
    class SubscriptionPlanDTO {
        private Long id;
        private String planType;
        private Double price;
        private String currency;
        private Integer maxArticlesPerMonth;
        private Integer maxWordsPerArticle;
        private Boolean aiFeaturesEnabled;
        private Boolean prioritySupport;
        private String createTime;
        private String updateTime;
        private Integer deleted;

        public SubscriptionPlanDTO() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getPlanType() { return planType; }
        public void setPlanType(String planType) { this.planType = planType; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public Integer getMaxArticlesPerMonth() { return maxArticlesPerMonth; }
        public void setMaxArticlesPerMonth(Integer maxArticlesPerMonth) { this.maxArticlesPerMonth = maxArticlesPerMonth; }
        public Integer getMaxWordsPerArticle() { return maxWordsPerArticle; }
        public void setMaxWordsPerArticle(Integer maxWordsPerArticle) { this.maxWordsPerArticle = maxWordsPerArticle; }
        public Boolean getAiFeaturesEnabled() { return aiFeaturesEnabled; }
        public void setAiFeaturesEnabled(Boolean aiFeaturesEnabled) { this.aiFeaturesEnabled = aiFeaturesEnabled; }
        public Boolean getPrioritySupport() { return prioritySupport; }
        public void setPrioritySupport(Boolean prioritySupport) { this.prioritySupport = prioritySupport; }
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
        public String getUpdateTime() { return updateTime; }
        public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
        public Integer getDeleted() { return deleted; }
        public void setDeleted(Integer deleted) { this.deleted = deleted; }
    }

    /**
     * 创建订阅计划DTO
     */
    interface SubscriptionPlanCreateDTO {
        String getPlanType();
        Double getPrice();
        String getCurrency();
        Integer getMaxArticlesPerMonth();
        Integer getMaxWordsPerArticle();
        Boolean getAiFeaturesEnabled();
        Boolean getPrioritySupport();
    }

    /**
     * 更新订阅计划DTO
     */
    interface SubscriptionPlanUpdateDTO {
        String getPlanType();
        Double getPrice();
        String getCurrency();
        Integer getMaxArticlesPerMonth();
        Integer getMaxWordsPerArticle();
        Boolean getAiFeaturesEnabled();
        Boolean getPrioritySupport();
    }

    /**
     * 用户订阅DTO接口定义 - 与init.sql中的subscription表字段完全一致
     */
    interface UserSubscriptionDTO {
        Long getId();
        Long getUserId();
        String getPlanType();
        Double getPrice();
        String getCurrency();
        String getStatus();
        String getStartDate();
        String getEndDate();
        String getPaymentMethod();
        String getTransactionId();
        Boolean getAutoRenew();
        Integer getMaxArticlesPerMonth();
        Integer getMaxWordsPerArticle();
        Boolean getAiFeaturesEnabled();
        Boolean getPrioritySupport();
        String getCreatedAt();
        String getUpdatedAt();
        Integer getDeleted();
    }

    /**
     * 创建用户订阅DTO - 与init.sql中的subscription表字段完全一致
     */
    interface UserSubscriptionCreateDTO {
        Long getUserId();
        String getPlanType();
        Double getPrice();
        String getCurrency();
        String getStatus();
        String getStartDate();
        String getEndDate();
        String getPaymentMethod();
        String getTransactionId();
        Boolean getAutoRenew();
        Integer getMaxArticlesPerMonth();
        Integer getMaxWordsPerArticle();
        Boolean getAiFeaturesEnabled();
        Boolean getPrioritySupport();
    }

    /**
     * 订阅分页结果类
     */
    class SubscriptionPageResult {
        private List<UserSubscriptionDTO> list;
        private int total;
        private int page;
        private int pageSize;

        public SubscriptionPageResult() {}

        public SubscriptionPageResult(List<UserSubscriptionDTO> list, int total, int page, int pageSize) {
            this.list = list;
            this.total = total;
            this.page = page;
            this.pageSize = pageSize;
        }

        public List<UserSubscriptionDTO> getList() { return list; }
        public void setList(List<UserSubscriptionDTO> list) { this.list = list; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    }
}