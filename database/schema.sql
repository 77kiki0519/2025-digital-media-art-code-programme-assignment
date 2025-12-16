-- AI驱动线上课程系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS online_course_system 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE online_course_system;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(500) COMMENT '头像URL',
    student_no VARCHAR(50) COMMENT '学号',
    teacher_no VARCHAR(50) COMMENT '工号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_student_no (student_no),
    INDEX idx_teacher_no (teacher_no)
) COMMENT '用户表';

-- 2. 角色表
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) UNIQUE NOT NULL COMMENT '角色代码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) COMMENT '角色表';

-- 3. 用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) COMMENT '用户角色关联表';

-- 4. 课程表
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(50) UNIQUE NOT NULL COMMENT '课程编号',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    description TEXT COMMENT '课程描述',
    cover_image VARCHAR(500) COMMENT '封面图片',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID',
    category VARCHAR(50) COMMENT '课程分类',
    difficulty TINYINT COMMENT '难度：1-简单，2-中等，3-困难',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    INDEX idx_teacher (teacher_id),
    INDEX idx_status (status)
) COMMENT '课程表';

-- 5. 课程章节表
CREATE TABLE IF NOT EXISTS course_chapters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    chapter_name VARCHAR(100) NOT NULL COMMENT '章节名称',
    chapter_order INT NOT NULL COMMENT '章节顺序',
    parent_id BIGINT COMMENT '父章节ID（支持多级章节）',
    description TEXT COMMENT '章节描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_course (course_id),
    INDEX idx_parent (parent_id)
) COMMENT '课程章节表';

-- 6. 课程教材表（模块二：教材制作）
CREATE TABLE IF NOT EXISTS course_materials (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    chapter_id BIGINT COMMENT '章节ID',
    material_name VARCHAR(200) NOT NULL COMMENT '教材名称',
    material_type VARCHAR(20) NOT NULL COMMENT '教材类型：TEXT-文本，PPT-演示文稿，VIDEO-视频',
    content LONGTEXT COMMENT '文本内容（用于文本转PPT）',
    file_url VARCHAR(500) COMMENT '文件URL（PPT/视频）',
    thumbnail_url VARCHAR(500) COMMENT '缩略图URL',
    pages INT COMMENT 'PPT页数',
    duration INT COMMENT '视频时长（秒）',
    ai_generated TINYINT DEFAULT 0 COMMENT '是否AI生成：0-否，1-是',
    generation_status VARCHAR(20) COMMENT '生成状态：PENDING-待生成，PROCESSING-生成中，COMPLETED-已完成，FAILED-失败',
    generation_params TEXT COMMENT '生成参数（JSON格式）',
    order_num INT COMMENT '排序号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES course_chapters(id) ON DELETE SET NULL,
    INDEX idx_course (course_id),
    INDEX idx_chapter (chapter_id),
    INDEX idx_type (material_type)
) COMMENT '课程教材表';

-- 7. 课程视频表
CREATE TABLE IF NOT EXISTS course_videos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    chapter_id BIGINT COMMENT '章节ID',
    material_id BIGINT COMMENT '关联教材ID',
    title VARCHAR(200) NOT NULL COMMENT '视频标题',
    video_url VARCHAR(2000) NOT NULL COMMENT '视频URL',
    subtitle_url VARCHAR(500) COMMENT '字幕URL',
    cover_image VARCHAR(500) COMMENT '封面图',
    duration INT COMMENT '时长（秒）',
    order_num INT COMMENT '排序号',
    ai_generated TINYINT DEFAULT 0 COMMENT '是否AI生成：0-否，1-是',
    allow_speed TINYINT DEFAULT 1 COMMENT '允许倍速：0-否，1-是',
    allow_danmaku TINYINT DEFAULT 1 COMMENT '允许弹幕：0-否，1-是',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES course_chapters(id) ON DELETE SET NULL,
    FOREIGN KEY (material_id) REFERENCES course_materials(id) ON DELETE SET NULL,
    INDEX idx_course (course_id),
    INDEX idx_chapter (chapter_id)
) COMMENT '课程视频表';

-- 8. 视频播放进度表（模块三：线上课程）
CREATE TABLE IF NOT EXISTS video_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    video_id BIGINT NOT NULL COMMENT '视频ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    progress INT DEFAULT 0 COMMENT '播放进度（秒）',
    duration INT COMMENT '视频总时长（秒）',
    completed TINYINT DEFAULT 0 COMMENT '是否完成：0-未完成，1-已完成',
    last_position INT COMMENT '最后播放位置（秒）',
    play_times INT DEFAULT 0 COMMENT '播放次数',
    last_play_time TIMESTAMP COMMENT '最后播放时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES course_videos(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_video_student (video_id, student_id),
    INDEX idx_student (student_id)
) COMMENT '视频播放进度表';

-- 9. 视频笔记表（模块三：线上课程）
CREATE TABLE IF NOT EXISTS video_notes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    video_id BIGINT NOT NULL COMMENT '视频ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    time_point INT NOT NULL COMMENT '时间点（秒）',
    content TEXT NOT NULL COMMENT '笔记内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES course_videos(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_video (video_id),
    INDEX idx_student (student_id)
) COMMENT '视频笔记表';

-- 10. 视频弹幕表（模块三：线上课程）
CREATE TABLE IF NOT EXISTS video_danmaku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    video_id BIGINT NOT NULL COMMENT '视频ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    time_point INT NOT NULL COMMENT '时间点（秒）',
    content VARCHAR(500) NOT NULL COMMENT '弹幕内容',
    color VARCHAR(20) DEFAULT '#FFFFFF' COMMENT '弹幕颜色',
    type TINYINT DEFAULT 0 COMMENT '弹幕类型：0-滚动，1-顶部，2-底部',
    status TINYINT DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES course_videos(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_video_time (video_id, time_point),
    INDEX idx_student (student_id)
) COMMENT '视频弹幕表';

-- 11. 视频小测验表（模块三：线上课程）
CREATE TABLE IF NOT EXISTS video_quiz (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    video_id BIGINT NOT NULL COMMENT '视频ID',
    time_point INT NOT NULL COMMENT '弹出时间点（秒）',
    question_type VARCHAR(20) NOT NULL COMMENT '题型：SINGLE_CHOICE-单选，MULTIPLE_CHOICE-多选，TRUE_FALSE-判断',
    content TEXT NOT NULL COMMENT '题目内容',
    options TEXT NOT NULL COMMENT '选项（JSON格式）',
    correct_answer VARCHAR(200) NOT NULL COMMENT '正确答案',
    analysis TEXT COMMENT '答案解析',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES course_videos(id) ON DELETE CASCADE,
    INDEX idx_video (video_id)
) COMMENT '视频小测验表';

-- 12. 章节练习表（模块三：线上课程）
CREATE TABLE IF NOT EXISTS chapter_exercises (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    exercise_name VARCHAR(200) NOT NULL COMMENT '练习名称',
    description TEXT COMMENT '练习说明',
    total_score INT DEFAULT 100 COMMENT '总分',
    pass_score INT DEFAULT 60 COMMENT '及格分数',
    time_limit INT COMMENT '时间限制（分钟）',
    allow_retry TINYINT DEFAULT 1 COMMENT '允许重做：0-否，1-是',
    status TINYINT DEFAULT 1 COMMENT '状态：0-关闭，1-开放',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (chapter_id) REFERENCES course_chapters(id) ON DELETE CASCADE,
    INDEX idx_chapter (chapter_id)
) COMMENT '章节练习表';

-- 13. 章节练习题目表
CREATE TABLE IF NOT EXISTS chapter_exercise_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exercise_id BIGINT NOT NULL COMMENT '练习ID',
    question_type VARCHAR(20) NOT NULL COMMENT '题型：SINGLE_CHOICE-单选，MULTIPLE_CHOICE-多选，TRUE_FALSE-判断，SHORT_ANSWER-简答',
    content TEXT NOT NULL COMMENT '题目内容',
    options TEXT COMMENT '选项（JSON格式）',
    correct_answer TEXT COMMENT '正确答案',
    score DECIMAL(5,2) NOT NULL COMMENT '分值',
    analysis TEXT COMMENT '解析',
    question_order INT COMMENT '题目顺序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exercise_id) REFERENCES chapter_exercises(id) ON DELETE CASCADE,
    INDEX idx_exercise (exercise_id)
) COMMENT '章节练习题目表';

-- 14. 章节练习提交表（模块三：线上课程）
CREATE TABLE IF NOT EXISTS chapter_exercise_submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exercise_id BIGINT NOT NULL COMMENT '练习ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    answers TEXT COMMENT '学生答案（JSON格式）',
    score DECIMAL(5,2) DEFAULT 0.00 COMMENT '得分',
    is_passed TINYINT DEFAULT 0 COMMENT '是否通过：0-未通过，1-通过',
    submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    duration INT COMMENT '用时（分钟）',
    retry_count INT DEFAULT 0 COMMENT '重做次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exercise_id) REFERENCES chapter_exercises(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_exercise (exercise_id),
    INDEX idx_student (student_id)
) COMMENT '章节练习提交表';

-- 15. 视频字幕表（模块三：多语言字幕）
CREATE TABLE IF NOT EXISTS video_subtitles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    video_id BIGINT NOT NULL COMMENT '视频ID',
    language VARCHAR(10) NOT NULL COMMENT '语言代码：zh-CN, en-US, ja-JP等',
    language_name VARCHAR(50) COMMENT '语言名称：中文、English、日本語',
    subtitle_url VARCHAR(500) COMMENT '字幕文件URL',
    subtitle_content LONGTEXT COMMENT '字幕内容（JSON格式）',
    ai_translated TINYINT DEFAULT 0 COMMENT '是否AI翻译：0-否，1-是',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认字幕：0-否，1-是',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES course_videos(id) ON DELETE CASCADE,
    UNIQUE KEY uk_video_language (video_id, language),
    INDEX idx_video (video_id)
) COMMENT '视频字幕表';

-- 16. 知识点弹窗表（模块三：视频内嵌知识点）
CREATE TABLE IF NOT EXISTS knowledge_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    video_id BIGINT NOT NULL COMMENT '视频ID',
    time_point INT NOT NULL COMMENT '弹出时间点（秒）',
    title VARCHAR(200) NOT NULL COMMENT '知识点标题',
    content TEXT COMMENT '知识点详细内容',
    point_type VARCHAR(20) COMMENT '类型：INFO-提示，IMPORTANT-重点，WARNING-注意',
    related_url VARCHAR(500) COMMENT '相关资料链接',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES course_videos(id) ON DELETE CASCADE,
    INDEX idx_video_time (video_id, time_point)
) COMMENT '知识点弹窗表';

-- 17. 学习记录表（模块三：课程统计）
CREATE TABLE IF NOT EXISTS learning_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    chapter_id BIGINT COMMENT '章节ID',
    video_id BIGINT COMMENT '视频ID',
    action_type VARCHAR(20) NOT NULL COMMENT '行为类型：VIEW-观看，PAUSE-暂停，COMPLETE-完成，NOTE-笔记，QUIZ-测验',
    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
    duration INT COMMENT '持续时长（秒）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES course_chapters(id) ON DELETE SET NULL,
    FOREIGN KEY (video_id) REFERENCES course_videos(id) ON DELETE SET NULL,
    INDEX idx_student (student_id),
    INDEX idx_course (course_id),
    INDEX idx_action_time (action_time)
) COMMENT '学习记录表';

-- 18. 考试表（与后端 Exam.java 实体匹配）
CREATE TABLE IF NOT EXISTS exams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    exam_name VARCHAR(200) NOT NULL COMMENT '考试名称',
    exam_type VARCHAR(20) COMMENT '考试类型：QUIZ-测验，MIDTERM-期中，FINAL-期末',
    description TEXT COMMENT '考试说明',
    total_score INT COMMENT '总分',
    duration INT COMMENT '时长（分钟）',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    ai_generated TINYINT DEFAULT 0 COMMENT '是否AI生成：0-否，1-是',
    anti_cheat TINYINT DEFAULT 0 COMMENT '是否开启防作弊：0-否，1-是',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未发布，1-已发布',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_course (course_id)
) COMMENT '考试表';

-- 19. 试题表（与后端 ExamQuestion.java 实体匹配）
CREATE TABLE IF NOT EXISTS exam_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL COMMENT '考试ID',
    question_type VARCHAR(20) NOT NULL COMMENT '题型：SINGLE_CHOICE-单选，MULTIPLE_CHOICE-多选，TRUE_FALSE-判断，SHORT_ANSWER-简答',
    content TEXT NOT NULL COMMENT '题目内容',
    options TEXT COMMENT '选项（JSON格式）',
    correct_answer TEXT COMMENT '正确答案',
    score DECIMAL(5,2) NOT NULL COMMENT '分值',
    difficulty INT COMMENT '难度：1-简单，2-中等，3-困难',
    knowledge_point VARCHAR(200) COMMENT '知识点',
    analysis TEXT COMMENT '解析',
    question_order INT COMMENT '题目顺序',
    ai_generated TINYINT DEFAULT 0 COMMENT '是否AI生成：0-否，1-是',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    INDEX idx_exam (exam_id)
) COMMENT '试题表';

-- 20. 考试提交表（与后端 ExamSubmission.java 实体匹配）
CREATE TABLE IF NOT EXISTS exam_submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL COMMENT '考试ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    submit_time TIMESTAMP COMMENT '提交时间',
    duration INT COMMENT '用时(分钟)',
    total_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '总得分',
    status TINYINT DEFAULT 0 COMMENT '状态：0-进行中，1-已提交，2-已批改',
    ai_scored TINYINT DEFAULT 0 COMMENT '是否AI评分：0-否，1-是',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_exam (exam_id),
    INDEX idx_student (student_id)
) COMMENT '考试提交表';

-- 21. 报告作业表（与后端 Report.java 实体匹配）
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    report_name VARCHAR(200) NOT NULL COMMENT '报告名称',
    description TEXT COMMENT '报告描述',
    requirements TEXT COMMENT '详细要求',
    word_limit INT COMMENT '字数限制',
    deadline DATETIME COMMENT '截止时间',
    total_score INT COMMENT '总分',
    status TINYINT DEFAULT 1 COMMENT '状态：0-关闭，1-开放',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_course (course_id)
) COMMENT '报告作业表';

-- 22. 报告提交表（与后端 ReportSubmission.java 实体匹配）
CREATE TABLE IF NOT EXISTS report_submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL COMMENT '报告ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    title VARCHAR(200) COMMENT '报告标题',
    content LONGTEXT COMMENT '报告内容',
    file_url VARCHAR(500) COMMENT '文件URL',
    file_type VARCHAR(20) COMMENT '文件类型：WORD/PDF/MARKDOWN',
    word_count INT COMMENT '字数',
    submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未提交，1-已提交，2-已批改',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_report (report_id),
    INDEX idx_student (student_id)
) COMMENT '报告提交表';

-- 23. 提问表
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    video_id BIGINT COMMENT '视频ID',
    chapter_id BIGINT COMMENT '章节ID',
    question_type VARCHAR(20) COMMENT '问题类型：TEXT-文字，VOICE-语音',
    content TEXT NOT NULL COMMENT '问题内容',
    voice_url VARCHAR(500) COMMENT '语音URL',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待回答，1-已回答',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_course (course_id),
    INDEX idx_student (student_id)
) COMMENT '提问表';

-- 24. 回答表
CREATE TABLE IF NOT EXISTS answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL COMMENT '问题ID',
    answerer_id BIGINT COMMENT '回答者ID（教师或AI）',
    answerer_type VARCHAR(20) NOT NULL COMMENT '回答者类型：TEACHER-教师，AI-AI助教',
    content TEXT NOT NULL COMMENT '回答内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    INDEX idx_question (question_id)
) COMMENT '回答表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入角色数据
INSERT INTO roles (role_code, role_name, description) VALUES
('STUDENT', '学生', '可以学习课程、提交作业、参加考试'),
('TEACHER', '教师', '可以创建课程、发布作业、批改作业'),
('ADMIN', '管理员', '系统管理员，拥有所有权限');

-- 插入默认管理员账号（密码：admin123）
INSERT INTO users (username, password, real_name, email, status) VALUES
('admin', 'admin123', '系统管理员', 'admin@example.com', 1);

-- 插入测试教师账号（密码：teacher123）
INSERT INTO users (username, password, real_name, email, teacher_no, status) VALUES
('teacher1', 'teacher123', '张老师', 'teacher1@example.com', 'T001', 1);

-- 插入测试学生账号（密码：student123）
INSERT INTO users (username, password, real_name, email, student_no, status) VALUES
('student1', 'student123', '李同学', 'student1@example.com', 'S001', 1);

-- 绑定用户角色
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 3), -- admin -> ADMIN
(2, 2), -- teacher1 -> TEACHER
(3, 1); -- student1 -> STUDENT

-- ============================================
-- 注意事项
-- ============================================
-- 1. 以上密码为明文，需要通过后端程序加密
-- 2. 启动后端后，访问以下URL进行密码加密：
--    curl -X POST http://localhost:8085/api/admin/reset-passwords
-- 3. 或者直接使用已加密的密码替换上述明文密码
