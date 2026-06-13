<template>
  <!-- 酒店预约系统 - 精致黑色页脚 -->
  <el-footer class="hotel-footer">
    <!-- 顶部装饰线 -->
    <div class="footer-top-accent"></div>

    <div class="footer-container">
      <!-- 四列布局 -->
      <el-row :gutter="40">
        <!-- 关于我们 -->
        <el-col :xs="24" :sm="12" :md="6" class="footer-col">
          <h3 class="footer-title">
            <span class="title-dot"></span>关于我们
          </h3>
          <p class="footer-desc">我们致力于为您提供舒适、便捷的住宿体验</p>
          <p class="footer-info">
            <span class="info-icon">
              <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
                <circle cx="12" cy="9" r="2.5"/>
              </svg>
            </span>
            酒店地址：中国某省某市某区某街123号
          </p>
        </el-col>

        <!-- 联系我们 -->
        <el-col :xs="24" :sm="12" :md="6" class="footer-col">
          <h3 class="footer-title">
            <span class="title-dot"></span>联系我们
          </h3>
          <p class="footer-info">
            <span class="info-icon">
              <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
              </svg>
            </span>
            预订电话：400-123-4567
          </p>
          <p class="footer-info">
            <span class="info-icon">
              <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="4" width="20" height="16" rx="2"/>
                <path d="M22 4l-10 7L2 4"/>
              </svg>
            </span>
            客服邮箱：hotel@163.com
          </p>
        </el-col>

        <!-- 快速链接 -->
        <el-col :xs="24" :sm="12" :md="6" class="footer-col">
          <h3 class="footer-title">
            <span class="title-dot"></span>快速链接
          </h3>
          <div class="footer-links">
            <router-link to="/" class="footer-link-item">
              <span class="link-arrow">→</span>首页
            </router-link>
            <router-link to="/booking" class="footer-link-item">
              <span class="link-arrow">→</span>客房预订
            </router-link>
            <router-link to="/service" class="footer-link-item">
              <span class="link-arrow">→</span>酒店服务
            </router-link>
            <router-link to="/about" class="footer-link-item">
              <span class="link-arrow">→</span>关于我们
            </router-link>
          </div>
        </el-col>

        <!-- 关注我们 -->
        <el-col :xs="24" :sm="12" :md="6" class="footer-col">
          <h3 class="footer-title">
            <span class="title-dot"></span>关注我们
          </h3>
          <div class="social">
            <span class="social-btn social-wechat" title="微信">微</span>
            <span class="social-btn social-weibo" title="微博">博</span>
            <span class="social-btn social-douyin" title="抖音">抖</span>
          </div>
          <p class="social-hint">扫码关注，获取最新优惠</p>
        </el-col>
      </el-row>

      <!-- 版权信息 -->
      <div class="copyright">
        <div class="copyright-line"></div>
        <p class="copyright-text">
          © 2026 星奈汐音酒店预约管理系统
          <span class="copyright-divider">|</span>
          SpringBoot + Vue3 版权所有
        </p>
      </div>
    </div>
  </el-footer>
</template>

<script setup>
</script>

<style scoped lang="scss">
// ========== 核心：精致全屏黑色页脚 ==========
.hotel-footer {
  position: relative;
  background:
    // 微妙光晕 - 左侧金色暖光
    radial-gradient(ellipse at 15% 30%, rgba(255, 193, 7, 0.04) 0%, transparent 50%),
    // 微妙光晕 - 右侧暖光
    radial-gradient(ellipse at 85% 70%, rgba(255, 193, 7, 0.03) 0%, transparent 50%),
    // 主背景
    #1a1a1a;
  width: 100%;
  padding: 80px 0 0 0;    // 页脚整体上方留白
  margin: 0;
  color: #fff;
  box-sizing: border-box;
  min-height: 360px;
  overflow: hidden;
}

// ========== 顶部金色装饰线 ==========
.footer-top-accent {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 193, 7, 0.3) 15%,
    #ffc107 50%,
    rgba(255, 193, 7, 0.3) 85%,
    transparent 100%
  );
}

// ========== 内容容器（新增内部顶部间距） ==========
.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px 0 24px;   // 顶部内边距40px，让文字与黑色区域上边缘拉开距离
}

// ========== 列样式 ==========
.footer-col {
  margin-bottom: 36px;
  padding: 0 8px;
}

// ========== 标题样式 ==========
.footer-title {
  font-size: 17px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 18px;
  padding-bottom: 10px;
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  letter-spacing: 0.5px;

  // 标题左侧装饰点
  .title-dot {
    display: inline-block;
    width: 7px;
    height: 7px;
    background: #ffc107;
    border-radius: 50%;
    flex-shrink: 0;
    box-shadow: 0 0 8px rgba(255, 193, 7, 0.5);
    animation: dotPulse 2.5s ease-in-out infinite;
  }

  // 底部渐变下划线
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 32px;
    height: 2px;
    background: linear-gradient(90deg, #ffc107, rgba(255, 193, 7, 0.2));
    border-radius: 1px;
    transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1.2);
  }
}

// 标题悬停时下划线延长
.footer-col:hover .footer-title::after {
  width: 48px;
}

// 装饰点脉冲动画
@keyframes dotPulse {
  0%, 100% {
    box-shadow: 0 0 6px rgba(255, 193, 7, 0.4);
  }
  50% {
    box-shadow: 0 0 14px rgba(255, 193, 7, 0.8);
  }
}

// ========== 描述文字 ==========
.footer-desc {
  font-size: 13.5px;
  color: #a8a8a8;
  line-height: 1.7;
  margin: 0 0 14px 0;
}

// ========== 带图标的信息行 ==========
.footer-info {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  font-size: 13.5px;
  color: #b3b3b3;
  line-height: 1.7;
  margin: 0 0 10px 0;
  transition: color 0.3s ease;

  .info-icon {
    flex-shrink: 0;
    color: #ffc107;
    display: flex;
    align-items: center;
    margin-top: 2px;
    opacity: 0.8;
    transition: opacity 0.3s ease, transform 0.3s ease;
  }

  &:hover {
    color: #d4d4d4;

    .info-icon {
      opacity: 1;
      transform: translateY(-1px);
    }
  }
}

// ========== 快速链接 ==========
.footer-links {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.footer-link-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #b3b3b3;
  text-decoration: none;
  font-size: 13.5px;
  padding: 6px 0;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;

  .link-arrow {
    display: inline-block;
    font-size: 11px;
    color: #ffc107;
    opacity: 0;
    transform: translateX(-8px);
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  }

  &:hover {
    color: #ffc107;
    transform: translateX(4px);

    .link-arrow {
      opacity: 1;
      transform: translateX(0);
    }
  }
}

// ========== 社交媒体按钮 ==========
.social {
  display: flex;
  gap: 14px;
  margin-bottom: 16px;
}

.social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  font-size: 15px;
  font-weight: 600;
  color: #cccccc;
  background: rgba(255, 255, 255, 0.06);
  border: 1.5px solid rgba(255, 255, 255, 0.15);
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);
  user-select: none;
  position: relative;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
  }

  &:active {
    transform: translateY(-1px);
    transition: all 0.1s ease;
  }
}

// 微信悬停
.social-wechat:hover {
  background: #07c160;
  border-color: #07c160;
  color: #ffffff;
  box-shadow: 0 8px 24px rgba(7, 193, 96, 0.35);
}

// 微博悬停
.social-weibo:hover {
  background: #e6162d;
  border-color: #e6162d;
  color: #ffffff;
  box-shadow: 0 8px 24px rgba(230, 22, 45, 0.35);
}

// 抖音悬停
.social-douyin:hover {
  background: #ff0050;
  border-color: #ff0050;
  color: #ffffff;
  box-shadow: 0 8px 24px rgba(255, 0, 80, 0.35);
}

// 社交媒体提示文字
.social-hint {
  font-size: 12px;
  color: #777;
  margin: 0;
  letter-spacing: 0.3px;
}

// ========== 版权栏 ==========
.copyright {
  text-align: center;
  padding: 28px 0 24px 0;
  margin-top: 20px;
}

.copyright-line {
  width: 60px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  margin: 0 auto 18px auto;
}

.copyright-text {
  font-size: 13px;
  color: #777;
  margin: 0;
  letter-spacing: 0.4px;
  line-height: 1.6;
}

.copyright-divider {
  margin: 0 10px;
  color: #555;
  font-weight: 300;
}

// ========== 响应式适配 ==========
@media (max-width: 992px) {
  .hotel-footer {
    padding: 60px 0 0 0;
    min-height: auto;
  }

  .footer-container {
    padding: 30px 20px 0 20px;
  }

  .footer-col {
    margin-bottom: 28px;
  }

  .footer-title {
    font-size: 16px;
  }

  .social-btn {
    width: 38px;
    height: 38px;
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .hotel-footer {
    padding: 40px 0 0 0;
  }

  .footer-container {
    padding: 24px 16px 0 16px;
  }

  .footer-col {
    margin-bottom: 22px;
  }

  .footer-title {
    font-size: 15px;
    margin-bottom: 14px;
    padding-bottom: 8px;

    &::after {
      width: 26px;
    }

    .title-dot {
      width: 6px;
      height: 6px;
    }
  }

  .footer-col:hover .footer-title::after {
    width: 38px;
  }

  .footer-desc,
  .footer-info {
    font-size: 13px;
  }

  .footer-link-item {
    font-size: 13px;
    padding: 4px 0;
  }

  .social {
    gap: 10px;
  }

  .social-btn {
    width: 36px;
    height: 36px;
    font-size: 13px;
  }

  .copyright {
    padding: 20px 0 18px 0;
    margin-top: 10px;
  }

  .copyright-line {
    width: 40px;
    margin-bottom: 14px;
  }

  .copyright-text {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .hotel-footer {
    padding: 30px 0 0 0;
  }

  .footer-container {
    padding: 20px 12px 0 12px;
  }

  .social {
    gap: 8px;
  }

  .social-btn {
    width: 34px;
    height: 34px;
    font-size: 12px;
    border-width: 1px;
  }

  .social-hint {
    font-size: 11px;
  }

  .copyright-divider {
    margin: 0 6px;
  }
}
</style>