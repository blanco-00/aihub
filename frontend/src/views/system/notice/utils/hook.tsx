import { reactive, ref, onMounted, h } from "vue";
import { message } from "@/utils/message";
import {
  getNoticeList,
  getNoticeDetail,
  createNotice,
  updateNotice,
  publishNotice,
  withdrawNotice,
  deleteNotice,
  getNoticeCategoryList,
} from "@/api/notice";
import { addDialog } from "@/components/ReDialog";
import { ElMessageBox } from "element-plus";
import noticeForm from "./form.vue";
import { formatDateTime } from "@/utils/date";

export function useNotice(tableRef: any) {
  const form = reactive({
    title: "",
    categoryId: null,
    type: null,
    status: null,
  });

  const formRef = ref();
  const loading = ref(true);
  const dataList = ref([]);
  const categoryOptions = ref([]);
  const selectedNum = ref(0);
  const pagination = reactive({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true,
  });

  const columns: TableColumnList = [
    {
      label: "通知标题",
      prop: "title",
      minWidth: 200,
      showOverflowTooltip: true,
    },
    {
      label: "分类",
      prop: "categoryName",
      minWidth: 100,
    },
    {
      label: "类型",
      prop: "type",
      minWidth: 100,
      cellRenderer: ({ row }) => {
        const typeMap = { 1: "普通", 2: "重要", 3: "紧急" };
        const typeColorMap = { 1: "", 2: "warning", 3: "danger" };
        return (
          <el-tag type={typeColorMap[row.type]}>
            {typeMap[row.type] || "未知"}
          </el-tag>
        );
      },
    },
    {
      label: "发布人",
      prop: "publisherName",
      minWidth: 100,
    },
    {
      label: "状态",
      prop: "status",
      minWidth: 100,
      cellRenderer: ({ row }) => {
        const statusMap = { 0: "草稿", 1: "已发布", 2: "已撤回" };
        const statusColorMap = { 0: "info", 1: "success", 2: "warning" };
        return (
          <el-tag type={statusColorMap[row.status]}>
            {statusMap[row.status] || "未知"}
          </el-tag>
        );
      },
    },
    {
      label: "查看次数",
      prop: "viewCount",
      minWidth: 100,
    },
    {
      label: "已读/未读",
      prop: "readCount",
      minWidth: 120,
      cellRenderer: ({ row }) => (
        <span>
          {row.readCount || 0} / {row.unreadCount || 0}
        </span>
      ),
    },
    {
      label: "发布时间",
      prop: "publishTime",
      minWidth: 180,
      formatter: ({ publishTime }) => formatDateTime(publishTime),
    },
    {
      label: "操作",
      fixed: "right",
      width: 250,
      slot: "operation",
    },
  ];

  // 加载分类选项
  function loadCategoryOptions() {
    getNoticeCategoryList({ current: 1, size: 1000, status: 1 })
      .then((response: any) => {
        if (response?.data?.code === 200 && response?.data?.data) {
          categoryOptions.value = response.data.data.records || [];
        }
      })
      .catch(() => {
        categoryOptions.value = [];
      });
  }

  function onSearch() {
    loading.value = true;
    getNoticeList({
      current: pagination.currentPage,
      size: pagination.pageSize,
      title: form.title || undefined,
      categoryId: form.categoryId || undefined,
      type: form.type || undefined,
      status: form.status !== null ? form.status : undefined,
    })
      .then((response: any) => {
        // 处理响应格式：可能是 { data: { code: 200, data: {...} } } 或 { code: 200, data: {...} }
        const responseData = response?.data || response;
        if (responseData?.code === 200 && responseData?.data) {
          dataList.value = responseData.data.records || [];
          pagination.total = responseData.data.total || 0;
          pagination.pageSize = responseData.data.size || 10;
          pagination.currentPage = responseData.data.current || 1;
        } else if (responseData?.records) {
          // 如果直接返回分页数据
          dataList.value = responseData.records || [];
          pagination.total = responseData.total || 0;
          pagination.pageSize = responseData.size || 10;
          pagination.currentPage = responseData.current || 1;
        } else {
          dataList.value = [];
          pagination.total = 0;
        }
        console.log("通知列表数据:", dataList.value, "总数:", pagination.total);
      })
      .catch((error: any) => {
        console.error("获取通知列表失败", error);
        dataList.value = [];
        pagination.total = 0;
      })
      .finally(() => {
        loading.value = false;
      });
  }

  function resetForm() {
    form.title = "";
    form.categoryId = null;
    form.type = null;
    form.status = null;
    onSearch();
  }

  async function openDialog(title = "新增", row?: any) {
    // 如果是修改，需要先获取详情（包含发布范围信息）
    let noticeDetail = null;
    if (title === "修改" && row?.id) {
      try {
        const response = await getNoticeDetail(row.id);
        console.log("获取通知详情原始响应:", response);

        // http.request 在响应拦截器中返回 response.data，即 { code, message, data }
        // 所以 response 本身就是 { code, message, data } 格式
        if (response?.code === 200 && response?.data) {
          noticeDetail = response.data;
          console.log("通知详情数据:", noticeDetail);
          console.log("通知内容字段值:", noticeDetail.content);
          console.log("通知内容类型:", typeof noticeDetail.content);
        } else {
          console.error("通知详情响应格式异常:", response);
          message("获取通知详情失败：响应格式异常", { type: "error" });
          return;
        }
      } catch (error) {
        console.error("获取通知详情失败:", error);
        message("获取通知详情失败", { type: "error" });
        return;
      }
    }

    const formInlineData = {
      id: noticeDetail?.id || row?.id || undefined,
      title: noticeDetail?.title || row?.title || "",
      content: noticeDetail?.content ?? row?.content ?? "", // 使用 ?? 确保空字符串也能显示
      categoryId: noticeDetail?.categoryId || row?.categoryId || null,
      type: noticeDetail?.type ?? row?.type ?? 1,
      publishType: noticeDetail?.publishType ?? row?.publishType ?? 1,
      departmentIds: noticeDetail?.departmentIds || row?.departmentIds || [],
      roleIds: noticeDetail?.roleIds || row?.roleIds || [],
      userIds: noticeDetail?.userIds || row?.userIds || [],
      expireTime: noticeDetail?.expireTime || row?.expireTime || null,
      sortOrder: noticeDetail?.sortOrder ?? row?.sortOrder ?? 0,
    };

    console.log("准备传递给表单的数据:", formInlineData);
    console.log("表单内容字段值:", formInlineData.content);
    console.log("表单内容字段类型:", typeof formInlineData.content);

    addDialog({
      title: `${title}通知`,
      props: {
        formInline: formInlineData,
      },
      width: "800px",
      draggable: true,
      fullscreen: false,
      fullscreenIcon: true,
      closeOnClickModal: false,
      contentRenderer: () =>
        h(noticeForm, {
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
              const requestData = {
                title: curData.title,
                content: curData.content,
                categoryId: curData.categoryId || null,
                type: curData.type,
                publishType: curData.publishType,
                departmentIds:
                  curData.publishType === 2 ? curData.departmentIds : undefined,
                roleIds:
                  curData.publishType === 3 ? curData.roleIds : undefined,
                userIds:
                  curData.publishType === 4 ? curData.userIds : undefined,
                expireTime: curData.expireTime || null,
                sortOrder: curData.sortOrder || 0,
              };

              if (title === "新增") {
                const response = await createNotice(requestData);
                if (response?.code === 200 || response?.data?.code === 200) {
                  message(`成功新增通知：${curData.title}`, {
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
                const response = await updateNotice(curData.id, requestData);
                if (response?.code === 200 || response?.data?.code === 200) {
                  message(`成功修改通知：${curData.title}`, {
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

  function handlePublish(row: any) {
    ElMessageBox.confirm(`确定要发布通知"${row.title}"吗？`, "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(async () => {
        try {
          await publishNotice(row.id);
          message("发布成功", { type: "success" });
          onSearch();
        } catch (error: any) {
          message(error?.message || "发布失败", { type: "error" });
        }
      })
      .catch(() => {});
  }

  function handleWithdraw(row: any) {
    ElMessageBox.confirm(`确定要撤回通知"${row.title}"吗？`, "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(async () => {
        try {
          await withdrawNotice(row.id);
          message("撤回成功", { type: "success" });
          onSearch();
        } catch (error: any) {
          message(error?.message || "撤回失败", { type: "error" });
        }
      })
      .catch(() => {});
  }

  function handleDelete(row: any) {
    ElMessageBox.confirm(`确定要删除通知"${row.title}"吗？`, "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(async () => {
        try {
          await deleteNotice(row.id);
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
    loadCategoryOptions();
    onSearch();
  });

  return {
    form,
    loading,
    columns,
    dataList,
    categoryOptions,
    selectedNum,
    pagination,
    onSearch,
    resetForm,
    openDialog,
    handlePublish,
    handleWithdraw,
    handleDelete,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
  };
}
