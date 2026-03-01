import axios from 'axios'
import { Message } from 'element-ui'
import store from '../store'
import router from '../router'

const request = axios.create({
  baseURL: '',
  timeout: 30000
})

request.interceptors.request.use(config => {
  const token = store.state.token
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      Message.error(res.message || '\u8BF7\u6C42\u5931\u8D25')
      return Promise.reject(res)
    }
    return response
  },
  error => {
    if (error.response && error.response.status === 401) {
      store.commit('LOGOUT')
      router.push('/login')
    }
    Message.error(error.message || '\u7F51\u7EDC\u9519\u8BEF')
    return Promise.reject(error)
  }
)

// Export axios instance as default
export default request

// Auth
export const login = data => request.post('/api/auth/login', data)
export const register = data => request.post('/api/auth/register', data)

// User
export const getUserInfo = () => request.get('/api/user/info')
export const updateProfile = data => request.put('/api/user/profile', data)
export const changePassword = data => request.put('/api/user/password', data)
export const getUserList = params => request.get('/api/user/list', { params })
export const addUser = data => request.post('/api/user/add', data)
export const deleteUser = id => request.delete(`/api/user/${id}`)
export const updateUser = (id, data) => request.put(`/api/user/${id}`, data)

// Course
export const getCourseList = params => request.get('/api/course/list', { params })
export const addCourse = data => request.post('/api/course/add', data)
export const updateCourse = (id, data) => request.put(`/api/course/${id}`, data)
export const deleteCourse = id => request.delete(`/api/course/${id}`)

// Class
export const getClassList = params => request.get('/api/class/list', { params })
export const addClass = data => request.post('/api/class/add', data)
export const updateClass = (id, data) => request.put(`/api/class/${id}`, data)
export const deleteClass = id => request.delete(`/api/class/${id}`)
export const getStudentsByClass = classId => request.get(`/api/class/${classId}/students`)

// Teacher Course Class binding
export const getBindings = params => request.get('/api/bindCourse/list', { params })
export const addBinding = data => request.post('/api/bindCourse/add', data)
export const deleteBinding = id => request.delete(`/api/bindCourse/${id}`)

// Eval Dimension
export const getDimensions = () => request.get('/api/dimension/list')
export const addDimension = data => request.post('/api/dimension/add', data)
export const updateDimension = (id, data) => request.put(`/api/dimension/${id}`, data)
export const deleteDimension = id => request.delete(`/api/dimension/${id}`)

// Eval Weight
export const getWeights = courseId => request.get(`/api/weight/list/${courseId}`)
export const saveWeights = data => request.post('/api/weight/save', data)

// Scores
export const getStudentScores = params => request.get('/api/score/student', { params })
export const getClassScores = params => request.get('/api/score/class', { params })
export const saveScore = data => request.post('/api/score/save', data)
export const saveBatchScores = data => request.post('/api/score/batch', data)
export const importScores = (formData) => request.post('/api/score/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const getWeightedScore = (studentId, courseId) => request.get(`/api/score/weighted/${studentId}/${courseId}`)

// Comments
export const getComments = params => request.get('/api/comment/list', { params })
export const saveComment = data => request.post('/api/comment/save', data)
export const generateAIComment = params => request.post('/api/comment/ai-generate', null, { params })
export const publishComment = id => request.put(`/api/comment/publish/${id}`)

// Self Evaluation
export const getSelfEvals = params => request.get('/api/selfEval/list', { params })
export const saveSelfEval = data => request.post('/api/selfEval/save', data)
export const saveBatchSelfEvals = data => request.post('/api/selfEval/batch', data)

// Appeal
export const getStudentAppeals = () => request.get('/api/appeal/student')
export const getTeacherAppeals = () => request.get('/api/appeal/teacher')
export const submitAppeal = data => request.post('/api/appeal/submit', data)
export const replyAppeal = (id, data) => request.put(`/api/appeal/reply/${id}`, data)

// Announcement
export const getAnnouncements = params => request.get('/api/announcement/list', { params })
export const addAnnouncement = data => request.post('/api/announcement/add', data)
export const updateAnnouncement = (id, data) => request.put(`/api/announcement/${id}`, data)
export const deleteAnnouncement = id => request.delete(`/api/announcement/${id}`)

// Carousel
export const getCarousels = () => request.get('/api/carousel/list')
export const addCarousel = data => request.post('/api/carousel/add', data)
export const updateCarousel = (id, data) => request.put(`/api/carousel/${id}`, data)
export const deleteCarousel = id => request.delete(`/api/carousel/${id}`)

// Alert
export const getAlertRules = () => request.get('/api/alert/rules')
export const addAlertRule = data => request.post('/api/alert/rule', data)
export const updateAlertRule = (id, data) => request.put(`/api/alert/rule/${id}`, data)
export const deleteAlertRule = id => request.delete(`/api/alert/rule/${id}`)
export const getAlertRecords = () => request.get('/api/alert/records')
export const markAlertRead = id => request.put(`/api/alert/record/read/${id}`)

// Config
export const getConfigs = () => request.get('/api/config/list')
export const getConfig = key => request.get(`/api/config/${key}`)
export const updateConfig = data => request.put('/api/config/update', data)

// Semester
export const getSemesters = () => request.get('/api/semester/list')
export const getCurrentSemester = () => request.get('/api/semester/current')
export const addSemester = data => request.post('/api/semester/add', data)
export const updateSemester = (id, data) => request.put(`/api/semester/${id}`, data)
export const setCurrentSemester = id => request.put(`/api/semester/setCurrent/${id}`)

// Dashboard
export const getDashboardStats = () => request.get('/api/dashboard/stats')

// File upload
export const uploadFile = formData => request.post('/api/file/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })

// Aliases for admin views compatibility
export const getDimensionList = getDimensions
export const getSemesterList = getSemesters
export const getCarouselList = getCarousels
