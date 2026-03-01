<template>
  <div class="teacher-dashboard">
    <!-- Carousel -->
    <div class="dark-card carousel-card" v-if="carousels.length > 0">
      <el-carousel height="280px" :interval="4000" arrow="hover" indicator-position="outside">
        <el-carousel-item v-for="item in carousels" :key="item.id">
          <a :href="item.linkUrl || 'javascript:;'" class="carousel-link">
            <img :src="item.imageUrl" :alt="item.title" class="carousel-img" />
            <div class="carousel-title" v-if="item.title">{{ item.title }}</div>
          </a>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- Top Stats Row -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card amber-gradient">
          <div class="stat-icon">
            <i class="el-icon-notebook-2"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.courseCount }}</div>
            <div class="stat-label">我的课程数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card green-gradient">
          <div class="stat-icon">
            <i class="el-icon-office-building"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.classCount }}</div>
            <div class="stat-label">我的班级数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card purple-gradient">
          <div class="stat-icon">
            <i class="el-icon-finished"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.evaluatedCount }}</div>
            <div class="stat-label">已评价学生</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card red-gradient">
          <div class="stat-icon">
            <i class="el-icon-chat-line-round"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingFeedback }}</div>
            <div class="stat-label">待处理反馈</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Middle: ECharts Pie Chart -->
    <div class="dark-card chart-section">
      <div class="card-header">
        <span class="card-title">评价维度分布</span>
      </div>
      <div ref="pieChart" class="chart-container"></div>
    </div>

    <!-- Bottom: Recent Appeals/Feedback List -->
    <div class="dark-card feedback-section">
      <div class="card-header">
        <span class="card-title">最近反馈 / 申诉</span>
        <el-button type="text" class="view-all-btn" @click="$router.push('/teacher/appeals')">
          查看全部 <i class="el-icon-arrow-right"></i>
        </el-button>
      </div>
      <el-table
        :data="recentAppeals"
        style="width: 100%"
        class="dark-table"
        :header-cell-style="tableHeaderStyle"
        :cell-style="tableCellStyle"
        empty-text="暂无反馈记录"
      >
        <el-table-column prop="studentName" label="学生姓名" width="120"></el-table-column>
        <el-table-column prop="courseName" label="课程" width="160"></el-table-column>
        <el-table-column prop="content" label="反馈内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag
              :type="statusTagType(scope.row.status)"
              size="small"
              effect="dark"
            >
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getBindings, getTeacherAppeals, getCarousels } from '../../api'

export default {
  name: 'TeacherDashboard',
  data() {
    return {
      stats: {
        courseCount: 0,
        classCount: 0,
        evaluatedCount: 0,
        pendingFeedback: 0
      },
      recentAppeals: [],
      carousels: [],
      chartInstance: null,
      tableHeaderStyle: {
        background: '#162032',
        color: '#94a3b8',
        borderBottom: '1px solid #334155',
        fontWeight: '600'
      },
      tableCellStyle: {
        background: '#1e293b',
        color: '#e2e8f0',
        borderBottom: '1px solid #334155'
      }
    }
  },
  mounted() {
    this.loadData()
    this.fetchCarousels()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
  },
  methods: {
    async loadData() {
      try {
        const userId = this.$store.state.userInfo ? this.$store.state.userInfo.id : null

        // Load bindings for course/class stats
        const bindRes = await getBindings({ teacherId: userId })
        const bindings = bindRes.data.data || []
        const courseIds = new Set()
        const classIds = new Set()
        bindings.forEach(b => {
          if (b.courseId) courseIds.add(b.courseId)
          if (b.classId) classIds.add(b.classId)
        })
        this.stats.courseCount = courseIds.size
        this.stats.classCount = classIds.size

        // Load appeals
        const appealRes = await getTeacherAppeals()
        const appeals = appealRes.data.data || []
        this.recentAppeals = appeals.slice(0, 8)
        this.stats.pendingFeedback = appeals.filter(a => a.status === 'PENDING').length

        // Mock evaluated count (can be replaced with real API)
        this.stats.evaluatedCount = 56
      } catch (e) {
        console.error('Dashboard data load error:', e)
        // Use placeholder data if API fails
        this.stats = { courseCount: 5, classCount: 8, evaluatedCount: 56, pendingFeedback: 3 }
        this.recentAppeals = [
          { studentName: '张三', courseName: '高等数学', content: '成绩有误，请核实', status: 'PENDING', createTime: '2026-02-25 14:30:00' },
          { studentName: '李四', courseName: '大学英语', content: '考勤分数不正确', status: 'REPLIED', createTime: '2026-02-24 09:15:00' },
          { studentName: '王五', courseName: '数据结构', content: '实验成绩缺失', status: 'PENDING', createTime: '2026-02-23 16:45:00' }
        ]
      }
      this.initChart()
    },
    initChart() {
      this.$nextTick(() => {
        if (this.$refs.pieChart) {
          this.chartInstance = echarts.init(this.$refs.pieChart)
          const option = {
            backgroundColor: 'transparent',
            tooltip: {
              trigger: 'item',
              backgroundColor: '#1e293b',
              borderColor: '#334155',
              textStyle: { color: '#e2e8f0' }
            },
            legend: {
              orient: 'vertical',
              right: '5%',
              top: 'center',
              textStyle: { color: '#94a3b8', fontSize: 13 },
              itemWidth: 12,
              itemHeight: 12,
              itemGap: 16
            },
            series: [
              {
                name: '评价维度',
                type: 'pie',
                radius: ['40%', '70%'],
                center: ['40%', '50%'],
                avoidLabelOverlap: true,
                itemStyle: {
                  borderRadius: 8,
                  borderColor: '#1e293b',
                  borderWidth: 3
                },
                label: {
                  show: true,
                  color: '#e2e8f0',
                  fontSize: 12,
                  formatter: '{b}: {d}%'
                },
                labelLine: {
                  lineStyle: { color: '#475569' }
                },
                emphasis: {
                  label: { show: true, fontSize: 14, fontWeight: 'bold' },
                  itemStyle: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                  }
                },
                data: [
                  { value: 25, name: '考勤', itemStyle: { color: '#f59e0b' } },
                  { value: 25, name: '作业', itemStyle: { color: '#10b981' } },
                  { value: 20, name: '实验', itemStyle: { color: '#8b5cf6' } },
                  { value: 15, name: '测验', itemStyle: { color: '#ef4444' } },
                  { value: 15, name: '课堂参与', itemStyle: { color: '#3b82f6' } }
                ]
              }
            ]
          }
          this.chartInstance.setOption(option)
        }
      })
    },
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize()
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
    statusTagType(status) {
      const map = { PENDING: 'warning', REPLIED: 'success', CLOSED: 'info' }
      return map[status] || 'info'
    },
    statusText(status) {
      const map = { PENDING: '待处理', REPLIED: '已回复', CLOSED: '已关闭' }
      return map[status] || status
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const h = String(d.getHours()).padStart(2, '0')
      const min = String(d.getMinutes()).padStart(2, '0')
      return y + '-' + m + '-' + day + ' ' + h + ':' + min
    }
  }
}
</script>

<style scoped>
.teacher-dashboard {
  padding: 0;
}

/* Stats Row */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.amber-gradient {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.green-gradient {
  background: linear-gradient(135deg, #10b981, #059669);
}

.purple-gradient {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.red-gradient {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 4px;
}

/* Dark Card */
.dark-card {
  background: #1e293b;
  border-radius: 12px;
  border: 1px solid #334155;
  padding: 20px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #e2e8f0;
}

.view-all-btn {
  color: #f59e0b !important;
  font-size: 13px;
}

.view-all-btn:hover {
  color: #fbbf24 !important;
}

/* Chart */
.chart-section {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 360px;
}

/* Table Styles */
.dark-table {
  background: transparent !important;
}

.dark-table >>> .el-table__body-wrapper {
  background: #1e293b;
}

.dark-table >>> .el-table__empty-block {
  background: #1e293b;
  color: #64748b;
}

.dark-table >>> .el-table__row:hover > td {
  background: #263348 !important;
}

.dark-table >>> .el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell {
  background: #263348 !important;
}

.dark-table >>> th.el-table__cell {
  background: #162032 !important;
}

.dark-table >>> .el-table__body-wrapper::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.dark-table >>> .el-table__body-wrapper::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 6px;
}

.dark-table >>> .el-table__body-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.dark-table::before {
  display: none;
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
  background: #475569;
}
.carousel-card >>> .el-carousel__indicator.is-active .el-carousel__button {
  background: #f59e0b;
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
