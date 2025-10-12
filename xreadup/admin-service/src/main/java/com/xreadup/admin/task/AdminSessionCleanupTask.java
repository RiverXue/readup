package com.xreadup.admin.task;

import com.xreadup.admin.util.AdminTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 管理员会话定时任务
 * 负责定期清理过期的管理员token，确保系统资源的合理使用
 */
@Component
public class AdminSessionCleanupTask {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminSessionCleanupTask.class);
    
    @Autowired
    private AdminTokenManager adminTokenManager;
    
    /**
     * 定期清理过期的管理员token
     * 每小时执行一次
     * 
     * 注意：在实际生产环境中，可以根据系统负载和token数量调整执行频率
     */
    @Scheduled(cron = "0 0 * * * ?")  // 每小时的0分0秒执行
    public void cleanExpiredAdminTokens() {
        logger.info("开始清理过期的管理员token...");
        
        try {
            adminTokenManager.cleanExpiredTokens();
            logger.info("管理员token清理任务完成");
        } catch (Exception e) {
            logger.error("清理过期管理员token失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 立即执行清理任务（用于手动触发）
     */
    public void triggerCleanup() {
        cleanExpiredAdminTokens();
    }
}