<template>
  <div class="page-container">
    <div class="light-card">
      <!-- Header -->
      <div class="search-bar">
        <span class="page-title">学期管理</span>
        <el-button type="success" icon="el-icon-plus" @click="handleAdd" style="margin-left: auto;">新增学期</el-button>
      </div>

      <!-- Table -->
      <el-table
        :data="tableData"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column prop="name" label="学期名" min-width="200"></el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="isCurrent" label="是否当前" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isCurrent === 1 ? 'success' : 'info'" effect="dark" size="small">
              {{ scope.row.isCurrent === 1 ? '当前学期' : '非当前' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" style="color: #61BFAD;" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              type="text"
              style="color: #f59e0b;"
              @click="handleSetCurrent(scope.row)"
              :disabled="scope.row.isCurrent === 1"
            >设为当前</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog
      :title="dialogType === 'add' ? '新增学期' : '编辑学期'"
      :visible.sync="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="semesterForm" label-width="80px">
        <el-form-item label="学期名" prop="name">
          <el-input v-model="form.name" placeholder="例如：2025-2026学年第一学期"></el-input>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="选择开始日期"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="选择结束日期"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          ></el-date-picker>
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
import { getSemesterList, addSemester, updateSemester, setCurrentSemester } from '../../api'

export default {
  name: 'SemesterManage',
  data() {
    return {
      tableData: [],
      dialogVisible: false,
      dialogType: 'add',
      submitting: false,
      form: {
        name: '',
        startDate: '',
        endDate: ''
      },
      rules: {
        name: [{ required: true, message: '请输入学期名', trigger: 'blur' }],
        startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
        endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      try {
        const res = await getSemesterList()
        if (res.data.code === 200) {
          this.tableData = res.data.data || []
        }
      } catch (e) {
        this.$message.error('获取学期列表失败')
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },
    handleAdd() {
      this.dialogType = 'add'
      this.form = { name: '', startDate: '', endDate: '' }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.semesterForm && this.$refs.semesterForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.form = {
        ...row,
        startDate: this.formatDate(row.startDate),
        endDate: this.formatDate(row.endDate)
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.semesterForm && this.$refs.semesterForm.clearValidate()
      })
    },
    handleSetCurrent(row) {
      this.$confirm(`确认将"${row.name}"设为当前学期吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await setCurrentSemester(row.id)
          if (res.data.code === 200) {
            this.$message.success('设置成功')
            this.fetchData()
          } else {
            this.$message.error(res.data.msg || '设置失败')
          }
        } catch (e) {
          this.$message.error('设置失败')
        }
      }).catch(() => {})
    },
    submitForm() {
      this.$refs.semesterForm.validate(async (valid) => {
        if (!valid) return
        this.submitting = true
        try {
          let res
          if (this.dialogType === 'add') {
            res = await addSemester(this.form)
          } else {
            res = await updateSemester(this.form.id, this.form)
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

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}
</style>
