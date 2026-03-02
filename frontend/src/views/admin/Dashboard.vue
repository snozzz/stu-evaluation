<template>
  <div class="dashboard-container">
    <!-- Carousel -->
    <div class="light-card carousel-card" v-if="carousels.length > 0">
      <el-carousel height="280px" :interval="4000" arrow="hover" indicator-position="outside">
        <el-carousel-item v-for="item in carousels" :key="item.id">
          <a :href="item.linkUrl || 'javascript:;'" class="carousel-link">
            <img :src="item.imageUrl" :alt="item.title" class="carousel-img" />
            <div class="carousel-title" v-if="item.title">{{ item.title }}</div>
          </a>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- Stat Cards -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <div class="stat-card" style="background: linear-gradient(135deg, #61BFAD, #4da89a);">
          <div class="stat-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalStudents || 0 }}</div>
            <div class="stat-label">学生总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: linear-gradient(135deg, #f59e0b, #d97706);">
          <div class="stat-icon">
            <i class="el-icon-s-custom"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalTeachers || 0 }}</div>
            <div class="stat-label">教师总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: linear-gradient(135deg, #8b5cf6, #7c3aed);">
          <div class="stat-icon">
            <i class="el-icon-reading"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalCourses || 0 }}</div>
            <div class="stat-label">课程总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: linear-gradient(135deg, #ef4444, #dc2626);">
          <div class="stat-icon">
            <i class="el-icon-data-analysis"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.evalCompletionRate || 0 }}%</div>
            <div class="stat-label">评价完成率</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Chart Section -->
    <div class="light-card chart-card">
      <div class="card-header">
        <span>评价统计</span>
      </div>
      <div ref="chartRef" class="chart-container"></div>
    </div>

    <!-- Announcements Section -->
    <div class="light-card announcement-card">
      <div class="card-header">
        <span>最新公告</span>
      </div>
      <div class="announcement-list">
        <div
          v-for="item in announcements"
          :key="item.id"
          class="announcement-item"
        >
          <div class="announcement-title">
            <el-tag v-if="item.isTop === 1" size="mini" type="danger" effect="dark" style="margin-right: 8px;">置顶</el-tag>
            {{ item.title }}
          </div>
          <div class="announcement-time">{{ item.createTime }}</div>
        </div>
        <div v-if="announcements.length === 0" class="empty-text">暂无公告</div>
      </div>
    </div>
  </div>
</template>

<script>
import { getDashboardStats, getAnnouncements, getCarousels } from '../../api'
import * as echarts from 'echarts'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: {
        totalStudents: 0,
        totalTeachers: 0,
        totalCourses: 0,
        evalCompletionRate: 0,
        courseEvalStats: []
      },
      announcements: [],
      carousels: [],
      chart: null
    }
  },
  mounted() {
    this.fetchStats()
    this.fetchAnnouncements()
    this.fetchCarousels()
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
    }
  },
  methods: {
    async fetchStats() {
      try {
        const res = await getDashboardStats()
        if (res.data.code === 200) {
          this.stats = res.data.data
          this.initChart()
        }
      } catch (e) {
        console.error('获取统计数据失败', e)
        this.initChart()
      }
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
    initChart() {
      this.chart = echarts.init(this.$refs.chartRef)
      const courseNames = []
      const evalCounts = []
      if (this.stats.courseEvalStats && this.stats.courseEvalStats.length > 0) {
        this.stats.courseEvalStats.forEach(item => {
          courseNames.push(item.courseName || item.name || '')
          evalCounts.push(item.count || item.evalCount || 0)
        })
      } else {
        // Mock data for display
        courseNames.push('高等数学', '大学英语', '数据结构', '操作系统', '计算机网络')
        evalCounts.push(120, 98, 86, 75, 110)
      }
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          backgroundColor: '#ffffff',
          borderColor: '#e5e7eb',
          textStyle: { color: '#2c3e50' }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: courseNames,
          axisLine: { lineStyle: { color: '#d1d5db' } },
          axisLabel: { color: '#64748b' }
        },
        yAxis: {
          type: 'value',
          axisLine: { lineStyle: { color: '#d1d5db' } },
          axisLabel: { color: '#64748b' },
          splitLine: { lineStyle: { color: '#e5e7eb' } }
        },
        series: [{
          name: '评价数量',
          type: 'bar',
          data: evalCounts,
          barWidth: '40%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#61BFAD' },
              { offset: 1, color: '#4da89a' }
            ]),
            borderRadius: [6, 6, 0, 0]
          }
        }]
      }
      this.chart.setOption(option)
      window.addEventListener('resize', () => {
        this.chart && this.chart.resize()
      })
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

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
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
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
}

.chart-card .chart-container {
  width: 100%;
  height: 400px;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.announcement-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #e5e7eb;
  transition: background 0.2s;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-item:hover {
  background: rgba(97, 191, 173, 0.05);
}

.announcement-title {
  color: #2c3e50;
  font-size: 14px;
  flex: 1;
}

.announcement-time {
  color: #64748b;
  font-size: 12px;
  margin-left: 20px;
  white-space: nowrap;
}

.empty-text {
  color: #64748b;
  text-align: center;
  padding: 40px 0;
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
.carousel-title {
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
</style>
