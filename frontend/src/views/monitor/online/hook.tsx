import dayjs from "dayjs";
import { message } from "@/utils/message";
import { getOnlineLogsList, forceOfflineUser } from "@/api/system";
import { reactive, ref, onMounted, toRaw } from "vue";
import type { PaginationProps } from "@pureadmin/table";

export function useRole() {
  const form = reactive({
    username: ""
  });
  const dataList = ref([]);
  const loading = ref(true);
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });
  const columns: TableColumnList = [
    {
      label: "用户名",
      prop: "username",
      minWidth: 100
    },
    {
      label: "登录 IP",
      prop: "ip",
      minWidth: 140
    },
    {
      label: "登录地点",
      prop: "address",
      minWidth: 140
    },
    {
      label: "操作系统",
      prop: "system",
      minWidth: 100
    },
    {
      label: "浏览器类型",
      prop: "browser",
      minWidth: 100
    },
    {
      label: "登录时间",
      prop: "loginTime",
      minWidth: 180,
      formatter: ({ loginTime }) =>
        dayjs(loginTime).format("YYYY-MM-DD HH:mm:ss")
    },
    {
      label: "操作",
      fixed: "right",
      slot: "operation"
    }
  ];

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
    // 选择变化处理
  }

  async function handleOffline(row) {
    try {
      const { code } = await forceOfflineUser(row.userId);
      if (code === 200) {
        message(`${row.username}已被强制下线`, { type: "success" });
        onSearch();
      } else {
        message("强制下线失败", { type: "error" });
      }
    } catch (error: any) {
      console.error("强制下线失败", error);
      message("强制下线失败", { type: "error" });
    }
  }

  async function onSearch() {
    loading.value = true;
    try {
      const { code, data } = await getOnlineLogsList(toRaw(form));
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
      console.error("获取在线用户列表失败", error);
      message("获取在线用户列表失败", { type: "error" });
      dataList.value = [];
      pagination.total = 0;
    } finally {
      loading.value = false;
    }
  }

  const resetForm = formEl => {
    if (!formEl) return;
    formEl.resetFields();
    onSearch();
  };

  onMounted(() => {
    onSearch();
  });

  return {
    form,
    loading,
    columns,
    dataList,
    pagination,
    onSearch,
    resetForm,
    handleOffline,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange
  };
}
