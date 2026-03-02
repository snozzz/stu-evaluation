<template>
  <div class="page-container">
    <div class="header-actions">
      <el-select v-model="selectedCourse" placeholder="选择课程查看作业" clearable @change="loadAssignments">
        <el-option
          v-for="c in courses"
          :key="c.id"
          :label="c.name || c.courseName"
          :value="c.id">
        </el-option>
      </el-select>
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
      <el-table-column prop="dueDate" label="截止日期" width="160"></el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="small" :type="statusType(scope.row)">
            {{ statusText(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="得分" width="100" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.status === 'GRADED'" style="color: #61BFAD; font-weight: bold;">
            {{ scope.row.score }}
          </span>
          <span v-else-if="scope.row.submitted" style="color: #9ca3af;">未批改</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template slot-scope="scope">
          <el-button v-if="!scope.row.submitted && !isExpired(scope.row)" type="text" size="small" @click="openSubmitDialog(scope.row)">提交</el-button>
          <span v-else-if="!scope.row.submitted && isExpired(scope.row)" style="color: #f56c6c;">已截止</span>
          <el-button v-else type="text" size="small" @click="openViewDialog(scope.row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Submit Dialog -->
    <el-dialog title="提交作业/实验" :visible.sync="submitDialogVisible" width="500px">
      <el-form :model="submitForm" label-width="80px">
        <el-form-item label="提交内容">
          <el-input type="textarea" :rows="4" v-model="submitForm.content" placeholder="输入文字内容..."></el-input>
        </el-form-item>
        <el-form-item label="附件链接">
          <el-input v-model="submitForm.fileUrl" placeholder="如网盘链接或文件URL"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </div>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog title="作业/实验详情" :visible.sync="viewDialogVisible" width="500px">
      <div v-if="currentView" class="view-content">
        <div class="view-item">
          <div class="label">我的提交:</div>
          <div class="value">{{ currentView.content || '无文字内容' }}</div>
        </div>
        <div class="view-item" v-if="currentView.fileUrl">
          <div class="label">附件:</div>
          <div class="value"><el-link type="primary" :href="currentView.fileUrl" target="_blank">{{ currentView.fileUrl }}</el-link></div>
        </div>
        <div class="view-item" v-if="currentView.status === 'GRADED'">
          <div class="label">得分:</div>
          <div class="value" style="color: #61BFAD; font-weight: bold; font-size: 18px;">{{ currentView.score }}</div>
        </div>
        <div class="view-item" v-if="currentView.feedback">
          <div class="label">老师评语:</div>
          <div class="value">{{ currentView.feedback }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStudentAssignments, submitAssignment, getCourseList } from '../../api'

export default {
  name: 'StudentAssignments',
  data() {
    return {
      courses: [],
      selectedCourse: '',
      assignments: [],
      loading: false,
      submitDialogVisible: false,
      submitting: false,
      submitForm: {
        assignmentId: null,
        content: '',
        fileUrl: ''
      },
      viewDialogVisible: false,
      currentView: null
    }
  },
  computed: {
    userId() {
      return this.$store.state.userInfo.id
    }
  },
  created() {
    this.fetchCourses()
    this.loadAssignments() // Load all initially
  },
  methods: {
    isExpired(row) {
      if (row.expired != null) return row.expired
      if (!row.dueDate) return false
      return new Date(row.dueDate).getTime() < Date.now()
    },
    statusText(row) {
      if (row.submitted) return '已提交'
      if (this.isExpired(row)) return '已截止'
      return '未提交'
    },
    statusType(row) {
      if (row.submitted) return 'success'
      if (this.isExpired(row)) return 'warning'
      return 'danger'
    },
    async fetchCourses() {
      try {
        const res = await getCourseList({ studentId: this.userId })
        if (res.data.code === 200) {
          const data = res.data.data
          this.courses = Array.isArray(data) ? data : (data.records || [])
        }
      } catch (e) {
        console.error('Failed to fetch courses', e)
      }
    },
    async loadAssignments() {
      this.loading = true
      try {
        const params = { studentId: this.userId }
        if (this.selectedCourse) {
          params.courseId = this.selectedCourse
        }
        const res = await getStudentAssignments(params)
        if (res.data.code === 200) {
          this.assignments = res.data.data || []
        }
      } finally {
        this.loading = false
      }
    },
    openSubmitDialog(row) {
      if (this.isExpired(row)) {
        this.$message.warning('该作业/实验已截止，无法提交')
        return
      }
      this.submitForm = {
        assignmentId: row.id,
        content: '',
        fileUrl: ''
      }
      this.submitDialogVisible = true
    },
    async handleSubmit() {
      this.submitting = true
      try {
        const payload = {
          ...this.submitForm,
          studentId: this.userId
        }
        const res = await submitAssignment(payload)
        if (res.data.code === 200) {
          this.$message.success('提交成功')
          this.submitDialogVisible = false
          this.loadAssignments()
        } else {
          this.$message.error(res.data.msg || '提交失败')
        }
      } finally {
        this.submitting = false
      }
    },
    openViewDialog(row) {
      this.currentView = row
      this.viewDialogVisible = true
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
  margin-bottom: 20px;
}
.view-content {
  padding: 10px;
}
.view-item {
  margin-bottom: 16px;
}
.view-item .label {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 4px;
}
.view-item .value {
  color: #2c3e50;
  font-size: 14px;
  background: #f8fafc;
  padding: 10px;
  border-radius: 6px;
  min-height: 40px;
}
</style>
