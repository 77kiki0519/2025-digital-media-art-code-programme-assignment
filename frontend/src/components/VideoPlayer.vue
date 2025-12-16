<template>
  <div class="video-player-container">
    <div class="video-wrapper" @mousemove="showControls" @mouseleave="hideControls">
      <!-- 视频播放器 -->
      <video 
        ref="videoPlayer" 
        :src="videoUrl"
        @timeupdate="onTimeUpdate"
        @loadedmetadata="onLoadedMetadata"
        @play="onPlay"
        @pause="onPause"
        @ended="onEnded"
        class="video-element"
      />

      <!-- 弹幕层 -->
      <div v-if="showDanmaku" class="danmaku-container">
        <div 
          v-for="(danmaku, index) in activeDanmaku" 
          :key="index"
          class="danmaku-item"
          :style="getDanmakuStyle(danmaku)"
        >
          {{ danmaku.content }}
        </div>
      </div>

      <!-- 控制栏 -->
      <div class="video-controls" :class="{ 'show': controlsVisible }">
        <div class="progress-bar" @click="seek">
          <div class="progress-played" :style="{ width: progress + '%' }"></div>
          <div class="progress-buffered"></div>
        </div>

        <div class="control-buttons">
          <div class="left-controls">
            <el-button 
              :icon="isPlaying ? VideoPause : VideoPlay" 
              circle 
              size="small"
              @click="togglePlay"
            />
            <span class="time-display">
              {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
            </span>
          </div>

          <div class="right-controls">
            <!-- 倍速 -->
            <el-dropdown @command="changeSpeed">
              <el-button size="small" text>
                {{ playbackRate }}x
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="0.5">0.5x</el-dropdown-item>
                  <el-dropdown-item :command="0.75">0.75x</el-dropdown-item>
                  <el-dropdown-item :command="1.0">1.0x</el-dropdown-item>
                  <el-dropdown-item :command="1.25">1.25x</el-dropdown-item>
                  <el-dropdown-item :command="1.5">1.5x</el-dropdown-item>
                  <el-dropdown-item :command="2.0">2.0x</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 字幕切换 -->
            <el-dropdown @command="changeSubtitle">
              <el-button 
                :icon="subtitleEnabled ? View : Hide" 
                size="small" 
                circle
                title="字幕"
              />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="null">关闭字幕</el-dropdown-item>
                  <el-dropdown-item 
                    v-for="subtitle in availableSubtitles" 
                    :key="subtitle.language"
                    :command="subtitle.language"
                  >
                    {{ subtitle.languageName }}
                    <el-tag v-if="subtitle.aiTranslated" size="small" type="success" style="margin-left: 5px">AI</el-tag>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 弹幕开关 -->
            <el-button 
              :icon="Comment" 
              size="small" 
              circle
              :type="showDanmaku ? 'primary' : ''"
              @click="toggleDanmaku"
              title="弹幕"
            />

            <!-- 笔记 -->
            <el-button 
              :icon="Edit" 
              size="small" 
              circle
              @click="openNoteDialog"
              title="记笔记"
            />

            <!-- 音量 -->
            <el-slider 
              v-model="volume" 
              :min="0" 
              :max="100" 
              style="width: 100px; margin: 0 10px;"
              @input="changeVolume"
            />

            <!-- 全屏 -->
            <el-button 
              :icon="FullScreen" 
              size="small" 
              circle
              @click="toggleFullscreen"
              title="全屏"
            />
          </div>
        </div>
      </div>

      <!-- 字幕显示 -->
      <div v-if="subtitleEnabled && currentSubtitle" class="subtitle-display">
        {{ currentSubtitle }}
      </div>
      
      <!-- 知识点弹窗 -->
      <el-dialog 
        v-model="knowledgePointVisible" 
        :title="currentKnowledgePoint?.title"
        width="500px"
        :show-close="true"
        class="knowledge-point-dialog"
      >
        <div class="knowledge-content">
          <el-icon v-if="currentKnowledgePoint?.pointType === 'IMPORTANT'" color="#E6A23C" size="24">
            <Warning />
          </el-icon>
          <el-icon v-else-if="currentKnowledgePoint?.pointType === 'WARNING'" color="#F56C6C" size="24">
            <WarnTriangleFilled />
          </el-icon>
          <el-icon v-else color="#409EFF" size="24">
            <InfoFilled />
          </el-icon>
          <div class="knowledge-text" v-html="currentKnowledgePoint?.content"></div>
        </div>
        <div v-if="currentKnowledgePoint?.relatedUrl" class="knowledge-link">
          <el-link :href="currentKnowledgePoint.relatedUrl" target="_blank" type="primary">
            查看相关资料
          </el-link>
        </div>
        <template #footer>
          <el-button @click="knowledgePointVisible = false">我知道了</el-button>
          <el-button type="primary" @click="continuePlay">继续播放</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- 弹幕输入 -->
    <div v-if="showDanmaku" class="danmaku-input">
      <el-input 
        v-model="danmakuInput" 
        placeholder="发个友善的弹幕吧~"
        @keyup.enter="sendDanmaku"
      >
        <template #append>
          <el-button @click="sendDanmaku">发送</el-button>
        </template>
      </el-input>
    </div>

    <!-- 笔记对话框 -->
    <el-dialog v-model="noteDialogVisible" title="记笔记" width="500px">
      <el-form>
        <el-form-item label="时间点">
          <el-text>{{ formatTime(currentTime) }}</el-text>
        </el-form-item>
        <el-form-item label="笔记内容">
          <el-input 
            v-model="noteContent" 
            type="textarea" 
            :rows="6"
            placeholder="记录你的学习笔记..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNote">保存</el-button>
        <el-button type="success" @click="organizeNotes">
          <el-icon><MagicStick /></el-icon>
          AI整理笔记
        </el-button>
      </template>
    </el-dialog>
    
    <!-- AI整理后的笔记 -->
    <el-dialog v-model="organizedNotesVisible" title="AI整理的笔记" width="700px">
      <div class="organized-notes" v-html="organizedNotesContent"></div>
      <template #footer>
        <el-button type="primary" @click="organizedNotesVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 笔记列表侧边栏 -->
    <div v-if="showNotesList" class="notes-sidebar">
      <div class="notes-header">
        <span>我的笔记</span>
        <el-button :icon="Close" size="small" text @click="showNotesList = false" />
      </div>
      <div class="notes-list">
        <div 
          v-for="note in notes" 
          :key="note.id" 
          class="note-item"
          @click="jumpToNote(note.timePoint)"
        >
          <div class="note-time">{{ formatTime(note.timePoint) }}</div>
          <div class="note-content">{{ note.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  VideoPlay, VideoPause, FullScreen, Comment, Edit, View, Hide, Close,
  Warning, WarnTriangleFilled, InfoFilled, MagicStick
} from '@element-plus/icons-vue'
import { 
  updateVideoProgress, 
  getVideoDanmaku, 
  sendDanmaku as sendDanmakuAPI,
  addVideoNote,
  getVideoNotes,
  getVideoSubtitles,
  getVideoKnowledgePoints,
  organizeNotes as organizeNotesAPI
} from '@/api/videoInteraction'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  videoId: {
    type: Number,
    required: true
  },
  videoUrl: {
    type: String,
    required: true
  },
  subtitleUrl: {
    type: String,
    default: ''
  }
})

const userStore = useUserStore()
const videoPlayer = ref(null)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const playbackRate = ref(1.0)
const volume = ref(100)
const subtitleEnabled = ref(false)
const currentSubtitle = ref('')
const showDanmaku = ref(true)
const controlsVisible = ref(true)
const danmakuInput = ref('')
const danmakuList = ref([])
const activeDanmaku = ref([])
const notes = ref([])
const noteDialogVisible = ref(false)
const noteContent = ref('')
const showNotesList = ref(false)
const availableSubtitles = ref([])
const currentLanguage = ref(null)
const knowledgePoints = ref([])
const knowledgePointVisible = ref(false)
const currentKnowledgePoint = ref(null)
const organizedNotesVisible = ref(false)
const organizedNotesContent = ref('')

let controlsTimer = null
let progressUpdateTimer = null
let lastKnowledgePointTime = -1

const progress = computed(() => {
  return duration.value > 0 ? (currentTime.value / duration.value) * 100 : 0
})

onMounted(async () => {
  // 设置初始音量
  if (videoPlayer.value) {
    videoPlayer.value.volume = volume.value / 100
  }
  
  // 加载弹幕
  await loadDanmaku()
  
  // 加载笔记
  await loadNotes()
  
  // 加载字幕列表
  await loadSubtitles()
  
  // 加载知识点
  await loadKnowledgePoints()
  
  // 定时更新播放进度
  startProgressUpdate()
})

onBeforeUnmount(() => {
  stopProgressUpdate()
})

const loadDanmaku = async () => {
  try {
    const res = await getVideoDanmaku(props.videoId)
    danmakuList.value = res.data || []
  } catch (error) {
    console.error('加载弹幕失败:', error)
  }
}

const loadNotes = async () => {
  try {
    if (!userStore.userId) {
      console.log('用户未登录，跳过加载笔记')
      return
    }
    const res = await getVideoNotes(props.videoId, userStore.userId)
    notes.value = res.data || []
  } catch (error) {
    console.error('加载笔记失败:', error)
  }
}

const onTimeUpdate = (e) => {
  currentTime.value = Math.floor(e.target.currentTime)
  
  // 显示当前时间点的弹幕
  updateActiveDanmaku()
  
  // 检查是否需要显示知识点弹窗
  checkKnowledgePoint()
}

const onLoadedMetadata = (e) => {
  duration.value = Math.floor(e.target.duration)
}

const onPlay = () => {
  isPlaying.value = true
}

const onPause = () => {
  isPlaying.value = false
}

const onEnded = () => {
  isPlaying.value = false
  // 记录完成
  updateProgress(true)
}

const togglePlay = () => {
  if (videoPlayer.value) {
    if (isPlaying.value) {
      videoPlayer.value.pause()
    } else {
      videoPlayer.value.play().catch(error => {
        console.error('视频播放失败:', error)
        ElMessage.error('视频播放失败，请检查视频URL是否有效')
      })
    }
  }
}

const seek = (e) => {
  const rect = e.currentTarget.getBoundingClientRect()
  const percent = (e.clientX - rect.left) / rect.width
  const seekTime = duration.value * percent
  
  if (videoPlayer.value) {
    videoPlayer.value.currentTime = seekTime
    currentTime.value = Math.floor(seekTime)
  }
}

const changeSpeed = (speed) => {
  playbackRate.value = speed
  if (videoPlayer.value) {
    videoPlayer.value.playbackRate = speed
  }
}

const changeVolume = (val) => {
  if (videoPlayer.value) {
    videoPlayer.value.volume = val / 100
  }
}

const loadSubtitles = async () => {
  try {
    const res = await getVideoSubtitles(props.videoId)
    availableSubtitles.value = res.data || []
    
    // 默认选择第一个字幕
    if (availableSubtitles.value.length > 0) {
      const defaultSubtitle = availableSubtitles.value.find(s => s.isDefault) || availableSubtitles.value[0]
      currentLanguage.value = defaultSubtitle.language
      subtitleEnabled.value = true
    }
  } catch (error) {
    console.error('加载字幕列表失败:', error)
  }
}

const changeSubtitle = (language) => {
  if (language === null) {
    subtitleEnabled.value = false
    currentLanguage.value = null
  } else {
    subtitleEnabled.value = true
    currentLanguage.value = language
    // TODO: 加载对应语言的字幕内容
  }
}

const loadKnowledgePoints = async () => {
  try {
    const res = await getVideoKnowledgePoints(props.videoId)
    knowledgePoints.value = res.data || []
  } catch (error) {
    console.error('加载知识点失败:', error)
  }
}

const checkKnowledgePoint = () => {
  // 检查是否有知识点需要在当前时间点显示
  const point = knowledgePoints.value.find(p => 
    p.timePoint === currentTime.value && p.timePoint !== lastKnowledgePointTime
  )
  
  if (point) {
    currentKnowledgePoint.value = point
    knowledgePointVisible.value = true
    lastKnowledgePointTime = currentTime.value
    
    // 暂停播放
    if (videoPlayer.value && isPlaying.value) {
      videoPlayer.value.pause()
    }
  }
}

const continuePlay = () => {
  knowledgePointVisible.value = false
  if (videoPlayer.value) {
    videoPlayer.value.play()
  }
}

const organizeNotes = async () => {
  if (notes.value.length === 0) {
    ElMessage.warning('暂无笔记可整理')
    return
  }
  
  if (!userStore.userId) {
    ElMessage.error('请先登录')
    return
  }
  
  try {
    ElMessage.info('AI正在整理您的笔记，请稍候...')
    const res = await organizeNotesAPI({
      studentId: userStore.userId,
      videoId: props.videoId
    })
    
    organizedNotesContent.value = res.data.replace(/\n/g, '<br>')
    organizedNotesVisible.value = true
    noteDialogVisible.value = false
  } catch (error) {
    console.error('AI整理笔记失败:', error)
    ElMessage.error('AI整理失败')
  }
}

const toggleDanmaku = () => {
  showDanmaku.value = !showDanmaku.value
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    videoPlayer.value.parentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const showControls = () => {
  controlsVisible.value = true
  clearTimeout(controlsTimer)
  controlsTimer = setTimeout(() => {
    if (isPlaying.value) {
      controlsVisible.value = false
    }
  }, 3000)
}

const hideControls = () => {
  clearTimeout(controlsTimer)
  if (isPlaying.value) {
    controlsVisible.value = false
  }
}

const updateActiveDanmaku = () => {
  // 获取当前时间点前后1秒的弹幕
  const currentDanmaku = danmakuList.value.filter(d => 
    d.timePoint >= currentTime.value && d.timePoint < currentTime.value + 1
  )
  
  activeDanmaku.value = currentDanmaku.map(d => ({
    ...d,
    left: Math.random() * 70 + 10, // 随机起始位置
    top: Math.random() * 70 + 10 // 随机高度
  }))
}

const getDanmakuStyle = (danmaku) => {
  return {
    left: danmaku.left + '%',
    top: danmaku.top + '%',
    color: danmaku.color || '#FFFFFF'
  }
}

const sendDanmaku = async () => {
  if (!danmakuInput.value.trim()) {
    return
  }
  
  if (!userStore.userId) {
    ElMessage.error('请先登录')
    return
  }
  
  try {
    await sendDanmakuAPI({
      videoId: props.videoId,
      studentId: userStore.userId,
      timePoint: Math.floor(currentTime.value),
      content: danmakuInput.value,
      color: '#FFFFFF',
      type: 0
    })
    
    ElMessage.success('弹幕发送成功')
    danmakuInput.value = ''
    
    // 重新加载弹幕
    await loadDanmaku()
  } catch (error) {
    console.error('发送弹幕失败:', error)
    ElMessage.error('发送弹幕失败')
  }
}

const openNoteDialog = () => {
  noteContent.value = ''
  noteDialogVisible.value = true
}

const saveNote = async () => {
  if (!noteContent.value.trim()) {
    ElMessage.warning('请输入笔记内容')
    return
  }
  
  if (!userStore.userId) {
    ElMessage.error('请先登录')
    return
  }
  
  try {
    await addVideoNote({
      videoId: props.videoId,
      studentId: userStore.userId,
      timePoint: Math.floor(currentTime.value),
      content: noteContent.value
    })
    
    ElMessage.success('笔记保存成功')
    noteDialogVisible.value = false
    noteContent.value = ''
    
    // 重新加载笔记
    await loadNotes()
  } catch (error) {
    console.error('保存笔记失败:', error)
    ElMessage.error('保存笔记失败')
  }
}

const jumpToNote = (timePoint) => {
  if (videoPlayer.value) {
    videoPlayer.value.currentTime = timePoint
    currentTime.value = timePoint
  }
}

const startProgressUpdate = () => {
  progressUpdateTimer = setInterval(() => {
    if (isPlaying.value) {
      updateProgress(false)
    }
  }, 10000) // 每10秒更新一次
}

const stopProgressUpdate = () => {
  if (progressUpdateTimer) {
    clearInterval(progressUpdateTimer)
  }
}

const updateProgress = async (completed) => {
  try {
    if (!userStore.userId) return
    await updateVideoProgress({
      videoId: props.videoId,
      studentId: userStore.userId,
      position: Math.floor(currentTime.value),
      duration: Math.floor(duration.value)
    })
  } catch (error) {
    console.error('更新进度失败:', error)
  }
}

const formatTime = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = Math.floor(seconds % 60)
  
  if (h > 0) {
    return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }
  return `${m}:${s.toString().padStart(2, '0')}`
}
</script>

<style scoped>
.video-player-container {
  position: relative;
  width: 100%;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.video-wrapper {
  position: relative;
  width: 100%;
  padding-top: 56.25%; /* 16:9 宽高比 */
  background: #000;
}

.video-element {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.danmaku-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.danmaku-item {
  position: absolute;
  white-space: nowrap;
  font-size: 16px;
  font-weight: bold;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
  animation: danmaku-move 5s linear;
}

@keyframes danmaku-move {
  from {
    transform: translateX(100vw);
  }
  to {
    transform: translateX(-100%);
  }
}

.video-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  padding: 20px 10px 10px;
  opacity: 0;
  transition: opacity 0.3s;
}

.video-controls.show {
  opacity: 1;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  margin-bottom: 10px;
  border-radius: 2px;
  position: relative;
}

.progress-played {
  height: 100%;
  background: #409eff;
  border-radius: 2px;
  transition: width 0.1s;
}

.control-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}

.left-controls,
.right-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.time-display {
  font-size: 14px;
  margin-left: 10px;
}

.subtitle-display {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 16px;
}

.danmaku-input {
  margin-top: 10px;
}

.notes-sidebar {
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
  width: 300px;
  background: #fff;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.notes-header {
  padding: 15px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.notes-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.note-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s;
}

.note-item:hover {
  background: #f5f7fa;
}

.note-time {
  color: #409eff;
  font-size: 12px;
  margin-bottom: 5px;
}

.note-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.knowledge-point-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.knowledge-content {
  display: flex;
  gap: 15px;
  align-items: flex-start;
}

.knowledge-text {
  flex: 1;
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
}

.knowledge-link {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.organized-notes {
  font-size: 15px;
  line-height: 2;
  color: #303133;
  max-height: 500px;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}
</style>

