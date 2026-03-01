<template>
  <div class="page-container">
    <div class="dark-card">
      <!-- Search Bar -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索课程名/课程代码"
          clearable
          class="dark-input"
          style="width: 250px; margin-right: 12px;"
          @keyup.enter.native="handleSearch"
        >
          <i slot="prefix" class="el-icon-search"></i>
        </el-input>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        <el-button type="success" icon="el-icon-plus" @click="handleAdd" style="margin-left: auto;">新增课程</el-button>
      </div>

      <!-- Table -->
      <el-table
        :data="tableData"
        style="width: 100%"
        class="dark-table"
        :header-cell-style="headerCellStyle"
        :cell-style="cellStyle"
        stripe
      >
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column prop="name" label="课程名" min-width="150"></el-table-column>
        <el-table-column prop="code" label="课程代码" width="130"></el-table-column>
        <el-table-column prop="semesterId" label="学期" width="160">
          <template slot-scope="scope">
            {{ getSemesterName(scope.row.semesterId) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" style="color: #10b981;" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" style="color: #ef4444;" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page.sync="currentPage"
          @current-change="fetchData"
        ></el-pagination>
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog
      :title="dialogType === 'add' ? '新增课程' : '编辑课程'"
      :visible.sync="dialogVisible"
      width="520px"
      custom-class="dark-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="courseForm" label-width="80px" class="dark-form">
        <el-form-item label="课程名" prop="name">
          <el-input v-model="form.name" placeholder="请输入课程名"></el-input>
        </el-form-item>
        <el-form-item label="课程代码" prop="code">
          <el-input v-model="form.code" placeholder="请输入课程代码"></el-input>
        </el-form-item>
        <el-form-item label="学期" prop="semesterId">
          <el-select v-model="form.semesterId" placeholder="请选择学期" style="width: 100%;">
            <el-option
              v-for="s in semesterList"
              :key="s.id"
              :label="s.name"
              :value="s.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCourseList, addCourse, updateCourse, deleteCourse, getSemesterList } from '../../api'

export default {
  name: 'CourseManage',
  data() {
    return {
      keyword: '',
      tableData: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      semesterList: [],
      dialogVisible: false,
      dialogType: 'add',
      submitting: false,
      form: {
        name: '',
        code: '',
        semesterId: '',
        description: ''
      },
      rules: {
        name: [{ required: true, message: '请输入课程名', trigger: 'blur' }],
        code: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
        semesterId: [{ required: true, message: '请选择学期', trigger: 'change' }]
      },
      headerCellStyle: {
        background: '#0f172a',
        color: '#e2e8f0',
        borderBottom: '1px solid #334155'
      },
      cellStyle: {
        background: '#1e293b',
        color: '#e2e8f0',
        borderBottom: '1px solid #334155'
      }
    }
  },
  mounted() {
    this.fetchData()
    this.fetchSemesters()
  },
  methods: {
    async fetchData() {
      try {
        const res = await getCourseList({
          page: this.currentPage,
          size: this.pageSize,
          keyword: this.keyword || undefined
        })
        if (res.data.code === 200) {
          this.tableData = res.data.data.records || []
          this.total = res.data.data.total || 0
        }
      } catch (e) {
        this.$message.error('获取课程列表失败')
      }
    },
    async fetchSemesters() {
      try {
        const res = await getSemesterList()
        if (res.data.code === 200) {
          this.semesterList = res.data.data || []
        }
      } catch (e) {
        console.error('获取学期列表失败', e)
      }
    },
    getSemesterName(id) {
      const s = this.semesterList.find(item => item.id === id)
      return s ? s.name : id
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchData()
    },
    handleAdd() {
      this.dialogType = 'add'
      this.form = { name: '', code: '', semesterId: '', description: '' }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.courseForm && this.$refs.courseForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.form = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.courseForm && this.$refs.courseForm.clearValidate()
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该课程吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteCourse(row.id)
          if (res.data.code === 200) {
            this.$message.success('删除成功')
            this.fetchData()
          } else {
            this.$message.error(res.data.msg || '删除失败')
          }
        } catch (e) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    submitForm() {
      this.$refs.courseForm.validate(async (valid) => {
        if (!valid) return
        this.submitting = true
        try {
          let res
          if (this.dialogType === 'add') {
            res = await addCourse(this.form)
          } else {
            res = await updateCourse(this.form.id, this.form)
          }
          if (res.data.code === 200) {
            this.$message.success(this.dialogType === 'add' ? '添加成功' : '更新成功')
            this.dialogVisible = false
            this.fetchData()
          } else {
            this.$message.error(res.data.msg || '操作失败')
          }
        } catch (e) {
          this.$message.error('操作失败')
        } finally {
          this.submitting = false
        }
      })
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.dark-card {
  background: #1e293b;
  border-radius: 12px;
  border: 1px solid #334155;
  padding: 20px;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dark-table {
  background: transparent !important;
  color: #e2e8f0;
}
.dark-table::before {
  display: none;
}
>>> .dark-table .el-table__body tr:hover > td {
  background: #334155 !important;
}
>>> .dark-table .el-table__body tr.el-table__row--striped td {
  background: #162032 !important;
}
>>> .dark-table th.el-table__cell {
  background: #0f172a !important;
  color: #e2e8f0;
  border-bottom: 1px solid #334155;
}
>>> .dark-table td.el-table__cell {
  border-bottom: 1px solid #334155;
}
>>> .dark-table .el-table__body-wrapper {
  background: #1e293b;
}
>>> .dark-table .el-table__empty-block {
  background: #1e293b;
  color: #64748b;
}

>>> .dark-dialog {
  background: #1e293b;
  border-radius: 12px;
  border: 1px solid #334155;
}
>>> .dark-dialog .el-dialog__header {
  border-bottom: 1px solid #334155;
}
>>> .dark-dialog .el-dialog__title {
  color: #e2e8f0;
}
>>> .dark-dialog .el-dialog__body {
  color: #e2e8f0;
}
>>> .dark-dialog .el-dialog__footer {
  border-top: 1px solid #334155;
}

>>> .dark-form .el-form-item__label {
  color: #cbd5e1;
}
>>> .dark-form .el-input__inner,
>>> .dark-form .el-textarea__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}
>>> .dark-form .el-input__inner:focus,
>>> .dark-form .el-textarea__inner:focus {
  border-color: #10b981;
}
>>> .dark-form .el-select .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}

>>> .dark-input .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}

>>> .el-pagination.is-background .el-pager li {
  background: #0f172a;
  color: #e2e8f0;
  border: 1px solid #334155;
}
>>> .el-pagination.is-background .el-pager li:not(.disabled).active {
  background: #10b981;
  border-color: #10b981;
}
>>> .el-pagination.is-background .btn-prev,
>>> .el-pagination.is-background .btn-next {
  background: #0f172a;
  color: #e2e8f0;
  border: 1px solid #334155;
}
>>> .el-pagination__total {
  color: #94a3b8;
}
>>> .el-pagination__jump {
  color: #94a3b8;
}
>>> .el-pagination__editor .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}
</style>
