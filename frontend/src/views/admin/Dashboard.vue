<template>
  <div class="dashboard-shell">
    <div class="dashboard-container">
      <el-row :gutter="20" class="stat-row">
        <el-col :span="6">
          <div class="stat-card" style="background: linear-gradient(135deg, #7b5ce7, #6544d7);">
            <div class="stat-icon"><i class="el-icon-reading"></i></div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalCourses || 0 }}</div>
              <div class="stat-label">全校课程数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card" style="background: linear-gradient(135deg, #5e8bff, #4a73f0);">
            <div class="stat-icon"><i class="el-icon-data-line"></i></div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.evaluatedClassCount || 0 }}</div>
              <div class="stat-label">已评价班级数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card" style="background: linear-gradient(135deg, #ec5db7, #d94aa2);">
            <div class="stat-icon"><i class="el-icon-time"></i></div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.pendingClassCount || 0 }}</div>
              <div class="stat-label">待评价班级数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card" style="background: linear-gradient(135deg, #fb7b90, #ef5e77);">
            <div class="stat-icon"><i class="el-icon-warning-outline"></i></div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.alertCount || 0 }}</div>
              <div class="stat-label">异常预警数</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="panel-row">
        <el-col :span="14">
          <div class="light-card chart-card">
            <div class="card-header"><span>各课程评价完成率（柱状图）</span></div>
            <div ref="courseCompletionChart" class="chart-container"></div>
          </div>
        </el-col>
        <el-col :span="10">
          <div class="light-card chart-card">
            <div class="card-header"><span>各学院/专业评价进度占比（饼图）</span></div>
            <div ref="progressPieChart" class="chart-container"></div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="panel-row">
        <el-col :span="14">
          <div class="light-card announcement-card">
            <div class="card-header"><span>最新公告</span></div>
            <div class="announcement-list">
              <div v-for="item in announcements" :key="item.id" class="announcement-item">
                <div class="announcement-title">
                  <el-tag v-if="item.isTop === 1" size="mini" type="danger" effect="dark" style="margin-right: 8px;">置顶</el-tag>
                  {{ item.title }}
                </div>
                <div class="announcement-time">{{ item.createTime }}</div>
              </div>
              <div v-if="announcements.length === 0" class="empty-text">暂无公告</div>
            </div>
          </div>
        </el-col>
        <el-col :span="10">
          <div class="light-card progress-card">
            <div class="card-header"><span>全局评价完成率</span></div>
            <el-progress
              :percentage="Number((stats.evalCompletionRate || 0).toFixed ? (stats.evalCompletionRate || 0).toFixed(1) : stats.evalCompletionRate || 0)"
              :stroke-width="18"
              color="#7b5ce7"
            ></el-progress>
            <div class="progress-meta">
              <div class="meta-item">
                <span class="meta-label">已评价班级</span>
                <span class="meta-value">{{ stats.evaluatedClassCount || 0 }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">待评价班级</span>
                <span class="meta-value">{{ stats.pendingClassCount || 0 }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">异常预警</span>
                <span class="meta-value danger">{{ stats.alertCount || 0 }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
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
        courseEvalStats: [],
        majorProgressStats: [],
        evaluatedClassCount: 0,
        pendingClassCount: 0,
        alertCount: 0
      },
      announcements: [],
      carousels: [],
      courseChart: null,
      pieChart: null
    }
  },
  mounted() {
    this.fetchStats()
    this.fetchAnnouncements()
    this.fetchCarousels()
  },
  beforeDestroy() {
    this.disposeCharts()
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    handleResize() {
      this.courseChart && this.courseChart.resize()
      this.pieChart && this.pieChart.resize()
    },
    disposeCharts() {
      if (this.courseChart) {
        this.courseChart.dispose()
        this.courseChart = null
      }
      if (this.pieChart) {
        this.pieChart.dispose()
        this.pieChart = null
      }
    },
    async fetchStats() {
      try {
        const res = await getDashboardStats()
        if (res.data.code === 200) {
          this.stats = res.data.data
          this.initCharts()
        }
      } catch (e) {
        console.error('获取统计数据失败', e)
        this.initCharts()
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
    initCharts() {
      this.disposeCharts()
      window.removeEventListener('resize', this.handleResize)

      // 1) 课程评价完成率
      if (this.$refs.courseCompletionChart) {
        this.courseChart = echarts.init(this.$refs.courseCompletionChart)
        const rows = (this.stats.courseEvalStats || []).slice().sort((a, b) => (b.completionRate || 0) - (a.completionRate || 0))
        const names = rows.map(r => r.courseName || '')
        const rates = rows.map(r => Number((r.completionRate || 0).toFixed ? (r.completionRate || 0).toFixed(1) : r.completionRate || 0))
        this.courseChart.setOption({
          backgroundColor: 'transparent',
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            backgroundColor: '#ffffff',
            borderColor: '#e5e7eb',
            textStyle: { color: '#2c3e50' },
            formatter: params => {
              const p = params && params[0] ? params[0] : null
              if (!p) return ''
              const row = rows[p.dataIndex] || {}
              return `${row.courseName}<br/>完成率: ${p.value}%<br/>已评价班级: ${row.evaluatedClasses || 0}/${row.totalClasses || 0}`
            }
          },
          grid: { left: '3%', right: '4%', bottom: '6%', containLabel: true },
          xAxis: {
            type: 'category',
            data: names,
            axisLine: { lineStyle: { color: '#d1d5db' } },
            axisLabel: { color: '#64748b', rotate: names.length > 8 ? 28 : 0 }
          },
          yAxis: {
            type: 'value',
            min: 0,
            max: 100,
            axisLabel: { color: '#64748b', formatter: '{value}%' },
            splitLine: { lineStyle: { color: '#e5e7eb' } }
          },
          series: [{
            name: '完成率',
            type: 'bar',
            data: rates,
            barWidth: '48%',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#8b6cf0' },
                { offset: 1, color: '#6b4ee0' }
              ]),
              borderRadius: [6, 6, 0, 0]
            },
            label: { show: true, position: 'top', formatter: '{c}%', color: '#6b4ee0' }
          }]
        })
      }

      // 2) 学院/专业进度占比
      if (this.$refs.progressPieChart) {
        this.pieChart = echarts.init(this.$refs.progressPieChart)
        const source = this.stats.majorProgressStats || []
        const pieData = source.map(x => ({
          name: x.status,
          value: x.count || 0
        }))
        this.pieChart.setOption({
          backgroundColor: 'transparent',
          tooltip: {
            trigger: 'item',
            backgroundColor: '#ffffff',
            borderColor: '#e5e7eb',
            textStyle: { color: '#2c3e50' },
            formatter: '{b}: {c} ({d}%)'
          },
          legend: {
            bottom: 0,
            textStyle: { color: '#64748b' }
          },
          series: [{
            type: 'pie',
            radius: ['40%', '68%'],
            center: ['50%', '45%'],
            data: pieData,
            itemStyle: {
              borderColor: '#fff',
              borderWidth: 2
            },
            color: ['#7b5ce7', '#ec5db7', '#7e8aa7'],
            label: { color: '#2c3e50' }
          }]
        })
      }

      window.addEventListener('resize', this.handleResize)
    }
  }
}
</script>

<style scoped>
.dashboard-shell {
  background: #f4f5fb;
  margin: -20px;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.dashboard-container {
  max-width: 1440px;
  margin: 0 auto;
}

.stat-row {
  margin-bottom: 20px;
}

.panel-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 14px;
  padding: 24px;
  display: flex;
  align-items: center;
  color: #fff;
  min-height: 104px;
  box-shadow: 0 8px 20px rgba(60, 37, 121, 0.18);
  transition: transform 0.25s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
}

.stat-icon {
  font-size: 34px;
  margin-right: 16px;
  opacity: 0.9;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.15;
}

.stat-label {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 4px;
}

.light-card {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #ecebfa;
  padding: 20px;
  box-shadow: 0 6px 20px rgba(93, 71, 167, 0.08);
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #2d2a4a;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #efedf9;
}

.chart-card .chart-container {
  width: 100%;
  height: 320px;
}

.announcement-card {
  min-height: 392px;
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
  border-bottom: 1px solid #efedf9;
  transition: background 0.2s;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-item:hover {
  background: rgba(123, 92, 231, 0.04);
}

.announcement-title {
  color: #2d2a4a;
  font-size: 14px;
  flex: 1;
}

.announcement-time {
  color: #7a7598;
  font-size: 12px;
  margin-left: 20px;
  white-space: nowrap;
}

.empty-text {
  color: #7a7598;
  text-align: center;
  padding: 40px 0;
  font-size: 14px;
}

.progress-card {
  min-height: 392px;
}

.progress-meta {
  margin-top: 20px;
  display: grid;
  grid-template-columns: 1fr;
  row-gap: 12px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f7f5ff;
  border-radius: 10px;
  padding: 10px 12px;
}

.meta-label {
  color: #6b6690;
  font-size: 13px;
}

.meta-value {
  color: #433f66;
  font-size: 18px;
  font-weight: 700;
}

.meta-value.danger {
  color: #d33d6a;
}
</style>
