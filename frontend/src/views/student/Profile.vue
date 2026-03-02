<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- Left: Avatar Card -->
      <el-col :span="8">
        <div class="light-card avatar-card">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-avatar :size="100" :src="avatarUrl" class="user-avatar">
                {{ avatarText }}
              </el-avatar>
              <el-upload
                class="avatar-upload"
                action=""
                :http-request="handleAvatarUpload"
                :show-file-list="false"
                accept="image/*"
              >
                <div class="avatar-overlay">
                  <i class="el-icon-camera"></i>
                </div>
              </el-upload>
            </div>
            <h3 class="user-name">{{ profileForm.realName || profileForm.username || '同学' }}</h3>
            <p class="user-role">学生</p>
          </div>

          <div class="info-list">
            <div class="info-item">
              <i class="el-icon-postcard"></i>
              <span class="info-label">学号</span>
              <span class="info-value">{{ profileForm.studentNo || '--' }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-office-building"></i>
              <span class="info-label">学院</span>
              <span class="info-value">{{ profileForm.college || '--' }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-collection"></i>
              <span class="info-label">专业</span>
              <span class="info-value">{{ profileForm.major || '--' }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-s-flag"></i>
              <span class="info-label">班级</span>
              <span class="info-value">{{ profileForm.className || '--' }}</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- Right: Profile Form & Password -->
      <el-col :span="16">
        <!-- Basic Info -->
        <div class="light-card">
          <div class="card-header">
            <span>基本信息</span>
          </div>
          <el-form
            :model="profileForm"
            ref="profileFormRef"
            label-width="80px"
            class="profile-form"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="昵称">
                  <el-input
                    v-model="profileForm.username"
                    placeholder="请输入昵称"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="真实姓名">
                  <el-input
                    v-model="profileForm.realName"
                    placeholder="请输入真实姓名"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="学号">
                  <el-input
                    v-model="profileForm.studentNo"
                    placeholder="请输入学号"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="班级">
                  <el-input
                    v-model="profileForm.className"
                    placeholder="请输入班级"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="专业">
                  <el-input
                    v-model="profileForm.major"
                    placeholder="请输入专业"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="学院">
                  <el-input
                    v-model="profileForm.college"
                    placeholder="请输入学院"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="手机号">
                  <el-input
                    v-model="profileForm.phone"
                    placeholder="请输入手机号"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邮箱">
                  <el-input
                    v-model="profileForm.email"
                    placeholder="请输入邮箱"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button
                type="primary"
                :loading="savingProfile"
                @click="handleSaveProfile"
                class="save-btn"
              >
                <i class="el-icon-check" v-if="!savingProfile"></i>
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- Change Password -->
        <div class="light-card">
          <div class="card-header">
            <span>修改密码</span>
          </div>
          <el-form
            :model="passwordForm"
            :rules="passwordRules"
            ref="passwordFormRef"
            label-width="100px"
            class="profile-form"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              ></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码 (至少6位)"
                show-password
              ></el-input>
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="savingPassword"
                @click="handleChangePassword"
                class="save-btn"
              >
                <i class="el-icon-lock" v-if="!savingPassword"></i>
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getUserInfo, updateProfile, changePassword, uploadFile } from '../../api'

export default {
  name: 'StudentProfile',
  data() {
    const validateConfirmPass = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      profileForm: {
        id: '',
        username: '',
        realName: '',
        studentNo: '',
        className: '',
        major: '',
        college: '',
        phone: '',
        email: '',
        avatar: ''
      },
      savingProfile: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      savingPassword: false,
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirmPass, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    userId() {
      return this.$store.state.userInfo.id
    },
    avatarUrl() {
      return this.profileForm.avatar || ''
    },
    avatarText() {
      const name = this.profileForm.realName || this.profileForm.username || ''
      return name ? name.charAt(0) : 'S'
    }
  },
  created() {
    this.loadProfile()
  },
  methods: {
    async loadProfile() {
      // Initialize from store
      const storeInfo = this.$store.state.userInfo || {}
      this.profileForm = {
        id: storeInfo.id || '',
        username: storeInfo.username || '',
        realName: storeInfo.realName || '',
        studentNo: storeInfo.studentNo || storeInfo.studentNumber || '',
        className: storeInfo.className || storeInfo.classname || '',
        major: storeInfo.major || '',
        college: storeInfo.college || storeInfo.department || '',
        phone: storeInfo.phone || storeInfo.mobile || '',
        email: storeInfo.email || '',
        avatar: storeInfo.avatar || ''
      }

      // Fetch latest from server
      try {
        const res = await getUserInfo(this.userId)
        if (res.data.code === 200) {
          const data = res.data.data
          this.profileForm = {
            id: data.id,
            username: data.username || '',
            realName: data.realName || '',
            studentNo: data.studentNo || data.studentNumber || '',
            className: data.className || data.classname || '',
            major: data.major || '',
            college: data.college || data.department || '',
            phone: data.phone || data.mobile || '',
            email: data.email || '',
            avatar: data.avatar || ''
          }
        }
      } catch (e) {
        console.error('获取用户信息失败', e)
      }
    },
    async handleSaveProfile() {
      this.savingProfile = true
      try {
        const res = await updateProfile(this.profileForm)
        if (res.data.code === 200) {
          this.$message.success('个人信息保存成功')
          // Update store
          const newInfo = { ...this.$store.state.userInfo, ...this.profileForm }
          this.$store.commit('SET_USER_INFO', newInfo)
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      } catch (e) {
        console.error('保存个人信息失败', e)
        this.$message.error('保存个人信息失败')
      } finally {
        this.savingProfile = false
      }
    },
    handleChangePassword() {
      this.$refs.passwordFormRef.validate(async valid => {
        if (!valid) return
        this.savingPassword = true
        try {
          const res = await changePassword({
            userId: this.userId,
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          })
          if (res.data.code === 200) {
            this.$message.success('密码修改成功')
            this.passwordForm = {
              oldPassword: '',
              newPassword: '',
              confirmPassword: ''
            }
            this.$refs.passwordFormRef.resetFields()
          } else {
            this.$message.error(res.data.msg || '密码修改失败')
          }
        } catch (e) {
          console.error('修改密码失败', e)
          this.$message.error('修改密码失败')
        } finally {
          this.savingPassword = false
        }
      })
    },
    async handleAvatarUpload(param) {
      const formData = new FormData()
      formData.append('file', param.file)
      try {
        const res = await uploadFile(formData)
        if (res.data.code === 200) {
          this.profileForm.avatar = res.data.data || res.data.url
          this.$message.success('头像上传成功')
        } else {
          this.$message.error(res.data.msg || '上传失败')
        }
      } catch (e) {
        console.error('头像上传失败', e)
        this.$message.error('头像上传失败')
      }
    }
  }
}
</script>

<style scoped>
.profile-container {
  /* container */
}

.light-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

/* Avatar Card */
.avatar-card {
  text-align: center;
  padding: 30px 20px;
}

.avatar-section {
  margin-bottom: 24px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.user-avatar {
  background: linear-gradient(135deg, #61BFAD, #4a9e8e) !important;
  color: #fff !important;
  font-size: 36px !important;
  font-weight: 600;
}

.avatar-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 36px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 0 0 50px 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay i {
  color: #fff;
  font-size: 16px;
}

.avatar-upload {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 36px;
}

.user-name {
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.user-role {
  color: #61BFAD;
  font-size: 13px;
  margin: 0;
  padding: 2px 12px;
  display: inline-block;
  background: rgba(97, 191, 173, 0.1);
  border-radius: 12px;
}

/* Info List */
.info-list {
  text-align: left;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid rgba(229, 231, 235, 0.4);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item i {
  color: #61BFAD;
  font-size: 16px;
  margin-right: 10px;
  width: 20px;
  text-align: center;
}

.info-label {
  color: #64748b;
  font-size: 13px;
  width: 40px;
}

.info-value {
  color: #2c3e50;
  font-size: 13px;
  flex: 1;
  text-align: right;
}

/* Form */
.profile-form >>> .el-form-item__label {
  color: #64748b;
}

.save-btn {
  background: linear-gradient(135deg, #61BFAD, #4a9e8e) !important;
  border: none !important;
  padding: 10px 32px !important;
  border-radius: 8px !important;
  transition: transform 0.2s, box-shadow 0.2s;
}

.save-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(97, 191, 173, 0.3);
}
</style>
