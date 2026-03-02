<template>
  <div class="self-eval-container">
    <!-- Course Select -->
    <div class="light-card">
      <div class="card-header">
        <span>自我评价</span>
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
    </div>

    <!-- Evaluation Form -->
    <div v-if="selectedCourseId && dimensions.length > 0">
      <div class="light-card" v-for="(dim, index) in dimensions" :key="dim.id">
        <div class="dimension-header">
          <div class="dimension-index">{{ index + 1 }}</div>
          <div class="dimension-info">
            <h4 class="dimension-name">{{ dim.name || dim.dimensionName }}</h4>
            <p class="dimension-desc" v-if="dim.description">{{ dim.description }}</p>
          </div>
          <div class="dimension-score-display" v-if="evalForm[dim.id] && evalForm[dim.id].score > 0">
            <span class="score-value">{{ evalForm[dim.id].score }}</span>
            <span class="score-unit">分</span>
          </div>
        </div>

        <div class="eval-form-item">
          <label class="form-label">评分 (0-100)</label>
          <div class="score-input-row">
            <el-slider
              v-model="evalForm[dim.id].score"
              :min="0"
              :max="100"
              :step="1"
              class="score-slider"
              :format-tooltip="val => val + ' 分'"
            ></el-slider>
            <el-input-number
              v-model="evalForm[dim.id].score"
              :min="0"
              :max="100"
              :step="1"
              size="small"
              class="score-number"
            ></el-input-number>
          </div>
        </div>

        <div class="eval-form-item">
          <label class="form-label">评价说明</label>
          <el-input
            type="textarea"
            v-model="evalForm[dim.id].comment"
            :rows="3"
            placeholder="请输入你对该维度的自我评价说明..."
            class="eval-textarea"
          ></el-input>
        </div>

        <!-- Show existing eval info -->
        <div v-if="existingEvals[dim.id]" class="existing-eval">
          <i class="el-icon-time"></i>
          上次提交: {{ existingEvals[dim.id].updateTime || existingEvals[dim.id].createTime }}
        </div>
      </div>

      <!-- Save Button -->
      <div class="save-section">
        <el-button
          type="primary"
          size="medium"
          :loading="saving"
          @click="handleSave"
          class="save-btn"
        >
          <i class="el-icon-check" v-if="!saving"></i>
          保存自评
        </el-button>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="selectedCourseId && dimensions.length === 0" class="light-card empty-state">
      <i class="el-icon-document" style="font-size: 48px; color: #e5e7eb;"></i>
      <p>该课程暂无评价维度</p>
    </div>

    <div v-else class="light-card empty-state">
      <i class="el-icon-edit-outline" style="font-size: 48px; color: #e5e7eb;"></i>
      <p>请先选择课程开始自我评价</p>
    </div>
  </div>
</template>

<script>
import { getSelfEvals, saveSelfEval, saveBatchSelfEvals, getDimensions, getCourseList } from '../../api'

export default {
  name: 'StudentSelfEval',
  data() {
    return {
      courses: [],
      selectedCourseId: '',
      dimensions: [],
      evalForm: {},
      existingEvals: {},
      saving: false
    }
  },
  computed: {
    userId() {
      return this.$store.state.userInfo.id
    }
  },
  created() {
    this.fetchCourses()
  },
  methods: {
    async fetchCourses() {
      try {
        const res = await getCourseList({ studentId: this.userId })
        if (res.data.code === 200) {
          const data = res.data.data
          this.courses = Array.isArray(data) ? data : (data.records || data.list || [])
        }
      } catch (e) {
        console.error('获取课程列表失败', e)
      }
    },
    async handleCourseChange(courseId) {
      this.evalForm = {}
      this.existingEvals = {}

      try {
        const [dimRes, evalRes] = await Promise.all([
          getDimensions({ courseId }),
          getSelfEvals({ courseId, studentId: this.userId })
        ])

        if (dimRes.data.code === 200) {
          this.dimensions = dimRes.data.data || []
        }

        let existingList = []
        if (evalRes.data.code === 200) {
          const ed = evalRes.data.data
          existingList = Array.isArray(ed) ? ed : (ed.records || [])
        }

        // Build form
        const form = {}
        const existing = {}
        this.dimensions.forEach(dim => {
          const found = existingList.find(
            e => e.dimensionId === dim.id || e.dimensionName === (dim.name || dim.dimensionName)
          )
          form[dim.id] = {
            score: found ? (found.score || found.selfScore || 0) : 0,
            comment: found ? (found.comment || found.content || '') : ''
          }
          if (found) {
            existing[dim.id] = found
          }
        })

        this.$set(this, 'evalForm', form)
        this.existingEvals = existing
      } catch (e) {
        console.error('获取评价数据失败', e)
        this.$message.error('获取评价数据失败')
      }
    },
    async handleSave() {
      // Validate
      const evalList = []
      for (const dim of this.dimensions) {
        const item = this.evalForm[dim.id]
        if (!item || item.score === 0) {
          // Allow zero scores but warn
        }
        evalList.push({
          courseId: this.selectedCourseId,
          studentId: this.userId,
          dimensionId: dim.id,
          score: item ? item.score : 0,
          comment: item ? item.comment : ''
        })
      }

      this.saving = true
      try {
        let res
        if (typeof saveBatchSelfEvals === 'function') {
          res = await saveBatchSelfEvals(evalList)
        } else {
          // Fallback to saving one by one
          for (const evalItem of evalList) {
            res = await saveSelfEval(evalItem)
          }
        }

        if (res && res.data.code === 200) {
          this.$message.success('自评保存成功')
          // Refresh existing data
          this.handleCourseChange(this.selectedCourseId)
        } else {
          this.$message.error(res?.data?.msg || '保存失败')
        }
      } catch (e) {
        console.error('保存自评失败', e)
        this.$message.error('保存自评失败，请稍后重试')
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.self-eval-container {
  /* container */
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
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.course-select {
  width: 220px;
}

/* Dimension Card */
.dimension-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
}

.dimension-index {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #61BFAD, #4a9e8e);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  margin-right: 14px;
  flex-shrink: 0;
}

.dimension-info {
  flex: 1;
}

.dimension-name {
  color: #2c3e50;
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px 0;
  line-height: 32px;
}

.dimension-desc {
  color: #64748b;
  font-size: 13px;
  margin: 0;
  line-height: 1.5;
}

.dimension-score-display {
  display: flex;
  align-items: baseline;
  margin-left: 16px;
  flex-shrink: 0;
}

.score-value {
  font-size: 28px;
  font-weight: 700;
  color: #61BFAD;
}

.score-unit {
  font-size: 13px;
  color: #64748b;
  margin-left: 4px;
}

/* Form */
.eval-form-item {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  color: #64748b;
  font-size: 13px;
  margin-bottom: 8px;
}

.score-input-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.score-slider {
  flex: 1;
}

.score-slider >>> .el-slider__bar {
  background: linear-gradient(90deg, #61BFAD, #7ed4c4);
}

.score-slider >>> .el-slider__button {
  border-color: #61BFAD;
  background: #61BFAD;
}

.score-number {
  width: 120px;
}

/* Existing Eval */
.existing-eval {
  color: #64748b;
  font-size: 12px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #e5e7eb;
}

.existing-eval i {
  margin-right: 4px;
}

/* Save Section */
.save-section {
  text-align: center;
  margin-bottom: 20px;
}

.save-btn {
  background: linear-gradient(135deg, #61BFAD, #4a9e8e) !important;
  border: none !important;
  padding: 12px 48px !important;
  font-size: 15px !important;
  border-radius: 8px !important;
  transition: transform 0.2s, box-shadow 0.2s;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(97, 191, 173, 0.4);
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
