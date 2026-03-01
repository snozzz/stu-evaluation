<template>
  <div class="appeals-container">
    <!-- Header -->
    <div class="dark-card">
      <div class="card-header">
        <span>反馈申诉</span>
        <el-button
          type="primary"
          size="small"
          class="add-btn"
          @click="showDialog"
        >
          <i class="el-icon-plus"></i> 提交新申诉
        </el-button>
      </div>

      <!-- Appeals Table -->
      <el-table
        :data="appeals"
        style="width: 100%"
        class="dark-table"
        :header-cell-style="{ background: '#0f172a', color: '#94a3b8', borderColor: '#334155' }"
        :cell-style="{ background: '#1e293b', color: '#e2e8f0', borderColor: '#334155' }"
        v-loading="loading"
        element-loading-background="rgba(30, 41, 59, 0.8)"
        empty-text="暂无申诉记录"
      >
        <el-table-column prop="courseName" label="课程" min-width="120">
          <template slot-scope="scope">
            {{ scope.row.courseName || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="content-text">{{ scope.row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag
              v-if="scope.row.status === 'PENDING'"
              type="warning"
              size="small"
              effect="dark"
            >待处理</el-tag>
            <el-tag
              v-else-if="scope.row.status === 'REPLIED'"
              type="success"
              size="small"
              effect="dark"
            >已回复</el-tag>
            <el-tag
              v-else-if="scope.row.status === 'CLOSED'"
              type="info"
              size="small"
              effect="dark"
            >已关闭</el-tag>
            <el-tag
              v-else
              size="small"
              effect="dark"
            >{{ scope.row.status || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="教师回复" min-width="200" show-overflow-tooltip>
          <template slot-scope="scope">
            <span v-if="scope.row.reply" class="reply-text">{{ scope.row.reply }}</span>
            <span v-else class="no-reply">暂无回复</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170" align="center">
          <template slot-scope="scope">
            <span class="time-text">{{ scope.row.createTime || '--' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrap" v-if="total > pageSize">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page.sync="currentPage"
          @current-change="fetchAppeals"
          class="dark-pagination"
        ></el-pagination>
      </div>
    </div>

    <!-- New Appeal Dialog -->
    <el-dialog
      title="提交新申诉"
      :visible.sync="dialogVisible"
      width="560px"
      custom-class="dark-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="appealForm" :rules="formRules" ref="appealFormRef" label-width="80px">
        <el-form-item label="课程" prop="courseId">
          <el-select
            v-model="appealForm.courseId"
            placeholder="请选择课程"
            style="width: 100%"
            class="dialog-select"
            @change="handleDialogCourseChange"
          >
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName || course.name"
              :value="course.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="教师" prop="teacherId">
          <el-select
            v-model="appealForm.teacherId"
            placeholder="请选择教师"
            style="width: 100%"
            class="dialog-select"
          >
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.realName || teacher.name || teacher.username"
              :value="teacher.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            type="textarea"
            v-model="appealForm.content"
            :rows="5"
            placeholder="请详细描述你的反馈或申诉内容..."
            class="dialog-textarea"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="dialogVisible = false" class="cancel-btn">取 消</el-button>
        <el-button type="primary" size="small" :loading="submitting" @click="handleSubmit" class="submit-btn">
          提 交
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getStudentAppeals, submitAppeal, getCourseList } from '../../api'

export default {
  name: 'StudentAppeals',
  data() {
    return {
      appeals: [],
      loading: false,
      total: 0,
      currentPage: 1,
      pageSize: 10,
      courses: [],
      teachers: [],
      dialogVisible: false,
      submitting: false,
      appealForm: {
        courseId: '',
        teacherId: '',
        content: ''
      },
      formRules: {
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
        content: [
          { required: true, message: '请输入申诉内容', trigger: 'blur' },
          { min: 10, message: '内容至少10个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    userId() {
      return this.$store.state.userInfo.id
    }
  },
  created() {
    this.fetchAppeals()
    this.fetchCourses()
  },
  methods: {
    async fetchAppeals() {
      this.loading = true
      try {
        const res = await getStudentAppeals({
          studentId: this.userId,
          page: this.currentPage,
          size: this.pageSize
        })
        if (res.data.code === 200) {
          const data = res.data.data
          if (Array.isArray(data)) {
            this.appeals = data
            this.total = data.length
          } else if (data && data.records) {
            this.appeals = data.records
            this.total = data.total || data.records.length
          } else {
            this.appeals = []
            this.total = 0
          }
        }
      } catch (e) {
        console.error('获取申诉列表失败', e)
        this.$message.error('获取申诉列表失败')
      } finally {
        this.loading = false
      }
    },
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
    handleDialogCourseChange(courseId) {
      this.appealForm.teacherId = ''
      const selectedCourse = this.courses.find(c => c.id === courseId)
      if (selectedCourse && selectedCourse.teachers) {
        this.teachers = selectedCourse.teachers
      } else if (selectedCourse && selectedCourse.teacherId) {
        this.teachers = [{
          id: selectedCourse.teacherId,
          realName: selectedCourse.teacherName || '教师',
          name: selectedCourse.teacherName || '教师'
        }]
      } else {
        this.teachers = []
      }
      // Auto-select if only one teacher
      if (this.teachers.length === 1) {
        this.appealForm.teacherId = this.teachers[0].id
      }
    },
    showDialog() {
      this.appealForm = {
        courseId: '',
        teacherId: '',
        content: ''
      }
      this.teachers = []
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.appealFormRef) {
          this.$refs.appealFormRef.clearValidate()
        }
      })
    },
    handleSubmit() {
      this.$refs.appealFormRef.validate(async valid => {
        if (!valid) return
        this.submitting = true
        try {
          const res = await submitAppeal({
            courseId: this.appealForm.courseId,
            teacherId: this.appealForm.teacherId,
            studentId: this.userId,
            content: this.appealForm.content
          })
          if (res.data.code === 200) {
            this.$message.success('申诉提交成功')
            this.dialogVisible = false
            this.fetchAppeals()
          } else {
            this.$message.error(res.data.msg || '提交失败')
          }
        } catch (e) {
          console.error('提交申诉失败', e)
          this.$message.error('提交申诉失败，请稍后重试')
        } finally {
          this.submitting = false
        }
      })
    }
  }
}
</script>

<style scoped>
.appeals-container {
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

.add-btn {
  background: linear-gradient(135deg, #ec4899, #be185d) !important;
  border: none !important;
  font-size: 13px !important;
}

.add-btn:hover {
  opacity: 0.9;
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

.content-text {
  color: #e2e8f0;
}

.reply-text {
  color: #cbd5e1;
}

.no-reply {
  color: #475569;
  font-style: italic;
}

.time-text {
  color: #94a3b8;
  font-size: 13px;
}

/* Pagination */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dark-pagination >>> .el-pager li {
  background: #0f172a;
  color: #94a3b8;
  border: 1px solid #334155;
}

.dark-pagination >>> .el-pager li.active {
  background: #ec4899;
  color: #fff;
  border-color: #ec4899;
}

.dark-pagination >>> .btn-prev,
.dark-pagination >>> .btn-next {
  background: #0f172a;
  color: #94a3b8;
  border: 1px solid #334155;
}

/* Dialog */
.dark-dialog >>> .el-dialog {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 12px;
}

.dark-dialog >>> .el-dialog__header {
  border-bottom: 1px solid #334155;
  padding: 16px 20px;
}

.dark-dialog >>> .el-dialog__title {
  color: #e2e8f0;
  font-size: 16px;
  font-weight: 600;
}

.dark-dialog >>> .el-dialog__headerbtn .el-dialog__close {
  color: #94a3b8;
}

.dark-dialog >>> .el-dialog__headerbtn .el-dialog__close:hover {
  color: #ec4899;
}

.dark-dialog >>> .el-dialog__body {
  padding: 24px 20px;
}

.dark-dialog >>> .el-dialog__footer {
  border-top: 1px solid #334155;
  padding: 12px 20px;
}

.dark-dialog >>> .el-form-item__label {
  color: #94a3b8;
}

.dialog-select >>> .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}

.dialog-select >>> .el-input__inner:focus {
  border-color: #ec4899;
}

.dialog-textarea >>> .el-textarea__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
  resize: vertical;
}

.dialog-textarea >>> .el-textarea__inner:focus {
  border-color: #ec4899;
}

.dialog-textarea >>> .el-textarea__inner::placeholder {
  color: #475569;
}

.cancel-btn {
  background: transparent !important;
  border-color: #334155 !important;
  color: #94a3b8 !important;
}

.cancel-btn:hover {
  border-color: #ec4899 !important;
  color: #ec4899 !important;
}

.submit-btn {
  background: linear-gradient(135deg, #ec4899, #be185d) !important;
  border: none !important;
}
</style>
