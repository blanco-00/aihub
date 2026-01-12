<template>
  <el-container class="setup-container">
    <el-main class="setup-main">
      <div class="setup-content">
        <el-card class="setup-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h2 class="setup-title">
                <el-icon class="tech-icon"><Setting /></el-icon>
                数据库配置向导
              </h2>
              <p class="setup-subtitle">配置数据库连接信息以完成系统初始化</p>
            </div>
          </template>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="120px"
            label-position="left"
          >
            <el-form-item label="数据库主机" prop="host">
              <el-input
                v-model="form.host"
                placeholder="localhost 或 IP 地址"
                :prefix-icon="Connection"
                clearable
              />
            </el-form-item>

            <el-form-item label="数据库端口" prop="port">
              <el-input-number
                v-model="form.port"
                :min="1"
                :max="65535"
                placeholder="3306"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="数据库名称" prop="database">
              <el-input
                v-model="form.database"
                placeholder="aihub"
                :prefix-icon="Folder"
                clearable
              />
              <div class="form-tip">
                <el-icon><InfoFilled /></el-icon>
                如果数据库不存在，请先创建：<code>CREATE DATABASE aihub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;</code>
              </div>
            </el-form-item>

            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="form.username"
                placeholder="root"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入数据库密码"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>

            <!-- 连接测试结果 -->
            <el-form-item v-if="testResult" label="连接测试">
              <el-alert
                v-if="testResult.success"
                :title="`连接成功！耗时 ${testResult.duration}ms`"
                type="success"
                :closable="false"
                show-icon
              >
                <template #default>
                  <div v-if="!testResult.databaseExists" class="test-result-detail">
                    <p>数据库连接正常，但数据库 <code>{{ form.database }}</code> 不存在。</p>
                    <p>请先创建数据库，然后点击"保存配置"继续。</p>
                  </div>
                  <div v-else class="test-result-detail">
                    <p>数据库连接正常，数据库已存在。</p>
                    <p>可以点击"保存配置"继续下一步。</p>
                  </div>
                </template>
              </el-alert>
              <el-alert
                v-else
                :title="testResult.errorMessage || '连接失败'"
                type="error"
                :closable="false"
                show-icon
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="testing"
                @click="handleTestConnection"
              >
                <el-icon v-if="!testing"><Connection /></el-icon>
                测试连接
              </el-button>
              <el-button
                type="success"
                :loading="saving"
                :disabled="!testResult || !testResult.success"
                @click="handleSaveConfig"
              >
                <el-icon v-if="!saving"><Check /></el-icon>
                保存配置
              </el-button>
              <el-button @click="handleSkip">
                跳过（使用环境变量）
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 配置保存成功提示 -->
          <el-alert
            v-if="configSaved"
            title="配置已保存成功！"
            type="success"
            :closable="false"
            show-icon
            class="save-success-alert"
          >
            <template #default>
              <div class="save-success-content">
                <p>配置已保存到 <code>application-local.yml</code></p>
                <p class="restart-tip">请重启后端应用后，点击下方按钮继续。</p>
                <div class="action-buttons">
                  <el-button
                    type="primary"
                    :loading="navigating"
                    :disabled="navigating"
                    @click="handleGoToHome"
                    class="go-home-button"
                  >
                    <el-icon v-if="!navigating"><HomeFilled /></el-icon>
                    {{ navigating ? '跳转中...' : '重启后继续' }}
                  </el-button>
                </div>
              </div>
            </template>
          </el-alert>

          <!-- 帮助信息 -->
          <el-divider />
          <div class="help-section">
            <h3 class="help-title">配置说明</h3>
            <ul class="help-list">
              <li>配置信息将保存到本地文件，不会提交到代码仓库</li>
              <li>保存后需要重启应用才能生效</li>
              <li>也可以使用环境变量方式配置，点击"跳过"查看说明</li>
            </ul>
          </div>
        </el-card>
      </div>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  Setting,
  Connection,
  Folder,
  User,
  Lock,
  Check,
  InfoFilled,
  HomeFilled,
} from '@element-plus/icons-vue'
import { setupService } from '../../services/setupService'
import type { DatabaseConfig, ConnectionTestResult } from '../../types/api'

const router = useRouter()
const formRef = ref<FormInstance>()
const testing = ref(false)
const saving = ref(false)
const testResult = ref<ConnectionTestResult | null>(null)
const configSaved = ref(false)
const navigating = ref(false) // 导航中状态，防止重复点击

const form = reactive<DatabaseConfig>({
  host: 'localhost',
  port: 3306,
  database: 'aihub',
  username: 'root',
  password: '',
})

const rules: FormRules = {
  host: [
    { required: true, message: '请输入数据库主机地址', trigger: 'blur' },
  ],
  port: [
    { required: true, message: '请输入数据库端口', trigger: 'blur' },
    { type: 'number', min: 1, max: 65535, message: '端口号必须在 1-65535 之间', trigger: 'blur' },
  ],
  database: [
    { required: true, message: '请输入数据库名称', trigger: 'blur' },
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
}

const handleTestConnection = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    try {
      testing.value = true
      testResult.value = null

      const result = await setupService.testConnection(form)
      testResult.value = result

      if (result.success) {
        if (!result.databaseExists) {
          ElMessage.warning('数据库连接成功，但数据库不存在，请先创建数据库')
        } else {
          ElMessage.success('数据库连接成功！')
        }
      } else {
        ElMessage.error(result.errorMessage || '连接失败')
      }
    } catch (error: any) {
      console.error('测试连接失败:', error)
      // 设置错误结果，让用户能看到错误信息
      testResult.value = {
        success: false,
        errorMessage: error.response?.data?.message || error.message || '网络错误，请检查后端服务是否启动',
        databaseExists: false,
        duration: 0,
      }
      ElMessage.error('测试连接失败：' + (error.response?.data?.message || error.message || '未知错误'))
    } finally {
      testing.value = false
    }
  })
}

const handleSaveConfig = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    if (!testResult.value || !testResult.value.success) {
      ElMessage.warning('请先测试连接并确保连接成功')
      return
    }

        try {
          saving.value = true
          await setupService.saveConfig(form)
          
          // 标记配置已保存
          configSaved.value = true
          
          ElMessage.success('配置已保存成功！请重启后端应用后继续。')
    } catch (error: any) {
      console.error('保存配置失败:', error)
      ElMessage.error('保存配置失败：' + (error.message || '未知错误'))
    } finally {
      saving.value = false
    }
  })
}

const handleSkip = () => {
  ElMessage.info('请参考文档配置环境变量：docs/backend/config.md')
  // 仍然允许跳转到初始化页面，让用户手动配置
  router.push('/init')
}

const handleGoToHome = async () => {
  // 防止重复点击
  if (navigating.value) {
    return
  }
  
  try {
    navigating.value = true
    // 跳转到首页（路由守卫会检查初始化状态）
    await router.push('/')
  } catch (error: any) {
    console.error('跳转失败:', error)
    ElMessage.error('跳转失败：' + (error.message || '未知错误'))
  } finally {
    // 延迟重置状态，避免快速切换
    setTimeout(() => {
      navigating.value = false
    }, 1000)
  }
}
</script>

<style scoped>
.setup-container {
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 20% 50%, rgba(24, 144, 255, 0.1) 0%, transparent 50%),
    #fafafa;
  position: relative;
}

.setup-container::before {
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

.setup-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  z-index: 1;
}

.setup-content {
  width: 100%;
  max-width: 600px;
}

.setup-card {
  background: #ffffff;
  border: 1px solid rgba(24, 144, 255, 0.1);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.setup-card:hover {
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.15);
}

.card-header {
  text-align: center;
  padding: 8px 0;
}

.setup-title {
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

.setup-subtitle {
  margin: 0;
  font-size: 14px;
  color: #8c8c8c;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

.form-tip {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
  font-size: 12px;
  color: #1890ff;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  line-height: 1.6;
}

.form-tip code {
  padding: 2px 6px;
  background: #fff;
  border: 1px solid #b3d8ff;
  border-radius: 2px;
  font-family: 'SF Mono', Monaco, 'Cascadia Code', monospace;
  font-size: 11px;
  color: #1890ff;
}

.test-result-detail {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
}

.test-result-detail code {
  padding: 2px 6px;
  background: #f0f0f0;
  border-radius: 2px;
  font-family: 'SF Mono', Monaco, 'Cascadia Code', monospace;
  font-size: 12px;
}

:deep(.el-form-item__content) {
  display: flex;
  gap: 12px;
}

.help-section {
  margin-top: 24px;
}

.help-title {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.help-list {
  margin: 0;
  padding-left: 20px;
  color: #595959;
  line-height: 1.8;
}

.help-list li {
  margin-bottom: 8px;
}

.save-success-alert {
  margin-top: 24px;
  margin-bottom: 24px;
}

.save-success-content {
  padding: 8px 0;
}

.save-success-content p {
  margin: 8px 0;
  color: #606266;
}

.save-success-content code {
  background-color: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  color: #e6a23c;
}

.restart-tip {
  color: #909399;
  font-size: 14px;
}

.action-buttons {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.go-home-button {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  transition: all 0.3s ease;
}

.go-home-button:hover {
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
  transform: translateY(-1px);
}
</style>
