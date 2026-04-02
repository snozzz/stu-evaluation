<template>
  <div class="page-container">
    <div class="header-actions">
      <el-select v-model="selectedCourse" placeholder="选择课程" clearable @change="loadAssignments">
        <el-option
          v-for="c in courses"
          :key="c.id"
          :label="c.courseName"
          :value="c.id">
        </el-option>
      </el-select>
      <el-button type="primary" icon="el-icon-plus" @click="showAddDialog">布置作业/实验</el-button>
    </div>

    <el-table :data="assignments" style="width: 100%; margin-top: 20px" v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="150"></el-table-column>
      <el-table-column prop="type" label="类型" width="100">
        <template slot-scope="scope">
          <el-tag size="small" :type="scope.row.type === 'EXPERIMENT' ? 'success' : 'info'">
            {{ scope.row.type === 'EXPERIMENT' ? '实验' : '作业' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="dueDateText" label="截止日期" width="180"></el-table-column>
      <el-table-column prop="createTimeText" label="发布时间" width="180"></el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="viewSubmissions(scope.row)">查看提交</el-button>
          <el-button type="text" size="small" style="color: #ef4444;" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Publish Dialog -->
    <el-dialog title="布置作业/实验" :visible.sync="addDialogVisible" width="500px">
      <el-form :model="addForm" :rules="rules" ref="addForm" label-width="80px">
        <el-form-item label="选择课程" prop="courseId">
          <el-select v-model="addForm.courseId" style="width: 100%">
            <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="addForm.title" placeholder="如：第一次作业"></el-input>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="addForm.type">
            <el-radio label="HOMEWORK">作业</el-radio>
            <el-radio label="EXPERIMENT">实验</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" :rows="3" v-model="addForm.description" placeholder="请输入详情描述"></el-input>
        </el-form-item>
        <el-form-item label="截止日期" prop="dueDate">
          <el-date-picker v-model="addForm.dueDate" type="datetime" placeholder="选择日期时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%"></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd" :loading="saving">确定</el-button>
      </div>
    </el-dialog>

    <!-- Submissions Dialog -->
    <el-dialog title="学生提交情况" :visible.sync="subDialogVisible" width="800px">
      <el-table :data="submissions" style="width: 100%" v-loading="subLoading">
        <el-table-column prop="studentNameText" label="学生姓名" width="120"></el-table-column>
        <el-table-column prop="content" label="提交内容" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column label="附件" width="100">
          <template slot-scope="scope">
            <el-link type="primary" v-if="scope.row.fileUrl" :href="scope.row.fileUrl" target="_blank">下载</el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTimeText" label="提交时间" width="180"></el-table-column>
        <el-table-column label="成绩" width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.status === 'GRADED'">{{ scope.row.score }}</span>
            <span v-else style="color: #f59e0b">未批改</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="openGradeDialog(scope.row)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- Grade Dialog -->
    <el-dialog title="批改作业" :visible.sync="gradeDialogVisible" width="400px">
      <el-form :model="gradeForm" label-width="60px">
        <el-form-item label="分数">
          <el-input-number v-model="gradeForm.score" :min="0" :max="100"></el-input-number>
        </el-form-item>
        <el-form-item label="反馈">
          <el-input type="textarea" v-model="gradeForm.feedback" :rows="3"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGrade">确定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { getAssignmentList, createAssignment, deleteAssignment, getAssignmentSubmissions, gradeAssignment, getBindings, getCourseList, getUserList } from '../../api'

export default {
  name: 'TeacherAssignments',
  data() {
    return {
      courses: [],
      selectedCourse: '',
      assignments: [],
      loading: false,
      addDialogVisible: false,
      saving: false,
      addForm: {
        courseId: '',
        title: '',
        type: 'HOMEWORK',
        description: '',
        dueDate: ''
      },
      rules: {
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        dueDate: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
      },
      subDialogVisible: false,
      submissions: [],
      subLoading: false,
      gradeDialogVisible: false,
      gradeForm: {
        id: null,
        score: 0,
        feedback: ''
      },
      studentNameMap: {}
    }
  },
  computed: {
    userId() {
      return this.$store.state.userInfo.id
    }
  },
  created() {
    this.fetchCourses()
    this.fetchStudentMap()
  },
  methods: {
    async fetchStudentMap() {
      try {
        const res = await getUserList({ role: 'STUDENT', page: 1, size: 1000 })
        if (res.data.code === 200) {
          const pageData = res.data.data || {}
          const students = pageData.records || []
          const studentNameMap = {}
          students.forEach(student => {
            const name = student.realName || student.nickname
            if (student.id != null && name) {
              studentNameMap[student.id] = name
            }
          })
          this.studentNameMap = studentNameMap
          if (this.submissions.length > 0) {
            this.submissions = this.submissions.map(item => this.normalizeSubmission(item))
          }
        }
      } catch (e) {
        console.error('Failed to fetch students', e)
      }
    },
    async fetchCourses() {
      try {
        const [bindRes, courseRes] = await Promise.all([
          getBindings({ teacherId: this.userId }),
          getCourseList({ page: 1, size: 1000 })
        ])
        const allCourses = []
        if (courseRes.data.code === 200) {
          const d = courseRes.data.data
          allCourses.push(...(d.records || d || []))
        }
        if (bindRes.data.code === 200) {
          const binds = Array.isArray(bindRes.data.data) ? bindRes.data.data : (bindRes.data.data.records || [])

          // Deduplicate courses
          const map = {}
          binds.forEach(b => {
            if (!map[b.courseId]) {
              const course = allCourses.find(c => c.id === b.courseId)
              map[b.courseId] = { id: b.courseId, courseName: course ? course.name : ('课程ID: ' + b.courseId) }
            }
          })
          this.courses = Object.values(map)
          if (this.courses.length > 0) {
            this.selectedCourse = this.courses[0].id
            this.loadAssignments()
          }
        }
      } catch (e) {
        console.error('Failed to fetch courses', e)
      }
    },
    async loadAssignments() {
      if (!this.selectedCourse) return
      this.loading = true
      try {
        const res = await getAssignmentList({ courseId: this.selectedCourse })
        if (res.data.code === 200) {
          this.assignments = (res.data.data || []).map(item => this.normalizeAssignment(item))
        }
      } finally {
        this.loading = false
      }
    },
    showAddDialog() {
      this.addForm = {
        courseId: this.selectedCourse,
        title: '',
        type: 'HOMEWORK',
        description: '',
        dueDate: ''
      }
      this.addDialogVisible = true
    },
    submitAdd() {
      this.$refs.addForm.validate(async valid => {
        if (!valid) return
        this.saving = true
        try {
          const payload = { ...this.addForm, teacherId: this.userId }
          const res = await createAssignment(payload)
          if (res.data.code === 200) {
            this.$message.success('布置成功')
            this.addDialogVisible = false
            this.loadAssignments()
          } else {
            this.$message.error(res.data.msg || '布置失败')
          }
        } finally {
          this.saving = false
        }
      })
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定删除该作业/实验吗？', '提示', { type: 'warning' })
        const res = await deleteAssignment(row.id)
        if (res.data.code === 200) {
          this.$message.success('删除成功')
          this.loadAssignments()
        }
      } catch (e) {}
    },
    async viewSubmissions(row) {
      this.subDialogVisible = true
      this.subLoading = true
      try {
        const res = await getAssignmentSubmissions({ assignmentId: row.id })
        if (res.data.code === 200) {
          this.submissions = (res.data.data || []).map(item => this.normalizeSubmission(item))
        }
      } finally {
        this.subLoading = false
      }
    },
    openGradeDialog(row) {
      this.gradeForm = {
        id: row.id,
        score: row.score || 0,
        feedback: row.feedback || ''
      }
      this.gradeDialogVisible = true
    },
    async submitGrade() {
      try {
        const res = await gradeAssignment(this.gradeForm)
        if (res.data.code === 200) {
          this.$message.success('批改成功')
          this.gradeDialogVisible = false
          // Refresh submissions
          const idx = this.submissions.findIndex(s => s.id === this.gradeForm.id)
          if (idx !== -1) {
            this.$set(this.submissions, idx, {
              ...this.submissions[idx],
              score: this.gradeForm.score,
              feedback: this.gradeForm.feedback,
              status: 'GRADED'
            })
          }
        }
      } catch (e) {
        this.$message.error('批改失败')
      }
    },
    normalizeAssignment(item) {
      return {
        ...item,
        dueDateText: this.formatDateTime(item.dueDate),
        createTimeText: this.formatDateTime(item.createTime)
      }
    },
    normalizeSubmission(item) {
      return {
        ...item,
        studentNameText: item.studentName || this.studentNameMap[item.studentId] || '未知学生',
        submitTimeText: this.formatDateTime(item.submitTime)
      }
    },
    formatDateTime(value) {
      if (!value) return '-'
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return String(value).replace('T', ' ').replace(/\.\d+/, '')
      }
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      const second = String(date.getSeconds()).padStart(2, '0')
      return `${year}/${month}/${day} ${hour}:${minute}:${second}`
    }
  }
}
</script>

<style scoped>
.page-container {
  background: #ffffff;
  padding: 24px;
  border-radius: 12px;
  min-height: calc(100vh - 120px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}
.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}
</style>
