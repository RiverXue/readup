package com.xreadup.admin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 管理员Token管理器
 * 负责生成、存储、验证和管理管理员登录token
 * 
 * 注意：这是一个简单的内存存储实现，实际生产环境应考虑使用Redis等持久化存储方案
 */
@Component
public class AdminTokenManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminTokenManager.class);
    
    // 存储管理员token和过期时间的映射
    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();
    
    // 用于防止暴力破解的计数器
    private final Map<String, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();
    
    // 用于存储锁定的账户信息
    private final Map<String, LockedAccount> lockedAccounts = new ConcurrentHashMap<>();
    
    // 锁定时间（分钟）
    private static final int LOCK_TIME_MINUTES = 15;
    
    // 最大失败尝试次数
    private static final int MAX_FAILED_ATTEMPTS = 5;
    
    // 用于生成token的随机数生成器
    private final Random random = new Random();
    
    // 盐值，用于增强token安全性
    private final String tokenSalt = generateRandomSalt();
    
    /**
     * Token信息内部类
     */
    private static class TokenInfo {
        private final Long userId;
        private final String username;
        private final String role;
        private final boolean superAdmin;
        private final LocalDateTime expireTime;
        private final String ipAddress; // 记录生成token时的IP地址
        private final String userAgent; // 记录生成token时的用户代理
        
        public TokenInfo(Long userId, String username, String role, boolean superAdmin, LocalDateTime expireTime, 
                        String ipAddress, String userAgent) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.superAdmin = superAdmin;
            this.expireTime = expireTime;
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
        }
        
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public boolean isSuperAdmin() { return superAdmin; }
        public LocalDateTime getExpireTime() { return expireTime; }
        public String getIpAddress() { return ipAddress; }
        public String getUserAgent() { return userAgent; }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expireTime);
        }
    }
    
    /**
     * 锁定账户信息类
     */
    private static class LockedAccount {
        private final LocalDateTime lockTime;
        
        public LockedAccount() {
            this.lockTime = LocalDateTime.now();
        }
        
        public boolean isLocked() {
            return LocalDateTime.now().isBefore(lockTime.plusMinutes(LOCK_TIME_MINUTES));
        }
        
        public LocalDateTime getLockTime() {
            return lockTime;
        }
    }
    
    /**
     * 生成安全的管理员token
     * 使用SHA-256算法结合随机数和时间戳生成token
     * @param userId 用户ID
     * @param username 用户名
     * @return 生成的安全token
     */
    public String generateSecureToken(Long userId, String username) {
        return generateSecureToken(userId, username, null, null);
    }
    
    /**
     * 生成安全的管理员token（增强版本，包含环境信息）
     * @param userId 用户ID
     * @param username 用户名
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     * @return 生成的安全token
     */
    public String generateSecureToken(String userIdStr, String clientIp, String userAgent) {
        try {
            Long userId = Long.parseLong(userIdStr);
            return generateSecureToken(userId, "admin_" + userId, clientIp, userAgent);
        } catch (NumberFormatException e) {
            logger.error("生成安全token失败: 无效的用户ID格式", e);
            // 降级方案：使用UUID
            return java.util.UUID.randomUUID().toString().replace("-", "");
        }
    }
    
    /**
     * 生成安全的管理员token（内部实现方法）
     * @param userId 用户ID
     * @param username 用户名
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     * @return 生成的安全token
     */
    private String generateSecureToken(Long userId, String username, String clientIp, String userAgent) {
        try {
            // 生成随机数
            long randomValue = random.nextLong();
            
            // 获取当前时间戳
            long timestamp = System.currentTimeMillis();
            
            // 构建token原材料
            String rawToken = userId + ":" + username + ":" + timestamp + ":" + randomValue + ":" + tokenSalt;
            
            // 使用SHA-256算法加密
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            
            // 使用Base64编码，移除可能的特殊字符
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error("生成安全token失败", e);
            // 降级方案：使用UUID
            return java.util.UUID.randomUUID().toString().replace("-", "");
        }
    }
    
    /**
     * 生成随机盐值
     * @return 随机盐值
     */
    private String generateRandomSalt() {
        return java.util.UUID.randomUUID().toString();
    }
    
    /**
     * 存储管理员token信息
     * @param token 管理员token
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色
     * @param superAdmin 是否超级管理员
     * @param expireTime 过期时间
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     */
    public void storeToken(String token, Long userId, String username, String role, boolean superAdmin, 
                          LocalDateTime expireTime, String ipAddress, String userAgent) {
        if (token == null || token.isEmpty()) {
            logger.warn("尝试存储空token");
            return;
        }
        
        tokenStore.put(token, new TokenInfo(userId, username, role, superAdmin, expireTime, ipAddress, userAgent));
        logger.debug("存储管理员token成功: userId={}, username={}, ip={}", userId, username, ipAddress);
    }
    
    /**
     * 简化版存储token方法，使用默认IP和userAgent
     */
    public void storeToken(String token, Long userId, String username, String role, boolean superAdmin, LocalDateTime expireTime) {
        storeToken(token, userId, username, role, superAdmin, expireTime, "unknown", "unknown");
    }
    
    /**
     * 验证管理员token是否有效
     * @param token 管理员token
     * @return token是否有效
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo == null) {
            logger.warn("token不存在");
            return false;
        }
        
        // 检查token是否过期
        if (tokenInfo.isExpired()) {
            // 清理过期token
            tokenStore.remove(token);
            logger.warn("token已过期: userId={}", tokenInfo.getUserId());
            return false;
        }
        
        logger.debug("token验证成功: userId={}", tokenInfo.getUserId());
        return true;
    }
    
    /**
     * 带IP和User-Agent检查的token验证
     * 增强安全性，防止token被盗用
     * @param token 管理员token
     * @param ipAddress 请求IP地址
     * @param userAgent 请求User-Agent
     * @return token是否有效且匹配当前环境
     */
    public boolean validateTokenWithEnvironment(String token, String ipAddress, String userAgent) {
        boolean isValid = validateToken(token);
        if (!isValid) {
            return false;
        }
        
        TokenInfo tokenInfo = tokenStore.get(token);
        
        // 检查IP地址是否匹配（开发环境可能使用动态IP，此处可配置是否严格检查）
        boolean ipMatch = "unknown".equals(tokenInfo.getIpAddress()) || tokenInfo.getIpAddress().equals(ipAddress);
        
        // 检查User-Agent是否匹配
        boolean userAgentMatch = "unknown".equals(tokenInfo.getUserAgent()) || 
                                userAgent != null && tokenInfo.getUserAgent().contains(getBrowserName(userAgent));
        
        if (!ipMatch || !userAgentMatch) {
            logger.warn("管理员登录环境发生变化，可能存在安全风险: userId={}, ip={}, userAgent={}", 
                        tokenInfo.getUserId(), ipAddress, userAgent);
            // 注意：在生产环境中，这里可能需要记录日志或触发警报，但不一定要拒绝访问
        }
        
        return true;
    }
    
    /**
     * 从User-Agent中提取浏览器名称
     * @param userAgent User-Agent字符串
     * @return 浏览器名称
     */
    private String getBrowserName(String userAgent) {
        if (userAgent.contains("Chrome")) {
            return "Chrome";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox";
        } else if (userAgent.contains("Safari")) {
            return "Safari";
        } else if (userAgent.contains("Edge")) {
            return "Edge";
        } else {
            return "Unknown";
        }
    }
    
    /**
     * 记录失败的登录尝试
     * @param username 用户名
     * @return 是否被锁定
     */
    public boolean recordFailedLoginAttempt(String username) {
        return recordFailedLoginAttempt(username, "unknown");
    }
    
    /**
     * 记录失败的登录尝试（带IP信息）
     * @param username 用户名
     * @param clientIp 客户端IP
     * @return 是否被锁定
     */
    public boolean recordFailedLoginAttempt(String username, String clientIp) {
        AtomicInteger attempts = failedAttempts.computeIfAbsent(username, k -> new AtomicInteger(0));
        int currentAttempts = attempts.incrementAndGet();
        
        logger.warn("管理员登录失败尝试: username={}, attempts={}", username, currentAttempts);
        
        if (currentAttempts >= MAX_FAILED_ATTEMPTS) {
            logger.error("管理员账户已被锁定: username={}, IP={}", username, clientIp);
            lockedAccounts.put(username, new LockedAccount());
            failedAttempts.remove(username); // 锁定后重置计数
            return true;
        }
        
        return false;
    }
    
    /**
     * 重置失败的登录尝试
     * @param username 用户名
     */
    public void resetFailedLoginAttempts(String username) {
        failedAttempts.remove(username);
        logger.info("管理员登录失败尝试已重置: username={}", username);
    }
    
    /**
     * 检查账户是否被锁定
     * @param username 用户名
     * @return 是否已锁定
     */
    public boolean isAccountLocked(String username) {
        LockedAccount lockedAccount = lockedAccounts.get(username);
        if (lockedAccount == null) {
            return false;
        }
        
        // 检查锁定是否已过期
        if (!lockedAccount.isLocked()) {
            lockedAccounts.remove(username);
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取账户锁定剩余时间（秒）
     * @param username 用户名
     * @return 剩余锁定时间（秒）
     */
    public long getLockedTimeRemaining(String username) {
        LockedAccount lockedAccount = lockedAccounts.get(username);
        if (lockedAccount == null) {
            return 0;
        }
        
        LocalDateTime unlockTime = lockedAccount.getLockTime().plusMinutes(LOCK_TIME_MINUTES);
        long secondsRemaining = java.time.Duration.between(LocalDateTime.now(), unlockTime).getSeconds();
        
        return Math.max(0, secondsRemaining);
    }
    
    /**
     * 存储带环境信息的token
     * 兼容旧代码的方法别名
     */
    public void storeTokenWithEnvironment(String token, Long userId, String username, String role, boolean superAdmin, 
                                        LocalDateTime expireTime, String ipAddress, String userAgent) {
        storeToken(token, userId, username, role, superAdmin, expireTime, ipAddress, userAgent);
    }
    
    /**
     * 根据token获取管理员用户ID
     * @param token 管理员token
     * @return 用户ID，如果token无效返回null
     */
    public Long getUserIdByToken(String token) {
        if (!validateToken(token)) {
            return null;
        }
        
        TokenInfo tokenInfo = tokenStore.get(token);
        return tokenInfo != null ? tokenInfo.getUserId() : null;
    }
    
    /**
     * 根据token获取管理员角色
     * @param token 管理员token
     * @return 角色，如果token无效返回null
     */
    public String getRoleByToken(String token) {
        if (!validateToken(token)) {
            return null;
        }
        
        TokenInfo tokenInfo = tokenStore.get(token);
        return tokenInfo != null ? tokenInfo.getRole() : null;
    }
    
    /**
     * 根据token检查是否为超级管理员
     * @param token 管理员token
     * @return 是否为超级管理员
     */
    public boolean isSuperAdminByToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        
        TokenInfo tokenInfo = tokenStore.get(token);
        return tokenInfo != null && tokenInfo.isSuperAdmin();
    }
    
    /**
     * 移除管理员token
     * @param token 管理员token
     */
    public void removeToken(String token) {
        if (token != null) {
            TokenInfo tokenInfo = tokenStore.remove(token);
            if (tokenInfo != null) {
                logger.debug("移除管理员token成功: userId={}", tokenInfo.getUserId());
            }
        }
    }
    
    /**
     * 清理过期的token
     * 可以定期调用此方法清理过期token
     */
    public void cleanExpiredTokens() {
        long beforeCount = tokenStore.size();
        
        tokenStore.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired();
            if (expired) {
                logger.debug("清理过期管理员token: userId={}", entry.getValue().getUserId());
            }
            return expired;
        });
        
        long afterCount = tokenStore.size();
        if (beforeCount > afterCount) {
            logger.info("清理过期管理员token完成，共清理: {}", beforeCount - afterCount);
        }
    }
}