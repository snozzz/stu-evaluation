<template>
  <div class="scores-container">
    <!-- Course Select -->
    <div class="dark-card">
      <div class="card-header">
        <span>我的成绩</span>
        <el-select
          v-model="selectedCourseId"
          placeholder="选择课程查看成绩"
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
    </div>

    <div v-if="selectedCourseId">
      <!-- Score Overview Table -->
      <div class="dark-card">
        <div class="card-header">
          <span>成绩总览</span>
          <div class="weighted-total" v-if="weightedTotal !== null">
            加权总分: <span class="total-score">{{ weightedTotal }}</span>
          </div>
        </div>
        <el-table
          :data="scoreTableData"
          style="width: 100%"
          class="dark-table"
          :header-cell-style="{ background: '#0f172a', color: '#94a3b8', borderColor: '#334155' }"
          :cell-style="{ background: '#1e293b', color: '#e2e8f0', borderColor: '#334155' }"
          empty-text="暂无成绩数据"
        >
          <el-table-column prop="dimensionName" label="维度" min-width="120"></el-table-column>
          <el-table-column prop="teacherScore" label="教师评分" width="120" align="center">
            <template slot-scope="scope">
              <span :class="getScoreClass(scope.row.teacherScore)">
                {{ scope.row.teacherScore != null ? scope.row.teacherScore : '--' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="selfScore" label="自评分数" width="120" align="center">
            <template slot-scope="scope">
              <span :class="getScoreClass(scope.row.selfScore)">
                {{ scope.row.selfScore != null ? scope.row.selfScore : '--' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="weight" label="权重" width="100" align="center">
            <template slot-scope="scope">
              {{ scope.row.weight != null ? scope.row.weight.toFixed(0) + '%' : '--' }}
            </template>
          </el-table-column>
          <el-table-column prop="weightedScore" label="加权得分" width="120" align="center">
            <template slot-scope="scope">
              <span class="weighted-score">
                {{ scope.row.weightedScore != null ? scope.row.weightedScore.toFixed(1) : '--' }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Teacher Comment Section -->
      <div class="dark-card" v-if="teacherComment">
        <div class="card-header">
          <span>教师评语</span>
        </div>
        <div class="comment-quote">
          <div class="quote-bar"></div>
          <div class="quote-content">
            <p class="comment-text">{{ teacherComment.content || teacherComment.comment }}</p>
            <p class="comment-meta">
              <span>{{ teacherComment.teacherName || '教师' }}</span>
              <span class="comment-time">{{ teacherComment.createTime }}</span>
            </p>
          </div>
        </div>
      </div>

      <!-- AI Summary Section -->
      <div class="dark-card" v-if="aiSummary">
        <div class="card-header">
          <span><i class="el-icon-cpu" style="margin-right: 6px;"></i>AI 综合评价</span>
        </div>
        <div class="ai-summary-box">
          <div class="ai-summary-content">{{ aiSummary }}</div>
        </div>
      </div>

      <!-- Bar Chart -->
      <div class="dark-card">
        <div class="card-header">
          <span>教师评分 vs 自评分数对比</span>
        </div>
        <div ref="barChart" class="chart-container"></div>
      </div>
    </div>

    <div v-else class="dark-card empty-state">
      <i class="el-icon-document-checked" style="font-size: 48px; color: #334155;"></i>
      <p>请选择课程查看成绩详情</p>
    </div>
  </div>
</template>

<script>
import { getStudentScores, getSelfEvals, getComments, getDimensions, getWeights, getWeightedScore, getCourseList } from '../../api'
import * as echarts from 'echarts'

export default {
  name: 'StudentScores',
  data() {
    return {
      courses: [],
      selectedCourseId: '',
      dimensions: [],
      teacherScores: [],
      selfEvals: [],
      weights: [],
      teacherComment: null,
      aiSummary: '',
      weightedTotal: null,
      barChart: null
    }
  },
  computed: {
    userId() {
      return this.$store.state.userInfo.id
    },
    scoreTableData() {
      return this.dimensions.map(dim => {
        const tScore = this.teacherScores.find(
          s => s.dimensionId === dim.id || s.dimensionName === (dim.name || dim.dimensionName)
        )
        const sEval = this.selfEvals.find(
          s => s.dimensionId === dim.id || s.dimensionName === (dim.name || dim.dimensionName)
        )
        const w = this.weights.find(
          w => w.dimensionId === dim.id
        )
        const teacherScore = tScore ? (tScore.score || tScore.teacherScore) : null
        const selfScore = sEval ? (sEval.score || sEval.selfScore) : null
        const weight = w ? Number(w.weight) : null
        const baseScore = teacherScore != null ? Number(teacherScore) : 0
        const weightedScore = weight != null && teacherScore != null ? baseScore * weight / 100 : null

        return {
          dimensionId: dim.id,
          dimensionName: dim.name || dim.dimensionName,
          teacherScore,
          selfScore,
          weight,
          weightedScore
        }
      })
    }
  },
  mounted() {
    this.fetchCourses()
  },
  beforeDestroy() {
    if (this.barChart) {
      this.barChart.dispose()
    }
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    handleResize() {
      if (this.barChart) {
        this.barChart.resize()
      }
    },
    getScoreClass(score) {
      if (score == null) return ''
      if (score >= 90) return 'score-excellent'
      if (score >= 75) return 'score-good'
      if (score >= 60) return 'score-pass'
      return 'score-fail'
    },
    async fetchCourses() {
      try {
        // Get all scores for this student to find which courses they have
        const res = await getStudentScores({ studentId: this.userId })
        if (res.data.code === 200) {
          const data = res.data.data
          if (Array.isArray(data)) {
            const courseIds = new Set()
            data.forEach(item => {
              if (item.courseId) courseIds.add(item.courseId)
            })
            // Fetch course list to get course names
            try {
              const courseRes = await getCourseList({ page: 1, size: 1000 })
              const cd = courseRes.data.data
              const allCourses = cd.records || cd || []
              this.courses = allCourses.filter(c => courseIds.has(c.id))
            } catch (e) {
              // Fallback: use courseId as name
              this.courses = Array.from(courseIds).map(id => ({ id, name: '课程' + id }))
            }
          } else if (data && data.courses) {
            this.courses = data.courses
          }
        }
      } catch (e) {
        console.error('获取课程列表失败', e)
      }
    },
    async handleCourseChange(courseId) {
      try {
        const [dimRes, scoreRes, selfRes, commentRes, weightRes] = await Promise.all([
          getDimensions(),
          getStudentScores({ courseId, studentId: this.userId }),
          getSelfEvals({ courseId, studentId: this.userId }),
          getComments({ courseId, studentId: this.userId }),
          getWeights(courseId)
        ])

        if (dimRes.data.code === 200) {
          this.dimensions = dimRes.data.data || []
        }
        if (scoreRes.data.code === 200) {
          const sd = scoreRes.data.data
          this.teacherScores = Array.isArray(sd) ? sd : (sd.scores || [])
        }
        if (selfRes.data.code === 200) {
          const se = selfRes.data.data
          this.selfEvals = Array.isArray(se) ? se : (se.records || [])
        }
        if (commentRes.data.code === 200) {
          const cd = commentRes.data.data
          if (Array.isArray(cd) && cd.length > 0) {
            this.teacherComment = cd[0]
            this.aiSummary = cd[0].aiSummary || ''
          } else if (cd && !Array.isArray(cd)) {
            this.teacherComment = cd
            this.aiSummary = cd.aiSummary || ''
          } else {
            this.teacherComment = null
            this.aiSummary = ''
          }
        }
        if (weightRes.data.code === 200) {
          this.weights = weightRes.data.data || []
        }

        this.fetchWeightedTotal(courseId)
        this.$nextTick(() => {
          this.initBarChart()
        })
      } catch (e) {
        console.error('获取成绩数据失败', e)
        this.$message.error('获取成绩数据失败')
      }
    },
    async fetchWeightedTotal(courseId) {
      try {
        const res = await getWeightedScore(this.userId, courseId)
        if (res.data.code === 200) {
          this.weightedTotal = res.data.data != null
            ? Number(res.data.data).toFixed(1)
            : null
        }
      } catch (e) {
        console.error('获取加权总分失败', e)
        // Calculate locally as fallback
        const total = this.scoreTableData.reduce((sum, row) => {
          return sum + (row.weightedScore || 0)
        }, 0)
        this.weightedTotal = total > 0 ? total.toFixed(1) : null
      }
    },
    initBarChart() {
      if (!this.$refs.barChart) return
      if (this.barChart) {
        this.barChart.dispose()
      }
      this.barChart = echarts.init(this.$refs.barChart)

      const dimNames = this.scoreTableData.map(d => d.dimensionName)
      const teacherValues = this.scoreTableData.map(d => d.teacherScore || 0)
      const selfValues = this.scoreTableData.map(d => d.selfScore || 0)

      if (dimNames.length === 0) {
        this.barChart.setOption({
          backgroundColor: 'transparent',
          title: {
            text: '暂无数据',
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
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: '#1e293b',
          borderColor: '#334155',
          textStyle: { color: '#e2e8f0' }
        },
        legend: {
          data: ['教师评分', '自评分数'],
          textStyle: { color: '#94a3b8' },
          top: 10
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: 50,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dimNames,
          axisLine: { lineStyle: { color: '#334155' } },
          axisLabel: {
            color: '#94a3b8',
            rotate: dimNames.length > 5 ? 30 : 0,
            fontSize: 12
          }
        },
        yAxis: {
          type: 'value',
          max: 100,
          axisLine: { lineStyle: { color: '#334155' } },
          axisLabel: { color: '#94a3b8' },
          splitLine: { lineStyle: { color: '#334155' } }
        },
        series: [
          {
            name: '教师评分',
            type: 'bar',
            barWidth: '30%',
            data: teacherValues,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#ec4899' },
                { offset: 1, color: '#be185d' }
              ]),
              borderRadius: [4, 4, 0, 0]
            }
          },
          {
            name: '自评分数',
            type: 'bar',
            barWidth: '30%',
            data: selfValues,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#8b5cf6' },
                { offset: 1, color: '#6d28d9' }
              ]),
              borderRadius: [4, 4, 0, 0]
            }
          }
        ]
      }

      this.barChart.setOption(option)
      window.addEventListener('resize', this.handleResize)
    }
  }
}
</script>

<style scoped>
.scores-container {
  /* container */
}

.dark-card {
  background: #1e293b;
  border-radius: 12px;
  border: 1px solid #334155;
  padding: 20px;
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #e2e8f0;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #334155;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.course-select {
  width: 220px;
}

.course-select >>> .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}

.course-select >>> .el-input__inner:focus {
  border-color: #ec4899;
}

.weighted-total {
  font-size: 14px;
  color: #94a3b8;
}

.total-score {
  font-size: 20px;
  font-weight: 700;
  color: #ec4899;
  margin-left: 4px;
}

/* Dark Table */
.dark-table >>> .el-table__empty-text {
  color: #64748b;
}

.dark-table >>> .el-table__body-wrapper {
  background: #1e293b;
}

.dark-table >>> .el-table--enable-row-hover .el-table__body tr:hover > td {
  background: #263348 !important;
}

.dark-table >>> .el-table::before {
  background-color: #334155;
}

.score-excellent {
  color: #10b981;
  font-weight: 600;
}

.score-good {
  color: #3b82f6;
  font-weight: 600;
}

.score-pass {
  color: #f59e0b;
  font-weight: 600;
}

.score-fail {
  color: #ef4444;
  font-weight: 600;
}

.weighted-score {
  color: #ec4899;
  font-weight: 600;
}

/* Comment Quote */
.comment-quote {
  display: flex;
  background: rgba(236, 72, 153, 0.05);
  border-radius: 8px;
  padding: 16px 20px;
  margin-top: 4px;
}

.quote-bar {
  width: 4px;
  background: linear-gradient(180deg, #ec4899, #be185d);
  border-radius: 4px;
  margin-right: 16px;
  flex-shrink: 0;
}

.quote-content {
  flex: 1;
}

.comment-text {
  color: #e2e8f0;
  font-size: 14px;
  line-height: 1.7;
  margin: 0 0 10px 0;
}

.comment-meta {
  color: #64748b;
  font-size: 12px;
  margin: 0;
  display: flex;
  gap: 16px;
}

.comment-time {
  color: #475569;
}

/* AI Summary */
.ai-summary-box {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.06), rgba(139, 92, 246, 0.06));
  border: 1px solid rgba(236, 72, 153, 0.15);
  border-radius: 8px;
  padding: 16px 20px;
}

.ai-summary-content {
  color: #cbd5e1;
  font-size: 14px;
  line-height: 1.8;
}

/* Chart */
.chart-container {
  width: 100%;
  height: 380px;
}

/* Empty State */
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
