package com.xreadup.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@Tag(name = "报表服务", description = "提供学习统计和报表生成功能")
public class ReportController {

    @GetMapping("/daily-stats")
    @Operation(summary = "获取每日学习统计", description = "获取用户的每日学习数据统计")
    public ResponseEntity<Map<String, Object>> getDailyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("articlesRead", 5);
        stats.put("wordsLearned", 25);
        stats.put("studyTime", 45);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/weekly-report")
    @Operation(summary = "获取周学习报告", description = "生成用户的周学习报告")
    public ResponseEntity<Map<String, Object>> getWeeklyReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("totalArticles", 35);
        report.put("totalWords", 175);
        report.put("totalStudyTime", 315);
        report.put("averageDifficulty", "B2");
        return ResponseEntity.ok(report);
    }

    @GetMapping("/streak")
    @Operation(summary = "获取连续学习天数", description = "获取用户的连续学习天数统计")
    public ResponseEntity<Map<String, Object>> getStreak() {
        Map<String, Object> streak = new HashMap<>();
        streak.put("currentStreak", 7);
        streak.put("longestStreak", 15);
        streak.put("totalDays", 30);
        return ResponseEntity.ok(streak);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查报表服务状态")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("报表服务运行正常");
    }
}