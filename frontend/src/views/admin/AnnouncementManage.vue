<template>
  <div class="page-container">
    <div class="dark-card">
      <!-- Header -->
      <div class="search-bar">
        <span class="page-title">公告管理</span>
        <el-button type="success" icon="el-icon-plus" @click="handleAdd" style="margin-left: auto;">发布公告</el-button>
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
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="publisherId" label="发布者" width="100"></el-table-column>
        <el-table-column prop="isTop" label="置顶" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isTop === 1 ? 'danger' : 'info'" effect="dark" size="small">
              {{ scope.row.isTop === 1 ? '置顶' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'" effect="dark" size="small">
              {{ scope.row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
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
      :title="dialogType === 'add' ? '发布公告' : '编辑公告'"
      :visible.sync="dialogVisible"
      width="600px"
      custom-class="dark-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="announcementForm" label-width="70px" class="dark-form">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题"></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
          ></el-input>
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch
            v-model="form.isTop"
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
import { getAnnouncements, addAnnouncement, updateAnnouncement, deleteAnnouncement } from '../../api'

export default {
  name: 'AnnouncementManage',
  data() {
    return {
      tableData: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      dialogVisible: false,
      dialogType: 'add',
      submitting: false,
      form: {
        title: '',
        content: '',
        isTop: 0
      },
      rules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
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
        const res = await getAnnouncements({ page: this.currentPage, size: this.pageSize })
        if (res.data.code === 200) {
          this.tableData = res.data.data.records || []
          this.total = res.data.data.total || 0
        }
      } catch (e) {
        this.$message.error('获取公告列表失败')
      }
    },
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const h = String(d.getHours()).padStart(2, '0')
      const min = String(d.getMinutes()).padStart(2, '0')
      return `${y}-${m}-${day} ${h}:${min}`
    },
    handleAdd() {
      this.dialogType = 'add'
      this.form = { title: '', content: '', isTop: 0 }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.announcementForm && this.$refs.announcementForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.form = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.announcementForm && this.$refs.announcementForm.clearValidate()
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该公告吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteAnnouncement(row.id)
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
      this.$refs.announcementForm.validate(async (valid) => {
        if (!valid) return
        this.submitting = true
        try {
          let res
          if (this.dialogType === 'add') {
            this.form.status = 1
            res = await addAnnouncement(this.form)
          } else {
            res = await updateAnnouncement(this.form.id, this.form)
          }
          if (res.data.code === 200) {
            this.$message.success(this.dialogType === 'add' ? '发布成功' : '更新成功')
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
