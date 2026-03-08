<template>
  <div class="appeals-container">
    <!-- Header -->
    <div class="light-card">
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
        v-loading="loading"
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
        ></el-pagination>
      </div>
    </div>

    <!-- New Appeal Dialog -->
    <el-dialog
      title="提交新申诉"
      :visible.sync="dialogVisible"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form :model="appealForm" :rules="formRules" ref="appealFormRef" label-width="80px">
        <el-form-item label="课程" prop="courseId">
          <el-select
            v-model="appealForm.courseId"
            placeholder="请选择课程"
            style="width: 100%"
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
          >
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.realName || teacher.name || teacher.nickname"
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
import { getStudentAppeals, submitAppeal, getCourseList, getStudentScores, getUserList } from '../../api'

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
      allTeachers: [],
      scoreRecords: [],
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
        const [courseRes, scoreRes, teacherRes] = await Promise.all([
          getCourseList({ page: 1, size: 1000 }),
          getStudentScores({ studentId: this.userId }),
          getUserList({ role: 'TEACHER', page: 1, size: 1000 })
        ])
        if (courseRes.data.code === 200) {
          const data = courseRes.data.data
          this.courses = data.records || data || []
        }
        if (scoreRes.data.code === 200) {
          this.scoreRecords = scoreRes.data.data || []
        }
        if (teacherRes.data.code === 200) {
          const data = teacherRes.data.data
          this.allTeachers = data.records || data || []
        }
      } catch (e) {
        console.error('获取课程列表失败', e)
      }
    },
    handleDialogCourseChange(courseId) {
      this.appealForm.teacherId = ''
      // 从学生的成绩记录中找到该课程对应的教师
      const teacherIds = [...new Set(
        this.scoreRecords
          .filter(s => s.courseId === courseId)
          .map(s => s.teacherId)
      )]
      this.teachers = this.allTeachers.filter(t => teacherIds.includes(t.id))
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

.add-btn {
  background: linear-gradient(135deg, #61BFAD, #4a9e8e) !important;
  border: none !important;
  font-size: 13px !important;
}

.add-btn:hover {
  opacity: 0.9;
}

.content-text {
  color: #2c3e50;
}

.reply-text {
  color: #475569;
}

.no-reply {
  color: #94a3b8;
  font-style: italic;
}

.time-text {
  color: #64748b;
  font-size: 13px;
}

/* Pagination */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.cancel-btn {
  background: transparent !important;
  border-color: #e5e7eb !important;
  color: #64748b !important;
}

.cancel-btn:hover {
  border-color: #61BFAD !important;
  color: #61BFAD !important;
}

.submit-btn {
  background: linear-gradient(135deg, #61BFAD, #4a9e8e) !important;
  border: none !important;
}
</style>
