import { reactive, ref, onMounted, h } from "vue";
import { message } from "@/utils/message";
import {
  getNoticeCategoryList,
  createNoticeCategory,
  updateNoticeCategory,
  deleteNoticeCategory,
} from "@/api/notice";
import { addDialog } from "@/components/ReDialog";
import { formRules } from "./rule";
import { ElMessageBox } from "element-plus";
import noticeCategoryForm from "./form.vue";
import { formatDateTime } from "@/utils/date";

export function useNoticeCategory(tableRef: any) {
  const form = reactive({
    name: "",
    code: "",
    status: null,
  });

  const formRef = ref();
  const loading = ref(true);
  const dataList = ref([]);
  const selectedNum = ref(0);
  const pagination = reactive({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true,
  });

  const columns: TableColumnList = [
    {
      label: "分类名称",
      prop: "name",
      minWidth: 120,
    },
    {
      label: "分类代码",
      prop: "code",
      minWidth: 120,
    },
    {
      label: "描述",
      prop: "description",
      minWidth: 200,
      showOverflowTooltip: true,
    },
    {
      label: "排序",
      prop: "sortOrder",
      minWidth: 80,
    },
    {
      label: "状态",
      prop: "status",
      minWidth: 100,
      cellRenderer: ({ row }) => (
        <el-tag type={row.status === 1 ? "success" : "danger"}>
          {row.status === 1 ? "启用" : "禁用"}
        </el-tag>
      ),
    },
    {
      label: "创建时间",
      prop: "createdAt",
      minWidth: 180,
      formatter: ({ createdAt }) => formatDateTime(createdAt),
    },
    {
      label: "操作",
      fixed: "right",
      width: 150,
      slot: "operation",
    },
  ];

  function onSearch() {
    loading.value = true;
    getNoticeCategoryList({
      current: pagination.currentPage,
      size: pagination.pageSize,
      name: form.name || undefined,
      code: form.code || undefined,
      status: form.status !== null ? form.status : undefined,
    })
      .then((response: any) => {
        // 处理响应格式：可能是 { data: { code: 200, data: {...} } } 或 { code: 200, data: {...} }
        const responseData = response?.data || response;
        if (responseData?.code === 200 && responseData?.data) {
          dataList.value = responseData.data.records || [];
          pagination.total = responseData.data.total || 0;
        } else if (responseData?.records) {
          // 如果直接返回分页数据
          dataList.value = responseData.records || [];
          pagination.total = responseData.total || 0;
        } else {
          dataList.value = [];
          pagination.total = 0;
        }
      })
      .catch((error: any) => {
        console.error("获取通知分类列表失败", error);
        dataList.value = [];
        pagination.total = 0;
      })
      .finally(() => {
        loading.value = false;
      });
  }

  function resetForm() {
    form.name = "";
    form.code = "";
    form.status = null;
    onSearch();
  }

  function openDialog(title = "新增", row?: any) {
    const formInlineData = {
      id: row?.id ?? undefined,
      name: row?.name ?? "",
      code: row?.code ?? "",
      description: row?.description ?? "",
      sortOrder: row?.sortOrder ?? 0,
      status: row?.status ?? 1,
    };

    addDialog({
      title: `${title}通知分类`,
      props: {
        formInline: formInlineData,
      },
      width: "500px",
      draggable: true,
      fullscreen: false,
      fullscreenIcon: true,
      closeOnClickModal: false,
      contentRenderer: () =>
        h(noticeCategoryForm, {
          ref: formRef,
          formInline: formInlineData,
        }),
      beforeSure: (done, { options, closeLoading }) => {
        const FormRef = formRef.value.getRef();
        const formComponent = formRef.value;
        const curData = formComponent?.getFormData
          ? formComponent.getFormData()
          : (options.props.formInline as any);

        FormRef.validate(async (valid: boolean) => {
          if (valid) {
            try {
              if (title === "新增") {
                const response = await createNoticeCategory(curData);
                if (response?.code === 200 || response?.data?.code === 200) {
                  message(`成功新增通知分类：${curData.name}`, {
                    type: "success",
                  });
                  done();
                  onSearch();
                } else {
                  closeLoading();
                  message(
                    response?.message || response?.data?.message || "创建失败",
                    { type: "error" },
                  );
                }
              } else {
                const response = await updateNoticeCategory(
                  curData.id,
                  curData,
                );
                if (response?.code === 200 || response?.data?.code === 200) {
                  message(`成功修改通知分类：${curData.name}`, {
                    type: "success",
                  });
                  done();
                  onSearch();
                } else {
                  closeLoading();
                  message(
                    response?.message || response?.data?.message || "更新失败",
                    { type: "error" },
                  );
                }
              }
            } catch (error: any) {
              closeLoading();
              message(
                error?.message || error?.response?.data?.message || "操作失败",
                { type: "error" },
              );
            }
          } else {
            closeLoading();
          }
        });
      },
    });
  }

  function handleDelete(row: any) {
    ElMessageBox.confirm(`确定要删除通知分类"${row.name}"吗？`, "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(async () => {
        try {
          await deleteNoticeCategory(row.id);
          message("删除成功", { type: "success" });
          onSearch();
        } catch (error: any) {
          message(error?.message || "删除失败", { type: "error" });
        }
      })
      .catch(() => {});
  }

  function handleSizeChange(val: number) {
    pagination.pageSize = val;
    onSearch();
  }

  function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    onSearch();
  }

  function handleSelectionChange(val: any) {
    selectedNum.value = val.length;
  }

  onMounted(() => {
    onSearch();
  });

  return {
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
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
  };
}
