<template>
  <el-container class="main-layout">
    <el-header>
      <div class="header-content">
        <div class="logo">
          <el-icon><School /></el-icon>
          <span>AI线上课程系统</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/courses">课程</el-menu-item>
          <el-menu-item v-if="userStore.isStudent" index="/my-courses">我的课程</el-menu-item>
          <el-menu-item v-if="userStore.isTeacher" index="/teach">我的教学</el-menu-item>
          <el-menu-item index="/exams">考试</el-menu-item>
          <el-menu-item index="/reports">报告</el-menu-item>
          <el-menu-item index="/qa">智能答疑</el-menu-item>
        </el-menu>
        
        <div class="user-info">
          <el-dropdown @command="handleCommand">
            <span class="user-name">
              {{ userStore.userInfo.realName }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleMenuSelect = (index) => {
  router.push(index)
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      })
      userStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
    } catch {}
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.el-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 0;
  z-index: 1000;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 30px;
  max-width: 1400px;
  margin: 0 auto;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 22px;
  font-weight: 700;
  color: #409eff;
  margin-right: 40px;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

.logo .el-icon {
  font-size: 28px;
  margin-right: 10px;
}

.el-menu {
  flex: 1;
  border: none;
}

.user-info {
  margin-left: 20px;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s;
  color: #606266;
  font-weight: 500;
}

.user-name:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

.el-main {
  background: #f5f7fa;
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}
</style>

