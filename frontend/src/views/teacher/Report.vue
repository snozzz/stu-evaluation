<template>
  <div class="report-page">
    <!-- Filters -->
    <div class="light-card filter-section">
      <el-row :gutter="16" type="flex" align="middle">
        <el-col :span="6">
          <div class="filter-label">选择课程</div>
          <el-select
            v-model="selectedCourse"
            placeholder="请选择课程"
            filterable
            style="width: 100%"
            @change="handleCourseChange"
          >
            <el-option
              v-for="course in courseOptions"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            ></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <div class="filter-label">选择班级</div>
          <el-select
            v-model="selectedClass"
            placeholder="请选择班级"
            filterable
            style="width: 100%"
            @change="handleClassChange"
          >
            <el-option
              v-for="cls in classOptions"
              :key="cls.id"
              :label="cls.name"
              :value="cls.id"
            ></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <div class="filter-label">查看学生（可选）</div>
          <el-select
            v-model="selectedStudent"
            placeholder="全班概览"
            filterable
            clearable
            style="width: 100%"
            @change="handleStudentChange"
          >
            <el-option
              v-for="stu in studentList"
              :key="stu.studentId"
              :label="stu.realName + ' (' + stu.studentNo + ')'"
              :value="stu.studentId"
            ></el-option>
          </el-select>
        </el-col>
      </el-row>
    </div>

    <!-- Class Overview (no student selected) -->
    <div v-if="selectedCourse && selectedClass && !selectedStudent && scoreData.length > 0">
      <el-row :gutter="20">
        <!-- Pie: Class average by dimension -->
        <el-col :span="12">
          <div class="light-card chart-card">
            <div class="card-header"><span>班级各维度平均分分布</span></div>
            <div ref="classPieChart" class="chart-box"></div>
          </div>
        </el-col>
        <!-- Bar: Each student's weighted total -->
        <el-col :span="12">
          <div class="light-card chart-card">
            <div class="card-header"><span>学生加权总分排名</span></div>
            <div ref="classBarChart" class="chart-box"></div>
          </div>
        </el-col>
      </el-row>

      <!-- Bar: Grouped bar - each dimension, all students -->
      <div class="light-card chart-card">
        <div class="card-header"><span>各维度成绩对比</span></div>
        <div ref="classDimBarChart" class="chart-box-wide"></div>
      </div>

      <!-- Stats Summary Cards -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <div class="mini-stat">
            <div class="mini-stat-value">{{ classSummary.studentCount }}</div>
            <div class="mini-stat-label">学生人数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="mini-stat">
            <div class="mini-stat-value">{{ classSummary.avgScore }}</div>
            <div class="mini-stat-label">班级均分</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="mini-stat">
            <div class="mini-stat-value">{{ classSummary.maxScore }}</div>
            <div class="mini-stat-label">最高分</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="mini-stat">
            <div class="mini-stat-value">{{ classSummary.minScore }}</div>
            <div class="mini-stat-label">最低分</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Student Individual (student selected) -->
    <div v-if="selectedStudent && studentDetail">
      <el-row :gutter="20">
        <!-- Radar: Student dimensions -->
        <el-col :span="12">
          <div class="light-card chart-card">
            <div class="card-header"><span>{{ studentDetail.realName }} - 能力雷达图</span></div>
            <div ref="studentRadarChart" class="chart-box"></div>
          </div>
        </el-col>
        <!-- Bar: Student vs class average -->
        <el-col :span="12">
          <div class="light-card chart-card">
            <div class="card-header"><span>个人成绩 vs 班级平均</span></div>
            <div ref="studentVsClassChart" class="chart-box"></div>
          </div>
        </el-col>
      </el-row>

      <!-- Score Detail Table -->
      <div class="light-card">
        <div class="card-header"><span>{{ studentDetail.realName }} 成绩明细</span></div>
        <el-table
          :data="studentScoreTable"
          style="width: 100%"
        >
          <el-table-column prop="dimName" label="评价维度" min-width="120"></el-table-column>
          <el-table-column prop="score" label="得分" width="100" align="center">
            <template slot-scope="scope">
              <span :class="getScoreClass(scope.row.score)">{{ scope.row.score != null ? scope.row.score : '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="classAvg" label="班级均分" width="100" align="center">
            <template slot-scope="scope">
              <span style="color: #64748b;">{{ scope.row.classAvg }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="diff" label="差值" width="100" align="center">
            <template slot-scope="scope">
              <span :style="{ color: scope.row.diff >= 0 ? '#61BFAD' : '#ef4444', fontWeight: 600 }">
                {{ scope.row.diff >= 0 ? '+' : '' }}{{ scope.row.diff }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="rank" label="班级排名" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.rank <= 3 ? 'warning' : 'info'" size="small" effect="dark">
                第{{ scope.row.rank }}名
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="!selectedCourse || !selectedClass" class="light-card empty-state">
      <i class="el-icon-pie-chart" style="font-size: 48px; color: #e5e7eb;"></i>
      <p>请选择课程和班级查看可视化报表</p>
    </div>
    <div v-if="selectedCourse && selectedClass && scoreData.length === 0 && !selectedStudent" class="light-card empty-state">
      <i class="el-icon-warning-outline" style="font-size: 48px; color: #e5e7eb;"></i>
      <p>该班级暂无成绩数据</p>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getBindings, getClassScores, getDimensions, getCourseList, getClassList, getStudentsByClass, getWeights } from '../../api'

export default {
  name: 'TeacherReport',
  data() {
    return {
      bindings: [],
      courseList: [],
      classList: [],
      dimensions: [],
      weights: [],
      selectedCourse: null,
      selectedClass: null,
      selectedStudent: null,
      scoreData: [],
      studentList: [],
      charts: []
    }
  },
  computed: {
    courseOptions() {
      const courseIds = new Set()
      this.bindings.forEach(b => courseIds.add(b.courseId))
      return this.courseList.filter(c => courseIds.has(c.id))
    },
    classOptions() {
      if (!this.selectedCourse) return []
      const classIds = new Set()
      this.bindings.filter(b => b.courseId === this.selectedCourse).forEach(b => classIds.add(b.classId))
      return this.classList.filter(c => classIds.has(c.id))
    },
    classSummary() {
      if (this.scoreData.length === 0) return { studentCount: 0, avgScore: '--', maxScore: '--', minScore: '--' }
      const totals = this.scoreData.map(s => {
        let sum = 0, cnt = 0
        this.dimensions.forEach(d => {
          const v = s.scores[d.id]
          if (v != null) { sum += Number(v); cnt++ }
        })
        return cnt > 0 ? sum / cnt : 0
      })
      const avg = totals.reduce((a, b) => a + b, 0) / totals.length
      return {
        studentCount: this.scoreData.length,
        avgScore: avg.toFixed(1),
        maxScore: Math.max(...totals).toFixed(1),
        minScore: Math.min(...totals).toFixed(1)
      }
    },
    studentDetail() {
      if (!this.selectedStudent) return null
      return this.scoreData.find(s => s.studentId === this.selectedStudent)
    },
    studentScoreTable() {
      if (!this.studentDetail) return []
      return this.dimensions.map(dim => {
        const score = this.studentDetail.scores[dim.id] != null ? Number(this.studentDetail.scores[dim.id]) : null
        // class average for this dimension
        let sum = 0, cnt = 0
        this.scoreData.forEach(s => {
          const v = s.scores[dim.id]
          if (v != null) { sum += Number(v); cnt++ }
        })
        const classAvg = cnt > 0 ? (sum / cnt).toFixed(1) : '--'
        const diff = score != null && classAvg !== '--' ? (score - parseFloat(classAvg)).toFixed(1) : 0
        // rank
        const allScores = this.scoreData.map(s => s.scores[dim.id] != null ? Number(s.scores[dim.id]) : -1).sort((a, b) => b - a)
        const rank = score != null ? allScores.indexOf(score) + 1 : this.scoreData.length
        return { dimName: dim.name, score, classAvg, diff: Number(diff), rank }
      })
    }
  },
  created() {
    this.loadInitData()
  },
  beforeDestroy() {
    this.disposeCharts()
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    disposeCharts() {
      this.charts.forEach(c => c && c.dispose())
      this.charts = []
    },
    handleResize() {
      this.charts.forEach(c => c && c.resize())
    },
    async loadInitData() {
      const userId = this.$store.state.userInfo ? this.$store.state.userInfo.id : null
      const [bindRes, courseRes, classRes, dimRes] = await Promise.all([
        getBindings({ teacherId: userId }),
        getCourseList({ page: 1, size: 1000 }),
        getClassList({ page: 1, size: 1000 }),
        getDimensions()
      ])
      this.bindings = bindRes.data.data || []
      const cd = courseRes.data.data
      this.courseList = cd.records || cd || []
      const cld = classRes.data.data
      this.classList = cld.records || cld || []
      this.dimensions = dimRes.data.data || []
      window.addEventListener('resize', this.handleResize)
    },
    handleCourseChange() {
      this.selectedClass = null
      this.selectedStudent = null
      this.scoreData = []
      this.studentList = []
      this.disposeCharts()
    },
    async handleClassChange() {
      this.selectedStudent = null
      this.disposeCharts()
      if (this.selectedCourse && this.selectedClass) {
        await this.loadScores()
        this.$nextTick(() => this.renderClassCharts())
      }
    },
    handleStudentChange() {
      this.disposeCharts()
      if (this.selectedStudent) {
        this.$nextTick(() => this.renderStudentCharts())
      } else {
        this.$nextTick(() => this.renderClassCharts())
      }
    },
    async loadScores() {
      const res = await getClassScores({ courseId: this.selectedCourse, classId: this.selectedClass })
      const rawScores = res.data.data || []
      const studentMap = {}
      rawScores.forEach(score => {
        if (!studentMap[score.studentId]) {
          studentMap[score.studentId] = { studentId: score.studentId, studentNo: '', realName: '', scores: {} }
        }
        studentMap[score.studentId].scores[score.dimensionId] = score.score
      })
      try {
        const userRes = await getStudentsByClass(this.selectedClass)
        const users = userRes.data.data || []
        users.forEach(u => {
          if (studentMap[u.id]) {
            studentMap[u.id].studentNo = u.studentNo || ''
            studentMap[u.id].realName = u.realName || u.username || ''
          }
        })
      } catch (e) { /* ignore */ }
      this.scoreData = Object.values(studentMap)
      this.studentList = this.scoreData.map(s => ({ studentId: s.studentId, realName: s.realName, studentNo: s.studentNo }))
      // load weights
      try {
        const wRes = await getWeights(this.selectedCourse)
        this.weights = wRes.data.data || []
      } catch (e) { this.weights = [] }
    },
    getWeightedTotal(student) {
      let total = 0, weightSum = 0
      this.dimensions.forEach(dim => {
        const v = student.scores[dim.id]
        const w = this.weights.find(w => w.dimensionId === dim.id)
        if (v != null && w) {
          total += Number(v) * Number(w.weight) / 100
          weightSum += Number(w.weight)
        } else if (v != null) {
          total += Number(v)
          weightSum += 100 / this.dimensions.length
        }
      })
      return weightSum > 0 ? total : 0
    },
    getDimAvg(dimId) {
      let sum = 0, cnt = 0
      this.scoreData.forEach(s => {
        if (s.scores[dimId] != null) { sum += Number(s.scores[dimId]); cnt++ }
      })
      return cnt > 0 ? sum / cnt : 0
    },
    getScoreClass(score) {
      if (score == null) return ''
      if (score >= 90) return 'score-excellent'
      if (score >= 75) return 'score-good'
      if (score >= 60) return 'score-pass'
      return 'score-fail'
    },
    initChart(ref) {
      const el = this.$refs[ref]
      if (!el) return null
      const chart = echarts.init(el)
      this.charts.push(chart)
      return chart
    },
    renderClassCharts() {
      // Pie: average by dimension
      const pie = this.initChart('classPieChart')
      if (pie) {
        const pieData = this.dimensions.map(dim => ({
          name: dim.name,
          value: Number(this.getDimAvg(dim.id).toFixed(1))
        }))
        const colors = ['#61BFAD', '#61BFAD', '#8b5cf6', '#ef4444', '#3b82f6']
        pie.setOption({
          backgroundColor: 'transparent',
          tooltip: { trigger: 'item', backgroundColor: '#ffffff', borderColor: '#e5e7eb', textStyle: { color: '#2c3e50' }, formatter: '{b}: {c}分 ({d}%)' },
          series: [{
            type: 'pie', radius: ['40%', '70%'], center: ['50%', '50%'],
            itemStyle: { borderRadius: 8, borderColor: '#ffffff', borderWidth: 3 },
            label: { color: '#2c3e50', fontSize: 12, formatter: '{b}\n{c}分' },
            labelLine: { lineStyle: { color: '#d1d5db' } },
            data: pieData.map((d, i) => ({ ...d, itemStyle: { color: colors[i % colors.length] } }))
          }]
        })
      }

      // Bar: student weighted totals ranked
      const bar = this.initChart('classBarChart')
      if (bar) {
        const ranked = this.scoreData.map(s => ({
          name: s.realName || s.studentNo,
          value: Number(this.getWeightedTotal(s).toFixed(1))
        })).sort((a, b) => b.value - a.value)
        bar.setOption({
          backgroundColor: 'transparent',
          tooltip: { trigger: 'axis', backgroundColor: '#ffffff', borderColor: '#e5e7eb', textStyle: { color: '#2c3e50' } },
          grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
          xAxis: {
            type: 'category', data: ranked.map(r => r.name),
            axisLine: { lineStyle: { color: '#e5e7eb' } },
            axisLabel: { color: '#64748b', rotate: ranked.length > 6 ? 30 : 0 }
          },
          yAxis: {
            type: 'value', max: 100,
            axisLine: { lineStyle: { color: '#e5e7eb' } },
            axisLabel: { color: '#64748b' },
            splitLine: { lineStyle: { color: '#e5e7eb' } }
          },
          series: [{
            type: 'bar', data: ranked.map(r => r.value), barWidth: '50%',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#61BFAD' }, { offset: 1, color: '#4ea899' }
              ]),
              borderRadius: [6, 6, 0, 0]
            },
            label: { show: true, position: 'top', color: '#61BFAD', fontSize: 12, formatter: '{c}' }
          }]
        })
      }

      // Grouped bar: dimensions x students
      const dimBar = this.initChart('classDimBarChart')
      if (dimBar) {
        const seriesColors = ['#61BFAD', '#61BFAD', '#8b5cf6', '#ef4444', '#3b82f6', '#ec4899']
        const studentNames = this.scoreData.map(s => s.realName || s.studentNo)
        const series = this.dimensions.map((dim, i) => ({
          name: dim.name, type: 'bar',
          data: this.scoreData.map(s => s.scores[dim.id] != null ? Number(s.scores[dim.id]) : 0),
          itemStyle: { color: seriesColors[i % seriesColors.length], borderRadius: [4, 4, 0, 0] }
        }))
        dimBar.setOption({
          backgroundColor: 'transparent',
          tooltip: { trigger: 'axis', backgroundColor: '#ffffff', borderColor: '#e5e7eb', textStyle: { color: '#2c3e50' } },
          legend: { data: this.dimensions.map(d => d.name), textStyle: { color: '#64748b' }, top: 0 },
          grid: { left: '3%', right: '4%', bottom: '3%', top: 40, containLabel: true },
          xAxis: {
            type: 'category', data: studentNames,
            axisLine: { lineStyle: { color: '#e5e7eb' } },
            axisLabel: { color: '#64748b' }
          },
          yAxis: {
            type: 'value', max: 100,
            axisLine: { lineStyle: { color: '#e5e7eb' } },
            axisLabel: { color: '#64748b' },
            splitLine: { lineStyle: { color: '#e5e7eb' } }
          },
          series
        })
      }
    },
    renderStudentCharts() {
      const stu = this.studentDetail
      if (!stu) return

      // Radar
      const radar = this.initChart('studentRadarChart')
      if (radar) {
        const indicator = this.dimensions.map(d => ({ name: d.name, max: 100 }))
        const stuValues = this.dimensions.map(d => stu.scores[d.id] != null ? Number(stu.scores[d.id]) : 0)
        const avgValues = this.dimensions.map(d => Number(this.getDimAvg(d.id).toFixed(1)))
        radar.setOption({
          backgroundColor: 'transparent',
          tooltip: { backgroundColor: '#ffffff', borderColor: '#e5e7eb', textStyle: { color: '#2c3e50' } },
          legend: { data: [stu.realName, '班级平均'], textStyle: { color: '#64748b' }, top: 0 },
          radar: {
            indicator,
            axisName: { color: '#64748b', fontSize: 12 },
            splitArea: { areaStyle: { color: ['rgba(97,191,173,0.02)', 'rgba(97,191,173,0.05)'] } },
            splitLine: { lineStyle: { color: '#e5e7eb' } },
            axisLine: { lineStyle: { color: '#e5e7eb' } }
          },
          series: [{
            type: 'radar',
            data: [
              { value: stuValues, name: stu.realName, lineStyle: { color: '#61BFAD', width: 2 }, areaStyle: { color: 'rgba(97,191,173,0.2)' }, itemStyle: { color: '#61BFAD' } },
              { value: avgValues, name: '班级平均', lineStyle: { color: '#8b5cf6', width: 2, type: 'dashed' }, areaStyle: { color: 'rgba(139,92,246,0.1)' }, itemStyle: { color: '#8b5cf6' } }
            ]
          }]
        })
      }

      // Bar: student vs class average
      const vsBar = this.initChart('studentVsClassChart')
      if (vsBar) {
        const dimNames = this.dimensions.map(d => d.name)
        const stuScores = this.dimensions.map(d => stu.scores[d.id] != null ? Number(stu.scores[d.id]) : 0)
        const avgScores = this.dimensions.map(d => Number(this.getDimAvg(d.id).toFixed(1)))
        vsBar.setOption({
          backgroundColor: 'transparent',
          tooltip: { trigger: 'axis', backgroundColor: '#ffffff', borderColor: '#e5e7eb', textStyle: { color: '#2c3e50' } },
          legend: { data: [stu.realName, '班级平均'], textStyle: { color: '#64748b' }, top: 0 },
          grid: { left: '3%', right: '4%', bottom: '3%', top: 40, containLabel: true },
          xAxis: {
            type: 'category', data: dimNames,
            axisLine: { lineStyle: { color: '#e5e7eb' } },
            axisLabel: { color: '#64748b' }
          },
          yAxis: {
            type: 'value', max: 100,
            axisLine: { lineStyle: { color: '#e5e7eb' } },
            axisLabel: { color: '#64748b' },
            splitLine: { lineStyle: { color: '#e5e7eb' } }
          },
          series: [
            {
              name: stu.realName, type: 'bar', barWidth: '30%', data: stuScores,
              itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#61BFAD' }, { offset: 1, color: '#4ea899' }]), borderRadius: [4, 4, 0, 0] }
            },
            {
              name: '班级平均', type: 'bar', barWidth: '30%', data: avgScores,
              itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#8b5cf6' }, { offset: 1, color: '#6d28d9' }]), borderRadius: [4, 4, 0, 0] }
            }
          ]
        })
      }
    }
  }
}
</script>

<style scoped>
.report-page { padding: 0; }

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

.filter-section { margin-bottom: 20px; }
.filter-label { font-size: 13px; color: #64748b; margin-bottom: 6px; }

.chart-card { padding: 20px; }
.chart-box { width: 100%; height: 360px; }
.chart-box-wide { width: 100%; height: 400px; }

/* Stats */
.stats-row { margin-bottom: 20px; }
.mini-stat {
  background: linear-gradient(135deg, #f9fafb, #ffffff);
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}
.mini-stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #61BFAD;
}
.mini-stat-label {
  font-size: 13px;
  color: #64748b;
  margin-top: 6px;
}

.score-excellent { color: #61BFAD; font-weight: 600; }
.score-good { color: #3b82f6; font-weight: 600; }
.score-pass { color: #61BFAD; font-weight: 600; }
.score-fail { color: #ef4444; font-weight: 600; }

/* Empty */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}
.empty-state p {
  color: #64748b;
  font-size: 14px;
  margin-top: 16px;
}
</style>
