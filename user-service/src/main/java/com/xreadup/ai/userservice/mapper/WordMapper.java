package com.xreadup.ai.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xreadup.ai.userservice.entity.Word;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 生词Mapper接口
 */
@Mapper
public interface WordMapper extends BaseMapper<Word> {
    /**
     * 根据用户ID查询单词列表
     */
    @Select("SELECT * FROM word WHERE user_ids LIKE CONCAT('%', #{userId}, '%')")
    List<Word> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据单词和用户ID查询单词
     */
    @Select("SELECT * FROM word WHERE word = #{word} AND user_ids LIKE CONCAT('%', #{userId}, '%') LIMIT 1")
    Word findByWordAndUserId(@Param("word") String word, @Param("userId") Long userId);
    
    /**
     * 检查单词是否存在（不考虑用户）
     */
    @Select("SELECT * FROM word WHERE word = #{word} LIMIT 1")
    Word findByWord(@Param("word") String word);
    
    /**
     * 更新单词的用户ID列表
     */
    @Update("UPDATE word SET user_ids = #{userIds} WHERE id = #{id}")
    int updateUserIds(@Param("id") Long id, @Param("userIds") String userIds);
}