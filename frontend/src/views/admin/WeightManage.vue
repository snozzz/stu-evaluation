<template>
  <div class="page-container">
    <div class="light-card">
      <!-- Course Selector -->
      <div class="top-bar">
        <span class="page-title">课程评价权重配置</span>
        <el-select
          v-model="selectedCourseId"
          placeholder="请选择课程"
          style="width: 300px; margin-left: 20px;"
          filterable
          @change="handleCourseChange"
        >
          <el-option
            v-for="course in courseList"
            :key="course.id"
            :label="course.name + ' (' + course.code + ')'"
            :value="course.id"
          ></el-option>
        </el-select>
      </div>

      <!-- Weight Config -->
      <div v-if="selectedCourseId" class="weight-section">
        <div class="weight-header">
          <span>维度权重设置</span>
          <span :class="['weight-total', totalWeight !== 100 ? 'weight-warning' : 'weight-ok']">
            权重总计: {{ totalWeight }}%
            <span v-if="totalWeight !== 100" class="warning-text">（权重总和应为100%）</span>
          </span>
        </div>

        <div v-if="weightList.length === 0" class="empty-text">
          暂无评价维度，请先在维度管理中添加维度
        </div>

        <div v-else class="weight-list">
          <div v-for="item in weightList" :key="item.dimensionId" class="weight-item">
            <div class="dimension-info">
              <span class="dimension-name">{{ item.dimensionName }}</span>
              <span class="dimension-desc">{{ item.dimensionDesc }}</span>
            </div>
            <div class="weight-input">
              <el-input-number
                v-model="item.weight"
                :min="0"
                :max="100"
                :precision="1"
                :step="5"
                size="medium"
              ></el-input-number>
              <span class="weight-unit">%</span>
            </div>
          </div>
        </div>

        <div class="save-bar" v-if="weightList.length > 0">
          <el-button type="primary" icon="el-icon-check" @click="handleSave" :loading="saving">
            保存权重配置
          </el-button>
        </div>
      </div>

      <div v-else class="empty-text">
        请先选择一门课程
      </div>
    </div>
  </div>
</template>

<script>
import { getCourseList, getWeights, saveWeights, getDimensionList } from '../../api'

export default {
  name: 'WeightManage',
  data() {
    return {
      courseList: [],
      dimensionList: [],
      selectedCourseId: null,
      weightList: [],
      saving: false
    }
  },
  computed: {
    totalWeight() {
      return this.weightList.reduce((sum, item) => sum + (Number(item.weight) || 0), 0)
    }
  },
  mounted() {
    this.fetchCourses()
    this.fetchDimensions()
  },
  methods: {
    async fetchCourses() {
      try {
        const res = await getCourseList({ page: 1, size: 9999 })
        if (res.data.code === 200) {
          this.courseList = res.data.data.records || res.data.data || []
        }
      } catch (e) {
        this.$message.error('获取课程列表失败')
      }
    },
    async fetchDimensions() {
      try {
        const res = await getDimensionList()
        if (res.data.code === 200) {
          this.dimensionList = res.data.data || []
        }
      } catch (e) {
        this.$message.error('获取维度列表失败')
      }
    },
    async handleCourseChange(courseId) {
      if (!courseId) {
        this.weightList = []
        return
      }
      try {
        const res = await getWeights(courseId)
        const existingWeights = res.data.code === 200 ? (res.data.data || []) : []
        // Build weight list from all dimensions, filling in existing weights
        this.weightList = this.dimensionList.map(dim => {
          const existing = existingWeights.find(w => w.dimensionId === dim.id)
          return {
            courseId: courseId,
            dimensionId: dim.id,
            dimensionName: dim.name,
            dimensionDesc: dim.description || '',
            weight: existing ? Number(existing.weight) : 0,
            id: existing ? existing.id : undefined
          }
        })
      } catch (e) {
        this.$message.error('获取权重数据失败')
      }
    },
    async handleSave() {
      if (this.totalWeight !== 100) {
        this.$confirm('权重总和不等于100%，确定要保存吗？', '警告', {
          confirmButtonText: '继续保存',
          cancelButtonText: '返回修改',
          type: 'warning'
        }).then(() => {
          this.doSave()
        }).catch(() => {})
      } else {
        this.doSave()
      }
    },
    async doSave() {
      this.saving = true
      try {
        const payload = this.weightList.map(item => ({
          id: item.id,
          courseId: this.selectedCourseId,
          dimensionId: item.dimensionId,
          weight: item.weight
        }))
        const res = await saveWeights(payload)
        if (res.data.code === 200) {
          this.$message.success('保存成功')
          this.handleCourseChange(this.selectedCourseId)
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      } catch (e) {
        this.$message.error('保存失败')
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.light-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.top-bar {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.weight-section {
  margin-top: 0;
}

.weight-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 15px;
  color: #2c3e50;
  font-weight: 500;
}

.weight-total {
  font-size: 14px;
  font-weight: 600;
}

.weight-ok {
  color: #61BFAD;
}

.weight-warning {
  color: #ef4444;
}

.warning-text {
  font-size: 12px;
  font-weight: 400;
}

.weight-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.weight-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  transition: border-color 0.2s;
}

.weight-item:hover {
  border-color: #61BFAD;
}

.dimension-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dimension-name {
  font-size: 15px;
  color: #2c3e50;
  font-weight: 500;
}

.dimension-desc {
  font-size: 12px;
  color: #64748b;
}

.weight-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.weight-unit {
  color: #94a3b8;
  font-size: 14px;
}

.save-bar {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.empty-text {
  color: #64748b;
  text-align: center;
  padding: 60px 0;
  font-size: 14px;
}
</style>
