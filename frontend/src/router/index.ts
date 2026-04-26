import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Documents from '../views/Documents.vue'
import RagChat from '../views/RagChat.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    {
      path: '/',
      component: Home,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/documents' },
        { path: 'documents', component: Documents },
        { path: 'rag', component: RagChat },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return '/login'
  }
})

export default router
