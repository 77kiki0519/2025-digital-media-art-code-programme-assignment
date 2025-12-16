<template>
  <div class="my-courses">
    <el-card>
      <template #header>
        <div class="header">
          <span>我的课程</span>
          <el-button type="primary" size="small" @click="showAllCourses = !showAllCourses">
            {{ showAllCourses ? '查看已选' : '浏览课程' }}
          </el-button>
        </div>
      </template>
      
      <!-- 已选课程 -->
      <div v-if="!showAllCourses">
        <el-row :gutter="20" v-if="myCourses.length > 0">
          <el-col :span="8" v-for="course in myCourses" :key="course.id">
            <el-card shadow="hover" class="course-card">
              <div class="course-cover">
                <el-icon :size="48" color="#409eff"><Reading /></el-icon>
              </div>
              <div class="course-info">
                <h3>{{ course.courseName }}</h3>
                <p>{{ course.description || '暂无描述' }}</p>
                <div class="course-meta">
                  <el-tag size="small">{{ course.category }}</el-tag>
                  <el-tag size="small" :type="getDifficultyType(course.difficulty)">
                    {{ getDifficultyText(course.difficulty) }}
                  </el-tag>
                </div>
                <div class="course-actions">
                  <el-button type="primary" size="small" @click="enterCourse(course)">进入学习</el-button>
                  <el-button size="small" @click="removeCourse(course)">退选</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-else description="您还没有选择课程，点击上方「浏览课程」开始选课" />
      </div>
      
      <!-- 浏览所有课程 -->
      <div v-else>
        <el-row :gutter="20" v-if="allCourses.length > 0">
          <el-col :span="8" v-for="course in allCourses" :key="course.id">
            <el-card shadow="hover" class="course-card">
              <div class="course-cover">
                <el-icon :size="48" color="#67c23a"><Reading /></el-icon>
              </div>
              <div class="course-info">
                <h3>{{ course.courseName }}</h3>
                <p>{{ course.description || '暂无描述' }}</p>
                <div class="course-meta">
                  <el-tag size="small">{{ course.category }}</el-tag>
                  <span class="teacher">讲师：{{ course.teacher?.realName || '未知' }}</span>
                </div>
                <div class="course-actions">
                  <el-button 
                    :type="isSelected(course) ? 'info' : 'success'" 
                    size="small" 
                    @click="selectCourse(course)"
                    :disabled="isSelected(course)"
                  >
                    {{ isSelected(course) ? '已选择' : '选择课程' }}
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-else description="暂无可选课程" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Reading } from '@element-plus/icons-vue'
import { getCourses } from '@/api/course'

const router = useRouter()
const showAllCourses = ref(false)
const allCourses = ref([])
const myCourses = ref([])

// 从localStorage加载已选课程
const loadMyCourses = () => {
  const saved = localStorage.getItem('myCourses')
  if (saved) {
    myCourses.value = JSON.parse(saved)
  }
}

// 保存已选课程到localStorage
const saveMyCourses = () => {
  localStorage.setItem('myCourses', JSON.stringify(myCourses.value))
}

const getDifficultyText = (difficulty) => {
  const map = { 1: '入门', 2: '进阶', 3: '高级' }
  return map[difficulty] || '未知'
}

const getDifficultyType = (difficulty) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[difficulty] || 'info'
}

const fetchAllCourses = async () => {
  try {
    const res = await getCourses({ page: 1, size: 100 })
    allCourses.value = res.data.records || res.data || []
  } catch (error) {
    console.error('获取课程失败:', error)
  }
}

const isSelected = (course) => {
  return myCourses.value.some(c => c.id === course.id)
}

const selectCourse = (course) => {
  if (isSelected(course)) return
  myCourses.value.push(course)
  saveMyCourses()
  ElMessage.success('选课成功')
}

const removeCourse = async (course) => {
  try {
    await ElMessageBox.confirm('确定要退选该课程吗？', '提示', { type: 'warning' })
    myCourses.value = myCourses.value.filter(c => c.id !== course.id)
    saveMyCourses()
    ElMessage.success('已退选')
  } catch {}
}

const enterCourse = (course) => {
  router.push(`/courses/${course.id}`)
}

onMounted(() => {
  loadMyCourses()
  fetchAllCourses()
})
</script>

<style scoped>
.my-courses {
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

.course-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.course-card:hover {
  transform: translateY(-4px);
}

.course-cover {
  height: 120px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  margin-bottom: 12px;
}

.course-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-info p {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: #909399;
  height: 36px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}

.teacher {
  font-size: 12px;
  color: #909399;
}

.course-actions {
  display: flex;
  gap: 8px;
}
</style>
