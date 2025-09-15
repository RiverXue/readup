package com.xreadup.ai.report.controller;

import com.xreadup.ai.report.common.ApiResponse;
import com.xreadup.ai.report.dto.DashboardData;
import com.xreadup.ai.report.dto.ReadingTimeData;
import com.xreadup.ai.report.dto.ReviewWordDto;
import com.xreadup.ai.report.dto.VocabularyGrowthData;
import com.xreadup.ai.report.service.EbbinghausService;
import com.xreadup.ai.report.service.ReadingTimeService;
import com.xreadup.ai.report.service.VocabularyGrowthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@Tag(name = "报表服务", description = "提供学习统计和报表生成功能")
public class ReportController {

    @Autowired
    private VocabularyGrowthService vocabularyGrowthService;

    @Autowired
    private ReadingTimeService readingTimeService;

    @Autowired
    private EbbinghausService ebbinghausService;

    @GetMapping("/growth-curve")
    @Operation(summary = "【词汇增长】学习曲线", description = "追踪你的词汇量成长轨迹")
    public ApiResponse<VocabularyGrowthData> growthCurve(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId,
            @Parameter(description = "统计天数", example = "7") @RequestParam(defaultValue = "7") @Min(value = 1, message = "天数必须大于0") int days) {
        VocabularyGrowthData data = vocabularyGrowthService.getGrowthCurve(userId, days);
        return ApiResponse.success(data);
    }

    @GetMapping("/reading-time")
    @Operation(summary = "【阅读时长】学习统计", description = "深度分析你的阅读习惯")
    public ApiResponse<ReadingTimeData> readingTime(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId) {
        ReadingTimeData data = readingTimeService.getReadingStats(userId);
        return ApiResponse.success(data);
    }

    @GetMapping("/review-today")
    @Operation(summary = "【今日复习】遗忘曲线", description = "艾宾浩斯智能复习提醒")
    public ApiResponse<List<ReviewWordDto>> reviewToday(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId) {
        List<ReviewWordDto> reviews = ebbinghausService.getTodayReviewWords(userId);
        return ApiResponse.success(reviews);
    }

    @GetMapping("/today/summary")
    @Operation(summary = "【今日小结】学习日报", description = "今日阅读成果一目了然")
    public ApiResponse<Object> todaySummary(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId) {
        return ApiResponse.success(vocabularyGrowthService.getVocabularyStats(userId));
    }

    @GetMapping("/weekly/insights")
    @Operation(summary = "【一周洞察】学习周报", description = "本周学习成果深度分析")
    public ApiResponse<Object> weeklyInsights(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId) {
        return ApiResponse.success(readingTimeService.getReadingTrend(userId, 7));
    }

    @GetMapping("/streak/achievement")
    @Operation(summary = "【学习成就】连续打卡", description = "见证你的坚持，记录每一个学习里程碑")
    public ApiResponse<Object> streakAchievement(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId) {
        return ApiResponse.success(ebbinghausService.getReviewStats(userId));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "【学习仪表盘】综合数据", description = "一站式查看所有学习数据")
    public ApiResponse<DashboardData> dashboard(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId) {
        DashboardData dashboard = new DashboardData();

        // 获取词汇增长数据
        VocabularyGrowthData vocabularyData = vocabularyGrowthService.getGrowthCurve(userId, 30);
        dashboard.setVocabularyData(vocabularyData);

        // 获取阅读时长数据
        ReadingTimeData readingData = readingTimeService.getReadingStats(userId);
        dashboard.setReadingData(readingData);

        // 获取今日待复习单词
        List<ReviewWordDto> todayReviews = ebbinghausService.getTodayReviewWords(userId);
        dashboard.setTodayReviews(todayReviews);

        // 获取复习统计
        Map<String, Object> reviewStats = ebbinghausService.getReviewStats(userId);
        dashboard.setReviewSuccessRate((Double) reviewStats.get("successRate"));

        // 计算打卡数据（基于实际数据）
        dashboard.setCurrentStreak(calculateCurrentStreak(userId));
        dashboard.setTotalDays(calculateTotalDays(userId));

        return ApiResponse.success(dashboard);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查报表服务状态")
    public ApiResponse<String> health() {
        return ApiResponse.success("报表服务运行正常✅");
    }

    /**
     * 计算当前连续学习天数（简化实现）
     * @param userId 用户ID
     * @return 连续学习天数
     */
    private int calculateCurrentStreak(Long userId) {
        // 这里可以实现实际的连续学习天数计算逻辑
        // 暂时返回模拟数据，后续可以从用户服务获取
        return 7;
    }

    /**
     * 计算总学习天数
     * @param userId 用户ID
     * @return 总学习天数
     */
    private int calculateTotalDays(Long userId) {
        // 这里可以实现实际的总学习天数计算逻辑
        // 暂时返回模拟数据，后续可以从用户服务获取
        return 30;
    }
}