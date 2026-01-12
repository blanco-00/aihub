<template>
  <el-container class="init-container">
    <el-main class="init-main">
      <div class="init-content">
        <el-card class="init-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h2 class="init-title">
                <el-icon class="tech-icon"><Setting /></el-icon>
                系统初始化
              </h2>
              <p class="init-subtitle">完成数据库初始化和创建第一个超级管理员账号</p>
            </div>
          </template>

          <!-- 数据库状态检查 -->
          <div v-if="!dbInitialized" class="db-status-section">
            <el-alert
              v-if="dbStatus.errorMessage"
              :title="dbStatus.errorMessage"
              type="error"
              :closable="false"
              show-icon
              class="db-alert"
            />
            <el-alert
              v-else-if="!dbStatus.connected"
              title="正在检查数据库连接..."
              type="info"
              :closable="false"
              show-icon
              class="db-alert"
            />
            <el-alert
              v-else-if="!dbStatus.tablesInitialized"
              title="数据库连接正常，但表结构未初始化"
              type="warning"
              :closable="false"
              show-icon
              class="db-alert"
            />

            <div v-if="dbStatus.connected && !dbStatus.tablesInitialized" class="db-init-action">
              <el-button
                type="primary"
                :loading="dbInitLoading"
                class="tech-button"
                @click="handleInitDatabase"
              >
                <el-icon v-if="!dbInitLoading"><Tools /></el-icon>
                初始化数据库表结构
              </el-button>
            </div>

            <div v-if="!dbStatus.connected" class="db-help">
              <p class="help-title">请先完成以下步骤：</p>
              <ol class="help-steps">
                <li>确保 MySQL 服务已启动</li>
                <li>创建数据库：<code>CREATE DATABASE aihub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;</code></li>
                <li>检查配置文件中的数据库连接信息是否正确</li>
              </ol>
            </div>
          </div>

          <!-- 创建超级管理员表单 -->
          <el-form
            v-if="dbInitialized"
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
            label-position="left"
            @submit.prevent="handleSubmit"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名（3-50个字符）"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="form.email"
                type="email"
                placeholder="请输入邮箱地址"
                :prefix-icon="Message"
                clearable
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码（8-50个字符）"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="tech-button"
                @click="handleSubmit"
              >
                <el-icon v-if="!loading"><Check /></el-icon>
                创建超级管理员
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Setting, User, Message, Lock, Check, Tools } from '@element-plus/icons-vue'
import { initService } from '../../services/initService'
import type { InitSuperAdminRequest, DatabaseStatusResponse } from '../../types/api'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const dbInitLoading = ref(false)
const dbStatus = ref<DatabaseStatusResponse>({
  connected: false,
  databaseExists: false,
  tablesInitialized: false,
})
const dbInitialized = ref(false)

// 检查数据库状态
const checkDatabaseStatus = async () => {
  try {
    const status = await initService.checkDatabaseStatus()
    dbStatus.value = status
    dbInitialized.value = status.connected && status.tablesInitialized
  } catch (error: any) {
    console.error('检查数据库状态失败:', error)
    dbStatus.value = {
      connected: false,
      databaseExists: false,
      tablesInitialized: false,
      errorMessage: '无法连接到服务器',
    }
  }
}

// 初始化数据库
const handleInitDatabase = async () => {
  try {
    dbInitLoading.value = true
    console.log('开始初始化数据库...')
    
    await initService.initializeDatabase()
    console.log('数据库初始化接口调用成功')
    
    ElMessage.success('数据库表结构初始化成功！')
    
    // 等待一小段时间，确保数据库操作完成
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 重新检查状态
    console.log('重新检查数据库状态...')
    await checkDatabaseStatus()
    console.log('数据库状态检查完成，dbInitialized:', dbInitialized.value)
    console.log('数据库状态:', dbStatus.value)
    
    // 如果初始化成功，dbInitialized 应该变为 true，表单会自动显示
    if (dbInitialized.value) {
      ElMessage.success('数据库初始化完成，请创建超级管理员')
    } else {
      // 如果状态检查显示未初始化，可能是检查有问题，手动设置为已初始化
      if (dbStatus.value.connected && !dbStatus.value.tablesInitialized) {
        console.warn('状态检查异常，但初始化已成功，手动更新状态')
        dbStatus.value.tablesInitialized = true
        dbInitialized.value = true
      }
    }
  } catch (error: any) {
    console.error('初始化数据库失败:', error)
    ElMessage.error('初始化数据库失败：' + (error.message || '未知错误'))
  } finally {
    dbInitLoading.value = false
    console.log('初始化流程结束，dbInitLoading:', dbInitLoading.value)
  }
}

onMounted(() => {
  checkDatabaseStatus()
})

const form = reactive<InitSuperAdminRequest & { confirmPassword: string }>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

// 自定义验证规则：确认密码
const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3-50个字符之间', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 50, message: '密码长度必须在8-50个字符之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    try {
      loading.value = true

      const requestData: InitSuperAdminRequest = {
        username: form.username,
        email: form.email,
        password: form.password,
      }

      await initService.createSuperAdmin(requestData)

      ElMessage.success('超级管理员创建成功！系统初始化完成')

      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push('/')
      }, 1500)
    } catch (error: any) {
      // 错误信息已在 apiClient 中处理，这里只做日志记录
      console.error('创建超级管理员失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.init-container {
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 20% 50%, rgba(24, 144, 255, 0.1) 0%, transparent 50%),
    #fafafa;
  position: relative;
}

.init-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: linear-gradient(rgba(24, 144, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(24, 144, 255, 0.03) 1px, transparent 1px);
  background-size: 20px 20px;
  pointer-events: none;
}

.init-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  z-index: 1;
}

.init-content {
  width: 100%;
  max-width: 500px;
}

.init-card {
  background: #ffffff;
  border: 1px solid rgba(24, 144, 255, 0.1);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.init-card:hover {
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.15);
}

.card-header {
  text-align: center;
  padding: 8px 0;
}

.init-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
}

.tech-icon {
  font-size: 28px;
  color: #1890ff;
}

.init-subtitle {
  margin: 0;
  font-size: 14px;
  color: #8c8c8c;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-input__wrapper) {
  border-radius: 4px;
}

.tech-button {
  width: 100%;
  height: 40px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  transition: all 0.3s ease;
  font-size: 16px;
  font-weight: 500;
}

.tech-button:hover {
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
  transform: translateY(-1px);
}

.tech-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.tech-button.is-loading {
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.db-status-section {
  margin-bottom: 24px;
}

.db-alert {
  margin-bottom: 16px;
  white-space: pre-line;
}

.db-init-action {
  text-align: center;
  margin-top: 24px;
}

.db-help {
  margin-top: 24px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 4px;
}

.help-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.help-steps {
  margin: 0;
  padding-left: 20px;
  color: #595959;
  line-height: 1.8;
}

.help-steps li {
  margin-bottom: 8px;
}

.help-steps code {
  padding: 2px 6px;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 2px;
  font-family: 'SF Mono', Monaco, 'Cascadia Code', monospace;
  font-size: 12px;
  color: #1890ff;
}
</style>
