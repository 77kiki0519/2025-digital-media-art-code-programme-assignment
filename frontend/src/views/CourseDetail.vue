<template>
  <div class="course-detail">
    <el-card>
      <div class="course-header">
        <div class="course-info">
          <h1>{{ course.courseName }}</h1>
          <p>{{ course.description }}</p>
          <div class="course-meta">
            <el-tag>{{ course.category }}</el-tag>
            <el-tag :type="getDifficultyType(course.difficulty)">
              {{ getDifficultyText(course.difficulty) }}
            </el-tag>
          </div>
        </div>
        <!-- 教师管理按钮 -->
        <div v-if="isTeacher" class="teacher-actions">
          <el-button type="primary" @click="showChapterDialog = true">
            <el-icon><Plus /></el-icon> 添加章节
          </el-button>
        </div>
      </div>
    </el-card>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 视频播放器 -->
      <el-col :span="16">
        <el-card v-if="currentVideo">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>{{ currentVideo.title }}</span>
              <!-- 教师视频管理 -->
              <div v-if="isTeacher">
                <el-button size="small" @click="manageKnowledgePoints(currentVideo)">
                  <el-icon><Memo /></el-icon> 知识点
                </el-button>
                <el-button size="small" @click="manageSubtitles(currentVideo)">
                  <el-icon><Guide /></el-icon> 字幕
                </el-button>
                <el-button size="small" @click="manageQuiz(currentVideo)">
                  <el-icon><EditPen /></el-icon> 测验
                </el-button>
              </div>
            </div>
          </template>
          <VideoPlayer 
            v-if="currentVideo.id"
            :video-id="currentVideo.id"
            :video-url="currentVideo.videoUrl"
            :subtitle-url="currentVideo.subtitleUrl"
          />
        </el-card>
        <el-card v-else>
          <el-empty description="请从右侧选择要观看的视频" />
        </el-card>
      </el-col>
      
      <!-- 章节和视频列表 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>课程目录</span>
          </template>
          <el-collapse v-model="activeChapters">
            <el-collapse-item 
              v-for="chapter in chapters" 
              :key="chapter.id"
              :name="chapter.id"
            >
              <template #title>
                <div class="chapter-header">
                  <span class="chapter-title">{{ chapter.chapterName }}</span>
                  <!-- 教师章节管理 -->
                  <div v-if="isTeacher" class="chapter-actions" @click.stop>
                    <el-button size="small" text @click="addVideo(chapter)">
                      <el-icon><Plus /></el-icon>
                    </el-button>
                    <el-button size="small" text @click="editChapter(chapter)">
                      <el-icon><Edit /></el-icon>
                    </el-button>
                    <el-button size="small" text type="danger" @click="deleteChapter(chapter.id)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </div>
              </template>
              <div 
                v-for="video in chapter.videos" 
                :key="video.id"
                class="video-item"
                :class="{ active: currentVideo?.id === video.id }"
              >
                <div @click="playVideo(video)" style="flex: 1; display: flex; align-items: center; gap: 8px;">
                  <el-icon><VideoPlay /></el-icon>
                  <span>{{ video.title }}</span>
                  <el-tag v-if="video.aiGenerated" size="small" type="success">AI</el-tag>
                </div>
                <!-- 教师视频管理 -->
                <div v-if="isTeacher" class="video-actions">
                  <el-button size="small" text @click="editVideo(video, chapter)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button size="small" text type="danger" @click="deleteVideo(video.id, chapter)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
          <el-empty v-if="chapters.length === 0" description="暂无课程内容" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加/编辑章节对话框 -->
    <el-dialog 
      v-model="showChapterDialog" 
      :title="editingChapter ? '编辑章节' : '添加章节'"
      width="500px"
    >
      <el-form :model="chapterForm" label-width="100px">
        <el-form-item label="章节名称">
          <el-input v-model="chapterForm.chapterName" placeholder="例如：第一章 人工智能基础" />
        </el-form-item>
        <el-form-item label="章节顺序">
          <el-input-number v-model="chapterForm.chapterOrder" :min="1" />
        </el-form-item>
        <el-form-item label="章节描述">
          <el-input v-model="chapterForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChapterDialog = false">取消</el-button>
        <el-button type="primary" @click="saveChapter">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑视频对话框 -->
    <el-dialog 
      v-model="showVideoDialog" 
      :title="editingVideo ? '编辑视频' : '添加视频'"
      width="600px"
    >
      <el-form :model="videoForm" label-width="100px">
        <el-form-item label="视频标题">
          <el-input v-model="videoForm.title" placeholder="例如：人工智能概述" />
        </el-form-item>
        <el-form-item label="视频URL">
          <el-input v-model="videoForm.videoUrl" placeholder="视频文件URL或在线地址" />
          <el-text type="info" size="small">提示：可以输入在线视频地址</el-text>
        </el-form-item>
        <el-form-item label="时长(秒)">
          <el-input-number v-model="videoForm.duration" :min="0" />
        </el-form-item>
        <el-form-item label="顺序">
          <el-input-number v-model="videoForm.orderNum" :min="1" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="videoForm.coverImage" placeholder="封面图URL(可选)" />
        </el-form-item>
        <el-form-item label="允许倍速">
          <el-switch v-model="videoForm.allowSpeed" />
        </el-form-item>
        <el-form-item label="允许弹幕">
          <el-switch v-model="videoForm.allowDanmaku" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showVideoDialog = false">取消</el-button>
        <el-button type="primary" @click="saveVideo">保存</el-button>
      </template>
    </el-dialog>

    <!-- 知识点管理对话框 -->
    <el-dialog v-model="showKnowledgeDialog" title="知识点管理" width="800px">
      <el-button type="primary" size="small" @click="addKnowledgePoint" style="margin-bottom: 15px;">
        <el-icon><Plus /></el-icon> 添加知识点
      </el-button>
      <el-table :data="knowledgePoints" style="width: 100%">
        <el-table-column prop="timePoint" label="时间点" width="100">
          <template #default="{ row }">{{ formatTime(row.timePoint) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="150" />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="pointType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.pointType === 'IMPORTANT'" type="warning">重点</el-tag>
            <el-tag v-else-if="row.pointType === 'WARNING'" type="danger">注意</el-tag>
            <el-tag v-else>提示</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" text @click="editKnowledgePoint(row)">编辑</el-button>
            <el-button size="small" text type="danger" @click="deleteKnowledgePoint(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 知识点编辑对话框 -->
    <el-dialog v-model="showKPEditDialog" title="编辑知识点" width="500px">
      <el-form :model="kpForm" label-width="100px">
        <el-form-item label="时间点(秒)">
          <el-input-number v-model="kpForm.timePoint" :min="0" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="kpForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="kpForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="kpForm.pointType">
            <el-option label="提示" value="INFO" />
            <el-option label="重点" value="IMPORTANT" />
            <el-option label="注意" value="WARNING" />
          </el-select>
        </el-form-item>
        <el-form-item label="相关链接">
          <el-input v-model="kpForm.relatedUrl" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showKPEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveKnowledgePoint">保存</el-button>
      </template>
    </el-dialog>

    <!-- 字幕管理对话框 -->
    <el-dialog v-model="showSubtitleDialog" title="字幕管理" width="700px">
      <el-button type="primary" size="small" @click="showAddSubtitleDialog = true" style="margin-bottom: 15px;">
        <el-icon><Plus /></el-icon> 添加字幕
      </el-button>
      <el-table :data="subtitles" style="width: 100%">
        <el-table-column prop="languageName" label="语言" width="120" />
        <el-table-column prop="language" label="语言代码" width="100" />
        <el-table-column label="AI翻译" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.aiTranslated" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="默认" width="80">
          <template #default="{ row }">
            <el-icon v-if="row.isDefault" color="green"><Check /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" text @click="translateFrom(row)">翻译</el-button>
            <el-button size="small" text type="danger" @click="deleteSubtitle(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加字幕对话框 -->
    <el-dialog v-model="showAddSubtitleDialog" title="添加字幕" width="500px">
      <el-form :model="subtitleForm" label-width="100px">
        <el-form-item label="语言">
          <el-select v-model="subtitleForm.language">
            <el-option label="中文" value="zh-CN" />
            <el-option label="English" value="en-US" />
            <el-option label="日本語" value="ja-JP" />
            <el-option label="한국어" value="ko-KR" />
            <el-option label="Français" value="fr-FR" />
            <el-option label="Deutsch" value="de-DE" />
          </el-select>
        </el-form-item>
        <el-form-item label="字幕内容">
          <el-input v-model="subtitleForm.subtitleContent" type="textarea" :rows="8" 
            placeholder="请输入SRT或VTT格式的字幕内容" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="subtitleForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddSubtitleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSubtitle">保存</el-button>
      </template>
    </el-dialog>

    <!-- AI翻译字幕对话框 -->
    <el-dialog v-model="showTranslateDialog" title="AI翻译字幕" width="500px">
      <el-form label-width="100px">
        <el-form-item label="源语言">
          <el-text>{{ translateSourceLanguage }}</el-text>
        </el-form-item>
        <el-form-item label="目标语言">
          <el-select v-model="translateTargetLanguage">
            <el-option label="中文" value="zh-CN" />
            <el-option label="English" value="en-US" />
            <el-option label="日本語" value="ja-JP" />
            <el-option label="한국어" value="ko-KR" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTranslateDialog = false">取消</el-button>
        <el-button type="primary" @click="executeTranslate" :loading="translating">
          {{ translating ? '翻译中...' : '开始翻译' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  VideoPlay, Plus, Edit, Delete, Memo, Guide, EditPen, Check 
} from '@element-plus/icons-vue'
import { getCourseDetail } from '@/api/course'
import { useUserStore } from '@/stores/user'
import VideoPlayer from '@/components/VideoPlayer.vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const course = ref({})
const chapters = ref([])
const currentVideo = ref(null)
const activeChapters = ref([])

// 教师权限（使用store中的isTeacher）
const isTeacher = computed(() => userStore.isTeacher)

// 章节管理
const showChapterDialog = ref(false)
const editingChapter = ref(null)
const chapterForm = ref({
  chapterName: '',
  chapterOrder: 1,
  description: ''
})

// 视频管理
const showVideoDialog = ref(false)
const editingVideo = ref(null)
const currentChapter = ref(null)
const videoForm = ref({
  title: '',
  videoUrl: '',
  duration: 300,
  orderNum: 1,
  coverImage: '',
  allowSpeed: true,
  allowDanmaku: true
})

// 知识点管理
const showKnowledgeDialog = ref(false)
const showKPEditDialog = ref(false)
const knowledgePoints = ref([])
const editingKP = ref(null)
const kpForm = ref({
  timePoint: 0,
  title: '',
  content: '',
  pointType: 'INFO',
  relatedUrl: ''
})

// 字幕管理
const showSubtitleDialog = ref(false)
const showAddSubtitleDialog = ref(false)
const showTranslateDialog = ref(false)
const subtitles = ref([])
const subtitleForm = ref({
  language: 'zh-CN',
  subtitleContent: '',
  isDefault: false
})
const translateSourceLanguage = ref('')
const translateTargetLanguage = ref('en-US')
const translateSourceContent = ref('')
const translating = ref(false)

const getDifficultyText = (difficulty) => {
  const map = { 1: '简单', 2: '中等', 3: '困难' }
  return map[difficulty] || '未知'
}

const getDifficultyType = (difficulty) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[difficulty] || 'info'
}

const formatTime = (seconds) => {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

onMounted(async () => {
  try {
    const res = await getCourseDetail(route.params.id)
    course.value = res.data
    await loadChapters()
  } catch (error) {
    console.error('获取课程详情失败:', error)
    ElMessage.error('课程不存在或已被删除')
    router.push('/courses')
  }
})

const loadChapters = async () => {
  try {
    const res = await axios.get(`/api/courses/${route.params.id}/chapters`)
    chapters.value = res.data.data || []
    // 展开所有章节
    activeChapters.value = chapters.value.map(c => c.id)
  } catch (error) {
    console.error('加载章节失败:', error)
  }
}

const playVideo = (video) => {
  currentVideo.value = video
}

// ========== 章节管理 ==========

const addVideo = (chapter) => {
  currentChapter.value = chapter
  editingVideo.value = null
  videoForm.value = {
    title: '',
    videoUrl: '',
    duration: 300,
    orderNum: (chapter.videos?.length || 0) + 1,
    coverImage: '',
    allowSpeed: true,
    allowDanmaku: true
  }
  showVideoDialog.value = true
}

const editChapter = (chapter) => {
  editingChapter.value = chapter
  chapterForm.value = { ...chapter }
  showChapterDialog.value = true
}

const saveChapter = async () => {
  try {
    if (editingChapter.value) {
      // 更新章节
      await axios.put(`/api/courses/chapters/${editingChapter.value.id}`, {
        ...chapterForm.value,
        courseId: course.value.id
      })
      ElMessage.success('章节更新成功')
    } else {
      // 新增章节
      await axios.post('/api/courses/chapters', {
        ...chapterForm.value,
        courseId: course.value.id
      })
      ElMessage.success('章节添加成功')
    }
    showChapterDialog.value = false
    await loadChapters()
  } catch (error) {
    console.error('保存章节失败:', error)
    ElMessage.error('操作失败')
  }
}

const deleteChapter = async (chapterId) => {
  try {
    await ElMessageBox.confirm('确定要删除此章节吗？', '提示', {
      type: 'warning'
    })
    await axios.delete(`/api/courses/chapters/${chapterId}`)
    ElMessage.success('删除成功')
    await loadChapters()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除章节失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// ========== 视频管理 ==========

const editVideo = (video, chapter) => {
  currentChapter.value = chapter
  editingVideo.value = video
  videoForm.value = { ...video }
  showVideoDialog.value = true
}

const saveVideo = async () => {
  try {
    const data = {
      ...videoForm.value,
      courseId: course.value.id,
      chapterId: currentChapter.value.id,
      allowSpeed: videoForm.value.allowSpeed ? 1 : 0,
      allowDanmaku: videoForm.value.allowDanmaku ? 1 : 0,
      aiGenerated: 0
    }
    
    if (editingVideo.value) {
      // 更新视频
      await axios.put(`/api/courses/videos/${editingVideo.value.id}`, data)
      ElMessage.success('视频更新成功')
    } else {
      // 新增视频
      await axios.post('/api/courses/videos', data)
      ElMessage.success('视频添加成功')
    }
    showVideoDialog.value = false
    await loadChapters()
  } catch (error) {
    console.error('保存视频失败:', error)
    ElMessage.error('操作失败')
  }
}

const deleteVideo = async (videoId, chapter) => {
  try {
    await ElMessageBox.confirm('确定要删除此视频吗？', '提示', {
      type: 'warning'
    })
    await axios.delete(`/api/courses/videos/${videoId}`)
    ElMessage.success('删除成功')
    await loadChapters()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除视频失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// ========== 知识点管理 ==========

const manageKnowledgePoints = async (video) => {
  currentVideo.value = video
  showKnowledgeDialog.value = true
  await loadKnowledgePoints(video.id)
}

const loadKnowledgePoints = async (videoId) => {
  try {
    const res = await axios.get(`/api/video-interaction/knowledge-points/${videoId}`)
    knowledgePoints.value = res.data.data || []
  } catch (error) {
    console.error('加载知识点失败:', error)
  }
}

const addKnowledgePoint = () => {
  editingKP.value = null
  kpForm.value = {
    timePoint: 0,
    title: '',
    content: '',
    pointType: 'INFO',
    relatedUrl: '',
    enabled: 1
  }
  showKPEditDialog.value = true
}

const editKnowledgePoint = (kp) => {
  editingKP.value = kp
  kpForm.value = { ...kp }
  showKPEditDialog.value = true
}

const saveKnowledgePoint = async () => {
  try {
    const data = {
      ...kpForm.value,
      videoId: currentVideo.value.id
    }
    
    if (editingKP.value) {
      await axios.put(`/api/video-interaction/knowledge-points/${editingKP.value.id}`, data)
      ElMessage.success('知识点更新成功')
    } else {
      await axios.post('/api/video-interaction/knowledge-points', data)
      ElMessage.success('知识点添加成功')
    }
    showKPEditDialog.value = false
    await loadKnowledgePoints(currentVideo.value.id)
  } catch (error) {
    console.error('保存知识点失败:', error)
    ElMessage.error('操作失败')
  }
}

const deleteKnowledgePoint = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此知识点吗？', '提示', {
      type: 'warning'
    })
    await axios.delete(`/api/video-interaction/knowledge-points/${id}`)
    ElMessage.success('删除成功')
    await loadKnowledgePoints(currentVideo.value.id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除知识点失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// ========== 字幕管理 ==========

const manageSubtitles = async (video) => {
  currentVideo.value = video
  showSubtitleDialog.value = true
  await loadSubtitles(video.id)
}

const loadSubtitles = async (videoId) => {
  try {
    const res = await axios.get(`/api/video-interaction/subtitles/video/${videoId}`)
    subtitles.value = res.data.data || []
  } catch (error) {
    console.error('加载字幕失败:', error)
  }
}

const saveSubtitle = async () => {
  try {
    const languageMap = {
      'zh-CN': '中文',
      'en-US': 'English',
      'ja-JP': '日本語',
      'ko-KR': '한국어',
      'fr-FR': 'Français',
      'de-DE': 'Deutsch'
    }
    
    await axios.post('/api/video-interaction/subtitles', {
      videoId: currentVideo.value.id,
      language: subtitleForm.value.language,
      languageName: languageMap[subtitleForm.value.language],
      subtitleContent: subtitleForm.value.subtitleContent,
      aiTranslated: 0,
      isDefault: subtitleForm.value.isDefault ? 1 : 0
    })
    
    ElMessage.success('字幕添加成功')
    showAddSubtitleDialog.value = false
    await loadSubtitles(currentVideo.value.id)
  } catch (error) {
    console.error('保存字幕失败:', error)
    ElMessage.error('操作失败')
  }
}

const deleteSubtitle = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此字幕吗？', '提示', {
      type: 'warning'
    })
    await axios.delete(`/api/video-interaction/subtitles/${id}`)
    ElMessage.success('删除成功')
    await loadSubtitles(currentVideo.value.id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除字幕失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const translateFrom = (subtitle) => {
  translateSourceLanguage.value = subtitle.languageName
  translateSourceContent.value = subtitle.subtitleContent
  translateTargetLanguage.value = 'en-US'
  showTranslateDialog.value = true
}

const executeTranslate = async () => {
  translating.value = true
  try {
    const res = await axios.post('/api/video-interaction/subtitles/translate', {
      videoId: currentVideo.value.id,
      sourceLanguage: translateSourceLanguage.value,
      targetLanguage: translateTargetLanguage.value,
      originalContent: translateSourceContent.value
    })
    
    ElMessage.success('AI翻译完成')
    showTranslateDialog.value = false
    await loadSubtitles(currentVideo.value.id)
  } catch (error) {
    console.error('AI翻译失败:', error)
    ElMessage.error('翻译失败')
  } finally {
    translating.value = false
  }
}

// ========== 测验管理 ==========

const manageQuiz = (video) => {
  ElMessage.info('测验管理功能开发中...')
}
</script>

<style scoped>
.course-detail {
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
  border-radius: 12px;
}

.course-info {
  flex: 1;
}

.course-info h1 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.course-info p {
  margin: 0 0 16px 0;
  color: #606266;
  line-height: 1.6;
  font-size: 16px;
}

.course-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.teacher-actions {
  display: flex;
  gap: 10px;
}

.chapter-header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chapter-title {
  font-weight: 500;
  color: #303133;
  flex: 1;
}

.chapter-actions {
  display: flex;
  gap: 5px;
}

.video-item {
  padding: 10px 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  border-radius: 4px;
  transition: all 0.3s;
  margin-bottom: 5px;
}

.video-item:hover {
  background: #f5f7fa;
}

.video-item.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.video-actions {
  display: flex;
  gap: 5px;
}
</style>
