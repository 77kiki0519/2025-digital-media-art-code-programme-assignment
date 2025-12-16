<template>
  <div class="exam-detail">
    <!-- 考试信息 -->
    <el-card v-if="!started">
      <template #header>
        <span>考试信息</span>
      </template>
      
      <el-descriptions :column="2" border v-if="exam">
        <el-descriptions-item label="考试名称">{{ exam.examName }}</el-descriptions-item>
        <el-descriptions-item label="考试时长">{{ exam.duration }} 分钟</el-descriptions-item>
        <el-descriptions-item label="总分">{{ exam.totalScore }} 分</el-descriptions-item>
        <el-descriptions-item label="题目数量">{{ questions.length }} 题</el-descriptions-item>
        <el-descriptions-item label="考试说明" :span="2">
          {{ exam.description || '无' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <div class="start-btn">
        <el-alert v-if="hasSubmitted" type="warning" :closable="false" style="margin-bottom: 20px;">
          您已经参加过该考试，不能重复参加
        </el-alert>
        <el-button type="primary" size="large" @click="startExam" :loading="loading" :disabled="hasSubmitted">
          {{ hasSubmitted ? '已参加' : '开始考试' }}
        </el-button>
      </div>
    </el-card>
    
    <!-- 答题界面 -->
    <div v-else class="exam-container">
      <!-- 倒计时 -->
      <div class="timer-bar">
        <div class="timer">
          <el-icon><Timer /></el-icon>
          剩余时间：{{ formatTime(remainingTime) }}
        </div>
        <el-button type="danger" @click="handleSubmitExam">提交试卷</el-button>
      </div>
      
      <!-- 题目列表 -->
      <el-card class="question-card" v-for="(q, index) in questions" :key="q.id">
        <div class="question-header">
          <span class="question-num">第 {{ index + 1 }} 题</span>
          <el-tag size="small">{{ getQuestionTypeText(q.questionType) }}</el-tag>
          <span class="score">{{ q.score }} 分</span>
        </div>
        
        <div class="question-content">{{ q.content }}</div>
        
        <!-- 单选题 -->
        <div v-if="q.questionType === 'SINGLE_CHOICE'" class="options">
          <el-radio-group v-model="answers[q.id]">
            <el-radio 
              v-for="(opt, key) in parseOptions(q.options)" 
              :key="key" 
              :value="key"
              class="option-item"
            >
              {{ key }}. {{ opt }}
            </el-radio>
          </el-radio-group>
        </div>
        
        <!-- 多选题 -->
        <div v-else-if="q.questionType === 'MULTIPLE_CHOICE'" class="options">
          <el-checkbox-group v-model="answers[q.id]">
            <el-checkbox 
              v-for="(opt, key) in parseOptions(q.options)" 
              :key="key" 
              :value="key"
              class="option-item"
            >
              {{ key }}. {{ opt }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
        
        <!-- 判断题 -->
        <div v-else-if="q.questionType === 'TRUE_FALSE'" class="options">
          <el-radio-group v-model="answers[q.id]">
            <el-radio value="true" class="option-item">正确</el-radio>
            <el-radio value="false" class="option-item">错误</el-radio>
          </el-radio-group>
        </div>
        
        <!-- 简答题 -->
        <div v-else class="options">
          <el-input 
            v-model="answers[q.id]" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入您的答案"
          />
        </div>
      </el-card>
      
      <div class="submit-area">
        <el-button type="primary" size="large" @click="handleSubmitExam">提交试卷</el-button>
      </div>
    </div>
    
    <!-- 成绩展示 -->
    <el-dialog v-model="showResult" title="考试结果" width="500px" :close-on-click-modal="false">
      <div class="result-content">
        <div class="score-display">
          <span class="score-num">{{ score }}</span>
          <span class="score-total">/ {{ exam?.totalScore }}</span>
        </div>
        <p class="result-msg">{{ getResultMessage() }}</p>
        
        <el-divider />
        
        <div class="answer-summary">
          <p>正确：<span class="correct">{{ correctCount }}</span> 题</p>
          <p>错误：<span class="wrong">{{ wrongCount }}</span> 题</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="goBack">返回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer } from '@element-plus/icons-vue'
import { getExamDetail, getExamQuestions, checkExamSubmission, submitExam as submitExamApi } from '@/api/exam'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const started = ref(false)
const hasSubmitted = ref(false)  // 是否已提交过
const exam = ref(null)
const questions = ref([])
const answers = ref({})
const remainingTime = ref(0)
const showResult = ref(false)
const score = ref(0)
const correctCount = ref(0)
const wrongCount = ref(0)

let timer = null

const getQuestionTypeText = (type) => {
  const map = {
    'SINGLE_CHOICE': '单选题',
    'MULTIPLE_CHOICE': '多选题',
    'TRUE_FALSE': '判断题',
    'SHORT_ANSWER': '简答题'
  }
  return map[type] || type
}

const parseOptions = (optionsStr) => {
  try {
    return JSON.parse(optionsStr)
  } catch {
    return {}
  }
}

const formatTime = (seconds) => {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

const fetchExam = async () => {
  loading.value = true
  try {
    const examId = route.params.id
    const [examRes, questionsRes] = await Promise.all([
      getExamDetail(examId),
      getExamQuestions(examId)
    ])
    exam.value = examRes.data
    questions.value = questionsRes.data || []
    remainingTime.value = (exam.value?.duration || 60) * 60
    
    // 检查是否已提交过
    try {
      const checkRes = await checkExamSubmission(examId, userStore.userInfo.userId)
      hasSubmitted.value = checkRes.data === true
    } catch (e) {
      console.error('检查提交状态失败:', e)
    }
  } catch (error) {
    console.error('获取考试信息失败:', error)
    ElMessage.error('获取考试信息失败')
  } finally {
    loading.value = false
  }
}

const startExam = () => {
  // 检查是否已提交
  if (hasSubmitted.value) {
    ElMessage.warning('您已经参加过该考试，不能重复参加')
    return
  }
  
  started.value = true
  // 初始化答案对象
  questions.value.forEach(q => {
    if (q.questionType === 'MULTIPLE_CHOICE') {
      answers.value[q.id] = []
    } else {
      answers.value[q.id] = ''
    }
  })
  // 开始倒计时
  timer = setInterval(() => {
    remainingTime.value--
    if (remainingTime.value <= 0) {
      handleSubmitExam()
    }
  }, 1000)
}

const handleSubmitExam = async () => {
  try {
    await ElMessageBox.confirm('确定要提交试卷吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  
  clearInterval(timer)
  
  // 计算分数
  let totalScore = 0
  let correct = 0
  let wrong = 0
  
  questions.value.forEach(q => {
    const userAnswer = answers.value[q.id]
    const correctAnswer = q.correctAnswer
    
    let isCorrect = false
    if (q.questionType === 'MULTIPLE_CHOICE') {
      // 多选题：数组比较
      isCorrect = JSON.stringify([...userAnswer].sort()) === JSON.stringify([...correctAnswer].sort())
    } else if (q.questionType === 'SHORT_ANSWER') {
      // 简答题：暂时给80%分数
      isCorrect = userAnswer && userAnswer.trim().length > 10
      if (isCorrect) totalScore += q.score * 0.8
    } else {
      isCorrect = userAnswer === correctAnswer
    }
    
    if (isCorrect && q.questionType !== 'SHORT_ANSWER') {
      totalScore += q.score
      correct++
    } else if (!isCorrect) {
      wrong++
    }
  })
  
  score.value = Math.round(totalScore)
  correctCount.value = correct
  wrongCount.value = wrong
  showResult.value = true
}

const getResultMessage = () => {
  const percent = score.value / (exam.value?.totalScore || 100)
  if (percent >= 0.9) return '优秀！继续保持！'
  if (percent >= 0.8) return '良好！再接再厉！'
  if (percent >= 0.6) return '及格，还需努力！'
  return '不及格，请认真复习后重考。'
}

const goBack = () => {
  router.push('/exams')
}

onMounted(() => {
  fetchExam()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.exam-detail {
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.start-btn {
  text-align: center;
  margin-top: 30px;
}

.timer-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: white;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.timer {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.question-card {
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.question-num {
  font-weight: 600;
  color: #303133;
}

.score {
  margin-left: auto;
  color: #909399;
  font-size: 14px;
}

.question-content {
  font-size: 16px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 20px;
}

.options {
  padding-left: 20px;
}

.option-item {
  display: block !important;
  margin-bottom: 12px !important;
  line-height: 1.6;
}

.submit-area {
  text-align: center;
  padding: 20px 0;
}

.result-content {
  text-align: center;
}

.score-display {
  margin-bottom: 20px;
}

.score-num {
  font-size: 64px;
  font-weight: 700;
  color: #409eff;
}

.score-total {
  font-size: 24px;
  color: #909399;
}

.result-msg {
  font-size: 18px;
  color: #606266;
}

.answer-summary {
  display: flex;
  justify-content: center;
  gap: 40px;
}

.answer-summary p {
  font-size: 16px;
  color: #606266;
}

.correct {
  color: #67c23a;
  font-weight: 600;
}

.wrong {
  color: #f56c6c;
  font-weight: 600;
}
</style>
