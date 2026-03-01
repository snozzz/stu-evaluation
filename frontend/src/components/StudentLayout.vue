<template>
  <div class="student-layout">
    <!-- Sidebar -->
    <div class="sidebar" :class="{ collapsed: isCollapse }">
      <div class="logo-area">
        <div class="logo-icon">
          <img v-if="logoUrl" :src="logoUrl" class="logo-img" />
          <i v-else class="el-icon-reading"></i>
        </div>
        <span v-show="!isCollapse" class="logo-text">{{ systemName || '学评系统' }}</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="#1a1a2e"
        text-color="#94a3b8"
        active-text-color="#f472b6"
        router
        :collapse="isCollapse"
      >
        <el-menu-item index="/student/dashboard">
          <i class="el-icon-s-home"></i>
          <span slot="title">学习概览</span>
        </el-menu-item>
        <el-menu-item index="/student/scores">
          <i class="el-icon-document-checked"></i>
          <span slot="title">我的成绩</span>
        </el-menu-item>
        <el-menu-item index="/student/selfEval">
          <i class="el-icon-edit-outline"></i>
          <span slot="title">自我评价</span>
        </el-menu-item>
        <el-menu-item index="/student/appeals">
          <i class="el-icon-message"></i>
          <span slot="title">反馈申诉</span>
        </el-menu-item>
        <el-menu-item index="/student/profile">
          <i class="el-icon-user"></i>
          <span slot="title">个人中心</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- Main Area -->
    <div class="main-area">
      <!-- Header -->
      <div class="header-bar">
        <div class="header-left">
          <i
            class="collapse-btn"
            :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
            @click="isCollapse = !isCollapse"
          ></i>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item
              v-for="(item, index) in breadcrumbList"
              :key="index"
              :to="item.path ? { path: item.path } : null"
            >
              {{ item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleDropdown">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ avatarText }}
              </el-avatar>
              <span class="user-name">{{ userName }}</span>
              <i class="el-icon-arrow-down"></i>
            </div>
            <el-dropdown-menu slot="dropdown" class="dark-dropdown">
              <el-dropdown-item command="profile">
                <i class="el-icon-user"></i> 个人中心
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <i class="el-icon-switch-button"></i> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>

      <!-- Content -->
      <div class="content-area">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import { getConfig } from '../api'

export default {
  name: 'StudentLayout',
  data() {
    return {
      isCollapse: false,
      logoUrl: '',
      systemName: ''
    }
  },
  mounted() {
    this.loadSystemConfig()
  },
  computed: {
    activeMenu() {
      return this.$route.path
    },
    userInfo() {
      return this.$store.state.userInfo || {}
    },
    userName() {
      return this.userInfo.realName || this.userInfo.username || '同学'
    },
    avatarText() {
      const name = this.userName
      return name ? name.charAt(0) : 'S'
    },
    breadcrumbList() {
      const matched = this.$route.matched.filter(item => item.meta && item.meta.title)
      const list = [{ name: '首页', path: '/student/dashboard' }]
      matched.forEach(item => {
        if (item.meta.title !== '首页') {
          list.push({ name: item.meta.title, path: item.path })
        }
      })
      return list
    }
  },
  methods: {
    async loadSystemConfig() {
      try {
        const [logoRes, nameRes] = await Promise.all([
          getConfig('system_logo'),
          getConfig('system_name')
        ])
        if (logoRes.data.code === 200 && logoRes.data.data) {
          this.logoUrl = logoRes.data.data.configValue || ''
        }
        if (nameRes.data.code === 200 && nameRes.data.data) {
          this.systemName = nameRes.data.data.configValue || ''
        }
      } catch (e) {
        // silently fall back to defaults
      }
    },
    handleDropdown(command) {
      if (command === 'profile') {
        this.$router.push('/student/profile')
      } else if (command === 'logout') {
        this.$store.dispatch('logout').then(() => {
          this.$router.push('/login')
        })
      }
    }
  }
}
</script>

<style scoped>
.student-layout {
  display: flex;
  height: 100vh;
  background: #0f0f1a;
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 220px;
  min-width: 220px;
  background: #1a1a2e;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #334155;
  transition: width 0.3s ease, min-width 0.3s ease;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px;
  min-width: 64px;
}

.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid #334155;
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f472b6, #ec4899);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  flex-shrink: 0;
  overflow: hidden;
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #f472b6, #fb7185);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  border-right: none !important;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
}

.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 4px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: transparent;
}

/* Override Element UI menu item styles */
.sidebar-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 8px;
  padding-left: 20px !important;
  font-size: 14px;
  transition: all 0.25s ease;
}

.sidebar-menu .el-menu-item i {
  color: inherit;
  margin-right: 8px;
  font-size: 16px;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(244, 114, 182, 0.08) !important;
  color: #e2e8f0 !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: rgba(244, 114, 182, 0.15) !important;
  color: #f472b6 !important;
  font-weight: 600;
  position: relative;
}

.sidebar-menu .el-menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #f472b6;
  border-radius: 0 3px 3px 0;
}

/* Collapsed state */
.sidebar.collapsed .sidebar-menu .el-menu-item {
  margin: 2px 4px;
  padding-left: 0 !important;
  text-align: center;
}

.sidebar.collapsed .sidebar-menu .el-menu-item i {
  margin-right: 0;
}

/* Main Area */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* Header */
.header-bar {
  height: 64px;
  background: #16213e;
  border-bottom: 1px solid #334155;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #f472b6;
}

/* Breadcrumb overrides */
.breadcrumb {
  line-height: 1;
}

.breadcrumb >>> .el-breadcrumb__inner {
  color: #94a3b8 !important;
  font-size: 13px;
}

.breadcrumb >>> .el-breadcrumb__inner a {
  color: #94a3b8 !important;
  font-weight: 400;
}

.breadcrumb >>> .el-breadcrumb__inner a:hover {
  color: #f472b6 !important;
}

.breadcrumb >>> .el-breadcrumb__separator {
  color: #475569 !important;
}

.breadcrumb >>> .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #e2e8f0 !important;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.05);
}

.user-avatar {
  background: linear-gradient(135deg, #f472b6, #ec4899) !important;
  color: #fff !important;
  font-size: 14px !important;
  font-weight: 600;
}

.user-name {
  color: #e2e8f0;
  font-size: 14px;
}

.user-info .el-icon-arrow-down {
  color: #94a3b8;
  font-size: 12px;
  transition: transform 0.2s;
}

/* Content */
.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #0f0f1a;
}

.content-area::-webkit-scrollbar {
  width: 6px;
}

.content-area::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 6px;
}

.content-area::-webkit-scrollbar-track {
  background: transparent;
}
</style>
