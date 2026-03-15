<script setup lang="ts">
import { useDictType } from "./utils/hook";
import { ref } from "vue";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { deviceDetection } from "@pureadmin/utils";
import { addDialog } from "@/components/ReDialog";
import { h } from "vue";
import dictDataList from "./components/dict-data-list.vue";
import { message } from "@/utils/message";

import Delete from "~icons/ep/delete";
import EditPen from "~icons/ep/edit-pen";
import Refresh from "~icons/ep/refresh";
import AddFill from "~icons/ri/add-circle-line";
import List from "~icons/ri/list-check";

defineOptions({
  name: "SystemDict",
});

const formRef = ref();
const tableRef = ref();
const dictDataFormRef = ref();

const {
  form,
  loading,
  columns,
  dataList,
  selectedNum,
  pagination,
  onSearch,
  resetForm,
  openDialog,
  handleDelete,
  handleBatchDelete,
  handleRefreshCache,
  handleSizeChange,
  handleCurrentChange,
  handleSelectionChange,
} = useDictType(tableRef);

// 打开字典项列表对话框
function handleDictDataList(row: any) {
  addDialog({
    title: `字典项列表 - ${row.dictName}`,
    props: {
      dictType: row.dictType,
      dictTypeId: row.id,
    },
    width: "60%",
    draggable: true,
    fullscreen: deviceDetection(),
    fullscreenIcon: true,
    closeOnClickModal: false,
    contentRenderer: () =>
      h(dictDataList, {
        ref: dictDataFormRef,
        dictType: row.dictType,
        dictTypeId: row.id,
      }),
  });
}

// 导出功能（TODO: 实现导出逻辑）
function handleExport() {
  message("导出功能待实现", { type: "info" });
}
</script>

<template>
  <div class="main">
    <el-form
      ref="formRef"
      :inline="true"
      :model="form"
      class="search-form bg-bg_color w-full pl-8 pt-[12px] overflow-auto"
    >
      <el-form-item label="字典名称：" prop="dictName">
        <el-input
          v-model="form.dictName"
          placeholder="请输入字典名称"
          clearable
          class="w-[180px]!"
        />
      </el-form-item>
      <el-form-item label="字典类型：" prop="dictType">
        <el-input
          v-model="form.dictType"
          placeholder="请输入字典类型"
          clearable
          class="w-[180px]!"
        />
      </el-form-item>
      <el-form-item label="字典状态：" prop="status">
        <el-select
          v-model="form.status"
          placeholder="所有"
          clearable
          class="w-[180px]!"
        >
          <el-option label="正常" value="1" />
          <el-option label="停用" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间：" prop="startTime">
        <el-date-picker
          v-model="form.startTime"
          type="datetime"
          placeholder="开始时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="w-[180px]!"
        />
      </el-form-item>
      <el-form-item label="至" prop="endTime">
        <el-date-picker
          v-model="form.endTime"
          type="datetime"
          placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="w-[180px]!"
        />
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

    <PureTableBar title="字典管理" :columns="columns" @refresh="onSearch">
      <template #buttons>
        <el-button
          type="primary"
          :icon="useRenderIcon(AddFill)"
          @click="openDialog()"
        >
          新增
        </el-button>
        <el-button
          type="success"
          :icon="useRenderIcon(EditPen)"
          :disabled="selectedNum === 0"
          @click="
            () => {
              const selectedRows = tableRef.getTableRef().getSelectionRows();
              if (selectedRows.length === 1) {
                openDialog('修改', selectedRows[0]);
              } else {
                message('请选择一条数据进行修改', { type: 'warning' });
              }
            }
          "
        >
          修改
        </el-button>
        <el-button
          type="danger"
          :icon="useRenderIcon(Delete)"
          :disabled="selectedNum === 0"
          @click="
            () => {
              const selectedRows = tableRef.getTableRef().getSelectionRows();
              handleBatchDelete(selectedRows);
            }
          "
        >
          删除
        </el-button>
        <el-button
          type="warning"
          :icon="useRenderIcon('ri/download-line')"
          @click="handleExport"
        >
          导出
        </el-button>
        <el-button
          type="danger"
          :icon="useRenderIcon('ri-refresh-line')"
          @click="handleRefreshCache"
        >
          刷新缓存
        </el-button>
      </template>
      <template v-slot="{ size, dynamicColumns }">
        <pure-table
          ref="tableRef"
          border
          align-whole="center"
          row-key="id"
          showOverflowTooltip
          table-layout="auto"
          :loading="loading"
          :size="size"
          :data="dataList"
          :columns="dynamicColumns"
          :pagination="pagination"
          :paginationSmall="size === 'small'"
          :header-cell-style="{
            background: 'var(--el-table-row-hover-bg-color)',
            color: 'var(--el-text-color-primary)',
          }"
          @page-size-change="handleSizeChange"
          @page-current-change="handleCurrentChange"
          @selection-change="handleSelectionChange"
        >
          <template #operation="{ row }">
            <el-button
              class="reset-margin"
              link
              type="primary"
              :icon="useRenderIcon(EditPen)"
              :size="size"
              @click="openDialog('修改', row)"
            >
              编辑
            </el-button>
            <el-button
              class="reset-margin"
              link
              type="primary"
              :icon="useRenderIcon(List)"
              :size="size"
              @click="handleDictDataList(row)"
            >
              列表
            </el-button>
            <el-button
              class="reset-margin"
              link
              type="danger"
              :icon="useRenderIcon(Delete)"
              :size="size"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </pure-table>
      </template>
    </PureTableBar>
  </div>
</template>

<style scoped lang="scss">
:deep(.el-dropdown-menu__item i) {
  margin: 0;
}

.search-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}
</style>
