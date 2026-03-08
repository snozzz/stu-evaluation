<template>
  <div class="scores-page">
    <!-- Top Filters -->
    <div class="light-card filter-section">
      <el-row :gutter="16" type="flex" align="middle">
        <el-col :span="6">
          <div class="filter-label">选择课程</div>
          <el-select
            v-model="selectedCourse"
            placeholder="请选择课程"
            filterable
            clearable
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
            clearable
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
        <el-col :span="12">
          <div class="filter-label">&nbsp;</div>
          <div class="filter-actions">
            <el-upload
              action=""
              :auto-upload="false"
              :show-file-list="false"
              accept=".xlsx,.xls"
              :on-change="handleOneClickImport"
              style="display: inline-block;"
            >
              <el-button
                type="warning"
                icon="el-icon-upload2"
                size="small"
                :disabled="!selectedCourse"
                :loading="importLoading"
              >
                一键导入
              </el-button>
            </el-upload>
            <el-button
              type="success"
              icon="el-icon-check"
              size="small"
              @click="handleSaveAll"
              :loading="saveAllLoading"
              :disabled="modifiedRows.length === 0"
            >
              保存所有修改 ({{ modifiedRows.length }})
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Score Table -->
    <div class="light-card table-section" v-loading="tableLoading" element-loading-background="rgba(255, 255, 255, 0.8)">
      <div class="card-header">
        <span class="card-title">
          成绩评价表
          <span v-if="selectedCourse && selectedClass" class="card-subtitle">
            (共 {{ scoreData.length }} 名学生)
          </span>
        </span>
      </div>

      <el-table
        v-if="selectedCourse && selectedClass"
        :data="scoreData"
        style="width: 100%"
        border
        empty-text="暂无学生数据"
      >
        <el-table-column prop="studentNo" label="学号" width="120" fixed></el-table-column>
        <el-table-column prop="realName" label="姓名" width="100" fixed></el-table-column>
        <el-table-column
          v-for="dim in dimensions"
          :key="dim.id"
          :label="dim.name"
          width="110"
          align="center"
        >
          <template slot-scope="scope">
            <span
              class="score-cell"
              :class="{ modified: isCellModified(scope.row.studentId, dim.id) }"
              @click="openEditDialog(scope.row, dim)"
            >
              {{ getScore(scope.row, dim.id) !== null ? getScore(scope.row, dim.id) : '-' }}
              <i class="el-icon-edit score-edit-icon"></i>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="加权总分" width="110" align="center">
          <template slot-scope="scope">
            <span class="weighted-score">{{ scope.row.weightedTotal || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              class="edit-btn"
              @click="openFullEditDialog(scope.row)"
            >
              <i class="el-icon-edit"></i> 编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-else class="empty-hint">
        <i class="el-icon-info"></i>
        <span>请先选择课程和班级以查看学生成绩</span>
      </div>
    </div>

    <!-- Score Edit Dialog (single dimension) -->
    <el-dialog
      :title="'编辑成绩 - ' + editDialog.dimName"
      :visible.sync="editDialog.visible"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="edit-dialog-content">
        <div class="edit-student-info">
          <span class="edit-label">学生：</span>
          <span class="edit-value">{{ editDialog.studentName }} ({{ editDialog.studentNo }})</span>
        </div>
        <div class="edit-score-row">
          <span class="edit-label">{{ editDialog.dimName }}分数：</span>
          <el-input-number
            v-model="editDialog.score"
            :min="0"
            :max="100"
            :precision="1"
            :step="1"
          ></el-input-number>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialog.visible = false" class="cancel-btn">取 消</el-button>
        <el-button type="warning" @click="handleSingleSave">确 定</el-button>
      </div>
    </el-dialog>

    <!-- Full Edit Dialog (all dimensions) -->
    <el-dialog
      title="编辑学生全部成绩"
      :visible.sync="fullEditDialog.visible"
      width="520px"
      :close-on-click-modal="false"
    >
      <div class="full-edit-content">
        <div class="edit-student-info" style="margin-bottom: 20px;">
          <span class="edit-label">学生：</span>
          <span class="edit-value">{{ fullEditDialog.studentName }} ({{ fullEditDialog.studentNo }})</span>
        </div>
        <el-form label-width="100px">
          <el-form-item v-for="dim in dimensions" :key="dim.id" :label="dim.name">
            <el-input-number
              v-model="fullEditDialog.scores[dim.id]"
              :min="0"
              :max="100"
              :precision="1"
              :step="1"
              style="width: 100%"
            ></el-input-number>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="fullEditDialog.visible = false" class="cancel-btn">取 消</el-button>
        <el-button type="warning" @click="handleFullSave" :loading="fullEditDialog.loading">保存全部</el-button>
      </div>
    </el-dialog>

    <!-- Full Edit Dialog -->
  </div>
</template>

<script>
import {
  getBindings,
  getClassScores,
  saveBatchScores,
  importScores,
  getDimensions,
  getCourseList,
  getClassList,
  getStudentsByClass
} from '../../api'

export default {
  name: 'TeacherScores',
  data() {
    return {
      bindings: [],
      courseList: [],
      classList: [],
      selectedCourse: null,
      selectedClass: null,
      dimensions: [],
      scoreData: [],
      modifiedRows: [],
      tableLoading: false,
      saveAllLoading: false,
      // Edit single dimension dialog
      editDialog: {
        visible: false,
        studentId: null,
        studentName: '',
        studentNo: '',
        dimId: null,
        dimName: '',
        score: 0,
        originalScore: 0
      },
      // Full edit dialog
      fullEditDialog: {
        visible: false,
        studentId: null,
        studentName: '',
        studentNo: '',
        scores: {},
        loading: false
      },
      // Import
      importLoading: false
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
      this.bindings
        .filter(b => b.courseId === this.selectedCourse)
        .forEach(b => classIds.add(b.classId))
      return this.classList.filter(c => classIds.has(c.id))
    }
  },
  created() {
    this.loadInitData()
  },
  methods: {
    async loadInitData() {
      try {
        const userId = this.$store.state.userInfo ? this.$store.state.userInfo.id : null

        const [bindRes, courseRes, classRes, dimRes] = await Promise.all([
          getBindings({ teacherId: userId }),
          getCourseList({ page: 1, size: 1000 }),
          getClassList({ page: 1, size: 1000 }),
          getDimensions()
        ])

        this.bindings = bindRes.data.data || []
        const courseData = courseRes.data.data
        this.courseList = courseData.records || courseData || []
        const classData = classRes.data.data
        this.classList = classData.records || classData || []
        this.dimensions = dimRes.data.data || []
      } catch (e) {
        console.error('Load init data error:', e)
        this.$message.error('初始化数据加载失败')
      }
    },
    handleCourseChange() {
      this.selectedClass = null
      this.scoreData = []
      this.modifiedRows = []
    },
    handleClassChange() {
      if (this.selectedCourse && this.selectedClass) {
        this.loadScores()
      } else {
        this.scoreData = []
        this.modifiedRows = []
      }
    },
    async loadScores() {
      if (!this.selectedCourse || !this.selectedClass) return

      this.tableLoading = true
      this.modifiedRows = []
      try {
        const res = await getClassScores({
          courseId: this.selectedCourse,
          classId: this.selectedClass
        })

        const rawScores = res.data.data || []
        // Group scores by student
        const studentMap = {}
        rawScores.forEach(score => {
          if (!studentMap[score.studentId]) {
            studentMap[score.studentId] = {
              studentId: score.studentId,
              studentNo: '',
              realName: '',
              scores: {},
              weightedTotal: null
            }
          }
          studentMap[score.studentId].scores[score.dimensionId] = score.score
        })

        // Load student details
        try {
          const userRes = await getStudentsByClass(this.selectedClass)
          const users = userRes.data.data || []
          users.forEach(u => {
            if (studentMap[u.id]) {
              studentMap[u.id].studentNo = u.studentNo || ''
              studentMap[u.id].realName = u.realName || u.nickname || ''
            }
          })
        } catch (e) {
          console.warn('Could not load student info:', e)
        }

        this.scoreData = Object.values(studentMap)

        // Calculate weighted totals
        this.scoreData.forEach(student => {
          this.calcWeightedTotal(student)
        })
      } catch (e) {
        console.error('Load scores error:', e)
        this.$message.error('加载成绩数据失败')
      } finally {
        this.tableLoading = false
      }
    },
    getScore(row, dimId) {
      if (row.scores && row.scores[dimId] !== undefined && row.scores[dimId] !== null) {
        return Number(row.scores[dimId])
      }
      return null
    },
    isCellModified(studentId, dimId) {
      return this.modifiedRows.some(
        m => m.studentId === studentId && m.dimensionId === dimId
      )
    },
    openEditDialog(row, dim) {
      this.editDialog.studentId = row.studentId
      this.editDialog.studentName = row.realName
      this.editDialog.studentNo = row.studentNo
      this.editDialog.dimId = dim.id
      this.editDialog.dimName = dim.name
      const currentScore = this.getScore(row, dim.id)
      this.editDialog.score = currentScore !== null ? currentScore : 0
      this.editDialog.originalScore = currentScore
      this.editDialog.visible = true
    },
    handleSingleSave() {
      const { studentId, dimId, score } = this.editDialog
      // Update local data
      const student = this.scoreData.find(s => s.studentId === studentId)
      if (student) {
        this.$set(student.scores, dimId, score)
        this.calcWeightedTotal(student)
      }

      // Track modification
      const existingIdx = this.modifiedRows.findIndex(
        m => m.studentId === studentId && m.dimensionId === dimId
      )
      const modEntry = {
        studentId: studentId,
        courseId: this.selectedCourse,
        dimensionId: dimId,
        teacherId: this.$store.state.userInfo ? this.$store.state.userInfo.id : null,
        score: score
      }
      if (existingIdx >= 0) {
        this.$set(this.modifiedRows, existingIdx, modEntry)
      } else {
        this.modifiedRows.push(modEntry)
      }

      this.editDialog.visible = false
      this.$message.success('已修改，点击"保存所有修改"提交到服务器')
    },
    openFullEditDialog(row) {
      this.fullEditDialog.studentId = row.studentId
      this.fullEditDialog.studentName = row.realName
      this.fullEditDialog.studentNo = row.studentNo
      const scores = {}
      this.dimensions.forEach(dim => {
        const val = this.getScore(row, dim.id)
        scores[dim.id] = val !== null ? val : 0
      })
      this.fullEditDialog.scores = scores
      this.fullEditDialog.loading = false
      this.fullEditDialog.visible = true
    },
    handleFullSave() {
      const { studentId, scores } = this.fullEditDialog
      const student = this.scoreData.find(s => s.studentId === studentId)
      const teacherId = this.$store.state.userInfo ? this.$store.state.userInfo.id : null

      this.dimensions.forEach(dim => {
        const newScore = scores[dim.id]
        if (student) {
          this.$set(student.scores, dim.id, newScore)
        }
        const existingIdx = this.modifiedRows.findIndex(
          m => m.studentId === studentId && m.dimensionId === dim.id
        )
        const modEntry = {
          studentId: studentId,
          courseId: this.selectedCourse,
          dimensionId: dim.id,
          teacherId: teacherId,
          score: newScore
        }
        if (existingIdx >= 0) {
          this.$set(this.modifiedRows, existingIdx, modEntry)
        } else {
          this.modifiedRows.push(modEntry)
        }
      })

      if (student) {
        this.calcWeightedTotal(student)
      }

      this.fullEditDialog.visible = false
      this.$message.success('已修改，点击"保存所有修改"提交到服务器')
    },
    async handleSaveAll() {
      if (this.modifiedRows.length === 0) return

      this.saveAllLoading = true
      try {
        const scores = this.modifiedRows.map(m => ({
          studentId: m.studentId,
          courseId: m.courseId,
          dimensionId: m.dimensionId,
          teacherId: m.teacherId,
          score: m.score,
          evalDate: new Date().toISOString()
        }))

        await saveBatchScores(scores)
        this.$message.success('所有修改已保存成功')
        this.modifiedRows = []
        this.loadScores()
      } catch (e) {
        this.$message.error('批量保存失败')
      } finally {
        this.saveAllLoading = false
      }
    },
    calcWeightedTotal(student) {
      // Simple average if no weights configured
      let total = 0
      let count = 0
      this.dimensions.forEach(dim => {
        const val = student.scores[dim.id]
        if (val !== undefined && val !== null) {
          total += Number(val)
          count++
        }
      })
      student.weightedTotal = count > 0 ? (total / count).toFixed(1) : null
    },
    async handleOneClickImport(file) {
      if (!this.selectedCourse) {
        this.$message.warning('请先选择课程')
        return
      }

      this.importLoading = true
      try {
        const teacherId = this.$store.state.userInfo ? this.$store.state.userInfo.id : null
        const formData = new FormData()
        formData.append('file', file.raw)
        formData.append('courseId', this.selectedCourse)
        formData.append('teacherId', teacherId)

        const res = await importScores(formData)
        if (res.data.code === 200) {
          this.$message.success(res.data.message || '导入成功')
          if (this.selectedClass) {
            this.loadScores()
          }
        } else {
          this.$message.error(res.data.message || res.data.msg || '导入失败')
        }
      } catch (e) {
        this.$message.error('导入失败')
      } finally {
        this.importLoading = false
      }
    }
  }
}
</script>

<style scoped>
.scores-page {
  padding: 0;
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.card-subtitle {
  font-size: 13px;
  color: #64748b;
  font-weight: 400;
  margin-left: 8px;
}

/* Filter Section */
.filter-section {
  margin-bottom: 20px;
}

.filter-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 6px;
}

.filter-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* Score Cells */
.score-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
  font-weight: 500;
}

.score-cell:hover {
  background: rgba(97, 191, 173, 0.15);
}

.score-cell.modified {
  color: #61BFAD;
  background: rgba(97, 191, 173, 0.1);
}

.score-edit-icon {
  font-size: 12px;
  color: #d1d5db;
  opacity: 0;
  transition: opacity 0.2s;
}

.score-cell:hover .score-edit-icon {
  opacity: 1;
  color: #61BFAD;
}

.weighted-score {
  font-weight: 700;
  color: #61BFAD;
  font-size: 15px;
}

.edit-btn {
  color: #61BFAD !important;
}

.edit-btn:hover {
  color: #4ea899 !important;
}

/* Empty Hint */
.empty-hint {
  text-align: center;
  padding: 60px 20px;
  color: #64748b;
  font-size: 14px;
}

.empty-hint i {
  font-size: 32px;
  display: block;
  margin-bottom: 12px;
  color: #d1d5db;
}

/* Edit Dialog */
.edit-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.edit-student-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.edit-label {
  color: #64748b;
  font-size: 14px;
}

.edit-value {
  color: #2c3e50;
  font-weight: 500;
}

.edit-score-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Import Content */
.import-content {
  color: #2c3e50;
}

.import-tips {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 20px;
  font-size: 13px;
  color: #64748b;
}

.import-tips p {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.import-tips ul {
  margin: 0;
  padding-left: 20px;
}

.import-tips li {
  line-height: 1.8;
}

.import-tips i {
  color: #61BFAD;
  margin-right: 4px;
}

.cancel-btn {
  background: transparent;
  border-color: #d1d5db;
  color: #64748b;
}

.cancel-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  color: #2c3e50;
}

.dialog-footer {
  text-align: right;
}
</style>
