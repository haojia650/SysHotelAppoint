<template>
    <div class="login-container">
        <div class="bg-decoration" aria-hidden="true">
            <span class="blob blob-1"></span>
            <span class="blob blob-2"></span>
            <span class="blob blob-3"></span>
            <span class="beam beam-1"></span>
            <span class="beam beam-2"></span>
        </div>

        <div class="login-shell">
            <div class="login-left">
                <img class="login-image" src="@/assets/loginbg.png" alt="找回密码背景图">
                <div class="login-left-overlay" aria-hidden="true"></div>
                <div class="login-left-content">
                    <div class="brand">
                        <div class="brand-dot" aria-hidden="true"></div>
                        <div class="brand-text">
                            <div class="brand-title">酒店预约管理系统</div>
                            <div class="brand-subtitle">安全重置密码，保障账号安全</div>
                        </div>
                    </div>

                    <div class="hero-slogan">
                        <div class="hero-title">忘记密码也不怕</div>
                        <div class="hero-desc">通过预留邮箱与联系方式校验身份，重置一个更安全的新密码。</div>
                    </div>

                    <div class="left-bottom">
                        <div class="brand-hint">请确认填写的信息真实有效，以便顺利完成找回。</div>
                        <div class="left-meta" aria-hidden="true">
                            <span class="meta-chip">多重校验</span>
                            <span class="meta-chip">验证码防刷</span>
                            <span class="meta-chip">数据安全</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="login-right">
                <div class="login-form-container">
                    <div class="login-header">
                        <div class="login-title-row">
                            <div class="login-title">找回密码</div>
                            <span class="title-chip" aria-hidden="true">密码重置</span>
                        </div>
                        <div class="login-desc">请填写账号及验证信息，以重置登录密码</div>
                    </div>

                    <el-form ref="loginFormRef" :model="formData" label-position="top" :rules="rules" class="login-form">
                        <el-form-item label="账号" prop="UserName">
                            <el-input v-model="formData.UserName" placeholder="请输入账号" clearable />
                        </el-form-item>

                        <el-form-item label="邮箱" prop="Email">
                            <el-input v-model="formData.Email" placeholder="请输入邮箱" clearable />
                        </el-form-item>

                        <el-form-item label="联系方式" prop="PhoneNumber">
                            <el-input v-model="formData.PhoneNumber" placeholder="请输入联系方式" clearable />
                        </el-form-item>

                        <el-form-item label="新密码" prop="Password">
                            <el-input type="password" v-model="formData.Password" show-password placeholder="请输入8-16位新密码"
                                clearable />
                        </el-form-item>

                        <el-form-item label="验证码" prop="Code">
                            <div class="code-container">
                                <el-input v-model="formData.Code" placeholder="请输入验证码" maxlength="6" />
                                <span class="code-image" title="验证码">
                                    <ValidCode ref="validCodeRef" />
                                </span>
                            </div>
                        </el-form-item>

                        <el-form-item class="login-actions">
                            <el-button type="primary" class="login-button" @click="handleForgetPassword">
                                确 定
                            </el-button>
                        </el-form-item>
                    </el-form>

                    <div class="login-options">
                        <div class="register-link">
                            <span class="muted">想起密码了？</span>
                            <RouterLink to="/Login">
                                <span class="link-text">去登录</span>
                            </RouterLink>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { Post } from '@/api/http';
import ValidCode from '@/components/Identifyingcode/ImageCode.vue';
import { ElMessage } from 'element-plus';
// 1. 引入 onMounted 和 onUnmounted
import { reactive, ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';

// 路由实例
const router = useRouter()

// 表单引用
const loginFormRef = ref(null)
const validCodeRef = ref(null)

// 表单数据
const formData = reactive({
    UserName: '',
    Password: '',
    Email: '',
    PhoneNumber: '',
    Code: ''
})

// 2. 定义键盘事件处理函数
const handleKeyPress = (event) => {
    // 如果按下的是 Enter 键，则触发确定按钮
    if (event.key === 'Enter') {
        handleForgetPassword()
    }
}

// 表单验证规则
const rules = {
    UserName: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { min: 3, max: 20, message: '账号长度应在3到20个字符之间', trigger: 'blur' }
    ],
    Password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {

                var reg = /^.{8,16}$/;
                if (!value || !reg.test(value)) {
                    callback(new Error('请输入8-16位密码'));
                } else {
                    callback();
                }
            }, trigger: 'blur'
        },
    ],
    Email: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                const reg = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/
                if (!value || !reg.test(value)) {
                    callback(new Error('请输入正确邮箱'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],
    PhoneNumber: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                const reg = /^1[123456789]\d{9}$/
                if (!value || !reg.test(value)) {
                    callback(new Error('请输入正确的手机号'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],
    Code: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                const identifyCode = validCodeRef.value.getCode()
                if (value !== identifyCode) {
                    callback(new Error('请输入正确的验证码'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ]
}

// 处理找回密码
const handleForgetPassword = async () => {
    if (!loginFormRef.value) return

    await loginFormRef.value.validate(async (valid) => {
        if (valid) {
            try {
                const res = await Post('/User/ForgetPassword', formData)
                if (res.Success) {
                    ElMessage.success('修改密码成功!')
                    router.push('/Login')
                } else {
                    validCodeRef.value.refreshCode()
                }
            } catch (error) {
                console.error(error)
                validCodeRef.value.refreshCode()
            }
        } else {
            ElMessage.error('验证不通过')
            validCodeRef.value.refreshCode()
        }
    })
}

// 3. 组件挂载时绑定键盘事件
onMounted(() => {
    window.addEventListener('keydown', handleKeyPress)
})

// 4. 组件卸载时移除键盘事件监听
onUnmounted(() => {
    window.removeEventListener('keydown', handleKeyPress)
})
</script>

<style scoped lang="scss">
.login-container {
    min-height: 100vh;
    height: 100vh;
    padding: 0;
    background:
        radial-gradient(1200px 700px at 14% 12%, rgba(236, 72, 153, 0.28), transparent 60%),
        radial-gradient(980px 640px at 86% 18%, rgba(56, 189, 248, 0.20), transparent 58%),
        radial-gradient(980px 760px at 68% 86%, rgba(124, 58, 237, 0.22), transparent 62%),
        linear-gradient(135deg, #6d28d9 0%, #ec4899 45%, #38bdf8 100%);
    position: relative;
    overflow: hidden;
    color-scheme: light;
}

.login-shell {
    position: relative;
    z-index: 1;
    width: 100%;
    height: 100%;
    min-height: 100vh;
    display: flex;
}

.bg-decoration {
    position: absolute;
    inset: 0;
    pointer-events: none;
    filter: blur(0px);
}

.bg-decoration::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
        linear-gradient(rgba(15, 23, 42, 0.04) 1px, transparent 1px),
        linear-gradient(90deg, rgba(15, 23, 42, 0.04) 1px, transparent 1px),
        radial-gradient(1px 1px at 10% 20%, rgba(15, 23, 42, 0.10), transparent 45%),
        radial-gradient(1px 1px at 30% 70%, rgba(15, 23, 42, 0.08), transparent 45%),
        radial-gradient(1px 1px at 80% 30%, rgba(15, 23, 42, 0.08), transparent 45%),
        radial-gradient(1px 1px at 70% 85%, rgba(15, 23, 42, 0.08), transparent 45%);
    background-size: 44px 44px, 44px 44px, auto, auto, auto, auto;
    opacity: 0.8;
    mix-blend-mode: multiply;
}

.blob {
    position: absolute;
    width: 420px;
    height: 420px;
    border-radius: 999px;
    opacity: 0.55;
    filter: blur(18px);
    transform: translate3d(0, 0, 0);
    animation: floaty 12s ease-in-out infinite;
}

.blob-1 {
    left: -120px;
    top: -140px;
    background: radial-gradient(circle at 30% 30%, rgba(236, 72, 153, 0.78), rgba(236, 72, 153, 0.06));
}

.blob-2 {
    right: -160px;
    top: -120px;
    background: radial-gradient(circle at 30% 30%, rgba(56, 189, 248, 0.70), rgba(56, 189, 248, 0.06));
    animation-delay: -4s;
}

.blob-3 {
    right: -120px;
    bottom: -170px;
    background: radial-gradient(circle at 30% 30%, rgba(124, 58, 237, 0.62), rgba(124, 58, 237, 0.06));
    animation-delay: -8s;
}

.beam {
    position: absolute;
    width: 900px;
    height: 260px;
    border-radius: 999px;
    filter: blur(22px);
    opacity: 0.45;
    transform: rotate(-14deg);
    animation: beamShift 16s ease-in-out infinite;
}

.beam-1 {
    left: -240px;
    top: 18%;
    background: linear-gradient(90deg, rgba(236, 72, 153, 0.00), rgba(236, 72, 153, 0.22), rgba(56, 189, 248, 0.00));
}

.beam-2 {
    right: -340px;
    bottom: 10%;
    height: 300px;
    transform: rotate(12deg);
    background: linear-gradient(90deg, rgba(124, 58, 237, 0.00), rgba(124, 58, 237, 0.20), rgba(56, 189, 248, 0.00));
    animation-delay: -6s;
}

.login-left {
    flex: 1.2;
    display: flex;
    align-items: center;
    justify-content: center;
    background:
        radial-gradient(900px 520px at 20% 20%, rgba(236, 72, 153, 0.32), transparent 62%),
        radial-gradient(900px 520px at 86% 30%, rgba(56, 189, 248, 0.26), transparent 64%),
        linear-gradient(135deg, rgba(109, 40, 217, 0.72), rgba(236, 72, 153, 0.36), rgba(56, 189, 248, 0.20));
    overflow: hidden;
    position: relative;
    z-index: 1;
}

.login-image {
    width: 100%;
    height: 100%;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    object-fit: cover;
    opacity: 0.18;
    filter: saturate(0.9) contrast(1.05) blur(1px);
}

.login-left-overlay {
    position: absolute;
    inset: 0;
    background:
        linear-gradient(135deg, rgba(2, 6, 23, 0.66), rgba(2, 6, 23, 0.12)),
        radial-gradient(900px 520px at 24% 18%, rgba(236, 72, 153, 0.26), transparent 62%),
        radial-gradient(900px 520px at 80% 26%, rgba(56, 189, 248, 0.18), transparent 65%);
    mix-blend-mode: multiply;
    pointer-events: none;
}

.login-left-content {
    position: relative;
    z-index: 1;
    width: 100%;
    height: 100%;
    padding: 42px 40px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    color: rgba(255, 255, 255, 0.92);
}

.brand {
    display: flex;
    align-items: center;
    gap: 12px;
}

.brand-dot {
    width: 12px;
    height: 12px;
    border-radius: 999px;
    background: linear-gradient(135deg, #7c3aed, #ec4899, #38bdf8);
    box-shadow: 0 0 0 6px rgba(236, 72, 153, 0.14);
}

.brand-title {
    font-size: 18px;
    font-weight: 700;
    letter-spacing: 0.5px;
}

.brand-subtitle {
    margin-top: 4px;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.76);
}

.brand-hint {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.82);
}

.hero-slogan {
    margin-top: 18px;
    max-width: 460px;
}

.hero-title {
    font-size: 34px;
    font-weight: 900;
    letter-spacing: 0.6px;
    line-height: 1.16;
    text-shadow: 0 18px 40px rgba(2, 6, 23, 0.45);
}

.hero-desc {
    margin-top: 10px;
    font-size: 14px;
    line-height: 1.7;
    color: rgba(255, 255, 255, 0.78);
    max-width: 520px;
}

.left-bottom {
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.left-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.meta-chip {
    display: inline-flex;
    align-items: center;
    height: 28px;
    padding: 0 12px;
    border-radius: 999px;
    font-size: 12px;
    color: rgba(255, 255, 255, 0.86);
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.16);
    backdrop-filter: blur(6px);
}

.login-right {
    flex: 0.9;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: clamp(28px, 4vw, 56px);
    background:
        linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.84));
    position: relative;
    z-index: 1;
}

.login-right::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
        radial-gradient(600px 260px at 80% 10%, rgba(56, 189, 248, 0.12), transparent 60%),
        radial-gradient(540px 260px at 10% 92%, rgba(236, 72, 153, 0.10), transparent 62%);
    pointer-events: none;
}

.login-form-container {
    width: 100%;
    max-width: 420px;
    margin: 0 auto;
    position: relative;
    z-index: 1;
    padding: 26px 24px 20px;
    border-radius: 16px;
    background: rgba(255, 255, 255, 0.86);
    border: 1px solid rgba(15, 23, 42, 0.08);
    box-shadow:
        0 16px 34px rgba(15, 23, 42, 0.10),
        0 4px 12px rgba(15, 23, 42, 0.06);
}

.login-header {
    margin-bottom: 18px;
}

.login-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.title-chip {
    display: inline-flex;
    align-items: center;
    height: 28px;
    padding: 0 12px;
    border-radius: 999px;
    font-size: 12px;
    color: #0f172a;
    background: rgba(236, 72, 153, 0.14);
    border: 1px solid rgba(236, 72, 153, 0.22);
}

.login-title {
    font-size: 26px;
    color: #0f172a;
    font-weight: 800;
    letter-spacing: 0.2px;
}

.login-desc {
    margin-top: 8px;
    font-size: 13px;
    color: #64748b;
}

.login-form {
    margin-top: 10px;
}

.code-container {
    display: flex;
    align-items: center;
    gap: 12px;
}

.code-image {
    flex-shrink: 0;
    display: inline-flex;
    border-radius: 10px;
    overflow: hidden;
    background: rgba(15, 23, 42, 0.04);
    border: 1px solid rgba(15, 23, 42, 0.08);
}

.login-actions {
    margin-top: 6px;
}

.login-button {
    width: 100%;
    height: 44px;
    font-size: 16px;
    font-weight: 700;
    letter-spacing: 2px;
    border-radius: 12px;
    background: linear-gradient(135deg, #7c3aed 0%, #ec4899 48%, #38bdf8 100%);
    border: none;
    box-shadow:
        0 14px 28px rgba(236, 72, 153, 0.24),
        0 4px 10px rgba(15, 23, 42, 0.08);
    transition: transform 0.15s ease, box-shadow 0.15s ease, filter 0.15s ease;
}

.login-button:hover {
    filter: brightness(1.03);
    transform: translateY(-1px);
    box-shadow:
        0 18px 34px rgba(236, 72, 153, 0.28),
        0 6px 12px rgba(15, 23, 42, 0.10);
}

.login-button:active {
    transform: translateY(0px);
}

.login-options {
    display: flex;
    justify-content: space-between;
    margin-top: 20px;
    font-size: 14px;
    color: #606266;
}

.muted {
    color: #64748b;
}

.link-text {
    color: #7c3aed;
    margin-left: 5px;
    cursor: pointer;
    transition: color 0.3s;
}

.link-text:hover {
    color: #ec4899;
}

@media (max-width: 992px) {
    .login-container {
        height: auto;
        min-height: 100vh;
    }

    .login-shell {
        flex-direction: column;
        height: auto;
        min-height: 100vh;
    }

    .login-left {
        flex: none;
        min-height: 44vh;
    }

    .login-left-content {
        padding: 34px 24px;
    }

    .login-right {
        flex: 1;
        padding: 26px 18px 34px;
    }

    .login-form-container {
        max-width: 520px;
        padding: 22px 18px 18px;
        border-radius: 14px;
    }
}

@keyframes floaty {
    0%,
    100% {
        transform: translate3d(0, 0, 0) scale(1);
    }

    50% {
        transform: translate3d(0, -14px, 0) scale(1.02);
    }
}

@keyframes beamShift {
    0%,
    100% {
        opacity: 0.42;
        filter: blur(22px);
        transform: translate3d(0, 0, 0) rotate(-14deg);
    }

    50% {
        opacity: 0.52;
        filter: blur(24px);
        transform: translate3d(0, -12px, 0) rotate(-10deg);
    }
}
</style>

