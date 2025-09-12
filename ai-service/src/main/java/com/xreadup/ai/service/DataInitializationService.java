package com.xreadup.ai.service;

import com.xreadup.ai.mapper.AiAnalysisMapper;
import com.xreadup.ai.model.entity.AiAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * 数据初始化服务
 * <p>
 * 负责系统启动时的数据检查和初始化工作
 * 确保数据库表结构和初始数据正确
 * </p>
 * 
 * @author XReadUp Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private AiAnalysisMapper aiAnalysisMapper;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始数据初始化检查...");
        
        try {
            // 检查数据库连接
            checkDatabaseConnection();
            
            // 检查表结构
            checkTableStructure();
            
            // 统计现有数据
            countExistingData();
            
            log.info("数据初始化检查完成");
            
        } catch (Exception e) {
            log.error("数据初始化检查失败", e);
            throw e;
        }
    }

    /**
     * 检查数据库连接
     */
    private void checkDatabaseConnection() {
        try {
            // 简单的查询测试连接
            aiAnalysisMapper.existsByArticleId(1L);
            log.info("数据库连接正常");
        } catch (Exception e) {
            log.error("数据库连接异常", e);
            throw new RuntimeException("数据库连接失败", e);
        }
    }

    /**
     * 检查表结构
     */
    private void checkTableStructure() {
        try {
            // 检查表是否存在
            aiAnalysisMapper.selectByArticleId(99999L); // 不存在的ID，测试表结构
            log.info("数据库表结构正常");
        } catch (Exception e) {
            log.error("数据库表结构异常", e);
            throw new RuntimeException("数据库表结构检查失败", e);
        }
    }

    /**
     * 统计现有数据
     */
    private void countExistingData() {
        try {
            // 这里可以添加数据统计逻辑
            log.info("数据库表结构检查完成");
        } catch (Exception e) {
            log.warn("数据统计异常，但继续启动", e);
        }
    }
}