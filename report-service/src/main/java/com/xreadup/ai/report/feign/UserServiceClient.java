package com.xreadup.ai.report.feign;

import com.xreadup.ai.report.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务Feign客户端，用于调用user-service的API
 */
@FeignClient(name = "user-service", path = "/api/user")
public interface UserServiceClient {
    
    /**
     * 调用用户服务的每日打卡接口，获取连续打卡天数
     * @param userId 用户ID
     * @return 包含连续打卡天数的响应对象
     */
    @PostMapping("/progress/check-in")
    ApiResponse<Integer> getCheckInStreak(@RequestParam("userId") Long userId);
}