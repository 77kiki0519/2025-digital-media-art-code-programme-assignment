<template>
  <div class="exams">
    <el-card>
      <template #header>
        <div class="header">
          <span>{{ isTeacher ? '考试管理' : '我的考试' }}</span>
          <el-button v-if="isTeacher" type="primary" @click="showCreateDialog">创建考试</el-button>
        </div>
      </template>
      
      <!-- 选择课程 -->
      <div class="course-select">
        <span>选择课程：</span>
        <el-select v-model="selectedCourseId" placeholder="请选择课程" @change="fetchExams">
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="course.courseName"
            :value="course.id"
          />
        </el-select>
      </div>
      
      <!-- 考试列表 -->
      <el-table :data="displayExams" style="width: 100%; margin-top: 20px;" v-loading="loading">
        <el-table-column prop="examName" label="考试名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 教师操作 -->
        <el-table-column v-if="isTeacher" label="操作" width="300">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="showQuestionsDialog(row)">
              试题
            </el-button>
            <el-button text type="info" size="small" @click="showSubmissionsDialog(row)">
              提交记录
            </el-button>
            <el-button text type="success" size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '发布' }}
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
        <!-- 学生操作 -->
        <el-table-column v-else label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="enterExam(row)">
              参加考试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && displayExams.length === 0" 
        :description="isTeacher ? '暂无考试，请先选择课程或创建考试' : '暂无可参加的考试'" />
    </el-card>
    
    <!-- 创建考试对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建考试" width="600px">
      <el-form :model="examForm" label-width="100px">
        <el-form-item label="选择课程" required>
          <el-select v-model="examForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考试名称" required>
          <el-input v-model="examForm.examName" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="考试描述">
          <el-input v-model="examForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="总分">
          <el-input-number v-model="examForm.totalScore" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="examForm.duration" :min="1" :max="600" />
        </el-form-item>
        <el-form-item label="立即发布">
          <el-switch v-model="examForm.publish" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateExam" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>
    
    <!-- 试题管理对话框 -->
    <el-dialog v-model="questionsDialogVisible" :title="currentExam?.examName + ' - 试题管理'" width="800px">
      <div class="questions-header">
        <el-button type="primary" size="small" @click="showAddQuestionDialog">添加试题</el-button>
      </div>
      
      <el-table :data="questions" style="width: 100%">
        <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题型" width="100">
          <template #default="{ row }">
            {{ getQuestionTypeText(row.questionType) }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column prop="correctAnswer" label="正确答案" width="100" />
      </el-table>
      
      <el-empty v-if="questions.length === 0" description="暂无试题" />
    </el-dialog>
    
    <!-- 添加试题对话框 -->
    <el-dialog v-model="addQuestionDialogVisible" title="添加试题" width="600px">
      <el-form :model="questionForm" label-width="100px">
        <el-form-item label="题型" required>
          <el-select v-model="questionForm.questionType" style="width: 100%">
            <el-option label="单选题" value="SINGLE_CHOICE" />
            <el-option label="多选题" value="MULTIPLE_CHOICE" />
            <el-option label="判断题" value="TRUE_FALSE" />
            <el-option label="简答题" value="SHORT_ANSWER" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" required>
          <el-input v-model="questionForm.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="选项" v-if="questionForm.questionType !== 'SHORT_ANSWER' && questionForm.questionType !== 'TRUE_FALSE'">
          <el-input v-model="questionForm.options" type="textarea" :rows="4" 
            placeholder='JSON格式，如：{"A": "选项A", "B": "选项B", "C": "选项C", "D": "选项D"}' />
        </el-form-item>
        <el-form-item label="正确答案" required>
          <el-input v-model="questionForm.correctAnswer" :placeholder="questionForm.questionType === 'TRUE_FALSE' ? '输入 true 或 false' : '输入正确答案，如 A 或 AB'" />
        </el-form-item>
        <el-form-item label="分值" required>
          <el-input-number v-model="questionForm.score" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="questionForm.analysis" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addQuestionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddQuestion" :loading="submitting">添加</el-button>
      </template>
    </el-dialog>
    
    <!-- 提交记录对话框 -->
    <el-dialog v-model="submissionsDialogVisible" title="考试提交记录" width="900px">
      <el-table :data="submissions" v-loading="loading">
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ row.startTime ? new Date(row.startTime).toLocaleString('zh-CN') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="{ row }">
            {{ row.submitTime ? new Date(row.submitTime).toLocaleString('zh-CN') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="得分" width="100">
          <template #default="{ row }">
            {{ row.totalScore !== null && row.totalScore !== undefined ? row.totalScore : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'primary' : 'info'">
              {{ row.status === 0 ? '进行中' : row.status === 1 ? '已提交' : '已批改' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && submissions.length === 0" description="暂无提交记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourses } from '@/api/course'
import { getCourseExams, createExam, updateExam, deleteExam, addQuestion, getExamQuestions, getExamSubmissions, checkExamSubmission } from '@/api/exam'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const courses = ref([])
const exams = ref([])
const questions = ref([])
const selectedCourseId = ref(null)
const currentExam = ref(null)

const createDialogVisible = ref(false)
const questionsDialogVisible = ref(false)
const addQuestionDialogVisible = ref(false)
const submissionsDialogVisible = ref(false)
const submissions = ref([])

// 判断是否是教师
const isTeacher = computed(() => {
  return userStore.userInfo.roles?.some(r => r.roleCode === 'TEACHER' || r.roleCode === 'ADMIN')
})

// 学生只能看到已发布的考试
const displayExams = computed(() => {
  if (isTeacher.value) return exams.value
  return exams.value.filter(e => e.status === 1)
})

const examForm = ref({
  courseId: null,
  examName: '',
  description: '',
  totalScore: 100,
  duration: 60,
  publish: true
})

const questionForm = ref({
  questionType: 'SINGLE_CHOICE',
  content: '',
  options: '',
  correctAnswer: '',
  score: 10,
  analysis: ''
})

const getQuestionTypeText = (type) => {
  const map = {
    'SINGLE_CHOICE': '单选题',
    'MULTIPLE_CHOICE': '多选题',
    'TRUE_FALSE': '判断题',
    'SHORT_ANSWER': '简答题'
  }
  return map[type] || type
}

const fetchCourses = async () => {
  try {
    const params = isTeacher.value ? { teacherId: userStore.userInfo.userId } : {}
    const res = await getCourses(params)
    courses.value = res.data.records || res.data || []
  } catch (error) {
    console.error('获取课程失败:', error)
  }
}

const fetchExams = async () => {
  if (!selectedCourseId.value) {
    exams.value = []
    return
  }
  loading.value = true
  try {
    const res = await getCourseExams(selectedCourseId.value)
    exams.value = res.data || []
  } catch (error) {
    console.error('获取考试列表失败:', error)
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  examForm.value = {
    courseId: selectedCourseId.value,
    examName: '',
    description: '',
    totalScore: 100,
    duration: 60,
    publish: true
  }
  createDialogVisible.value = true
}

const handleCreateExam = async () => {
  if (!examForm.value.courseId) {
    ElMessage.warning('请选择课程')
    return
  }
  if (!examForm.value.examName) {
    ElMessage.warning('请输入考试名称')
    return
  }
  
  submitting.value = true
  try {
    const { publish, ...examData } = examForm.value
    await createExam({
      ...examData,
      status: publish ? 1 : 0
    })
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    selectedCourseId.value = examForm.value.courseId
    fetchExams()
  } catch (error) {
    console.error('创建考试失败:', error)
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (exam) => {
  try {
    await updateExam(exam.id, {
      ...exam,
      status: exam.status === 1 ? 0 : 1
    })
    ElMessage.success(exam.status === 1 ? '已下架' : '已发布')
    fetchExams()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

const handleDelete = async (exam) => {
  try {
    await ElMessageBox.confirm('确定要删除该考试吗？', '提示', { type: 'warning' })
    await deleteExam(exam.id)
    ElMessage.success('删除成功')
    fetchExams()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const showQuestionsDialog = async (exam) => {
  currentExam.value = exam
  questionsDialogVisible.value = true
  try {
    const res = await getExamQuestions(exam.id)
    questions.value = res.data || []
  } catch (error) {
    console.error('获取试题失败:', error)
  }
}

const showAddQuestionDialog = () => {
  questionForm.value = {
    questionType: 'SINGLE_CHOICE',
    content: '',
    options: '{"A": "", "B": "", "C": "", "D": ""}',
    correctAnswer: '',
    score: 10,
    analysis: ''
  }
  addQuestionDialogVisible.value = true
}

const handleAddQuestion = async () => {
  if (!questionForm.value.content) {
    ElMessage.warning('请输入题目内容')
    return
  }
  if (!questionForm.value.correctAnswer) {
    ElMessage.warning('请输入正确答案')
    return
  }
  
  submitting.value = true
  try {
    await addQuestion({
      examId: currentExam.value.id,
      ...questionForm.value
    })
    ElMessage.success('添加成功')
    addQuestionDialogVisible.value = false
    const res = await getExamQuestions(currentExam.value.id)
    questions.value = res.data || []
  } catch (error) {
    console.error('添加试题失败:', error)
  } finally {
    submitting.value = false
  }
}

// 查看提交记录
const showSubmissionsDialog = async (exam) => {
  currentExam.value = exam
  loading.value = true
  try {
    const res = await getExamSubmissions(exam.id)
    submissions.value = res.data || []
    submissionsDialogVisible.value = true
  } catch (error) {
    console.error('获取提交记录失败:', error)
    ElMessage.error('获取提交记录失败')
  } finally {
    loading.value = false
  }
}

// 学生进入考试
const enterExam = async (exam) => {
  // 先检查是否已参加过
  try {
    const res = await checkExamSubmission(exam.id, userStore.userInfo.userId)
    if (res.data === true) {
      ElMessage.warning('您已经参加过该考试，不能重复参加')
      return
    }
    router.push(`/exams/${exam.id}`)
  } catch (error) {
    console.error('检查考试状态失败:', error)
    router.push(`/exams/${exam.id}`)
  }
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.exams {
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

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-select {
  display: flex;
  align-items: center;
  gap: 12px;
}

.course-select span {
  color: #606266;
  font-size: 14px;
}

.questions-header {
  margin-bottom: 16px;
}
</style>
