<script setup lang="ts">
import { ref, reactive } from "vue";
import { useUserStoreHook } from "@/store/modules/user";
import { getRegister } from "@/api/user"; // 引入我们刚才写的 API
import { message } from "@/utils/message";
import type { FormInstance, FormRules } from "element-plus";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import Lock from "@iconify-icons/ri/lock-fill";
import Iphone from "@iconify-icons/ep/iphone";
import User from "@iconify-icons/ri/user-3-fill";
import Mail from "@iconify-icons/ri/mail-fill"; // 引入邮箱图标

defineOptions({
  name: "Regist"
});

const ruleFormRef = ref<FormInstance>();
const loading = ref(false);

const ruleForm = reactive({
  username: "", // 用户ID
  nickname: "", // 姓名
  password: "",
  repeatPassword: "",
  phone: "",
  email: ""
});

// 表单校验规则
const registRules = reactive<FormRules>({
  username: [
    { required: true, message: "请输入用户ID (账号)", trigger: "blur" }
  ],
  nickname: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码至少6位", trigger: "blur" }
  ],
  repeatPassword: [
    { required: true, message: "请再次输入密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== ruleForm.password) {
          callback(new Error("两次输入密码不一致!"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不对", trigger: "blur" }
  ]
});

// 提交注册
const onUpdate = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true;
      try {
        // 调用后端注册接口
        const res = await getRegister({
          username: ruleForm.username,
          password: ruleForm.password,
          nickname: ruleForm.nickname,
          phone: ruleForm.phone,
          email: ruleForm.email
        });

        if (res.success) {
          message("注册成功，请登录", { type: "success" });
          // 切换回登录页 (调用 useUserStoreHook().SET_CURRENTPAGE(0))
          useUserStoreHook().SET_CURRENTPAGE(0);
        } else {
          message(res.message || "注册失败", { type: "error" });
        }
      } catch (error) {
        console.error(error);
        message("注册发生错误", { type: "error" });
      } finally {
        loading.value = false;
      }
    }
  });
};

// 返回登录
const onBack = () => {
  useUserStoreHook().SET_CURRENTPAGE(0);
};
</script>

<template>
  <el-form
    ref="ruleFormRef"
    :model="ruleForm"
    :rules="registRules"
    size="large"
  >
    <el-form-item prop="username">
      <el-input
        v-model="ruleForm.username"
        clearable
        placeholder="用户ID (登录账号)"
        :prefix-icon="useRenderIcon(User)"
      />
    </el-form-item>

    <el-form-item prop="nickname">
      <el-input
        v-model="ruleForm.nickname"
        clearable
        placeholder="您的姓名"
        :prefix-icon="useRenderIcon(User)"
      />
    </el-form-item>

    <el-form-item prop="phone">
      <el-input
        v-model="ruleForm.phone"
        clearable
        placeholder="手机号码"
        :prefix-icon="useRenderIcon(Iphone)"
      />
    </el-form-item>

    <el-form-item prop="email">
      <el-input
        v-model="ruleForm.email"
        clearable
        placeholder="电子邮箱"
        :prefix-icon="useRenderIcon(Mail)"
      />
    </el-form-item>

    <el-form-item prop="password">
      <el-input
        v-model="ruleForm.password"
        clearable
        show-password
        placeholder="密码 (至少6位)"
        :prefix-icon="useRenderIcon(Lock)"
      />
    </el-form-item>

    <el-form-item prop="repeatPassword">
      <el-input
        v-model="ruleForm.repeatPassword"
        clearable
        show-password
        placeholder="确认密码"
        :prefix-icon="useRenderIcon(Lock)"
      />
    </el-form-item>

    <el-form-item>
      <el-button
        class="w-full"
        size="large"
        type="primary"
        :loading="loading"
        @click="onUpdate(ruleFormRef)"
      >
        确定注册
      </el-button>
    </el-form-item>

    <el-form-item>
      <el-button class="w-full" size="large" @click="onBack">
        返回登录
      </el-button>
    </el-form-item>
  </el-form>
</template>
