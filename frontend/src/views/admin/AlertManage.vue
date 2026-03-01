<template>
  <div class="page-container">
    <div class="dark-card">
      <!-- Header -->
      <div class="search-bar">
        <span class="page-title">预警规则管理</span>
        <el-button type="success" icon="el-icon-plus" @click="handleAdd" style="margin-left: auto;">新增规则</el-button>
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
        <el-table-column prop="name" label="规则名" min-width="160"></el-table-column>
        <el-table-column prop="ruleType" label="类型" width="150">
          <template slot-scope="scope">
            <el-tag :type="ruleTypeTag(scope.row.ruleType)" effect="dark" size="small">
              {{ ruleTypeLabel(scope.row.ruleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="threshold" label="阈值" width="100"></el-table-column>
        <el-table-column prop="isEnabled" label="是否启用" width="120">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.isEnabled === 1"
              active-color="#10b981"
              inactive-color="#475569"
              @change="handleToggleEnable(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
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
      :title="dialogType === 'add' ? '新增规则' : '编辑规则'"
      :visible.sync="dialogVisible"
      width="500px"
      custom-class="dark-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="alertForm" label-width="80px" class="dark-form">
        <el-form-item label="规则名" prop="name">
          <el-input v-model="form.name" placeholder="请输入规则名称"></el-input>
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" placeholder="请选择规则类型" style="width: 100%;">
            <el-option label="成绩过低" value="SCORE_LOW"></el-option>
            <el-option label="作业缺交" value="HOMEWORK_MISSING"></el-option>
            <el-option label="评价延迟" value="EVAL_DELAY"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="阈值" prop="threshold">
          <el-input-number
            v-model="form.threshold"
            :min="0"
            :precision="1"
            style="width: 100%;"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch
            v-model="form.isEnabled"
            :active-value="1"
            :inactive-value="0"
            active-color="#10b981"
          ></el-switch>
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
import { getAlertRules, addAlertRule, updateAlertRule, deleteAlertRule } from '../../api'

export default {
  name: 'AlertManage',
  data() {
    return {
      tableData: [],
      dialogVisible: false,
      dialogType: 'add',
      submitting: false,
      form: {
        name: '',
        ruleType: '',
        threshold: 0,
        isEnabled: 1
      },
      rules: {
        name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
        ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
        threshold: [{ required: true, message: '请输入阈值', trigger: 'blur' }]
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
        const res = await getAlertRules()
        if (res.data.code === 200) {
          this.tableData = res.data.data || []
        }
      } catch (e) {
        this.$message.error('获取规则列表失败')
      }
    },
    ruleTypeLabel(type) {
      const map = {
        SCORE_LOW: '成绩过低',
        HOMEWORK_MISSING: '作业缺交',
        EVAL_DELAY: '评价延迟'
      }
      return map[type] || type
    },
    ruleTypeTag(type) {
      const map = {
        SCORE_LOW: 'danger',
        HOMEWORK_MISSING: 'warning',
        EVAL_DELAY: ''
      }
      return map[type] || 'info'
    },
    async handleToggleEnable(row) {
      try {
        const updated = { ...row, isEnabled: row.isEnabled === 1 ? 0 : 1 }
        const res = await updateAlertRule(row.id, updated)
        if (res.data.code === 200) {
          this.$message.success(updated.isEnabled === 1 ? '已启用' : '已禁用')
          this.fetchData()
        } else {
          this.$message.error(res.data.msg || '操作失败')
        }
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    handleAdd() {
      this.dialogType = 'add'
      this.form = { name: '', ruleType: '', threshold: 0, isEnabled: 1 }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.alertForm && this.$refs.alertForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.form = { ...row, threshold: Number(row.threshold) }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.alertForm && this.$refs.alertForm.clearValidate()
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该规则吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteAlertRule(row.id)
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
      this.$refs.alertForm.validate(async (valid) => {
        if (!valid) return
        this.submitting = true
        try {
          let res
          if (this.dialogType === 'add') {
            res = await addAlertRule(this.form)
          } else {
            res = await updateAlertRule(this.form.id, this.form)
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
>>> .dark-form .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}
>>> .dark-form .el-input__inner:focus {
  border-color: #10b981;
}
>>> .dark-form .el-select .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}
>>> .dark-form .el-input-number .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}
>>> .dark-form .el-input-number .el-input-number__decrease,
>>> .dark-form .el-input-number .el-input-number__increase {
  background: #334155;
  border-color: #334155;
  color: #e2e8f0;
}
</style>
