<template>
  <div class="comments-page">
    <!-- Top Filters -->
    <div class="dark-card filter-section">
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
        <el-col :span="6">
          <div class="filter-label">选择学生</div>
          <el-select
            v-model="selectedStudent"
            placeholder="请选择学生"
            filterable
            clearable
            style="width: 100%"
            @change="handleStudentChange"
            :disabled="!selectedClass"
          >
            <el-option
              v-for="stu in studentList"
              :key="stu.id"
              :label="stu.realName + ' (' + (stu.studentNo || '') + ')'"
              :value="stu.id"
            ></el-option>
          </el-select>
        </el-col>
      </el-row>
    </div>

    <!-- Comment Editor Section -->
    <div v-if="selectedStudent && selectedCourse" class="dark-card comment-section">
      <div class="card-header">
        <span class="card-title">
          评语管理
          <span class="student-tag">{{ currentStudentName }}</span>
        </span>
        <div class="publish-status">
          <el-tag
            v-if="currentComment.id"
            :type="currentComment.isPublished === 1 ? 'success' : 'info'"
            size="small"
            effect="dark"
          >
            {{ currentComment.isPublished === 1 ? '已发布' : '未发布' }}
          </el-tag>
        </div>
      </div>

      <!-- AI Comment Area -->
      <div class="ai-section">
        <div class="section-label">
          <i class="el-icon-magic-stick"></i> AI 生成评语
        </div>
        <div class="ai-result" v-if="aiComment">
          <div class="ai-comment-text">{{ aiComment }}</div>
          <el-button type="text" size="small" class="use-ai-btn" @click="useAIComment">
            <i class="el-icon-document-copy"></i> 使用此评语
          </el-button>
        </div>
        <el-button
          type="warning"
          size="small"
          icon="el-icon-magic-stick"
          @click="handleGenerateAI"
          :loading="aiLoading"
          class="ai-generate-btn"
        >
          AI生成评语
        </el-button>
      </div>

      <!-- Manual Comment Area -->
      <div class="manual-section">
        <div class="section-label">
          <i class="el-icon-edit-outline"></i> 教师评语
        </div>
        <el-input
          v-model="commentText"
          type="textarea"
          :rows="6"
          placeholder="请输入评语内容，或使用AI生成后修改..."
          class="dark-textarea"
          maxlength="1000"
          show-word-limit
        ></el-input>
      </div>

      <!-- Action Buttons -->
      <div class="comment-actions">
        <el-button
          type="warning"
          icon="el-icon-check"
          @click="handleSaveComment"
          :loading="saveLoading"
        >
          保存评语
        </el-button>
        <el-button
          type="success"
          icon="el-icon-s-promotion"
          @click="handlePublishComment"
          :loading="publishLoading"
          :disabled="!currentComment.id"
        >
          发布评语
        </el-button>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="dark-card empty-section">
      <div class="empty-hint">
        <i class="el-icon-chat-line-round"></i>
        <span>请选择课程和学生以管理评语</span>
      </div>
    </div>

    <!-- Comment History -->
    <div v-if="commentHistory.length > 0" class="dark-card history-section">
      <div class="card-header">
        <span class="card-title">该课程评语列表</span>
      </div>
      <el-table
        :data="commentHistory"
        style="width: 100%"
        class="dark-table"
        :header-cell-style="tableHeaderStyle"
        :cell-style="tableCellStyle"
      >
        <el-table-column label="学生" width="120">
          <template slot-scope="scope">
            {{ getStudentName(scope.row.studentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评语" show-overflow-tooltip></el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.isPublished === 1 ? 'success' : 'info'"
              size="small"
              effect="dark"
            >
              {{ scope.row.isPublished === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import {
  getBindings,
  getComments,
  saveComment,
  generateAIComment,
  publishComment,
  getCourseList,
  getClassList,
  getStudentsByClass
} from '../../api'

export default {
  name: 'TeacherComments',
  data() {
    return {
      bindings: [],
      courseList: [],
      classList: [],
      studentList: [],
      selectedCourse: null,
      selectedClass: null,
      selectedStudent: null,
      commentText: '',
      aiComment: '',
      aiLoading: false,
      saveLoading: false,
      publishLoading: false,
      currentComment: {
        id: null,
        isPublished: 0
      },
      commentHistory: [],
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
    },
    currentStudentName() {
      const stu = this.studentList.find(s => s.id === this.selectedStudent)
      return stu ? stu.realName : ''
    }
  },
  created() {
    this.loadInitData()
  },
  methods: {
    async loadInitData() {
      try {
        const userId = this.$store.state.userInfo ? this.$store.state.userInfo.id : null

        const [bindRes, courseRes, classRes] = await Promise.all([
          getBindings({ teacherId: userId }),
          getCourseList({ page: 1, size: 1000 }),
          getClassList({ page: 1, size: 1000 })
        ])

        this.bindings = bindRes.data.data || []
        const courseData = courseRes.data.data
        this.courseList = courseData.records || courseData || []
        const classData = classRes.data.data
        this.classList = classData.records || classData || []
      } catch (e) {
        console.error('Load init data error:', e)
      }
    },
    handleCourseChange() {
      this.selectedClass = null
      this.selectedStudent = null
      this.studentList = []
      this.resetComment()
      this.loadCommentHistory()
    },
    async handleClassChange() {
      this.selectedStudent = null
      this.resetComment()
      if (this.selectedClass) {
        await this.loadStudents()
        this.loadCommentHistory()
      } else {
        this.studentList = []
        this.commentHistory = []
      }
    },
    async handleStudentChange() {
      this.resetComment()
      if (this.selectedStudent && this.selectedCourse) {
        await this.loadExistingComment()
      }
    },
    async loadStudents() {
      try {
        const res = await getStudentsByClass(this.selectedClass)
        const data = res.data.data
        this.studentList = data || []
      } catch (e) {
        console.error('Load students error:', e)
        this.studentList = []
      }
    },
    async loadExistingComment() {
      try {
        const teacherId = this.$store.state.userInfo ? this.$store.state.userInfo.id : null
        const res = await getComments({
          studentId: this.selectedStudent,
          courseId: this.selectedCourse,
          teacherId: teacherId
        })
        const comments = res.data.data || []
        if (comments.length > 0) {
          const latest = comments[0]
          this.currentComment = {
            id: latest.id,
            isPublished: latest.isPublished || 0
          }
          this.commentText = latest.comment || ''
          if (latest.aiComment) {
            this.aiComment = latest.aiComment
          }
        }
      } catch (e) {
        console.error('Load comment error:', e)
      }
    },
    async loadCommentHistory() {
      if (!this.selectedCourse) {
        this.commentHistory = []
        return
      }
      try {
        const teacherId = this.$store.state.userInfo ? this.$store.state.userInfo.id : null
        const res = await getComments({
          courseId: this.selectedCourse,
          teacherId: teacherId
        })
        this.commentHistory = res.data.data || []
      } catch (e) {
        this.commentHistory = []
      }
    },
    resetComment() {
      this.commentText = ''
      this.aiComment = ''
      this.currentComment = { id: null, isPublished: 0 }
    },
    async handleGenerateAI() {
      if (!this.selectedStudent || !this.selectedCourse) {
        this.$message.warning('请先选择课程和学生')
        return
      }

      this.aiLoading = true
      try {
        const res = await generateAIComment({
          studentId: this.selectedStudent,
          courseId: this.selectedCourse
        })
        this.aiComment = res.data.data || ''
        this.$message.success('AI评语生成成功')
      } catch (e) {
        this.$message.error('AI评语生成失败，请稍后重试')
      } finally {
        this.aiLoading = false
      }
    },
    useAIComment() {
      this.commentText = this.aiComment
      this.$message.success('已填入AI评语，可继续编辑')
    },
    async handleSaveComment() {
      if (!this.commentText.trim()) {
        this.$message.warning('请输入评语内容')
        return
      }

      this.saveLoading = true
      try {
        const teacherId = this.$store.state.userInfo ? this.$store.state.userInfo.id : null
        const data = {
          studentId: this.selectedStudent,
          courseId: this.selectedCourse,
          teacherId: teacherId,
          comment: this.commentText.trim(),
          aiComment: this.aiComment || null,
          isPublished: 0
        }
        if (this.currentComment.id) {
          data.id = this.currentComment.id
        }

        await saveComment(data)
        this.$message.success('评语保存成功')
        // Reload to get the new id
        await this.loadExistingComment()
        this.loadCommentHistory()
      } catch (e) {
        this.$message.error('保存失败')
      } finally {
        this.saveLoading = false
      }
    },
    async handlePublishComment() {
      if (!this.currentComment.id) {
        this.$message.warning('请先保存评语')
        return
      }

      this.publishLoading = true
      try {
        await publishComment(this.currentComment.id)
        this.$message.success('评语已发布，学生可以查看')
        this.currentComment.isPublished = 1
        this.loadCommentHistory()
      } catch (e) {
        this.$message.error('发布失败')
      } finally {
        this.publishLoading = false
      }
    },
    getStudentName(studentId) {
      const stu = this.studentList.find(s => s.id === studentId)
      return stu ? stu.realName : ('学生ID: ' + studentId)
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
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
.comments-page {
  padding: 0;
}

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

.student-tag {
  display: inline-block;
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 13px;
  margin-left: 10px;
  font-weight: 500;
}

/* Filter Section */
.filter-section {
  margin-bottom: 20px;
}

.filter-label {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 6px;
}

/* Select Styles */
.comments-page >>> .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}

.comments-page >>> .el-input__inner:focus {
  border-color: #f59e0b;
}

.comments-page >>> .el-input__inner::placeholder {
  color: #475569;
}

/* AI Section */
.ai-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #162032;
  border-radius: 10px;
  border: 1px solid #334155;
}

.section-label {
  font-size: 14px;
  color: #f59e0b;
  font-weight: 600;
  margin-bottom: 12px;
}

.section-label i {
  margin-right: 4px;
}

.ai-result {
  background: #0f172a;
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 12px;
  position: relative;
}

.ai-comment-text {
  color: #cbd5e1;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
}

.use-ai-btn {
  color: #f59e0b !important;
  margin-top: 8px;
}

.use-ai-btn:hover {
  color: #fbbf24 !important;
}

.ai-generate-btn {
  border-radius: 8px;
}

/* Manual Section */
.manual-section {
  margin-bottom: 20px;
}

.manual-section .section-label {
  color: #94a3b8;
}

/* Textarea Styles */
.comments-page >>> .el-textarea__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.7;
}

.comments-page >>> .el-textarea__inner:focus {
  border-color: #f59e0b;
}

.comments-page >>> .el-textarea__inner::placeholder {
  color: #475569;
}

.comments-page >>> .el-input__count {
  background: transparent;
  color: #475569;
}

/* Action Buttons */
.comment-actions {
  display: flex;
  gap: 12px;
  padding-top: 4px;
}

/* Empty State */
.empty-section {
  min-height: 200px;
}

.empty-hint {
  text-align: center;
  padding: 60px 20px;
  color: #64748b;
  font-size: 14px;
}

.empty-hint i {
  font-size: 40px;
  display: block;
  margin-bottom: 12px;
  color: #475569;
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

.dark-table::before {
  display: none;
}
</style>
