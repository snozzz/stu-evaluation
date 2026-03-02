<template>
  <div class="comments-page">
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
        <el-col :span="6">
          <div class="filter-label">快捷操作</div>
          <el-button
            type="warning"
            icon="el-icon-s-claim"
            style="width: 100%"
            @click="handlePublishAllComments"
            :loading="publishAllLoading"
            :disabled="!selectedCourse || !selectedClass || studentList.length === 0"
          >
            一键发布全班评语
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- Comment Editor Section -->
    <div v-if="selectedStudent && selectedCourse" class="light-card comment-section">
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
          >
            {{ currentComment.isPublished === 1 ? '已发布' : '未发布' }}
          </el-tag>
        </div>
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
          placeholder="请输入评语内容..."
          maxlength="1000"
          show-word-limit
        ></el-input>
      </div>

      <!-- Action Buttons -->
      <div class="comment-actions">
        <el-button
          type="primary"
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
    <div v-else class="light-card empty-section">
      <div class="empty-hint">
        <i class="el-icon-chat-line-round"></i>
        <span>请选择课程和学生以管理评语</span>
      </div>
    </div>

    <!-- Comment History -->
    <div v-if="commentHistory.length > 0" class="light-card history-section">
      <div class="card-header">
        <span class="card-title">该课程评语列表</span>
      </div>
      <el-table
        :data="commentHistory"
        style="width: 100%"
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
  publishComment,
  getCourseList,
  getClassList,
  getStudentsByClass
} from '../../api'

export default {
  name: 'TeacherComments',
  data() {
    const defaultCommentText = '该生表现良好'
    return {
      defaultCommentText: defaultCommentText,
      bindings: [],
      courseList: [],
      classList: [],
      studentList: [],
      selectedCourse: null,
      selectedClass: null,
      selectedStudent: null,
      commentText: defaultCommentText,
      saveLoading: false,
      publishLoading: false,
      publishAllLoading: false,
      currentComment: {
        id: null,
        isPublished: 0
      },
      commentHistory: []
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
          this.commentText = (latest.comment && latest.comment.trim()) ? latest.comment : this.defaultCommentText
        } else {
          this.resetComment()
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
      this.commentText = this.defaultCommentText
      this.currentComment = { id: null, isPublished: 0 }
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
          isPublished: 0
        }
        if (this.currentComment.id) {
          data.id = this.currentComment.id
        }

        await saveComment(data)
        this.$message.success('评语保存成功')
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
    async handlePublishAllComments() {
      if (!this.selectedCourse || !this.selectedClass) {
        this.$message.warning('请先选择课程和班级')
        return
      }
      if (!this.studentList.length) {
        this.$message.warning('当前班级暂无学生')
        return
      }

      try {
        await this.$confirm(
          `将为当前班级 ${this.studentList.length} 名学生批量发布评语，是否继续？`,
          '提示',
          { type: 'warning' }
        )
      } catch (e) {
        return
      }

      this.publishAllLoading = true
      try {
        const teacherId = this.$store.state.userInfo ? this.$store.state.userInfo.id : null
        const commentMap = new Map()
        ;(this.commentHistory || []).forEach(item => {
          if (!commentMap.has(item.studentId)) {
            commentMap.set(item.studentId, item)
          }
        })

        let affected = 0
        for (const stu of this.studentList) {
          const existing = commentMap.get(stu.id)
          const payload = {
            studentId: stu.id,
            courseId: this.selectedCourse,
            teacherId: teacherId,
            comment: (existing && existing.comment && existing.comment.trim()) ? existing.comment : this.defaultCommentText,
            isPublished: 1
          }
          if (existing && existing.id) {
            payload.id = existing.id
          }
          await saveComment(payload)
          affected += 1
        }

        this.$message.success(`已批量发布 ${affected} 名学生评语`)
        await this.loadCommentHistory()
        if (this.selectedStudent) {
          await this.loadExistingComment()
        }
      } catch (e) {
        this.$message.error('批量发布失败')
      } finally {
        this.publishAllLoading = false
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

.student-tag {
  display: inline-block;
  background: rgba(97, 191, 173, 0.15);
  color: #61BFAD;
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
  color: #64748b;
  margin-bottom: 6px;
}

/* Manual Section */
.manual-section {
  margin-bottom: 20px;
}

.section-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 600;
  margin-bottom: 12px;
}

.section-label i {
  margin-right: 4px;
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
  color: #9ca3af;
  font-size: 14px;
}

.empty-hint i {
  font-size: 40px;
  display: block;
  margin-bottom: 12px;
  color: #d1d5db;
}
</style>
