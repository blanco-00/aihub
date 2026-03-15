import dayjs from "dayjs";
import dictTypeForm from "../form/dict-type.vue";
import { message } from "@/utils/message";
import { ElMessageBox } from "element-plus";
import { usePublicHooks } from "../../hooks";
import { addDialog } from "@/components/ReDialog";
import type { DictTypeFormItemProps } from "./types";
import type { PaginationProps } from "@pureadmin/table";
import { deviceDetection } from "@pureadmin/utils";
import {
  getDictTypeList,
  createDictType,
  updateDictType,
  deleteDictType,
  refreshDictCache,
  type CreateDictTypeRequest,
  type UpdateDictTypeRequest,
} from "@/api/dict";
import { type Ref, reactive, ref, h, onMounted, onBeforeUnmount } from "vue";

export function useDictType(tableRef: Ref) {
  const form = reactive({
    dictName: "",
    dictType: "",
    status: "",
    startTime: "",
    endTime: "",
  });
  const curRow = ref();
  const formRef = ref();
  const dataList = ref([]);
  const loading = ref(true);
  const switchLoadMap = ref({});
  const { switchStyle } = usePublicHooks();
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
      label: "字典主键",
      prop: "id",
      width: 90,
    },
    {
      label: "字典名称",
      prop: "dictName",
      minWidth: 130,
    },
    {
      label: "字典类型",
      prop: "dictType",
      minWidth: 130,
    },
    {
      label: "状态",
      cellRenderer: (scope) => (
        <el-switch
          size={scope.props.size === "small" ? "small" : "default"}
          loading={switchLoadMap.value[scope.index]?.loading}
          v-model={scope.row.status}
          active-value={1}
          inactive-value={0}
          active-text="正常"
          inactive-text="停用"
          inline-prompt
          style={switchStyle.value}
          onChange={() => onChange(scope as any)}
        />
      ),
      minWidth: 90,
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
      width: 240,
      slot: "operation",
    },
  ];

  async function onChange({ row, index }) {
    ElMessageBox.confirm(
      `确认要<strong>${
        row.status === 0 ? "停用" : "启用"
      }</strong><strong style='color:var(--el-color-primary)'>${
        row.dictName
      }</strong>吗?`,
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
        switchLoadMap.value[index] = Object.assign(
          {},
          switchLoadMap.value[index],
          {
            loading: true,
          },
        );
        try {
          const newStatus = row.status === 0 ? 1 : 0;
          const request: UpdateDictTypeRequest = {
            dictName: row.dictName,
            dictType: row.dictType,
            status: newStatus,
            remark: row.remark,
          };
          const response = await updateDictType(row.id, request);
          if (!isMounted.value) return;
          if (response.code === 200) {
            row.status = newStatus;
            message(`已${row.status === 0 ? "停用" : "启用"}${row.dictName}`, {
              type: "success",
            });
          } else {
            message(response.message || "更新字典状态失败", { type: "error" });
            row.status === 0 ? (row.status = 1) : (row.status = 0);
          }
        } catch (error: any) {
          if (!isMounted.value) return;
          message(error.message || "更新字典状态失败", { type: "error" });
          row.status === 0 ? (row.status = 1) : (row.status = 0);
        } finally {
          if (isMounted.value) {
            switchLoadMap.value[index] = Object.assign(
              {},
              switchLoadMap.value[index],
              {
                loading: false,
              },
            );
          }
        }
      })
      .catch(() => {
        if (isMounted.value) {
          row.status === 0 ? (row.status = 1) : (row.status = 0);
        }
      });
  }

  async function handleDelete(row) {
    ElMessageBox.confirm(
      `确认要删除字典名称为<strong style='color:var(--el-color-primary)'>${
        row.dictName
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
          const response = await deleteDictType(row.id);
          if (!isMounted.value) return;
          if (response.code === 200) {
            message(`您删除了字典名称为${row.dictName}的这条数据`, {
              type: "success",
            });
            onSearch();
          } else {
            message(response.message || "删除字典失败", { type: "error" });
          }
        } catch (error: any) {
          if (!isMounted.value) return;
          message(error.message || "删除字典失败", { type: "error" });
        }
      })
      .catch(() => {});
  }

  async function handleBatchDelete(rows) {
    if (rows.length === 0) {
      message("请选择要删除的数据", { type: "warning" });
      return;
    }

    ElMessageBox.confirm(
      `确认要删除选中的<strong style='color:var(--el-color-primary)'>${
        rows.length
      }</strong>条数据吗?`,
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
          const deletePromises = rows.map((row: any) => deleteDictType(row.id));
          await Promise.all(deletePromises);
          if (!isMounted.value) return;
          message(`成功删除${rows.length}条数据`, { type: "success" });
          onSearch();
        } catch (error: any) {
          if (!isMounted.value) return;
          message(error.message || "批量删除失败", { type: "error" });
        }
      })
      .catch(() => {});
  }

  async function handleRefreshCache() {
    ElMessageBox.confirm("确认要刷新字典缓存吗?", "系统提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
      draggable: true,
    })
      .then(async () => {
        if (!isMounted.value) return;
        try {
          const response = await refreshDictCache();
          if (!isMounted.value) return;
          if (response.code === 200) {
            message("刷新字典缓存成功", { type: "success" });
          } else {
            message(response.message || "刷新字典缓存失败", { type: "error" });
          }
        } catch (error: any) {
          if (!isMounted.value) return;
          message(error.message || "刷新字典缓存失败", { type: "error" });
        }
      })
      .catch(() => {});
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

  async function onSearch() {
    if (!isMounted.value) return;
    loading.value = true;
    try {
      const response = await getDictTypeList({
        current: pagination.currentPage,
        size: pagination.pageSize,
        dictName: form.dictName || undefined,
        dictType: form.dictType || undefined,
        status: form.status ? Number(form.status) : undefined,
        startTime: form.startTime || undefined,
        endTime: form.endTime || undefined,
      });

      if (!isMounted.value) return;

      if (response.code === 200 && response.data) {
        dataList.value = response.data.records || [];
        pagination.total = response.data.total || 0;
        pagination.pageSize = response.data.size || 10;
        pagination.currentPage = response.data.current || 1;
      } else {
        message(response.message || "获取字典列表失败", { type: "error" });
        dataList.value = [];
      }
    } catch (error: any) {
      if (!isMounted.value) return;
      message(error.message || "获取字典列表失败", { type: "error" });
      dataList.value = [];
    } finally {
      if (isMounted.value) {
        loading.value = false;
      }
    }
  }

  const resetForm = (formEl) => {
    if (!formEl) return;
    formEl.resetFields();
    form.dictName = "";
    form.dictType = "";
    form.status = "";
    form.startTime = "";
    form.endTime = "";
    pagination.currentPage = 1;
    onSearch();
  };

  function openDialog(title = "新增", row?: DictTypeFormItemProps) {
    addDialog({
      title: `${title}字典`,
      props: {
        formInline: {
          id: row?.id,
          dictName: row?.dictName ?? "",
          dictType: row?.dictType ?? "",
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
        h(dictTypeForm, { ref: formRef, formInline: null }),
      beforeSure: async (done, { options }) => {
        const FormRef = formRef.value.getRef();
        const curData = options.props.formInline as DictTypeFormItemProps;
        FormRef.validate(async (valid: boolean) => {
          if (valid) {
            if (!isMounted.value) return;
            try {
              if (title === "新增") {
                const request: CreateDictTypeRequest = {
                  dictName: curData.dictName,
                  dictType: curData.dictType,
                  status: curData.status ?? 1,
                  remark: curData.remark,
                };
                const response = await createDictType(request);
                if (!isMounted.value) return;
                if (response.code === 200) {
                  message(
                    `您${title}了字典名称为${curData.dictName}的这条数据`,
                    {
                      type: "success",
                    },
                  );
                  done();
                  onSearch();
                } else {
                  message(response.message || "创建字典失败", {
                    type: "error",
                  });
                }
              } else {
                const request: UpdateDictTypeRequest = {
                  dictName: curData.dictName,
                  dictType: curData.dictType,
                  status: curData.status,
                  remark: curData.remark,
                };
                const response = await updateDictType(curData.id, request);
                if (!isMounted.value) return;
                if (response.code === 200) {
                  message(
                    `您${title}了字典名称为${curData.dictName}的这条数据`,
                    {
                      type: "success",
                    },
                  );
                  done();
                  onSearch();
                } else {
                  message(response.message || "更新字典失败", {
                    type: "error",
                  });
                }
              }
            } catch (error: any) {
              if (!isMounted.value) return;
              message(error.message || `${title}字典失败`, { type: "error" });
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

  return {
    form,
    curRow,
    formRef,
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
  };
}
