<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { register as registerApi } from '@/api/login'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)

const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

const loginForm = reactive({
  name: '',
  password: '',
})

const registerForm = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  userType: 1,
})

const loginRules: FormRules = {
  name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerRules: FormRules = {
  username: [{ required: true, message: '请输入学号/工号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  console.log("提交前表单：", loginForm)
  loading.value = true
  try {
    await userStore.login(loginForm.name, loginForm.password)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await registerApi({
      username: registerForm.username,
      realName: registerForm.realName,
      password: registerForm.password,
      userType: registerForm.userType,
      gender: 0,
    })
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.name = registerForm.username
    loginForm.password = registerForm.password
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="title">社团综合管理平台</h2>
      <p class="subtitle">Club Administration System</p>

      <el-tabs v-model="activeTab" class="auth-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            size="large"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="name">
              <el-input v-model="loginForm.name" placeholder="用户名" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">
                登 录
              </el-button>
            </el-form-item>
          </el-form>

        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" size="large">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="学号/工号" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="realName">
              <el-input v-model="registerForm.realName" placeholder="真实姓名" prefix-icon="UserFilled" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item label="用户类型">
              <el-radio-group v-model="registerForm.userType">
                <el-radio :value="1">学生</el-radio>
                <el-radio :value="2">老师</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">
                注 册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(74, 144, 217, 0.35), transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(212, 175, 55, 0.2), transparent 45%),
    linear-gradient(145deg, #1e3a5f 0%, #2a4a6f 45%, #1a2f4a 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.title {
  text-align: center;
  margin: 0 0 8px;
  font-size: 24px;
  color: #303133;
}

.subtitle {
  text-align: center;
  margin: 0 0 24px;
  color: #909399;
  font-size: 14px;
}

.auth-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }
}

.submit-btn {
  width: 100%;
}

.hint {
  text-align: center;
  color: #909399;
  font-size: 12px;
  margin: 8px 0 0;
}
</style>
