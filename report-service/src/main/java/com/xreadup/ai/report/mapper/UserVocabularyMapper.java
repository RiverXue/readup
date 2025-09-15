package com.xreadup.ai.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xreadup.ai.report.dto.ReviewWordDto;
import com.xreadup.ai.report.entity.UserVocabulary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserVocabularyMapper extends BaseMapper<UserVocabulary> {

    @Select("SELECT COALESCE(COUNT(*), 0) FROM word WHERE user_id = #{userId}")
    Integer countTotalWords(@Param("userId") Long userId);

    @Select("SELECT COALESCE(COUNT(*), 0) FROM word WHERE user_id = #{userId} AND DATE(added_at) >= #{date}")
    Integer countWordsAfterDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT DATE(added_at) as date, COALESCE(COUNT(*), 0) as count FROM word WHERE user_id = #{userId} AND DATE(added_at) >= #{startDate} GROUP BY DATE(added_at) ORDER BY DATE(added_at)")
    List<Map<String, Object>> getDailyWordCounts(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Select("SELECT review_status as difficulty, COALESCE(COUNT(*), 0) as count FROM word WHERE user_id = #{userId} GROUP BY review_status")
    List<Map<String, Object>> getDifficultyDistribution(@Param("userId") Long userId);

    @Select("SELECT w.id as wordId, w.word, w.meaning, '' as example, w.review_status as difficulty, 0 as reviewCount, w.next_review_at as dueDate FROM word w WHERE w.user_id = #{userId} AND w.next_review_at <= CURDATE() ORDER BY w.next_review_at")
    List<ReviewWordDto> getWordsForReview(@Param("userId") Long userId);

    @Select("SELECT COALESCE(COUNT(*), 0) FROM word WHERE user_id = #{userId} AND next_review_at <= CURDATE()")
    Integer countWordsForReview(@Param("userId") Long userId);

    @Select("SELECT COALESCE(COUNT(*), 0) FROM word WHERE user_id = #{userId} AND last_reviewed_at IS NOT NULL")
    Integer countReviewedWords(@Param("userId") Long userId);

    @Select("SELECT COALESCE(COUNT(*), 0) FROM word WHERE user_id = #{userId} AND last_reviewed_at IS NOT NULL")
    Integer countSuccessfulReviews(@Param("userId") Long userId);

    @Update("UPDATE word SET next_review_at = #{nextReviewDate}, last_reviewed_at = NOW() WHERE id = #{wordId}")
    int updateReviewStatus(@Param("wordId") Long wordId, @Param("nextReviewDate") LocalDate nextReviewDate, @Param("result") boolean result);

    @Select("SELECT COALESCE(COUNT(*), 0) FROM word WHERE user_id = #{userId} AND DATE(added_at) >= #{startDate} AND DATE(added_at) <= #{endDate}")
    Integer countWordsByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}