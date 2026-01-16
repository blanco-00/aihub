<script setup lang="ts">
import { ref } from "vue";
import { useSystemMonitor } from "./utils/hook";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { ElProgress } from "element-plus";

import Refresh from "~icons/ep/refresh";

defineOptions({
  name: "SystemMonitor"
});

const autoRefresh = ref(true);
const {
  loading,
  monitorInfo,
  formatBytes,
  formatUptime,
  fetchMonitorInfo,
  startAutoRefresh,
  stopAutoRefresh
} = useSystemMonitor();

// 监听自动刷新开关
// 性能优化：刷新间隔设置为5秒，与后端缓存时间一致
const handleAutoRefreshChange = (val: boolean) => {
  if (val) {
    startAutoRefresh(5000); // 5秒刷新一次，与后端缓存时间一致
  } else {
    stopAutoRefresh();
  }
};

// 计算状态颜色
const getStatusColor = (status: string) => {
  if (status === "正常") return "success";
  if (status === "未配置") return "info";
  return "danger";
};
</script>

<template>
  <div class="main">
    <div class="mb-4 flex justify-between items-center">
      <h2 class="text-lg font-semibold">运行状况</h2>
      <div class="flex items-center gap-4">
        <el-switch
          v-model="autoRefresh"
          active-text="自动刷新"
          inactive-text="手动刷新"
          @change="handleAutoRefreshChange"
        />
        <el-button
          type="primary"
          :icon="useRenderIcon('ri:refresh-line')"
          :loading="loading"
          @click="fetchMonitorInfo"
        >
          刷新
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- 主机信息 -->
      <el-card shadow="hover" class="monitor-card">
        <template #header>
          <div class="flex items-center gap-2">
            <component :is="useRenderIcon('ep:monitor')" class="w-[18px] h-[18px]" />
            <span class="font-semibold">主机信息</span>
          </div>
        </template>
        <div v-if="monitorInfo?.host" class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <div class="text-sm text-gray-500 mb-1">操作系统</div>
              <div class="font-medium">{{ monitorInfo.host.osName }} {{ monitorInfo.host.osVersion }}</div>
            </div>
            <div>
              <div class="text-sm text-gray-500 mb-1">系统架构</div>
              <div class="font-medium">{{ monitorInfo.host.osArch }}</div>
            </div>
            <div>
              <div class="text-sm text-gray-500 mb-1">主机名</div>
              <div class="font-medium">{{ monitorInfo.host.hostName }}</div>
            </div>
            <div>
              <div class="text-sm text-gray-500 mb-1">IP地址</div>
              <div class="font-medium">{{ monitorInfo.host.ip }}</div>
            </div>
            <div>
              <div class="text-sm text-gray-500 mb-1">运行时间</div>
              <div class="font-medium">{{ formatUptime(monitorInfo.host.uptime) }}</div>
            </div>
          </div>
          
          <el-divider />
          
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium">CPU使用率</span>
              <span class="text-sm">{{ monitorInfo.host.cpuUsage }}%</span>
            </div>
            <el-progress
              :percentage="monitorInfo.host.cpuUsage"
              :color="monitorInfo.host.cpuUsage > 80 ? '#f56c6c' : monitorInfo.host.cpuUsage > 60 ? '#e6a23c' : '#67c23a'"
            />
            <div class="text-xs text-gray-500 mt-1">CPU核心数: {{ monitorInfo.host.cpuCores }}</div>
          </div>
          
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium">内存使用率</span>
              <span class="text-sm">{{ monitorInfo.host.memoryUsage }}%</span>
            </div>
            <el-progress
              :percentage="monitorInfo.host.memoryUsage"
              :color="monitorInfo.host.memoryUsage > 80 ? '#f56c6c' : monitorInfo.host.memoryUsage > 60 ? '#e6a23c' : '#67c23a'"
            />
            <div class="text-xs text-gray-500 mt-1">
              已用: {{ formatBytes(monitorInfo.host.usedMemory) }} / 
              总计: {{ formatBytes(monitorInfo.host.totalMemory) }}
            </div>
          </div>
          
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium">磁盘使用率</span>
              <span class="text-sm">{{ monitorInfo.host.diskUsage }}%</span>
            </div>
            <el-progress
              :percentage="monitorInfo.host.diskUsage"
              :color="monitorInfo.host.diskUsage > 80 ? '#f56c6c' : monitorInfo.host.diskUsage > 60 ? '#e6a23c' : '#67c23a'"
            />
            <div class="text-xs text-gray-500 mt-1">
              已用: {{ formatBytes(monitorInfo.host.usedDisk) }} / 
              总计: {{ formatBytes(monitorInfo.host.totalDisk) }}
            </div>
          </div>
        </div>
        <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
      </el-card>

      <!-- JVM信息 -->
      <el-card shadow="hover" class="monitor-card">
        <template #header>
          <div class="flex items-center gap-2">
            <component :is="useRenderIcon('ep:cpu')" class="w-[18px] h-[18px]" />
            <span class="font-semibold">JVM信息</span>
          </div>
        </template>
        <div v-if="monitorInfo?.jvm" class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <div class="text-sm text-gray-500 mb-1">JVM名称</div>
              <div class="font-medium text-sm">{{ monitorInfo.jvm.jvmName }}</div>
            </div>
            <div>
              <div class="text-sm text-gray-500 mb-1">Java版本</div>
              <div class="font-medium text-sm">{{ monitorInfo.jvm.javaVersion }}</div>
            </div>
            <div>
              <div class="text-sm text-gray-500 mb-1">线程数</div>
              <div class="font-medium">{{ monitorInfo.jvm.threadCount }}</div>
            </div>
            <div>
              <div class="text-sm text-gray-500 mb-1">GC次数</div>
              <div class="font-medium">{{ monitorInfo.jvm.gcCount }}</div>
            </div>
          </div>
          
          <el-divider />
          
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium">堆内存使用率</span>
              <span class="text-sm">{{ monitorInfo.jvm.heapUsage }}%</span>
            </div>
            <el-progress
              :percentage="monitorInfo.jvm.heapUsage"
              :color="monitorInfo.jvm.heapUsage > 80 ? '#f56c6c' : monitorInfo.jvm.heapUsage > 60 ? '#e6a23c' : '#67c23a'"
            />
            <div class="text-xs text-gray-500 mt-1">
              已用: {{ formatBytes(monitorInfo.jvm.heapUsed) }} / 
              总计: {{ formatBytes(monitorInfo.jvm.heapTotal) }}
            </div>
          </div>
          
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium">非堆内存使用率</span>
              <span class="text-sm">{{ monitorInfo.jvm.nonHeapUsage }}%</span>
            </div>
            <el-progress
              :percentage="monitorInfo.jvm.nonHeapUsage"
              :color="monitorInfo.jvm.nonHeapUsage > 80 ? '#f56c6c' : monitorInfo.jvm.nonHeapUsage > 60 ? '#e6a23c' : '#67c23a'"
            />
            <div class="text-xs text-gray-500 mt-1">
              已用: {{ formatBytes(monitorInfo.jvm.nonHeapUsed) }} / 
              总计: {{ formatBytes(monitorInfo.jvm.nonHeapTotal) }}
            </div>
          </div>
        </div>
        <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
      </el-card>

      <!-- MySQL信息 -->
      <el-card shadow="hover" class="monitor-card">
        <template #header>
          <div class="flex items-center gap-2">
            <component :is="useRenderIcon('ep:data-base')" class="w-[18px] h-[18px]" />
            <span class="font-semibold">MySQL数据库</span>
          </div>
        </template>
        <div v-if="monitorInfo?.mysql" class="space-y-4">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-sm text-gray-500 mb-1">状态</div>
              <el-tag :type="getStatusColor(monitorInfo.mysql.status)">
                {{ monitorInfo.mysql.status }}
              </el-tag>
            </div>
            <div>
              <div class="text-sm text-gray-500 mb-1">版本</div>
              <div class="font-medium">{{ monitorInfo.mysql.version || "未知" }}</div>
            </div>
          </div>
          
          <el-divider />
          
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium">连接池使用率</span>
              <span class="text-sm">{{ monitorInfo.mysql.connectionUsage }}%</span>
            </div>
            <el-progress
              :percentage="monitorInfo.mysql.connectionUsage"
              :color="monitorInfo.mysql.connectionUsage > 80 ? '#f56c6c' : monitorInfo.mysql.connectionUsage > 60 ? '#e6a23c' : '#67c23a'"
            />
            <div class="text-xs text-gray-500 mt-1">
              活跃: {{ monitorInfo.mysql.activeConnections }} / 
              最大: {{ monitorInfo.mysql.maxConnections }} / 
              空闲: {{ monitorInfo.mysql.idleConnections }}
            </div>
          </div>
        </div>
        <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
      </el-card>

      <!-- Redis信息 -->
      <el-card shadow="hover" class="monitor-card">
        <template #header>
          <div class="flex items-center gap-2">
            <component :is="useRenderIcon('ep:connection')" class="w-[18px] h-[18px]" />
            <span class="font-semibold">Redis缓存</span>
          </div>
        </template>
        <div v-if="monitorInfo?.redis" class="space-y-4">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-sm text-gray-500 mb-1">状态</div>
              <el-tag :type="getStatusColor(monitorInfo.redis.status)">
                {{ monitorInfo.redis.status }}
              </el-tag>
            </div>
            <div>
              <div class="text-sm text-gray-500 mb-1">版本</div>
              <div class="font-medium">{{ monitorInfo.redis.version || "未知" }}</div>
            </div>
          </div>
          
          <el-divider />
          
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium">内存使用率</span>
              <span class="text-sm">{{ monitorInfo.redis.memoryUsage }}%</span>
            </div>
            <el-progress
              v-if="monitorInfo.redis.memoryUsage > 0"
              :percentage="monitorInfo.redis.memoryUsage"
              :color="monitorInfo.redis.memoryUsage > 80 ? '#f56c6c' : monitorInfo.redis.memoryUsage > 60 ? '#e6a23c' : '#67c23a'"
            />
            <div class="text-xs text-gray-500 mt-1">
              已用: {{ formatBytes(monitorInfo.redis.usedMemory) }}
              <span v-if="monitorInfo.redis.maxMemory > 0">
                / 最大: {{ formatBytes(monitorInfo.redis.maxMemory) }}
              </span>
            </div>
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <div class="text-sm text-gray-500 mb-1">连接客户端</div>
              <div class="font-medium">{{ monitorInfo.redis.connectedClients }}</div>
            </div>
            <div>
              <div class="text-sm text-gray-500 mb-1">键数量</div>
              <div class="font-medium">{{ monitorInfo.redis.totalKeys }}</div>
            </div>
          </div>
        </div>
        <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
      </el-card>
    </div>
  </div>
</template>

<style scoped lang="scss">
.main {
  padding: 24px;
}

.monitor-card {
  min-height: 400px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color);
}

:deep(.el-card__body) {
  padding: 20px;
}

.grid {
  display: grid;
}

.grid-cols-1 {
  grid-template-columns: repeat(1, minmax(0, 1fr));
}

.grid-cols-2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.gap-4 {
  gap: 1rem;
}

@media (min-width: 768px) {
  .md\:grid-cols-2 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.space-y-4 > * + * {
  margin-top: 1rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.mb-1 {
  margin-bottom: 0.25rem;
}

.mt-1 {
  margin-top: 0.25rem;
}

.flex {
  display: flex;
}

.justify-between {
  justify-content: space-between;
}

.items-center {
  align-items: center;
}

.gap-2 {
  gap: 0.5rem;
}

.text-sm {
  font-size: 0.875rem;
}

.text-xs {
  font-size: 0.75rem;
}

.text-lg {
  font-size: 1.125rem;
}

.font-semibold {
  font-weight: 600;
}

.font-medium {
  font-weight: 500;
}

.text-gray-500 {
  color: var(--el-text-color-secondary);
}

.text-gray-400 {
  color: var(--el-text-color-placeholder);
}

.py-8 {
  padding-top: 2rem;
  padding-bottom: 2rem;
}

.text-center {
  text-align: center;
}
</style>
