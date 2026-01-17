<script setup lang="ts">
import { ref, markRaw, onMounted } from "vue";
import ReCol from "@/components/ReCol";
import { useDark, randomGradient } from "./utils";
import WelcomeTable from "./components/table/index.vue";
import { ReNormalCountTo } from "@/components/ReCountTo";
import { useRenderFlicker } from "@/components/ReFlicker";
import { ChartBar, ChartLine, ChartRound } from "./components/charts";
import Segmented, { type OptionsType } from "@/components/ReSegmented";
import { getWelcomeStatistics } from "@/api/system";
import { message } from "@/utils/message";
import GroupLine from "~icons/ri/group-line";
import Question from "~icons/ri/question-answer-line";
import CheckLine from "~icons/ri/chat-check-line";
import Smile from "~icons/ri/star-smile-line";

defineOptions({
  name: "Welcome"
});

const { isDark } = useDark();

let curWeek = ref(1); // 0上周、1本周
const optionsBasis: Array<OptionsType> = [
  {
    label: "上周"
  },
  {
    label: "本周"
  }
];

// 响应式数据
const chartData = ref([
  {
    icon: GroupLine,
    bgColor: "#effaff",
    color: "#41b6ff",
    duration: 2200,
    name: "用户总数",
    value: 0,
    percent: "+0%",
    data: []
  },
  {
    icon: Question,
    bgColor: "#fff5f4",
    color: "#e85f33",
    duration: 1600,
    name: "今日新增",
    value: 0,
    percent: "+0%",
    data: []
  },
  {
    icon: CheckLine,
    bgColor: "#eff8f4",
    color: "#26ce83",
    duration: 1500,
    name: "今日登录",
    value: 0,
    percent: "+0%",
    data: []
  },
  {
    icon: Smile,
    bgColor: "#f6f4fe",
    color: "#7846e5",
    duration: 100,
    name: "总登录数",
    value: 0,
    percent: "+0%",
    data: []
  }
]);

const barChartData = ref([
  {
    requireData: [],
    questionData: []
  },
  {
    requireData: [],
    questionData: []
  }
]);

const progressData = ref([
  { week: "周一", percentage: 85, duration: 110, color: "#41b6ff" },
  { week: "周二", percentage: 86, duration: 105, color: "#41b6ff" },
  { week: "周三", percentage: 88, duration: 100, color: "#41b6ff" },
  { week: "周四", percentage: 89, duration: 95, color: "#41b6ff" },
  { week: "周五", percentage: 94, duration: 90, color: "#26ce83" },
  { week: "周六", percentage: 96, duration: 85, color: "#26ce83" },
  { week: "周日", percentage: 100, duration: 80, color: "#26ce83" }
].reverse());

const latestNewsData = ref([]);
const tableData = ref([]);

const loading = ref(false);

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true;
  try {
    const { code, data } = await getWelcomeStatistics();
    if (code === 200 && data) {
      // 更新统计卡片数据
      if (data.cards && data.cards.length >= 4) {
        chartData.value[0].value = data.cards[0].value || 0;
        chartData.value[0].percent = `+${(data.cards[0].percent || 0).toFixed(0)}%`;
        chartData.value[0].data = data.cards[0].data || [];
        
        chartData.value[1].value = data.cards[1].value || 0;
        chartData.value[1].percent = "+0%";
        chartData.value[1].data = data.cards[1].data || [];
        
        chartData.value[2].value = data.cards[2].value || 0;
        chartData.value[2].percent = "+0%";
        chartData.value[2].data = data.cards[2].data || [];
        
        chartData.value[3].value = data.cards[3].value || 0;
        chartData.value[3].percent = `+${(data.cards[3].percent || 0).toFixed(0)}%`;
        chartData.value[3].data = data.cards[3].data || [];
      }
      
      // 更新图表数据
      if (data.chartData) {
        barChartData.value[0] = {
          requireData: data.chartData.lastWeek?.requireData || [],
          questionData: data.chartData.lastWeek?.questionData || []
        };
        barChartData.value[1] = {
          requireData: data.chartData.thisWeek?.requireData || [],
          questionData: data.chartData.thisWeek?.questionData || []
        };
      }
      
      // 更新最新动态
      if (data.latestNews) {
        latestNewsData.value = data.latestNews.map((item: any) => ({
          date: item.date,
          requiredNumber: item.requiredNumber || 0,
          resolveNumber: item.resolveNumber || 0
        }));
      }
      
      // 更新表格数据
      if (data.tableData) {
        tableData.value = data.tableData.map((item: any, index: number) => ({
          id: index + 1,
          date: item.date,
          requiredNumber: item.requiredNumber || 0,
          questionNumber: item.questionNumber || 0,
          resolveNumber: item.resolveNumber || 0,
          satisfaction: item.satisfaction || 95
        }));
      }
    }
  } catch (error) {
    console.error("加载统计数据失败", error);
    message("加载统计数据失败", { type: "error" });
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadStatistics();
});
</script>

<template>
  <div class="welcome-container">
    <el-row :gutter="24" justify="space-around">
      <re-col
        v-for="(item, index) in chartData"
        :key="index"
        v-motion
        class="mb-[18px]"
        :value="6"
        :md="12"
        :sm="12"
        :xs="24"
        :initial="{
          opacity: 0,
          y: 100
        }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: {
            delay: 80 * (index + 1)
          }
        }"
      >
        <el-card class="line-card" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">
              {{ item.name }}
            </span>
            <div
              class="w-8 h-8 flex justify-center items-center rounded-md"
              :style="{
                backgroundColor: isDark ? 'transparent' : item.bgColor
              }"
            >
              <IconifyIconOffline
                :icon="item.icon"
                :color="item.color"
                width="18"
                height="18"
              />
            </div>
          </div>
          <div class="flex justify-between items-start mt-3">
            <div class="w-1/2">
              <ReNormalCountTo
                :duration="item.duration"
                :fontSize="'1.6em'"
                :startVal="100"
                :endVal="item.value"
              />
              <p class="font-medium text-green-500">{{ item.percent }}</p>
            </div>
            <ChartLine
              v-if="item.data.length > 1"
              class="w-1/2!"
              :color="item.color"
              :data="item.data"
            />
            <ChartRound v-else class="w-1/2!" />
          </div>
        </el-card>
      </re-col>

      <re-col
        v-motion
        class="mb-[18px]"
        :value="18"
        :xs="24"
        :initial="{
          opacity: 0,
          y: 100
        }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: {
            delay: 400
          }
        }"
      >
        <el-card class="bar-card" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">分析概览</span>
            <Segmented v-model="curWeek" :options="optionsBasis" />
          </div>
          <div class="flex justify-between items-start mt-3">
            <ChartBar
              :requireData="barChartData[curWeek].requireData"
              :questionData="barChartData[curWeek].questionData"
            />
          </div>
        </el-card>
      </re-col>

      <re-col
        v-motion
        class="mb-[18px]"
        :value="6"
        :xs="24"
        :initial="{
          opacity: 0,
          y: 100
        }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: {
            delay: 480
          }
        }"
      >
        <el-card shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">解决概率</span>
          </div>
          <div
            v-for="(item, index) in progressData"
            :key="index"
            :class="[
              'flex',
              'justify-between',
              'items-start',
              index === 0 ? 'mt-8' : 'mt-[2.15rem]'
            ]"
          >
            <el-progress
              :text-inside="true"
              :percentage="item.percentage"
              :stroke-width="21"
              :color="item.color"
              striped
              striped-flow
              :duration="item.duration"
            />
            <span class="text-nowrap ml-2 text-text_color_regular text-sm">
              {{ item.week }}
            </span>
          </div>
        </el-card>
      </re-col>

      <re-col
        v-motion
        class="mb-[18px]"
        :value="18"
        :xs="24"
        :initial="{
          opacity: 0,
          y: 100
        }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: {
            delay: 560
          }
        }"
      >
        <el-card shadow="never" class="h-[580px]">
          <div class="flex justify-between">
            <span class="text-md font-medium">数据统计</span>
          </div>
          <WelcomeTable :table-data="tableData" class="mt-3" />
        </el-card>
      </re-col>

      <re-col
        v-motion
        class="mb-[18px]"
        :value="6"
        :xs="24"
        :initial="{
          opacity: 0,
          y: 100
        }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: {
            delay: 640
          }
        }"
      >
        <el-card shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">最新动态</span>
          </div>
          <el-scrollbar max-height="504" class="mt-3">
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in latestNewsData"
                :key="index"
                center
                placement="top"
                :icon="
                  markRaw(
                    useRenderFlicker({
                      background: randomGradient({
                        randomizeHue: true
                      })
                    })
                  )
                "
                :timestamp="item.date"
              >
                <p class="text-text_color_regular text-sm">
                  {{
                    `新增 ${item.requiredNumber} 条问题，${item.resolveNumber} 条已解决`
                  }}
                </p>
              </el-timeline-item>
            </el-timeline>
          </el-scrollbar>
        </el-card>
      </re-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
.welcome-container {
  min-height: 100%;
  padding: 20px;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
}

:deep(.el-card) {
  --el-card-border-color: none;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);

  /* 解决概率进度条宽度 */
  .el-progress--line {
    width: 85%;
  }

  /* 解决概率进度条字体大小 */
  .el-progress-bar__innerText {
    font-size: 15px;
    color: var(--el-text-color-primary);
  }

  /* 隐藏 el-scrollbar 滚动条 */
  .el-scrollbar__bar {
    display: none;
  }

  /* el-timeline 每一项上下、左右边距 */
  .el-timeline-item {
    margin: 0 6px;
  }
}

.main-content {
  margin: 20px 20px 0 !important;
}

/* 确保文字在暗黑模式下可见 */
.text-md,
.text-sm {
  color: var(--el-text-color-primary) !important;
}

.font-medium {
  color: var(--el-text-color-primary) !important;
}

/* 确保所有文本元素可见 */
.welcome-container :deep(*) {
  color: var(--el-text-color-primary);
}

/* 确保 el-row 和 el-col 可见 */
:deep(.el-row),
:deep(.el-col) {
  color: var(--el-text-color-primary);
}

/* 确保进度条文本可见 */
:deep(.el-progress__text) {
  color: var(--el-text-color-primary) !important;
}

/* 确保时间线文本可见 */
:deep(.el-timeline-item__timestamp) {
  color: var(--el-text-color-regular) !important;
}

:deep(.el-timeline-item__content) {
  color: var(--el-text-color-regular) !important;
}
</style>
