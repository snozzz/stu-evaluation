<template>
  <div class="page-container">
    <div class="light-card">
      <!-- Search Bar -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索班级名称"
          clearable
          style="width: 250px; margin-right: 12px;"
          @keyup.enter.native="handleSearch"
        >
          <i slot="prefix" class="el-icon-search"></i>
        </el-input>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        <el-button type="success" icon="el-icon-plus" @click="handleAdd" style="margin-left: auto;">新增班级</el-button>
      </div>

      <!-- Table -->
      <el-table
        :data="tableData"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column prop="name" label="班级名" min-width="150"></el-table-column>
        <el-table-column prop="college" label="学院" min-width="150"></el-table-column>
        <el-table-column prop="major" label="专业" min-width="150"></el-table-column>
        <el-table-column prop="grade" label="年级" width="120"></el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" style="color: #61BFAD;" @click="handleEdit(scope.row)">编辑</el-button>
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
      :title="dialogType === 'add' ? '新增班级' : '编辑班级'"
      :visible.sync="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="classForm" label-width="70px">
        <el-form-item label="班级名" prop="name">
          <el-input v-model="form.name" placeholder="请输入班级名"></el-input>
        </el-form-item>
        <el-form-item label="学院" prop="college">
          <el-input v-model="form.college" placeholder="请输入学院"></el-input>
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" placeholder="请输入专业"></el-input>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" placeholder="请输入年级"></el-input>
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
import { getClassList, addClass, updateClass, deleteClass } from '../../api'

export default {
  name: 'ClassManage',
  data() {
    return {
      keyword: '',
      tableData: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      dialogVisible: false,
      dialogType: 'add',
      submitting: false,
      form: {
        name: '',
        college: '',
        major: '',
        grade: ''
      },
      rules: {
        name: [{ required: true, message: '请输入班级名', trigger: 'blur' }],
        college: [{ required: true, message: '请输入学院', trigger: 'blur' }],
        major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
        grade: [{ required: true, message: '请输入年级', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      try {
        const res = await getClassList({
          page: this.currentPage,
          size: this.pageSize,
          keyword: this.keyword || undefined
        })
        if (res.data.code === 200) {
          this.tableData = res.data.data.records || []
          this.total = res.data.data.total || 0
        }
      } catch (e) {
        this.$message.error('获取班级列表失败')
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchData()
    },
    handleAdd() {
      this.dialogType = 'add'
      this.form = { name: '', college: '', major: '', grade: '' }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.classForm && this.$refs.classForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.form = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.classForm && this.$refs.classForm.clearValidate()
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该班级吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteClass(row.id)
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
      this.$refs.classForm.validate(async (valid) => {
        if (!valid) return
        this.submitting = true
        try {
          let res
          if (this.dialogType === 'add') {
            res = await addClass(this.form)
          } else {
            res = await updateClass(this.form.id, this.form)
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

.light-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 0;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
