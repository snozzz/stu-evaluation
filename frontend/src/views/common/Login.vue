<template>
  <div class="login-page">
    <!-- Animated background decorations -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="circle circle-4"></div>
      <div class="circle circle-5"></div>
    </div>

    <!-- Login Card -->
    <div class="login-card">
      <!-- Logo & Title -->
      <div class="card-header">
        <div class="logo-icon">
          <i class="el-icon-s-data"></i>
        </div>
        <h1 class="title">学生学习过程评价系统</h1>
        <p class="subtitle">Student Learning Evaluation System</p>
      </div>

      <!-- Form -->
      <el-form
        ref="loginForm"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.native.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
            class="dark-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            show-password
            class="dark-input"
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) return
        this.loading = true
        this.$store.dispatch('login', this.form).then(() => {
          const role = this.$store.state.userInfo.role
          const routeMap = {
            ADMIN: '/admin/dashboard',
            TEACHER: '/teacher/dashboard',
            STUDENT: '/student/dashboard'
          }
          const target = routeMap[role] || '/login'
          this.$router.push(target)
          this.$message.success('登录成功')
        }).catch(err => {
          this.$message.error(err.message || '登录失败，请检查用户名和密码')
        }).finally(() => {
          this.loading = false
        })
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f0f1a 0%, #1a1a2e 50%, #16213e 100%);
  position: relative;
  overflow: hidden;
}

/* Background floating circles */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.07;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: #10b981;
  top: -100px;
  right: -100px;
  animation: float-1 20s ease-in-out infinite;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: #f59e0b;
  bottom: -80px;
  left: -80px;
  animation: float-2 25s ease-in-out infinite;
}

.circle-3 {
  width: 200px;
  height: 200px;
  background: #f472b6;
  top: 50%;
  left: 10%;
  animation: float-3 18s ease-in-out infinite;
}

.circle-4 {
  width: 150px;
  height: 150px;
  background: #10b981;
  bottom: 20%;
  right: 15%;
  animation: float-4 22s ease-in-out infinite;
}

.circle-5 {
  width: 100px;
  height: 100px;
  background: #f59e0b;
  top: 20%;
  left: 40%;
  animation: float-5 15s ease-in-out infinite;
}

@keyframes float-1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-60px, 40px) scale(1.1); }
  66% { transform: translate(30px, -20px) scale(0.95); }
}

@keyframes float-2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(50px, -30px) scale(1.05); }
  66% { transform: translate(-40px, 50px) scale(0.9); }
}

@keyframes float-3 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(80px, -60px); }
}

@keyframes float-4 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-50px, 40px); }
}

@keyframes float-5 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(40px, 30px) scale(1.2); }
}

/* Login Card - Glass morphism */
.login-card {
  width: 420px;
  padding: 48px 40px 36px;
  background: rgba(30, 41, 59, 0.65);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(51, 65, 85, 0.6);
  border-radius: 20px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  position: relative;
  z-index: 10;
}

/* Card Header */
.card-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #10b981, #059669);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-bottom: 16px;
  box-shadow: 0 4px 16px rgba(16, 185, 129, 0.3);
}

.title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px;
  background: linear-gradient(135deg, #10b981, #34d399, #f59e0b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

.subtitle {
  font-size: 13px;
  color: #64748b;
  margin: 0;
  letter-spacing: 0.5px;
}

/* Form */
.login-form {
  width: 100%;
}

.login-form >>> .el-form-item {
  margin-bottom: 22px;
}

/* Dark input styling */
.login-form >>> .el-input__inner {
  background: rgba(15, 15, 26, 0.6);
  border: 1px solid #334155;
  border-radius: 10px;
  color: #e2e8f0;
  height: 46px;
  font-size: 14px;
  padding-left: 40px;
  transition: all 0.3s ease;
}

.login-form >>> .el-input__inner::placeholder {
  color: #64748b;
}

.login-form >>> .el-input__inner:focus {
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
}

.login-form >>> .el-input__prefix {
  left: 12px;
  color: #64748b;
  font-size: 16px;
  display: flex;
  align-items: center;
}

.login-form >>> .el-input__suffix {
  color: #64748b;
}

.login-form >>> .el-input__suffix .el-input__icon {
  color: #64748b;
}

/* Form item error states */
.login-form >>> .el-form-item__error {
  color: #f87171;
  font-size: 12px;
  padding-top: 4px;
}

.login-form >>> .el-form-item.is-error .el-input__inner {
  border-color: #ef4444;
}

/* Login Button */
.login-btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #10b981, #059669) !important;
  color: #fff !important;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(16, 185, 129, 0.3);
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgba(16, 185, 129, 0.4);
  background: linear-gradient(135deg, #34d399, #10b981) !important;
}

.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}
</style>
