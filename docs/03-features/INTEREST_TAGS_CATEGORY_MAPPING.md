# 兴趣标签与文章分类映射关系

## 📋 **完整映射表**

| 兴趣标签 (interestTag) | 文章分类 (category) | 中文显示 | 说明 |
|----------------------|-------------------|---------|------|
| `technology` | `technology` | 科技 | 技术、IT、人工智能等 |
| `business` | `business` | 商业 | 经济、金融、创业等 |
| `entertainment` | `entertainment` | 娱乐 | 电影、音乐、游戏等 |
| `sports` | `sports` | 体育 | 运动、比赛、健身等 |
| `science` | `science` | 科学 | 科学研究、发现等 |
| `health` | `health` | 健康 | 医疗、养生、心理健康等 |
| `world` | `world` | 国际 | 国际新闻、全球事件等 |
| `nation` | `nation` | 国内 | 国内新闻、政策等 |
| `general` | `general` | 综合 | 综合类文章 |

## 🔄 **数据流转过程**

### 1. **用户注册时**
- 用户在注册页面选择兴趣标签（如：`technology`）
- 数据保存到 `user.interest_tag` 字段

### 2. **文章推荐时**
- 系统读取用户的 `interest_tag` 字段
- 将兴趣标签转换为对应的文章分类
- 根据分类筛选推荐文章

### 3. **文章分类来源**
- **AI自动分类**：`article.category` 字段
- **手动标注分类**：`article.manual_category` 字段
- **基于GNews API标准**：确保分类一致性

## 🎯 **推荐算法逻辑**

```javascript
// 前端推荐逻辑示例
const convertInterestTagsToEnglish = (tags: string[]): string[] => {
  return tags.map(tag => {
    // 直接映射，因为现在兴趣标签与文章分类完全一致
    return tag // technology, business, entertainment, etc.
  }).filter(tag => CATEGORY_MAP[tag]) // 只保留有效的分类
}
```

## 📊 **数据库字段说明**

### 用户表 (user)
```sql
`interest_tag` VARCHAR(50) COMMENT '兴趣标签：technology/business/entertainment/sports/science/health/world/nation/general'
```

### 文章表 (article)
```sql
`category` VARCHAR(50) COMMENT 'AI自动分类'
`manual_category` VARCHAR(50) COMMENT '手动标注分类：科技/商业/娱乐/体育/科学/健康/国际/国内/综合'
```

## ✅ **修复内容**

1. **统一了兴趣标签与文章分类**：现在完全一一对应
2. **扩展了兴趣标签选项**：从3个增加到9个
3. **确保了数据一致性**：前端选择与后端存储完全匹配
4. **优化了推荐算法**：基于统一的分类体系

## 🚀 **使用建议**

1. **新用户注册**：可以选择多个感兴趣的分类
2. **文章推荐**：系统会根据用户兴趣标签推荐相关文章
3. **个性化体验**：不同兴趣标签的用户会看到不同的内容推荐
4. **数据统计**：可以分析用户兴趣分布，优化内容策略
