package com.xreadup.ai.report.task;

import com.xreadup.ai.report.service.EbbinghausService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 艾宾浩斯复习定时任务
 * 每天凌晨2点执行，更新用户的复习计划
 */
@Component
public class EbbinghausReviewTask {

    private static final Logger logger = LoggerFactory.getLogger(EbbinghausReviewTask.class);

    @Autowired
    private EbbinghausService ebbinghausService;

    /**
     * 每日更新复习计划
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateReviewSchedule() {
        logger.info("开始执行艾宾浩斯复习计划更新任务");
        try {
            ebbinghausService.updateAllUsersReviewSchedule();
            logger.info("艾宾浩斯复习计划更新完成");
        } catch (Exception e) {
            logger.error("更新艾宾浩斯复习计划失败", e);
        }
    }

    /**
     * 每周清理过期复习记录
     * 每周日凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void cleanupExpiredReviews() {
        logger.info("开始清理过期复习记录");
        try {
            ebbinghausService.cleanupExpiredReviews();
            logger.info("过期复习记录清理完成");
        } catch (Exception e) {
            logger.error("清理过期复习记录失败", e);
        }
    }

    /**
     * 每月生成学习报告
     * 每月1日凌晨4点执行
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void generateMonthlyReport() {
        logger.info("开始生成月度学习报告");
        try {
            ebbinghausService.generateMonthlyReport();
            logger.info("月度学习报告生成完成");
        } catch (Exception e) {
            logger.error("生成月度学习报告失败", e);
        }
    }
}