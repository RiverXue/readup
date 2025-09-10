-- 先清空现有数据
USE readup_ai;

-- 清空所有表（按外键依赖顺序）
DELETE FROM review_schedule;
DELETE FROM reading_log;
DELETE FROM word;
DELETE FROM ai_cache;
DELETE FROM reading_streak;
DELETE FROM article;
DELETE FROM user;

-- 重置自增计数器
ALTER TABLE user AUTO_INCREMENT = 1;
ALTER TABLE article AUTO_INCREMENT = 1;
ALTER TABLE word AUTO_INCREMENT = 1;
ALTER TABLE reading_log AUTO_INCREMENT = 1;
ALTER TABLE ai_cache AUTO_INCREMENT = 1;
ALTER TABLE review_schedule AUTO_INCREMENT = 1;
ALTER TABLE reading_streak AUTO_INCREMENT = 1;

-- 用户表测试数据（使用简单明文密码，方便测试）
INSERT INTO `user` (`username`, `password`, `phone`, `interest_tag`, `identity_tag`, `learning_goal_words`, `target_reading_time`) VALUES
('test1', '123456', '13800138001', 'tech', '考研', 8000, 60),
('test2', '123456', '13800138002', 'business', '职场', 5000, 45),
('test3', '123456', '13800138003', 'culture', '留学', 10000, 90),
('test4', '123456', '13800138004', 'tech', '四六级', 4000, 30),
('admin', 'admin123', '13800138005', 'business', '考研', 7000, 75),
('user1', 'password1', '13800138006', 'culture', '职场', 6000, 50),
('user2', 'password2', '13800138007', 'tech', '留学', 9000, 80),
('user3', 'password3', '13800138008', 'business', '四六级', 3500, 25),
('student', 'student123', '13800138009', 'culture', '考研', 7500, 65),
('teacher', 'teacher123', '13800138010', 'tech', '职场', 5500, 40);

-- 文章表测试数据（包含所有字段的完整数据）
INSERT INTO `article` (`title`, `content_en`, `content_cn`, `description`, `url`, `image`, `published_at`, `source`, `category`, `difficulty_level`, `manual_difficulty`, `manual_category`, `word_count`, `read_count`, `like_count`, `is_featured`) VALUES
('AI Revolution in Education', 'Artificial intelligence is transforming education through personalized learning paths, intelligent tutoring systems, and automated assessment. Students now receive customized content based on their learning pace and style.', '人工智能正在通过个性化学习路径、智能辅导系统和自动评估来改变教育。学生现在可以根据自己的学习节奏和风格接收定制内容。', '探索AI如何革命性地改变教育领域', 'https://example.com/ai-education', 'https://example.com/images/ai-education.jpg', '2024-01-15 10:00:00', 'TechNews', 'technology', 'B2', 'B2', '科技', 125, 156, 45, 1),
('Climate Change Global Impact', 'Global warming affects every aspect of our lives, from rising sea levels threatening coastal cities to extreme weather disrupting agriculture. Understanding these impacts is crucial for developing adaptation strategies.', '全球变暖影响着我们生活的方方面面，从威胁沿海城市的海平面上升到极端天气对农业的破坏。理解这些影响对于制定适应战略至关重要。', '深入分析气候变化对全球的深远影响', 'https://example.com/climate-impact', 'https://example.com/images/climate.jpg', '2024-01-14 14:30:00', 'ScienceDaily', 'environment', 'C1', 'C1', '环境', 180, 89, 23, 0),
('Remote Work Future Trends', 'The shift to remote work has permanently changed the workplace landscape. Companies are adopting hybrid models, employees enjoy better work-life balance, and cities are reimagining office spaces.', '向远程工作的转变已经永久性地改变了工作场所格局。公司正在采用混合模式，员工享受更好的工作与生活平衡，城市正在重新构想办公空间。', '远程工作如何重塑未来职场格局', 'https://example.com/remote-work', 'https://example.com/images/remote-work.jpg', '2024-01-13 09:15:00', 'BusinessWeek', 'business', 'B2', 'B2', '商业', 95, 234, 67, 1),
('Space Exploration Breakthrough', 'Recent advances in rocket technology and private space companies have made space more accessible than ever. Mars colonization plans and lunar bases represent humanity''s next frontier.', '火箭技术的最新进展和私人太空公司使太空比以往任何时候都更容易进入。火星殖民计划和月球基地代表了人类的下一个前沿。', '太空探索技术的突破性进展和未来展望', 'https://example.com/space-exploration', 'https://example.com/images/space.jpg', '2024-01-12 16:45:00', 'SpaceNews', 'science', 'B1', 'B1', '科学', 110, 178, 34, 0),
('Blockchain Beyond Bitcoin', 'Blockchain technology extends far beyond cryptocurrencies into supply chain management, digital identity, and smart contracts. This distributed ledger technology promises to revolutionize multiple industries.', '区块链技术远远超出了加密货币的范畴，延伸到供应链管理、数字身份和智能合约。这种分布式账本技术有望彻底改变多个行业。', '区块链技术在各个行业的创新应用', 'https://example.com/blockchain-tech', 'https://example.com/images/blockchain.jpg', '2024-01-11 11:20:00', 'TechCrunch', 'technology', 'C1', 'C1', '科技', 145, 145, 56, 1),
('Digital Mental Health', 'The digital age brings new mental health challenges including social media addiction, information overload, and cyberbullying. Solutions include digital detox strategies and online therapy platforms.', '数字时代带来了新的心理健康挑战，包括社交媒体成瘾、信息过载和网络欺凌。解决方案包括数字排毒策略和在线治疗平台。', '数字时代心理健康的挑战与解决方案', 'https://example.com/digital-health', 'https://example.com/images/mental-health.jpg', '2024-01-10 13:00:00', 'HealthJournal', 'health', 'B2', 'B2', '健康', 130, 267, 89, 0),
('Renewable Energy Surge', 'Solar and wind power costs have dropped dramatically, making renewable energy the cheapest option in many regions. Battery storage improvements and grid modernization support this transition.', '太阳能和风能成本大幅下降，使可再生能源成为许多地区最便宜的能源选择。电池存储的改进和电网现代化支持这一转变。', '可再生能源成本下降推动全球能源转型', 'https://example.com/renewable-energy', 'https://example.com/images/renewable.jpg', '2024-01-09 15:30:00', 'EnergyToday', 'environment', 'B1', 'B1', '环境', 105, 198, 42, 1),
('Cybersecurity Evolution', 'Modern cyber threats include AI-powered attacks, ransomware targeting critical infrastructure, and sophisticated phishing schemes. Defense strategies emphasize zero-trust architecture and continuous monitoring.', '现代网络威胁包括人工智能驱动的攻击、针对关键基础设施的勒索软件和复杂的网络钓鱼计划。防御战略强调零信任架构和持续监控。', '网络安全威胁的演变与防御策略', 'https://example.com/cybersecurity', 'https://example.com/images/cybersecurity.jpg', '2024-01-08 12:00:00', 'SecurityFocus', 'technology', 'C2', 'C2', '科技', 165, 123, 29, 0),
('Supply Chain Resilience', 'Global supply chains face challenges from geopolitical tensions, pandemic disruptions, and climate events. Companies are building resilience through diversification, nearshoring, and digital tracking systems.', '全球供应链面临地缘政治紧张局势、疫情中断和气候事件的挑战。公司通过多样化、近岸外包和数字跟踪系统建立弹性。', '构建弹性供应链应对全球挑战', 'https://example.com/supply-chain', 'https://example.com/images/supply-chain.jpg', '2024-01-07 10:45:00', 'LogisticsToday', 'business', 'B2', 'B2', '商业', 140, 156, 51, 1),
('AI Healthcare Revolution', 'Artificial intelligence is revolutionizing healthcare through early disease detection, personalized treatment plans, and drug discovery acceleration. AI systems now outperform doctors in certain diagnostic tasks.', '人工智能正在通过早期疾病检测、个性化治疗方案和药物发现加速彻底改变医疗保健。人工智能系统现在在某些诊断任务中胜过医生。', 'AI在医疗保健领域的革命性应用', 'https://example.com/ai-healthcare', 'https://example.com/images/ai-healthcare.jpg', '2024-01-06 14:15:00', 'MedicalTech', 'health', 'C1', 'C1', '健康', 155, 289, 78, 1),
('Digital Currency Future', 'Central bank digital currencies (CBDCs) are being explored by nations worldwide to modernize payment systems and maintain monetary sovereignty while addressing privacy and security concerns.', '世界各国正在探索中央银行数字货币（CBDC），以现代化支付系统并保持货币主权，同时解决隐私和安全问题。', '央行数字货币的未来发展趋势', 'https://example.com/digital-currency', 'https://example.com/images/digital-currency.jpg', '2024-01-05 09:30:00', 'FinanceToday', 'finance', 'B2', 'B2', '金融', 120, 167, 35, 0),
('Sustainable Agriculture Tech', 'Innovations in vertical farming, precision agriculture, and biotechnology are increasing food production while reducing environmental impact. These technologies promise food security for growing populations.', '垂直农业、精准农业和生物技术的创新正在增加粮食生产，同时减少环境影响。这些技术为不断增长的人口保障粮食安全。', '可持续农业技术创新保障未来粮食安全', 'https://example.com/sustainable-agriculture', 'https://example.com/images/agriculture.jpg', '2024-01-04 16:00:00', 'AgriTech', 'environment', 'B1', 'B1', '环境', 115, 134, 28, 1);

-- 单词表测试数据
INSERT INTO `word` (`user_id`, `word`, `meaning`, `source_article_id`, `review_status`, `next_review_at`) VALUES
(1, 'revolutionize', '彻底改变', 1, 'learning', DATE_ADD(NOW(), INTERVAL 1 DAY)),
(1, 'personalized', '个性化的', 1, 'new', DATE_ADD(NOW(), INTERVAL 1 DAY)),
(2, 'mitigation', '缓解，减轻', 2, 'mastered', DATE_ADD(NOW(), INTERVAL 15 DAY)),
(2, 'adaptation', '适应，改编', 2, 'learning', DATE_ADD(NOW(), INTERVAL 2 DAY)),
(3, 'hybrid', '混合的', 3, 'new', DATE_ADD(NOW(), INTERVAL 1 DAY)),
(3, 'accessible', '可接近的，易理解的', 4, 'learning', DATE_ADD(NOW(), INTERVAL 1 DAY)),
(4, 'distributed', '分布式的', 5, 'mastered', DATE_ADD(NOW(), INTERVAL 30 DAY)),
(4, 'addiction', '成瘾', 6, 'learning', DATE_ADD(NOW(), INTERVAL 3 DAY)),
(5, 'dramatically', '显著地', 7, 'new', DATE_ADD(NOW(), INTERVAL 1 DAY)),
(5, 'sophisticated', '复杂的，精密的', 8, 'learning', DATE_ADD(NOW(), INTERVAL 2 DAY)),
(6, 'resilience', '弹性，恢复力', 9, 'mastered', DATE_ADD(NOW(), INTERVAL 20 DAY)),
(6, 'detection', '检测，发现', 10, 'learning', DATE_ADD(NOW(), INTERVAL 1 DAY)),
(7, 'sovereignty', '主权', 11, 'new', DATE_ADD(NOW(), INTERVAL 1 DAY)),
(7, 'innovations', '创新', 12, 'learning', DATE_ADD(NOW(), INTERVAL 1 DAY)),
(8, 'transformation', '转变，转化', 1, 'mastered', DATE_ADD(NOW(), INTERVAL 45 DAY)),
(8, 'sustainable', '可持续的', 12, 'learning', DATE_ADD(NOW(), INTERVAL 2 DAY)),
(9, 'cybersecurity', '网络安全', 8, 'new', DATE_ADD(NOW(), INTERVAL 1 DAY)),
(9, 'personalization', '个性化', 1, 'learning', DATE_ADD(NOW(), INTERVAL 3 DAY)),
(10, 'biotechnology', '生物技术', 12, 'mastered', DATE_ADD(NOW(), INTERVAL 60 DAY)),
(10, 'transparency', '透明度', 5, 'learning', DATE_ADD(NOW(), INTERVAL 1 DAY));

-- 阅读记录测试数据
INSERT INTO `reading_log` (`user_id`, `article_id`, `read_time_sec`, `finished_at`) VALUES
(1, 1, 300, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 2, 450, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 3, 280, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 4, 380, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 5, 320, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 6, 410, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 7, 290, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 8, 390, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 9, 350, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, 10, 470, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 11, 310, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(6, 12, 420, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 1, 340, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(7, 3, 380, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(8, 5, 360, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(8, 7, 430, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9, 9, 300, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9, 11, 400, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(10, 2, 370, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 4, 440, DATE_SUB(NOW(), INTERVAL 6 DAY));

-- 阅读打卡测试数据
INSERT INTO `reading_streak` (`user_id`, `streak_days`, `last_read_date`) VALUES
(1, 15, CURDATE()),
(2, 8, CURDATE()),
(3, 23, CURDATE()),
(4, 5, CURDATE()),
(5, 12, CURDATE()),
(6, 19, CURDATE()),
(7, 7, CURDATE()),
(8, 31, CURDATE()),
(9, 3, CURDATE()),
(10, 45, CURDATE());

-- 显示插入结果
SELECT '数据插入完成！' AS message;
SELECT CONCAT('用户总数: ', (SELECT COUNT(*) FROM user)) AS result;
SELECT CONCAT('文章总数: ', (SELECT COUNT(*) FROM article)) AS result;
SELECT CONCAT('单词总数: ', (SELECT COUNT(*) FROM word)) AS result;
SELECT CONCAT('阅读记录总数: ', (SELECT COUNT(*) FROM reading_log)) AS result;