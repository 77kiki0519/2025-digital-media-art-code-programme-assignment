<template>
  <div class="profile">
    <el-card>
      <template #header>
        <div class="header">
          <span>个人信息</span>
          <el-button v-if="!editing" type="primary" size="small" @click="startEdit">编辑</el-button>
        </div>
      </template>
      
      <!-- 查看模式 -->
      <el-descriptions v-if="!editing" :column="2" border>
        <el-descriptions-item label="用户名">
          {{ userStore.userInfo.username }}
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名">
          {{ userStore.userInfo.realName }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ userStore.userInfo.email || '未设置' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ userStore.userInfo.phone || '未设置' }}
        </el-descriptions-item>
        <el-descriptions-item label="学号" v-if="userStore.userInfo.studentNo">
          {{ userStore.userInfo.studentNo }}
        </el-descriptions-item>
        <el-descriptions-item label="工号" v-if="userStore.userInfo.teacherNo">
          {{ userStore.userInfo.teacherNo }}
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-for="role in userStore.userInfo.roles" :key="role.id" style="margin-right: 8px;">
            {{ role.roleName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ formatDate(userStore.userInfo.createdAt) }}
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 编辑模式 -->
      <el-form v-else :model="editForm" label-width="100px" style="max-width: 500px;">
        <el-form-item label="用户名">
          <el-input :value="userStore.userInfo.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 修改密码 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>修改密码</span>
      </template>
      
      <el-form :model="passwordForm" label-width="100px" style="max-width: 500px;">
        <el-form-item label="当前密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="changePassword" :loading="changingPwd">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const editing = ref(false)
const saving = ref(false)
const changingPwd = ref(false)

const editForm = ref({
  realName: '',
  email: '',
  phone: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const formatDate = (date) => {
  if (!date) return '未知'
  return new Date(date).toLocaleDateString('zh-CN')
}

const startEdit = () => {
  editForm.value = {
    realName: userStore.userInfo.realName || '',
    email: userStore.userInfo.email || '',
    phone: userStore.userInfo.phone || ''
  }
  editing.value = true
}

const cancelEdit = () => {
  editing.value = false
}

const saveProfile = async () => {
  saving.value = true
  try {
    // 更新本地状态（实际应该调用API）
    userStore.userInfo.realName = editForm.value.realName
    userStore.userInfo.email = editForm.value.email
    userStore.userInfo.phone = editForm.value.phone
    
    ElMessage.success('保存成功')
    editing.value = false
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  if (!passwordForm.value.oldPassword) {
    ElMessage.warning('请输入当前密码')
    return
  }
  if (!passwordForm.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码至少6个字符')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  changingPwd.value = true
  try {
    // 实际应该调用API修改密码
    ElMessage.success('密码修改成功，请重新登录')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    changingPwd.value = false
  }
}
</script>

<style scoped>
.profile {
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

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
  width: 100px;
}

:deep(.el-descriptions__content) {
  color: #303133;
}
</style>
