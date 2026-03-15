<script setup lang="ts">
import { formUpload } from "@/api/mock";
import { message } from "@/utils/message";
import { onMounted, reactive, ref } from "vue";
import {
  type UserInfo,
  getMine,
  updateProfile,
  updatePassword,
} from "@/api/user";
import type { FormInstance, FormRules } from "element-plus";
import ReCropperPreview from "@/components/ReCropperPreview";
import { createFormData, deviceDetection } from "@pureadmin/utils";
import uploadLine from "~icons/ri/upload-line";

defineOptions({
  name: "Profile",
});

const imgSrc = ref("");
const cropperBlob = ref();
const cropRef = ref();
const uploadRef = ref();
const isShow = ref(false);
const userInfoFormRef = ref<FormInstance>();
const passwordFormRef = ref<FormInstance>();
const activeTab = ref("profile");

const userInfos = reactive({
  avatar: "",
  nickname: "",
  email: "",
  phone: "",
  description: "",
});

// 密码修改表单
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const rules = reactive<FormRules<UserInfo>>({
  nickname: [{ required: true, message: "昵称必填", trigger: "blur" }],
  email: [
    { required: true, message: "邮箱必填", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱地址", trigger: "blur" },
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号码",
      trigger: "blur",
    },
  ],
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

function queryEmail(queryString, callback) {
  const emailList = [
    { value: "@qq.com" },
    { value: "@126.com" },
    { value: "@163.com" },
  ];
  let results = [];
  let queryList = [];
  emailList.map((item) =>
    queryList.push({ value: queryString.split("@")[0] + item.value }),
  );
  results = queryString
    ? queryList.filter(
        (item) =>
          item.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0,
      )
    : queryList;
  callback(results);
}

const onChange = (uploadFile) => {
  const reader = new FileReader();
  reader.onload = (e) => {
    imgSrc.value = e.target.result as string;
    isShow.value = true;
  };
  reader.readAsDataURL(uploadFile.raw);
};

const handleClose = () => {
  cropRef.value.hidePopover();
  uploadRef.value.clearFiles();
  isShow.value = false;
};

const onCropper = ({ blob }) => (cropperBlob.value = blob);

const handleSubmitImage = async () => {
  try {
    const formData = createFormData({
      file: new File([cropperBlob.value], "avatar.jpg", { type: "image/jpeg" }),
    });
    const { code, data } = await formUpload(formData, "avatar");
    if (code === 200 && data) {
      // 更新用户头像
      await updateProfile({
        avatar: data.url,
      });
      message("更新头像成功", { type: "success" });
      handleClose();
      loadUserInfo();
    } else {
      message("更新头像失败", { type: "error" });
    }
  } catch (error) {
    console.error("头像上传失败", error);
    message(`提交异常 ${error}`, { type: "error" });
  }
};

// 更新个人信息
const onSubmit = async (formEl: FormInstance) => {
  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        await updateProfile({
          nickname: userInfos.nickname,
          email: userInfos.email,
          phone: userInfos.phone,
          description: userInfos.description,
        });
        message("更新信息成功", { type: "success" });
        loadUserInfo();
      } catch (error) {
        message("更新信息失败", { type: "error" });
      }
    }
  });
};

// 修改密码
const onSubmitPassword = async (formEl: FormInstance) => {
  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        await updatePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword,
        });
        message("修改密码成功", { type: "success" });
        passwordForm.oldPassword = "";
        passwordForm.newPassword = "";
        passwordForm.confirmPassword = "";
        formEl.resetFields();
      } catch (error) {
        message("修改密码失败", { type: "error" });
      }
    }
  });
};

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const { code, data } = await getMine();
    if (code === 0) {
      userInfos.avatar = data.avatar || "";
      userInfos.nickname = data.nickname || data.username;
      userInfos.email = data.email;
      userInfos.phone = data.phone || "";
      userInfos.description = data.description || "";
    }
  } catch (error) {
    message("加载用户信息失败", { type: "error" });
  }
};

onMounted(() => {
  loadUserInfo();
});
</script>

<template>
  <div
    :class="[
      'min-w-[180px]',
      deviceDetection() ? 'max-w-[100%]' : 'max-w-[70%]',
    ]"
  >
    <h3 class="my-8!">个人信息</h3>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本资料" name="profile">
        <el-form
          ref="userInfoFormRef"
          label-position="top"
          :rules="rules"
          :model="userInfos"
        >
          <el-form-item label="头像">
            <el-avatar :size="80" :src="userInfos.avatar" />
            <el-upload
              ref="uploadRef"
              accept="image/*"
              action="#"
              :limit="1"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="onChange"
            >
              <el-button plain class="ml-4!">
                <IconifyIconOffline :icon="uploadLine" />
                <span class="ml-2">更新头像</span>
              </el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="userInfos.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-autocomplete
              v-model="userInfos.email"
              :fetch-suggestions="queryEmail"
              :trigger-on-focus="false"
              placeholder="请输入邮箱"
              clearable
              class="w-full"
            />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input
              v-model="userInfos.phone"
              placeholder="请输入联系电话"
              clearable
            />
          </el-form-item>
          <el-form-item label="简介">
            <el-input
              v-model="userInfos.description"
              placeholder="请输入简介"
              type="textarea"
              :autosize="{ minRows: 6, maxRows: 8 }"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          <el-button type="primary" @click="onSubmit(userInfoFormRef)">
            更新信息
          </el-button>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="修改密码" name="password">
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
              placeholder="请输入新密码"
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
          <el-button type="primary" @click="onSubmitPassword(passwordFormRef)">
            修改密码
          </el-button>
        </el-form>
      </el-tab-pane>
    </el-tabs>
    <el-dialog
      v-model="isShow"
      width="40%"
      title="编辑头像"
      destroy-on-close
      :closeOnClickModal="false"
      :before-close="handleClose"
      :fullscreen="deviceDetection()"
    >
      <ReCropperPreview ref="cropRef" :imgSrc="imgSrc" @cropper="onCropper" />
      <template #footer>
        <div class="dialog-footer">
          <el-button bg text @click="handleClose">取消</el-button>
          <el-button bg text type="primary" @click="handleSubmitImage">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
