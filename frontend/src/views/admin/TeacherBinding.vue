<template>
  <div class="teacher-binding-page">
    <div class="dark-card">
      <div class="card-header">
        <span class="card-title">教师权限分配</span>
      </div>

      <!-- Teacher Selector -->
      <div class="filter-bar">
        <span class="filter-label">选择教师：</span>
        <el-select
          v-model="selectedTeacherId"
          placeholder="请选择教师"
          filterable
          clearable
          class="dark-select teacher-select"
          @change="handleTeacherChange"
        >
          <el-option
            v-for="t in teacherList"
            :key="t.id"
            :label="t.realName || t.username"
            :value="t.id"
          ></el-option>
        </el-select>
        <el-button
          v-if="selectedTeacherId"
          type="primary"
          size="small"
          icon="el-icon-plus"
          @click="openAddDialog"
        >
          新增绑定
        </el-button>
      </div>

      <!-- Binding Table -->
      <div v-if="selectedTeacherId">
        <el-table
          :data="bindings"
          style="width: 100%"
          class="dark-table"
          :header-cell-style="tableHeaderStyle"
          :cell-style="tableCellStyle"
          v-loading="loadingBindings"
          element-loading-background="rgba(30, 41, 59, 0.8)"
          empty-text="该教师暂无绑定记录"
        >
          <el-table-column type="index" label="#" width="60"></el-table-column>
          <el-table-column label="课程名" min-width="180">
            <template slot-scope="scope">
              <span class="course-name">{{ getCourseName(scope.row.courseId) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="班级名" min-width="180">
            <template slot-scope="scope">
              <span>{{ getClassName(scope.row.classId) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="绑定时间" width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template slot-scope="scope">
              <el-popconfirm
                title="确定要解绑该课程班级吗？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="handleUnbind(scope.row)"
              >
                <el-button
                  slot="reference"
                  type="danger"
                  size="mini"
                  plain
                >
                  解绑
                </el-button>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-else class="empty-tip">
        <i class="el-icon-info"></i>
        <span>请先选择一位教师查看其绑定情况</span>
      </div>
    </div>

    <!-- Add Binding Dialog -->
    <el-dialog
      title="新增课程班级绑定"
      :visible.sync="addDialogVisible"
      width="480px"
      custom-class="dark-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="addForm" label-width="80px" class="dark-form">
        <el-form-item label="选择课程">
          <el-select
            v-model="addForm.courseId"
            placeholder="请选择课程"
            filterable
            style="width: 100%"
            class="dark-select"
          >
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择班级">
          <el-select
            v-model="addForm.classId"
            placeholder="请选择班级"
            filterable
            style="width: 100%"
            class="dark-select"
          >
            <el-option
              v-for="cls in classList"
              :key="cls.id"
              :label="cls.name"
              :value="cls.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false" class="cancel-btn">取 消</el-button>
        <el-button type="primary" @click="handleAddBinding" :loading="addLoading">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, getBindings, addBinding, deleteBinding, getCourseList, getClassList } from '../../api'

export default {
  name: 'TeacherBinding',
  data() {
    return {
      teacherList: [],
      selectedTeacherId: null,
      bindings: [],
      courseList: [],
      classList: [],
      loadingBindings: false,
      addDialogVisible: false,
      addLoading: false,
      addForm: {
        courseId: null,
        classId: null
      },
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
  created() {
    this.loadBaseData()
  },
  methods: {
    async loadBaseData() {
      try {
        const [userRes, courseRes, classRes] = await Promise.all([
          getUserList({ role: 'TEACHER', page: 1, size: 1000 }),
          getCourseList({ page: 1, size: 1000 }),
          getClassList({ page: 1, size: 1000 })
        ])
        const userData = userRes.data.data
        this.teacherList = userData.records || userData || []
        const courseData = courseRes.data.data
        this.courseList = courseData.records || courseData || []
        const classData = classRes.data.data
        this.classList = classData.records || classData || []
      } catch (e) {
        this.$message.error('基础数据加载失败')
      }
    },
    async handleTeacherChange(teacherId) {
      if (!teacherId) {
        this.bindings = []
        return
      }
      this.loadingBindings = true
      try {
        const res = await getBindings({ teacherId })
        this.bindings = res.data.data || []
      } catch (e) {
        this.$message.error('获取绑定列表失败')
      } finally {
        this.loadingBindings = false
      }
    },
    getCourseName(courseId) {
      const course = this.courseList.find(c => c.id === courseId)
      return course ? course.name : ('课程ID: ' + courseId)
    },
    getClassName(classId) {
      const cls = this.classList.find(c => c.id === classId)
      return cls ? cls.name : ('班级ID: ' + classId)
    },
    openAddDialog() {
      this.addForm = { courseId: null, classId: null }
      this.addDialogVisible = true
    },
    async handleAddBinding() {
      if (!this.addForm.courseId) {
        this.$message.warning('请选择课程')
        return
      }
      if (!this.addForm.classId) {
        this.$message.warning('请选择班级')
        return
      }
      const exists = this.bindings.some(
        b => b.courseId === this.addForm.courseId && b.classId === this.addForm.classId
      )
      if (exists) {
        this.$message.warning('该课程班级绑定已存在')
        return
      }
      this.addLoading = true
      try {
        await addBinding({
          teacherId: this.selectedTeacherId,
          courseId: this.addForm.courseId,
          classId: this.addForm.classId
        })
        this.$message.success('绑定成功')
        this.addDialogVisible = false
        this.handleTeacherChange(this.selectedTeacherId)
      } catch (e) {
        this.$message.error('绑定失败')
      } finally {
        this.addLoading = false
      }
    },
    async handleUnbind(row) {
      try {
        await deleteBinding(row.id)
        this.$message.success('解绑成功')
        this.handleTeacherChange(this.selectedTeacherId)
      } catch (e) {
        this.$message.error('解绑失败')
      }
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
.teacher-binding-page {
  padding: 0;
}

.dark-card {
  background: #1e293b;
  border-radius: 12px;
  border: 1px solid #334155;
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #e2e8f0;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #334155;
}

.filter-label {
  color: #94a3b8;
  font-size: 14px;
  white-space: nowrap;
}

.teacher-select {
  width: 260px;
}

.course-name {
  color: #10b981;
  font-weight: 500;
}

.empty-tip {
  text-align: center;
  color: #64748b;
  padding: 60px 0;
  font-size: 14px;
}

.empty-tip i {
  margin-right: 6px;
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

.dark-table >>> .el-table__fixed-right::before,
.dark-table >>> .el-table__fixed::before {
  display: none;
}

.dark-table >>> .el-table__fixed,
.dark-table >>> .el-table__fixed-right {
  background: #1e293b;
}

/* Select Styles */
>>> .dark-select .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}

>>> .dark-select .el-input__inner:focus {
  border-color: #10b981;
}

/* Dialog Styles */
.teacher-binding-page >>> .dark-dialog {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 12px;
}

.teacher-binding-page >>> .dark-dialog .el-dialog__header {
  border-bottom: 1px solid #334155;
  padding: 16px 20px;
}

.teacher-binding-page >>> .dark-dialog .el-dialog__title {
  color: #e2e8f0;
  font-size: 16px;
  font-weight: 600;
}

.teacher-binding-page >>> .dark-dialog .el-dialog__headerbtn .el-dialog__close {
  color: #94a3b8;
}

.teacher-binding-page >>> .dark-dialog .el-dialog__body {
  padding: 20px;
}

.teacher-binding-page >>> .dark-dialog .el-dialog__footer {
  border-top: 1px solid #334155;
  padding: 12px 20px;
}

/* Form Styles */
.dark-form >>> .el-form-item__label {
  color: #94a3b8;
}

.dark-form >>> .el-input__inner,
.dark-form >>> .el-select .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}

.dark-form >>> .el-input__inner:focus,
.dark-form >>> .el-select .el-input__inner:focus {
  border-color: #10b981;
}

.dark-form >>> .el-select-dropdown {
  background: #1e293b;
  border-color: #334155;
}

.dark-form >>> .el-select-dropdown__item {
  color: #e2e8f0;
}

.dark-form >>> .el-select-dropdown__item.hover,
.dark-form >>> .el-select-dropdown__item:hover {
  background: #263348;
}

.dark-form >>> .el-select-dropdown__item.selected {
  color: #10b981;
}

.cancel-btn {
  background: transparent;
  border-color: #475569;
  color: #94a3b8;
}

.cancel-btn:hover {
  background: #334155;
  border-color: #475569;
  color: #e2e8f0;
}

.dialog-footer {
  text-align: right;
}
</style>
