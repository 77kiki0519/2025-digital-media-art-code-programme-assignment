<template>
  <div class="material-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>教材制作（AI智能生成）</span>
          <el-button type="primary" @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="material-tabs">
        <!-- 文本转PPT -->
        <el-tab-pane label="文本转PPT" name="text-to-ppt">
          <el-form :model="pptForm" label-width="120px" style="max-width: 800px">
            <el-form-item label="课程">
              <el-select v-model="pptForm.courseId" placeholder="请选择课程" @change="onCourseChange">
                <el-option 
                  v-for="course in courses" 
                  :key="course.id" 
                  :label="course.courseName" 
                  :value="course.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="章节">
              <el-select v-model="pptForm.chapterId" placeholder="请选择章节（可选）">
                <el-option 
                  v-for="chapter in chapters" 
                  :key="chapter.id" 
                  :label="chapter.chapterName" 
                  :value="chapter.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="教材名称">
              <el-input v-model="pptForm.materialName" placeholder="请输入教材名称" />
            </el-form-item>

            <el-form-item label="文本内容">
              <el-input 
                v-model="pptForm.textContent" 
                type="textarea" 
                :rows="10"
                placeholder="请输入要转换为PPT的文本内容，AI将自动提取要点生成PPT"
              />
            </el-form-item>

            <el-form-item label="生成参数">
              <el-checkbox-group v-model="pptOptions">
                <el-checkbox label="auto_format">自动排版布局</el-checkbox>
                <el-checkbox label="add_charts">自动生成图表</el-checkbox>
                <el-checkbox label="modern_style">现代扁平化设计</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="generating" @click="generatePPT">
                <el-icon><MagicStick /></el-icon>
                AI生成PPT
              </el-button>
              <el-text type="info" style="margin-left: 10px">
                AI将根据您的文本内容自动生成专业PPT
              </el-text>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- PPT转视频 -->
        <el-tab-pane label="PPT转视频" name="ppt-to-video">
          <el-form :model="videoForm" label-width="120px" style="max-width: 800px">
            <el-form-item label="选择PPT">
              <el-select v-model="videoForm.materialId" placeholder="请选择已创建的PPT">
                <el-option 
                  v-for="material in pptMaterials" 
                  :key="material.id" 
                  :label="material.materialName" 
                  :value="material.id"
                />
              </el-select>
              <el-button type="text" @click="refreshMaterials">刷新列表</el-button>
            </el-form-item>

            <el-form-item label="配音语音">
              <el-radio-group v-model="videoParams.voice">
                <el-radio label="female">女声（温柔）</el-radio>
                <el-radio label="male">男声（浑厚）</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="播放速度">
              <el-slider v-model="videoParams.speed" :min="0.5" :max="2" :step="0.1" show-stops />
              <el-text>{{ videoParams.speed }}x</el-text>
            </el-form-item>

            <el-form-item label="每页时长">
              <el-input-number v-model="videoParams.pageTime" :min="3" :max="30" /> 秒
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="converting" @click="convertToVideo">
                <el-icon><VideoCamera /></el-icon>
                转换为视频
              </el-button>
              <el-text type="info" style="margin-left: 10px">
                AI将为PPT生成讲解词并配音转为视频
              </el-text>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 生成记录 -->
        <el-tab-pane label="生成记录" name="history">
          <el-table :data="materials" style="width: 100%">
            <el-table-column prop="materialName" label="名称" width="200" />
            <el-table-column prop="materialType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.materialType === 'TEXT'">文本</el-tag>
                <el-tag v-else-if="row.materialType === 'PPT'" type="success">PPT</el-tag>
                <el-tag v-else-if="row.materialType === 'VIDEO'" type="warning">视频</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="generationStatus" label="状态" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.generationStatus === 'PENDING'" type="info">待生成</el-tag>
                <el-tag v-else-if="row.generationStatus === 'PROCESSING'" type="warning">生成中</el-tag>
                <el-tag v-else-if="row.generationStatus === 'COMPLETED'" type="success">已完成</el-tag>
                <el-tag v-else-if="row.generationStatus === 'FAILED'" type="danger">失败</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="aiGenerated" label="AI生成" width="100">
              <template #default="{ row }">
                <el-icon v-if="row.aiGenerated" color="green"><Check /></el-icon>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button v-if="row.generationStatus === 'COMPLETED'" type="primary" size="small" link @click="viewMaterial(row)">
                  查看
                </el-button>
                <el-button type="danger" size="small" link @click="deleteMaterial(row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, VideoCamera, Check } from '@element-plus/icons-vue'
import { textToPPT, pptToVideo, getMaterialsByCourse, deleteMaterial as deleteMaterialAPI } from '@/api/material'
import { getCourses } from '@/api/course'

const activeTab = ref('text-to-ppt')
const generating = ref(false)
const converting = ref(false)

const courses = ref([])
const chapters = ref([])
const pptMaterials = ref([])
const materials = ref([])

const pptForm = ref({
  courseId: null,
  chapterId: null,
  materialName: '',
  textContent: ''
})

const pptOptions = ref(['auto_format', 'modern_style'])

const videoForm = ref({
  materialId: null
})

const videoParams = ref({
  voice: 'female',
  speed: 1.0,
  pageTime: 5
})

onMounted(async () => {
  await loadCourses()
})

const loadCourses = async () => {
  try {
    const res = await getCourses({ page: 1, size: 100 })
    courses.value = res.data.records || []
    
    // 如果有课程且没有选择课程，默认选择第一个
    if (courses.value.length > 0 && !pptForm.value.courseId) {
      pptForm.value.courseId = courses.value[0].id
      await onCourseChange(courses.value[0].id)
    }
  } catch (error) {
    console.error('加载课程列表失败:', error)
    courses.value = []
  }
}

const onCourseChange = async (courseId) => {
  // TODO: 加载章节列表
  chapters.value = []
  await refreshMaterials()
}

const refreshMaterials = async () => {
  // 如果没有选择课程，尝试使用第一个课程
  if (!pptForm.value.courseId) {
    if (courses.value.length > 0) {
      pptForm.value.courseId = courses.value[0].id
    } else {
      materials.value = []
      pptMaterials.value = []
      return
    }
  }
  
  try {
    const res = await getMaterialsByCourse(pptForm.value.courseId)
    materials.value = res.data || []
    pptMaterials.value = materials.value.filter(m => m.materialType === 'PPT' && m.generationStatus === 'COMPLETED')
  } catch (error) {
    console.error('加载教材列表失败:', error)
    materials.value = []
    pptMaterials.value = []
  }
}

const generatePPT = async () => {
  if (!pptForm.value.courseId || !pptForm.value.materialName || !pptForm.value.textContent) {
    ElMessage.warning('请填写完整信息')
    return
  }

  generating.value = true
  try {
    const params = {
      options: pptOptions.value
    }
    
    await textToPPT({
      courseId: pptForm.value.courseId,
      chapterId: pptForm.value.chapterId,
      materialName: pptForm.value.materialName,
      textContent: pptForm.value.textContent,
      params: JSON.stringify(params)
    })
    
    ElMessage.success('PPT生成任务已提交，AI正在为您生成...')
    
    // 切换到记录标签页
    activeTab.value = 'history'
    await refreshMaterials()
    
    // 清空表单
    pptForm.value.materialName = ''
    pptForm.value.textContent = ''
  } catch (error) {
    console.error('生成PPT失败:', error)
    ElMessage.error('生成PPT失败: ' + (error.message || '未知错误'))
  } finally {
    generating.value = false
  }
}

const convertToVideo = async () => {
  if (!videoForm.value.materialId) {
    ElMessage.warning('请选择要转换的PPT')
    return
  }

  converting.value = true
  try {
    await pptToVideo({
      materialId: videoForm.value.materialId,
      params: JSON.stringify(videoParams.value)
    })
    
    ElMessage.success('视频转换任务已提交，AI正在为您生成...')
    
    // 切换到记录标签页
    activeTab.value = 'history'
    await refreshMaterials()
  } catch (error) {
    console.error('转换视频失败:', error)
    ElMessage.error('转换视频失败: ' + (error.message || '未知错误'))
  } finally {
    converting.value = false
  }
}

const viewMaterial = (row) => {
  // 构建详情信息
  let detailInfo = `教材名称：${row.materialName}\n` +
    `教材类型：${row.materialType === 'TEXT' ? '文本' : row.materialType === 'PPT' ? 'PPT' : '视频'}\n` +
    `AI生成：${row.aiGenerated ? '是' : '否'}\n` +
    `生成状态：${getStatusText(row.generationStatus)}\n` +
    `创建时间：${row.createdAt}\n`
  
  if (row.materialType === 'PPT' && row.pages) {
    detailInfo += `PPT页数：${row.pages}页\n`
  }
  
  if (row.materialType === 'VIDEO' && row.duration) {
    const minutes = Math.floor(row.duration / 60)
    const seconds = row.duration % 60
    detailInfo += `视频时长：${minutes}分${seconds}秒\n`
  }
  
  if (row.fileUrl) {
    detailInfo += `\n文件URL：${row.fileUrl}\n\n点击"打开文件"可下载或查看`
  } else {
    detailInfo += `\n提示：文件正在生成中或生成失败`
  }
  
  if (row.content && row.materialType === 'TEXT') {
    detailInfo += `\n\n内容预览：\n${row.content.substring(0, 200)}${row.content.length > 200 ? '...' : ''}`
  }
  
  ElMessageBox.confirm(detailInfo, '教材详情', {
    confirmButtonText: row.fileUrl ? '下载文件' : '关闭',
    cancelButtonText: '关闭',
    showCancelButton: !!row.fileUrl,
    type: 'info'
  }).then(() => {
    if (row.fileUrl) {
      // 构建完整的文件URL
      const fileUrl = row.fileUrl.startsWith('http') ? row.fileUrl : `${window.location.origin}${row.fileUrl}`
      // 创建一个临时的a标签下载文件
      const link = document.createElement('a')
      link.href = fileUrl
      link.download = row.materialName
      link.target = '_blank'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }
  }).catch(() => {})
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待生成',
    'PROCESSING': '生成中',
    'COMPLETED': '已完成',
    'FAILED': '失败'
  }
  return map[status] || status
}

const deleteMaterial = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该教材吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    await deleteMaterialAPI(id)
    ElMessage.success('删除成功')
    await refreshMaterials()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.material-create {
  padding: 20px;
  animation: fadeIn 0.5s;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.material-tabs {
  margin-top: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>

