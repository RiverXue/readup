package com.xreadup.ai.report.controller;

import com.xreadup.ai.report.common.ApiResponse;
import com.xreadup.ai.report.dto.DashboardData;
import com.xreadup.ai.report.dto.ReadingTimeData;
import com.xreadup.ai.report.dto.ReviewWordDto;
import com.xreadup.ai.report.dto.VocabularyGrowthData;
import com.xreadup.ai.report.feign.UserServiceClient;
import com.xreadup.ai.report.service.ReadingTimeService;
import com.xreadup.ai.report.service.VocabularyGrowthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 阅读记录请求参数
 */
class ReadingRecordRequest {
    private Long userId;
    private Long articleId;
    private Integer readTimeSec;
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getArticleId() {
        return articleId;
    }
    
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    
    public Integer getReadTimeSec() {
        return readTimeSec;
    }
    
    public void setReadTimeSec(Integer readTimeSec) {
        this.readTimeSec = readTimeSec;
    }
}

@RestController
@RequestMapping("/api/report")
@Tag(name = "报表服务", description = "提供学习统计和报表生成功能")
public class ReportController {

    @Autowired
    private VocabularyGrowthService vocabularyGrowthService;

    @Autowired
    private ReadingTimeService readingTimeService;

    @Autowired
    private UserServiceClient userServiceClient;

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
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId,
            @Parameter(description = "统计天数", example = "7") @RequestParam(defaultValue = "7") @Min(value = 1, message = "天数必须大于0") int days) {
        ReadingTimeData data = readingTimeService.getReadingStats(userId, days);
        return ApiResponse.success(data);
    }
    
    @PostMapping("/reading-record")
    @Operation(summary = "【记录阅读】保存阅读时长", description = "记录用户的文章阅读时长")
    public ApiResponse<String> recordReading(
            @RequestBody @NotNull(message = "请求参数不能为空") ReadingRecordRequest request) {
        readingTimeService.recordReading(
            request.getUserId(), 
            request.getArticleId(), 
            request.getReadTimeSec());
        return ApiResponse.success("阅读记录保存成功");
    }



    @GetMapping("/today/summary")
    @Operation(summary = "【今日小结】学习日报", description = "今日阅读成果一目了然")
    public ApiResponse<Object> todaySummary(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId) {
        Map<String, Object> todayData = new HashMap<>();
        
        // 获取词汇统计
        Map<String, Object> vocabularyStats = vocabularyGrowthService.getVocabularyStats(userId);
        todayData.putAll(vocabularyStats);
        
        // 添加今日新增词汇字段
        todayData.put("dailyNewWords", vocabularyStats.get("dailyNewWords"));
        
        // 获取今日阅读数据
        ReadingTimeData readingData = readingTimeService.getReadingStats(userId, 7);
        todayData.put("todayMinutes", readingData.getTodayMinutes());
        todayData.put("todayArticles", readingData.getTodayArticles());
        
        return ApiResponse.success(todayData);
    }

    @GetMapping("/weekly/insights")
    @Operation(summary = "【一周洞察】学习周报", description = "本周学习成果深度分析")
    public ApiResponse<Object> weeklyInsights(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId) {
        Map<String, Object> weeklyData = new HashMap<>();
        
        // 获取本周阅读趋势数据
        Map<String, Object> currentWeekTrend = readingTimeService.getReadingTrend(userId, 7);
        weeklyData.putAll(currentWeekTrend);
        
        // 获取上周阅读趋势数据用于对比
        Map<String, Object> previousWeekTrend = readingTimeService.getReadingTrend(userId, 14);
        List<ReadingTimeData.DailyReading> previousWeekReadings = previousWeekTrend.get("dailyReadings") != null ? 
            (List<ReadingTimeData.DailyReading>) previousWeekTrend.get("dailyReadings") : new ArrayList<>();
        
        // 计算上周数据（第8-14天）
        int previousWeekMinutes = 0;
        int previousWeekArticles = 0;
        if (previousWeekReadings.size() > 7) {
            for (int i = 7; i < previousWeekReadings.size(); i++) {
                ReadingTimeData.DailyReading reading = previousWeekReadings.get(i);
                previousWeekMinutes += reading.getMinutes() != null ? reading.getMinutes() : 0;
                previousWeekArticles += reading.getArticles() != null ? reading.getArticles() : 0;
            }
        }
        
        // 计算本周数据
        List<ReadingTimeData.DailyReading> currentWeekReadings = currentWeekTrend.get("dailyReadings") != null ? 
            (List<ReadingTimeData.DailyReading>) currentWeekTrend.get("dailyReadings") : new ArrayList<>();
        int currentWeekMinutes = 0;
        int currentWeekArticles = 0;
        for (ReadingTimeData.DailyReading reading : currentWeekReadings) {
            currentWeekMinutes += reading.getMinutes() != null ? reading.getMinutes() : 0;
            currentWeekArticles += reading.getArticles() != null ? reading.getArticles() : 0;
        }
        
        // 获取词汇增长数据用于计算变化量
        Map<String, Object> vocabularyStats = vocabularyGrowthService.getVocabularyStats(userId);
        int currentWeekWords = (Integer) vocabularyStats.get("weeklyNewWords");
        
        // 简化处理：假设上周词汇数据为本周的一半（实际项目中应该查询真实数据）
        int previousWeekWords = Math.max(0, currentWeekWords / 2);
        
        // 计算变化量
        int minutesChange = currentWeekMinutes - previousWeekMinutes;
        int articlesChange = currentWeekArticles - previousWeekArticles;
        int wordsChange = currentWeekWords - previousWeekWords;
        
        // 添加变化量数据
        weeklyData.put("minutesChange", minutesChange);
        weeklyData.put("articlesChange", articlesChange);
        weeklyData.put("wordsChange", wordsChange);
        
        // 添加词汇增长数据（使用已获取的数据）
        weeklyData.put("weeklyNewWords", vocabularyStats.get("weeklyNewWords"));
        weeklyData.put("totalWords", vocabularyStats.get("totalWords"));
        
        // 从阅读趋势数据中获取总文章数，避免重复调用
        ReadingTimeData readingData = readingTimeService.getReadingStats(userId, 7);
        weeklyData.put("totalArticles", readingData.getTotalArticles());
        
        return ApiResponse.success(weeklyData);
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
        ReadingTimeData readingData = readingTimeService.getReadingStats(userId, 30);
        dashboard.setReadingData(readingData);

        // 计算打卡数据（基于实际数据）
        dashboard.setCurrentStreak(calculateCurrentStreak(userId));
        dashboard.setTotalDays(calculateTotalDays(userId));

        return ApiResponse.success(dashboard);
    }
    
    @GetMapping("/review-today")
    @Operation(summary = "【待复习单词】今日复习列表", description = "获取用户今日需要复习的单词列表")
    public ApiResponse<List<ReviewWordDto>> getTodayReviewWords(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId) {
        try {
            List<ReviewWordDto> reviewWords = vocabularyGrowthService.getTodayReviewWords(userId);
            
            // 为每个单词计算复习进度和格式化复习时间
            for (ReviewWordDto word : reviewWords) {
                word.setProgress(vocabularyGrowthService.calculateReviewProgress(word.getNextReviewTime()));
                word.setFormattedReviewTime(vocabularyGrowthService.formatNextReviewTime(word.getNextReviewTime()));
            }
            
            return ApiResponse.success(reviewWords);
        } catch (Exception e) {
            return ApiResponse.error("获取待复习单词失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/review/{wordId}")
    @Operation(summary = "【记录复习】单词复习结果", description = "记录单词的复习结果")
    public ApiResponse<String> recordReviewResult(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId,
            @Parameter(description = "单词ID", required = true) @PathVariable("wordId") Long wordId,
            @Parameter(description = "是否记住", required = true) @RequestParam("remembered") boolean isRemembered) {
        
        try {
            boolean success = vocabularyGrowthService.recordReviewResult(userId, wordId, isRemembered);
            
            if (success) {
                return ApiResponse.success("复习结果记录成功");
            } else {
                return ApiResponse.error("复习结果记录失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("服务器错误：" + e.getMessage());
        }
    }
    
    @PostMapping("/no-longer-review/{wordId}")
    @Operation(summary = "【不再巩固】单词设置", description = "设置单词为不再需要巩固")
    public ApiResponse<String> setWordAsNoLongerReview(
            @Parameter(description = "单词ID", required = true) @PathVariable("wordId") Long wordId) {
        
        try {
            boolean success = vocabularyGrowthService.setWordAsNoLongerReview(wordId);
            
            if (success) {
                return ApiResponse.success("单词已设置为不再巩固");
            } else {
                return ApiResponse.error("设置失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("服务器错误：" + e.getMessage());
        }
    }
    
    @GetMapping("/review-progress")
    @Operation(summary = "【复习进度】计算进度", description = "计算单词的复习进度百分比")
    public ApiResponse<Integer> calculateReviewProgress(
            @Parameter(description = "下次复习时间", required = true) @RequestParam("nextReviewTime") String nextReviewTime) {
        
        try {
            int progress = vocabularyGrowthService.calculateReviewProgress(nextReviewTime);
            
            return ApiResponse.success(progress);
        } catch (Exception e) {
            return ApiResponse.error("计算失败：" + e.getMessage());
        }
    }


    @GetMapping("/historical-data")
    @Operation(summary = "【历史数据】获取历史学习数据", description = "获取指定时间段的历史学习数据用于对比分析")
    public ApiResponse<Map<String, Object>> getHistoricalData(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId,
            @Parameter(description = "开始日期", required = true) @RequestParam @NotNull(message = "开始日期不能为空") String startDate,
            @Parameter(description = "结束日期", required = true) @RequestParam @NotNull(message = "结束日期不能为空") String endDate) {
        
        try {
            Map<String, Object> historicalData = new HashMap<>();
            
            // 解析日期
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            // 计算天数
            int days = (int) ChronoUnit.DAYS.between(start, end) + 1;
            
            // 使用新的日期范围查询方法获取历史数据
            List<ReadingTimeData.DailyReading> dailyReadings = readingTimeService.getDailyReadingsByDateRange(userId, start, end);
            
            // 为每日阅读数据添加词汇信息
            if (dailyReadings != null) {
                for (ReadingTimeData.DailyReading reading : dailyReadings) {
                    // 添加模拟词汇数据（实际项目中应该从词汇表获取）
                    reading.setNewWords(Math.max(1, (int)(Math.random() * 5) + 1));
                }
            }
            
            // 计算总阅读时长和平均阅读时长
            int totalMinutes = dailyReadings != null ? dailyReadings.stream()
                    .mapToInt(ReadingTimeData.DailyReading::getMinutes)
                    .sum() : 0;
            
            double averagePerDay = days > 0 ? (double) totalMinutes / days : 0.0;
            
            historicalData.put("dailyReadings", dailyReadings != null ? dailyReadings : new ArrayList<>());
            historicalData.put("totalMinutes", totalMinutes);
            historicalData.put("averagePerDay", Math.round(averagePerDay * 100) / 100.0);
            
            // 获取词汇增长数据
            Map<String, Object> vocabularyStats = vocabularyGrowthService.getVocabularyStats(userId);
            historicalData.put("vocabularyData", vocabularyStats);
            
            return ApiResponse.success(historicalData);
        } catch (Exception e) {
            return ApiResponse.error("获取历史数据失败：" + e.getMessage());
        }
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查报表服务状态")
    public ApiResponse<String> health() {
        return ApiResponse.success("报表服务运行正常✅");
    }

    /**
     * 计算当前连续学习天数
     * @param userId 用户ID
     * @return 连续学习天数
     */
    private int calculateCurrentStreak(Long userId) {
        try {
            // 调用用户服务的打卡API获取真实的连续打卡天数
            ApiResponse<Integer> response = userServiceClient.getCheckInStreak(userId);
            
            // 检查响应是否成功且包含有效的数据
            if (response != null && response.getCode() == 200 && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            // 发生异常时记录日志，返回默认值
            System.err.println("获取连续打卡天数失败: " + e.getMessage());
        }
        
        // 失败时返回默认值
        return 7;
    }

    /**
     * 计算总学习天数
     * @param userId 用户ID
     * @return 总学习天数
     */
    private int calculateTotalDays(Long userId) {
        try {
            // 调用用户服务获取用户详情，获取注册日期
            ApiResponse<Map<String, Object>> userDetailResponse = userServiceClient.getUserDetail(userId);
            
            // 检查响应是否成功且包含有效的数据
            if (userDetailResponse != null && userDetailResponse.getCode() == 200 && userDetailResponse.getData() != null) {
                Map<String, Object> userData = userDetailResponse.getData();
                if (userData != null && userData.containsKey("data")) {
                    Map<String, Object> userInfo = (Map<String, Object>) userData.get("data");
                    if (userInfo != null && userInfo.containsKey("createdAt")) {
                        // 解析用户注册日期
                        String createdAtStr = userInfo.get("createdAt").toString();
                        // 假设createdAt格式为ISO格式或可直接解析的日期字符串
                        // 这里需要根据实际格式进行适当的解析
                        LocalDateTime createdAt;
                        try {
                            createdAt = LocalDateTime.parse(createdAtStr);
                        } catch (Exception e) {
                            // 如果解析失败，尝试其他格式或记录日志
                            System.err.println("解析用户注册日期失败: " + e.getMessage());
                            // 尝试使用默认格式解析
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            createdAt = LocalDateTime.parse(createdAtStr, formatter);
                        }
                        
                        // 计算从注册日期到现在的天数
                        LocalDateTime now = LocalDateTime.now();
                        long days = ChronoUnit.DAYS.between(createdAt, now);
                        // 确保返回的天数不小于1
                        return Math.max(1, (int) days);
                    }
                }
            }
            
            // 如果无法获取注册日期，返回默认估算值
            return 90;
        } catch (Exception e) {
            // 发生异常时记录日志，返回默认值
            System.err.println("获取总学习天数失败: " + e.getMessage());
            return 30; // 失败时返回默认值
        }
    }
}