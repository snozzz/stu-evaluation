<template>
  <div class="login-page">
    <!-- Particle canvas background -->
    <canvas ref="particleCanvas" class="particle-canvas"></canvas>

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
        <el-form-item prop="role">
          <el-select
            v-model="form.role"
            placeholder="请选择身份"
            style="width: 100%"
            class="role-select"
          >
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="教师" value="TEACHER"></el-option>
            <el-option label="学生" value="STUDENT"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入学号"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            show-password
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
        password: '',
        role: ''
      },
      rules: {
        role: [
          { required: true, message: '请选择身份', trigger: 'change' }
        ],
        username: [
          { required: true, message: '请输入学号', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      },
      loading: false,
      particles: [],
      animationId: null
    }
  },
  mounted() {
    this.initParticles()
  },
  beforeDestroy() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId)
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
          this.$message.error(err.message || '登录失败，请检查学号和密码')
        }).finally(() => {
          this.loading = false
        })
      })
    },
    initParticles() {
      const canvas = this.$refs.particleCanvas
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      const resize = () => {
        canvas.width = window.innerWidth
        canvas.height = window.innerHeight
      }
      resize()
      window.addEventListener('resize', resize)

      // Create particles
      const count = 60
      this.particles = []
      for (let i = 0; i < count; i++) {
        this.particles.push({
          x: Math.random() * canvas.width,
          y: Math.random() * canvas.height,
          vx: (Math.random() - 0.5) * 0.5,
          vy: (Math.random() - 0.5) * 0.5,
          r: Math.random() * 2 + 1,
          opacity: Math.random() * 0.5 + 0.2
        })
      }

      const animate = () => {
        ctx.clearRect(0, 0, canvas.width, canvas.height)

        // Update and draw particles
        for (let i = 0; i < this.particles.length; i++) {
          const p = this.particles[i]
          p.x += p.vx
          p.y += p.vy

          // Wrap around
          if (p.x < 0) p.x = canvas.width
          if (p.x > canvas.width) p.x = 0
          if (p.y < 0) p.y = canvas.height
          if (p.y > canvas.height) p.y = 0

          ctx.beginPath()
          ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
          ctx.fillStyle = `rgba(97, 191, 173, ${p.opacity})`
          ctx.fill()

          // Draw connections
          for (let j = i + 1; j < this.particles.length; j++) {
            const q = this.particles[j]
            const dx = p.x - q.x
            const dy = p.y - q.y
            const dist = Math.sqrt(dx * dx + dy * dy)
            if (dist < 120) {
              ctx.beginPath()
              ctx.moveTo(p.x, p.y)
              ctx.lineTo(q.x, q.y)
              ctx.strokeStyle = `rgba(97, 191, 173, ${0.15 * (1 - dist / 120)})`
              ctx.lineWidth = 0.5
              ctx.stroke()
            }
          }
        }

        this.animationId = requestAnimationFrame(animate)
      }
      animate()
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
  background: linear-gradient(135deg, #e8f5f1 0%, #f5f5f5 50%, #e0f2ee 100%);
  position: relative;
  overflow: hidden;
}

.particle-canvas {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

/* Login Card */
.login-card {
  width: 420px;
  padding: 48px 40px 36px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
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
  background: linear-gradient(135deg, #61BFAD, #4da89a);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-bottom: 16px;
  box-shadow: 0 4px 16px rgba(97, 191, 173, 0.3);
}

.title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px;
  color: #2c3e50;
  letter-spacing: 1px;
}

.subtitle {
  font-size: 13px;
  color: #9ca3af;
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

.login-form >>> .el-input__inner {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  color: #2c3e50;
  height: 46px;
  font-size: 14px;
  padding-left: 40px;
  transition: all 0.3s ease;
}

.login-form >>> .el-input__inner::placeholder {
  color: #9ca3af;
}

.login-form >>> .el-input__inner:focus {
  border-color: #61BFAD;
  box-shadow: 0 0 0 3px rgba(97, 191, 173, 0.15);
}

.login-form >>> .el-input__prefix {
  left: 12px;
  color: #9ca3af;
  font-size: 16px;
  display: flex;
  align-items: center;
}

.login-form >>> .el-input__suffix {
  color: #9ca3af;
}

.login-form >>> .el-input__suffix .el-input__icon {
  color: #9ca3af;
}

/* Role select */
.role-select >>> .el-input__inner {
  padding-left: 15px;
}

/* Form item error states */
.login-form >>> .el-form-item__error {
  color: #ef4444;
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
  background: linear-gradient(135deg, #61BFAD, #4da89a) !important;
  color: #fff !important;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(97, 191, 173, 0.3);
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgba(97, 191, 173, 0.4);
  background: linear-gradient(135deg, #4da89a, #61BFAD) !important;
}

.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(97, 191, 173, 0.3);
}
</style>
