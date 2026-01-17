<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useUser } from "./utils/hook";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { getDepartmentTree } from "@/api/department";
import type { DepartmentInfo } from "@/api/department";

import Upload from "~icons/ri/upload-line";
import Password from "~icons/ri/lock-password-line";
import More from "~icons/ep/more-filled";
import Delete from "~icons/ep/delete";
import EditPen from "~icons/ep/edit-pen";
import Refresh from "~icons/ep/refresh";
import AddFill from "~icons/ri/add-circle-line";

defineOptions({
  name: "SystemUser"
});

const formRef = ref();
const tableRef = ref();
const departmentTree = ref<DepartmentInfo[]>([]);
const selectedDepartmentId = ref<number | undefined>(undefined);
const sidebarCollapsed = ref(false); // 控制左侧组织机构树的折叠状态

const {
  form,
  loading,
  columns,
  dataList,
  selectedNum,
  pagination,
  buttonClass,
  deviceDetection,
  onSearch,
  resetForm,
  onbatchDel,
  openDialog,
  handleUpdate,
  handleDelete,
  handleUpload,
  handleReset,
  handleSizeChange,
  onSelectionCancel,
  handleCurrentChange,
  handleSelectionChange
} = useUser(tableRef);

// 加载部门树
onMounted(async () => {
  try {
    const { code, data } = await getDepartmentTree();
    if (code === 200 && data) {
      // 后端已经返回树结构，直接使用，不需要再用 handleTree 处理
      departmentTree.value = data as DepartmentInfo[];
    }
  } catch (error) {
    console.error("加载部门树失败", error);
  }
});

// 部门树节点点击事件（联动用户列表）
function handleDepartmentNodeClick(data: DepartmentInfo) {
  // 如果点击的是已选中的节点，则取消选择
  if (selectedDepartmentId.value === data.id) {
    clearDepartmentFilter();
    return;
  }
  
  // 设置选中的部门ID
  selectedDepartmentId.value = data.id;
  form.departmentId = data.id;
  
  // 重置到第一页并搜索
  pagination.currentPage = 1;
  onSearch();
}

// 清除部门筛选（联动用户列表）
function clearDepartmentFilter() {
  selectedDepartmentId.value = undefined;
  form.departmentId = undefined;
  
  // 重置到第一页并搜索
  pagination.currentPage = 1;
  onSearch();
}

// 根据部门ID获取部门名称（用于显示当前筛选）
function getDepartmentName(deptId: number | undefined): string {
  if (!deptId) return '';
  
  function findDept(depts: DepartmentInfo[], id: number): DepartmentInfo | null {
    for (const dept of depts) {
      if (dept.id === id) return dept;
      if (dept.children) {
        const found = findDept(dept.children, id);
        if (found) return found;
      }
    }
    return null;
  }
  
  const dept = findDept(departmentTree.value, deptId);
  return dept ? dept.name : '未知部门';
}
</script>

<template>
  <div class="user-management-container">
    <el-container class="h-full">
      <!-- 左侧：组织机构树 -->
      <el-aside 
        :width="sidebarCollapsed ? '60px' : '280px'" 
        class="dept-sidebar"
        :class="{ 'collapsed': sidebarCollapsed }"
      >
        <div class="p-4">
          <div class="flex items-center justify-between mb-4">
            <h3 v-if="!sidebarCollapsed" class="text-lg font-semibold dept-title">组织机构</h3>
            <div class="flex items-center gap-2">
              <el-button
                v-if="!sidebarCollapsed && selectedDepartmentId"
                text
                type="primary"
                size="small"
                @click="clearDepartmentFilter"
              >
                清除筛选
              </el-button>
              <el-button
                text
                type="primary"
                size="small"
                :icon="useRenderIcon(sidebarCollapsed ? 'ri:menu-unfold-line' : 'ri:menu-fold-line')"
                @click="sidebarCollapsed = !sidebarCollapsed"
                :title="sidebarCollapsed ? '展开' : '折叠'"
              />
            </div>
          </div>
          <!-- 显示当前筛选的部门（如果有） -->
          <div v-if="!sidebarCollapsed && selectedDepartmentId" class="mb-3 p-2 rounded bg-[var(--el-fill-color-light)]">
            <div class="text-xs text-[var(--el-text-color-secondary)] mb-1">当前筛选：</div>
            <div class="text-sm font-medium text-[var(--el-color-primary)]">
              {{ getDepartmentName(selectedDepartmentId) }}
            </div>
          </div>
          <el-tree
            v-if="!sidebarCollapsed"
            :data="departmentTree"
            :props="{ children: 'children', label: 'name' }"
            :highlight-current="true"
            :current-node-key="selectedDepartmentId"
            node-key="id"
            @node-click="handleDepartmentNodeClick"
            class="department-tree"
          >
            <template #default="{ node, data }">
              <span class="flex items-center dept-tree-node">
                <el-icon class="mr-2 dept-icon">
                  <component :is="useRenderIcon('ri:folder-line')" />
                </el-icon>
                <span 
                  class="dept-label"
                  :class="{ 'dept-selected': selectedDepartmentId === data.id }"
                >
                  {{ node.label }}
                </span>
              </span>
            </template>
          </el-tree>
          <!-- 折叠状态下只显示图标 -->
          <div v-else class="flex flex-col items-center gap-2 pt-4">
            <el-tooltip
              v-for="dept in departmentTree"
              :key="dept.id"
              :content="dept.name"
              placement="right"
            >
              <div
                class="cursor-pointer p-2 rounded hover:bg-[var(--el-fill-color-light)] transition-colors"
                :class="{ 'bg-[var(--el-color-primary-light-9)]': selectedDepartmentId === dept.id }"
                @click="handleDepartmentNodeClick(dept)"
              >
                <el-icon 
                  class="text-lg block"
                  :class="{ 'text-[var(--el-color-primary)]': selectedDepartmentId === dept.id }"
                >
                  <component :is="useRenderIcon('ri:folder-line')" />
                </el-icon>
              </div>
            </el-tooltip>
          </div>
        </div>
      </el-aside>

      <!-- 右侧：用户列表 -->
      <el-main class="p-0">
        <div class="w-full h-full">
          <el-form
            ref="formRef"
            :inline="true"
            :model="form"
            class="search-form bg-bg_color w-full pl-8 pt-[12px] overflow-auto"
          >
            <el-form-item label="用户名称：" prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名称"
                clearable
                class="w-[180px]!"
              />
            </el-form-item>
            <el-form-item label="手机号码：" prop="phone">
              <el-input
                v-model="form.phone"
                placeholder="请输入手机号码"
                clearable
                class="w-[180px]!"
              />
            </el-form-item>
            <el-form-item label="状态：" prop="status">
              <el-select
                v-model="form.status"
                placeholder="请选择"
                clearable
                class="w-[180px]!"
              >
                <el-option label="已开启" value="1" />
                <el-option label="已关闭" value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :icon="useRenderIcon('ri/search-line')"
                :loading="loading"
                @click="onSearch"
              >
                搜索
              </el-button>
              <el-button :icon="useRenderIcon(Refresh)" @click="resetForm(formRef)">
                重置
              </el-button>
            </el-form-item>
          </el-form>

          <PureTableBar
            title="用户管理"
            :columns="columns"
            @refresh="onSearch"
          >
            <template #buttons>
              <el-button
                type="primary"
                :icon="useRenderIcon(AddFill)"
                @click="openDialog()"
              >
                新增用户
              </el-button>
            </template>
            <template v-slot="{ size, dynamicColumns }">
              <div
                v-if="selectedNum > 0"
                v-motion-fade
                class="bg-[var(--el-fill-color-light)] w-full h-[46px] mb-2 pl-4 flex items-center"
              >
                <div class="flex-auto">
                  <span
                    style="font-size: var(--el-font-size-base)"
                    class="selected-count-text"
                  >
                    已选 {{ selectedNum }} 项
                  </span>
                  <el-button type="primary" text @click="onSelectionCancel">
                    取消选择
                  </el-button>
                </div>
                <el-popconfirm title="是否确认删除?" @confirm="onbatchDel">
                  <template #reference>
                    <el-button type="danger" text class="mr-1!">
                      批量删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
              <pure-table
                ref="tableRef"
                row-key="id"
                adaptive
                :adaptiveConfig="{ offsetBottom: 108 }"
                align-whole="center"
                table-layout="auto"
                :loading="loading"
                :size="size"
                :data="dataList"
                :columns="dynamicColumns"
                :pagination="{ ...pagination, size }"
                :header-cell-style="{
                  background: 'var(--el-fill-color-light)',
                  color: 'var(--el-text-color-primary)'
                }"
                @selection-change="handleSelectionChange"
                @page-size-change="handleSizeChange"
                @page-current-change="handleCurrentChange"
              >
                <template #operation="{ row }">
                  <el-button
                    class="reset-margin"
                    link
                    type="primary"
                    :size="size"
                    :icon="useRenderIcon(EditPen)"
                    @click="openDialog('修改', row)"
                  >
                    修改
                  </el-button>
                  <el-popconfirm
                    :title="`是否确认删除用户编号为${row.id}的这条数据`"
                    @confirm="handleDelete(row)"
                  >
                    <template #reference>
                      <el-button
                        class="reset-margin"
                        link
                        type="primary"
                        :size="size"
                        :icon="useRenderIcon(Delete)"
                      >
                        删除
                      </el-button>
                    </template>
                  </el-popconfirm>
                  <el-dropdown>
                    <el-button
                      class="ml-3! mt-[2px]!"
                      link
                      type="primary"
                      :size="size"
                      :icon="useRenderIcon(More)"
                      @click="handleUpdate(row)"
                    />
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item>
                          <el-button
                            :class="buttonClass"
                            link
                            type="primary"
                            :size="size"
                            :icon="useRenderIcon(Upload)"
                            @click="handleUpload(row)"
                          >
                            上传头像
                          </el-button>
                        </el-dropdown-item>
                        <el-dropdown-item>
                          <el-button
                            :class="buttonClass"
                            link
                            type="primary"
                            :size="size"
                            :icon="useRenderIcon(Password)"
                            @click="handleReset(row)"
                          >
                            重置密码
                          </el-button>
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </template>
              </pure-table>
            </template>
          </PureTableBar>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<style lang="scss" scoped>
.user-management-container {
  height: calc(100vh - 84px);
}

:deep(.el-dropdown-menu__item i) {
  margin: 0;
}

:deep(.el-button:focus-visible) {
  outline: none;
}

.search-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
  
  /* 表单标签颜色 - 使用主题变量 */
  :deep(.el-form-item__label) {
    color: var(--el-text-color-primary);
  }
}

/* 左侧部门树面板样式 - 适配深色/浅色主题 */
.dept-sidebar {
  background-color: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color);
  overflow-y: auto;
  transition: width 0.3s ease;
  
  &.collapsed {
    overflow: visible;
  }
  
  .dept-title {
    color: var(--el-text-color-primary);
  }
}

.department-tree {
  :deep(.el-tree) {
    background-color: transparent;
    color: var(--el-text-color-primary);
  }
  
  :deep(.el-tree-node__content) {
    height: 36px;
    padding: 0 8px;
    color: var(--el-text-color-primary);
    border-radius: 4px;
    margin-bottom: 2px;
  }
  
  :deep(.el-tree-node__content:hover) {
    background-color: var(--el-fill-color-light);
  }
  
  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    background-color: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
    font-weight: 500;
  }
  
  :deep(.el-tree-node__expand-icon) {
    color: var(--el-text-color-regular);
  }
  
  :deep(.el-tree-node__label) {
    color: var(--el-text-color-primary);
  }
  
  /* 部门树节点样式 */
  .dept-tree-node {
    width: 100%;
  }
  
  .dept-icon {
    color: var(--el-text-color-regular);
    font-size: 16px;
  }
  
  .dept-label {
    color: var(--el-text-color-primary);
    transition: color 0.2s;
  }
  
  .dept-selected {
    color: var(--el-color-primary);
    font-weight: 500;
  }
}

/* 选中数量文本 - 使用主题变量（适配深色/浅色主题） */
.selected-count-text {
  color: var(--el-text-color-secondary);
}

/* 文本颜色 - 统一使用主题变量（适配深色/浅色主题） */
.text-primary {
  color: var(--el-color-primary);
  font-weight: 500;
}

/* 确保所有文本颜色使用主题变量 */
.user-management-container {
  /* 容器内所有文本默认使用主题变量 */
  color: var(--el-text-color-primary);
  
  /* 确保链接颜色使用主题变量 */
  a {
    color: var(--el-color-primary);
  }
  
  /* 确保图标颜色使用主题变量 */
  .el-icon {
    color: var(--el-text-color-regular);
  }
}
</style>
