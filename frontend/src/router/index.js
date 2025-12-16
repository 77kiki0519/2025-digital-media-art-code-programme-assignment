import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('@/views/Courses.vue')
      },
      {
        path: 'courses/:id',
        name: 'CourseDetail',
        component: () => import('@/views/CourseDetail.vue')
      },
      {
        path: 'my-courses',
        name: 'MyCourses',
        component: () => import('@/views/MyCourses.vue'),
        meta: { role: 'STUDENT' }
      },
      {
        path: 'teach',
        name: 'Teach',
        component: () => import('@/views/Teach.vue'),
        meta: { role: 'TEACHER' }
      },
      {
        path: 'material/create',
        name: 'MaterialCreate',
        component: () => import('@/views/MaterialCreate.vue'),
        meta: { role: 'TEACHER' }
      },
      {
        path: 'exams',
        name: 'Exams',
        component: () => import('@/views/Exams.vue')
      },
      {
        path: 'exams/:id',
        name: 'ExamDetail',
        component: () => import('@/views/ExamDetail.vue')
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/Reports.vue')
      },
      {
        path: 'qa',
        name: 'QA',
        component: () => import('@/views/QA.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth !== false && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router


