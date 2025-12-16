<template>
  <div class="teach">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>我的教学</span>
          <div>
            <el-button type="success" @click="$router.push('/material/create')">
              <el-icon><MagicStick /></el-icon>
              AI教材制作
            </el-button>
            <el-button type="primary" @click="resetForm(); dialogVisible = true">创建课程</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="courses" style="width: 100%">
        <el-table-column prop="courseName" label="课程名称" width="200" />
        <el-table-column prop="courseCode" label="课程编码" width="120" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="row.difficulty === 1 ? 'success' : row.difficulty === 3 ? 'danger' : 'warning'">
              {{ row.difficulty === 1 ? '简单' : row.difficulty === 3 ? '困难' : '中等' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="viewDetail(row)">
              详情
            </el-button>
            <el-button text type="warning" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button text :type="row.status === 1 ? 'info' : 'success'" size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 创建/编辑课程对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课程' : '创建课程'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程名称">
          <el-input v-model="form.courseName" />
        </el-form-item>
        <el-form-item label="课程编码">
          <el-input v-model="form.courseCode" />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false; resetForm()">取消</el-button>
        <el-button type="primary" @click="handleSubmit">{{ isEdit ? '更新' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import { getCourses, createCourse, updateCourse, deleteCourse } from '@/api/course'
import { useUserStore } from '@/stores/user'

const router = useRouter()

const userStore = useUserStore()
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingCourseId = ref(null)
const courses = ref([])

const form = ref({
  courseName: '',
  courseCode: '',
  description: '',
  category: '',
  difficulty: 2,
  status: 1,  // 默认已发布
  teacherId: userStore.userInfo.userId
})

const getStatusText = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '已下架' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const fetchCourses = async () => {
  try {
    const res = await getCourses({ teacherId: userStore.userInfo.userId })
    courses.value = res.data.records || res.data
  } catch (error) {
    console.error('获取课程列表失败:', error)
  }
}

const handleSubmit = async () => {
  if (!form.value.courseName || !form.value.courseCode) {
    ElMessage.warning('请填写课程名称和编码')
    return
  }
  
  try {
    if (isEdit.value) {
      // 更新课程
      await updateCourse(editingCourseId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      // 创建课程
      await createCourse(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchCourses()
  } catch (error) {
    console.error(isEdit.value ? '更新课程失败:' : '创建课程失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

const handleEdit = (row) => {
  isEdit.value = true
  editingCourseId.value = row.id
  form.value = {
    courseName: row.courseName,
    courseCode: row.courseCode,
    description: row.description || '',
    category: row.category || '',
    difficulty: row.difficulty || 2,
    status: row.status,
    teacherId: row.teacherId
  }
  dialogVisible.value = true
}

const resetForm = () => {
  isEdit.value = false
  editingCourseId.value = null
  form.value = {
    courseName: '',
    courseCode: '',
    description: '',
    category: '',
    difficulty: 2,
    status: 1,
    teacherId: userStore.userInfo.userId
  }
}

const toggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateCourse(row.id, {
      ...row,
      status: newStatus
    })
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
    fetchCourses()
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('操作失败')
  }
}

const viewDetail = (row) => {
  ElMessageBox.alert(
    `课程名称：${row.courseName}\n` +
    `课程编码：${row.courseCode}\n` +
    `分类：${row.category || '未分类'}\n` +
    `难度：${row.difficulty === 1 ? '简单' : row.difficulty === 3 ? '困难' : '中等'}\n` +
    `描述：${row.description || '无描述'}\n` +
    `状态：${getStatusText(row.status)}`,
    '课程详情',
    {
      confirmButtonText: '关闭',
      type: 'info'
    }
  )
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该课程吗？删除后无法恢复！', '警告', { 
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    fetchCourses()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除课程失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.teach {
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

:deep(.el-table) {
  margin-top: 20px;
}

:deep(.el-button) {
  margin: 0 4px;
}
</style>

