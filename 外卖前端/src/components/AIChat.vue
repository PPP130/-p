<template>
  <div class="ai-chat-wrapper">
    <!-- 浮动按钮 -->
    <div class="ai-chat-btn" @click="toggleChat" :class="{ active: isOpen }">
      <div class="chat-btn-inner">
        <svg v-if="!isOpen" viewBox="0 0 28 28" width="28" height="28" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M21 13.5C21 12 20.5 10.5 19.5 9.5C18.5 8.5 17 8 15.5 8C15.2 8 14.9 8 14.6 8.1C14.5 7.1 14.2 6.2 13.6 5.4C12.2 3.5 9.5 2.6 7.2 3.3C5.6 3.8 4.3 4.9 3.6 6.3C2.9 7.7 2.8 9.3 3.4 10.8C3.6 11.3 3.9 11.8 4.2 12.2C4.1 12.5 4 12.8 4 13.1C4 14.6 4.5 16 5.5 17C6.5 18 7.9 18.5 9.4 18.5C9.7 18.5 10 18.5 10.3 18.4C10.5 19.4 10.8 20.4 11.4 21.2C12.2 22.3 13.4 22.9 14.7 22.9C15.6 22.9 16.4 22.6 17.1 22.1C18 21.4 18.5 20.4 18.6 19.3C18.9 19.4 19.2 19.4 19.5 19.4C20.5 19.4 21.5 19 22.2 18.3C23 17.6 23.4 16.6 23.4 15.5C23.5 14.9 23.3 14.2 23 13.6C22.3 13.55 21.6 13.55 21 13.5Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="9" cy="13" r="1" fill="currentColor"/>
          <circle cx="14" cy="13" r="1" fill="currentColor"/>
          <circle cx="19" cy="13" r="1" fill="currentColor"/>
        </svg>
        <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M18 6L6 18M6 6l12 12"/>
        </svg>
      </div>
      <div class="chat-badge" v-if="!isOpen">
        <span class="pulse-dot"></span>
      </div>
    </div>

    <!-- 聊天窗口 -->
    <Transition name="slide">
      <div v-if="isOpen" class="ai-chat-window">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="header-info">
            <div class="header-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2a7 7 0 017 7c0 3-2 5.5-4 7-.7.5-1 1-1 2v1H10v-1c0-1-.3-1.5-1-2C7 14.5 5 12 5 9a7 7 0 017-7z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="9" cy="9" r="1" fill="currentColor"/>
                <circle cx="15" cy="9" r="1" fill="currentColor"/>
                <path d="M9 17h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>
            <div>
              <div class="header-title">{{ viewMode === 'history' ? '历史对话' : 'AI经营助手' }}</div>
              <div class="header-status">
                <span class="status-dot"></span>
                {{ viewMode === 'history' ? historySessions.length + '条记录' : '在线' }}
              </div>
            </div>
          </div>
          <div class="header-actions">
            <!-- 历史模式：返回按钮 -->
            <template v-if="viewMode === 'history'">
              <el-tooltip content="返回对话" placement="left" :popper-style="{ zIndex: 10000 }">
                <el-icon class="action-btn" @click="backToChat"><Back /></el-icon>
              </el-tooltip>
            </template>
            <!-- 聊天模式：历史和清空按钮 -->
            <template v-else>
              <el-tooltip v-if="loggedIn" content="历史对话" placement="left" :popper-style="{ zIndex: 10000 }">
                <el-icon class="action-btn" @click="showHistory"><Clock /></el-icon>
              </el-tooltip>
              <el-tooltip content="新对话" placement="left" :popper-style="{ zIndex: 10000 }">
                <el-icon class="action-btn" @click="clearChat"><RefreshLeft /></el-icon>
              </el-tooltip>
            </template>
          </div>
        </div>

        <!-- 历史会话列表 -->
        <div v-if="viewMode === 'history'" class="history-list">
          <div v-if="historyLoading" class="history-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <template v-else>
            <div
              v-for="session in historySessions"
              :key="session.id"
              class="history-item"
              @click="loadHistorySession(session)"
            >
              <div class="history-item-icon">
                <el-icon><ChatLineRound /></el-icon>
              </div>
              <div class="history-item-content">
                <div class="history-item-title">{{ session.title || '新对话' }}</div>
                <div class="history-item-meta">
                  <span>{{ session.messageCount }}条消息</span>
                  <span>{{ formatHistoryTime(session.updatedAt) }}</span>
                </div>
              </div>
              <el-icon
                class="history-item-delete"
                @click.stop="deleteHistorySession(session.id)"
              >
                <Delete />
              </el-icon>
            </div>
            <el-empty v-if="historySessions.length === 0" description="暂无历史对话" :image-size="80" />
          </template>
        </div>

        <!-- 历史详情查看 -->
        <div v-else-if="viewMode === 'history-detail'" class="chat-messages" ref="messagesRef">
          <div
            v-for="(msg, i) in historyMessages"
            :key="i"
            class="message-item"
            :class="msg.role"
          >
            <div class="message-avatar assistant-avatar" v-if="msg.role === 'assistant'">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2a7 7 0 017 7c0 3-2 5.5-4 7-.7.5-1 1-1 2v1H10v-1c0-1-.3-1.5-1-2C7 14.5 5 12 5 9a7 7 0 017-7z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="9" cy="9" r="1" fill="currentColor"/>
                <circle cx="15" cy="9" r="1" fill="currentColor"/>
              </svg>
            </div>
            <div class="message-bubble">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
            </div>
          </div>
        </div>

        <!-- 聊天消息列表 -->
        <div v-else class="chat-messages" ref="messagesRef">
          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="message-item"
            :class="msg.role"
          >
            <div class="message-avatar assistant-avatar" v-if="msg.role === 'assistant'">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2a7 7 0 017 7c0 3-2 5.5-4 7-.7.5-1 1-1 2v1H10v-1c0-1-.3-1.5-1-2C7 14.5 5 12 5 9a7 7 0 017-7z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="9" cy="9" r="1" fill="currentColor"/>
                <circle cx="15" cy="9" r="1" fill="currentColor"/>
              </svg>
            </div>
            <div class="message-bubble">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
            </div>
          </div>
          <div v-if="loading" class="message-item assistant">
            <div class="message-avatar assistant-avatar">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2a7 7 0 017 7c0 3-2 5.5-4 7-.7.5-1 1-1 2v1H10v-1c0-1-.3-1.5-1-2C7 14.5 5 12 5 9a7 7 0 017-7z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="9" cy="9" r="1" fill="currentColor"/>
                <circle cx="15" cy="9" r="1" fill="currentColor"/>
              </svg>
            </div>
            <div class="message-bubble">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入框（聊天模式才显示） -->
        <div v-if="viewMode === 'chat'" class="chat-input">
          <!-- 图片预览 -->
          <div v-if="imagePreviewUrl" class="image-preview">
            <img :src="imagePreviewUrl" alt="预览" />
            <el-icon class="image-remove" @click="removeImage"><Close /></el-icon>
          </div>
          <!-- 隐藏的文件input -->
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            style="display: none"
            @change="onImageSelect"
          />
          <div class="input-row">
            <el-button
              :icon="Camera"
              circle
              @click="triggerImageSelect"
              :disabled="loading"
              class="image-btn"
            />
            <el-input
              v-model="inputText"
              placeholder="问我任何经营问题..."
              :disabled="loading"
              @keyup.enter="sendMessage"
              size="large"
              class="chat-text-input"
            >
              <template #append>
                <el-button :icon="Promotion" @click="sendMessage" :loading="loading" />
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import {
  Close,
  Promotion,
  RefreshLeft,
  Clock,
  Back,
  Loading,
  ChatLineRound,
  Delete,
  Camera,
} from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

interface Props {
  shopId?: string
  loggedIn?: boolean
  token?: string
}

const props = withDefaults(defineProps<Props>(), {
  shopId: '',
  loggedIn: false,
  token: '',
})

const AI_BASE_URL = 'http://localhost:8000'

const isOpen = ref(false)
const inputText = ref('')
const loading = ref(false)
const messagesRef = ref<HTMLElement>()
const sessionId = ref<string | null>(null)
// viewMode: 'chat' | 'history' | 'history-detail'
const viewMode = ref<'chat' | 'history' | 'history-detail'>('chat')

interface Message {
  role: 'user' | 'assistant'
  content: string
}

interface HistorySession {
  id: string
  title: string
  lastMessage: string
  messageCount: number
  updatedAt: string
}

const messages = ref<Message[]>([])
const historySessions = ref<HistorySession[]>([])
const historyMessages = ref<Message[]>([])
const historyLoading = ref(false)

// 图片上传相关
const selectedImageBase64 = ref('')
const imagePreviewUrl = ref('')
const fileInputRef = ref<HTMLInputElement>()

// 选择图片
function onImageSelect(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过5MB')
    return
  }
  imagePreviewUrl.value = URL.createObjectURL(file)
  const reader = new FileReader()
  reader.onload = () => {
    const result = reader.result as string
    selectedImageBase64.value = result.split(',')[1] || ''
  }
  reader.readAsDataURL(file)
  ;(e.target as HTMLInputElement).value = ''
}

// 移除已选图片
function removeImage() {
  selectedImageBase64.value = ''
  imagePreviewUrl.value = ''
}

// 触发文件选择
function triggerImageSelect() {
  fileInputRef.value?.click()
}

const WELCOME_LOGGED_IN = '你好！我是你的AI经营助手，可以帮你：\n\n- 查询菜品信息和价格\n- 对比同行定价\n- 查看今日订单和营业额\n- 分析销售趋势\n\n有什么想了解的，直接问我就行！'
const WELCOME_ANONYMOUS = '你好，需要我为你介绍这个项目吗'

function getWelcomeMessage(): string {
  return props.loggedIn ? WELCOME_LOGGED_IN : WELCOME_ANONYMOUS
}

// 切换聊天窗口
function toggleChat() {
  isOpen.value = !isOpen.value
  if (isOpen.value && messages.value.length === 0 && viewMode.value === 'chat') {
    messages.value.push({
      role: 'assistant',
      content: getWelcomeMessage(),
    })
  }
}

// 清空聊天（新对话）
function clearChat() {
  if (sessionId.value) {
    fetch(`${AI_BASE_URL}/api/chat/${sessionId.value}`, {
      method: 'DELETE',
    }).catch(() => {})
  }
  sessionId.value = null
  messages.value = [{
    role: 'assistant',
    content: getWelcomeMessage(),
  }]
}

// 显示历史会话列表
async function showHistory() {
  viewMode.value = 'history'
  historyLoading.value = true
  try {
    const shopId = props.shopId || localStorage.getItem('shopId') || 'default'
    const response = await fetch(
      `${AI_BASE_URL}/api/chat/sessions?shop_id=${shopId}&page=1&page_size=50`
    )
    if (response.ok) {
      const data = await response.json()
      historySessions.value = data.sessions || []
    }
  } catch (error) {
    ElMessage.error('获取历史对话失败')
  } finally {
    historyLoading.value = false
  }
}

// 返回聊天模式
function backToChat() {
  viewMode.value = 'chat'
  historyMessages.value = []
  if (messages.value.length === 0) {
    messages.value.push({
      role: 'assistant',
      content: getWelcomeMessage(),
    })
  }
}

// 加载历史会话
async function loadHistorySession(session: HistorySession) {
  viewMode.value = 'history-detail'
  historyMessages.value = []
  try {
    const response = await fetch(`${AI_BASE_URL}/api/chat/records/session/${session.id}`)
    if (response.ok) {
      const data = await response.json()
      historyMessages.value = (data.records || []).map((r: any) => ({
        role: r.role,
        content: r.content,
      }))
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    ElMessage.error('加载对话详情失败')
  }
}

// 删除历史会话
async function deleteHistorySession(sid: string) {
  try {
    await ElMessageBox.confirm('确定删除这条对话记录？', '提示', {
      type: 'warning',
    })
    const response = await fetch(`${AI_BASE_URL}/api/chat/sessions/${sid}`, {
      method: 'DELETE',
    })
    if (response.ok) {
      ElMessage.success('删除成功')
      historySessions.value = historySessions.value.filter((s) => s.id !== sid)
    }
  } catch {}
}

// 格式化历史时间
function formatHistoryTime(time: string): string {
  if (!time) return ''
  return dayjs(time).fromNow()
}

// 格式化消息（支持换行和表格）
function formatMessage(text: string): string {
  return text
    .replace(/\n/g, '<br>')
    .replace(/\|(.+)\|/g, (match) => {
      if (match.includes('---')) return ''
      const cells = match.split('|').filter(Boolean).map(c => c.trim())
      return '<div class="table-row">' + cells.map(c => `<span>${c}</span>`).join('') + '</div>'
    })
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
}

// 发送消息（SSE流式输出）
async function sendMessage() {
  const text = inputText.value.trim()
  const hasImage = !!selectedImageBase64.value
  if ((!text && !hasImage) || loading.value) return

  const displayText = text + (hasImage ? ' [图片]' : '')
  messages.value.push({ role: 'user', content: displayText })
  inputText.value = ''
  loading.value = true

  const aiMsgIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })

  await nextTick()
  scrollToBottom()

  try {
    const body: Record<string, any> = {
      shop_id: props.shopId || 'default',
      message: text || '请分析这张图片',
      logged_in: props.loggedIn,
      token: props.token || '',
      image_base64: selectedImageBase64.value || '',
    }
    if (sessionId.value) {
      body.session_id = sessionId.value
    }
    selectedImageBase64.value = ''
    imagePreviewUrl.value = ''

    const response = await fetch(`${AI_BASE_URL}/api/chat/stream`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })

    const reader = response.body!.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (!line.startsWith('data: ')) continue

        const jsonStr = line.slice(6).trim()
        if (!jsonStr) continue

        try {
          const event = JSON.parse(jsonStr)

          if (event.type === 'token') {
            messages.value[aiMsgIndex].content += event.content
            scrollToBottom()
          } else if (event.type === 'done') {
            messages.value[aiMsgIndex].content = event.content
          } else if (event.type === 'error') {
            messages.value[aiMsgIndex].content = '抱歉，处理出错了，请稍后再试。'
          } else if (event.session_id) {
            sessionId.value = event.session_id
          }
        } catch {}
      }
    }
  } catch (error) {
    messages.value[aiMsgIndex].content = '网络连接失败，请检查AI服务是否启动。'
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

// 滚动到底部
function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}
</script>

<style scoped>
.ai-chat-wrapper {
  position: fixed;
  bottom: 28px;
  right: 28px;
  z-index: 9999;
}

/* 浮动按钮 */
.ai-chat-btn {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #c67b5c;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 3px 12px rgba(198,123,92,0.35);
  transition: all 0.25s ease;
}

.ai-chat-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(198,123,92,0.45);
}

.ai-chat-btn.active {
  background: #2d2016;
  box-shadow: 0 4px 16px rgba(45,32,22,0.3);
}

.ai-chat-btn.active:hover {
  box-shadow: 0 4px 16px rgba(45,32,22,0.4);
}

.chat-btn-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

/* 消息点标记 */
.chat-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 16px;
  height: 16px;
}

.pulse-dot {
  position: absolute;
  inset: 0;
  background: #ef4444;
  border-radius: 50%;
  border: 2px solid #fff;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

/* 聊天窗口 */
.ai-chat-window {
  position: absolute;
  bottom: 76px;
  right: 0;
  width: 400px;
  height: 560px;
  background: #fff;
  border-radius: 20px;
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

/* 动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.97);
}

/* 头部 */
.chat-header {
  background: #c67b5c;
  color: #fff;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  backdrop-filter: blur(4px);
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.header-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  opacity: 0.85;
  margin-top: 2px;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #4ade80;
  border-radius: 50%;
  animation: status-pulse 2s ease-in-out infinite;
}

@keyframes status-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.header-actions .action-btn {
  cursor: pointer;
  opacity: 0.8;
  transition: all 0.2s;
  color: #fff !important;
  font-size: 18px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.header-actions .action-btn:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.15);
}

.header-actions :deep(.el-tooltip__trigger) {
  display: flex;
  align-items: center;
}

.header-actions :deep(.el-icon) {
  color: #fff !important;
}

/* 历史会话列表 */
.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.history-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 0;
  color: #999;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.history-item:hover {
  background: #f5f7fa;
}

.history-item-icon {
  width: 40px;
  height: 40px;
  background: rgba(198,123,92,0.1);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c67b5c;
  flex-shrink: 0;
}

.history-item-content {
  flex: 1;
  min-width: 0;
}

.history-item-title {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-item-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.history-item-delete {
  color: #ccc;
  cursor: pointer;
  transition: color 0.2s;
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.history-item-delete:hover {
  color: #f56c6c;
  background: #fef2f2;
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 10px;
  max-width: 85%;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.assistant-avatar {
  background: #c67b5c;
  color: #fff;
}

.assistant-avatar svg {
  color: currentColor;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
}

.message-item.assistant .message-bubble {
  background: #f5f7fa;
  color: #333;
  border-top-left-radius: 4px;
}

.message-item.user .message-bubble {
  background: #c67b5c;
  color: #fff;
  border-top-right-radius: 4px;
}

/* 打字动画 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 7px;
  height: 7px;
  background: #9ca3af;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

/* 输入框 */
.chat-input {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
  flex-shrink: 0;
}

.chat-input :deep(.chat-text-input .el-input-group__append) {
  background: #c67b5c;
  border: none;
  color: #fff;
  border-radius: 0 10px 10px 0;
}

.chat-input :deep(.el-input__wrapper) {
  border-radius: 10px 0 0 10px;
}

.input-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-row .el-input {
  flex: 1;
}

.image-btn {
  color: #667eea !important;
  border-color: #e5e7eb !important;
  flex-shrink: 0;
}

.image-btn:hover {
  color: #c67b5c !important;
  border-color: #c67b5c !important;
}

.image-preview {
  position: relative;
  display: inline-block;
  margin-bottom: 10px;
}

.image-preview img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 10px;
  border: 2px solid #c67b5c;
}

.image-preview .image-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 20px;
  height: 20px;
  background: #ef4444;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.3);
}

/* 表格样式 */
.message-text :deep(.table-row) {
  display: flex;
  gap: 16px;
  padding: 4px 0;
  border-bottom: 1px solid #eee;
  font-size: 13px;
}

.message-text :deep(.table-row span) {
  flex: 1;
}

/* 响应式 */
@media (max-width: 480px) {
  .ai-chat-wrapper {
    bottom: 16px;
    right: 16px;
  }

  .ai-chat-btn {
    width: 52px;
    height: 52px;
  }

  .ai-chat-window {
    width: calc(100vw - 32px);
    height: calc(100vh - 120px);
    max-width: 400px;
    max-height: 560px;
    right: 0;
    bottom: 68px;
    border-radius: 16px;
  }
}

/* 减少动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .ai-chat-btn {
    animation: none;
  }
  .pulse-dot {
    animation: none;
  }
  .status-dot {
    animation: none;
  }
}
</style>
