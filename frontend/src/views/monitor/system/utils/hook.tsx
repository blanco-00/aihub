import { message } from "@/utils/message";
import {
  getSystemMonitorInfo,
  type SystemMonitorInfo,
} from "@/api/systemMonitor";
import { ref, onMounted, onUnmounted } from "vue";

export function useSystemMonitor() {
  const loading = ref(false);
  const monitorInfo = ref<SystemMonitorInfo | null>(null);
  let refreshTimer: NodeJS.Timeout | null = null;

  /**
   * 格式化字节大小
   */
  function formatBytes(bytes: number): string {
    if (bytes === 0) return "0 B";
    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB", "TB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
  }

  /**
   * 格式化时间（秒转可读格式）
   */
  function formatUptime(seconds: number): string {
    const days = Math.floor(seconds / 86400);
    const hours = Math.floor((seconds % 86400) / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    if (days > 0) {
      return `${days}天 ${hours}小时 ${minutes}分钟`;
    } else if (hours > 0) {
      return `${hours}小时 ${minutes}分钟`;
    } else if (minutes > 0) {
      return `${minutes}分钟 ${secs}秒`;
    } else {
      return `${secs}秒`;
    }
  }

  /**
   * 获取监控信息
   */
  async function fetchMonitorInfo() {
    loading.value = true;
    try {
      const { code, data } = await getSystemMonitorInfo();
      if (code === 200 && data) {
        monitorInfo.value = data;
      } else {
        message("获取系统监控信息失败", {
          type: "error",
        });
      }
    } catch (error: any) {
      console.error("[系统监控] 请求失败", error);
      message(error?.message || "获取系统监控信息失败", {
        type: "error",
      });
    } finally {
      loading.value = false;
    }
  }

  /**
   * 开始自动刷新
   * 性能优化：后端监控数据缓存5秒，刷新间隔不少于5秒，避免不必要的性能开销
   */
  function startAutoRefresh(interval: number = 5000) {
    if (refreshTimer) {
      clearInterval(refreshTimer);
    }
    // 确保刷新间隔不少于5秒（后端缓存时间）
    const minInterval = 5000;
    const actualInterval = Math.max(interval, minInterval);
    refreshTimer = setInterval(() => {
      fetchMonitorInfo();
    }, actualInterval);
  }

  /**
   * 停止自动刷新
   */
  function stopAutoRefresh() {
    if (refreshTimer) {
      clearInterval(refreshTimer);
      refreshTimer = null;
    }
  }

  onMounted(() => {
    fetchMonitorInfo();
    // 每5秒自动刷新一次
    startAutoRefresh(5000);
  });

  onUnmounted(() => {
    stopAutoRefresh();
  });

  return {
    loading,
    monitorInfo,
    formatBytes,
    formatUptime,
    fetchMonitorInfo,
    startAutoRefresh,
    stopAutoRefresh,
  };
}
