<template>
  <div class="reports">
    <el-card>
      <template #header>
        <div class="header">
          <span>{{ isTeacher ? '报告作业管理' : '我的报告作业' }}</span>
          <el-button v-if="isTeacher" type="primary" @click="showCreateDialog">创建报告作业</el-button>
        </div>
      </template>
      
      <!-- 选择课程 -->
      <div class="course-select">
        <span>选择课程：</span>
        <el-select v-model="selectedCourseId" placeholder="请选择课程" @change="fetchReports">
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="course.courseName"
            :value="course.id"
          />
        </el-select>
      </div>
      
      <!-- 报告列表 -->
      <el-table :data="displayReports" style="width: 100%; margin-top: 20px;" v-loading="loading">
        <el-table-column prop="reportName" label="报告名称" />
        <el-table-column prop="description" label="要求描述" show-overflow-tooltip />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column label="截止时间" width="160">
          <template #default="{ row }">
            {{ row.deadline ? formatDate(row.deadline) : '无限期' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 教师操作 -->
        <el-table-column v-if="isTeacher" label="操作" width="280">
          <template #default="{ row }">
            <el-button text type="info" size="small" @click="showSubmissionsDialog(row)">
              提交记录
            </el-button>
            <el-button text type="success" size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '关闭' : '发布' }}
            </el-button>
            <el-button text type="primary" size="small" @click="showAIGradeDialog(row)">
              AI批改
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
        <!-- 学生操作 -->
        <el-table-column v-else label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showSubmitDialog(row)">
              提交报告
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && displayReports.length === 0" 
        :description="isTeacher ? '暂无报告作业，请先选择课程或创建报告作业' : '暂无需要提交的报告'" />
    </el-card>
    
    <!-- 创建报告对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建报告作业" width="600px">
      <el-form :model="reportForm" label-width="100px">
        <el-form-item label="选择课程" required>
          <el-select v-model="reportForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报告名称" required>
          <el-input v-model="reportForm.reportName" placeholder="请输入报告名称" />
        </el-form-item>
        <el-form-item label="要求描述">
          <el-input v-model="reportForm.description" type="textarea" :rows="3" placeholder="报告要求说明" />
        </el-form-item>
        <el-form-item label="详细要求">
          <el-input v-model="reportForm.requirements" type="textarea" :rows="3" placeholder="详细评分标准" />
        </el-form-item>
        <el-form-item label="字数限制">
          <el-input-number v-model="reportForm.wordLimit" :min="100" :max="50000" :step="100" />
        </el-form-item>
        <el-form-item label="总分">
          <el-input-number v-model="reportForm.totalScore" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="reportForm.deadline"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="立即发布">
          <el-switch v-model="reportForm.publish" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateReport" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>
    
    <!-- 学生提交报告对话框 -->
    <el-dialog v-model="submitDialogVisible" title="提交报告" width="700px">
      <div class="report-info" v-if="currentReport">
        <h3>{{ currentReport.reportName }}</h3>
        <p class="requirements">{{ currentReport.requirements || currentReport.description }}</p>
        <p class="meta">
          字数要求：{{ currentReport.wordLimit || '不限' }} 字
          <span v-if="currentReport.deadline"> | 截止时间：{{ formatDate(currentReport.deadline) }}</span>
        </p>
      </div>
      
      <el-form :model="submitForm" label-width="80px">
        <el-form-item label="报告内容" required>
          <el-input 
            v-model="submitForm.content" 
            type="textarea" 
            :rows="12" 
            placeholder="请输入您的报告内容..."
            show-word-limit
            :maxlength="50000"
          />
        </el-form-item>
        <el-form-item label="当前字数">
          <span :class="{'word-count': true, 'warning': submitForm.content.length < (currentReport?.wordLimit || 0) * 0.8}">
            {{ submitForm.content.length }} 字
          </span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReport" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
    
    <!-- AI批改对话框 -->
    <el-dialog v-model="aiGradeDialogVisible" title="AI批改报告" width="700px">
      <el-form :model="gradeForm" label-width="100px">
        <el-form-item label="报告要求">
          <el-input :value="currentReport?.requirements || currentReport?.description" disabled type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="学生报告" required>
          <el-input v-model="gradeForm.content" type="textarea" :rows="8" placeholder="粘贴学生提交的报告内容" />
        </el-form-item>
      </el-form>
      
      <div v-if="gradeResult" class="grade-result">
        <el-divider>AI评分结果</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="总分">{{ gradeResult.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="主题相关性">{{ gradeResult.relevanceScore }}</el-descriptions-item>
          <el-descriptions-item label="逻辑结构">{{ gradeResult.structureScore }}</el-descriptions-item>
          <el-descriptions-item label="知识点覆盖">{{ gradeResult.knowledgeScore }}</el-descriptions-item>
          <el-descriptions-item label="语言规范">{{ gradeResult.languageScore }}</el-descriptions-item>
        </el-descriptions>
        <div class="feedback">
          <h4>评语：</h4>
          <p>{{ gradeResult.feedback }}</p>
        </div>
        <div v-if="gradeResult.suggestions && gradeResult.suggestions.length" class="suggestions">
          <h4>改进建议：</h4>
          <ul>
            <li v-for="(s, i) in gradeResult.suggestions" :key="i">{{ s }}</li>
          </ul>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="aiGradeDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleAIGrade" :loading="grading">
          {{ gradeResult ? '重新批改' : '开始批改' }}
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 提交记录对话框 -->
    <el-dialog v-model="submissionsDialogVisible" title="报告提交记录" width="1000px">
      <el-table :data="submissions" v-loading="loading">
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="title" label="报告标题" width="200" show-overflow-tooltip />
        <el-table-column prop="wordCount" label="字数" width="100" />
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="{ row }">
            {{ row.submitTime ? new Date(row.submitTime).toLocaleString('zh-CN') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'primary' : 'info'">
              {{ row.status === 0 ? '未提交' : row.status === 1 ? '已提交' : '已批改' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="viewSubmissionContent(row)">
              查看内容
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && submissions.length === 0" description="暂无提交记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourses } from '@/api/course'
import { getCourseReports, createReport, updateReport, deleteReport, reviewReportWithAI, getReportSubmissions, submitReport } from '@/api/report'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const grading = ref(false)
const courses = ref([])
const reports = ref([])
const selectedCourseId = ref(null)
const currentReport = ref(null)
const gradeResult = ref(null)

const createDialogVisible = ref(false)
const submitDialogVisible = ref(false)
const aiGradeDialogVisible = ref(false)
const submissionsDialogVisible = ref(false)
const submissions = ref([])

// 判断是否是教师
const isTeacher = computed(() => {
  return userStore.userInfo.roles?.some(r => r.roleCode === 'TEACHER' || r.roleCode === 'ADMIN')
})

// 学生只能看到已发布的报告
const displayReports = computed(() => {
  if (isTeacher.value) return reports.value
  return reports.value.filter(r => r.status === 1)
})

const reportForm = ref({
  courseId: null,
  reportName: '',
  description: '',
  requirements: '',
  wordLimit: 1000,
  totalScore: 100,
  deadline: null,
  publish: true
})

const submitForm = ref({
  content: ''
})

const gradeForm = ref({
  content: ''
})

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
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

const fetchReports = async () => {
  if (!selectedCourseId.value) {
    reports.value = []
    return
  }
  loading.value = true
  try {
    const res = await getCourseReports(selectedCourseId.value)
    reports.value = res.data || []
  } catch (error) {
    console.error('获取报告列表失败:', error)
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  reportForm.value = {
    courseId: selectedCourseId.value,
    reportName: '',
    description: '',
    requirements: '',
    wordLimit: 1000,
    totalScore: 100,
    deadline: null,
    publish: true
  }
  createDialogVisible.value = true
}

const handleCreateReport = async () => {
  if (!reportForm.value.courseId) {
    ElMessage.warning('请选择课程')
    return
  }
  if (!reportForm.value.reportName) {
    ElMessage.warning('请输入报告名称')
    return
  }
  
  submitting.value = true
  try {
    const { publish, ...reportData } = reportForm.value
    await createReport({
      ...reportData,
      status: publish ? 1 : 0
    })
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    selectedCourseId.value = reportForm.value.courseId
    fetchReports()
  } catch (error) {
    console.error('创建报告失败:', error)
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (report) => {
  try {
    await updateReport(report.id, {
      ...report,
      status: report.status === 1 ? 0 : 1
    })
    ElMessage.success(report.status === 1 ? '已关闭' : '已发布')
    fetchReports()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

const handleDelete = async (report) => {
  try {
    await ElMessageBox.confirm('确定要删除该报告作业吗？', '提示', { type: 'warning' })
    await deleteReport(report.id)
    ElMessage.success('删除成功')
    fetchReports()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 学生提交报告
const showSubmitDialog = async (report) => {
  currentReport.value = report
  submitForm.value = { content: '' }
  
  // 检查是否已有提交记录，不允许重复提交
  try {
    const res = await getReportSubmissions(report.id)
    const mySubmission = res.data?.find(s => s.studentId === userStore.userInfo.userId)
    if (mySubmission) {
      ElMessage.warning('您已经提交过该报告，不能重复提交')
      return
    }
  } catch (error) {
    console.error('获取提交记录失败:', error)
  }
  
  submitDialogVisible.value = true
}

const handleSubmitReport = async () => {
  if (!submitForm.value.content.trim()) {
    ElMessage.warning('请输入报告内容')
    return
  }
  
  // 检查字数要求
  const wordCount = submitForm.value.content.length
  const wordLimit = currentReport.value?.wordLimit
  if (wordLimit && wordCount < wordLimit * 0.8) {
    const confirm = await ElMessageBox.confirm(
      `当前字数 ${wordCount} 字，未达到建议字数 ${wordLimit} 字的80%，是否继续提交？`,
      '字数提示',
      { type: 'warning' }
    ).catch(() => false)
    if (!confirm) return
  }
  
  submitting.value = true
  try {
    await submitReport(currentReport.value.id, {
      studentId: userStore.userInfo.userId,
      title: `${currentReport.value.reportName} - ${userStore.userInfo.realName || userStore.userInfo.username}`,
      content: submitForm.value.content,
      wordCount: wordCount,
      status: 1
    })
    ElMessage.success('报告提交成功！')
    submitDialogVisible.value = false
    // 刷新报告列表，更新提交状态
    fetchReports()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

const showSubmissionsDialog = async (report) => {
  currentReport.value = report
  loading.value = true
  try {
    const res = await getReportSubmissions(report.id)
    submissions.value = res.data || []
    submissionsDialogVisible.value = true
  } catch (error) {
    console.error('获取提交记录失败:', error)
    ElMessage.error('获取提交记录失败')
  } finally {
    loading.value = false
  }
}

const showAIGradeDialog = (report) => {
  currentReport.value = report
  gradeForm.value = { content: '' }
  gradeResult.value = null
  aiGradeDialogVisible.value = true
}

const viewSubmissionContent = (submission) => {
  ElMessageBox.alert(submission.content || '无内容', `${submission.studentName}的报告`, {
    confirmButtonText: '关闭',
    dangerouslyUseHTMLString: false,
    type: 'info'
  })
}

const handleAIGrade = async () => {
  if (!gradeForm.value.content.trim()) {
    ElMessage.warning('请输入学生报告内容')
    return
  }
  
  grading.value = true
  try {
    const res = await reviewReportWithAI({
      requirement: currentReport.value.requirements || currentReport.value.description,
      content: gradeForm.value.content
    })
    gradeResult.value = res.data
    ElMessage.success('批改完成')
  } catch (error) {
    console.error('AI批改失败:', error)
    ElMessage.error('批改失败，请稍后重试')
  } finally {
    grading.value = false
  }
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.reports {
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

.report-info {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.report-info h3 {
  margin: 0 0 12px 0;
  color: #303133;
}

.report-info .requirements {
  color: #606266;
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.report-info .meta {
  color: #909399;
  font-size: 13px;
  margin: 0;
}

.word-count {
  font-size: 14px;
  color: #67c23a;
}

.word-count.warning {
  color: #e6a23c;
}

.grade-result {
  margin-top: 20px;
}

.feedback, .suggestions {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.feedback h4, .suggestions h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 14px;
}

.feedback p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.suggestions ul {
  margin: 0;
  padding-left: 20px;
  color: #606266;
}

.suggestions li {
  margin-bottom: 4px;
}
</style>
