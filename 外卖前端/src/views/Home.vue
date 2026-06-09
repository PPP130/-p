<template>
  <div class="home">
    <!-- 顶部导航 -->
    <nav class="navbar" :class="{ scrolled: isScrolled }">
      <div class="nav-content">
        <div class="logo">
          <svg class="logo-icon" viewBox="0 0 32 32" fill="none">
            <circle cx="16" cy="16" r="14" fill="currentColor" opacity="0.08"/>
            <path d="M10 14c0-3 2.5-6 6-6s6 3 6 6c0 4-6 10-6 10s-6-6-6-10z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <circle cx="16" cy="13" r="2" fill="currentColor"/>
          </svg>
          <span class="logo-text">点餐平台</span>
        </div>
        <div class="nav-actions">
          <el-button text class="nav-btn" @click="$router.push('/login')">登录</el-button>
          <el-button type="primary" round class="nav-btn join-btn" @click="$router.push('/register')">
            店铺加盟
          </el-button>
        </div>
      </div>
    </nav>

    <!-- Hero -->
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-text">
          <h1>把好吃的<br/>推荐给更多的人</h1>
          <p>开店不用愁，从注册到上线只需几步。我们帮你搞定菜品管理、订单处理、数据分析，你只管做好每一道菜。</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" round @click="$router.push('/register')" class="btn-primary">
              开始使用
            </el-button>
            <el-button size="large" round @click="$router.push('/login')" class="btn-secondary">
              登录后台
            </el-button>
          </div>
        </div>
        <div class="hero-visual">
          <div class="visual-card">
            <div class="card-header">
              <span class="dot red"></span>
              <span class="dot yellow"></span>
              <span class="dot green"></span>
            </div>
            <div class="card-body">
              <div class="mini-stat">
                <div class="mini-label">今日营业额</div>
                <div class="mini-value">¥2,860</div>
                <div class="mini-trend up">+12% 较昨日</div>
              </div>
              <div class="mini-orders">
                <div class="mini-order-row">
                  <span class="order-id">#1024</span>
                  <span class="order-status done">已完成</span>
                </div>
                <div class="mini-order-row">
                  <span class="order-id">#1025</span>
                  <span class="order-status cooking">制作中</span>
                </div>
                <div class="mini-order-row">
                  <span class="order-id">#1026</span>
                  <span class="order-status pending">待接单</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="hero-divider"></div>
    </section>

    <!-- 核心功能 -->
    <section class="section features-section">
      <div class="section-inner">
        <div class="section-label">功能</div>
        <h2 class="section-title">你需要的，这里都有</h2>
        <p class="section-desc">四个核心模块，覆盖一家店从开张到日常运营的全部需求</p>
        <div class="features-grid">
          <div class="feature-card" v-for="(item, i) in features" :key="i" :ref="el => setCardRef(el, i)">
            <div class="feature-icon-wrap">
              <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#c67b5c" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" v-html="item.iconSvg"></svg>
            </div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据展示 -->
    <section class="section stats-section">
      <div class="section-inner">
        <div class="section-label light">数据</div>
        <h2 class="section-title light">一些数字</h2>
        <p class="section-desc light">平台上线以来，已有越来越多商家选择与我们一起成长</p>
        <div class="stats-grid">
          <div class="stat-item" v-for="(item, i) in stats" :key="i" :ref="el => setStatItemRef(el, i)">
            <div class="stat-number">
              <span class="stat-value" :ref="el => setStatValueRef(el, i)">0</span>
              <span class="stat-suffix">{{ item.suffix }}</span>
            </div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 加入流程 -->
    <section class="section process-section">
      <div class="section-inner">
        <div class="section-label">流程</div>
        <h2 class="section-title">三步开店</h2>
        <p class="section-desc">简单几步，你的线上店铺就能开始接单了</p>
        <div class="process-timeline">
          <div class="process-step" v-for="(item, i) in steps" :key="i" :ref="el => setStepRef(el, i)">
            <div class="step-number-wrap">
              <span class="step-num">{{ String(i + 1).padStart(2, '0') }}</span>
            </div>
            <div class="step-text">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </div>
          </div>
        </div>
        <div class="process-cta" ref="processCta">
          <el-button type="primary" size="large" round @click="$router.push('/register')" class="btn-cta">
            申请入驻
          </el-button>
          <p class="cta-hint">审核通过后即可登录使用</p>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <section class="footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="footer-logo">
            <svg viewBox="0 0 32 32" width="22" height="22" fill="none">
              <circle cx="16" cy="16" r="14" fill="currentColor" opacity="0.08"/>
              <path d="M10 14c0-3 2.5-6 6-6s6 3 6 6c0 4-6 10-6 10s-6-6-6-10z" stroke="currentColor" stroke-width="1.5" fill="none"/>
              <circle cx="16" cy="13" r="2" fill="currentColor"/>
            </svg>
            <span>点餐平台</span>
          </div>
          <p>让每一家餐厅都好好做菜，剩下的交给我们</p>
        </div>
        <div class="footer-links">
          <div class="footer-col">
            <h4>链接</h4>
            <a @click="$router.push('/login')">管理后台登录</a>
            <a @click="$router.push('/register')">申请店铺加盟</a>
          </div>
          <div class="footer-col">
            <h4>联系</h4>
            <p>contact@ordering-platform.com</p>
            <p>400-888-9999</p>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2026 点餐平台</p>
      </div>
    </section>

    <!-- AI客服 -->
    <AIChat />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import gsap from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import AIChat from '@/components/AIChat.vue'

gsap.registerPlugin(ScrollTrigger)

const isScrolled = ref(false)

// Refs
const featureCards = ref<HTMLElement[]>([])
const statItems = ref<HTMLElement[]>([])
const statValues = ref<HTMLElement[]>([])
const processSteps = ref<HTMLElement[]>([])
const processCta = ref<HTMLElement>()

function setCardRef(el: unknown, i: number) {
  if (el instanceof HTMLElement) featureCards.value[i] = el
}
function setStatItemRef(el: unknown, i: number) {
  if (el instanceof HTMLElement) statItems.value[i] = el
}
function setStatValueRef(el: unknown, i: number) {
  if (el instanceof HTMLElement) statValues.value[i] = el
}
function setStepRef(el: unknown, i: number) {
  if (el instanceof HTMLElement) processSteps.value[i] = el
}

const features = [
  {
    title: '菜品管理',
    desc: '分类、口味、图片，想怎么搭配就怎么配，批量操作不费劲',
    iconSvg: '<path d="M6 3v18M18 3v18M6 8h12M6 13h12M6 18h12"/>',
  },
  {
    title: '订单管理',
    desc: '新订单自动提醒，接单、出餐、配送，每一步清清楚楚',
    iconSvg: '<rect x="4" y="2" width="16" height="20" rx="2"/><path d="M8 6h8M8 10h8M8 14h5"/>',
  },
  {
    title: '数据统计',
    desc: '卖了多少、赚了多少钱，图表一目了然，决策有依据',
    iconSvg: '<path d="M4 20V10M9 20V6M14 20v-8M19 20V4"/>',
  },
  {
    title: '多店铺支持',
    desc: '一家店、十家店都一样，各管各的账，平台统一管理',
    iconSvg: '<path d="M3 21h18M5 21V8l7-5 7 5v13M9 21v-5h6v5"/>',
  },
]

const stats = [
  { value: 12000, suffix: '+', label: '累计完成订单' },
  { value: 500, suffix: '+', label: '正在经营的商家' },
  { value: 80000, suffix: '+', label: '月活用户' },
  { value: 99.9, suffix: '%', label: '系统稳定运行' },
]

const steps = [
  { title: '填写信息', desc: '花两分钟填好店名、地址和联系方式，提交就行' },
  { title: '等待审核', desc: '我们通常 1-3 个工作日就能审完，审完会通知你' },
  { title: '登录开店', desc: '拿到账号密码后登录，配置好菜品就能开始接单了' },
]

function handleScroll() {
  isScrolled.value = window.scrollY > 50
}

function animateCounter(el: HTMLElement, target: number) {
  const obj = { val: 0 }
  gsap.to(obj, {
    val: target,
    duration: 2,
    ease: 'power2.out',
    onUpdate: () => {
      el.textContent = target % 1 === 0 ? Math.floor(obj.val).toLocaleString() : obj.val.toFixed(1)
    },
  })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)

  // Hero 动画
  const heroTl = gsap.timeline()
  heroTl.from('.hero-text h1', { y: 40, opacity: 0, duration: 0.8, ease: 'power3.out' })
  heroTl.from('.hero-text p', { y: 30, opacity: 0, duration: 0.6, ease: 'power3.out' }, '-=0.4')
  heroTl.from('.hero-actions', { y: 20, opacity: 0, duration: 0.5, ease: 'power3.out' }, '-=0.3')
  heroTl.from('.hero-visual', { y: 30, opacity: 0, duration: 0.7, ease: 'power3.out' }, '-=0.5')

  // 功能卡片
  featureCards.value.filter(Boolean).forEach((el, i) => {
    gsap.set(el, { y: 40, opacity: 0 })
    ScrollTrigger.create({
      trigger: el,
      once: true,
      onEnter: () => {
        gsap.to(el, { y: 0, opacity: 1, duration: 0.6, delay: i * 0.1, ease: 'power3.out' })
      },
    })
  })

  // 数据统计
  statItems.value.filter(Boolean).forEach((el, i) => {
    gsap.set(el, { y: 30, opacity: 0 })
    ScrollTrigger.create({
      trigger: el,
      once: true,
      onEnter: () => {
        gsap.to(el, {
          y: 0, opacity: 1, duration: 0.6, delay: i * 0.08, ease: 'power3.out',
          onComplete: () => {
            if (statValues.value[i]) animateCounter(statValues.value[i]!, stats[i].value)
          },
        })
      },
    })
  })

  // 流程步骤
  processSteps.value.filter(Boolean).forEach((el, i) => {
    gsap.set(el, { y: 30, opacity: 0 })
    ScrollTrigger.create({
      trigger: el,
      once: true,
      onEnter: () => gsap.to(el, { y: 0, opacity: 1, duration: 0.6, delay: i * 0.15, ease: 'power3.out' }),
    })
  })

  if (processCta.value) {
    gsap.set(processCta.value, { y: 20, opacity: 0 })
    ScrollTrigger.create({
      trigger: processCta.value,
      once: true,
      onEnter: () => gsap.to(processCta.value!, { y: 0, opacity: 1, duration: 0.6, ease: 'power3.out' }),
    })
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  ScrollTrigger.getAll().forEach(t => t.kill())
})
</script>

<style scoped>
.home {
  overflow-x: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* 导航 */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 16px 0;
  transition: all 0.25s ease;
}

.navbar.scrolled {
  background: rgba(255, 252, 248, 0.92);
  backdrop-filter: blur(8px);
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  padding: 10px 0;
}

.nav-content {
  max-width: 1120px;
  margin: 0 auto;
  padding: 0 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #2d2016;
}

.logo-icon {
  width: 32px;
  height: 32px;
  color: #c67b5c;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-btn {
  color: #2d2016 !important;
  font-weight: 500;
  font-size: 15px;
}

.join-btn {
  background: #c67b5c !important;
  border: none !important;
  color: #fff !important;
}

.join-btn:hover {
  background: #b56b4c !important;
}

/* Hero */
.hero {
  background: #fffcf8;
  padding: 140px 32px 80px;
  position: relative;
}

.hero-inner {
  max-width: 1120px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 80px;
}

.hero-text {
  flex: 1;
}

.hero-text h1 {
  font-size: 52px;
  font-weight: 800;
  color: #2d2016;
  line-height: 1.2;
  margin-bottom: 24px;
  letter-spacing: -0.5px;
}

.hero-text p {
  font-size: 17px;
  color: #6b5d50;
  line-height: 1.75;
  margin-bottom: 36px;
  max-width: 440px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-actions .el-button {
  padding: 14px 32px;
  font-size: 16px;
  font-weight: 600;
}

.btn-primary {
  background: #c67b5c !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(198,123,92,0.3);
}

.btn-primary:hover {
  background: #b56b4c !important;
  box-shadow: 0 4px 12px rgba(198,123,92,0.35);
}

.btn-secondary {
  background: #fff !important;
  border: 1.5px solid #d4c4a8 !important;
  color: #2d2016 !important;
}

.btn-secondary:hover {
  border-color: #c67b5c !important;
  color: #c67b5c !important;
}

/* Hero 视觉 - 模拟后台界面卡片 */
.hero-visual {
  flex: 1;
  max-width: 420px;
}

.visual-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(45,32,22,0.08);
  overflow: hidden;
  border: 1px solid rgba(0,0,0,0.04);
}

.card-header {
  display: flex;
  gap: 6px;
  padding: 14px 18px;
  background: #faf7f2;
  border-bottom: 1px solid #ede8df;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot.red { background: #e8836b; }
.dot.yellow { background: #e5c07b; }
.dot.green { background: #8fb573; }

.card-body {
  padding: 24px;
}

.mini-stat {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ede8df;
}

.mini-label {
  font-size: 13px;
  color: #8b7d6f;
  margin-bottom: 6px;
}

.mini-value {
  font-size: 32px;
  font-weight: 800;
  color: #2d2016;
  letter-spacing: -0.5px;
}

.mini-trend {
  font-size: 13px;
  margin-top: 6px;
}

.mini-trend.up {
  color: #5a9a6b;
}

.mini-orders {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mini-order-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #faf7f2;
  border-radius: 10px;
  font-size: 14px;
}

.order-id {
  color: #2d2016;
  font-weight: 600;
  font-family: 'SF Mono', 'Fira Code', monospace;
}

.order-status {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.order-status.done {
  background: #e8f5e9;
  color: #3d7a4a;
}

.order-status.cooking {
  background: #fff3e0;
  color: #b56b4c;
}

.order-status.pending {
  background: #e3f2fd;
  color: #4a7fb5;
}

.hero-divider {
  max-width: 1120px;
  margin: 0 auto;
  height: 1px;
  background: linear-gradient(90deg, transparent, #e0d5c5, transparent);
}

/* Section 通用 */
.section {
  padding: 100px 32px;
}

.section-inner {
  max-width: 1120px;
  margin: 0 auto;
}

.section-label {
  font-size: 13px;
  font-weight: 600;
  color: #c67b5c;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-bottom: 12px;
}

.section-label.light {
  color: rgba(255,255,255,0.6);
}

.section-title {
  font-size: 36px;
  font-weight: 700;
  color: #2d2016;
  margin-bottom: 12px;
}

.section-title.light {
  color: #fff;
}

.section-desc {
  font-size: 16px;
  color: #8b7d6f;
  margin-bottom: 56px;
  line-height: 1.6;
}

.section-desc.light {
  color: rgba(255,255,255,0.7);
}

/* 功能卡片 */
.features-section {
  background: #faf7f2;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.feature-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px 24px;
  transition: all 0.25s ease;
  border: 1px solid #ede8df;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(45,32,22,0.06);
  border-color: transparent;
}

.feature-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: #fdf5ee;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  transition: all 0.25s ease;
}

.feature-card:hover .feature-icon-wrap {
  background: #c67b5c;
}

.feature-card:hover .feature-icon-wrap svg {
  stroke: #fff;
}

.feature-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2d2016;
  margin-bottom: 10px;
}

.feature-card p {
  font-size: 14px;
  color: #8b7d6f;
  line-height: 1.65;
}

/* 数据统计 */
.stats-section {
  background: #c67b5c;
  position: relative;
}

.stats-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #c67b5c, #a85d3f);
  pointer-events: none;
}

.stats-section .section-inner {
  position: relative;
  z-index: 1;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
}

.stat-item {
  text-align: center;
  color: #fff;
  padding: 24px;
  border-radius: 16px;
  background: rgba(255,255,255,0.1);
  transition: all 0.25s ease;
}

.stat-item:hover {
  background: rgba(255,255,255,0.16);
}

.stat-value {
  font-size: 44px;
  font-weight: 800;
  letter-spacing: -1px;
}

.stat-suffix {
  font-size: 22px;
  font-weight: 600;
  margin-left: 2px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.85;
  margin-top: 8px;
}

/* 流程 */
.process-section {
  background: #fffcf8;
}

.process-timeline {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
  margin-bottom: 56px;
}

.process-step {
  position: relative;
}

.step-number-wrap {
  margin-bottom: 20px;
}

.step-num {
  font-size: 40px;
  font-weight: 800;
  color: #ede8df;
  line-height: 1;
  font-family: 'SF Mono', 'Fira Code', monospace;
}

.step-text h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2d2016;
  margin-bottom: 10px;
}

.step-text p {
  font-size: 15px;
  color: #8b7d6f;
  line-height: 1.65;
}

.process-cta {
  text-align: center;
}

.btn-cta {
  background: #c67b5c !important;
  border: none !important;
  padding: 16px 40px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(198,123,92,0.3);
}

.btn-cta:hover {
  background: #b56b4c !important;
}

.cta-hint {
  font-size: 13px;
  color: #8b7d6f;
  margin-top: 12px;
}

/* Footer */
.footer {
  background: #2d2016;
  color: #fff;
  padding: 64px 32px 0;
}

.footer-inner {
  max-width: 1120px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  padding-bottom: 48px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #fff;
}

.footer-brand p {
  color: rgba(255,255,255,0.45);
  font-size: 14px;
  line-height: 1.6;
  max-width: 260px;
}

.footer-links {
  display: flex;
  gap: 64px;
}

.footer-col h4 {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255,255,255,0.8);
  margin-bottom: 16px;
  letter-spacing: 0.5px;
}

.footer-col a,
.footer-col p {
  display: block;
  font-size: 14px;
  color: rgba(255,255,255,0.4);
  margin-bottom: 8px;
  cursor: pointer;
  transition: color 0.2s;
}

.footer-col a:hover {
  color: #c67b5c;
}

.footer-bottom {
  max-width: 1120px;
  margin: 0 auto;
  padding: 20px 0;
  text-align: center;
}

.footer-bottom p {
  font-size: 13px;
  color: rgba(255,255,255,0.25);
}

/* 响应式 */
@media (max-width: 1024px) {
  .features-grid { grid-template-columns: repeat(2, 1fr); }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .hero-inner { gap: 48px; }
  .hero-text h1 { font-size: 42px; }
}

@media (max-width: 768px) {
  .hero { padding: 120px 24px 60px; }
  .hero-inner { flex-direction: column; gap: 40px; text-align: center; }
  .hero-text p { margin-left: auto; margin-right: auto; }
  .hero-actions { justify-content: center; }
  .hero-visual { max-width: 100%; width: 100%; }
  .hero-text h1 { font-size: 36px; }
  .section { padding: 72px 24px; }
  .section-title { font-size: 28px; }
  .features-grid { grid-template-columns: 1fr; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .process-timeline { grid-template-columns: 1fr; gap: 24px; }
  .footer-inner { flex-direction: column; gap: 36px; }
  .footer-links { flex-direction: column; gap: 24px; }
  .nav-content { padding: 0 20px; }
}

@media (max-width: 480px) {
  .hero-text h1 { font-size: 28px; }
  .hero-text p { font-size: 15px; }
  .stats-grid { grid-template-columns: 1fr; gap: 16px; }
  .stat-value { font-size: 36px; }
  .section-title { font-size: 24px; }
  .nav-content { padding: 0 16px; }
}

@media (prefers-reduced-motion: reduce) {
  * { animation-duration: 0.01ms !important; animation-iteration-count: 1 !important; transition-duration: 0.01ms !important; }
}
</style>
