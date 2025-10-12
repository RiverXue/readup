package com.xreadup.admin.client;

import com.xreadup.admin.dto.ApiResponse;
import com.xreadup.admin.dto.UserLoginRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * User-Service OpenFeign客户端
 * 用于调用用户服务的API
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    /**
     * 用户登录
     * @param loginRequest 登录请求对象
     * @return 登录响应
     */
    @PostMapping("/api/user/login")
    ApiResponse<Map<String, Object>> login(@RequestBody UserLoginRequest loginRequest);
    
    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param userUpdateDTO 用户更新信息
     * @return 更新结果响应
     */
    @PutMapping("/api/user/update/{userId}")
    ApiResponse<Boolean> updateUser(@PathVariable("userId") Long userId, @RequestBody UserUpdateDTO userUpdateDTO);
    
    /**
     * 禁用用户
     * @param userId 用户ID
     * @return 禁用结果响应
     */
    @PutMapping("/api/user/delete/{userId}")
    ApiResponse<Boolean> disableUser(@PathVariable("userId") Long userId);
    
    /**
     * 启用用户
     * @param userId 用户ID
     * @return 启用结果响应
     */
    @PutMapping("/api/user/enable/{userId}")
    ApiResponse<Boolean> enableUser(@PathVariable("userId") Long userId);
    
    /**
     * 获取用户订阅列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param userId 用户ID（可选筛选）
     * @param planType 计划类型（可选筛选）
     * @param status 状态（可选筛选）
     * @return 用户订阅列表响应
     */
    @GetMapping("/api/user/subscription/list")
    ApiResponse<Map<String, Object>> getUserSubscriptionList(
            @RequestParam("page") Integer page,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status);
    
    /**
     * 获取用户详情
     * @param userId 用户ID
     * @return 用户详情响应
     */
    @GetMapping("/api/user/detail/{userId}")
    ApiResponse<UserDTO> getUserDetail(@PathVariable("userId") Long userId);

    /**
     * 获取用户统计信息
     * @return 用户统计信息响应
     */
    @GetMapping("/api/user/stats")
    ApiResponse<Map<String, Object>> getUserStats();

    /**
     * 获取用户学习进度统计
     * @param userId 用户ID
     * @return 用户学习进度统计响应
     */
    @GetMapping("/api/user/progress/{userId}")
    ApiResponse<Map<String, Object>> getUserLearningProgress(@PathVariable("userId") Long userId);
    
    /**
     * 获取用户列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param username 用户名（可选）
     * @param phone 电话（可选）
     * @param interestTag 兴趣标签（可选）
     * @param identityTag 身份标签（可选）
     * @param status 用户状态（可选）
     * @return 用户列表响应
     */
    @GetMapping("/api/user/list")
    ApiResponse<UserListDTO> getUserList(@RequestParam("page") Integer page,
                                        @RequestParam("pageSize") Integer pageSize,
                                        @RequestParam(value = "username", required = false) String username,
                                        @RequestParam(value = "phone", required = false) String phone,
                                        @RequestParam(value = "interestTag", required = false) String interestTag,
                                        @RequestParam(value = "identityTag", required = false) String identityTag,
                                        @RequestParam(value = "status", required = false) String status);
    
    /**
     * 内部类：UserListDTO
     * 用户列表数据传输对象
     */
    class UserListDTO {
        private List<UserListItemDTO> records;
        private long total;
        private long pages;
        private long current;
        private long size;
        
        // getters and setters
        public List<UserListItemDTO> getRecords() { return records; }
        public void setRecords(List<UserListItemDTO> records) { this.records = records; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getPages() { return pages; }
        public void setPages(long pages) { this.pages = pages; }
        public long getCurrent() { return current; }
        public void setCurrent(long current) { this.current = current; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
    }
    
    /**
     * 内部类：UserListItemDTO
     * 用户列表项数据传输对象 - 与init.sql中的user表字段完全一致
     */
    class UserListItemDTO {
        private Long id;
        private String username;
        private String phone;
        private String interestTag;
        private String identityTag;
        private Integer learningGoalWords;
        private Integer targetReadingTime;
        private String status;
        private String createdAt;
        
        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getInterestTag() { return interestTag; }
        public void setInterestTag(String interestTag) { this.interestTag = interestTag; }
        public String getIdentityTag() { return identityTag; }
        public void setIdentityTag(String identityTag) { this.identityTag = identityTag; }
        public Integer getLearningGoalWords() { return learningGoalWords; }
        public void setLearningGoalWords(Integer learningGoalWords) { this.learningGoalWords = learningGoalWords; }
        public Integer getTargetReadingTime() { return targetReadingTime; }
        public void setTargetReadingTime(Integer targetReadingTime) { this.targetReadingTime = targetReadingTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    /**
     * 内部类：UserDetailDTO
     * 用户详情数据传输对象 - 与init.sql中的user表字段完全一致
     */
    class UserDetailDTO {
        private Long id;
        private String username;
        private String phone;
        private String interestTag;
        private String identityTag;
        private Integer learningGoalWords;
        private Integer targetReadingTime;
        private String status;
        private String createdAt;
        
        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getInterestTag() { return interestTag; }
        public void setInterestTag(String interestTag) { this.interestTag = interestTag; }
        public String getIdentityTag() { return identityTag; }
        public void setIdentityTag(String identityTag) { this.identityTag = identityTag; }
        public Integer getLearningGoalWords() { return learningGoalWords; }
        public void setLearningGoalWords(Integer learningGoalWords) { this.learningGoalWords = learningGoalWords; }
        public Integer getTargetReadingTime() { return targetReadingTime; }
        public void setTargetReadingTime(Integer targetReadingTime) { this.targetReadingTime = targetReadingTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
    
    /**
     * 内部类：UserUpdateDTO
     * 用户更新数据传输对象
     */
    class UserUpdateDTO {
        private String phone;
        private String interestTag;
        private String identityTag;
        private Integer learningGoalWords;
        private Integer targetReadingTime;
        
        // getters and setters
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getInterestTag() { return interestTag; }
        public void setInterestTag(String interestTag) { this.interestTag = interestTag; }
        public String getIdentityTag() { return identityTag; }
        public void setIdentityTag(String identityTag) { this.identityTag = identityTag; }
        public Integer getLearningGoalWords() { return learningGoalWords; }
        public void setLearningGoalWords(Integer learningGoalWords) { this.learningGoalWords = learningGoalWords; }
        public Integer getTargetReadingTime() { return targetReadingTime; }
        public void setTargetReadingTime(Integer targetReadingTime) { this.targetReadingTime = targetReadingTime; }
    }
    
    /**
     * 内部类：UserDTO
     * 用户数据传输对象
     */
    class UserDTO {
        private Long id;
        private String username;
        private String phone;
        private String interestTag;
        private String identityTag;
        private Integer learningGoalWords;
        private Integer targetReadingTime;
        private String createdAt;
        
        public UserDTO() {}
        
        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getInterestTag() { return interestTag; }
        public void setInterestTag(String interestTag) { this.interestTag = interestTag; }
        public String getIdentityTag() { return identityTag; }
        public void setIdentityTag(String identityTag) { this.identityTag = identityTag; }
        public Integer getLearningGoalWords() { return learningGoalWords; }
        public void setLearningGoalWords(Integer learningGoalWords) { this.learningGoalWords = learningGoalWords; }
        public Integer getTargetReadingTime() { return targetReadingTime; }
        public void setTargetReadingTime(Integer targetReadingTime) { this.targetReadingTime = targetReadingTime; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
    
    /**
     * 内部类：UserSubscriptionDTO
     * 用户订阅数据传输对象
     */
    class UserSubscriptionDTO {
        private Long id;
        private Long userId;
        private String planType;
        private Double price;
        private String status;
        private String startDate;
        private String endDate;
        
        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getPlanType() { return planType; }
        public void setPlanType(String planType) { this.planType = planType; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }
}