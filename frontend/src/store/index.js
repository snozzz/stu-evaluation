import Vue from 'vue'
import Vuex from 'vuex'
import { login as loginApi, getUserInfo as getUserInfoApi } from '../api'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    role: localStorage.getItem('role') || ''
  },

  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },

    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      state.role = userInfo.role || ''
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      localStorage.setItem('role', userInfo.role || '')
    },

    LOGOUT(state) {
      state.token = ''
      state.userInfo = {}
      state.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('role')
    }
  },

  actions: {
    login({ commit }, loginForm) {
      return new Promise((resolve, reject) => {
        loginApi(loginForm).then(res => {
          const data = res.data.data
          commit('SET_TOKEN', data.token)
          commit('SET_USER_INFO', data.userInfo)
          resolve(data)
        }).catch(err => {
          reject(err)
        })
      })
    },

    getUserInfo({ commit }) {
      return new Promise((resolve, reject) => {
        getUserInfoApi().then(res => {
          const data = res.data.data
          commit('SET_USER_INFO', data)
          resolve(data)
        }).catch(err => {
          reject(err)
        })
      })
    },

    logout({ commit }) {
      commit('LOGOUT')
    }
  },

  getters: {
    isLoggedIn: state => !!state.token,
    isAdmin: state => state.role === 'ADMIN',
    isTeacher: state => state.role === 'TEACHER',
    isStudent: state => state.role === 'STUDENT',
    userRole: state => state.role,
    userName: state => state.userInfo.name || state.userInfo.username || ''
  }
})
