<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="welcome-banner">
          <h1>欢迎使用AI驱动线上课程系统</h1>
          <p>智能化教学，让学习更高效</p>
        </div>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="feature-card">
          <el-icon :size="40" color="#409eff"><VideoCamera /></el-icon>
          <h3>在线课程</h3>
          <p>海量优质课程，随时随地学习</p>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="feature-card">
          <el-icon :size="40" color="#67c23a"><ChatDotRound /></el-icon>
          <h3>AI智能答疑</h3>
          <p>24小时在线，即问即答</p>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="feature-card">
          <el-icon :size="40" color="#e6a23c"><Document /></el-icon>
          <h3>智能批改</h3>
          <p>AI辅助批改，快速反馈</p>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="feature-card">
          <el-icon :size="40" color="#f56c6c"><DataAnalysis /></el-icon>
          <h3>学习分析</h3>
          <p>个性化学习路径推荐</p>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>推荐课程</span>
              <el-button text @click="$router.push('/courses')">查看更多 →</el-button>
            </div>
          </template>
          
          <el-empty v-if="!courses.length" description="暂无课程" />
          
          <el-row :gutter="20" v-else>
            <el-col :span="6" v-for="course in courses" :key="course.id">
              <el-card shadow="hover" :body-style="{ padding: '0' }" class="course-card">
                <img :src="course.coverImage || 'https://via.placeholder.com/300x200'" class="course-cover" />
                <div style="padding: 14px;">
                  <h4>{{ course.courseName }}</h4>
                  <p class="course-desc">{{ course.description }}</p>
                  <div class="course-footer">
                    <el-tag size="small">{{ getDifficultyText(course.difficulty) }}</el-tag>
                    <el-button text type="primary" @click="$router.push(`/courses/${course.id}`)">
                      进入学习
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCourses } from '@/api/course'

const courses = ref([])

const getDifficultyText = (difficulty) => {
  const map = { 1: '简单', 2: '中等', 3: '困难' }
  return map[difficulty] || '未知'
}

onMounted(async () => {
  try {
    const res = await getCourses({ page: 1, size: 4, status: 1 })
    courses.value = res.data.records
  } catch (error) {
    console.error('获取课程列表失败:', error)
  }
})
</script>

<style scoped>
.home {
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.welcome-banner {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  padding: 60px 40px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
}

.welcome-banner h1 {
  margin: 0 0 10px 0;
  font-size: 36px;
  font-weight: 600;
}

.welcome-banner p {
  margin: 0;
  font-size: 18px;
  opacity: 0.95;
}

.feature-card {
  text-align: center;
  padding: 30px 20px;
  transition: all 0.3s;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #409eff;
}

.feature-card h3 {
  margin: 15px 0 10px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.feature-card p {
  margin: 0;
  color: #909399;
  font-size: 14px;
  line-height: 1.6;
}

.course-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.course-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #409eff;
}

.course-cover {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.course-card h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-desc {
  margin: 0 0 12px 0;
  color: #909399;
  font-size: 13px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

