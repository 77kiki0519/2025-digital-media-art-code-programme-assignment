-- 添加测试数据：章节、视频、知识点、字幕等

USE online_course_system;

-- 1. 为课程ID=1添加章节
INSERT INTO course_chapters (course_id, chapter_name, chapter_order, description) VALUES
(1, '第一章：人工智能基础', 1, '介绍人工智能的基本概念'),
(1, '第二章：机器学习入门', 2, '学习机器学习的基本原理'),
(1, '第三章：深度学习应用', 3, '深度学习实战案例');

-- 2. 为章节添加视频
INSERT INTO course_videos (course_id, chapter_id, title, video_url, subtitle_url, cover_image, duration, order_num, ai_generated, allow_speed, allow_danmaku) VALUES
(1, 1, 'AI概述与发展历史', 'https://www.w3schools.com/html/mov_bbb.mp4', '', '', 300, 1, 0, 1, 1),
(1, 1, '人工智能的应用领域', 'https://www.w3schools.com/html/movie.mp4', '', '', 420, 2, 0, 1, 1),
(1, 2, '监督学习原理', 'https://www.w3schools.com/html/mov_bbb.mp4', '', '', 360, 1, 0, 1, 1),
(1, 2, '非监督学习方法', 'https://www.w3schools.com/html/movie.mp4', '', '', 380, 2, 0, 1, 1),
(1, 3, 'CNN卷积神经网络', 'https://www.w3schools.com/html/mov_bbb.mp4', '', '', 450, 1, 0, 1, 1);

-- 3. 为视频添加多语言字幕
INSERT INTO video_subtitles (video_id, language, language_name, subtitle_content, ai_translated, is_default) VALUES
(1, 'zh-CN', '中文', '这是中文字幕内容...', 0, 1),
(1, 'en-US', 'English', 'This is English subtitle content...', 1, 0),
(1, 'ja-JP', '日本語', 'これは日本語の字幕内容です...', 1, 0);

-- 4. 为视频添加知识点弹窗
INSERT INTO knowledge_points (video_id, time_point, title, content, point_type, enabled) VALUES
(1, 30, '重要概念', '人工智能的三大核心：感知、学习、推理', 'IMPORTANT', 1),
(1, 90, '注意事项', '弱人工智能与强人工智能的区别', 'WARNING', 1),
(1, 150, '扩展阅读', '推荐阅读《人工智能：一种现代方法》', 'INFO', 1);

-- 5. 为视频添加小测验
INSERT INTO video_quiz (video_id, time_point, question_type, content, options, correct_answer, analysis, enabled) VALUES
(1, 60, 'SINGLE_CHOICE', '人工智能是哪一年提出的概念？', 
 '["A. 1950年", "B. 1956年", "C. 1960年", "D. 1970年"]', 
 'B', '人工智能概念于1956年在达特茅斯会议上首次提出', 1),
(1, 120, 'TRUE_FALSE', '深度学习是机器学习的一个子领域', 
 '["正确", "错误"]', 
 '正确', '深度学习确实是机器学习的一个重要分支', 1);

-- 6. 添加章节练习
INSERT INTO chapter_exercises (chapter_id, exercise_name, description, total_score, pass_score, time_limit, allow_retry, status) VALUES
(1, '第一章测验', '检验你对AI基础知识的掌握程度', 100, 60, 30, 1, 1),
(2, '第二章练习', '机器学习基础测试', 100, 60, 45, 1, 1);

-- 7. 为章节练习添加题目
INSERT INTO chapter_exercise_questions (exercise_id, question_type, content, options, correct_answer, score, analysis, question_order) VALUES
(1, 'SINGLE_CHOICE', '以下哪项不是人工智能的应用领域？', 
 '["A. 图像识别", "B. 自然语言处理", "C. 手工编程", "D. 语音识别"]',
 'C', 10.00, '手工编程不是AI应用，AI强调的是自动学习', 1),
(1, 'MULTIPLE_CHOICE', '机器学习包括哪些类型？（多选）',
 '["A. 监督学习", "B. 非监督学习", "C. 强化学习", "D. 手动学习"]',
 'A,B,C', 15.00, '机器学习的三大类型：监督学习、非监督学习、强化学习', 2);

-- 完成
SELECT '测试数据插入完成！' as message;


