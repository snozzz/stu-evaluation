<template>
  <div class="page-container">
    <div class="light-card">
      <div class="card-header">
        <span class="page-title">系统配置</span>
      </div>

      <div v-if="loading" class="loading-text">加载中...</div>

      <div v-else class="config-list">
        <div v-for="item in configList" :key="item.id" class="config-item">
          <div class="config-info">
            <div class="config-label">{{ item.remark || item.configKey }}</div>
            <div class="config-key">{{ item.configKey }}</div>
          </div>

          <!-- Logo upload for system_logo -->
          <template v-if="item.configKey === 'system_logo'">
            <div class="config-value logo-value">
              <div class="logo-preview" v-if="item.configValue">
                <img :src="item.configValue" class="logo-preview-img" />
              </div>
              <div class="logo-preview logo-placeholder" v-else>
                <i class="el-icon-picture-outline"></i>
                <span>暂无Logo</span>
              </div>
              <el-upload
                class="logo-upload"
                action="/api/file/upload"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="(res) => handleLogoUploadSuccess(res, item)"
                :before-upload="beforeLogoUpload"
                accept="image/*"
              >
                <el-button size="small" type="primary" icon="el-icon-upload2">上传Logo</el-button>
              </el-upload>
            </div>
          </template>

          <!-- Normal text input for other configs -->
          <template v-else>
            <div class="config-value">
              <el-input
                v-model="item.configValue"
                placeholder="请输入配置值"
                size="medium"
                class="config-input"
              ></el-input>
            </div>
          </template>

          <div class="config-action">
            <el-button
              type="primary"
              size="small"
              icon="el-icon-check"
              :loading="item._saving"
              @click="handleSave(item)"
            >保存</el-button>
          </div>
        </div>

        <div v-if="configList.length === 0" class="empty-text">暂无配置项</div>
      </div>
    </div>
  </div>
</template>

<script>
import { getConfigs, updateConfig } from '../../api'

export default {
  name: 'ConfigManage',
  data() {
    return {
      configList: [],
      loading: false
    }
  },
  computed: {
    uploadHeaders() {
      const token = this.$store.state.token
      return token ? { Authorization: 'Bearer ' + token } : {}
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await getConfigs()
        if (res.data.code === 200) {
          this.configList = (res.data.data || []).map(item => ({
            ...item,
            _saving: false
          }))
        }
      } catch (e) {
        this.$message.error('获取配置列表失败')
      } finally {
        this.loading = false
      }
    },
    beforeLogoUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isImage) {
        this.$message.error('只能上传图片文件')
        return false
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB')
        return false
      }
      return true
    },
    handleLogoUploadSuccess(res, item) {
      if (res.code === 200 && res.data) {
        item.configValue = res.data
        this.handleSave(item)
      } else {
        this.$message.error(res.msg || '上传失败')
      }
    },
    async handleSave(item) {
      if (item.configKey !== 'system_logo' && !item.configValue && item.configValue !== '0') {
        this.$message.warning('配置值不能为空')
        return
      }
      this.$set(item, '_saving', true)
      try {
        const res = await updateConfig({
          configKey: item.configKey,
          configValue: item.configValue
        })
        if (res.data.code === 200) {
          this.$message.success(`"${item.remark || item.configKey}" 保存成功`)
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      } catch (e) {
        this.$message.error('保存失败')
      } finally {
        this.$set(item, '_saving', false)
      }
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

.card-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.config-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.config-item {
  display: flex;
  align-items: center;
  padding: 18px 16px;
  border-bottom: 1px solid #e5e7eb;
  gap: 20px;
  transition: background 0.2s;
}

.config-item:last-child {
  border-bottom: none;
}

.config-item:hover {
  background: rgba(97, 191, 173, 0.03);
}

.config-info {
  width: 240px;
  flex-shrink: 0;
}

.config-label {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
  margin-bottom: 4px;
}

.config-key {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.config-value {
  flex: 1;
}

.logo-value {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-preview {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
}

.logo-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.logo-placeholder {
  flex-direction: column;
  gap: 4px;
  color: #909399;
  font-size: 12px;
}

.logo-placeholder i {
  font-size: 20px;
}

.config-action {
  flex-shrink: 0;
}

.loading-text {
  text-align: center;
  color: #909399;
  padding: 60px 0;
  font-size: 14px;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 60px 0;
  font-size: 14px;
}
</style>
