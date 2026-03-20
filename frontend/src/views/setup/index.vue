<template>
  <div class="select-none">
    <img :src="bg" class="wave" />
    <div class="init-container">
      <div class="img">
        <component :is="toRaw(illustration)" />
      </div>
      <div class="init-box">
        <div class="init-form">
          <avatar class="avatar" />
          <Motion>
            <h2 class="outline-hidden">
              <TypeIt
                :options="{
                  strings: ['系统初始化'],
                  cursor: false,
                  speed: 100,
                }"
              />
            </h2>
          </Motion>

          <el-steps
            :active="currentStep"
            finish-status="success"
            align-center
            style="margin: 32px 0"
          >
            <el-step title="检查数据库" />
            <el-step title="创建管理员" />
          </el-steps>

          <Motion :delay="100">
            <el-alert
              title="系统初始化向导"
              type="info"
              :closable="false"
              show-icon
              style="margin-bottom: 24px"
            >
              <template #default>
                <p style="margin: 0">欢迎使用 AIHub 系统初始化向导</p>
                <p style="margin: 8px 0 0 0">请按照步骤完成系统初始化配置</p>
              </template>
            </el-alert>
          </Motion>

          <div class="step-content">
            <!-- 步骤1: 检查数据库 -->
            <Motion v-if="currentStep === 0" :delay="150">
              <div class="step-panel">
                <!-- 加载状态 -->
                <el-alert
                  v-if="!dbStatus"
                  type="info"
                  title="正在检查数据库状态..."
                  :closable="false"
                  style="margin-bottom: 24px"
                />

                <!-- 数据库连接失败 -->
                <el-alert
                  v-if="dbStatus && !dbStatus.connected"
                  type="error"
                  title="数据库连接失败"
                  :description="
                    dbStatus.errorMessage || '请检查数据库配置和连接状态'
                  "
                  style="margin-bottom: 24px"
                  :closable="false"
                />

                <!-- 数据库连接成功 -->
                <el-alert
                  v-if="dbStatus && dbStatus.connected"
                  type="success"
                  title="数据库连接正常"
                  description="数据库连接成功，Flyway 会在应用启动时自动初始化表结构"
                  style="margin-bottom: 24px"
                  :closable="false"
                />

                <div class="flex justify-center">
                  <!-- 数据库连接成功，显示下一步按钮 -->
                  <el-button
                    v-if="dbStatus && dbStatus.connected"
                    type="primary"
                    size="large"
                    @click="currentStep = 1"
                  >
                    下一步：创建管理员
                  </el-button>

                  <!-- 数据库连接失败，显示重试按钮 -->
                  <el-button
                    v-if="dbStatus && !dbStatus.connected"
                    type="warning"
                    size="large"
                    @click="checkDatabaseStatus"
                  >
                    重新检查数据库状态
                  </el-button>

                  <!-- 状态未加载，显示检查按钮 -->
                  <el-button
                    v-if="!dbStatus"
                    type="primary"
                    size="large"
                    :loading="initDbLoading"
                    @click="checkDatabaseStatus"
                  >
                    检查数据库状态
                  </el-button>
                </div>
              </div>
            </Motion>

            <!-- 步骤2: 创建管理员 -->
            <Motion v-if="currentStep === 1" :delay="150">
              <div class="step-panel">
                <el-form
                  ref="adminFormRef"
                  :model="adminForm"
                  :rules="adminRules"
                  size="large"
                >
                  <el-form-item prop="username">
                    <el-input
                      v-model="adminForm.username"
                      placeholder="请输入用户名（3-50个字符）"
                      :prefix-icon="useRenderIcon(User)"
                      clearable
                    />
                  </el-form-item>
                  <el-form-item prop="email">
                    <el-input
                      v-model="adminForm.email"
                      placeholder="请输入邮箱"
                      :prefix-icon="useRenderIcon(Info)"
                      clearable
                    />
                  </el-form-item>
                  <el-form-item prop="password">
                    <el-input
                      v-model="adminForm.password"
                      type="password"
                      placeholder="请输入密码（8-18位数字、字母、符号的任意两种组合）"
                      :prefix-icon="useRenderIcon(Lock)"
                      show-password
                      clearable
                    />
                  </el-form-item>
                  <el-form-item prop="confirmPassword">
                    <el-input
                      v-model="adminForm.confirmPassword"
                      type="password"
                      placeholder="请再次输入密码"
                      :prefix-icon="useRenderIcon(Lock)"
                      show-password
                      clearable
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button
                      class="w-full"
                      type="primary"
                      size="default"
                      :loading="creating"
                      :disabled="creating"
                      @click="createAdmin"
                    >
                      创建管理员
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </Motion>
          </div>
        </div>
      </div>
    </div>
    <div
      class="w-full flex-c absolute bottom-3 text-sm text-[rgba(0,0,0,0.6)] dark:text-[rgba(220,220,242,0.8)]"
    >
      Copyright © 2025-present
      <a
        class="hover:text-primary!"
        href="https://github.com/pure-admin"
        target="_blank"
      >
        &nbsp;AIHub
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, toRaw } from "vue";
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
} from "element-plus";
import Motion from "@/views/login/utils/motion";
import TypeIt from "@/components/ReTypeit";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { useLayout } from "@/layout/hooks/useLayout";
import { useDataThemeChange } from "@/layout/hooks/useDataThemeChange";
import { REGEXP_PWD } from "@/views/login/utils/rule";
import { $t, transformI18n } from "@/plugins/i18n";
import {
  getDatabaseStatus,
  createSuperAdmin,
  type DatabaseStatus,
  type InitSuperAdminRequest,
} from "@/api/init";
import { clearInitStatusCache } from "@/router";
import { bg, avatar, illustration } from "@/views/login/utils/static";
import router from "@/router";
import Lock from "~icons/ri/lock-fill";
import User from "~icons/ri/user-3-fill";
import Info from "~icons/ri/information-line";

defineOptions({
  name: "Init",
});

// 初始化主题（确保暗黑模式生效）
const { initStorage } = useLayout();
initStorage();
const { themeMode, dataThemeChange } = useDataThemeChange();
dataThemeChange(themeMode.value);

const currentStep = ref(0);
const adminFormRef = ref<FormInstance>();
const initDbLoading = ref(false);
const creating = ref(false);
const dbStatus = ref<DatabaseStatus | null>(null);

const adminForm = reactive<InitSuperAdminRequest>({
  username: "",
  email: "",
  password: "",
  confirmPassword: "",
});

const adminRules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    {
      min: 3,
      max: 50,
      message: "用户名长度必须在3-50个字符之间",
      trigger: "blur",
    },
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
  ],
  password: [
    {
      validator: (rule, value, callback) => {
        if (value === "") {
          callback(new Error(transformI18n($t("login.purePassWordReg"))));
        } else if (!REGEXP_PWD.test(value)) {
          callback(new Error(transformI18n($t("login.purePassWordRuleReg"))));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value === "") {
          callback(new Error("请确认密码"));
        } else if (value !== adminForm.password) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

// 检查数据库状态
const checkDatabaseStatus = async () => {
  initDbLoading.value = true;
  try {
    const response = await getDatabaseStatus();
    if (response.code === 200) {
      dbStatus.value = response.data;
      if (response.data.connected) {
        // 数据库连接成功，进入下一步
        // 注意：表结构初始化由 Flyway 自动处理，无需手动操作
        currentStep.value = 1;
      }
    } else {
      ElMessage.error(response.message || "检查数据库状态失败");
    }
  } catch (error: any) {
    console.error("检查数据库状态失败", error);
    ElMessage.error(
      error?.message || "检查数据库状态失败，请检查后端服务是否正常",
    );
  } finally {
    initDbLoading.value = false;
  }
};

// 创建超级管理员
const createAdmin = async () => {
  if (!adminFormRef.value) return;
  await adminFormRef.value.validate(async (valid) => {
    if (valid) {
      creating.value = true;
      try {
        const response = await createSuperAdmin(adminForm);
        if (response.code === 200) {
          // 清除初始化状态缓存，确保下次检查时能获取最新状态
          clearInitStatusCache();
          ElMessage.success("超级管理员创建成功，正在跳转到登录页...");
          setTimeout(() => {
            router.push("/login");
          }, 1500);
        } else {
          ElMessage.error(response.message || "创建失败");
        }
      } catch (error: any) {
        ElMessage.error(error?.message || "创建失败");
      } finally {
        creating.value = false;
      }
    }
  });
};

onMounted(async () => {
  // 页面加载时自动检查数据库状态
  // 注意：初始化状态检查已在路由守卫中完成
  // 如果页面能显示，说明系统未初始化，直接显示初始化页面
  await checkDatabaseStatus();
});
</script>

<style scoped>
@import url("@/style/login.css");
</style>

<style lang="scss" scoped>
.init-container {
  width: 100vw;
  height: 100vh;
  max-width: 100%;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 18rem;
  padding: 0 2rem;
}

.img {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.img img {
  width: 500px;
}

.init-box {
  display: flex;
  align-items: center;
  text-align: center;
  overflow: hidden;
}

.init-form {
  width: 360px;
}

.avatar {
  width: 350px;
  height: 80px;
}

.init-form h2 {
  text-transform: uppercase;
  margin: 15px 0;
  color: #999;
  font:
    bold 200% Consolas,
    Monaco,
    monospace;
}

.step-content {
  margin-top: 24px;
}

.step-panel {
  min-height: 200px;
}

@media screen and (max-width: 1180px) {
  .init-container {
    grid-gap: 9rem;
  }

  .init-form {
    width: 290px;
  }

  .init-form h2 {
    font-size: 2.4rem;
    margin: 8px 0;
  }

  .img img {
    width: 360px;
  }

  .avatar {
    width: 280px;
    height: 80px;
  }
}

@media screen and (max-width: 968px) {
  .wave {
    display: none;
  }

  .img {
    display: none;
  }

  .init-container {
    grid-template-columns: 1fr;
  }

  .init-box {
    justify-content: center;
  }
}
</style>
