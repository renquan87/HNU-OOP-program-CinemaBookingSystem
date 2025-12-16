<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { reactive, ref } from "vue";
import Motion from "../utils/motion";
import { message } from "@/utils/message";
import { getRegister } from "@/api/user";
import { REGEXP_EMAIL, updateRules } from "../utils/rule";
import type { FormInstance, FormRules } from "element-plus";
import { useVerifyCode } from "../utils/verifyCode";
import { $t, transformI18n } from "@/plugins/i18n";
import { useUserStoreHook } from "@/store/modules/user";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import Lock from "~icons/ri/lock-fill";
import Iphone from "~icons/ep/iphone";
import User from "~icons/ri/user-3-fill";
import Mail from "~icons/ri/mail-fill";
import Keyhole from "~icons/ri/shield-keyhole-line";

const { t } = useI18n();
const checked = ref(false);
const loading = ref(false);
const ruleForm = reactive({
  username: "",
  nickname: "",
  phone: "",
  email: "",
  verifyCode: "",
  password: "",
  repeatPassword: ""
});
const ruleFormRef = ref<FormInstance>();
const { isDisabled, text } = useVerifyCode();
const registerRules = reactive<FormRules>({
  ...updateRules,
  username: [
    {
      required: true,
      message: transformI18n($t("login.pureUsernameReg")),
      trigger: "blur"
    }
  ],
  nickname: [
    {
      required: true,
      message: transformI18n($t("login.pureNickNameReg")),
      trigger: "blur"
    }
  ],
  email: [
    {
      required: true,
      message: transformI18n($t("login.pureEmailReg")),
      trigger: "blur"
    },
    {
      pattern: REGEXP_EMAIL,
      message: transformI18n($t("login.pureEmailCorrectReg")),
      trigger: "blur"
    }
  ]
});
const repeatPasswordRule = [
  {
    validator: (rule, value, callback) => {
      if (value === "") {
        callback(new Error(transformI18n($t("login.purePassWordSureReg"))));
      } else if (ruleForm.password !== value) {
        callback(
          new Error(transformI18n($t("login.purePassWordDifferentReg")))
        );
      } else {
        callback();
      }
    },
    trigger: "blur"
  }
];

const onUpdate = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  loading.value = true;
  try {
    await formEl.validate();
  } catch {
    loading.value = false;
    return;
  }

  if (!checked.value) {
    loading.value = false;
    message(transformI18n($t("login.pureTickPrivacy")), {
      type: "warning"
    });
    return;
  }

  try {
    const res = await getRegister({
      username: ruleForm.username,
      nickname: ruleForm.nickname,
      phone: ruleForm.phone,
      email: ruleForm.email,
      password: ruleForm.password
    });

    if (res.success) {
      message(transformI18n($t("login.pureRegisterSuccess")), {
        type: "success"
      });
      useVerifyCode().end();
      useUserStoreHook().SET_CURRENTPAGE(0);
    } else {
      message(res.message || transformI18n($t("login.pureRegisterFail")), {
        type: "error"
      });
    }
  } catch (error) {
    console.error(error);
    message(transformI18n($t("login.pureRegisterError")), {
      type: "error"
    });
  } finally {
    loading.value = false;
  }
};

function onBack() {
  useVerifyCode().end();
  useUserStoreHook().SET_CURRENTPAGE(0);
}
</script>

<template>
  <el-form
    ref="ruleFormRef"
    :model="ruleForm"
    :rules="registerRules"
    size="large"
  >
    <Motion>
      <el-form-item
        :rules="[
          {
            required: true,
            message: transformI18n($t('login.pureUsernameReg')),
            trigger: 'blur'
          }
        ]"
        prop="username"
      >
        <el-input
          v-model="ruleForm.username"
          clearable
          :placeholder="t('login.pureUsername')"
          :prefix-icon="useRenderIcon(User)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="100">
      <el-form-item prop="nickname">
        <el-input
          v-model="ruleForm.nickname"
          clearable
          :placeholder="t('login.pureNickName')"
          :prefix-icon="useRenderIcon(User)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="150">
      <el-form-item prop="phone">
        <el-input
          v-model="ruleForm.phone"
          clearable
          :placeholder="t('login.purePhone')"
          :prefix-icon="useRenderIcon(Iphone)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="200">
      <el-form-item prop="email">
        <el-input
          v-model="ruleForm.email"
          clearable
          :placeholder="t('login.pureEmail')"
          :prefix-icon="useRenderIcon(Mail)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="250">
      <el-form-item prop="verifyCode">
        <div class="w-full flex justify-between">
          <el-input
            v-model="ruleForm.verifyCode"
            clearable
            :placeholder="t('login.pureSmsVerifyCode')"
            :prefix-icon="useRenderIcon(Keyhole)"
          />
          <el-button
            :disabled="isDisabled"
            class="ml-2!"
            @click="useVerifyCode().start(ruleFormRef, 'phone')"
          >
            {{
              text.length > 0
                ? text + t("login.pureInfo")
                : t("login.pureGetVerifyCode")
            }}
          </el-button>
        </div>
      </el-form-item>
    </Motion>

    <Motion :delay="300">
      <el-form-item prop="password">
        <el-input
          v-model="ruleForm.password"
          clearable
          show-password
          :placeholder="t('login.purePassword')"
          :prefix-icon="useRenderIcon(Lock)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="350">
      <el-form-item :rules="repeatPasswordRule" prop="repeatPassword">
        <el-input
          v-model="ruleForm.repeatPassword"
          clearable
          show-password
          :placeholder="t('login.pureSure')"
          :prefix-icon="useRenderIcon(Lock)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="400">
      <el-form-item>
        <el-checkbox v-model="checked">
          {{ t("login.pureReadAccept") }}
        </el-checkbox>
        <el-button link type="primary">
          {{ t("login.purePrivacyPolicy") }}
        </el-button>
      </el-form-item>
    </Motion>

    <Motion :delay="450">
      <el-form-item>
        <el-button
          class="w-full"
          size="default"
          type="primary"
          :loading="loading"
          @click="onUpdate(ruleFormRef)"
        >
          {{ t("login.pureDefinite") }}
        </el-button>
      </el-form-item>
    </Motion>

    <Motion :delay="500">
      <el-form-item>
        <el-button class="w-full" size="default" @click="onBack">
          {{ t("login.pureBack") }}
        </el-button>
      </el-form-item>
    </Motion>
  </el-form>
</template>
