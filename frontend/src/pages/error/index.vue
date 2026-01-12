<template>
  <el-container class="error-container">
    <el-main class="error-main">
      <div class="error-content">
        <el-card class="error-card" shadow="hover">
          <div class="error-icon">
            <el-icon :size="80" color="#f5222d"><WarningFilled /></el-icon>
          </div>
          <h1 class="error-title">{{ errorTitle }}</h1>
          <p class="error-message">{{ errorMessage }}</p>
          
          <div class="error-details" v-if="errorDetails">
            <el-alert
              :title="errorDetails"
              type="error"
              :closable="false"
              show-icon
            />
          </div>

          <div class="error-actions">
            <el-button type="primary" @click="handleRetry">
              <el-icon><Refresh /></el-icon>
              重试
            </el-button>
            <el-button @click="handleGoHome">
              <el-icon><HomeFilled /></el-icon>
              返回首页
            </el-button>
          </div>
        </el-card>
      </div>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { WarningFilled, Refresh, HomeFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { initService } from '../../services/initService'

const router = useRouter()
const route = useRoute()

const errorTitle = ref('连接服务器失败')
const errorMessage = ref('无法连接到后端服务器，请检查服务是否正常运行。')
const errorDetails = ref<string>('')

// 从路由参数获取错误信息
onMounted(() => {
  const errorType = route.query.type as string
  const errorMsg = route.query.message as string

  if (errorType === 'network') {
    errorTitle.value = '网络连接错误'
    errorMessage.value = '无法连接到后端服务器'
    errorDetails.value = errorMsg || '请检查：\n1. 后端服务是否已启动（端口 8080）\n2. 网络连接是否正常\n3. 防火墙设置是否正确'
  } else if (errorType === 'server') {
    errorTitle.value = '服务器错误'
    errorMessage.value = '服务器返回了错误响应'
    errorDetails.value = errorMsg || '请稍后重试或联系管理员'
  } else if (errorMsg) {
    errorDetails.value = errorMsg
  }
})

const handleRetry = async () => {
  try {
    ElMessage.info('正在检查服务器连接...')
    await initService.checkStatus()
    // 如果检查成功，说明服务器已恢复，跳转到首页
    ElMessage.success('服务器连接成功')
    router.push('/')
  } catch (error: any) {
    ElMessage.error('仍然无法连接到服务器')
    console.error('重试失败:', error)
  }
}

const handleGoHome = () => {
  // 跳转到初始化页面，而不是首页
  // 因为如果后端服务未启动，跳转到首页会被路由守卫拦截，再次跳转到错误页面
  router.push('/init')
}
</script>

<style scoped>
.error-container {
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(245, 34, 45, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 20% 50%, rgba(245, 34, 45, 0.1) 0%, transparent 50%),
    #fafafa;
  position: relative;
}

.error-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  z-index: 1;
}

.error-content {
  width: 100%;
  max-width: 600px;
}

.error-card {
  background: #ffffff;
  border: 1px solid rgba(245, 34, 45, 0.1);
  border-radius: 8px;
  text-align: center;
  padding: 48px 32px;
}

.error-icon {
  margin-bottom: 24px;
}

.error-title {
  margin: 0 0 16px 0;
  font-size: 28px;
  font-weight: 600;
  color: #f5222d;
}

.error-message {
  margin: 0 0 24px 0;
  font-size: 16px;
  color: #8c8c8c;
  line-height: 1.6;
}

.error-details {
  margin: 24px 0;
  text-align: left;
}

.error-details :deep(.el-alert) {
  white-space: pre-line;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 32px;
}

.error-actions .el-button {
  min-width: 120px;
}
</style>
