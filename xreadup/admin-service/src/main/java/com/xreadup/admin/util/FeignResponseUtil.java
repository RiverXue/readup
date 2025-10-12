package com.xreadup.admin.util;

import com.xreadup.admin.dto.ApiResponse;
import org.slf4j.Logger;

/**
 * Feign响应处理工具类
 * 统一处理FeignClient调用的响应判断逻辑
 */
public class FeignResponseUtil {
    
    /**
     * 判断Feign响应是否成功
     * @param response Feign响应对象
     * @return 是否成功
     */
    public static boolean isSuccess(ApiResponse<?> response) {
        return response != null && (response.isSuccess() || response.getCode() == 200);
    }
    
    /**
     * 判断Feign响应是否成功，并记录日志
     * @param response Feign响应对象
     * @param logger 日志记录器
     * @param operation 操作名称
     * @return 是否成功
     */
    public static boolean isSuccess(ApiResponse<?> response, Logger logger, String operation) {
        if (response == null) {
            logger.warn("{}失败: 响应为空", operation);
            return false;
        }
        
        if (!isSuccess(response)) {
            logger.warn("{}失败: code={}, message={}", operation, response.getCode(), response.getMessage());
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取响应数据，如果失败则返回默认值
     * @param response Feign响应对象
     * @param defaultValue 默认值
     * @param <T> 数据类型
     * @return 响应数据或默认值
     */
    public static <T> T getDataOrDefault(ApiResponse<T> response, T defaultValue) {
        return isSuccess(response) ? response.getData() : defaultValue;
    }
    
    /**
     * 获取响应数据，如果失败则返回默认值，并记录日志
     * @param response Feign响应对象
     * @param defaultValue 默认值
     * @param logger 日志记录器
     * @param operation 操作名称
     * @param <T> 数据类型
     * @return 响应数据或默认值
     */
    public static <T> T getDataOrDefault(ApiResponse<T> response, T defaultValue, Logger logger, String operation) {
        if (isSuccess(response, logger, operation)) {
            return response.getData();
        }
        return defaultValue;
    }
}
