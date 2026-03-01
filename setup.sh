#!/bin/bash
# ============================================================
# 学生学习过程评价系统 - 环境搭建与部署脚本
# Student Learning Process Evaluation System - Setup & Deploy
#
# 用途：记录系统从零搭建到运行的所有命令，便于迁移复现
# 前置要求：Java 8, Maven 3.x, Node.js 16+, MySQL/MariaDB, Python(bcrypt)
# ============================================================

# ----------------------------------------------------------
# 第一步：启动数据库服务（MariaDB / MySQL）
# 如果是 Linux 系统，将 brew 命令替换为 systemctl start mariadb
# ----------------------------------------------------------
brew services start mariadb

# ----------------------------------------------------------
# 第二步：激活 Python 虚拟环境（用于生成 bcrypt 密码哈希）
# pyenv virtualenv 名称为 cry，如迁移环境需先创建：
#   pyenv virtualenv 3.x.x cry
#   pip install bcrypt
# ----------------------------------------------------------
eval "$(pyenv init -)"
eval "$(pyenv virtualenv-init -)"
pyenv activate cry

# ----------------------------------------------------------
# 第三步：使用 Python bcrypt 库生成管理员默认密码的哈希值
# 明文密码：admin123
# BCrypt rounds=10，生成的哈希会写入数据库 sys_user 表
# Spring Security 的 BCryptPasswordEncoder 兼容此格式
# ----------------------------------------------------------
ADMIN_HASH=$(python3 -c "
import bcrypt
password = b'admin123'
hashed = bcrypt.hashpw(password, bcrypt.gensalt(rounds=10))
print(hashed.decode())
")
echo "生成的管理员密码哈希: $ADMIN_HASH"

# ----------------------------------------------------------
# 第四步：初始化数据库（创建库、表、插入默认数据）
# 使用 mysql 客户端执行，用户名按实际环境修改
# 密码哈希通过变量注入，避免硬编码
# ----------------------------------------------------------
mysql -u snoz <<EOSQL
DROP DATABASE IF EXISTS stu_evaluation;
CREATE DATABASE stu_evaluation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE stu_evaluation;

-- 此处省略建表语句（已在项目初始化时执行，详见 effect.txt 中的数据库设计）
-- 仅更新管理员密码哈希
UPDATE sys_user SET password='${ADMIN_HASH}' WHERE username='admin';
EOSQL

# ----------------------------------------------------------
# 第五步：编译并启动 Spring Boot 后端
# 端口：8088（见 application.yml）
# 首次运行会下载 Maven 依赖，耗时较长
# ----------------------------------------------------------
cd backend
mvn clean compile -q
# 启动后端（后台运行，日志输出到 backend.log）
nohup mvn spring-boot:run > ../backend.log 2>&1 &
echo "后端启动中，日志: backend.log, 端口: 8088"
cd ..

# ----------------------------------------------------------
# 第六步：安装前端依赖并启动 Vue 开发服务器
# 端口：9090（见 vue.config.js）
# 如果 npm install 慢，可配置淘宝镜像：
#   npm config set registry https://registry.npmmirror.com
# ----------------------------------------------------------
cd frontend
npm install
# 启动前端（后台运行，日志输出到 frontend.log）
nohup npm run serve > ../frontend.log 2>&1 &
echo "前端启动中，日志: frontend.log, 端口: 9090"
cd ..

echo "============================================"
echo "系统部署完成！"
echo "前端地址: http://localhost:9090"
echo "后端地址: http://localhost:8088"
echo "管理员账号: admin / admin123"
echo "============================================"
