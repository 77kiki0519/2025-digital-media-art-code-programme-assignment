<template>
  <div class="qa">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>智能答疑</span>
          <el-button v-if="messages.length > 0" text type="danger" @click="clearHistory">
            清空记录
          </el-button>
        </div>
      </template>
      
      <div class="chat-container">
        <div class="messages" ref="messagesRef">
          <!-- 加载状态 -->
          <div v-if="loadingHistory" class="loading-history">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载历史记录...</span>
          </div>
          
          <!-- 空状态 -->
          <div v-else-if="messages.length === 0" class="empty-state">
            <el-icon :size="48"><ChatDotRound /></el-icon>
            <p>开始提问，AI助教为您解答</p>
          </div>
          
          <!-- 消息列表 -->
          <div v-else v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
            <div class="message-content">
              <div class="message-header">
                <el-avatar :size="32" v-if="msg.role === 'user'">
                  {{ userStore.userInfo.realName?.charAt(0) || '我' }}
                </el-avatar>
                <el-avatar :size="32" v-else style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                  AI
                </el-avatar>
                <span class="message-name">
                  {{ msg.role === 'user' ? (userStore.userInfo.realName || '我') : 'AI助教' }}
                </span>
                <span class="message-time" v-if="msg.time">
                  {{ formatTime(msg.time) }}
                </span>
              </div>
              <div class="message-text">{{ msg.content }}</div>
            </div>
          </div>
        </div>
        
        <div class="input-area">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题..."
            @keyup.enter.ctrl="handleSend"
            :disabled="loading"
          />
          <el-button type="primary" @click="handleSend" :loading="loading">
            发送 (Ctrl+Enter)
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, ChatDotRound } from '@element-plus/icons-vue'
import { createQuestion, getChatHistory, saveAnswer } from '@/api/question'
import { getCourses } from '@/api/course'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const messagesRef = ref(null)
const inputMessage = ref('')
const loading = ref(false)
const loadingHistory = ref(false)
const messages = ref([])
const availableCourseId = ref(null)

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return date.toLocaleDateString('zh-CN', { 
    month: 'numeric', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载历史聊天记录
const loadHistory = async () => {
  if (!userStore.userInfo.userId) return
  
  loadingHistory.value = true
  try {
    const res = await getChatHistory(
      userStore.userInfo.userId, 
      availableCourseId.value,
      50
    )
    if (res.data && res.data.length > 0) {
      messages.value = res.data.map(item => ({
        role: item.role,
        content: item.content,
        time: item.time
      }))
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
  } finally {
    loadingHistory.value = false
  }
}

// 获取可用的课程ID并加载历史
onMounted(async () => {
  try {
    const res = await getCourses({ page: 1, size: 1 })
    if (res.data && res.data.records && res.data.records.length > 0) {
      availableCourseId.value = res.data.records[0].id
    }
    // 加载历史记录
    await loadHistory()
  } catch (error) {
    console.error('初始化失败:', error)
  }
})

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 清空历史记录（仅前端）
const clearHistory = () => {
  ElMessageBox.confirm('确定要清空聊天记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    messages.value = []
    ElMessage.success('已清空')
  }).catch(() => {})
}

const handleSend = async () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }
  
  if (!availableCourseId.value) {
    ElMessage.warning('系统初始化中，请稍后再试')
    return
  }
  
  const userMessage = inputMessage.value
  messages.value.push({
    role: 'user',
    content: userMessage,
    time: new Date().toISOString()
  })
  
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()
  
  let questionId = null
  let fullAnswer = ''
  
  try {
    // 创建问题
    const questionRes = await createQuestion({
      courseId: availableCourseId.value,
      studentId: userStore.userInfo.userId,
      content: userMessage,
      questionType: 'TEXT',
      status: 0
    })
    questionId = questionRes.data.id
    
    // 添加AI回复占位
    const aiMessageIndex = messages.value.length
    messages.value.push({
      role: 'assistant',
      content: '',
      time: new Date().toISOString()
    })
    
    // 使用流式API获取回答
    const token = localStorage.getItem('token')
    const response = await fetch(`/api/questions/${questionId}/answer-stream?courseContext=通用课程`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Accept': 'text/event-stream'
      }
    })
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')
      
      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const content = line.substring(6).replace(/\\n/g, '\n')
          fullAnswer += content
          messages.value[aiMessageIndex].content = fullAnswer
          scrollToBottom()
        }
      }
    }
    
    // 流式完成后保存回答到数据库
    if (questionId && fullAnswer) {
      await saveAnswer(questionId, fullAnswer)
    }
    
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-container {
  height: 600px;
  display: flex;
  flex-direction: column;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: linear-gradient(to bottom, #fafbfc, #f5f7fa);
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
}

.loading-history {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: #909399;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.empty-state p {
  margin-top: 16px;
  font-size: 14px;
}

.message {
  margin-bottom: 24px;
  display: flex;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.message-name {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.message-text {
  background: white;
  padding: 14px 18px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  border: 1px solid #ebeef5;
}

.message.user .message-text {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  border: none;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.input-area {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-area .el-input {
  flex: 1;
}

.input-area .el-button {
  height: 40px;
}
</style>

