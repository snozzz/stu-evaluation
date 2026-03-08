<template>
  <div class="page-container">
    <div class="light-card">
      <!-- Search Bar -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索学号/姓名"
          clearable
          style="width: 220px; margin-right: 12px;"
          @keyup.enter.native="handleSearch"
        >
          <i slot="prefix" class="el-icon-search"></i>
        </el-input>
        <el-select
          v-model="roleFilter"
          placeholder="角色筛选"
          clearable
          style="width: 150px; margin-right: 12px;"
          @change="handleSearch"
        >
          <el-option label="全部" value=""></el-option>
          <el-option label="管理员" value="ADMIN"></el-option>
          <el-option label="教师" value="TEACHER"></el-option>
          <el-option label="学生" value="STUDENT"></el-option>
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        <el-button type="success" icon="el-icon-plus" @click="handleAdd" style="margin-left: auto;">新增用户</el-button>
      </div>

      <!-- Table -->
      <el-table
        :data="tableData"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column label="头像" width="70" align="center">
          <template slot-scope="scope">
            <el-avatar :size="36" :src="scope.row.avatar" icon="el-icon-user-solid"></el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号" width="130"></el-table-column>
        <el-table-column prop="realName" label="姓名" width="120"></el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template slot-scope="scope">
            <el-tag
              :type="roleTagType(scope.row.role)"
              size="small"
            >{{ roleLabel(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="college" label="学院" min-width="140"></el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
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
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      :visible.sync="dialogVisible"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="userForm" label-width="80px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号" :disabled="dialogType === 'edit'"></el-input>
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="教师" value="TEACHER"></el-option>
            <el-option label="学生" value="STUDENT"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="form.college" placeholder="请输入学院"></el-input>
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="form.major" placeholder="请输入专业"></el-input>
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="form.className" placeholder="请输入班级"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
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
import { getUserList, addUser, updateUser, deleteUser } from '../../api'

export default {
  name: 'UserManage',
  data() {
    return {
      keyword: '',
      roleFilter: '',
      tableData: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      dialogVisible: false,
      dialogType: 'add',
      submitting: false,
      form: {
        username: '',
        password: '',
        realName: '',
        role: '',
        college: '',
        major: '',
        className: '',
        studentNo: '',
        phone: '',
        email: ''
      },
      rules: {
        studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      try {
        const res = await getUserList({
          page: this.currentPage,
          size: this.pageSize,
          role: this.roleFilter || undefined,
          keyword: this.keyword || undefined
        })
        if (res.data.code === 200) {
          this.tableData = res.data.data.records || []
          this.total = res.data.data.total || 0
        }
      } catch (e) {
        this.$message.error('获取用户列表失败')
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchData()
    },
    handleAdd() {
      this.dialogType = 'add'
      this.form = {
        username: '',
        password: '',
        realName: '',
        role: '',
        college: '',
        major: '',
        className: '',
        studentNo: '',
        phone: '',
        email: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.userForm && this.$refs.userForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.form = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.userForm && this.$refs.userForm.clearValidate()
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteUser(row.id)
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
      this.$refs.userForm.validate(async (valid) => {
        if (!valid) return
        this.submitting = true
        try {
          let res
          if (this.dialogType === 'add') {
            res = await addUser(this.form)
          } else {
            res = await updateUser(this.form.id, this.form)
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
    },
    roleLabel(role) {
      const map = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
      return map[role] || role
    },
    roleTagType(role) {
      const map = { ADMIN: 'danger', TEACHER: 'warning', STUDENT: '' }
      return map[role] || 'info'
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
