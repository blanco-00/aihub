import dayjs from "dayjs";
import { message } from "@/utils/message";
import { getFileList, deleteFile, downloadFile } from "@/api/system";
import { reactive, ref, onMounted } from "vue";
import { ElMessageBox } from "element-plus";
import type { FileListParams, FileInfo } from "./types";
import { cloneDeep } from "@pureadmin/utils";

export function useFile() {
  const form = reactive<FileListParams>({
    category: undefined,
    keyword: undefined,
    current: 1,
    size: 10,
  });

  const formRef = ref();
  const tableRef = ref();
  const dataList = ref<FileInfo[]>([]);
  const loading = ref(true);
  const selectedNum = ref(0);

  const pagination = reactive({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true,
  });

  const columns: TableColumnList = [
    {
      label: "文件名",
      prop: "filename",
      minWidth: 200,
      align: "left",
      showOverflowTooltip: true,
    },
    {
      label: "文件类型",
      prop: "contentType",
      minWidth: 120,
    },
    {
      label: "文件分类",
      prop: "category",
      minWidth: 100,
    },
    {
      label: "文件大小",
      prop: "size",
      minWidth: 100,
      formatter: ({ size }) => {
        if (size < 1024) {
          return size + " B";
        } else if (size < 1024 * 1024) {
          return (size / 1024).toFixed(2) + " KB";
        } else {
          return (size / 1024 / 1024).toFixed(2) + " MB";
        }
      },
    },
    {
      label: "上传用户",
      prop: "uploadUsername",
      minWidth: 120,
    },
    {
      label: "引用次数",
      prop: "referenceCount",
      minWidth: 100,
    },
    {
      label: "上传时间",
      prop: "uploadTime",
      minWidth: 180,
      formatter: ({ uploadTime }) =>
        uploadTime ? dayjs(uploadTime).format("YYYY-MM-DD HH:mm:ss") : "",
    },
    {
      label: "操作",
      fixed: "right",
      width: 200,
      slot: "operation",
    },
  ];

  function handleSelectionChange(val: FileInfo[]) {
    selectedNum.value = val.length;
  }

  function resetForm(formEl: any) {
    if (!formEl) return;
    formEl.resetFields();
    form.category = undefined;
    form.keyword = undefined;
    form.current = 1;
    pagination.currentPage = 1;
    onSearch();
  }

  async function onSearch() {
    loading.value = true;
    try {
      const params: FileListParams = {
        category: form.category || undefined,
        keyword: form.keyword || undefined,
        current: pagination.currentPage,
        size: pagination.pageSize,
      };

      const { code, data } = await getFileList(params);
      if (code === 200 && data) {
        dataList.value = data.records || [];
        pagination.total = data.total || 0;
        pagination.pageSize = data.size || 10;
        pagination.currentPage = data.current || 1;
      } else {
        dataList.value = [];
        pagination.total = 0;
      }
    } catch (error: any) {
      console.error("[文件列表查询] 请求失败", error);
      message("获取文件列表失败", { type: "error" });
      dataList.value = [];
      pagination.total = 0;
    } finally {
      loading.value = false;
    }
  }

  function handleSizeChange(size: number) {
    pagination.pageSize = size;
    pagination.currentPage = 1;
    form.current = 1;
    onSearch();
  }

  function handleCurrentChange(page: number) {
    pagination.currentPage = page;
    form.current = page;
    onSearch();
  }

  function onSelectionCancel() {
    tableRef.value.getTableRef().clearSelection();
  }

  async function handleDelete(row: FileInfo) {
    try {
      await ElMessageBox.confirm(
        `确定要删除文件 "${row.filename}" 吗？`,
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        },
      );

      const { code } = await deleteFile(row.url);
      if (code === 200) {
        message("删除成功", { type: "success" });
        onSearch();
      } else {
        message("删除失败", { type: "error" });
      }
    } catch (error: any) {
      if (error !== "cancel") {
        console.error("删除文件失败", error);
        message("删除失败", { type: "error" });
      }
    }
  }

  async function handleDownload(row: FileInfo) {
    try {
      const blob = await downloadFile(row.url);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = row.filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      message("下载成功", { type: "success" });
    } catch (error: any) {
      console.error("下载文件失败", error);
      message("下载失败", { type: "error" });
    }
  }

  function handlePreview(row: FileInfo) {
    // 如果是图片，直接打开预览
    if (row.contentType?.startsWith("image/")) {
      window.open(row.url, "_blank");
    } else {
      // 其他文件类型，打开新窗口
      window.open(row.url, "_blank");
    }
  }

  onMounted(() => {
    onSearch();
  });

  return {
    form,
    formRef,
    tableRef,
    loading,
    columns,
    dataList,
    selectedNum,
    pagination,
    onSearch,
    resetForm,
    handleDelete,
    handleDownload,
    handlePreview,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
    onSelectionCancel,
  };
}
