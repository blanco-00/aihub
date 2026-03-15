<script setup lang="ts">
import { ref, reactive } from "vue";
import { message } from "@/utils/message";
import { deviceDetection } from "@pureadmin/utils";
import { updatePassword, getMine, type UserInfo } from "@/api/user";
import type { FormInstance, FormRules } from "element-plus";

defineOptions({
  name: "AccountManagement",
});

const passwordDialogVisible = ref(false);
const passwordFormRef = ref<FormInstance>();

// 密码修改表单
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const passwordRules = reactive<FormRules>({
  oldPassword: [{ required: true, message: "请输入当前密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
});

// 用户信息
const userInfo = ref<UserInfo | null>(null);

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const { code, data } = await getMine();
    if (code === 0 && data) {
      userInfo.value = data;
    }
  } catch (error) {
    console.error("加载用户信息失败", error);
  }
};

// 打开密码修改对话框
const openPasswordDialog = () => {
  passwordDialogVisible.value = true;
  // 重置表单
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
  passwordFormRef.value?.resetFields();
};

// 提交密码修改
const onSubmitPassword = async (formEl: FormInstance) => {
  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        await updatePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword,
        });
        message("修改密码成功", { type: "success" });
        passwordDialogVisible.value = false;
        passwordForm.oldPassword = "";
        passwordForm.newPassword = "";
        passwordForm.confirmPassword = "";
        formEl.resetFields();
      } catch (error) {
        console.error("修改密码失败", error);
        message("修改密码失败", { type: "error" });
      }
    }
  });
};

// 获取密码强度描述
const getPasswordStrength = () => {
  // 这里可以根据实际业务逻辑判断密码强度
  return "强";
};

// 获取手机号脱敏显示
const getMaskedPhone = () => {
  if (userInfo.value?.phone) {
    const phone = userInfo.value.phone;
    if (phone.length >= 11) {
      return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    return phone;
  }
  return "未绑定";
};

// 获取邮箱脱敏显示
const getMaskedEmail = () => {
  if (userInfo.value?.email) {
    const email = userInfo.value.email;
    const atIndex = email.indexOf("@");
    if (atIndex > 0) {
      const prefix = email.substring(0, Math.min(3, atIndex));
      return prefix + "***" + email.substring(atIndex);
    }
    return email;
  }
  return "未绑定";
};

const list = ref([
  {
    title: "账户密码",
    illustrate: `当前密码强度：${getPasswordStrength()}`,
    button: "修改",
    action: "password",
  },
  {
    title: "密保手机",
    illustrate: `已经绑定手机：${getMaskedPhone()}`,
    button: "修改",
    action: "phone",
    disabled: true,
  },
  {
    title: "密保问题",
    illustrate: "未设置密保问题，密保问题可有效保护账户安全",
    button: "修改",
    action: "question",
    disabled: true,
  },
  {
    title: "备用邮箱",
    illustrate: `已绑定邮箱：${getMaskedEmail()}`,
    button: "修改",
    action: "email",
    disabled: true,
  },
]);

function onClick(item: any) {
  if (item.disabled) {
    message("该功能暂未实现", { type: "info" });
    return;
  }

  if (item.action === "password") {
    openPasswordDialog();
  } else {
    message("请根据具体业务自行实现", { type: "info" });
  }
}

// 初始化加载用户信息
loadUserInfo();
</script>

<template>
  <div
    :class="[
      'min-w-[180px]',
      deviceDetection() ? 'max-w-[100%]' : 'max-w-[70%]',
    ]"
  >
    <h3 class="my-8!">账户管理</h3>
    <div v-for="(item, index) in list" :key="index">
      <div class="flex items-center">
        <div class="flex-1">
          <p>{{ item.title }}</p>
          <el-text class="mx-1" type="info">{{ item.illustrate }}</el-text>
        </div>
        <el-button
          type="primary"
          text
          :disabled="item.disabled"
          @click="onClick(item)"
        >
          {{ item.button }}
        </el-button>
      </div>
      <el-divider />
    </div>

    <!-- 密码修改对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="500px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="passwordFormRef"
        label-position="top"
        :rules="passwordRules"
        :model="passwordForm"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="onSubmitPassword(passwordFormRef!)">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.el-divider--horizontal {
  border-top: 0.1px var(--el-border-color) var(--el-border-style);
}
</style>
