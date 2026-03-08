<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- Left: Avatar -->
      <el-col :span="8">
        <div class="light-card avatar-card">
          <div class="card-header">
            <span class="card-title">头像设置</span>
          </div>
          <div class="avatar-area">
            <div class="avatar-preview">
              <img v-if="userForm.avatar" :src="userForm.avatar" alt="avatar" class="avatar-img" />
              <div v-else class="avatar-placeholder">
                <span>{{ avatarText }}</span>
              </div>
            </div>
            <el-upload
              action=""
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleAvatarChange"
              accept="image/*"
              class="avatar-upload"
            >
              <el-button type="primary" size="small" icon="el-icon-camera">
                更换头像
              </el-button>
            </el-upload>
            <div class="avatar-tip">支持 jpg/png 格式，大小不超过 2MB</div>
          </div>

          <!-- User Summary -->
          <div class="user-summary">
            <div class="summary-name">{{ userForm.realName || userForm.nickname || '管理员' }}</div>
            <div class="summary-role">
              <el-tag type="danger" size="small" effect="dark">管理员</el-tag>
            </div>
            <div class="summary-info" v-if="userForm.college">
              <i class="el-icon-office-building"></i> {{ userForm.college }}
            </div>
          </div>
        </div>
      </el-col>

      <!-- Right: Profile Form & Password -->
      <el-col :span="16">
        <!-- Profile Form -->
        <div class="light-card profile-form-card">
          <div class="card-header">
            <span class="card-title">基本信息</span>
          </div>
          <el-form ref="profileForm" :model="userForm" label-width="80px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="昵称">
                  <el-input v-model="userForm.nickname" placeholder="请输入昵称"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="真实姓名">
                  <el-input v-model="userForm.realName" placeholder="请输入真实姓名"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="手机号">
                  <el-input v-model="userForm.phone" placeholder="请输入手机号"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邮箱">
                  <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="学院">
                  <el-input v-model="userForm.college" placeholder="请输入学院"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="工号">
                  <el-input v-model="userForm.studentNo" disabled></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile" :loading="profileLoading">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- Password Change -->
        <div class="light-card password-card">
          <div class="card-header">
            <span class="card-title">修改密码</span>
          </div>
          <el-form
            ref="passwordForm"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    placeholder="请输入当前密码"
                    show-password
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    show-password
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请确认新密码"
                    show-password
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
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
  name: 'AdminProfile',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    return {
      userForm: {
        id: null,
        nickname: '',
        realName: '',
        phone: '',
        email: '',
        college: '',
        studentNo: '',
        avatar: ''
      },
      profileLoading: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordLoading: false,
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入当前密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    avatarText() {
      const name = this.userForm.realName || this.userForm.nickname || '管'
      return name.charAt(0)
    }
  },
  mounted() {
    this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      try {
        const res = await getUserInfo()
        const user = res.data.data
        this.userForm = {
          id: user.id,
          nickname: user.nickname || '',
          realName: user.realName || '',
          phone: user.phone || '',
          email: user.email || '',
          college: user.college || '',
          studentNo: user.studentNo || '',
          avatar: user.avatar || ''
        }
      } catch (e) {
        this.$message.error('加载用户信息失败')
      }
    },
    async handleAvatarChange(file) {
      const isImage = file.raw.type.startsWith('image/')
      const isLt2M = file.raw.size / 1024 / 1024 < 2
      if (!isImage) {
        this.$message.error('只能上传图片格式文件')
        return
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB')
        return
      }
      try {
        const formData = new FormData()
        formData.append('file', file.raw)
        const res = await uploadFile(formData)
        this.userForm.avatar = res.data.data
        this.$message.success('头像上传成功，请保存修改')
      } catch (e) {
        this.$message.error('头像上传失败')
      }
    },
    async handleUpdateProfile() {
      this.profileLoading = true
      try {
        const data = {
          id: this.userForm.id,
          nickname: this.userForm.nickname,
          realName: this.userForm.realName,
          phone: this.userForm.phone,
          email: this.userForm.email,
          college: this.userForm.college,
          avatar: this.userForm.avatar
        }
        await updateProfile(data)
        this.$message.success('个人信息更新成功')
        if (this.$store.state.userInfo) {
          this.$store.state.userInfo.realName = this.userForm.realName
          this.$store.state.userInfo.nickname = this.userForm.nickname
          this.$store.state.userInfo.avatar = this.userForm.avatar
        }
      } catch (e) {
        this.$message.error('更新失败')
      } finally {
        this.profileLoading = false
      }
    },
    handleChangePassword() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (!valid) return
        this.passwordLoading = true
        try {
          await changePassword({
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          })
          this.$message.success('密码修改成功，请重新登录')
          this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
          this.$refs.passwordForm.resetFields()
          setTimeout(() => {
            this.$store.dispatch('logout').then(() => {
              this.$router.push('/login')
            })
          }, 1500)
        } catch (e) {
          this.$message.error('密码修改失败')
        } finally {
          this.passwordLoading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.profile-page {
  padding: 0;
}

.light-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #ecebfa;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(93, 71, 167, 0.06);
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
  color: #2d2a4a;
}

.avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 10px 0 20px;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #7b5ce7;
  box-shadow: 0 0 20px rgba(123, 92, 231, 0.2);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #7b5ce7, #6544d7);
  color: #fff;
  font-size: 40px;
  font-weight: 700;
}

.avatar-upload {
  display: inline-block;
}

.avatar-tip {
  font-size: 12px;
  color: #999;
  text-align: center;
}

.user-summary {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #ecebfa;
}

.summary-name {
  font-size: 18px;
  font-weight: 600;
  color: #2d2a4a;
}

.summary-info {
  font-size: 13px;
  color: #7a7598;
}

.summary-info i {
  margin-right: 4px;
  color: #b0adc5;
}

.profile-page >>> .el-form-item__error {
  color: #ef4444;
}

.profile-page >>> .el-form-item.is-error .el-input__inner {
  border-color: #ef4444;
}
</style>
