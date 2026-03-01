<template>
  <div class="page-container">
    <div class="dark-card">
      <!-- Header -->
      <div class="search-bar">
        <span class="page-title">评价维度管理</span>
        <el-button type="success" icon="el-icon-plus" @click="handleAdd" style="margin-left: auto;">新增维度</el-button>
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
        <el-table-column prop="name" label="维度名" min-width="150"></el-table-column>
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip></el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100"></el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" style="color: #10b981;" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" style="color: #ef4444;" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog
      :title="dialogType === 'add' ? '新增维度' : '编辑维度'"
      :visible.sync="dialogVisible"
      width="500px"
      custom-class="dark-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="dimensionForm" label-width="70px" class="dark-form">
        <el-form-item label="维度名" prop="name">
          <el-input v-model="form.name" placeholder="请输入维度名"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述"></el-input>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%;"></el-input-number>
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
import { getDimensionList, addDimension, updateDimension, deleteDimension } from '../../api'

export default {
  name: 'DimensionManage',
  data() {
    return {
      tableData: [],
      dialogVisible: false,
      dialogType: 'add',
      submitting: false,
      form: {
        name: '',
        description: '',
        sortOrder: 0
      },
      rules: {
        name: [{ required: true, message: '请输入维度名', trigger: 'blur' }],
        sortOrder: [{ required: true, message: '请输入排序值', trigger: 'blur' }]
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
  },
  methods: {
    async fetchData() {
      try {
        const res = await getDimensionList()
        if (res.data.code === 200) {
          this.tableData = res.data.data || []
        }
      } catch (e) {
        this.$message.error('获取维度列表失败')
      }
    },
    handleAdd() {
      this.dialogType = 'add'
      this.form = { name: '', description: '', sortOrder: 0 }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.dimensionForm && this.$refs.dimensionForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.form = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.dimensionForm && this.$refs.dimensionForm.clearValidate()
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该维度吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteDimension(row.id)
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
      this.$refs.dimensionForm.validate(async (valid) => {
        if (!valid) return
        this.submitting = true
        try {
          let res
          if (this.dialogType === 'add') {
            res = await addDimension(this.form)
          } else {
            res = await updateDimension(this.form.id, this.form)
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

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #e2e8f0;
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
>>> .dark-form .el-input-number .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}
</style>
