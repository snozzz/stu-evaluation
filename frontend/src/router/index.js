import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '../store'

Vue.use(VueRouter)

// 解决重复点击相同路由报 NavigationDuplicated 错误
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/common/Login.vue'),
    meta: { title: '\u767B\u5F55' }
  },
  {
    path: '/',
    redirect: () => {
      const role = store.state.role
      if (role === 'ADMIN') return '/admin/dashboard'
      if (role === 'TEACHER') return '/teacher/dashboard'
      if (role === 'STUDENT') return '/student/dashboard'
      return '/login'
    }
  },
  // Admin routes
  {
    path: '/admin',
    component: () => import('../components/AdminLayout.vue'),
    meta: { title: '\u7BA1\u7406\u540E\u53F0', role: 'ADMIN' },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '\u63A7\u5236\u53F0', role: 'ADMIN' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('../views/admin/UserManage.vue'),
        meta: { title: '\u7528\u6237\u7BA1\u7406', role: 'ADMIN' }
      },
      {
        path: 'courses',
        name: 'CourseManage',
        component: () => import('../views/admin/CourseManage.vue'),
        meta: { title: '\u8BFE\u7A0B\u7BA1\u7406', role: 'ADMIN' }
      },
      {
        path: 'classes',
        name: 'ClassManage',
        component: () => import('../views/admin/ClassManage.vue'),
        meta: { title: '\u73ED\u7EA7\u7BA1\u7406', role: 'ADMIN' }
      },
      {
        path: 'dimensions',
        name: 'DimensionManage',
        component: () => import('../views/admin/DimensionManage.vue'),
        meta: { title: '\u8BC4\u4EF7\u7EF4\u5EA6', role: 'ADMIN' }
      },
      {
        path: 'semesters',
        name: 'SemesterManage',
        component: () => import('../views/admin/SemesterManage.vue'),
        meta: { title: '\u5B66\u671F\u7BA1\u7406', role: 'ADMIN' }
      },
      {
        path: 'announcements',
        name: 'AnnouncementManage',
        component: () => import('../views/admin/AnnouncementManage.vue'),
        meta: { title: '\u516C\u544A\u7BA1\u7406', role: 'ADMIN' }
      },
      {
        path: 'carousels',
        name: 'CarouselManage',
        component: () => import('../views/admin/CarouselManage.vue'),
        meta: { title: '\u8F6E\u64AD\u56FE\u7BA1\u7406', role: 'ADMIN' }
      },
      {
        path: 'configs',
        name: 'ConfigManage',
        component: () => import('../views/admin/ConfigManage.vue'),
        meta: { title: '\u7CFB\u7EDF\u914D\u7F6E', role: 'ADMIN' }
      },
      {
        path: 'alerts',
        name: 'AlertManage',
        component: () => import('../views/admin/AlertManage.vue'),
        meta: { title: '\u9884\u8B66\u7BA1\u7406', role: 'ADMIN' }
      },
      {
        path: 'weights',
        name: 'WeightManage',
        component: () => import('../views/admin/WeightManage.vue'),
        meta: { title: '\u6743\u91CD\u7BA1\u7406', role: 'ADMIN' }
      },
      {
        path: 'bindings',
        name: 'TeacherBindings',
        component: () => import('../views/admin/TeacherBinding.vue'),
        meta: { title: '\u6743\u9650\u5206\u914D', role: 'ADMIN' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/admin/Profile.vue'),
        meta: { title: '个人中心', role: 'ADMIN' }
      }
    ]
  },
  // Teacher routes
  {
    path: '/teacher',
    component: () => import('../components/TeacherLayout.vue'),
    meta: { title: '\u6559\u5E08\u7AEF', role: 'TEACHER' },
    redirect: '/teacher/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('../views/teacher/Dashboard.vue'),
        meta: { title: '\u6559\u5E08\u9996\u9875', role: 'TEACHER' }
      },
      {
        path: 'bindCourse',
        name: 'BindCourse',
        component: () => import('../views/teacher/BindCourse.vue'),
        meta: { title: '\u6388\u8BFE\u7BA1\u7406', role: 'TEACHER' }
      },
      {
        path: 'scores',
        name: 'TeacherScores',
        component: () => import('../views/teacher/Scores.vue'),
        meta: { title: '\u6210\u7EE9\u7BA1\u7406', role: 'TEACHER' }
      },
      {
        path: 'assignments',
        name: 'TeacherAssignments',
        component: () => import('../views/teacher/Assignments.vue'),
        meta: { title: '作业与实验', role: 'TEACHER' }
      },
      {
        path: 'comments',
        name: 'CommentManage',
        component: () => import('../views/teacher/Comments.vue'),
        meta: { title: '\u8BC4\u8BED\u7BA1\u7406', role: 'TEACHER' }
      },
      {
        path: 'appeals',
        name: 'TeacherAppeals',
        component: () => import('../views/teacher/Appeals.vue'),
        meta: { title: '\u7533\u8BC9\u5904\u7406', role: 'TEACHER' }
      },
      {
        path: 'report',
        name: 'TeacherReport',
        component: () => import('../views/teacher/Report.vue'),
        meta: { title: '\u53EF\u89C6\u5316\u62A5\u8868', role: 'TEACHER' }
      },
      {
        path: 'profile',
        name: 'TeacherProfile',
        component: () => import('../views/teacher/Profile.vue'),
        meta: { title: '\u4E2A\u4EBA\u4FE1\u606F', role: 'TEACHER' }
      }
    ]
  },
  // Student routes
  {
    path: '/student',
    component: () => import('../components/StudentLayout.vue'),
    meta: { title: '\u5B66\u751F\u7AEF', role: 'STUDENT' },
    redirect: '/student/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('../views/student/Dashboard.vue'),
        meta: { title: '\u5B66\u751F\u9996\u9875', role: 'STUDENT' }
      },
      {
        path: 'scores',
        name: 'StudentScores',
        component: () => import('../views/student/Scores.vue'),
        meta: { title: '\u6211\u7684\u6210\u7EE9', role: 'STUDENT' }
      },
      {
        path: 'assignments',
        name: 'StudentAssignments',
        component: () => import('../views/student/Assignments.vue'),
        meta: { title: '作业与实验', role: 'STUDENT' }
      },
      {
        path: 'selfEval',
        name: 'SelfEval',
        component: () => import('../views/student/SelfEval.vue'),
        meta: { title: '\u81EA\u8BC4\u7BA1\u7406', role: 'STUDENT' }
      },
      {
        path: 'appeals',
        name: 'StudentAppeals',
        component: () => import('../views/student/Appeals.vue'),
        meta: { title: '\u6211\u7684\u7533\u8BC9', role: 'STUDENT' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('../views/student/Profile.vue'),
        meta: { title: '\u4E2A\u4EBA\u4FE1\u606F', role: 'STUDENT' }
      }
    ]
  }
]

const router = new VueRouter({
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  // Set page title
  if (to.meta && to.meta.title) {
    document.title = to.meta.title + ' - \u5B66\u751F\u5B66\u4E1A\u8BC4\u4EF7\u7CFB\u7EDF'
  }

  const token = store.state.token
  const role = store.state.role

  // Allow access to login page without authentication
  if (to.path === '/login') {
    if (token) {
      // Already logged in, redirect to role-specific home
      if (role === 'ADMIN') return next('/admin/dashboard')
      if (role === 'TEACHER') return next('/teacher/dashboard')
      if (role === 'STUDENT') return next('/student/dashboard')
    }
    return next()
  }

  // Check authentication
  if (!token) {
    return next('/login')
  }

  // Check role-based access
  if (to.meta && to.meta.role) {
    if (to.meta.role !== role) {
      // Redirect to the correct role home
      if (role === 'ADMIN') return next('/admin/dashboard')
      if (role === 'TEACHER') return next('/teacher/dashboard')
      if (role === 'STUDENT') return next('/student/dashboard')
      return next('/login')
    }
  }

  next()
})

export default router
