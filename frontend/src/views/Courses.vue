<template>
  <div class="courses">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>课程列表</span>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程"
            style="width: 300px;"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6" v-for="course in courses" :key="course.id">
          <el-card shadow="hover" :body-style="{ padding: '0' }" class="course-card" @click="$router.push(`/courses/${course.id}`)">
            <img :src="course.coverImage || 'https://via.placeholder.com/300x200'" class="course-cover" />
            <div style="padding: 14px;">
              <h4>{{ course.courseName }}</h4>
              <p class="course-desc">{{ course.description }}</p>
              <div class="course-footer">
                <el-tag size="small" :type="getDifficultyType(course.difficulty)">
                  {{ getDifficultyText(course.difficulty) }}
                </el-tag>
                <el-tag size="small" v-if="course.category">{{ course.category }}</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-empty v-if="!courses.length" description="暂无课程" />
      
      <div style="margin-top: 20px; text-align: center;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[8, 16, 24, 32]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchCourses"
          @current-change="fetchCourses"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getCourses, searchCourses } from '@/api/course'

const searchKeyword = ref('')
const courses = ref([])
const pagination = ref({
  page: 1,
  size: 8,
  total: 0
})

const getDifficultyText = (difficulty) => {
  const map = { 1: '简单', 2: '中等', 3: '困难' }
  return map[difficulty] || '未知'
}

const getDifficultyType = (difficulty) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[difficulty] || 'info'
}

const fetchCourses = async () => {
  try {
    const res = await getCourses({
      page: pagination.value.page,
      size: pagination.value.size,
      status: 1
    })
    courses.value = res.data.records
    pagination.value.total = res.data.total
  } catch (error) {
    console.error('获取课程列表失败:', error)
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    fetchCourses()
    return
  }
  
  try {
    const res = await searchCourses({
      keyword: searchKeyword.value,
      page: pagination.value.page,
      size: pagination.value.size
    })
    courses.value = res.data.records
    pagination.value.total = res.data.total
  } catch (error) {
    console.error('搜索课程失败:', error)
  }
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.courses {
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
  height: 180px;
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
  gap: 8px;
  flex-wrap: wrap;
}
</style>

