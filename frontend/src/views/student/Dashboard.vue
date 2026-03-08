<template>
  <div class="dashboard-container">
    <!-- Carousel -->
    <div class="light-card carousel-card" v-if="carousels.length > 0">
      <el-carousel height="280px" :interval="4000" arrow="hover" indicator-position="outside">
        <el-carousel-item v-for="item in carousels" :key="item.id">
          <a :href="item.linkUrl || 'javascript:;'" class="carousel-link">
            <img :src="item.imageUrl" :alt="item.title" class="carousel-img" />
            <div class="carousel-title-overlay" v-if="item.title">{{ item.title }}</div>
          </a>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- Welcome Banner -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h2 class="welcome-title">欢迎回来, {{ studentName }}</h2>
        <p class="welcome-sub">查看你的学习进度和评价概览</p>
      </div>
      <div class="welcome-decoration">
        <i class="el-icon-reading"></i>
      </div>
    </div>

    <!-- Stat Cards -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <div class="stat-card pink-gradient">
          <div class="stat-icon">
            <i class="el-icon-reading"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.courseCount || 0 }}</div>
            <div class="stat-label">我的课程</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card green-gradient">
          <div class="stat-icon">
            <i class="el-icon-data-analysis"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.avgScore || '--' }}</div>
            <div class="stat-label">平均分</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card amber-gradient">
          <div class="stat-icon">
            <i class="el-icon-edit-outline"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.pendingSelfEval || 0 }}</div>
            <div class="stat-label">待完成自评</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card red-gradient">
          <div class="stat-icon">
            <i class="el-icon-warning-outline"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ alertCount }}</div>
            <div class="stat-label">异常提醒</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Middle Section -->
    <el-row :gutter="20">
      <!-- Radar Chart -->
      <el-col :span="16">
        <div class="light-card">
          <div class="card-header">
            <span>能力维度雷达图</span>
            <el-select
              v-model="selectedCourseId"
              placeholder="选择课程"
              size="small"
              class="course-select"
              @change="handleCourseChange"
            >
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.courseName || course.name"
                :value="course.id"
              ></el-option>
            </el-select>
          </div>
          <div ref="radarChart" class="chart-container"></div>
          <div v-if="!selectedCourseId" class="empty-chart-tip">
            请选择课程查看维度评分
          </div>
        </div>
      </el-col>

      <!-- Announcements & Alerts -->
      <el-col :span="8">
        <div class="light-card">
          <div class="card-header">
            <span>最新公告</span>
          </div>
          <div class="announcement-list">
            <div
              v-for="item in announcements"
              :key="'ann-' + item.id"
              class="announcement-item"
              @click="showAnnouncement(item)"
            >
              <div class="announcement-dot"></div>
              <div class="announcement-content">
                <div class="announcement-title">{{ item.title }}</div>
                <div class="announcement-time">{{ item.createTime }}</div>
              </div>
            </div>
            <div v-if="announcements.length === 0" class="empty-text">暂无公告</div>
          </div>
        </div>

        <div class="dark-card" style="margin-top: 20px;">
          <div class="card-header">
            <span>异常提醒</span>
            <el-badge :value="alertCount" :hidden="alertCount === 0" class="alert-badge">
            </el-badge>
          </div>
          <div class="alert-list">
            <div
              v-for="item in alertRecords"
              :key="'alert-' + item.id"
              class="alert-item"
            >
              <div class="alert-icon">
                <i class="el-icon-warning"></i>
              </div>
              <div class="alert-content">
                <div class="alert-title">{{ item.content || item.message }}</div>
                <div class="alert-time">{{ item.createTime }}</div>
              </div>
            </div>
            <div v-if="alertRecords.length === 0" class="empty-text">暂无异常提醒</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="light-card">
      <div class="card-header">
        <span>个人多课程总分对比（柱状图）</span>
      </div>
      <div ref="courseTotalChart" class="chart-container"></div>
    </div>

    <el-dialog :title="currentAnnouncement.title" :visible.sync="announcementVisible" width="600px" append-to-body>
      <div class="announcement-detail-time">{{ currentAnnouncement.createTime }}</div>
      <div class="announcement-detail-content">{{ currentAnnouncement.content || '暂无内容' }}</div>
      <span slot="footer">
        <el-button @click="announcementVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getDashboardStats, getStudentScores, getDimensions, getAnnouncements, getStudentAlerts, getCarousels, getCourseList } from '../../api'
import * as echarts from 'echarts'

export default {
  name: 'StudentDashboard',
  data() {
    return {
      stats: {
        courseCount: 0,
        avgScore: '--',
        pendingSelfEval: 0
      },
      alertCount: 0,
      alertRecords: [],
      announcements: [],
      carousels: [],
      courses: [],
      selectedCourseId: '',
      dimensions: [],
      scores: [],
      radarChart: null,
      courseTotalChart: null,
      courseTotalData: [],
      announcementVisible: false,
      currentAnnouncement: {}
    }
  },
  computed: {
    userId() {
      return this.$store.state.userInfo.id
    },
    studentName() {
      const info = this.$store.state.userInfo || {}
      return info.realName || info.nickname || '同学'
    }
  },
  mounted() {
    this.fetchStats()
    this.fetchStudentCourses()
    this.fetchAnnouncements()
    this.fetchAlertRecords()
    this.fetchCarousels()
  },
  beforeDestroy() {
    if (this.radarChart) {
      this.radarChart.dispose()
    }
    if (this.courseTotalChart) {
      this.courseTotalChart.dispose()
    }
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    handleResize() {
      if (this.radarChart) {
        this.radarChart.resize()
      }
      if (this.courseTotalChart) {
        this.courseTotalChart.resize()
      }
    },
    async fetchStats() {
      try {
        const res = await getDashboardStats({ userId: this.userId })
        if (res.data.code === 200) {
          const data = res.data.data
          this.stats = {
            courseCount: data.courseCount || data.totalCourses || 0,
            avgScore: data.avgScore != null ? data.avgScore : '--',
            pendingSelfEval: data.pendingSelfEval || 0
          }
        }
      } catch (e) {
        console.error('获取统计数据失败', e)
      }
    },
    async fetchStudentCourses() {
      try {
        // 从学生的成绩记录中提取有哪些课程
        const [scoreRes, courseRes] = await Promise.all([
          getStudentScores({ studentId: this.userId }),
          getCourseList({ page: 1, size: 1000 })
        ])
        const allCourses = []
        if (courseRes.data.code === 200) {
          const d = courseRes.data.data
          allCourses.push(...(d.records || d || []))
        }
        if (scoreRes.data.code === 200) {
          const scores = scoreRes.data.data || []
          this.buildCourseTotalData(scores, allCourses)
          // 提取学生有成绩的courseId
          const courseIdSet = [...new Set(scores.map(s => s.courseId))]
          if (courseIdSet.length > 0) {
            this.courses = allCourses.filter(c => courseIdSet.includes(c.id))
            this.stats.courseCount = this.courses.length
          } else {
            this.courses = allCourses
          }
        } else {
          this.courses = allCourses
        }
        if (this.courses.length > 0 && !this.selectedCourseId) {
          this.selectedCourseId = this.courses[0].id
          this.handleCourseChange(this.selectedCourseId)
        }
        this.$nextTick(() => this.initCourseTotalChart())
      } catch (e) {
        console.error('获取课程列表失败', e)
      }
    },
    buildCourseTotalData(scores, allCourses) {
      const byCourse = {}
      ;(scores || []).forEach(s => {
        if (!s.courseId || s.score == null) return
        if (!byCourse[s.courseId]) byCourse[s.courseId] = []
        byCourse[s.courseId].push(Number(s.score))
      })
      const courseMap = {}
      ;(allCourses || []).forEach(c => { courseMap[c.id] = c.name || c.courseName || ('课程' + c.id) })
      this.courseTotalData = Object.keys(byCourse).map(cid => {
        const arr = byCourse[cid]
        const avg = arr.reduce((a, b) => a + b, 0) / arr.length
        return {
          courseId: Number(cid),
          courseName: courseMap[cid] || ('课程' + cid),
          totalScore: Number(avg.toFixed(1))
        }
      }).sort((a, b) => b.totalScore - a.totalScore)
    },
    initCourseTotalChart() {
      if (!this.$refs.courseTotalChart) return
      if (this.courseTotalChart) {
        this.courseTotalChart.dispose()
      }
      this.courseTotalChart = echarts.init(this.$refs.courseTotalChart)

      const names = this.courseTotalData.map(x => x.courseName)
      const values = this.courseTotalData.map(x => x.totalScore)
      if (names.length === 0) {
        this.courseTotalChart.setOption({
          backgroundColor: 'transparent',
          title: {
            text: '暂无多课程成绩数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#64748b', fontSize: 14 }
          }
        })
        return
      }

      this.courseTotalChart.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: '#ffffff',
          borderColor: '#e5e7eb',
          textStyle: { color: '#2c3e50' }
        },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: names,
          axisLine: { lineStyle: { color: '#e5e7eb' } },
          axisLabel: { color: '#64748b', rotate: names.length > 6 ? 20 : 0 }
        },
        yAxis: {
          type: 'value',
          max: 100,
          axisLine: { lineStyle: { color: '#e5e7eb' } },
          axisLabel: { color: '#64748b' },
          splitLine: { lineStyle: { color: '#e5e7eb' } }
        },
        series: [{
          name: '总分',
          type: 'bar',
          data: values,
          barWidth: '46%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#3b82f6' },
              { offset: 1, color: '#2563eb' }
            ]),
            borderRadius: [6, 6, 0, 0]
          },
          label: { show: true, position: 'top', color: '#2563eb' }
        }]
      })
    },
    showAnnouncement(item) {
      this.currentAnnouncement = item
      this.announcementVisible = true
    },
    async fetchAnnouncements() {
      try {
        const res = await getAnnouncements({ page: 1, size: 5 })
        if (res.data.code === 200) {
          this.announcements = res.data.data.records || res.data.data || []
        }
      } catch (e) {
        console.error('获取公告失败', e)
      }
    },
    async fetchAlertRecords() {
      try {
        const res = await getStudentAlerts(this.userId)
        if (res.data.code === 200) {
          const data = res.data.data || []
          this.alertRecords = Array.isArray(data) ? data : []
          this.alertCount = this.alertRecords.length
        }
      } catch (e) {
        console.error('获取异常提醒失败', e)
        this.alertRecords = []
        this.alertCount = 0
      }
    },
    async fetchCarousels() {
      try {
        const res = await getCarousels()
        if (res.data.code === 200) {
          this.carousels = (res.data.data || []).filter(c => c.status === 1)
        }
      } catch (e) {
        console.error('获取轮播图失败', e)
      }
    },
    async handleCourseChange(courseId) {
      try {
        const [dimRes, scoreRes] = await Promise.all([
          getDimensions({ courseId }),
          getStudentScores({ courseId, studentId: this.userId })
        ])
        if (dimRes.data.code === 200) {
          this.dimensions = dimRes.data.data || []
        }
        if (scoreRes.data.code === 200) {
          this.scores = scoreRes.data.data || []
        }
        this.initRadarChart()
      } catch (e) {
        console.error('获取维度评分失败', e)
      }
    },
    initRadarChart() {
      if (!this.$refs.radarChart) return
      if (this.radarChart) {
        this.radarChart.dispose()
      }
      this.radarChart = echarts.init(this.$refs.radarChart)

      const indicators = this.dimensions.map(d => ({
        name: d.name || d.dimensionName,
        max: 100
      }))

      const scoreValues = this.dimensions.map(d => {
        const found = this.scores.find(
          s => s.dimensionId === d.id || s.dimensionName === d.name
        )
        return found ? (found.score || found.teacherScore || 0) : 0
      })

      if (indicators.length === 0) {
        this.radarChart.setOption({
          backgroundColor: 'transparent',
          title: {
            text: '暂无维度数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#64748b', fontSize: 14 }
          }
        })
        return
      }

      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          backgroundColor: '#ffffff',
          borderColor: '#e5e7eb',
          textStyle: { color: '#2c3e50' }
        },
        radar: {
          indicator: indicators,
          shape: 'polygon',
          splitNumber: 5,
          name: {
            textStyle: {
              color: '#64748b',
              fontSize: 12
            }
          },
          splitLine: {
            lineStyle: { color: '#e5e7eb' }
          },
          splitArea: {
            show: true,
            areaStyle: {
              color: ['rgba(97, 191, 173, 0.02)', 'rgba(97, 191, 173, 0.04)',
                'rgba(97, 191, 173, 0.06)', 'rgba(97, 191, 173, 0.08)',
                'rgba(97, 191, 173, 0.1)']
            }
          },
          axisLine: {
            lineStyle: { color: '#e5e7eb' }
          }
        },
        series: [{
          type: 'radar',
          data: [{
            value: scoreValues,
            name: '我的评分',
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
              color: '#61BFAD',
              width: 2
            },
            areaStyle: {
              color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
                { offset: 0, color: 'rgba(97, 191, 173, 0.3)' },
                { offset: 1, color: 'rgba(97, 191, 173, 0.05)' }
              ])
            },
            itemStyle: {
              color: '#61BFAD',
              borderColor: '#61BFAD',
              borderWidth: 2
            }
          }]
        }]
      }

      this.radarChart.setOption(option)
      window.addEventListener('resize', this.handleResize)
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  /* container */
}

/* Welcome Banner */
.welcome-banner {
  background: linear-gradient(135deg, #61BFAD, #4da89a);
  border-radius: 12px;
  padding: 28px 32px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: -30%;
  right: -5%;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 50%;
}

.welcome-banner::after {
  content: '';
  position: absolute;
  bottom: -40%;
  right: 10%;
  width: 150px;
  height: 150px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.welcome-content {
  position: relative;
  z-index: 1;
}

.welcome-title {
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 6px 0;
}

.welcome-sub {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  margin: 0;
}

.welcome-decoration {
  font-size: 64px;
  color: rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 1;
}

/* Stat Cards */
.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  color: #fff;
  min-height: 100px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.pink-gradient {
  background: linear-gradient(135deg, #61BFAD, #4da89a);
}

.green-gradient {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.amber-gradient {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.red-gradient {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.stat-icon {
  font-size: 40px;
  margin-right: 20px;
  opacity: 0.9;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  opacity: 0.85;
  margin-top: 4px;
}

/* Light Card */
.light-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.course-select {
  width: 180px;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.empty-chart-tip {
  text-align: center;
  color: #64748b;
  padding: 40px 0;
  font-size: 14px;
}

/* Announcements */
.announcement-list {
  display: flex;
  flex-direction: column;
}

.announcement-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
  cursor: pointer;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #61BFAD;
  margin-top: 6px;
  margin-right: 12px;
  flex-shrink: 0;
}

.announcement-content {
  flex: 1;
  min-width: 0;
}

.announcement-title {
  color: #2c3e50;
  font-size: 13px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-time {
  color: #9ca3af;
  font-size: 12px;
  margin-top: 4px;
}

/* Alerts */
.alert-badge {
  margin-left: 8px;
}

.alert-list {
  display: flex;
  flex-direction: column;
}

.alert-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.alert-item:last-child {
  border-bottom: none;
}

.alert-icon {
  color: #ef4444;
  font-size: 18px;
  margin-right: 10px;
  margin-top: 2px;
  flex-shrink: 0;
}

.alert-content {
  flex: 1;
  min-width: 0;
}

.alert-title {
  color: #2c3e50;
  font-size: 13px;
  line-height: 1.5;
}

.alert-time {
  color: #9ca3af;
  font-size: 12px;
  margin-top: 4px;
}

.empty-text {
  color: #9ca3af;
  text-align: center;
  padding: 30px 0;
  font-size: 14px;
}

.carousel-card {
  padding: 0;
  overflow: hidden;
  margin-bottom: 20px;
}
.carousel-card >>> .el-carousel__indicators--outside {
  margin-top: -10px;
}
.carousel-card >>> .el-carousel__indicator .el-carousel__button {
  background: #d1d5db;
}
.carousel-card >>> .el-carousel__indicator.is-active .el-carousel__button {
  background: #61BFAD;
}
.carousel-link {
  display: block;
  width: 100%;
  height: 100%;
  position: relative;
  text-decoration: none;
}
.carousel-img {
  width: 100%;
  height: 280px;
  object-fit: cover;
}
.carousel-title-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 20px;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  color: #fff;
  font-size: 16px;
  font-weight: 500;
}

.announcement-detail-time {
  color: #999;
  font-size: 13px;
  margin-bottom: 16px;
}

.announcement-detail-content {
  color: #333;
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
