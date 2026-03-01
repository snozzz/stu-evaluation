<template>
  <div class="page-container">
    <div class="dark-card">
      <!-- Header -->
      <div class="search-bar">
        <span class="page-title">轮播图管理</span>
        <el-button type="success" icon="el-icon-plus" @click="handleAdd" style="margin-left: auto;">新增轮播</el-button>
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
        <el-table-column prop="title" label="标题" min-width="150"></el-table-column>
        <el-table-column prop="imageUrl" label="图片预览" width="140">
          <template slot-scope="scope">
            <el-image
              v-if="scope.row.imageUrl"
              :src="scope.row.imageUrl"
              :preview-src-list="[scope.row.imageUrl]"
              style="width: 100px; height: 56px; border-radius: 4px;"
              fit="cover"
            ></el-image>
            <span v-else style="color: #64748b;">无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="linkUrl" label="链接" min-width="180" show-overflow-tooltip></el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80"></el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" effect="dark" size="small">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
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
      :title="dialogType === 'add' ? '新增轮播' : '编辑轮播'"
      :visible.sync="dialogVisible"
      width="550px"
      custom-class="dark-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="carouselForm" label-width="80px" class="dark-form">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="图片地址" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="请输入图片URL或上传图片">
            <el-upload
              slot="append"
              action="/api/file/upload"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
              :headers="uploadHeaders"
            >
              <el-button size="small" icon="el-icon-upload">上传</el-button>
            </el-upload>
          </el-input>
          <el-image
            v-if="form.imageUrl"
            :src="form.imageUrl"
            style="width: 200px; height: 112px; margin-top: 8px; border-radius: 6px; border: 1px solid #334155;"
            fit="cover"
          ></el-image>
        </el-form-item>
        <el-form-item label="链接地址">
          <el-input v-model="form.linkUrl" placeholder="请输入跳转链接"></el-input>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-color="#10b981"
            active-text="启用"
            inactive-text="禁用"
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
import { getCarouselList, addCarousel, updateCarousel, deleteCarousel } from '../../api'

export default {
  name: 'CarouselManage',
  data() {
    return {
      tableData: [],
      dialogVisible: false,
      dialogType: 'add',
      submitting: false,
      form: {
        title: '',
        imageUrl: '',
        linkUrl: '',
        sortOrder: 0,
        status: 1
      },
      rules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        imageUrl: [{ required: true, message: '请输入图片地址', trigger: 'blur' }]
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
  computed: {
    uploadHeaders() {
      return { Authorization: 'Bearer ' + this.$store.state.token }
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      try {
        const res = await getCarouselList()
        if (res.data.code === 200) {
          this.tableData = res.data.data || []
        }
      } catch (e) {
        this.$message.error('获取轮播列表失败')
      }
    },
    handleAdd() {
      this.dialogType = 'add'
      this.form = { title: '', imageUrl: '', linkUrl: '', sortOrder: 0, status: 1 }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.carouselForm && this.$refs.carouselForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.form = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.carouselForm && this.$refs.carouselForm.clearValidate()
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该轮播图吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteCarousel(row.id)
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
    handleUploadSuccess(response) {
      if (response.code === 200) {
        this.form.imageUrl = response.data
        this.$message.success('上传成功')
      } else {
        this.$message.error('上传失败')
      }
    },
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isImage) {
        this.$message.error('只能上传图片文件')
        return false
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过2MB')
        return false
      }
      return true
    },
    submitForm() {
      this.$refs.carouselForm.validate(async (valid) => {
        if (!valid) return
        this.submitting = true
        try {
          let res
          if (this.dialogType === 'add') {
            res = await addCarousel(this.form)
          } else {
            res = await updateCarousel(this.form.id, this.form)
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
>>> .dark-form .el-input-group__append {
  background: #334155;
  border-color: #334155;
  color: #e2e8f0;
}
>>> .dark-form .el-input-number .el-input__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
}
</style>
