/**
 * 路由配置
 */
import { createRouter, createWebHistory } from 'vue-router'
import { initService } from '../services/initService'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/setup',
      name: 'Setup',
      component: () => import('../pages/setup/index.vue'),
      meta: {
        title: '数据库配置',
        requiresInit: false, // 配置页面不需要检查初始化状态
      },
    },
    {
      path: '/init',
      name: 'Init',
      component: () => import('../pages/init/index.vue'),
      meta: {
        title: '系统初始化',
        requiresInit: false, // 初始化页面不需要检查初始化状态
      },
    },
    {
      path: '/',
      name: 'Home',
      redirect: '/dashboard',
      meta: {
        requiresInit: true, // 需要系统已初始化
      },
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('../pages/dashboard/index.vue'),
      meta: {
        title: '仪表盘',
        requiresInit: true,
      },
    },
    {
      path: '/error',
      name: 'Error',
      component: () => import('../pages/error/index.vue'),
      meta: {
        title: '错误页面',
        requiresInit: false,
      },
    },
  ],
})

// 路由守卫：检查系统初始化状态
router.beforeEach(async (to, from, next) => {
  // 如果目标路由不需要检查初始化状态（如配置页面、初始化页面、错误页面），直接放行
  if (to.meta.requiresInit === false || to.path === '/error' || to.path === '/setup') {
    next()
    return
  }

  // 如果从错误页面跳转到配置页面或初始化页面，直接放行（避免循环）
  if (from.path === '/error' && (to.path === '/setup' || to.path === '/init')) {
    next()
    return
  }

  try {
    // 先检查是否已配置数据库
    const { setupService } = await import('../services/setupService')
    const configured = await setupService.checkSetupStatus()
    
    if (!configured) {
      // 未配置数据库，跳转到配置页面
      if (to.path !== '/setup') {
        ElMessage.info('请先配置数据库连接信息')
        next('/setup')
        return
      }
    }

    // 检查系统是否已初始化
    const initialized = await initService.checkStatus()

    if (!initialized) {
      // 未初始化，跳转到初始化页面
      if (to.path !== '/init' && to.path !== '/setup') {
        ElMessage.warning('系统未初始化，请先创建超级管理员')
        next('/init')
        return
      }
    } else {
      // 已初始化，如果访问初始化页面或配置页面，重定向到首页
      if (to.path === '/init' || to.path === '/setup') {
        next('/')
        return
      }
    }

    next()
  } catch (error: any) {
    // 检查失败，可能是网络错误或服务未启动
    console.error('检查状态失败:', error)
    
    // 如果目标页面是配置页面或初始化页面，允许访问（这些页面会自己处理错误）
    if (to.path === '/setup' || to.path === '/init') {
      next()
      return
    }
    
    // 判断错误类型
    const isNetworkError = 
      error.code === 'ECONNREFUSED' || 
      error.message?.includes('Network Error') ||
      error.message?.includes('ECONNREFUSED') ||
      !error.response

    if (isNetworkError) {
      // 网络错误，跳转到错误页面，显示真实错误信息
      const errorMessage = error.message || '无法连接到后端服务器（端口 8080）'
      next({
        path: '/error',
        query: {
          type: 'network',
          message: `连接被拒绝：${errorMessage}\n\n请检查：\n1. 后端服务是否已启动\n2. 服务是否运行在端口 8080\n3. 防火墙设置是否正确`
        }
      })
      return
    }

    // 其他错误（如服务器返回错误响应），也跳转到错误页面
    const errorMessage = error.response?.data?.message || error.message || '服务器返回了错误响应'
    next({
      path: '/error',
      query: {
        type: 'server',
        message: errorMessage
      }
    })
  }
})

export default router
