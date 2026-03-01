<template>
  <div class="appeals-page">
    <div class="dark-card">
      <div class="card-header">
        <span class="card-title">学生反馈与申诉</span>
        <div class="header-stats">
          <el-tag type="warning" effect="dark" size="small">
            待处理: {{ pendingCount }}
          </el-tag>
        </div>
      </div>

      <el-table
        :data="appeals"
        style="width: 100%"
        class="dark-table"
        :header-cell-style="tableHeaderStyle"
        :cell-style="tableCellStyle"
        v-loading="loading"
        element-loading-background="rgba(30, 41, 59, 0.8)"
        empty-text="暂无申诉记录"
      >
        <el-table-column type="index" label="#" width="50"></el-table-column>
        <el-table-column label="学生姓名" width="120">
          <template slot-scope="scope">
            <span class="student-name">{{ scope.row.studentName || ('学生ID: ' + scope.row.studentId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="课程" width="160">
          <template slot-scope="scope">
            {{ scope.row.courseName || ('课程ID: ' + scope.row.courseId) }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="申诉内容" show-overflow-tooltip min-width="200">
          <template slot-scope="scope">
            <span class="appeal-content">{{ scope.row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag
              :type="statusTagType(scope.row.status)"
              size="small"
              effect="dark"
            >
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="回复" show-overflow-tooltip width="200">
          <template slot-scope="scope">
            <span v-if="scope.row.reply" class="reply-text">{{ scope.row.reply }}</span>
            <span v-else class="no-reply">暂无回复</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              class="reply-btn"
              @click="openReplyDialog(scope.row)"
            >
              <i class="el-icon-chat-dot-round"></i>
              {{ scope.row.status === 'PENDING' ? '回复' : '查看' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Reply Dialog -->
    <el-dialog
      title="处理申诉"
      :visible.sync="replyDialog.visible"
      width="560px"
      custom-class="dark-dialog"
      :close-on-click-modal="false"
    >
      <div class="reply-dialog-content">
        <!-- Appeal Info -->
        <div class="appeal-info-block">
          <div class="info-row">
            <span class="info-label">学生：</span>
            <span class="info-value">{{ replyDialog.studentName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">课程：</span>
            <span class="info-value">{{ replyDialog.courseName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">状态：</span>
            <el-tag
              :type="statusTagType(replyDialog.status)"
              size="small"
              effect="dark"
            >
              {{ statusText(replyDialog.status) }}
            </el-tag>
          </div>
        </div>

        <!-- Appeal Content -->
        <div class="appeal-content-block">
          <div class="block-label">申诉内容</div>
          <div class="block-text">{{ replyDialog.content }}</div>
        </div>

        <!-- Existing Reply -->
        <div v-if="replyDialog.existingReply" class="existing-reply-block">
          <div class="block-label">已有回复</div>
          <div class="block-text reply-existing">{{ replyDialog.existingReply }}</div>
        </div>

        <!-- Reply Input -->
        <div class="reply-input-block">
          <div class="block-label">
            {{ replyDialog.status === 'PENDING' ? '撰写回复' : '修改回复' }}
          </div>
          <el-input
            v-model="replyDialog.replyText"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容..."
            maxlength="500"
            show-word-limit
          ></el-input>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="replyDialog.visible = false" class="cancel-btn">取 消</el-button>
        <el-button
          type="warning"
          @click="handleReply"
          :loading="replyDialog.loading"
          :disabled="!replyDialog.replyText.trim()"
        >
          提交回复
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTeacherAppeals, replyAppeal } from '../../api'

export default {
  name: 'TeacherAppeals',
  data() {
    return {
      appeals: [],
      loading: false,
      replyDialog: {
        visible: false,
        id: null,
        studentName: '',
        courseName: '',
        content: '',
        status: '',
        existingReply: '',
        replyText: '',
        loading: false
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
  computed: {
    pendingCount() {
      return this.appeals.filter(a => a.status === 'PENDING').length
    }
  },
  created() {
    this.loadAppeals()
  },
  methods: {
    async loadAppeals() {
      this.loading = true
      try {
        const res = await getTeacherAppeals()
        this.appeals = (res.data.data || []).map(a => ({
          ...a,
          studentName: a.studentName || null,
          courseName: a.courseName || null
        }))
      } catch (e) {
        console.error('Load appeals error:', e)
        this.$message.error('加载申诉列表失败')
      } finally {
        this.loading = false
      }
    },
    openReplyDialog(row) {
      this.replyDialog.id = row.id
      this.replyDialog.studentName = row.studentName || ('学生ID: ' + row.studentId)
      this.replyDialog.courseName = row.courseName || ('课程ID: ' + row.courseId)
      this.replyDialog.content = row.content
      this.replyDialog.status = row.status
      this.replyDialog.existingReply = row.reply || ''
      this.replyDialog.replyText = row.reply || ''
      this.replyDialog.loading = false
      this.replyDialog.visible = true
    },
    async handleReply() {
      if (!this.replyDialog.replyText.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }

      this.replyDialog.loading = true
      try {
        await replyAppeal(this.replyDialog.id, {
          reply: this.replyDialog.replyText.trim()
        })
        this.$message.success('回复成功')
        this.replyDialog.visible = false
        this.loadAppeals()
      } catch (e) {
        this.$message.error('回复失败')
      } finally {
        this.replyDialog.loading = false
      }
    },
    statusTagType(status) {
      const map = { PENDING: 'warning', REPLIED: 'success', CLOSED: 'info' }
      return map[status] || 'info'
    },
    statusText(status) {
      const map = { PENDING: '待处理', REPLIED: '已回复', CLOSED: '已关闭' }
      return map[status] || status
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
.appeals-page {
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

.student-name {
  color: #f59e0b;
  font-weight: 500;
}

.appeal-content {
  color: #cbd5e1;
}

.reply-text {
  color: #10b981;
}

.no-reply {
  color: #475569;
  font-style: italic;
}

.reply-btn {
  color: #f59e0b !important;
}

.reply-btn:hover {
  color: #fbbf24 !important;
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

.dark-table >>> .el-table__body-wrapper::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.dark-table >>> .el-table__body-wrapper::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 6px;
}

.dark-table >>> .el-table__body-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

/* Dialog Styles */
.appeals-page >>> .dark-dialog {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 12px;
}

.appeals-page >>> .dark-dialog .el-dialog__header {
  border-bottom: 1px solid #334155;
  padding: 16px 20px;
}

.appeals-page >>> .dark-dialog .el-dialog__title {
  color: #e2e8f0;
  font-size: 16px;
  font-weight: 600;
}

.appeals-page >>> .dark-dialog .el-dialog__headerbtn .el-dialog__close {
  color: #94a3b8;
}

.appeals-page >>> .dark-dialog .el-dialog__body {
  padding: 20px;
}

.appeals-page >>> .dark-dialog .el-dialog__footer {
  border-top: 1px solid #334155;
  padding: 12px 20px;
}

/* Reply Dialog Content */
.reply-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.appeal-info-block {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 12px 16px;
  background: #162032;
  border-radius: 8px;
  border: 1px solid #334155;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.info-label {
  color: #64748b;
  font-size: 13px;
}

.info-value {
  color: #e2e8f0;
  font-size: 13px;
  font-weight: 500;
}

.block-label {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 8px;
  font-weight: 600;
}

.block-text {
  background: #0f172a;
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 12px 14px;
  color: #cbd5e1;
  font-size: 14px;
  line-height: 1.6;
}

.reply-existing {
  color: #10b981;
  border-color: rgba(16, 185, 129, 0.3);
}

/* Textarea in Dialog */
.appeals-page >>> .el-textarea__inner {
  background: #0f172a;
  border-color: #334155;
  color: #e2e8f0;
  border-radius: 8px;
}

.appeals-page >>> .el-textarea__inner:focus {
  border-color: #f59e0b;
}

.appeals-page >>> .el-textarea__inner::placeholder {
  color: #475569;
}

.appeals-page >>> .el-input__count {
  background: transparent;
  color: #475569;
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
