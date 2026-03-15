<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from "vue";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { deviceDetection } from "@pureadmin/utils";
import { addDialog } from "@/components/ReDialog";
import { h } from "vue";
import dictDataForm from "../form/dict-data.vue";
import {
  getDictDataList,
  createDictData,
  updateDictData,
  deleteDictData,
  type CreateDictDataRequest,
  type UpdateDictDataRequest,
} from "@/api/dict";
import { message } from "@/utils/message";
import { ElMessageBox } from "element-plus";
import type { PaginationProps } from "@pureadmin/table";
import dayjs from "dayjs";

import Delete from "~icons/ep/delete";
import EditPen from "~icons/ep/edit-pen";
import AddFill from "~icons/ri/add-circle-line";

const props = defineProps<{
  dictType: string;
  dictTypeId: number;
}>();

const formRef = ref();
const tableRef = ref();
const dictDataFormRef = ref();
const loading = ref(true);
const dataList = ref([]);
const selectedNum = ref(0);
const isMounted = ref(true);
const pagination = reactive<PaginationProps>({
  total: 0,
  pageSize: 10,
  currentPage: 1,
  background: true,
});

const columns: TableColumnList = [
  {
    label: "勾选列",
    type: "selection",
    fixed: "left",
    reserveSelection: true,
  },
  {
    label: "字典编码",
    prop: "id",
    width: 90,
  },
  {
    label: "字典标签",
    prop: "dictLabel",
    minWidth: 130,
  },
  {
    label: "字典键值",
    prop: "dictValue",
    minWidth: 130,
  },
  {
    label: "字典排序",
    prop: "sortOrder",
    width: 100,
  },
  {
    label: "状态",
    prop: "status",
    minWidth: 90,
    cellRenderer: ({ row }) =>
      h(
        "el-tag",
        {
          type: row.status === 1 ? "success" : "danger",
          effect: "plain",
        },
        row.status === 1 ? "正常" : "停用",
      ),
  },
  {
    label: "备注",
    prop: "remark",
    minWidth: 160,
  },
  {
    label: "创建时间",
    prop: "createdAt",
    minWidth: 160,
    formatter: ({ createdAt }) =>
      createdAt ? dayjs(createdAt).format("YYYY-MM-DD HH:mm:ss") : "-",
  },
  {
    label: "操作",
    fixed: "right",
    width: 150,
    slot: "operation",
  },
];

async function onSearch() {
  if (!isMounted.value) return;
  loading.value = true;
  try {
    const response = await getDictDataList(props.dictType, {
      current: pagination.currentPage,
      size: pagination.pageSize,
    });

    if (!isMounted.value) return;

    if (response.code === 200 && response.data) {
      dataList.value = response.data.records || [];
      pagination.total = response.data.total || 0;
      pagination.pageSize = response.data.size || 10;
      pagination.currentPage = response.data.current || 1;
    } else {
      message(response.message || "获取字典项列表失败", { type: "error" });
      dataList.value = [];
    }
  } catch (error: any) {
    if (!isMounted.value) return;
    message(error.message || "获取字典项列表失败", { type: "error" });
    dataList.value = [];
  } finally {
    if (isMounted.value) {
      loading.value = false;
    }
  }
}

function handleSizeChange(val: number) {
  pagination.pageSize = val;
  pagination.currentPage = 1;
  onSearch();
}

function handleCurrentChange(val: number) {
  pagination.currentPage = val;
  onSearch();
}

function handleSelectionChange(val) {
  selectedNum.value = val.length;
}

async function handleDelete(row: any) {
  ElMessageBox.confirm(
    `确认要删除字典标签为<strong style='color:var(--el-color-primary)'>${
      row.dictLabel
    }</strong>的这条数据吗?`,
    "系统提示",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
      dangerouslyUseHTMLString: true,
      draggable: true,
    },
  )
    .then(async () => {
      if (!isMounted.value) return;
      try {
        const response = await deleteDictData(row.id);
        if (!isMounted.value) return;
        if (response.code === 200) {
          message(`您删除了字典标签为${row.dictLabel}的这条数据`, {
            type: "success",
          });
          onSearch();
        } else {
          message(response.message || "删除字典项失败", { type: "error" });
        }
      } catch (error: any) {
        if (!isMounted.value) return;
        message(error.message || "删除字典项失败", { type: "error" });
      }
    })
    .catch(() => {});
}

function openDialog(title = "新增", row?: any) {
  addDialog({
    title: `${title}字典项`,
    props: {
      formInline: {
        id: row?.id,
        dictType: props.dictType,
        dictLabel: row?.dictLabel ?? "",
        dictValue: row?.dictValue ?? "",
        sortOrder: row?.sortOrder ?? 0,
        status: row?.status ?? 1,
        remark: row?.remark ?? "",
      },
    },
    width: "40%",
    draggable: true,
    fullscreen: deviceDetection(),
    fullscreenIcon: true,
    closeOnClickModal: false,
    contentRenderer: () =>
      h(dictDataForm, { ref: dictDataFormRef, formInline: null }),
    beforeSure: async (done, { options }) => {
      const FormRef = dictDataFormRef.value.getRef();
      const curData = options.props.formInline;
      FormRef.validate(async (valid: boolean) => {
        if (valid) {
          if (!isMounted.value) return;
          try {
            if (title === "新增") {
              const request: CreateDictDataRequest = {
                dictType: curData.dictType,
                dictLabel: curData.dictLabel,
                dictValue: curData.dictValue,
                sortOrder: curData.sortOrder ?? 0,
                status: curData.status ?? 1,
                remark: curData.remark,
              };
              const response = await createDictData(request);
              if (!isMounted.value) return;
              if (response.code === 200) {
                message(`您${title}了字典项${curData.dictLabel}`, {
                  type: "success",
                });
                done();
                onSearch();
              } else {
                message(response.message || "创建字典项失败", {
                  type: "error",
                });
              }
            } else {
              const request: UpdateDictDataRequest = {
                dictType: curData.dictType,
                dictLabel: curData.dictLabel,
                dictValue: curData.dictValue,
                sortOrder: curData.sortOrder ?? 0,
                status: curData.status,
                remark: curData.remark,
              };
              const response = await updateDictData(curData.id, request);
              if (!isMounted.value) return;
              if (response.code === 200) {
                message(`您${title}了字典项${curData.dictLabel}`, {
                  type: "success",
                });
                done();
                onSearch();
              } else {
                message(response.message || "更新字典项失败", {
                  type: "error",
                });
              }
            }
          } catch (error: any) {
            if (!isMounted.value) return;
            message(error.message || `${title}字典项失败`, { type: "error" });
          }
        }
      });
    },
  });
}

onMounted(() => {
  isMounted.value = true;
  onSearch();
});

onBeforeUnmount(() => {
  isMounted.value = false;
});
</script>

<template>
  <div>
    <PureTableBar title="字典项列表" :columns="columns" @refresh="onSearch">
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
              if (selectedRows.length > 0) {
                ElMessageBox.confirm(
                  `确认要删除选中的${selectedRows.length}条数据吗?`,
                  '系统提示',
                  {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    draggable: true,
                  },
                )
                  .then(async () => {
                    try {
                      const deletePromises = selectedRows.map((row: any) =>
                        deleteDictData(row.id),
                      );
                      await Promise.all(deletePromises);
                      message(`成功删除${selectedRows.length}条数据`, {
                        type: 'success',
                      });
                      onSearch();
                    } catch (error: any) {
                      message(error.message || '批量删除失败', {
                        type: 'error',
                      });
                    }
                  })
                  .catch(() => {});
              }
            }
          "
        >
          删除
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
</style>
