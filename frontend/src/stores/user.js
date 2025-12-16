import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  
  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => userInfo.value.id || userInfo.value.userId)
  const isTeacher = computed(() => {
    return userInfo.value.roles?.some(role => role.roleCode === 'TEACHER')
  })
  const isStudent = computed(() => {
    return userInfo.value.roles?.some(role => role.roleCode === 'STUDENT')
  })
  
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }
  
  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
  
  return {
    token,
    userInfo,
    userId,
    isLoggedIn,
    isTeacher,
    isStudent,
    setToken,
    setUserInfo,
    logout
  }
})


