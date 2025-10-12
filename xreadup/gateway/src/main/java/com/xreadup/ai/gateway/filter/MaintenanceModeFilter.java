package com.xreadup.ai.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 维护模式全局过滤器
 * 检查系统是否处于维护模式，如果是则拒绝所有请求
 * 支持缓存机制，避免频繁调用配置服务
 * 
 * @author XReadUp
 * @since 2025-10-12
 */
@Component
@Slf4j
public class MaintenanceModeFilter implements GlobalFilter, Ordered {
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Value("${admin.service.url:http://localhost:8085}")
    private String adminServiceUrl;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 缓存维护模式状态
    private final AtomicBoolean maintenanceMode = new AtomicBoolean(false);
    private final AtomicLong lastCheckTime = new AtomicLong(0);
    private static final long CACHE_DURATION_MS = 30000; // 30秒缓存
    
    public MaintenanceModeFilter() {
        log.info("维护模式过滤器已创建");
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.debug("维护模式过滤器被调用: {}", exchange.getRequest().getURI());
        
        // 检查是否为管理员请求，如果是则直接放行
        String requestPath = exchange.getRequest().getURI().getPath();
        if (requestPath.startsWith("/api/admin/")) {
            log.debug("管理员请求，直接放行: {}", requestPath);
            return chain.filter(exchange);
        }
        
        // 检查是否需要刷新缓存
        if (shouldRefreshCache()) {
            log.debug("需要刷新维护模式缓存");
            return checkMaintenanceMode()
                    .flatMap(isMaintenance -> {
                        maintenanceMode.set(isMaintenance);
                        lastCheckTime.set(System.currentTimeMillis());
                        
                        log.debug("维护模式状态: {}", isMaintenance);
                        
                        if (isMaintenance) {
                            log.warn("系统处于维护模式，拒绝请求: {}", exchange.getRequest().getURI());
                            return handleMaintenanceMode(exchange);
                        }
                        return chain.filter(exchange);
                    })
                    .onErrorResume(error -> {
                        log.error("检查维护模式失败，使用缓存状态", error);
                        // 出错时使用缓存状态
                        if (maintenanceMode.get()) {
                            return handleMaintenanceMode(exchange);
                        }
                        return chain.filter(exchange);
                    });
        } else {
            // 使用缓存状态
            log.debug("使用缓存的维护模式状态: {}", maintenanceMode.get());
            if (maintenanceMode.get()) {
                log.debug("使用缓存的维护模式状态，拒绝请求: {}", exchange.getRequest().getURI());
                return handleMaintenanceMode(exchange);
            }
            return chain.filter(exchange);
        }
    }
    
    /**
     * 检查是否需要刷新缓存
     */
    private boolean shouldRefreshCache() {
        long currentTime = System.currentTimeMillis();
        long lastCheck = lastCheckTime.get();
        return (currentTime - lastCheck) > CACHE_DURATION_MS;
    }
    
    /**
     * 检查维护模式状态
     */
    private Mono<Boolean> checkMaintenanceMode() {
        return webClientBuilder.build()
                .get()
                .uri(adminServiceUrl + "/api/admin/system-config/internal/maintenance/status")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    // 处理API响应格式: {code: 200, data: {maintenanceMode: true}, success: true}
                    Map<String, Object> data = (Map<String, Object>) response.get("data");
                    if (data != null) {
                        Boolean maintenanceMode = (Boolean) data.get("maintenanceMode");
                        return maintenanceMode != null ? maintenanceMode : false;
                    }
                    return false;
                })
                .timeout(Duration.ofSeconds(5)) // 5秒超时
                .onErrorReturn(false); // 出错时默认非维护模式
    }
    
    /**
     * 处理维护模式响应
     */
    private Mono<Void> handleMaintenanceMode(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        exchange.getResponse().getHeaders().add("Retry-After", "60"); // 建议60秒后重试
        
        String responseBody = "{\"success\":false,\"message\":\"系统正在维护中，请稍后再试\",\"code\":503}";
        
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(responseBody.getBytes()))
        );
    }
    
    @Override
    public int getOrder() {
        return -1000; // 最高优先级
    }
}
