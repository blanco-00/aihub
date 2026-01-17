<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { ref, computed, onMounted, watch } from "vue";
import { noticesData } from "./data";
import NoticeList from "./components/NoticeList.vue";
import BellIcon from "~icons/lucide/bell";
import { getMyNotices, getUnreadNoticeCount } from "@/api/notice";
import { formatRelativeTime } from "@/utils/date";
import { markNoticeAsRead } from "@/api/notice";

const { t } = useI18n();

// 通知数据：第一个是系统通知（对接后端），后面是内置的消息和待办
const notices = ref(noticesData);
const activeKey = ref(noticesData[0]?.key);
const unreadCount = ref(0);
// 筛选模式：'all' 显示全部，'unread' 只显示未读
const filterMode = ref<'all' | 'unread'>('unread');

// 加载系统通知（第一个标签页）
async function loadSystemNotices() {
  console.log("开始加载系统通知...");
  try {
    // 获取未读数量
    console.log("调用 getUnreadNoticeCount API...");
    const countResponse: any = await getUnreadNoticeCount();
    console.log("未读数量API响应:", countResponse);
    
    // http.request 返回的是 response.data，即 {code, message, data}
    // 所以 countResponse 就是 {code: 200, message: "操作成功", data: 0}
    if (countResponse && countResponse.code === 200) {
      unreadCount.value = countResponse.data || 0;
      console.log("未读数量:", unreadCount.value);
    } else {
      unreadCount.value = 0;
      console.log("未读数量解析失败，使用默认值 0，响应数据:", countResponse);
    }

    // 获取通知列表
    // 如果筛选模式是"只显示未读"，则只获取未读通知；否则获取所有通知
    console.log("调用 getMyNotices API...", "筛选模式:", filterMode.value);
    const listResponse: any = await getMyNotices({
      current: 1,
      size: filterMode.value === 'unread' ? 20 : 10, // 未读模式显示更多
      isRead: filterMode.value === 'unread' ? 0 : undefined // 只显示未读时传0
    });
    console.log("通知列表API原始响应:", listResponse);

    // http.request 返回的是 response.data，即 {code, message, data}
    // 所以 listResponse 就是 {code: 200, message: "操作成功", data: {records: [...], total: 1, ...}}
    if (listResponse && listResponse.code === 200 && listResponse.data) {
      const records = listResponse.data.records || [];
      console.log("通知记录数:", records.length, "记录:", records);
      
      // 转换为通知列表格式
      notices.value[0].list = records.map((item: any) => {
        console.log("处理通知项:", item);
        
        // 处理内容：移除HTML标签，只保留文本
        let description = item.content || "";
        if (description) {
          // 移除HTML标签
          description = description.replace(/<[^>]*>/g, "");
          // 去除首尾空白
          description = description.trim();
          // 限制长度
          if (description.length > 50) {
            description = description.substring(0, 50) + "...";
          }
        }
        
        // 如果内容为空，使用默认提示
        if (!description || description.trim() === "") {
          description = "暂无内容";
        }
        
        const noticeItem = {
          avatar: "",
          title: item.title || "无标题",
          description: description,
          datetime: formatRelativeTime(item.publishTime),
          type: "1",
          status: item.type === 3 ? "danger" : item.type === 2 ? "warning" : "info",
          extra: item.isRead === 0 ? "未读" : "已读",
          noticeId: item.noticeId || item.id
        };
        
        console.log("转换后的通知项:", noticeItem);
        return noticeItem;
      });
      console.log("系统通知加载成功:", notices.value[0].list.length, "条", notices.value[0].list);
    } else {
      notices.value[0].list = [];
      console.log("系统通知响应格式异常:", listResponse);
    }
  } catch (error) {
    console.error("加载系统通知失败", error);
    notices.value[0].list = [];
    unreadCount.value = 0;
  }
}

// 计算标签显示（包含未读数量）
const getLabel = computed(
  () => item => {
    if (item.key === "1") {
      // 系统通知标签，显示未读数量（使用服务器返回的未读数量）
      return t(item.name) + (unreadCount.value > 0 ? `(${unreadCount.value})` : "");
    }
    return t(item.name) + (item.list.length > 0 ? `(${item.list.length})` : "");
  }
);

// 计算过滤后的通知列表
const filteredNoticeList = computed(() => {
  if (activeKey.value !== "1") {
    return notices.value.find(n => n.key === activeKey.value)?.list || [];
  }
  
  const list = notices.value[0].list || [];
  if (filterMode.value === 'unread') {
    // 只显示未读
    return list.filter((n: any) => n.extra === "未读");
  } else {
    // 显示全部，但未读的排在前面
    const unread = list.filter((n: any) => n.extra === "未读");
    const read = list.filter((n: any) => n.extra === "已读");
    return [...unread, ...read];
  }
});

// 监听 activeKey 变化，切换到通知标签时刷新数据
watch(activeKey, (newKey) => {
  if (newKey === "1") {
    loadSystemNotices();
  }
});

// 处理通知已读事件
function handleNoticeRead(noticeId: number) {
  // 更新本地通知列表中的已读状态
  const notice = notices.value[0].list.find((n: any) => n.noticeId === noticeId);
  if (notice) {
    notice.extra = "已读";
  }
  // 刷新未读数量
  loadSystemNotices();
}

// 监听下拉框打开/关闭事件
function handleDropdownVisibleChange(visible: boolean) {
  if (visible) {
    // 下拉框打开时，加载通知数据
    console.log("下拉框打开，开始加载通知");
    loadSystemNotices();
  }
}

onMounted(() => {
  // 页面加载时也加载一次（用于显示未读数量）
  loadSystemNotices();
  // 每30秒刷新一次未读数量
  setInterval(() => {
    loadSystemNotices();
  }, 30000);
});
</script>

<template>
  <el-dropdown trigger="click" placement="bottom-end" @visible-change="handleDropdownVisibleChange">
    <span
      :class="['dropdown-badge', 'navbar-bg-hover', 'select-none', 'mr-[7px]']"
    >
      <el-badge :value="unreadCount > 0 ? unreadCount : ''" :hidden="unreadCount === 0">
        <span class="header-notice-icon">
          <IconifyIconOffline :icon="BellIcon" />
        </span>
      </el-badge>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-tabs
          v-model="activeKey"
          :stretch="true"
          class="dropdown-tabs"
          :style="{ width: notices.length === 0 ? '200px' : '330px' }"
        >
          <el-empty
            v-if="notices.length === 0"
            :description="t('status.pureNoMessage')"
            :image-size="60"
          />
          <span v-else>
            <template v-for="item in notices" :key="item.key">
              <el-tab-pane :label="getLabel(item)" :name="`${item.key}`">
                <!-- 系统通知标签页：显示筛选按钮 -->
                <div v-if="item.key === '1'" class="notice-filter-bar">
                  <el-radio-group v-model="filterMode" size="small" @change="loadSystemNotices">
                    <el-radio-button label="unread">未读</el-radio-button>
                    <el-radio-button label="all">全部</el-radio-button>
                  </el-radio-group>
                </div>
                <el-scrollbar max-height="330px">
                  <div class="noticeList-container">
                    <NoticeList 
                      :list="item.key === '1' ? filteredNoticeList : item.list" 
                      :emptyText="item.emptyText"
                      @read="handleNoticeRead"
                    />
                  </div>
                </el-scrollbar>
              </el-tab-pane>
            </template>
          </span>
        </el-tabs>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style lang="scss" scoped>
/* ”铃铛“摇晃衰减动画 */
@keyframes pure-bell-ring {
  0%,
  100% {
    transform-origin: top;
  }

  15% {
    transform: rotateZ(10deg);
  }

  30% {
    transform: rotateZ(-10deg);
  }

  45% {
    transform: rotateZ(5deg);
  }

  60% {
    transform: rotateZ(-5deg);
  }

  75% {
    transform: rotateZ(2deg);
  }
}

.dropdown-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 48px;
  cursor: pointer;

  .header-notice-icon {
    font-size: 16px;
  }

  &:hover {
    .header-notice-icon svg {
      animation: pure-bell-ring 1s both;
    }
  }
}

.dropdown-tabs {
  .noticeList-container {
    padding: 15px 24px 0;
  }
  
  .notice-filter-bar {
    padding: 8px 24px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    justify-content: center;
    
    .dark & {
      border-bottom-color: #303030;
    }
    
    :deep(.el-radio-group) {
      .el-radio-button__inner {
        padding: 5px 15px;
        font-size: 12px;
      }
    }
  }

  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__nav-wrap)::after {
    height: 1px;
  }

  :deep(.el-tabs__nav-wrap) {
    padding: 0 36px;
  }
}
</style>
