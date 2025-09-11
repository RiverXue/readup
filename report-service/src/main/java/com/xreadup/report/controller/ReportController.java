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

    @GetMapping("/today/summary")
    @Operation(summary = "【今日小结】学习日报", description = "今日阅读成果一目了然")
    public ResponseEntity<Map<String, Object>> todaySummary() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("articlesRead", 5);
        stats.put("wordsLearned", 25);
        stats.put("studyTime", 45);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/weekly/insights")
    @Operation(summary = "【一周洞察】学习周报", description = "本周学习成果深度分析")
    public ResponseEntity<Map<String, Object>> weeklyInsights() {
        Map<String, Object> report = new HashMap<>();
        report.put("totalArticles", 35);
        report.put("totalWords", 175);
        report.put("totalStudyTime", 315);
        report.put("averageDifficulty", "B2");
        return ResponseEntity.ok(report);
    }

    @GetMapping("/streak/achievement")
    @Operation(summary = "【学习成就】连续打卡", description = "见证你的坚持，记录每一个学习里程碑")
    public ResponseEntity<Map<String, Object>> streakAchievement() {
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