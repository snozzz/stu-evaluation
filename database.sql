-- ============================================================================
-- 学生学习过程评价系统 - 数据库初始化脚本
-- Student Learning Process Evaluation System
--
-- 使用方法:
--   mysql -u root -p < database.sql
--
-- 包含: 建库 → 17张表 → 初始数据(配置/学期/用户/课程/班级/绑定/维度/权重/评分/评语/公告/预警)
-- ============================================================================

DROP DATABASE IF EXISTS stu_evaluation;
CREATE DATABASE stu_evaluation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE stu_evaluation;

-- ============================================================================
-- 1. 系统配置表
-- ============================================================================
CREATE TABLE sys_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  config_key VARCHAR(100) NOT NULL,
  config_value TEXT DEFAULT NULL,
  remark VARCHAR(255) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 2. 学期表
-- ============================================================================
CREATE TABLE semester (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  is_current TINYINT(1) DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 3. 用户表（管理员 / 教师 / 学生 共用）
-- ============================================================================
CREATE TABLE sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) DEFAULT NULL,
  nickname VARCHAR(50) DEFAULT NULL,
  avatar VARCHAR(500) DEFAULT NULL,
  phone VARCHAR(20) DEFAULT NULL,
  email VARCHAR(100) DEFAULT NULL,
  role ENUM('ADMIN','TEACHER','STUDENT') NOT NULL,
  college VARCHAR(100) DEFAULT NULL,
  major VARCHAR(100) DEFAULT NULL,
  class_name VARCHAR(100) DEFAULT NULL,
  student_no VARCHAR(50) DEFAULT NULL,
  status TINYINT(1) DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 4. 课程表
-- ============================================================================
CREATE TABLE course (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(50) DEFAULT NULL,
  description TEXT DEFAULT NULL,
  semester_id BIGINT DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY semester_id (semester_id),
  CONSTRAINT course_ibfk_1 FOREIGN KEY (semester_id) REFERENCES semester (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 5. 班级表
-- ============================================================================
CREATE TABLE class_info (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  college VARCHAR(100) DEFAULT NULL,
  major VARCHAR(100) DEFAULT NULL,
  grade VARCHAR(20) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 6. 教师-课程-班级绑定表
-- ============================================================================
CREATE TABLE teacher_course_class (
  id BIGINT NOT NULL AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  class_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tcc (teacher_id, course_id, class_id),
  KEY course_id (course_id),
  KEY class_id (class_id),
  CONSTRAINT teacher_course_class_ibfk_1 FOREIGN KEY (teacher_id) REFERENCES sys_user (id),
  CONSTRAINT teacher_course_class_ibfk_2 FOREIGN KEY (course_id) REFERENCES course (id),
  CONSTRAINT teacher_course_class_ibfk_3 FOREIGN KEY (class_id) REFERENCES class_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 7. 学生-班级关联表
-- ============================================================================
CREATE TABLE student_class (
  id BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  class_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sc (student_id, class_id),
  KEY class_id (class_id),
  CONSTRAINT student_class_ibfk_1 FOREIGN KEY (student_id) REFERENCES sys_user (id),
  CONSTRAINT student_class_ibfk_2 FOREIGN KEY (class_id) REFERENCES class_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 8. 评价维度表
-- ============================================================================
CREATE TABLE eval_dimension (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  sort_order INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 9. 课程评价权重表
-- ============================================================================
CREATE TABLE course_eval_weight (
  id BIGINT NOT NULL AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  dimension_id BIGINT NOT NULL,
  weight DECIMAL(5,2) DEFAULT 0.00,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cw (course_id, dimension_id),
  KEY dimension_id (dimension_id),
  CONSTRAINT course_eval_weight_ibfk_1 FOREIGN KEY (course_id) REFERENCES course (id),
  CONSTRAINT course_eval_weight_ibfk_2 FOREIGN KEY (dimension_id) REFERENCES eval_dimension (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 10. 评价成绩表
-- ============================================================================
CREATE TABLE eval_score (
  id BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  dimension_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  score DECIMAL(5,2) DEFAULT NULL,
  eval_date DATE DEFAULT NULL,
  semester_id BIGINT DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY student_id (student_id),
  KEY course_id (course_id),
  KEY dimension_id (dimension_id),
  KEY teacher_id (teacher_id),
  CONSTRAINT eval_score_ibfk_1 FOREIGN KEY (student_id) REFERENCES sys_user (id),
  CONSTRAINT eval_score_ibfk_2 FOREIGN KEY (course_id) REFERENCES course (id),
  CONSTRAINT eval_score_ibfk_3 FOREIGN KEY (dimension_id) REFERENCES eval_dimension (id),
  CONSTRAINT eval_score_ibfk_4 FOREIGN KEY (teacher_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 11. 教师评语表
-- ============================================================================
CREATE TABLE eval_comment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  comment TEXT DEFAULT NULL,
  ai_comment TEXT DEFAULT NULL,
  semester_id BIGINT DEFAULT NULL,
  is_published TINYINT(1) DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY student_id (student_id),
  KEY course_id (course_id),
  KEY teacher_id (teacher_id),
  CONSTRAINT eval_comment_ibfk_1 FOREIGN KEY (student_id) REFERENCES sys_user (id),
  CONSTRAINT eval_comment_ibfk_2 FOREIGN KEY (course_id) REFERENCES course (id),
  CONSTRAINT eval_comment_ibfk_3 FOREIGN KEY (teacher_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 12. 学生自评表
-- ============================================================================
CREATE TABLE self_evaluation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  dimension_id BIGINT NOT NULL,
  score DECIMAL(5,2) DEFAULT NULL,
  comment TEXT DEFAULT NULL,
  semester_id BIGINT DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY student_id (student_id),
  KEY course_id (course_id),
  KEY dimension_id (dimension_id),
  CONSTRAINT self_evaluation_ibfk_1 FOREIGN KEY (student_id) REFERENCES sys_user (id),
  CONSTRAINT self_evaluation_ibfk_2 FOREIGN KEY (course_id) REFERENCES course (id),
  CONSTRAINT self_evaluation_ibfk_3 FOREIGN KEY (dimension_id) REFERENCES eval_dimension (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 13. 申诉表
-- ============================================================================
CREATE TABLE appeal (
  id BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  reply TEXT DEFAULT NULL,
  status ENUM('PENDING','REPLIED','CLOSED') DEFAULT 'PENDING',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY student_id (student_id),
  KEY course_id (course_id),
  KEY teacher_id (teacher_id),
  CONSTRAINT appeal_ibfk_1 FOREIGN KEY (student_id) REFERENCES sys_user (id),
  CONSTRAINT appeal_ibfk_2 FOREIGN KEY (course_id) REFERENCES course (id),
  CONSTRAINT appeal_ibfk_3 FOREIGN KEY (teacher_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 14. 公告表
-- ============================================================================
CREATE TABLE announcement (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  content TEXT DEFAULT NULL,
  publisher_id BIGINT DEFAULT NULL,
  is_top TINYINT(1) DEFAULT 0,
  status TINYINT(1) DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY publisher_id (publisher_id),
  CONSTRAINT announcement_ibfk_1 FOREIGN KEY (publisher_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 15. 轮播图表
-- ============================================================================
CREATE TABLE carousel (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) DEFAULT NULL,
  image_url VARCHAR(500) NOT NULL,
  link_url VARCHAR(500) DEFAULT NULL,
  sort_order INT DEFAULT 0,
  status TINYINT(1) DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 16. 预警规则表
-- ============================================================================
CREATE TABLE alert_rule (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  rule_type ENUM('SCORE_LOW','HOMEWORK_MISSING','EVAL_DELAY') NOT NULL,
  threshold DECIMAL(5,2) DEFAULT NULL,
  is_enabled TINYINT(1) DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 17. 预警记录表
-- ============================================================================
CREATE TABLE alert_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  rule_id BIGINT DEFAULT NULL,
  user_id BIGINT DEFAULT NULL,
  course_id BIGINT DEFAULT NULL,
  message TEXT DEFAULT NULL,
  is_read TINYINT(1) DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY rule_id (rule_id),
  KEY user_id (user_id),
  CONSTRAINT alert_record_ibfk_1 FOREIGN KEY (rule_id) REFERENCES alert_rule (id),
  CONSTRAINT alert_record_ibfk_2 FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 初始数据
-- ============================================================================

-- 系统配置
INSERT INTO sys_config (config_key, config_value, remark) VALUES
  ('system_name',     '学生学习过程评价系统', '系统名称'),
  ('system_logo',     '',                     '系统Logo图片URL'),
  ('eval_period',     'SEMESTER',             '评价周期: WEEK/MONTH/SEMESTER'),
  ('eval_levels',     '优秀,良好,中等,合格,不合格', '评价等级'),
  ('backup_enabled',  '1',                    '是否启用自动备份');

-- 学期
INSERT INTO semester (name, start_date, end_date, is_current) VALUES
  ('2025-2026学年第二学期', '2026-02-01', '2026-07-15', 1);

-- 用户
--   admin123 → $2b$10$hrh229ScwJa8jLnu8EGSXOIBmWXHGxjRQp3HD2sRH8LrmyazPG7y.
--   123456   → $2b$10$hLR2EEXoy8gt5E0wIxVCZujuSPW0V0Jc.kEyp/0NakLZ8YprqWdy2
INSERT INTO sys_user (username, password, real_name, role, college, major, class_name, student_no) VALUES
  ('admin',    '$2b$10$hrh229ScwJa8jLnu8EGSXOIBmWXHGxjRQp3HD2sRH8LrmyazPG7y.', '系统管理员', 'ADMIN',   NULL,         NULL,               NULL,       NULL),
  ('teacher1', '$2b$10$hLR2EEXoy8gt5E0wIxVCZujuSPW0V0Jc.kEyp/0NakLZ8YprqWdy2', '张老师',     'TEACHER', '计算机学院', NULL,               NULL,       NULL),
  ('teacher2', '$2b$10$hLR2EEXoy8gt5E0wIxVCZujuSPW0V0Jc.kEyp/0NakLZ8YprqWdy2', '李老师',     'TEACHER', '数学学院',   NULL,               NULL,       NULL),
  ('stu001',   '$2b$10$hLR2EEXoy8gt5E0wIxVCZujuSPW0V0Jc.kEyp/0NakLZ8YprqWdy2', '王明',       'STUDENT', '计算机学院', '计算机科学与技术', '计科2301', '2023001'),
  ('stu002',   '$2b$10$hLR2EEXoy8gt5E0wIxVCZujuSPW0V0Jc.kEyp/0NakLZ8YprqWdy2', '刘芳',       'STUDENT', '计算机学院', '计算机科学与技术', '计科2301', '2023002'),
  ('stu003',   '$2b$10$hLR2EEXoy8gt5E0wIxVCZujuSPW0V0Jc.kEyp/0NakLZ8YprqWdy2', '赵强',       'STUDENT', '计算机学院', '软件工程',         '软工2301', '2023003'),
  ('stu004',   '$2b$10$hLR2EEXoy8gt5E0wIxVCZujuSPW0V0Jc.kEyp/0NakLZ8YprqWdy2', '陈静',       'STUDENT', '计算机学院', '软件工程',         '软工2301', '2023004');

-- 课程
INSERT INTO course (name, code, description, semester_id) VALUES
  ('数据结构',   'CS201',   '学习常用数据结构与算法',   1),
  ('高等数学',   'MATH101', '微积分、线性代数等',       1),
  ('数据库原理', 'CS301',   '关系型数据库设计与应用',   1);

-- 班级
INSERT INTO class_info (name, college, major, grade) VALUES
  ('计科2301', '计算机学院', '计算机科学与技术', '2023'),
  ('软工2301', '计算机学院', '软件工程',         '2023');

-- 教师-课程-班级绑定
--   teacher1(id=2): 数据结构→计科2301, 数据库原理→软工2301
--   teacher2(id=3): 高等数学→计科2301, 高等数学→软工2301
INSERT INTO teacher_course_class (teacher_id, course_id, class_id) VALUES
  (2, 1, 1),
  (2, 3, 2),
  (3, 2, 1),
  (3, 2, 2);

-- 学生-班级关联
--   stu001(id=4), stu002(id=5) → 计科2301(id=1)
--   stu003(id=6), stu004(id=7) → 软工2301(id=2)
INSERT INTO student_class (student_id, class_id) VALUES
  (4, 1),
  (5, 1),
  (6, 2),
  (7, 2);

-- 评价维度
INSERT INTO eval_dimension (name, description, sort_order) VALUES
  ('考勤',     '课堂出勤情况',     1),
  ('作业',     '课后作业完成情况', 2),
  ('实验',     '实验课表现',       3),
  ('测验',     '课堂测验成绩',     4),
  ('课堂参与', '课堂互动与参与度', 5);

-- 课程评价权重
--   数据结构: 考勤15% 作业25% 实验25% 测验20% 课堂参与15%
--   高等数学: 考勤10% 作业30% 实验20% 测验30% 课堂参与10%
INSERT INTO course_eval_weight (course_id, dimension_id, weight) VALUES
  (1, 1, 15.00), (1, 2, 25.00), (1, 3, 25.00), (1, 4, 20.00), (1, 5, 15.00),
  (2, 1, 10.00), (2, 2, 30.00), (2, 3, 20.00), (2, 4, 30.00), (2, 5, 10.00);

-- 评价成绩示例（teacher1 给 stu001、stu002 的数据结构评分）
INSERT INTO eval_score (student_id, course_id, dimension_id, teacher_id, score, eval_date, semester_id) VALUES
  (4, 1, 1, 2, 92.00, '2026-02-20', 1),
  (4, 1, 2, 2, 85.00, '2026-02-20', 1),
  (4, 1, 3, 2, 88.00, '2026-02-20', 1),
  (4, 1, 4, 2, 90.00, '2026-02-20', 1),
  (4, 1, 5, 2, 78.00, '2026-02-20', 1),
  (5, 1, 1, 2, 88.00, '2026-02-20', 1),
  (5, 1, 2, 2, 75.00, '2026-02-20', 1),
  (5, 1, 3, 2, 92.00, '2026-02-20', 1),
  (5, 1, 4, 2, 80.00, '2026-02-20', 1),
  (5, 1, 5, 2, 85.00, '2026-02-20', 1);

-- 教师评语示例
INSERT INTO eval_comment (student_id, course_id, teacher_id, comment, semester_id, is_published) VALUES
  (4, 1, 2, '王明同学在数据结构课程中表现优秀，算法理解能力强，实验完成质量高。建议继续深入学习高级数据结构。', 1, 1),
  (5, 1, 2, '刘芳同学学习认真，实验能力突出，但课堂参与度有待提升，建议积极发言。', 1, 1);

-- 公告
INSERT INTO announcement (title, content, publisher_id, is_top, status) VALUES
  ('2025-2026学年第二学期评价工作通知', '各位老师请在3月底前完成期中评价录入工作，确保评价数据准确完整。', 1, 1, 1),
  ('系统升级通知', '系统将于本周六凌晨进行维护升级，届时暂停服务约2小时。', 1, 0, 1),
  ('评价标准调整说明', '根据教务处最新要求，课堂参与度评分标准已调整，请各位教师参照新标准执行。', 1, 0, 1);

-- 预警规则
INSERT INTO alert_rule (name, rule_type, threshold, is_enabled) VALUES
  ('成绩偏低预警', 'SCORE_LOW',        60.00, 1),
  ('作业未交预警', 'HOMEWORK_MISSING',  1.00, 1),
  ('评价进度滞后', 'EVAL_DELAY',       50.00, 1);
